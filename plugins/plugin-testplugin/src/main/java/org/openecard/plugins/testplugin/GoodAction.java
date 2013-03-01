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

import iso.std.iso_iec._24727.tech.schema.EstablishContext;
import iso.std.iso_iec._24727.tech.schema.EstablishContextResponse;
import iso.std.iso_iec._24727.tech.schema.ListIFDs;
import iso.std.iso_iec._24727.tech.schema.ListIFDsResponse;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.interfaces.DispatcherException;
import org.openecard.gui.UserConsentNavigator;
import org.openecard.gui.definition.Step;
import org.openecard.gui.definition.UserConsentDescription;
import org.openecard.gui.executor.ExecutionEngine;
import org.openecard.plugins.PluginAction;
import org.openecard.plugins.wrapper.PluginDispatcher;
import org.openecard.plugins.wrapper.PluginUserConsent;


/**
 * This is a good-natured action that lists the connected ifds and shortly shows a dialog.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class GoodAction implements PluginAction {

    private PluginDispatcher dispatcher;
    private PluginUserConsent gui;

    /**
     * Creates a new TestAction.
     * 
     * @param dispatcher PluginDispatcher that will be used in perform-method
     * @param gui UserConset that will be used in perform-method
     */
    public GoodAction(PluginDispatcher dispatcher, PluginUserConsent gui) {
	this.dispatcher = dispatcher;
	this.gui = gui;
    }

    @Override
    public String getID() {
	return "";
    }

    @Override
    public String getName() {
	return "TestAction";
    }

    @Override
    public String getDescription() {
	return "A simple TestAction";
    }

    @Override
    public InputStream getLogo() {
	return null; 
    }

    @Override
    public void perform() throws DispatcherException, InvocationTargetException {
	Dispatcher ifd = dispatcher;

	EstablishContext establishContext = new EstablishContext();
	EstablishContextResponse establishContextResponse = (EstablishContextResponse) ifd.deliver(establishContext);

	byte[] contextHandle = establishContextResponse.getContextHandle();

	ListIFDs listIFDs = new ListIFDs();
	listIFDs.setContextHandle(contextHandle);

	ListIFDsResponse resp = (ListIFDsResponse) ifd.deliver(listIFDs);
	for (String s : resp.getIFDName()) {
	    System.out.println(s);
	}

	UserConsentDescription uc = new UserConsentDescription("test");

	// create step
	Step s = new Step("ID", "teststep");
	s.setInstantReturn(true);

	// add step
	uc.getSteps().add(s);

	UserConsentNavigator nav = gui.obtainNavigator(uc);
	ExecutionEngine exec = new ExecutionEngine(nav);
	// run gui
	exec.process();

    }

    @Override
    public boolean isMainActivity() {
	return false;
    }

}
