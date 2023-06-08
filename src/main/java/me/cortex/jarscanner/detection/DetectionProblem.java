package me.cortex.jarscanner.detection;

import javax.annotation.Nullable;

/**
 * Problem outline for {@link Detection} operations.
 */
public class DetectionProblem {
	private final Throwable throwable;
	private final String message;

	/**
	 * @param throwable
	 * 		Cause of the problem. May be {@code null}.
	 * @param message
	 * 		Problem description. May be {@code null}.
	 */
	public DetectionProblem(@Nullable Throwable throwable, @Nullable String message) {
		this.throwable = throwable;
		this.message = message;
	}

	/**
	 * @return Problem description. May be {@code null}.
	 */
	@Nullable
	public String getMessage() {
		return message;
	}

	/**
	 * @return Cause of the problem. May be {@code null}.
	 */
	@Nullable
	public Throwable getThrowable() {
		return throwable;
	}

	@Override
	public String toString() {
		return "DetectionProblem{" +
				"throwable=" + throwable +
				", message='" + message + '\'' +
				'}';
	}
}
