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
 * @author Tobias Wich
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
    public static final BigInteger ECARD_API_VERSION_SUBMINOR = BigInteger.valueOf(5);


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

	    /**
	     * There was some unknown error.
	     * An unexpected error has occurred during processing which cannot be represented by the standard codes or
	     * specific service error codes. The error and detail texts can describe the error more closely.
	     */
	    public static final String UNKNOWN_ERROR = APP_PREFIX + "/common#unknownError";
	    /**
	     * Use of the function by the client application is not permitted.
	     */
	    public static final String NO_PERM = APP_PREFIX + "/common#noPermission";
	    /**
	     * Internal error.
	     */
	    public static final String INT_ERROR = APP_PREFIX + "/common#internalError";
	    public static final String PARM_ERROR = APP_PREFIX + "/common#parameterError";
	    /**
	     * API function unknown.
	     */
	    public static final String UNKNOWN_API = APP_PREFIX + "/common#unknownAPIFunction";
	    /**
	     * Framework or layer not initialised.
	     */
	    public static final String NOT_INITIALIZED = APP_PREFIX + "/common#notInitialized";
	    /**
	     * Warning indicating termination of an active session.
	     */
	    public static final String CON_DISCONNECT = APP_PREFIX + "/common#warningConnectionDisconnected";
	    /**
	     * Warning indicating termination of an active session.
	     */
	    public static final String SESS_TERMINATED = APP_PREFIX + "/common#SessionTerminatedWarning";
	    /**
	     * Parameter error.
	     * There was some problem with a provided or omitted parameter.
	     */
	    public static final String INCORRECT_PARM = APP_PREFIX + "/common#incorrectParameter";
	    public static final String COMMUNICATION_ERROR = APP_PREFIX + "/common#communicationError";

	    /**
	     * Application layer - CardInfo management minor codes.
	     * The error codes of the errors which can occur in conjunction with faulty invocations of the management
	     * functions for CardInfo structures are grouped in the CardInfo category.
	     */
	    public static class CIF {

		/**
		 * CardInfo file cannot be added.
		 */
		public static final String ADD_NOT_POSSIBLE = APP_CIF_PREFIX + "addNotPossible";
		/**
		 * CardInfo file does not exist.
		 */
		public static final String NOT_EXISTING = APP_CIF_PREFIX + "notExisting";
		/**
		 * CardInfo file cannot be deleted.
		 */
		public static final String DEL_NOT_POSSIBLE = APP_CIF_PREFIX + "deleteNotPossible";
		/**
		 * The CardInfo file already exists.
		 */
		public static final String ALREADY_EXISTING = APP_CIF_PREFIX + "alreadyExisting";
		/**
		 * The CardInfo file is incorrect.
		 */
		public static final String INCORRECT_FILE = APP_CIF_PREFIX + "incorrectFile";

	    };

	    /**
	     * Application layer - Framework Update minor codes.
	     * This section describes the error codes which can occur during execution of a framework update.
	     */
	    public static class FW {

		/**
		 * Update service is not accessible.
		 */
		public static final String SERVICE_NA = APP_FW_PREFIX + "serviceNotAvailable";
		/**
		 * Unknown module.
		 */
		public static final String UNKNOWN_MODULE = APP_FW_PREFIX + "unknownModule";
		/**
		 * Invalid version number for module.
		 */
		public static final String INVALID_VERSION = APP_FW_PREFIX + "invalidVersionNumber";
		/**
		 * Operating system not supported.
		 */
		public static final String OS_NOT_SUPPORTED = APP_FW_PREFIX + "operationSystemNotSupported";
		/**
		 * No available space.
		 */
		public static final String NO_SPACE = APP_FW_PREFIX + "noSpaceAvailable";
		/**
		 * Access denied.
		 */
		public static final String SEC_NOT_SATISFIED = APP_FW_PREFIX + "securityConditionsNotSatisfied";

	    };

	    /**
	     * Application layer - IFD minor codes.
	     * The following errors can occur as the result of incorrect information during management of card
	     * terminals. These errors are grouped in the IFD category.
	     */
	    public static class IFD {

		/**
		 * The card terminal configuration cannot be written.
		 */
		public static final String WRITE_CONF_IMPOSS = APP_IFD_PREFIX + "writeConfigurationNotPossible";
		/**
		 * The card terminal cannot be added.
		 */
		public static final String COULD_NOT_ADD = APP_IFD_PREFIX + "couldNotAdd";
		/**
		 * The card terminal cannot be deleted.
		 */
		public static final String DEL_IMPOSS = APP_IFD_PREFIX + "deleteNotPossible";
		/**
		 * The card terminal already exists.
		 */
		public static final String ADD_IMPOSS = APP_IFD_PREFIX + "addNotPossible";

	    };

	    /**
	     * Application layer - Trusted Viewer minor codes.
	     * The following errors can occur during use of the management functions for trusted viewers.
	     */
	    public static class Viewer {

		/**
		 * The trusted viewer cannot be deleted.
		 */
		public static final String DEL_IMPOSS = APP_TV_PREFIX + "deleteNotPossible";
		/**
		 * Invalid TrustedViewerId.
		 */
		public static final String INVALID_ID = APP_TV_PREFIX + "invalidID";
		/**
		 * Invalid configuration information for the trusted viewer.
		 */
		public static final String INVALID_CONF = APP_TV_PREFIX + "invalidConfiguration";
		/**
		 * The trusted viewer already exists with the entered ID.
		 */
		public static final String EXISTING = APP_TV_PREFIX + "alreadyExisting";

	    };

	    /**
	     * Application layer - TSL minor codes.
	     * The following error codes are generated by the functions for management of trust service status lists
	     * (TSLs).
	     */
	    public static class TSL {

		/**
		 * TSLSequenceNumber has been ignored.
		 * As only a {@code TSLSequenceNumber} but no {@code SchemeName} has been specified, the
		 * {@code TSLSequenceNumber}-element has been ignored.
		 */
		public static final String SEQNUM_IGNORED = APP_TSL_PREFIX + "TSLSequenceNumberIgnoredWarning";

	    };

	};


	/**
	 * Dispatcher layer minor codes.
	 * The errors caused by communication to and from the eCard-API-Framework are described here.
	 */
	public static class Disp {

	    private static final String DP_PREFIX = MINOR_PREFIX + "dp";

	    /**
	     * Time exceeded (timeout).
	     * The operation was terminated as the set time was exceeded.
	     */
	    public static final String TIMEOUT = DP_PREFIX + "#timeoutError";
	    public static final String UNKNOWN_CHANNEL_HANDLE = DP_PREFIX + "#unknownChannelHandle";
	    /**
	     * Communication error.
	     */
	    public static final String COMM_ERROR = DP_PREFIX + "#communicationError";
	    /**
	     * Failure to open a trusted channel.
	     */
	    public static final String CHANNEL_ESTABLISHMENT_FAILED = DP_PREFIX + "#trustedChannelEstablishmentFailed";
	    /**
	     * Unknown protocol.
	     */
	    public static final String UNKOWN_PROTOCOL = DP_PREFIX + "#unknownProtocol";
	    /**
	     * Unknown cipher suite.
	     */
	    public static final String UNKNOWN_CIPHER = DP_PREFIX + "#unknownCipherSuite";
	    /**
	     * Unknown web service binding.
	     */
	    public static final String UNKNOWN_BINDING = DP_PREFIX + "#unknownWebserviceBinding";
	    /**
	     * Node not reachable.
	     */
	    public static final String NODE_NOT_REACHABLE = DP_PREFIX + "#nodeNotReachable";
	    // this one is mentioned in EstablishContexResponse TR-03112-6
	    /**
	     * Invalid channel handle.
	     */
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

		/**
		 * Stated hash algorithm is not supported.
		 */
		public static final String HASH_NOT_SUPPORTED = ALG_PFX + "hashAlgorithmNotSupported";
		/**
		 * The stated signature algorithm is not supported.
		 */
		public static final String SIG_NOT_SUPPORTED = ALG_PFX + "signatureAlgorithmNotSupported";

	    };

	    /**
	     * Identity layer - CertificateRequest minor codes.
	     * The errors of the identity layer which can occur when a certificate is requested are grouped in this
	     * category.
	     */
	    public static class CertificateRequest {

		/**
		 * Unknown attribute in the certificate application.
		 */
		public static final String UNKNOWN_ATTR = CR_PFX + "unknownAttribute";
		/**
		 * Generation of the certificate application failed.
		 */
		public static final String CREATION_FAILED = CR_PFX + "creationOfCertificateRequestFailed";
		/**
		 * Submission of the certificate application failed.
		 */
		public static final String SUBMISSION_FAILED = CR_PFX + "submissionFailed";
		/**
		 * Unknown transaction identifier.
		 */
		public static final String UNKOWN_TRANSACTION = CR_PFX + "unknownTransactionID";
		/**
		 * Not possible to collect the certificate.
		 */
		public static final String DOWNLOAD_FAILED = CR_PFX + "certificateDownloadFailed";
		/**
		 * No subject specified in request.
		 */
		public static final String SUBJECT_MISSING = CR_PFX + "subjectMissing";

	    };

	    /**
	     * Identity layer - Encryption minor codes.
	     * The Encryption category contains the errors of the identity layer which occur during encryption or
	     * decryption.
	     */
	    public static class Encryption {

		/**
		 * Specific nodes can only be encrypted in case of an XML document.
		 */
		public static final String NODES_ENC = ENC_PFX + "encryptionOfCertainNodesOnlyForXMLDocuments";
		/**
		 * The encryption format is not supported.
		 */
		public static final String FORMAT_NOT_SUPPORTED = ENC_PFX + "encryptionFormatNotSupported";
		/**
		 * The encryption certificate of an intended recipient is invalid.
		 */
		public static final String INVALID_CERT = ENC_PFX + "invalidCertificate";

	    };

	    /**
	     * Identity layer - Key minor codes.
	     * The errors of the identity layer which can occur when a key is generated or keys are used are grouped in
	     * this category.
	     */
	    public static class Key {

		/**
		 * Key generation is not possible.
		 */
		public static final String KEYGEN_NOT_POSSIBLE = KEY_PFX + "keyGenerationNotPossible";
		/**
		 * The stated encryption algorithm is not supported.
		 */
		public static final String ENC_ALG_NOT_SUPPORTED = KEY_PFX + "encryptionAlgorithmNotSupported";

	    };

	    /**
	     * Identity layer - Service minor codes.
	     * The Service category contains the errors of the identity layer which occur due to the non-accessibility
	     * of the service to be used.
	     */
	    public static class Service {

		/**
		 * The OCSP responder is inaccessible.
		 */
		public static final String OCSP_UNREACHABLE = SERVICE_PFX + "ocspResponderUnreachable";
		/**
		 * The directory service is inaccessible.
		 */
		public static final String DIR_UNREACHABLE = SERVICE_PFX + "directoryServiceUnreachable";
		/**
		 * The time stamp service is inaccessible.
		 */
		public static final String TS_UNREACHABLE = SERVICE_PFX + "timeStampServiceUnreachable";

	    };

	    /**
	     * Identity layer - Signature minor codes.
	     * All errors and warnings of the identity layer which occur during signature generation or signature
	     * verification are assigned to the Signature category.
	     */
	    public static class Signature {

		/**
		 * The signature format is not supported.
		 * The stated signature or time stamp format is not supported.
		 */
		public static final String FORMAT_NOT_SUPPORTED = SIG_PFX + "signatureFormatNotSupported";
		/**
		 * PDF signature for non-PDF document requested.
		 */
		public static final String SIG_NON_PDFDOC = SIG_PFX + "PDFSignatureForNonPDFDocument";
		/**
		 * IncludeEContent not possible.
		 * This warning is returned if the {@code IncludeEContent} flag is set when a PDF signature or a time
		 * stamp is generated, or when a hash value is transmitted for signature generation.
		 */
		public static final String INCLUDE_ECONTENT_NOT_POSSIBLE = SIG_PFX + "unableToIncludeEContentWarning";
		/**
		 * The SignaturePlacement flag was ignored.
		 * This warning is returned when the {@code SignaturePlacement} flag was set for a non-XML-based signature.
		 */
		public static final String IGNORE_SIG_PLACEMENT = SIG_PFX + "ignoredSignaturePlacementFlagWarning";
		/**
		 * The certificate is not available.
		 * The stated certificate is not available for the function. This could be due to an incorrect
		 * reference or a deleted data field.
		 */
		public static final String CERT_NOT_FOUND = SIG_PFX + "certificateNotFound";
		/**
		 * The certificate cannot be interpreted.
		 * The format of the stated certificate is unknown and cannot be interpreted.
		 */
		public static final String WRONG_CERT_FORMAT = SIG_PFX + "certificateFormatNotCorrect";
		/**
		 * Invalid certificate reference.
		 */
		public static final String INVALID_CERT_REF = SIG_PFX + "invalidCertificateReference";
		/**
		 * The certificate chain is interrupted.
		 * The stated certificate chain is interrupted. It is therefore not possible to complete full
		 * verification up to the root certificate.
		 */
		public static final String CHAIN_INTERRUPTED = SIG_PFX + "certificateChainInterrupted";
		/**
		 * It was not possible to resolve the object reference.
		 */
		public static final String OBJREF_UNRESOLVEABLE = SIG_PFX + "resolutionOfObjectReferenceImpossible";
		/**
		 * The transformation algorithm is not supported.
		 */
		public static final String TRANSFORM_NOT_SUPPORTED = SIG_PFX + "transformationAlgorithmNotSupported";
		/**
		 * The viewer is unknown or not available.
		 */
		public static final String UNKNOWN_VIEWER = SIG_PFX + "unknownViewer";
		/**
		 * The certificate path was not checked.
		 * Due to some problems it was not possible to validate the certificate path.
		 */
		public static final String CERT_PATH_NOT_VALIDATED = SIG_PFX + "certificatePathNotValidated";
		/**
		 * The certificate status was not checked.
		 * Due to some problems it was not possible to check the certificate status.
		 */
		public static final String CERT_STATUS_NOT_CHECKED = SIG_PFX + "certificateStatusNotChecked";
		/**
		 * The signature manifest was not verified.
		 * This is a warning.
		 */
		public static final String SIG_MANIFEST_NOT_CHECKED = SIG_PFX + "signatureManifestNotCheckedWarning";
		/**
		 * The suitability of the signature and hash algorithms was not checked.
		 */
		public static final String ALGS_NOT_CHECKED = SIG_PFX + "suitabilityOfAlgorithmsNotChecked";
		/**
		 * No signature-related data were found (detached signature without EContent).
		 */
		public static final String DETACHED_NO_CONTENT = SIG_PFX + "detachedSignatureWithoutEContent";
		/**
		 * It is not possible to interpret revocation information.
		 */
		public static final String WRONG_REVOCATION_INFO = SIG_PFX + "improperRevocationInformation";
		/**
		 * The signature format is incorrect.
		 * The format of the transmitted signature does not correspond to the respective specification. This
		 * error occurs when a supported format is recognised (e.g. in accordance with [RFC3275] or [RFC3369]),
		 * but the signature does not meet the respective form requirements. If the transmitted format was
		 * already not recognised, error {@link #FORMAT_NOT_SUPPORTED} is returned.
		 */
		public static final String INVALID_SIG_FORMAT = SIG_PFX + "invalidSignatureFormat";
		/**
		 * The security of the signature algorithm is not suitable at the relevant point of time.
		 */
		public static final String SIGALG_NOT_SUITABLE = SIG_PFX + "signatureAlgorithmNotSuitable";
		/**
		 * The security of the hash algorithm is not suitable at the relevant point of time.
		 */
		public static final String HASHALG_NOT_SUITABLE = SIG_PFX + "hashAlgorithmNotSuitable";
		/**
		 * The certificate path is invalid.
		 */
		public static final String CERT_PATH_INVALID = SIG_PFX + "invalidCertificatePath";
		/**
		 * The certificate has been revoked.
		 */
		public static final String CERT_REVOKED = SIG_PFX + "certificateRevoked";
		/**
		 * The reference time is outside the validity period of a certificate.
		 */
		public static final String CERT_EXPIRED = SIG_PFX + "referenceTimeNotWithinCertificateValidityPeriod";
		/**
		 * Invalid extensions in a certificate.
		 */
		public static final String INVALID_CERT_EXT = SIG_PFX + "invalidCertificateExtension";
		/**
		 * Verification of a signature manifest has failed.
		 */
		public static final String SIG_MANIFEST_WRONG = SIG_PFX + "signatureManifestNotCorrect";
		/**
		 * The stated {@code SignatureType} does not support {@code SignatureForm} parameter.
		 */
		public static final String SIG_NOT_SUPPORT_FORM_CLARIFICATION = SIG_PFX + "signatureTypeDoesNot" +
			"SupportSignatureFormClarificationWarning";
		/**
		 * Unknown {@code SignatureForm}.
		 */
		public static final String UNKNOWN_SIG_FORM = SIG_PFX + "unknownSignatureForm";
		/**
		 * {@code IncludeObject} only permitted with XML signatures.
		 */
		public static final String INCLUDE_ONLY_XML = SIG_PFX + "includeObjectOnlyForXMLSignatureAllowedWarning";
		/**
		 * It was not possible to resolve the XPath expression.
		 */
		public static final String XPATH_ERROR = SIG_PFX + "xPathEvaluationError";
		/**
		 * Wrong message digest.
		 * The calculated digest of the message is not equal to the message digest in the
		 * {@code MessageDigest}-attribute of the CMS-Signature or the {@code DigestValue}-element of the
		 * XML-signature respectively.
		 */
		public static final String WRONG_MSG_DIGEST = SIG_PFX + "wrongMessageDigest";
		/**
		 * IFD inconsistency.
		 * The compatibility-check with the present IFD failed.
		 */
		public static final String IFD_INCONSISTENCY = SIG_PFX + "IFDInconsistency";
	    };

	    /**
	     * Identity layer - Viewer minor codes.
	     * The Viewer category contains the errors which occur in connection with the trusted viewer within the
	     * identity layer.
	     */
	    public static class Viewer {

		/**
		 * Unsuitable stylesheet for transmitted document.
		 */
		public static final String UNSUITABLE_SS = VIEW_PFX + "unsuitableStylesheetForDocument";
		/**
		 * Cancellation by the viewer.
		 */
		public static final String USER_CANCEL = VIEW_PFX + "cancelationByUser";
		/**
		 * Time exceeded (timeout).
		 * The operation was terminated as the set time was exceeded.
		 */
		public static final String TIMEOUT = VIEW_PFX + "timeout";
		/**
		 * The ViewerMessage is too long.
		 */
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

	    /**
	     * Cancellation by the user.
	     * A necessary user intervention (e.g. PIN entry or confirmation of the signature generation in the trusted
	     * viewer) was terminated by cancellation.
	     */
	    public static final String CANCELLATION_BY_USER = SAL_PFX + "#cancellationByUser";
	    /**
	     * The name already exists.
	     */
	    public static final String NAME_EXISTS = SAL_PFX + "#nameExists";
	    /**
	     * The prerequisite is not met.
	     */
	    public static final String PREREQUISITES_NOT_SATISFIED = SAL_PFX + "#prerequisitesNotSatisfied";
	    /**
	     * Unknown protocol.
	     */
	    public static final String PROTOCOL_NOT_RECOGNIZED = SAL_PFX + "#protocolNotRecognized";
	    /**
	     * Unsuitable protocol for the required action.
	     */
	    public static final String INAPPROPRIATE_PROTOCOL_FOR_ACTION = SAL_PFX + "#inappropriateProtocolForAction";
	    /**
	     * The verified signature is not valid.
	     */
	    public static final String INVALID_SIGNATURE = SAL_PFX + "#invalidSignature";
	    /**
	     * The selected key is not valid.
	     */
	    public static final String INVALID_KEY = SAL_PFX + "#invalidKey";
	    /**
	     * No initialisation carried out.
	     * The used operation requires initialisation.
	     */
	    public static final String NOT_INITIALIZED = SAL_PFX + "#notInitialized";
	    /**
	     * Warning - Too many results.
	     */
	    public static final String TOO_MANY_RESULTS = SAL_PFX + "#tooManyResults";
	    /**
	     * Warning - The connection has been disconnected.
	     */
	    public static final String WARN_CONNECTION_DISCONNECT = SAL_PFX + "#warningConnectionDisconnected";
	    /**
	     * Warning – An established session was terminated.
	     */
	    public static final String WARN_SESSION_END = SAL_PFX + "#warningSessionEnded";
	    /**
	     * The name does not exist.
	     * The stated name of a card application service, DID, Data Set etc. does not exist.
	     */
	    public static final String NAMED_ENTITY_NOT_FOUND = SAL_PFX + "#namedEntityNotFound";
	    /**
	     * The resources are insufficient.
	     */
	    public static final String INSUFFICIENT_RES = SAL_PFX + "#insufficientResources";
	    /**
	     * Access denied.
	     */
	    public static final String SECURITY_CONDITION_NOT_SATISFIED = SAL_PFX + "#securityConditionNotSatisfied";
	    /**
	     * Exclusive reservation is not possible.
	     * Exclusive reservation of the eCard is not possible, because other applications are currently accessing
	     * the eCard.
	     */
	    public static final String EXCLUSIVE_NA = SAL_PFX + "#exclusiveNotAvailable";
	    /**
	     * Warning - there is no active session.
	     * This warning indicates that there is no active session, which can be terminated with
	     * {@code CardApplicationEndSession}.
	     */
	    public static final String NO_SESSION = SAL_PFX + "#noActiveSession";
	    /**
	     * Decryption not possible.
	     * No suitable keys for decryption found.
	     */
	    public static final String DEC_NOT_POSSIBLE = SAL_PFX + "#decryptionNotPossible";
	    /**
	     * Invalid access control information.
	     */
	    public static final String INVALID_ACL = SAL_PFX + "#invalidAccessControlInformation";
	    /**
	     * Unknown protocol.
	     * The requested protocol is unknown.
	     */
	    public static final String UNKOWN_PROTOCOL = SAL_PFX + "#unknownProtocol";
	    /**
	     * Unknown card type specified.
	     */
	    public static final String UNKNOWN_CARDTYPE = SAL_PFX + "#unknownCardType";
	    /**
	     * Unknown DID name specified.
	     */
	    public static final String UNKOWN_DID_NAME = SAL_PFX + "#unknownDIDName";
	    /**
	     * File not found.
	     */
	    public static final String FILE_NOT_FOUND = SAL_PFX + "#fileNotFound";
	    // Not in TR-03112-1, but GetCardInfoOrACD call ?!?
	    public static final String UNKNOWN_HANDLE = SAL_PFX + "#unknownConnectionHandle";
	    /**
	     * The requested session could not be created as one with the same identifier already exists.
	     */
	    public static final String SESSION_EXISTS = SAL_PFX + "#sessionAlreadyExists";

	    /**
	     * Service-Access layer - Support-Interface minor codes.
	     */
	    public static class Support {

		/**
		 * Encoding not possible.
		 */
		public static final String ENCODING_ERROR = SUPPORT_PFX + "encodingError";
		/**
		 * Decoding not possible.
		 */
		public static final String DECODING_ERROR = SUPPORT_PFX + "decodingError";
		/**
		 * Validation of the schema has failed.
		 */
		public static final String SCHEMA_VAILD_FAILED = SUPPORT_PFX + "schemaValidationError";
		/**
		 * A warning occurred during validation of the schema.
		 */
		public static final String SCHEMA_VAILD_WARN = SUPPORT_PFX + "schemaValidationWarning";
		/**
		 * No suitable schema is available.
		 */
		public static final String NO_SCHEMA = SUPPORT_PFX + "noAppropriateSchema";
		/**
		 * The CardInfo repository server is not accessible.
		 */
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

	    /**
	     * Time exceeded (timeout).
	     * The operation was terminated as the set time was exceeded.
	     */
	    public static final String TIMEOUT_ERROR = IFD_PREFIX + "/common#timeoutError";
	    /**
	     * Unknown context handle.
	     */
	    public static final String INVALID_CONTEXT_HANDLE = IFD_PREFIX + "/common#invalidContextHandle";
	    /**
	     * Cancellation by the user.
	     * A necessary user intervention (e.g. PIN entry) was terminated by cancellation.
	     */
	    public static final String CANCELLATION_BY_USER = IFD_PREFIX + "#cancellationByUser";
	    /**
	     * Unknown session identifier.
	     */
	    public static final String UNKNOWN_SESSION = IFD_PREFIX + "/common#unkownSessionIdentifier";
	    /**
	     * Unknown slot handle.
	     */
	    public static final String INVALID_SLOT_HANDLE = IFD_PREFIX + "/common#invalidSlotHandle";

	    // Not yet specified by the BSI
	    public static final String PASSWORD_SUSPENDED = IFD_PREFIX + "/passwordSuspended";
	    public static final String PASSWORD_BLOCKED = IFD_PREFIX + "/passwordBlocked";
	    public static final String PASSWORD_ERROR = IFD_PREFIX + "/passwordError";
	    public static final String PASSWORD_DEACTIVATED = IFD_PREFIX + "/passwordDeactivated";
	    public static final String AUTHENTICATION_FAILED = IFD_PREFIX + "/authenticationFailed";
	    public static final String PASSWORDS_DONT_MATCH = IFD_PREFIX + "/passwordDontMatch";
	    public static final String UNKNOWN_ERROR = IFD_PREFIX + "/unknownError";

	    /**
	     * IFD layer - IO minor codes.
	     * The errors of the Terminal-Layer which occur in connection with the input or output of data on the
	     * terminal are grouped in the IO category.
	     */
	    public static class IO {

		/**
		 * Unknown input unit.
		 */
		public static final String UNKNOWN_INPUT_UNIT = IO_PREFIX + "unknownInputUnit";
		/**
		 * Unknown display index.
		 */
		public static final String UNKNOWN_DISPLAY_INDEX = IO_PREFIX + "unknownDisplayIndex";
		/**
		 * It is not possible to cancel the command.
		 */
		public static final String CANCEL_NOT_POSSIBLE = IO_PREFIX + "cancelNotPossible";
		/**
		 * No smart card transaction has been started.
		 */
		public static final String NO_TRANSACTION_STARTED = IFD_PREFIX + "noTransactionStarted";
		/**
		 * Newly recorded identification data do not correspond.
		 */
		public static final String REPEATED_DATA_MISMATCH = IFD_PREFIX + "repeatedDataMismatch";
		/**
		 * Unknown pin format.
		 */
		public static final String UNKNOWN_PIN_FORMAT = IO_PREFIX + "unknownPINFormat";
		/**
		 * Unknwon output device.
		 */
		public static final String UNKNOWN_OUTPUT_DEVICE = IO_PREFIX + "unknownOutputDevice";
		/**
		 * Unknown biometric sub-type.
		 */
		public static final String UNKNOWN_BIOMETRIC_TYPE = IO_PREFIX + "unknownBiometricSubtype";

	    };

	    /**
	     * IFD layer - Terminal minor codes.
	     * The Terminal category groups the errors which occur due to the status or properties of the terminal.
	     */
	    public static class Terminal {

		/**
		 * The card terminal does not exist.
		 * The addressed card terminal ({@code IFDName}) is unknown.
		 */
		public static final String UNKNOWN_IFD = TERM_PREFIX + "unknownIFD";
		/**
		 * No eCard available.
		 * The request was not successful, because there is no card captured by the indicated slot.
		 */
		public static final String NO_CARD = TERM_PREFIX + "noCard";
		/**
		 * The request was not successful, because the card is already used by another process.
		 */
		public static final String IFD_SHARING_VIOLATION = TERM_PREFIX + "IFDSharingViolation";
		/**
		 * Unknown Action.
		 * The requested action to be performed is unknown.
		 */
		public static final String UNKNOWN_ACTION = TERM_PREFIX + "unknownAction";
		/**
		 * Unknown Slot.
		 */
		public static final String UNKNOWN_SLOT = TERM_PREFIX + "unknownSlot";
		/**
		 * Access Error.
		 */
		public static final String ACCESS_ERROR = TERM_PREFIX + "accessError";
		/**
		 * Access Error.
		 */
		public static final String PREPARE_DEVICES_ERROR = TERM_PREFIX + "prepareDevicesError";
	    };

	};

    };

    private static final TreeMap<String,String> msgMap = new TreeMap<>();

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
