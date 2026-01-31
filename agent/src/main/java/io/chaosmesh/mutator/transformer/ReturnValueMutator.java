package io.chaosmesh.mutator.transformer;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * ASM MethodVisitor that mutates method return values.
 * Supports strategies: null, zero, false, empty, exception, custom, random
 */
public class ReturnValueMutator extends MethodVisitor {
    private final String strategy;
    private final Object customValue;
    private final String methodDescriptor;
    private final Object randomValue;  // Pre-generated random value from config
    private final boolean mutateFields; // Whether to mutate object fields instead of returning null
    private final boolean recursive;    // Whether to recursively mutate nested objects
    private final java.util.Set<String> excludeFields; // Fields to exclude from mutation

    public ReturnValueMutator(int api, MethodVisitor mv, String strategy, Object customValue,
                              String methodDescriptor, Object randomValue) {
        this(api, mv, strategy, customValue, methodDescriptor, randomValue, false, false, null);
    }

    public ReturnValueMutator(int api, MethodVisitor mv, String strategy, Object customValue,
                              String methodDescriptor, Object randomValue,
                              boolean mutateFields, boolean recursive,
                              java.util.Set<String> excludeFields) {
        super(api, mv);
        this.strategy = strategy;
        this.customValue = customValue;
        this.methodDescriptor = methodDescriptor;
        this.randomValue = randomValue;
        this.mutateFields = mutateFields;
        this.recursive = recursive;
        this.excludeFields = excludeFields;
    }

    @Override
    public void visitInsn(int opcode) {
        // Intercept return instructions
        if (isReturnInstruction(opcode)) {
            Type returnType = Type.getReturnType(methodDescriptor);

            switch (strategy) {
                case "null":
                    mutateToNull(opcode, returnType);
                    return;
                case "zero":
                    mutateToZero(opcode, returnType);
                    return;
                case "false":
                    mutateToFalse(opcode, returnType);
                    return;
                case "empty":
                    mutateToEmpty(opcode, returnType);
                    return;
                case "exception":
                    mutateToException();
                    return;
                case "custom":
                    mutateToCustom(opcode, returnType);
                    return;
                case "random":
                    mutateToRandom(opcode, returnType);
                    return;
            }
        }

        super.visitInsn(opcode);
    }

    private boolean isReturnInstruction(int opcode) {
        return opcode == Opcodes.IRETURN || opcode == Opcodes.LRETURN ||
               opcode == Opcodes.FRETURN || opcode == Opcodes.DRETURN ||
               opcode == Opcodes.ARETURN || opcode == Opcodes.RETURN;
    }

    private void mutateToNull(int opcode, Type returnType) {
        if (opcode == Opcodes.ARETURN) {
            mv.visitInsn(Opcodes.POP);  // Remove original return value
            mv.visitInsn(Opcodes.ACONST_NULL);
            mv.visitInsn(Opcodes.ARETURN);
        } else {
            super.visitInsn(opcode);  // Can't return null for primitives
        }
    }

    private void mutateToZero(int opcode, Type returnType) {
        switch (opcode) {
            case Opcodes.IRETURN:
                mv.visitInsn(Opcodes.POP);
                mv.visitInsn(Opcodes.ICONST_0);
                mv.visitInsn(Opcodes.IRETURN);
                break;
            case Opcodes.LRETURN:
                mv.visitInsn(Opcodes.POP2);
                mv.visitInsn(Opcodes.LCONST_0);
                mv.visitInsn(Opcodes.LRETURN);
                break;
            case Opcodes.FRETURN:
                mv.visitInsn(Opcodes.POP);
                mv.visitInsn(Opcodes.FCONST_0);
                mv.visitInsn(Opcodes.FRETURN);
                break;
            case Opcodes.DRETURN:
                mv.visitInsn(Opcodes.POP2);
                mv.visitInsn(Opcodes.DCONST_0);
                mv.visitInsn(Opcodes.DRETURN);
                break;
            default:
                super.visitInsn(opcode);
        }
    }

    private void mutateToFalse(int opcode, Type returnType) {
        // Boolean is represented as int (I) in bytecode, sort is Type.INT (5)
        if (opcode == Opcodes.IRETURN) {
            mv.visitInsn(Opcodes.POP);
            mv.visitInsn(Opcodes.ICONST_0);  // false
            mv.visitInsn(Opcodes.IRETURN);
        } else {
            super.visitInsn(opcode);
        }
    }

    private void mutateToEmpty(int opcode, Type returnType) {
        if (opcode == Opcodes.ARETURN && returnType.getSort() == Type.OBJECT &&
            returnType.getInternalName().equals("java/lang/String")) {
            mv.visitInsn(Opcodes.POP);
            mv.visitLdcInsn("");
            mv.visitInsn(Opcodes.ARETURN);
        } else {
            super.visitInsn(opcode);
        }
    }

    private void mutateToException() {
        String exceptionClass = customValue != null ? customValue.toString() : "java.lang.RuntimeException";
        String exceptionMessage = "Mutated return value";

        mv.visitTypeInsn(Opcodes.NEW, exceptionClass.replace('.', '/'));
        mv.visitInsn(Opcodes.DUP);
        mv.visitLdcInsn(exceptionMessage);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL,
            exceptionClass.replace('.', '/'),
            "<init>",
            "(Ljava/lang/String;)V",
            false);
        mv.visitInsn(Opcodes.ATHROW);
    }

    private void mutateToCustom(int opcode, Type returnType) {
        if (customValue == null) {
            super.visitInsn(opcode);
            return;
        }

        switch (opcode) {
            case Opcodes.IRETURN:
                mv.visitInsn(Opcodes.POP);
                pushIntValue(customValue);
                mv.visitInsn(Opcodes.IRETURN);
                break;
            case Opcodes.ARETURN:
                mv.visitInsn(Opcodes.POP);
                if (customValue instanceof String) {
                    mv.visitLdcInsn(customValue);
                } else {
                    mv.visitInsn(Opcodes.ACONST_NULL);
                }
                mv.visitInsn(Opcodes.ARETURN);
                break;
            default:
                super.visitInsn(opcode);
        }
    }

    private void mutateToRandom(int opcode, Type returnType) {
        switch (opcode) {
            case Opcodes.IRETURN:
                // For boolean (also IRETURN), XOR with 1 negates the value
                // For other int types, replace with pre-generated random value
                if (returnType.getDescriptor().equals("Z")) {
                    // Boolean: negate (XOR 1)
                    mv.visitInsn(Opcodes.ICONST_1);
                    mv.visitInsn(Opcodes.IXOR);
                } else {
                    // int/short/byte/char: replace with pre-generated random int
                    mv.visitInsn(Opcodes.POP);
                    int value = getRandomInt();
                    pushIntValue(value);
                }
                mv.visitInsn(Opcodes.IRETURN);
                break;
            case Opcodes.LRETURN:
                mv.visitInsn(Opcodes.POP2);
                mv.visitLdcInsn(getRandomLong());
                mv.visitInsn(Opcodes.LRETURN);
                break;
            case Opcodes.FRETURN:
                mv.visitInsn(Opcodes.POP);
                mv.visitLdcInsn(getRandomFloat());
                mv.visitInsn(Opcodes.FRETURN);
                break;
            case Opcodes.DRETURN:
                mv.visitInsn(Opcodes.POP2);
                mv.visitLdcInsn(getRandomDouble());
                mv.visitInsn(Opcodes.DRETURN);
                break;
            case Opcodes.ARETURN:
                if ("java/lang/String".equals(returnType.getInternalName())) {
                    // Return pre-generated random UUID string
                    mv.visitInsn(Opcodes.POP);
                    mv.visitLdcInsn(getRandomString());
                } else if (mutateFields) {
                    // Mutate object fields using MutationHelper
                    // Stack: [obj] -> [mutated_obj]
                    emitMutateObjectFields();
                } else {
                    // Other objects: return null
                    mv.visitInsn(Opcodes.POP);
                    mv.visitInsn(Opcodes.ACONST_NULL);
                }
                mv.visitInsn(Opcodes.ARETURN);
                break;
            default:
                // RETURN (void) - nothing to mutate
                super.visitInsn(opcode);
        }
    }

    /**
     * Emits bytecode to call MutationHelper.mutateObjectFields(obj, randomValues, recursive, excludeFields)
     * Stack before: [obj]
     * Stack after: [mutated_obj]
     */
    private void emitMutateObjectFields() {
        // Push randomValue (as a constant or null)
        emitRandomValueConstant();

        // Push recursive flag
        mv.visitInsn(recursive ? Opcodes.ICONST_1 : Opcodes.ICONST_0);

        // Push excludeFields set (or null)
        emitExcludeFieldsSet();

        // Call MutationHelper.mutateObjectFields(Object, Object, boolean, Set)
        mv.visitMethodInsn(Opcodes.INVOKESTATIC,
            "io/chaosmesh/mutator/runtime/MutationHelper",
            "mutateObjectFields",
            "(Ljava/lang/Object;Ljava/lang/Object;ZLjava/util/Set;)Ljava/lang/Object;",
            false);
    }

    @SuppressWarnings("unchecked")
    private void emitRandomValueConstant() {
        if (randomValue == null) {
            mv.visitInsn(Opcodes.ACONST_NULL);
            return;
        }

        // Create a new HashMap and populate it with random values
        mv.visitTypeInsn(Opcodes.NEW, "java/util/HashMap");
        mv.visitInsn(Opcodes.DUP);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/util/HashMap", "<init>", "()V", false);

        if (randomValue instanceof java.util.Map) {
            java.util.Map<String, Object> values = (java.util.Map<String, Object>) randomValue;
            for (java.util.Map.Entry<String, Object> entry : values.entrySet()) {
                mv.visitInsn(Opcodes.DUP);
                mv.visitLdcInsn(entry.getKey());
                emitBoxedValue(entry.getValue());
                mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/Map", "put",
                    "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", true);
                mv.visitInsn(Opcodes.POP); // Discard put() return value
            }
        }
    }

    private void emitBoxedValue(Object value) {
        if (value == null) {
            mv.visitInsn(Opcodes.ACONST_NULL);
        } else if (value instanceof Integer) {
            mv.visitLdcInsn(value);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf",
                "(I)Ljava/lang/Integer;", false);
        } else if (value instanceof Long) {
            mv.visitLdcInsn(value);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Long", "valueOf",
                "(J)Ljava/lang/Long;", false);
        } else if (value instanceof Float) {
            mv.visitLdcInsn(value);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Float", "valueOf",
                "(F)Ljava/lang/Float;", false);
        } else if (value instanceof Double) {
            mv.visitLdcInsn(value);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Double", "valueOf",
                "(D)Ljava/lang/Double;", false);
        } else if (value instanceof String) {
            mv.visitLdcInsn(value);
        } else {
            mv.visitInsn(Opcodes.ACONST_NULL);
        }
    }

    private void emitExcludeFieldsSet() {
        if (excludeFields == null || excludeFields.isEmpty()) {
            mv.visitInsn(Opcodes.ACONST_NULL);
            return;
        }

        // Create HashSet and add excluded field names
        mv.visitTypeInsn(Opcodes.NEW, "java/util/HashSet");
        mv.visitInsn(Opcodes.DUP);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/util/HashSet", "<init>", "()V", false);

        for (String fieldName : excludeFields) {
            mv.visitInsn(Opcodes.DUP);
            mv.visitLdcInsn(fieldName);
            mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/Set", "add",
                "(Ljava/lang/Object;)Z", true);
            mv.visitInsn(Opcodes.POP); // Discard add() return value
        }
    }

    @SuppressWarnings("unchecked")
    private int getRandomInt() {
        if (randomValue instanceof Number) {
            return ((Number) randomValue).intValue();
        } else if (randomValue instanceof java.util.Map) {
            Object val = ((java.util.Map<String, Object>) randomValue).get("int");
            return val instanceof Number ? ((Number) val).intValue() : 0;
        }
        return 0;
    }

    @SuppressWarnings("unchecked")
    private long getRandomLong() {
        if (randomValue instanceof Number) {
            return ((Number) randomValue).longValue();
        } else if (randomValue instanceof java.util.Map) {
            Object val = ((java.util.Map<String, Object>) randomValue).get("long");
            return val instanceof Number ? ((Number) val).longValue() : 0L;
        }
        return 0L;
    }

    @SuppressWarnings("unchecked")
    private float getRandomFloat() {
        if (randomValue instanceof Number) {
            return ((Number) randomValue).floatValue();
        } else if (randomValue instanceof java.util.Map) {
            Object val = ((java.util.Map<String, Object>) randomValue).get("float");
            return val instanceof Number ? ((Number) val).floatValue() : 0.0f;
        }
        return 0.0f;
    }

    @SuppressWarnings("unchecked")
    private double getRandomDouble() {
        if (randomValue instanceof Number) {
            return ((Number) randomValue).doubleValue();
        } else if (randomValue instanceof java.util.Map) {
            Object val = ((java.util.Map<String, Object>) randomValue).get("double");
            return val instanceof Number ? ((Number) val).doubleValue() : 0.0;
        }
        return 0.0;
    }

    @SuppressWarnings("unchecked")
    private String getRandomString() {
        if (randomValue instanceof String) {
            return (String) randomValue;
        } else if (randomValue instanceof java.util.Map) {
            Object val = ((java.util.Map<String, Object>) randomValue).get("string");
            return val instanceof String ? (String) val : java.util.UUID.randomUUID().toString();
        }
        return java.util.UUID.randomUUID().toString();
    }

    private void pushIntValue(int intValue) {
        if (intValue >= -1 && intValue <= 5) {
            mv.visitInsn(Opcodes.ICONST_0 + intValue);
        } else if (intValue >= Byte.MIN_VALUE && intValue <= Byte.MAX_VALUE) {
            mv.visitIntInsn(Opcodes.BIPUSH, intValue);
        } else if (intValue >= Short.MIN_VALUE && intValue <= Short.MAX_VALUE) {
            mv.visitIntInsn(Opcodes.SIPUSH, intValue);
        } else {
            mv.visitLdcInsn(intValue);
        }
    }

    private void pushIntValue(Object value) {
        int intValue;
        if (value instanceof Number) {
            intValue = ((Number) value).intValue();
        } else if (value instanceof String) {
            try {
                intValue = Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                intValue = 0;
            }
        } else {
            intValue = 0;
        }
        pushIntValue(intValue);
    }
}
