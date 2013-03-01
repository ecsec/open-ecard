#include "Platform.h"
#include <string.h>
#include "ECFReader.h"
#include "BaseCommunication.h"
#include "eca_defines.h"
#include "eca_module_errors.h"

#define base CEC30Reader

CECFReader::CECFReader(CReader *Owner,CBaseCommunication *Communicator)
			  :base(Owner,Communicator)
{
}

CECFReader::~CECFReader(void)
{
}


uint16_t CECFReader::HostToReaderShort(uint16_t Value)
{
   return InversByteOrderShort(htons(Value));
}

uint32_t CECFReader::HostToReaderLong(uint32_t Value)
{
   return InversByteOrderLong(htonl(Value));
}

void CECFReader::SetHWString(char *String)
{
   strcpy(String,"ECF_");
}

void CECFReader::GetProductString(uint8_t *Product)
{
	memcpy(Product,"CJECF",5);
	if(GetEnviroment("ecom_f_ident",0)!=0)
	{
		memcpy(Product,"ECUSB",5);
	}
}


CJ_RESULT CECFReader::CtSetContrast(EContrast eContrast,uint32_t *Result)
{
	CJ_RESULT Res;
	switch(eContrast)
	{
	case ContrastVeryLow:
		Res=_CtSetContrast(40,Result);
		break;
	case ContrastLow:
		Res=_CtSetContrast(30,Result);
		break;
	case ContrastMedium:
		Res=_CtSetContrast(20,Result);
		break;
	case ContrastHigh:
		Res=_CtSetContrast(10,Result);
		break;
	case ContrastVeryHigh:
		Res=_CtSetContrast(0,Result);
		break;
	default:
		Res=CJ_ERR_WRONG_PARAMETER;
	}
	return Res;
}

CJ_RESULT CECFReader::SetFlashMask(void)
{
	uint32_t Result;
	uint32_t Value=HostToReaderLong(0xa374b516);

   return Escape(MODULE_ID_KERNEL,CCID_ESCAPE_MODULE_SET_FLASH_MASK,(uint8_t *)&Value,sizeof(Value),&Result,0,0);
}


int CECFReader::GetWarmstartTimeout(void)
{
	return 4500;
}
