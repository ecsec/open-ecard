package org.openecard.sc.iface

import org.openecard.sc.iface.feature.Feature

interface TerminalConnection {
	val terminal: Terminal

	val isCardConnected: Boolean
	val card: Card?

	@Throws(
		InvalidHandle::class,
		InvalidValue::class,
		NoService::class,
		NoSmartcard::class,
		CommError::class,
	)
	fun disconnect(disposition: CardDisposition = CardDisposition.LEAVE)

	@Throws(
		InsufficientBuffer::class,
		InvalidHandle::class,
		InvalidParameter::class,
		InvalidValue::class,
		NoMemory::class,
		NoService::class,
		ReaderUnavailable::class,
		CommError::class,
		InternalSystemError::class,
		RemovedCard::class,
		ResetCard::class,
		NoSmartcard::class,
		ProtoMismatch::class,
		SharingViolation::class,
		UnknownReader::class,
		UnsupportedFeature::class,
		UnpoweredCard::class,
		UnresponsiveCard::class,
	)
	fun reconnect(
		protocol: PreferredCardProtocol = PreferredCardProtocol.ANY,
		shareMode: ShareMode = ShareMode.SHARED,
		disposition: CardDisposition = CardDisposition.LEAVE,
	)

	@Throws(
		InsufficientBuffer::class,
		InvalidHandle::class,
		InvalidParameter::class,
		InvalidValue::class,
		NoService::class,
		NotTransacted::class,
		ReaderUnavailable::class,
		UnsupportedFeature::class,
		CommError::class,
		RemovedCard::class,
		ResetCard::class,
	)
	fun getFeatures(): Set<Feature>

	@Throws(
		InvalidHandle::class,
		InvalidValue::class,
		NoService::class,
		ReaderUnavailable::class,
		SharingViolation::class,
		CommError::class,
	)
	fun beginTransaction()

	@Throws(
		InvalidHandle::class,
		InvalidValue::class,
		NoService::class,
		ReaderUnavailable::class,
		SharingViolation::class,
		CommError::class,
	)
	fun endTransaction()
}

@Throws(
	InsufficientBuffer::class,
	InvalidHandle::class,
	InvalidParameter::class,
	InvalidValue::class,
	NoService::class,
	NotTransacted::class,
	ReaderUnavailable::class,
	UnsupportedFeature::class,
	CommError::class,
	RemovedCard::class,
	ResetCard::class,
)
inline fun <reified FT : Feature> TerminalConnection.feature(): FT? = getFeatures().filterIsInstance<FT>().firstOrNull()
