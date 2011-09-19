package de.ecsec.ecard.client.event;

import de.ecsec.core.common.enums.EventType;
import de.ecsec.core.common.interfaces.EventCallback;
import de.ecsec.core.common.interfaces.EventFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class Dispatcher {

    private static class EventTypeFilter implements EventFilter {

        private ArrayList<EventType> eventType;

        public EventTypeFilter(EventType ... eventType) {
            if (eventType.length == 0) {
                this.eventType = new ArrayList<EventType>(Arrays.asList(EventType.values()));
            } else {
                this.eventType = new ArrayList<EventType>(Arrays.asList(eventType));
            }
        }


        @Override
        public boolean matches(EventType t, Object o) {
            for (EventType next : eventType) {
                if (t.equals(next)) {
                    return true;
                }
            }
            return false;
        }
        
    }

    private static class EventRunner implements Runnable {
        EventCallback cb; EventType t; Object o;

        public EventRunner(EventCallback cb, EventType t, Object o) {
            this.cb = cb; this.t = t; this.o = o;
        }

        @Override
        public void run() {
            cb.signalEvent(t, o);
        }

    }


    // needed because threadPool must be accessible here
    private final EventManager manager;

    private final Semaphore guard;
    private final ConcurrentHashMap<EventCallback,ArrayList<EventFilter>> eventFilter;

    public Dispatcher(EventManager manager) {
        this.manager = manager;
        this.guard = new Semaphore(1);
        this.eventFilter = new ConcurrentHashMap<EventCallback, ArrayList<EventFilter>>();
    }


    public synchronized EventCallback add(EventCallback cb) {
        add(cb, new EventTypeFilter());
        return cb;
    }
    public synchronized EventCallback add(EventCallback cb, EventType ... eventTypes) {
        add(cb, new EventTypeFilter(eventTypes));
        return cb;
    }
    public synchronized EventCallback add(EventCallback cb, EventFilter filter) {
        try {
            guard.acquire();
            if (! eventFilter.containsKey(cb)) {
                eventFilter.put(cb, new ArrayList<EventFilter>());
            }
            eventFilter.get(cb).add(filter);
            guard.release();
        } catch (InterruptedException ex) {
            // ignore this bullshit
        }
        return cb;
    }

    public synchronized EventCallback del(EventCallback cb) {
        try {
            guard.acquire();
            if (eventFilter.containsKey(cb)) {
                eventFilter.remove(cb);
            }
            guard.release();
        } catch (InterruptedException ex) {
            // ignore this bullshit
        }
        return cb;
    }

    public void notify(EventType t, Object o) {
        try {
            guard.acquire();
            // check every callback for a matching filter
            for (Map.Entry<EventCallback,ArrayList<EventFilter>> entry : eventFilter.entrySet()) {
                EventCallback cb = entry.getKey();
                for (EventFilter filter : entry.getValue()) {
                    if (filter.matches(t, o)) {
                        fork(cb, t, o);
                        break;
                    }
                }
            }
            guard.release();
        } catch (InterruptedException ex) {
            // ignore this bullshit
        }
    }

    private Future<?> fork(EventCallback cb, EventType t, Object o) {
        Future<?> f = manager.threadPool.submit(new EventRunner(cb, t, o));
        return f;
    }

}
