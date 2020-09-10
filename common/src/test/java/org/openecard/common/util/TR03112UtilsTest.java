/****************************************************************************
 * Copyright (C) 2013-2017 HS Coburg.
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

package org.openecard.common.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import org.mockito.Mockito;
import org.openecard.bouncycastle.tls.Certificate;
import org.openecard.bouncycastle.tls.TlsServerCertificate;
import org.openecard.bouncycastle.tls.crypto.TlsCertificate;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 *
 * @author Dirk Petrautzki
 */
public class TR03112UtilsTest {

    private static final byte[] incorrectHash = StringUtils.toByteArray("898F265C130D567741FA5F695749C691289128AD398D3D43C476BC7C2C6AF123");
    private static final byte[] correctHash = StringUtils.toByteArray("898F265C130D567741FA5F695749C691289128AD398D3D43C476BC7C2C6AFFF3");
    private static final byte[] x509Certificate = StringUtils.toByteArray("3082051E30820406A00302010202030A75D4300D06092A864886F70D0101050500303C310B300906035504061302555331173015060355040A130E47656F54727573742C20496E632E311430120603550403130B526170696453534C204341301E170D3133303230363036313033315A170D3135303331303137303735355A3081B731293027060355040513202F4667612F3041674A62643936464B446D366E664F747A4365496D6A4D315A5531133011060355040B130A475431363035393931363131302F060355040B1328536565207777772E726170696473736C2E636F6D2F7265736F75726365732F637073202863293133312F302D060355040B1326446F6D61696E20436F6E74726F6C2056616C696461746564202D20526170696453534C2852293111300F06035504030C082A2E6D74672E646530820122300D06092A864886F70D01010105000382010F003082010A0282010100B62586596B0634775C1DBA354599B2D7467A3F820BD1BE32A9532428DC5731A378884BAA26FC0186C4C6E196AE5A2089FDD5F6E1A78293BDBDFCA511D228C9ABA48BCDFF13D0B2E9D149386ABFD9B65EA982351C112CE98F609F85FC33A3BC421C59D98968CF9937AE071AAA8AFC6889362C154E01E2DA0CFF5E64775EF9C5BDD12AC144FE2983BDA2B8B3F86343C35C01528A8173799C40A13DE68512596831792AEDA4EBE5F5C59204790EA040E5E146B21C4D9B825E3D6F1DB38AD9D80501937274157D8B26B263070F4FC8272AB77F72AB414DBD4549EB61396E04F3C8F49410CF7BCF224C053C08BA1850D74A0264CF9C594CCC9701B9AFE98DE194722D0203010001A38201AB308201A7301F0603551D230418301680146B693D6A18424ADD8F026539FD35248678911630300E0603551D0F0101FF0404030205A0301D0603551D250416301406082B0601050507030106082B06010505070302301B0603551D110414301282082A2E6D74672E646582066D74672E646530430603551D1F043C303A3038A036A0348632687474703A2F2F726170696473736C2D63726C2E67656F74727573742E636F6D2F63726C732F726170696473736C2E63726C301D0603551D0E04160414346BED12E92D661D695E5C01C10380FD1B561B04300C0603551D130101FF04023000307806082B06010505070101046C306A302D06082B060105050730018621687474703A2F2F726170696473736C2D6F6373702E67656F74727573742E636F6D303906082B06010505073002862D687474703A2F2F726170696473736C2D6169612E67656F74727573742E636F6D2F726170696473736C2E637274304C0603551D20044530433041060A6086480186F8450107363033303106082B060105050702011625687474703A2F2F7777772E67656F74727573742E636F6D2F7265736F75726365732F637073300D06092A864886F70D0101050500038201010030A3F897068D0012972993A3BDD0ECBE45ACD9180E19701A6455B7958C94509295EDCB3FFBB8C586A543EB2EB7B7EB1EF71E9E5E8813C45CA52406EF758F2085AA185DFB44BFDA880DAC7281B3677780AA78281958F907478179B36DC69163FE0F22CC3385A67A52749281705FA1B3B4F2D68AE9DA3428BAF3CCBD70EB85293EF5F9DC3B250AE0DBE8F30C74BD312F01708AE732CFD606AF344CC8AE74CBDF0AF3D5BF4124623A6DC1ED073BE2ED641285B2DA26AE19AC535DACBF4BC9166BC81F612564DDC9EAA54CDC8A15625F0AFF9C6AF448506C39977E9DF8AAA5BEF1AB9D945211FA0110EAD42F25D8B09975C9F3ABA04C729B8C2E31BE72E6243B5933");

    @Test
    public void testSameOriginPolicy() throws MalformedURLException {
	URL url1 = new URL("http://example.com/dir/page.html");
	URL url2 = new URL("http://example.com/dir/inner/another.html");
	boolean result = TR03112Utils.checkSameOriginPolicy(url1, url2);
	Assert.assertTrue(result);
	url2 = new URL("http://example.com/dir2/another.html");
	result = TR03112Utils.checkSameOriginPolicy(url1, url2);
	Assert.assertTrue(result);
	url2 = new URL("http://www.example.com/dir/another.html");
	result = TR03112Utils.checkSameOriginPolicy(url1, url2);
	Assert.assertFalse(result);
	url2 = new URL("http://example.com:89/dir/another.html");
	result = TR03112Utils.checkSameOriginPolicy(url1, url2);
	Assert.assertFalse(result);
	url2 = new URL("https://example.com/dir/another.html");
	result = TR03112Utils.checkSameOriginPolicy(url1, url2);
	Assert.assertFalse(result);
    }

    @Test
    public void testInCommCertificates() throws IOException {
	TlsServerCertificate mockTlsCert = Mockito.mock(TlsServerCertificate.class);
	Certificate mockCert = Mockito.mock(Certificate.class);
	TlsCertificate mockTlsCert2 = Mockito.mock(TlsCertificate.class);
	Mockito.when(mockTlsCert.getCertificate()).thenReturn(mockCert);
	Mockito.when(mockCert.getCertificateAt(0)).thenReturn(mockTlsCert2);
	Mockito.when(mockTlsCert2.getEncoded()).thenReturn(x509Certificate);

	ArrayList<byte[]> listOfHashes = new ArrayList<>();
	listOfHashes.add(correctHash);
	boolean result = TR03112Utils.isInCommCertificates(mockTlsCert, listOfHashes, null);
	Assert.assertTrue(result);
	listOfHashes.clear();
	listOfHashes.add(incorrectHash);
	result = TR03112Utils.isInCommCertificates(mockTlsCert, listOfHashes, null);
	Assert.assertFalse(result);
    }

    @Test
    public void testIsRedirectStatusCode() {
	boolean result = TR03112Utils.isRedirectStatusCode(200);
	Assert.assertFalse(result);
	result = TR03112Utils.isRedirectStatusCode(300);
	Assert.assertFalse(result);
	result = TR03112Utils.isRedirectStatusCode(301);
	Assert.assertTrue(result);
	result = TR03112Utils.isRedirectStatusCode(302);
	Assert.assertTrue(result);
	result = TR03112Utils.isRedirectStatusCode(303);
	Assert.assertTrue(result);
	result = TR03112Utils.isRedirectStatusCode(304);
	Assert.assertFalse(result);
	result = TR03112Utils.isRedirectStatusCode(305);
	Assert.assertFalse(result);
	result = TR03112Utils.isRedirectStatusCode(306);
	Assert.assertFalse(result);
	result = TR03112Utils.isRedirectStatusCode(307);
	Assert.assertTrue(result);
	result = TR03112Utils.isRedirectStatusCode(400);
	Assert.assertFalse(result);
    }

}
