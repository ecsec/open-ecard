package org.openecard.client.sal.protocol.eac.gui;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map;
import org.openecard.client.common.I18n;
import org.openecard.client.crypto.common.asn1.cvc.CardVerifiableCertificate;
import org.openecard.client.crypto.common.asn1.cvc.CertificateDescription;
import org.openecard.client.gui.definition.OutputInfoUnit;
import org.openecard.client.gui.definition.PasswordField;
import org.openecard.client.gui.definition.Step;
import org.openecard.client.gui.definition.Text;
import org.openecard.client.gui.definition.ToggleText;
import org.openecard.client.gui.executor.ExecutionResults;


/**
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class CVCStep {

    // GUI translation constants
    private static final String TITLE = "step_cvc_title";
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
    //
    private I18n lang = I18n.getTranslation("sal");
    private Step step = new Step(lang.translationForKey(TITLE));
    private CertificateDescription description;
    private CardVerifiableCertificate certificate;
    private GUIContentMap content;

    public CVCStep(GUIContentMap content) {
	this.content = content;
	this.certificate = (CardVerifiableCertificate) content.get(GUIContentMap.ELEMENT.CERTIFICATE);
	this.description = (CertificateDescription) content.get(GUIContentMap.ELEMENT.CERTIFICATE_DESCRIPTION);
    }

    public Step create() {
	Text decription = new Text();
	decription.setText(lang.translationForKey(DESCRIPTION));
	step.getInputInfoUnits().add(decription);

	// SubjectName
	ToggleText subjectName = new ToggleText();
	subjectName.setTitle(lang.translationForKey(SUBJECT_NAME));
	subjectName.setText(description.getSubjectName());
	step.getInputInfoUnits().add(subjectName);

	// SubjectURL
	ToggleText subjectURL = new ToggleText();
	subjectURL.setTitle(lang.translationForKey(SUBJECT_URL));
	subjectURL.setText(description.getSubjectURL());
	step.getInputInfoUnits().add(subjectURL);

	// TermsofUsage
	ToggleText termsOfUsage = new ToggleText();
	termsOfUsage.setTitle(lang.translationForKey(TERMS_OF_USAGE));
	termsOfUsage.setText(description.getTermsOfUsage().toString());
	termsOfUsage.setCollapsed(true);
	step.getInputInfoUnits().add(termsOfUsage);

	// Validity
	DateFormat dateFormat;
	try {
	    dateFormat = new SimpleDateFormat(lang.translationForKey(VALIDITY_FORMAT));
	} catch (Exception e) {
	    dateFormat = new SimpleDateFormat();
	}
	StringBuilder sb = new StringBuilder();
	sb.append(lang.translationForKey(VALIDITY_FROM));
	sb.append(" ");
	sb.append(dateFormat.format(certificate.getEffectiveDate().getTime()));
	sb.append(" ");
	sb.append(lang.translationForKey(VALIDITY_TO));
	sb.append(" ");
	sb.append(dateFormat.format(certificate.getExpirationDate().getTime()));

	ToggleText validity = new ToggleText();
	validity.setTitle(lang.translationForKey(VALIDITY));
	validity.setText(sb.toString());
	validity.setCollapsed(true);
	step.getInputInfoUnits().add(validity);

	// IssuerName
	ToggleText issuerName = new ToggleText();
	issuerName.setTitle(lang.translationForKey(ISSUER_NAME));
	issuerName.setText(description.getIssuerName());
	issuerName.setCollapsed(true);
	step.getInputInfoUnits().add(issuerName);

	// IssuerURL
	ToggleText issuerURL = new ToggleText();
	issuerURL.setTitle(lang.translationForKey(ISSUER_URL));
	issuerURL.setText(description.getIssuerURL());
	issuerURL.setCollapsed(true);
	step.getInputInfoUnits().add(issuerURL);

	return step;
    }

    public void processResult(Map<String, ExecutionResults> results) {
	processResult(results.get(step.getID()));
    }

    private void processResult(ExecutionResults executionResults) {
	//NOP
    }
}
