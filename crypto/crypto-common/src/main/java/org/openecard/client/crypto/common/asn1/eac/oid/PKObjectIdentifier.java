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
package org.openecard.client.crypto.common.asn1.eac.oid;


/**
 * See BSI-TR-03110, version 2.10
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public abstract interface PKObjectIdentifier extends EACObjectIdentifier {

    /**
     * id-PK-DH OBJECT IDENTIFIER ::= {id-PK 1}
     */
    public static final String id_PK_DH = id_PK + ".1";
    /**
     * id-PK-ECDH OBJECT IDENTIFIER ::= {id-PK 2}
     */
    public static final String id_PK_ECDH = id_PK + ".2";

}
