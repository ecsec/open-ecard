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

package org.openecard.richclient

import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.joran.JoranConfigurator
import ch.qos.logback.classic.util.ContextInitializer
import ch.qos.logback.core.joran.spi.JoranException
import org.openecard.common.util.FileUtils.homeConfigDir
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException

/**
 * Helper class for the Logback configuration.
 *
 * @author Tobias Wich
 */
object LogbackConfig {
    private const val CFG_FILENAME = "richclient_logback.xml"
    private var LOG: Logger? = null // loaded on demand, for reason see in load() below

    @get:Throws(IOException::class)
    val confFile: File
        get() {
            val cfgDir = homeConfigDir
            val logFileStr = cfgDir.toString() + File.separator + CFG_FILENAME
            val logFile = File(logFileStr)
            return logFile
        }

    /**
     * Load Logback configuration.
     * At first the code tries to load the file '$HOME/.openecard/richclient_logback.xml', if it does not exist, then
     * the default config is used. The default configuration is loaded from within the richclient jar.
     *
     * @throws IOException If no config could be loaded.
     * @throws SecurityException If the config dir is not writable.
     * @throws JoranException If the config file contains invalid content.
     */
    @Throws(IOException::class, SecurityException::class, JoranException::class)
    fun load() {
        val logFile = confFile

        if (logFile.canRead() && logFile.isFile) {
            // this prevents loading the bundled config
            System.setProperty("logback.configurationFile", logFile.absolutePath)

            // make sure to reload logging config
            val ctx = LoggerFactory.getILoggerFactory() as LoggerContext
            ctx.reset()
            val conf = JoranConfigurator()
            conf.context = ctx
            ctx.reset()
            conf.doConfigure(logFile)

            // load logger after loading config, in order to avoid loading bundled config first
            if (LOG == null) {
                LOG = LoggerFactory.getLogger(LogbackConfig::class.java)
            }
            LOG!!.info("Configured Logback with config file from: {}", logFile)
        } else {
            loadDefault()
        }
    }

    @Throws(JoranException::class)
    fun loadDefault() {
        System.clearProperty("logback.configurationFile")
        val ctx = LoggerFactory.getILoggerFactory() as LoggerContext
        ctx.reset()
        val init = ContextInitializer(ctx)
        init.autoConfig()
    }
}
