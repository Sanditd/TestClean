package com.testcleansing.model;

public class TestStandard {

    // Standard template for test case creation
    public static final String TEMPLATE = """
        =========================================
        TEST CASE TEMPLATE
        =========================================
        
        ID: [Unique identifier, e.g., TC-XXX]
        
        TITLE: [Verb + what to test]
        Examples:
        - Verify user can login with valid credentials
        - Check payment processing with credit card
        - Validate search functionality returns results
        
        DESCRIPTION: [Brief description of what this test does]
        (Minimum 20 characters, should explain the purpose)
        
        PRIORITY: [HIGH / MEDIUM / LOW]
        HIGH - Critical business flow
        MEDIUM - Important feature
        LOW - Nice to have
        
        STEPS:
        1. [First action]
        2. [Second action]
        3. [Third action]
        (Each step should be clear and actionable)
        
        EXPECTED RESULT:
        [What should happen after executing the steps]
        (Should be specific and measurable)
        
        =========================================
        """;

    // Quality checklist
    public static final String[] QUALITY_CHECKLIST = {
            "✓ Title starts with a verb (Verify, Check, Test, Validate)",
            "✓ Description clearly explains the test purpose",
            "✓ Priority is set correctly",
            "✓ Steps are numbered and sequential",
            "✓ Each step describes a single action",
            "✓ Expected result is specific and verifiable",
            "✓ Expected result ends with a period",
            "✓ No duplicate test cases exist",
            "✓ Test case is atomic (tests one thing)",
            "✓ Test case is independent (no dependencies)"
    };

    // Common violations and fixes
    public static final String[] COMMON_VIOLATIONS = {
            "Missing title → Add descriptive title starting with a verb",
            "No steps defined → Break down test into sequential steps",
            "Vague expected result → Specify exact outcome",
            "Priority not set → Assign HIGH/MEDIUM/LOW based on business impact",
            "Steps not numbered → Number each step (1., 2., 3.)",
            "Too many actions in one step → Split into multiple steps"
    };
}