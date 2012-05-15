package org.openecard.client.sal.protocol.eac.gui;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import org.openecard.client.common.I18n;
import org.openecard.client.crypto.common.asn1.cvc.CardVerifiableCertificate;
import org.openecard.client.crypto.common.asn1.cvc.CertificateDescription;
import org.openecard.client.gui.definition.Step;
import org.openecard.client.gui.definition.ToggleText;

/**
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class CVCStep {

    // GUI translation constants
    private static final String TITLE = "service_providers_statements_title";
    private static final String SUBJECT_NAME = "service_providers_name";
    private static final String SUBJECT_URL = "service_providers_internetaddress";
    private static final String TERMS_OF_USAGE = "service_providers_termsofusage";
    private static final String VALIDITY = "service_providers_validity";
    private static final String VALIDITY_FORMAT = "service_providers_validity_format";
    private static final String VALIDITY_FROM = "service_providers_validity_from";
    private static final String VALIDITY_TO = "service_providers_validity_to";
    private static final String ISSUER_NAME = "service_providers_validity";
    private static final String ISSUER_URL = "service_providers_validity";
    //
    private I18n lang = I18n.getTranslation("sal");
    private Step step = new Step(lang.translationForKey(TITLE));
    private CertificateDescription description;
    private CardVerifiableCertificate certificate;
    private ContentMap content;

    public CVCStep(ContentMap content) {
	this.content = content;
	this.certificate = (CardVerifiableCertificate) content.get(ContentMap.ELEMENT.CERTIFICATE);
	this.description = (CertificateDescription) content.get(ContentMap.ELEMENT.CERTIFICATEDESCRIPTION);
    }

    public Step create() {
	// SubjectName
	ToggleText subjectName = new ToggleText();
	subjectName.setTitle(SUBJECT_NAME);
	subjectName.setText(description.getSubjectName());
	step.getInputInfoUnits().add(subjectName);

	// SubjectURL
	ToggleText subjectURL = new ToggleText();
	subjectURL.setTitle(SUBJECT_URL);
	subjectURL.setText(description.getSubjectURL());
	step.getInputInfoUnits().add(subjectURL);

	// TermsofUsage
	ToggleText termsOfUsage = new ToggleText();
	termsOfUsage.setTitle(TERMS_OF_USAGE);
	termsOfUsage.setText(description.getTermsOfUsage().toString());
	termsOfUsage.setCollapsed(true);
	step.getInputInfoUnits().add(termsOfUsage);

	// Validity
	DateFormat dateFormat = new SimpleDateFormat(VALIDITY_FORMAT);
	StringBuilder sb = new StringBuilder();
	sb.append(VALIDITY_FROM);
	sb.append(dateFormat.format(certificate.getEffectiveDate()));
	sb.append(VALIDITY_TO);
	sb.append(dateFormat.format(certificate.getExpirationDate()));

	ToggleText validity = new ToggleText();
	validity.setTitle(VALIDITY);
	validity.setText(sb.toString());
	validity.setCollapsed(true);
	step.getInputInfoUnits().add(validity);

	// IssuerName
	ToggleText issuerName = new ToggleText();
	issuerName.setTitle(ISSUER_NAME);
	issuerName.setText(description.getIssuerName());
	issuerName.setCollapsed(true);
	step.getInputInfoUnits().add(issuerName);

	// IssuerURL
	ToggleText issuerURL = new ToggleText();
	issuerURL.setTitle(ISSUER_URL);
	issuerURL.setText(description.getIssuerURL());
	issuerURL.setCollapsed(true);
	step.getInputInfoUnits().add(issuerURL);

	return step;
    }
}
