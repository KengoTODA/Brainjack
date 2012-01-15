package jp.skypencil.brainjack;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.DUP2;
import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Opcodes.IALOAD;
import static org.objectweb.asm.Opcodes.IASTORE;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFNE;
import static org.objectweb.asm.Opcodes.IADD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.ISUB;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.NEWARRAY;
import static org.objectweb.asm.Opcodes.PUTSTATIC;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.T_INT;

import java.io.InputStream;
import java.util.Deque;

import javax.annotation.Nonnull;

import jp.skypencil.brainjack.Command.ACCEPT;
import jp.skypencil.brainjack.Command.DECREMENT_DATA;
import jp.skypencil.brainjack.Command.DECREMENT_DATA_POINTER;
import jp.skypencil.brainjack.Command.END_LOOP;
import jp.skypencil.brainjack.Command.INCREMENT_DATA;
import jp.skypencil.brainjack.Command.INCREMENT_DATA_POINTER;
import jp.skypencil.brainjack.Command.OUTPUT;
import jp.skypencil.brainjack.Command.START_LOOP;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import com.google.common.collect.Lists;

class CompilerVisitor implements Visitor {
	private final MethodVisitor methodVisitor;
	private final String innerFullClassName;
	private Deque<Label> loopBeginLabels = Lists.newLinkedList();
	private Deque<Label> loopEndLabels = Lists.newLinkedList();
	private static final String DATA_POINTER = "_dataPointer";
	private static final String DATA_POINTER_TYPE = Type.INT_TYPE.getDescriptor();
	private static final String DATA_ARRAY = "_dataArray";
	private static final String DATA_ARRAY_TYPE = Type.getDescriptor(int[].class);
	private static final int DATA_ARRAY_SIZE = 30000;

	CompilerVisitor(@Nonnull String innerFullClassName, @Nonnull MethodVisitor visitor) {
		this.innerFullClassName = checkNotNull(innerFullClassName);
		this.methodVisitor = checkNotNull(visitor);
		this.methodVisitor.visitLdcInsn(DATA_ARRAY_SIZE);
		this.methodVisitor.visitIntInsn(NEWARRAY, T_INT);
		this.methodVisitor.visitFieldInsn(PUTSTATIC, innerFullClassName, DATA_ARRAY, DATA_ARRAY_TYPE);
	}

	void createField(ClassVisitor classVisitor) {
		classVisitor.visitField(ACC_PUBLIC | ACC_STATIC, DATA_POINTER, DATA_POINTER_TYPE, null, 0).visitEnd();
		classVisitor.visitField(ACC_PUBLIC | ACC_STATIC, DATA_ARRAY, DATA_ARRAY_TYPE, null, null).visitEnd();
	}

	@Override
	public void visit(START_LOOP start_LOOP) {
		Label loopStart = new Label();
		Label loopEnd = new Label();
		methodVisitor.visitLabel(loopStart);
		methodVisitor.visitFieldInsn(GETSTATIC, this.innerFullClassName, DATA_ARRAY, DATA_ARRAY_TYPE);
		methodVisitor.visitFieldInsn(GETSTATIC, this.innerFullClassName, DATA_POINTER, DATA_POINTER_TYPE);
		methodVisitor.visitInsn(IALOAD);
		methodVisitor.visitJumpInsn(IFEQ, loopEnd);
		loopBeginLabels.push(loopStart);
		loopEndLabels.push(loopEnd);
	}

	@Override
	public void visit(END_LOOP end_LOOP) {
		if (loopBeginLabels.isEmpty()) {
			methodVisitor.visitFieldInsn(GETSTATIC, this.innerFullClassName, DATA_ARRAY, DATA_ARRAY_TYPE);
			methodVisitor.visitFieldInsn(GETSTATIC, this.innerFullClassName, DATA_POINTER, DATA_POINTER_TYPE);
			methodVisitor.visitInsn(IALOAD);
			Label walkThrough = new Label();
			// throw IllegalCommandsException because there is no label to return
			methodVisitor.visitJumpInsn(IFEQ, walkThrough);
			methodVisitor.visitTypeInsn(NEW, Type.getInternalName(IllegalCommandsException.class));
			methodVisitor.visitInsn(DUP);
			methodVisitor.visitLdcInsn("illegal pair of '[' and ']'");
			methodVisitor.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(IllegalCommandsException.class), "<init>", "(Ljava/lang/String;)V");
			methodVisitor.visitInsn(ATHROW);
			methodVisitor.visitLabel(walkThrough);
		} else {
			methodVisitor.visitFieldInsn(GETSTATIC, this.innerFullClassName, DATA_ARRAY, DATA_ARRAY_TYPE);
			methodVisitor.visitFieldInsn(GETSTATIC, this.innerFullClassName, DATA_POINTER, DATA_POINTER_TYPE);
			methodVisitor.visitInsn(IALOAD);
			methodVisitor.visitJumpInsn(IFNE, loopBeginLabels.pop());
			methodVisitor.visitLabel(loopEndLabels.pop());
		}
	}

	@Override
	public void visit(ACCEPT accept) {
		methodVisitor.visitFieldInsn(GETSTATIC, this.innerFullClassName, DATA_ARRAY, DATA_ARRAY_TYPE);
		methodVisitor.visitFieldInsn(GETSTATIC, this.innerFullClassName, DATA_POINTER, DATA_POINTER_TYPE);
		methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "in", Type.getDescriptor(InputStream.class));
		methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/InputStream", "read", "()I");
		methodVisitor.visitInsn(DUP);
		methodVisitor.visitInsn(ICONST_1);
		methodVisitor.visitInsn(IADD);
		Label successToRead = new Label();
		methodVisitor.visitJumpInsn(IFNE, successToRead);
		methodVisitor.visitInsn(POP);
		methodVisitor.visitInsn(ICONST_0);
		methodVisitor.visitLabel(successToRead);
		methodVisitor.visitInsn(IASTORE);
	}

	@Override
	public void visit(OUTPUT output) {
		methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
		methodVisitor.visitFieldInsn(GETSTATIC, this.innerFullClassName, DATA_ARRAY, DATA_ARRAY_TYPE);
		methodVisitor.visitFieldInsn(GETSTATIC, this.innerFullClassName, DATA_POINTER, DATA_POINTER_TYPE);
		methodVisitor.visitInsn(IALOAD);
		methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "print", "(C)V");
	}

	@Override
	public void visit(DECREMENT_DATA decrement_DATA) {
		methodVisitor.visitFieldInsn(GETSTATIC, this.innerFullClassName, DATA_ARRAY, DATA_ARRAY_TYPE);
		methodVisitor.visitFieldInsn(GETSTATIC, this.innerFullClassName, DATA_POINTER, DATA_POINTER_TYPE);
		methodVisitor.visitInsn(DUP2);
		methodVisitor.visitInsn(IALOAD);
		methodVisitor.visitInsn(ICONST_1);
		methodVisitor.visitInsn(ISUB);
		methodVisitor.visitInsn(IASTORE);
	}

	@Override
	public void visit(INCREMENT_DATA increment_DATA) {
		methodVisitor.visitFieldInsn(GETSTATIC, this.innerFullClassName, DATA_ARRAY, DATA_ARRAY_TYPE);
		methodVisitor.visitFieldInsn(GETSTATIC, this.innerFullClassName, DATA_POINTER, DATA_POINTER_TYPE);
		methodVisitor.visitInsn(DUP2);
		methodVisitor.visitInsn(IALOAD);
		methodVisitor.visitInsn(ICONST_1);
		methodVisitor.visitInsn(IADD);
		methodVisitor.visitInsn(IASTORE);
	}

	@Override
	public void visit(DECREMENT_DATA_POINTER decrement_DATA_POINTER) {
		methodVisitor.visitFieldInsn(GETSTATIC, this.innerFullClassName, DATA_POINTER, DATA_POINTER_TYPE);
		methodVisitor.visitInsn(ICONST_1);
		methodVisitor.visitInsn(ISUB);
		methodVisitor.visitFieldInsn(PUTSTATIC, this.innerFullClassName, DATA_POINTER, DATA_POINTER_TYPE);
	}

	@Override
	public void visit(INCREMENT_DATA_POINTER increment_DATA_POINTER) {
		methodVisitor.visitFieldInsn(GETSTATIC, this.innerFullClassName, DATA_POINTER, DATA_POINTER_TYPE);
		methodVisitor.visitInsn(ICONST_1);
		methodVisitor.visitInsn(IADD);
		methodVisitor.visitFieldInsn(PUTSTATIC, this.innerFullClassName, DATA_POINTER, DATA_POINTER_TYPE);
	}

	void end() {
		methodVisitor.visitInsn(RETURN);
		for (Label label : loopEndLabels) {
			methodVisitor.visitLabel(label);
		}
		methodVisitor.visitTypeInsn(NEW, Type.getInternalName(IllegalCommandsException.class));
		methodVisitor.visitInsn(DUP);
		methodVisitor.visitLdcInsn("illegal pair of '[' and ']'");
		methodVisitor.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(IllegalCommandsException.class), "<init>", "(Ljava/lang/String;)V");
		methodVisitor.visitInsn(ATHROW);
		methodVisitor.visitMaxs(100, 100);
		methodVisitor.visitEnd();
	}
}
