package com.arits.datafast.browser;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


public class BrowserManager {
    private static final String BUNDLED_CHROMIUM_LINUX =
            System.getProperty("user.home")
                    + "/.cache/ms-playwright/chromium-1223/chrome-linux/chrome";
    private static final String BUNDLED_CHROMIUM_MAC =
            System.getProperty("user.home")
                    + "/Library/Caches/ms-playwright/chromium-1223/"
                    + "chrome-mac/Chromium.app/Contents/MacOS/Chromium";
    private static final String BUNDLED_CHROMIUM_WIN =
            System.getProperty("user.home")
                    + "\\AppData\\Local\\ms-playwright\\chromium-1223\\chrome-win\\chrome.exe";
    private static final Path TEMP_PROFILE_DIR =
            Paths.get(System.getProperty("java.io.tmpdir"), "datafast-chrome-profile");

    public BrowserManager() {
        // or maybe private BrowserManager() {
    }

    public static BrowserSession launch(String startUrl) {
        String executablePath = resolveExecutablePath();
        System.out.println("[BrowserManager] Using browser executable: " + executablePath);

        Playwright playwright = Playwright.create(
                new Playwright.CreateOptions().setEnv(
                        java.util.Map.of("PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD", "1")
                )
        );

        try {
            List<String> args = new ArrayList<>();

            args.add("--app=" + startUrl);
            args.add("--no-first-run");
            args.add("--no-default-browser-check");
            args.add("--disable-extensions");
            args.add("--disable-infobars");

            // Forces native OS Window framework to launch fully maximized across screens
            args.add("--start-maximized");

            BrowserContext context = playwright.chromium().launchPersistentContext(
                    TEMP_PROFILE_DIR,
                    new BrowserType.LaunchPersistentContextOptions()
                            .setExecutablePath(Paths.get(executablePath))
                            .setHeadless(false)
                            .setArgs(args)
                            // Passing null drops Playwright's static viewport resolution boundary block.
                            // The web canvas layout scales perfectly to fill 100% of the maximized window size.
                            .setViewportSize(null)
            );

            Page page = context.pages().isEmpty()
                    ? context.newPage()
                    : context.pages().get(0);

            page.navigate(startUrl);

            return new BrowserSession(playwright, context, page);

        } catch (Exception e) {
            playwright.close();
            throw new RuntimeException("[BrowserManager] Failed to launch browser: "
                    + e.getMessage(), e);
        }
    }

    private static String resolveExecutablePath() {
        List<String> candidates = new ArrayList<>();
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("linux")) {
            candidates.add("/usr/bin/google-chrome");
            candidates.add("/usr/bin/google-chrome-stable");
            candidates.add("/usr/bin/chromium-browser");
            candidates.add("/usr/bin/chromium");
            candidates.add("/usr/bin/microsoft-edge");
            candidates.add("/usr/bin/microsoft-edge-stable");
            candidates.add(BUNDLED_CHROMIUM_LINUX);

        } else if (os.contains("mac")) {
            candidates.add("/Applications/Google Chrome.app/Contents/MacOS/Google Chrome");
            candidates.add("/Applications/Microsoft Edge.app/Contents/MacOS/Microsoft Edge");
            candidates.add(BUNDLED_CHROMIUM_MAC);

        } else if (os.contains("win")) {
            candidates.add("C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe");
            candidates.add("C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe");
            candidates.add("C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe");
            candidates.add("C:\\Program Files\\Microsoft\\Edge\\Application\\msedge.exe");
            candidates.add(BUNDLED_CHROMIUM_WIN);
        }

        for (String candidate : candidates) {
            if (Files.isExecutable(Path.of(candidate))) {
                return candidate;
            }
        }

        throw new RuntimeException(
                "[BrowserManager] No usable browser found. Checked: " + candidates
                        + "\nInstall Chrome/Edge or run: "
                        + "mvn exec:java -Dexec.mainClass=com.microsoft.playwright.CLI "
                        + "-Dexec.args=\"install chromium\" -Dexec.classpathScope=compile"
        );
    }

    public static class BrowserSession implements AutoCloseable {
        private final Playwright playwright;
        private final BrowserContext context;
        private final Page page;

        private BrowserSession(Playwright playwright, BrowserContext context, Page page) {
            this.playwright = playwright;
            this.context = context;
            this.page = page;
        }

        public Page getPage() {
            return page;
        }

        @Override
        public void close() {
            try {
                page.close();
            } catch (Exception ignored) {
            }
            try {
                context.close();
            } catch (Exception ignored) {
            }
            try {
                playwright.close();
            } catch (Exception ignored) {
            }
        }
    }
}