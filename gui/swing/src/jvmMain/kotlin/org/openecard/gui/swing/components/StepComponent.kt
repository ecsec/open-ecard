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
package org.openecard.gui.swing.components

import org.openecard.gui.definition.OutputInfoUnit
import java.awt.Component

/**
 * Every component on a StepFrame must implement this interface.<br></br>
 * It abstracts the verification logic like password length validation,
 * supplies a function to get the result for the UserConsentResponse and bundles
 * the swing components in a single component.
 *
 * @author Tobias Wich
 */
interface StepComponent {
	/**
	 * Get GUI component (AWT) so it can be drawn on a container.
	 * Every StepComponent must have exactly one GUI component containing all elements.
	 * @return Drawable component.
	 */
	val component: Component

	/**
	 * Determine if this component has content which can be validated.
	 * @return True when StepComponent.validate() can be called, false otherwise.
	 */
	val isValueType: Boolean

	/**
	 * Validate the contents of this component. A meaningful result is only
	 * expected if StepComponent.isValueType() returns true.<br></br>
	 * For example in case of a TextInput, this function checks if the text is
	 * within the bounds of minLength and maxLength.
	 * @return True if component is valid, false if not. Undefined behaviour
	 * when component does not contain validatable content.
	 */
	fun validate(): Boolean

	/**
	 * The UserConsentResponse contains the result for all steps. Every step can get
	 * these results from its components with this function.
	 * @return Value for use in UserConsentResponse when StepComponent.isValueType()
	 * returns true, undefined (also null possible) otherwise.
	 */
	val value: OutputInfoUnit?
}
