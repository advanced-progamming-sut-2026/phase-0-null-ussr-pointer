package com.ussr.pvz.model.engine.event;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class GameEventBus {

    private record Entry<T extends GameEvent>(Class<T> type, Consumer<T> handler) {

        @SuppressWarnings("unchecked")
            void tryHandle(GameEvent event) {
                if (type.isInstance(event)) {
                    handler.accept((T) event);
                }
            }
        }

    public interface Subscription {
        void unsubscribe();
    }

    private final List<Entry<?>> entries = new CopyOnWriteArrayList<>();

    public <T extends GameEvent> Subscription subscribe(Class<T> eventType, Consumer<T> handler) {
        Entry<T> entry = new Entry<>(
                Objects.requireNonNull(eventType, "eventType"),
                Objects.requireNonNull(handler, "handler")
        );
        entries.add(entry);
        return () -> entries.remove(entry);
    }

    public void publish(GameEvent event) {
        for (Entry<?> entry : entries) {
            entry.tryHandle(event);
        }
    }

    public void clear() {
        entries.clear();
    }
}
