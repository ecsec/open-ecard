package org.openecard.client.gui.swing;

import java.awt.Container;
import javax.swing.JDialog;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openecard.client.gui.UserConsentNavigator;
import org.openecard.client.gui.definition.BoxItem;
import org.openecard.client.gui.definition.Checkbox;
import org.openecard.client.gui.definition.Hyperlink;
import org.openecard.client.gui.definition.Passwordfield;
import org.openecard.client.gui.definition.Radiobox;
import org.openecard.client.gui.definition.Step;
import org.openecard.client.gui.definition.Text;
import org.openecard.client.gui.definition.Textfield;
import org.openecard.client.gui.definition.UserConsentDescription;
import org.openecard.client.gui.executor.ExecutionEngine;
import static org.junit.Assert.*;
import org.openecard.client.gui.definition.*;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 * @editor Vladislav Mladenov
 */
public class RunGUI {

    private UserConsentDescription uc;

    @Before
    public void setUp() throws Exception {
        uc = new UserConsentDescription("Identitätsnachweis");
        
        uc.getSteps().add(identityCheckStep());
        uc.getSteps().add(providerInfoStep());
        uc.getSteps().add(reqestedDataStep());
        uc.getSteps().add(pinInputStep());
        uc.getSteps().add(checkDataStep());
        
//        Step identityCheck_ServerConnection_Step = new Step("Anbieterinformationen");
//	uc.getSteps().add(identityCheck_ServerConnection_Step);
//	Text i1 = new Text();
//	i1.setText("Name des Anbieters\n"+
//                "Frauenhofer ");
//                
//	identityCheck_ServerConnection_Step.getInputInfoUnits().add(i1);
//	Hyperlink h1 = new Hyperlink();
//	h1.setHref("http://www.cardinfo.eu");
//	identityCheck_ServerConnection_Step.getInputInfoUnits().add(h1);
//	
//        
//              Textfield t1 = new Textfield();
//	identityCheck_ServerConnection_Step.getInputInfoUnits().add(t1);
//              
//	t1.setName("text input1");
//	t1.setText("Hello World input.");
//	Passwordfield p1 = new Passwordfield();
//	identityCheck_ServerConnection_Step.getInputInfoUnits().add(p1);
//	p1.setName("pass input1");
//	p1.setText("PIN:");

               
              
              	
        
//	Checkbox i2 = new Checkbox();
//	requestedData_Step.getInputInfoUnits().add(i2);
//	BoxItem bi1 = new BoxItem();
//	i2.getBoxItems().add(bi1);
//	bi1.setName("box1");
//	bi1.setChecked(false);
//	bi1.setDisabled(false);
//	bi1.setText("Box 1");
//	BoxItem bi2 = new BoxItem();
//	i2.getBoxItems().add(bi2);
//	bi2.setName("box2");
//	bi2.setChecked(true);
//	bi2.setDisabled(true);
//	bi2.setText("Box 2");
//	// add also to step 1
//	identityCheck_ServerConnection_Step.getInputInfoUnits().add(i2);

//	requestedData_Step.getInputInfoUnits().add(i2);
//	Radiobox i3 = new Radiobox();
//	dataTransaction_Step.getInputInfoUnits().add(i3);
//	BoxItem bi3 = new BoxItem();
//	i3.getBoxItems().add(bi3);
//	bi3.setName("box1");
//	bi3.setChecked(true);
//	bi3.setDisabled(false);
//	bi3.setText("Box 1");
//	BoxItem bi4 = new BoxItem();
//	i3.getBoxItems().add(bi4);
//	bi4.setName("box2");
//	bi4.setChecked(true);
//	bi4.setDisabled(false);
//	bi4.setText("Box 2");
    }

    
    private Step identityCheckStep ()
    {
        Step identityCheck_ServerConnection_Step = new Step ("Start");//("Identitätsnachweis wird gestartet");
        Text serverConnectionText = new Text ();
        serverConnectionText.setText("Verbindung zum Server wird aufgebaut");
        identityCheck_ServerConnection_Step.getInputInfoUnits().add(serverConnectionText);

        return identityCheck_ServerConnection_Step;
    }
    
    private Step providerInfoStep ()
    {
        Step providerInformation_Step = new Step ("Anbieterinformationen");
        Text providerName_Text = new Text ();
        providerName_Text.setText("Name: \nFrauenhofer FOKUS\n\n");
        providerInformation_Step.getInputInfoUnits().add(providerName_Text);
        Text providerAddress_Text = new Text ();
        providerAddress_Text.setText("Adresse:\n"
                + "Kaiserin Augusta 31\n"
                + "D-10589 Berlin\n"
                + "elan-kontakt@fokus.frauenhofer.de");
        providerInformation_Step.getInputInfoUnits().add(providerAddress_Text);
        
        return providerInformation_Step;
    }
    
    private Step reqestedDataStep () throws Exception
    {
        Step requestedData_Step = new Step("Angefragte Daten");
        
        Text requestedDataDescription = new Text();
        requestedDataDescription.setText(""//"Angefragte Daten\n\n"
                + "Für den genannten Zweck bitten wie Sie,\n"
                + "die folgenden Daten aus Ihrem Personalausweis\n"
                + "zu übermitteln:");
        requestedData_Step.getInputInfoUnits().add(requestedDataDescription);
        Hyperlink  dataPrivacyDescriptionLink = new Hyperlink();
        dataPrivacyDescriptionLink.setHref("http://www.dataprivacy.eu");
        requestedData_Step.getInputInfoUnits().add(dataPrivacyDescriptionLink);

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
        doctordegreeBoxItem.setChecked(false);
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
        dataToSendSelection.getBoxItems().add(addressBoxItem);
        dataToSendSelection.getBoxItems().add(birthdayBoxItem);
        dataToSendSelection.getBoxItems().add(birthplaceBoxItem);
        dataToSendSelection.getBoxItems().add(identiycardtypeBoxItem);
        dataToSendSelection.getBoxItems().add(certificationcountryBoxItem);
        dataToSendSelection.getBoxItems().add(habitationBoxItem);
        dataToSendSelection.getBoxItems().add(ageverificationBoxItem);
        requestedData_Step.getInputInfoUnits().add(dataToSendSelection);
//        requestedData_Step.getInputInfoUnits().add(sendAgreement_Text);
//        requestedData_Step.getInputInfoUnits().add(p1);

        return requestedData_Step;
    }
    
    private Step checkDataStep ()
    {
       Step dataTransaction_Step = new Step("Identitätsnachweis");// wird durchgeführt");
//        Checkbox statusMessages_CheckBox = new Checkbox();

        Text requestedPIN_Text = new Text();
        requestedPIN_Text.setText("Eingegebene PIN");
        BoxItem pinCorrekt = new BoxItem();
        pinCorrekt.setName("pinCorrect");
        pinCorrekt.setChecked(true);
        pinCorrekt.setText("OK");
//        statusMessages_CheckBox.getBoxItems().add(pinCorrekt);
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
    
    private Step pinInputStep () throws Exception
    {
        Step requestedData_Step = new Step("PIN Eingabe");
        
/*                Text requestedDataDescription = new Text();
                requestedDataDescription.setText(""//Angefragte Daten\n\n"
                        + "Für den genannten Zweck bitten wie Sie,\n"
                        + "die folgenden Daten aus Ihrem Personalausweis\n"
                        + "zu übermitteln\n"
                        + "eine Zeile Dummy-Text\n"
                        + "eine Zeile Dummy-Text\n"
                        + "eine Zeile Dummy-Text\n"
                        + "eine Zeile Dummy-Text\n"
                        + "eine Zeile Dummy-Text\n"
                        + "eine Zeile Dummy-Text\n");
                requestedData_Step.getInputInfoUnits().add(requestedDataDescription);
                Hyperlink  dataPrivacyDescriptionLink = new Hyperlink();
        dataPrivacyDescriptionLink.setHref("http://www.dataprivacy.eu");
        requestedData_Step.getInputInfoUnits().add(dataPrivacyDescriptionLink);
*/
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

                Text sendAgreement_Text = new Text ();
                sendAgreement_Text.setText("Wenn Sie mit der Übermittlung der ausgewählten\n"
                        + "Daten einverstanden sind, geben Sie bitte\n"
                        + "Ihre 6-stellige PIN ein.");
                Passwordfield p1 = new Passwordfield();
        p1.setName("pass input1");
        p1.setText("PIN:");

                dataToSendSelection.getBoxItems().add(vornameBoxItem);
                dataToSendSelection.getBoxItems().add(nameBoxItem);
                dataToSendSelection.getBoxItems().add(doctordegreeBoxItem);
                dataToSendSelection.getBoxItems().add(addressBoxItem);
                dataToSendSelection.getBoxItems().add(birthdayBoxItem);
                dataToSendSelection.getBoxItems().add(birthplaceBoxItem);
                dataToSendSelection.getBoxItems().add(identiycardtypeBoxItem);
                dataToSendSelection.getBoxItems().add(certificationcountryBoxItem);
                dataToSendSelection.getBoxItems().add(habitationBoxItem);
                dataToSendSelection.getBoxItems().add(ageverificationBoxItem);
                requestedData_Step.getInputInfoUnits().add(dataToSendSelection);
                requestedData_Step.getInputInfoUnits().add(sendAgreement_Text);
                requestedData_Step.getInputInfoUnits().add(p1);
                
                return requestedData_Step;
    }
    
    private class TestDialog implements DialogWrapper {

	private JDialog dialog;

	public TestDialog() {
	    this.dialog = new JDialog();
	    this.dialog.setSize(600, 400);
	    this.dialog.setVisible(false);
	    this.dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
	}


	@Override
	public void setTitle(String title) {
	    dialog.setTitle(title);
	}

	@Override
	public Container getRootPanel() {
	    return dialog.getContentPane();
	}

	@Override
	public void showDialog() {
	    this.dialog.setVisible(true);
	}

	@Override
	public void hideDialog() {
	    this.dialog.setVisible(false);
	}

    }

    /**
     * Uncomment the <code>@Ignore</code> line to run a demo gui so you can debug it.
     */
    //@Ignore
    @Test
    public void runUC() {
	TestDialog dialog = new TestDialog();
	SwingUserConsent ucEngine = new SwingUserConsent(dialog);
	UserConsentNavigator navigator = ucEngine.obtainNavigator(uc);
	ExecutionEngine exec = new ExecutionEngine(navigator);
	exec.process();
    }

}
