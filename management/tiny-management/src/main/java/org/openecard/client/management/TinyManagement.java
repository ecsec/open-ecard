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

package org.openecard.client.management;

import java.math.BigInteger;

import oasis.names.tc.dss._1_0.core.schema.Result;

import org.openecard.client.common.ECardConstants;
import org.openecard.client.common.WSHelper;
import org.openecard.client.common.interfaces.Environment;
import org.openecard.ws.Management;

import de.bund.bsi.ecard.api._1.APIACLList;
import de.bund.bsi.ecard.api._1.APIACLListResponse;
import de.bund.bsi.ecard.api._1.APIACLModify;
import de.bund.bsi.ecard.api._1.APIACLModifyResponse;
import de.bund.bsi.ecard.api._1.AddCardInfoFiles;
import de.bund.bsi.ecard.api._1.AddCardInfoFilesResponse;
import de.bund.bsi.ecard.api._1.AddCertificate;
import de.bund.bsi.ecard.api._1.AddCertificateResponse;
import de.bund.bsi.ecard.api._1.AddTSL;
import de.bund.bsi.ecard.api._1.AddTSLResponse;
import de.bund.bsi.ecard.api._1.AddTrustedCertificate;
import de.bund.bsi.ecard.api._1.AddTrustedCertificateResponse;
import de.bund.bsi.ecard.api._1.AddTrustedViewer;
import de.bund.bsi.ecard.api._1.AddTrustedViewerResponse;
import de.bund.bsi.ecard.api._1.DeleteCardInfoFiles;
import de.bund.bsi.ecard.api._1.DeleteCardInfoFilesResponse;
import de.bund.bsi.ecard.api._1.DeleteCertificate;
import de.bund.bsi.ecard.api._1.DeleteCertificateResponse;
import de.bund.bsi.ecard.api._1.DeleteTSL;
import de.bund.bsi.ecard.api._1.DeleteTSLResponse;
import de.bund.bsi.ecard.api._1.DeleteTrustedViewer;
import de.bund.bsi.ecard.api._1.DeleteTrustedViewerResponse;
import de.bund.bsi.ecard.api._1.ExportCertificate;
import de.bund.bsi.ecard.api._1.ExportCertificateResponse;
import de.bund.bsi.ecard.api._1.ExportTSL;
import de.bund.bsi.ecard.api._1.ExportTSLResponse;
import de.bund.bsi.ecard.api._1.FrameworkUpdate;
import de.bund.bsi.ecard.api._1.FrameworkUpdateResponse;
import de.bund.bsi.ecard.api._1.GetCardInfoList;
import de.bund.bsi.ecard.api._1.GetCardInfoListResponse;
import de.bund.bsi.ecard.api._1.GetDefaultParameters;
import de.bund.bsi.ecard.api._1.GetDefaultParametersResponse;
import de.bund.bsi.ecard.api._1.GetDirectoryServices;
import de.bund.bsi.ecard.api._1.GetDirectoryServicesResponse;
import de.bund.bsi.ecard.api._1.GetOCSPServices;
import de.bund.bsi.ecard.api._1.GetOCSPServicesResponse;
import de.bund.bsi.ecard.api._1.GetTSServices;
import de.bund.bsi.ecard.api._1.GetTSServicesResponse;
import de.bund.bsi.ecard.api._1.GetTrustedIdentities;
import de.bund.bsi.ecard.api._1.GetTrustedIdentitiesResponse;
import de.bund.bsi.ecard.api._1.GetTrustedViewerConfiguration;
import de.bund.bsi.ecard.api._1.GetTrustedViewerConfigurationResponse;
import de.bund.bsi.ecard.api._1.GetTrustedViewerList;
import de.bund.bsi.ecard.api._1.GetTrustedViewerListResponse;
import de.bund.bsi.ecard.api._1.InitializeFramework;
import de.bund.bsi.ecard.api._1.InitializeFrameworkResponse;
import de.bund.bsi.ecard.api._1.InitializeFrameworkResponse.Version;
import de.bund.bsi.ecard.api._1.RegisterIFD;
import de.bund.bsi.ecard.api._1.RegisterIFDResponse;
import de.bund.bsi.ecard.api._1.SetCardInfoList;
import de.bund.bsi.ecard.api._1.SetCardInfoListResponse;
import de.bund.bsi.ecard.api._1.SetDefaultParameters;
import de.bund.bsi.ecard.api._1.SetDefaultParametersResponse;
import de.bund.bsi.ecard.api._1.SetDirectoryServices;
import de.bund.bsi.ecard.api._1.SetDirectoryServicesResponse;
import de.bund.bsi.ecard.api._1.SetOCSPServices;
import de.bund.bsi.ecard.api._1.SetOCSPServicesResponse;
import de.bund.bsi.ecard.api._1.SetTSServices;
import de.bund.bsi.ecard.api._1.SetTSServicesResponse;
import de.bund.bsi.ecard.api._1.SetTrustedViewerConfiguration;
import de.bund.bsi.ecard.api._1.SetTrustedViewerConfigurationResponse;
import de.bund.bsi.ecard.api._1.TerminateFramework;
import de.bund.bsi.ecard.api._1.TerminateFrameworkResponse;
import de.bund.bsi.ecard.api._1.UnregisterIFD;
import de.bund.bsi.ecard.api._1.UnregisterIFDResponse;

/**
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 *
 */
public class TinyManagement implements  Management {

    private Environment env;

    public TinyManagement(Environment env) {
        this.env = env;
    }
    
    @Override
    public AddCardInfoFilesResponse addCardInfoFiles(AddCardInfoFiles arg0) {
	   return WSHelper.makeResponse(AddCardInfoFilesResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    @Override
    public AddCertificateResponse addCertificate(AddCertificate arg0) {
	   return WSHelper.makeResponse(AddCertificateResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    @Override
    public AddTSLResponse addTSL(AddTSL arg0) {
	   return WSHelper.makeResponse(AddTSLResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    @Override
    public AddTrustedCertificateResponse addTrustedCertificate(AddTrustedCertificate arg0) {
	 return WSHelper.makeResponse(AddTrustedCertificateResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    @Override
    public AddTrustedViewerResponse addTrustedViewer(AddTrustedViewer arg0) {
	 return WSHelper.makeResponse(AddTrustedViewerResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    @Override
    public APIACLListResponse apiaclList(APIACLList arg0) {
	 return WSHelper.makeResponse(APIACLListResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    @Override
    public APIACLModifyResponse apiaclModify(APIACLModify arg0) {
	 return WSHelper.makeResponse(APIACLModifyResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    @Override
    public DeleteCardInfoFilesResponse deleteCardInfoFiles(DeleteCardInfoFiles arg0) {
	 return WSHelper.makeResponse(DeleteCardInfoFilesResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    @Override
    public DeleteCertificateResponse deleteCertificate(DeleteCertificate arg0) {
	 return WSHelper.makeResponse(DeleteCertificateResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    @Override
    public DeleteTSLResponse deleteTSL(DeleteTSL arg0) {
	 return WSHelper.makeResponse(DeleteTSLResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    @Override
    public DeleteTrustedViewerResponse deleteTrustedViewer(DeleteTrustedViewer arg0) {
	 return WSHelper.makeResponse(DeleteTrustedViewerResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    @Override
    public ExportCertificateResponse exportCertificate(ExportCertificate arg0) {
	 return WSHelper.makeResponse(ExportCertificateResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    @Override
    public ExportTSLResponse exportTSL(ExportTSL arg0) {
	 return WSHelper.makeResponse(ExportTSLResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    @Override
    public FrameworkUpdateResponse frameworkUpdate(FrameworkUpdate arg0) {
	 return WSHelper.makeResponse(FrameworkUpdateResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    @Override
    public GetCardInfoListResponse getCardInfoList(GetCardInfoList arg0) {
	 return WSHelper.makeResponse(GetCardInfoListResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    @Override
    public GetDefaultParametersResponse getDefaultParameters(GetDefaultParameters arg0) {
	 return WSHelper.makeResponse(GetDefaultParametersResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    @Override
    public GetDirectoryServicesResponse getDirectoryServices(GetDirectoryServices arg0) {
	 return WSHelper.makeResponse(GetDirectoryServicesResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    @Override
    public GetOCSPServicesResponse getOCSPServices(GetOCSPServices arg0) {
	 return WSHelper.makeResponse(GetOCSPServicesResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    @Override
    public GetTSServicesResponse getTSServices(GetTSServices arg0) {
	 return WSHelper.makeResponse(GetTSServicesResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    @Override
    public GetTrustedIdentitiesResponse getTrustedIdentities(GetTrustedIdentities arg0) {
	 return WSHelper.makeResponse(GetTrustedIdentitiesResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    @Override
    public GetTrustedViewerConfigurationResponse getTrustedViewerConfiguration(GetTrustedViewerConfiguration arg0) {
	 return WSHelper.makeResponse(GetTrustedViewerConfigurationResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    @Override
    public GetTrustedViewerListResponse getTrustedViewerList(GetTrustedViewerList arg0) {
	 return WSHelper.makeResponse(GetTrustedViewerListResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    @Override
    public InitializeFrameworkResponse initializeFramework(InitializeFramework arg0) {
	InitializeFrameworkResponse initializeFrameworkResponse = new InitializeFrameworkResponse();
	Version version = new Version();
	//TODO version should be a constant somewhere else
    	version.setMajor(new BigInteger("1"));
    	version.setMinor(new BigInteger("6"));
    	version.setSubMinor(new BigInteger("0"));
    	initializeFrameworkResponse.setVersion(version);
    	Result r = new Result();
    	r.setResultMajor(ECardConstants.Major.OK);
    	initializeFrameworkResponse.setResult(r);
    	return initializeFrameworkResponse;
    }

    @Override
    public RegisterIFDResponse registerIFD(RegisterIFD arg0) {
	 return WSHelper.makeResponse(RegisterIFDResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    @Override
    public SetCardInfoListResponse setCardInfoList(SetCardInfoList arg0) {
	 return WSHelper.makeResponse(SetCardInfoListResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    @Override
    public SetDefaultParametersResponse setDefaultParameters(SetDefaultParameters arg0) {
	 return WSHelper.makeResponse(SetDefaultParametersResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    @Override
    public SetDirectoryServicesResponse setDirectoryServices(SetDirectoryServices arg0) {
	 return WSHelper.makeResponse(SetDirectoryServicesResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    @Override
    public SetOCSPServicesResponse setOCSPServices(SetOCSPServices arg0) {
	 return WSHelper.makeResponse(SetOCSPServicesResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    @Override
    public SetTSServicesResponse setTSServices(SetTSServices arg0) {
	 return WSHelper.makeResponse(SetTSServicesResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    @Override
    public SetTrustedViewerConfigurationResponse setTrustedViewerConfiguration(SetTrustedViewerConfiguration arg0) {
	 return WSHelper.makeResponse(SetTrustedViewerConfigurationResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    @Override
    public TerminateFrameworkResponse terminateFramework(TerminateFramework arg0) {
	 return WSHelper.makeResponse(TerminateFrameworkResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

    @Override
    public UnregisterIFDResponse unregisterIFD(UnregisterIFD arg0) {
	 return WSHelper.makeResponse(UnregisterIFDResponse.class, WSHelper.makeResultUnknownError("Not supported yet."));
    }

}
