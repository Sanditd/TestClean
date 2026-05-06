package com.testcleansing.validator;

import com.testcleansing.model.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestCaseValidator {
    private static final Logger logger = LoggerFactory.getLogger(TestCaseValidator.class);

    public static boolean validate(TestCase testCase) {
        boolean isValid = true;

        // Check title
        if (testCase.getTitle() == null || testCase.getTitle().trim().isEmpty()) {
            logger.error("Test case {} has no title", testCase.getId());
            isValid = false;
        }

        // Check description
        if (testCase.getDescription() == null || testCase.getDescription().trim().isEmpty()) {
            logger.warn("Test case {} has no description", testCase.getId());
        }

        // Check steps
        if (testCase.getSteps() == null || testCase.getSteps().isEmpty()) {
            logger.warn("Test case {} has no steps defined", testCase.getId());
        }

        // Check expected result
        if (testCase.getExpectedResult() == null || testCase.getExpectedResult().trim().isEmpty()) {
            logger.warn("Test case {} has no expected result", testCase.getId());
        }

        // Check priority
        if (testCase.getPriority() == null) {
            logger.warn("Test case {} has no priority set", testCase.getId());
        }

        return isValid;
    }
}