package com.rwtema.careerbees.helpers;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PrivateHelper {


	private static final Map<Class<?>, Class<?>> primitiveTypes = new HashMap<>(13);

	static {
		primitiveTypes.put(Boolean.class, boolean.class);
		primitiveTypes.put(Byte.class, byte.class);
		primitiveTypes.put(Character.class, char.class);
		primitiveTypes.put(Double.class, double.class);
		primitiveTypes.put(Float.class, float.class);
		primitiveTypes.put(Integer.class, int.class);
		primitiveTypes.put(Long.class, long.class);
		primitiveTypes.put(Short.class, short.class);
		primitiveTypes.put(Void.class, void.class);
	}


	public static Field getField(@Nonnull Class clazz, @Nonnull String... fields) {
		for (String fieldName : fields) {
			if (Stream.of(clazz.getDeclaredFields()).map(Field::getName).noneMatch(fieldName::equals)) {
				continue;
			}

			try {
				Field declaredField = clazz.getDeclaredField(fieldName);
				if (declaredField != null) {
					declaredField.setAccessible(true);
					return declaredField;
				}
			} catch (NoSuchFieldException e) {
				throw new RuntimeException(e);
			}
		}
		if (clazz == Object.class) {
			throw new RuntimeException("Unable to find fields: " + Stream.of(fields).collect(Collectors.joining(", ")));
		}
		return getField(clazz.getSuperclass(), fields);
	}


	public static Method getMethod(@Nonnull Class<?> clazz, Class<?>[] parameters, @Nonnull String... methods) {
		for (String methodName : methods) {
			Method bestMatch = null;
			for (Method method : clazz.getDeclaredMethods()) {
				if (!method.getName().equals(methodName)) continue;
				Class<?>[] parameterTypes = method.getParameterTypes();
				if (parameterTypes.length != parameters.length) {
					continue;
				}
				boolean flag = true;
				for (int i = 0; i < parameterTypes.length; i++) {
					if (parameters[i] != parameterTypes[i]) {
						flag = false;
						break;
					}
				}
				if (flag) {
					method.setAccessible(true);
					return method;
				}
				flag = true;
				for (int i = 0; i < parameterTypes.length; i++) {
					if (!(parameters[i] == parameterTypes[i] || primitiveTypes.get(parameters[i]) == parameterTypes[i])) {
						flag = false;
						break;
					}
				}
				if (flag) {
					bestMatch = method;
				}
			}
			if (bestMatch != null) {
				bestMatch.setAccessible(true);
				return bestMatch;
			}
		}
		if (clazz == Object.class) {
			throw new RuntimeException("Unable to find method: " + Stream.of(methods).collect(Collectors.joining(", ")));
		}
		return getMethod(clazz.getSuperclass(), parameters, methods);
	}
}
