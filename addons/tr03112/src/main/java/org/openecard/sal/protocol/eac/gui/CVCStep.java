/****************************************************************************
 * Copyright (C) 2012-2015 ecsec GmbH.
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

package org.openecard.sal.protocol.eac.gui;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import org.openecard.common.I18n;
import org.openecard.gui.definition.Document;
import org.openecard.gui.definition.Step;
import org.openecard.gui.definition.Text;
import org.openecard.gui.definition.ToggleText;
import org.openecard.sal.protocol.eac.EACData;


/**
 * CVC GUI step for EAC.
 *
 * @author Tobias Wich
 * @author Moritz Horsch
 * @author Hans-Martin Haase
 */
public class CVCStep extends Step {

    private static final I18n LANG = I18n.getTranslation("eac");
    // step id
    public static final String STEP_ID = "PROTOCOL_EAC_GUI_STEP_CVC";
    // GUI translation constants
    private static final String TITLE = "step_cvc_title";
    private static final String STEP_DESCRIPTION = "step_cvc_step_description";
    private static final String DESCRIPTION = "step_cvc_description";
    private static final String SUBJECT_NAME = "cvc_subject_name";
    private static final String SUBJECT_URL = "cvc_subject_url";
    private static final String TERMS_OF_USAGE = "cvc_termsofusage";
    private static final String VALIDITY = "cvc_validity";
    private static final String VALIDITY_FORMAT = "cvc_validity_format";
    private static final String VALIDITY_FROM = "cvc_validity_from";
    private static final String VALIDITY_TO = "cvc_validity_to";
    private static final String ISSUER_NAME = "cvc_issuer_name";
    private static final String ISSUER_URL = "cvc_issuer_url";

    private final EACData eacData;

    public CVCStep(EACData eacData) {
	super(STEP_ID, LANG.translationForKey(TITLE));
	this.eacData = eacData;
	setDescription(LANG.translationForKey(STEP_DESCRIPTION));

	// create step elements
	addElements();
    }

    public static Step createDummy() {
	Step s = new Step(STEP_ID);
	s.setTitle(LANG.translationForKey(TITLE));
	s.setDescription(LANG.translationForKey(STEP_DESCRIPTION));
	return s;
    }

    private void addElements() {
	Text description = new Text();
	description.setText(LANG.translationForKey(DESCRIPTION));
	getInputInfoUnits().add(description);

	// SubjectName
	ToggleText subjectName = new ToggleText();
	subjectName.setID("SubjectName");
	subjectName.setTitle(LANG.translationForKey(SUBJECT_NAME));
	subjectName.setText(eacData.certificateDescription.getSubjectName());
	getInputInfoUnits().add(subjectName);

	// SubjectURL
	ToggleText subjectURL = new ToggleText();
	subjectURL.setID("SubjectURL");
	subjectURL.setTitle(LANG.translationForKey(SUBJECT_URL));
	if (eacData.certificateDescription.getSubjectURL() != null) {
	    subjectURL.setText(eacData.certificateDescription.getSubjectURL());
	} else {
	    subjectURL.setText("");
	}
	getInputInfoUnits().add(subjectURL);

	// TermsOfUsage
	ToggleText termsOfUsage = new ToggleText();
	termsOfUsage.setID("TermsOfUsage");
	termsOfUsage.setTitle(LANG.translationForKey(TERMS_OF_USAGE));
	Document doc = new Document();
	doc.setMimeType(eacData.certificateDescription.getTermsOfUsageMimeType());
	doc.setValue(eacData.certificateDescription.getTermsOfUsageBytes());
	termsOfUsage.setDocument(doc);
	termsOfUsage.setCollapsed(true);
	getInputInfoUnits().add(termsOfUsage);

	// Validity
	DateFormat dateFormat;
	try {
	    dateFormat = new SimpleDateFormat(LANG.translationForKey(VALIDITY_FORMAT));
	} catch (IllegalArgumentException e) {
	    dateFormat = new SimpleDateFormat();
	}
	StringBuilder sb = new StringBuilder(150);
	sb.append(LANG.translationForKey(VALIDITY_FROM));
	sb.append(" ");
	sb.append(dateFormat.format(eacData.certificate.getEffectiveDate().getTime()));
	sb.append(" ");
	sb.append(LANG.translationForKey(VALIDITY_TO));
	sb.append(" ");
	sb.append(dateFormat.format(eacData.certificate.getExpirationDate().getTime()));

	ToggleText validity = new ToggleText();
	validity.setID("Validity");
	validity.setTitle(LANG.translationForKey(VALIDITY));
	validity.setText(sb.toString());
	validity.setCollapsed(true);
	getInputInfoUnits().add(validity);

	// IssuerName
	ToggleText issuerName = new ToggleText();
	issuerName.setID("IssuerName");
	issuerName.setTitle(LANG.translationForKey(ISSUER_NAME));
	issuerName.setText(eacData.certificateDescription.getIssuerName());
	issuerName.setCollapsed(true);
	getInputInfoUnits().add(issuerName);

	// IssuerURL
	ToggleText issuerURL = new ToggleText();
	issuerURL.setID("IssuerURL");
	issuerURL.setTitle(LANG.translationForKey(ISSUER_URL));
	// issuer url is optional so perform a null check
	if (eacData.certificateDescription.getIssuerURL() != null) {
	    issuerURL.setText(eacData.certificateDescription.getIssuerURL());
	} else {
	    issuerURL.setText("");
	}
	issuerURL.setCollapsed(true);
	getInputInfoUnits().add(issuerURL);
    }

}
