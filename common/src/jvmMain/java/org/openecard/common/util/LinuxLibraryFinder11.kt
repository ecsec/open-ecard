/****************************************************************************
 * Copyright (C) 2013 ecsec GmbH.
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
package org.openecard.common.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.*
import javax.annotation.Nonnull

/**
 * Helper class to find shared object (.so) libraries in linux systems.
 * The algorithm to find the objects is the same as used in the dynamic loader ld. There is one exception to this rule,
 * the ELF variables DT_RPATH and DT_RUNPATH are not evaluated, because they are not available in java.
 *
 * @author Tobias Wich
 */
object LinuxLibraryFinder {
    private val logger: Logger = LoggerFactory.getLogger(LinuxLibraryFinder::class.java)

    /**
     * Gets a file object pointing to the library which has been searched.
     * On success, the file points to first file found which is readable and thus can be used.
     *
     * The algorithm to find the library can be found in the ld.so(8) manpage and is as follows:
     *
     *  1. Check paths in `LD_LIBRARY_PATH` environment variable.
     *  1. Check for library in `/etc/ld.so.cache` by executing `ldconfig -p` and searching the output.
     *  1. Check the base library paths `/lib` and `/usr/lib` or `/lib64` and `/usr/lib64`
     * depending on the architecture.
     *
     *
     * @param name Name of the library, such as pcsclite.
     * @param version Version suffix such as 1, 1.0 or null if no suffix is desired.
     * @return The file object to the library.
     * @throws java.io.FileNotFoundException Thrown if the requested library could not be found.
     */
    @Nonnull
    @Throws(FileNotFoundException::class)
    fun getLibraryPath(@Nonnull name: String?, version: String?): File {
        // add version only if it has a meaningful value
        var version = version
        version = version ?: ""
        version = if (version.isEmpty()) "" else (".$version")
        val libname = System.mapLibraryName(name) + version
        // LD_LIBRARY_PATH
        var result = findInEnv(libname, System.getenv("LD_LIBRARY_PATH"))
        if (result != null) {
            return result
        }
        // ld.so.cache
        result = findInLdCache(libname)
        if (result != null) {
            return result
        }
        // base lib paths
        result = findInBaseLibPaths(libname)
        if (result != null) {
            return result
        }

        throw FileNotFoundException("Library $libname not found on your system.")
    }

    private fun findInEnv(@Nonnull libname: String, env: String?): File? {
        if (env != null) {
            for (path in env.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
                // append lib to path and see if it exists
                val result = "$path/$libname"
                val test = File(result.trim { it <= ' ' })
                if (test.canRead()) {
                    return test
                }
            }
        }
        // nothing found
        return null
    }

    private fun findInLdCache(@Nonnull libname: String): File? {
        val ldconfExec = findProgramFile("ldconfig") + " -p"
        var p: Process? = null
        try {
            p = Runtime.getRuntime().exec(ldconfExec)
            val cacheData = p.inputStream
            val cacheDataReader = BufferedReader(InputStreamReader(cacheData))
            var next: String
            while ((cacheDataReader.readLine().also { next = it }) != null) {
                if (next.endsWith(libname)) {
                    // extract library path from entry
                    // the line can look like this:
                    // libpcsclite.so.1 (libc6,x86-64) => /usr/lib/x86_64-linux-gnu/libpcsclite.so.1
                    val endIdx = next.lastIndexOf("=>")
                    if (endIdx != -1) {
                        val result = next.substring(endIdx + 2)
                        val test = File(result.trim { it <= ' ' })
                        if (test.canRead()) {
                            return test
                        }
                    }
                }
            }
        } catch (ex: IOException) {
            logger.debug("Library {} not found in ld.so.cache.", libname)
        } finally {
            if (p != null) {
                try {
                    p.inputStream.close()
                } catch (ex: IOException) {
                    // dafuq?!?
                }
                try {
                    p.outputStream.close()
                } catch (ex: IOException) {
                    // dafuq?!?
                }
                try {
                    p.errorStream.close()
                } catch (ex: IOException) {
                    // dafuq?!?
                }
            }
        }
        return null
    }

    @Nonnull
    private fun findProgramFile(@Nonnull name: String): String {
        var path = System.getenv()["PATH"]
        path = path ?: ""
        path = "/sbin:/usr/sbin:$path"
        // loop through entries and find program
        for (entry in path.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
            val fname = "$entry/$name"
            val f = File(fname)
            if (f.canExecute()) {
                return fname
            }
        }
        // nothing found, maybe the file is in the same path
        return name
    }

    private fun findInBaseLibPaths(@Nonnull libname: String): File? {
        val archSuffix = if ("64" == System.getProperty("sun.arch.data.model")) "64" else ""
        val basePaths = arrayOf(
            "/lib$archSuffix",
            "/usr/lib$archSuffix"
        )
        // look for lib in those paths
        for (path in basePaths) {
            val fname = "$path/$libname"
            val test = File(fname)
            if (test.canRead()) {
                return test
            }
        }
        return null
    }
}
