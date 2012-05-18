/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openecard.client.sal.protocol.eac.gui;

import java.util.Map;
import java.util.TreeMap;
import org.openecard.client.common.I18n;
import org.openecard.client.crypto.common.asn1.cvc.CHAT;
import org.openecard.client.crypto.common.asn1.cvc.CardVerifiableCertificate;
import org.openecard.client.crypto.common.asn1.cvc.CertificateDescription;
import org.openecard.client.gui.definition.BoxItem;
import org.openecard.client.gui.definition.Checkbox;
import org.openecard.client.gui.definition.OutputInfoUnit;
import org.openecard.client.gui.definition.Step;
import org.openecard.client.gui.definition.Text;
import org.openecard.client.gui.definition.ToggleText;
import org.openecard.client.gui.executor.ExecutionResults;


/**
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class CHATStep {

    // GUI translation constants
    private static final String TITLE = "step_chat_title";
    private static final String DESCRIPTION = "step_chat_description";
    private static final String DATA_GROUP_PREFIX = "";
    //
    private I18n lang = I18n.getTranslation("sal");
    private Step step = new Step(lang.translationForKey(TITLE));
    private CHAT requiredCHAT, optionalCHAT, selectedCHAT;
    private GUIContentMap content;
    CardVerifiableCertificate certificate;
    CertificateDescription certificateDescription;

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
	decriptionText = decriptionText.replaceFirst("%s", certificateDescription.getIssuerName());

	Text decription = new Text();
	decription.setText(decriptionText);
	step.getInputInfoUnits().add(decription);

	Checkbox readAccessCheckBox = new Checkbox();
	TreeMap<CHAT.DataGroup, Boolean> readAccess = requiredCHAT.getReadAccess();
	int i = 0;
	for (Map.Entry<CHAT.DataGroup, Boolean> entry : readAccess.entrySet()) {
	    // filter only the first 8 dgs
	    if (i > 8) {
		break;
	    }
	    CHAT.DataGroup dataGroup = entry.getKey();
	    Boolean isRequired = entry.getValue();
//	    if (isRequired) {
	    BoxItem item = new BoxItem();
	    item.setName(dataGroup.name());
	    item.setChecked(isRequired);
	    item.setText(lang.translationForKey(DATA_GROUP_PREFIX + dataGroup.name()));

	    readAccessCheckBox.getBoxItems().add(item);
//	}
	}
	step.getInputInfoUnits().add(readAccessCheckBox);

	ToggleText requestedDataDescription1 = new ToggleText();
	requestedDataDescription1.setTitle("Hinweis");
	requestedDataDescription1.setText("Die markierten Elemente benötigt der Anbieter zur Durchführung seiner Dienstleistung. Optionale Daten können Sie hinzufügen.");
	requestedDataDescription1.setCollapsed(!true);
	step.getInputInfoUnits().add(requestedDataDescription1);
    }

    public Step getStep() {
	return step;
    }

    public void processResult(Map<String, ExecutionResults> results) {
	processResult(results.get(step.getID()));
    }

    private void processResult(ExecutionResults executionResults) {
	for (OutputInfoUnit output : executionResults.getResults()) {
	    if (output instanceof Checkbox) {
		Checkbox cb = (Checkbox) output;
		for (BoxItem item : cb.getBoxItems()) {
		    selectedCHAT.setReadAccess(item.getName(), item.isChecked());
		}
	    }
	}
    }
}
