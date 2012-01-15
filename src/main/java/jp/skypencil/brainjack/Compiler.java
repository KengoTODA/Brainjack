package jp.skypencil.brainjack;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.objectweb.asm.Opcodes.*;

import java.lang.reflect.Method;
import java.util.EnumSet;

import javax.annotation.Nonnull;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

public class Compiler {
	byte[] compile(@Nonnull String commands, @Nonnull String classFullName) {
		checkNotNull(commands);
		checkNotNull(classFullName);
		String innerFullClassName = classFullName.replaceAll("\\.", "/");
		String className = classFullName;

		if (className.contains(".")) {
			className = className.substring(className.lastIndexOf('.') + 1);
		}

		ClassWriter cw = new ClassWriter(0);
		cw.visit(V1_5, ACC_PUBLIC, innerFullClassName, null, "java/lang/Object", null);
		createConstructor(cw, innerFullClassName);
		EnumSet<Command> usedCommand = createMain(cw, commands, innerFullClassName);
		createMethod(cw, usedCommand);
		cw.visitEnd();

		return cw.toByteArray();
	}

	private void createConstructor(ClassWriter cw, String innerFullClassName) {
		MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
		mv.visitMaxs(1, 1);
		mv.visitVarInsn(ALOAD, 0); // push `this` to the operand stack
		mv.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(Object.class), "<init>", "()V"); // call the constructor of super class

		mv.visitInsn(RETURN);
		mv.visitEnd();
	}

	private EnumSet<Command> createMain(ClassWriter cw, String commands,
			String innerFullClassName) {
		// TODO Auto-generated method stub
		return null;
	}

	private void createMethod(ClassWriter cw, EnumSet<Command> usedCommand) {
		for (Command command : usedCommand) {
			Class<Command> clazz = command.getDeclaringClass();
			try {
				Method method = clazz.getDeclaredMethod("execute", Context.class);
			} catch (SecurityException e) {
				throw new RuntimeException();
			} catch (NoSuchMethodException unreachable) {
				throw new AssertionError(unreachable);
			}
		}
	}

}
