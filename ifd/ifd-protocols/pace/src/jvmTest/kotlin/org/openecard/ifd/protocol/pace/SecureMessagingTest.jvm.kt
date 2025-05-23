/****************************************************************************
 * Copyright (C) 2012-2024 HS Coburg.
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

package org.openecard.ifd.protocol.pace

import org.openecard.common.util.StringUtils
import org.testng.Assert
import org.testng.annotations.Test

/**
 *
 * @author Dirk Petrautzki
 */
class SecureMessagingTest {
	@Test
	@Throws(Exception::class)
	fun testDecryption() {
		//
		// setup Secure Messaging
		//
		val keyEnc = StringUtils.toByteArray("68 40 6B 41 62 10 05 63 D9 C9 01 A6 15 4D 29 01", true)
		val keyMac = StringUtils.toByteArray("73 FF 26 87 84 F7 2A F8 33 FD C9 46 40 49 AF C9", true)
		val sm = SecureMessaging(keyMac, keyEnc)

		//
		// test Case 1: DO99|DO8E|SW1SW2
		//
		var apduToDecrypt = StringUtils.toByteArray("990290008E0812503AC2A74CC4639000")
		var decryptedAPDU = sm.decrypt(apduToDecrypt)
		var expectedDecryptedAPDU = StringUtils.toByteArray("9000")
		Assert.assertEquals(decryptedAPDU, expectedDecryptedAPDU)

		//
		// test Case 2: DO87|DO99|DO8E|SW1SW2
		//
		// only needed to increment the ssc for mac calculation
		sm.encrypt(StringUtils.toByteArray("002281B6"))
		apduToDecrypt =
			StringUtils.toByteArray("871101FFC073CB761DC0461DDAFA3217DFB392990290008E08442DFAAF0E4588969000", true)
		decryptedAPDU = sm.decrypt(apduToDecrypt)
		expectedDecryptedAPDU = StringUtils.toByteArray("547E4EAB03B235D29000")
		Assert.assertEquals(decryptedAPDU, expectedDecryptedAPDU)

		//
		// test not encrypted apdu
		//
		try {
			decryptedAPDU = sm.decrypt(StringUtils.toByteArray("9000"))
			Assert.fail("Decrypting an unencrypted APDU should give an error.")
		} catch (e: Exception) {
			// expected
		}

		//
		// test missing DO99
		//
		try {
			apduToDecrypt = StringUtils.toByteArray("8E0812503AC2A74CC4639000")
			decryptedAPDU = sm.decrypt(apduToDecrypt)
			Assert.fail("Decrypting an APDU without DO99 should give an error.")
		} catch (e: Exception) {
			// expected
		}

		//
		// test missing DO8E
		//
		try {
			apduToDecrypt = StringUtils.toByteArray("990290009000")
			decryptedAPDU = sm.decrypt(apduToDecrypt)
			Assert.fail("Decrypting an APDU without DO8E should give an error.")
		} catch (e: Exception) {
			// expected
		}

		//
		// test missing Status
		//
		try {
			apduToDecrypt =
				StringUtils.toByteArray("871101FFC073CB761DC0461DDAFA3217DFB392990290008E08442DFAAF0E458896")
			decryptedAPDU = sm.decrypt(apduToDecrypt)
			Assert.fail("Decrypting an APDU without Statusbytes should give an error.")
		} catch (e: Exception) {
			// expected
		}

		//
		// test wrong mac
		//
		try {
			apduToDecrypt =
				StringUtils.toByteArray("871101FFC073CB761DC0461DDAFA3217DFB392990290008E0812345678910111269000")
			decryptedAPDU = sm.decrypt(apduToDecrypt)
			Assert.fail("Decrypting an APDU with wrong MAC should give an error.")
		} catch (e: Exception) {
			// expected
		}
	}

	@Test
	@Throws(Exception::class)
	fun testEncryption() {
		//
		// setup Secure Messaging
		//
		val keyEnc = StringUtils.toByteArray("68 40 6B 41 62 10 05 63 D9 C9 01 A6 15 4D 29 01", true)
		val keyMac = StringUtils.toByteArray("73 FF 26 87 84 F7 2A F8 33 FD C9 46 40 49 AF C9", true)
		val sm = SecureMessaging(keyMac, keyEnc)

		//
		// test Case 1. : |CLA|INS|P1|P2|
		//
		var plainAPDU = StringUtils.toByteArray("00840000")
		var encryptedAPDU = sm.encrypt(plainAPDU)
		var expectedEncryptedAPDU = StringUtils.toByteArray("0C8400000A8E08F146CA58D9D8796200")
		Assert.assertEquals(encryptedAPDU, expectedEncryptedAPDU)

		//
		// test Case 2. : |CLA|INS|P1|P2|LE|
		//
		plainAPDU = StringUtils.toByteArray("0084000008")
		encryptedAPDU = sm.encrypt(plainAPDU)
		expectedEncryptedAPDU = StringUtils.toByteArray("0C84000000000D9701088E081DC3357110820CAD0000")
		Assert.assertEquals(encryptedAPDU, expectedEncryptedAPDU)

		//
		// test Case 2.1 : |CLA|INS|P1|P2|EXTLE|
		//
		plainAPDU = StringUtils.toByteArray("00840000001111")
		encryptedAPDU = sm.encrypt(plainAPDU)
		expectedEncryptedAPDU = StringUtils.toByteArray("0C84000000000E970211118E08EEF70779FD0263D60000")
		Assert.assertEquals(encryptedAPDU, expectedEncryptedAPDU)

		//
		// test Case 3. : |CLA|INS|P1|P2|LC|DATA|
		//
		plainAPDU = StringUtils.toByteArray("00 22 81 B6 0F 83 0D 44 45 43 56 43 41 41 54 30 30 30 30 31", true)
		encryptedAPDU = sm.encrypt(plainAPDU)
		expectedEncryptedAPDU =
			StringUtils.toByteArray("0C2281B61D871101BEE6E33D7D2F6D8662ED4CF56739794C8E0808FEA4E3EEEC972000")
		Assert.assertEquals(encryptedAPDU, expectedEncryptedAPDU)

		//
		// test Case 3. : Case 3.1: |CLA|INS|P1|P2|EXTLC|DATA|
		//
		plainAPDU =
			StringUtils.toByteArray(
				"002A00BE0001B67F4E82016E5F290100420E44455445535465494430303030317F4982011D060A04007F000702020202038120A9FB57DBA1EEA9BC3E660A909D838D726E3BF623D52620282013481D1F6E537782207D5A0975FC2C3057EEF67530417AFFE7FB8055C126DC5C6CE94A4B44F330B5D9832026DC5C6CE94A4B44F330B5D9BBD77CBF958416295CF7E1CE6BCCDC18FF8C07B68441048BD2AEB9CB7E57CB2C4B482FFC81B7AFB9DE27E1E3BD23C23A4453BD9ACE3262547EF835C3DAC4FD97F8461A14611DC9C27745132DED8E545C1D54C72F0469978520A9FB57DBA1EEA9BC3E660A909D838D718C397AA3B561A6F7901E0E82974856A7864104096EB58BFD86252238EC2652185C43C3A56C320681A21E37A8E69DDC387C0C5F5513856EFE2FDC656E604893212E29449B365E304605AC5413E75BE31E641F128701015F200E44455445535465494430303030327F4C12060904007F0007030102025305FE0F01FFFF5F25060100000902015F24060103000902015F3740141120A0FDFC011A52F3F72B387A3DC7ACA88B4868D5AE9741780B6FF8A0B49E5F55169A2D298EF5CF95935DCA0C3DF3E9D42DC45F74F2066317154961E6C746",
			)
		expectedEncryptedAPDU =
			StringUtils.toByteArray(
				"0C2A00BE0001CF878201C1014A82C32F941900EE386FBE1E2C8927AE46DE58AD5FB70CF24829052D19E6D4A234E350BC6027EF199865BD02A3FFD625099960F708FE276F0592EB440CB03382DE424687313B1D0973302881C5081C7272AE8A38643834C90CB3363983CCE122FFDFE5A4F04B9F7149F94E96BAF8D3355387A8BADC90272B91E0DC404D473C864D7B97CD3C0CA3358E4375A9D73E889044801584AB76DABF817C68ABE788AE4F31DBA5F44B617B1840985DDF4E897C9295DF88E7475BDF09D16497A55477FD6D7FE1009284ACDF6A4921F4745456663CFF625082793755F56DB32E1BD9D9CEFE3DC492F8437A5E31027E388D2B518249D3A58C7D3FFAA3BE0EAED58F0311783F688F33A4C206F394127CEE0056FF35D285D45AB2E995AD36991007C9FB5A3E7154E4750956F106C0974D04888EFEE9290307D962DFB8EA9E58ECECF9EE2F10B34E6254E2EB1CC7013814E8F1BDA7C93FD1AC1FAB6110EBA1156C9E6DBEEB0DE951C5A325E546ED874791489A63774A783BF1F42333326D3F1ADF4350534DA588BDAE4738CBF7FDB3B7EAFC33E4E75DFC3C19C7C758E72930B203A319BFCC4144FCAF266541E1626433679ACA0C665DF7A5ACC1348B0BACB2AD7D2D34B8AA4ED48E0875AA2DE9BC7546DE0000",
			)
		encryptedAPDU = sm.encrypt(plainAPDU)
		Assert.assertEquals(encryptedAPDU, expectedEncryptedAPDU)

		//
		// test Case 4. : |CLA|INS|P1|P2|LC|DATA|LE|
		//
		plainAPDU = StringUtils.toByteArray("00 22 81 B6 0F 83 0D 44 45 43 56 43 41 41 54 30 30 30 30 31 08", true)
		encryptedAPDU = sm.encrypt(plainAPDU)
		expectedEncryptedAPDU =
			StringUtils.toByteArray(
				"0C2281B6000020871101B86BC36EBADED003068B831CC70D8E0E9701088E08972AE4B01DB329930000",
			)
		Assert.assertEquals(encryptedAPDU, expectedEncryptedAPDU)

		//
		// test Case 4.1: |CLA|INS|P1|P2|EXTLC|DATA|LE|
		//
		plainAPDU =
			StringUtils.toByteArray(
				"002A00BE0001B67F4E82016E5F290100420E44455445535465494430303030317F4982011D060A04007F000702020202038120A9FB57DBA1EEA9BC3E660A909D838D726E3BF623D52620282013481D1F6E537782207D5A0975FC2C3057EEF67530417AFFE7FB8055C126DC5C6CE94A4B44F330B5D9832026DC5C6CE94A4B44F330B5D9BBD77CBF958416295CF7E1CE6BCCDC18FF8C07B68441048BD2AEB9CB7E57CB2C4B482FFC81B7AFB9DE27E1E3BD23C23A4453BD9ACE3262547EF835C3DAC4FD97F8461A14611DC9C27745132DED8E545C1D54C72F0469978520A9FB57DBA1EEA9BC3E660A909D838D718C397AA3B561A6F7901E0E82974856A7864104096EB58BFD86252238EC2652185C43C3A56C320681A21E37A8E69DDC387C0C5F5513856EFE2FDC656E604893212E29449B365E304605AC5413E75BE31E641F128701015F200E44455445535465494430303030327F4C12060904007F0007030102025305FE0F01FFFF5F25060100000902015F24060103000902015F3740141120A0FDFC011A52F3F72B387A3DC7ACA88B4868D5AE9741780B6FF8A0B49E5F55169A2D298EF5CF95935DCA0C3DF3E9D42DC45F74F2066317154961E6C74608",
			)
		expectedEncryptedAPDU =
			StringUtils.toByteArray(
				"0C2A00BE0001D3878201C1010B75D6845C75ABA7E3770F1AA257ACA627903624AE9000368B4FD57E7F8E512CE0B39DF79E7E9C22952ABC84F63B3F4EF051FACC973DACD075523CFEBA70A21305846D5D579B33BBC02B1B1BE9D951288D80B05A8B837FBE3B99F948918609C4DB0A36E92E03933FBABDDE2D839DF1AD12EB95AA662B4C6AAE777FB3FFB5ADEEE60AA3A0E0864E5C559DE6B135AE4F420B0B9B320B2BF74C76F11494746A6470306156E36487E2A5EA8CE128EBAB5FBB7F1644A122A919524E514D9D186E854F1396898D8984E2B46542A3C89E2A77A1867E15762EA90A2A5BDE30F766CF7AF869289A8E9C1E84A8DBB6925C7C15ABE088745E1CCAE17E56B603AB0D10E4E26391B6DC5282984DBBA9DC3747FC0566C7ECF06D5CC2958D6BB910594C372EE5256F8DD07CCBB35026A3C7428AEEDF5CC3F826B45500071D664B32095958EA0E919964F029195C99A967B5A53C152EE0CBBBEC4387A41240070C175AE177AEE2606F7AAE52DAB9AF73D429575F6B3247BE3CF25D88B19196561259A2E7DA95132F7C4197CAEEAD7A2AB168934F594D9ACAB0C6BF1486777CDC3251CA24BD5E56440EB2CC52A39ED02D70F6BD958FECDD2D2D1F3D1CA2A67544853DBF0E03ED6591970200088E08EF75289CCD0AC5D30000",
			)
		encryptedAPDU = sm.encrypt(plainAPDU)
		Assert.assertEquals(encryptedAPDU, expectedEncryptedAPDU)

		//
		// Case 4.2: |CLA|INS|P1|P2|LC|DATA|EXTLE|
		//
		plainAPDU =
			StringUtils.toByteArray("00 22 81 B6 0F 83 0D 44 45 43 56 43 41 41 54 30 30 30 30 31 00 11 11", true)
		encryptedAPDU = sm.encrypt(plainAPDU)
		expectedEncryptedAPDU =
			StringUtils.toByteArray(
				"0C2281B60000218711011E52F828C7DE2F1F27EEEB5838065F4D970211118E08F9FD84145B179F100000",
			)
		Assert.assertEquals(encryptedAPDU, expectedEncryptedAPDU)

		//
		// test Case 4.3: |CLA|INS|P1|P2|EXTLC|DATA|EXTLE|
		//
		plainAPDU =
			StringUtils.toByteArray(
				"002A00BE0001B67F4E82016E5F290100420E44455445535465494430303030317F4982011D060A04007F000702020202038120A9FB57DBA1EEA9BC3E660A909D838D726E3BF623D52620282013481D1F6E537782207D5A0975FC2C3057EEF67530417AFFE7FB8055C126DC5C6CE94A4B44F330B5D9832026DC5C6CE94A4B44F330B5D9BBD77CBF958416295CF7E1CE6BCCDC18FF8C07B68441048BD2AEB9CB7E57CB2C4B482FFC81B7AFB9DE27E1E3BD23C23A4453BD9ACE3262547EF835C3DAC4FD97F8461A14611DC9C27745132DED8E545C1D54C72F0469978520A9FB57DBA1EEA9BC3E660A909D838D718C397AA3B561A6F7901E0E82974856A7864104096EB58BFD86252238EC2652185C43C3A56C320681A21E37A8E69DDC387C0C5F5513856EFE2FDC656E604893212E29449B365E304605AC5413E75BE31E641F128701015F200E44455445535465494430303030327F4C12060904007F0007030102025305FE0F01FFFF5F25060100000902015F24060103000902015F3740141120A0FDFC011A52F3F72B387A3DC7ACA88B4868D5AE9741780B6FF8A0B49E5F55169A2D298EF5CF95935DCA0C3DF3E9D42DC45F74F2066317154961E6C746001111",
			)
		expectedEncryptedAPDU =
			StringUtils.toByteArray(
				"0C2A00BE0001D3878201C101070C74102E32F53FA7EC751CCB18A8A38EB94E08D7BF0D80D1FB5279876DFB2F5AD1ED1BE93F22FEB6C06F565D8C1DBFA1BC73C4FC5BFFDEF187FECAEE47A63C0D13200843F3D86AB7C163BEFBEC39791855985375AE9D76C4B6D932DD7739B5CCB25A19190F31ADFD93A83698E5B38CDCAB08C2E235F2058F2008F0791C7454F37A98670EC38677A2F4840756F5F9164349FD7A9FB3266FF18713A50240A4B194C8EEB763F5A39ED5EF1D849D2C5912D0A9EA6F50150AF6076B0EE0B7F32E520D21173A8AA7E0C0517126F8507C4DAC08C8ACBB2F950A2FE18B640B580390867BDF7D07865C6DB54B6C86D0BC82AFF62D06D227579932B78A1484EFDDB7701D583A631A028EDBCB7175088477E27AB24DCF9C6E9508C53B13DF0003935CD8611D9055B717BB789B6A5A514612340DFBA38404C2AE7DF48311114542F38462D8F4C6249429FC75FB978DCA232E59390C6E4C3C5ED67E965EC1FC4C5A6AED615FC30812DB49CC0D54677B9D053C3938886C015066E5FF700F07DB7C941DEBDE1F3426CB2900402D7D1A9D0DDC4E1D1865C5E8D0C7F7EBCA6CA4026C95AE5EC5C3AF9D95948909D2D4D184F781714FC86BCCCD0D84202BEB08B61ADA102C4C7F7D970211118E08B72072246273518D0000",
			)
		encryptedAPDU = sm.encrypt(plainAPDU)
		Assert.assertEquals(encryptedAPDU, expectedEncryptedAPDU)

		//
		// test already encrypted apdu
		//
		try {
			sm.encrypt(expectedEncryptedAPDU)
			Assert.fail("Encrypting an already encrypted APDU should give an error.")
		} catch (e: Exception) {
			// expected
		}
	}
}
