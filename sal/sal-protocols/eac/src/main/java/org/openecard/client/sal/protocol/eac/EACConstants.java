/*
 * Copyright 2012 Moritz Horsch.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openecard.client.sal.protocol.eac;


/**
 * Defines constants for the EAC protocol.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class EACConstants {

    // EF.CardSecurity file identifier
    public static final short EF_CARDSECURITY_FID = (short) 0x011D;
    // Internal data
    protected static final String INTERNAL_DATA_CERTIFICATES = "Certificates";
    protected static final String INTERNAL_DATA_AUTHENTICATED_AUXILIARY_DATA = "AuthenticatedAuxiliaryData";
    protected static final String INTERNAL_DATA_PK_PCD = "PKPCD";
    protected static final String INTERNAL_DATA_SECURITY_INFOS = "SecurityInfos";

}
