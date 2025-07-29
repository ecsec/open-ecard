package org.openecard.sc.iface.info

import org.junit.jupiter.api.assertInstanceOf
import org.junit.jupiter.api.assertNotNull
import org.openecard.sc.iface.LifeCycleStatus
import org.openecard.utils.common.hex
import kotlin.test.Test
import kotlin.test.assertEquals

class FileInfoTest {
	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun `parse FCP`() {
		// value from nPA EF.DIR
		val fcpValue = hex("621a80020100c502005a82010183022f008801f08a0105a1038b0103")
		val fcp = FileInfo.fromSelectResponseData(fcpValue)

		assertInstanceOf<Fcp>(fcp)

		assertEquals(256u, fcp.numBytes)

		assertNotNull(fcp.fileDescriptor?.fdByte)
		assertEquals(true, fcp.fileDescriptor?.fdByte?.isEf)
		assertEquals(false, fcp.fileDescriptor?.fdByte?.isDf)
		assertEquals(EfCategory.WORKING_EF, fcp.fileDescriptor?.fdByte?.efCategory)
		assertEquals(EfStructure.TRANSPARENT, fcp.fileDescriptor?.fdByte?.efStructure)

		assertEquals(0x2F00u, fcp.fileIdentifier)
		assertEquals(0x1Eu, fcp.shortEf)

		assertEquals(LifeCycleStatus.OPERATIONAL_ACTIVE, fcp.lifeCycleStatus)
	}
}
