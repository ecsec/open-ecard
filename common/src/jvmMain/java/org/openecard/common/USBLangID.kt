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
package org.openecard.common

import java.util.Locale

/**
 * Taken from http://www.usb.org/developers/docs/USB_LANGIDs.pdf
 *
 * @author Tobias Wich
 */
@Suppress("ktlint:standard:enum-entry-name-case")
enum class USBLangID(
	code: Int,
) {
	Afrikaans(0x0436),
	Albanian(0x041c),
	Arabic_Saudi_Arabia(0x0401),
	Arabic_Iraq(0x0801),
	Arabic_Egypt(0x0c01),
	Arabic_Libya(0x1001),
	Arabic_Algeria(0x1401),
	Arabic_Morocco(0x1801),
	Arabic_Tunisia(0x1c01),
	Arabic_Oman(0x2001),
	Arabic_Yemen(0x2401),
	Arabic_Syria(0x2801),
	Arabic_Jordan(0x2c01),
	Arabic_Lebanon(0x3001),
	Arabic_Kuwait(0x3401),
	Arabic_U_A_E(0x3801),
	Arabic_Bahrain(0x3c01),
	Arabic_Qatar(0x4001),
	Armenian(0x042b),
	Assamese(0x044d),
	Azeri_Latin(0x042c),
	Azeri_Cyrillic(0x082c),
	Basque(0x042d),
	Belarussian(0x0423),
	Bengali(0x0445),
	Bulgarian(0x0402),
	Burmese(0x0455),
	Catalan(0x0403),
	Chinese_Taiwan(0x0404),
	Chinese_PRC(0x0804),
	Chinese_Hong_Kong_SAR_PRC(0x0c04),
	Chinese_Singapore(0x1004),
	Chinese_Macau_SAR(0x1404),
	Croatian(0x041a),
	Czech(0x0405),
	Danish(0x0406),
	Dutch_Netherlands(0x0413),
	Dutch_Belgium(0x0813),
	English_United_States(0x0409),
	English_United_Kingdom(0x0809),
	English_Australian(0x0c09),
	English_Canadian(0x1009),
	English_New_Zealand(0x1409),
	English_Ireland(0x1809),
	English_South_Africa(0x1c09),
	English_Jamaica(0x2009),
	English_Caribbean(0x2409),
	English_Belize(0x2809),
	English_Trinidad(0x2c09),
	English_Zimbabwe(0x3009),
	English_Philippines(0x3409),
	Estonian(0x0425),
	Faeroese(0x0438),
	Farsi(0x0429),
	Finnish(0x040b),
	French_Standard(0x040c),
	French_Belgian(0x080c),
	French_Canadian(0x0c0c),
	French_Switzerland(0x100c),
	French_Luxembourg(0x140c),
	French_Monaco(0x180c),
	Georgian(0x0437),
	German_Standard(0x0407),
	German_Switzerland(0x0807),
	German_Austria(0x0c07),
	German_Luxembourg(0x1007),
	German_Liechtenstein(0x1407),
	Greek(0x0408),
	Gujarati(0x0447),
	Hebrew(0x040d),
	Hindi(0x0439),
	Hungarian(0x040e),
	Icelandic(0x040f),
	Indonesian(0x0421),
	Italian_Standard(0x0410),
	Italian_Switzerland(0x0810),
	Japanese(0x0411),
	Kannada(0x044b),
	Kashmiri_India(0x0860),
	Kazakh(0x043f),
	Konkani(0x0457),
	Korean(0x0412),
	Korean_Johab(0x0812),
	Latvian(0x0426),
	Lithuanian(0x0427),
	Lithuanian_Classic(0x0827),
	Macedonian(0x042f),
	Malay_Malaysian(0x043e),
	Malay_Brunei_Darussalam(0x083e),
	Malayalam(0x044c),
	Manipuri(0x0458),
	Marathi(0x044e),
	Nepali_India(0x0861),
	Norwegian_Bokmal(0x0414),
	Norwegian_Nynorsk(0x0814),
	Oriya(0x0448),
	Polish(0x0415),
	Portuguese_Brazil(0x0416),
	Portuguese_Standard(0x0816),
	Punjabi(0x0446),
	Romanian(0x0418),
	Russian(0x0419),
	Sanskrit(0x044f),
	Serbian_Cyrillic(0x0c1a),
	Serbian_Latin(0x081a),
	Sindhi(0x0459),
	Slovak(0x041b),
	Slovenian(0x0424),
	Spanish_Traditional_Sort(0x040a),
	Spanish_Mexican(0x080a),
	Spanish_Modern_Sort(0x0c0a),
	Spanish_Guatemala(0x100a),
	Spanish_Costa_Rica(0x140a),
	Spanish_Panama(0x180a),
	Spanish_Dominican_Republic(0x1c0a),
	Spanish_Venezuela(0x200a),
	Spanish_Colombia(0x240a),
	Spanish_Peru(0x280a),
	Spanish_Argentina(0x2c0a),
	Spanish_Ecuador(0x300a),
	Spanish_Chile(0x340a),
	Spanish_Uruguay(0x380a),
	Spanish_Paraguay(0x3c0a),
	Spanish_Bolivia(0x400a),
	Spanish_El_Salvador(0x440a),
	Spanish_Honduras(0x480a),
	Spanish_Nicaragua(0x4c0a),
	Spanish_Puerto_Rico(0x500a),
	Sutu(0x0430),
	Swahili_Kenya(0x0441),
	Swedish(0x041d),
	Swedish_Finland(0x081d),
	Tamil(0x0449),
	Tatar_Tatarstan(0x0444),
	Telugu(0x044a),
	Thai(0x041e),
	Turkish(0x041f),
	Ukrainian(0x0422),
	Urdu_Pakistan(0x0420),
	Urdu_India(0x0820),
	Uzbek_Latin(0x0443),
	Uzbek_Cyrillic(0x0843),
	Vietnamese(0x042a),
	HID_Usage_Data_Descriptor(0x04ff),
	HID_Vendor_Defined_1(0xf0ff),
	HID_Vendor_Defined_2(0xf4ff),
	HID_Vendor_Defined_3(0xf8ff),
	HID_Vendor_Defined_4(0xfcff),
	;

	val code: Short = code.toShort()

	companion object {
		fun getCode(l: Locale): Short {
			val lang = l.language
			val country = l.country

			if (lang == Locale("de").language) {
				return when (country) {
					Locale("ch").country -> {
						German_Switzerland.code
					}
					Locale("at").country -> {
						German_Austria.code
					}
					Locale("li").country -> {
						German_Liechtenstein.code
					}
					Locale("lu").country -> {
						German_Luxembourg.code
					}
					else -> {
						German_Standard.code
					}
				}
			} else if (lang == Locale("en").language) {
				when (country) {
					Locale("gb").country -> {
						return English_United_Kingdom.code
					}
					Locale("us").country -> {
						return English_United_States.code
					}
					Locale("au").country -> {
						return English_Australian.code
					}
					Locale("bz").country -> {
						return English_Belize.code
					}
					Locale("ca").country -> {
						return English_Canadian.code
					}
					Locale("ie").country -> {
						return English_Ireland.code
					}
					Locale("jm").country -> {
						return English_Jamaica.code
					}
					Locale("nz").country -> {
						return English_New_Zealand.code
					}
					Locale("ph").country -> {
						return English_Philippines.code
					}
					Locale("za").country -> {
						return English_South_Africa.code
					}
					Locale("tt").country -> {
						return English_Trinidad.code
					}
					Locale("zw").country -> {
						return English_Zimbabwe.code
					}
				}
			}

			return English_United_States.code
		}
	}
}
