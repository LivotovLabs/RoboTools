package eu.livotov.labs.android.robotools.content;

import android.os.Parcelable;
import android.util.SparseArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map;

/**
 * Root class for all models.
 * <p/>
 * Model is also allows you to store some object inside as a tag.
 */
@SuppressWarnings("unused")
public abstract class Model {

    /**
     * The model's tag.
     */
    private Object mTag;

    /**
     * Map used to store model's tags.
     */
    private SparseArray<Object> mKeyedTags;

    /**
     * Returns this model's tag.
     *
     * @return the Object stored in this model as a tag
     * @see #setTag(Object)
     * @see #getTag(int)
     */
    public Object getTag() {
        return mTag;
    }

    /**
     * Sets the tag associated with this model. A tag can be used to store
     * data within a model without resorting to another data structure.
     *
     * @param tag an Object to tag the model with
     * @see #getTag()
     * @see #setTag(int, Object)
     */
    public void setTag(Object tag) {
        mTag = tag;
    }

    /**
     * Returns the tag associated with this model and the specified key.
     *
     * @param key The key identifying the tag
     * @return the Object stored in this model as a tag
     * @see #setTag(int, Object)
     * @see #getTag()
     */
    public Object getTag(int key) {
        if (mKeyedTags != null) return mKeyedTags.get(key);
        return null;
    }

    /**
     * Sets a tag associated with this model and a key. A tag can be used
     * to store data within a model without resorting to another
     * data structure.
     *
     * @see #setTag(Object)
     * @see #getTag(int)
     */
    public void setTag(int key, final Object tag) {
        if (mKeyedTags == null) {
            mKeyedTags = new SparseArray<Object>(2);
        }
        mKeyedTags.put(key, tag);
    }

    /**
     * Parses object from source.
     *
     * @param response server API object.
     * @return this object.
     * @throws JSONException if any critical error occurred while parsing.
     */
    public Model parse(JSONObject response) throws JSONException {
        return parseViaReflection(this, response);
    }

    /**
     * Parses object with follow rules:
     * <p/>
     * 1. All fields should had a public access.
     * 2. The name of the filed should be fully equal to name of JSONObject key.
     * 3. Supports parse of all Java primitives, all {@link java.lang.String},
     * arrays of primitive types, {@link java.lang.String}s and {@link Model}s,
     * list implementation like {@link RTList}
     *
     * @param object object to initialize
     * @param source data to read values
     * @param <T>    type of result
     * @return initialized according with given data object
     * @throws JSONException if source object structure is invalid
     */
    @SuppressWarnings("rawtypes")
    public static <T> T parseViaReflection(T object, JSONObject source) throws JSONException {
        if (source == null) {
            return object;
        }
        for (Field field : object.getClass().getFields()) {
            field.setAccessible(true);
            String fieldName = field.getName();
            Class<?> fieldType = field.getType();

            Object value = source.opt(fieldName);
            if (value == null) {
                continue;
            }
            try {
                if (fieldType.isPrimitive()) {
                    if(value instanceof Number) {
                        Number number = (Number) value;
                        if (fieldType.equals(int.class)) {
                            field.setInt(object, number.intValue());
                        } else if (fieldType.equals(long.class)) {
                            field.setLong(object, number.longValue());
                        } else if (fieldType.equals(float.class)) {
                            field.setFloat(object, number.floatValue());
                        } else if (fieldType.equals(double.class)) {
                            field.setDouble(object, number.doubleValue());
                        } else if (fieldType.equals(short.class)) {
                            field.setShort(object, number.shortValue());
                        } else if (fieldType.equals(byte.class)) {
                            field.setByte(object, number.byteValue());
                        }
                    } else if ( fieldType.equals(boolean.class) && value instanceof Boolean) {
                        field.setBoolean(object, (Boolean) value);
                    }
                } else {
                    Object result = field.get(object);
                    if (value.getClass().equals(fieldType)) {
                        result = value;
                    } else if (fieldType.isArray() && value instanceof JSONArray) {
                        result = parseArrayViaReflection((JSONArray) value, fieldType);
                    } else if (RTList.class.equals(fieldType)) {
                        ParameterizedType genericTypes = (ParameterizedType) field.getGenericType();
                        Class<?> genericType = (Class<?>) genericTypes.getActualTypeArguments()[0];
                        if (Model.class.isAssignableFrom(genericType) && value instanceof JSONArray) {
                            result = new RTList((JSONArray) value, genericType);
                        }
                    } else if (Model.class.isAssignableFrom(fieldType) && value instanceof JSONObject) {
                        result = ((Model) fieldType.newInstance()).parse((JSONObject) value);
                    }
                    field.set(object, result);
                }
            } catch (InstantiationException e) {
                throw new JSONException(e.getMessage());
            } catch (IllegalAccessException e) {
                throw new JSONException(e.getMessage());
            } catch (NoSuchMethodError e) {
                // Вы не поверите, но у некоторых вендоров getFields() вызывает ВОТ ЭТО.
                // Иногда я всерьез задумываюсь, правильно ли я поступил, выбрав Android в качестве платформы разработки.
                throw new JSONException(e.getMessage());
            }
        }
        return object;
    }

    /**
     * Parses array from given JSONArray.
     * Supports parsing of primitive types and {@link Model} instances.
     *
     * @param array      JSONArray to parse
     * @param arrayClass type of array field in class.
     * @return object to set to array field in class
     * @throws JSONException if given array have incompatible type with given field.
     */
    public static Object parseArrayViaReflection(JSONArray array, Class arrayClass) throws JSONException {
        Object result = Array.newInstance(arrayClass.getComponentType(), array.length());
        Class<?> subType = arrayClass.getComponentType();
        for (int i = 0; i < array.length(); i++) {
            try {
                Object item = array.opt(i);
                if (Model.class.isAssignableFrom(subType) && item instanceof JSONObject) {
                    Model model = (Model) subType.newInstance();
                    item = model.parse((JSONObject) item);
                }
                Array.set(result, i, item);
            } catch (InstantiationException e) {
                throw new JSONException(e.getMessage());
            } catch (IllegalAccessException e) {
                throw new JSONException(e.getMessage());
            } catch (IllegalArgumentException e) {
                throw new JSONException(e.getMessage());
            }
        }
        return result;
    }
}
