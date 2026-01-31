package io.chaosmesh.mutator.runtime;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class MutationHelperTest {

    // Test class with various field types
    static class TestObject {
        public int intField = 100;
        public long longField = 200L;
        public float floatField = 1.5f;
        public double doubleField = 2.5;
        public boolean boolField = true;
        public String stringField = "original";
        public short shortField = 10;
        public byte byteField = 5;
        public char charField = 'X';
    }

    // Test class with nested object
    static class OuterObject {
        public int outerInt = 1;
        public TestObject nested = new TestObject();
    }

    @Test
    void testMutateNullObject() {
        Object result = MutationHelper.mutateObjectFields(null, null, false, null);
        assertNull(result);
    }

    @Test
    void testMutateObjectWithRandomValues() {
        TestObject obj = new TestObject();
        Map<String, Object> randomValues = new HashMap<>();
        randomValues.put("int", 999);
        randomValues.put("long", 888L);
        randomValues.put("float", 0.5f);
        randomValues.put("double", 0.25);
        randomValues.put("string", "mutated-uuid");

        Object result = MutationHelper.mutateObjectFields(obj, randomValues, false, null);

        assertSame(obj, result);
        assertEquals(999, obj.intField);
        assertEquals(888L, obj.longField);
        assertEquals(0.5f, obj.floatField);
        assertEquals(0.25, obj.doubleField);
        assertFalse(obj.boolField); // Negated from true
        assertEquals("mutated-uuid", obj.stringField);
    }

    @Test
    void testMutateObjectWithExcludedFields() {
        TestObject obj = new TestObject();
        Map<String, Object> randomValues = new HashMap<>();
        randomValues.put("int", 999);
        randomValues.put("string", "mutated");

        Set<String> excluded = new HashSet<>();
        excluded.add("intField");
        excluded.add("stringField");

        MutationHelper.mutateObjectFields(obj, randomValues, false, excluded);

        // Excluded fields should remain unchanged
        assertEquals(100, obj.intField);
        assertEquals("original", obj.stringField);
        // Non-excluded boolean should be negated
        assertFalse(obj.boolField);
    }

    @Test
    void testMutateNestedObjectNonRecursive() {
        OuterObject obj = new OuterObject();
        Map<String, Object> randomValues = new HashMap<>();
        randomValues.put("int", 999);

        MutationHelper.mutateObjectFields(obj, randomValues, false, null);

        // Outer field should be mutated
        assertEquals(999, obj.outerInt);
        // Nested object fields should NOT be mutated (non-recursive)
        assertEquals(100, obj.nested.intField);
    }

    @Test
    void testMutateNestedObjectRecursive() {
        OuterObject obj = new OuterObject();
        Map<String, Object> randomValues = new HashMap<>();
        randomValues.put("int", 999);
        randomValues.put("string", "mutated");

        MutationHelper.mutateObjectFields(obj, randomValues, true, null);

        // Outer field should be mutated
        assertEquals(999, obj.outerInt);
        // Nested object fields should also be mutated (recursive)
        assertEquals(999, obj.nested.intField);
        assertEquals("mutated", obj.nested.stringField);
    }

    @Test
    void testMutateWithNullRandomValues() {
        TestObject obj = new TestObject();

        MutationHelper.mutateObjectFields(obj, null, false, null);

        // With null random values, numeric fields get 0, boolean negated
        assertEquals(0, obj.intField);
        assertEquals(0L, obj.longField);
        assertFalse(obj.boolField);
    }

    @Test
    void testMutateShortByteChar() {
        TestObject obj = new TestObject();
        Map<String, Object> randomValues = new HashMap<>();
        randomValues.put("int", 65); // 'A' in ASCII

        MutationHelper.mutateObjectFields(obj, randomValues, false, null);

        assertEquals((short) 65, obj.shortField);
        assertEquals((byte) 65, obj.byteField);
        // char is based on (int % 26) + 'A'
        assertTrue(obj.charField >= 'A' && obj.charField <= 'Z');
    }
}
