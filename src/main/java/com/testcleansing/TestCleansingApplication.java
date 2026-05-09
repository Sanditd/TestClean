package com.testcleansing;

import com.testcleansing.model.TestCase;
import com.testcleansing.model.TestCoverageScore;
import com.testcleansing.model.SimilarityResult;
import com.testcleansing.service.CleansingEngine;
import java.util.*;

public class TestCleansingApplication {

    private static List<TestCase> testCases = new ArrayList<>();
    private static Scanner scanner = new Scanner(System.in);
    private static CleansingEngine engine = new CleansingEngine();

    public static void main(String[] args) {
        // Load initial sample test cases
        loadSampleTestCases();

        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║        TEST CASE CLEANSING & INTELLIGENCE SYSTEM          ║");
        System.out.println("║                    Version 1.0.0                          ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");

        while (true) {
            displayMainMenu();
            int choice = getIntInput("Enter your choice: ");

            switch (choice) {
                case 1:
                    runCleansingEngine();
                    break;
                case 2:
                    addNewTestCase();
                    break;
                case 3:
                    viewAllTestCases();
                    break;
                case 5:
                    System.out.println("\n👋 Thank you for using Test Cleansing System!");
                    System.out.println("Exiting...\n");
                    System.exit(0);
                    break;
                default:
                    System.out.println("❌ Invalid choice! Please enter 1-5");
            }
        }
    }

    // ========== DISPLAY MENU ==========

    private static void displayMainMenu() {
        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║                         MAIN MENU                         ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.println("║                                                            ║");
        System.out.println("║  ┌────────────────────────────────────────────────────┐   ║");
        System.out.println("║  │  1. 🚀 RUN CLEANSING ENGINE                        │   ║");
        System.out.println("║  │  2. ➕ ADD NEW TEST CASE                           │   ║");
        System.out.println("║  │  3. 📋 VIEW ALL TEST CASES                         │   ║");
        System.out.println("║  │  5. 🚪 EXIT                                        │   ║");
        System.out.println("║  └────────────────────────────────────────────────────┘   ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.print("\n👉 ");
    }

    // ========== OPTION 1: RUN CLEANSING ENGINE ==========

    private static void runCleansingEngine() {
        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║                    RUNNING CLEANSING ENGINE                ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");

        if (testCases.isEmpty()) {
            System.out.println("\n⚠️  No test cases found! Please add test cases first.");
            waitForEnter();
            return;
        }

        System.out.println("\n📊 Processing " + testCases.size() + " test cases...\n");

        // Run the cleansing engine
        CleansingEngine.CleansingReport report = engine.runCleansing(testCases);

        // Display detailed results

        waitForEnter();
    }

    // ========== OPTION 2: ADD NEW TEST CASE (WITH VALIDATION) ==========

    private static void addNewTestCase() {
        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║                    ADD NEW TEST CASE                       ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");

        System.out.println("\n📝 Enter test case details (press Enter after each field):\n");

        // Get Test Case ID
        String id;
        while (true) {
            id = getStringInput("Test Case ID (e.g., TC-100): ");
            if (isUniqueId(id)) {
                break;
            }
            System.out.println("   ⚠️  ID already exists! Please use a unique ID.");
        }

        // Get Title
        String title = getStringInput("Title: ");
        while (title.trim().isEmpty()) {
            System.out.println("   ⚠️  Title cannot be empty!");
            title = getStringInput("Title: ");
        }

        // Get Description
        String description = getStringInput("Description: ");

        // Get Steps
        System.out.println("\n📌 Enter Test Steps (enter 'done' when finished):");
        List<String> steps = new ArrayList<>();
        int stepNum = 1;
        while (true) {
            String step = getStringInput("   Step " + stepNum + ": ");
            if (step.equalsIgnoreCase("done")) {
                break;
            }
            if (!step.trim().isEmpty()) {
                steps.add(step);
                stepNum++;
            }
        }

        if (steps.isEmpty()) {
            System.out.println("   ⚠️  No steps entered. Adding default step.");
            steps.add("1. Execute test case as described in the description");
        }

        // Get Expected Result
        String expectedResult = getStringInput("Expected Result: ");
        if (expectedResult.trim().isEmpty()) {
            expectedResult = "Test should pass as expected.";
        }

        // Get Priority
        System.out.println("\n📊 Priority Options:");
        System.out.println("   1. HIGH");
        System.out.println("   2. MEDIUM");
        System.out.println("   3. LOW");
        int priorityChoice = getIntInput("Choose priority (1-3): ");
        TestCase.Priority priority;
        switch (priorityChoice) {
            case 1: priority = TestCase.Priority.HIGH; break;
            case 3: priority = TestCase.Priority.LOW; break;
            default: priority = TestCase.Priority.MEDIUM;
        }

        // Get Execution Count
        int executionCount = getIntInput("Execution Count (how many times run): ");
        if (executionCount <= 0) executionCount = 1;

        // Create the test case
        TestCase newTestCase = TestCase.builder()
                .id(id)
                .title(title)
                .description(description)
                .steps(steps)
                .expectedResult(expectedResult)
                .priority(priority)
                .executionCount(executionCount)
                .build();

        // VALIDATE BEFORE ADDING
        System.out.println("\n🔍 Validating test case...");
        System.out.println("   " + "─".repeat(40));

        boolean isValid = engine.validateAndAddTestCase(newTestCase, testCases);

        if (isValid) {
            testCases.add(newTestCase);
            System.out.println("\n✅ Test case added successfully!");
            System.out.println("\n📄 Preview of added test case:");
            engine.previewStandardFormat(newTestCase);
        } else {
            System.out.println("\n❌ Test case was NOT added due to validation errors.");
            System.out.println("   Please fix the issues and try again.");
        }

        waitForEnter();
    }

    // ========== OPTION 3: VIEW ALL TEST CASES ==========

    private static void viewAllTestCases() {
        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║                   ALL TEST CASES                           ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");

        if (testCases.isEmpty()) {
            System.out.println("\n⚠️  No test cases found!");
            waitForEnter();
            return;
        }

        System.out.println("\n📋 Total: " + testCases.size() + " test case(s)\n");
        System.out.println("┌─────┬──────────────────────────────────────────────────┬──────────┬────────────┐");
        System.out.println("│ ID  │ Title                                            │ Priority │ Executions │");
        System.out.println("├─────┼──────────────────────────────────────────────────┼──────────┼────────────┤");

        for (TestCase tc : testCases) {
            System.out.printf("│ %-4s│ %-48s │ %-8s │ %-10d │%n",
                    tc.getId(),
                    truncateString(tc.getTitle(), 48),
                    tc.getPriority(),
                    tc.getExecutionCount() != null ? tc.getExecutionCount() : 0);
        }

        System.out.println("└─────┴──────────────────────────────────────────────────┴──────────┴────────────┘");

        // Option to view details of a specific test case
        System.out.print("\n🔍 View details of a test case? (Enter ID or 'no'): ");
        String choice = scanner.nextLine().trim();
        if (!choice.equalsIgnoreCase("no") && !choice.isEmpty()) {
            TestCase found = testCases.stream()
                    .filter(tc -> tc.getId().equalsIgnoreCase(choice))
                    .findFirst()
                    .orElse(null);

            if (found != null) {
                displayTestCaseDetails(found);
            } else {
                System.out.println("❌ Test case not found!");
            }
        }

        waitForEnter();
    }

    private static void displayTestCaseDetails(TestCase tc) {
        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║                    TEST CASE DETAILS                       ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.println("\n   ID: " + tc.getId());
        System.out.println("   Title: " + tc.getTitle());
        System.out.println("   Description: " + (tc.getDescription() != null && !tc.getDescription().isEmpty() ? tc.getDescription() : "N/A"));
        System.out.println("   Priority: " + tc.getPriority());
        System.out.println("   Execution Count: " + (tc.getExecutionCount() != null ? tc.getExecutionCount() : 0));

        System.out.println("\n   Steps:");
        if (tc.getSteps() != null && !tc.getSteps().isEmpty()) {
            for (String step : tc.getSteps()) {
                System.out.println("      " + step);
            }
        } else {
            System.out.println("      No steps defined");
        }

        System.out.println("\n   Expected Result:");
        System.out.println("      " + (tc.getExpectedResult() != null ? tc.getExpectedResult() : "N/A"));

        // Show quality score
        int qualityScore = engine.getQualityScore(tc);
        System.out.println("\n   Quality Score: " + qualityScore + "/100");

        // Show quality bar
        System.out.print("   Quality: [");
        int barLength = qualityScore / 5;
        for (int i = 0; i < 20; i++) {
            if (i < barLength) {
                System.out.print("█");
            } else {
                System.out.print("░");
            }
        }
        System.out.println("]");

        if (qualityScore < 70) {
            System.out.println("\n   ⚠️  Suggestions to improve quality:");
            if (tc.getTitle() == null || tc.getTitle().isEmpty()) {
                System.out.println("      • Add a descriptive title");
            }
            if (tc.getSteps() == null || tc.getSteps().isEmpty()) {
                System.out.println("      • Add test steps");
            }
            if (tc.getExpectedResult() == null || tc.getExpectedResult().isEmpty()) {
                System.out.println("      • Add expected result");
            }
        }
    }

    // ========== OPTION 4: VIEW STATISTICS ==========

    // ========== HELPER METHODS ==========

    private static void loadSampleTestCases() {
        testCases = createSampleTestCases();
        System.out.println("✅ Loaded " + testCases.size() + " sample test cases");
    }

    private static List<TestCase> createSampleTestCases() {
        List<TestCase> samples = new ArrayList<>();

        // ============================================================
        // SECTION 1: LOGIN TEST CASES (DUPLICATES & SIMILAR)
        // ============================================================

        // TC-001: Original Login Test
        samples.add(TestCase.builder()
                .id("TC-001")
                .title("Verify user can login with valid credentials")
                .description("Test login functionality with valid username and password")
                .steps(Arrays.asList(
                        "1. Navigate to login page",
                        "2. Enter valid username",
                        "3. Enter valid password",
                        "4. Click login button"))
                .expectedResult("User redirected to dashboard page")
                .priority(TestCase.Priority.HIGH)
                .executionCount(150)
                .build());

        // TC-002: DUPLICATE of TC-001 (almost identical - should be 90%+ similar)
        samples.add(TestCase.builder()
                .id("TC-002")
                .title("Verify user can login with valid credentials")  // SAME TITLE
                .description("Test login functionality with valid username and password")  // SAME DESCRIPTION
                .steps(Arrays.asList(
                        "1. Navigate to login page",
                        "2. Enter valid username",
                        "3. Enter valid password",
                        "4. Click login button"))
                .expectedResult("User redirected to dashboard page")
                .priority(TestCase.Priority.HIGH)
                .executionCount(148)
                .build());

        // TC-003: Similar Login Test (different wording - should be 70-80% similar)
        samples.add(TestCase.builder()
                .id("TC-003")
                .title("Check successful login using correct username/password")
                .description("Login attempt with valid credentials should succeed")
                .steps(Arrays.asList(
                        "1. Go to login screen",
                        "2. Type correct username",
                        "3. Type correct password",
                        "4. Press login button"))
                .expectedResult("Successful login to the system dashboard")
                .priority(TestCase.Priority.HIGH)
                .executionCount(120)
                .build());

        // TC-004: Another similar login test
        samples.add(TestCase.builder()
                .id("TC-004")
                .title("Login with valid credentials test")
                .description("Verify that user can login using valid username and password")
                .steps(Arrays.asList(
                        "1. Open login page",
                        "2. Input valid username",
                        "3. Input valid password",
                        "4. Hit submit"))
                .expectedResult("User navigates to dashboard")
                .priority(TestCase.Priority.HIGH)
                .executionCount(130)
                .build());

        // ============================================================
        // SECTION 2: PAYMENT TEST CASES (SIMILAR)
        // ============================================================

        // TC-005: Payment Test Original
        samples.add(TestCase.builder()
                .id("TC-005")
                .title("Payment processing with credit card")
                .description("Test credit card payment flow works correctly")
                .steps(Arrays.asList(
                        "1. Add items to shopping cart",
                        "2. Proceed to checkout",
                        "3. Enter credit card details",
                        "4. Submit payment"))
                .expectedResult("Payment successful, order confirmation displayed")
                .priority(TestCase.Priority.HIGH)
                .executionCount(80)
                .build());

        // TC-006: Similar Payment Test
        samples.add(TestCase.builder()
                .id("TC-006")
                .title("Credit card payment processing test")
                .description("Verify credit card payment is processed correctly")
                .steps(Arrays.asList(
                        "1. Add products to cart",
                        "2. Go to checkout page",
                        "3. Fill credit card information",
                        "4. Click pay now"))
                .expectedResult("Order confirmation received, payment successful")
                .priority(TestCase.Priority.HIGH)
                .executionCount(75)
                .build());

        // ============================================================
        // SECTION 3: LOGOUT TEST CASES (SIMILAR)
        // ============================================================

        // TC-007: Logout Test Original
        samples.add(TestCase.builder()
                .id("TC-007")
                .title("User logout functionality")
                .description("Test that logout button ends user session")
                .steps(Arrays.asList(
                        "1. Login to the system",
                        "2. Click logout button",
                        "3. Verify session is terminated"))
                .expectedResult("User logged out and redirected to login page")
                .priority(TestCase.Priority.MEDIUM)
                .executionCount(45)
                .build());

        // TC-008: Similar Logout Test
        samples.add(TestCase.builder()
                .id("TC-008")
                .title("Logout from application")
                .description("Test that user can successfully logout from the system")
                .steps(Arrays.asList(
                        "1. User logs in first",
                        "2. User clicks logout link",
                        "3. Session should be terminated"))
                .expectedResult("User redirected to login screen")
                .priority(TestCase.Priority.MEDIUM)
                .executionCount(40)
                .build());

        // ============================================================
        // SECTION 4: SEARCH TEST CASES (SIMILAR)
        // ============================================================

        // TC-009: Search Test Original
        samples.add(TestCase.builder()
                .id("TC-009")
                .title("Search product by name")
                .description("Test search functionality returns correct results")
                .steps(Arrays.asList(
                        "1. Enter product name in search box",
                        "2. Click search button",
                        "3. View search results"))
                .expectedResult("Products matching search term are displayed")
                .priority(TestCase.Priority.MEDIUM)
                .executionCount(30)
                .build());

        // TC-010: Similar Search Test
        samples.add(TestCase.builder()
                .id("TC-010")
                .title("Product search functionality verification")
                .description("Verify search returns correct products when searching by name")
                .steps(Arrays.asList(
                        "1. Type product name in search field",
                        "2. Click on search icon",
                        "3. Check search results page"))
                .expectedResult("Relevant products appear in search results")
                .priority(TestCase.Priority.MEDIUM)
                .executionCount(25)
                .build());

        // ============================================================
        // SECTION 5: REGISTRATION TEST CASES (SIMILAR)
        // ============================================================

        // TC-011: Registration Test
        samples.add(TestCase.builder()
                .id("TC-011")
                .title("New user registration with valid data")
                .description("Register new account with valid email and password")
                .steps(Arrays.asList(
                        "1. Click register link",
                        "2. Enter email address",
                        "3. Create password",
                        "4. Confirm password",
                        "5. Submit form"))
                .expectedResult("Registration successful, confirmation email sent")
                .priority(TestCase.Priority.HIGH)
                .executionCount(60)
                .build());

        // TC-012: Similar Registration Test
        samples.add(TestCase.builder()
                .id("TC-012")
                .title("User signup with valid information")
                .description("Create new user account using valid registration data")
                .steps(Arrays.asList(
                        "1. Navigate to signup page",
                        "2. Provide email",
                        "3. Set password",
                        "4. Confirm password",
                        "5. Click register"))
                .expectedResult("Account created successfully, user can login")
                .priority(TestCase.Priority.HIGH)
                .executionCount(55)
                .build());

        // ============================================================
        // SECTION 6: PASSWORD RESET TEST CASES (SIMILAR)
        // ============================================================

        // TC-013: Password Reset Test
        samples.add(TestCase.builder()
                .id("TC-013")
                .title("Forgot password reset via email")
                .description("User can reset password using forgot password link")
                .steps(Arrays.asList(
                        "1. Click forgot password link",
                        "2. Enter registered email",
                        "3. Click send reset link",
                        "4. Check email"))
                .expectedResult("Password reset email received with reset link")
                .priority(TestCase.Priority.MEDIUM)
                .executionCount(20)
                .build());

        // TC-014: Similar Password Reset Test
        samples.add(TestCase.builder()
                .id("TC-014")
                .title("Password recovery functionality test")
                .description("Test that users can recover password via email")
                .steps(Arrays.asList(
                        "1. Go to login page",
                        "2. Click 'Forgot Password'",
                        "3. Input email address",
                        "4. Request password reset"))
                .expectedResult("Reset password email is sent to user's inbox")
                .priority(TestCase.Priority.MEDIUM)
                .executionCount(18)
                .build());

        // ============================================================
        // SECTION 7: DIFFERENT CATEGORY TESTS (SANITY/REGRESSION)
        // ============================================================

        // TC-015: SANITY TEST (Low priority, rarely executed)
        samples.add(TestCase.builder()
                .id("TC-015")
                .title("Verify page title")
                .description("Check that homepage title is correct")
                .steps(Arrays.asList(
                        "1. Open application",
                        "2. Check browser title"))
                .expectedResult("Title matches expected value")
                .priority(TestCase.Priority.LOW)
                .executionCount(5)
                .build());

        // TC-016: SANITY TEST (Low priority, rarely executed)
        samples.add(TestCase.builder()
                .id("TC-016")
                .title("Check footer links")
                .description("Verify all footer links are working")
                .steps(Arrays.asList(
                        "1. Scroll to bottom of page",
                        "2. Click each footer link"))
                .expectedResult("All links navigate to correct pages")
                .priority(TestCase.Priority.LOW)
                .executionCount(3)
                .build());

        // TC-017: SANITY TEST (Very low priority)
        samples.add(TestCase.builder()
                .id("TC-017")
                .title("Logo display verification")
                .description("Check company logo appears correctly")
                .steps(Arrays.asList(
                        "1. Load homepage",
                        "2. Check logo visibility"))
                .expectedResult("Logo is displayed properly")
                .priority(TestCase.Priority.LOW)
                .executionCount(2)
                .build());

        // ============================================================
        // SECTION 8: UNIQUE TEST (No duplicates)
        // ============================================================

        // TC-018: Unique test - should have no similar matches
        samples.add(TestCase.builder()
                .id("TC-018")
                .title("Export report to PDF")
                .description("Test that user can export data report as PDF")
                .steps(Arrays.asList(
                        "1. Navigate to reports page",
                        "2. Click export button",
                        "3. Select PDF format",
                        "4. Download file"))
                .expectedResult("PDF file downloaded successfully")
                .priority(TestCase.Priority.MEDIUM)
                .executionCount(15)
                .build());

        return samples;
    }

    private static String getStringInput(String prompt) {
        System.out.print("   " + prompt);
        return scanner.nextLine().trim();
    }

    private static int getIntInput(String prompt) {
        while (true) {
            try {
                System.out.print("   " + prompt);
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("   ⚠️  Please enter a valid number!");
            }
        }
    }

    private static boolean isUniqueId(String id) {
        return testCases.stream().noneMatch(tc -> tc.getId().equals(id));
    }

    private static String truncateString(String str, int length) {
        if (str == null) return "";
        if (str.length() <= length) return str;
        return str.substring(0, length - 3) + "...";
    }

    private static String getSimilarityIcon(double score) {
        if (score >= 0.9) return "🔴";
        if (score >= 0.7) return "🟡";
        if (score >= 0.5) return "🟢";
        return "⚪";
    }

    private static void exportResultsToFile(CleansingEngine.CleansingReport report) {
        try {
            String filename = "cleansing-report-" + System.currentTimeMillis() + ".txt";
            java.io.FileWriter writer = new java.io.FileWriter(filename);

            writer.write("TEST CASE CLEANSING REPORT\n");
            writer.write("=========================\n\n");
            writer.write("Generated: " + new Date() + "\n\n");
            writer.write("Total Test Cases: " + report.normalizedCount + "\n");
            writer.write("Similar Pairs Found: " + (report.similarities != null ? report.similarities.size() : 0) + "\n\n");

            writer.write("SIMILARITY REPORT:\n");
            writer.write("-----------------\n");
            if (report.similarities != null) {
                for (SimilarityResult r : report.similarities) {
                    writer.write(String.format("%s ↔ %s: %.1f%%\n",
                            r.getTestCase1().getId(), r.getTestCase2().getId(),
                            r.getSimilarityScore() * 100));
                }
            }

            writer.write("\nTEST CATEGORIZATION:\n");
            writer.write("-------------------\n");
            if (report.categorized != null) {
                writer.write("\nSMOKE TESTS:\n");
                for (TestCase tc : report.categorized.getOrDefault(TestCoverageScore.TestType.SMOKE, new ArrayList<>())) {
                    writer.write("  - " + tc.getId() + ": " + tc.getTitle() + "\n");
                }

                writer.write("\nREGRESSION TESTS:\n");
                for (TestCase tc : report.categorized.getOrDefault(TestCoverageScore.TestType.REGRESSION, new ArrayList<>())) {
                    writer.write("  - " + tc.getId() + ": " + tc.getTitle() + "\n");
                }

                writer.write("\nSANITY TESTS:\n");
                for (TestCase tc : report.categorized.getOrDefault(TestCoverageScore.TestType.SANITY, new ArrayList<>())) {
                    writer.write("  - " + tc.getId() + ": " + tc.getTitle() + "\n");
                }
            }

            writer.close();
            System.out.println("\n✅ Results exported to: " + filename);
        } catch (Exception e) {
            System.out.println("❌ Error exporting: " + e.getMessage());
        }
    }

    private static void waitForEnter() {
        System.out.print("\n⏎ Press Enter to continue...");
        scanner.nextLine();
    }
}