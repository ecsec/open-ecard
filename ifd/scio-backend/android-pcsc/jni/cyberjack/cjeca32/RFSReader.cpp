#include "Platform.h"
#include <string.h>
#include <stdio.h>
#include "RFSReader.h"
#include "BaseCommunication.h"

#include "memory_placements.h"


#define base CECPReader

CRFSReader::CRFSReader(CReader *Owner,CBaseCommunication *Communicator)
			  :base(Owner,Communicator)
{
}

CRFSReader::~CRFSReader(void)
{
}

void CRFSReader::SetHWString(char *String)
{
   strcpy(String,"RFS_");
}

void CRFSReader::GetProductString(uint8_t *Product)
{
	memcpy(Product,"CJRFS",5);
}

bool CRFSReader::ATRFilter(bool IsWarm)
{
		if(m_ATR_Length>=5)
		{
			if(m_ATR[0]==0x4b)
			{
				m_ATR[0]=0x3b;
				m_bIsRF=true;
				return true;
			}
		}
		return base::ATRFilter(IsWarm);
}

RSCT_IFD_RESULT CRFSReader::IfdPower(uint32_t Mode, uint8_t *ATR, uint32_t *ATR_Length, uint32_t Timeout)
{
	uint32_t OrginalATR_Length=0;
	if(ATR_Length!=0)
		OrginalATR_Length=*ATR_Length;
	m_bIsRF=false;
	RSCT_IFD_RESULT Result=base::IfdPower(Mode,ATR,ATR_Length,Timeout);
	if(Result==STATUS_SUCCESS && Mode==SCARD_COLD_RESET || Mode==SCARD_WARM_RESET)
	{
		if(m_bIsRF)
		{
				m_PossibleProtocols=/*SCARD_PROTOCOL_T0 |*/ SCARD_PROTOCOL_T1;
				m_ActiveProtocol=SCARD_PROTOCOL_T1;
        m_ReaderState=SCARD_SPECIFIC;
		}
	}

	return Result;

}


uint32_t CRFSReader::GetReadersInputBufferSize()
{
	cj_ModuleInfo *Info;
	Info=FindModule(MODULE_ID_KERNEL);
	
  return 5130;
}

bool CRFSReader::PinDirectSupported()
{
	return true;
}

CJ_RESULT CRFSReader::SpecialLess3_0_41()
{
	return CJ_SUCCESS;
}

RSCT_IFD_RESULT CRFSReader::IfdSetProtocol(uint32_t *pProtocol)
{
	if(m_bIsRF)
	{
		uint32_t Protocol=*pProtocol;
		*pProtocol=0;
		if(m_ReaderState==SCARD_ABSENT)
			return STATUS_NO_MEDIA;
		if(m_ReaderState==SCARD_SPECIFIC)
		{
			if(Protocol & (SCARD_PROTOCOL_DEFAULT | SCARD_PROTOCOL_OPTIMAL))
				Protocol|=(SCARD_PROTOCOL_T0 | SCARD_PROTOCOL_T1 | SCARD_PROTOCOL_RAW);
			if(m_ActiveProtocol & Protocol)
			{
				*pProtocol=m_ActiveProtocol;
				return STATUS_SUCCESS;
			}
			else
				return STATUS_NOT_SUPPORTED;
		}
		else
			return STATUS_NOT_SUPPORTED;
	}
	return base::IfdSetProtocol(pProtocol);
}

RSCT_IFD_RESULT CRFSReader::_IfdTransmit(const uint8_t *cmd, uint16_t cmd_len,uint8_t *response,uint16_t *response_len)
{
	//int l;
	//char *str;
	//	uint16_t rest=*response_len;
	if(cmd_len==5 && cmd[0]==0xff && cmd[1]==0x9a && cmd[2]==0x01  && cmd[4]==0)
	{
		switch(cmd[3])
		{
		case 9:
			if(*response_len>=8)
			{
				memcpy(response,"848500\x90\x00",8);
				*response_len=8;
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
		return CEC30Reader::_IfdTransmit(cmd,cmd_len,response,response_len);
	}

	if(m_bIsRF && cmd_len==5 && cmd[0]==0xff && cmd[1]==0xca && cmd[2]==0x01 && cmd[3]==0)
	{
			if(*response_len>=m_ATR_Length-3)
			{
				if(cmd[4]==0 || cmd[4]>=m_ATR_Length-5)
				{
				   memcpy(response,m_ATR+4,m_ATR_Length-5);
					if(cmd[4]!=0 && cmd[4]>m_ATR_Length-5)
					{
				      memset(response+m_ATR_Length-5,0,cmd[4]-m_ATR_Length+5);
				      memcpy(response+cmd[4],"\x62\x82",2);
					   *response_len=cmd[4]+2;
					}
					else
					{
				      memcpy(response+m_ATR_Length-5,"\x90\x00",2);
					   *response_len=(uint16_t)(m_ATR_Length-3);
					}
					return STATUS_SUCCESS;
				}
			}
			if(*response_len>=2)
			{
			   response[0]=0x6C;
			   response[1]=(uint8_t)(m_ATR_Length-5);
				*response_len=2;
				return STATUS_SUCCESS;
			}
   		return STATUS_BUFFER_TOO_SMALL;
	}
	return base::_IfdTransmit(cmd,cmd_len,response,response_len);
}

CJ_RESULT CRFSReader::CtSetContrast(EContrast eContrast,uint32_t *Result)
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

RSCT_IFD_RESULT CRFSReader::IfdVendor(uint32_t IoCtrlCode,uint8_t *Input,uint32_t InputLength,uint8_t *Output,uint32_t *OutputLength)
{
	uint32_t Result;
	uint32_t ResponseLen=*OutputLength-6;
	uint32_t ApplicationErrorLength=4;
	uint16_t InternalLength;
	uint16_t InternalLength2;
	uint16_t InternalLengthBuffer;
	uint8_t lengthCHAT;
	uint8_t lengthPIN;
	uint16_t lengthCD;
	uint8_t lengthCAR;
	uint8_t lengthCARprev;
	switch(IoCtrlCode)
	{
	case CJPCSC_VEN_IOCTRL_EXECUTE_PACE:
		if(InputLength<3 || *OutputLength<6)
			return STATUS_INFO_LENGTH_MISMATCH;
		memcpy(&InternalLengthBuffer,Input+1,2);
		if(InputLength!=3+InternalLengthBuffer)
			return STATUS_INFO_LENGTH_MISMATCH;
		InternalLength=HostToReaderShort(InternalLengthBuffer);
		if(CopyIfdInput(Input,InputLength))
			return STATUS_INSUFFICIENT_RESOURCES;
		Input=ifd_in_buffer;
		memcpy(Input+1,&InternalLength,2);
		if(Input[0]==2)
		{
			if(InputLength>4)
				lengthCHAT=Input[4];
			if(InputLength>5+lengthCHAT)
				lengthPIN=Input[5+lengthCHAT];
/*			if(InputLength==7+lengthCHAT+lengthPIN)
				return STATUS_INFO_LENGTH_MISMATCH;*/
			if(InputLength>7+lengthCHAT+lengthPIN)
			{
				memcpy(&lengthCD,Input+7+lengthCHAT+lengthPIN,2);
/*				if(InputLength!=8+lengthCHAT+lengthPIN+lengthCD)
   				return STATUS_INFO_LENGTH_MISMATCH;*/
				lengthCD=HostToReaderShort(lengthCD);
				memcpy(Input+7+lengthCHAT+lengthPIN,&lengthCD,2);
			}
		}
		switch(CtApplicationData(MODULE_ID_KERNEL,CCID_ESCAPE_DO_PACE,Input,InputLength, &Result, Output+6, &ResponseLen, Output, &ApplicationErrorLength))
		{
		case CJ_SUCCESS:
			if(*OutputLength<6+ResponseLen || ResponseLen>65535)
   			return STATUS_INSUFFICIENT_RESOURCES;
			*OutputLength=6+ResponseLen;
	      InternalLength2=(uint16_t)ResponseLen;
			memcpy(Output+4,&InternalLength2,2);
			if(ApplicationErrorLength==0)
				memset(Output,0,4);
			if(Input[0]==2 && ResponseLen>=4)
			{
            memcpy(&InternalLength,Output+8,2);
				InternalLength=ReaderToHostShort(InternalLength);
            memcpy(Output+8,&InternalLength,2);
				if(ResponseLen>6+InternalLength)
				{
					lengthCAR=Output[10+InternalLength];
					if(ResponseLen>7+InternalLength+lengthCAR)
					{
						lengthCARprev=Output[11+InternalLength+lengthCAR];
						if(lengthCARprev>0 && GetEnviroment("PACE_DisableCARprev",0))
						{
							Output[11+InternalLength+lengthCAR]=0;
							memmove(Output+12+InternalLength+lengthCAR,Output+12+InternalLength+lengthCAR+lengthCARprev,ResponseLen-(6+InternalLength+lengthCAR+lengthCARprev));
							ResponseLen-=lengthCARprev;
							*OutputLength-=lengthCARprev;
							InternalLength2-=lengthCARprev;
							memcpy(Output+4,&InternalLength2,2);

							lengthCARprev=0;
						}
						if(ResponseLen>8+InternalLength+lengthCAR+lengthCARprev)
						{
							memcpy(&InternalLengthBuffer,Output+12+InternalLength+lengthCAR+lengthCARprev,2);
							InternalLengthBuffer=ReaderToHostShort(InternalLengthBuffer);
							memcpy(Output+12+InternalLength+lengthCAR+lengthCARprev,&InternalLengthBuffer,2);
						}
					}
				}
			}
			return STATUS_SUCCESS;
		default:
			*OutputLength=0;
			return STATUS_UNHANDLED_EXCEPTION;
		}
		
	default:
	   return base::IfdVendor(IoCtrlCode,Input,InputLength,Output,OutputLength);
	}
}

CJ_RESULT CRFSReader::BuildReaderInfo()
{
	CJ_RESULT Res;

	Res = base::BuildReaderInfo();
	

	m_ReaderInfo.HardwareMask |=	RSCT_READER_HARDWARE_MASK_RFID |
									RSCT_READER_HARDWARE_MASK_PACE;
	return Res;
}

bool CRFSReader::HastModulestoreInfo()
{
	return true;
}

CJ_RESULT CRFSReader::CtSetSilentMode(bool boolMode,bool *pboolMode,uint32_t *Result)
{
	if(pboolMode)
		*pboolMode=boolMode;
  if(Result)
		*Result=0;
	return CJ_SUCCESS;
}

CJ_RESULT CRFSReader::CtGetSilentMode(bool *pboolMode,uint32_t *Result)
{
	if(pboolMode)
		*pboolMode=0;
  if(Result)
		*Result=0;
	return CJ_SUCCESS;
}
