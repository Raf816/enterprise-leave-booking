package com.staffs.leavebooking.common.domain;

import com.staffs.leavebooking.common.events.Event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AggregateRoot<T> extends Entity<T> {

    private final List<Event> domainEvents = new ArrayList<>();

    protected AggregateRoot(Identity<T> id) {
        super(id);
    }

    protected void addDomainEvent(Event event) {
        domainEvents.add(event);
    }

    protected void removeDomainEvent(Event event) {
        domainEvents.remove(event);
    }

    public List<Event> listOfDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public void clearDomainEvents() {
        domainEvents.clear();
    }

    public boolean domainEventsExist() {
        return !domainEvents.isEmpty();
    }
}
