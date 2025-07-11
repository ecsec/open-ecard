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
}
