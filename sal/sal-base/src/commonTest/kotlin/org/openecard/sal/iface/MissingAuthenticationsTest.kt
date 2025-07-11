package org.openecard.sal.iface

import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import org.openecard.cif.definition.acl.AlwaysTree
import org.openecard.cif.definition.acl.BoolTreeAnd
import org.openecard.cif.definition.acl.BoolTreeOr
import org.openecard.cif.definition.acl.CifAclOr
import org.openecard.cif.definition.acl.DidStateReference
import org.openecard.cif.definition.acl.NeverTree
import org.openecard.sal.iface.dids.PaceDid
import org.openecard.sal.iface.dids.PinDid
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class MissingAuthenticationsTest {
	val paceDid =
		mock<PaceDid> {
			every { name } returns "PACE"
		}
	val pinDid =
		mock<PinDid> {
			every { name } returns "PIN"
		}
	val c2cDid =
		mock<PinDid> {
			every { name } returns "C2C"
		}

	@Test
	fun `resolve never tree`() {
		assertIs<MissingAuthentications.Unsolveable>(NeverTree.missingAuthentications(listOf(), setOf()))
	}

	@Test
	fun `resolve always tree`() {
		val ma =
			AlwaysTree
				.missingAuthentications(listOf(), setOf())
				.missingDidObjectOrThrow()
		assertTrue { ma.isSolved }
		assertEquals(BoolTreeOr(listOf()), ma.decisions)
		assertEquals(listOf(), ma.options)
	}

	@Test fun `resolve tree with missing DIDs`() {
		val tree =
			CifAclOr(
				listOf(
					BoolTreeAnd(listOf(DidStateReference.forName("PACE"))),
					BoolTreeAnd(listOf(DidStateReference.forName("PIN"), DidStateReference.forName("C2C"))),
				),
			)

		tree
			.missingAuthentications(listOf(paceDid, pinDid), setOf())
			.missingDidObjectOrThrow()
			.let { ma ->
				assertFalse { ma.isSolved }
				assertEquals(1, ma.decisions.or.size)
				assertContentEquals(
					listOf("PACE"),
					ma.options
						.firstOrNull()
						?.map { it.authDid.name },
				)
			}

		tree
			.missingAuthentications(listOf(c2cDid, pinDid), setOf())
			.missingDidObjectOrThrow()
			.let { ma ->
				assertFalse { ma.isSolved }
				assertEquals(1, ma.options.size)
				assertContentEquals(
					listOf("PIN", "C2C"),
					ma.options
						.firstOrNull()
						?.map { it.authDid.name },
				)
			}
	}

	@Test
	fun `remove unsupported DIDs`() {
		val tree =
			CifAclOr(
				listOf(
					BoolTreeAnd(listOf(DidStateReference.forName("PACE"))),
					BoolTreeAnd(listOf(DidStateReference.forName("PIN"), DidStateReference.forName("C2C"))),
				),
			)

		tree
			.missingAuthentications(listOf(c2cDid, pinDid, paceDid), setOf())
			.missingDidObjectOrThrow()
			.let { ma1 ->
				assertFalse { ma1.isSolved }
				assertEquals(2, ma1.options.size)

				ma1
					.removeUnsupported(
						listOf(DidStateReference.forName("PACE"), DidStateReference.forName("PIN")),
					).missingDidObjectOrThrow()
					.let { ma2 ->
						assertEquals(1, ma2.options.size)
						assertContentEquals(
							listOf("PACE"),
							ma2.options
								.firstOrNull()
								?.map { it.authDid.name },
						)
					}

				ma1
					.removeUnsupported(
						listOf(DidStateReference.forName("C2C"), DidStateReference.forName("PIN")),
					).missingDidObjectOrThrow()
					.let { ma2 ->
						assertEquals(1, ma2.decisions.or.size)
						assertContentEquals(
							listOf("PIN", "C2C"),
							ma2.options
								.firstOrNull()
								?.map { it.authDid.name },
						)
					}
			}
	}
}
