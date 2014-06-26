package eu.livotov.labs.android.robotools.app;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;
import eu.livotov.labs.android.robotools.R;
import eu.livotov.labs.android.robotools.content.RequestQueue;
import eu.livotov.labs.android.robotools.injector.Injector;

public class Activity extends FragmentActivity {

    private RequestQueue mLoader;

    public void showToast(CharSequence text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

    public void showToast(int textId) {
        Toast.makeText(getApplicationContext(), textId, Toast.LENGTH_SHORT).show();
    }

    public RequestQueue getRestLoader() {
        if (mLoader == null) {
            mLoader = RequestQueue.with(this);
        }
        return mLoader;
    }

    @Override
    public void startActivity(Intent intent) {
        try {
            super.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getApplicationContext(), R.string.application_not_found, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Injector.init(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Injector.recycle(this);
    }
}
