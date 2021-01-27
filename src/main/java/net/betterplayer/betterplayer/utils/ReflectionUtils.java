package net.betterplayer.betterplayer.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionUtils {

	public static Class<?> getClass(String name) {
		try {
			return Class.forName(name);
		} catch (ClassNotFoundException e) {
		}
		
		return null;
	}
	
	public static Constructor<?> getConstructor(Class<?> clazz, Class<?>... params) {
		try {
			return clazz.getConstructor(params);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static Object invokeMethod(Object o, Method m, Object... params) {
		try {
			return m.invoke(o, params);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static Object invokeMethodWithExceptions(Object o, Method m, Object... params) throws InvocationTargetException, IllegalAccessException, IllegalArgumentException {
		return m.invoke(o, params);
	}
	
	public static Object createInstance(Constructor<?> constructor, Object... params) {
		try {
			return constructor.newInstance(params);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	public static Method getMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) {
		try {
			return clazz.getMethod(methodName, paramTypes);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		
		return null;
	}
}
