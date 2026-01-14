package com.cboda.intelligent.utils;

import java.lang.reflect.*;
import java.util.*;

public class TypeUtils {

    public static Type canonicalize(Type type) {
        if (type instanceof Class) {
            Class<?> cls = (Class<?>) type;
            return cls.isArray() ? new GenericArrayTypeImpl(canonicalize(cls.getComponentType())) : cls;
        } else if (type instanceof ParameterizedType) {
            ParameterizedType p = (ParameterizedType) type;
            return new ParameterizedTypeImpl(
                    p.getOwnerType(),
                    p.getRawType(),
                    p.getActualTypeArguments()
            );
        } else if (type instanceof GenericArrayType) {
            GenericArrayType g = (GenericArrayType) type;
            return new GenericArrayTypeImpl(g.getGenericComponentType());
        } else if (type instanceof WildcardType) {
            WildcardType w = (WildcardType) type;
            return new WildcardTypeImpl(w.getUpperBounds(), w.getLowerBounds());
        } else {
            return type; // 其他类型（如 TypeVariable）无法 canonicalize
        }
    }

    // 以下是辅助静态内部类（简化版，实际需完整实现 equals/hashCode）
    private static final class ParameterizedTypeImpl implements ParameterizedType {
        private final Type ownerType;
        private final Type rawType;
        private final Type[] typeArgs;

        ParameterizedTypeImpl(Type ownerType, Type rawType, Type[] typeArgs) {
            this.ownerType = ownerType;
            this.rawType = rawType;
            this.typeArgs = typeArgs != null ? Arrays.stream(typeArgs).map(TypeUtils::canonicalize).toArray(Type[]::new) : new Type[0];
        }

        @Override public Type[] getActualTypeArguments() { return typeArgs; }
        @Override public Type getRawType() { return rawType; }
        @Override public Type getOwnerType() { return ownerType; }

        @Override public boolean equals(Object o) {
            if (!(o instanceof ParameterizedType)) return false;
            ParameterizedType that = (ParameterizedType) o;
            return Objects.equals(ownerType, that.getOwnerType())
                    && Objects.equals(rawType, that.getRawType())
                    && Arrays.equals(typeArgs, that.getActualTypeArguments());
        }

        @Override public int hashCode() {
            return Objects.hash(ownerType, rawType, Arrays.hashCode(typeArgs));
        }
    }

    private static final class GenericArrayTypeImpl implements GenericArrayType {
        private final Type componentType;
        GenericArrayTypeImpl(Type componentType) {
            this.componentType = canonicalize(componentType);
        }
        @Override public Type getGenericComponentType() { return componentType; }
        @Override public boolean equals(Object o) {
            return o instanceof GenericArrayType && Objects.equals(componentType, ((GenericArrayType) o).getGenericComponentType());
        }
        @Override public int hashCode() { return Objects.hash(componentType); }
    }

    private static final class WildcardTypeImpl implements WildcardType {
        private final Type[] upperBounds;
        private final Type[] lowerBounds;
        WildcardTypeImpl(Type[] upperBounds, Type[] lowerBounds) {
            this.upperBounds = upperBounds != null ? Arrays.stream(upperBounds).map(TypeUtils::canonicalize).toArray(Type[]::new) : new Type[0];
            this.lowerBounds = lowerBounds != null ? Arrays.stream(lowerBounds).map(TypeUtils::canonicalize).toArray(Type[]::new) : new Type[0];
        }
        @Override public Type[] getUpperBounds() { return upperBounds; }
        @Override public Type[] getLowerBounds() { return lowerBounds; }
        @Override public boolean equals(Object o) {
            if (!(o instanceof WildcardType)) return false;
            WildcardType that = (WildcardType) o;
            return Arrays.equals(upperBounds, that.getUpperBounds()) && Arrays.equals(lowerBounds, that.getLowerBounds());
        }
        @Override public int hashCode() {
            return Arrays.hashCode(upperBounds) ^ Arrays.hashCode(lowerBounds);
        }
    }
}