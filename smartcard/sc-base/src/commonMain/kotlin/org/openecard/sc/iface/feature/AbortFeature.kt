package org.openecard.sc.iface.feature

import org.openecard.sc.iface.CommError
import org.openecard.sc.iface.InsufficientBuffer
import org.openecard.sc.iface.InvalidHandle
import org.openecard.sc.iface.InvalidParameter
import org.openecard.sc.iface.InvalidValue
import org.openecard.sc.iface.NoService
import org.openecard.sc.iface.NotTransacted
import org.openecard.sc.iface.ReaderUnavailable
import org.openecard.sc.iface.RemovedCard
import org.openecard.sc.iface.ResetCard

interface AbortFeature : Feature {
	@Throws(
		InsufficientBuffer::class,
		InvalidHandle::class,
		InvalidParameter::class,
		InvalidValue::class,
		NoService::class,
		NotTransacted::class,
		ReaderUnavailable::class,
		CommError::class,
		ResetCard::class,
		RemovedCard::class,
	)
	fun cancelRunningProcess()
}
