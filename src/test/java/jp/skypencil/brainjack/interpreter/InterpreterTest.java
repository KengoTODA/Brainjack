// don't put in jp.skypencil.brainjack to test call from other package
package jp.skypencil.brainjack.interpreter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import jp.skypencil.brainjack.AbstractTest;
import jp.skypencil.brainjack.Context;
import jp.skypencil.brainjack.Interpreter;

public class InterpreterTest extends AbstractTest {

	@Override
	protected String execute(String commands, InputStream input) throws IOException {
		Interpreter interpreter = new Interpreter();
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		Context context = interpreter.execute(commands, input, output);
		System.out.println(context);
		return output.toString("UTF-8");
	}

}
