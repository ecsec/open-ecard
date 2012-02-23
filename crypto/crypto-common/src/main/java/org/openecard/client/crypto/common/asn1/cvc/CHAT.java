/* Copyright 2012, Hochschule fuer angewandte Wissenschaften Coburg 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecard.client.crypto.common.asn1.cvc;

import org.openecard.client.common.I18n;
import org.openecard.client.common.tlv.TLV;
import org.openecard.client.common.tlv.TLVException;
import org.openecard.client.common.util.ByteUtils;
import org.openecard.client.common.util.StringUtils;

/**
 * Holds the information of a certificate holder authorization template
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 * 
 */
public class CHAT {
    
    public enum TerminalType {
	InspectionSystem, AuthenticationTerminal, SignatureTerminal
    }

    public enum Role {
	CVCA, DV_official, DV_unofficial, AuthenticationTerminal
    }

    public enum SpecialFunction {
	Install_Qualified_Certificate, 
	Install_Certificate, 
	PIN_Management, 
	CAN_allowed, 
	Privileged_Terminal, 
	Restricted_Identification {
	  public String toString() {
	      return I18n.getTranslation("sal").translationForKey("restricted_identification");
	  }
	},
	Community_ID_Verification, 
	Age_Verification {
	    public String toString() {
		return I18n.getTranslation("sal").translationForKey("age_confirmation");
	    }
	},
    }

    public enum DataGroup {
	DG1_Document_Type {
	  public String toString(){
	      return I18n.getTranslation("sal").translationForKey("type_of_id_card");
	  }
	},
	DG2_Issuing_State {
		  public String toString(){
		      return  I18n.getTranslation("sal").translationForKey("issuing_state");
		  }
		}, 
	DG3_Date_of_Expiry, 
	DG4_GivenNames {
		  public String toString(){
		      return  I18n.getTranslation("sal").translationForKey("given_names");
		  }
		},
	DG5_FamilyNames {
		  public String toString(){
		      return  I18n.getTranslation("sal").translationForKey("family_names");
		  }
		}, 
	DG6_ArtisticName {
		  public String toString(){
		      return  I18n.getTranslation("sal").translationForKey("religious_artistic_name");
		  }
		} , 
	DG7_AcademicTitle {
		  public String toString(){
		      return  I18n.getTranslation("sal").translationForKey("doctoral_degree");
		  }
		}, 
	DG8_DateOfBirth{
		  public String toString(){
		      return  I18n.getTranslation("sal").translationForKey("date_of_birth");
		  }
		}, 
	DG9_PlaceOfBirth {
		  public String toString(){
		      return  I18n.getTranslation("sal").translationForKey("place_of_birth");
		  }
		}, 
	DG10_Nationality, 
	DG11_Sex, 
	DG12_OptionalDataR, 
	DG13_RFU, 
	DG14_RFU, 
	DG15_RFU, 
	DG16_RFU, 
	DG17_PlaceOfResidence {
		  public String toString(){
		      return  I18n.getTranslation("sal").translationForKey("address");
		  }
		}, 
	DG18_CommunityID, 
	DG19_ResidencePermitI, 
	DG20_ResidencePermitII, 
	DG21_OptionalDataRW
    }

    private Role role;

    public Role getRole() {
	return role;
    }

    public boolean[] getSpecialFunctions() {
	return specialFunctions;
    }

    public boolean[] getReadAccess() {
	return readAccess;
    }

    public boolean[] getWriteAccess() {
	return writeAccess;
    }

    public TerminalType getType() {
	return type;
    }

    private boolean[] specialFunctions = new boolean[8];
    private boolean[] readAccess = new boolean[21];
    private boolean[] writeAccess = new boolean[5];

    private TerminalType type;
    private final byte[] oidAuthenticationTerminal = StringUtils.toByteArray("04007F000703010202");
    byte[] oid;

    /**
     * Creates a new empty CHAT for the specified Terminal-Type
     * 
     * @param type
     *            Type of the Terminal this CHAT belongs to
     */
    public CHAT(TerminalType type) {
	this.type = type;
	this.role = Role.AuthenticationTerminal;
    }

    public CHAT(TLV tlv) {

	if (ByteUtils.compare(oidAuthenticationTerminal, tlv.findChildTags(0x06).get(0).getValue())) {
	    this.oid = oidAuthenticationTerminal;
	    this.type = TerminalType.AuthenticationTerminal;

	} else {
	    throw new IllegalArgumentException("Terminal Type not yet supported: "
		    + ByteUtils.toHexString(tlv.findChildTags(0x06).get(0).getValue()));
	}

	this.parseDiscretionaryData(tlv.findChildTags(0x53).get(0).getValue());

    }

    private void parseDiscretionaryData(byte[] data) {

	// Role is encoded in bit 0 and 1
	switch ((byte) (data[0] & ((byte) 0xC0))) {
	case (byte) 0xC0:
	    this.role = Role.CVCA;
	    break;
	case (byte) 0x80:
	    this.role = Role.DV_official;
	    break;
	case 0x40:
	    this.role = Role.DV_unofficial;
	    break;
	case 0x00:
	    this.role = Role.AuthenticationTerminal;
	    break;
	}

	// Special Functions are encoded in Bit 32 to 39
	specialFunctions[SpecialFunction.Install_Qualified_Certificate.ordinal()] = isBitSet(32, data);
	specialFunctions[SpecialFunction.Install_Certificate.ordinal()] = isBitSet(33, data);
	specialFunctions[SpecialFunction.PIN_Management.ordinal()] = isBitSet(34, data);
	specialFunctions[SpecialFunction.CAN_allowed.ordinal()] = isBitSet(35, data);
	specialFunctions[SpecialFunction.Privileged_Terminal.ordinal()] = isBitSet(36, data);
	specialFunctions[SpecialFunction.Restricted_Identification.ordinal()] = isBitSet(37, data);
	specialFunctions[SpecialFunction.Community_ID_Verification.ordinal()] = isBitSet(38, data);
	specialFunctions[SpecialFunction.Age_Verification.ordinal()] = isBitSet(39, data);

	// Read acces is encoded in bit 11 to 31

	// Bits 7 to 10 are RFU

	// write access is encoded in bits 2 to 6

	writeAccess[DataGroup.DG17_PlaceOfResidence.ordinal() - 16] = isBitSet(2, data);
	writeAccess[DataGroup.DG18_CommunityID.ordinal() - 16] = isBitSet(3, data);
	writeAccess[DataGroup.DG19_ResidencePermitI.ordinal() - 16] = isBitSet(4, data);
	writeAccess[DataGroup.DG20_ResidencePermitII.ordinal() - 16] = isBitSet(5, data);
	writeAccess[DataGroup.DG21_OptionalDataRW.ordinal() - 16] = isBitSet(6, data);

	readAccess[DataGroup.DG1_Document_Type.ordinal()] = isBitSet(31, data);
	readAccess[DataGroup.DG2_Issuing_State.ordinal()] = isBitSet(30, data);
	readAccess[DataGroup.DG3_Date_of_Expiry.ordinal()] = isBitSet(29, data);
	readAccess[DataGroup.DG4_GivenNames.ordinal()] = isBitSet(28, data);
	readAccess[DataGroup.DG5_FamilyNames.ordinal()] = isBitSet(27, data);
	readAccess[DataGroup.DG6_ArtisticName.ordinal()] = isBitSet(26, data);
	readAccess[DataGroup.DG7_AcademicTitle.ordinal()] = isBitSet(25, data);
	readAccess[DataGroup.DG8_DateOfBirth.ordinal()] = isBitSet(24, data);
	readAccess[DataGroup.DG9_PlaceOfBirth.ordinal()] = isBitSet(23, data);
	readAccess[DataGroup.DG10_Nationality.ordinal()] = isBitSet(22, data);
	readAccess[DataGroup.DG11_Sex.ordinal()] = isBitSet(21, data);
	readAccess[DataGroup.DG12_OptionalDataR.ordinal()] = isBitSet(20, data);
	readAccess[DataGroup.DG13_RFU.ordinal()] = isBitSet(19, data);
	readAccess[DataGroup.DG14_RFU.ordinal()] = isBitSet(18, data);
	readAccess[DataGroup.DG15_RFU.ordinal()] = isBitSet(17, data);
	readAccess[DataGroup.DG16_RFU.ordinal()] = isBitSet(16, data);
	readAccess[DataGroup.DG17_PlaceOfResidence.ordinal()] = isBitSet(15, data);
	readAccess[DataGroup.DG18_CommunityID.ordinal()] = isBitSet(14, data);
	readAccess[DataGroup.DG19_ResidencePermitI.ordinal()] = isBitSet(13, data);
	readAccess[DataGroup.DG20_ResidencePermitII.ordinal()] = isBitSet(12, data);
	readAccess[DataGroup.DG21_OptionalDataRW.ordinal()] = isBitSet(11, data);

    }

    private boolean isBitSet(int position, byte[] array) {
	return ((array[position / 8] & (128 >> (position % 8))) > 0);
    }

    public byte[] getBytes() throws TLVException {

	/* temporarily solution */
	byte[] bytes = new byte[] { 0x7f, 0x4c, 0x12, 0x06, 0x09, 0x04, 0x00, 0x7f, 0x00, 0x07, 0x03, 0x01, 0x02, 0x02, 0x53, 0x05 };
	byte[] data = new byte[5];

	// Role is encoded in bit 0 and 1
	switch (this.role) {
	case CVCA:
	    data[0] |= 0xC0;
	    break;
	case DV_official:
	    data[0] |= 0x80;
	    break;
	case DV_unofficial:
	    data[0] |= 0x40;
	    break;
	case AuthenticationTerminal:
	    break;
	}

	// Special Functions are encoded in Bit 32 to 39
	for (int i = 0; i < specialFunctions.length; i++) {
	    if (specialFunctions[i])
		setBit(i + 32, data);
	}

	for (int i = 0; i < readAccess.length; i++) {
	    if (readAccess[i])
		setBit(31 - i, data);
	}

	for (int i = 0; i < writeAccess.length; i++) {
	    if (writeAccess[i])
		setBit(i + 2, data);
	}

	return ByteUtils.concatenate(bytes, data);
    }

    private void setBit(int position, byte[] data) {
	data[position / 8] |= (128 >> (position % 8));
    }

    public String toString() {
	try {
	    return ByteUtils.toHexString(this.getBytes());
	} catch (TLVException e) {
	    return "";
	}
    }

    public void setSpecialFunctions(boolean[] specialFunctions2) {
	this.specialFunctions = specialFunctions2;

    }

    public void setReadAccess(boolean[] readAccess2) {
	this.readAccess = readAccess2;
    }

}
