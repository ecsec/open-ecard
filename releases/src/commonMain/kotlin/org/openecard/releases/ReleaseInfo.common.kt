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

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder


@Serializable
class ReleaseInfo (
	val version: SemverVersion,
	val latestVersion: VersionData,
	val maintenanceVersions: List<VersionData>,
	val artifacts: List<Artifact>,
	val versionStatus: VersionStatus,
)

@Serializable
class VersionData (
	val version: SemverVersion,
	val artifacts: List<Artifact>,
)

@Serializable
class Artifact(
	val type: ArtifactType,
	val url: String,
	val sha256: String,
)

@Serializable(with = ArtifactTypeSerializer::class)
enum class ArtifactType {
	EXE,
	MSI,
	DMG,
	PKG,
	DEB,
	RPM,
	OTHER,
}

object ArtifactTypeSerializer: KSerializer<ArtifactType> {
	override val descriptor = PrimitiveSerialDescriptor("ArtifactType", PrimitiveKind.STRING)

	override fun deserialize(decoder: Decoder): ArtifactType {
		return try {
			ArtifactType.valueOf(decoder.decodeString())
		} catch (e: IllegalArgumentException) {
			// make sure the code doesn't break if a new type is added
			ArtifactType.OTHER
		}
	}

	override fun serialize(encoder: Encoder, value: ArtifactType) {
		encoder.encodeString(value.name)
	}
}
