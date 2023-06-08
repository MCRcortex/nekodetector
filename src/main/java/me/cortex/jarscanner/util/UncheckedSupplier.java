package me.cortex.jarscanner.util;

import java.util.function.Supplier;

/**
 * Wrapper for {@link Supplier} for checked exception handling.
 *
 * @param <T>
 * 		Supplied value type.
 */
public interface UncheckedSupplier<T> extends Supplier<T> {
	@Override
	default T get() {
		try {
			return getUnsafe();
		} catch (Throwable ex) {
			throw new RuntimeException(ex);
		}
	}

	T getUnsafe() throws Throwable;
}
