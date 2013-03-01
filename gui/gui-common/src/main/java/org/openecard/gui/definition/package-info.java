/****************************************************************************
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
 ***************************************************************************/

/**
 * User consent definition classes.
 * The definition classes are used to describe the user consent.<br/>
 * In order to create a user consent, {@link Step} instances must be added to a {@link UserConsent} instance. Each Step
 * can contain elements (subclasses of {@link InfoUnit}. The {@link InputInfoUnit} interface is the base of elements
 * which have no output values, while the {@link OutputInfoUnit} interface represents elements which do have an output
 * value.
 */
package org.openecard.gui.definition;
