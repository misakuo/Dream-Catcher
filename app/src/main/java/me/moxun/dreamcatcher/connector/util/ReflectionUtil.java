package me.moxun.dreamcatcher.connector.util;

import android.view.View;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

public final class ReflectionUtil {
    private ReflectionUtil() {
    }

    public static Object invokeMethod(Object receiver, String name) {
        try {
            Method method = receiver.getClass().getDeclaredMethod(name);
            method.setAccessible(true);
            return method.invoke(receiver);
        } catch (NoSuchMethodException e) {
            LogUtil.w(e.toString());
        } catch (InvocationTargetException e) {
            LogUtil.w(e.toString());
        } catch (IllegalAccessException e) {
            LogUtil.w(e.toString());
        }
        return null;
    }

    @Nullable
    public static Class<?> tryGetClassForName(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    @Nullable
    public static Field tryGetDeclaredField(Class<?> theClass, String fieldName) {
        try {
            return theClass.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            LogUtil.d(
                    e,
                    "Could not retrieve %s field from %s",
                    fieldName,
                    theClass);

            return null;
        }
    }

    @Nullable
    public static Object getFieldValue(Field field, Object target) {
        try {
            return field.get(target);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static Set<String> getAllPairMethodsByView(View target) {
        return getAllPairMethods(target.getClass());
    }

    public static Set<String> getAllPairMethods(Class<? extends View> clazz) {
        Method[] methods = clazz.getMethods();
        Set<String> getter = new HashSet<>();
        Set<String> setter = new HashSet<>();
        for (Method method : methods) {
            if (Modifier.isPublic(method.getModifiers())) {
                if (method.getParameterTypes().length == 1) {
                    Class param = method.getParameterTypes()[0];
                    if (method.getName().startsWith("set")) {
                        if (param.isPrimitive() || CharSequence.class.isAssignableFrom(param.getClass()) && !param.getSimpleName().equals("Object")) {
                            //LogUtil.e("Reflect", "Setter: " + method.getName() + "," + param.getSimpleName());
                            setter.add(method.getName().substring(3));
                        }
                    }
                } else if (method.getParameterTypes().length == 0) {
                    if (method.getName().startsWith("get")) {
                        if (method.getReturnType().isPrimitive() || method.getReturnType().isAssignableFrom(CharSequence.class) && !method.getReturnType().getSimpleName().equals("Object")) {
                            //LogUtil.e("Reflect", "Getter: " + method.getName() + "," + method.getReturnType().getSimpleName());
                            getter.add(method.getName().substring(3));
                        }
                    }
                }
            }
        }
        getter.retainAll(setter);
        return getter;
    }

    public static Set<String> getAllPairDeclaredMethods(Class<? extends View> clazz) {
        Method[] methods = clazz.getDeclaredMethods();
        Set<String> getter = new HashSet<>();
        Set<String> setter = new HashSet<>();
        for (Method method : methods) {
            if (Modifier.isPublic(method.getModifiers())) {
                if (method.getParameterTypes().length == 1) {
                    Class param = method.getParameterTypes()[0];
                    if (method.getName().startsWith("set")) {
                        if (param.isPrimitive() || CharSequence.class.isAssignableFrom(param.getClass()) && !param.getSimpleName().equals("Object")) {
                            setter.add(method.getName().substring(3));
                        }
                    }
                } else if (method.getParameterTypes().length == 0) {
                    if (method.getName().startsWith("get")) {
                        if (method.getReturnType().isPrimitive() || method.getReturnType().isAssignableFrom(CharSequence.class) && !method.getReturnType().getSimpleName().equals("Object")) {
                            getter.add(method.getName().substring(3));
                        }
                    }
                }
            }
        }
        getter.retainAll(setter);
        return getter;
    }

    public static void main(String args[]) {
        for (String s : getAllPairDeclaredMethods(TextView.class)) {
            System.out.println(s);
        }
    }
}
