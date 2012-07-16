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

package org.openecard.client.sal.protocol.eac.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.openecard.client.common.I18n;
import org.openecard.client.crypto.common.asn1.cvc.CHAT;
import org.openecard.client.crypto.common.asn1.cvc.CHAT.DataGroup;
import org.openecard.client.crypto.common.asn1.cvc.CHAT.SpecialFunction;
import org.openecard.client.crypto.common.asn1.cvc.CardVerifiableCertificate;
import org.openecard.client.crypto.common.asn1.cvc.CertificateDescription;
import org.openecard.client.gui.definition.*;
import org.openecard.client.gui.executor.ExecutionResults;


/**
 * Implements a GUI user consent step for the CHAT.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class CHATStep {

    // GUI translation constants
    private static final String TITLE = "step_chat_title";
    private static final String DESCRIPTION = "step_chat_description";
    private static final String CHAT_BOXES = "readAccessCheckBox";

    private I18n lang = I18n.getTranslation("sal");
    private Step step = new Step(lang.translationForKey(TITLE));
    private CHAT requiredCHAT, optionalCHAT, selectedCHAT;
    private GUIContentMap content;
    private CardVerifiableCertificate certificate;
    private CertificateDescription certificateDescription;

    /**
     * Creates a new GUI user consent step for the CHAT.
     *
     * @param content GUI content
     */
    public CHATStep(GUIContentMap content) {
	this.requiredCHAT = (CHAT) content.get(GUIContentMap.ELEMENT.REQUIRED_CHAT);
	this.selectedCHAT = (CHAT) content.get(GUIContentMap.ELEMENT.REQUIRED_CHAT);
	this.optionalCHAT = (CHAT) content.get(GUIContentMap.ELEMENT.OPTIONAL_CHAT);

	certificate = (CardVerifiableCertificate) content.get(GUIContentMap.ELEMENT.CERTIFICATE);
	certificateDescription = (CertificateDescription) content.get(GUIContentMap.ELEMENT.CERTIFICATE_DESCRIPTION);
	initialize();
    }

    private void initialize() {
	String decriptionText = lang.translationForKey(DESCRIPTION);
	decriptionText = decriptionText.replaceFirst("%s", certificateDescription.getSubjectName());

	Text decription = new Text();
	decription.setText(decriptionText);
	step.getInputInfoUnits().add(decription);

	Checkbox readAccessCheckBox = new Checkbox(CHAT_BOXES);
	TreeMap<CHAT.DataGroup, Boolean> requiredReadAccess = requiredCHAT.getReadAccess();
	TreeMap<CHAT.DataGroup, Boolean> optionalReadAccess = optionalCHAT.getReadAccess();
	TreeMap<SpecialFunction, Boolean> requiredSpecialFunctions = requiredCHAT.getSpecialFunctions();
	TreeMap<SpecialFunction, Boolean> optionalSpecialFunctions = optionalCHAT.getSpecialFunctions();

	DataGroup[] dataGroups = DataGroup.values();
	SpecialFunction[] specialFunctions = SpecialFunction.values();

	// iterate over all 21 eID application data groups
	for (int i = 0; i < 21; i++) {
	    DataGroup dataGroup = dataGroups[i];
	    if (requiredReadAccess.get(dataGroup)) {
		readAccessCheckBox.getBoxItems().add(makeRequiredBoxItem(dataGroup));
	    } else if (optionalReadAccess.get(dataGroup)) {
		readAccessCheckBox.getBoxItems().add(makeOptionalBoxItem(dataGroup));
	    }
	}

	// iterate over all 8 special functions
	for (int i = 0; i < 8; i++) {
	    SpecialFunction specialFunction = specialFunctions[i];
	    if (requiredSpecialFunctions.get(specialFunction)) {
		readAccessCheckBox.getBoxItems().add(makeRequiredBoxItem(specialFunction));
	    } else if (optionalSpecialFunctions.get(specialFunction)) {
		readAccessCheckBox.getBoxItems().add(makeOptionalBoxItem(specialFunction));
	    }
	}

	step.getInputInfoUnits().add(readAccessCheckBox);

	// TODO: check required and optional CHAT against certificate
	// TODO: internationalize the following toggletext
	ToggleText requestedDataDescription1 = new ToggleText();
	requestedDataDescription1.setTitle("Hinweis");
	requestedDataDescription1.setText("Die markierten Elemente benötigt der Anbieter zur Durchführung seiner Dienstleistung. Optionale Daten können Sie hinzufügen.");
	requestedDataDescription1.setCollapsed(!true);
	step.getInputInfoUnits().add(requestedDataDescription1);
    }

    /**
     * Constructs a BoxItem for a <b>optional</b> data group or special function and returns it.</br>
     * It will not be checked and is not disabled.
     *
     * @param value data group or special function to construct the BoxItem for
     * @return constructed BoxItem
     */
    private BoxItem makeOptionalBoxItem(Enum<?> value) {
	BoxItem item = new BoxItem();
	item.setName(value.name());
	item.setChecked(false);
	item.setDisabled(false);
	item.setText(lang.translationForKey(value.name()));
	return item;
    }

    /**
     * Constructs a BoxItem for a <b>required</b> data group or special function and returns it.</br>
     * It will be checked and disabled.
     *
     * @param value data group or special function to construct the BoxItem for
     * @return constructed BoxItem
     */
    private BoxItem makeRequiredBoxItem(Enum<?> value) {
	BoxItem item = new BoxItem();
	item.setName(value.name());
	item.setChecked(true);
	item.setDisabled(true);
	item.setText(lang.translationForKey(value.name()));
	return item;
    }

    /**
     * Returns the generated step.
     *
     * @return Step
     */
    public Step getStep() {
	return step;
    }

    /**
     * Processes the results of step.
     *
     * @param results Results
     */
    public void processResult(Map<String, ExecutionResults> results) {
	List<String> dataGroupsNames = getDataGroupNames();
	List<String> specialFunctionsNames = getSpecialFunctionNames();
	ExecutionResults executionResults = results.get(step.getID());

	if (executionResults == null) {
	    return;
	}

	Checkbox cb = (Checkbox) executionResults.getResult(CHAT_BOXES);
	for (BoxItem item : cb.getBoxItems()) {
	    if (dataGroupsNames.contains(item.getName())) {
		selectedCHAT.setReadAccess(item.getName(), item.isChecked());
	    } else if (specialFunctionsNames.contains(item.getName())) {
		selectedCHAT.setSpecialFunction(item.getName(), item.isChecked());
	    }
	}
    }

    /**
     * Returns a list containing the names of all special functions.
     * @return list containing the names of all special functions.
     */
    private List<String> getSpecialFunctionNames() {
	List<String> specialFunctionNames = new ArrayList<String>();
	for (SpecialFunction dg : SpecialFunction.values()) {
	    specialFunctionNames.add(dg.name());
	}
	return specialFunctionNames;
    }

    /**
     * Returns a list containing the names of all data groups.
     * @return list containing the names of all data groups.
     */
    private List<String> getDataGroupNames() {
	List<String> dataGroupNames = new ArrayList<String>();
	for (DataGroup dg : DataGroup.values()) {
	    dataGroupNames.add(dg.name());
	}
	return dataGroupNames;
    }

}
