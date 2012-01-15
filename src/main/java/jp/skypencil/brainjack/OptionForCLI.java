package jp.skypencil.brainjack;

import java.io.File;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

final class OptionForCLI {
	static enum Mode {
		INTERPRET, COMPILE
	}

	@Option(name="-c", required=true)
	private String commands;

	@Option(name="-d")
	private File outputRootDirectory;

	@Option(name="-n")
	private String className;

	@Argument(required=true)
	private Mode mode;

	String getCommands() {
		return commands;
	}

	File getOutputRootDirectory() {
		if (outputRootDirectory == null) {
			return new File(".");
		} else {
			return outputRootDirectory;
		}
	}

	String getClassName() {
		return className;
	}

	Mode getMode() {
		return mode;
	}
}
