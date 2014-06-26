package eu.livotov.labs.android.robotools.content;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.regex.Pattern;

/**
 * Universal data list for REST API.
 * This class is not thread-safe.
 *
 * @param <T> type of stored values.
 */
@SuppressWarnings("unchecked")
public class RTList<T extends Model> extends Model implements java.util.List<T>, Parcelable {

    /**
     * Decorated list
     */
    private ArrayList<T> items = new ArrayList<T>();

    /**
     * Creates empty list.
     */
    public RTList() {

    }

    /**
     * Creates list and fills it according with given data.
     */
    public RTList(java.util.List<? extends T> data) {
        assert data != null;
        items = new ArrayList<T>(data);
    }

    /**
     * Creates list and fills it according with data in {@code from}.
     *
     * @param from  an array of items in the list. You can use null.
     * @param clazz class represents a model that has a public constructor with {@link org.json.JSONObject} argument.
     */
    public RTList(JSONArray from, Class<? extends T> clazz) {
        fill(from, clazz);
    }

    /**
     * Creates list and fills it according with data in {@code from}.
     *
     * @param from    an array of items in the list. You can use null.
     * @param creator interface implementation to parse objects.
     */
    public RTList(JSONArray from, Parser<T> creator) {

        fill(from, creator);
    }

    /**
     * Creates list and fills it according with data in {@code from}.
     *
     * @param from  an array of items in the list. You can use null.
     * @param clazz class represents a model that has a public constructor with {@link org.json.JSONObject} argument.
     */
    public void fill(JSONArray from, Class<? extends T> clazz) {
        try {
            fill(from, new ReflectParser<T>(clazz));
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    /**
     * Fills list according with data in {@code from}.
     *
     * @param from    an array of items in the list. You can use null.
     * @param creator interface implementation to parse objects.
     */
    public void fill(JSONArray from, Parser<? extends T> creator) {
        if (from != null) {
            for (int i = 0; i < from.length(); i++) {
                try {
                    T object = creator.parseObject(from.getJSONObject(i));
                    if (object != null) {
                        items.add(object);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * Searches through the list of available items. <br />
     * <br />
     * The search will be carried out not by the content of characters per line, and the content of them in separate words. <br />
     * <br />
     * Search is not case sensitive.  <br />
     * <br />
     * To support search class {@code T} must have overridden method {@link #toString()},
     * search will be carried out exactly according to the result of calling this method. <br />
     * <br />
     * <br />
     * Suppose there are elements in the list of contents:
     * <code><pre>
     * - Hello world
     * - Hello test
     * </pre></code>
     * In this case, the matches will be on search phrases {@code 'Hel'}, {@code 'Hello'}, {@code 'test'}, but not on {@code 'llo'}, {@code 'llo world'}
     *
     * @param query search query can not be equal to {@code null}, but can be an empty string.
     * @return created based on the search results new list. If no matches are found, the list will be empty.
     */
    public RTList<T> search(String query) {
        RTList<T> result = new RTList<T>();
        final Pattern pattern = Pattern.compile("(?i).*\\b" + query + ".*");
        for (T item : this) {
            if (pattern.matcher(((Object) item).toString()).find()) {
                result.add(item);
            }
        }
        return result;
    }

    @Override
    public void add(int location, T object) {
        items.add(location, object);
    }

    @Override
    public boolean add(T object) {
        return items.add(object);
    }

    @Override
    public boolean addAll(int location, Collection<? extends T> collection) {
        return items.addAll(location, collection);
    }

    @Override
    public boolean addAll(Collection<? extends T> collection) {
        return items.addAll(collection);
    }

    @Override
    public void clear() {
        items.clear();
    }

    @Override
    public boolean contains(Object object) {
        return items.contains(object);
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        assert collection != null;
        return items.containsAll(collection);
    }

    @Override
    public boolean equals(Object object) {
        return ((Object) this).getClass().equals(object.getClass()) && items.equals(object);
    }

    @Override
    public T get(int location) {
        return items.get(location);
    }

    @Override
    public int indexOf(Object object) {
        return items.indexOf(object);
    }

    @Override
    public boolean isEmpty() {
        return items.isEmpty();
    }

    @Override
    public Iterator<T> iterator() {
        return items.iterator();
    }

    @Override
    public int lastIndexOf(Object object) {
        return items.lastIndexOf(object);
    }


    @Override
    public ListIterator<T> listIterator() {
        return items.listIterator();
    }

    @Override
    public ListIterator<T> listIterator(int location) {
        return items.listIterator(location);
    }

    @Override
    public T remove(int location) {
        return items.remove(location);
    }

    @Override
    public boolean remove(Object object) {
        return items.remove(object);
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        assert collection != null;
        return items.removeAll(collection);
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        return items.retainAll(collection);
    }

    @Override
    public T set(int location, T object) {
        return items.set(location, object);
    }

    @Override
    public int size() {
        return items.size();
    }

    @Override
    public java.util.List<T> subList(int start, int end) {
        return items.subList(start, end);
    }

    @Override
    public Object[] toArray() {
        return items.toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] array) {
        assert array != null;
        return items.toArray(array);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(items);
    }

    private RTList(Parcel in) {
       in.readList(items, Model.class.getClassLoader());
    }

    public static Creator<RTList> CREATOR = new Creator<RTList>() {
        public RTList createFromParcel(Parcel source) {
            return new RTList(source);
        }

        public RTList[] newArray(int size) {
            return new RTList[size];
        }
    };

    /**
     * Used when parsing the list objects as interator created from {@link org.json.JSONArray} a instances of items of the list.
     *
     * @param <D> list item type.
     */
    public static interface Parser<D> {

        /**
         * Creates a list item of its representation return REST API from {@link org.json.JSONArray}
         *
         * @param source representation of the object in the format returned by REST API.
         * @return created element to add to the list.
         * @throws Exception if the exception is thrown, the element iterated this method will not be added to the list.
         */
        D parseObject(JSONObject source) throws Exception;
    }

    /**
     * Parser list items using reflection mechanism.
     * To use an object class must have a public constructor that accepts {@link org.json.JSONObject}.
     * If, during the creation of the object constructor will throw any exception, the element will not be added to the list.
     *
     * @param <D> list item type.
     */
    public final static class ReflectParser<D extends Model> implements Parser<D> {

        private final Class<? extends D> clazz;

        public ReflectParser(Class<? extends D> clazz) throws NoSuchMethodException {
            this.clazz = clazz;
        }

        @Override
        public D parseObject(JSONObject source) throws Exception {
            return (D) clazz.newInstance().parse(source);
        }
    }

}
