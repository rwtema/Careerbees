package com.rwtema.careerbees.helpers;

import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.stream.Collectors;
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

	public static Field getField(Class clazz, String... fields){
		for (String fieldName : fields) {
			if (Stream.of(clazz.getDeclaredFields()).map(Field::getName).noneMatch(fieldName::equals)) {
				continue;
			}

			try{
				Field declaredField =  clazz.getDeclaredField(fieldName);
				if(declaredField != null){
					declaredField.setAccessible(true);
					return declaredField;
				}
			} catch (NoSuchFieldException e) {
				throw new RuntimeException(e);
			}
		}
		if(clazz == Object.class){
			throw new RuntimeException("Unable to find fields: " + Stream.of(fields).collect(Collectors.joining(", ")) );
		}
		return getField(clazz.getSuperclass());
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
