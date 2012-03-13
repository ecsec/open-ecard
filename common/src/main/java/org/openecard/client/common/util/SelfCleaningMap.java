/*
 * Copyright 2012 Tobias Wich ecsec GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openecard.client.common.util;

import java.util.*;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class SelfCleaningMap<K extends Comparable,V> implements Map<K,V> {

    private static final long rerun = 30 * 1000;
    private final long kill;
    private final RemoveActionFactory<V> actionFactory;

    private Map<K,Entry> _map;


    public <M extends Map> SelfCleaningMap(Class<M> c) throws InstantiationException, IllegalAccessException {
        this(c, 15 * 60);
    };

    public <M extends Map> SelfCleaningMap(Class<M> c, RemoveActionFactory<V> actionFactory) throws InstantiationException, IllegalAccessException {
        this(c, actionFactory, 15 * 60);
    };

    /**
     * 
     * @param c
     * @param lifetime in minutes
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public <M extends Map> SelfCleaningMap(Class<M> c, int lifetime) throws InstantiationException, IllegalAccessException {
        this(c, null, lifetime);
    };

    public <M extends Map> SelfCleaningMap(Class<M> c, RemoveActionFactory<V> actionFactory, int lifetime) throws InstantiationException, IllegalAccessException {
        this.kill = lifetime * 1000;
        this.actionFactory = actionFactory;
	this._map = c.newInstance();
    };

    private boolean hasAction() {
        return this.actionFactory != null;
    }
    private RemoveAction<V> getAction(V v) {
        return actionFactory.create(v);
    }

    private class Entry {

	public final V v;
	public final Date d;

	public Entry(V v) {
	    this.v = v;
	    this.d = new Date();
	}

	public Entry(V v, Date d) {
	    this.v = v;
	    this.d = d;
	}

    };

    private Cleaner cleaner = null;

    private class Cleaner extends Thread {

	@Override
	public void run() {
	    synchronized (this) {
		while (!_map.isEmpty()) { // only run as long as there are entries in the map
		    try {
			wait(rerun); // block 30 seconds
			long now = System.currentTimeMillis();
			Iterator<Map.Entry<K, Entry>> i = _map.entrySet().iterator();
			while (i.hasNext()) {
			    Map.Entry<K, Entry> e = i.next();
			    long old = e.getValue().d.getTime();
			    if ((now - old) > kill) {
				// remove entry
                                if (hasAction()) {
                                    getAction(e.getValue().v).perform();
                                }
				i.remove();
				// notify all listening objects
				V v = e.getValue().v;
				synchronized (v) {
				    v.notifyAll();
				}
			    }
			}
		    } catch (InterruptedException ex) {
			// nobody cares
		    }
		}
		// done delete this thread from outer class
		cleaner = null;
	    }
	}
    };



    @Override
    public synchronized int size() {
	return _map.size();
    }

    @Override
    public synchronized boolean isEmpty() {
	return _map.isEmpty();
    }

    @Override
    public synchronized void putAll(Map m) {
	Iterator<Map.Entry> i = m.entrySet().iterator();
	while (i.hasNext()) {
	    Map.Entry next = i.next();
	    SelfCleaningMap.Entry e = new SelfCleaningMap.Entry(next.getValue());
	    _map.put((K) next.getKey(), e);
	}
    }

    @Override
    public synchronized void clear() {
	Iterator<Map.Entry<K, Entry>> i = _map.entrySet().iterator();
	while (i.hasNext()) {
	    Map.Entry<K, Entry> e = i.next();
	    // remove entry
            if (hasAction()) {
                getAction(e.getValue().v).perform();
            }
	    i.remove();
	    // notify all listening objects
	    V v = e.getValue().v;
	    synchronized (v) {
		v.notifyAll();
	    }
	}
    }

    @Override
    public Set keySet() {
	throw new UnsupportedOperationException("It is not safe to request lists of objects which may get deleted in another thread.");
    }

    @Override
    public Collection values() {
	throw new UnsupportedOperationException("It is not safe to request lists of objects which may get deleted in another thread.");
    }

    @Override
    public Set entrySet() {
	throw new UnsupportedOperationException("It is not safe to request lists of objects which may get deleted in another thread.");
    }

    @Override
    public synchronized boolean containsKey(Object key) {
	boolean result =  _map.containsKey((K) key);
	if (result == true) {
	    // update access time
	    get((K) key);
	}
	return result;
    }

    @Override
    public synchronized boolean containsValue(Object value) {
	Iterator<Map.Entry<K,Entry>> i = _map.entrySet().iterator();
	while (i.hasNext()) {
	    Map.Entry<K,Entry> next = i.next();
	    V v = next.getValue().v;
	    if ((value == null && v == null) ||
		(value != null && value.equals(v))) {
		// update access time
		next.getValue().d.setTime(System.currentTimeMillis());
		return true;
	    }
	}
	return false; // none found
    }

    @Override
    public synchronized V get(Object key) {
	Entry e = _map.get((K) key);
	if (e != null) {
	    // update access time
	    e.d.setTime(System.currentTimeMillis());
	    return e.v;
	}
	return null;
    }

    @Override
    public synchronized V put(K key, V value) {
	// start killer thread if non is running
	if (cleaner == null) {
	    cleaner = new Cleaner();
	    cleaner.start();
	}

	Entry result = _map.put(key, new Entry(value));
	return (result == null) ? null : result.v;
    }

    @Override
    public synchronized V remove(Object key) {
	Entry e = _map.remove((K) key);
        if (hasAction()) {
            getAction(e.v).perform();
        }
	if (e != null) {
	    synchronized (e.v) {
		e.v.notifyAll();
	    }
	    return e.v;
	}
	return null;
    }

}
