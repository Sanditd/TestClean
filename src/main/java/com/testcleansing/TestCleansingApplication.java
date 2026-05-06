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
                case 4:
                    viewStatistics();
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
        System.out.println("║  │  4. 📊 VIEW STATISTICS                             │   ║");
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
        displayCleansingResults(report);

        // Ask if user wants to export results
        System.out.print("\n📁 Do you want to export results to file? (y/n): ");
        String export = scanner.nextLine().trim().toLowerCase();
        if (export.equals("y") || export.equals("yes")) {
            exportResultsToFile(report);
        }

        waitForEnter();
    }

    private static void displayCleansingResults(CleansingEngine.CleansingReport report) {
        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║                      CLEANSING RESULTS                      ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");

        // Summary
        System.out.println("\n📋 SUMMARY:");
        System.out.println("   ✅ Total Test Cases: " + report.normalizedCount);
        System.out.println("   🔄 Similar Pairs Found: " + (report.similarities != null ? report.similarities.size() : 0));
        System.out.println("   🔴 Duplicates (>90%): " + report.duplicatesFound);

        // Similarity Report
        if (report.similarities != null && !report.similarities.isEmpty()) {
            System.out.println("\n🔍 SIMILARITY REPORT:");
            System.out.println("   " + "─".repeat(55));

            // Sort by similarity score
            report.similarities.sort((a, b) -> Double.compare(b.getSimilarityScore(), a.getSimilarityScore()));

            int count = Math.min(10, report.similarities.size());
            for (int i = 0; i < count; i++) {
                SimilarityResult r = report.similarities.get(i);
                String icon = getSimilarityIcon(r.getSimilarityScore());
                System.out.printf("   %s %s ↔ %s: %.1f%%%n",
                        icon, r.getTestCase1().getId(), r.getTestCase2().getId(),
                        r.getSimilarityScore() * 100);
                System.out.printf("      📝 '%s'%n", truncateString(r.getTestCase1().getTitle(), 50));
                System.out.printf("      📝 '%s'%n", truncateString(r.getTestCase2().getTitle(), 50));
                System.out.println();
            }
        } else {
            System.out.println("\n🔍 SIMILARITY REPORT:");
            System.out.println("   ✅ No similar test cases found above threshold!");
        }

        // Test Categorization
        if (report.categorized != null) {
            System.out.println("\n🏷️  TEST CATEGORIZATION:");
            System.out.println("   " + "─".repeat(55));

            List<TestCase> smokeTests = report.categorized.getOrDefault(TestCoverageScore.TestType.SMOKE, new ArrayList<>());
            System.out.printf("   🔥 SMOKE TESTS: %d test(s)%n", smokeTests.size());
            for (TestCase tc : smokeTests) {
                System.out.printf("      • %s: %s%n", tc.getId(), truncateString(tc.getTitle(), 60));
            }

            List<TestCase> regressionTests = report.categorized.getOrDefault(TestCoverageScore.TestType.REGRESSION, new ArrayList<>());
            System.out.printf("\n   🔄 REGRESSION TESTS: %d test(s)%n", regressionTests.size());
            for (TestCase tc : regressionTests) {
                System.out.printf("      • %s: %s%n", tc.getId(), truncateString(tc.getTitle(), 60));
            }

            List<TestCase> sanityTests = report.categorized.getOrDefault(TestCoverageScore.TestType.SANITY, new ArrayList<>());
            System.out.printf("\n   ✓ SANITY TESTS: %d test(s)%n", sanityTests.size());
            for (TestCase tc : sanityTests) {
                System.out.printf("      • %s: %s%n", tc.getId(), truncateString(tc.getTitle(), 60));
            }
        }

        // Recommendations
        System.out.println("\n💡 RECOMMENDATIONS:");
        System.out.println("   " + "─".repeat(55));
        List<String> recommendations = engine.getImprovementSuggestions(report);
        for (String rec : recommendations) {
            System.out.println("   • " + rec);
        }

        System.out.println("\n⏱️  Processing Time: " + report.processingTimeMs + " ms");
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

    private static void viewStatistics() {
        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║                      SYSTEM STATISTICS                     ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");

        if (testCases.isEmpty()) {
            System.out.println("\n⚠️  No test cases found!");
            waitForEnter();
            return;
        }

        // Run quick stats
        CleansingEngine.CleansingReport report = engine.runCleansing(testCases);

        System.out.println("\n📊 TEST CASE STATISTICS:");
        System.out.println("   " + "─".repeat(40));
        System.out.printf("   📝 Total Test Cases: %d%n", testCases.size());
        System.out.printf("   🔥 Smoke Tests: %d%n",
                report.categorized.getOrDefault(TestCoverageScore.TestType.SMOKE, new ArrayList<>()).size());
        System.out.printf("   🔄 Regression Tests: %d%n",
                report.categorized.getOrDefault(TestCoverageScore.TestType.REGRESSION, new ArrayList<>()).size());
        System.out.printf("   ✓ Sanity Tests: %d%n",
                report.categorized.getOrDefault(TestCoverageScore.TestType.SANITY, new ArrayList<>()).size());

        // Priority distribution
        System.out.println("\n📊 PRIORITY DISTRIBUTION:");
        System.out.println("   " + "─".repeat(40));
        long highCount = testCases.stream().filter(tc -> tc.getPriority() == TestCase.Priority.HIGH).count();
        long mediumCount = testCases.stream().filter(tc -> tc.getPriority() == TestCase.Priority.MEDIUM).count();
        long lowCount = testCases.stream().filter(tc -> tc.getPriority() == TestCase.Priority.LOW).count();

        System.out.printf("   🔴 HIGH: %d (%.0f%%)%n", highCount, (double)highCount/testCases.size()*100);
        System.out.printf("   🟡 MEDIUM: %d (%.0f%%)%n", mediumCount, (double)mediumCount/testCases.size()*100);
        System.out.printf("   🟢 LOW: %d (%.0f%%)%n", lowCount, (double)lowCount/testCases.size()*100);

        // Similarity stats
        if (report.similarities != null && !report.similarities.isEmpty()) {
            double avgSimilarity = report.similarities.stream()
                    .mapToDouble(SimilarityResult::getSimilarityScore)
                    .average()
                    .orElse(0);

            System.out.println("\n🔍 SIMILARITY STATISTICS:");
            System.out.println("   " + "─".repeat(40));
            System.out.printf("   🔄 Similar Pairs: %d%n", report.similarities.size());
            System.out.printf("   📊 Average Similarity: %.1f%%%n", avgSimilarity * 100);
        }

        // Quality stats
        double avgQuality = testCases.stream()
                .mapToInt(tc -> engine.getQualityScore(tc))
                .average()
                .orElse(0);

        System.out.println("\n⭐ QUALITY STATISTICS:");
        System.out.println("   " + "─".repeat(40));
        System.out.printf("   📊 Average Quality Score: %.1f/100%n", avgQuality);

        waitForEnter();
    }

    // ========== HELPER METHODS ==========

    private static void loadSampleTestCases() {
        testCases = createSampleTestCases();
        System.out.println("✅ Loaded " + testCases.size() + " sample test cases");
    }

    private static List<TestCase> createSampleTestCases() {
        List<TestCase> samples = new ArrayList<>();

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

        samples.add(TestCase.builder()
                .id("TC-002")
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

        samples.add(TestCase.builder()
                .id("TC-003")
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

        samples.add(TestCase.builder()
                .id("TC-004")
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