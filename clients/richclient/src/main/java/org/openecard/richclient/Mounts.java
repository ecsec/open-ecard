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
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * This class parses the "/proc/mounts" file under UNIX systems to retrieve mount information in from of
 * {@link MountInfo} objects
 *
 * @author Sebastian Schuberth
 */
public class Mounts {

    /**
     * Hold the parsed mount information in memory
     */
    private final List<MountInfo> parsedMountInfo;

    /**
     * Mapping from mount path to MountInfo to speed up lookup of mount paths
     */
    private final Map<String, MountInfo> map;

    private static Mounts instance = null;

    /**
     * Private constructor to create a {@code Mounts} instance
     *
     * @param parsedMountInfo a {@code List} of {@link MountInfo} parsed from /proc/mounts
     * @param map a mapping from mount paths to {@link MountInfo} to speed up lookup of mount paths
     */
    private Mounts(List<MountInfo> parsedMountInfo, Map<String, MountInfo> map) {
	this.parsedMountInfo = parsedMountInfo;
	this.map = map;
    }

    /**
     * Parses "/proc/mounts" and returns a {@code Mounts} object that contains a List of all {@link MountInfo}
     *
     * @return the parsed mount information as a {@code List} of {@link MountInfo MountInfos} within a {@code Mounts}
     * object
     * @throws IOException if there is an error reading the "/proc/mounts" file
     */
    public static Mounts readMounts() throws IOException {
	// lazy load the mount information
	if (instance == null) {
	    List<MountInfo> parsedMountInfo = new ArrayList<>();
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

	    // convert mountInfos to mapping from mount path to MountInfo to speed up lookup of mount paths
	    Map<String, MountInfo> map = new HashMap<>();
	    for (MountInfo mountInfo : parsedMountInfo) {
		map.put(mountInfo.getMountPath(), mountInfo);
	    }
	    instance = new Mounts(parsedMountInfo, map);
	}
	return instance;
    }

    /**
     * Returns all parsed mount information in a list
     *
     * @return a {@code List} of all parsed {@link MountInfo MountInfos}
     */
    public List<MountInfo> getMountInfoList() {
	return parsedMountInfo;
    }

    /**
     * Returns a mapping from all mount paths to their respective {@code MountInfo}
     *
     * @return a mapping from all mount paths to their respective {@link MountInfo MountInfos}
     */
    public Map<String, MountInfo> getMountInfoMapping() {
	return map;
    }

    /**
     * Checks if for the provided file path the longest matching mount path has the "noexec" flag set
     *
     * @param filePath the path to check
     * @return true, if the "noexec" flag for the provided file path is set; false, if the "noexec" flag is not set or
     * if the file does not have a matching mount path in /proc/mounts (not even "/")
     * @throws IOException if an I/O error occurs during the construction of the canonical pathname
     * @throws NoSuchElementException if no matching mount path (not even "/") exists in /proc/mounts
     */
    public boolean hasNoExecFlag(String filePath) throws IOException, NoSuchElementException {
	// get canonical file path, as the path might be a symlink
	String canonicalFilePath = new File(filePath).getCanonicalPath();
	// look up the mount point this path is in
	MountInfo mountPoint = getMatchingMountPoint(canonicalFilePath);
	// check if noexec flag is set for this mount point
	return mountPoint.checkIfFlagIsSet("noexec");
    }

    /**
     * Returns the longest matching mount point for the provided file path
     *
     * @param filePath the file path
     * @return the longest matching {@link MountInfo} for the given path
     * @throws IOException if an I/O error occurs during the construction of the canonical pathname
     * @throws NoSuchElementException if no matching mount path (not even "/") exists in /proc/mounts
     */
    public MountInfo getMatchingMountPoint(String filePath) throws IOException, NoSuchElementException {
	File file = new File(filePath);
	// find the closest parent to the directory 
	while (file != null) {
	    String path = file.getCanonicalPath();
	    if (map.containsKey(path)) {
		// we found the longest matching mount path; this could be "/" at the very end
		MountInfo mountInfo = map.get(path);
		return mountInfo;
	    }
	    // continue with parent of current file
	    file = file.getParentFile();
	}
	// we did not find a matching mount path (not even "/"), throw error
	throw new NoSuchElementException("There is no matching mount point for path " + filePath);
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
