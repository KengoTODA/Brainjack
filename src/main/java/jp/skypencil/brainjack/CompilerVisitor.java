package jp.skypencil.brainjack;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.BALOAD;
import static org.objectweb.asm.Opcodes.BASTORE;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.DUP2;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.IADD;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFNE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.NEWARRAY;
import static org.objectweb.asm.Opcodes.PUTSTATIC;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.T_BYTE;

import java.io.InputStream;
import java.util.Deque;

import javax.annotation.Nonnull;

import jp.skypencil.brainjack.Command.Accept;
import jp.skypencil.brainjack.Command.DecrementData;
import jp.skypencil.brainjack.Command.DecrementDataPointer;
import jp.skypencil.brainjack.Command.EndLoop;
import jp.skypencil.brainjack.Command.IncrementData;
import jp.skypencil.brainjack.Command.IncrementDataPointer;
import jp.skypencil.brainjack.Command.Output;
import jp.skypencil.brainjack.Command.StartLoop;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import com.google.common.collect.Lists;

class CompilerVisitor implements Visitor {
	private @Nonnull final MethodVisitor methodVisitor;
	private @Nonnull final String innerFullClassName;
	private @Nonnull Deque<Label> loopBeginLabels = Lists.newLinkedList();
	private @Nonnull Deque<Label> loopEndLabels = Lists.newLinkedList();
	private static final String DATA_POINTER = "_dataPointer";
	private static final String DATA_POINTER_TYPE = Type.INT_TYPE.getDescriptor();
	private static final String DATA_ARRAY = "_dataArray";
	private static final String DATA_ARRAY_TYPE = Type.getDescriptor(byte[].class);
	private static final int DATA_ARRAY_SIZE = 30000;

	CompilerVisitor(@Nonnull String innerFullClassName, @Nonnull MethodVisitor visitor) {
		this.innerFullClassName = checkNotNull(innerFullClassName);
		this.methodVisitor = checkNotNull(visitor);
		this.methodVisitor.visitLdcInsn(DATA_ARRAY_SIZE);
		this.methodVisitor.visitIntInsn(NEWARRAY, T_BYTE);
		this.methodVisitor.visitFieldInsn(PUTSTATIC, innerFullClassName, DATA_ARRAY, DATA_ARRAY_TYPE);
		this.methodVisitor.visitInsn(ICONST_0);
		this.methodVisitor.visitFieldInsn(PUTSTATIC, innerFullClassName, DATA_POINTER, DATA_POINTER_TYPE);
	}

	void createField(@Nonnull ClassVisitor classVisitor) {
		checkNotNull(classVisitor);
		classVisitor.visitField(ACC_PUBLIC | ACC_STATIC, DATA_POINTER, DATA_POINTER_TYPE, null, 0).visitEnd();
		classVisitor.visitField(ACC_PUBLIC | ACC_STATIC, DATA_ARRAY, DATA_ARRAY_TYPE, null, null).visitEnd();
	}

	@Override
	public void visit(StartLoop startLoop) {
		Label loopStart = new Label();
		Label loopEnd = new Label();
		methodVisitor.visitLabel(loopStart);
		methodVisitor.visitFieldInsn(GETSTATIC, this.innerFullClassName, DATA_ARRAY, DATA_ARRAY_TYPE);
		methodVisitor.visitFieldInsn(GETSTATIC, this.innerFullClassName, DATA_POINTER, DATA_POINTER_TYPE);
		methodVisitor.visitInsn(BALOAD);
		methodVisitor.visitJumpInsn(IFEQ, loopEnd);
		loopBeginLabels.push(loopStart);
		loopEndLabels.push(loopEnd);
	}

	@Override
	public void visit(EndLoop endLoop) {
		methodVisitor.visitFieldInsn(GETSTATIC, this.innerFullClassName, DATA_ARRAY, DATA_ARRAY_TYPE);
		methodVisitor.visitFieldInsn(GETSTATIC, this.innerFullClassName, DATA_POINTER, DATA_POINTER_TYPE);
		methodVisitor.visitInsn(BALOAD);

		if (loopBeginLabels.isEmpty()) {
			Label walkThrough = new Label();
			// throw IllegalCommandsException because there is no label to return
			methodVisitor.visitJumpInsn(IFEQ, walkThrough);
			methodVisitor.visitTypeInsn(NEW, Type.getInternalName(IllegalArgumentException.class));
			methodVisitor.visitInsn(DUP);
			methodVisitor.visitLdcInsn("illegal pair of '[' and ']'");
			methodVisitor.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(IllegalArgumentException.class), "<init>", "(Ljava/lang/String;)V");
			methodVisitor.visitInsn(ATHROW);
			methodVisitor.visitLabel(walkThrough);
		} else {
			methodVisitor.visitJumpInsn(IFNE, loopBeginLabels.pop());
			methodVisitor.visitLabel(loopEndLabels.pop());
		}
	}

	@Override
	public void visit(Accept accept) {
		methodVisitor.visitFieldInsn(GETSTATIC, this.innerFullClassName, DATA_ARRAY, DATA_ARRAY_TYPE);
		methodVisitor.visitFieldInsn(GETSTATIC, this.innerFullClassName, DATA_POINTER, DATA_POINTER_TYPE);
		methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "in", Type.getDescriptor(InputStream.class));
		methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/InputStream", "read", "()I");
		methodVisitor.visitInsn(BASTORE);
	}

	@Override
	public void visit(Output output) {
		methodVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
		methodVisitor.visitFieldInsn(GETSTATIC, this.innerFullClassName, DATA_ARRAY, DATA_ARRAY_TYPE);
		methodVisitor.visitFieldInsn(GETSTATIC, this.innerFullClassName, DATA_POINTER, DATA_POINTER_TYPE);
		methodVisitor.visitInsn(BALOAD);
		methodVisitor.visitInsn(DUP);
		Label noNeedToConvert = new Label();
		methodVisitor.visitJumpInsn(Opcodes.IFGT, noNeedToConvert);

		// loaded value is negative, so we should make it positive.
		methodVisitor.visitLdcInsn(256);
		methodVisitor.visitInsn(IADD);

		methodVisitor.visitLabel(noNeedToConvert);
		methodVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "print", "(C)V");
	}

	@Override
	public void visit(DecrementData decrementData) {
		methodVisitor.visitFieldInsn(GETSTATIC, this.innerFullClassName, DATA_ARRAY, DATA_ARRAY_TYPE);
		methodVisitor.visitFieldInsn(GETSTATIC, this.innerFullClassName, DATA_POINTER, DATA_POINTER_TYPE);
		methodVisitor.visitInsn(DUP2);
		methodVisitor.visitInsn(BALOAD);
		methodVisitor.visitLdcInsn(-1);
		methodVisitor.visitInsn(IADD);
		methodVisitor.visitInsn(BASTORE);
	}

	@Override
	public void visit(IncrementData incrementData) {
		methodVisitor.visitFieldInsn(GETSTATIC, this.innerFullClassName, DATA_ARRAY, DATA_ARRAY_TYPE);
		methodVisitor.visitFieldInsn(GETSTATIC, this.innerFullClassName, DATA_POINTER, DATA_POINTER_TYPE);
		methodVisitor.visitInsn(DUP2);
		methodVisitor.visitInsn(BALOAD);
		methodVisitor.visitInsn(ICONST_1);
		methodVisitor.visitInsn(IADD);
		methodVisitor.visitInsn(BASTORE);
	}

	@Override
	public void visit(DecrementDataPointer decrementDataPointer) {
		methodVisitor.visitFieldInsn(GETSTATIC, this.innerFullClassName, DATA_POINTER, DATA_POINTER_TYPE);
		methodVisitor.visitLdcInsn(-1);
		methodVisitor.visitInsn(IADD);
		methodVisitor.visitFieldInsn(PUTSTATIC, this.innerFullClassName, DATA_POINTER, DATA_POINTER_TYPE);
	}

	@Override
	public void visit(IncrementDataPointer incrementDataPointer) {
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
		methodVisitor.visitTypeInsn(NEW, Type.getInternalName(IllegalArgumentException.class));
		methodVisitor.visitInsn(DUP);
		methodVisitor.visitLdcInsn("illegal pair of '[' and ']'");
		methodVisitor.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(IllegalArgumentException.class), "<init>", "(Ljava/lang/String;)V");
		methodVisitor.visitInsn(ATHROW);
		methodVisitor.visitMaxs(100, 100);
		methodVisitor.visitEnd();
	}
}
