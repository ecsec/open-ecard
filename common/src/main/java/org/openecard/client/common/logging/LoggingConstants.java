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

package org.openecard.client.common.logging;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;


/**
 * Defines global marker for the SLF4J logging framework.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class LoggingConstants {

    // Convenience marker for methods
    public static final Marker ENTER = MarkerFactory.getMarker("ENTERING");
    public static final Marker EXIT = MarkerFactory.getMarker("EXITING");
    public static final Marker THROWING = MarkerFactory.getMarker("THROWING");
    // Levels
    public static final Marker SEVERE = MarkerFactory.getMarker("SEVERE");
    public static final Marker INFO = MarkerFactory.getMarker("INFO");
    public static final Marker CONFIG = MarkerFactory.getMarker("CONFIG");
    public static final Marker FINE = MarkerFactory.getMarker("FINE");
    public static final Marker FINER = MarkerFactory.getMarker("FINER");
    public static final Marker FINEST = MarkerFactory.getMarker("FINEST");

}
