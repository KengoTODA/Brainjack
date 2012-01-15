package jp.skypencil.brainjack;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import javax.annotation.Nonnull;

public class Context {
	@Override
	public String toString() {
		return "Context [instructionPointer=" + instructionPointer
				+ ", dataPointer=" + dataPointer + ", commands="
				+ Arrays.toString(commands) + ", array="
				+ Arrays.toString(array) + ", input=" + input + ", output="
				+ output + "]";
	}

	int instructionPointer;
	int dataPointer;
	byte[] commands = new byte[1024];
	byte[] array = new byte[30000];
	InputStream input;
	OutputStream output;

	private Context(String commands, InputStream input, OutputStream output) throws UnsupportedEncodingException {
		this.commands = checkNotNull(commands).getBytes("UTF-8");
		this.input = checkNotNull(input);
		this.output = checkNotNull(output);
	}

	@Nonnull
	static Context create(@Nonnull String commands, @Nonnull InputStream input, @Nonnull OutputStream output) {
		try {
			return new Context(commands, input, output);
		} catch (UnsupportedEncodingException unreachable) {
			throw new AssertionError(unreachable);
		}
	}
}
