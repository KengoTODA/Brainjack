package jp.skypencil.brainjack;

import java.io.IOException;

import javax.annotation.Nonnull;

import jp.skypencil.brainjack.Command.Accept;
import jp.skypencil.brainjack.Command.DecrementData;
import jp.skypencil.brainjack.Command.DecrementDataPointer;
import jp.skypencil.brainjack.Command.EndLoop;
import jp.skypencil.brainjack.Command.IncrementData;
import jp.skypencil.brainjack.Command.IncrementDataPointer;
import jp.skypencil.brainjack.Command.Output;
import jp.skypencil.brainjack.Command.StartLoop;
import static com.google.common.base.Preconditions.checkNotNull;
import static jp.skypencil.brainjack.Command.CHAR_START_LOOP;
import static jp.skypencil.brainjack.Command.CHAR_END_LOOP;

public class InterpreterVisitor implements Visitor {
	private final Context context;

	InterpreterVisitor(@Nonnull Context context) {
		this.context = checkNotNull(context);
	}

	@Override
	public void visit(StartLoop startLoop) {
		byte data = context.array[context.dataPointer];
		if (data == 0) {
			jumpForward(context);
		} else {
			context.instructionPointer++;
		}
	}

	@Override
	public void visit(EndLoop endLoop) {
		byte data = context.array[context.dataPointer];
		if (data != 0) {
			jumpBack(context);
		} else {
			context.instructionPointer++;
		}
	}

	@Override
	public void visit(Accept accept) throws IOException {
		int data = context.input.read();
		context.array[context.dataPointer] = (byte) data;
		context.instructionPointer++;
	}

	@Override
	public void visit(Output output) throws IOException {
		int data = context.array[context.dataPointer];
		if (data < 0) {
			data += 256;
		}
		context.output.write(data);
		context.instructionPointer++;
	}

	@Override
	public void visit(DecrementData decrementData) {
		context.array[context.dataPointer]--;
		context.instructionPointer++;
	}

	@Override
	public void visit(IncrementData incrementData) {
		context.array[context.dataPointer]++;
		context.instructionPointer++;
	}

	@Override
	public void visit(DecrementDataPointer decrementDataPointer) {
		context.dataPointer--;
		context.instructionPointer++;
	}

	@Override
	public void visit(IncrementDataPointer incrementDataPointer) {
		context.dataPointer++;
		context.instructionPointer++;
	}

	private void jumpForward(@Nonnull Context context) {
		assert context != null;
		int counter = 1;
		byte[] commands = context.commands;
		while (counter > 0) {
			context.instructionPointer++;
			if (context.instructionPointer >= commands.length) {
				throw new IllegalArgumentException("illegal pair of '[' and ']'");
			}

			switch ((char) commands[context.instructionPointer]) {
				case CHAR_START_LOOP: counter++; break;
				case CHAR_END_LOOP: counter--; break;
			}
		}
	}

	private void jumpBack(@Nonnull Context context) {
		assert context != null;
		int counter = 1;
		byte[] commands = context.commands;
		while (counter > 0) {
			context.instructionPointer--;
			if (context.instructionPointer < 0) {
				throw new IllegalArgumentException("illegal pair of '[' and ']'");
			}

			switch ((char) commands[context.instructionPointer]) {
				case CHAR_START_LOOP: counter--; break;
				case CHAR_END_LOOP: counter++; break;
			}
		}
	}
}
