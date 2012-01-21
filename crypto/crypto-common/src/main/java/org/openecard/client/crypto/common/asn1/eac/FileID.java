/*
 * Copyright 2012 Moritz Horsch.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openecard.client.crypto.common.asn1.eac;

import org.openecard.bouncycastle.asn1.ASN1OctetString;
import org.openecard.bouncycastle.asn1.ASN1Sequence;


/**
 * @author Moritz Horsch <horsch at cdc.informatik.tu-darmstadt.de>
 */
public final class FileID {

    private byte[] fid;
    private byte[] sfid;

    /**
     * Instantiates a new file id.
     *
     * @param seq the ASN1 encoded sequence
     */
    public FileID(ASN1Sequence seq) {
        if (seq.size() == 1) {
            fid = ASN1OctetString.getInstance(seq.getObjectAt(0)).getOctets();

        } else if (seq.size() == 2) {
            fid = ASN1OctetString.getInstance(seq.getObjectAt(0)).getOctets();
            sfid = ASN1OctetString.getInstance(seq.getObjectAt(1)).getOctets();
        } else {
            throw new IllegalArgumentException("Sequence wrong size for FileID");
        }
    }

    /**
     * Gets the single instance of FileID.
     *
     * @param obj
     * @return single instance of FileID
     */
    public static FileID getInstance(Object obj) {
        if (obj == null || obj instanceof FileID) {
            return (FileID) obj;
        } else if (obj instanceof ASN1Sequence) {
            return new FileID((ASN1Sequence) obj);
        }

        throw new IllegalArgumentException("Unknown object in factory: " + obj.getClass().getName());
    }

    /**
     * Gets the file identifier (FID).
     *
     * @return the FID
     */
    public byte[] getFID() {
        return fid;
    }

    /**
     * Gets the short file identifier (SFID).
     *
     * @return the SFID
     */
    public byte[] getSFID() {
        return sfid;
    }
}
