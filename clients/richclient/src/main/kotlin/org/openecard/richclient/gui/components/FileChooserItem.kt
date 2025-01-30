/****************************************************************************
 * Copyright (C) 2014 ecsec GmbH.
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
package org.openecard.richclient.gui.components

import org.openecard.common.I18n
import java.io.File
import java.util.*
import javax.swing.JFileChooser
import javax.swing.filechooser.FileFilter

/**
 *
 * @author Hans-Martin Haase
 */
class FileChooserItem internal constructor(fileTypes: String) : JFileChooser(File(System.getProperty("user.home"))) {
    private val lang: I18n = I18n.getTranslation("addon")

    init {
        // set open ecard logo in the title bar
        setDialogTitle(lang.translationForKey("addon.settings.file.select"))
        setFileFilter(GenericFileTypeFilter(fileTypes))
        setDialogType(OPEN_DIALOG)
        setFileSelectionMode(FILES_AND_DIRECTORIES)
    }

    private inner class GenericFileTypeFilter(fileTypes: String) : FileFilter() {
        private val fileTypes: List<String>

        init {
            val types: Array<String> = fileTypes.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            this.fileTypes = ArrayList(Arrays.asList(*types))
        }

        override fun accept(file: File): Boolean {
            if (file.isDirectory()) {
                return true
            }

            val startPosSuffix: Int = file.getName().lastIndexOf(".")
            if (startPosSuffix > -1) {
                for (elem: String? in fileTypes) {
                    var suffix: String = file.getName().substring(startPosSuffix)
                    suffix = suffix.replace(".", "")

                    if (suffix.equals(elem, ignoreCase = true)) {
                        return true
                    }
                }
            }
            return false
        }

        override fun getDescription(): String {
            val msg: StringBuilder = StringBuilder()
            for (type: String? in fileTypes) {
                msg.append(".")
                msg.append(type)
                msg.append(", ")
            }

            return msg.toString()
        }
    }
}
