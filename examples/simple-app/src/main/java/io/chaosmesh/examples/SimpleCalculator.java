package io.chaosmesh.examples;

/**
 * Simple calculator application for testing runtime mutations.
 */
public class SimpleCalculator {
    private static final int MAX_VALUE = 100;
    private static final String SUCCESS_MSG = "Operation successful";
    private static final String ERROR_MSG = "Operation failed";

    public static void main(String[] args) throws InterruptedException {
        SimpleCalculator calc = new SimpleCalculator();

        System.out.println("Starting SimpleCalculator test application...");
        System.out.println("Control API available at http://localhost:8080");
        System.out.println();

        // Run continuous tests
        while (true) {
            testArithmetic(calc);
            testConstants(calc);
            testStrings(calc);

            Thread.sleep(2000);
            System.out.println("---");
        }
    }

    private static void testArithmetic(SimpleCalculator calc) {
        int a = 10, b = 5;
        System.out.println("Testing arithmetic operations:");
        System.out.println("  add(" + a + ", " + b + ") = " + calc.add(a, b));
        System.out.println("  subtract(" + a + ", " + b + ") = " + calc.subtract(a, b));
        System.out.println("  multiply(" + a + ", " + b + ") = " + calc.multiply(a, b));
        System.out.println("  divide(" + a + ", " + b + ") = " + calc.divide(a, b));
    }

    private static void testConstants(SimpleCalculator calc) {
        System.out.println("Testing constants:");
        System.out.println("  MAX_VALUE = " + MAX_VALUE);
        System.out.println("  isValid(50) = " + calc.isValid(50));
        System.out.println("  isValid(150) = " + calc.isValid(150));
    }

    private static void testStrings(SimpleCalculator calc) {
        System.out.println("Testing strings:");
        System.out.println("  getSuccessMessage() = " + calc.getSuccessMessage());
        System.out.println("  getErrorMessage() = " + calc.getErrorMessage());
    }

    public int add(int a, int b) {
        return a + b;
    }

    public int subtract(int a, int b) {
        return a - b;
    }

    public int multiply(int a, int b) {
        return a * b;
    }

    public int divide(int a, int b) {
        return a / b;
    }

    public boolean isValid(int value) {
        return value < MAX_VALUE;
    }

    public String getSuccessMessage() {
        return SUCCESS_MSG;
    }

    public String getErrorMessage() {
        return ERROR_MSG;
    }
}
