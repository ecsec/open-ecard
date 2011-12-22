package org.openecard.client.common.sal;


/**
 * Enum with all functions that are protocol specific.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public enum FunctionType {

    CardApplicationStartSession,
    CardApplicationEndSession,
    Encipher,
    Decipher,
    GetRandom,
    Hash,
    Sign,
    VerifySignature,
    VerifyCertificate,
    DIDCreate,
    DIDGet,
    DIDUpdate,
    DIDDelete,
    DIDAuthenticate;

}
