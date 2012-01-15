package jp.skypencil.brainjack;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nonnull;

public class IllegalCommandsException extends RuntimeException {

	private static final long serialVersionUID = -2617294350767892527L;

	public IllegalCommandsException(@Nonnull String message) {
		super(checkNotNull(message));
	}
}
