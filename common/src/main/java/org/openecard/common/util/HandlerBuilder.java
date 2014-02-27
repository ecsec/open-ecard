/****************************************************************************
 * Copyright (C) 2014 ecsec GmbH.
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

package org.openecard.common.util;

import iso.std.iso_iec._24727.tech.schema.CardApplicationPathType;
import iso.std.iso_iec._24727.tech.schema.ChannelHandleType;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType.RecognitionInfo;
import java.math.BigInteger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;


/**
 * Builder class for {@code CardApplicationPathType} and {@code ConnectionHandleType}.
 * The set methods always emit a copy of the builder with the respective value set. This makes it easy to supply
 * preconfigured builder instances.<br/>
 * The builder instance is immutable making it safe to use in different threads.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
@Immutable
public class HandlerBuilder {

    private final byte[] contextHandle;
    private final String ifdName;
    private final BigInteger slotIdx;
    private final byte[] cardApp;
    private final byte[] slotHandle;
    // recognition
    private final String cardType;
    private final byte[] cardIdentifier;
    // channel handle
    private final String protocolEndpoint;
    private final String sessionId;
    private final String binding;
    // TODO: path security


    private HandlerBuilder() {
	this(null, null, null, null, null, null, null, null, null, null);
    }

    private HandlerBuilder(byte[] contextHandle, String ifdName, BigInteger slotIdx, byte[] cardApp, byte[] slotHandle,
	    String cardType, byte[] cardIdentifier, String protocolEndpoint, String sessionId, String binding) {
	this.contextHandle = contextHandle;
	this.ifdName = ifdName;
	this.slotIdx = slotIdx;
	this.cardApp = cardApp;
	this.slotHandle = slotHandle;
	this.cardType = cardType;
	this.cardIdentifier = cardIdentifier;
	this.protocolEndpoint = protocolEndpoint;
	this.sessionId = sessionId;
	this.binding = binding;
    }


    /**
     * Create an empty {@code HandlerBuilder} instance.
     *
     * @return Empty instance of a {@code HandlerBuilder}.
     */
    public static HandlerBuilder create() {
	return new HandlerBuilder();
    }


    private <T extends CardApplicationPathType> T buildAppPath(T path) {
	path.setChannelHandle(buildChannelHandle());
	path.setContextHandle(contextHandle);
	path.setIFDName(ifdName);
	path.setSlotIndex(slotIdx);
	path.setCardApplication(cardApp);
	return path;
    }

    /**
     * Creates a {@code CardApplicationPathType} with all available values in the builder.
     *
     * @return A {@code CardApplicationPathType} instance.
     */
    @Nonnull
    public CardApplicationPathType buildAppPath() {
	CardApplicationPathType path = buildAppPath(new CardApplicationPathType());
	return path;
    }

    /**
     * Creates a {@code ConnectionHandleType} with all available values in the builder.
     *
     * @return A {@code ConnectionHandleType} instance.
     */
    @Nonnull
    public ConnectionHandleType buildConnectionHandle() {
	ConnectionHandleType handle = buildAppPath(new ConnectionHandleType());
	handle.setSlotHandle(slotHandle);
	handle.setRecognitionInfo(buildRecognitionInfo());
	return handle;
    }

    /**
     * Creates a {@code RecognitionInfo} if the relevant values are set in the instance.
     *
     * @return A {@code ConnectionHandleType} instance, or {@code null} if no values are available.
     */
    @Nullable
    public RecognitionInfo buildRecognitionInfo() {
	if (cardType != null) {
	    RecognitionInfo rInfo = new RecognitionInfo();
	    rInfo.setCardType(cardType);
	    rInfo.setCardIdentifier(cardIdentifier);
	    return rInfo;
	} else {
	    return null;
	}
    }

    /**
     * Creates a {@code ChannelHandleType} if the relevant values are set in the instance.
     *
     * @return A {@code ChannelHandleType} instance, or {@code null} if no values are available.
     */
    @Nullable
    public ChannelHandleType buildChannelHandle() {
	if (protocolEndpoint != null || sessionId != null || binding != null ) {
	    ChannelHandleType chan = new ChannelHandleType();
	    chan.setProtocolTerminationPoint(protocolEndpoint);
	    chan.setSessionIdentifier(sessionId);
	    chan.setBinding(binding);
	    return chan;
	} else {
	    return null;
	}
    }


    public HandlerBuilder setContextHandle(byte[] contextHandle) {
	HandlerBuilder b = new HandlerBuilder(contextHandle, ifdName, slotIdx, cardApp, slotHandle, cardType,
		cardIdentifier, protocolEndpoint, sessionId, binding);
	return b;
    }

    public HandlerBuilder setIfdName(String ifdName) {
	HandlerBuilder b = new HandlerBuilder(contextHandle, ifdName, slotIdx, cardApp, slotHandle, cardType,
		cardIdentifier, protocolEndpoint, sessionId, binding);
	return b;
    }

    public HandlerBuilder setSlotIdx(BigInteger slotIdx) {
	HandlerBuilder b = new HandlerBuilder(contextHandle, ifdName, slotIdx, cardApp, slotHandle, cardType,
		cardIdentifier, protocolEndpoint, sessionId, binding);
	return b;
    }

    public HandlerBuilder setCardApp(byte[] cardApp) {
	HandlerBuilder b = new HandlerBuilder(contextHandle, ifdName, slotIdx, cardApp, slotHandle, cardType,
		cardIdentifier, protocolEndpoint, sessionId, binding);
	return b;
    }

    public HandlerBuilder setSlotHandle(byte[] slotHandle) {
	HandlerBuilder b = new HandlerBuilder(contextHandle, ifdName, slotIdx, cardApp, slotHandle, cardType,
		cardIdentifier, protocolEndpoint, sessionId, binding);
	return b;
    }

    public HandlerBuilder setRecognitionInfo(RecognitionInfo info) {
	if (info != null) {
	    HandlerBuilder b = new HandlerBuilder(contextHandle, ifdName, slotIdx, cardApp, slotHandle,
		info.getCardType(), info.getCardIdentifier(), protocolEndpoint, sessionId, binding);
	return b;
	} else {
	    return this;
	}
    }

    public HandlerBuilder setCardType(RecognitionInfo info) {
	if (info != null) {
	    return setCardType(info.getCardType());
	} else {
	    return this;
	}
    }
    public HandlerBuilder setCardType(String cardType) {
	HandlerBuilder b = new HandlerBuilder(contextHandle, ifdName, slotIdx, cardApp, slotHandle, cardType,
		cardIdentifier, protocolEndpoint, sessionId, binding);
	return b;
    }

    public HandlerBuilder setCardIdentifier(RecognitionInfo info) {
	if (info != null) {
	    return setCardIdentifier(info.getCardIdentifier());
	} else {
	    return this;
	}
    }
    public HandlerBuilder setCardIdentifier(byte[] cardIdentifier) {
	HandlerBuilder b = new HandlerBuilder(contextHandle, ifdName, slotIdx, cardApp, slotHandle, cardType,
		cardIdentifier, protocolEndpoint, sessionId, binding);
	return b;
    }

    public HandlerBuilder setChannelHandle(ChannelHandleType channel) {
	if (channel != null) {
	    HandlerBuilder b = new HandlerBuilder(contextHandle, ifdName, slotIdx, cardApp, slotHandle, cardType,
		cardIdentifier, channel.getProtocolTerminationPoint(), channel.getSessionIdentifier(),
		channel.getBinding());
	return b;
	} else {
	    return this;
	}
    }

    public HandlerBuilder setProtocolEndpoint(String protocolEndpoint) {
	HandlerBuilder b = new HandlerBuilder(contextHandle, ifdName, slotIdx, cardApp, slotHandle, cardType,
		cardIdentifier, protocolEndpoint, sessionId, binding);
	return b;
    }

    public HandlerBuilder setSessionId(String sessionId) {
	HandlerBuilder b = new HandlerBuilder(contextHandle, ifdName, slotIdx, cardApp, slotHandle, cardType,
		cardIdentifier, protocolEndpoint, sessionId, binding);
	return b;
    }

    public HandlerBuilder setBinding(String binding) {
	HandlerBuilder b = new HandlerBuilder(contextHandle, ifdName, slotIdx, cardApp, slotHandle, cardType,
		cardIdentifier, protocolEndpoint, sessionId, binding);
	return b;
    }

}
