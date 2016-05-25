package eu.livotov.labs.android.robotools.utils;

import android.support.annotation.NonNull;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Small helper class for safe working with Lists
 */
public class RTListUtil {
    /**
     * Replace all elements from source into destination. Destination List will be cleared
     */
    public static <T> void replace(@NonNull List<? super T> destination, @NonNull List<? extends T> source) {
        if (destination.size() != 0) {
            destination.clear();
        }
        destination.addAll(source);
    }

    /**
     * Add all elements from source into destination.
     */
    public static <T> void addAll(@NonNull List<? super T> destination, @NonNull List<? extends T> source) {
        destination.addAll(source);
    }

    /**
     * Add all unique elements from source into destination
     * <br/> If some element already exist in destination List, he will ignored
     */
    public static <T> void addUnique(@NonNull List<? super T> destination, @NonNull List<? extends T> source) {

        for (int i = 0; i < source.size(); i++) {
            if (!destination.contains(source.get(i))) {
                destination.add(source.get(i));
            }
        }
    }

    /**
     * Safe check if give list contains given element
     */
    public static <T> boolean contains(@NonNull List<? super T> list, @NonNull T element) {
        if (isEmpty(list))
            return false;
        return list.contains(element);
    }

    /**
     * Safe check if give array contains given element
     */
    public static <T> boolean contains(@NonNull T[] array, @NonNull T element) {
        for (T item : array) {
            if (item != null) {
                if (item.equals(element)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns index of the give element in give array, or -1 if such element was not found
     */
    public static <T> int indexOf(@NonNull T[] array, @NonNull T element) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(element)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Check if given element not last in given list
     */
    public static <T> boolean isNotLast(@NonNull List<T> list, @NonNull T element) {
        return list.indexOf(element) < list.size() - 1;
    }

    /**
     * Check if such position safe for given list to get some element.
     * <br/> Use to avoid {@link ArrayIndexOutOfBoundsException} or etc.
     */
    public static <T> boolean isSafePosittion(List<T> list, int position) {
        return !isEmpty(list) && isSafePosition(list.size(), position);
    }

    /**
     * Check if such position safe for given list size to get some element.
     * <br/> Use to avoid {@link ArrayIndexOutOfBoundsException} or etc.
     */
    public static boolean isSafePosition(int totalCount, int position) {
        return position >= 0 && position < totalCount;
    }

    /**
     * Check list null or empty
     */
    public static <T> boolean isEmpty(List<T> list) {
        return list == null || list.isEmpty();
    }

    /**
     * Safe wrapper method for {@link Collections#sort(List)}
     */
    public static <T extends Comparable> void sort(@NonNull List<T> list) {
        if (isEmpty(list))
            return;
        Collections.sort(list);
    }

    /**
     * Safe wrapper method for {@link Collections#sort(List, Comparator)}
     */
    public static <T extends Comparable> void sort(@NonNull List<T> list, Comparator<? super T> comparator) {
        if (isEmpty(list) || comparator == null)
            return;
        Collections.sort(list, comparator);
    }
}
