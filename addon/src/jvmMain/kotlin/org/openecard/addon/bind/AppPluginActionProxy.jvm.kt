/****************************************************************************
 * Copyright (C) 2013-2024 ecsec GmbH.
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
 ***************************************************************************/

package org.openecard.addon.bind

import org.openecard.addon.AbstractFactory
import org.openecard.addon.ActionInitializationException
import org.openecard.addon.Context

/**
 * Proxy class wrapping a AppPluginAction.
 * The proxy loads the action and calls the actual execute function of the plug-in implementation. <br></br>
 * If the plug-in has a custom function and it is found by the proxy, then this one is called directly (not implemented).
 *
 * @author Tobias Wich
 * @author Dirk Petrautzki
 */
class AppPluginActionProxy(implClass: String, classLoader: ClassLoader) :
    AbstractFactory<AppPluginAction>(implClass, classLoader), AppPluginAction {
    private var c: AppPluginAction? = null

//    val actionDescription: Unit
//        get() {
//            throw UnsupportedOperationException()
//        }

    override fun execute(
        body: RequestBody?,
        parameters: Map<String, String>?,
        headers: Headers?,
        attachments: List<Attachment>?,
        extraParams: Map<String, Any>?
    ): BindingResult {
        //TODO use annotations to find the right function
        return c?.execute(body, parameters, headers, attachments, extraParams)
			?: throw IllegalStateException("AppPluginAction not initialized")
    }

    @Throws(ActionInitializationException::class)
    override fun init(aCtx: Context) {
        c = loadInstance(aCtx, AppPluginAction::class.java)
    }

    override fun destroy(force: Boolean) {
        c?.destroy(force)
    }
}
