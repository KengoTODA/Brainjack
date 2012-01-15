package jp.skypencil.brainjack;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.Map;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Maps;

abstract class Command {
	private static final Map<Byte, Command> map;

	@Nonnull
	static Command[] values() {
		return new Command[] {
				new IncrementData(),
				new IncrementDataPointer(),
				new Accept(),
				new DecrementData(),
				new DecrementDataPointer(),
				new EndLoop(),
				new Output(),
				new StartLoop()
		};
	}

	static {
		map = Maps.newHashMap();
		for (Command command : values()) {
			map.put(command.getCharacter(), command);
		}
	}

	@Nullable
	static Command fromByte(@Nonnegative byte byteData) {
		return map.get(Byte.valueOf(byteData));
	}

	static class IncrementDataPointer extends Command {
		@Override
		@Nonnegative
		byte getCharacter() {
			return '>';
		}

		@Override
		void accept(@Nonnull Visitor visitor) {
			checkNotNull(visitor).visit(this);
		}
	};
	static class  DecrementDataPointer extends Command {
		@Override
		@Nonnegative
		byte getCharacter() {
			return '<';
		}

		@Override
		void accept(@Nonnull Visitor visitor) {
			checkNotNull(visitor).visit(this);
		}
	};
	static class IncrementData extends Command {
		@Override
		@Nonnegative
		byte getCharacter() {
			return '+';
		}

		@Override
		void accept(@Nonnull Visitor visitor) {
			checkNotNull(visitor).visit(this);
		}
	};
	static class DecrementData extends Command {
		@Override
		@Nonnegative
		byte getCharacter() {
			return '-';
		}

		@Override
		void accept(@Nonnull Visitor visitor) {
			checkNotNull(visitor).visit(this);
		}
	};
	static class Output extends Command {
		@Override
		@Nonnegative
		byte getCharacter() {
			return '.';
		}

		@Override
		void accept(@Nonnull Visitor visitor) throws IOException {
			checkNotNull(visitor).visit(this);
		}
	};
	static class Accept extends Command {
		@Override
		@Nonnegative
		byte getCharacter() {
			return ',';
		}

		@Override
		void accept(@Nonnull Visitor visitor) throws IOException {
			checkNotNull(visitor).visit(this);
		}
	};
	static class StartLoop extends Command {
		@Override
		@Nonnegative
		byte getCharacter() {
			return CHAR_START_LOOP;
		}

		@Override
		void accept(@Nonnull Visitor visitor) {
			checkNotNull(visitor).visit(this);
		}
	};
	static class EndLoop extends Command {
		@Override
		@Nonnegative
		byte getCharacter() {
			return CHAR_END_LOOP;
		}

		@Override
		void accept(@Nonnull Visitor visitor) {
			checkNotNull(visitor).visit(this);
		}
	};

	static final char CHAR_START_LOOP = '[';
	static final char CHAR_END_LOOP = ']';

	@Nonnegative
	abstract byte getCharacter();
	abstract void accept(@Nonnull Visitor visitor) throws IOException;
}
