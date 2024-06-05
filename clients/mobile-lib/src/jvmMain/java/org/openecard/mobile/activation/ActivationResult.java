/** **************************************************************************
 * Copyright (C) 2019-2024 ecsec GmbH.
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
 ************************************************************************** */

package org.openecard.mobile.activation;

import org.openecard.robovm.annotations.FrameworkInterface;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Mike
 */
@FrameworkInterface
public interface ActivationResult {

    String getRedirectUrl();

    ActivationResultCode getResultCode();

    String getErrorMessage();

    /**
     * If present, represents the minor error code of the error leading to the termination of the process.
     *
     * @see org.openecard.common.ECardConstants.Minor ECardConstants
     * @return The minor result code or {@code null} if not present.
     */
    String getProcessResultMinor();

	Set<String> getResultParameterKeys();
	String getResultParameter(String key);
}
