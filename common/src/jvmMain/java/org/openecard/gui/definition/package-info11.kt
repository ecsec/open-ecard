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
 */
/**
 * User consent definition classes.
 * The definition classes are used to describe the user consent.<br></br>
 * In order to create a user consent, [Step] instances must be added to a [org.openecard.gui.UserConsent]
 * instance. Each Step can contain elements (subclasses of [InfoUnit]. The [InputInfoUnit] interface is the
 * base of elements which have no output values, while the [OutputInfoUnit] interface represents elements which do
 * have an output value.
 */
package org.openecard.gui.definition

import org.openecard.common.util.ValueGenerators.genBase64Session
import org.openecard.common.interfaces.Dispatcher.safeDeliver
import org.openecard.common.WSHelper.checkResult
import org.openecard.common.ECardException.Companion.makeOasisResultTraitImpl
import org.openecard.common.ECardException.message
import org.openecard.common.WSHelper.makeResultError
import org.openecard.common.WSHelper.createException
import org.openecard.common.util.FileUtils.resolveResourceAsURL
import org.openecard.common.util.JAXBUtils.deepCopy
import org.openecard.common.interfaces.EventFilter.matches
import org.openecard.common.interfaces.EventCallback.signalEvent
import org.openecard.common.util.FileUtils.resolveResourceAsStream
import org.openecard.common.util.FileUtils.homeConfigDir
import org.openecard.ws.common.OverridingProperties.getProperty
import org.openecard.ws.common.OverridingProperties.properties
import org.openecard.gui.file.AcceptAllFilesFilter
import org.openecard.gui.executor.ExecutionResults
import org.openecard.gui.StepResult
import org.openecard.gui.executor.StepActionResult
import org.openecard.gui.executor.StepAction
import org.openecard.gui.ResultStatus
import org.openecard.gui.executor.StepActionResultStatus
import org.openecard.gui.UserConsentNavigator
import org.openecard.common.ThreadTerminateException
import org.openecard.gui.definition.OutputInfoUnit
import org.openecard.gui.definition.InputInfoUnit
import org.openecard.gui.executor.StepActionCallable
import org.openecard.common.interfaces.InvocationTargetExceptionUnchecked
import org.openecard.gui.executor.BackgroundTask
import org.openecard.common.util.ValueGenerators
import org.openecard.gui.executor.DummyAction
import org.openecard.gui.definition.IDTrait
import org.openecard.gui.definition.InfoUnitElementType
import org.openecard.gui.definition.InfoUnit
import org.openecard.gui.definition.AbstractBox
import org.openecard.gui.definition.AbstractTextField
import org.openecard.gui.definition.BoxItem
import org.openecard.gui.definition.SignatureField
import org.openecard.gui.file.FileDialogResult
import org.openecard.gui.definition.UserConsentDescription
import org.openecard.gui.message.MessageDialogResult
import org.openecard.common.tlv.iso7816.FCP
import org.openecard.common.tlv.iso7816.FMD
import org.openecard.common.tlv.TLVException
import org.openecard.common.tlv.iso7816.DataElements
import org.openecard.common.tlv.iso7816.ApplicationTemplate
import org.openecard.common.tlv.TagClass
import org.openecard.common.tlv.iso7816.CIOChoice
import org.openecard.common.apdu.exception.APDUException
import org.openecard.common.tlv.iso7816.EF_DIR
import org.openecard.common.apdu.utils.CardUtils
import org.openecard.common.tlv.iso7816.CardFlags
import org.openecard.common.tlv.iso7816.TLVList
import org.openecard.common.tlv.iso7816.TLVBitString
import org.openecard.common.tlv.iso7816.GenericPathOrObjects
import org.openecard.common.tlv.iso7816.PrivateKeyChoice
import org.openecard.common.tlv.iso7816.CertificateChoice
import org.openecard.common.tlv.iso7816.AuthenticationObjectChoice
import org.openecard.common.tlv.iso7816.TLVType
import org.openecard.common.tlv.iso7816.FileDescriptorByte
import org.openecard.common.tlv.iso7816.DataCodingByte
import org.openecard.common.tlv.iso7816.DataElement
import org.openecard.common.tlv.iso7816.GenericPublicKeyObject
import org.openecard.common.tlv.iso7816.GenericSecretKeyObject
import org.openecard.common.tlv.iso7816.GenericPrivateKeyObject
import org.openecard.common.tlv.iso7816.PrivateRSAKeyAttributes
import org.openecard.common.tlv.iso7816.PrivateECKeyAttributes
import org.openecard.common.tlv.iso7816.SecurityConditionChoice
import org.openecard.common.tlv.iso7816.GenericCertificateObject
import org.openecard.common.tlv.iso7816.X509CertificateAttribute
import org.openecard.common.tlv.iso7816.ReferencedValue
import org.openecard.common.tlv.iso7816.CIODDO
import org.openecard.common.tlv.iso7816.CommonObjectAttributes
import org.openecard.common.tlv.iso7816.CommonKeyAttributes
import org.openecard.common.tlv.iso7816.AuthReference
import org.openecard.common.tlv.iso7816.GenericObjectValue
import org.openecard.common.tlv.iso7816.GenericDataContainerObject
import org.openecard.common.tlv.iso7816.GenericAuthenticationObject
import org.openecard.common.tlv.iso7816.PasswordAttributes
import org.openecard.common.tlv.TagLengthValue
import org.openecard.common.apdu.common.CardCommandAPDU
import org.openecard.common.apdu.common.CardResponseAPDU
import org.openecard.common.apdu.utils.FileControlParameters
import org.openecard.common.apdu.ReadBinary
import org.openecard.common.apdu.common.CardCommandStatus
import org.openecard.common.apdu.ReadRecord
import org.openecard.common.apdu.UpdateRecord
import org.openecard.common.ECardConstants.Minor
import org.openecard.common.apdu.common.APDUTemplateFunction
import org.openecard.common.apdu.common.APDUTemplateException
import org.openecard.common.apdu.common.CardAPDU
import iso.std.iso_iec._24727.tech.schema.Transmit
import iso.std.iso_iec._24727.tech.schema.InputAPDUInfoType
import iso.std.iso_iec._24727.tech.schema.TransmitResponse
import org.openecard.common.WSHelper
import org.openecard.common.WSHelper.WSException
import org.openecard.common.apdu.common.TLVFunction
import iso.std.iso_iec._24727.tech.schema.CardCallTemplateType
import org.openecard.common.apdu.common.CardCommandTemplate
import org.openecard.common.ECardException
import org.openecard.common.apdu.CreateFile
import org.openecard.common.apdu.DeleteFile
import org.openecard.common.apdu.EraseBinary
import org.openecard.common.apdu.EraseRecord
import org.openecard.common.apdu.WriteRecord
import org.openecard.common.apdu.GetChallenge
import org.openecard.common.apdu.UpdateBinary
import org.openecard.common.apdu.ResetRetryCounter
import org.openecard.common.apdu.GeneralAuthenticate
import org.openecard.common.apdu.InternalAuthenticate
import org.openecard.common.apdu.ExternalAuthentication
import org.openecard.common.apdu.PerformSecurityOperation
import org.openecard.common.apdu.ManageSecurityEnvironment
import org.openecard.common.util.UtilException
import iso.std.iso_iec._24727.tech.schema.PasswordAttributesType
import org.openecard.common.util.PINUtils
import iso.std.iso_iec._24727.tech.schema.PasswordTypeType
import org.openecard.common.ECardConstants
import org.openecard.common.util.SysUtils
import org.openecard.common.util.FileSafeBase64
import org.openecard.common.util.HTMLUtils
import org.openecard.common.util.LongUtils
import org.openecard.common.util.ShortUtils
import org.openecard.common.util.UrlEncoder
import org.openecard.common.util.UrlBuilder
import org.openecard.common.util.HttpRequestLineUtils
import org.openecard.common.util.DomainUtils
import org.openecard.common.util.HandlerBuilder
import org.openecard.common.util.HandlerUtils
import iso.std.iso_iec._24727.tech.schema.CardApplicationPathType
import iso.std.iso_iec._24727.tech.schema.ChannelHandleType
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType.RecognitionInfo
import iso.std.iso_iec._24727.tech.schema.PathSecurityType
import org.openecard.common.util.IntegerUtils
import iso.std.iso_iec._24727.tech.schema.CardApplicationList
import iso.std.iso_iec._24727.tech.schema.CardApplicationListResponse
import iso.std.iso_iec._24727.tech.schema.DataSetList
import iso.std.iso_iec._24727.tech.schema.DataSetListResponse
import iso.std.iso_iec._24727.tech.schema.DIDList
import iso.std.iso_iec._24727.tech.schema.DIDListResponse
import iso.std.iso_iec._24727.tech.schema.CardApplicationConnect
import iso.std.iso_iec._24727.tech.schema.CardApplicationConnectResponse
import org.openecard.common.util.TR03112Utils
import org.openecard.bouncycastle.tls.TlsServerCertificate
import iso.std.iso_iec._24727.tech.schema.IFDStatusType
import iso.std.iso_iec._24727.tech.schema.SlotStatusType
import org.openecard.common.util.IFDStatusDiff
import org.openecard.common.util.CombinedPromise
import org.openecard.common.util.RemoveActionFactory
import org.openecard.common.util.RemoveAction
import org.openecard.common.util.SelfCleaningMap
import org.openecard.common.util.LinuxLibraryFinder
import org.openecard.common.interfaces.DocumentSchemaValidator
import org.openecard.common.util.JAXPSchemaValidator
import org.openecard.common.interfaces.DocumentValidatorException
import org.openecard.common.util.JAXPSchemaValidator.CustomErrorHandler
import org.openecard.common.util.SecureRandomFactory.LinuxSecureRandom
import org.openecard.common.util.SecureRandomFactory.SeedSource
import org.openecard.common.util.SecureRandomFactory.UrandomSeedSource
import org.openecard.common.util.SecureRandomFactory
import org.openecard.common.util.SecureRandomFactory.SecureRandomSeedSource
import org.openecard.common.util.JAXBUtils
import org.openecard.ws.marshal.MarshallingTypeException
import org.openecard.ws.marshal.WSMarshallerException
import org.openecard.common.event.EventTypeFilter
import org.openecard.common.event.EventDispatcherImpl
import iso.std.iso_iec._24727.tech.schema.DIDAbstractMarkerType
import iso.std.iso_iec._24727.tech.schema.KeyRefType
import iso.std.iso_iec._24727.tech.schema.StateInfoType
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticationDataType
import org.openecard.common.anytype.AuthDataMap
import org.openecard.common.anytype.pin.PINCompareDIDAuthenticateOutputType
import iso.std.iso_iec._24727.tech.schema.PinCompareDIDAuthenticateInputType
import org.openecard.common.anytype.AuthDataResponse
import org.openecard.common.anytype.pin.PINCompareDIDAuthenticateInputType
import iso.std.iso_iec._24727.tech.schema.PinCompareDIDAuthenticateOutputType
import org.openecard.common.OpenecardProperties
import org.openecard.common.I18nKey
import org.openecard.gui.UserConsent
import org.openecard.ws.Management
import org.openecard.common.interfaces.CardRecognition
import org.openecard.common.interfaces.CIFProvider
import org.openecard.common.interfaces.SalSelector
import org.openecard.common.USBLangID
import org.openecard.ws.common.OverridingProperties

