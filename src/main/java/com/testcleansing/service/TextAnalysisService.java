package com.testcleansing.service;

import com.testcleansing.model.TestCase;
import com.testcleansing.util.TextNormalizer;
import com.testcleansing.nlp.TextVectorizer;
import java.util.List;
import java.util.stream.Collectors;

public class TextAnalysisService {

    public TestCase analyzeAndNormalize(TestCase testCase) {
        if (testCase == null) {
            return null;
        }

        String fullText = testCase.getFullText();
        if (fullText == null || fullText.trim().isEmpty()) {
            fullText = testCase.getTitle() != null ? testCase.getTitle() : "";
        }

        String normalizedText = TextNormalizer.normalize(fullText);
        testCase.setNormalizedText(normalizedText);

        Double[] vector = TextVectorizer.vectorize(normalizedText);
        testCase.setVector(vector);

        return testCase;
    }

    public List<TestCase> analyzeBatch(List<TestCase> testCases) {
        if (testCases == null || testCases.isEmpty()) {
            return List.of();
        }

        return testCases.stream()
                .map(this::analyzeAndNormalize)
                .collect(Collectors.toList());
    }
}