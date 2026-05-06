package com.testcleansing.model;

public class TestCoverageScore {
    private TestCase testCase;
    private double totalScore;
    private TestType testType;

    public enum TestType {
        SMOKE("🔥 SMOKE - Run on every build", 18),
        REGRESSION("🔄 REGRESSION - Run on major changes", 12),
        SANITY("✓ SANITY - Basic validation", 0);

        private final String description;
        private final int threshold;

        TestType(String description, int threshold) {
            this.description = description;
            this.threshold = threshold;
        }

        public String getDescription() { return description; }
        public int getThreshold() { return threshold; }
    }

    public TestCoverageScore() {}

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private TestCoverageScore score = new TestCoverageScore();

        public Builder testCase(TestCase testCase) { score.testCase = testCase; return this; }
        public Builder totalScore(double totalScore) { score.totalScore = totalScore; return this; }
        public Builder testType(TestType testType) { score.testType = testType; return this; }

        public TestCoverageScore build() { return score; }
    }

    public TestType calculateTestType() {
        if (totalScore > TestType.SMOKE.getThreshold()) {
            return TestType.SMOKE;
        } else if (totalScore >= TestType.REGRESSION.getThreshold()) {
            return TestType.REGRESSION;
        } else {
            return TestType.SANITY;
        }
    }

    // Getters
    public TestCase getTestCase() { return testCase; }
    public double getTotalScore() { return totalScore; }
    public TestType getTestType() { return testType; }

    // Setters
    public void setTestCase(TestCase testCase) { this.testCase = testCase; }
    public void setTotalScore(double totalScore) { this.totalScore = totalScore; }
    public void setTestType(TestType testType) { this.testType = testType; }
}