#include "Platform.h"
#include <string.h>
#include "ECAReader.h"
#include "BaseCommunication.h"

#define base CEC30Reader

CECAReader::CECAReader(CReader *Owner,CBaseCommunication *Communicator)
			  :base(Owner,Communicator)
{
}

CECAReader::~CECAReader(void)
{
}


uint16_t CECAReader::HostToReaderShort(uint16_t Value)
{
   return InversByteOrderShort(htons(Value));
}

uint32_t CECAReader::HostToReaderLong(uint32_t Value)
{
   return InversByteOrderLong(htonl(Value));
}

void CECAReader::SetHWString(char *String)
{
   strcpy(String,"ECA_");
}

void CECAReader::GetProductString(uint8_t *Product)
{
	memcpy(Product,"CJECA",5);
	if(GetEnviroment("ecom_a_ident",0)!=0)
	{
		memcpy(Product,"ECUSB",5);
	}
}


CJ_RESULT CECAReader::CtSetContrast(EContrast eContrast,uint32_t *Result)
{
	CJ_RESULT Res;
	switch(eContrast)
	{
	case ContrastVeryLow:
		Res=_CtSetContrast(70,Result);
		break;
	case ContrastLow:
		Res=_CtSetContrast(100,Result);
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

RSCT_IFD_RESULT CECAReader::IfdPower(uint32_t Mode, uint8_t *ATR, uint32_t *ATR_Length, uint32_t Timeout)
{
//	cj_ModuleInfo *pInfo;
	switch(Mode)
	{
	case SCARD_COLD_RESET:
	case SCARD_WARM_RESET:
		*ATR_Length=0;
/*		pInfo=FindModule(MODULE_ID_KERNEL);
		if(pInfo && (pInfo->Version<0x30 || (pInfo->Version==0x30 && pInfo->Revision<=6)))
		{
			return STATUS_UNRECOGNIZED_MEDIA;
		}
		else
			return base::IfdPower(SCARD_POWER_DOWN,NULL,NULL,0);*/
		if(memcmp(m_ReaderInfo.TestDate+6,"2007",4)==0 && 
			(memcmp(m_ReaderInfo.TestDate+3,"05",2)<0 || 
			 (memcmp(m_ReaderInfo.TestDate+3,"05",2)==0 && memcmp(m_ReaderInfo.TestDate,"03",2)<=0)))
		{
			return STATUS_UNRECOGNIZED_MEDIA;
		}
		else
			return base::IfdPower(Mode,ATR,ATR_Length,Timeout);
	default:
		return base::IfdPower(Mode,ATR,ATR_Length,Timeout);
	}
}
