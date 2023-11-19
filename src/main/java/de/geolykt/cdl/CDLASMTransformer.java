package de.geolykt.cdl;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.slf4j.LoggerFactory;

import de.geolykt.starloader.transformers.ASMTransformer;

public class CDLASMTransformer extends ASMTransformer {

    private static final String TARGET = "snoddasmannen/galimulator/Person";
    private static final String ATOMIC_INT = "java/util/concurrent/atomic/AtomicInteger";

    @Override
    public boolean accept(@NotNull ClassNode node) {
        if (!node.name.equals(TARGET)) {
            return false;
        }
        int modified = 0;
        
        for (MethodNode method : node.methods) {
            if (!method.name.equals("<init>")) {
                continue;
            }
            for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (insn.getOpcode() != Opcodes.BIPUSH) {
                    continue;
                }
                IntInsnNode iinsn = (IntInsnNode) insn;
                if (iinsn.operand != 20) {
                    continue;
                }
                FieldInsnNode injectFInsn = new FieldInsnNode(Opcodes.GETSTATIC, "de/geolykt/cdl/CDLExtension", "MAX_FAMILY_SIZE", "L" + ATOMIC_INT + ";");
                MethodInsnNode injectMInsn = new MethodInsnNode(Opcodes.INVOKEVIRTUAL, ATOMIC_INT, "get", "()I");
                method.instructions.insertBefore(insn, injectFInsn);
                method.instructions.insertBefore(insn, injectMInsn);
                method.instructions.remove(insn);
                modified++;
            }
        }

        if (modified != 2) {
            LoggerFactory.getLogger(getClass()).error("Modified the maximum family size constant {} times, expected 2. This may be a bug.", modified);
        }

        return modified != 0;
    }

    @Override
    public boolean isValidTarget(@NotNull String internalName) {
        return TARGET.equals(internalName);
    }

    @Override
    public int getPriority() {
        return 110;
    }
}
