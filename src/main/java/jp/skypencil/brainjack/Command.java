package jp.skypencil.brainjack;

import java.io.IOException;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

abstract class Command {
	static Command[] values() {
		return new Command[] {
				new INCREMENT_DATA(),
				new INCREMENT_DATA_POINTER(),
				new ACCEPT(),
				new DECREMENT_DATA(),
				new DECREMENT_DATA_POINTER(),
				new END_LOOP(),
				new OUTPUT(),
				new START_LOOP()
		};
	}
	static class INCREMENT_DATA_POINTER extends Command {
		@Override
		byte getCharacter() {
			return '>';
		}

		@Override
		void execute(Context context) {
			context.dataPointer++;
			context.instructionPointer++;
		}

		@Override
		void accept(Visitor visitor) {
			visitor.visit(this);
		}
	};
	static class  DECREMENT_DATA_POINTER extends Command {
		@Override
		byte getCharacter() {
			return '<';
		}

		@Override
		void execute(Context context) {
			context.dataPointer--;
			context.instructionPointer++;
		}

		@Override
		void accept(Visitor visitor) {
			visitor.visit(this);
		}
	};
	static class INCREMENT_DATA extends Command {
		@Override
		byte getCharacter() {
			return '+';
		}

		@Override
		void execute(Context context) {
			context.array[context.dataPointer]++;
			context.instructionPointer++;
		}

		@Override
		void accept(Visitor visitor) {
			visitor.visit(this);
		}
	};
	static class DECREMENT_DATA extends Command {
		@Override
		byte getCharacter() {
			return '-';
		}

		@Override
		void execute(Context context) {
			context.array[context.dataPointer]--;
			context.instructionPointer++;
		}

		@Override
		void accept(Visitor visitor) {
			visitor.visit(this);
		}
	};
	static class OUTPUT extends Command {
		@Override
		byte getCharacter() {
			return '.';
		}

		@Override
		void execute(Context context) throws IOException {
			byte data = context.array[context.dataPointer];
			context.output.write(data);
			context.instructionPointer++;
		}

		@Override
		void accept(Visitor visitor) {
			visitor.visit(this);
		}
	};
	static class ACCEPT extends Command {
		@Override
		byte getCharacter() {
			return ',';
		}

		@Override
		void execute(Context context) throws IOException {
			byte data = (byte) context.input.read();
			if (data == -1) {
				data = 0;
			}
			context.array[context.dataPointer] = data;
			context.instructionPointer++;
		}

		@Override
		void accept(Visitor visitor) {
			visitor.visit(this);
		}
	};
	static class START_LOOP extends Command {
		@Override
		byte getCharacter() {
			return CHAR_START_LOOP;
		}

		@Override
		void execute(Context context) {
			byte data = context.array[context.dataPointer];
			if (data == 0) {
				jumpForward(context);
			} else {
				context.instructionPointer++;
			}
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

		@Override
		void accept(Visitor visitor) {
			visitor.visit(this);
		}
	};
	static class END_LOOP extends Command {
		@Override
		byte getCharacter() {
			return CHAR_END_LOOP;
		}

		@Override
		void execute(Context context) {
			byte data = context.array[context.dataPointer];
			if (data != 0) {
				jumpBack(context);
			} else {
				context.instructionPointer++;
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

		@Override
		void accept(Visitor visitor) {
			visitor.visit(this);
		}
	};

	private static final char CHAR_START_LOOP = '[';
	private static final char CHAR_END_LOOP = ']';

	@Nonnegative
	abstract byte getCharacter();
	abstract void execute(@Nonnull Context context) throws IOException;
	abstract void accept(@Nonnull Visitor visitor);
}
