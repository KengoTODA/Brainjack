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
		void accept(Visitor visitor) throws IOException {
			visitor.visit(this);
		}
	};
	static class ACCEPT extends Command {
		@Override
		byte getCharacter() {
			return ',';
		}

		@Override
		void accept(Visitor visitor) throws IOException {
			visitor.visit(this);
		}
	};
	static class START_LOOP extends Command {
		@Override
		byte getCharacter() {
			return CHAR_START_LOOP;
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
		void accept(Visitor visitor) {
			visitor.visit(this);
		}
	};

	static final char CHAR_START_LOOP = '[';
	static final char CHAR_END_LOOP = ']';

	@Nonnegative
	abstract byte getCharacter();
	abstract void accept(@Nonnull Visitor visitor) throws IOException;
}
