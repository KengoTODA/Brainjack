package jp.skypencil.brainjack;

public class IllegalCommandsException extends RuntimeException {

	private static final long serialVersionUID = -2617294350767892527L;

	IllegalCommandsException(String message) {
		super(message);
	}
}
