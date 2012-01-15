package jp.skypencil.brainjack;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class Interpreter {
	private static final Map<Byte, Command> byteToCommand = new HashMap<Byte, Command>();
	private final Logger logger = Logger.getLogger(getClass().getName());

	static {
		for (Command command : Command.values()) {
			byteToCommand.put(command.getCharacter(), command);
		}
	}

	public Context execute(String commands, InputStream input, OutputStream output) throws IOException {
		checkNotNull(commands);
		checkNotNull(input);
		checkNotNull(output);

		Context context = Context.create(commands, input, output);
		InterpreterVisitor visitor = new InterpreterVisitor(context);
		while (context.instructionPointer < commands.length()) {
			byte byteData = context.commands[context.instructionPointer];
			Command command = byteToCommand.get(byteData);
			if (command == null) {
				logger.warning("unknown command: " + Byte.toString(byteData));
			} else {
				command.accept(visitor);
			}
		}

		return context;
	}
}
