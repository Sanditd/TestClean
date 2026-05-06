package com.testcleansing.service;

import com.testcleansing.model.TestCase;
import com.testcleansing.model.SimilarityResult;
import com.testcleansing.model.TestCoverageScore;
import com.testcleansing.service.StandardizationService.StandardizationResult;
import com.testcleansing.service.StandardizationService.ValidationResult;
import com.testcleansing.service.StandardizationService.ComplianceReport;
import java.util.*;

public class CleansingEngine {

    // Initialize all services
    private TextAnalysisService textAnalysisService = new TextAnalysisService();
    private SimilarityDetectionService similarityService = new SimilarityDetectionService();
    private StandardizationService standardizationService = new StandardizationService();
    private CoverageScoringService scoringService = new CoverageScoringService();

    // ========== MAIN CLEANSING PROCESS ==========

    public CleansingReport runCleansing(List<TestCase> testCases) {
        System.out.println("\n🚀 Starting Test Case Cleansing Process...\n");
        long startTime = System.currentTimeMillis();

        CleansingReport report = new CleansingReport();

        // Step 1: Text Analysis
        System.out.println("📝 Step 1: Analyzing and normalizing test cases");
        List<TestCase> analyzedCases = textAnalysisService.analyzeBatch(testCases);
        report.normalizedCount = analyzedCases.size();

        // Step 2: Detect Duplicates
        System.out.println("\n🔍 Step 2: Detecting duplicates and overlaps");
        List<SimilarityResult> similarities = similarityService.findSimilarTestCases(analyzedCases);
        report.similarities = similarities;
        report.duplicatesFound = similarities.stream()
                .filter(s -> s.getSimilarityScore() >= 0.9)
                .count();
        report.mergeSuggestions = similarityService.getMergeSuggestions(similarities);

        // Step 3: Standardize Test Cases (NEW!)
        System.out.println("\n📋 Step 3: Standardizing test case structure");
        runStandardizationCheck(analyzedCases);
        List<TestCase> standardizedCases = standardizationService.standardizeBatch(analyzedCases);
        report.standardizedCount = standardizedCases.size();

        // Step 4: Coverage Scoring
        System.out.println("\n📊 Step 4: Calculating coverage scores");
        List<TestCoverageScore> scores = scoringService.calculateBatchScores(standardizedCases);
        report.coverageScores = scores;
        report.categorized = scoringService.categorizeTestCases(scores);

        report.processingTimeMs = System.currentTimeMillis() - startTime;
        printSummary(report);

        return report;
    }

    // ========== STANDARDIZATION METHODS ==========

    /**
     * Run complete standardization check
     */
    public void runStandardizationCheck(List<TestCase> testCases) {
        System.out.println("  🔧 Checking test cases against standard format...");

        // First, standardize all test cases
        List<TestCase> standardized = standardizationService.standardizeBatch(testCases);

        // Generate compliance report
        ComplianceReport report = standardizationService.generateComplianceReport(standardized);
        report.print();

        // Show detailed issues for non-compliant test cases
        int issueCount = 0;
        for (TestCase tc : standardized) {
            StandardizationResult result = standardizationService.checkStandardFormat(tc);
            if (!result.isValid() || !result.getWarnings().isEmpty()) {
                if (issueCount == 0) {
                    System.out.println("\n  📝 DETAILED ISSUES:");
                }
                issueCount++;
                System.out.println("\n     " + issueCount + ". " + tc.getId() + ": " + tc.getTitle());

                if (!result.isValid()) {
                    System.out.println("        ❌ Violations:");
                    for (String violation : result.getViolations()) {
                        System.out.println("           - " + violation);
                    }
                }
                if (!result.getWarnings().isEmpty()) {
                    System.out.println("        ⚠️  Warnings:");
                    for (String warning : result.getWarnings()) {
                        System.out.println("           - " + warning);
                    }
                }
            }
        }

        if (issueCount == 0) {
            System.out.println("\n  ✅ All test cases follow the standard format!");
        }
    }

    /**
     * Preview how a test case would look in standard format
     */
    public void previewStandardFormat(TestCase testCase) {
        System.out.println("\n📄 STANDARD FORMAT PREVIEW:");
        System.out.println(standardizationService.formatAsStandard(testCase));
    }

    /**
     * Validate a new test case before adding to suite
     */
    public boolean validateAndAddTestCase(TestCase newTestCase, List<TestCase> existingTestCases) {
        System.out.println("\n🔍 VALIDATING NEW TEST CASE");
        System.out.println("=".repeat(50));

        ValidationResult result = standardizationService.validateNewTestCase(newTestCase, existingTestCases);

        if (result.isValid()) {
            System.out.println("\n✅ Test case is valid and ready to be added!");
            System.out.println("\nPreview in standard format:");
            System.out.println(standardizationService.formatAsStandard(newTestCase));
            return true;
        } else {
            System.out.println("\n❌ Test case has errors and cannot be added:");
            for (String error : result.getErrors()) {
                System.out.println("   • " + error);
            }
            return false;
        }
    }

    /**
     * Get quality score for a test case (0-100)
     */
    public int getQualityScore(TestCase testCase) {
        StandardizationResult result = standardizationService.checkStandardFormat(testCase);
        return result.getQualityScore();
    }

    // ========== RECOMMENDATIONS ==========

    public List<String> getImprovementSuggestions(CleansingReport report) {
        List<String> suggestions = new ArrayList<>();

        if (report.duplicatesFound > 0) {
            suggestions.add("Found " + report.duplicatesFound + " duplicate test cases. Consider merging them.");
        }

        if (report.mergeSuggestions != null && !report.mergeSuggestions.isEmpty()) {
            suggestions.add("Found " + report.mergeSuggestions.size() + " high-similarity test pairs that could be consolidated.");
        }

        int smokeCount = report.categorized.getOrDefault(TestCoverageScore.TestType.SMOKE, new ArrayList<>()).size();
        int regressionCount = report.categorized.getOrDefault(TestCoverageScore.TestType.REGRESSION, new ArrayList<>()).size();
        int sanityCount = report.categorized.getOrDefault(TestCoverageScore.TestType.SANITY, new ArrayList<>()).size();

        suggestions.add(String.format("Test distribution: %d Smoke, %d Regression, %d Sanity",
                smokeCount, regressionCount, sanityCount));

        if (smokeCount > 10) {
            suggestions.add("Smoke suite is large (" + smokeCount + " tests). Consider prioritizing only critical paths.");
        }

        suggestions.add("Review similar test cases to reduce automation maintenance effort by up to 40%.");

        return suggestions;
    }

    // ========== SUMMARY METHODS ==========

    private void printSummary(CleansingReport report) {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("📋 CLEANSING SUMMARY");
        System.out.println("=".repeat(50));
        System.out.println("✅ Total Test Cases: " + report.normalizedCount);
        System.out.println("🔄 Similar Pairs Found: " + (report.similarities != null ? report.similarities.size() : 0));
        System.out.println("🔴 Duplicates (>90%): " + report.duplicatesFound);
        System.out.println("📝 Merge Suggestions: " + (report.mergeSuggestions != null ? report.mergeSuggestions.size() : 0));
        System.out.println("🔧 Standardized: " + report.standardizedCount);

        System.out.println("\n🏷️  TEST CATEGORIZATION:");
        System.out.println("  🔥 SMOKE Tests: " + report.categorized.getOrDefault(TestCoverageScore.TestType.SMOKE, new ArrayList<>()).size());
        System.out.println("  🔄 REGRESSION Tests: " + report.categorized.getOrDefault(TestCoverageScore.TestType.REGRESSION, new ArrayList<>()).size());
        System.out.println("  ✓ SANITY Tests: " + report.categorized.getOrDefault(TestCoverageScore.TestType.SANITY, new ArrayList<>()).size());

        System.out.println("\n⏱️  Processing Time: " + report.processingTimeMs + " ms");
        System.out.println("=".repeat(50));
    }


    // ========== INNER CLASS ==========

    public static class CleansingReport {
        public int normalizedCount;
        public long duplicatesFound;
        public int standardizedCount;
        public List<SimilarityResult> similarities;
        public List<String> mergeSuggestions;
        public List<TestCoverageScore> coverageScores;
        public Map<TestCoverageScore.TestType, List<TestCase>> categorized = new HashMap<>();
        public long processingTimeMs;
    }


}