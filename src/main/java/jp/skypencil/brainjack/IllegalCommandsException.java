package jp.skypencil.brainjack;

class IllegalCommandsException extends RuntimeException {

	private static final long serialVersionUID = -2617294350767892527L;

	IllegalCommandsException(String message) {
		super(message);
	}
}
