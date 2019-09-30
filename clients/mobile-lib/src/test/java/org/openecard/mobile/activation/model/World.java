/****************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
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
package org.openecard.mobile.activation.model;

import java.io.IOException;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import static org.mockito.Mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openecard.common.ifd.scio.SCIOATR;
import org.openecard.common.util.Promise;
import org.openecard.mobile.activation.ActivationController;
import org.openecard.mobile.activation.ActivationResult;
import org.openecard.mobile.activation.ActivationResultCode;
import org.openecard.mobile.activation.ConfirmTwoPasswordsOperation;
import org.openecard.mobile.activation.ContextManager;
import org.openecard.mobile.activation.PinManagementControllerFactory;
import org.openecard.mobile.activation.PinManagementInteraction;
import org.openecard.mobile.activation.ServiceErrorResponse;
import org.openecard.mobile.activation.common.CommonActivationUtils;
import static org.openecard.mobile.activation.model.Timeout.MIN_WAIT_TIMEOUT;
import static org.openecard.mobile.activation.model.Timeout.WAIT_TIMEOUT;
import org.openecard.mobile.ex.ApduExtLengthNotSupported;
import org.openecard.mobile.ex.NfcDisabled;
import org.openecard.mobile.ex.NfcUnavailable;
import org.openecard.mobile.ex.UnableToInitialize;
import org.openecard.scio.AbstractNFCCard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

/**
 *
 * @author Neil Crossley
 */
public class World implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(World.class);

    private final CommonActivationUtils activationUtils;
    private final MockNFCCapabilitiesConfigurator capabilities;
    private final MobileTerminalConfigurator terminalConfigurator;
    public final ContextWorld contextWorld;
    public final PinManagementWorld pinManagementWorld;
    private AbstractNFCCard currentNfcCard;

    public World(CommonActivationUtils activationUtils, MockNFCCapabilitiesConfigurator capabilities, MobileTerminalConfigurator terminalConfigurator) {
	this.activationUtils = activationUtils;
	this.capabilities = capabilities;
	this.terminalConfigurator = terminalConfigurator;
	this.contextWorld = new ContextWorld();
	this.pinManagementWorld = new PinManagementWorld();
    }

    public void microSleep() {
	try {
	    LOG.debug("Sleeping.");
	    Thread.sleep(MIN_WAIT_TIMEOUT);
	} catch (InterruptedException ex) {
	    throw new RuntimeException(ex);
	}
    }

    public void givenNpaCardInserted() {
	LOG.debug("NPA card inserted.");
	AbstractNFCCard spyCard = mock(AbstractNFCCard.class, withSettings()
		.useConstructor(this.terminalConfigurator.terminal).defaultAnswer(CALLS_REAL_METHODS));

	this.currentNfcCard = spyCard;
	doReturn(true).when(currentNfcCard).isCardPresent();
	doReturn(new SCIOATR(new byte[]{59, -118, -128, 1, -128, 49, -72, 115, -124, 1, -32, -126, -112, 0, 6})).when(currentNfcCard).getATR();
	try {
	    Map.Entry<byte[], byte[]>[] behaviour = new Map.Entry[]{
		new SimpleImmutableEntry(new byte[]{0, -92, 0, 12, 2, 63, 0}, new byte[]{-112, 0}),
		new SimpleImmutableEntry(new byte[]{0, -92, 2, 12, 2, 0, 3}, new byte[]{106, -126}),
		new SimpleImmutableEntry(new byte[]{0, -92, 0, 12, 2, 63, 0}, new byte[]{-112, 0}),
		new SimpleImmutableEntry(new byte[]{0, -92, 2, 12, 2, 47, 0}, new byte[]{-112, 0}),
		new SimpleImmutableEntry(new byte[]{0, -78, 4, 4, -1}, new byte[]{109, 0}),
		new SimpleImmutableEntry(new byte[]{0, -92, 0, 12, 2, 63, 0}, new byte[]{-112, 0}),
		new SimpleImmutableEntry(new byte[]{0, -92, 2, 12, 2, 47, 0}, new byte[]{-112, 0}),
		new SimpleImmutableEntry(new byte[]{0, -78, 3, 4, -1}, new byte[]{109, 0}),
		new SimpleImmutableEntry(new byte[]{0, -92, 4, 12, 15, -16, 69, 115, 116, 69, 73, 68, 32, 118, 101, 114, 32, 49, 46, 48}, new byte[]{106, -126}),
		new SimpleImmutableEntry(new byte[]{0, -92, 4, 12, 15, -46, 51, 0, 0, 0, 69, 115, 116, 69, 73, 68, 32, 118, 51, 53}, new byte[]{106, -126}),
		new SimpleImmutableEntry(new byte[]{0, -92, 0, 12, 2, 63, 0}, new byte[]{-112, 0}),
		new SimpleImmutableEntry(new byte[]{0, -92, 2, 12, 2, 47, 0}, new byte[]{-112, 0}),
		new SimpleImmutableEntry(new byte[]{0, -80, 0, 0, -1}, new byte[]{97, 50, 79, 15, -24, 40, -67, 8, 15, -96, 0, 0, 1, 103, 69, 83, 73, 71, 78, 80, 15, 67, 73, 65, 32, 122, 117, 32, 68, 70, 46, 101, 83, 105, 103, 110, 81, 0, 115, 12, 79, 10, -96, 0, 0, 1, 103, 69, 83, 73, 71, 78, 97, 9, 79, 7, -96, 0, 0, 2, 71, 16, 1, 97, 11, 79, 9, -24, 7, 4, 0, 127, 0, 7, 3, 2, 97, 12, 79, 10, -96, 0, 0, 1, 103, 69, 83, 73, 71, 78, 98, -126}),
		new SimpleImmutableEntry(new byte[]{0, -92, 0, 12, 2, 63, 0}, new byte[]{-112, 0}),
		new SimpleImmutableEntry(new byte[]{0, 34, -63, -92, 15, -128, 10, 4, 0, 127, 0, 7, 2, 2, 4, 2, 2, -125, 1, 3}, new byte[]{-112, 0}),
		new SimpleImmutableEntry(new byte[]{0, -92, 0, 12, 2, 63, 0}, new byte[]{-112, 0}),
		new SimpleImmutableEntry(new byte[]{0, -92, 2, 4, 2, 1, 28, -1}, new byte[]{98, 26, -128, 2, 4, 0, -59, 2, 4, 0, -126, 1, 1, -125, 2, 1, 28, -120, 1, -32, -118, 1, 5, -95, 3, -117, 1, 3, -112, 0}),
		new SimpleImmutableEntry(new byte[]{0, -80, 0, 0, -1}, new byte[]{49, -127, -63, 48, 13, 6, 8, 4, 0, 127, 0, 7, 2, 2, 2, 2, 1, 2, 48, 18, 6, 10, 4, 0, 127, 0, 7, 2, 2, 3, 2, 2, 2, 1, 2, 2, 1, 72, 48, 18, 6, 10, 4, 0, 127, 0, 7, 2, 2, 4, 2, 2, 2, 1, 2, 2, 1, 13, 48, 28, 6, 9, 4, 0, 127, 0, 7, 2, 2, 3, 2, 48, 12, 6, 7, 4, 0, 127, 0, 7, 1, 2, 2, 1, 13, 2, 1, 72, 48, 42, 6, 8, 4, 0, 127, 0, 7, 2, 2, 6, 22, 30, 104, 116, 116, 112, 58, 47, 47, 98, 115, 105, 46, 98, 117, 110, 100, 46, 100, 101, 47, 99, 105, 102, 47, 110, 112, 97, 46, 120, 109, 108, 48, 62, 6, 8, 4, 0, 127, 0, 7, 2, 2, 8, 49, 50, 48, 18, 6, 10, 4, 0, 127, 0, 7, 2, 2, 3, 2, 2, 2, 1, 2, 2, 1, 73, 48, 28, 6, 9, 4, 0, 127, 0, 7, 2, 2, 3, 2, 48, 12, 6, 7, 4, 0, 127, 0, 7, 1, 2, 2, 1, 13, 2, 1, 73, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -112, 0}),
		new SimpleImmutableEntry(new byte[]{0, -80, 0, -1, -1}, new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -112, 0}),
		new SimpleImmutableEntry(new byte[]{0, -80, 1, -2, -1}, new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -112, 0}),
		new SimpleImmutableEntry(new byte[]{0, -80, 2, -3, -1}, new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -112, 0}),
		new SimpleImmutableEntry(new byte[]{0, -80, 3, -4, 4}, new byte[]{0, 0, 0, 0, -112, 0}),
		new SimpleImmutableEntry(new byte[]{0, 34, -63, -92, 18, -128, 10, 4, 0, 127, 0, 7, 2, 2, 4, 2, 2, -125, 1, 3, -124, 1, 13}, new byte[]{-112, 0}),
		new SimpleImmutableEntry(new byte[]{16, -122, 0, 0, 2, 124, 0, 0}, new byte[]{124, 18, -128, 16, -25, -18, 124, -88, -16, 19, 122, 104, 90, -97, -35, -124, -37, -72, 5, 7, -112, 0}),
		new SimpleImmutableEntry(new byte[]{16, -122, 0, 0, 69, 124, 67, -127, 65, 4, 41, 89, -115, 38, 101, -67, 80, 75, 71, -76, -55, 45, 21, 73, -73, -25, 96, -79, -58, 16, 85, 108, -52, 23, 104, -105, -74, -64, -99, 52, 2, 90, 105, 109, -83, 86, -83, -109, 107, -113, 42, 58, -115, -104, 1, 100, -15, -88, 14, 48, -74, 42, 120, 0, 41, -7, 97, 40, -16, -114, -68, -93, -78, 17, 0}, new byte[]{124, 67, -126, 65, 4, 124, -18, -10, 108, -57, -63, -35, 3, 100, -36, -94, 3, 121, -21, -36, -107, -92, 115, -30, 126, 34, 104, -102, -19, -103, 71, -122, 6, 119, 26, -72, 94, 39, 82, 60, 8, 55, -80, -95, 11, 51, -59, -63, -95, -125, -102, -28, 15, 80, -46, -60, 125, -2, -82, 26, 89, 47, -56, 36, 81, -30, -104, -117, 115, -112, 0}),
		new SimpleImmutableEntry(new byte[]{16, -122, 0, 0, 69, 124, 67, -125, 65, 4, 68, 32, -92, 7, 25, -25, -32, -21, -67, 89, 53, 100, 79, -30, -6, 126, 0, -17, 120, 115, -8, -88, -109, -120, -112, 122, 67, 9, -87, -105, -8, 106, 115, 49, 77, 93, 108, 28, 105, 124, 64, -17, 2, 103, -92, 63, 60, -126, 102, 75, -27, -93, -15, 80, -44, 125, -23, -19, -61, -53, 90, -7, -60, 115, 0}, new byte[]{124, 67, -124, 65, 4, 28, 39, -124, -122, -45, -62, -57, 49, -9, -88, 39, -14, 14, -120, 76, -10, -114, -75, -126, 120, 32, 68, -5, -17, 48, 61, -69, -29, 27, 96, 84, -81, 28, 103, -57, 104, -107, -69, -47, -44, 17, 91, 76, 20, 101, 28, -27, -15, 65, -70, -36, 14, 63, 93, 80, 116, -88, -41, 1, -101, 107, -126, 3, 97, -112, 0}),
		new SimpleImmutableEntry(new byte[]{0, -122, 0, 0, 12, 124, 10, -123, 8, -127, 73, 52, -41, 115, -41, -16, -39, 0}, new byte[]{124, 10, -122, 8, 107, 50, -36, 60, -114, 45, -22, 76, -112, 0}),
		new SimpleImmutableEntry(new byte[]{12, 44, 2, 3, 29, -121, 17, 1, 93, -1, -125, -1, -40, -23, 37, -77, 48, 55, 89, -69, 46, -14, -109, 8, -114, 8, 127, 119, 116, 4, 65, -35, 55, 67, 0}, new byte[]{-103, 2, -112, 0, -114, 8, 38, -89, 17, 43, 85, 8, -78, 14, -112, 0}),
		new SimpleImmutableEntry(new byte[]{0, -92, 0, 12, 2, 63, 0}, new byte[]{-112, 0}),
		new SimpleImmutableEntry(new byte[]{0, -92, 2, 12, 2, 0, 3}, new byte[]{106, -126}),
		new SimpleImmutableEntry(new byte[]{0, -92, 0, 12, 2, 63, 0}, new byte[]{-112, 0}),
		new SimpleImmutableEntry(new byte[]{0, -92, 2, 12, 2, 47, 0}, new byte[]{-112, 0}),
		new SimpleImmutableEntry(new byte[]{0, -78, 4, 4, -1}, new byte[]{109, 0}),
		new SimpleImmutableEntry(new byte[]{0, -92, 0, 12, 2, 63, 0}, new byte[]{-112, 0}),
		new SimpleImmutableEntry(new byte[]{0, -92, 2, 12, 2, 47, 0}, new byte[]{-112, 0}),
		new SimpleImmutableEntry(new byte[]{0, -78, 3, 4, -1}, new byte[]{109, 0}),
		new SimpleImmutableEntry(new byte[]{0, -92, 4, 12, 15, -16, 69, 115, 116, 69, 73, 68, 32, 118, 101, 114, 32, 49, 46, 48}, new byte[]{106, -126}),
		new SimpleImmutableEntry(new byte[]{0, -92, 4, 12, 15, -46, 51, 0, 0, 0, 69, 115, 116, 69, 73, 68, 32, 118, 51, 53}, new byte[]{106, -126}),
		new SimpleImmutableEntry(new byte[]{0, -92, 0, 12, 2, 63, 0}, new byte[]{-112, 0}),
		new SimpleImmutableEntry(new byte[]{0, -92, 2, 12, 2, 47, 0}, new byte[]{-112, 0}),
		new SimpleImmutableEntry(new byte[]{0, -80, 0, 0, -1}, new byte[]{97, 50, 79, 15, -24, 40, -67, 8, 15, -96, 0, 0, 1, 103, 69, 83, 73, 71, 78, 80, 15, 67, 73, 65, 32, 122, 117, 32, 68, 70, 46, 101, 83, 105, 103, 110, 81, 0, 115, 12, 79, 10, -96, 0, 0, 1, 103, 69, 83, 73, 71, 78, 97, 9, 79, 7, -96, 0, 0, 2, 71, 16, 1, 97, 11, 79, 9, -24, 7, 4, 0, 127, 0, 7, 3, 2, 97, 12, 79, 10, -96, 0, 0, 1, 103, 69, 83, 73, 71, 78, 98, -126}),};

	    for (int i = 0; i < behaviour.length; i++) {
		Map.Entry<byte[], byte[]> entry = behaviour[i];
		doReturn(entry.getValue()).when(currentNfcCard).transceive(entry.getKey());
	    }

	    doAnswer(new Answer<byte[]>() {
		private int index = 0;

		byte[][] behaviour = new byte[][]{
		    new byte[]{97, 50, 79, 15, -24, 40, -67, 8, 15, -96, 0, 0, 1, 103, 69, 83, 73, 71, 78, 80, 15, 67, 73, 65, 32, 122, 117, 32, 68, 70, 46, 101, 83, 105, 103, 110, 81, 0, 115, 12, 79, 10, -96, 0, 0, 1, 103, 69, 83, 73, 71, 78, 97, 9, 79, 7, -96, 0, 0, 2, 71, 16, 1, 97, 11, 79, 9, -24, 7, 4, 0, 127, 0, 7, 3, 2, 97, 12, 79, 10, -96, 0, 0, 1, 103, 69, 83, 73, 71, 78, 98, -126},
		    new byte[]{49, -127, -63, 48, 13, 6, 8, 4, 0, 127, 0, 7, 2, 2, 2, 2, 1, 2, 48, 18, 6, 10, 4, 0, 127, 0, 7, 2, 2, 3, 2, 2, 2, 1, 2, 2, 1, 72, 48, 18, 6, 10, 4, 0, 127, 0, 7, 2, 2, 4, 2, 2, 2, 1, 2, 2, 1, 13, 48, 28, 6, 9, 4, 0, 127, 0, 7, 2, 2, 3, 2, 48, 12, 6, 7, 4, 0, 127, 0, 7, 1, 2, 2, 1, 13, 2, 1, 72, 48, 42, 6, 8, 4, 0, 127, 0, 7, 2, 2, 6, 22, 30, 104, 116, 116, 112, 58, 47, 47, 98, 115, 105, 46, 98, 117, 110, 100, 46, 100, 101, 47, 99, 105, 102, 47, 110, 112, 97, 46, 120, 109, 108, 48, 62, 6, 8, 4, 0, 127, 0, 7, 2, 2, 8, 49, 50, 48, 18, 6, 10, 4, 0, 127, 0, 7, 2, 2, 3, 2, 2, 2, 1, 2, 2, 1, 73, 48, 28, 6, 9, 4, 0, 127, 0, 7, 2, 2, 3, 2, 48, 12, 6, 7, 4, 0, 127, 0, 7, 1, 2, 2, 1, 13, 2, 1, 73, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -112, 0},
		    new byte[]{97, 50, 79, 15, -24, 40, -67, 8, 15, -96, 0, 0, 1, 103, 69, 83, 73, 71, 78, 80, 15, 67, 73, 65, 32, 122, 117, 32, 68, 70, 46, 101, 83, 105, 103, 110, 81, 0, 115, 12, 79, 10, -96, 0, 0, 1, 103, 69, 83, 73, 71, 78, 97, 9, 79, 7, -96, 0, 0, 2, 71, 16, 1, 97, 11, 79, 9, -24, 7, 4, 0, 127, 0, 7, 3, 2, 97, 12, 79, 10, -96, 0, 0, 1, 103, 69, 83, 73, 71, 78, 98, -126
		    }};

		@Override
		public byte[] answer(InvocationOnMock arg0) throws Throwable {
		    byte[] result = behaviour[index];
		    if (behaviour.length - 1 < index) {
			index += 1;
		    }
		    return result;
		}
	    }
	    ).when(currentNfcCard).transceive(new byte[]{0, -80, 0, 0, -1});

	} catch (IOException ex) {
	    throw new RuntimeException(ex);
	}

	this.terminalConfigurator.terminal.setNFCCard(currentNfcCard);
    }

    @Override
    public void close() throws Exception {
	LOG.debug("Closing.");
	this.pinManagementWorld.close();
	this.contextWorld.close();

    }

    public void givenaCardRemoved() {
	LOG.debug("Card removed.");
	if (currentNfcCard == null) {
	    throw new IllegalStateException("Cannot remove a card when none has been inserted!");
	}

	doReturn(false).when(currentNfcCard).isCardPresent();
	terminalConfigurator.terminal.setNFCCard(null);
    }

    public class PinManagementWorld implements AutoCloseable {

	private PinManagementControllerFactory _pinManagementFactory;
	private Set<String> supportedCards;
	private Promise<ActivationResult> promisedActivationResult;
	private Promise<Void> promisedStarted;
	private Promise<Void> promisedRequestCardInsertion;
	private Promise<String> promisedRecognizeCard;
	private Promise<ConfirmTwoPasswordsOperation> promisedOperationEnterTwoPasswords;
	private PinManagementInteraction interaction;
	private ActivationController activationController;
	private Promise<Void> promisedRemoveCard;

	private PinManagementControllerFactory pinManagementFactory() {
	    if (_pinManagementFactory == null) {
		_pinManagementFactory = activationUtils.pinManagementFactory();
	    }
	    return _pinManagementFactory;
	}

	public void startSimplePinManagement() {
	    LOG.debug("Start simple pin management.");
	    supportedCards = new HashSet<>();
	    promisedActivationResult = new Promise<>();
	    promisedStarted = new Promise<>();
	    promisedRequestCardInsertion = new Promise();
	    promisedRecognizeCard = new Promise();
	    promisedRemoveCard = new Promise();
	    promisedOperationEnterTwoPasswords = new Promise<>();
	    interaction = mock(PinManagementInteraction.class);
	    doAnswer((Answer<Void>) (InvocationOnMock arg0) -> {
		if (promisedRequestCardInsertion.isDelivered()) {
		    promisedRequestCardInsertion = new Promise();
		}
		promisedRequestCardInsertion.deliver(null);
		return null;
	    }).when(interaction).requestCardInsertion();
	    doAnswer((Answer<Void>) (InvocationOnMock arg0) -> {
		if (promisedRemoveCard.isDelivered()) {
		    promisedRemoveCard = new Promise();
		}
		promisedRemoveCard.deliver(null);
		return null;
	    }).when(interaction).onCardRemoved();
	    doAnswer((Answer<Void>) (InvocationOnMock arg0) -> {
		if (promisedRequestCardInsertion.isDelivered()) {
		    promisedRequestCardInsertion = new Promise();
		}
		promisedOperationEnterTwoPasswords.deliver((ConfirmTwoPasswordsOperation) arg0.getArguments()[0]);
		return null;
	    }).when(interaction).onPinChangeable(anyInt(), any());

	    doAnswer((Answer<Void>) (InvocationOnMock arg0) -> {
		if (promisedRecognizeCard.isDelivered()) {
		    promisedRecognizeCard = new Promise();
		}
		promisedRecognizeCard.deliver((String) arg0.getArguments()[0]);
		return null;
	    }).when(interaction).onCardRecognized(anyString());
	    activationController = pinManagementFactory().create(
		    supportedCards,
		    PromiseDeliveringFactory.controllerCallback.deliverStartedCompletion(promisedStarted, promisedActivationResult),
		    interaction);
	    activationController.start();
	}

	public void expectActivationResult(ActivationResultCode code) {
	    LOG.debug("Expect activation result {}.", code);
	    try {
		ActivationResult result = promisedActivationResult.deref(WAIT_TIMEOUT, TimeUnit.MILLISECONDS);
		Assert.assertEquals(result.getResultCode(), code);
	    } catch (InterruptedException | TimeoutException ex) {
		throw new RuntimeException(ex);
	    }
	}

	public void expectOnStarted() {
	    LOG.debug("Expect on started.");
	    try {
		promisedStarted.deref(WAIT_TIMEOUT, TimeUnit.MILLISECONDS);
	    } catch (InterruptedException | TimeoutException ex) {
		throw new RuntimeException(ex);
	    }
	}

	public void expectCardInsertionRequest() {
	    LOG.debug("Expect on started.");
	    try {
		promisedRequestCardInsertion.deref(WAIT_TIMEOUT, TimeUnit.MILLISECONDS);
	    } catch (InterruptedException | TimeoutException ex) {
		throw new RuntimeException(ex);
	    }
	}

	public void expectRecognitionOfNpaCard() {
	    LOG.debug("Expect recognition of NPA card.");
	    try {
		String type = promisedRecognizeCard.deref(WAIT_TIMEOUT, TimeUnit.MILLISECONDS);
		Assert.assertEquals(type, "http://bsi.bund.de/cif/npa.xml");
	    } catch (InterruptedException | TimeoutException ex) {
		throw new RuntimeException(ex);
	    }
	}

	private void expectPinChangeWithSuccess(String currentPin, String newPin, boolean expected) {
	    try {
		ConfirmTwoPasswordsOperation operation = promisedOperationEnterTwoPasswords.deref(WAIT_TIMEOUT, TimeUnit.MILLISECONDS);
		if (operation == null) {
		    throw new IllegalStateException();
		}
		boolean result = operation.enter(currentPin, newPin);
		Assert.assertEquals(result, expected);
	    } catch (InterruptedException | TimeoutException ex) {
		throw new RuntimeException(ex);
	    }
	}

	public void expectRemovalOfCard() {
	    LOG.debug("Expect removal of card.");
	    try {
		promisedRemoveCard.deref(WAIT_TIMEOUT, TimeUnit.MILLISECONDS);
	    } catch (InterruptedException | TimeoutException ex) {
		throw new RuntimeException(ex);
	    }
	}

	public void expectSuccessfulPinChange() {
	    expectPinChangeWithSuccess("123123", "123123", true);
	}

	public void expectIncorrectPinChangeToFail() {
	    expectPinChangeWithSuccess("123123", "847826", false);
	}

	public void cancelPinManagement() {
	    LOG.debug("Cancel pin management.");
	    this.activationController.cancelAuthentication();
	}

	@Override
	public void close() throws Exception {
	    releasePromise(promisedActivationResult);
	    promisedActivationResult = null;
	    releasePromise(promisedStarted);
	    promisedStarted = null;

	    ActivationController oldActivationController = activationController;
	    if (oldActivationController != null) {
		oldActivationController.cancelAuthentication();
		activationController = null;
	    }
	    if (this._pinManagementFactory != null && oldActivationController != null) {
		this._pinManagementFactory.destroy(activationController);
	    }
	}


    }

    public class ContextWorld implements AutoCloseable {

	private ContextManager _contextManager;

	private ContextManager contextManager() {
	    if (_contextManager == null) {
		_contextManager = activationUtils.context(capabilities.build());
	    }
	    return _contextManager;
	}

	public ContextWorld startSuccessfully() {
	    LOG.debug("Start successfully.");
	    Promise<ServiceErrorResponse> resultStart = new Promise<>();
	    try {
		contextManager().start(PromiseDeliveringFactory.createContextServiceDelivery(resultStart));
	    } catch (UnableToInitialize | NfcUnavailable | NfcDisabled | ApduExtLengthNotSupported ex) {
		throw new RuntimeException(ex);
	    }

	    try {
		Assert.assertNull(resultStart.deref(WAIT_TIMEOUT, TimeUnit.MILLISECONDS));
	    } catch (InterruptedException | TimeoutException ex) {
		throw new RuntimeException(ex);
	    }
	    return this;
	}

	public ContextWorld stopSuccessfully() {
	    LOG.debug("Stop successfully.");
	    Promise<ServiceErrorResponse> resultStart = new Promise<>();
	    contextManager().stop(PromiseDeliveringFactory.createContextServiceDelivery(resultStart));

	    try {
		Assert.assertNull(resultStart.deref(WAIT_TIMEOUT, TimeUnit.MILLISECONDS));
	    } catch (InterruptedException | TimeoutException ex) {
		throw new RuntimeException(ex);
	    }
	    return this;
	}

	@Override
	public void close() throws Exception {
	    if (_contextManager != null) {
		try {
		    stopSuccessfully();
		} catch (Exception | AssertionError ex) {
		    // Suppress all exceptions.
		}
	    }
	}

    }

    private static <T> void releasePromise(Promise<T> promise) {
	if (promise != null) {
	    if (!promise.isCancelled() && !promise.isDelivered()) {
		promise.cancel();
	    }
	}
    }
}
