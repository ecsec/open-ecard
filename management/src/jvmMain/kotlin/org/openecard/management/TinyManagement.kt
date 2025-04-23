/****************************************************************************
 * Copyright (C) 2012-2018 HS Coburg.
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

package org.openecard.management

import de.bund.bsi.ecard.api._1.*
import org.openecard.common.ECardConstants
import org.openecard.common.WSHelper
import org.openecard.common.interfaces.Environment
import org.openecard.common.interfaces.Publish
import org.openecard.ws.Management

/**
 *
 * @author Dirk Petrautzki
 */
class TinyManagement(
	private val env: Environment,
) : Management {
	override fun addCardInfoFiles(arg0: AddCardInfoFiles): AddCardInfoFilesResponse =
		WSHelper.makeResponse(
			AddCardInfoFilesResponse::class.java,
			WSHelper.makeResultUnknownError("Not supported yet."),
		)

	override fun addCertificate(arg0: AddCertificate): AddCertificateResponse =
		WSHelper.makeResponse(
			AddCertificateResponse::class.java,
			WSHelper.makeResultUnknownError("Not supported yet."),
		)

	override fun addTSL(arg0: AddTSL): AddTSLResponse =
		WSHelper.makeResponse(
			AddTSLResponse::class.java,
			WSHelper.makeResultUnknownError("Not supported yet."),
		)

	override fun addTrustedCertificate(arg0: AddTrustedCertificate): AddTrustedCertificateResponse =
		WSHelper.makeResponse(
			AddTrustedCertificateResponse::class.java,
			WSHelper.makeResultUnknownError("Not supported yet."),
		)

	override fun addTrustedViewer(arg0: AddTrustedViewer): AddTrustedViewerResponse =
		WSHelper.makeResponse(
			AddTrustedViewerResponse::class.java,
			WSHelper.makeResultUnknownError("Not supported yet."),
		)

	override fun apiaclList(arg0: APIACLList): APIACLListResponse =
		WSHelper.makeResponse(
			APIACLListResponse::class.java,
			WSHelper.makeResultUnknownError("Not supported yet."),
		)

	override fun apiaclModify(arg0: APIACLModify): APIACLModifyResponse =
		WSHelper.makeResponse(
			APIACLModifyResponse::class.java,
			WSHelper.makeResultUnknownError("Not supported yet."),
		)

	override fun deleteCardInfoFiles(arg0: DeleteCardInfoFiles): DeleteCardInfoFilesResponse =
		WSHelper.makeResponse(
			DeleteCardInfoFilesResponse::class.java,
			WSHelper.makeResultUnknownError("Not supported yet."),
		)

	override fun deleteCertificate(arg0: DeleteCertificate): DeleteCertificateResponse =
		WSHelper.makeResponse(
			DeleteCertificateResponse::class.java,
			WSHelper.makeResultUnknownError("Not supported yet."),
		)

	override fun deleteTSL(arg0: DeleteTSL): DeleteTSLResponse =
		WSHelper.makeResponse(
			DeleteTSLResponse::class.java,
			WSHelper.makeResultUnknownError("Not supported yet."),
		)

	override fun deleteTrustedViewer(arg0: DeleteTrustedViewer): DeleteTrustedViewerResponse =
		WSHelper.makeResponse(
			DeleteTrustedViewerResponse::class.java,
			WSHelper.makeResultUnknownError("Not supported yet."),
		)

	override fun exportCertificate(arg0: ExportCertificate): ExportCertificateResponse =
		WSHelper.makeResponse(
			ExportCertificateResponse::class.java,
			WSHelper.makeResultUnknownError("Not supported yet."),
		)

	override fun exportTSL(arg0: ExportTSL): ExportTSLResponse =
		WSHelper.makeResponse(
			ExportTSLResponse::class.java,
			WSHelper.makeResultUnknownError("Not supported yet."),
		)

	override fun frameworkUpdate(arg0: FrameworkUpdate): FrameworkUpdateResponse =
		WSHelper.makeResponse(
			FrameworkUpdateResponse::class.java,
			WSHelper.makeResultUnknownError("Not supported yet."),
		)

	override fun getCardInfoList(arg0: GetCardInfoList): GetCardInfoListResponse =
		WSHelper.makeResponse(
			GetCardInfoListResponse::class.java,
			WSHelper.makeResultUnknownError("Not supported yet."),
		)

	override fun getDefaultParameters(arg0: GetDefaultParameters): GetDefaultParametersResponse =
		WSHelper.makeResponse(
			GetDefaultParametersResponse::class.java,
			WSHelper.makeResultUnknownError("Not supported yet."),
		)

	override fun getDirectoryServices(arg0: GetDirectoryServices): GetDirectoryServicesResponse =
		WSHelper.makeResponse(
			GetDirectoryServicesResponse::class.java,
			WSHelper.makeResultUnknownError("Not supported yet."),
		)

	override fun getOCSPServices(arg0: GetOCSPServices): GetOCSPServicesResponse =
		WSHelper.makeResponse(
			GetOCSPServicesResponse::class.java,
			WSHelper.makeResultUnknownError("Not supported yet."),
		)

	override fun getTSServices(arg0: GetTSServices): GetTSServicesResponse =
		WSHelper.makeResponse(
			GetTSServicesResponse::class.java,
			WSHelper.makeResultUnknownError("Not supported yet."),
		)

	override fun getTrustedIdentities(arg0: GetTrustedIdentities): GetTrustedIdentitiesResponse =
		WSHelper.makeResponse(
			GetTrustedIdentitiesResponse::class.java,
			WSHelper.makeResultUnknownError("Not supported yet."),
		)

	override fun getTrustedViewerConfiguration(
		arg0: GetTrustedViewerConfiguration,
	): GetTrustedViewerConfigurationResponse =
		WSHelper.makeResponse(
			GetTrustedViewerConfigurationResponse::class.java,
			WSHelper.makeResultUnknownError("Not supported yet."),
		)

	override fun getTrustedViewerList(arg0: GetTrustedViewerList): GetTrustedViewerListResponse =
		WSHelper.makeResponse(
			GetTrustedViewerListResponse::class.java,
			WSHelper.makeResultUnknownError("Not supported yet."),
		)

	@Publish
	override fun initializeFramework(arg0: InitializeFramework): InitializeFrameworkResponse {
		val ifr: InitializeFrameworkResponse =
			WSHelper.makeResponse(
				InitializeFrameworkResponse::class.java,
				WSHelper.makeResultOK(),
			)
		val version = InitializeFrameworkResponse.Version()
		version.setMajor(ECardConstants.ECARD_API_VERSION_MAJOR)
		version.setMinor(ECardConstants.ECARD_API_VERSION_MINOR)
		version.setSubMinor(ECardConstants.ECARD_API_VERSION_SUBMINOR)
		ifr.setVersion(version)
		return ifr
	}

	override fun registerIFD(arg0: RegisterIFD): RegisterIFDResponse =
		WSHelper.makeResponse(
			RegisterIFDResponse::class.java,
			WSHelper.makeResultUnknownError("Not supported yet."),
		)

	override fun setCardInfoList(arg0: SetCardInfoList): SetCardInfoListResponse =
		WSHelper.makeResponse(
			SetCardInfoListResponse::class.java,
			WSHelper.makeResultUnknownError("Not supported yet."),
		)

	override fun setDefaultParameters(arg0: SetDefaultParameters): SetDefaultParametersResponse =
		WSHelper.makeResponse(
			SetDefaultParametersResponse::class.java,
			WSHelper.makeResultUnknownError("Not supported yet."),
		)

	override fun setDirectoryServices(arg0: SetDirectoryServices): SetDirectoryServicesResponse =
		WSHelper.makeResponse(
			SetDirectoryServicesResponse::class.java,
			WSHelper.makeResultUnknownError("Not supported yet."),
		)

	override fun setOCSPServices(arg0: SetOCSPServices): SetOCSPServicesResponse =
		WSHelper.makeResponse(
			SetOCSPServicesResponse::class.java,
			WSHelper.makeResultUnknownError("Not supported yet."),
		)

	override fun setTSServices(arg0: SetTSServices): SetTSServicesResponse =
		WSHelper.makeResponse(
			SetTSServicesResponse::class.java,
			WSHelper.makeResultUnknownError("Not supported yet."),
		)

	override fun setTrustedViewerConfiguration(
		arg0: SetTrustedViewerConfiguration,
	): SetTrustedViewerConfigurationResponse =
		WSHelper.makeResponse(
			SetTrustedViewerConfigurationResponse::class.java,
			WSHelper.makeResultUnknownError("Not supported yet."),
		)

	override fun terminateFramework(arg0: TerminateFramework): TerminateFrameworkResponse =
		WSHelper.makeResponse(
			TerminateFrameworkResponse::class.java,
			WSHelper.makeResultUnknownError("Not supported yet."),
		)

	override fun unregisterIFD(arg0: UnregisterIFD): UnregisterIFDResponse =
		WSHelper.makeResponse(
			UnregisterIFDResponse::class.java,
			WSHelper.makeResultUnknownError("Not supported yet."),
		)
}
