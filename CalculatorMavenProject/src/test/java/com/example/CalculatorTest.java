package com.example;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Test Math Operation in Calculator class")
public class CalculatorTest {
    private Calculator calculator;

    @BeforeAll
    static void setup() {
        System.out.println("Executing @BeforeAll method.");
    }

    @AfterAll
    static void cleanup() {
        System.out.println("Executing @AfterAll method.");
    }

    @BeforeEach
    void beforeEachTestMethod() {
        System.out.println("Executing @BeforeEach method.");
        calculator = new Calculator();
    }

    @AfterEach
    void afterEachTestMethod() {
        System.out.println("Executing @AfterEach method.");
    }


    @DisplayName("Test 4/2 = 2")
    @Test
    void testIntegerDivision_whenFourDividedByTwo_ShouldReturnTwo() {
        int result = calculator.integerDivision(4, 2);
        assertEquals(2, result, "4/2 did not produce 2");
    }

    @DisplayName("Divided by zero")
    @Test
    void testIntegerDivision_whenDividendDividedByZero_ShouldThrowArithmeticException() {
        int value1 = 4;
        int value2 = 0;
        String expectedExceptionMessage = "/ by zero";

        ArithmeticException actualException = assertThrows(ArithmeticException.class, () -> calculator.integerDivision(value1, value2),
                "Division by Zero should have thrown an Arithmetic exception");

        assertEquals(expectedExceptionMessage, actualException.getMessage());
    }


    @Test
    void integerSubtraction() {
        int result = calculator.integerSubtraction(33, 1);
        assertEquals(32, result, "The integerDivision() did not produce expected result");
    }

    @DisplayName("Test 33-1 = 32")
    @ParameterizedTest
    @MethodSource("integerSubtractionInputParameters")
    void integerSubtraction1(int value1, int value2, int expectedResult) {
        Calculator calculator = new Calculator();

        int actualResult = calculator.integerSubtraction(value1, value2);
        assertEquals(expectedResult, actualResult, () -> value1 + " " + value2 + "id not produce " + expectedResult);
    }

    private static Stream<Arguments> integerSubtractionInputParameters() {
        return Stream.of(
                Arguments.of(33, 1, 32),
                Arguments.of(54, 1, 53),
                Arguments.of(24, 1, 23)
        );
    }

    // Replicating above test case - if u keep the same test name and argument name - will work
    @DisplayName("Test 33-1 = 32")
    @ParameterizedTest
    @MethodSource()
    void integerSubtraction_new(int value1, int value2, int expectedResult) {
        Calculator calculator = new Calculator();

        int actualResult = calculator.integerSubtraction(value1, value2);
        assertEquals(expectedResult, actualResult, () -> value1 + " " + value2 + "id not produce " + expectedResult);
    }


    private static Stream<Arguments> integerSubtraction_new() {
        return Stream.of(
                Arguments.of(33, 1, 32),
                Arguments.of(54, 1, 53),
                Arguments.of(24, 1, 23)
        );
    }

    // Replicating the above again
    @DisplayName("Test 33-1 = 32")
    @ParameterizedTest
    @CsvSource({"33,1,32", "11,1,10", "100,1,99"})
    void integerSubtractionNew(int value1, int value2, int expectedResult) {
        Calculator calculator = new Calculator();

        int actualResult = calculator.integerSubtraction(value1, value2);
        assertEquals(expectedResult, actualResult, () -> value1 + " " + value2 + "id not produce " + expectedResult);
    }


    // Replicating the above again
    @DisplayName("Test 33-1 = 32")
    @ParameterizedTest
    @CsvFileSource(resources = "/integerDivision.csv")
    void integerSubtractionNew1(int value1, int value2, int expectedResult) {
        Calculator calculator = new Calculator();

        int actualResult = calculator.integerSubtraction(value1, value2);
        assertEquals(expectedResult, actualResult, () -> value1 + " " + value2 + "id not produce " + expectedResult);
    }


    @ParameterizedTest
    @ValueSource(strings = {"John", "Jane", "Mike"})
    void testValueSourceDemonstration(String firstName) {
        System.out.println(firstName);
        assertNotNull(firstName);
    }
}