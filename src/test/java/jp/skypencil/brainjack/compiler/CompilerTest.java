// don't put in jp.skypencil.brainjack to test call from other package
package jp.skypencil.brainjack.compiler;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import jp.skypencil.brainjack.AbstractTest;
import jp.skypencil.brainjack.Compiler;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

import com.google.common.io.Files;

public class CompilerTest extends AbstractTest {

	private static final String PACKAGE_NAME = "pkg.";
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
		Compiler compiler = new Compiler();
		byte[] binary = compiler.compile(commands, PACKAGE_NAME + testName.getMethodName());

		// for javap debug
		Files.write(binary, new File("target", testName.getMethodName() + ".class"));

		Class<?> clazz = new OriginalClassLoader().defineClass(PACKAGE_NAME + testName.getMethodName(), binary);
		Method method = clazz.getMethod("main", String[].class);
		assertThat(Modifier.isStatic(method.getModifiers()), is(true));
		assertThat(Modifier.isPublic(method.getModifiers()), is(true));

		InputStream defaultInput = System.in;
		System.setIn(input);
		try {
			method.invoke(null, new Object[] { new String[]{ } });
		} catch (InvocationTargetException e) {
			throw e.getCause();
		} finally {
			System.setIn(defaultInput);
		}
		return byteArray.toString("UTF-8");
	}
}
