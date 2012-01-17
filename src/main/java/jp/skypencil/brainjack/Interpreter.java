package jp.skypencil.brainjack;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

public class Interpreter {
	private final Logger logger = Logger.getLogger(getClass().getName());

	public Context execute(String commands, InputStream input, OutputStream output) throws IOException {
		checkNotNull(commands);
		checkNotNull(input);
		checkNotNull(output);

		Context context = Context.create(commands, input, output);
		InterpreterVisitor visitor = new InterpreterVisitor(context);
		while (context.instructionPointer < commands.length()) {
			byte byteData = context.commands[context.instructionPointer];
			Command command = Command.fromByte(byteData);
			if (command == null) {
				logger.warning("unknown command: " + Byte.toString(byteData));
				++context.instructionPointer;
			} else {
				command.accept(visitor);
			}
		}

		return context;
	}
}
