// don't put in jp.skypencil.brainjack to test call from other package
package jp.skypencil.brainjack.compiler;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import jp.skypencil.brainjack.AbstractTest;
import jp.skypencil.brainjack.Main;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.kohsuke.args4j.CmdLineException;

import com.google.common.io.CountingOutputStream;
import com.google.common.io.Files;

public class CompilerWithCLITest extends AbstractTest {
	private static final String ROOT_DIR_PATH = "target/compilerWithCLI";
	private ByteArrayOutputStream byteArray;
	private PrintStream defaultOutput;
	@Rule
	public TestName testName = new TestName();
	private CountingOutputStream countingStream;

	@Before
	public void switchOutput() {
		byteArray = new ByteArrayOutputStream();
		defaultOutput = System.out;
		countingStream = new CountingOutputStream(byteArray);
		PrintStream stream = new PrintStream(countingStream);
		System.setOut(stream);
	}

	@After
	public void resetOutput() {
		System.setOut(defaultOutput);
	}

	@Override
	protected String execute(String commands, InputStream input)
			throws Throwable {
		String className = "pkg.withcli." + testName.getMethodName();
		File classFile = new File(ROOT_DIR_PATH, className.replaceAll("\\.", "/") + ".class");
		Files.createParentDirs(classFile);
		classFile.delete();

		assertThat(classFile.exists(), is(false));
		new Main().run(new String[] { "compile", "-c", commands, "-d", ROOT_DIR_PATH, "-n", className });
		assertThat(classFile.isFile(), is(true));
		Class<?> clazz = new OriginalClassLoader().defineClass(className, Files.toByteArray(classFile));
		Method method = clazz.getMethod("main", String[].class);
		assertThat(Modifier.isStatic(method.getModifiers()), is(true));
		assertThat(Modifier.isPublic(method.getModifiers()), is(true));

		InputStream defaultInput = System.in;
		System.setIn(input);
		try {
			method.invoke(null, new Object[] { new String[]{ } });
			System.out.flush();
		} catch (InvocationTargetException e) {
			throw e.getCause();
		} finally {
			System.setIn(defaultInput);
		}
		return new String(byteArray.toByteArray(), 0, (int) countingStream.getCount(), "UTF-8");
	}

	@Test(expected=IllegalArgumentException.class)
	public void testWithoutClassName() throws CmdLineException, IOException {
		new Main().run(new String[]{"compile", "-c", "", "-d", ROOT_DIR_PATH});
	}

	@Test(expected=IllegalArgumentException.class)
	public void testWithWrongDirectory() throws CmdLineException, IOException {
		File file = new File(ROOT_DIR_PATH, "file");
		file.createNewFile();
		new Main().run(new String[]{"compile", "-c", "", "-d", file.getAbsolutePath(), "-n", "pkg.ClassName" });
	}

	@Test
	public void testWithoutPackage() throws CmdLineException, IOException {
		File classFile = new File(ROOT_DIR_PATH, "ClassName.class");
		classFile.delete();
		new Main().run(new String[]{"compile", "-c", "", "-d", ROOT_DIR_PATH, "-n", "ClassName" });
		assertThat(classFile.exists(), is(true));
	}
}
