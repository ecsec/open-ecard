package org.openecard.sc.pace.cvc

enum class EidDataGroup(
	val readDg: ReadAccess,
	val writeDg: WriteAccess? = null,
) {
	DOCUMENT_TYPE(ReadAccess.DG01),
	ISSUING_ENTITY(ReadAccess.DG02),
	DATE_OF_EXPIRY(ReadAccess.DG03),
	GIVEN_NAMES(ReadAccess.DG04),
	FAMILY_NAMES(ReadAccess.DG05),
	NOM_DE_PLUME(ReadAccess.DG06),
	ACADEMIC_TITLE(ReadAccess.DG07),
	DATE_OF_BIRTH(ReadAccess.DG08),
	PLACE_OF_BIRTH(ReadAccess.DG09),
	NATIONALITY(ReadAccess.DG10),
	SEX(ReadAccess.DG11),
	OPTIONAL_DATA(ReadAccess.DG12),
	BIRTH_NAME(ReadAccess.DG13),
	WRITTEN_SIGNATURE(ReadAccess.DG14),

	// DG16 is RFU
	DATE_OF_ISSUANCE(ReadAccess.DG15),
	PLACE_OF_RESIDENCE(ReadAccess.DG17, WriteAccess.DG17),
	COMMUNITY_ID(ReadAccess.DG18, WriteAccess.DG18),
	RESIDENCE_PERMIT_1(ReadAccess.DG19, WriteAccess.DG19),
	RESIDENCE_PERMIT_2(ReadAccess.DG20, WriteAccess.DG20),
	PHONE_NUMBER(ReadAccess.DG21, WriteAccess.DG21),
	EMAIL(ReadAccess.DG22, WriteAccess.DG22),
}

fun ReadAccess.toEidDataGroup(): EidDataGroup? = EidDataGroup.entries.find { it.readDg == this }

fun WriteAccess.toEidDataGroup(): EidDataGroup? = EidDataGroup.entries.find { it.writeDg == this }
