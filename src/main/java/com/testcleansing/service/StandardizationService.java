package com.testcleansing.service;

import com.testcleansing.model.TestCase;
import com.testcleansing.model.TestStandard;
import com.testcleansing.validator.TestCaseValidator;
import java.util.*;
import java.util.regex.Pattern;

public class StandardizationService {

    // Standard format template
    private static final String STANDARD_FORMAT = """
        =========================================
        TEST CASE ID: %s
        =========================================
        TITLE: %s
        -----------------------------------------
        DESCRIPTION: %s
        -----------------------------------------
        PRIORITY: %s
        -----------------------------------------
        STEPS:
        %s
        -----------------------------------------
        EXPECTED RESULT: %s
        =========================================
        """;

    // Step format pattern
    private static final Pattern STEP_PATTERN = Pattern.compile("^\\d+\\..+");

    /**
     * Check if a test case follows standard format
     */
    public StandardizationResult checkStandardFormat(TestCase testCase) {
        System.out.println("\n  📋 Checking standard format for: " + testCase.getId());

        List<String> violations = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // Check 1: Title format
        if (testCase.getTitle() == null || testCase.getTitle().trim().isEmpty()) {
            violations.add("Title is missing or empty");
        } else if (!isValidTitle(testCase.getTitle())) {
            warnings.add("Title should start with a verb (Verify, Check, Test, Validate)");
        }

        // Check 2: Description format
        if (testCase.getDescription() == null || testCase.getDescription().trim().isEmpty()) {
            violations.add("Description is missing");
        } else if (testCase.getDescription().length() < 20) {
            warnings.add("Description is too short (minimum 20 characters recommended)");
        }

        // Check 3: Steps format
        if (testCase.getSteps() == null || testCase.getSteps().isEmpty()) {
            violations.add("Test steps are missing");
        } else {
            checkStepsFormat(testCase.getSteps(), violations, warnings);
        }

        // Check 4: Expected result format
        if (testCase.getExpectedResult() == null || testCase.getExpectedResult().trim().isEmpty()) {
            violations.add("Expected result is missing");
        } else if (!isValidExpectedResult(testCase.getExpectedResult())) {
            warnings.add("Expected result should end with a period and be descriptive");
        }

        // Check 5: Priority
        if (testCase.getPriority() == null) {
            violations.add("Priority is not set (must be HIGH, MEDIUM, or LOW)");
        }

        return new StandardizationResult(
                violations.isEmpty(),
                violations,
                warnings,
                calculateQualityScore(violations.size(), warnings.size())
        );
    }

    /**
     * Validate new test case before creation
     */
    public ValidationResult validateNewTestCase(TestCase newTestCase, List<TestCase> existingTestCases) {
        System.out.println("\n  🔍 Validating new test case: " + newTestCase.getTitle());

        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // 1. Check for duplicate ID
        if (existingTestCases.stream().anyMatch(tc -> tc.getId().equals(newTestCase.getId()))) {
            errors.add("Test case ID '" + newTestCase.getId() + "' already exists");
        }

        // 2. Check for duplicate content
        for (TestCase existing : existingTestCases) {
            double similarity = calculateSimilarity(newTestCase, existing);
            if (similarity > 0.85) {
                warnings.add("Very similar to existing test case " + existing.getId() +
                        " (" + (int)(similarity * 100) + "% similar)");
            }
        }

        // 3. Check standard format
        StandardizationResult formatCheck = checkStandardFormat(newTestCase);
        if (!formatCheck.isValid()) {
            errors.addAll(formatCheck.getViolations());
        }
        warnings.addAll(formatCheck.getWarnings());

        // 4. Check required fields
        if (newTestCase.getSteps() == null || newTestCase.getSteps().isEmpty()) {
            errors.add("Test steps are required");
        }

        if (newTestCase.getExpectedResult() == null || newTestCase.getExpectedResult().trim().isEmpty()) {
            errors.add("Expected result is required");
        }

        boolean isValid = errors.isEmpty();

        if (isValid) {
            System.out.println("  ✅ Test case is valid and ready for creation");
        } else {
            System.out.println("  ❌ Test case has " + errors.size() + " error(s)");
            for (String error : errors) {
                System.out.println("     - " + error);
            }
        }

        if (!warnings.isEmpty()) {
            System.out.println("  ⚠️  Warnings: " + warnings.size());
            for (String warning : warnings) {
                System.out.println("     - " + warning);
            }
        }

        return new ValidationResult(isValid, errors, warnings);
    }

    /**
     * Convert test case to standard format
     */
    public TestCase standardize(TestCase testCase) {
        System.out.println("  🔧 Standardizing test case: " + testCase.getId());

        // Standardize title
        testCase.setTitle(standardizeTitle(testCase.getTitle()));

        // Standardize description
        if (testCase.getDescription() != null) {
            testCase.setDescription(standardizeDescription(testCase.getDescription()));
        }

        // Standardize steps
        if (testCase.getSteps() != null && !testCase.getSteps().isEmpty()) {
            testCase.setSteps(standardizeSteps(testCase.getSteps()));
        } else if (testCase.getDescription() != null) {
            // Extract steps from description if not provided
            testCase.setSteps(extractStepsFromDescription(testCase.getDescription()));
        }

        // Standardize expected result
        if (testCase.getExpectedResult() != null) {
            testCase.setExpectedResult(standardizeExpectedResult(testCase.getExpectedResult()));
        }

        // Set default priority if null
        if (testCase.getPriority() == null) {
            testCase.setPriority(TestCase.Priority.MEDIUM);
        }

        return testCase;
    }

    /**
     * Format test case as standard string
     */
    public String formatAsStandard(TestCase testCase) {
        StringBuilder stepsFormatted = new StringBuilder();
        if (testCase.getSteps() != null) {
            for (int i = 0; i < testCase.getSteps().size(); i++) {
                stepsFormatted.append(String.format("    %d. %s%n", i + 1, testCase.getSteps().get(i)));
            }
        } else {
            stepsFormatted.append("    No steps defined");
        }

        return String.format(STANDARD_FORMAT,
                testCase.getId(),
                testCase.getTitle(),
                testCase.getDescription() != null ? testCase.getDescription() : "N/A",
                testCase.getPriority() != null ? testCase.getPriority() : "MEDIUM",
                stepsFormatted.toString(),
                testCase.getExpectedResult() != null ? testCase.getExpectedResult() : "N/A"
        );
    }

    /**
     * Batch standardize multiple test cases
     */
    public List<TestCase> standardizeBatch(List<TestCase> testCases) {
        System.out.println("\n  🔧 Standardizing " + testCases.size() + " test cases...");
        List<TestCase> standardized = new ArrayList<>();
        for (TestCase tc : testCases) {
            standardized.add(standardize(tc));
        }
        System.out.println("  ✅ Standardized " + standardized.size() + " test cases");
        return standardized;
    }

    /**
     * Generate a compliance report for all test cases
     */
    public ComplianceReport generateComplianceReport(List<TestCase> testCases) {
        System.out.println("\n  📊 Generating compliance report...");

        int total = testCases.size();
        int compliant = 0;
        int partialCompliant = 0;
        int nonCompliant = 0;
        List<String> recommendations = new ArrayList<>();

        for (TestCase tc : testCases) {
            StandardizationResult result = checkStandardFormat(tc);
            if (result.isValid() && result.getWarnings().isEmpty()) {
                compliant++;
            } else if (result.isValid()) {
                partialCompliant++;
                recommendations.add(tc.getId() + ": " + result.getWarnings().get(0));
            } else {
                nonCompliant++;
                recommendations.add(tc.getId() + ": Missing " + result.getViolations());
            }
        }

        return new ComplianceReport(total, compliant, partialCompliant, nonCompliant, recommendations);
    }

    // ========== PRIVATE HELPER METHODS ==========

    private boolean isValidTitle(String title) {
        String[] validStartWords = {"Verify", "Check", "Test", "Validate", "Ensure"};
        for (String word : validStartWords) {
            if (title.startsWith(word)) {
                return true;
            }
        }
        return false;
    }

    private boolean isValidExpectedResult(String expectedResult) {
        return expectedResult.trim().endsWith(".") && expectedResult.length() > 10;
    }

    private void checkStepsFormat(List<String> steps, List<String> violations, List<String> warnings) {
        for (int i = 0; i < steps.size(); i++) {
            String step = steps.get(i);
            if (!STEP_PATTERN.matcher(step).matches() && !step.matches("^\\d+\\..*")) {
                warnings.add("Step " + (i+1) + " should start with a number (e.g., '1. ')");
            }
            if (step.length() < 10) {
                warnings.add("Step " + (i+1) + " is too short (less than 10 characters)");
            }
        }
    }

    private int calculateQualityScore(int violations, int warnings) {
        int score = 100;
        score -= violations * 20;
        score -= warnings * 5;
        return Math.max(0, score);
    }

    private double calculateSimilarity(TestCase tc1, TestCase tc2) {
        String text1 = (tc1.getTitle() + " " + tc1.getDescription()).toLowerCase();
        String text2 = (tc2.getTitle() + " " + tc2.getDescription()).toLowerCase();

        Set<String> words1 = new HashSet<>(Arrays.asList(text1.split("\\s+")));
        Set<String> words2 = new HashSet<>(Arrays.asList(text2.split("\\s+")));

        Set<String> intersection = new HashSet<>(words1);
        intersection.retainAll(words2);

        Set<String> union = new HashSet<>(words1);
        union.addAll(words2);

        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }

    private String standardizeTitle(String title) {
        if (title == null) return "Untitled Test Case";

        title = title.trim();
        if (!isValidTitle(title)) {
            title = "Verify " + title.toLowerCase();
        }
        return title;
    }

    private String standardizeDescription(String description) {
        description = description.trim();
        if (!description.endsWith(".")) {
            description += ".";
        }
        return description;
    }

    private List<String> standardizeSteps(List<String> steps) {
        List<String> standardized = new ArrayList<>();
        for (int i = 0; i < steps.size(); i++) {
            String step = steps.get(i).trim();
            if (!STEP_PATTERN.matcher(step).matches()) {
                step = (i + 1) + ". " + step;
            }
            if (!step.endsWith(".")) {
                step += ".";
            }
            standardized.add(step);
        }
        return standardized;
    }

    private List<String> extractStepsFromDescription(String description) {
        List<String> steps = new ArrayList<>();
        String[] sentences = description.split("(?<=[.!?])\\s+");

        for (int i = 0; i < sentences.length; i++) {
            steps.add((i + 1) + ". " + sentences[i].trim());
        }

        return steps;
    }

    private String standardizeExpectedResult(String expectedResult) {
        expectedResult = expectedResult.trim();
        if (!expectedResult.endsWith(".")) {
            expectedResult += ".";
        }
        return expectedResult;
    }

    // ========== INNER CLASSES ==========

    public static class StandardizationResult {
        private final boolean valid;
        private final List<String> violations;
        private final List<String> warnings;
        private final int qualityScore;

        public StandardizationResult(boolean valid, List<String> violations, List<String> warnings, int qualityScore) {
            this.valid = valid;
            this.violations = violations;
            this.warnings = warnings;
            this.qualityScore = qualityScore;
        }

        public boolean isValid() { return valid; }
        public List<String> getViolations() { return violations; }
        public List<String> getWarnings() { return warnings; }
        public int getQualityScore() { return qualityScore; }
    }

    public static class ValidationResult {
        private final boolean valid;
        private final List<String> errors;
        private final List<String> warnings;

        public ValidationResult(boolean valid, List<String> errors, List<String> warnings) {
            this.valid = valid;
            this.errors = errors;
            this.warnings = warnings;
        }

        public boolean isValid() { return valid; }
        public List<String> getErrors() { return errors; }
        public List<String> getWarnings() { return warnings; }
    }

    public static class ComplianceReport {
        private final int total;
        private final int compliant;
        private final int partialCompliant;
        private final int nonCompliant;
        private final List<String> recommendations;

        public ComplianceReport(int total, int compliant, int partialCompliant, int nonCompliant, List<String> recommendations) {
            this.total = total;
            this.compliant = compliant;
            this.partialCompliant = partialCompliant;
            this.nonCompliant = nonCompliant;
            this.recommendations = recommendations;
        }

        public void print() {
            System.out.println("\n  📋 COMPLIANCE REPORT");
            System.out.println("  " + "=".repeat(40));
            System.out.println("  Total Test Cases: " + total);
            System.out.println("  ✅ Fully Compliant: " + compliant);
            System.out.println("  ⚠️  Partially Compliant: " + partialCompliant);
            System.out.println("  ❌ Non-Compliant: " + nonCompliant);
            System.out.println("  Compliance Rate: " + (compliant * 100 / total) + "%");

            if (!recommendations.isEmpty()) {
                System.out.println("\n  📝 Recommendations:");
                for (String rec : recommendations) {
                    System.out.println("     • " + rec);
                }
            }
            System.out.println("  " + "=".repeat(40));
        }
    }
}