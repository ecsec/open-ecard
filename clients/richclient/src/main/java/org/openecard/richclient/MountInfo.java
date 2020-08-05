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

package org.openecard.richclient;

import java.util.Map;

/**
 * This class represents mount information obtained from the "/proc/mounts" file under UNIX systems
 *
 * @author Sebastian Schuberth
 */
public class MountInfo {

    /**
     * The mounted device (can be "none" or any arbitrary string for virtual file systems).
     */
    private final String device;
    /**
     * The path where the file system is mounted.
     */
    private final String mountPath;
    /**
     * The file system.
     */
    private final String fileSystem;
    /**
     * The mount options.
     */
    private final Map<String, String> mountOptions;

    /**
     * The constructor to create a new {@code MountInfo} object
     *
     * @param device the mounted device
     * @param mountPath the path where the file system is mounted
     * @param fileSystem the file sytem
     * @param mountOptions the mount options as a mapping from option to value
     */
    MountInfo(String device, String mountPath, String fileSystem, Map<String, String> mountOptions) {
	this.device = device;
	this.mountPath = mountPath;
	this.fileSystem = fileSystem;
	this.mountOptions = mountOptions;
    }

    /**
     * Returns the mounted device
     *
     * @return the mounted device
     */
    public String getDevice() {
	return device;
    }

    /**
     * Returns the mount path
     *
     * @return the mount path
     */
    public String getMountPath() {
	return mountPath;
    }

    /**
     * Returns the file system
     *
     * @return the file system
     */
    public String getFileSystem() {
	return fileSystem;
    }

    /**
     * Returns the mount options as a {@code Map}
     *
     * @return the mount options
     */
    public Map<String, String> getMountOptions() {
	return mountOptions;
    }

    /**
     * Checks if the provided flag is set in the mount options
     *
     * @param flag the flag to check
     * @return true, if the flag is set; false otherwise
     */
    public boolean checkIfFlagIsSet(String flag) {
	return mountOptions.containsKey(flag);
    }

}
