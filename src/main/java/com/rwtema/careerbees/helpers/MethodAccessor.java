package com.rwtema.careerbees.helpers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class MethodAccessor<T, O> {
	final Method method;

	protected MethodAccessor(Class<O> owner, Class<?>[] params, String... names) {
		this.method = PrivateHelper.getMethod(owner, params, names);
	}

	protected T invoke(O owner, Object... params) {
		try {
			//noinspection unchecked
			return (T) method.invoke(owner, params);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	public static class NoParams<T, O> extends MethodAccessor<T, O> {

		public NoParams(Class<O> owner, String... names) {
			super(owner, new Class[]{}, names);
		}

		public T invoke(O owner) {
			return super.invoke(owner);
		}
	}

	public static class OneParam<T, O, P1> extends MethodAccessor<T, O> {

		public OneParam(Class<O> owner, Class<P1> param, String... names) {
			super(owner, new Class[]{param}, names);
		}

		public T invoke(O owner, P1 param) {
			return super.invoke(owner, param);
		}
	}

	public static class TwoParam<T, O, P1, P2> extends MethodAccessor<T, O> {

		public TwoParam(Class<O> owner, Class<P1> param1, Class<P2> param2, String... names) {
			super(owner, new Class[]{param1, param2}, names);
		}

		public T invoke(O owner, P1 param1, P2 param2) {
			return super.invoke(owner, param1, param2);
		}
	}
}
