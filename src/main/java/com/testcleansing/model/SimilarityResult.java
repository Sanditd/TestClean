package com.testcleansing.model;

public class SimilarityResult {
    private TestCase testCase1;
    private TestCase testCase2;
    private double similarityScore;

    public enum SimilarityType {
        DUPLICATE("🔴 MERGE - Identical", 0.90),
        VERY_HIGH("🟠 STRONG MERGE CANDIDATE", 0.80),
        HIGH("🟡 CONSIDER MERGING", 0.70),
        MEDIUM("🟢 REVIEW FOR REUSE", 0.50),
        LOW("⚪ KEEP SEPARATE", 0.0);

        private final String suggestion;
        private final double threshold;

        SimilarityType(String suggestion, double threshold) {
            this.suggestion = suggestion;
            this.threshold = threshold;
        }

        public String getSuggestion() { return suggestion; }
        public double getThreshold() { return threshold; }
    }

    public SimilarityResult() {}

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private SimilarityResult result = new SimilarityResult();

        public Builder testCase1(TestCase testCase1) { result.testCase1 = testCase1; return this; }
        public Builder testCase2(TestCase testCase2) { result.testCase2 = testCase2; return this; }
        public Builder similarityScore(double similarityScore) { result.similarityScore = similarityScore; return this; }

        public SimilarityResult build() { return result; }
    }

    public SimilarityType determineType() {
        if (similarityScore >= SimilarityType.DUPLICATE.getThreshold()) {
            return SimilarityType.DUPLICATE;
        } else if (similarityScore >= SimilarityType.VERY_HIGH.getThreshold()) {
            return SimilarityType.VERY_HIGH;
        } else if (similarityScore >= SimilarityType.HIGH.getThreshold()) {
            return SimilarityType.HIGH;
        } else if (similarityScore >= SimilarityType.MEDIUM.getThreshold()) {
            return SimilarityType.MEDIUM;
        } else {
            return SimilarityType.LOW;
        }
    }

    public String getRecommendation() {
        SimilarityType type = determineType();
        int percent = (int)(similarityScore * 100);

        switch (type) {
            case DUPLICATE:
                return String.format("%s: %s into %s (%d%% similar)",
                        type.getSuggestion(), testCase1.getId(), testCase2.getId(), percent);
            case VERY_HIGH:
                return String.format("%s: %s & %s (%d%% similar)",
                        type.getSuggestion(), testCase1.getId(), testCase2.getId(), percent);
            case HIGH:
                return String.format("%s: %s & %s (%d%% similar)",
                        type.getSuggestion(), testCase1.getId(), testCase2.getId(), percent);
            case MEDIUM:
                return String.format("%s: %s & %s (%d%% similar)",
                        type.getSuggestion(), testCase1.getId(), testCase2.getId(), percent);
            default:
                return String.format("%s: %s & %s (%d%% similar)",
                        type.getSuggestion(), testCase1.getId(), testCase2.getId(), percent);
        }
    }

    // Getters and Setters
    public TestCase getTestCase1() { return testCase1; }
    public void setTestCase1(TestCase testCase1) { this.testCase1 = testCase1; }
    public TestCase getTestCase2() { return testCase2; }
    public void setTestCase2(TestCase testCase2) { this.testCase2 = testCase2; }
    public double getSimilarityScore() { return similarityScore; }
    public void setSimilarityScore(double similarityScore) { this.similarityScore = similarityScore; }
}