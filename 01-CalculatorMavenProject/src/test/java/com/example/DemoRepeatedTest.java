package com.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DemoRepeatedTest {
    private Calculator calculator;

    @BeforeEach
    void beforeEachTestMethod() {
        System.out.println("Executing @BeforeEach method.");
        calculator = new Calculator();
    }

    @DisplayName("Divided by zero")
    @RepeatedTest(value = 3, name = "{displayName}. Repetition {currentRepetition} of {totalRepetitions}")
    void testIntegerDivision_whenDividendDividedByZero_ShouldThrowArithmeticException(
            RepetitionInfo repetitionInfo, TestInfo testInfo) {

        System.out.println("Running " + testInfo.getTestMethod().get().getName());
        System.out.println("Repetition # " + repetitionInfo.getCurrentRepetition() + " of " + repetitionInfo.getTotalRepetitions());

        int value1 = 4;
        int value2 = 0;
        String expectedExceptionMessage = "/ by zero";

        ArithmeticException actualException = assertThrows(ArithmeticException.class, () -> calculator.integerDivision(value1, value2),
                "Division by Zero should have thrown an Arithmetic exception");

        assertEquals(expectedExceptionMessage, actualException.getMessage());
    }
}