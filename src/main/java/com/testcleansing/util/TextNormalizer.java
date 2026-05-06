package com.testcleansing.util;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;

public class TextNormalizer {
    private static final List<String> STOP_WORDS = Arrays.asList(
            "the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for",
            "of", "with", "by", "is", "are", "was", "were", "be", "been", "being",
            "have", "has", "had", "having", "do", "does", "did", "doing", "verify",
            "check", "test", "validate", "ensure"
    );

    public static String normalize(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        String normalized = text.toLowerCase();
        normalized = Normalizer.normalize(normalized, Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{M}", "");
        normalized = normalized.replaceAll("[^a-zA-Z0-9\\s]", " ");
        normalized = normalized.replaceAll("\\s+", " ").trim();

        for (String stopWord : STOP_WORDS) {
            normalized = normalized.replaceAll("\\b" + stopWord + "\\b", "");
        }

        normalized = normalized.replaceAll("\\s+", " ").trim();
        return normalized;
    }
}