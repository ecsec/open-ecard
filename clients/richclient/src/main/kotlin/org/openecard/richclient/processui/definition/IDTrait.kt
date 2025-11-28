/*
 * Copyright (C) 2012 ecsec GmbH.
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
 */
package org.openecard.richclient.processui.definition

import org.openecard.utils.common.generateSessionIdBase64
import kotlin.random.Random

/**
 * Class implementing the ID portion common to all `InfoUnit`s.
 * This class creates IDs if none are given and provides an implementation of the ID getter and setter.
 *
 * @author Tobias Wich
 */
abstract class IDTrait(
	random: Random = Random.Default,
	override var id: String = random.generateSessionIdBase64(),
) : InfoUnit
