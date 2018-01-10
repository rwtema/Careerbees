package com.rwtema.careerbees.helpers;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;

public class FieldAccessor<T, O> {
	final Field field;

	public FieldAccessor(Class<O> owner, String... names) {
		this.field = PrivateHelper.getField(owner, names);
	}

	public FieldAccessor(Field field) {
		this.field = field;
	}

	@Nonnull
	@SuppressWarnings("unchecked")
	public T get(O owner) {
		try {
			return (T) field.get(owner);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public void set(O owner, T value) {
		try {
			field.set(owner, value);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
}
