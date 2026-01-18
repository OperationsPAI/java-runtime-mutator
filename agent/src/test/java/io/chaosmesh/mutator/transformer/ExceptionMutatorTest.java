package io.chaosmesh.mutator.transformer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import static org.mockito.Mockito.*;
import static org.objectweb.asm.Opcodes.ASM9;

class ExceptionMutatorTest {

    @Mock
    private MethodVisitor mv;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testExceptionInjectionWithMessage() {
        ExceptionMutator mutator = new ExceptionMutator(ASM9, mv, "java.lang.RuntimeException", "Test error");

        mutator.visitCode();

        verify(mv).visitCode();
        verify(mv).visitTypeInsn(Opcodes.NEW, "java/lang/RuntimeException");
        verify(mv).visitInsn(Opcodes.DUP);
        verify(mv).visitLdcInsn("Test error");
        verify(mv).visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "(Ljava/lang/String;)V", false);
        verify(mv).visitInsn(Opcodes.ATHROW);
    }

    @Test
    void testExceptionInjectionWithoutMessage() {
        ExceptionMutator mutator = new ExceptionMutator(ASM9, mv, "java.lang.IllegalStateException", null);

        mutator.visitCode();

        verify(mv).visitTypeInsn(Opcodes.NEW, "java/lang/IllegalStateException");
        verify(mv, never()).visitLdcInsn(anyString());
        verify(mv).visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/IllegalStateException", "<init>", "()V", false);
    }
}
