package com.testcleansing.nlp;

import com.testcleansing.util.TextNormalizer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TextVectorizer {
    private static final Map<String, Integer> vocabulary = new ConcurrentHashMap<>();
    private static int vocabSize = 0;

    public static Double[] vectorize(String text) {
        String normalized = TextNormalizer.normalize(text);
        String[] tokens = normalized.split("\\s+");

        Map<String, Integer> termFreq = new HashMap<>();
        for (String token : tokens) {
            termFreq.put(token, termFreq.getOrDefault(token, 0) + 1);
        }

        synchronized (vocabulary) {
            for (String term : termFreq.keySet()) {
                if (!vocabulary.containsKey(term)) {
                    vocabulary.put(term, vocabSize++);
                }
            }
        }

        Double[] vector = new Double[vocabSize];
        Arrays.fill(vector, 0.0);

        for (Map.Entry<String, Integer> entry : termFreq.entrySet()) {
            Integer index = vocabulary.get(entry.getKey());
            if (index != null) {
                vector[index] = (double) entry.getValue() / tokens.length;
            }
        }

        return vector;
    }

    public static double cosineSimilarity(Double[] vector1, Double[] vector2) {
        if (vector1 == null || vector2 == null) return 0.0;

        int minLength = Math.min(vector1.length, vector2.length);
        double dotProduct = 0.0, norm1 = 0.0, norm2 = 0.0;

        for (int i = 0; i < minLength; i++) {
            double v1 = vector1[i] != null ? vector1[i] : 0.0;
            double v2 = vector2[i] != null ? vector2[i] : 0.0;
            dotProduct += v1 * v2;
            norm1 += Math.pow(v1, 2);
            norm2 += Math.pow(v2, 2);
        }

        if (norm1 == 0 || norm2 == 0) return 0.0;
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
}