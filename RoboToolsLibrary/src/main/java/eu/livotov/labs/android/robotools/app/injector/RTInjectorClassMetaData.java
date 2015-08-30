package eu.livotov.labs.android.robotools.app.injector;

import android.animation.AnimatorInflater;
import android.app.ActionBar;
import android.app.Application;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import eu.livotov.labs.android.robotools.app.injector.ann.InjectExtra;
import eu.livotov.labs.android.robotools.app.injector.ann.InjectResource;
import eu.livotov.labs.android.robotools.app.injector.ann.InjectServiceConnection;
import eu.livotov.labs.android.robotools.app.injector.ann.InjectView;

/**
 * Represent injector's class metadata
 */
final class RTInjectorClassMetaData
{

    static final String TAG = "RTInjector";
    static final boolean DEBUG = true;

    int menu;
    int layout;
    int title;

    List<Field> actionBars;
    List<Field> applications;
    List<Pair<Field, String>> systemServices;

    Map<Class, int[]> rootIds;

    Map<Integer, Method> clickers;
    List<Pair<Field, InjectExtra.MetaData>> extras;

    List<IntPair<Field>> fragments;
    List<IntPair<Field>> menuItems;
    List<Pair<Field, InjectResource.MetaData>> resources;

    List<Pair<Field, IntentFilter>> receivers;
    List<Pair<Field, InjectServiceConnection.MetaData>> serviceConnections;
    List<Pair<Field, InjectView.MetaData>> widgets;

    public void initActionBars(Object to, ActionBar actionBar) {
        if (actionBars != null) {
            for (Field field : actionBars) {
                try {
                    field.set(to, actionBar);
                } catch (Throwable t) {
                    logReportInit(ActionBar.class, field, t);
                }
            }
        }
    }

    public void releaseActionBars(Object to) {
        if (actionBars != null) {
            for (Field field : actionBars) {
                try {
                    field.set(to, null);
                } catch (Throwable t) {
                    logReportRelease(field, t);
                }
            }
        }
    }

    public void initApplications(Object to, Application application) {
        if (applications != null) {
            for (Field field : applications) {
                try {
                    field.set(to, application);
                } catch (Throwable t) {
                    logReportInit(Application.class, field, t);
                }
            }
        }
    }

    public void releaseApplications(Object to) {
        if (applications != null) {
            for (Field field : applications) {
                try {
                    field.set(to, null);
                } catch (Throwable t) {
                    logReportRelease(field, t);
                }
            }
        }
    }

    public void initSystemServices(Object to, Context context) {
        if (systemServices != null) {
            for (Pair<Field, String> entry : systemServices) {
                try {
                    String serviceId = entry.second;
                    if (serviceId != null) {
                        //noinspection ResourceType
                        entry.first.set(to, context.getSystemService(serviceId));
                    } else {
                        throw new IllegalArgumentException("No such system service " + entry.getClass().getSimpleName());
                    }
                } catch (Throwable t) {
                    logReportInit("SystemService", entry.first, t);
                }
            }
        }
    }

    public void releaseSystemServices(Object to) {
        if (systemServices != null) {
            for (Pair<Field, String> pair : systemServices) {
                try {
                    pair.first.set(to, null);
                } catch (Throwable t) {
                    logReportInit("System service", pair.first, t);
                }
            }
        }
    }

    public void initMenuHierarchy(Object to, MenuInflater inflater, Menu menu) {
        if (this.menu != 0) {
            inflater.inflate(this.menu, menu);
            if (menuItems != null) {
                for (IntPair<Field> entry : menuItems) {
                    Field field = entry.first;
                    try {
                        field.set(to, menu.findItem(entry.second));
                    } catch (Throwable t) {
                        logReportInit(MenuItem.class, field, t);
                    }
                }
            }
        }
    }

    public void releaseMenuHierarchy(Object to) {
        if (menuItems != null) {
            for (IntPair<Field> entry : menuItems) {
                try {
                    entry.first.set(to, null);
                } catch (Throwable t) {
                    logReportRelease(entry.first, t);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void initFragments(Object to, FragmentManager fragmentManager) {
        if (fragments != null) {
            for (IntPair<Field> entry : fragments) {
                Field field = entry.first;
                Class clazz = field.getType();
                int id = entry.second;

                try {
                    if (entry.first == null || !Fragment.class.isAssignableFrom(clazz)) { // null to skip this block next time without isAssignableFrom check
                        entry.first = null;
                        throw new ClassCastException(clazz.getSimpleName() + " is not assignable to " + Fragment.class.getSimpleName());
                    }

                    Fragment fragment = fragmentManager.findFragmentById(id);
                    if (fragment == null) {
                        fragment = ((Class<? extends Fragment>) clazz).newInstance();
                        fragmentManager.beginTransaction().add(id, fragment).commit();
                    }
                    field.set(to, fragment);
                } catch (Throwable t) {
                    logReportInit(Fragment.class, field, t);
                }
            }
        }
    }

    public void releaseFragments(Object to) {
        if (fragments != null) {
            for (IntPair<Field> entry : fragments) {
                try {
                    entry.first.set(to, null);
                } catch (Throwable t) {
                    logReportRelease(entry.first, t);
                }
            }
        }
    }

    public void initReceivers(Object to, Context context) {
        if (receivers != null) {
            for (Pair<Field, IntentFilter> entry : receivers) {
                try {
                    context.registerReceiver((BroadcastReceiver) entry.first.get(to), entry.second);
                } catch (Throwable t) {
                    logReportInit(BroadcastReceiver.class, entry.first, t);
                }
            }
        }
    }

    public void releaseReceivers(Object to, Context context) {
        if (receivers != null) {
            for (Pair<Field, IntentFilter> entry : receivers) {
                try {
                    context.unregisterReceiver((BroadcastReceiver) entry.first.get(to));
                } catch (Throwable t) {
                    logReportRelease(entry.first, t);
                }
            }
        }
    }

    public void initServiceConnections(Object to, Context context) {
        if (serviceConnections != null) {
            for (Pair<Field, InjectServiceConnection.MetaData> entry : serviceConnections) {
                Field field = entry.first;
                InjectServiceConnection.MetaData metaData = entry.second;

                try {
                    context.bindService(new Intent(context, metaData.clazz), ((android.content.ServiceConnection) field.get(to)), metaData.flag);
                } catch (Throwable t) {
                    logReportInit(android.content.ServiceConnection.class, field, t);
                }
            }
        }
    }

    public void releaseServiceConnections(Object to, Context context) {
        if (serviceConnections != null) {
            for (Pair<Field, InjectServiceConnection.MetaData> entry : serviceConnections) {
                try {
                    context.unbindService((android.content.ServiceConnection) entry.first.get(to));
                } catch (Throwable t) {
                    logReportRelease(entry.first, t);
                }
            }
        }
    }

    public void initResources(Object to, Context context) {
        if (this.resources != null && this.resources.size() > 0) {
            Resources resources = context.getResources();

            TypedValue tv = null;

            for (Pair<Field, InjectResource.MetaData> entry : this.resources) {
                Field field = entry.first;
                InjectResource.MetaData metaData = entry.second;

                InjectResource.MetaData.Type type = metaData.type;
                int id = metaData.id;

                try {
                    if (type != null) {
                        switch (type) {
                            case CharSequence:
                                field.set(to, resources.getText(id));
                                break;
                            case String:
                                field.set(to, resources.getString(id));
                                break;
                            case CharSequenceArray:
                                field.set(to, resources.getTextArray(id));
                                break;
                            case StringArray:
                                field.set(to, resources.getStringArray(id));
                                break;
                            case Drawable:
                                field.set(to, resources.getDrawable(id));
                                break;
                            case Int_:
                                if (tv == null) {
                                    tv = new TypedValue();
                                }
                                resources.getValue(id, tv, true);
                                if (tv.type == TypedValue.TYPE_DIMENSION) {
                                    field.setInt(to, (int) TypedValue.complexToDimension(tv.data, resources.getDisplayMetrics()));
                                } else {
                                    field.setInt(to, tv.data);
                                }
                                break;
                            case Float_:
                                if (tv == null) {
                                    tv = new TypedValue();
                                }
                                resources.getValue(id, tv, true);
                                if (tv.type == TypedValue.TYPE_DIMENSION) {
                                    field.setFloat(to, TypedValue.complexToDimension(tv.data, resources.getDisplayMetrics()));
                                } else if (tv.type == TypedValue.TYPE_FRACTION) {
                                    field.setFloat(to, TypedValue.complexToFraction(tv.data, 1, 1));
                                } else {
                                    throw new IllegalArgumentException("float type must be used only for dimensions and fraction resources");
                                }
                                break;
                            case Boolean_:
                                field.setBoolean(to, resources.getBoolean(id));
                                break;
                            case IntArray:
                                field.set(to, resources.getIntArray(id));
                                break;
                            case Animation:
                                field.set(to, AnimationUtils.loadAnimation(context, id));
                                break;
                            case Interpolator:
                                field.set(to, AnimationUtils.loadInterpolator(context, id));
                                break;
                            case Animator:
                                field.set(to, AnimatorInflater.loadAnimator(context, id));
                                break;
                            case XmlResourceParser:
                                field.set(to, resources.getXml(id));
                                break;
                            case Movie:
                                field.set(to, resources.getMovie(id));
                                break;
                            case ColorStateList:
                                field.set(to, resources.getColorStateList(id));
                                break;
                            case Boolean:
                                field.set(to, resources.getBoolean(id));
                                break;
                            case Integer:
                                if (tv == null) {
                                    tv = new TypedValue();
                                }
                                resources.getValue(id, tv, true);
                                if (tv.type == TypedValue.TYPE_DIMENSION) {
                                    field.set(to, (int) TypedValue.complexToDimension(tv.data, resources.getDisplayMetrics()));
                                } else {
                                    field.set(to, tv.data);
                                }
                                break;
                            case Float:
                                if (tv == null) {
                                    tv = new TypedValue();
                                }
                                resources.getValue(id, tv, true);
                                if (tv.type == TypedValue.TYPE_DIMENSION) {
                                    field.set(to, TypedValue.complexToDimension(tv.data, resources.getDisplayMetrics()));
                                } else if (tv.type == TypedValue.TYPE_FRACTION) {
                                    field.set(to, TypedValue.complexToFraction(tv.data, 1, 1));
                                } else {
                                    throw new IllegalArgumentException("Float type must be used only for dimensions and fraction resources");
                                }
                                break;
                        }
                    } else {
                        throw new IllegalArgumentException("Unsupported field type");
                    }
                } catch (Throwable t) {
                    logReportInit(Resources.class, field, t);
                }
            }
        }
    }

    public void releaseResources(Object to) {
        if (resources != null) {
            for (Pair<Field, InjectResource.MetaData> entry : resources) {
                try {
                    if (!entry.first.getType().isPrimitive()) {
                        entry.first.set(to, null);
                    }
                } catch (Throwable t) {
                    logReportRelease(entry.first, t);
                }
            }
        }
    }

    public void initClickers(Object to, View root) {
        if (this.clickers != null && !clickers.isEmpty()) {
            ReflectClickListener listener = new ReflectClickListener(to, clickers);
            for (Integer id : clickers.keySet()) {
                root.findViewById(id).setOnClickListener(listener);
            }
        }
    }

    public void initExtras(Object to, Bundle bundle) {
        if (extras != null) {
            for (Pair<Field, InjectExtra.MetaData> entry : extras) {
                Field field = entry.first;
                InjectExtra.MetaData metaData = entry.second;

                InjectExtra.MetaData.Type type = metaData.type;
                String key = metaData.extra;

                if (bundle.containsKey(key)) {
                    if (type != null) {
                        try {
                            switch (type) {
                                case String:
                                    field.set(to, bundle.getString(key));
                                    break;
                                case StringArray:
                                    field.set(to, bundle.getStringArray(key));
                                    break;
                                case Int:
                                    field.setInt(to, bundle.getInt(key));
                                    break;
                                case IntArray:
                                    field.set(to, bundle.getIntArray(key));
                                    break;
                                case ParcelableArray:
                                    field.set(to, bundle.getParcelableArray(key));
                                    break;
                                case Parcelable:
                                    field.set(to, bundle.getParcelable(key));
                                    break;
                                case Boolean:
                                    field.setBoolean(to, bundle.getBoolean(key));
                                    break;
                                case BooleanArray:
                                    field.set(to, bundle.getBooleanArray(key));
                                    break;
                                case Bundle:
                                    field.set(to, bundle.getBundle(key));
                                    break;
                                case Byte:
                                    field.setByte(to, bundle.getByte(key));
                                    break;
                                case ByteArray:
                                    field.set(to, bundle.getByteArray(key));
                                    break;
                                case Char:
                                    field.setChar(to, bundle.getChar(key));
                                    break;
                                case CharArray:
                                    field.set(to, bundle.getCharArray(key));
                                    break;
                                case CharSequence:
                                    field.set(to, bundle.getCharSequence(key));
                                    break;
                                case CharSequenceArray:
                                    field.set(to, bundle.getCharSequenceArray(key));
                                    break;
                                case Double:
                                    field.setDouble(to, bundle.getDouble(key));
                                    break;
                                case DoubleArray:
                                    field.set(to, bundle.getDoubleArray(key));
                                    break;
                                case Float:
                                    field.setFloat(to, bundle.getFloat(key));
                                    break;
                                case FloatArray:
                                    field.set(to, bundle.getFloatArray(key));
                                    break;
                                case IBinder:
                                    if (Build.VERSION.SDK_INT>=18)
                                    {
                                        field.set(to, bundle.getBinder(key));
                                    }
                                    break;
                                case ArrayListInteger:
                                    field.set(to, bundle.getIntegerArrayList(key));
                                    break;
                                case ArrayListString:
                                    field.set(to, bundle.getStringArrayList(key));
                                    break;
                                case ArrayListParcelable:
                                    field.set(to, bundle.getParcelableArrayList(key));
                                    break;
                                case Long:
                                    field.setLong(to, bundle.getLong(key));
                                    break;
                                case LongArray:
                                    field.set(to, bundle.getLongArray(key));
                                    break;
                                case Serializable:
                                    field.set(to, bundle.getSerializable(key));
                                    break;
                                case Short:
                                    field.setShort(to, bundle.getShort(key));
                                    break;
                                case ShortArray:
                                    field.set(to, bundle.getShortArray(key));
                                    break;
                                case SparseParcelableArray:
                                    field.set(to, bundle.getSparseParcelableArray(key));
                                    break;
                            }
                        } catch (Throwable t) {
                            logReportInit("Extra", field, t);
                        }
                    } else {
                        throw new IllegalArgumentException("Unsupported field type");
                    }
                }
            }
        }
    }

    public void releaseExtras(Object to) {
        if (extras != null) {
            for (Pair<Field, InjectExtra.MetaData> field : extras) {
                try {
                    if (!field.first.getType().isPrimitive()) {
                        field.first.set(to, null);
                    }
                } catch (Throwable t) {
                    logReportRelease(field.first, t);
                }
            }
        }
    }

    public View initWidgets(Object to, LayoutInflater inflater, ViewGroup container) {
        View root = null;
        if (layout != 0) {
            root = inflater.inflate(layout, container, false);

            if (widgets != null) {
                for (Pair<Field, InjectView.MetaData> entry : widgets) {
                    Field field = entry.first;
                    InjectView.MetaData data = entry.second;

                    try {
                        View view = root.findViewById(data.id);
                        if (view != null) {
                            field.set(to, view);
                            int id = view.getId();
                        }
                    } catch (Throwable t) {
                        logReportInit("Widget", field, t);
                    }
                }
            }
        }
        return root;
    }

    public void releaseWidgets(Object to) {
        if (widgets != null) {
            for (Pair<Field, InjectView.MetaData> entry : widgets) {
                try {
                    entry.first.set(to, null);
                } catch (Throwable t) {
                    logReportRelease(entry.first, t);
                }
            }
        }
    }

    private static boolean contains(int[] in, int v) {
        for (final int i : in) {
            if (i == v) {
                return true;
            }
        }
        return false;
    }

    private static void logReportInit(Class value, Field field, Throwable t) {
        logReportInit(value.getSimpleName(), field, t);
    }

    private static void logReportInit(String value, Field field, Throwable t) {
        if (DEBUG) {
            Log.e(TAG, String.format("Error, %s value on field %s#%s:%s", value, field.getDeclaringClass().getSimpleName(), field.getClass(), field.getName()), t);
        }
    }

    private static void logReportRelease(Field field, Throwable t) {
        if (DEBUG) {
            Log.e(TAG, String.format("Unable to release field %s#%s:%s", field.getDeclaringClass().getSimpleName(), field.getClass(), field.getName()), t);
        }
    }

    private final static class ReflectClickListener implements View.OnClickListener {

        final Object receiver;
        final Map<Integer, Method> clickers;

        private ReflectClickListener(Object receiver, Map<Integer, Method> clickers) {
            this.receiver = receiver;
            this.clickers = clickers;
        }

        @Override
        public void onClick(View v) {
            if (clickers != null) {
                Method method = clickers.get(v.getId());
                if (method != null) {
                    try {
                        method.invoke(receiver);
                    } catch (Throwable t) {
                        if (DEBUG) {
                            Log.e(TAG, String.format("Unable to invoke method %s() in class %s", method.getName(), receiver.getClass().getSimpleName()), t);
                        }
                    }
                }
            }
        }
    }

    // Help class to reduce int autoboxing
    final static class IntPair<T> {
        T first;
        int second;

        IntPair(T first, int second) {
            this.first = first;
            this.second = second;
        }
    }
}
