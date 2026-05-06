package com.testcleansing.service;

import com.testcleansing.model.TestCase;
import com.testcleansing.model.SimilarityResult;
import java.util.*;

public class SimilarityDetectionService {

    private static final double SIMILARITY_THRESHOLD = 0.4; // Lowered to 40%

    public List<SimilarityResult> findSimilarTestCases(List<TestCase> testCases) {
        List<SimilarityResult> results = new ArrayList<>();

        System.out.println("\n  🔍 Analyzing " + testCases.size() + " test cases for similarities...");

        for (int i = 0; i < testCases.size(); i++) {
            for (int j = i + 1; j < testCases.size(); j++) {
                TestCase tc1 = testCases.get(i);
                TestCase tc2 = testCases.get(j);

                double similarity = calculateImprovedSimilarity(tc1, tc2);

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

        results.sort((a, b) -> Double.compare(b.getSimilarityScore(), a.getSimilarityScore()));

        System.out.println("  ✅ Found " + results.size() + " similar pairs (threshold: " +
                (int)(SIMILARITY_THRESHOLD * 100) + "%)");

        if (!results.isEmpty()) {
            System.out.println("\n  🏆 TOP SIMILARITIES:");
            int count = Math.min(10, results.size());
            for (int i = 0; i < count; i++) {
                SimilarityResult r = results.get(i);
                System.out.printf("    %d. %s ↔ %s: %.1f%%%n",
                        i+1, r.getTestCase1().getId(), r.getTestCase2().getId(),
                        r.getSimilarityScore() * 100);
            }
        }

        return results;
    }

    private double calculateImprovedSimilarity(TestCase tc1, TestCase tc2) {
        // Get titles and descriptions
        String title1 = tc1.getTitle() != null ? tc1.getTitle().toLowerCase() : "";
        String title2 = tc2.getTitle() != null ? tc2.getTitle().toLowerCase() : "";
        String desc1 = tc1.getDescription() != null ? tc1.getDescription().toLowerCase() : "";
        String desc2 = tc2.getDescription() != null ? tc2.getDescription().toLowerCase() : "";

        // 1. Title similarity (40% weight)
        double titleSimilarity = calculateStringSimilarity(title1, title2);

        // 2. Description similarity (40% weight)
        double descSimilarity = calculateStringSimilarity(desc1, desc2);

        // 3. Keyword similarity (20% weight)
        double keywordSimilarity = calculateKeywordSimilarity(tc1, tc2);

        // Weighted score
        double totalSimilarity = (titleSimilarity * 0.4) + (descSimilarity * 0.4) + (keywordSimilarity * 0.2);

        return totalSimilarity;
    }

    private double calculateStringSimilarity(String str1, String str2) {
        if (str1.isEmpty() && str2.isEmpty()) return 1.0;
        if (str1.isEmpty() || str2.isEmpty()) return 0.0;

        // Get words
        String[] words1 = str1.replaceAll("[^a-z0-9\\s]", "").split("\\s+");
        String[] words2 = str2.replaceAll("[^a-z0-9\\s]", "").split("\\s+");

        Set<String> set1 = new HashSet<>(Arrays.asList(words1));
        Set<String> set2 = new HashSet<>(Arrays.asList(words2));

        // Remove common stop words
        Set<String> stopWords = new HashSet<>(Arrays.asList(
                "the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for",
                "of", "with", "by", "is", "are", "was", "were", "be", "been", "being",
                "have", "has", "had", "having", "do", "does", "did", "doing", "verify",
                "check", "test", "validate", "ensure", "user", "can"
        ));

        set1.removeAll(stopWords);
        set2.removeAll(stopWords);

        if (set1.isEmpty() && set2.isEmpty()) return 1.0;
        if (set1.isEmpty() || set2.isEmpty()) return 0.0;

        // Calculate Jaccard similarity
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);

        return (double) intersection.size() / union.size();
    }

    private double calculateKeywordSimilarity(TestCase tc1, TestCase tc2) {
        // Define keyword categories
        Map<String, List<String>> keywordCategories = new HashMap<>();
        keywordCategories.put("login", Arrays.asList("login", "signin", "authenticate", "credentials"));
        keywordCategories.put("payment", Arrays.asList("payment", "checkout", "credit", "card", "transaction", "order"));
        keywordCategories.put("logout", Arrays.asList("logout", "signout", "exit"));
        keywordCategories.put("search", Arrays.asList("search", "find", "lookup", "query"));
        keywordCategories.put("register", Arrays.asList("register", "signup", "create account", "new user"));
        keywordCategories.put("password", Arrays.asList("password", "reset", "recovery", "forgot"));

        String text1 = (tc1.getTitle() + " " + tc1.getDescription()).toLowerCase();
        String text2 = (tc2.getTitle() + " " + tc2.getDescription()).toLowerCase();

        int matchedCategories = 0;
        int totalCategories = keywordCategories.size();

        for (String category : keywordCategories.keySet()) {
            boolean hasIn1 = false;
            boolean hasIn2 = false;

            for (String keyword : keywordCategories.get(category)) {
                if (text1.contains(keyword)) hasIn1 = true;
                if (text2.contains(keyword)) hasIn2 = true;
            }

            if (hasIn1 && hasIn2) {
                matchedCategories++;
            }
        }

        return totalCategories > 0 ? (double) matchedCategories / totalCategories : 0.0;
    }

    public List<String> getMergeSuggestions(List<SimilarityResult> similarities) {
        List<String> suggestions = new ArrayList<>();
        for (SimilarityResult result : similarities) {
            // Suggest merge for similarities above 60%
            if (result.getSimilarityScore() >= 0.6) {
                suggestions.add(result.getRecommendation());
            }
        }
        return suggestions;
    }
}