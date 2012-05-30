package org.openecard.client.connector.tctoken;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.List;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Moritz Horsch <moritz.horsch@cdc.informatik.tu-darmstadt.de>
 */
public class TCTokenParserTest {

    @Test
    public void testParse() throws Exception {

	URL testFileLocation = getClass().getResource("/TCToken.xml");
	File testFile = new File(testFileLocation.toURI());

	TCTokenParser parser = new TCTokenParser();
	List<TCToken> tokens = parser.parse(new FileInputStream(testFile));

	TCToken t = tokens.get(0);
	assertEquals(t.getSessionIdentifier(), "3eab1b41ecc1ce5246acf6f4e275");
	assertEquals(t.getServerAddress().toString(), "https://eid-ref.my-service.de:443");
	assertEquals(t.getRefreshAddress().toString(), "https://eid.services.my.net:443/?sessionID=D9D6851A7C02167A5699DA57657664715F4D9C44E50A94F7A83909D24AFA997A");
	assertEquals(t.getBinding(), "urn:liberty:paos:2006-08");
    }

}
