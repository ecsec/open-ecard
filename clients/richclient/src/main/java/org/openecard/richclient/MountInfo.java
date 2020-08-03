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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class parses the "/proc/mounts" file under UNIX systems to retrieve mount information
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
    private MountInfo(String device, String mountPath, String fileSystem, Map<String, String> mountOptions) {
	this.device = device;
	this.mountPath = mountPath;
	this.fileSystem = fileSystem;
	this.mountOptions = mountOptions;
    }

    /**
     * Hold the parsed mount information in memory
     */
    private static List<MountInfo> parsedMountInfo;

    /**
     * Parses "/proc/mounts" and returns the mount information in a list.
     *
     * @return the parsed mount information as a {@code List} of {@link MountInfo MountInfos}
     * @throws IOException if there is an error reading the "/proc/mounts" file
     */
    public static List<MountInfo> getMounts() throws IOException {
	// lazy load the mount information
	if (parsedMountInfo == null) {
	    parsedMountInfo = new ArrayList<>();
	    try (BufferedReader bufferedReader = new BufferedReader(new FileReader("/proc/mounts"))) {
		String line;

		while ((line = bufferedReader.readLine()) != null) {
		    // split by whitespace to get the 6 parts individually
		    String[] parts = line.split(" ");
		    String device = parts[0];
		    String mountPath = parts[1];
		    String fileSystem = parts[2];
		    String mountOptions = parts[3];
		    Map<String, String> parsedMountOptions = parseOptions(mountOptions);
		    MountInfo mountInfo = new MountInfo(device, mountPath, fileSystem, parsedMountOptions);
		    parsedMountInfo.add(mountInfo);
		}
	    }
	}
	return parsedMountInfo;
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

    /**
     * Parses the provided mount options in the form of "key=value,key=value,key,..."; for example "rw,mode=0664".
     *
     * @param mountOptions the mount options String to parse into a map
     * @return Mapping between keys and values (with null values for value-less keys, e.g. "rw").
     */
    private static Map<String, String> parseOptions(String mountOptions) {
	Map<String, String> optionsDict = new HashMap<>();
	// split options by "," first
	String[] options = mountOptions.split(",");
	for (String option : options) {
	    // then split each option into a key and a potential value
	    String[] optionParts = option.split("=");
	    switch (optionParts.length) {
		case 0:
		    // should not happen
		    continue;
		case 1:
		    // only key like "rw" is present without value
		    optionsDict.put(optionParts[0], null);
		    break;
		default:
		    // we have a key=value pair
		    optionsDict.put(optionParts[0], optionParts[1]);
		    break;
	    }
	}
	return optionsDict;
    }

}
