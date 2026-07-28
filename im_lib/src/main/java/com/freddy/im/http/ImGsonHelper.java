package com.freddy.im.http;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * IM HTTP 反序列化：兼容接口把数字字段返回为 {@code ""} / {@code null} 的情况。
 */
public final class ImGsonHelper {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Integer.class, new LenientIntegerAdapter(false))
            .registerTypeAdapter(int.class, new LenientIntegerAdapter(true))
            .registerTypeAdapter(Long.class, new LenientLongAdapter(false))
            .registerTypeAdapter(long.class, new LenientLongAdapter(true))
            .registerTypeAdapter(Double.class, new LenientDoubleAdapter(false))
            .registerTypeAdapter(double.class, new LenientDoubleAdapter(true))
            .registerTypeAdapter(Boolean.class, new LenientBooleanAdapter(false))
            .registerTypeAdapter(boolean.class, new LenientBooleanAdapter(true))
            .create();

    private ImGsonHelper() {
    }

    public static Gson getGson() {
        return GSON;
    }

    private static final class LenientIntegerAdapter extends TypeAdapter<Integer> {
        private final boolean primitive;

        LenientIntegerAdapter(boolean primitive) {
            this.primitive = primitive;
        }

        @Override
        public void write(JsonWriter out, Integer value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(value);
            }
        }

        @Override
        public Integer read(JsonReader in) throws IOException {
            Integer parsed = readNullableInt(in);
            if (parsed == null && primitive) {
                return 0;
            }
            return parsed;
        }
    }

    private static final class LenientLongAdapter extends TypeAdapter<Long> {
        private final boolean primitive;

        LenientLongAdapter(boolean primitive) {
            this.primitive = primitive;
        }

        @Override
        public void write(JsonWriter out, Long value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(value);
            }
        }

        @Override
        public Long read(JsonReader in) throws IOException {
            Long parsed = readNullableLong(in);
            if (parsed == null && primitive) {
                return 0L;
            }
            return parsed;
        }
    }

    private static final class LenientDoubleAdapter extends TypeAdapter<Double> {
        private final boolean primitive;

        LenientDoubleAdapter(boolean primitive) {
            this.primitive = primitive;
        }

        @Override
        public void write(JsonWriter out, Double value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(value);
            }
        }

        @Override
        public Double read(JsonReader in) throws IOException {
            Double parsed = readNullableDouble(in);
            if (parsed == null && primitive) {
                return 0D;
            }
            return parsed;
        }
    }

    private static final class LenientBooleanAdapter extends TypeAdapter<Boolean> {
        private final boolean primitive;

        LenientBooleanAdapter(boolean primitive) {
            this.primitive = primitive;
        }

        @Override
        public void write(JsonWriter out, Boolean value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(value);
            }
        }

        @Override
        public Boolean read(JsonReader in) throws IOException {
            Boolean parsed = readNullableBoolean(in);
            if (parsed == null && primitive) {
                return false;
            }
            return parsed;
        }
    }

    @Nullable
    private static Boolean readNullableBoolean(JsonReader in) throws IOException {
        JsonToken token = in.peek();
        if (token == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        if (token == JsonToken.BOOLEAN) {
            return in.nextBoolean();
        }
        if (token == JsonToken.NUMBER) {
            return in.nextDouble() != 0D;
        }
        if (token == JsonToken.STRING) {
            String value = in.nextString();
            if (TextUtils.isEmpty(value)) {
                return null;
            }
            String trimmed = value.trim();
            if ("1".equals(trimmed) || "true".equalsIgnoreCase(trimmed)) {
                return true;
            }
            if ("0".equals(trimmed) || "false".equalsIgnoreCase(trimmed)) {
                return false;
            }
            return null;
        }
        in.skipValue();
        return null;
    }

    @Nullable
    private static Integer readNullableInt(JsonReader in) throws IOException {
        JsonToken token = in.peek();
        if (token == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        if (token == JsonToken.STRING) {
            String value = in.nextString();
            if (TextUtils.isEmpty(value)) {
                return null;
            }
            try {
                return Integer.parseInt(value.trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        if (token == JsonToken.NUMBER) {
            return (int) in.nextDouble();
        }
        if (token == JsonToken.BOOLEAN) {
            return in.nextBoolean() ? 1 : 0;
        }
        in.skipValue();
        return null;
    }

    @Nullable
    private static Long readNullableLong(JsonReader in) throws IOException {
        JsonToken token = in.peek();
        if (token == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        if (token == JsonToken.STRING) {
            String value = in.nextString();
            if (TextUtils.isEmpty(value)) {
                return null;
            }
            try {
                return Long.parseLong(value.trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        if (token == JsonToken.NUMBER) {
            return (long) in.nextDouble();
        }
        if (token == JsonToken.BOOLEAN) {
            return in.nextBoolean() ? 1L : 0L;
        }
        in.skipValue();
        return null;
    }

    @Nullable
    private static Double readNullableDouble(JsonReader in) throws IOException {
        JsonToken token = in.peek();
        if (token == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        if (token == JsonToken.STRING) {
            String value = in.nextString();
            if (TextUtils.isEmpty(value)) {
                return null;
            }
            try {
                return Double.parseDouble(value.trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        if (token == JsonToken.NUMBER) {
            return in.nextDouble();
        }
        if (token == JsonToken.BOOLEAN) {
            return in.nextBoolean() ? 1D : 0D;
        }
        in.skipValue();
        return null;
    }
}
