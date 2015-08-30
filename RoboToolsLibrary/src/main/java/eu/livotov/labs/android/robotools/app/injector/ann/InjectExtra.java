package eu.livotov.labs.android.robotools.app.injector.ann;

import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.util.SparseArray;

import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;

/**
 * Injects value of an extra parameter of {@link android.app.Activity} или {@link android.app.Fragment}.
 * <p/>
 * Supported extra value types are: <br />
 * <ul>
 * <li>{@link boolean}</li>
 * <li>{@link boolean[]}</li>
 * <li>{@link android.os.Bundle}</li>
 * <li>{@link byte}</li>
 * <li>{@link byte[]}</li>
 * <li>{@link char}</li>
 * <li>{@link char[]}</li>
 * <li>{@link CharSequence}</li>
 * <li>{@link CharSequence[]}</li>
 * <li>{@link double}</li>
 * <li>{@link double[]}</li>
 * <li>{@link float}</li>
 * <li>{@link float[]}</li>
 * <li>{@link android.os.IBinder}</li>
 * <li>{@link int}</li>
 * <li>{@link int[]}</li>
 * <li>{@link java.util.ArrayList<Integer><}</li>
 * <li>{@link long}</li>
 * <li>{@link long[]}</li>
 * <li>{@link android.os.Parcelable[]}</li>
 * <li>{@link android.os.Parcelable}</li>
 * <li>{@link java.util.ArrayList<android.os.Parcelable>}</li>
 * <li>{@link java.io.Serializable}</li>
 * <li>{@link short}</li>
 * <li>{@link short[]}</li>
 * <li>{@link android.util.SparseArray<android.os.Parcelable>}</li>
 * <li>{@link String}</li>
 * <li>{@link String[]}</li>
 * <li>{@link java.util.ArrayList<String>}</li>
 * <p/>
 * </ul>
 * <p/>
 * In case extra key is not found, field will be left blank.
 * In case non compatible extra value type, field will be  left blank among with the logcat warning.
 * Applicable to {@link android.app.Activity}, {@link android.app.Fragment}, {@link android.app.Service}.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface InjectExtra {

    /**
     * @return extra name to serialize
     */
    String value();

    static class MetaData {

        public final String extra;
        public final Type type;

        public enum Type {
            String,
            StringArray,
            Int,
            IntArray,
            ParcelableArray,
            Parcelable,
            Boolean,
            BooleanArray,
            Bundle,
            Byte,
            ByteArray,
            Char,
            CharArray,
            CharSequence,
            CharSequenceArray,
            Double,
            DoubleArray,
            Float,
            FloatArray,
            IBinder,
            ArrayListInteger,
            ArrayListString,
            ArrayListParcelable,
            Long,
            LongArray,
            Serializable,
            Short,
            ShortArray,
            SparseParcelableArray
        }

        public MetaData(InjectExtra extra, Field field) {
            this.extra = extra.value();

            Class clazz = field.getType();

            if (String.class == clazz) {
                type = Type.String;
            } else if (String[].class == clazz) {
                type = Type.StringArray;
            } else if (int.class == clazz) {
                type = Type.Int;
            } else if (int[].class == clazz) {
                type = Type.IntArray;
            } else if (Parcelable[].class.isAssignableFrom(clazz)) {
                type = Type.ParcelableArray;
            } else if (Parcelable.class.isAssignableFrom(clazz)) {
                type = Type.Parcelable;
            } else if (boolean.class == clazz) {
                type = Type.Boolean;
            } else if (boolean[].class == clazz) {
                type = Type.BooleanArray;
            } else if (Bundle.class == clazz) {
                type = Type.Bundle;
            } else if (byte.class == clazz) {
                type = Type.Byte;
            } else if (byte[].class == clazz) {
                type = Type.ByteArray;
            } else if (char.class == clazz) {
                type = Type.Char;
            } else if (char[].class == clazz) {
                type = Type.CharArray;
            } else if (CharSequence.class == clazz) {
                type = Type.CharSequence;
            } else if (CharSequence[].class == clazz) {
                type = Type.CharSequenceArray;
            } else if (double.class == clazz) {
                type = Type.Double;
            } else if (double[].class == clazz) {
                type = Type.DoubleArray;
            } else if (float.class == clazz) {
                type = Type.Float;
            } else if (float[].class == clazz) {
                type = Type.FloatArray;
            } else if (IBinder.class.isAssignableFrom(clazz)) {
                type = Type.IBinder;
            } else if (ArrayList.class == clazz) {
                java.lang.reflect.Type genericFieldType = field.getGenericType();
                java.lang.reflect.Type[] parameters;
                if (genericFieldType instanceof ParameterizedType && (parameters = ((ParameterizedType) genericFieldType).getActualTypeArguments()).length > 0) {
                    Class t = (Class) parameters[0];
                    if (Integer.class.equals(t)) {
                        type = Type.ArrayListInteger;
                    } else if (String.class.equals(t)) {
                        type = Type.ArrayListString;
                    } else if (Parcelable.class.isAssignableFrom(t)) {
                        type = Type.ArrayListParcelable;
                    } else {
                        type = null;
                    }
                } else {
                    type = null;
                }
            } else if (long.class == clazz) {
                type = Type.Long;
            } else if (long[].class == clazz) {
                type = Type.LongArray;
            } else if (Serializable.class.isAssignableFrom(clazz)) {
                type = Type.Serializable;
            } else if (short.class == clazz) {
                type = Type.Short;
            } else if (short[].class == clazz) {
                type = Type.ShortArray;
            } else if (SparseArray.class == clazz) {
                java.lang.reflect.Type genericFieldType = field.getGenericType();
                java.lang.reflect.Type[] parameters;
                if (genericFieldType instanceof ParameterizedType && (parameters = ((ParameterizedType) genericFieldType).getActualTypeArguments()).length > 0 && Parcelable.class.isAssignableFrom((Class) parameters[0])) {
                    type = Type.SparseParcelableArray;
                } else {
                    type = null;
                }
            } else {
                type = null;
            }
        }


    }
}
