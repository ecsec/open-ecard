#include "Platform.h"
#include <string.h>
#include <stdio.h>
#include "RFKReader.h"
#include "BaseCommunication.h"

#include "memory_placements.h"


#define base CRFSReader

CRFKReader::CRFKReader(CReader *Owner,CBaseCommunication *Communicator)
			  :base(Owner,Communicator)
{
}

CRFKReader::~CRFKReader(void)
{
}

void CRFKReader::SetHWString(char *String)
{
   strcpy(String,"RFK_");
}

void CRFKReader::GetProductString(uint8_t *Product)
{
	memcpy(Product,"CJRFK",5);
}

CJ_RESULT CRFKReader::CtSetBacklight(EBacklight eBacklight,uint32_t *Result)
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