package com.rwtema.careerbees.helpers;

import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.stream.Stream;

public class PrivateHelper {

	public static final HashMap<Class<? extends EntityLiving>, Method> methods = new HashMap<>();

	public static ResourceLocation getLootTable(EntityLiving entityLiving) {
		Class<? extends EntityLiving> aClass = entityLiving.getClass();
		Method method = methods.computeIfAbsent(aClass, clazz -> {
			Method declaredMethod = getMethod(aClass);
			if (declaredMethod != null) return declaredMethod;
			throw new RuntimeException();
		});
		try {
			return (ResourceLocation) method.invoke(entityLiving);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	@Nullable
	private static Method getMethod(Class<? extends EntityLiving> aClass) {
		for (String methodName : new String[]{"getLootTable", "func_184276_b", "func_184647_J"}) {
			if (Stream.of(aClass.getDeclaredMethods()).map(Method::getName).noneMatch(methodName::equals)) {
				continue;
			}

			try {
				Method declaredMethod = aClass.getDeclaredMethod(methodName);
				if (declaredMethod != null) {
					declaredMethod.setAccessible(true);
					return declaredMethod;
				}
			} catch (NoSuchMethodException ignore) {
				throw new RuntimeException(ignore);
			}
		}
		if(aClass == EntityLiving.class){
			throw new RuntimeException("Unable to find getLootTable() method");
		}
		return getMethod((Class<? extends EntityLiving>) aClass.getSuperclass());
	}
}
