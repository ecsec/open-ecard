/****************************************************************************
 * Copyright (C) 2012-2019 Ruhr Uni Bochum.
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
package org.openecard.gui.swing

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.gui.ResultStatus
import org.openecard.gui.StepResult
import org.openecard.gui.definition.*
import org.openecard.gui.executor.*
import org.openecard.gui.swing.common.GUIDefaults.initialize
import org.testng.annotations.BeforeTest
import org.testng.annotations.Test
import java.io.IOException
import java.util.regex.Pattern

private val LOG = KotlinLogging.logger { }

/**
 *
 * @author Tobias Wich
 * @author Vladislav Mladenov
 */
class RunGUI {
	private lateinit var uc: UserConsentDescription

	@BeforeTest
	@Throws(Exception::class)
	fun setUp() {
		uc = UserConsentDescription("Identitätsnachweis")

		uc.getSteps().add(identityCheckStep())
		uc.getSteps().add(providerInfoStep())
		val requestedDataStep = requestedDataStep()
		uc.getSteps().add(requestedDataStep)
		uc.getSteps().add(pinInputStep(requestedDataStep))
		uc.getSteps().add(checkDataStep())

		initialize()
	}

	fun validateColor(hex: String): Boolean {
		val pattern = Pattern.compile("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$")
		val matcher = pattern.matcher(hex)
		return matcher.matches()
	}

	private fun identityCheckStep(): Step {
		val identityCheck_ServerConnection_Step = Step("Start") // ("Identitätsnachweis wird gestartet");
		val serverConnectionText = Text()
		serverConnectionText.setText("Verbindung zum Server wird aufgebaut")
		identityCheck_ServerConnection_Step.getInputInfoUnits().add(serverConnectionText)

		val providerName_Text1 = ToggleText()
		providerName_Text1.title = "Name"
		providerName_Text1.setText("Frauenhofer FOKUS\n\n")

		// 	identityCheck_ServerConnection_Step.getInputInfoUnits().add(providerName_Text1);
		return identityCheck_ServerConnection_Step
	}

	@Throws(IOException::class)
	private fun providerInfoStep(): Step {
		val step = Step("Anbieter")

		val decription = Text()
		decription.setText("Zu dem Dienstanbieter und seiner Berechtigung liegen folgende Information vor.")
		step.getInputInfoUnits().add(decription)

		val name = ToggleText()
		name.title = "Name"
		name.setText("Fraunhofer FOKUS")
		step.getInputInfoUnits().add(name)

		val url = ToggleText()
		url.title = "Internetadresse"
		url.setText("http://www.fraunhofer.de")
		// 	url.setCollapsed(true);
		step.getInputInfoUnits().add(url)

		val termsofUsage = ToggleText()
		termsofUsage.title = "Nutzungsbestimmungen"
		termsofUsage.setText(
			(
				"Anschrift:\nTest-Diensteanbieter\nTest-Strasse 1\n12345 Test-Ort\n\n" +
					"E-Mail-Adresse:\ninfo@test-diensteanbieter.de\n\n" +
					"Zweck des Auslesevorgangs:\nEntwicklung und Test von Software\n\n" +
					"Zuständige Datenschutzbehörde:\nTest-Datenschutzbehörde\nTest-Strasse 1\n12345 Test-Ort"
			),
		)
		termsofUsage.isCollapsed = true
		step.getInputInfoUnits().add(termsofUsage)

		val termsofUsageHtml = ToggleText()
		termsofUsageHtml.title = "Nutzungsbestimmungen (HTML)"
		val usageTextHtml = RunGUI::class.java.getResourceAsStream("/description.html")!!.readAllBytes()
		termsofUsageHtml.setDocument(Document("text/html", usageTextHtml))
		termsofUsageHtml.setCollapsed(true)
		step.getInputInfoUnits().add(termsofUsageHtml)

		val termsofUsagePdf = ToggleText()
		termsofUsagePdf.title = "Nutzungsbestimmungen (PDF)"
		val usageTextPdf = RunGUI::class.java.getResourceAsStream("/description.pdf")!!.readAllBytes()
		termsofUsagePdf.document = Document("application/pdf", usageTextPdf)
		termsofUsagePdf.isCollapsed = true
		step.getInputInfoUnits().add(termsofUsagePdf)

		val validation = ToggleText()
		validation.title = "Gültigkeit"
		validation.setText("Von 01.01.2012 bis zum 02.01.2012")
		validation.isCollapsed = true
		step.getInputInfoUnits().add(validation)

		val subjectName = ToggleText()
		subjectName.title = "Aussteller des Berechtigung"
		subjectName.setText("D-Trust GmbH")
		subjectName.isCollapsed = true
		step.getInputInfoUnits().add(subjectName)

		val subjectURL = ToggleText()
		subjectURL.title = "Internetadresse des Ausstellers"
		subjectURL.setText("http://www.dtrust.de")
		subjectURL.isCollapsed = true
		step.getInputInfoUnits().add(subjectURL)

		return step
	}

	@Throws(Exception::class)
	private fun requestedDataStep(): Step {
		val requestedData_Step1 = Step("Angefragte Daten")
		requestedData_Step1.setAction(RequestedDataAction(requestedData_Step1))
		val requestedDataDescription = Text()
		requestedDataDescription.setText(
			"Der Anbieter \"Test-Diensteanbieter\"  fordert zum Zweck \"Entwicklung und Test von Software\" die folgenden Daten von Ihnen an:",
		)
		requestedData_Step1.getInputInfoUnits().add(requestedDataDescription)

		// 	Hyperlink dataPrivacyDescriptionLink = new Hyperlink();
// 	dataPrivacyDescriptionLink.setHref("http://www.dataprivacy.eu");
// 	pinInputStep.getInputInfoUnits().add(dataPrivacyDescriptionLink);
		val dataToSendSelection = Checkbox("c1")
		val vornameBoxItem = BoxItem()
		vornameBoxItem.name = "vornameBoxItem"
		vornameBoxItem.isChecked = true
		vornameBoxItem.isDisabled = false
		vornameBoxItem.text = "Vorname"

		val nameBoxItem = BoxItem()
		nameBoxItem.name = "nameBoxItem"
		nameBoxItem.isChecked = true
		nameBoxItem.isDisabled = false
		nameBoxItem.text = "Name"
		val doctordegreeBoxItem = BoxItem()
		doctordegreeBoxItem.name = "doctordegreeBoxItem"
		doctordegreeBoxItem.isChecked = true
		doctordegreeBoxItem.isDisabled = true
		doctordegreeBoxItem.text = "Doktorgrad"
		val addressBoxItem = BoxItem()
		addressBoxItem.name = "addressBoxItem"
		addressBoxItem.isChecked = true
		addressBoxItem.isDisabled = false
		addressBoxItem.text = "Anschrift"
		val birthdayBoxItem = BoxItem()
		birthdayBoxItem.name = "birthdayBoxItem"
		birthdayBoxItem.isChecked = false
		birthdayBoxItem.isDisabled = false
		birthdayBoxItem.text = "Geburtstag"
		val birthplaceBoxItem = BoxItem()
		birthplaceBoxItem.name = "birthplaceBoxItem"
		birthplaceBoxItem.isChecked = false
		birthplaceBoxItem.isDisabled = false
		birthplaceBoxItem.text = "Geburtsort"
		//        BoxItem pseudonymBoxItem = new BoxItem();
//        pseudonymBoxItem.setName("pseudonymBoxItem");
//        pseudonymBoxItem.setChecked(false);
//        pseudonymBoxItem.setDisabled(true);
//        pseudonymBoxItem.setText("Ordens-oder Künstlername");
		val identiycardtypeBoxItem = BoxItem()
		identiycardtypeBoxItem.name = "identiycardtypeBoxItem"
		identiycardtypeBoxItem.isChecked = false
		identiycardtypeBoxItem.isDisabled = true
		identiycardtypeBoxItem.text = "Ausweistyp"
		val certificationcountryBoxItem = BoxItem()
		certificationcountryBoxItem.name = "certificationcountryBoxItem"
		certificationcountryBoxItem.isChecked = false
		certificationcountryBoxItem.isDisabled = true
		certificationcountryBoxItem.text = "Ausstellendes Land"
		val habitationBoxItem = BoxItem()
		habitationBoxItem.name = "habitationBoxItem"
		habitationBoxItem.isChecked = false
		habitationBoxItem.isDisabled = true
		habitationBoxItem.text = "Wohnort"
		val ageverificationBoxItem = BoxItem()
		ageverificationBoxItem.name = "ageverificationBoxItem"
		ageverificationBoxItem.isChecked = false
		ageverificationBoxItem.isDisabled = true

		//
//        Text sendAgreement_Text = new Text ();
//        sendAgreement_Text.setText("Wenn Sie mit der Übermittlung der ausgewählten Daten einverstanden sind  , geben Sie bitte Ihre 6/stellige PIN ein.");
//        ageverificationBoxItem.setText("Alterverifikation");
//        Passwordfield p1 = new Passwordfield();
//        p1.setName("pass input1");
//        p1.setText("PIN:");
		dataToSendSelection.getBoxItems().add(vornameBoxItem)
		dataToSendSelection.getBoxItems().add(nameBoxItem)
		dataToSendSelection.getBoxItems().add(doctordegreeBoxItem)
		// 	dataToSendSelection.getBoxItems().add(addressBoxItem);
// 	dataToSendSelection.getBoxItems().add(birthdayBoxItem);
// 	dataToSendSelection.getBoxItems().add(birthplaceBoxItem);
// 	dataToSendSelection.getBoxItems().add(identiycardtypeBoxItem);
// 	dataToSendSelection.getBoxItems().add(certificationcountryBoxItem);
// 	dataToSendSelection.getBoxItems().add(habitationBoxItem);
// 	dataToSendSelection.getBoxItems().add(ageverificationBoxItem);
		requestedData_Step1.getInputInfoUnits().add(dataToSendSelection)

		val requestedDataDescription1 = ToggleText()
		requestedDataDescription1.title = "Hinweis"
		requestedDataDescription1.setText(
			"Die markierten Elemente benötigt der Anbieter zur Durchführung seiner Dienstleistung. Optionale Daten können Sie hinzufügen.",
		)
		requestedDataDescription1.isCollapsed = false
		requestedData_Step1.getInputInfoUnits().add(requestedDataDescription1)

		return requestedData_Step1
	}

	private fun checkDataStep(): Step {
		val dataTransaction_Step = Step("Identitätsnachweis") // wird durchgeführt");
		val requestedPIN_Text = Text()
		requestedPIN_Text.setText("Eingegebene PIN")
		val pinCorrekt = BoxItem()
		pinCorrekt.name = "pinCorrect"
		pinCorrekt.isChecked = true
		pinCorrekt.text = "OK"
		dataTransaction_Step.getInputInfoUnits().add(requestedPIN_Text)

		val cerificate_Text = Text()
		cerificate_Text.setText("Berechtigungszertifikat")
		val certificateCorrekt = BoxItem()
		certificateCorrekt.name = "certificateCorrekt"
		certificateCorrekt.isChecked = true
		certificateCorrekt.text = "OK"
		//        statusMessages_CheckBox.getBoxItems().add(certificateCorrekt);
		dataTransaction_Step.getInputInfoUnits().add(cerificate_Text)

		val eCard_Text = Text()
		eCard_Text.setText("Verwendete Karte")
		val eCardCorrekt = BoxItem()
		eCardCorrekt.name = "eCardCorrekt"
		eCardCorrekt.isChecked = true
		eCardCorrekt.text = "OK"
		dataTransaction_Step.getInputInfoUnits().add(eCard_Text)

		//        statusMessages_CheckBox.getBoxItems().add(eCardCorrekt);
		val dataTransaction_Text = Text()
		dataTransaction_Text.setText("Datenübermittlung wird geprüft")
		val dataTransactionCorrekt = BoxItem()
		dataTransactionCorrekt.name = "dataTransactionCorrekt"
		dataTransactionCorrekt.isChecked = true
		dataTransactionCorrekt.text = "OK"
		//        statusMessages_CheckBox.getBoxItems().add(dataTransactionCorrekt);
		dataTransaction_Step.getInputInfoUnits().add(dataTransaction_Text)

		//        dataTransaction_Step.getInputInfoUnits().add(statusMessages_CheckBox);
		return dataTransaction_Step
	}

	@Throws(Exception::class)
	private fun pinInputStep(requestedDataStep: Step): Step {
		val pinInputStep = Step("PIN-Eingabe")
		pinInputStep.setAction(PinInputAction(pinInputStep, requestedDataStep))
		val t = Text()
		t.setText(
			"Durch die Eingabe Ihrer PIN bestätigen Sie, dass folgende markierte Daten an den Anbieter übermittelt werden.",
		)
		pinInputStep.getInputInfoUnits().add(t)
		val dataToSendSelection = Checkbox("c1")
		val vornameBoxItem = BoxItem()
		vornameBoxItem.name = "vornameBoxItem"
		vornameBoxItem.isChecked = true
		vornameBoxItem.isDisabled = true
		vornameBoxItem.text = "Vorname"
		val nameBoxItem = BoxItem()
		nameBoxItem.name = "nameBoxItem"
		nameBoxItem.isChecked = true
		nameBoxItem.isDisabled = true
		nameBoxItem.text = "Name"
		val doctordegreeBoxItem = BoxItem()
		doctordegreeBoxItem.name = "doctordegreeBoxItem"
		doctordegreeBoxItem.isChecked = false
		doctordegreeBoxItem.isDisabled = true
		doctordegreeBoxItem.text = "Doktorgrad"
		val addressBoxItem = BoxItem()
		addressBoxItem.name = "addressBoxItem"
		addressBoxItem.isChecked = true
		addressBoxItem.isDisabled = true
		addressBoxItem.text = "Anschrift"
		val birthdayBoxItem = BoxItem()
		birthdayBoxItem.name = "birthdayBoxItem"
		birthdayBoxItem.isChecked = false
		birthdayBoxItem.isDisabled = true
		birthdayBoxItem.text = "Geburtstag"
		val birthplaceBoxItem = BoxItem()
		birthplaceBoxItem.name = "birthplaceBoxItem"
		birthplaceBoxItem.isChecked = false
		birthplaceBoxItem.isDisabled = true
		birthplaceBoxItem.text = "Geburtsort"
		val pseudonymBoxItem = BoxItem()
		pseudonymBoxItem.name = "pseudonymBoxItem"
		pseudonymBoxItem.isChecked = false
		pseudonymBoxItem.isDisabled = true
		pseudonymBoxItem.text = "Ordens-oder Künstlername"
		val identiycardtypeBoxItem = BoxItem()
		identiycardtypeBoxItem.name = "identiycardtypeBoxItem"
		identiycardtypeBoxItem.isChecked = false
		identiycardtypeBoxItem.isDisabled = true
		identiycardtypeBoxItem.text = "Ausweistyp"
		val certificationcountryBoxItem = BoxItem()
		certificationcountryBoxItem.name = "certificationcountryBoxItem"
		certificationcountryBoxItem.isChecked = false
		certificationcountryBoxItem.isDisabled = true
		certificationcountryBoxItem.text = "Ausstellendes Land"
		val habitationBoxItem = BoxItem()
		habitationBoxItem.name = "habitationBoxItem"
		habitationBoxItem.isChecked = false
		habitationBoxItem.isDisabled = true
		habitationBoxItem.text = "Wohnort"
		val ageverificationBoxItem = BoxItem()
		ageverificationBoxItem.name = "ageverificationBoxItem"
		ageverificationBoxItem.isChecked = false
		ageverificationBoxItem.isDisabled = true
		ageverificationBoxItem.text = "Altersverifikation"

		val sendAgreement_Text = Text()
		sendAgreement_Text.setText(
			(
				"Wenn Sie mit der Übermittlung der ausgewählten\n" +
					"Daten einverstanden sind, geben Sie bitte\n" +
					"Ihre 6-stellige PIN ein."
			),
		)
		val p1 = PasswordField("pf1")
		p1.description = "pass input1"
		p1.description = "PIN:"
		p1.maxLength = 6

		dataToSendSelection.getBoxItems().add(vornameBoxItem)
		dataToSendSelection.getBoxItems().add(nameBoxItem)
		// 	dataToSendSelection.getBoxItems().add(doctordegreeBoxItem);
// 	dataToSendSelection.getBoxItems().add(addressBoxItem);
// 	dataToSendSelection.getBoxItems().add(birthdayBoxItem);
// 	dataToSendSelection.getBoxItems().add(birthplaceBoxItem);
// 	dataToSendSelection.getBoxItems().add(identiycardtypeBoxItem);
// 	dataToSendSelection.getBoxItems().add(certificationcountryBoxItem);
// 	dataToSendSelection.getBoxItems().add(habitationBoxItem);
// 	dataToSendSelection.getBoxItems().add(ageverificationBoxItem);
		pinInputStep.getInputInfoUnits().add(dataToSendSelection)
		// 	pinInputStep.getInputInfoUnits().add(sendAgreement_Text);
		pinInputStep.getInputInfoUnits().add(p1)

		return pinInputStep
	}

	/**
	 * Uncomment the
	 * `@Ignore` line to run a demo gui so you can debug it.
	 */
	@Test(enabled = false)
	fun runUC() {
		try {
			val dialog = SwingDialogWrapper()
			val ucEngine = SwingUserConsent(dialog)
			val navigator = ucEngine.obtainNavigator(uc)
			val exec = ExecutionEngine(navigator)

			exec.process()
		} catch (w: Throwable) {
			LOG.error(w) { "${w.message}" }
		}
	}

	private class RequestedDataAction(
		private val step: Step,
	) : StepAction(
			step,
		) {
		override fun perform(
			oldResults: MutableMap<String?, ExecutionResults?>?,
			result: StepResult,
		): StepActionResult {
			val d = result.getResults().toTypedArray()
			var cc: Checkbox? = null
			for (i in d.indices) {
				if (d[i] is Checkbox) {
					cc = d[i] as Checkbox?
					println(cc!!.getBoxItems())
				}
			}

			val l = cc!!.getBoxItems()
			for (b in l) {
				println(b.name + " " + b.isChecked)
			}

			val data = step.getInputInfoUnits().toTypedArray()
			// 		    Object[] data = uc.getSteps().get(uc.getSteps().indexOf("PIN-Eingabe"));
			when (result.getStatus()) {
				ResultStatus.BACK -> // 			    for (int i = 0; i < data.length; i++) {
// 				if (data[i] instanceof Checkbox) {
// 				    Checkbox c = (Checkbox) data[i];
// 				    c.getBoxItems().clear();
// 				    c.getBoxItems().addAll(cc.getBoxItems());
// 				}
// 			    }
					return StepActionResult(StepActionResultStatus.BACK)

				ResultStatus.OK -> {
					var i = 0
					while (i < data.size) {
						if (data[i] is Checkbox) {
							val c = data[i] as Checkbox
							c.getBoxItems().clear()
							c.getBoxItems().addAll(cc.getBoxItems())
						}
						i++
					}
					return StepActionResult(StepActionResultStatus.NEXT)
				}

				else -> return StepActionResult(StepActionResultStatus.REPEAT)
			}
		}
	}

	private class PinInputAction(
		step: Step,
		private val requestedData_Step1: Step,
	) : StepAction(step) {
		override fun perform(
			oldResults: MutableMap<String?, ExecutionResults?>?,
			result: StepResult,
		): StepActionResult {
// 		    Object[] d = null;
// 		    for(ExecutionResults e : oldResults.values()){
// 			System.out.println(e.getStepName());
// 			if(e.getStepName().equals("Angefragte Daten")){
// 			    d = e.getResults().toArray();
// 			}
// 		    }
			val d = result.getResults().toTypedArray()
			var cc: Checkbox? = null
			for (i in d.indices) {
				if (d[i] is Checkbox) {
					cc = d[i] as Checkbox?
					println(cc!!.getBoxItems())
				}
			}
			val l = cc!!.getBoxItems()
			for (b in l) {
				println(b.name + " " + b.isChecked)
			}
			// 		    Object[] data = requestedData_Step1.getInputInfoUnits().toArray();
			val data = requestedData_Step1.getInputInfoUnits().toTypedArray()
			when (result.getStatus()) {
				ResultStatus.BACK -> {
					var i = 0
					while (i < data.size) {
						if (data[i] is Checkbox) {
							val c = data[i] as Checkbox
							c.getBoxItems().clear()
							c.getBoxItems().addAll(cc.getBoxItems())
						}
						i++
					}
					return StepActionResult(StepActionResultStatus.BACK)
				}

				ResultStatus.OK -> // 			    for (int i = 0; i < data.length; i++) {
// 				if (data[i] instanceof Checkbox) {
// 				    Checkbox c = (Checkbox) data[i];
// 				    c.getBoxItems().clear();
// 				    c.getBoxItems().addAll(cc.getBoxItems());
// 				}
// 			    }
					return StepActionResult(StepActionResultStatus.NEXT)

				else -> return StepActionResult(StepActionResultStatus.REPEAT)
			}
		}
	}
}
