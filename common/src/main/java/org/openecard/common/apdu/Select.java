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

package org.openecard.common.apdu;

import org.openecard.common.apdu.common.CardCommandAPDU;
import org.openecard.common.util.ShortUtils;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 * @author Johannes Schmoelz <johannes.schmoelz@ecsec.de>
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class Select extends CardCommandAPDU {

    /**
     * SELECT command instruction byte
     */
    private static final byte SELECT_INS = (byte) 0xA4;
    private static final byte FCI = (byte) 0x00;
    private static final byte FCP = (byte) 0x04;
    private static final byte FMD = (byte) 0x08;

    /**
     * Creates a new Select APDU.
     *
     * @param p1 Parameter byte P1
     * @param p2 Parameter byte P2
     */
    public Select(byte p1, byte p2) {
	super(x00, SELECT_INS, p1, p2);
    }

    /**
     * Set to return FCI (File Control Information) template.
     */
    public void setFCI() {
	setP2(FCI);
	setLE(xFF);
    }

    /**
     * Set to return FCP (File Control Parameters) template.
     */
    public void setFCP() {
	setP2(FCP);
	setLE(xFF);
    }

    /**
     * Set to return FCP (File Management Data) template.
     */
    public void setFMD() {
	setP2(FMD);
	setLE(xFF);
    }

    /**
     * Implements a Select APDU to select the Master File.
     */
    public static final class MasterFile extends Select {

	/** MasterFile file identifier */
	public static final byte[] MF_FID = new byte[]{(byte) 0x3F, (byte) 0x00};

	/**
	 * Creates a new Select APDU to select the Master File.
	 */
	public MasterFile() {
	    super((byte) 0x00, (byte) 0x0C);
	    setData(MF_FID);
	}
    }

    /**
     * Implements a Select APDU to select a file (DF or EF).
     */
    public static final class File extends Select {
	/**
	 * Creates a new Select APDU to select a file.
	 *
	 * @param fid File Identifier
	 */
	public File(byte[] fid) {
	    super((byte) 0x00, (byte) 0x0C);
	    setData(fid);
	}
    }

    /**
     * Implements a Select APDU to select a directory (DF).
     */
    public static final class ChildDirectory extends Select {

	/**
	 * Creates a new Select APDU to select a directory.
	 *
	 * @param fid File Identifier
	 */
	public ChildDirectory(byte[] fid) {
	    super((byte) 0x01, (byte) 0x0C);
	    setData(fid);
	}
    }

    /**
     * Implements a Select APDU to select a file (EF).
     */
    public static final class ChildFile extends Select {

	/**
	 * Creates a new Select APDU to select a file.
	 *
	 * @param fid File Identifier
	 */
	public ChildFile(byte[] fid) {
	    super((byte) 0x02, (byte) 0x0C);
	    setData(fid);
	}

	/**
	 * Creates a new Select APDU to select a file.
	 *
	 * @param fid ChildFile Identifier
	 */
	public ChildFile(short fid){
	    this(ShortUtils.toByteArray(fid));
	}
    }

    /**
     * Implements a Select APDU to select the parent directory.
     */
    public static final class Parent extends Select {

	/**
	 * Creates a new Select APDU to select the parent directory.
	 */
	public Parent() {
	    super((byte) 0x03, (byte) 0x0C);
	}
    }

    /**
     * Implements a Select APDU to select a application.
     */
    public static final class Application extends Select {

	/**
	 * Creates a new Select APDU to select a application.
	 *
	 * @param aid Application Identifier
	 */
	public Application(byte[] aid) {
	    super((byte) 0x04, (byte) 0x0C);
	    setData(aid);
	}
    }

    /**
     * Implements a Select APDU to select a absolute path.
     */
    public static final class AbsolutePath extends Select {

	/**
	 * Creates a new Select APDU to select a absolute path.
	 *
	 * @param aid Application Identifier
	 */
	public AbsolutePath(byte[] aid) {
	    super((byte) 0x08, (byte) 0x0C);
	    setData(aid);
	}
    }

    /**
     * Implements a Select APDU to select a relative path.
     */
    public static final class RelativePath extends Select {

	/**
	 * Creates a new Select APDU to select a relative path.
	 *
	 * @param aid Application Identifier
	 */
	public RelativePath(byte[] aid) {
	    super((byte) 0x09, (byte) 0x0C);
	    setData(aid);
	}
    }

}
