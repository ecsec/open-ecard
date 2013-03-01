#include "Platform.h"
#include <string.h>
#include <stdio.h>
#include "CPTReader.h"
#include "BaseCommunication.h"

#define base CEC30Reader

CCPTReader::CCPTReader(CReader *Owner,CBaseCommunication *Communicator)
			  :base(Owner,Communicator)
{
}

CCPTReader::~CCPTReader(void)
{
}

uint16_t CCPTReader::HostToReaderShort(uint16_t Value)
{
   return InversByteOrderShort(htons(Value));
}

uint32_t CCPTReader::HostToReaderLong(uint32_t Value)
{
   return InversByteOrderLong(htonl(Value));
}



void CCPTReader::SetHWString(char *String)
{
   strcpy(String,"CPT_");
}

void CCPTReader::GetProductString(uint8_t *Product)
{
	memcpy(Product,"CJCPT",5);
}


CJ_RESULT CCPTReader::CtSetContrast(EContrast eContrast,uint32_t *Result)
{
	CJ_RESULT Res;
	switch(eContrast)
	{
	case ContrastVeryLow:
		Res=_CtSetContrast(0,Result);
		break;
	case ContrastLow:
		Res=_CtSetContrast(70,Result);
		break;
	case ContrastMedium:
		Res=_CtSetContrast(120,Result);
		break;
	case ContrastHigh:
		Res=_CtSetContrast(160,Result);
		break;
	case ContrastVeryHigh:
		Res=_CtSetContrast(255,Result);
		break;
	default:
		Res=CJ_ERR_WRONG_PARAMETER;
	}
	return Res;
}


CJ_RESULT CCPTReader::CtSetBacklight(EBacklight eBacklight,uint32_t *Result)
{
	CJ_RESULT Res;
	switch(eBacklight)
	{
	case BacklightOff:
		Res=_CtSetBacklight(0,Result);
		break;
	case BacklightVeryLow:
		Res=_CtSetBacklight(70,Result);
		break;
	case BacklightLow:
		Res=_CtSetBacklight(100,Result);
		break;
	case BacklightMedium:
		Res=_CtSetBacklight(120,Result);
		break;
	case BacklightHigh:
		Res=_CtSetBacklight(160,Result);
		break;
	case BacklightVeryHigh:
		Res=_CtSetBacklight(255,Result);
		break;
	default:
		Res=CJ_ERR_WRONG_PARAMETER;
	}
	return Res;
}

bool CCPTReader::HastModulestoreInfo()
{
	return true;
}

CJ_RESULT CCPTReader::CtSetSilentMode(bool boolMode,bool *pboolMode,uint32_t *Result)
{
	if(pboolMode)
		*pboolMode=boolMode;
  if(Result)
		*Result=0;
	return CJ_SUCCESS;
}

CJ_RESULT CCPTReader::CtGetSilentMode(bool *pboolMode,uint32_t *Result)
{
	if(pboolMode)
		*pboolMode=0;
  if(Result)
		*Result=0;
	return CJ_SUCCESS;
}

CJ_RESULT CCPTReader::SpecialLess3_0_41()
{
	return CJ_SUCCESS;
}

bool CCPTReader::PinDirectSupported()
{
	return true;
}
