package io.chaosmesh.mutator.transformer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import static org.mockito.Mockito.*;
import static org.objectweb.asm.Opcodes.ASM9;

class LoggingMutatorTest {

    @Mock
    private MethodVisitor mv;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSuppressLoggingCalls() {
        LoggingMutator mutator = new LoggingMutator(ASM9, mv, "suppress");

        mutator.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "org/slf4j/Logger", "debug", "(Ljava/lang/String;)V", false);
        verify(mv, never()).visitMethodInsn(anyInt(), anyString(), eq("debug"), anyString(), anyBoolean());

        mutator.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "org/slf4j/Logger", "error", "(Ljava/lang/String;)V", false);
        verify(mv, never()).visitMethodInsn(anyInt(), anyString(), eq("error"), anyString(), anyBoolean());
    }

    @Test
    void testNonLoggingMethodNotAffected() {
        LoggingMutator mutator = new LoggingMutator(ASM9, mv, "suppress");

        mutator.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "com/example/Test", "process", "()V", false);

        verify(mv).visitMethodInsn(Opcodes.INVOKEVIRTUAL, "com/example/Test", "process", "()V", false);
    }
}
