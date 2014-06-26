package eu.livotov.labs.android.robotools.injector;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.livotov.labs.android.robotools.widget.ListHolder;

// TODO: добавить поддержку иъекций и для ViewHolder
@SuppressWarnings("ResourceType")
public class Injector {

    private Injector() {

    }

    private final static Map<Class, ClassInfo> mCache = new HashMap<Class, ClassInfo>();

    private static Class sActionBarActivityClass;
    private static Class sNativeFragmentClass;

    static {
        try {
            sActionBarActivityClass = Class.forName("android.support.v7.app.ActionBarActivity");
        } catch (ClassNotFoundException e) {
            sActionBarActivityClass = null;
        }

        try {
            sNativeFragmentClass = Class.forName("android.app.Fragment");
        } catch (ClassNotFoundException e) {
            sNativeFragmentClass = null;
        }
    }

    /**
     * Инициализирует Activity.
     * Вызывать в {@link android.app.Activity#onCreate(android.os.Bundle)}.
     *
     * Поддерживает следующие аннотации:
     * {@link eu.livotov.labs.android.robotools.injector.InjectContent} - работает как {@link android.app.Activity#setContentView(int)},
     * {@link eu.livotov.labs.android.robotools.injector.InjectView} - работает как {@link android.app.Activity#findViewById(int)},
     * {@link eu.livotov.labs.android.robotools.injector.InjectFragment} - работает как {@link android.support.v4.app.FragmentManager#findFragmentById(int)} для {@link android.support.v4.app.FragmentActivity#getSupportFragmentManager()}.
     * {@link eu.livotov.labs.android.robotools.injector.Handle} - помечает View как объект, которому понадобится установить callback
     * {@link eu.livotov.labs.android.robotools.injector.Listener} - наделяет поле ролью Callback
     * @param activity Activity для инциализации
     */
    public static <T extends FragmentActivity> void init(T activity) {
        ClassInfo info = loadClass(activity.getClass());
        int layoutId = info.layoutId;
        if (layoutId != 0) {
            activity.setContentView(layoutId);
        }

        try {
            if (info.views != null) {
                for (Map.Entry<Field, Integer> entry : info.views.entrySet()) {
                    entry.getKey().set(activity, activity.findViewById(entry.getValue()));
                }
            }
            if (info.fragments != null) {
                for (Map.Entry<Field, InjectFragment> entry : info.fragments.entrySet()) {
                    Field field = entry.getKey();
                    int id = entry.getValue().value();
                    Fragment fragment = activity.getSupportFragmentManager().findFragmentById(id);
                    if (fragment == null && Fragment.class.isAssignableFrom(field.getType())) {
                        fragment = entry.getValue().fragment().newInstance();
                        activity.getSupportFragmentManager().beginTransaction().add(id, fragment).commit();
                    }

                    field.set(activity, fragment);
                }
            }
            initHandles(activity, info);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Инициализурет фрагмент.
     *
     * Вызывать в {@link android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)},
     * передавая ссылку на фрагмент и container и возвращая результата вызова метода как результат вызова {@link android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)}.
     *
     * {@link eu.livotov.labs.android.robotools.injector.InjectContent} - работает как указатель rootView для фрагмента.
     * {@link eu.livotov.labs.android.robotools.injector.InjectView} - работает как {@link View#findViewById(int)} для View, полученного в результате {@link eu.livotov.labs.android.robotools.injector.InjectView},
     * {@link eu.livotov.labs.android.robotools.injector.InjectFragment} - работает как {@link android.support.v4.app.FragmentManager#findFragmentById(int)} для {@link android.support.v4.app.Fragment#getChildFragmentManager()}.
     * {@link eu.livotov.labs.android.robotools.injector.Handle} - помечает View как объект, которому понадобится установить callback
     * {@link eu.livotov.labs.android.robotools.injector.Listener} - наделяет поле ролью Callback
     *
     * @param fragment фрагмент для инициализации
     * @param container родительский элемент, подаваемый на вход {@link android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)}
     * @return View для возвращения в {@link android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)}
     */
    public static <T extends Fragment> View init(T fragment, ViewGroup container) {
        View root = null;
        ClassInfo info = loadClass(fragment.getClass());
        int layoutId = info.layoutId;
        if (layoutId != 0) {
            root = LayoutInflater.from(fragment.getActivity()).inflate(layoutId, container, false);
        }

        if (root != null) {
            try {
                if (info.views != null) {
                    for (Map.Entry<Field, Integer> entry : info.views.entrySet()) {
                        entry.getKey().set(fragment, root.findViewById(entry.getValue()));
                    }
                }
                if (info.fragments != null) {
                    for (Map.Entry<Field, InjectFragment> entry : info.fragments.entrySet()) {
                        Field field = entry.getKey();
                        int id = entry.getValue().value();
                        Fragment childFragment = fragment.getChildFragmentManager().findFragmentById(id);
                        if (childFragment == null && Fragment.class.isAssignableFrom(field.getType())) {
                            childFragment = entry.getValue().fragment().newInstance();
                            fragment.getChildFragmentManager().beginTransaction().add(id, childFragment).commit();
                        }
                        field.set(fragment, childFragment);
                    }
                }
                initHandles(fragment, info);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return root;
    }

    /**
     * Инициализурет {@link eu.livotov.labs.android.robotools.widget.ListHolder}.
     *
     * Вызывать в {@link eu.livotov.labs.android.robotools.widget.ListHolder#inflate(android.view.ViewGroup)},
     * передавая ссылку на {@link eu.livotov.labs.android.robotools.widget.ListHolder} и container и возвращая результата вызова метода как результат вызова {@link eu.livotov.labs.android.robotools.widget.ListHolder#inflate(android.view.ViewGroup)}.
     *
     * {@link eu.livotov.labs.android.robotools.injector.InjectContent} - работает как указатель rootView для {@link eu.livotov.labs.android.robotools.widget.ListHolder}.
     * {@link eu.livotov.labs.android.robotools.injector.InjectView} - работает как {@link View#findViewById(int)} для View, полученного в результате {@link eu.livotov.labs.android.robotools.injector.InjectView},
     * {@link eu.livotov.labs.android.robotools.injector.Handle} - помечает View как объект, которому понадобится установить callback
     * {@link eu.livotov.labs.android.robotools.injector.Listener} - наделяет поле ролью Callback
     *
     * @param holder {@link eu.livotov.labs.android.robotools.widget.ListHolder} для инициализации.
     * @param parentView родительский элемент, подаваемый на вход {@link eu.livotov.labs.android.robotools.widget.ListHolder#inflate(android.view.ViewGroup)}
     * @return View для возвращения в {@link eu.livotov.labs.android.robotools.widget.ListHolder#inflate(android.view.ViewGroup)}
     */
    public static <T extends ListHolder> View init(T holder, ViewGroup parentView) {
        View root = null;
        ClassInfo info = loadClass(holder.getClass());
        int layoutId = info.layoutId;
        if (layoutId != 0) {
            root = LayoutInflater.from(parentView.getContext()).inflate(layoutId, parentView, false);
        }

        if (root != null) {
            try {
                if (info.views != null) {
                    for (Map.Entry<Field, Integer> entry : info.views.entrySet()) {
                        entry.getKey().set(holder, root.findViewById(entry.getValue()));
                    }
                }
                initHandles(holder, info);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return root;
    }

    public static void recycle(Object host) {
        ClassInfo info = loadClass(host.getClass());
        try {
            if (info.views != null) {
                for (Map.Entry<Field, Integer> entry : info.views.entrySet()) {
                    entry.getKey().set(host, null);
                }
            }
            if (info.fragments != null) {
                for (Map.Entry<Field, InjectFragment> entry : info.fragments.entrySet()) {
                    entry.getKey().set(host, null);
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private static <T> T findListener(Class<T> targetType, Object instance, Map<Class, Field> fields, Class handler) throws IllegalAccessException, InstantiationException {
        if (targetType.isInstance(instance)) {
            return (T) instance;
        }
        if (targetType.isAssignableFrom(handler)) {
            return (T) handler.newInstance();
        }
        if (fields != null) {
            for (Map.Entry<Class, Field> field : fields.entrySet()) {
                if (targetType.isAssignableFrom(field.getKey())) {
                    return (T) field.getValue().get(instance);
                }
            }
        }
        return null;
    }

    private static void initHandles(Object instance, ClassInfo info) throws IllegalAccessException, InstantiationException {
        if (info.handles != null) {
            for (Map.Entry<Field, Handle> entry : info.handles.entrySet()) {
                View view = ((View) entry.getKey().get(instance));
                for (Handle.Type type : entry.getValue().value()) {
                    switch (type) {
                        case CLICK: {
                            view.setOnClickListener(findListener(View.OnClickListener.class, instance, info.listeners, entry.getValue().handler()));
                        }
                        break;
                        case LONG_CLICK: {
                            view.setOnLongClickListener(findListener(View.OnLongClickListener.class, instance, info.listeners, entry.getValue().handler()));
                        }
                        break;
                        case CONTEXT_MENU: {
                            view.setOnCreateContextMenuListener(findListener(View.OnCreateContextMenuListener.class, instance, info.listeners, entry.getValue().handler()));
                        }
                        break;

                        case TOUCH: {
                            view.setOnTouchListener(findListener(View.OnTouchListener.class, instance, info.listeners, entry.getValue().handler()));
                        }
                        break;
                        case PRE_DRAW: {
                            view.getViewTreeObserver().addOnPreDrawListener(findListener(ViewTreeObserver.OnPreDrawListener.class, instance, info.listeners, entry.getValue().handler()));
                        }
                        break;
                        case DRAW: {
                            view.getViewTreeObserver().addOnDrawListener(findListener(ViewTreeObserver.OnDrawListener.class, instance, info.listeners, entry.getValue().handler()));
                        }
                        break;
                        case GLOBAL_LAYOUT: {
                            view.getViewTreeObserver().addOnGlobalLayoutListener(findListener(ViewTreeObserver.OnGlobalLayoutListener.class, instance, info.listeners, entry.getValue().handler()));
                        }
                        break;
                        case KEY: {
                            view.setOnKeyListener(findListener(View.OnKeyListener.class, instance, info.listeners, entry.getValue().handler()));
                        }
                        break;
                        case SCROLL: {
                            if (view instanceof AdapterView) {
                                ((AbsListView) view).setOnScrollListener(findListener(AbsListView.OnScrollListener.class, instance, info.listeners, entry.getValue().handler()));
                            }
                        }
                        break;
                        case ITEM_CLICK: {
                            if (view instanceof AdapterView) {
                                ((AdapterView) view).setOnItemClickListener(findListener(AdapterView.OnItemClickListener.class, instance, info.listeners, entry.getValue().handler()));
                            }
                        }
                        break;
                        case ADAPTER: {
                            if (view instanceof AdapterView) {
                                ((AdapterView) view).setAdapter(findListener(ListAdapter.class, instance, info.listeners, entry.getValue().handler()));
                            }
                        }
                        break;
                        case TEXT_EDIT: {
                            if (view instanceof TextView) {
                                ((TextView) view).addTextChangedListener(findListener(TextWatcher.class, instance, info.listeners, entry.getValue().handler()));
                            }
                        }
                        break;
                    }
                }
            }
        }
    }

    private static ClassInfo loadClass(Class clazz) {
        ClassInfo result = mCache.get(clazz);
        if (result == null) {
            result = new ClassInfo();

            // Get layout annotation
            result.layoutId = getLayoutRes(clazz);

            // Fill fields annotations
            List<Field> fields = new ArrayList<Field>();
            fields = getFields(fields, clazz);
            for (Field field : fields) {
                field.setAccessible(true);
                Class<?> fieldType = field.getType();

                Listener listener = field.getAnnotation(Listener.class);
                if (listener != null) {
                    if (result.listeners == null) {
                        result.listeners = new HashMap<Class, Field>();
                    }
                    result.listeners.put(fieldType, field);
                }
                if (View.class.isAssignableFrom(fieldType)) {

                    InjectView injectView = field.getAnnotation(InjectView.class);
                    if (injectView != null) {
                        if (result.views == null) {
                            result.views = new HashMap<Field, Integer>();
                        }
                        result.views.put(field, injectView.value());
                    }
                    Handle handle = field.getAnnotation(Handle.class);
                    if (handle != null) {
                        if (result.handles == null) {
                            result.handles = new HashMap<Field, Handle>();
                        }
                        result.handles.put(field, handle);
                    }

                } else if (Fragment.class.isAssignableFrom(fieldType)) {
                    InjectFragment injectFragment = field.getAnnotation(InjectFragment.class);
                    if (injectFragment != null) {
                        if (result.fragments == null) {
                            result.fragments = new HashMap<Field, InjectFragment>();
                        }
                        result.fragments.put(field, injectFragment);
                    }
                }
            }
            mCache.put(clazz, result);
        }
        return result;
    }

    private static int getLayoutRes(Class clazz) {
        int result = 0;

        // Get layout annotation
        Annotation injectContent = clazz.getAnnotation(InjectContent.class);
        if (injectContent instanceof InjectContent) {
            result = ((InjectContent) injectContent).value();
        } else {
            Class superClass = clazz.getSuperclass();
            if(superClass != null) {
                result = getLayoutRes(superClass);
            }
        }
        return result;
    }

    private static List<Field> getFields(List<Field> fields, Class<?> type) {
        for (Field field: type.getDeclaredFields()) {
            if(!Modifier.isStatic(field.getModifiers())) {
                fields.add(field);
            }
        }
        Class<?> superclass = type.getSuperclass();
        if (superclass != null && !superclass.equals(Fragment.class) && !superclass.equals(Activity.class) &&
                !superclass.equals(FragmentActivity.class) && !superclass.equals(sNativeFragmentClass) && !superclass.equals(sActionBarActivityClass)
                && !superclass.equals(eu.livotov.labs.android.robotools.app.Fragment.class)) {
            fields = getFields(fields, type.getSuperclass());
        }
        return fields;
    }

    static class ClassInfo {
        int layoutId;
        Map<Field, Integer> views;
        Map<Field, InjectFragment> fragments;
        Map<Field, Handle> handles;
        Map<Class, Field> listeners;
    }

}
