package com.ussr.pvz.model.engine.event;

import java.util.ArrayList;
import java.util.List;
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

    private final List<Entry<?>> entries = new ArrayList<>();

    public <T extends GameEvent> void subscribe(Class<T> eventType, Consumer<T> handler) {
        entries.add(new Entry<>(eventType, handler));
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