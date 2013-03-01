#include "Platform.h"
#include <string.h>
#include <stdio.h>
#include "ECRReader.h"
#include "BaseCommunication.h"

#include "memory_placements.h"


#define base CECPReader

CECRReader::CECRReader(CReader *Owner,CBaseCommunication *Communicator)
			  :base(Owner,Communicator)
{
}

CECRReader::~CECRReader(void)
{
}

void CECRReader::SetHWString(char *String)
{
   strcpy(String,"ECR_");
}

void CECRReader::GetProductString(uint8_t *Product)
{
	memcpy(Product,"CJECR",5);
}

RSCT_IFD_RESULT CECRReader::IfdPower(uint32_t Mode, uint8_t *ATR, uint32_t *ATR_Length, uint32_t Timeout)
{
	uint32_t OrginalATR_Length=0;
	if(ATR_Length!=0)
		OrginalATR_Length=*ATR_Length;
	RSCT_IFD_RESULT Result=base::IfdPower(Mode,ATR,ATR_Length,Timeout);
	m_bIsRF=false;
	if(Result==STATUS_SUCCESS && Mode==SCARD_COLD_RESET || Mode==SCARD_WARM_RESET)
	{
		if(m_ATR_Length>=6 && m_ATR_Length<=12)
		{
			uint8_t tck=0;
			if(m_ATR[0]==0x41)
			{
				bool bDoSelectTCL=true;

				m_SerialNumberLength=m_ATR_Length-3;
				memcpy(m_SerialNumber,m_ATR+3,m_SerialNumberLength);
				if(m_ATR[1] & 0x20)
				{
					if(m_ATR[1]==0x28 || m_ATR[1]==0x38)
					{
						if(GetEnviroment("PrefereMifareClasic",0))
						{
							bDoSelectTCL=false;
						}
					}
					if(bDoSelectTCL)
					{
						CCID_Message Message;
						CCID_Response Response;
						unsigned int i;

						memset(&Message,0,sizeof(Message));
						Message.bMessageType=PC_TO_RDR_SETPARAMETERS;
          			Message.Header.SetParameters.bProtocolNum=3;//Get ATS
						switch(Transfer(&Message,&Response))
						{
						case CJ_SUCCESS:
							if((Response.bStatus&3)==2)
								return STATUS_NO_MEDIA;
							else if((Response.bStatus&3)==1 || (Response.bStatus & 0x40))
								return STATUS_IO_TIMEOUT;
							memcpy(m_ATR,"\x3b\x80\x80\x01",4);
							if(Response.Data.abData[0]>15)
								return STATUS_NO_MEDIA;
							Response.Data.abData[2]&=0x70;
							Response.Data.abData[2]>>=4;
							i=0;
							while(Response.Data.abData[2])
							{
								if(Response.Data.abData[2] & 1)
								   i++;
								Response.Data.abData[2]>>=1;
							}
							Response.Data.abData[0]-=2+i;
							m_ATR[1]|=Response.Data.abData[0];
							memcpy(m_ATR+4,Response.Data.abData+3+i,Response.Data.abData[0]);
							m_ATR_Length=Response.Data.abData[0]+4;

							break;
						default:
							return STATUS_DEVICE_NOT_CONNECTED;
						}
					}
				}
				else
					bDoSelectTCL=false;
				if(!bDoSelectTCL)
				{
					uint8_t merkSAK=m_ATR[1];
				   memcpy(m_ATR,"\x3b\x8f\x80\x01\x80\x4f\x0c\xA0\x00\x00\x03\x06\x00\x00\x00\x00\x00\x00\x00",19);
					m_ATR_Length=19;
					switch(merkSAK & 0xdf)
					{
					case 0x08:
						memcpy(m_ATR+12,"\x03\x00\x01",3);
						break;
					case 0x09:
						memcpy(m_ATR+12,"\x03\x00\x26",3);
						break;
					case 0x18:
						memcpy(m_ATR+12,"\x03\x00\x02",3);
						break;
					default:;
					}
				}
				for(unsigned int i=1;i<m_ATR_Length;i++)
				{
					tck^=m_ATR[i];
				}
				m_ATR[m_ATR_Length]=tck;
				m_ATR_Length++;
				if(OrginalATR_Length<m_ATR_Length)
					return STATUS_BUFFER_TOO_SMALL;
				memcpy(ATR,m_ATR,m_ATR_Length);
				*ATR_Length=m_ATR_Length;
				m_PossibleProtocols=/*SCARD_PROTOCOL_T0 |*/ SCARD_PROTOCOL_T1;
				m_ActiveProtocol=SCARD_PROTOCOL_T1;
//   		        m_ReaderState=SCARD_NEGOTIABLE;
   		        m_ReaderState=SCARD_SPECIFIC;
				m_bIsRF=true;
			}
			else if(m_ATR[0]==0x42)
			{
				m_SerialNumberLength=4;
				memcpy(m_SerialNumber,m_ATR,4);
				memmove(m_ATR+4,m_ATR+1,8);
				memcpy(m_ATR,"\x3b\x88\x80\x01",4);
				m_ATR_Length=12;
				for(unsigned int i=1;i<m_ATR_Length;i++)
				{
					tck^=m_ATR[i];
				}
				m_ATR[m_ATR_Length]=tck;
				m_ATR_Length++;
				if(OrginalATR_Length<m_ATR_Length)
					return STATUS_BUFFER_TOO_SMALL;
				memcpy(ATR,m_ATR,m_ATR_Length);
				*ATR_Length=m_ATR_Length;
				m_PossibleProtocols=/*SCARD_PROTOCOL_T0 |*/ SCARD_PROTOCOL_T1;
				m_ActiveProtocol=SCARD_PROTOCOL_T1;
//   		        m_ReaderState=SCARD_NEGOTIABLE;
   		        m_ReaderState=SCARD_SPECIFIC;
				m_bIsRF=true;
			}
		}
	}

	return Result;

}

//#include <assert.h>

uint32_t CECRReader::GetReadersInputBufferSize()
{
	cj_ModuleInfo *Info;
//	assert(0);
	Info=FindModule(MODULE_ID_KERNEL);
	
	if(Info!=NULL && (Info->Version>=0x30 || Info->Version==0x30 && Info->Revision>=70))
	   return 2048;
	else
		return base::GetReadersInputBufferSize();
}




RSCT_IFD_RESULT CECRReader::IfdSetProtocol(uint32_t *pProtocol)
{
	if(m_bIsRF)
	{
		uint32_t Protocol=*pProtocol;
		*pProtocol=0;
		if(m_ReaderState==SCARD_ABSENT)
			return STATUS_NO_MEDIA;
		if(m_ReaderState==SCARD_NEGOTIABLE)
		{


			if(Protocol & (SCARD_PROTOCOL_DEFAULT | SCARD_PROTOCOL_OPTIMAL))
				Protocol|=(SCARD_PROTOCOL_T0 | SCARD_PROTOCOL_T1);
			
			if((Protocol & SCARD_PROTOCOL_T0) && (m_PossibleProtocols & SCARD_PROTOCOL_T0))
			{
   			m_ActiveProtocol=SCARD_PROTOCOL_T0;
			}
			else if((Protocol & SCARD_PROTOCOL_T1) && (m_PossibleProtocols & SCARD_PROTOCOL_T1))
			{
				m_ActiveProtocol=SCARD_PROTOCOL_T1;
			}
			else
			{
				return STATUS_INVALID_DEVICE_REQUEST;
			}

			*pProtocol=m_ActiveProtocol;
			m_ReaderState=SCARD_SPECIFIC;
			return STATUS_SUCCESS;

		}
		else if(m_ReaderState==SCARD_SPECIFIC)
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
			return STATUS_IO_TIMEOUT;
	}
	return base::IfdSetProtocol(pProtocol);
}

RSCT_IFD_RESULT CECRReader::_IfdTransmit(const uint8_t *cmd, uint16_t cmd_len,uint8_t *response,uint16_t *response_len)
{
	int l;
	char *str;
	//	uint16_t rest=*response_len;
	if(cmd_len==5 && cmd[0]==0xff && cmd[1]==0x9a && cmd[2]==0x01  && cmd[4]==0)
	{
		switch(cmd[3])
		{
		case 3:
			if(*response_len>=(l=strlen("cyberJack RFID komfort (Test)"))+2)
			{
				memcpy(response,"cyberJack RFID komfort (Test)",l);
				memcpy(response+l,"\x90\x00",2);
				*response_len=l+2;
				return STATUS_SUCCESS;
			}
			else
			{
				*response_len=0;
				return STATUS_BUFFER_TOO_SMALL;
			}
			break;
		case 4:
			if(*response_len>=6)
			{
				sprintf((char *)response,"%04X\x90",0x0450);
				*response_len=6;
				return STATUS_SUCCESS;
			}
			else
			{
				*response_len=0;
				return STATUS_BUFFER_TOO_SMALL;
			}
			break;
		case 8:

			if(*response_len>=7  && GetReadersInputBufferSize()<=99999 || *response_len>=6 && GetReadersInputBufferSize()<=9999)
			{
				sprintf((char *)response,"%d",(int)768);
				memcpy(response+(l=strlen((char *)response)),"\x90\x00",2);
				*response_len=l+2;
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

	if(m_bIsRF && cmd_len==5 && cmd[0]==0xff && cmd[1]==0xca && cmd[2]<=0x01 && cmd[3]==0)
	{
		if(cmd[2]==0)
		{
			if(*response_len>=m_SerialNumberLength+2)
			{
				if(cmd[4]==0 || cmd[4]>=m_SerialNumberLength)
				{
				   memcpy(response,m_SerialNumber,m_SerialNumberLength);
					if(cmd[4]!=0 && cmd[4]>m_SerialNumberLength)
					{
				      memset(response+m_SerialNumberLength,0,cmd[4]-m_SerialNumberLength);
				      memcpy(response+cmd[4],"\x62\x82",2);
					   *response_len=cmd[4]+2;
					}
					else
					{
				      memcpy(response+m_SerialNumberLength,"\x90\x00",2);
					   *response_len=m_SerialNumberLength+2;
					}
					return STATUS_SUCCESS;
				}
			}
			if(*response_len>=2)
			{
			   response[0]=0x6C;
			   response[1]=m_SerialNumberLength;
				*response_len=2;
				return STATUS_SUCCESS;
			}
   		return STATUS_BUFFER_TOO_SMALL;
		}
		else
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
	}
	return base::_IfdTransmit(cmd,cmd_len,response,response_len);
}

RSCT_IFD_RESULT CECRReader::IfdVendor(uint32_t IoCtrlCode,uint8_t *Input,uint32_t InputLength,uint8_t *Output,uint32_t *OutputLength)
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

CJ_RESULT CECRReader::BuildReaderInfo()
{
	CJ_RESULT Res;

	Res = base::BuildReaderInfo();
	

	m_ReaderInfo.HardwareMask |=	RSCT_READER_HARDWARE_MASK_RFID |
									RSCT_READER_HARDWARE_MASK_PACE;
	return Res;
}

