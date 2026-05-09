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
        List<TestCase> standardizedCases = analyzedCases; // Skip standardization
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
        System.out.println("\n" + "=".repeat(60));
        System.out.println("📋 CLEANSING SUMMARY");
        System.out.println("=".repeat(60));
        System.out.println("✅ Total Test Cases: " + report.normalizedCount);
        System.out.println("🔄 Similar Pairs Found: " + (report.similarities != null ? report.similarities.size() : 0));
        System.out.println("🔴 Duplicates (>90%): " + report.duplicatesFound);
        System.out.println("📝 Merge Suggestions: " + (report.mergeSuggestions != null ? report.mergeSuggestions.size() : 0));

        System.out.println("\n" + "=".repeat(60));
        System.out.println("📊 SIMILARITY COLOR LEGEND");
        System.out.println("=".repeat(60));
        System.out.println("  🔴 RED:     90% - 100%  → DUPLICATE - Merge immediately");
        System.out.println("  🟡 YELLOW:  70% - 89%   → HIGH SIMILARITY - Consider merging");
        System.out.println("  🟢 GREEN:   50% - 69%   → MEDIUM SIMILARITY - Review for reuse");
        System.out.println("  ⚪ WHITE:   40% - 49%   → LOW SIMILARITY - Keep separate");
        System.out.println("  (Hidden):   Below 40%   → NOT SIMILAR - No action needed");

        // ========== ADD THIS SECTION - DETAILED SIMILARITY REPORT ==========
        if (report.similarities != null && !report.similarities.isEmpty()) {
            System.out.println("\n" + "=".repeat(60));
            System.out.println("🔍 SIMILARITY REPORT (Top " + Math.min(20, report.similarities.size()) + ")");
            System.out.println("=".repeat(60));

            // Sort by similarity score (highest first)
            report.similarities.sort((a, b) -> Double.compare(b.getSimilarityScore(), a.getSimilarityScore()));

            int count = Math.min(20, report.similarities.size());
            for (int i = 0; i < count; i++) {
                SimilarityResult r = report.similarities.get(i);
                double score = r.getSimilarityScore();

                // Choose icon based on similarity score
                String icon;
                if (score >= 0.9) {
                    icon = "🔴";
                } else if (score >= 0.7) {
                    icon = "🟡";
                } else {
                    icon = "🟢";
                }

                System.out.printf("\n  %s %s ↔ %s: %.1f%%%n",
                        icon, r.getTestCase1().getId(), r.getTestCase2().getId(), score * 100);
                System.out.printf("     📝 '%s'%n", truncateTitle(r.getTestCase1().getTitle(), 55));
                System.out.printf("     📝 '%s'%n", truncateTitle(r.getTestCase2().getTitle(), 55));
            }
        }
        // ========== END OF ADDED SECTION ==========

        System.out.println("\n" + "=".repeat(60));
        System.out.println("🏷️  TEST CATEGORIZATION DETAILS");
        System.out.println("=".repeat(60));

        // Get the categorized test cases
        Map<TestCoverageScore.TestType, List<TestCase>> categorized = report.categorized;

        // 1. SMOKE TESTS
        List<TestCase> smokeTests = categorized.getOrDefault(TestCoverageScore.TestType.SMOKE, new ArrayList<>());
        System.out.println("\n🔥 SMOKE TESTS (Critical - Run on every build): " + smokeTests.size() + " test(s)");
        System.out.println("-".repeat(58));
        if (!smokeTests.isEmpty()) {
            for (int i = 0; i < smokeTests.size(); i++) {
                TestCase tc = smokeTests.get(i);
                System.out.printf("  %d. %s: %s%n", (i+1), tc.getId(), truncateTitle(tc.getTitle(), 50));
            }
        } else {
            System.out.println("  No smoke tests found");
        }

        // 2. REGRESSION TESTS
        List<TestCase> regressionTests = categorized.getOrDefault(TestCoverageScore.TestType.REGRESSION, new ArrayList<>());
        System.out.println("\n🔄 REGRESSION TESTS (Run on major changes): " + regressionTests.size() + " test(s)");
        System.out.println("-".repeat(58));
        if (!regressionTests.isEmpty()) {
            for (int i = 0; i < regressionTests.size(); i++) {
                TestCase tc = regressionTests.get(i);
                System.out.printf("  %d. %s: %s%n", (i+1), tc.getId(), truncateTitle(tc.getTitle(), 50));
            }
        } else {
            System.out.println("  No regression tests found");
        }

        // 3. SANITY TESTS
        List<TestCase> sanityTests = categorized.getOrDefault(TestCoverageScore.TestType.SANITY, new ArrayList<>());
        System.out.println("\n✓ SANITY TESTS (Basic validation - Run as needed): " + sanityTests.size() + " test(s)");
        System.out.println("-".repeat(58));
        if (!sanityTests.isEmpty()) {
            for (int i = 0; i < sanityTests.size(); i++) {
                TestCase tc = sanityTests.get(i);
                System.out.printf("  %d. %s: %s%n", (i+1), tc.getId(), truncateTitle(tc.getTitle(), 50));
            }
        } else {
            System.out.println("  No sanity tests found");
        }

        System.out.println("\n" + "=".repeat(60));
        System.out.println("⏱️  Processing Time: " + report.processingTimeMs + " ms");
        System.out.println("=".repeat(60));
    }

    // Helper method to truncate long titles
    private String truncateTitle(String title, int maxLength) {
        if (title == null) return "";
        if (title.length() <= maxLength) return title;
        return title.substring(0, maxLength - 3) + "...";
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