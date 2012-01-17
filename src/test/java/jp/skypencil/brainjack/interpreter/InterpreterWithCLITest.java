// don't put in jp.skypencil.brainjack to test call from other package
package jp.skypencil.brainjack.interpreter;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

import jp.skypencil.brainjack.AbstractTest;
import jp.skypencil.brainjack.Main;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

public class InterpreterWithCLITest extends AbstractTest {
	private static final String ROOT_DIR_PATH = "target/interpreterWithCLI";
	private static final String CLASS_NAME = "Test";
	private ByteArrayOutputStream byteArray;
	private PrintStream defaultOutput;
	@Rule
	public TestName testName = new TestName();

	@Before
	public void switchOutput() {
		byteArray = new ByteArrayOutputStream();
		PrintStream stream = new PrintStream(byteArray);
		defaultOutput = System.out;
		System.setOut(stream);
	}

	@After
	public void resetOutput() {
		System.setOut(defaultOutput);
	}

	@Override
	protected String execute(String commands, InputStream input)
			throws Throwable {
		InputStream defaultInput = System.in;
		System.setIn(input);
		try {
			new Main().run(new String[] { "interpret", "-c", commands, "-d", ROOT_DIR_PATH, "-n", CLASS_NAME });
		} finally {
			System.setIn(defaultInput);
		}
		return byteArray.toString("UTF-8");
	}
}
