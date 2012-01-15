package jp.skypencil.brainjack;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import com.google.common.base.Strings;

public abstract class AbstractTest {
	protected abstract String execute(String commands, InputStream input) throws Throwable;

	private String execute(String commands) throws IOException {
		try {
			return execute(commands, new ByteArrayInputStream(new byte[0]));
		} catch (IOException | IllegalCommandsException e) {
			throw e;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	private static final String CMD_HELLO_WORLD = "+++++++++[>++++++++>+++++++++++>+++++<<<-]>.>++.+++++++..+++.>-.------------.<++++++++.--------.+++.------.--------.>+.";

	@Test
	public void testHelloWorld() throws IOException {
		assertThat(execute(CMD_HELLO_WORLD), equalTo("Hello, world!"));
	}

	@Test(expected=IllegalCommandsException.class)
	public void testEndlessLoop() throws IOException {
		execute("[");
	}

	@Test
	public void testStartlessLoop() throws IOException {
		assertThat(execute("]"), equalTo(""));
	}

	@Test
	public void testEndlessLoopWithNonZeroData() throws IOException {
		execute("+[");
	}

	@Test(expected=IllegalCommandsException.class)
	public void testStartlessLoopWithNonZeroData() throws IOException {
		execute("+]");
	}

	@Test
	public void testReverse() throws Throwable {
		InputStream input = new ByteArrayInputStream("abcde".getBytes("UTF-8"));
		assertThat(execute(">,[>,]<[.<]", input), equalTo("edcba"));
	}

	@Test
	public void testUpperCase() throws Throwable {
		InputStream input = new ByteArrayInputStream("abcde".getBytes("UTF-8"));
		assertThat(execute(",[" + Strings.repeat("-", 32) + ".,]", input), equalTo("ABCDE"));
	}
}
