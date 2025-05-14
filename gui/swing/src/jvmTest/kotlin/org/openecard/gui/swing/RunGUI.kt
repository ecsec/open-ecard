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
import org.openecard.gui.definition.BoxItem
import org.openecard.gui.definition.Checkbox
import org.openecard.gui.definition.Document
import org.openecard.gui.definition.PasswordField
import org.openecard.gui.definition.Step
import org.openecard.gui.definition.Text
import org.openecard.gui.definition.ToggleText
import org.openecard.gui.definition.UserConsentDescription
import org.openecard.gui.executor.ExecutionEngine
import org.openecard.gui.executor.ExecutionResults
import org.openecard.gui.executor.StepAction
import org.openecard.gui.executor.StepActionResult
import org.openecard.gui.executor.StepActionResultStatus
import org.openecard.gui.results
import org.openecard.gui.status
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

		uc.steps.add(identityCheckStep())
		uc.steps.add(providerInfoStep())
		val requestedDataStep = requestedDataStep()
		uc.steps.add(requestedDataStep)
		uc.steps.add(pinInputStep(requestedDataStep))
		uc.steps.add(checkDataStep())

		initialize()
	}

	fun validateColor(hex: String): Boolean {
		val pattern = Pattern.compile("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$")
		val matcher = pattern.matcher(hex)
		return matcher.matches()
	}

	private fun identityCheckStep(): Step {
		val identityCheckServerConnectionStep = Step("Start") // ("Identitätsnachweis wird gestartet");
		val serverConnectionText = Text()
		serverConnectionText.text = "Verbindung zum Server wird aufgebaut"
		identityCheckServerConnectionStep.inputInfoUnits.add(serverConnectionText)

		val providerNameText1 = ToggleText()
		providerNameText1.title = "Name"
		providerNameText1.text = "Frauenhofer FOKUS\n\n"

		// 	identityCheck_ServerConnection_Step.inputInfoUnits.add(providerName_Text1);
		return identityCheckServerConnectionStep
	}

	@Throws(IOException::class)
	private fun providerInfoStep(): Step {
		val step = Step("Anbieter")

		val decription = Text()
		decription.text = "Zu dem Dienstanbieter und seiner Berechtigung liegen folgende Information vor."
		step.inputInfoUnits.add(decription)

		val name = ToggleText()
		name.title = "Name"
		name.text = "Fraunhofer FOKUS"
		step.inputInfoUnits.add(name)

		val url = ToggleText()
		url.title = "Internetadresse"
		url.text = "http://www.fraunhofer.de"
		// 	url.setCollapsed(true);
		step.inputInfoUnits.add(url)

		val termsofUsage = ToggleText()
		termsofUsage.title = "Nutzungsbestimmungen"
		termsofUsage.text =
			(
				"Anschrift:\nTest-Diensteanbieter\nTest-Strasse 1\n12345 Test-Ort\n\n" +
					"E-Mail-Adresse:\ninfo@test-diensteanbieter.de\n\n" +
					"Zweck des Auslesevorgangs:\nEntwicklung und Test von Software\n\n" +
					"Zuständige Datenschutzbehörde:\nTest-Datenschutzbehörde\nTest-Strasse 1\n12345 Test-Ort"
			)

		termsofUsage.isCollapsed = true
		step.inputInfoUnits.add(termsofUsage)

		val termsofUsageHtml = ToggleText()
		termsofUsageHtml.title = "Nutzungsbestimmungen (HTML)"
		val usageTextHtml = RunGUI::class.java.getResourceAsStream("/description.html")!!.readAllBytes()
		termsofUsageHtml.document = Document("text/html", usageTextHtml)
		termsofUsageHtml.isCollapsed = true
		step.inputInfoUnits.add(termsofUsageHtml)

		val termsofUsagePdf = ToggleText()
		termsofUsagePdf.title = "Nutzungsbestimmungen (PDF)"
		val usageTextPdf = RunGUI::class.java.getResourceAsStream("/description.pdf")!!.readAllBytes()
		termsofUsagePdf.document = Document("application/pdf", usageTextPdf)
		termsofUsagePdf.isCollapsed = true
		step.inputInfoUnits.add(termsofUsagePdf)

		val validation = ToggleText()
		validation.title = "Gültigkeit"
		validation.text = "Von 01.01.2012 bis zum 02.01.2012"
		validation.isCollapsed = true
		step.inputInfoUnits.add(validation)

		val subjectName = ToggleText()
		subjectName.title = "Aussteller des Berechtigung"
		subjectName.text = "D-Trust GmbH"
		subjectName.isCollapsed = true
		step.inputInfoUnits.add(subjectName)

		val subjectURL = ToggleText()
		subjectURL.title = "Internetadresse des Ausstellers"
		subjectURL.text = "http://www.dtrust.de"
		subjectURL.isCollapsed = true
		step.inputInfoUnits.add(subjectURL)

		return step
	}

	@Throws(Exception::class)
	private fun requestedDataStep(): Step {
		val requestedDataStep1 = Step("Angefragte Daten")
		requestedDataStep1.action = RequestedDataAction(requestedDataStep1)
		val requestedDataDescription = Text()
		requestedDataDescription.text =
			"Der Anbieter \"Test-Diensteanbieter\"  fordert zum Zweck \"Entwicklung und Test von Software\" die folgenden Daten von Ihnen an:"
		requestedDataStep1.inputInfoUnits.add(requestedDataDescription)

		// 	Hyperlink dataPrivacyDescriptionLink = new Hyperlink();
// 	dataPrivacyDescriptionLink.setHref("http://www.dataprivacy.eu");
// 	pinInputStep.inputInfoUnits.add(dataPrivacyDescriptionLink);
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
//        pseudonymBoxItem.text = "Ordens-oder Künstlername";
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
//        sendAgreement_Text.text = "Wenn Sie mit der Übermittlung der ausgewählten Daten einverstanden sind  , geben Sie bitte Ihre 6/stellige PIN ein.";
//        ageverificationBoxItem.text = "Alterverifikation";
//        Passwordfield p1 = new Passwordfield();
//        p1.setName("pass input1");
//        p1.text = "PIN:";
		dataToSendSelection.boxItems.add(vornameBoxItem)
		dataToSendSelection.boxItems.add(nameBoxItem)
		dataToSendSelection.boxItems.add(doctordegreeBoxItem)
		// 	dataToSendSelection.boxItems.add(addressBoxItem);
// 	dataToSendSelection.boxItems.add(birthdayBoxItem);
// 	dataToSendSelection.boxItems.add(birthplaceBoxItem);
// 	dataToSendSelection.boxItems.add(identiycardtypeBoxItem);
// 	dataToSendSelection.boxItems.add(certificationcountryBoxItem);
// 	dataToSendSelection.boxItems.add(habitationBoxItem);
// 	dataToSendSelection.boxItems.add(ageverificationBoxItem);
		requestedDataStep1.inputInfoUnits.add(dataToSendSelection)

		val requestedDataDescription1 = ToggleText()
		requestedDataDescription1.title = "Hinweis"
		requestedDataDescription1.text =
			"Die markierten Elemente benötigt der Anbieter zur Durchführung seiner Dienstleistung. Optionale Daten können Sie hinzufügen."
		requestedDataDescription1.isCollapsed = false
		requestedDataStep1.inputInfoUnits.add(requestedDataDescription1)

		return requestedDataStep1
	}

	private fun checkDataStep(): Step {
		val dataTransactionStep = Step("Identitätsnachweis") // wird durchgeführt");
		val requestedPINText = Text()
		requestedPINText.text = "Eingegebene PIN"
		val pinCorrekt = BoxItem()
		pinCorrekt.name = "pinCorrect"
		pinCorrekt.isChecked = true
		pinCorrekt.text = "OK"
		dataTransactionStep.inputInfoUnits.add(requestedPINText)

		val cerificateText = Text()
		cerificateText.text = "Berechtigungszertifikat"
		val certificateCorrekt = BoxItem()
		certificateCorrekt.name = "certificateCorrekt"
		certificateCorrekt.isChecked = true
		certificateCorrekt.text = "OK"
		//        statusMessages_CheckBox.boxItems.add(certificateCorrekt);
		dataTransactionStep.inputInfoUnits.add(cerificateText)

		val eCardText = Text()
		eCardText.text = "Verwendete Karte"
		val eCardCorrekt = BoxItem()
		eCardCorrekt.name = "eCardCorrekt"
		eCardCorrekt.isChecked = true
		eCardCorrekt.text = "OK"
		dataTransactionStep.inputInfoUnits.add(eCardText)

		//        statusMessages_CheckBox.boxItems.add(eCardCorrekt);
		val dataTransactionText = Text()
		dataTransactionText.text = "Datenübermittlung wird geprüft"
		val dataTransactionCorrekt = BoxItem()
		dataTransactionCorrekt.name = "dataTransactionCorrekt"
		dataTransactionCorrekt.isChecked = true
		dataTransactionCorrekt.text = "OK"
		//        statusMessages_CheckBox.boxItems.add(dataTransactionCorrekt);
		dataTransactionStep.inputInfoUnits.add(dataTransactionText)

		//        dataTransaction_Step.inputInfoUnits.add(statusMessages_CheckBox);
		return dataTransactionStep
	}

	@Throws(Exception::class)
	private fun pinInputStep(requestedDataStep: Step): Step {
		val pinInputStep = Step("PIN-Eingabe")
		pinInputStep.action = PinInputAction(pinInputStep, requestedDataStep)
		val t = Text()
		t.text =
			"Durch die Eingabe Ihrer PIN bestätigen Sie, dass folgende markierte Daten an den Anbieter übermittelt werden."
		pinInputStep.inputInfoUnits.add(t)
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

		val sendAgreementText = Text()
		sendAgreementText.text =
			"Wenn Sie mit der Übermittlung der ausgewählten\n" +
			"Daten einverstanden sind, geben Sie bitte\n" +
			"Ihre 6-stellige PIN ein."
		
		val p1 = PasswordField("pf1")
		p1.description = "pass input1"
		p1.description = "PIN:"
		p1.maxLength = 6

		dataToSendSelection.boxItems.add(vornameBoxItem)
		dataToSendSelection.boxItems.add(nameBoxItem)
		// 	dataToSendSelection.boxItems.add(doctordegreeBoxItem);
// 	dataToSendSelection.boxItems.add(addressBoxItem);
// 	dataToSendSelection.boxItems.add(birthdayBoxItem);
// 	dataToSendSelection.boxItems.add(birthplaceBoxItem);
// 	dataToSendSelection.boxItems.add(identiycardtypeBoxItem);
// 	dataToSendSelection.boxItems.add(certificationcountryBoxItem);
// 	dataToSendSelection.boxItems.add(habitationBoxItem);
// 	dataToSendSelection.boxItems.add(ageverificationBoxItem);
		pinInputStep.inputInfoUnits.add(dataToSendSelection)
		// 	pinInputStep.inputInfoUnits.add(sendAgreement_Text);
		pinInputStep.inputInfoUnits.add(p1)

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
			oldResults: Map<String, ExecutionResults>,
			result: StepResult,
		): StepActionResult {
			val d = result.results.toTypedArray()
			var cc: Checkbox? = null
			for (i in d.indices) {
				if (d[i] is Checkbox) {
					cc = d[i] as Checkbox?
					println(cc!!.boxItems)
				}
			}

			val l = cc!!.boxItems
			for (b in l) {
				println(b.name + " " + b.isChecked)
			}

			val data = step.inputInfoUnits.toTypedArray()
			// 		    Object[] data = uc.steps.get(uc.steps.indexOf("PIN-Eingabe"));
			when (result.status) {
				ResultStatus.BACK -> // 			    for (int i = 0; i < data.length; i++) {
// 				if (data[i] instanceof Checkbox) {
// 				    Checkbox c = (Checkbox) data[i];
// 				    c.boxItems.clear();
// 				    c.boxItems.addAll(cc.boxItems);
// 				}
// 			    }
					return StepActionResult(StepActionResultStatus.BACK)

				ResultStatus.OK -> {
					var i = 0
					while (i < data.size) {
						if (data[i] is Checkbox) {
							val c = data[i] as Checkbox
							c.boxItems.clear()
							c.boxItems.addAll(cc.boxItems)
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
			oldResults: Map<String, ExecutionResults>,
			result: StepResult,
		): StepActionResult {
// 		    Object[] d = null;
// 		    for(ExecutionResults e : oldResults.values()){
// 			System.out.println(e.getStepName());
// 			if(e.getStepName().equals("Angefragte Daten")){
// 			    d = e.getResults().toArray();
// 			}
// 		    }
			val d = result.results.toTypedArray()
			var cc: Checkbox? = null
			for (i in d.indices) {
				if (d[i] is Checkbox) {
					cc = d[i] as Checkbox?
					println(cc!!.boxItems)
				}
			}
			val l = cc!!.boxItems
			for (b in l) {
				println(b.name + " " + b.isChecked)
			}
			// 		    Object[] data = requestedData_Step1.inputInfoUnits.toArray();
			val data = requestedData_Step1.inputInfoUnits.toTypedArray()
			when (result.status) {
				ResultStatus.BACK -> {
					var i = 0
					while (i < data.size) {
						if (data[i] is Checkbox) {
							val c = data[i] as Checkbox
							c.boxItems.clear()
							c.boxItems.addAll(cc.boxItems)
						}
						i++
					}
					return StepActionResult(StepActionResultStatus.BACK)
				}

				ResultStatus.OK -> // 			    for (int i = 0; i < data.length; i++) {
// 				if (data[i] instanceof Checkbox) {
// 				    Checkbox c = (Checkbox) data[i];
// 				    c.boxItems.clear();
// 				    c.boxItems.addAll(cc.boxItems);
// 				}
// 			    }
					return StepActionResult(StepActionResultStatus.NEXT)

				else -> return StepActionResult(StepActionResultStatus.REPEAT)
			}
		}
	}
}
