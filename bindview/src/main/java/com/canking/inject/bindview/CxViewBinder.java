package com.canking.inject.bindview;

import android.app.Activity;
import android.util.Log;
import android.view.ViewGroup;

import com.canking.inject.bindview.processor.AbstractInjector;

import java.util.LinkedHashMap;
import java.util.Map;


public class CxViewBinder {
    static final Map<Class<?>, AbstractInjector<Object>> INJECTORS = new LinkedHashMap<Class<?>, AbstractInjector<Object>>();

    public static void bind(Activity activity) {
        AbstractInjector<Object> injector = findInjector(activity);
        injector.bind(activity);
    }

    //not support
    public static void bind(ViewGroup target) {
        AbstractInjector<Object> injector = findInjector(target);
        injector.bind(target);
    }

    public static final String PROXY = "PROXY";

    private static AbstractInjector<Object> findInjector(Object activity) {
        Class<?> clazz = activity.getClass();
        AbstractInjector<Object> injector = INJECTORS.get(clazz);
        if (injector == null) {
            try {
                String className = clazz.getName() + "_" + PROXY;
                Log.e("changxing", "className:" + className);

                Class injectorClazz = Class.forName(className);
                injector = (AbstractInjector<Object>) injectorClazz
                        .newInstance();

                INJECTORS.put(clazz, injector);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                Log.e("changxing", "ClassNotFoundException:" + e.getMessage());

            } catch (Exception e) {
                e.printStackTrace();
                Log.e("changxing", "Exception:" + e.getMessage());
            }
        }
        return injector;
    }
}
