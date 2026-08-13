import org.jetbrains.org.objectweb.asm.ClassReader;
import org.jetbrains.org.objectweb.asm.ClassWriter;
import org.jetbrains.org.objectweb.asm.Opcodes;
import org.jetbrains.org.objectweb.asm.tree.AbstractInsnNode;
import org.jetbrains.org.objectweb.asm.tree.ClassNode;
import org.jetbrains.org.objectweb.asm.tree.InsnList;
import org.jetbrains.org.objectweb.asm.tree.MethodInsnNode;
import org.jetbrains.org.objectweb.asm.tree.MethodNode;
import org.jetbrains.org.objectweb.asm.tree.VarInsnNode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Copies IntelliJ GUI Designer generated methods into a newly compiled class.
 */
public class FormSetupMethodCopier {
    private static final String SETUP_METHOD_NAME = "$$$setupUI$$$";

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            throw new IllegalArgumentException("Usage: FormSetupMethodCopier <sourceClass> <targetClass>");
        }

        ClassNode sourceClass = readClass(new File(args[0]));
        ClassNode targetClass = readClass(new File(args[1]));
        copyGeneratedMethods(sourceClass, targetClass);
        injectSetupInvocation(targetClass);

        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        targetClass.accept(classWriter);
        try (FileOutputStream outputStream = new FileOutputStream(args[1])) {
            outputStream.write(classWriter.toByteArray());
        }
    }

    private static ClassNode readClass(File classFile) throws Exception {
        try (FileInputStream inputStream = new FileInputStream(classFile)) {
            ClassNode classNode = new ClassNode();
            new ClassReader(inputStream).accept(classNode, 0);
            return classNode;
        }
    }

    private static void copyGeneratedMethods(ClassNode sourceClass, ClassNode targetClass) {
        boolean hasSetupMethod = false;
        for (MethodNode sourceMethod : sourceClass.methods) {
            if (!sourceMethod.name.startsWith("$$$")) {
                continue;
            }
            targetClass.methods.removeIf(method -> sourceMethod.name.equals(method.name)
                    && sourceMethod.desc.equals(method.desc));
            targetClass.methods.add(sourceMethod);
            hasSetupMethod |= SETUP_METHOD_NAME.equals(sourceMethod.name);
        }
        if (!hasSetupMethod) {
            throw new IllegalStateException("Missing generated method: " + SETUP_METHOD_NAME);
        }
    }

    private static void injectSetupInvocation(ClassNode targetClass) {
        MethodNode constructor = findMethod(targetClass, "<init>");
        if (constructor == null) {
            throw new IllegalStateException("Missing constructor");
        }
        for (AbstractInsnNode instruction : constructor.instructions) {
            if (instruction instanceof MethodInsnNode) {
                MethodInsnNode methodInvocation = (MethodInsnNode) instruction;
                if (SETUP_METHOD_NAME.equals(methodInvocation.name)) {
                    return;
                }
            }
        }

        AbstractInsnNode returnInstruction = constructor.instructions.getLast();
        InsnList setupInvocation = new InsnList();
        setupInvocation.add(new VarInsnNode(Opcodes.ALOAD, 0));
        setupInvocation.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, targetClass.name, SETUP_METHOD_NAME, "()V", false));
        constructor.instructions.insertBefore(returnInstruction, setupInvocation);
    }

    private static MethodNode findMethod(ClassNode classNode, String methodName) {
        for (MethodNode method : classNode.methods) {
            if (methodName.equals(method.name)) {
                return method;
            }
        }
        return null;
    }
}
