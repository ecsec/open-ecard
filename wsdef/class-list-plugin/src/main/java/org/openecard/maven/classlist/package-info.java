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
 * Maven plugin which appends an entry to a file when it is a JAXB element class.
 * The plugin creates a file named in the {@code fileName} parameter and places it into the the directory named by the
 * parameter {@code outputDirectory}. The contents of the file are entries of fully qualified class names with all JAXB
 * element classes found below the directory named by the {@code classDirectory} parameter. The {@code excludes} list
 * can contain fully qualified class names which should not occur in the list.
 * <p>
 * The plugin is executed in the {@code process-classes} phase. The execution goal is named {@code class-list}.
 */
package org.openecard.maven.classlist;
