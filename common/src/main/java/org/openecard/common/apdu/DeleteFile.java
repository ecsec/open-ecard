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

package org.openecard.common.apdu;

import org.openecard.common.apdu.common.CardCommandAPDU;
import org.openecard.common.util.ShortUtils;


/**
 * DELETE FILE command.
 * See ISO/IEC 7816-9 Section 9.3.3.
 *
 * @author Hans-Martin Haase <hans-martin.haase@ecsec.de>
 */
public class DeleteFile extends CardCommandAPDU {

    /**
     * Instruction byte for the DELETE FILE command.
     */
    private static final byte INS_DELETE_FILE = (byte) 0xE4;

    /**
     * P2 value for FCI return type.
     */
    private static final byte FCI = (byte) 0x00;

    /**
     * P2 value for FCP return type.
     */
    private static final byte FCP = (byte) 0x04;

    /**
     * P2 value for FMD return type.
     */
    private static final byte FMD = (byte) 0x08;

    public DeleteFile(byte p1, byte p2) {
	super(x00, INS_DELETE_FILE, p1, p2);
    }

    public void setFCI() {
	setP2(FCI);
    }

    public void setFCP() {
	setP2(FCP);
    }

    public void setFMD() {
	setP2(FMD);
    }

    /**
     * Implements a DELETE FILE APDU to delete the Master File.
     */
    public static final class MasterFile extends DeleteFile {

	/** 
	 * MasterFile file identifier .
	 */
	public static final byte[] MF_FID = new byte[]{(byte) 0x3F, (byte) 0x00};

	/**
	 * Creates a new DELETE FILE APDU to delete the Master File.
	 */
	public MasterFile() {
	    super((byte) 0x00, (byte) 0x0C);
	    setData(MF_FID);
	}
    }

    /**
     * Implements a DELETE FILE APDU to delete a file (DF or EF).
     */
    public static final class File extends DeleteFile {
	/**
	 * Creates a new DELETE FILE APDU to delete a file.
	 *
	 * @param fid File Identifier
	 */
	public File(byte[] fid) {
	    super((byte) 0x00, (byte) 0x0C);
	    setData(fid);
	}
    }

    /**
     * Implements a DELETE FILE APDU to delete a directory (DF).
     */
    public static final class ChildDirectory extends DeleteFile {

	/**
	 * Creates a new DELETE FILE APDU to delete a directory.
	 *
	 * @param fid File Identifier
	 */
	public ChildDirectory(byte[] fid) {
	    super((byte) 0x01, (byte) 0x0C);
	    setData(fid);
	}
    }

    /**
     * Implements a DELETE FILE APDU to delete a file (EF).
     */
    public static final class ChildFile extends DeleteFile {

	/**
	 * Creates a new DELETE FILE APDU to delete a file.
	 *
	 * @param fid File Identifier
	 */
	public ChildFile(byte[] fid) {
	    super((byte) 0x02, (byte) 0x0C);
	    setData(fid);
	}

	/**
	 * Creates a new DELETE FILE APDU to delete a file.
	 *
	 * @param fid ChildFile Identifier
	 */
	public ChildFile(short fid){
	    this(ShortUtils.toByteArray(fid));
	}
    }

    /**
     * Implements a DELETE FILE APDU to delete the parent directory.
     */
    public static final class Parent extends DeleteFile {

	/**
	 * Creates a new DELETE FILE APDU to delete the parent directory.
	 */
	public Parent() {
	    super((byte) 0x03, (byte) 0x0C);
	}
    }

    /**
     * Implements a DELETE FILE APDU to delete a application.
     */
    public static final class Application extends DeleteFile {

	/**
	 * Creates a new DELETE FILE APDU to delete a application.
	 *
	 * @param aid Application Identifier
	 */
	public Application(byte[] aid) {
	    super((byte) 0x04, (byte) 0x0C);
	    setData(aid);
	}
    }

    /**
     * Implements a DELETE FILE APDU to delete a absolute path.
     */
    public static final class AbsolutePath extends DeleteFile {

	/**
	 * Creates a new DELETE FILE APDU to delete a absolute path.
	 *
	 * @param aid Application Identifier
	 */
	public AbsolutePath(byte[] aid) {
	    super((byte) 0x08, (byte) 0x0C);
	    setData(aid);
	}
    }

    /**
     * Implements a DELETE FILE APDU to delete a relative path.
     */
    public static final class RelativePath extends DeleteFile {

	/**
	 * Creates a new DELETE FILE APDU to delete a relative path.
	 *
	 * @param aid Application Identifier
	 */
	public RelativePath(byte[] aid) {
	    super((byte) 0x09, (byte) 0x0C);
	    setData(aid);
	}
    }

}
