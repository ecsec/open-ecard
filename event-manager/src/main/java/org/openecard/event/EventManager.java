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

package org.openecard.event;

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.GetStatus;
import iso.std.iso_iec._24727.tech.schema.GetStatusResponse;
import iso.std.iso_iec._24727.tech.schema.IFDStatusType;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.annotation.Nonnull;
import org.openecard.common.WSHelper;
import org.openecard.common.WSHelper.WSException;
import org.openecard.common.enums.EventType;
import org.openecard.common.interfaces.Environment;
import org.openecard.common.interfaces.EventCallback;
import org.openecard.common.interfaces.EventFilter;
import org.openecard.common.util.HandlerBuilder;
import org.openecard.common.util.ValueGenerators;
import org.openecard.recognition.CardRecognition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Main class of the event system.
 * Use this to create and operate an event manager.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class EventManager implements org.openecard.common.interfaces.EventManager {

    private static final Logger logger = LoggerFactory.getLogger(EventManager.class);

    protected final CardRecognition cr;
    protected final Environment env;
    protected final byte[] ctx;
    protected final String sessionId;
    protected final boolean recognize;
    private final HandlerBuilder builder;
    private final EventDispatcher dispatcher;

    protected ExecutorService threadPool;

    private Future<?> watcher;


    public EventManager(CardRecognition cr, Environment env, byte[] ctx) {
	this.cr = cr;
	this.recognize = cr != null;
	this.env = env;
	this.ctx = ctx;
	this.sessionId = ValueGenerators.generateSessionID();
	this.builder = HandlerBuilder.create()
		.setContextHandle(ctx)
		.setSessionId(sessionId);
	this.dispatcher = new EventDispatcher(this);
    }

    @Override
    public synchronized Object initialize() {
	threadPool = Executors.newCachedThreadPool();
	// start watcher thread
	watcher = threadPool.submit(new EventRunner(this, builder));
	// TODO: remove return value altogether
	return new ArrayList<ConnectionHandleType>();
    }

    @Override
    public synchronized void terminate() {
	watcher.cancel(true);
	threadPool.shutdownNow();
    }


    protected List<IFDStatusType> ifdStatus() throws WSException {
	GetStatus status = new GetStatus();
	status.setContextHandle(ctx);
	GetStatusResponse statusResponse = env.getIFD().getStatus(status);
	List<IFDStatusType> result;

	WSHelper.checkResult(statusResponse);
	result = statusResponse.getIFDStatus();
	return result;
    }


    protected synchronized void notify(EventType eventType, Object eventData) {
	dispatcher.notify(eventType, eventData);
    }

    @Override
    public void register(EventCallback callback, EventFilter filter) {
	dispatcher.add(callback, filter);
    }

    @Override
    public void register(EventCallback callback, EventType type) {
	dispatcher.add(callback, type);
    }

    @Override
    public void register(@Nonnull EventCallback callback, @Nonnull List<EventType> types) {
	dispatcher.add(callback, types.toArray(new EventType[types.size()]));
    }

    @Override
    public synchronized void registerAllEvents(EventCallback callback) {
	dispatcher.add(callback);
    }

    @Override
    public void unregister(EventCallback callback) {
	dispatcher.del(callback);
    }

}
