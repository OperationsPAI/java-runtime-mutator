package io.chaosmesh.mutator.transformer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import static org.mockito.Mockito.*;
import static org.objectweb.asm.Opcodes.ASM9;

class MethodCallMutatorTest {

    @Mock
    private MethodVisitor mv;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSkipMethodCall() {
        MethodCallMutator mutator = new MethodCallMutator(ASM9, mv, "skip", "targetMethod");

        mutator.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "com/example/Test", "targetMethod", "(I)V", false);

        verify(mv, times(2)).visitInsn(Opcodes.POP);
        verify(mv, never()).visitMethodInsn(anyInt(), anyString(), anyString(), anyString(), anyBoolean());
    }

    @Test
    void testSkipStaticMethodCall() {
        MethodCallMutator mutator = new MethodCallMutator(ASM9, mv, "skip", "targetMethod");

        mutator.visitMethodInsn(Opcodes.INVOKESTATIC, "com/example/Test", "targetMethod", "(I)V", false);

        verify(mv).visitInsn(Opcodes.POP);
        verify(mv, never()).visitMethodInsn(anyInt(), anyString(), anyString(), anyString(), anyBoolean());
    }

    @Test
    void testNullReturnStrategy() {
        MethodCallMutator mutator = new MethodCallMutator(ASM9, mv, "null_return", "targetMethod");

        mutator.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "com/example/Test", "targetMethod", "()Ljava/lang/String;", false);

        verify(mv).visitInsn(Opcodes.POP);
        verify(mv).visitInsn(Opcodes.ACONST_NULL);
    }

    @Test
    void testNoMutationForDifferentMethod() {
        MethodCallMutator mutator = new MethodCallMutator(ASM9, mv, "skip", "targetMethod");

        mutator.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "com/example/Test", "otherMethod", "(I)V", false);

        verify(mv).visitMethodInsn(Opcodes.INVOKEVIRTUAL, "com/example/Test", "otherMethod", "(I)V", false);
    }
}
