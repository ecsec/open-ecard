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

package org.openecard.common;

import java.math.BigInteger;
import java.util.TreeMap;


/**
 * Constants regarding the eCard-API-Framework.
 * Most of the values in here are defined in BSI TR-03112-1.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class ECardConstants {

    private static final String ECARD_PREFIX = "http://www.bsi.bund.de/ecard/api/1.1/";
    private static final String MAJOR_PREFIX = ECARD_PREFIX + "resultmajor#";
    private static final String DSS_PREFIX   = "urn:oasis:names:tc:dss:1.0:";
    private static final String MINOR_PREFIX = ECARD_PREFIX + "resultminor/";


    //
    // Inofficial Constants
    //

    public static final int CONTEXT_HANDLE_DEFAULT_SIZE = 16;
    public static final int SLOT_HANDLE_DEFAULT_SIZE = 24;

    public static final String UNKNOWN_CARD = "http://bsi.bund.de/cif/unknown";
    public static final String PAOS_NEXT = ECARD_PREFIX + "PAOS/GetNextCommand";

    public static final String ACTOR_NEXT = "http://schemas.xmlsoap.org/soap/actor/next";
    public static final String SOAP_ENVELOPE = "http://schemas.xmlsoap.org/soap/envelope/";
    public static final String PAOS_VERSION_11 = "urn:liberty:paos:2003-08";
    public static final String PAOS_VERSION_20 = "urn:liberty:paos:2006-08";
    public static final String WS_ADDRESSING = "http://www.w3.org/2005/03/addressing";

    public static final BigInteger ECARD_API_VERSION_MAJOR = BigInteger.valueOf(1);
    public static final BigInteger ECARD_API_VERSION_MINOR = BigInteger.valueOf(1);
    public static final BigInteger ECARD_API_VERSION_SUBMINOR = BigInteger.valueOf(4);


    public static class CIF {

	public static final String GET_SPECIFIED = ECARD_PREFIX + "cardinfo/action#getSpecifiedFile";
	public static final String GET_RELATED = ECARD_PREFIX + "cardinfo/action#getRelatedFiles";
	public static final String GET_OTHER = ECARD_PREFIX + "cardinfo/action#getOtherFiles";

    };

    public static class Profile {

	public static final String ECARD_1_1 = "http://www.bsi.bund.de/ecard/api/1.1";

    };

    public static class Protocol {

	public static final String PIN_COMPARE    = "urn:oid:1.3.162.15480.3.0.9";
	public static final String MUTUAL_AUTH    = "urn:oid:1.3.162.15480.3.0.12";
	public static final String EAC_GENERIC    = "urn:oid:1.3.162.15480.3.0.14";
	public static final String EAC2           = "urn:oid:1.3.162.15480.3.0.14.2";
	public static final String RSA_AUTH       = "urn:oid:1.3.162.15480.3.0.15";
	public static final String GENERIC_CRYPTO = "urn:oid:1.3.162.15480.3.0.25";
	public static final String TERMINAL_AUTH  = "urn:oid:0.4.0.127.0.7.2.2.2";
	public static final String CHIP_AUTH      = "urn:oid:0.4.0.127.0.7.2.2.3";
	public static final String PACE           = "urn:oid:0.4.0.127.0.7.2.2.4";
	public static final String RESTRICTED_ID  = "urn:oid:0.4.0.127.0.7.2.2.5";

    };

    public static class IFD {

	public static class Protocol {

	    public static final String T0 = "urn:iso:std:iso-iec:7816:-3:tech:protocols:T-equals-0";
	    public static final String T1 = "urn:iso:std:iso-iec:7816:-3:tech:protocols:T-equals-1";
	    public static final String T2 = "urn:iso:std:iso-iec:10536:tech:protocols:T-equals-2";
	    public static final String TYPE_A = "urn:iso:std:iso-iec:14443:-2:tech:protocols:Type-A";
	    public static final String TYPE_B = "urn:iso:std:iso-iec:14443:-2:tech:protocols:Type-B";

	};

    };

    //
    // Major values
    //

    public static class Major {

	public static final String OK = MAJOR_PREFIX + "ok";
	public static final String PENDING = DSS_PREFIX + "profiles:asynchronousprocessing:resultmajor:Pending";
	public static final String WARN = MAJOR_PREFIX + "warning";
	public static final String ERROR = MAJOR_PREFIX + "error";
	public static final String NEXT = MAJOR_PREFIX + "nextRequest";

    };


    //
    // Minor values
    //

    public static class Minor {

	/**
	 * Application layer minor codes.
	 * The error codes assigned to this layer indicate operation errors of the applications using the
	 * eCard-API-Framework. These include in particular incorrect or faulty (configuration) data.
	 */
	public static class App {

	    private static final String APP_PREFIX     = MINOR_PREFIX + "al";
	    private static final String APP_CIF_PREFIX = APP_PREFIX + "/CardInfo#";        // CardInfo
	    private static final String APP_FW_PREFIX  = APP_PREFIX + "/FrameworkUpdate#"; // Framework Update
	    private static final String APP_IFD_PREFIX = APP_PREFIX + "/IFD#";             // IFD
	    private static final String APP_TV_PREFIX  = APP_PREFIX + "/TrustedViewer#";   // TrustedViewer
	    private static final String APP_TSL_PREFIX = APP_PREFIX + "/TSL#";             // TrustedViewer

	    public static final String UNKNOWN_ERROR = APP_PREFIX + "/common#unknownError";
	    public static final String NO_PERM = APP_PREFIX + "/common#noPermission";
	    public static final String INT_ERROR = APP_PREFIX + "/common#internalError";
	    public static final String PARM_ERROR = APP_PREFIX + "/common#parameterError";
	    public static final String UNKNOWN_API = APP_PREFIX + "/common#unknownAPIFunction";
	    public static final String NOT_INITIALIZED = APP_PREFIX + "/common#notInitialized";
	    public static final String CON_DISCONNECT = APP_PREFIX + "/common#warningConnectionDisconnected";
	    public static final String SESS_TERMINATED = APP_PREFIX + "/common#SessionTerminatedWarning";
	    public static final String INCORRECT_PARM = APP_PREFIX + "/common#incorrectParameter";
	    public static final String COMMUNICATION_ERROR = APP_PREFIX + "/common#communicationError";

	    /**
	     * Application layer - CardInfo management minor codes.
	     * The error codes of the errors which can occur in conjunction with faulty invocations of the management
	     * functions for CardInfo structures are grouped in the CardInfo category.
	     */
	    public static class CIF {

		public static final String ADD_NOT_POSSIBLE = APP_CIF_PREFIX + "addNotPossible";
		public static final String NOT_EXISTING = APP_CIF_PREFIX + "notExisting";
		public static final String DEL_NOT_POSSIBLE = APP_CIF_PREFIX + "deleteNotPossible";
		public static final String ALREADY_EXISTING = APP_CIF_PREFIX + "alreadyExisting";
		public static final String INCORRECT_FILE = APP_CIF_PREFIX + "incorrectFile";

	    };

	    /**
	     * Application layer - Framework Update minor codes.
	     * This section describes the error codes which can occur during execution of a framework update.
	     */
	    public static class FW {

		public static final String SERVICE_NA = APP_FW_PREFIX + "serviceNotAvailable";
		public static final String UNKNOWN_MODULE = APP_FW_PREFIX + "unknownModule";
		public static final String INVALID_VERSION = APP_FW_PREFIX + "invalidVersionNumber";
		public static final String OS_NOT_SUPPORTED = APP_FW_PREFIX + "operationSystemNotSupported";
		public static final String NO_SPACE = APP_FW_PREFIX + "noSpaceAvailable";
		public static final String SEC_NOT_SATISFIED = APP_FW_PREFIX + "securityConditionsNotSatisfied";

	    };

	    /**
	     * Application layer - IFD minor codes.
	     * The following errors can occur as the result of incorrect information during management of card
	     * terminals. These errors are grouped in the IFD category.
	     */
	    public static class IFD {

		public static final String WRITE_CONF_IMPOSS = APP_IFD_PREFIX + "writeConfigurationNotPossible";
		public static final String COULD_NOT_ADD = APP_IFD_PREFIX + "couldNotAdd";
		public static final String DEL_IMPOSS = APP_IFD_PREFIX + "deleteNotPossible";
		public static final String ADD_IMPOSS = APP_IFD_PREFIX + "addNotPossible";

	    };

	    /**
	     * Application layer - Trusted Viewer minor codes.
	     * The following errors can occur during use of the management functions for trusted viewers.
	     */
	    public static class Viewer {

		public static final String DEL_IMPOSS = APP_TV_PREFIX + "deleteNotPossible";
		public static final String INVALID_ID = APP_TV_PREFIX + "invalidID";
		public static final String INVALID_CONF = APP_TV_PREFIX + "invalidConfiguration";
		public static final String EXISTING = APP_TV_PREFIX + "alreadyExisting";

	    };

	    /**
	     * Application layer - TSL minor codes.
	     * The following error codes are generated by the functions for management of trust service status lists
	     * (TSLs).
	     */
	    public static class TSL {

		public static final String SEQNUM_IGNORED = APP_TSL_PREFIX + "TSLSequenceNumberIgnoredWarning";

	    };

	};


	/**
	 * Dispatcher layer minor codes.
	 * The errors caused by communication to and from the eCard-API-Framework are described here.
	 */
	public static class Disp {

	    private static final String DP_PREFIX = MINOR_PREFIX + "dp";

	    public static final String TIMEOUT = DP_PREFIX + "#timeoutError";
	    public static final String UNKNOWN_CHANNEL_HANDLE = DP_PREFIX + "#unknownChannelHandle";
	    public static final String COMM_ERROR = DP_PREFIX + "#communicationError";
	    public static final String CHANNEL_ESTABLISHMENT_FAILED = DP_PREFIX + "#trustedChannelEstablishmentFailed";
	    public static final String UNKOWN_PROTOCOL = DP_PREFIX + "#unknownProtocol";
	    public static final String UNKNOWN_CIPHER = DP_PREFIX + "#unknownCipherSuite";
	    public static final String UNKNOWN_BINDING = DP_PREFIX + "#unknownWebserviceBinding";
	    public static final String NODE_NOT_REACHABLE = DP_PREFIX + "#nodeNotReachable";
	    // this one is mentioned in EstablishContexResponse TR-03112-6
	    public static final String INVALID_CHANNEL_HANDLE = DP_PREFIX + "#invalidChannelHandle";

	};


	/**
	 * Identity layer minor codes.
	 * Errors caused by the identity layer of the eCard-API-Framework are assigned to the identity layer.
	 */
	public static class Ident {
	    
	    private static final String IL_PREFIX   = MINOR_PREFIX + "il";
	    private static final String ALG_PFX     = IL_PREFIX + "/algorithm#";
	    private static final String CR_PFX      = IL_PREFIX + "/certificateRequest#";
	    private static final String ENC_PFX     = IL_PREFIX + "/encryption#";
	    private static final String KEY_PFX     = IL_PREFIX + "/key#";
	    private static final String SERVICE_PFX = IL_PREFIX + "/service#";
	    private static final String SIG_PFX     = IL_PREFIX + "/signature#";
	    private static final String VIEW_PFX    = IL_PREFIX + "/viewer#";

	    /**
	     * Identity layer - Algorithm minor codes.
	     * The errors of the identity layer caused by the algorithms stated for use are grouped in this category.
	     */
	    public static class Algorithm {

		public static final String HASH_NOT_SUPPORTED = ALG_PFX + "hashAlgorithmNotSupported";
		public static final String SIG_NOT_SUPPORTED = ALG_PFX + "signatureAlgorithmNotSupported";

	    };

	    /**
	     * Identity layer - CertificateRequest minor codes.
	     * The errors of the identity layer which can occur when a certificate is requested are grouped in this
	     * category.
	     */
	    public static class CertificateRequest {

		public static final String UNKNOWN_ATTR = CR_PFX + "unknownAttribute";
		public static final String CREATION_FAILED = CR_PFX + "creationOfCertificateRequestFailed";
		public static final String SUBMISSION_FAILED = CR_PFX + "submissionFailed";
		public static final String UNKOWN_TRANSACTION = CR_PFX + "unknownTransactionID";
		public static final String DOWNLOAD_FAILED = CR_PFX + "certificateDownloadFailed";
		public static final String SUBJECT_MISSING = CR_PFX + "subjectMissing";

	    };

	    /**
	     * Identity layer - Encryption minor codes.
	     * The Encryption category contains the errors of the identity layer which occur during encryption or
	     * decryption.
	     */
	    public static class Encryption {

		public static final String NODES_ENC = ENC_PFX + "encryptionOfCertainNodesOnlyForXMLDocuments";
		public static final String FORMAT_NOT_SUPPORTED = ENC_PFX + "encryptionFormatNotSupported";
		public static final String INVALID_CERT = ENC_PFX + "invalidCertificate";

	    };

	    /**
	     * Identity layer - Key minor codes.
	     * The errors of the identity layer which can occur when a key is generated or keys are used are grouped in
	     * this category.
	     */
	    public static class Key {

		public static final String KEYGEN_NOT_POSSIBLE = KEY_PFX + "keyGenerationNotPossible";
		public static final String ENC_ALG_NOT_SUPPORTED = KEY_PFX + "encryptionAlgorithmNotSupported";

	    };

	    /**
	     * Identity layer - Service minor codes.
	     * The Service category contains the errors of the identity layer which occur due to the non-accessibility
	     * of the service to be used.
	     */
	    public static class Service {

		public static final String OCSP_UNREACHABLE = SERVICE_PFX + "ocspResponderUnreachable";
		public static final String DIR_UNREACHABLE = SERVICE_PFX + "directoryServiceUnreachable";
		public static final String TS_UNREACHABLE = SERVICE_PFX + "timeStampServiceUnreachable";

	    };

	    /**
	     * Identity layer - Signature minor codes.
	     * All errors and warnings of the identity layer which occur during signature generation or signature
	     * verification are assigned to the Signature category.
	     */
	    public static class Signature {

		public static final String FORMAT_NOT_SUPPORTED = SIG_PFX + "signatureFormatNotSupported";
		public static final String SIG_NON_PDFDOC = SIG_PFX + "PDFSignatureForNonPDFDocument";
		public static final String INCLUDE_ECONTENT_NOT_POSSIBLE = SIG_PFX + "unableToIncludeEContentWarning";
		public static final String IGNORE_SIG_PLACEMENT = SIG_PFX + "ignoredSignaturePlacementFlagWarning";
		public static final String CERT_NOT_FOUND = SIG_PFX + "certificateNotFound";
		public static final String WRONG_CERT_FORMAT = SIG_PFX + "certificateFormatNotCorrect";
		public static final String INVALID_CERT_REF = SIG_PFX + "invalidCertificateReference";
		public static final String CHAIN_INTERRUPTED = SIG_PFX + "certificateChainInterrupted";
		public static final String OBJREF_UNRESOLVEABLE = SIG_PFX + "resolutionOfObjectReferenceImpossible";
		public static final String TRANSFORM_NOT_SUPPORTED = SIG_PFX + "transformationAlgorithmNotSupported";
		public static final String UNKNOWN_VIEWER = SIG_PFX + "unknownViewer";
		public static final String CERT_PATH_NOT_VALIDATED = SIG_PFX + "certificatePathNotValidated";
		public static final String CERT_STATUS_NOT_CHECKED = SIG_PFX + "certificateStatusNotChecked";
		public static final String SIG_MANIFEST_NOT_CHECKED = SIG_PFX + "signatureManifestNotCheckedWarning";
		public static final String ALGS_NOT_CHECKED = SIG_PFX + "suitabilityOfAlgorithmsNotChecked";
		public static final String DETACHED_NO_CONTENT = SIG_PFX + "detachedSignatureWithoutEContent";
		public static final String WRONG_REVOCATION_INFO = SIG_PFX + "improperRevocationInformation";
		public static final String INVALID_SIG_FORMAT = SIG_PFX + "invalidSignatureFormat";
		public static final String SIGALG_NOT_SUITABLE = SIG_PFX + "signatureAlgorithmNotSuitable";
		public static final String HASHALG_NOT_SUITABLE = SIG_PFX + "hashAlgorithmNotSuitable";
		public static final String CERT_PATH_INVALID = SIG_PFX + "invalidCertificatePath";
		public static final String CERT_REVOKED = SIG_PFX + "certificateRevoked";
		public static final String CERT_EXPIRED = SIG_PFX + "referenceTimeNotWithinCertificateValidityPeriod";
		public static final String INVALID_CERT_EXT = SIG_PFX + "invalidCertificateExtension";
		public static final String SIG_MANIFEST_WRONG = SIG_PFX + "signatureManifestNotCorrect";
		public static final String SIG_NOT_SUPPORT_FORM_CLARIFICATION = SIG_PFX + "signatureTypeDoesNot" +
			"SupportSignatureFormClarificationWarning";
		public static final String UNKNOWN_SIG_FORM = SIG_PFX + "unknownSignatureForm";
		public static final String INCLUDE_ONLY_XML = SIG_PFX + "includeObjectOnlyForXMLSignatureAllowedWarning";
		public static final String XPATH_ERROR = SIG_PFX + "xPathEvaluationError";
		public static final String WRONG_MSG_DIGEST = SIG_PFX + "wrongMessageDigest";
		public static final String IFD_INCONSISTENCY = SIG_PFX + "IFDInconsistency";
	    };

	    /**
	     * Identity layer - Viewer minor codes.
	     * The Viewer category contains the errors which occur in connection with the trusted viewer within the
	     * identity layer.
	     */
	    public static class Viewer {

		public static final String UNSUITABLE_SS = VIEW_PFX + "unsuitableStylesheetForDocument";
		public static final String USER_CANCEL = VIEW_PFX + "cancelationByUser";
		public static final String TIMEOUT = VIEW_PFX + "timeout";
		public static final String MSG_TOO_LONG = VIEW_PFX + "viewerMessageTooLong";

	    };

	};


	/**
	 * Service-Access layer minor codes.
	 * Errors resulting from the Support-Interface or the ISO24727-3-Interface are assigned to the
	 * Service-Access-Layer.
	 */
	public static class SAL {

	    private static final String SAL_PFX     = MINOR_PREFIX + "sal";
	    private static final String SUPPORT_PFX = SAL_PFX + "/support#";
	    private static final String EAC_PFX = SAL_PFX + "/mEAC#";

	    public static final String CANCELLATION_BY_USER = SAL_PFX + "#cancellationByUser";
	    public static final String NAME_EXISTS = SAL_PFX + "#nameExists";
	    public static final String PREREQUISITES_NOT_SATISFIED = SAL_PFX + "#prerequisitesNotSatisfied";
	    public static final String PROTOCOL_NOT_RECOGNIZED = SAL_PFX + "#protocolNotRecognized";
	    public static final String INAPPROPRIATE_PROTOCOL_FOR_ACTION = SAL_PFX + "#inappropriateProtocolForAction";
	    public static final String INVALID_SIGNATURE = SAL_PFX + "#invalidSignature";
	    public static final String INVALID_KEY = SAL_PFX + "#invalidKey";
	    public static final String NOT_INITIALIZED = SAL_PFX + "#notInitialized";
	    public static final String TOO_MANY_RESULTS = SAL_PFX + "#tooManyResults";
	    public static final String WARN_CONNECTION_DISCONNECT = SAL_PFX + "#warningConnectionDisconnected";
	    public static final String WARN_SESSION_END = SAL_PFX + "#warningSessionEnded";
	    public static final String NAMED_ENTITY_NOT_FOUND = SAL_PFX + "#namedEntityNotFound";
	    public static final String INSUFFICIENT_RES = SAL_PFX + "#insufficientResources";
	    public static final String SECURITY_CONDITINON_NOT_SATISFIED = SAL_PFX + "#securityConditionNotSatisfied";
	    public static final String EXCLUSIVE_NA = SAL_PFX + "#exclusiveNotAvailable";
	    public static final String NO_SESSION = SAL_PFX + "#noActiveSession";
	    public static final String DEC_NOT_POSSIBLE = SAL_PFX + "#decryptionNotPossible";
	    public static final String INVALID_ACL = SAL_PFX + "#invalidAccessControlInformation";
	    public static final String UNKOWN_PROTOCOL = SAL_PFX + "#unknownProtocol";
	    public static final String UNKNOWN_CARDTYPE = SAL_PFX + "#unknownCardType";
	    public static final String UNKOWN_DID_NAME = SAL_PFX + "#unknownDIDName";
	    public static final String FILE_NOT_FOUND = SAL_PFX + "#fileNotFound";
	    // Not in TR-03112-1, but GetCardInfoOrACD call ?!?
	    public static final String UNKNOWN_HANDLE = SAL_PFX + "#unknownConnectionHandle";

	    /**
	     * Service-Access layer - Support-Interface minor codes.
	     */
	    public static class Support {

		public static final String ENCODING_ERROR = SUPPORT_PFX + "encodingError";
		public static final String DECODING_ERROR = SUPPORT_PFX + "decodingError";
		public static final String SCHEMA_VAILD_FAILED = SUPPORT_PFX + "schemaValidationError";
		public static final String SCHEMA_VAILD_WARN = SUPPORT_PFX + "schemaValidationWarning";
		public static final String NO_SCHEMA = SUPPORT_PFX + "noAppropriateSchema";
		public static final String REPO_UNREACHABLE = SUPPORT_PFX + "cardInfoRepositoryUnreachable";

	    };

	    /**
	     * Service-Access layer - EAC minor codes.
	     */
	    public static class EAC {

		public static final String AGE_VERIF_FAILED = EAC_PFX + "AgeVerificationFailedWarning";
		public static final String COMMUNITY_VERIF_FAILED = EAC_PFX + "CommunityVerificationFailedWarning";
		public static final String DOC_VALID_FAILED = EAC_PFX + "DocumentValidityVerificationFailed";

	    };

	};


	/**
	 * IFD layer minor codes.
	 */
	public static class IFD {

	    private static final String IFD_PREFIX = MINOR_PREFIX + "ifdl";
	    private static final String IO_PREFIX = IFD_PREFIX + "/IO#";
	    private static final String TERM_PREFIX = IFD_PREFIX + "/terminal#";

	    public static final String TIMEOUT_ERROR = IFD_PREFIX + "/common#timeoutError";
	    public static final String INVALID_CONTEXT_HANDLE = IFD_PREFIX + "/common#invalidContextHandle";
	    public static final String CANCELLATION_BY_USER = IFD_PREFIX + "#cancellationByUser";
	    public static final String UNKNOWN_SESSION = IFD_PREFIX + "/common#unkownSessionIdentifier";
	    public static final String INVALID_SLOT_HANDLE = IFD_PREFIX + "/common#invalidSlotHandle";

	    // Not yet specified by the BSI
	    public static final String PASSWORD_SUSPENDED = IFD_PREFIX + "/passwordSuspended";
	    public static final String PASSWORD_BLOCKED = IFD_PREFIX + "/passwordBlocked";
	    public static final String PASSWORD_ERROR = IFD_PREFIX + "/passwordError";
	    public static final String PASSWORD_DEACTIVATED = IFD_PREFIX + "/passwordDeactivated";
	    public static final String AUTHENTICATION_FAILED = IFD_PREFIX + "/authenticationFailed";
	    public static final String UNKNOWN_ERROR = IFD_PREFIX + "/unknownError";

	    /**
	     * IFD layer - IO minor codes.
	     * The errors of the Terminal-Layer which occur in connection with the input or output of data on the
	     * terminal are grouped in the IO category.
	     */
	    public static class IO {

		public static final String UNKNOWN_INPUT_UNIT = IO_PREFIX + "unknownInputUnit";
		public static final String UNKNOWN_DISPLAY_INDEX = IO_PREFIX + "unknownDisplayIndex";
		public static final String CANCEL_NOT_POSSIBLE = IO_PREFIX + "cancelNotPossible";
		public static final String NO_TRANSACTION_STARTED = IFD_PREFIX + "noTransactionStarted";
		public static final String REPEATED_DATA_MISMATCH = IFD_PREFIX + "repeatedDataMismatch";
		public static final String UNKNOWN_PIN_FORMAT = IO_PREFIX + "unknownPINFormat";
		public static final String UNKNOWN_OUTPUT_DEVICE = IO_PREFIX + "unknownOutputDevice";
		public static final String UNKNOWN_BIOMETRIC_TYPE = IO_PREFIX + "unknownBiometricSubtype";

	    };

	    /**
	     * IFD layer - Terminal minor codes.
	     * The Terminal category groups the errors which occur due to the status or properties of the terminal.
	     */
	    public static class Terminal {

		public static final String UNKNOWN_IFD = TERM_PREFIX + "unknownIFD";
		public static final String NO_CARD = TERM_PREFIX + "noCard";
		public static final String IFD_SHARING_VIOLATION = TERM_PREFIX + "IFDSharingViolation";
		public static final String UNKNOWN_ACTION = TERM_PREFIX + "unknownAction";
		public static final String UNKNOWN_SLOT = TERM_PREFIX + "unknownSlot";
		public static final String ACCESS_ERROR = TERM_PREFIX + "accessError";

	    };

	};

    };

    private static final TreeMap<String,String> msgMap = new TreeMap<String, String>();

    static {
	// major types
	msgMap.put(Major.OK, "No error occurred during execution of the operation.");
	msgMap.put(Major.WARN, "If the result of the operation is in principle OK, but there is a detail which may " +
		"require closer investigation, a warning is given as a response.");
	msgMap.put(Major.ERROR, "An error occurred during execution of the operation.");
	msgMap.put(Major.NEXT, "This result appears if at least one more request is expected within a protocol.");

	// minor App
	// minor App common
	msgMap.put(Minor.App.NO_PERM, "Use of the function by the client application is not permitted.");
	msgMap.put(Minor.App.INT_ERROR, "Internal error.");
	msgMap.put(Minor.App.PARM_ERROR, "There was some problem with a provided or omitted parameter.");

	// minor SAL
	msgMap.put(Minor.SAL.UNKNOWN_HANDLE, "Unknown connection handle specified.");
	msgMap.put(Minor.SAL.UNKNOWN_CARDTYPE, "Unknown card type specified.");
	// minor SAL support
	msgMap.put(Minor.SAL.Support.REPO_UNREACHABLE, "The CardInfo repository server is not accessible");
    }

    @Deprecated
    public static String URI2Msg(final String uri) {
	String result = msgMap.get(uri);
	return (result != null) ? result : "";
    }

}
