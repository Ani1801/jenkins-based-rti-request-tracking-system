package com.college.rti.selenium;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.io.File;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-End Selenium WebDriver test suite for the Jenkins-Based RTI Request Tracking System.
 * Tests Citizen submission, validation, public status inquiry, PII privacy boundaries,
 * and Reviewer queue inspection, state machine triage, and lifecycle transitions.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RtiWorkflowSeleniumE2ETest {

    @LocalServerPort
    private int port;

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--window-size=1920,1080");

        File winChrome = new File("C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe");
        if (winChrome.exists()) {
            options.setBinary(winChrome);
        }

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception ignored) {
            }
        }
    }

    private String getBaseUrl() {
        return "http://localhost:" + port;
    }

    @Test
    @DisplayName("Citizen Workflow: Open submission page and verify required form fields & validation")
    void testCitizenSubmissionFormValidation() {
        driver.get(getBaseUrl() + "/index.html");

        WebElement nameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("applicantName")));
        WebElement emailInput = driver.findElement(By.id("applicantEmail"));
        WebElement phoneInput = driver.findElement(By.id("applicantPhone"));
        WebElement categorySelect = driver.findElement(By.id("category"));
        WebElement descriptionInput = driver.findElement(By.id("description"));
        WebElement submitBtn = driver.findElement(By.id("submitBtn"));

        assertThat(nameInput.isDisplayed()).isTrue();
        assertThat(emailInput.isDisplayed()).isTrue();
        assertThat(phoneInput.isDisplayed()).isTrue();
        assertThat(categorySelect.isDisplayed()).isTrue();
        assertThat(descriptionInput.isDisplayed()).isTrue();
        assertThat(submitBtn.isDisplayed()).isTrue();

        // Submit blank form to trigger validation
        submitBtn.click();

        WebElement errorAlert = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("validationErrors")));
        assertThat(errorAlert.isDisplayed()).isTrue();

        WebElement errorMessage = driver.findElement(By.id("errorMessage"));
        assertThat(errorMessage.getText()).contains("Applicant name is required");
    }

    @Test
    @DisplayName("Citizen Workflow: Submit valid RTI request and verify tracking ID display")
    void testCitizenRequestSubmissionAndTrackingIdDisplay() {
        driver.get(getBaseUrl() + "/index.html");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("applicantName"))).sendKeys("Devendra Joshi");
        driver.findElement(By.id("applicantEmail")).sendKeys("devendra.joshi@example.com");
        driver.findElement(By.id("applicantPhone")).sendKeys("9876543210");
        new Select(driver.findElement(By.id("category"))).selectByValue("ACADEMIC");
        driver.findElement(By.id("description")).sendKeys("Requesting certified copies of the 2025 semester grade cards and examination ledger.");

        driver.findElement(By.id("submitBtn")).click();

        WebElement successAlert = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("submissionSuccess")));
        assertThat(successAlert.isDisplayed()).isTrue();

        WebElement trackingIdElem = driver.findElement(By.id("trackingIdDisplay"));
        String trackingId = trackingIdElem.getText().trim();

        assertThat(trackingId).matches("^RTI-\\d{4}-[A-Z0-9]+$");

        WebElement trackLink = driver.findElement(By.id("trackRequestLink"));
        assertThat(trackLink.isDisplayed()).isTrue();
        assertThat(trackLink.getAttribute("href")).contains(trackingId);
    }

    @Test
    @DisplayName("Citizen Workflow: Public status lookup, timeline verification, and PII protection")
    void testCitizenPublicTrackingAndPiiOmission() {
        // Step 1: Submit a request via the citizen portal
        driver.get(getBaseUrl() + "/index.html");

        String applicantName = "Pooja Hegde";
        String applicantEmail = "pooja.hegde@example.com";
        String applicantPhone = "9123456780";

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("applicantName"))).sendKeys(applicantName);
        driver.findElement(By.id("applicantEmail")).sendKeys(applicantEmail);
        driver.findElement(By.id("applicantPhone")).sendKeys(applicantPhone);
        new Select(driver.findElement(By.id("category"))).selectByValue("INFRASTRUCTURE");
        driver.findElement(By.id("description")).sendKeys("Inquiry regarding laboratory computer allocation and software license audit.");

        driver.findElement(By.id("submitBtn")).click();

        WebElement trackingElem = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("trackingIdDisplay")));
        String trackingId = trackingElem.getText().trim();
        assertThat(trackingId).isNotEmpty();

        // Step 2: Open tracking page
        driver.get(getBaseUrl() + "/track.html");

        WebElement tokenInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("trackingIdInput")));
        tokenInput.sendKeys(trackingId);
        driver.findElement(By.id("trackBtn")).click();

        // Step 3: Verify displayed status and audit timeline
        WebElement resultCard = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("trackResult")));
        assertThat(resultCard.isDisplayed()).isTrue();

        WebElement resultTrackingId = driver.findElement(By.id("resultTrackingId"));
        assertThat(resultTrackingId.getText().trim()).isEqualTo(trackingId);

        WebElement categoryDisplay = driver.findElement(By.id("categoryDisplay"));
        assertThat(categoryDisplay.getText().trim()).isEqualTo("INFRASTRUCTURE");

        WebElement statusDisplay = driver.findElement(By.id("statusDisplay"));
        assertThat(statusDisplay.getText().trim()).isEqualTo("SUBMITTED");

        List<WebElement> timelineItems = driver.findElements(By.className("history-item"));
        assertThat(timelineItems).isNotEmpty();
        assertThat(timelineItems.get(0).getText()).contains("SUBMITTED");

        // Step 4: Confirm Applicant PII is NOT displayed anywhere on the public tracking page
        String pageSource = driver.getPageSource();
        assertThat(pageSource).doesNotContain(applicantName);
        assertThat(pageSource).doesNotContain(applicantEmail);
        assertThat(pageSource).doesNotContain(applicantPhone);

        // Verify PII DOM IDs do not exist on track.html
        assertThat(driver.findElements(By.id("applicantName"))).isEmpty();
        assertThat(driver.findElements(By.id("applicantEmail"))).isEmpty();
        assertThat(driver.findElements(By.id("applicantPhone"))).isEmpty();
    }

    @Test
    @DisplayName("Citizen Workflow: Graceful handling of invalid or non-existent tracking IDs")
    void testCitizenTrackingInvalidIdGracefulHandling() {
        driver.get(getBaseUrl() + "/track.html");

        WebElement tokenInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("trackingIdInput")));
        tokenInput.sendKeys("RTI-2026-NONEXISTENT");
        driver.findElement(By.id("trackBtn")).click();

        WebElement errorAlert = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("trackError")));
        assertThat(errorAlert.isDisplayed()).isTrue();

        WebElement errorMsg = driver.findElement(By.id("trackErrorMessage"));
        assertThat(errorMsg.getText()).contains("No RTI request found matching tracking identifier");

        // Ensure result card remains hidden
        WebElement resultCard = driver.findElement(By.id("trackResult"));
        assertThat(resultCard.getAttribute("class")).contains("hidden");
    }

    @Test
    @DisplayName("Reviewer Workflow: Queue inspection, state machine triage, decision execution, and invalid transition rejection")
    void testReviewerWorkflowAndStateTransitions() {
        // Step 1: Create a request via UI
        driver.get(getBaseUrl() + "/index.html");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("applicantName"))).sendKeys("Karan Verma");
        driver.findElement(By.id("applicantEmail")).sendKeys("karan.verma@example.com");
        driver.findElement(By.id("applicantPhone")).sendKeys("9812345678");
        new Select(driver.findElement(By.id("category"))).selectByValue("FINANCIAL");
        driver.findElement(By.id("description")).sendKeys("Requesting statement of university scholarship distribution for academic year 2025.");
        driver.findElement(By.id("submitBtn")).click();

        WebElement trackingElem = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("trackingIdDisplay")));
        String trackingId = trackingElem.getText().trim();

        // Step 2: Open reviewer queue
        driver.get(getBaseUrl() + "/reviewer.html");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("reviewerQueueTable")));

        // Wait until queue contains the new request row
        WebElement queueBody = wait.until(driver -> {
            WebElement body = driver.findElement(By.id("reviewerQueueBody"));
            return body.getText().contains(trackingId) ? body : null;
        });
        assertThat(queueBody.getText()).contains(trackingId);

        // Find and click the 'Inspect & Review' button for this request row
        WebElement inspectBtn = queueBody.findElement(By.xpath(".//tr[td[contains(text(), '" + trackingId + "')]]//button"));
        inspectBtn.click();

        // Step 3: Verify details panel opens with applicant metadata
        WebElement decisionPanel = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("decisionPanel")));
        assertThat(decisionPanel.isDisplayed()).isTrue();

        assertThat(driver.findElement(By.id("selectedTrackingId")).getText()).contains(trackingId);
        assertThat(driver.findElement(By.id("selectedApplicantName")).getText()).isEqualTo("Karan Verma");
        assertThat(driver.findElement(By.id("selectedCurrentStatus")).getText()).isEqualTo("SUBMITTED");

        // Step 4: Verify invalid state transition is rejected when UI exposes it (SUBMITTED -> APPROVED)
        new Select(driver.findElement(By.id("targetStatusSelect"))).selectByValue("APPROVED");
        WebElement remarksInput = driver.findElement(By.id("remarksInput"));
        remarksInput.clear();
        remarksInput.sendKeys("Attempting direct approval without mandatory triage.");

        driver.findElement(By.id("confirmDecisionBtn")).click();

        WebElement reviewerAlert = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("reviewerAlert")));
        assertThat(reviewerAlert.getAttribute("class")).contains("alert-error");
        assertThat(driver.findElement(By.id("alertTitle")).getText()).isEqualTo("Transition Rejected");
        assertThat(driver.findElement(By.id("alertMessage")).getText()).contains("Invalid state transition from SUBMITTED to APPROVED");

        // Status must remain SUBMITTED
        assertThat(driver.findElement(By.id("selectedCurrentStatus")).getText()).isEqualTo("SUBMITTED");

        // Step 5: Valid transition: SUBMITTED -> UNDER_REVIEW
        new Select(driver.findElement(By.id("targetStatusSelect"))).selectByValue("UNDER_REVIEW");
        remarksInput.clear();
        remarksInput.sendKeys("Application triaged and forwarded to finance department for document audit.");

        driver.findElement(By.id("confirmDecisionBtn")).click();

        wait.until(ExpectedConditions.attributeContains(By.id("reviewerAlert"), "class", "alert-success"));
        assertThat(driver.findElement(By.id("alertTitle")).getText()).isEqualTo("Transition Committed");
        assertThat(driver.findElement(By.id("selectedCurrentStatus")).getText()).isEqualTo("UNDER_REVIEW");

        // Step 6: Valid transition: UNDER_REVIEW -> APPROVED
        new Select(driver.findElement(By.id("targetStatusSelect"))).selectByValue("APPROVED");
        remarksInput.clear();
        remarksInput.sendKeys("Scholarship records verified against finance office ledger. Request approved.");

        driver.findElement(By.id("confirmDecisionBtn")).click();

        wait.until(ExpectedConditions.attributeContains(By.id("reviewerAlert"), "class", "alert-success"));

        // Step 7: Verify updated status and complete audit trail on Citizen Tracking page
        driver.get(getBaseUrl() + "/track.html?trackingId=" + trackingId);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("trackResult")));
        assertThat(driver.findElement(By.id("statusDisplay")).getText()).isEqualTo("APPROVED");

        List<WebElement> timeline = driver.findElements(By.className("history-item"));
        assertThat(timeline.size()).isGreaterThanOrEqualTo(3);

        String fullTimelineText = driver.findElement(By.id("historyTimeline")).getText();
        assertThat(fullTimelineText).contains("SUBMITTED");
        assertThat(fullTimelineText).contains("UNDER_REVIEW");
        assertThat(fullTimelineText).contains("APPROVED");
        assertThat(fullTimelineText).contains("Scholarship records verified");
    }

    @Test
    @DisplayName("Reviewer Workflow: Mandatory remarks validation prevents empty comments")
    void testReviewerMandatoryRemarksValidation() {
        // Submit request to have an active item
        driver.get(getBaseUrl() + "/index.html");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("applicantName"))).sendKeys("Sneha Patil");
        driver.findElement(By.id("applicantEmail")).sendKeys("sneha.patil@example.com");
        driver.findElement(By.id("applicantPhone")).sendKeys("9765432109");
        new Select(driver.findElement(By.id("category"))).selectByValue("ADMINISTRATIVE");
        driver.findElement(By.id("description")).sendKeys("Requesting roster of committee members for campus development board.");
        driver.findElement(By.id("submitBtn")).click();

        WebElement trackingElem = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("trackingIdDisplay")));
        String trackingId = trackingElem.getText().trim();

        // Open reviewer portal
        driver.get(getBaseUrl() + "/reviewer.html");

        WebElement queueBody = wait.until(driver -> {
            WebElement body = driver.findElement(By.id("reviewerQueueBody"));
            return body.getText().contains(trackingId) ? body : null;
        });

        WebElement inspectBtn = queueBody.findElement(By.xpath(".//tr[td[contains(text(), '" + trackingId + "')]]//button"));
        inspectBtn.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("decisionPanel")));

        // Clear remarks and submit
        driver.findElement(By.id("remarksInput")).clear();
        driver.findElement(By.id("confirmDecisionBtn")).click();

        WebElement alert = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("reviewerAlert")));
        assertThat(alert.getAttribute("class")).contains("alert-error");
        assertThat(driver.findElement(By.id("alertMessage")).getText()).contains("Decision remarks must not be blank");
    }
}
