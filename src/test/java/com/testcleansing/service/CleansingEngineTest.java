package com.testcleansing.service;

import com.testcleansing.model.TestCase;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class CleansingEngineTest {

    @Test
    public void testSimilarityDetection() {
        CleansingEngine engine = new CleansingEngine();

        TestCase tc1 = TestCase.builder()
                .id("TC-001")
                .title("Login test")
                .description("Test login functionality")
                .priority(TestCase.Priority.HIGH)
                .build();

        TestCase tc2 = TestCase.builder()
                .id("TC-002")
                .title("User login verification")
                .description("Verify user can login")
                .priority(TestCase.Priority.HIGH)
                .build();

        List<TestCase> testCases = Arrays.asList(tc1, tc2);
        var results = engine.findSimilarTestCases(testCases);

        assertNotNull(results);
    }

    @Test
    public void testScoreCalculation() {
        CleansingEngine engine = new CleansingEngine();

        TestCase tc = TestCase.builder()
                .id("TC-001")
                .title("Critical payment test")
                .description("Process payment")
                .priority(TestCase.Priority.HIGH)
                .executionCount(100)
                .build();

        var scores = engine.calculateScores(Arrays.asList(tc));

        assertEquals(1, scores.size());
        assertTrue(scores.get(0).getTotalScore() > 0);
    }
}