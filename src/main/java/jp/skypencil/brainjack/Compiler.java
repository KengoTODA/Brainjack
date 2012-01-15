package jp.skypencil.brainjack;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V1_5;

import java.io.IOException;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import com.google.common.base.Charsets;

public class Compiler {
	private final Logger logger = Logger.getLogger(getClass().getName());

	@Nonnull
	public byte[] compile(@Nonnull String commands, @Nonnull String classFullName) {
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
		createMain(cw, commands, innerFullClassName);
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

	private void createMain(ClassWriter cw, String commands,
			String innerFullClassName) {
		MethodVisitor mv = cw.visitMethod(
				ACC_PUBLIC | ACC_STATIC,
				"main",
				Type.getMethodDescriptor(Type.VOID_TYPE, new Type[]{Type.getObjectType("[Ljava/lang/String;")}),
				null, new String[]{ Type.getInternalName(Throwable.class) });

		CompilerVisitor visitor = new CompilerVisitor(innerFullClassName, mv);
		visitor.createField(cw);
		createCommands(visitor, commands, innerFullClassName);
		visitor.end();
	}

	private void createCommands(CompilerVisitor visitor, String commands,
			String innerFullClassName) {
		for (byte byteData : commands.getBytes(Charsets.UTF_8)) {
			Command command = Command.fromByte(byteData);
			if (command == null) {
				logger.warning("unknown command: " + Byte.toString(byteData));
			} else {
				try {
					command.accept(visitor);
				} catch (IOException unreachable) {
					// CompilerVisitor doesn't throw IOException
					throw new AssertionError(unreachable);
				}
			}
		}
	}
}
