#include "Platform.h"
#include <string.h>
#include <stdio.h>
#include "ECPReader.h"
#include "BaseCommunication.h"

#define base CEC30Reader

CECPReader::CECPReader(CReader *Owner,CBaseCommunication *Communicator)
			  :base(Owner,Communicator)
{
}

CECPReader::~CECPReader(void)
{
}


uint16_t CECPReader::HostToReaderShort(uint16_t Value)
{
   return InversByteOrderShort(htons(Value));
}

uint32_t CECPReader::HostToReaderLong(uint32_t Value)
{
   return InversByteOrderLong(htonl(Value));
}

void CECPReader::SetHWString(char *String)
{
   strcpy(String,"ECP_");
}

void CECPReader::GetProductString(uint8_t *Product)
{
	memcpy(Product,"CJECP",5);
}


CJ_RESULT CECPReader::CtSetContrast(EContrast eContrast,uint32_t *Result)
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

CJ_RESULT CECPReader::BuildReaderInfo()
{
	CJ_RESULT Res;

	Res = base::BuildReaderInfo();

	m_ReaderInfo.HardwareMask |=	RSCT_READER_HARDWARE_MASK_BUZZER |
									RSCT_READER_HARDWARE_MASK_DISPLAY_ONOFF;
	return Res;
}

RSCT_IFD_RESULT CECPReader::_IfdTransmit(const uint8_t *cmd, uint16_t cmd_len,uint8_t *response,uint16_t *response_len)
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
				sprintf((char *)response,"%04X\x90",0x0420);
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
