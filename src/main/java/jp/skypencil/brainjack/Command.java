package jp.skypencil.brainjack;

import java.io.IOException;

enum Command {
	INCREMENT_DATA_POINTER {
		@Override
		byte getCharacter() {
			return '>';
		}

		@Override
		void execute(Context context) {
			context.dataPointer++;
			context.instructionPointer++;
		}
	},
	DECREMENT_DATA_POINTER {
		@Override
		byte getCharacter() {
			return '<';
		}

		@Override
		void execute(Context context) {
			context.dataPointer--;
			context.instructionPointer++;
		}
	},
	INCREMENT_DATA {
		@Override
		byte getCharacter() {
			return '+';
		}

		@Override
		void execute(Context context) {
			context.array[context.dataPointer]++;
			context.instructionPointer++;
		}
	},
	DECREMENT_DATA {
		@Override
		byte getCharacter() {
			return '-';
		}

		@Override
		void execute(Context context) {
			context.array[context.dataPointer]--;
			context.instructionPointer++;
		}
	},
	OUTPUT {
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
	},
	ACCEPT {
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
	},
	START_LOOP {
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
				if (context.instructionPointer >= context.commands.length) {
					throw new IllegalCommandsException("illegal pair of '[' and ']'");
				}
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
	},
	END_LOOP {
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
	};

	private static final char CHAR_START_LOOP = '[';
	private static final char CHAR_END_LOOP = ']';
	abstract byte getCharacter();
	abstract void execute(Context context) throws IOException;
}
