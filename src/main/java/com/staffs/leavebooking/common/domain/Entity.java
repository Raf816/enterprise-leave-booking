package com.staffs.leavebooking.common.domain;

import java.util.Objects;

public abstract class Entity<T> {

    public static final String IDENTITY_CANNOT_BE_NULL = "Identity cannot be null";

    protected final Identity<T> id;

    protected Entity(Identity<T> id) {
        if (id == null) {
            throw new IllegalArgumentException(IDENTITY_CANNOT_BE_NULL);
        }
        this.id = id;
    }

    public Identity<T> id() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity<?> entity = (Entity<?>) o;
        return Objects.equals(id, entity.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
