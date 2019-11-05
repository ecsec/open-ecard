/****************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ***************************************************************************/

package org.openecard.mobile.ui;

import java.util.ArrayList;
import java.util.List;
import org.openecard.gui.definition.Checkbox;
import org.openecard.gui.definition.Document;
import org.openecard.gui.definition.InputInfoUnit;
import org.openecard.gui.definition.OutputInfoUnit;
import org.openecard.gui.definition.Step;
import org.openecard.gui.definition.ToggleText;
import org.openecard.mobile.activation.BoxItem;
import org.openecard.mobile.activation.ServerData;
import org.openecard.mobile.activation.TermsOfUsage;


/**
 *
 * @author Tobias Wich
 */
class ServerDataImpl implements ServerData {

    private String subject;
    private String issuer;
    private String subjectUrl;
    private String issuerUrl;
    private String validity;
    private TermsOfUsage termsOfUsage;
    private List<BoxItem> readAccessAttributes;
    private List<BoxItem> writeAccessAttributes;

    private Checkbox readBox;
    private Checkbox writeBox;

    public ServerDataImpl(Step cvcStep, Step chatStep) {
	loadValuesFromCVCStep(cvcStep);
	loadValuesFromChatStep(chatStep);
    }


    @Override
    public String getSubject() {
	return subject;
    }

    @Override
    public String getSubjectUrl() {
	return subjectUrl;
    }

    @Override
    public String getIssuer() {
	return issuer;
    }

    @Override
    public String getIssuerUrl() {
	return issuerUrl;
    }

    @Override
    public String getValidity() {
	return validity;
    }

    @Override
    public TermsOfUsage getTermsOfUsage() {
	return termsOfUsage;
    }

    @Override
    public List<BoxItem> getReadAccessAttributes() {
	return new ArrayList<>(readAccessAttributes);
    }

    @Override
    public List<BoxItem> getWriteAccessAttributes() {
	return new ArrayList<>(writeAccessAttributes);
    }

    private void loadValuesFromCVCStep(Step step1) {
	for (InputInfoUnit next : step1.getInputInfoUnits()) {
	    if ("SubjectName".equals(next.getID()) && next instanceof ToggleText) {
		ToggleText tt = (ToggleText) next;
		subject = tt.getText();
	    } else if ("SubjectURL".equals(next.getID()) && next instanceof ToggleText) {
		ToggleText tt = (ToggleText) next;
		subjectUrl = tt.getText();
	    } else if ("TermsOfUsage".equals(next.getID()) && next instanceof ToggleText) {
		ToggleText tt = (ToggleText) next;
		Document d = tt.getDocument();
		termsOfUsage = new TermsOfUsageImpl(d.getMimeType(), d.getValue());
	    } else if ("Validity".equals(next.getID()) && next instanceof ToggleText) {
		ToggleText tt = (ToggleText) next;
		validity = tt.getText();
	    } else if ("IssuerName".equals(next.getID()) && next instanceof ToggleText) {
		ToggleText tt = (ToggleText) next;
		issuer = tt.getText();
	    } else if ("IssuerURL".equals(next.getID()) && next instanceof ToggleText) {
		ToggleText tt = (ToggleText) next;
		issuerUrl = tt.getText();
	    }
	}

	ArrayList<BoxItem> readAccess = new ArrayList<>();
	ArrayList<BoxItem> writeAccess = new ArrayList<>();
    }

    private void loadValuesFromChatStep(Step step2) {
	readAccessAttributes = new ArrayList<>();
	writeAccessAttributes = new ArrayList<>();

	for (InputInfoUnit next : step2.getInputInfoUnits()) {
	    if ("ReadCHATCheckBoxes".equals(next.getID()) && next instanceof Checkbox) {
		Checkbox cb = (Checkbox) next;
		readBox = cb;
		for (org.openecard.gui.definition.BoxItem nb : cb.getBoxItems()) {
		    BoxItem bi = new BoxItemImpl(nb.getName(), nb.isChecked(), nb.isDisabled(), nb.getText());
		    readAccessAttributes.add(bi);
		}
	    } else if ("WriteCHATCheckBoxes".equals(next.getID()) && next instanceof Checkbox) {
		Checkbox cb = (Checkbox) next;
		writeBox = cb;
		for (org.openecard.gui.definition.BoxItem nb : cb.getBoxItems()) {
		    BoxItem bi = new BoxItemImpl(nb.getName(), nb.isChecked(), nb.isDisabled(), nb.getText());
		    writeAccessAttributes.add(bi);
		}
	    }
	}
    }

    public List<OutputInfoUnit> getSelection(List<BoxItem> itemsRead, List<BoxItem> itemsWrite) {
	List<OutputInfoUnit> outInfos = new ArrayList<>();

	copyBox(outInfos, readBox, itemsRead);
	copyBox(outInfos, writeBox, itemsWrite);

	return outInfos;
    }

    private void copyBox(List<OutputInfoUnit> outInfos, Checkbox oldBox, List<BoxItem> items) {
	if (oldBox != null) {
	    // create copy of the checkbox
	    Checkbox newBox = new Checkbox(oldBox.getID());
	    newBox.copyContentFrom(oldBox);

	    // copy changed values
	    for (org.openecard.gui.definition.BoxItem next : newBox.getBoxItems()) {
		String name = next.getName();
		BoxItem receivedItem = getItem(name, items);
		if (receivedItem != null) {
		    next.setChecked(receivedItem.isChecked());
		}
	    }

	    outInfos.add(newBox);
	}
    }

    private BoxItem getItem(String name, List<BoxItem> items) {
	for (BoxItem next : items) {
	    if (name.equals(next.getName())) {
		return next;
	    }
	}
	return null;
    }

}
