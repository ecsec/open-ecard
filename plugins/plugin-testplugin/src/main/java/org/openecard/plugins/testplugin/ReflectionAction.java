/****************************************************************************
 * Copyright (C) 2012 HS Coburg.
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

package org.openecard.plugins.testplugin;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import org.openecard.common.interfaces.DispatcherException;
import org.openecard.plugins.PluginAction;
import org.openecard.plugins.wrapper.PluginDispatcher;


/**
 * An action for testing forbidden use of reflections.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class ReflectionAction implements PluginAction {

    private PluginDispatcher dispatcher;

    /**
     * Creates a new EvilAction.
     *
     * @param dispatcher PluginDispatcher that will be used in perform-method
     */
    public ReflectionAction(PluginDispatcher dispatcher) {
	this.dispatcher = dispatcher;
    }

    @Override
    public String getID() {
	return "";
    }

    @Override
    public String getName() {
	return "Evil Test Action";
    }

    @Override
    public String getDescription() {
	return "This Action does evil stuff.";
    }

    @Override
    public InputStream getLogo() {
	return null;
    }

    @Override
    public void perform() throws DispatcherException, InvocationTargetException {
	Class<?> c = dispatcher.getClass();
	Field[] mbrs = c.getDeclaredFields();
	System.out.format("%s:%n", "Fields");
	for (Member mbr : mbrs) {
	    if (mbr instanceof Field) {
		System.out.format("  %s%n", ((Field) mbr).toGenericString());
	    } else if (mbr instanceof Constructor) {
		System.out.format("  %s%n", ((Constructor<?>) mbr).toGenericString());
	    } else if (mbr instanceof Method) {
		System.out.format("  %s%n", ((Method) mbr).toGenericString());
	    }
	}
	if (mbrs.length == 0) {
	    System.out.format("  -- No %s --%n", "Fields");
	}
	System.out.format("%n");
    }

    @Override
    public boolean isMainActivity() {
	return false;
    }

}
