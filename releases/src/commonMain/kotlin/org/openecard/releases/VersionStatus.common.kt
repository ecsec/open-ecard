/****************************************************************************
 * Copyright (C) 2024 ecsec GmbH.
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

package org.openecard.releases

import io.github.z4kn4fein.semver.Version
import io.github.z4kn4fein.semver.constraints.Constraint
import io.github.z4kn4fein.semver.constraints.toConstraint
import io.github.z4kn4fein.semver.toVersion
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
class VersionStatus(
	val maintained: List<SemverConstraint>,
	val security: List<SemverConstraint>,
)

typealias SemverVersion =
	@Serializable(SemverVersionSerializer::class)
	Version

object SemverVersionSerializer : KSerializer<Version> {
	override val descriptor = PrimitiveSerialDescriptor("SemverVersion", PrimitiveKind.STRING)

	override fun deserialize(decoder: Decoder): Version = decoder.decodeString().toVersion()

	override fun serialize(
		encoder: Encoder,
		value: Version,
	) {
		encoder.encodeString(value.toString())
	}
}

typealias SemverConstraint =
	@Serializable(SemverConstraintSerializer::class)
	Constraint

object SemverConstraintSerializer : KSerializer<Constraint> {
	override val descriptor = PrimitiveSerialDescriptor("SemverConstraint", PrimitiveKind.STRING)

	override fun deserialize(decoder: Decoder): Constraint = decoder.decodeString().toConstraint()

	override fun serialize(
		encoder: Encoder,
		value: Constraint,
	) {
		encoder.encodeString(value.toString())
	}
}
