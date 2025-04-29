/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file is part of the Open eCard App.
 *
 * GNU General Public License Usage
 * This file may be used under the terms of the GNU General Public
 * License version 3.0 as published by the Free Software Foundation
 * and appearing in the file LICENSE.GPL included in the packaging of
 * this file. Please review the following information to ensure the
 * GNU General Public License version 3.0 requirements will be met:
 * http://www.gnu.org/copyleft/gpl.html.
 *
 * Other Usage
 * Alternatively, this file may be used in accordance with the terms
 * and conditions contained in a signed written agreement between
 * you and ecsec GmbH.
 *
 */
package org.openecard.common.util

import java.util.*
import java.util.concurrent.atomic.AtomicInteger

/**
 *
 * @author Tobias Wich
 * @param <K>
 * @param <V>
</V></K> */
class SelfCleaningMap<K : Comparable<*>?, V>(
    c: Class<M>,
    private val actionFactory: RemoveActionFactory<V>?, lifetime: Int
) :
    MutableMap<K, V?> {
    private val kill = (lifetime * 1000).toLong()

    private val _map: MutableMap<K, Entry> =
        c.getDeclaredConstructor().newInstance()


    constructor(c: Class<M>) : this(c, 15 * 60)

    constructor(c: Class<M>, actionFactory: RemoveActionFactory<V>?) : this(c, actionFactory, 15 * 60)

    /**
     *
     * @param <M>
     * @param c
     * @param lifetime in minutes
     * @throws InstantiationException
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
    </M> */
    constructor(c: Class<M>, lifetime: Int) : this(c, null, lifetime)

    private fun hasAction(): Boolean {
        return this.actionFactory != null
    }

    private fun getAction(v: V): RemoveAction<V?>? {
        return actionFactory!!.create(v)
    }

    private inner class Entry {
        val v: V
        val d: Date

        constructor(v: V) {
            this.v = v
            this.d = Date()
        }

        constructor(v: V, d: Date) {
            this.v = v
            this.d = d
        }
    }

    private var cleaner: Cleaner? = null

    private inner class Cleaner : Thread(String.format("MapCleaner-%d", THREAD_NUM.getAndIncrement())) {
        override fun run() {
            synchronized(this) {
                while (!_map.isEmpty()) { // only run as long as there are entries in the map
                    try {
                        (this as Object).wait(RERUN) // block 30 seconds

                        val now = System.currentTimeMillis()
                        val i: MutableIterator<Map.Entry<K, Entry>> = _map.entries.iterator()
                        while (i.hasNext()) {
                            val e: Map.Entry<K, Entry> = i.next()
                            val old: Long = e.value.d.getTime()
                            if ((now - old) > kill) {
                                // remove entry
                                if (hasAction()) {
                                    getAction(e.value.v)!!.perform()
                                }
                                i.remove()
                                // notify all listening objects
                                val v: V = e.value.v
                                synchronized(v) {
                                    (v as Object).notifyAll()
                                }
                            }
                        }
                    } catch (ex: InterruptedException) {
                        // nobody cares
                    }
                }
                // done delete this thread from outer class
                cleaner = null
            }
        }
    }


    @Synchronized
    override fun size(): Int {
        return _map.size
    }

    @Synchronized
    override fun isEmpty(): Boolean {
        return _map.isEmpty()
    }

    @Synchronized
    override fun putAll(m: Map<*, *>) {
        val i = m.entries.iterator()
        while (i.hasNext()) {
            val next = i.next()
            val e: Entry = Entry(next.value)
            _map[next.key as K] = e
        }
    }

    @Synchronized
    override fun clear() {
        val i: MutableIterator<Map.Entry<K, Entry>> = _map.entries.iterator()
        while (i.hasNext()) {
            val e: Map.Entry<K, Entry> = i.next()
            // remove entry
            if (hasAction()) {
                getAction(e.value.v)!!.perform()
            }
            i.remove()
            // notify all listening objects
            val v: V = e.value.v
            synchronized(v) {
                (v as Object).notifyAll()
            }
        }
    }

    override fun keySet(): Set<*> {
        throw UnsupportedOperationException("It is not safe to request lists of objects which may get deleted in another thread.")
    }

    override fun values(): Collection<*> {
        throw UnsupportedOperationException("It is not safe to request lists of objects which may get deleted in another thread.")
    }

    override fun entrySet(): Set<*> {
        throw UnsupportedOperationException("It is not safe to request lists of objects which may get deleted in another thread.")
    }

    @Synchronized
    override fun containsKey(key: Any): Boolean {
        val result = _map.containsKey(key as K)
        if (result == true) {
            // update access time
            get(key as K)
        }
        return result
    }

    @Synchronized
    override fun containsValue(value: Any?): Boolean {
        val i: Iterator<Map.Entry<K, Entry>> = _map.entries.iterator()
        while (i.hasNext()) {
            val next: Map.Entry<K, Entry> = i.next()
            val v: V = next.value.v
            if ((value == null && v == null) ||
                (value != null && value == v)
            ) {
                // update access time
                next.value.d.setTime(System.currentTimeMillis())
                return true
            }
        }
        return false // none found
    }

    @Synchronized
    override fun get(key: Any): V? {
        val e: Entry? = _map[key as K]
        if (e != null) {
            // update access time
            e.d.setTime(System.currentTimeMillis())
            return e.v
        }
        return null
    }

    @Synchronized
    override fun put(key: K, value: V?): V? {
        // start killer thread if non is running
        if (cleaner == null) {
            cleaner = Cleaner()
            cleaner.start()
        }

        val result: Entry? = _map.put(key, Entry(value))
        return if (result == null) null else result.v
    }

    @Synchronized
    override fun remove(key: Any): V? {
        val e: Entry? = _map.remove(key as K)
        if (hasAction()) {
            getAction(e.v)!!.perform()
        }
        if (e != null) {
            synchronized(e.v) {
                (e.v as Object).notifyAll()
            }
            return e.v
        }
        return null
    }

    companion object {
        private const val RERUN = (30 * 1000).toLong()
        private val THREAD_NUM = AtomicInteger(1)
    }
}
