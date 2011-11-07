package org.openecard.client.common.tlv.iso7816;

import org.openecard.client.common.tlv.Parser;
import org.openecard.client.common.tlv.TLV;
import org.openecard.client.common.tlv.TLVException;
import org.openecard.client.common.tlv.Tag;
import org.openecard.client.common.tlv.TagClass;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class CIODDO {

    private final TLV tlv;

    private byte[] providerId;
    private Path odfPath;
    private Path ciaInfoPath;
    private byte[] applicationIdentifier;

    public CIODDO(TLV tlv) throws TLVException {
	if (tlv.getTagNumWithClass() != 0x73) {
	    throw new TLVException("Not of type CIODDO.");
	}
	this.tlv = tlv;

	Parser p = new Parser(tlv.getChild());
	// provider id
	providerId = null;
	if (p.match(new Tag(TagClass.UNIVERSAL, true, 6))) {
	    providerId = p.next(0).getValue();
	}
	// odf path
	odfPath = null;
	if (p.match(new Tag(TagClass.UNIVERSAL, false, 16))) {
	    odfPath = new Path(p.next(0));
	}
	// cia info
	if (p.match(new Tag(TagClass.CONTEXT, false, 0))) {
	    ciaInfoPath = new Path(p.next(0));
	}
	// app id
	if (p.match(new Tag(TagClass.APPLICATION, true, 15))) {
	    applicationIdentifier = p.next(0).getValue();
	}
    }

    public CIODDO(byte[] data) throws TLVException {
	this(TLV.fromBER(data));
    }


    public boolean hasProviderId() {
	return providerId != null;
    }
    public byte[] getProviderId() {
	return providerId;
    }

    public boolean hasOdfPath() {
	return odfPath != null;
    }
    public Path getOdfPath() {
	return odfPath;
    }

    public boolean hasCIAInfoPath() {
	return ciaInfoPath != null;
    }
    public Path getCIAInfoPath() {
	return ciaInfoPath;
    }

    public boolean hasApplicationIdentifier() {
	return applicationIdentifier != null;
    }
    public byte[] getApplicationIdentifier() {
	return applicationIdentifier;
    }

}
