package jp.skypencil.brainjack;

import java.io.IOException;

import jp.skypencil.brainjack.Command.ACCEPT;
import jp.skypencil.brainjack.Command.DECREMENT_DATA;
import jp.skypencil.brainjack.Command.DECREMENT_DATA_POINTER;
import jp.skypencil.brainjack.Command.END_LOOP;
import jp.skypencil.brainjack.Command.INCREMENT_DATA;
import jp.skypencil.brainjack.Command.INCREMENT_DATA_POINTER;
import jp.skypencil.brainjack.Command.OUTPUT;
import jp.skypencil.brainjack.Command.START_LOOP;
import static jp.skypencil.brainjack.Command.CHAR_START_LOOP;
import static jp.skypencil.brainjack.Command.CHAR_END_LOOP;

public class InterpreterVisitor implements Visitor {
	private final Context context;

	InterpreterVisitor(Context context) {
		this.context = context;
	}

	@Override
	public void visit(START_LOOP start_LOOP) {
		byte data = context.array[context.dataPointer];
		if (data == 0) {
			jumpForward(context);
		} else {
			context.instructionPointer++;
		}
	}

	@Override
	public void visit(END_LOOP end_LOOP) {
		byte data = context.array[context.dataPointer];
		if (data != 0) {
			jumpBack(context);
		} else {
			context.instructionPointer++;
		}
	}

	@Override
	public void visit(ACCEPT accept) throws IOException {
		byte data = (byte) context.input.read();
		if (data == -1) {
			data = 0;
		}
		context.array[context.dataPointer] = data;
		context.instructionPointer++;
	}

	@Override
	public void visit(OUTPUT output) throws IOException {
		byte data = context.array[context.dataPointer];
		context.output.write(data);
		context.instructionPointer++;
	}

	@Override
	public void visit(DECREMENT_DATA decrement_DATA) {
		context.array[context.dataPointer]--;
		context.instructionPointer++;
	}

	@Override
	public void visit(INCREMENT_DATA increment_DATA) {
		context.array[context.dataPointer]++;
		context.instructionPointer++;
	}

	@Override
	public void visit(DECREMENT_DATA_POINTER decrement_DATA_POINTER) {
		context.dataPointer--;
		context.instructionPointer++;
	}

	@Override
	public void visit(INCREMENT_DATA_POINTER increment_DATA_POINTER) {
		context.dataPointer++;
		context.instructionPointer++;
	}

	private void jumpForward(Context context) {
		int counter = 1;
		byte[] commands = context.commands;
		while (counter > 0) {
			context.instructionPointer++;
			if (context.instructionPointer >= commands.length) {
				throw new IllegalCommandsException("illegal pair of '[' and ']'");
			}

			switch ((char) commands[context.instructionPointer]) {
				case CHAR_START_LOOP: counter++; break;
				case CHAR_END_LOOP: counter--; break;
			}
		}
	}

	private void jumpBack(Context context) {
		int counter = 1;
		byte[] commands = context.commands;
		while (counter > 0) {
			context.instructionPointer--;
			if (context.instructionPointer < 0) {
				throw new IllegalCommandsException("illegal pair of '[' and ']'");
			}

			switch ((char) commands[context.instructionPointer]) {
				case CHAR_START_LOOP: counter--; break;
				case CHAR_END_LOOP: counter++; break;
			}
		}
	}
}
