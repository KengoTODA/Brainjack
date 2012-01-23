package jp.skypencil.brainjack;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import com.google.common.base.Strings;
import com.google.common.io.Files;

public class Main {
	private static final int STATUS_CODE_OK = 0;
	private static final int STATUS_CODE_ERROR = 9;
	private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

	private CmdLineParser parser;

	public static void main(String[] args) {
		int statusCode = STATUS_CODE_OK;
		Main main = new Main();
		try {
			main.run(args);
		} catch (CmdLineException e) {
			main.printUsage(System.out);
			e.printStackTrace();
			statusCode = STATUS_CODE_ERROR;
		} catch (IllegalArgumentException e) {
			main.printUsage(System.out);
			e.printStackTrace();
			statusCode = STATUS_CODE_ERROR;
		} catch (IOException e) {
			e.printStackTrace();
			statusCode = STATUS_CODE_ERROR;
		}
		System.exit(statusCode);
	}

	private void printUsage(PrintStream out) {
		if (parser == null) {
			throw new IllegalStateException();
		} else {
			parser.printUsage(out);
		}
	}

	public void run(String[] args) throws CmdLineException, IOException {
		OptionForCLI option = new OptionForCLI();
		this.parser = new CmdLineParser(option);
		parser.parseArgument(args);
		run(option);
	}

	void run(@Nonnull OptionForCLI option) throws IOException, AssertionError {
		switch (option.getMode()) {
			case INTERPRET:
				new Interpreter().execute(option.getCommands(), System.in, System.out);
				System.out.flush();
				break;
			case COMPILE:
				if (Strings.isNullOrEmpty(option.getClassName()) ||
						!option.getOutputRootDirectory().isDirectory()) {
					throw new IllegalArgumentException("compile mode require className");
				} else {
					byte[] binary = new Compiler().compile(option.getCommands(), option.getClassName());
					File classFile = new File(option.getOutputRootDirectory(), option.getClassName().replace('.', '/').concat(".class"));
					Files.write(binary, classFile);
					LOGGER.info("finish writing to: " + classFile.getAbsolutePath());
				}
				break;
			default:
				throw new AssertionError("unknown mode: " + option.getMode());
		}
	}
}
