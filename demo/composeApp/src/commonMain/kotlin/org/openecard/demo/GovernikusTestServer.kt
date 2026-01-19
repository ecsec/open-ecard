package org.openecard.demo

import io.ktor.client.HttpClient
import io.ktor.client.request.forms.submitForm
import io.ktor.http.Url
import io.ktor.http.parameters

/**
 * Utility class to interact with the governikus test server.
 */
class GovernikusTestServer(
	val url: String = "https://test.governikus-eid.de/Autent-DemoApplication/samlstationary",
	val client: HttpClient = HttpClient { followRedirects = false },
) {
	/**
	 * Loads a new TC Token from the server.
	 */
	suspend fun loadTcTokenUrl(): String {
		val localhostUrl =
			client
				.submitForm(
					url = url,
					formParameters =
						parameters {
							append("changeAllNatural", "ALLOWED")
							append("requestedAttributesEidForm.documentType", "ALLOWED")
							append("requestedAttributesEidForm.issuingState", "ALLOWED")
							append("requestedAttributesEidForm.dateOfExpiry", "ALLOWED")
							append("requestedAttributesEidForm.givenNames", "ALLOWED")
							append("requestedAttributesEidForm.familyNames", "ALLOWED")
							append("requestedAttributesEidForm.artisticName", "ALLOWED")
							append("requestedAttributesEidForm.academicTitle", "ALLOWED")
							append("requestedAttributesEidForm.dateOfBirth", "ALLOWED")
							append("requestedAttributesEidForm.placeOfBirth", "ALLOWED")
							append("requestedAttributesEidForm.nationality", "ALLOWED")
							append("requestedAttributesEidForm.birthName", "ALLOWED")
							append("requestedAttributesEidForm.placeOfResidence", "ALLOWED")
							append("requestedAttributesEidForm.communityID", "ALLOWED")
							append("requestedAttributesEidForm.residencePermitI", "ALLOWED")
							append("requestedAttributesEidForm.restrictedId", "ALLOWED")
							append("ageVerificationForm.ageToVerify", "0")
							append("ageVerificationForm.ageVerification", "PROHIBITED")
							append("placeVerificationForm.placeToVerify", "02760401100000")
							append("placeVerificationForm.placeVerification", "PROHIBITED")
							append("eidTypesForm.cardCertified", "ALLOWED")
							append("eidTypesForm.seCertified", "ALLOWED")
							append("eidTypesForm.seEndorsed", "ALLOWED")
							append("eidTypesForm.hwKeyStore", "ALLOWED")
							append("transactionInfo", "")
							append("levelOfAssurance", "BUND_HOCH")
							append("transactionAttestationForm.selectedTransactionAttestation", "")
							append("transactionAttestationForm.selectedContextData", "null")
							append("transactionAttestationForm.contextInformation", "")
						},
				) {
				}.headers["Location"]
		checkNotNull(localhostUrl)
		val tokenUrl = Url(localhostUrl).parameters["tcTokenURL"]
		return checkNotNull(tokenUrl)
	}
}
