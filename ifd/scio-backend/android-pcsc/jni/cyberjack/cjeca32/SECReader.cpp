#include "Platform.h"
#include <string.h>
#include <stdio.h>
#include "SECReader.h"
#include "BaseCommunication.h"

#define base CEC30Reader

CSECReader::CSECReader(CReader *Owner,CBaseCommunication *Communicator)
			  :base(Owner,Communicator)
{
}

CSECReader::~CSECReader(void)
{
}


uint16_t CSECReader::HostToReaderShort(uint16_t Value)
{
   return InversByteOrderShort(htons(Value));
}

uint32_t CSECReader::HostToReaderLong(uint32_t Value)
{
   return InversByteOrderLong(htonl(Value));
}

void CSECReader::SetHWString(char *String)
{
   strcpy(String,"SEC_");
}

void CSECReader::GetProductString(uint8_t *Product)
{
	memcpy(Product,"CJSEC",5);
}


CJ_RESULT CSECReader::CtSetContrast(EContrast eContrast,uint32_t *Result)
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

RSCT_IFD_RESULT CSECReader::_IfdTransmit(const uint8_t *cmd, uint16_t cmd_len,uint8_t *response,uint16_t *response_len)
{
	int l;
	char *str;
	//	uint16_t rest=*response_len;
	if(cmd_len==5 && cmd[0]==0xff && cmd[1]==0x9a && cmd[2]==0x01  && cmd[4]==0)
	{
		switch(cmd[3])
		{
		case 4:
			if(*response_len>=6)
			{
				sprintf((char *)response,"%04X\x90",0x0410);
				*response_len=6;
				return STATUS_SUCCESS;
			}
			else
			{
				*response_len=0;
				return STATUS_BUFFER_TOO_SMALL;
			}
			break;
		default:;
		}
	}

	return base::_IfdTransmit(cmd,cmd_len,response,response_len);
}

