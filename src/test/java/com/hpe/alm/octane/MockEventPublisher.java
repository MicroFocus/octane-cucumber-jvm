package com.hpe.alm.octane;

import cucumber.api.event.Event;
import cucumber.api.event.EventHandler;
import cucumber.api.event.EventPublisher;

import java.util.*;

class MockEventPublisher implements EventPublisher {
    private Map<Class<? extends Event>, List<EventHandler>> handlers = new HashMap();

    public final <T extends Event> void registerHandlerFor(Class<T> eventType, EventHandler<T> handler) {
        if (handlers.containsKey(eventType)) {
            handlers.get(eventType).add(handler);
        } else {
            List<EventHandler> list = new ArrayList();
            list.add(handler);
            handlers.put(eventType, list);
        }

    }

    public final <T extends Event> void removeHandlerFor(Class<T> eventType, EventHandler<T> handler) {
        if (handlers.containsKey(eventType)) {
            handlers.get(eventType).remove(handler);
        }
    }

    public void send(Event event) {
        Iterator handlersIterator;
        EventHandler handler;
        if (handlers.containsKey(Event.class)) {
            handlersIterator = handlers.get(Event.class).iterator();

            while(handlersIterator.hasNext()) {
                handler = (EventHandler)handlersIterator.next();
                handler.receive(event);
            }
        }

        if (handlers.containsKey(event.getClass())) {
            handlersIterator = handlers.get(event.getClass()).iterator();

            while(handlersIterator.hasNext()) {
                handler = (EventHandler)handlersIterator.next();
                handler.receive(event);
            }
        }

    }
}
