package org.openecard.addons.tr03124.transport

actual fun newKtorClientBuilder(certTracker: EserviceCertTracker): KtorClientBuilder =
	CertTrackingClientBuilder(certTracker)
