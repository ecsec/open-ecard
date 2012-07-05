/****************************************************************************
 * Copyright (C) 2012 HS Coburg.
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

package org.openecard.client.crypto.common.asn1.cvc;

import java.util.Iterator;
import java.util.TreeMap;
import org.openecard.client.common.tlv.TLV;
import org.openecard.client.common.tlv.TLVException;
import org.openecard.client.common.tlv.TagClass;
import org.openecard.client.common.util.ByteUtils;
import org.openecard.client.crypto.common.asn1.eac.oid.CVCertificatesObjectIdentifier;
import org.openecard.client.crypto.common.asn1.utils.ObjectIdentifierUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implements the Certificate Holder Authorization Template (CHAT)
 *
 * See BSI-TR-03110, version 2.10, part 3, section C.4.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public final class CHAT {

    private static final Logger _logger = LoggerFactory.getLogger(CHAT.class);

    private String oid;
    private Role role;
    private byte[] discretionaryData;
    private TreeMap<DataGroup, Boolean> writeAccess = new TreeMap<DataGroup, Boolean>() {
	{
	    DataGroup[] data = DataGroup.values();
	    for (int i = 16; i < 21; i++) {
		put(data[i], false);
	    }
	}
    };
    private TreeMap<DataGroup, Boolean> readAccess = new TreeMap<DataGroup, Boolean>() {
	{
	    DataGroup[] data = DataGroup.values();
	    for (int i = 0; i < 21; i++) {
		put(data[i], false);
	    }
	}
    };
    private TreeMap<SpecialFunction, Boolean> specialFunctions = new TreeMap<SpecialFunction, Boolean>() {
	{
	    SpecialFunction[] data = SpecialFunction.values();
	    for (int i = 0; i < data.length; i++) {
		put(data[i], false);
	    }
	}
    };
    private TreeMap<AccessRight, Boolean> accessRights = new TreeMap<AccessRight, Boolean>() {
	{
	    AccessRight[] data = AccessRight.values();
	    for (int i = 0; i < data.length; i++) {
		put(data[i], false);
	    }
	}
    };

    /**
     * Represents the roles.
     * See BSI-TR-03110, version 2.10, part 3, section C.4.1.
     * See BSI-TR-03110, version 2.10, part 3, section C.4.2.
     * See BSI-TR-03110, version 2.10, part 3, section C.4.3.
     */
    public enum Role {
	CVCA, DV_OFFICIAL, DV_NON_OFFICIAL,
	AUTHENTICATION_TERMINAL, INSPECTION_TERMINAL, SIGNATURE_TERMINAL
    }

    /**
     * Represents the special functions.
     * See BSI-TR-03110, version 2.10, part 3, section C.4.2.
     */
    public enum SpecialFunction {
	INSTALL_QUALIFIED_CERTIFICATE, INSTALL_CERTIFICATE, PIN_MANAGEMENT,
	CAN_ALLOWED, PRIVILEGED_TERMINAL, RESTRICTED_IDENTIFICATION,
	COMMUNITY_ID_VERIFICATION, AGE_VERIFICATION;
    }

    /**
     * Represents the data groups.
     * See BSI-TR-03110, version 2.10, part 3, section C.4.2.
     * See BSI-TR-03110, version 2.10, part 2, section A.1.
     */
    public enum DataGroup {
	DG01, DG02, DG03, DG04, DG05, DG06, DG07,
	DG08, DG09, DG10, DG11, DG12, DG13, DG14,
	DG15, DG16, DG17, DG18, DG19, DG20, DG21;
    }

    /**
     * Represents the access rights.
     * See BSI-TR-03110, version 2.10, part 3, section C.4.1.
     * See BSI-TR-03110, version 2.10, part 3, section C.4.3.
     */
    public enum AccessRight {
	DG03, DG04, GENERATE_SIGNATURE, GENERATE_QUALIFIED_SIGNATURE;
    }

    /**
     * Creates a new CHAT.
     *
     * @param chat CHAT
     * @throws TLVException
     */
    public CHAT(byte[] chat) throws TLVException {
	this(TLV.fromBER(chat));
    }

    /**
     * Creates a new CHAT.
     *
     * @param tlv TLV
     * @throws TLVException
     */
    public CHAT(TLV tlv) throws TLVException {
	oid = ObjectIdentifierUtils.toString(tlv.findChildTags(0x06).get(0).getValue());
	discretionaryData = tlv.findChildTags(0x53).get(0).getValue();

	if (oid.equals(CVCertificatesObjectIdentifier.id_IS)) {
	    // Inspection systems
	    parseRole(discretionaryData[0]);
	    parseAccessRights(discretionaryData[0]);
	} else if (oid.equals(CVCertificatesObjectIdentifier.id_AT)) {
	    // Authentication terminal
	    parseRole(discretionaryData[0]);
	    parseWriteAccess(discretionaryData);
	    parseReadAccess(discretionaryData);
	    parseSpecialFunctions(discretionaryData);
	} else if (oid.equals(CVCertificatesObjectIdentifier.id_ST)) {
	    // Signature terminal
	    parseRole(discretionaryData[0]);
	    parseAccessRights(discretionaryData[0]);
	}
    }

    /**
     * Parse the role of the CHAT.
     *
     * @param roleByte Role
     */
    private void parseRole(byte roleByte) {
	roleByte = (byte) (roleByte & (byte) 0xC0);

	switch (roleByte) {
	    case (byte) 0xC0:
		role = Role.CVCA;
		break;
	    case (byte) 0x80:
		role = Role.DV_OFFICIAL;
		break;
	    case (byte) 0x40:
		role = Role.DV_NON_OFFICIAL;
		break;
	    case (byte) 0x00:
		if (oid.equals(CVCertificatesObjectIdentifier.id_IS)) {
		    role = Role.INSPECTION_TERMINAL;
		} else if (oid.equals(CVCertificatesObjectIdentifier.id_AT)) {
		    role = Role.AUTHENTICATION_TERMINAL;
		} else if (oid.equals(CVCertificatesObjectIdentifier.id_ST)) {
		    role = Role.SIGNATURE_TERMINAL;
		}
		break;
	    default:
		break;
	}
    }

    /**
     * Parse the access rights of the CHAT.
     *
     * @param accessRightsByte Access rights
     */
    private void parseAccessRights(byte accessRightsByte) {
	if (role.equals(Role.INSPECTION_TERMINAL)) {
	    if (ByteUtils.isBitSet(6, new byte[]{accessRightsByte})) {
		// Read access to ePassport application: DG 4 (Iris)
		accessRights.put(AccessRight.DG04, Boolean.TRUE);
	    }
	    if (ByteUtils.isBitSet(7, new byte[]{accessRightsByte})) {
		// Read access to ePassport application: DG 3 (Fingerprint)
		accessRights.put(AccessRight.DG03, Boolean.TRUE);
	    }
	} else if (role.equals(Role.SIGNATURE_TERMINAL)) {
	    if (ByteUtils.isBitSet(6, new byte[]{accessRightsByte})) {
		// Generate qualified electronic signature
		accessRights.put(AccessRight.GENERATE_QUALIFIED_SIGNATURE, Boolean.TRUE);
	    }
	    if (ByteUtils.isBitSet(7, new byte[]{accessRightsByte})) {
		// Generate electronic signature
		accessRights.put(AccessRight.GENERATE_SIGNATURE, Boolean.TRUE);
	    }
	}
    }

    /**
     * Parse the write access of the CHAT.
     *
     * @param discretionaryData Discretionary data
     */
    private void parseWriteAccess(byte[] discretionaryData) {
	Iterator<DataGroup> it = writeAccess.keySet().iterator();
	for (int i = 2; i < 6; i++) {
	    DataGroup item = (DataGroup) it.next();
	    writeAccess.put(item, ByteUtils.isBitSet(i, discretionaryData));
	}
    }

    /**
     * Parse the read access of the CHAT.
     *
     * @param discretionaryData Discretionary data
     */
    private void parseReadAccess(byte[] discretionaryData) {
	Iterator<DataGroup> it = readAccess.keySet().iterator();
	for (int i = 31; i > 11; i--) {
	    DataGroup item = (DataGroup) it.next();
	    readAccess.put(item, ByteUtils.isBitSet(i, discretionaryData));
	}
    }

    /**
     * Parse the special functions of the CHAT.
     *
     * @param discretionaryData Discretionary data
     */
    private void parseSpecialFunctions(byte[] discretionaryData) {
	Iterator<SpecialFunction> it = specialFunctions.keySet().iterator();
	for (int i = 32; i < 40; i++) {
	    SpecialFunction item = (SpecialFunction) it.next();
	    specialFunctions.put(item, ByteUtils.isBitSet(i, discretionaryData));
	}
    }

    /**
     * Returns the role of the CHAT.
     *
     * @return Write access
     */
    public Role getRole() {
	return role;
    }

    /**
     * Returns the write access of the CHAT.
     *
     * @return Write access
     */
    public TreeMap<DataGroup, Boolean> getWriteAccess() {
	return writeAccess;
    }

    /**
     * Sets the write access of the CHAT.
     *
     * @param dataGroup Data group
     * @param selected Selected
     * @return True if data group is set, otherwise false
     */
    public boolean setWriteAccess(DataGroup dataGroup, boolean selected) {
	if (writeAccess.containsKey(dataGroup)) {
	    writeAccess.put(dataGroup, selected);
	    return true;
	}
	return false;
    }

    /**
     * Sets the write access of the CHAT.
     *
     * @param dataGroup Data Group
     * @param selected Selected
     * @return True if data group is set, otherwise false
     */
    public boolean setWriteAccess(String dataGroup, boolean selected) {
	return setWriteAccess(DataGroup.valueOf(dataGroup), selected);
    }

    /**
     * Sets the write access of the CHAT.
     *
     * @param writeAccess Write access
     */
    public void setWriteAccess(TreeMap<DataGroup, Boolean> writeAccess) {
	Iterator<DataGroup> it = this.writeAccess.keySet().iterator();
	while (it.hasNext()) {
	    DataGroup item = (DataGroup) it.next();
	    this.writeAccess.put(item, writeAccess.get(item));
	}
    }

    /**
     * Returns the read access of the CHAT.
     *
     * @return Read access
     */
    public TreeMap<DataGroup, Boolean> getReadAccess() {
	return readAccess;
    }

    /**
     * Sets the read access of the CHAT.
     *
     * @param dataGroup Data group
     * @param selected Selected
     * @return True if the data group is set, otherwise false
     */
    public boolean setReadAccess(DataGroup dataGroup, boolean selected) {
	if (readAccess.containsKey(dataGroup)) {
	    readAccess.put(dataGroup, selected);
	    return true;
	}
	return false;
    }

    /**
     * Sets the read access of the CHAT.
     *
     * @param dataGroup Data group
     * @param selected Selected
     * @return True if the data group is set, otherwise false
     */
    public boolean setReadAccess(String dataGroup, boolean selected) {
	return setReadAccess(DataGroup.valueOf(dataGroup), selected);
    }

    /**
     * Sets the read access of the CHAT.
     *
     * @param readAccess Read access
     */
    public void setReadAccess(TreeMap<DataGroup, Boolean> readAccess) {
	Iterator<DataGroup> it = this.readAccess.keySet().iterator();
	while (it.hasNext()) {
	    DataGroup item = (DataGroup) it.next();
	    this.readAccess.put(item, readAccess.get(item));
	}
    }

    /**
     * Returns the special function of the CHAT.
     *
     * @return Special functions
     */
    public TreeMap<SpecialFunction, Boolean> getSpecialFunctions() {
	return specialFunctions;
    }

    /**
     * Sets the special functions of the CHAT.
     *
     * @param specialFunction Special functions
     * @param selected Selected
     * @return True if the special function is set, otherwise false
     */
    public boolean setSpecialFunctions(SpecialFunction specialFunction, boolean selected) {
	if (specialFunctions.containsKey(specialFunction)) {
	    specialFunctions.put(specialFunction, selected);
	    return true;
	}
	return false;
    }

    /**
     * Sets the given special function of the CHAT.
     *
     * @param specialFunction Special function
     * @param selected Selected
     * @return True if the special function is set, otherwise false
     */
    public boolean setSpecialFunction(String specialFunction, boolean selected) {
	return setSpecialFunctions(SpecialFunction.valueOf(specialFunction), selected);
    }

    /**
     * Sets the special functions of the CHAT.
     *
     * @param specialFunctions Special functions
     */
    public void setSpecialFunctions(TreeMap<SpecialFunction, Boolean> specialFunctions) {
	Iterator<SpecialFunction> it = this.specialFunctions.keySet().iterator();
	while (it.hasNext()) {
	    SpecialFunction item = (SpecialFunction) it.next();
	    this.specialFunctions.put(item, specialFunctions.get(item));
	}
    }

    /**
     * Returns the access rights of the CHAT.
     *
     * @return Access rights
     */
    public TreeMap<AccessRight, Boolean> getAccessRights() {
	return accessRights;
    }

    /**
     * Sets the special functions of the CHAT.
     *
     * @param accessRight Access right
     * @param selected Selected
     * @return True if the access right is set, otherwise false
     */
    public boolean setAccessRights(AccessRight accessRight, boolean selected) {
	if (accessRights.containsKey(accessRight)) {
	    accessRights.put(accessRight, selected);
	    return true;
	}
	return false;
    }

    /**
     * Sets the special functions of the CHAT.
     *
     * @param accessRight Access right
     * @param selected Selected
     * @return True if the access right is set, otherwise false
     */
    public boolean setAccessRights(String accessRight, boolean selected) {
	return setAccessRights(AccessRight.valueOf(accessRight), selected);
    }

    /**
     * Sets the special functions of the CHAT.
     *
     * @param accessRights Access right
     */
    public void setAccessRights(TreeMap<AccessRight, Boolean> accessRights) {
	Iterator<AccessRight> it = this.accessRights.keySet().iterator();
	while (it.hasNext()) {
	    AccessRight item = (AccessRight) it.next();
	    this.accessRights.put(item, accessRights.get(item));
	}
    }

    /**
     * Returns the CHAT as a byte array.
     *
     * @return CHAT
     * @throws TLVException
     */
    public byte[] toByteArray() throws TLVException {
	byte[] data = new byte[5];

	// Decode role in bit 0 to 1.
	switch (role) {
	    case CVCA:
		data[0] |= 0xC0;
		break;
	    case DV_OFFICIAL:
		data[0] |= 0x80;
		break;
	    case DV_NON_OFFICIAL:
		data[0] |= 0x40;
		break;
	    default:
		break;
	}

	// Decode write access in bit 2 to 6.
	Iterator<DataGroup> it1 = writeAccess.keySet().iterator();
	for (int i = 2; i < 6; i++) {
	    DataGroup item = (DataGroup) it1.next();
	    if (writeAccess.get(item)) {
		ByteUtils.setBit(i, data);
	    }
	}

	// Decode read access in bit 11 to 31.
	Iterator<DataGroup> it2 = readAccess.keySet().iterator();
	for (int i = 31; i > 11; i--) {
	    DataGroup item = (DataGroup) it2.next();
	    if (readAccess.get(item)) {
		ByteUtils.setBit(i, data);
	    }
	}

	// Decode special functions in bit 32 to 40.
	Iterator<SpecialFunction> it3 = specialFunctions.keySet().iterator();
	for (int i = 32; i < 40; i++) {
	    SpecialFunction item = (SpecialFunction) it3.next();
	    if (specialFunctions.get(item)) {
		ByteUtils.setBit(i, data);
	    }
	}

	// Decode access rights in bit 2 to 7.
	Iterator<AccessRight> it4 = accessRights.keySet().iterator();
	for (int i = 6; i < 7; i++) {
	    AccessRight item = (AccessRight) it4.next();
	    if (accessRights.get(item)) {
		ByteUtils.setBit(i, data);
	    }
	}

	TLV discretionaryDataObject = new TLV();
	discretionaryDataObject.setTagNumWithClass((byte) 0x53);
	discretionaryDataObject.setValue(data);

	TLV oidObject = new TLV();
	oidObject.setTagNumWithClass((byte) 0x06);
	oidObject.setValue(ObjectIdentifierUtils.getValue(oid));
	oidObject.addToEnd(discretionaryDataObject);

	TLV chatObject = new TLV();
	chatObject.setTagNum((byte) 0x4C);
	chatObject.setTagClass(TagClass.APPLICATION);
	chatObject.setChild(oidObject);

	return chatObject.toBER(true);
    }

    /**
     * Returns the CHAT as a hex encoded string.
     *
     * @return CHAT
     */
    public String toHexString() {
	try {
	    return ByteUtils.toHexString(toByteArray(), true);
	} catch (TLVException ex) {
	    _logger.error(ex.getMessage(), ex);
	    return null;
	}
    }

    @Override
    public String toString() {
	try {
	    return ByteUtils.toHexString(toByteArray());
	} catch (TLVException ex) {
	    _logger.error(ex.getMessage(), ex);
	    return null;
	}
    }

}
