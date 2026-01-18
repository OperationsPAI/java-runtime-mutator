package io.chaosmesh.mutator.transformer;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Random;

/**
 * Mutates constant values in bytecode (integers, floats, strings, etc.)
 */
public class ConstantMutator extends MethodVisitor {
    private final String mutationType;
    private final Object fromValue;
    private final Object toValue;
    private final Random random = new Random();

    public ConstantMutator(int api, MethodVisitor mv, String mutationType, Object fromValue, Object toValue) {
        super(api, mv);
        this.mutationType = mutationType;
        this.fromValue = fromValue;
        this.toValue = toValue;
    }

    @Override
    public void visitLdcInsn(Object value) {
        // Mutate constant pool values (strings, large integers, floats, doubles)
        if (shouldMutate(value)) {
            super.visitLdcInsn(getMutatedValue(value));
        } else {
            super.visitLdcInsn(value);
        }
    }

    @Override
    public void visitIntInsn(int opcode, int operand) {
        // Mutate small integer constants (BIPUSH, SIPUSH)
        if (shouldMutate(operand)) {
            super.visitIntInsn(opcode, (Integer) getMutatedValue(operand));
        } else {
            super.visitIntInsn(opcode, operand);
        }
    }

    @Override
    public void visitInsn(int opcode) {
        // Mutate iconst_* instructions (ICONST_M1, ICONST_0, ..., ICONST_5)
        if (opcode >= Opcodes.ICONST_M1 && opcode <= Opcodes.ICONST_5) {
            int value = opcode - Opcodes.ICONST_0;
            if (shouldMutate(value)) {
                int mutated = (Integer) getMutatedValue(value);
                if (mutated >= -1 && mutated <= 5) {
                    super.visitInsn(Opcodes.ICONST_0 + mutated);
                } else {
                    super.visitLdcInsn(mutated);
                }
                return;
            }
        }
        super.visitInsn(opcode);
    }

    private boolean shouldMutate(Object value) {
        if (fromValue == null) {
            return true; // Mutate all constants of this type
        }

        // Handle string-to-number conversion for comparison
        if (fromValue instanceof String && value instanceof Number) {
            try {
                if (value instanceof Integer) {
                    return Integer.parseInt((String) fromValue) == ((Integer) value);
                } else if (value instanceof Long) {
                    return Long.parseLong((String) fromValue) == ((Long) value);
                } else if (value instanceof Float) {
                    return Float.parseFloat((String) fromValue) == ((Float) value);
                } else if (value instanceof Double) {
                    return Double.parseDouble((String) fromValue) == ((Double) value);
                }
            } catch (NumberFormatException e) {
                // Fall through to direct comparison
            }
        }

        return fromValue.equals(value);
    }

    private Object getMutatedValue(Object original) {
        if (toValue != null) {
            // Handle string-to-number conversion for toValue
            if (toValue instanceof String && original instanceof Number) {
                try {
                    if (original instanceof Integer) {
                        return Integer.parseInt((String) toValue);
                    } else if (original instanceof Long) {
                        return Long.parseLong((String) toValue);
                    } else if (original instanceof Float) {
                        return Float.parseFloat((String) toValue);
                    } else if (original instanceof Double) {
                        return Double.parseDouble((String) toValue);
                    }
                } catch (NumberFormatException e) {
                    // Fall through to return toValue as-is
                }
            }
            return toValue;
        }

        // Apply default mutation strategies
        if (original instanceof Integer) {
            return mutateInteger((Integer) original);
        } else if (original instanceof Long) {
            return mutateLong((Long) original);
        } else if (original instanceof Float) {
            return mutateFloat((Float) original);
        } else if (original instanceof Double) {
            return mutateDouble((Double) original);
        } else if (original instanceof String) {
            return mutateString((String) original);
        }

        return original;
    }

    private int mutateInteger(int value) {
        switch (mutationType) {
            case "zero": return 0;
            case "one": return 1;
            case "minus_one": return -1;
            case "max": return Integer.MAX_VALUE;
            case "min": return Integer.MIN_VALUE;
            case "negate": return -value;
            case "increment": return value + 1;
            case "decrement": return value - 1;
            case "random": return random.nextInt();
            default: return 0;
        }
    }

    private long mutateLong(long value) {
        switch (mutationType) {
            case "zero": return 0L;
            case "one": return 1L;
            case "minus_one": return -1L;
            case "max": return Long.MAX_VALUE;
            case "min": return Long.MIN_VALUE;
            case "negate": return -value;
            case "random": return random.nextLong();
            default: return 0L;
        }
    }

    private float mutateFloat(float value) {
        switch (mutationType) {
            case "zero": return 0.0f;
            case "one": return 1.0f;
            case "nan": return Float.NaN;
            case "infinity": return Float.POSITIVE_INFINITY;
            case "neg_infinity": return Float.NEGATIVE_INFINITY;
            case "negate": return -value;
            case "random": return random.nextFloat();
            default: return 0.0f;
        }
    }

    private double mutateDouble(double value) {
        switch (mutationType) {
            case "zero": return 0.0;
            case "one": return 1.0;
            case "nan": return Double.NaN;
            case "infinity": return Double.POSITIVE_INFINITY;
            case "neg_infinity": return Double.NEGATIVE_INFINITY;
            case "negate": return -value;
            case "random": return random.nextDouble();
            default: return 0.0;
        }
    }

    private String mutateString(String value) {
        switch (mutationType) {
            case "empty": return "";
            case "null": return null;
            case "reverse": return new StringBuilder(value).reverse().toString();
            case "uppercase": return value.toUpperCase();
            case "lowercase": return value.toLowerCase();
            case "random": return generateRandomString(value.length());
            default: return "";
        }
    }

    private String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
