package com.arits.datafast.automation;

import com.arits.datafast.automation.base.AutomationException;
import com.arits.datafast.automation.base.BaseAutomation;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

/**
 * Proof-of-concept automation: searches YouTube for a term and plays the first result.
 *
 * This is the POC that proves the full chain works:
 *   login → token received → browser launches → Playwright controls it
 *
 * Will be replaced by real automation classes (IssueEXPAutomation, etc.)
 * once the login/session/browser infrastructure is verified end-to-end.
 */
public class YoutubeAutomation implements BaseAutomation {

    private final String searchTerm;

    public YoutubeAutomation(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    @Override
    public void prepare() throws AutomationException {
        if (searchTerm == null || searchTerm.isBlank()) {
            throw new AutomationException("Search term must not be empty.");
        }
    }

    @Override
    public void execute(Page page) throws AutomationException {
        try {
            page.navigate("https://www.youtube.com");
            page.waitForLoadState();
            sleep(3000);

            dismissCookieBanner(page);
            searchFor(page, searchTerm);
            playFirstResult(page);

            sleep(60000);

        } catch (AutomationException e) {
            throw e;
        } catch (Exception e) {
            throw new AutomationException(
                    "YouTube automation failed: " + e.getMessage(), e);
        }
    }

    // --- Private steps ---

    private void dismissCookieBanner(Page page) {
        String[] cookieSelectors = {
                "button[aria-label='Accept the use of cookies and other data for the purposes described']",
                "button[aria-label='Accept all']",
                "ytd-button-renderer:has-text('Accept all')",
                "button:has-text('Accept all')",
                "button:has-text('I agree')"
        };
        for (String selector : cookieSelectors) {
            try {
                if (page.locator(selector).first().isVisible()) {
                    page.locator(selector).first().click();
                    sleep(2000);
                    break;
                }
            } catch (Exception ignored) {}
        }
    }

    private void searchFor(Page page, String term) throws AutomationException {
        try {
            page.keyboard().press("/");
            sleep(1000);

            page.waitForSelector(
                    "[name='search_query']",
                    new Page.WaitForSelectorOptions()
                            .setState(WaitForSelectorState.ATTACHED)
                            .setTimeout(5000)
            );

            var searchBox = page.locator("input[name='search_query']").first();
            searchBox.focus();
            searchBox.click(new com.microsoft.playwright.Locator.ClickOptions().setForce(true));
            sleep(300);

            searchBox.fill("");
            searchBox.pressSequentially(term,
                    new com.microsoft.playwright.Locator.PressSequentiallyOptions().setDelay(150));
            sleep(500);

            page.keyboard().press("Enter");

            page.waitForSelector(
                    "[id='video-title']",
                    new Page.WaitForSelectorOptions()
                            .setState(WaitForSelectorState.ATTACHED)
            );
            sleep(1500);

        } catch (Exception e) {
            throw new AutomationException("Failed to search for: " + term, e);
        }
    }

    private void playFirstResult(Page page) throws AutomationException {
        try {
            page.locator("ytd-video-renderer #video-title")
                    .first()
                    .click(new com.microsoft.playwright.Locator.ClickOptions().setForce(true));
        } catch (Exception e) {
            throw new AutomationException("Failed to click first video result.", e);
        }
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
