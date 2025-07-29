package org.openecard.sal.iface

import org.openecard.cif.definition.CardProtocol
import org.openecard.cif.definition.acl.AclDefinition
import org.openecard.cif.definition.acl.AlwaysTree
import org.openecard.cif.definition.acl.BoolTreeAnd
import org.openecard.cif.definition.acl.BoolTreeLeaf
import org.openecard.cif.definition.acl.CifAclOr
import org.openecard.cif.definition.acl.DidStateReference
import org.openecard.cif.definition.acl.NeverTree
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AclUtilsTest {
	@Test
	fun `test selectForProtocol`() {
		val paceTree = CifAclOr(listOf(BoolTreeAnd(listOf(DidStateReference.forName("PACE")))))
		val acl =
			AclDefinition(
				mapOf(
					CardProtocol.Grouped.CONTACT to CifAclOr(listOf(BoolTreeAnd(listOf(BoolTreeLeaf.True)))),
					// copy to make tree different objects
					CardProtocol.Grouped.CONTACTLESS to paceTree.copy(),
				),
			)
		assertEquals(NeverTree, acl.selectForProtocol(org.openecard.sc.iface.CardProtocol.RAW))
		assertEquals(AlwaysTree, acl.selectForProtocol(org.openecard.sc.iface.CardProtocol.T0))
		assertEquals(AlwaysTree, acl.selectForProtocol(org.openecard.sc.iface.CardProtocol.T1))
		assertEquals(AlwaysTree, acl.selectForProtocol(org.openecard.sc.iface.CardProtocol.T15))
		assertEquals(paceTree, acl.selectForProtocol(org.openecard.sc.iface.CardProtocol.TCL))
	}

	@Test
	fun `test has solution`() {
		assertFalse { NeverTree.hasSolution() }
		assertFalse { CifAclOr(listOf(BoolTreeAnd(listOf()))).hasSolution() }
		assertTrue { AlwaysTree.hasSolution() }
		assertTrue {
			val tree = CifAclOr(listOf(BoolTreeAnd(listOf(DidStateReference.forName("PACE")))))
			tree.hasSolution()
		}
		assertTrue {
			val tree =
				CifAclOr(
					listOf(
						BoolTreeAnd(listOf()),
						BoolTreeAnd(listOf(DidStateReference.forName("PACE"))),
					),
				)
			tree.hasSolution()
		}
	}

	@Test
	fun `is True`() {
		assertFalse { NeverTree.isTrue() }
		assertTrue { AlwaysTree.isTrue() }
		assertFalse {
			val tree = CifAclOr(listOf(BoolTreeAnd(listOf(DidStateReference.forName("PACE")))))
			tree.isTrue()
		}
		assertFalse {
			val tree = CifAclOr(listOf(BoolTreeAnd(listOf(BoolTreeLeaf.True, DidStateReference.forName("PACE")))))
			tree.isTrue()
		}
		assertTrue {
			val tree =
				CifAclOr(
					listOf(
						BoolTreeAnd(listOf(BoolTreeLeaf.True)),
						BoolTreeAnd(listOf(DidStateReference.forName("PACE"))),
					),
				)
			tree.isTrue()
		}
	}

	@Test
	fun `remove unsupported DIDs`() {
		assertEquals(
			NeverTree,
			NeverTree.removeUnsupportedDids(listOf("PACE")),
		)
		assertEquals(
			AlwaysTree,
			AlwaysTree.removeUnsupportedDids(listOf("PACE")),
		)
		assertEquals(
			CifAclOr(listOf(BoolTreeAnd(listOf(DidStateReference.forName("PACE"))))),
			CifAclOr(
				listOf(
					BoolTreeAnd(listOf(DidStateReference.forName("PACE"))),
					BoolTreeAnd(listOf(DidStateReference.forName("PIN"), DidStateReference.forName("C2C"))),
				),
			).removeUnsupportedDids(listOf("C2C", "PACE")),
		)
		assertEquals(
			CifAclOr(listOf(BoolTreeAnd(listOf(DidStateReference.forName("PACE"))))),
			CifAclOr(
				listOf(
					BoolTreeAnd(listOf(DidStateReference.forName("PIN"))),
					BoolTreeAnd(listOf(DidStateReference.forName("PACE"))),
				),
			).removeUnsupportedDids(listOf("PACE")),
		)
		assertEquals(
			NeverTree,
			CifAclOr(listOf(BoolTreeAnd(listOf(DidStateReference.forName("PACE")))))
				.removeUnsupportedDids(listOf("PIN")),
		)
	}

	@Test
	fun `reduce authenticated DIDs`() {
		assertEquals(
			NeverTree,
			NeverTree.reduceWithAuthenticatedDids(setOf(DidStateReference.forName("PACE"))),
		)
		assertEquals(
			AlwaysTree,
			AlwaysTree.reduceWithAuthenticatedDids(setOf(DidStateReference.forName("PACE"))),
		)
		assertEquals(
			AlwaysTree,
			CifAclOr(
				listOf(BoolTreeAnd(listOf(DidStateReference.forName("PACE")))),
			).reduceWithAuthenticatedDids(setOf(DidStateReference.forName("PACE"))),
		)
		assertEquals(
			AlwaysTree,
			CifAclOr(
				listOf(
					BoolTreeAnd(listOf(DidStateReference.forName("PIN"))),
					BoolTreeAnd(listOf(DidStateReference.forName("PACE"))),
				),
			).reduceWithAuthenticatedDids(setOf(DidStateReference.forName("PACE"))),
		)
		assertEquals(
			CifAclOr(
				listOf(
					BoolTreeAnd(listOf(DidStateReference.forName("PIN"))),
				),
			),
			CifAclOr(
				listOf(
					BoolTreeAnd(listOf(DidStateReference.forName("PIN"), DidStateReference.forName("PACE"))),
				),
			).reduceWithAuthenticatedDids(setOf(DidStateReference.forName("PACE"))),
		)
	}
}
