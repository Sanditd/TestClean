package com.testcleansing.service;

import com.testcleansing.model.TestCase;
import com.testcleansing.model.TestCoverageScore;
import java.util.*;

public class CoverageScoringService {

    private static final List<String> CRITICAL_KEYWORDS = Arrays.asList(
            "login", "logout", "authenticate", "payment", "checkout",
            "submit", "register", "signup", "transaction", "order", "cart"
    );

    public TestCoverageScore calculateScore(TestCase testCase) {
        int priorityScore = testCase.getPriority().getWeight() * 3;
        int frequencyScore = calculateExecutionFrequencyScore(testCase) * 2;
        int keywordScore = calculateKeywordDensityScore(testCase) * 2;

        double totalScore = priorityScore + frequencyScore + keywordScore;

        TestCoverageScore score = TestCoverageScore.builder()
                .testCase(testCase)
                .totalScore(totalScore)
                .build();

        score.setTestType(score.calculateTestType());
        return score;
    }

    public List<TestCoverageScore> calculateBatchScores(List<TestCase> testCases) {
        List<TestCoverageScore> scores = new ArrayList<>();
        for (TestCase testCase : testCases) {
            scores.add(calculateScore(testCase));
        }
        return scores;
    }

    public Map<TestCoverageScore.TestType, List<TestCase>> categorizeTestCases(List<TestCoverageScore> scores) {
        Map<TestCoverageScore.TestType, List<TestCase>> categorized = new HashMap<>();

        for (TestCoverageScore.TestType type : TestCoverageScore.TestType.values()) {
            categorized.put(type, new ArrayList<>());
        }

        for (TestCoverageScore score : scores) {
            categorized.get(score.getTestType()).add(score.getTestCase());
        }

        return categorized;
    }

    private int calculateExecutionFrequencyScore(TestCase testCase) {
        if (testCase.getExecutionCount() == null) return 1;
        if (testCase.getExecutionCount() > 100) return 5;
        if (testCase.getExecutionCount() > 50) return 4;
        if (testCase.getExecutionCount() > 20) return 3;
        if (testCase.getExecutionCount() > 10) return 2;
        return 1;
    }

    private int calculateKeywordDensityScore(TestCase testCase) {
        String fullText = testCase.getFullText().toLowerCase();
        int keywordCount = 0;

        for (String keyword : CRITICAL_KEYWORDS) {
            if (fullText.contains(keyword)) {
                keywordCount++;
            }
        }

        if (keywordCount >= 5) return 5;
        if (keywordCount >= 3) return 4;
        if (keywordCount >= 2) return 3;
        if (keywordCount >= 1) return 2;
        return 1;
    }
}