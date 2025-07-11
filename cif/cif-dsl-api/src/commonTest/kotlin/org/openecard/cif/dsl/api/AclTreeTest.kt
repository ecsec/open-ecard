package org.openecard.cif.dsl.api

import org.openecard.cif.definition.CardProtocol
import org.openecard.cif.definition.acl.AlwaysTree
import org.openecard.cif.definition.acl.BoolTreeAnd
import org.openecard.cif.definition.acl.BoolTreeLeaf
import org.openecard.cif.definition.acl.BoolTreeOr
import org.openecard.cif.definition.acl.DidStateReference
import org.openecard.cif.definition.acl.NeverTree
import org.openecard.cif.definition.acl.PaceAclQualifier
import org.openecard.cif.dsl.api.acl.AclBoolTreeBuilder
import org.openecard.cif.dsl.api.acl.AclBoolTreeBuilder.didState
import org.openecard.cif.dsl.api.acl.AclBoolTreeBuilder.paceQualifier
import org.openecard.cif.dsl.api.acl.AclScope
import org.openecard.utils.serialization.toPrintable
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

typealias ProtocolAclBuilderType = AclScope.() -> Unit

class AclTreeTest {
	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun never() {
		val result = AclBoolTreeBuilder.Never

		assertEquals(NeverTree, result)
	}

	@Test
	fun always() {
		val result = AclBoolTreeBuilder.Always

		assertEquals(AlwaysTree, result)
	}

	@OptIn(ExperimentalUuidApi::class, ExperimentalStdlibApi::class, ExperimentalUnsignedTypes::class)
	@Test
	fun singleDidState() {
		val givenName = Uuid.random().toString()
		val givenActive = false
		val givenPaceQualifier = Random.nextBytes(16).toUByteArray()

		val result =
			didState(
				name = givenName,
				active = givenActive,
				paceQualifier(givenPaceQualifier.toHexString()),
			)

		assertEquals(
			BoolTreeOr(
				listOf(
					BoolTreeAnd(
						listOf<BoolTreeLeaf>(
							DidStateReference(
								name = givenName,
								active = givenActive,
								stateQualifier =
									PaceAclQualifier(givenPaceQualifier.toPrintable()),
							),
						),
					),
				),
			),
			result,
		)
	}

	@Test
	fun singleProtocolNeverAclBuilderTypeTest() {
		var acls: ProtocolAclBuilderType = {
			acl(CardProtocol.Grouped.CONTACT) {
				Never
			}
		}
	}

	@Test
	fun singleProtocolAlwaysAclBuilderTypeTest() {
		var acls: ProtocolAclBuilderType = {
			acl(CardProtocol.Grouped.CONTACTLESS) {
				Always
			}
		}
	}

	@Test
	fun multipleProtocolAclBuilderTypeTest() {
		var acls: ProtocolAclBuilderType = {
			acl(CardProtocol.Grouped.CONTACTLESS) {
				Always
			}
			acl(CardProtocol.Grouped.CONTACTLESS) {
				Always
			}
		}
	}

	@Test
	fun aclBuilderTypeTest() {
		var acls: ProtocolAclBuilderType = {
			acl(CardProtocol.Technical.T0) {
				and(
					{ Always },
					{ didState("ZKD.FD", true) },
					{ didState("BDS.R2", false, paceQualifier("abcd1234")) },
				)
			}
			acl(CardProtocol.Technical.T0) {
				and(
					{ didState("ABC.FD", true) },
					{ didState("CBA.FD", false, paceQualifier("abcd1234")) },
				)
			}
			acl(CardProtocol.Technical.T1) {
				Always
			}
			acl(CardProtocol.Technical.TCL) {
				and(
					{ activeDidState("ABC.TR", paceQualifier("abcd1234")) },
					{ activeDidState("CBA.FD", paceQualifier("abcd1234")) },
					{
						or(
							activeDidState("RFD.PL"),
							activeDidState("GF.NM"),
							activeDidState("DFSD.FD"),
						)
					},
					{
						or(
							activeDidState("RFD.PL"),
							activeDidState("GF.NM"),
							activeDidState("DFSD.FD"),
						)
					},
				)
			}
			acl(CardProtocol.Technical.T15) {
				or(
					{ activeDidState("ABC.TR", paceQualifier("abcd1234")) },
					{ activeDidState("CBA.FD", paceQualifier("abcd1234")) },
					{
						and(
							activeDidState("RFD.PL"),
							activeDidState("GF.NM"),
							activeDidState("DFSD.FD"),
						)
					},
					{
						and(
							activeDidState("RFD.PL"),
							activeDidState("GF.NM"),
							activeDidState("DFSD.FD"),
						)
					},
					{
						and(
							activeDidState("RFD.PL"),
							activeDidState("GF.NM"),
							activeDidState("DFSD.FD"),
						)
					},
				)
			}
		}
	}
}
