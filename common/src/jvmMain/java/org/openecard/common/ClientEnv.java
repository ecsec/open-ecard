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
 ***************************************************************************/

package org.openecard.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import org.openecard.common.interfaces.CardRecognition;
import org.openecard.common.interfaces.CIFProvider;
import org.openecard.common.interfaces.Dispatchable;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.interfaces.Environment;
import org.openecard.common.interfaces.EventDispatcher;
import org.openecard.common.interfaces.SalSelector;
import org.openecard.gui.UserConsent;
import org.openecard.ws.IFD;
import org.openecard.ws.Management;
import org.openecard.ws.SAL;


/**
 *
 * @author Johannes.Schmoelz
 */
public class ClientEnv implements Environment {

    private UserConsent gui;
    private IFD ifd;
    private final LinkedHashSet<byte[]> ifdCtx;
    private SAL sal;
    private EventDispatcher manager;
    private Dispatcher dispatcher;
    private Management management;
    private CardRecognition recognition;
    private CIFProvider cifProvider;
    private SalSelector salSelector;
    private final Map<String, Object> genericComponents;

    public ClientEnv() {
	genericComponents = new ConcurrentSkipListMap<>();
	ifdCtx = new LinkedHashSet<>();
    }


    @Override
    public void setGui(UserConsent gui) {
	this.gui = gui;
    }

    @Override
    public UserConsent getGui() {
	return this.gui;
    }

    @Override
    public void setIfd(IFD ifd) {
	this.ifd = ifd;
    }

    @Override
    @Dispatchable(interfaceClass = IFD.class)
    public IFD getIfd() {
	return ifd;
    }

    @Override
    public synchronized void addIfdCtx(byte[] ctx) {
	if (ctx != null && ctx.length > 0) {
	    ifdCtx.add(Arrays.copyOf(ctx, ctx.length));
	}
    }

    @Override
    public synchronized void removeIfdCtx(byte[] ctx) {
	Iterator<byte[]> it = ifdCtx.iterator();
	while (it.hasNext()) {
	    byte[] next = it.next();
	    if (Arrays.equals(next, ctx)) {
		it.remove();
		return;
	    }
	}
    }

    @Override
    public synchronized List<byte[]> getIfdCtx() {
	ArrayList<byte[]> result = new ArrayList<>(ifdCtx.size());
	for (byte[] next : ifdCtx) {
	    result.add(Arrays.copyOf(next, next.length));
	}
	return result;
    }

    @Override
    public void setEventDispatcher(EventDispatcher manager) {
	this.manager = manager;
    }

    @Override
    public EventDispatcher getEventDispatcher() {
	return manager;
    }

    @Override
    public void setSal(SAL sal) {
	this.sal = sal;
    }

    @Override
    @Dispatchable(interfaceClass = SAL.class)
    public SAL getSal() {
	return sal;
    }

    @Override
    public void setDispatcher(Dispatcher dispatcher) {
	this.dispatcher = dispatcher;
    }

    @Override
    public Dispatcher getDispatcher() {
	return dispatcher;
    }

    @Override
    public void setGenericComponent(String id, Object component) {
	genericComponents.put(id, component);
    }

    @Override
    public Object getGenericComponent(String id) {
	return genericComponents.get(id);
    }

    @Override
    public void setManagement(Management management) {
	this.management = management;
    }

    @Override
    @Dispatchable(interfaceClass = Management.class)
    public Management getManagement() {
	return management;
    }

    @Override
    public void setRecognition(CardRecognition recognition) {
	this.recognition = recognition;
    }

    @Override
    public CardRecognition getRecognition() {
	return recognition;
    }

    @Override
    public void setCifProvider(CIFProvider provider) {
	this.cifProvider = provider;
    }

    @Override
    public CIFProvider getCifProvider() {
	return cifProvider;
    }

    @Override
    public void setSalSelector(SalSelector salSelect) {
	this.salSelector = salSelect;
    }

    @Override
    public SalSelector getSalSelector() {
	return this.salSelector;
    }

}
