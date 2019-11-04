/****************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ***************************************************************************/

package org.openecard.common.sal.state;

import iso.std.iso_iec._24727.tech.schema.ChannelHandleType;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import org.openecard.addon.sal.SALProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Tobias Wich
 */
public class StateEntry {

    private static Logger LOG = LoggerFactory.getLogger(StateEntry.class);

    private final String session;
    private SALProtocol protocol;
    private String protocolName;
    private ConnectedCardEntry cardEntry;
    private byte[] ctxHandle;

    public StateEntry(String session) {
	this.session = session;
    }

    public String getSession() {
	return session;
    }

    public ConnectedCardEntry setConnectedCard(byte[] slotHandle, CardEntry card) {
	if (this.cardEntry != null) {
	    LOG.warn("Session {} already connected to a card {}. Replacing with card {}.", this.cardEntry, card);
	}
	this.cardEntry = new ConnectedCardEntry(slotHandle, card);
	this.ctxHandle = card.ctxHandle;
	return this.cardEntry;
    }

    public void removeCard() {
	cardEntry = null;
    }

    public void setProtocol(SALProtocol protocol, String protocolName) {
	this.protocol = protocol;
    }

    public void removeProtocol(String protocolName) {
	if (this.protocolName == null ? protocolName != null : !this.protocolName.equals(protocolName)) {
	    LOG.warn("Removing protocol with name {} that does not match existing name {}.",
		    protocolName, this.protocolName);
	}
	this.protocol = null;
	this.protocolName = null;
    }

    public SALProtocol getProtocol() {
	return protocol;
    }

    public String getProtocolName() {
	return this.protocolName;
    }

    public ConnectedCardEntry getCardEntry() {
	return cardEntry;
    }

    public byte[] getContextHandle() {
	return ctxHandle;
    }

    public ConnectionHandleType getConnectionHandle() {
	ConnectionHandleType connectionHandle = new ConnectionHandleType();
	fillConnectionHandle(connectionHandle);
	return connectionHandle;
    }

    public void fillConnectionHandle(ConnectionHandleType connectionHandle) {
	ChannelHandleType channelHandle = connectionHandle.getChannelHandle();
	if (channelHandle == null) {
	    channelHandle = new ChannelHandleType();
	    connectionHandle.setChannelHandle(channelHandle);
	}
	channelHandle.setSessionIdentifier(session);
	connectionHandle.setContextHandle(ctxHandle);
	if (this.cardEntry != null) {
	    this.cardEntry.fillConnectionHandle(connectionHandle);
	}
    }
}
