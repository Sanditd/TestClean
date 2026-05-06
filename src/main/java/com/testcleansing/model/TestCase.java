package com.testcleansing.model;

import java.util.List;

public class TestCase {
    private String id;
    private String title;
    private String description;
    private List<String> steps;
    private String expectedResult;
    private Priority priority;
    private Integer executionCount;
    private String normalizedText;
    private Double[] vector;

    public enum Priority {
        HIGH(3), MEDIUM(2), LOW(1);

        private final int weight;

        Priority(int weight) {
            this.weight = weight;
        }

        public int getWeight() {
            return weight;
        }
    }

    public TestCase() {}

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private TestCase testCase = new TestCase();

        public Builder id(String id) { testCase.id = id; return this; }
        public Builder title(String title) { testCase.title = title; return this; }
        public Builder description(String description) { testCase.description = description; return this; }
        public Builder steps(List<String> steps) { testCase.steps = steps; return this; }
        public Builder expectedResult(String expectedResult) { testCase.expectedResult = expectedResult; return this; }
        public Builder priority(Priority priority) { testCase.priority = priority; return this; }
        public Builder executionCount(Integer executionCount) { testCase.executionCount = executionCount; return this; }
        public Builder normalizedText(String normalizedText) { testCase.normalizedText = normalizedText; return this; }
        public Builder vector(Double[] vector) { testCase.vector = vector; return this; }

        public TestCase build() { return testCase; }
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public List<String> getSteps() { return steps; }
    public String getExpectedResult() { return expectedResult; }
    public Priority getPriority() { return priority; }
    public Integer getExecutionCount() { return executionCount; }
    public String getNormalizedText() { return normalizedText; }
    public Double[] getVector() { return vector; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setSteps(List<String> steps) { this.steps = steps; }
    public void setExpectedResult(String expectedResult) { this.expectedResult = expectedResult; }
    public void setPriority(Priority priority) { this.priority = priority; }
    public void setExecutionCount(Integer executionCount) { this.executionCount = executionCount; }
    public void setNormalizedText(String normalizedText) { this.normalizedText = normalizedText; }
    public void setVector(Double[] vector) { this.vector = vector; }

    public String getFullText() {
        String titleText = title != null ? title : "";
        String descriptionText = description != null ? description : "";
        String stepsText = "";
        if (steps != null && !steps.isEmpty()) {
            stepsText = String.join(" ", steps);
        }
        String expectedText = expectedResult != null ? expectedResult : "";

        return titleText + " " + descriptionText + " " + stepsText + " " + expectedText;
    }
}