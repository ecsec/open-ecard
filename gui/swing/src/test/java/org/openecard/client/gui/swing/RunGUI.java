/****************************************************************************
 * Copyright (C) 2012 Ruhr Uni Bochum.
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

package org.openecard.client.gui.swing;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openecard.client.gui.StepResult;
import org.openecard.client.gui.UserConsentNavigator;
import org.openecard.client.gui.definition.*;
import org.openecard.client.gui.executor.*;
import org.openecard.client.gui.swing.common.GUIDefaults;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 * @editor Vladislav Mladenov
 */
public class RunGUI {

    private UserConsentDescription uc;

    @BeforeTest
    public void setUp() throws Exception {
	uc = new UserConsentDescription("Identitätsnachweis");

	uc.getSteps().add(identityCheckStep());
	uc.getSteps().add(providerInfoStep());
	uc.getSteps().add(reqestedDataStep());
	uc.getSteps().add(pinInputStep());
	uc.getSteps().add(checkDataStep());

	GUIDefaults.initialize();
    }

    public boolean validateColor(final String hex) {
	Pattern pattern = Pattern.compile("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$");
	Matcher matcher = pattern.matcher(hex);
	return matcher.matches();

    }

    private Step identityCheckStep() {
	Step identityCheck_ServerConnection_Step = new Step("Start");//("Identitätsnachweis wird gestartet");
	Text serverConnectionText = new Text();
	serverConnectionText.setText("Verbindung zum Server wird aufgebaut");
	identityCheck_ServerConnection_Step.getInputInfoUnits().add(serverConnectionText);

	ToggleText providerName_Text1 = new ToggleText();
	providerName_Text1.setTitle("Name");
	providerName_Text1.setText("Frauenhofer FOKUS\n\n");
//	identityCheck_ServerConnection_Step.getInputInfoUnits().add(providerName_Text1);

	return identityCheck_ServerConnection_Step;
    }

    private Step providerInfoStep() {
	Step step = new Step("Anbieter");

	Text decription = new Text();
	decription.setText("Zu dem Dienstanbieter und seiner Berechtigung liegen folgende Information vor.");
	step.getInputInfoUnits().add(decription);

	ToggleText name = new ToggleText();
	name.setTitle("Name");
	name.setText("Fraunhofer FOKUS");
	step.getInputInfoUnits().add(name);

	ToggleText url = new ToggleText();
	url.setTitle("Internetadresse");
	url.setText("http://www.fraunhofer.de");
//	url.setCollapsed(true);
	step.getInputInfoUnits().add(url);

	ToggleText termsofUsage = new ToggleText();
	termsofUsage.setTitle("Nutzungsbestimmungen");
	termsofUsage.setText("Anschrift:\nTest-Diensteanbieter\nTest-Strasse 1\n12345 Test-Ort\n\n"
		+ "E-Mail-Adresse:\ninfo@test-diensteanbieter.de\n\n"
		+ "Zweck des Auslesevorgangs:\nEntwicklung und Test von Software\n\n"
		+ "Zuständige Datenschutzbehörde:\nTest-Datenschutzbehörde\nTest-Strasse 1\n12345 Test-Ort");
	termsofUsage.setCollapsed(true);
	step.getInputInfoUnits().add(termsofUsage);

	ToggleText validation = new ToggleText();
	validation.setTitle("Gültigkeit");
	validation.setText("Von 01.01.2012 bis zum 02.01.2012");
	validation.setCollapsed(true);
	step.getInputInfoUnits().add(validation);

	ToggleText subjectName = new ToggleText();
	subjectName.setTitle("Aussteller des Berechtigung");
	subjectName.setText("D-Trust GmbH");
	subjectName.setCollapsed(true);
	step.getInputInfoUnits().add(subjectName);

	ToggleText subjectURL = new ToggleText();
	subjectURL.setTitle("Internetadresse des Ausstellers");
	subjectURL.setText("http://www.dtrust.de");
	subjectURL.setCollapsed(true);
	step.getInputInfoUnits().add(subjectURL);

	return step;
    }
    Step requestedData_Step1 = new Step("Angefragte Daten");


    private Step reqestedDataStep() throws Exception {
	Text requestedDataDescription = new Text();
	requestedDataDescription.setText("Der Anbieter \"Test-Diensteanbieter\"  fordert zum Zweck \"Entwicklung und Test von Software\" die folgenden Daten von Ihnen an:");
	requestedData_Step1.getInputInfoUnits().add(requestedDataDescription);
//	Hyperlink dataPrivacyDescriptionLink = new Hyperlink();
//	dataPrivacyDescriptionLink.setHref("http://www.dataprivacy.eu");
//	requestedData_Step.getInputInfoUnits().add(dataPrivacyDescriptionLink);

	Checkbox dataToSendSelection = new Checkbox();
	BoxItem vornameBoxItem = new BoxItem();
	vornameBoxItem.setName("vornameBoxItem");
	vornameBoxItem.setChecked(true);
	vornameBoxItem.setDisabled(false);
	vornameBoxItem.setText("Vorname");

	BoxItem nameBoxItem = new BoxItem();
	nameBoxItem.setName("nameBoxItem");
	nameBoxItem.setChecked(true);
	nameBoxItem.setDisabled(false);
	nameBoxItem.setText("Name");
	BoxItem doctordegreeBoxItem = new BoxItem();
	doctordegreeBoxItem.setName("doctordegreeBoxItem");
	doctordegreeBoxItem.setChecked(true);
	doctordegreeBoxItem.setDisabled(true);
	doctordegreeBoxItem.setText("Doktorgrad");
	BoxItem addressBoxItem = new BoxItem();
	addressBoxItem.setName("addressBoxItem");
	addressBoxItem.setChecked(true);
	addressBoxItem.setDisabled(false);
	addressBoxItem.setText("Anschrift");
	BoxItem birthdayBoxItem = new BoxItem();
	birthdayBoxItem.setName("birthdayBoxItem");
	birthdayBoxItem.setChecked(false);
	birthdayBoxItem.setDisabled(false);
	birthdayBoxItem.setText("Geburtstag");
	BoxItem birthplaceBoxItem = new BoxItem();
	birthplaceBoxItem.setName("birthplaceBoxItem");
	birthplaceBoxItem.setChecked(false);
	birthplaceBoxItem.setDisabled(false);
	birthplaceBoxItem.setText("Geburtsort");
//        BoxItem pseudonymBoxItem = new BoxItem();
//        pseudonymBoxItem.setName("pseudonymBoxItem");
//        pseudonymBoxItem.setChecked(false);
//        pseudonymBoxItem.setDisabled(true);
//        pseudonymBoxItem.setText("Ordens-oder Künstlername");
	BoxItem identiycardtypeBoxItem = new BoxItem();
	identiycardtypeBoxItem.setName("identiycardtypeBoxItem");
	identiycardtypeBoxItem.setChecked(false);
	identiycardtypeBoxItem.setDisabled(true);
	identiycardtypeBoxItem.setText("Ausweistyp");
	BoxItem certificationcountryBoxItem = new BoxItem();
	certificationcountryBoxItem.setName("certificationcountryBoxItem");
	certificationcountryBoxItem.setChecked(false);
	certificationcountryBoxItem.setDisabled(true);
	certificationcountryBoxItem.setText("Ausstellendes Land");
	BoxItem habitationBoxItem = new BoxItem();
	habitationBoxItem.setName("habitationBoxItem");
	habitationBoxItem.setChecked(false);
	habitationBoxItem.setDisabled(true);
	habitationBoxItem.setText("Wohnort");
	BoxItem ageverificationBoxItem = new BoxItem();
	ageverificationBoxItem.setName("ageverificationBoxItem");
	ageverificationBoxItem.setChecked(false);
	ageverificationBoxItem.setDisabled(true);
//
//        Text sendAgreement_Text = new Text ();
//        sendAgreement_Text.setText("Wenn Sie mit der Übermittlung der ausgewählten Daten einverstanden sind  , geben Sie bitte Ihre 6/stellige PIN ein.");
//        ageverificationBoxItem.setText("Alterverifikation");
//        Passwordfield p1 = new Passwordfield();
//        p1.setName("pass input1");
//        p1.setText("PIN:");

	dataToSendSelection.getBoxItems().add(vornameBoxItem);
	dataToSendSelection.getBoxItems().add(nameBoxItem);
	dataToSendSelection.getBoxItems().add(doctordegreeBoxItem);
//	dataToSendSelection.getBoxItems().add(addressBoxItem);
//	dataToSendSelection.getBoxItems().add(birthdayBoxItem);
//	dataToSendSelection.getBoxItems().add(birthplaceBoxItem);
//	dataToSendSelection.getBoxItems().add(identiycardtypeBoxItem);
//	dataToSendSelection.getBoxItems().add(certificationcountryBoxItem);
//	dataToSendSelection.getBoxItems().add(habitationBoxItem);
//	dataToSendSelection.getBoxItems().add(ageverificationBoxItem);
	requestedData_Step1.getInputInfoUnits().add(dataToSendSelection);

	ToggleText requestedDataDescription1 = new ToggleText();
	requestedDataDescription1.setTitle("Hinweis");
	requestedDataDescription1.setText("Die markierten Elemente benötigt der Anbieter zur Durchführung seiner Dienstleistung. Optionale Daten können Sie hinzufügen.");
	requestedDataDescription1.setCollapsed(!true);
	requestedData_Step1.getInputInfoUnits().add(requestedDataDescription1);

	return requestedData_Step1;
    }

    private Step checkDataStep() {
	Step dataTransaction_Step = new Step("Identitätsnachweis");// wird durchgeführt");
	Text requestedPIN_Text = new Text();
	requestedPIN_Text.setText("Eingegebene PIN");
	BoxItem pinCorrekt = new BoxItem();
	pinCorrekt.setName("pinCorrect");
	pinCorrekt.setChecked(true);
	pinCorrekt.setText("OK");
	dataTransaction_Step.getInputInfoUnits().add(requestedPIN_Text);

	Text cerificate_Text = new Text();
	cerificate_Text.setText("Berechtigungszertifikat");
	BoxItem certificateCorrekt = new BoxItem();
	certificateCorrekt.setName("certificateCorrekt");
	certificateCorrekt.setChecked(true);
	certificateCorrekt.setText("OK");
//        statusMessages_CheckBox.getBoxItems().add(certificateCorrekt);
	dataTransaction_Step.getInputInfoUnits().add(cerificate_Text);

	Text eCard_Text = new Text();
	eCard_Text.setText("Verwendete Karte");
	BoxItem eCardCorrekt = new BoxItem();
	eCardCorrekt.setName("eCardCorrekt");
	eCardCorrekt.setChecked(true);
	eCardCorrekt.setText("OK");
	dataTransaction_Step.getInputInfoUnits().add(eCard_Text);
//        statusMessages_CheckBox.getBoxItems().add(eCardCorrekt);

	Text dataTransaction_Text = new Text();
	dataTransaction_Text.setText("Datenübermittlung wird geprüft");
	BoxItem dataTransactionCorrekt = new BoxItem();
	dataTransactionCorrekt.setName("dataTransactionCorrekt");
	dataTransactionCorrekt.setChecked(true);
	dataTransactionCorrekt.setText("OK");
//        statusMessages_CheckBox.getBoxItems().add(dataTransactionCorrekt);
	dataTransaction_Step.getInputInfoUnits().add(dataTransaction_Text);

//        dataTransaction_Step.getInputInfoUnits().add(statusMessages_CheckBox);

	return dataTransaction_Step;
    }
    Step requestedData_Step = new Step("PIN-Eingabe");

    private Step pinInputStep() throws Exception {
	Text t = new Text();
	t.setText("Durch die Eingabe Ihrer PIN bestätigen Sie, dass folgende markierte Daten an den Anbieter übermittelt werden.");
	requestedData_Step.getInputInfoUnits().add(t);
	Checkbox dataToSendSelection = new Checkbox();
	BoxItem vornameBoxItem = new BoxItem();
	vornameBoxItem.setName("vornameBoxItem");
	vornameBoxItem.setChecked(true);
	vornameBoxItem.setDisabled(true);
	vornameBoxItem.setText("Vorname");
	BoxItem nameBoxItem = new BoxItem();
	nameBoxItem.setName("nameBoxItem");
	nameBoxItem.setChecked(true);
	nameBoxItem.setDisabled(true);
	nameBoxItem.setText("Name");
	BoxItem doctordegreeBoxItem = new BoxItem();
	doctordegreeBoxItem.setName("doctordegreeBoxItem");
	doctordegreeBoxItem.setChecked(false);
	doctordegreeBoxItem.setDisabled(true);
	doctordegreeBoxItem.setText("Doktorgrad");
	BoxItem addressBoxItem = new BoxItem();
	addressBoxItem.setName("addressBoxItem");
	addressBoxItem.setChecked(true);
	addressBoxItem.setDisabled(true);
	addressBoxItem.setText("Anschrift");
	BoxItem birthdayBoxItem = new BoxItem();
	birthdayBoxItem.setName("birthdayBoxItem");
	birthdayBoxItem.setChecked(false);
	birthdayBoxItem.setDisabled(true);
	birthdayBoxItem.setText("Geburtstag");
	BoxItem birthplaceBoxItem = new BoxItem();
	birthplaceBoxItem.setName("birthplaceBoxItem");
	birthplaceBoxItem.setChecked(false);
	birthplaceBoxItem.setDisabled(true);
	birthplaceBoxItem.setText("Geburtsort");
	BoxItem pseudonymBoxItem = new BoxItem();
	pseudonymBoxItem.setName("pseudonymBoxItem");
	pseudonymBoxItem.setChecked(false);
	pseudonymBoxItem.setDisabled(true);
	pseudonymBoxItem.setText("Ordens-oder Künstlername");
	BoxItem identiycardtypeBoxItem = new BoxItem();
	identiycardtypeBoxItem.setName("identiycardtypeBoxItem");
	identiycardtypeBoxItem.setChecked(false);
	identiycardtypeBoxItem.setDisabled(true);
	identiycardtypeBoxItem.setText("Ausweistyp");
	BoxItem certificationcountryBoxItem = new BoxItem();
	certificationcountryBoxItem.setName("certificationcountryBoxItem");
	certificationcountryBoxItem.setChecked(false);
	certificationcountryBoxItem.setDisabled(true);
	certificationcountryBoxItem.setText("Ausstellendes Land");
	BoxItem habitationBoxItem = new BoxItem();
	habitationBoxItem.setName("habitationBoxItem");
	habitationBoxItem.setChecked(false);
	habitationBoxItem.setDisabled(true);
	habitationBoxItem.setText("Wohnort");
	BoxItem ageverificationBoxItem = new BoxItem();
	ageverificationBoxItem.setName("ageverificationBoxItem");
	ageverificationBoxItem.setChecked(false);
	ageverificationBoxItem.setDisabled(true);
	ageverificationBoxItem.setText("Altersverifikation");

	Text sendAgreement_Text = new Text();
	sendAgreement_Text.setText("Wenn Sie mit der Übermittlung der ausgewählten\n"
		+ "Daten einverstanden sind, geben Sie bitte\n"
		+ "Ihre 6-stellige PIN ein.");
	PasswordField p1 = new PasswordField();
	p1.setDescription("pass input1");
	p1.setDescription("PIN:");

	dataToSendSelection.getBoxItems().add(vornameBoxItem);
	dataToSendSelection.getBoxItems().add(nameBoxItem);
//	dataToSendSelection.getBoxItems().add(doctordegreeBoxItem);
//	dataToSendSelection.getBoxItems().add(addressBoxItem);
//	dataToSendSelection.getBoxItems().add(birthdayBoxItem);
//	dataToSendSelection.getBoxItems().add(birthplaceBoxItem);
//	dataToSendSelection.getBoxItems().add(identiycardtypeBoxItem);
//	dataToSendSelection.getBoxItems().add(certificationcountryBoxItem);
//	dataToSendSelection.getBoxItems().add(habitationBoxItem);
//	dataToSendSelection.getBoxItems().add(ageverificationBoxItem);
	requestedData_Step.getInputInfoUnits().add(dataToSendSelection);
//	requestedData_Step.getInputInfoUnits().add(sendAgreement_Text);
	requestedData_Step.getInputInfoUnits().add(p1);

	return requestedData_Step;
    }

    /**
     * Uncomment the
     * <code>@Ignore</code> line to run a demo gui so you can debug it.
     */
    //@Ignore
    @Test
    public void runUC() {
	try {
	    SwingDialogWrapper dialog = new SwingDialogWrapper();
	    SwingUserConsent ucEngine = new SwingUserConsent(dialog);
	    UserConsentNavigator navigator = ucEngine.obtainNavigator(uc);
	    ExecutionEngine exec = new ExecutionEngine(navigator);
	    StepAction sp = new StepAction("Angefragte Daten") {

		@Override
		public StepActionResult perform(Map<String, ExecutionResults> oldResults, StepResult result) {
		    Object[] d = result.getResults().toArray();
		    Checkbox cc = null;
		    for (int i = 0; i < d.length; i++) {
			if (d[i] instanceof Checkbox) {
			    cc = (Checkbox) d[i];
			    System.out.println(cc.getBoxItems());
			}
		    }

		    List<BoxItem> l = cc.getBoxItems();
		    for (BoxItem b : l) {
			System.out.println(b.getName() + " " + b.isChecked());
		    }

		    Object[] data = requestedData_Step.getInputInfoUnits().toArray();
//		    Object[] data = uc.getSteps().get(uc.getSteps().indexOf("PIN-Eingabe"));
		    switch (result.getStatus()) {

			case BACK:

//			    for (int i = 0; i < data.length; i++) {
//				if (data[i] instanceof Checkbox) {
//				    Checkbox c = (Checkbox) data[i];
//				    c.getBoxItems().clear();
//				    c.getBoxItems().addAll(cc.getBoxItems());
//				}
//			    }
			    return new StepActionResult(StepActionResultStatus.BACK);
			case OK:
			    for (int i = 0; i < data.length; i++) {
				if (data[i] instanceof Checkbox) {
				    Checkbox c = (Checkbox) data[i];
				    c.getBoxItems().clear();
				    c.getBoxItems().addAll(cc.getBoxItems());
				}
			    }
			    return new StepActionResult(StepActionResultStatus.NEXT);
			default:
			    return new StepActionResult(StepActionResultStatus.REPEAT);
		    }
		}
	    };
	    StepAction sp1 = new StepAction("PIN-Eingabe") {

		@Override
		public StepActionResult perform(Map<String, ExecutionResults> oldResults, StepResult result) {
//		    Object[] d = null;
//		    for(ExecutionResults e : oldResults.values()){
//			System.out.println(e.getStepName());
//			if(e.getStepName().equals("Angefragte Daten")){
//			    d = e.getResults().toArray();
//			}
//		    }
		    Object[] d = result.getResults().toArray();
		    Checkbox cc = null;
		    for (int i = 0; i < d.length; i++) {
			if (d[i] instanceof Checkbox) {
			    cc = (Checkbox) d[i];
			    System.out.println(cc.getBoxItems());
			}
		    }
		    List<BoxItem> l = cc.getBoxItems();
		    for (BoxItem b : l) {
			System.out.println(b.getName() + " " + b.isChecked());
		    }
//		    Object[] data = requestedData_Step1.getInputInfoUnits().toArray();
		    System.out.println(uc.getSteps().size());
		    Object[] data = uc.getSteps().get(uc.getSteps().indexOf(requestedData_Step1)).getInputInfoUnits().toArray();
		    switch (result.getStatus()) {

			case BACK:

			    for (int i = 0; i < data.length; i++) {
				if (data[i] instanceof Checkbox) {
				    Checkbox c = (Checkbox) data[i];
				    c.getBoxItems().clear();
				    c.getBoxItems().addAll(cc.getBoxItems());
				}
			    }
			    return new StepActionResult(StepActionResultStatus.BACK);
			case OK:
//			    for (int i = 0; i < data.length; i++) {
//				if (data[i] instanceof Checkbox) {
//				    Checkbox c = (Checkbox) data[i];
//				    c.getBoxItems().clear();
//				    c.getBoxItems().addAll(cc.getBoxItems());
//				}
//			    }
			    return new StepActionResult(StepActionResultStatus.NEXT);
			default:
			    return new StepActionResult(StepActionResultStatus.REPEAT);
		    }
		}
	    };

	    exec.addCustomAction(sp);
	    exec.addCustomAction(sp1);
	    exec.process();
	} catch (Throwable w) {
	    w.printStackTrace();
	}
    }

}
