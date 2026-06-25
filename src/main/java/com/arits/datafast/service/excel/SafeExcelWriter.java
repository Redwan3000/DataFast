package com.arits.datafast.service.excel;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Write-safety wrapper for the automation run writing results back into
 * the user's Excel file.
 * <p>
 * Sequence: copy original -> temp file, write into the temp file as the
 * automation progresses, then on success atomically replace the original
 * with the temp file and delete the temp file. On failure, just delete
 * the temp file — the original is never touched.
 * <p>
 * This protects the source file if it's open elsewhere, edited, or
 * deleted mid-run, and avoids leaving scratch workbooks lying around —
 * worth being strict about given these files carry sensitive account data.
 */
public class SafeExcelWriter implements Closeable {

    private static final Logger log = LoggerFactory.getLogger(SafeExcelWriter.class);

    private final File originalFile;
    private final Path tempFile;
    private final Workbook workbook;
    private boolean settled = false; // true once commit/discard has run

    private SafeExcelWriter(File originalFile, Path tempFile, Workbook workbook) {
        this.originalFile = originalFile;
        this.tempFile = tempFile;
        this.workbook = workbook;
    }

    public static SafeExcelWriter open(File originalFile) throws IOException {
        Path tempFile = Files.createTempFile("datafast_", "_" + originalFile.getName());
        Files.copy(originalFile.toPath(), tempFile, StandardCopyOption.REPLACE_EXISTING);

        Workbook workbook;
        try (FileInputStream fis = new FileInputStream(tempFile.toFile())) {
            workbook = WorkbookFactory.create(fis);
        }

        log.info("[SafeExcelWriter] Opened temp copy '{}' for '{}'", tempFile, originalFile.getName());
        return new SafeExcelWriter(originalFile, tempFile, workbook);
    }

    public Workbook workbook() {
        return workbook;
    }

    /**
     * Flushes current in-memory changes to the temp file. Call per-row or in batches.
     */
    public void flush() throws IOException {
        try (FileOutputStream fos = new FileOutputStream(tempFile.toFile())) {
            workbook.write(fos);
        }
    }

    /**
     * Final step on success: flush, close, atomically swap into place, clean up.
     */
    public void commitAndCleanUp() throws IOException {
        flush();
        workbook.close();

        try {
            Files.move(tempFile, originalFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            log.info("[SafeExcelWriter] Committed results to '{}'", originalFile.getName());
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(tempFile, originalFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            log.warn("[SafeExcelWriter] Atomic move unsupported on this filesystem, used regular move for '{}'",
                    originalFile.getName());
        } finally {
            settled = true;
        }
    }

    /**
     * Call on failure — original file is left untouched, temp file is deleted.
     */
    public void discardAndCleanUp() {
        try {
            workbook.close();
        } catch (IOException ignored) {
        }
        cleanupTempFile();
        settled = true;
    }

    private void cleanupTempFile() {
        try {
            Files.deleteIfExists(tempFile);
        } catch (IOException e) {
            log.warn("[SafeExcelWriter] Could not delete temp file '{}': {}", tempFile, e.getMessage());
        }
    }

    /**
     * Safety net if the caller forgets to commit/discard explicitly — never overwrites silently here.
     */
    @Override
    public void close() throws IOException {
        if (!settled) {
            log.warn("[SafeExcelWriter] Closed without commit/discard — discarding temp file as a precaution.");
            discardAndCleanUp();
        }
    }
}