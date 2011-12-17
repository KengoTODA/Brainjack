package jp.skypencil.brainjack;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

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
