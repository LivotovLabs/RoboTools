package eu.livotov.labs.android.robotools.widget;

import android.widget.BaseAdapter;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public abstract class BaseCollectionAdapter<T> extends BaseAdapter {

    protected final Object lock = new Object();
    protected List<T> data;

    public void add(T object) {
        synchronized (lock) {
            data.add(object);
        }
        notifyDataSetChanged();
    }

    public void add(int position, T object) {
        synchronized (lock) {
            data.add(position, object);
        }
        notifyDataSetChanged();
    }

    public void addAll(Collection<? extends T> collection) {
        synchronized (lock) {
            data.addAll(collection);
        }
        notifyDataSetChanged();
    }

    public void insert(T object, int index) {
        synchronized (lock) {
            data.add(index, object);
        }
        notifyDataSetChanged();
    }

    public int size() {
        if (data != null) {
            synchronized (lock) {
                return data.size();
            }
        }
        return 0;
    }

    public boolean setData(List<T> data) {
        synchronized (lock) {
            this.data = data;
        }
        notifyDataSetChanged();
        return true;
    }

    public java.util.List<T> subList(int start, int end) {
        synchronized (lock) {
            return data.subList(start, end);
        }
    }

    public void remove(T object) {
        synchronized (lock) {
            data.remove(object);
        }
        notifyDataSetChanged();
    }

    public void clear() {
        synchronized (lock) {
            data.clear();
        }
        notifyDataSetChanged();
    }

    public void sort(Comparator<? super T> comparator) {
        synchronized (lock) {
            Collections.sort(data, comparator);
        }
        notifyDataSetChanged();
    }

    @Override
    public boolean isEmpty() {
        return data == null || data.size() == 0;
    }
}