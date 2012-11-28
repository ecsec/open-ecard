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

package org.openecard.ifd.scio;

import iso.std.iso_iec._24727.tech.schema.PinInputType;
import java.util.Map;
import org.openecard.gui.StepResult;
import org.openecard.gui.executor.ExecutionResults;
import org.openecard.gui.executor.StepAction;
import org.openecard.gui.executor.StepActionResult;
import org.openecard.gui.executor.StepActionResultStatus;
import org.openecard.ifd.scio.reader.PCSCFeatures;
import org.openecard.ifd.scio.reader.PCSCPinVerify;
import org.openecard.ifd.scio.wrapper.SCTerminal;


/**
 * Action to perform a native pin verify in the GUI executor.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class NativePinStepAction extends StepAction {

    public IFDException exception = null;
    public byte[] response = null;
    private final PinInputType pinInput;
    private final SCTerminal term;
    private final byte[] template;

    public NativePinStepAction(String stepName, PinInputType pinInput, SCTerminal term, byte[] template) {
	super(stepName);
	this.pinInput = pinInput;
	this.term = term;
	this.template = template;
    }

    @Override
    public StepActionResult perform(Map<String, ExecutionResults> oldResults, StepResult result) {
	try {
	    response = nativePinVerify(pinInput, term, template);
	} catch (IFDException ex) {
	    exception = ex;
	}
	return new StepActionResult(StepActionResultStatus.NEXT);
    }

    private static byte[] nativePinVerify(PinInputType pinInput, SCTerminal term, byte[] template) throws IFDException {
	// get data for verify command and perform it
	PCSCPinVerify verifyStruct = new PCSCPinVerify(pinInput.getPasswordAttributes(), template);
	byte[] verifyStructData = verifyStruct.toBytes();
	byte[] result = term.executeCtrlCode(PCSCFeatures.VERIFY_PIN_DIRECT, verifyStructData);
	return result;
    }

}
