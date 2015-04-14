/****************************************************************************
 * Copyright (C) 2015 ecsec GmbH.
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

package org.openecard.addons.tr03124.gui;

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import java.util.List;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import org.openecard.common.I18n;
import org.openecard.gui.definition.BoxItem;
import org.openecard.gui.definition.Radiobox;
import org.openecard.gui.definition.Step;
import org.openecard.gui.definition.Text;
import org.openecard.recognition.CardRecognition;


/**
 * Step implementation which represents a card selection dialog in case the are multiple valid cards available.
 *
 * @author Hans-Martin Haase
 */
public class CardSelectionStep extends Step {

    private static final String ID = "CredentialSelection";

    private final TreeMap<String, ConnectionHandleType> avCardWithName = new TreeMap<>();
    private final I18n lang = I18n.getTranslation("tr03112");
    private final CardRecognition rec;

    /**
     * Creates a new CardSelectionStep from the given title, the available cards and the card recognition.
     *
     * @param title Title of this step.
     * @param availableCards List of {@link ConnectionHandleType} objects representing the available credentials.
     * @param rec {@link CardRecognition} object used to translate cardTypeNames into human understandable strings.
     */
    public CardSelectionStep(@Nonnull String title, @Nonnull List<ConnectionHandleType> availableCards,
	    @Nonnull CardRecognition rec) {
	super(ID, title);
	setReversible(false);

	for (ConnectionHandleType conHandle : availableCards) {
	    avCardWithName.put(rec.getTranslatedCardName(conHandle.getRecognitionInfo().getCardType()), conHandle);
	}
	this.rec = rec;
	addElements();
    }

    /**
     * Add the UI elements to the step.
     */
    private void addElements() {
	Text description = new Text();
	description.setText(lang.translationForKey("card.selection.message"));
	Radiobox radioBox = new Radiobox("credentialSelectionBox");
	radioBox.setGroupText("Available Credentials");
	for (String cardName : avCardWithName.keySet()) {
	    BoxItem item = new BoxItem();
	    item.setName(avCardWithName.get(cardName).getRecognitionInfo().getCardType());
	    item.setText(cardName);
	    radioBox.getBoxItems().add(item);
	}

	getInputInfoUnits().add(description);
	getInputInfoUnits().add(radioBox);
    }

    /**
     * Update the step with a new list of connection handles.
     *
     * @param availableCards List of available cards represented by connection handles.
     */
    public void update(List<ConnectionHandleType> availableCards) {
	this.avCardWithName.clear();
	for (ConnectionHandleType handle : availableCards) {
	    avCardWithName.put(rec.getTranslatedCardName(handle.getRecognitionInfo().getCardType()), handle);
	}

	CardMonitorTask task = (CardMonitorTask) getBackgroundTask();
	if (task != null) {
	    ConnectionHandleType handle = task.getResult();
	    if (handle.getRecognitionInfo() != null && handle.getRecognitionInfo().getCardType() != null) {
		avCardWithName.put(rec.getTranslatedCardName(handle.getRecognitionInfo().getCardType()), handle);
	    }
	}

	getInputInfoUnits().clear();
	addElements();
    }

}
