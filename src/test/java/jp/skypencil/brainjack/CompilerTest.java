package jp.skypencil.brainjack;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.junit.After;
import org.junit.Before;

public class CompilerTest extends AbstractTest {

	private static final String DUMMY_CLASS_NAME = "pkg.CompiledClass";
	private ByteArrayOutputStream byteArray;
	private PrintStream defaultOutput;

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
			throws IOException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Compiler compiler = new Compiler();
		byte[] binary = compiler.compile(commands, DUMMY_CLASS_NAME);
		Class<?> clazz = new OriginalClassLoader().defineClass(DUMMY_CLASS_NAME, binary);
		Method method = clazz.getMethod("main", String[].class);
		assertThat(Modifier.isStatic(method.getModifiers()), is(true));
		assertThat(Modifier.isPublic(method.getModifiers()), is(true));

		InputStream defaultInput = System.in;
		System.setIn(input);
		try {
			method.invoke(null, new Object[] { new String[]{ } });
		} finally {
			System.setIn(defaultInput);
		}
		return byteArray.toString("UTF-8");
	}

	private static class OriginalClassLoader extends ClassLoader {
		public Class<?> defineClass(String name, byte[] b) {
			return defineClass(name, b, 0, b.length);
		}
	}}
