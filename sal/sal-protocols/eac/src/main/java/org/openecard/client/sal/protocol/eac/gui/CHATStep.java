/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openecard.client.sal.protocol.eac.gui;

import iso.std.iso_iec._24727.tech.schema.CardCall;
import java.util.Map;
import java.util.TreeMap;
import org.openecard.client.common.I18n;
import org.openecard.client.crypto.common.asn1.cvc.CHAT;
import org.openecard.client.crypto.common.asn1.cvc.CardVerifiableCertificate;
import org.openecard.client.crypto.common.asn1.cvc.CertificateDescription;
import org.openecard.client.gui.definition.BoxItem;
import org.openecard.client.gui.definition.Checkbox;
import org.openecard.client.gui.definition.Step;
import org.openecard.client.gui.definition.Text;
import org.openecard.client.gui.definition.ToggleText;


/**
 *
 * @author John
 */
public class CHATStep {

    // GUI translation constants
    private static final String TITLE = "step_chat_title";
    private static final String DESCRIPTION = "step_chat_description";
    private static final String DATA_GROUP_PREFIX = "";
    private static final String SUBJECT_URL = "service_providers_internetaddress";
    private static final String TERMS_OF_USAGE = "service_providers_termsofusage";
    private static final String VALIDITY = "service_providers_validity";
    private static final String VALIDITY_FORMAT = "service_providers_validity";
    private static final String VALIDITY_FROM = "service_providers_validity";
    private static final String VALIDITY_TO = "service_providers_validity";
    private static final String ISSUER_NAME = "service_providers_validity";
    private static final String ISSUER_URL = "service_providers_validity";
    //
    private I18n lang = I18n.getTranslation("sal");
    private Step step = new Step(lang.translationForKey(TITLE));
    private CHAT requiredCHAT, optionalCHAT;
    private GUIContentMap content;
    CardVerifiableCertificate certificate;
    CertificateDescription certificateDescription;

    public CHATStep(GUIContentMap content) {
	this.requiredCHAT = (CHAT) content.get(GUIContentMap.ELEMENT.REQUIRED_CHAT);
	this.optionalCHAT = (CHAT) content.get(GUIContentMap.ELEMENT.OPTIONAL_CHAT);;
	certificate = (CardVerifiableCertificate) content.get(GUIContentMap.ELEMENT.CERTIFICATE);
	certificateDescription = (CertificateDescription) content.get(GUIContentMap.ELEMENT.CERTIFICATE_DESCRIPTION);
    }

    public Step create() {

	String decriptionText = lang.translationForKey(DESCRIPTION);
	decriptionText = decriptionText.replaceFirst("%s", certificateDescription.getIssuerName());

	Text decription = new Text();
	decription.setText(decriptionText);
	step.getInputInfoUnits().add(decription);


	Checkbox readAccessCheckBox = new Checkbox();
	TreeMap<CHAT.DataGroup, Boolean> readAccess = requiredCHAT.getReadAccess();
	System.out.println(readAccess.size());
	for (Map.Entry<CHAT.DataGroup, Boolean> entry : readAccess.entrySet()) {

	    CHAT.DataGroup dataGroup = entry.getKey();
	    Boolean isRequired = entry.getValue();
	    System.out.println(">" + dataGroup.name() + " " + isRequired);
	    if (isRequired) {
		BoxItem item = new BoxItem();
		item.setName(dataGroup.name());
		item.setChecked(true);
		item.setText(lang.translationForKey(DATA_GROUP_PREFIX + dataGroup.name()));

		readAccessCheckBox.getBoxItems().add(item);
	    }
	}
	step.getInputInfoUnits().add(readAccessCheckBox);

	ToggleText requestedDataDescription1 = new ToggleText();
	requestedDataDescription1.setTitle("Hinweis");
	requestedDataDescription1.setText("Die markierten Elemente benötigt der Anbieter zur Durchführung seiner Dienstleistung. Optionale Daten können Sie hinzufügen.");
	requestedDataDescription1.setCollapsed(!true);
	step.getInputInfoUnits().add(requestedDataDescription1);

	return step;
    }
}
