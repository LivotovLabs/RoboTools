package eu.livotov.labs.android.robotools.app.injector;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.ContentProvider;
import android.content.IntentFilter;
import android.util.Pair;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import eu.livotov.labs.android.robotools.app.RTActivity;
import eu.livotov.labs.android.robotools.app.injector.ann.InjectActionBar;
import eu.livotov.labs.android.robotools.app.injector.ann.InjectApp;
import eu.livotov.labs.android.robotools.app.injector.ann.InjectClickEvent;
import eu.livotov.labs.android.robotools.app.injector.ann.InjectExtra;
import eu.livotov.labs.android.robotools.app.injector.ann.InjectFragment;
import eu.livotov.labs.android.robotools.app.injector.ann.InjectLayout;
import eu.livotov.labs.android.robotools.app.injector.ann.InjectOptionMenu;
import eu.livotov.labs.android.robotools.app.injector.ann.InjectReceiver;
import eu.livotov.labs.android.robotools.app.injector.ann.InjectResource;
import eu.livotov.labs.android.robotools.app.injector.ann.InjectServiceConnection;
import eu.livotov.labs.android.robotools.app.injector.ann.InjectSystemService;
import eu.livotov.labs.android.robotools.app.injector.ann.InjectView;

/**
 * Loads class metadata for injector
 */
final class RTInjectorClassMetadataLoader
{

    private final static Map<Class, RTInjectorClassMetaData> sCache = new HashMap<>();

    private RTInjectorClassMetadataLoader()
    {
    }

    public static RTInjectorClassMetaData loadClass(Class clazz)
    {
        RTInjectorClassMetaData result = sCache.get(clazz);
        if (result == null)
        {
            MetaData data = new MetaData();
            loadClassMetaData(clazz, data);
            result = data.toClassMetaData();
            sCache.put(clazz, result);
        }
        return result;
    }

    static void loadClassMetaData(Class clazz, MetaData data)
    {
        // @Inject
        Annotation classAnnotation = clazz.getAnnotation(InjectLayout.class);
        if (classAnnotation instanceof InjectLayout)
        {
            if (data.injects == null)
            {
                data.injects = new Stack<>();
            }
            data.injects.add(new InjectLayout.MetaData(((InjectLayout) classAnnotation)));
        }

        // @Click
        for (Method method : clazz.getDeclaredMethods())
        {
            method.setAccessible(true);
            if (!Modifier.isStatic(method.getModifiers()))
            {
                InjectClickEvent annotation = method.getAnnotation(InjectClickEvent.class);
                if (annotation != null)
                {
                    if (data.clickers == null)
                    {
                        data.clickers = new HashMap<>();
                    }
                    for (int id : annotation.value())
                    {
                        data.clickers.put(id, method);
                    }
                }
            }
        }

        for (Field field : clazz.getDeclaredFields())
        {
            field.setAccessible(true);
            int modifiers = field.getModifiers();
            if (!Modifier.isStatic(modifiers))
            {
                Annotation[] annotations = field.getAnnotations();
                if (annotations != null)
                {
                    for (Annotation annotation : annotations)
                    {

                        // @Widget
                        if (annotation instanceof InjectView)
                        {
                            if (data.widgets == null)
                            {
                                data.widgets = new HashMap<>();
                            }
                            data.widgets.put(field, new InjectView.MetaData(((InjectView) annotation)));
                        }

                        // @Extra
                        else if (annotation instanceof InjectExtra)
                        {
                            if (data.extras == null)
                            {
                                data.extras = new HashMap<>();
                            }
                            data.extras.put(field, new InjectExtra.MetaData((InjectExtra) annotation, field));
                        }

                        // @Fragment
                        else if (annotation instanceof InjectFragment)
                        {
                            if (data.fragments == null)
                            {
                                data.fragments = new HashMap<>();
                            }
                            data.fragments.put(field, ((InjectFragment) annotation).value());
                        }

                        // @Resource
                        else if (annotation instanceof InjectResource)
                        {
                            if (data.resources == null)
                            {
                                data.resources = new HashMap<>();
                            }
                            data.resources.put(field, new InjectResource.MetaData((InjectResource) annotation, field));
                        }

                        // @SystemService
                        else if (annotation instanceof InjectSystemService)
                        {
                            if (data.systemServices == null)
                            {
                                data.systemServices = new HashMap<Field, String>();
                            }
                            data.systemServices.put(field, InjectSystemService.SERVICES_MAP.get(field.getType()));
                        }

                        // @Receiver
                        else if (annotation instanceof InjectReceiver)
                        {
                            if (data.receivers == null)
                            {
                                data.receivers = new HashMap<>();
                            }
                            data.receivers.put(field, InjectReceiver.Processor.process(((InjectReceiver) annotation)));
                        }

                        // @ServiceConnection
                        else if (annotation instanceof InjectServiceConnection)
                        {
                            if (data.serviceConnections == null)
                            {
                                data.serviceConnections = new HashMap<>();
                            }
                            data.serviceConnections.put(field, new InjectServiceConnection.MetaData(((InjectServiceConnection) annotation)));
                        }

                        // @ActBar
                        else if (annotation instanceof InjectActionBar)
                        {
                            if (data.actionBars == null)
                            {
                                data.actionBars = new ArrayList<>();
                            }
                            data.actionBars.add(field);
                        }

                        // @App
                        else if (annotation instanceof InjectApp)
                        {
                            if (data.applications == null)
                            {
                                data.applications = new ArrayList<>();
                            }
                            data.applications.add(field);
                        }

                        // @OptionMenu
                        else if (annotation instanceof InjectOptionMenu)
                        {
                            if (data.menuItems == null)
                            {
                                data.menuItems = new HashMap<>();
                            }
                            data.menuItems.put(field, ((InjectOptionMenu) annotation).value());
                        }
                        else

                        {
                        }
                    }
                }
            }
        }

        Class superclass = clazz.getSuperclass();
        if (superclass != null && canMoveToUpperLevel(superclass))
        {
            loadClassMetaData(superclass, data);
        }
    }

    static boolean canMoveToUpperLevel(Class superclass)
    {
        if (Activity.class.isAssignableFrom(superclass))
        {
            return !(RTActivity.class.equals(superclass) || Activity.class.equals(superclass));
        }
        if (InjectFragment.class.isAssignableFrom(superclass))
        {
            return !InjectFragment.class.equals(superclass);
        }
        if (Application.class.isAssignableFrom(superclass))
        {
            return !Application.class.equals(superclass);
        }
        if (Service.class.isAssignableFrom(superclass))
        {
            return !Service.class.equals(superclass);
        }
        if (ContentProvider.class.isAssignableFrom(superclass))
        {
            return !ContentProvider.class.equals(superclass);
        }
        return false;
    }

    private static class MetaData
    {
        Stack<InjectLayout.MetaData> injects;

        List<Field> actionBars;
        List<Field> applications;
        Map<Field, String> systemServices;

        Map<Class, int[]> rootListeners;

        Map<Integer, Method> clickers;
        Map<Field, InjectExtra.MetaData> extras;

        Map<Field, Integer> fragments;
        Map<Field, Integer> menuItems;
        Map<Field, InjectResource.MetaData> resources;

        Map<Field, IntentFilter> receivers;
        Map<Field, InjectServiceConnection.MetaData> serviceConnections;
        Map<Field, InjectView.MetaData> widgets;

        RTInjectorClassMetaData toClassMetaData()
        {
            RTInjectorClassMetaData result = new RTInjectorClassMetaData();

            if (injects != null)
            {
                while (!injects.empty())
                {
                    InjectLayout.MetaData data = injects.pop();
                    if (data.layout != 0)
                    {
                        result.layout = data.layout;
                    }
                    if (data.menu != 0)
                    {
                        result.menu = data.menu;
                    }
                    if (data.title != 0)
                    {
                        result.title = data.title;
                    }
                }
            }
            result.rootIds = rootListeners;
            result.clickers = clickers;
            if (actionBars != null)
            {
                result.actionBars = actionBars;
            }
            if (applications != null)
            {
                result.applications = applications;
            }
            if (systemServices != null)
            {
                result.systemServices = mapToPairList(systemServices);
            }
            if (extras != null)
            {
                result.extras = mapToPairList(extras);
            }
            if (fragments != null)
            {
                result.fragments = mapToPairIntList(fragments);
            }
            if (menuItems != null)
            {
                result.menuItems = mapToPairIntList(menuItems);
            }
            if (resources != null)
            {
                result.resources = mapToPairList(resources);
            }
            if (receivers != null)
            {
                result.receivers = mapToPairList(receivers);
            }
            if (serviceConnections != null)
            {
                result.serviceConnections = mapToPairList(serviceConnections);
            }
            if (widgets != null)
            {
                result.widgets = mapToPairList(widgets);
            }
            return result;
        }
    }

    @SuppressWarnings("unchecked")
    private static <K, V> Pair<K, V>[] mapToPairArray(Map<K, V> map)
    {
        Pair<K, V>[] result = new Pair[map.size()];
        int i = 0;
        for (Map.Entry<K, V> entry : map.entrySet())
        {
            result[i] = new Pair<>(entry.getKey(), entry.getValue());
            i++;
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static <K> RTInjectorClassMetaData.IntPair<K>[] mapToPairIntArray(Map<K, Integer> map)
    {
        RTInjectorClassMetaData.IntPair<K>[] result = new RTInjectorClassMetaData.IntPair[map.size()];
        int i = 0;
        for (Map.Entry<K, Integer> entry : map.entrySet())
        {
            result[i] = new RTInjectorClassMetaData.IntPair<>(entry.getKey(), entry.getValue());
            i++;
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static <K, V> List<Pair<K, V>> mapToPairList(Map<K, V> map)
    {
        List<Pair<K, V>> result = new ArrayList<>(map.size());
        int i = 0;
        for (Map.Entry<K, V> entry : map.entrySet())
        {
            result.add(i, new Pair<>(entry.getKey(), entry.getValue()));
            i++;
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static <K> List<RTInjectorClassMetaData.IntPair<K>> mapToPairIntList(Map<K, Integer> map)
    {
        List<RTInjectorClassMetaData.IntPair<K>> result = new ArrayList<>(map.size());
        int i = 0;
        for (Map.Entry<K, Integer> entry : map.entrySet())
        {
            result.add(i, new RTInjectorClassMetaData.IntPair<>(entry.getKey(), entry.getValue()));
            i++;
        }
        return result;
    }
}
