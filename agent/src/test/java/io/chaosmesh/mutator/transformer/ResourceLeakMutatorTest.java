package io.chaosmesh.mutator.transformer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import static org.mockito.Mockito.*;
import static org.objectweb.asm.Opcodes.ASM9;

class ResourceLeakMutatorTest {

    @Mock
    private MethodVisitor mv;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSkipCloseMethod() {
        ResourceLeakMutator mutator = new ResourceLeakMutator(ASM9, mv, "skip_close");

        mutator.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/InputStream", "close", "()V", false);

        verify(mv, never()).visitMethodInsn(anyInt(), anyString(), anyString(), anyString(), anyBoolean());
    }

    @Test
    void testNonCloseMethodNotAffected() {
        ResourceLeakMutator mutator = new ResourceLeakMutator(ASM9, mv, "skip_close");

        mutator.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "com/example/Test", "process", "()V", false);

        verify(mv).visitMethodInsn(Opcodes.INVOKEVIRTUAL, "com/example/Test", "process", "()V", false);
    }
}
