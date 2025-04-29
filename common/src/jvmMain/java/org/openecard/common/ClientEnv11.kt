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
package org.openecard.common

import org.openecard.common.interfaces.*
import org.openecard.gui.UserConsent
import org.openecard.ws.IFD
import org.openecard.ws.Management
import org.openecard.ws.SAL
import java.util.concurrent.ConcurrentSkipListMap

/**
 *
 * @author Johannes.Schmoelz
 */
class ClientEnv : Environment {
    override var gui: UserConsent? = null

    @get:Dispatchable(interfaceClass = IFD::class)
    override var ifd: IFD? = null
    override val ifdCtx: LinkedHashSet<ByteArray> = LinkedHashSet()

    @get:Dispatchable(interfaceClass = SAL::class)
    override var sal: SAL? = null
    override var eventDispatcher: EventDispatcher? = null
    override var dispatcher: Dispatcher? = null

    @get:Dispatchable(interfaceClass = Management::class)
    override var management: Management? = null
    override var recognition: CardRecognition? = null
    override var cifProvider: CIFProvider? = null
    override var salSelector: SalSelector? = null
    private val genericComponents: MutableMap<String, Any> = ConcurrentSkipListMap()


    @Synchronized
    override fun addIfdCtx(ctx: ByteArray?) {
        if (ctx != null && ctx.size > 0) {
            ifdCtx.add(ctx.copyOf(ctx.size))
        }
    }

    @Synchronized
    override fun removeIfdCtx(ctx: ByteArray) {
        val it = ifdCtx.iterator()
        while (it.hasNext()) {
            val next = it.next()
            if (next.contentEquals(ctx)) {
                it.remove()
                return
            }
        }
    }

    @Synchronized
    override fun getIfdCtx(): List<ByteArray> {
        val result = ArrayList<ByteArray>(ifdCtx.size)
        for (next in ifdCtx) {
            result.add(next.copyOf(next.size))
        }
        return result
    }

    override fun setGenericComponent(id: String, component: Any) {
        genericComponents[id] = component
    }

    override fun getGenericComponent(id: String): Any? {
        return genericComponents[id]
    }
}
