/****************************************************************************
 * Copyright (C) 2012-2019 ecsec GmbH.
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
package org.openecard.scio

import io.github.oshai.kotlinlogging.KotlinLogging
import jnasmartcardio.Smartcardio
import org.openecard.common.ifd.scio.SCIOTerminals
import org.openecard.common.util.LinuxLibraryFinder
import java.security.NoSuchAlgorithmException
import javax.smartcardio.TerminalFactory

private val LOG = KotlinLogging.logger {  }

/**
 * Proxy and abstracted Factory for SCIO PC/SC driver.
 *
 * @author Tobias Wich
 * @author Benedikt Biallowons
 */
class PCSCFactory : org.openecard.common.ifd.scio.TerminalFactory {
    private val osName: String = System.getProperty("os.name")
	val rawFactory: TerminalFactory

    /**
     * Default constructor with fixes for the faulty SmartcardIO library.
     *
     * @throws java.io.FileNotFoundException if pcsclite for Linux can't be found.
     * @throws NoSuchAlgorithmException if no PC/SC provider can be found.
     */
    init {
		if (osName.startsWith("Linux")) {
            val libFile = LinuxLibraryFinder.getLibraryPath("pcsclite", "1")
            System.setProperty("sun.security.smartcardio.library", libFile.absolutePath)
        }

        try {
			LOG.info { "Trying to initialize PCSC subsystem." }
            this.rawFactory = TerminalFactory.getInstance(ALGORITHM, null, Smartcardio())
			LOG.info { "Successfully initialized PCSC subsystem." }
        } catch (ex: NoSuchAlgorithmException) {
			error { "Failed to initialize smartcard system." }
            throw ex
        }
    }

    override val type: String = rawFactory.type

    override fun terminals(): SCIOTerminals {
        return PCSCTerminals(this)
    }

}

private const val ALGORITHM = "PC/SC"

