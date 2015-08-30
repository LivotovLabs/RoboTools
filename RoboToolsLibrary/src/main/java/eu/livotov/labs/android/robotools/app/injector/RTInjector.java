package eu.livotov.labs.android.robotools.app.injector;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.app.Fragment;
import android.app.Service;
import android.content.ContentProvider;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;
import java.util.Enumeration;

import dalvik.system.DexFile;

public class RTInjector
{

    private RTInjector() {
    }

    public final static class ServiceInjector {

        final RTInjectorClassMetaData data;
        final Service service;

        public ServiceInjector(Service service) {
            this.data = RTInjectorClassMetadataLoader.loadClass(service.getClass());
            this.service = service;
        }

        public void onCreate() {
            data.initApplications(service, service.getApplication());
            data.initSystemServices(service, service);
            data.initResources(service, service);
            data.initReceivers(service, service);
            data.initServiceConnections(service, service);
        }

        public void onDestroy() {
            data.releaseApplications(service);
            data.releaseSystemServices(service);
            data.releaseResources(service);
            data.releaseReceivers(service, service);
            data.releaseServiceConnections(service, service);
        }
    }

    public final static class ApplicationInjector {
        final RTInjectorClassMetaData data;
        final Application application;

        public ApplicationInjector(Application application) {
            this.data = RTInjectorClassMetadataLoader.loadClass(application.getClass());
            this.application = application;
        }

        public void onCreate() {
            data.initSystemServices(application, application);
            data.initResources(application, application);
        }
    }

    public final static class ContentProviderInjector {

        final RTInjectorClassMetaData data;
        final ContentProvider contentProvider;

        public ContentProviderInjector(ContentProvider contentProvider) {
            this.data = RTInjectorClassMetadataLoader.loadClass(contentProvider.getClass());
            this.contentProvider = contentProvider;
        }

        public void onCreate() {
            data.initSystemServices(contentProvider, contentProvider.getContext());
            data.initResources(contentProvider, contentProvider.getContext());
        }
    }

    public final static class ActivityInjector {

        final RTInjectorClassMetaData data;
        final Activity activity;

        public ActivityInjector(Activity activity) {
            this.data = RTInjectorClassMetadataLoader.loadClass(activity.getClass());
            this.activity = activity;
        }

        public void onCreate() {
            data.initApplications(activity, activity.getApplication());
            data.initSystemServices(activity, activity);
            data.initResources(activity, activity);
            data.initActionBars(activity, activity.getActionBar());
            Bundle extras = activity.getIntent().getExtras();
            if(extras != null) {
                data.initExtras(activity, extras);
            }

            View contentView = data.initWidgets(activity, activity.getLayoutInflater(), null);
            if(contentView != null) {
                activity.setContentView(contentView);
                data.initClickers(activity, contentView);
            }
            data.initFragments(activity, activity.getFragmentManager());
            if(data.title != 0) {
                activity.setTitle(data.title);
            }
        }

        public void onCreateOptionsMenu(Menu menu) {
            data.initMenuHierarchy(activity, activity.getMenuInflater(), menu);
        }

        public void onStart() {
            data.initReceivers(activity, activity);
            data.initServiceConnections(activity, activity);
        }

        public void onStop() {
            data.releaseReceivers(activity, activity);
            data.releaseServiceConnections(activity, activity);
        }

        public void onDestroy() {
            data.releaseApplications(activity);
            data.releaseSystemServices(activity);
            data.releaseResources(activity);
            data.releaseActionBars(activity);
            data.releaseExtras(activity);
            data.releaseWidgets(activity);
            data.releaseFragments(activity);
            data.releaseMenuHierarchy(activity);
        }

    }

    public final static class FragmentInjector {

        final RTInjectorClassMetaData data;
        final Fragment fragment;

        public FragmentInjector(Fragment fragment) {
            this.data = RTInjectorClassMetadataLoader.loadClass(fragment.getClass());
            this.fragment = fragment;
        }

        public void onCreate() {
            Activity activity = fragment.getActivity();

            data.initApplications(fragment, activity.getApplication());
            data.initSystemServices(fragment, activity);
            data.initResources(fragment, fragment.getActivity());
            if(activity instanceof Activity) {
                data.initActionBars(fragment, activity.getActionBar());
            } else {
                Log.e("Injector", "Cant init ActionBars: Activity myst extends android.support.v7.app.ActionBarActivity");
            }
            Bundle args = fragment.getArguments();
            if(args != null) {
                data.initExtras(fragment, args);
            }
            if(data.menu != 0) {
                fragment.setHasOptionsMenu(true);
            }
        }

        public void onDestroy() {
            data.releaseApplications(fragment);
            data.releaseSystemServices(fragment);
            data.releaseResources(fragment);
            data.releaseActionBars(fragment);
            data.releaseExtras(fragment);
        }

        public void onStart() {
            Activity activity = fragment.getActivity();
            data.initReceivers(fragment, activity);
            data.initServiceConnections(fragment, activity);
        }

        public void onStop() {
            Activity activity = fragment.getActivity();
            data.releaseReceivers(fragment, activity);
            data.releaseServiceConnections(fragment, activity);
        }

        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            data.initMenuHierarchy(fragment, inflater, menu);
        }

        public void onDestroyOptionsMenu() {
            data.releaseMenuHierarchy(fragment);
        }

        @TargetApi(17)
        public View onCreateView(LayoutInflater inflater, ViewGroup container) {
            if(data.title != 0) {
                fragment.getActivity().setTitle(data.title);
            }

            View root = data.initWidgets(fragment, inflater, container);
            if(root != null) {
                data.initClickers(fragment, root);
            }
            data.initFragments(fragment, Build.VERSION.SDK_INT>=17 ? fragment.getChildFragmentManager() : fragment.getFragmentManager());
            return root;
        }

        public void onDestroyView() {
            data.releaseWidgets(fragment);
            data.releaseFragments(fragment);
        }
    }

    public static void loadClasses(Class... classes) {
        if(classes != null) {
            for (Class clazz: classes) {
                RTInjectorClassMetadataLoader.loadClass(clazz);
            }
        }
    }

    public static void loadClasses(Context context, String subpackageName) {
        long start = System.currentTimeMillis();
        int count = 0;
        try {
            String path = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0).sourceDir;
            final String packageName = context.getPackageName();

            if (!TextUtils.isEmpty(subpackageName)) {
                Log.i(RTInjectorClassMetaData.TAG, "Detected specific package name for classes : " + subpackageName);
                if (subpackageName.startsWith(".")) {
                    subpackageName = packageName + subpackageName;
                }
            } else {
                subpackageName = packageName;
            }
            Log.i(RTInjectorClassMetaData.TAG, "Searching for the classes in: " + subpackageName);

            DexFile dexfile = new DexFile(path);
            Enumeration entries = dexfile.entries();

            while (entries.hasMoreElements()) {
                String name = (String) entries.nextElement();

                if (name != null && name.startsWith(subpackageName)) {

                    try {
                        Class clazz = Class.forName(name, true, context.getClass().getClassLoader());
                        if(clazz != null) {
                            RTInjectorClassMetadataLoader.loadClass(clazz);
                            count++;
                        }
                    } catch (ClassNotFoundException e) {
                        Log.e(RTInjectorClassMetaData.TAG, e.getMessage(), e);
                    }
                }
            }
        } catch (IOException | PackageManager.NameNotFoundException e) {
            Log.e(RTInjectorClassMetaData.TAG, e.getMessage(), e);
        }
        Log.i(RTInjectorClassMetaData.TAG, String.format("Found %s classes in %s ms", count, (System.currentTimeMillis() - start)));
    }

}
