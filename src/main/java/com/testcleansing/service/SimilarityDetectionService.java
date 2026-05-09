package com.testcleansing.service;

import com.testcleansing.model.TestCase;
import com.testcleansing.model.SimilarityResult;
import com.testcleansing.nlp.TextVectorizer;
import java.util.*;

public class SimilarityDetectionService {

    private static final double SIMILARITY_THRESHOLD = 0.7; // 40%

    public List<SimilarityResult> findSimilarTestCases(List<TestCase> testCases) {
        List<SimilarityResult> results = new ArrayList<>();

        // Compare every pair of test cases
        for (int i = 0; i < testCases.size(); i++) {
            for (int j = i + 1; j < testCases.size(); j++) {
                TestCase tc1 = testCases.get(i);
                TestCase tc2 = testCases.get(j);

                // USE THE VECTORS from TextAnalysisService!
                Double[] vector1 = tc1.getVector();
                Double[] vector2 = tc2.getVector();

                // Calculate cosine similarity
                double similarity = TextVectorizer.cosineSimilarity(vector1, vector2);

                if (similarity >= SIMILARITY_THRESHOLD) {
                    SimilarityResult result = SimilarityResult.builder()
                            .testCase1(tc1)
                            .testCase2(tc2)
                            .similarityScore(similarity)
                            .build();
                    results.add(result);
                }
            }
        }

        // Sort by similarity (highest first)
        results.sort((a, b) -> Double.compare(b.getSimilarityScore(), a.getSimilarityScore()));

        System.out.println("  ✅ Found " + results.size() + " similar pairs (threshold: " +
                (int)(SIMILARITY_THRESHOLD * 100) + "%)");

        return results;
    }

    public List<String> getMergeSuggestions(List<SimilarityResult> similarities) {
        List<String> suggestions = new ArrayList<>();
        for (SimilarityResult result : similarities) {
            if (result.getSimilarityScore() >= 0.7) { // 60% threshold for merge
                suggestions.add(result.getRecommendation());
            }
        }
        return suggestions;
    }
}