#include "Platform.h"
#include "CCIDReader.h"
#include "Reader.h"
#include "BaseCommunication.h"
#include <stdio.h>
#include <math.h>

#include "memory_placements.h"

#define base CBaseReader

using namespace std;


CCCIDReader::CCCIDReader(CReader *Owner,CBaseCommunication *Communicator)
  :CBaseReader(Owner,Communicator)
  ,cmd_buffer(NULL)
  ,cmd_buffer_len(0)
  ,ifd_in_buffer(NULL)
  ,ifd_in_buffer_len(0)
  ,m_SequenceNo(0)
{
}

CCCIDReader::~CCCIDReader(void)
{
	if(cmd_buffer_len>0)
		delete [] cmd_buffer;
	if(ifd_in_buffer_len>0)
		delete [] ifd_in_buffer;
}

#ifdef IT_TEST2
int CCCIDReader::Transfer(CCID_Message *Message, CCID_Response *Response)
{
	int Res;
	CCID_Message InternalMessage;
	CCID_Response InternalResponse;

	CheckReaderDependend(*Message);
	memset(&InternalMessage,0,sizeof(InternalMessage));
	InternalMessage.bMessageType=PC_TO_RDR_GETSLOTSTATUS;
	InternalMessage.bSeq=m_SequenceNo+1;
	memset(&InternalResponse,0,sizeof(InternalResponse));



	uint32_t Length=Message->dwLength;
	Message->bSeq=m_SequenceNo;
/*	if(Message->bMessageType==PC_TO_RDR_ESCAPE && Message->dwLength>12)
	{
		if(Message->Data.Escape.Reader.EC30.dwApplication==0x55555555 && 
			Message->Data.Escape.Reader.EC30.wFunction==0)
		{
			uint16_t lFilter=(uint16_t)((((uint16_t)Message->Data.Escape.Reader.EC30.Data.abDate[0])<<8)+((uint16_t)Message->Data.Escape.Reader.EC30.Data.abDate[1])));
			uint16_t lMatch =(uint16_t)((((uint16_t)Message->Data.Escape.Reader.EC30.Data.abDate[1])<<8)+((uint16_t)Message->Data.Escape.Reader.EC30.Data.abDate[3])));
			uint16_t lPost=(uint16_t)((((uint16_t)Message->Data.Escape.Reader.EC30.Data.abDate[2])<<8)+((uint16_t)Message->Data.Escape.Reader.EC30.Data.abDate[5])));
			if(Message->dwLength==12+lFilter+lMatch+lPost)
			{
				if(lFilter>m_lFilter)
				{
					if(m_lFilter)
					{
						delete[] m_bFilter;
						m_lFilter=0;
					}
					m_bFilter=new uint16_t[



				}
			}



	}*/

#ifdef IT_TEST
	if(Message->bMessageType!=PC_TO_RDR_SECURE)
#endif
	Message->bSlot=0;
	if(!m_pCommunicator)
		return CJ_ERR_DEVICE_LOST;
	Message->dwLength=HostToReaderLong(Length);
	if((Res=Write(Message,10+Length))!=CJ_SUCCESS)
	{
		return Res;
	}
	if(Message->bMessageType==PC_TO_RDR_SECURE)
	{
		if((Res=Write(&InternalMessage,10))!=CJ_SUCCESS)
		{
			return Res;
		}
	}
	Length=sizeof(CCID_Response);
	if((Res=m_pCommunicator->Read(Response,&Length))!=CJ_SUCCESS)
	{
		return Res;
	}

	if(Message->bMessageType==PC_TO_RDR_SECURE)
	{
		uint32_t L=sizeof(InternalResponse);
		if((Res=m_pCommunicator->Read(&InternalResponse,&L))!=CJ_SUCCESS)
		{
			return Res;
		}
	}

	if(m_SequenceNo!=Response->bSeq)
	{
		return CJ_ERR_SEQ;
	}

	if((Response->bStatus & 3)!=0)
	{
		m_ActiveProtocol=0;
		m_ATR_Length=0;
	}
	if((Response->bStatus & 3)==2)
		m_ReaderState=SCARD_ABSENT;

	m_SequenceNo++;
	if(Message->bMessageType==PC_TO_RDR_SECURE)
		m_SequenceNo++;
	Response->dwLength=ReaderToHostLong(Response->dwLength);
	return CJ_SUCCESS;
}
#else
int CCCIDReader::Transfer(CCID_Message *Message, CCID_Response *Response)
{
	int Res;
	uint32_t Length;
	CheckReaderDepended(*Message);
	Length=Message->dwLength;
	Message->bSeq=m_SequenceNo;
#ifdef IT_TEST
	if(Message->bMessageType!=PC_TO_RDR_SECURE)
#endif
	Message->bSlot=0;
	if(!m_pCommunicator)
		return CJ_ERR_DEVICE_LOST;
	Message->dwLength=HostToReaderLong(Length);
	if((Res=Write(Message,10+Length))!=CJ_SUCCESS)
	{
		return Res;
	}
	do
	{
    	Length=sizeof(CCID_Response);
		if((Res=m_pCommunicator->Read(Response,&Length))!=CJ_SUCCESS)
		{
			return Res;
		}
		while(m_SequenceNo!=Response->bSeq)
		{
   			Length=sizeof(CCID_Response);
			if((Res=m_pCommunicator->Read(Response,&Length))!=CJ_SUCCESS)
   			return CJ_ERR_SEQ;
		}
	}while(Message->bMessageType==PC_TO_RDR_XFRBLOCK && Response->bMessageType==RDR_TO_PC_DATABLOCK && Response->bStatus==0x80 && Response->dwLength==0 && Length==10);


	if((Response->bStatus & 3)!=0)
	{
		m_ActiveProtocol=0;
		m_ATR_Length=0;
	}
	if((Response->bStatus & 3)==2)
		m_ReaderState=SCARD_ABSENT;

	m_SequenceNo++;
	Response->dwLength=ReaderToHostLong(Response->dwLength);
	return CJ_SUCCESS;
}
#endif
void CCCIDReader::DoInterruptCallback(uint8_t *Data,uint32_t ulDataLen)
{
	if(ulDataLen==2)
	{
		m_CriticalCallback.Enter();
		switch(Data[0])
		{
		case RDR_TO_PC_NOTIFYSLOTCHANGE:
			if(Data[1] & 1)
			   m_Owner->DebugLeveled(DEBUG_MASK_COMMUNICATION_INT,"NOTIFY: Slotstatus changed -- Inserted");
			else
			   m_Owner->DebugLeveled(DEBUG_MASK_COMMUNICATION_INT,"NOTIFY: Slotstatus changed -- Removed");
			if(m_ChangeIntCallback!=NULL)
				m_ChangeIntCallback(m_ChangeCallbackCtx,Data[1] & 1);
			break;
		case RDR_TO_PC_KEYEVENT:
			m_Owner->DebugLeveled(DEBUG_MASK_COMMUNICATION_INT,"NOTIFY: Key event");
			if(m_KeyIntCallback!=NULL)
				m_KeyIntCallback(m_KeyCallbackCtx,Data[1]);
			break;
		default:
			m_Owner->DebugLeveled(DEBUG_MASK_COMMUNICATION_INT | DEBUG_MASK_COMMUNICATION_ERROR,"Unknown Interrupt");
		}
		m_CriticalCallback.Leave();
	}
}

RSCT_IFD_RESULT CCCIDReader::IfdGetState(uint32_t *State)
{
	CCID_Message Message;
	CCID_Response Response;
	memset(&Message,0,sizeof(Message));
	*State=SCARD_UNKNOWN;

	Message.bMessageType=PC_TO_RDR_GETSLOTSTATUS;
	if(Transfer(&Message,&Response)==CJ_SUCCESS)
	{
		if(Response.bMessageType!=RDR_TO_PC_SLOTSTATUS)
			return STATUS_SUCCESS;
		switch(Response.bStatus & 3)
		{
		case 0:
			*State=m_ReaderState;
			break;
		case 1:
			*State=SCARD_SWALLOWED;
			m_ReaderState=SCARD_SWALLOWED;
			break;
		default:
			*State=SCARD_ABSENT;
			m_ReaderState=SCARD_ABSENT;
		}
	}
	else
		return STATUS_DEVICE_NOT_CONNECTED;
	
	return STATUS_SUCCESS;
}

RSCT_IFD_RESULT CCCIDReader::IfdSetProtocol(uint32_t *pProtocol)
{
	CCID_Message Message;
	CCID_Response Response;
	memset(&Message,0,sizeof(Message));
	uint32_t Protocol=*pProtocol;
	*pProtocol=0;

	Message.bMessageType=PC_TO_RDR_SETPARAMETERS;
	if(m_ReaderState==SCARD_ABSENT)
		return STATUS_NO_MEDIA;
	if(m_ReaderState==SCARD_NEGOTIABLE)
	{
		char bufferTA1[128];
		char bufferTC1[128];
		char bufferc[3];

		if(m_ATR[0]==0xff || (m_ATR[0] & 0xf0)==0x80)
		{
			*pProtocol=SCARD_PROTOCOL_RAW;
		   return STATUS_SUCCESS;
		}


		sprintf(bufferTA1,"ReplaceTA1_%02X",(int)m_TA1);
		strcpy(bufferTC1,"ReplaceTC1_");
		for(unsigned int i=0;i<m_ATR_Length;i++)
		{
			sprintf(bufferc,"%02X",(int)(m_ATR[i]));
			strcat(bufferTC1,bufferc);
		}


		if(Protocol & (SCARD_PROTOCOL_DEFAULT | SCARD_PROTOCOL_OPTIMAL))
         Protocol|=(SCARD_PROTOCOL_T0 | SCARD_PROTOCOL_T1);
/*		if((Protocol & (SCARD_PROTOCOL_T0 | SCARD_PROTOCOL_T1)==(SCARD_PROTOCOL_T0 | SCARD_PROTOCOL_T1))
         Protocol|=SCARD_PROTOCOL_OPTIMAL;*/
		
		if((Protocol & SCARD_PROTOCOL_T0) && (m_PossibleProtocols & SCARD_PROTOCOL_T0))
		{
			Message.dwLength=5;
			Message.Header.SetParameters.bProtocolNum=0;
			Message.Data.SetParameters.T0.bGuardTimeT0=(uint8_t)GetEnviroment(bufferTC1, m_TC1);
			Message.Data.SetParameters.T0.bmFindexDindex=(uint8_t)GetEnviroment(bufferTA1, m_TA1);
			Message.Data.SetParameters.T0.bWaitingIntegerT0=m_TC2;
		}
		else if((Protocol & SCARD_PROTOCOL_T1) && (m_PossibleProtocols & SCARD_PROTOCOL_T1))
		{
			Message.dwLength=7;
			Message.Header.SetParameters.bProtocolNum=1;
			Message.Data.SetParameters.T1.bGuardTimeT1=(uint8_t)GetEnviroment(bufferTC1, m_TC1);
			Message.Data.SetParameters.T1.bmFindexDindex=(uint8_t)GetEnviroment(bufferTA1, m_TA1);
			Message.Data.SetParameters.T1.bWaitingIntegerT1=m_TB3;
			Message.Data.SetParameters.T1.bIFSC=m_TA3;
		}
		else
		{
			return STATUS_INVALID_DEVICE_REQUEST;
		}

		switch(Transfer(&Message,&Response))
		{
		case CJ_SUCCESS:
			if((Response.bStatus&3)==2)
				return STATUS_NO_MEDIA;
			else if((Response.bStatus&3)==1 || (Response.bStatus & 0x40))
				return STATUS_IO_TIMEOUT;
			else
			{
				if(Message.Header.SetParameters.bProtocolNum==0)
				{
					m_ActiveProtocol=SCARD_PROTOCOL_T0;
				}
				else
				{
					m_ActiveProtocol=SCARD_PROTOCOL_T1;
				}
				*pProtocol=m_ActiveProtocol;
				m_ReaderState=SCARD_SPECIFIC;
			   return STATUS_SUCCESS;
			}
		default:
			return STATUS_DEVICE_NOT_CONNECTED;
		}

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


int CCCIDReader::ctBcsReset(uint8_t *atr,uint8_t *atr_len,uint8_t *historical,uint8_t *hist_len,uint8_t prevered,int Timeout)
{
	uint32_t len=*atr_len;
	uint32_t Protocol=SCARD_PROTOCOL_DEFAULT;
	*atr_len=0;
	if(prevered==1)
		Protocol=SCARD_PROTOCOL_T0;
	else if(prevered==2)
		Protocol=SCARD_PROTOCOL_T1;
	switch(IfdPower(SCARD_COLD_RESET,atr,&len,Timeout))
	{
	case STATUS_SUCCESS:
		switch(IfdSetProtocol(&Protocol))
		{
		case STATUS_SUCCESS:
			*atr_len=(uint8_t)len;
			memcpy(historical,m_Historical,m_Historical_Length);
			*hist_len=(uint8_t)m_Historical_Length;
   		return CJ_SUCCESS;
		case STATUS_DEVICE_NOT_CONNECTED:
			return CJ_ERR_DEVICE_LOST;
		case STATUS_NO_MEDIA:
			return CJ_ERR_NO_ICC;
		case STATUS_UNRECOGNIZED_MEDIA:
		case STATUS_INVALID_PARAMETER:
		case STATUS_IO_TIMEOUT:
		default:
			return CJ_ERR_PROT;
		}
	case STATUS_DEVICE_NOT_CONNECTED:
		return CJ_ERR_DEVICE_LOST;
	case STATUS_NO_MEDIA:
		return CJ_ERR_NO_ICC;
	case STATUS_CANCELLED:
		return CJ_ERR_PIN_CANCELED;
	case STATUS_UNRECOGNIZED_MEDIA:
	case STATUS_INVALID_PARAMETER:
	case STATUS_IO_TIMEOUT:
	default:
		return CJ_ERR_PROT;
	}
}

bool CCCIDReader::IsClass2(void)
{
	return true;
}

bool CCCIDReader::CheckUpdate(void)
{
	return false;
}



char CCCIDReader::CtData(uint8_t *sad,uint8_t *dad,const uint8_t *cmd, uint16_t lenc,uint8_t *response,uint16_t *lenr)
{
	if(cmd_buffer_len<lenc)
	{
		if(cmd_buffer_len>0)
			delete cmd_buffer;
		cmd_buffer_len=lenc+4096;
		cmd_buffer=new uint8_t[cmd_buffer_len];
		if(cmd_buffer==NULL)
			return -11;
        }
        memcpy(cmd_buffer,cmd,lenc);
	return _CtData(sad,dad,cmd_buffer,lenc,response,lenr);

}

bool CCCIDReader::CopyIfdInput(const uint8_t *Input,uint32_t Length)
{
   if(ifd_in_buffer_len<Length)
	{
		if(ifd_in_buffer_len>0)
			delete ifd_in_buffer;
		ifd_in_buffer_len=Length+4096;
		ifd_in_buffer=new uint8_t[ifd_in_buffer_len];
		if(ifd_in_buffer==NULL)
			ifd_in_buffer_len=0;
	}
	if(ifd_in_buffer)
	   memcpy(ifd_in_buffer,Input,Length);
	return (ifd_in_buffer==NULL);
}

bool CCCIDReader::IsClass3(void)
{
	return false;
}

void CCCIDReader::TransformText(uint8_t *tag,int len)
{
	while(len)
	{
	   len--;
		if(*tag=='\r')
			*tag='\n';
		tag++;
	}
}

CJ_RESULT CCCIDReader::cjOutput(uint8_t timeout,uint8_t *tag50,int tag50len)
{
	return CJ_SUCCESS;
}

CJ_RESULT CCCIDReader::KTLightCall(uint8_t *sad,uint8_t *dad,uint8_t *cmd, uint16_t lenc,int32_t Lc,uint8_t *data_ptr,int32_t Le,uint8_t *response,uint16_t *lenr)
{
	*sad=2;
	*dad=2;
	*lenr=2;
	memcpy(response,"\x6d\x00",2);
	return CJ_SUCCESS;
}

char CCCIDReader::_CtData(uint8_t *sad,uint8_t *dad,uint8_t *cmd, uint16_t lenc,uint8_t *response,uint16_t *lenr)
{
	uint8_t *data_ptr=NULL;
	int32_t Lc=-1;
	int32_t Le=-1;
	if(*dad==1 && *sad==2)					
	{
		if(*lenr<2)
		{
			*lenr=0;
			return -11;
		}
		if(lenc==5)
		{
			Le=cmd[4];
		}
		else if(lenc==5+cmd[4])
		{
			Lc=cmd[4];
			data_ptr=cmd+5;
#if defined(_LINUX)
		  /* bugfix 20071018 MPreuss: in case of REQUEST_ICC it is ok to omit Le in APDU */
			if (cmd[0]==0x20 && cmd[1]==0x12)
			  Le=0;
#endif
		}
		else if(lenc==6+cmd[4] && cmd[4]!=0)
		{
			Lc=cmd[4];
			data_ptr=cmd+5;
			Le=cmd[lenc-1];
		}
		else if(lenc>=7 && cmd[4]==0)
		{
			if(lenc==7)
			{
				Le=((int32_t)cmd[5]<<8)+cmd[6];
			}
			else if(lenc==7+((uint32_t)cmd[5]<<8)+cmd[6])
			{
				Lc=((int32_t)cmd[5]<<8)+cmd[6];
				data_ptr=cmd+7;
			}
			else if(lenc==9+((uint32_t)cmd[5]<<8)+cmd[6] && (cmd[5]!=0 || cmd[6]!=0))
			{
				Lc=((int32_t)cmd[5]<<8)+cmd[6];
				data_ptr=cmd+7;
				Le=((int32_t)cmd[lenc-2]<<8)+cmd[lenc-1];
			}
			else
			{
				response[0]=0x67;
				response[1]=0x00;
				*lenr=2;
				return 0;
			}
		}
		else if(lenc!=4)
		{
			response[0]=0x67;
			response[1]=0x00;
			*lenr=2;
			return 0;
		}
		*dad=2;
		*sad=1;
		if(cmd[0]==0x00)
		{
			if(cmd[1]==0xa4)
			{
				if(cmd[2]!=0 || cmd[3]!=0)
				{
					response[0]=0x6A;
					response[1]=0x00;
					*lenr=2;
					return 0;
				}
				if(Lc!=2)
				{
					response[0]=0x67;
					response[1]=0x00;
					*lenr=2;
					return 0;
				}
				if(memcmp(data_ptr,"\x3f\x00",2)==0)
				{
					if(*lenr<12)
					{
						return -11;
					}
					memcpy(m_reader_path,cmd+5,2);
					m_reader_path_len=2;
					memset(m_reader_file,0,2);
					memcpy(response,"\x00\x2d\x00\x2d\x88\x00\x00\x00\x00\x00\x90\x00",12);
					*lenr=12;
					return 0;
				}
				if(memcmp(data_ptr,"\x00\x20",2)==0 && m_reader_path_len==2 && memcmp(m_reader_path,"\x3f\x00",2)==0)
				{
					if(*lenr<12)
					{
						return -11;
					}
					memcpy(m_reader_file,cmd+5,2);
					memcpy(response,"\x00\x0e\x00\x0e\x08\x00\x00\x00\x00\x00\x90\x00",12);
					*lenr=12;
					return 0;
				}
				if(memcmp(data_ptr,"\x7f\x60",2)==0 && m_reader_path_len==2 && memcmp(m_reader_path,"\x3f\x00",2)==0)
				{
					if(*lenr<12)
					{
						return -11;
					}
					memcpy(m_reader_path+2,cmd+5,2);
					m_reader_path_len=4;
					memset(m_reader_file,0,2);
					memcpy(response,"\x00\x05\x00\x05\x88\x00\x00\x00\x00\x00\x90\x00",12);
					*lenr=12;
					return 0;
				}
				if(memcmp(data_ptr,"\x60\x20",2)==0 && m_reader_path_len==4 && memcmp(m_reader_path,"\x3f\x00\x7F\x60",4)==0)
				{
					if(*lenr<12)
					{
						return -11;
					}
					memcpy(m_reader_file,cmd+5,2);
					memcpy(response,"\x00\x10\x00\x10\x08\x00\x00\x00\x00\x00\x90\x00",12);
					*lenr=12;
					return 0;
				}
				if(memcmp(data_ptr,"\x60\x21",2)==0 && m_reader_path_len==4 && memcmp(m_reader_path,"\x3f\x00\x7F\x60",4)==0)
				{
					if(*lenr<12)
					{
						return -11;
					}
					memcpy(m_reader_file,cmd+5,2);
					memcpy(response,"\x00\x10\x00\x10\x08\x00\x00\x00\x00\x00\x90\x00",12);
					*lenr=12;
					return 0;
				}
				if(memcmp(data_ptr,"\x60\x30",2)==0 && m_reader_path_len==4 && memcmp(m_reader_path,"\x3f\x00\x7F\x60",4)==0)
				{
					if(*lenr<12)
					{
						return -11;
					}
					memcpy(m_reader_file,cmd+5,2);
					memcpy(response,"\x00\x04\x00\x04\x08\x00\x00\x00\x00\x00\x90\x00",12);
					*lenr=12;
					return 0;
				}
				if(memcmp(data_ptr,"\x60\x31",2)==0 && m_reader_path_len==4 && memcmp(m_reader_path,"\x3f\x00\x7F\x60",4)==0)
				{
					if(*lenr<12)
					{
						return -11;
					}
					memcpy(m_reader_file,cmd+5,2);
					memcpy(response,"\x00\x03\x00\x03\x08\x00\x00\x00\x00\x00\x90\x00",12);
					*lenr=12;
					return 0;
				}
				if(memcmp(data_ptr,"\x7f\x70",2)==0 && m_reader_path_len==2 && memcmp(m_reader_path,"\x3f\x00",2)==0)
				{
					if(*lenr<12)
					{
						return -11;
					}
					memcpy(m_reader_path+2,cmd+5,2);
					m_reader_path_len=4;
					memset(m_reader_file,0,2);
					memcpy(response,"\x00\x19\x00\x19\x88\x00\x00\x00\x00\x00\x90\x00",12);
					*lenr=12;
					return 0;
				}
				if(memcmp(data_ptr,"\x70\x20",2)==0 && m_reader_path_len==4 && memcmp(m_reader_path,"\x3f\x00\x7F\x70",4)==0)
				{
					if(*lenr<12)
					{
						return -11;
					}
					memcpy(m_reader_file,cmd+5,2);
					memcpy(response,"\x00\x0f\x00\x0f\x08\x00\x00\x00\x00\x00\x90\x00",12);
					*lenr=12;
					return 0;
				}
				if(memcmp(data_ptr,"\x70\x21",2)==0 && m_reader_path_len==4 && memcmp(m_reader_path,"\x3f\x00\x7F\x70",4)==0)
				{
					if(*lenr<12)
					{
						return -11;
					}
					memcpy(m_reader_file,cmd+5,2);
					memcpy(response,"\x00\x10\x00\x10\x08\x00\x00\x00\x00\x00\x90\x00",12);
					*lenr=12;
					return 0;
				}
				response[0]=0x6A;
				response[1]=0x82;
				*lenr=2;
				return 0;
			}
			else if(cmd[1]==0xb0)
			{
				if(m_reader_path_len==2 && memcmp(m_reader_path,"\x3f\x00",2)==0 && memcmp(m_reader_file,"\x00\x20",2)==0)
				{
					if(*lenr<16)
						return -11;
					memcpy(response,"\x01\x05\x43\x4a\x50\x50\x41\x02\01\00\x03\x02\x20\x00\x90\x00",16);
					*lenr=16;
					return 0;
				}

				if(m_reader_path_len==4 && memcmp(m_reader_path,"\x3f\x00\x7f\x60",4)==0 && memcmp(m_reader_file,"\x60\x20",2)==0)
				{
					if(*lenr<18)
						return -11;
					memcpy(response,"\x03\x02\x20\x00\x10\x01\x00\x11\x01\x00\x12\x01\x00\x13\x01\x00\x90\x00",18);
					*lenr=18;
					return 0;
				}

				if(m_reader_path_len==4 && memcmp(m_reader_path,"\x3f\x00\x7f\x60",4)==0 && memcmp(m_reader_file,"\x60\x21",2)==0)
				{
					if(*lenr<18)
						return -11;
					memcpy(response,"\x03\x02\x20\x00\x10\x01\x00\x11\x01\x00\x12\x01\x00\x13\x01\x00\x90\x00",18);
					*lenr=18;
					return 0;
				}

				if(m_reader_path_len==4 && memcmp(m_reader_path,"\x3f\x00\x7f\x60",4)==0 && memcmp(m_reader_file,"\x60\x30",2)==0)
				{
					if(*lenr<6)
						return -11;
					memcpy(response,"\x30\x02\x01\x03\x90\x00",6);
					*lenr=6;
					return 0;
				}
				if(m_reader_path_len==4 && memcmp(m_reader_path,"\x3f\x00\x7f\x60",4)==0 && memcmp(m_reader_file,"\x60\x31",2)==0)
				{
					if(*lenr<5)
						return -11;
					memcpy(response,"\x30\x01\xff\x90\x00",5);
					*lenr=5;
					return 0;
				}
				if(m_reader_path_len==4 && memcmp(m_reader_path,"\x3f\x00\x7f\x70",4)==0 && memcmp(m_reader_file,"\x70\x20",2)==0)
				{
					if(*lenr<17)
						return -11;
					memcpy(response,"\x03\x02\x20\x00\x20\x01\x81\x22\x06\01\x02\x03\x80\x81\x82\x90\x00",17);
					*lenr=17;
					return 0;
				}
				if(m_reader_path_len==4 && memcmp(m_reader_path,"\x3f\x00\x7f\x70",4)==0 && memcmp(m_reader_file,"\x70\x21",2)==0)
				{
					if(*lenr<18)
						return -11;
					memcpy(response,"\x03\x02\x20\x00\x21\x02\x01\x89\x22\x06\01\x02\x03\x80\x81\x82\x90\x00",17);
					*lenr=18;
					return 0;
				}
				response[0]=0x69;
				response[1]=0x82;
				*lenr=2;
				return 0;
			}
			else
			{
				response[0]=0x6D;
				response[1]=0x00;
				*lenr=2;
				return 0;
			}
		}
		else if(cmd[0]==0x20)
		{


			if(cmd[1]==0x10)  /*B1*/
			{
				if(cmd[2]>1 || cmd[2]==0 && cmd[3]!=0 || cmd[2]==1 && cmd[3]>2)
				{
					response[0]=0x6A;
					response[1]=0x00;
					*lenr=2;
					return 0;
				}
				/* FIX 2007/11/29: Seems like this expression is incorrect, at least it deviates from "0x20, 0x11" */
				/*if(cmd[2]==1 && cmd[3]>0 || Lc!=-1 || Le!=0)*/
				if(cmd[2]==1 && cmd[3]>0 && (Lc!=-1 || Le!=0))
				{
					response[0]=0x67;
					response[1]=0x00;
					*lenr=2;
					return 0;
				}
				IfdPower(SCARD_POWER_DOWN,0,0);

				if(cmd[2]==0x01)
				{
					uint8_t atr[250];
					uint8_t historical[15];
					uint8_t hist_len=sizeof(historical);
					uint8_t atr_len=(uint8_t)sizeof(atr);
					switch(ctBcsReset(atr,&atr_len,historical,&hist_len,0))
					{
					case CJ_SUCCESS:
						if(cmd[3]==0)
						{
							if(atr[0]==0x3b || atr[0]==0x3f)
							{
								response[0]=0x90;
								response[1]=0x01;
							}
							else
							{
								response[0]=0x90;
								response[1]=0x00;
							}
							*lenr=2;
						}
						else if(cmd[3]==1)
						{
							if(atr_len+2>*lenr)
							{
								IfdPower(SCARD_POWER_DOWN,0,0);
								return -11;
							}
							else
							{
								memcpy(response,atr,atr_len);
								if(atr[0]==0x3b || atr[0]==0x3f)
								{
									response[atr_len]=0x90;
									response[atr_len+1]=0x01;
								}
								else
								{
									response[atr_len]=0x90;
									response[atr_len+1]=0x00;
								}
								*lenr=(uint16_t)(atr_len+2);
							}
						}
						else
						{
							if(hist_len+2>*lenr)
							{
								IfdPower(SCARD_POWER_DOWN,0,0);
								*lenr=0;
								return -11;
							}
							else
							{
								memcpy(response,historical,hist_len);
								if(atr[0]==0x3b || atr[0]==0x3f)
								{
									response[hist_len]=0x90;
									response[hist_len+1]=0x01;
								}
								else
								{
									response[hist_len]=0x90;
									response[hist_len+1]=0x00;
								}
								*lenr=(uint16_t)(hist_len+2);
							}
						}
						return 0;
					case CJ_ERR_PARITY:
					case CJ_ERR_TIMEOUT:
					case CJ_ERR_PROT:

						response[0]=0x64;
						response[1]=0x00;
						*lenr=2;
						return 0;
					case CJ_ERR_NO_ICC:
						response[0]=0x64;
						response[1]=0xA1;
						*lenr=2;
						return 0;
					default:
						IfdPower(SCARD_POWER_DOWN,0,0);
						return -128;
					}
				}
				else
				{
					response[0]=0x90;
					response[1]=0x00;
					*lenr=2;
					return 0;
				}
			}
			else if(cmd[1]==0x11)
			{
				if(cmd[2]>1 || cmd[2]==0 && cmd[3]!=0 || cmd[2]==1 && cmd[3]>2)
				{
					response[0]=0x6A;
					response[1]=0x00;
					*lenr=2;
					return 0;
				}
				if(cmd[2]==1 && cmd[3]>0 && (Lc!=-1 || Le!=0))
				{
					response[0]=0x67;
					response[1]=0x00;
					*lenr=2;
					return 0;
				}
				IfdPower(SCARD_POWER_DOWN,0,0);
				//IfdPower(SCARD_POWER_DOWN,0,0);
//				Sleep(3000);

				if(cmd[2]==0x01)
				{
					uint8_t atr[250];
					uint8_t historical[15];
					uint8_t hist_len=sizeof(historical);
					uint8_t atr_len=(uint8_t)sizeof(atr);
					switch(ctBcsReset(atr,&atr_len,historical,&hist_len,0))
					{
					case CJ_SUCCESS:
						if(cmd[3]==0)
						{
							if(atr[0]==0x3b || atr[0]==0x3f)
							{
								response[0]=0x90;
								response[1]=0x01;
							}
							else
							{
								response[0]=0x90;
								response[1]=0x00;
							}
							*lenr=2;
						}
						else if(cmd[3]==1)
						{
							if(atr_len+2>*lenr)
							{
								IfdPower(SCARD_POWER_DOWN,0,0);
								*lenr=0;
								return -11;
							}
							else
							{
								memcpy(response,atr,atr_len);
								if(atr[0]==0x3b || atr[0]==0x3f)
								{
									response[atr_len]=0x90;
									response[atr_len+1]=0x01;
								}
								else
								{
									response[atr_len]=0x90;
									response[atr_len+1]=0x00;
								}
								*lenr=(uint16_t)(atr_len+2);
							}
						}
						else
						{
							if(hist_len+2>*lenr)
							{
								IfdPower(SCARD_POWER_DOWN,0,0);
								*lenr=0;
								return -11;
							}
							else
							{
								memcpy(response,historical,hist_len);
								if(atr[0]==0x3b || atr[0]==0x3f)
								{
									response[hist_len]=0x90;
									response[hist_len+1]=0x01;
								}
								else
								{
									response[hist_len]=0x90;
									response[hist_len+1]=0x00;
								}
								*lenr=(uint16_t)(hist_len+2);
							}
						}
						return 0;
					case CJ_ERR_PARITY:
					case CJ_ERR_TIMEOUT:
					case CJ_ERR_PROT:

						response[0]=0x64;
						response[1]=0x00;
						*lenr=2;
						return 0;
					case CJ_ERR_NO_ICC:
						response[0]=0x64;
						response[1]=0xA1;
						*lenr=2;
						return 0;
					default:
						IfdPower(SCARD_POWER_DOWN,0,0);
						return -128;
					}
				}
				else
				{
					response[0]=0x90;
					response[1]=0x00;
					*lenr=2;
					return 0;
				}
			}

			else if(cmd[1]==0x12)
			{
				uint32_t State;
				int Timeout=0;
				cmd[3]&=0x0f;
				if(cmd[2]!=1 || cmd[3]>2)
				{
					response[0]=0x6A;
					response[1]=0x00;
					*lenr=2;
					return 0;
				}
				if(cmd[3]>0 && (Le!=0))
				{
					response[0]=0x67;
					response[1]=0x00;
					*lenr=2;
					return 0;
				}
				if(IfdGetState(&State)==STATUS_DEVICE_NOT_CONNECTED)
				{
					*lenr=0;
					return -128;
				}
				if(State==SCARD_SPECIFIC)
				{
					response[0]=0x62;
					response[1]=0x01;
					*lenr=2;
					return 0;
				}
				if(Lc!=-1)
				{
					uint8_t *tag80;
					int taglen;
					if((tag80=GetTag(data_ptr,Lc,0x80,&taglen))!=NULL || Lc==1)
					{
						if(Lc==1)
							Timeout=data_ptr[0];
						else
						{
							if(taglen!=1)
							{
								response[0]=0x67;
								response[1]=0x00;
								*lenr=2;
								return 0;
							}
							Timeout=*tag80;
						}

					}
				}
				{
					uint8_t atr[250];
					uint8_t historical[15];
					uint8_t hist_len=sizeof(historical);
					uint8_t atr_len=(uint8_t)sizeof(atr);
					Timeout*=1000;
					switch(ctBcsReset(atr,&atr_len,historical,&hist_len,0,Timeout))
					{
					case CJ_SUCCESS:
						if(cmd[3]==0)
						{
							if(atr[0]==0x3b || atr[0]==0x3f)
							{
								response[0]=0x90;
								response[1]=0x01;
							}
							else
							{
								response[0]=0x90;
								response[1]=0x00;
							}
							*lenr=2;
						}
						else if(cmd[3]==1)
						{
							if(atr_len+2>*lenr)
							{
								IfdPower(SCARD_POWER_DOWN,0,0);
								return -11;
							}
							else
							{
								memcpy(response,atr,atr_len);
								if(atr[0]==0x3b || atr[0]==0x3f)
								{
									response[atr_len]=0x90;
									response[atr_len+1]=0x01;
								}
								else
								{
									response[atr_len]=0x90;
									response[atr_len+1]=0x00;
								}
								*lenr=(uint16_t)(atr_len+2);
							}
						}
						else
						{
							if(hist_len+2>*lenr)
							{
								IfdPower(SCARD_POWER_DOWN,0,0);
								return -11;
							}
							else
							{
								memcpy(response,historical,hist_len);
								if(atr[0]==0x3b || atr[0]==0x3f)
								{
									response[hist_len]=0x90;
									response[hist_len+1]=0x01;
								}
								else
								{
									response[hist_len]=0x90;
									response[hist_len+1]=0x00;
								}
								*lenr=(uint16_t)(hist_len+2);
							}
						}
						return 0;
					case CJ_ERR_PARITY:
					case CJ_ERR_TIMEOUT:
					case CJ_ERR_PROT:

						response[0]=0x64;
						response[1]=0x00;
						*lenr=2;
						return 0;
					case CJ_ERR_PIN_CANCELED:
						response[0]=0x64;
						response[1]=0x01;
						*lenr=2;
						return 0;
					case CJ_ERR_NO_ICC:
						response[0]=0x62;
						response[1]=0x00;
						*lenr=2;
						return 0;
					default:
						IfdPower(SCARD_POWER_DOWN,0,0);
						*lenr=0;
						return -128;
					}
				}
			}
			else if(cmd[1]==0x13)
			{
				if(lenc==4 && cmd[2]==0x01 && cmd[3]==0x00)
				{
					if(*lenr<8)
					{
						*lenr=0;
						return -11;
					}
					response[0] = 0x71;
					response[1] = 0x01;
					response[2] = 0x00;
					response[3] = 0x72;
					response[4] = 0x01;
					switch(m_ActiveProtocol)
					{
					case SCARD_PROTOCOL_T0:
						response[5]=0;
						break;
					case SCARD_PROTOCOL_T1:
						response[5]=1;
						break;
					default:
						response[5]=0;
					}
					response[6] = 0x90;
					response[7] = 0x00;
					*lenr=8;
					return 0;
				}
				else if(Lc!=-1 || Le!=0)
				{
					response[0]=0x67;
					response[1]=0x00;
					*lenr=2;
					return 0;
				}
				if(cmd[2]>1 || (cmd[2]==0 && cmd[3]!=0x46 && cmd[3]!=0x80 && cmd[3]!=0x81) ||
					(cmd[2]==1 && cmd[3]!=0x80))
				{
					response[0]=0x6A;
					response[1]=0x00;
					*lenr=2;
					return 0;
				}
				if(cmd[3]==0x80)
				{
					uint32_t State;
					if(*lenr<5)
					{
						*lenr=0;
						return -11;
					}
					response[0]=0x80;
					response[1]=1;
					if(IfdGetState(&State)==STATUS_DEVICE_NOT_CONNECTED)
					{
						*lenr=0;
						return -128;
					}
					switch(State)
					{
					case SCARD_SPECIFIC:
					case SCARD_NEGOTIABLE:
						response[2]=0x05;
						break;
					case SCARD_SWALLOWED:
						response[2]=0x03;
						break;
					case SCARD_ABSENT:
						response[2]=0x00;
						break;
					default:
						*lenr=0;
						return -128;
					}
					response[3]=0x90;
					response[4]=0x00;
					*lenr=5;
				}
				else if(cmd[3]==0x81)
				{
					uint8_t r[15];
					int len=0;
					r[0]=0x81;
					if(m_ReaderInfo.HardwareMask & RSCT_READER_HARDWARE_MASK_ICC1)
					{
						len++;
						r[len+1]=1;
					}
					if(m_ReaderInfo.HardwareMask & RSCT_READER_HARDWARE_MASK_ICC2)
					{
						len++;
						r[len+1]=2;
					}
					if(m_ReaderInfo.HardwareMask & RSCT_READER_HARDWARE_MASK_ICC3)
					{
						len++;
						r[len+1]=3;
					}
					if(m_ReaderInfo.HardwareMask & RSCT_READER_HARDWARE_MASK_ICC4)
					{
						len++;
						r[len+1]=4;
					}
					if(m_ReaderInfo.HardwareMask & RSCT_READER_HARDWARE_MASK_ICC5)
					{
						len++;
						r[len+1]=5;
					}
					if(m_ReaderInfo.HardwareMask & RSCT_READER_HARDWARE_MASK_ICC6)
					{
						len++;
						r[len+1]=6;
					}
					if(m_ReaderInfo.HardwareMask & RSCT_READER_HARDWARE_MASK_ICC7)
					{
						len++;
						r[len+1]=7;
					}
					if(m_ReaderInfo.HardwareMask & RSCT_READER_HARDWARE_MASK_ICC8)
					{
						len++;
						r[len+1]=8;
					}
				   if(m_ReaderInfo.HardwareMask & RSCT_READER_HARDWARE_MASK_KEYPAD)
					{
						len++;
						r[len+1]=0x40;
					}
					if(m_ReaderInfo.HardwareMask & RSCT_READER_HARDWARE_MASK_KEYPAD)
					{
						len++;
						r[len+1]=0x50;
					}
					if(m_ReaderInfo.HardwareMask & RSCT_READER_HARDWARE_MASK_BIOMETRIC)
					{
						len++;
						r[len+1]=0x70;
					}
					r[1]=(uint8_t )len;
					r[len+2]=0x90;
					r[len+3]=0x00;
					if(*lenr<len+4)
					{
						*lenr=0;
						return -11;
					}
					else
					{
						memcpy(response,r,len+4);
						*lenr=len+4;
					}
				}
				else
				{
					if(*lenr<19)
					{
						*lenr=0;
						return -11;
					}
					response[0]=0x46;
					memcpy(response+2,"DESCT",5);
					GetProductString(response+7);
					cj_ModuleInfo *Info=FindModule(MODULE_ID_KERNEL);
					if(IsClass3() && Info==NULL)
					{
						*lenr=0;
						return -128;
					}
					if(GetEnviroment("ecom_a_ident",0)==0)
						sprintf((char *)(response+12)," V%1d.%1d",(int)(m_ReaderInfo.Version>>4),(int)(m_ReaderInfo.Version & 0x0f));
					else
						sprintf((char *)(response+12)," V%1d.%1d",(int)(2),(int)(0));
					if(IsClass3() && *lenr>=29)
					{
						memcpy(response+17,"; Rev. ",7);
						sprintf((char *)(response+24),"%3d",(int)Info->Revision);
						response[1]=25;
						response[27]=0x90;
						response[28]=0x00;
						*lenr=29;
					}
					else
					{
						response[1]=15;
						response[17]=0x90;
						response[18]=0x00;
						*lenr=19;
					}
				}
				return 0;
			}
			else if(cmd[1]==0x14)
			{
				if(Lc!=-1 || Le!=0)
				{
					response[0]=0x6A;
					response[1]=0x00;
					*lenr=2;
					return 0;
				}
				if(cmd[2]!=1 || cmd[3]!=0)
				{
					response[0]=0x6A;
					response[1]=0x00;
					*lenr=2;
					return 0;
				}

				if(IfdPower(SCARD_POWER_DOWN,0,0)!=STATUS_SUCCESS)
					return -10;
				if(m_ReaderState!=SCARD_ABSENT)
				{
					response[0]=0x90;
					response[1]=0x00;
				}
				else
				{
					response[0]=0x64;
					response[1]=0xA1;
				}
				*lenr=2;
				return 0;
			}
			else if(cmd[1]==0x15)
			{
				if(cmd[2]!=1)
				{
					response[0]=0x6A;
					response[1]=0x00;
					*lenr=2;
					return 0;
				}
				if(lenc!=4 && (Lc==-1 || Le!=-1))
				{
					response[0]=0x67;
					response[1]=0x00;
					*lenr=2;
					return 0;
				}

				if(IfdPower(SCARD_POWER_DOWN,0,0)!=STATUS_SUCCESS)
					return -10;
				if((cmd[3]&0x04)!=0x04 && lenc>4)
				{
					uint8_t *tag80;
					int taglen;
					int Timeout=0;
					uint32_t State;
					if((tag80=GetTag(data_ptr,Lc,0x80,&taglen))!=NULL || Lc==1)
					{
						if(Lc==1)
							Timeout=data_ptr[0];
						else
						{
							if(taglen!=1)
							{
								response[0]=0x67;
								response[1]=0x00;
								*lenr=2;
								return 0;
							}
							Timeout=*tag80;
						}
						Timeout*=1000;

						if(IfdPower(SCARD_POWER_DOWN,0,0,Timeout)!=STATUS_SUCCESS)
							return -10;

						if(IfdGetState(&State)==STATUS_DEVICE_NOT_CONNECTED)
							return -10;

						if(State==SCARD_ABSENT)
						{
							response[0]=0x90;
							response[1]=0x01;
						}
						else
						{
							response[0]=0x62;
							response[1]=0x00;
						}
					}
					else
					{
						response[0]=0x90;
						response[1]=0x00;
					}
				}
				else
				{
					response[0]=0x90;
					response[1]=0x00;
				}
				*lenr=2;
				return 0;
			}
   		else if(cmd[1]==0x16 && IsClass2() && (!IsClass3() || FindModule(MODULE_ID_MKT_COMP)))
			{
				int charlen;
				uint8_t timeout=15;
				uint8_t *tag80;
				uint8_t *tag50;
				int taglen;
				int taglen50;
				int i,j;
				uint8_t buffer[256];
				uint8_t text[256];
				uint8_t key;
				fctKeyIntCallback merkcallback;
				ctxPtr Context;

				if(Le==-1)
				{
					response[0]=0x67;
					response[1]=0x00;
					*lenr=2;
					return 0;
				}
				if(cmd[2]!=0x50 || (cmd[3]&0x0f)>0x02 || (cmd[3]&0xf0)>0x10 || (cmd[3]&0xf0)==0x10 && Le!=01)
				{
					response[0]=0x6A;
					response[1]=0x00;
					*lenr=2;
					return 0;
				}
				merkcallback=m_KeyIntCallback;
				Context=m_KeyCallbackCtx;
				SetKeyInterruptCallback(NULL,NULL);
				if(Lc!=-1 && ((tag80=GetTag(data_ptr,Lc,0x80,&taglen))!=NULL || Lc==1))
				{
					if(Lc==1)
						timeout=data_ptr[0];
					else
					{
						if(taglen!=1)
						{
							response[0]=0x67;
							response[1]=0x00;
							*lenr=2;
      					SetKeyInterruptCallback(merkcallback,Context);
							return 0;
						}
						timeout=*tag80;
					}
				}
				charlen=Le;
				if(charlen==0)
					charlen=256;
				tag50=GetTag(data_ptr,Lc,0x50,&taglen50);
				if(tag50)
				{
					TransformText(tag50,taglen50);
				}
				for(i=0;i<charlen;i++)
				{
                                        j=0;
					if((cmd[3] & 0x0f)==00)
					{
						j=0;
						memcpy(text,tag50,taglen50);
					}
					else if((cmd[3] & 0x0f)==01)
					{
						j=i;
						memcpy(text,tag50,taglen50);
						memcpy(text+taglen50,buffer,i);
					}
					else if((cmd[3] & 0x0f)==02)
					{
						j=i;
						memcpy(text,tag50,taglen50);
						memset(text+taglen50,'*',i);
					}
					if(cjInput(&key,timeout,text,taglen50+j)!=CJ_SUCCESS)
					{
						*lenr=0;
						SetKeyInterruptCallback(merkcallback,Context);
						return -128;
					}
					if(key==0xff)
					{
						response[0]=0x64;
						response[1]=0x00;
						*lenr=2;
   					SetKeyInterruptCallback(merkcallback,Context);
						return 0;
					}
					timeout=5;
					if((cmd[3]&0xf0)==0x10)
					{
						if(key!=10 && key!=11 || charlen==1)
						{
							buffer[i]=key;
						}
						else if(key==10)
						{
							response[0]=0x64;
							response[1]=0x01;
							*lenr=2;
							SetKeyInterruptCallback(merkcallback,Context);
							return 0;
						}
						else if(key==11)
						{
							break;
						}
					}
					else
					{
						if(key<10)
						{
							key+=(uint8_t)'0';
							buffer[i]=key;
						}
						else if(key==10)
						{
							response[0]=0x64;
							response[1]=0x01;
							*lenr=2;
							SetKeyInterruptCallback(merkcallback,Context);
							return 0;
						}
						else if(key==11)
						{
							break;
						}
						else
						{
							i--;
						}
					}
				}
				if(*lenr<i+2)
				{
					*lenr=0;
					SetKeyInterruptCallback(merkcallback,Context);
					return -11;
				}
				memcpy(response,buffer,i);
				response[i]=0x90;
				response[i+1]=0x00;
				*lenr=i+2;
				SetKeyInterruptCallback(merkcallback,Context);
				return 0;
			}
			else if(cmd[1]==0x17 && IsClass3() && FindModule(MODULE_ID_MKT_COMP))
			{
				uint8_t *tag80;
				uint8_t *tag50;
				int taglen;
				int taglen50;
				uint8_t timeout=0;

				if(Lc==-1 || Le!=-1)
				{
					response[0]=0x67;
					response[1]=0x00;
					*lenr=2;
					return 0;
				}

				if(cmd[2]!=0x40 || cmd[3]!=0)
				{
					response[0]=0x6A;
					response[1]=0x00;
					*lenr=2;
					return 0;
				}
				tag80=GetTag(data_ptr,Lc,0x80,&taglen);
				if(tag80)
				{
					if(taglen!=1)
					{
						response[0]=0x6A;
						response[1]=0x82;
						*lenr=2;
						return 0;
					}
					timeout=*tag80;
				}
				tag50=GetTag(data_ptr,Lc,0x50,&taglen50);
				if(!tag50 || taglen50==0)
				{
					response[0]=0x6A;
					response[1]=0x82;
					*lenr=2;
					return 0;
				}
				TransformText(tag50,taglen50);
				switch(cjOutput(timeout,tag50,taglen50))
				{
				case CJ_SUCCESS:
					response[0]=0x90;
					response[1]=0x00;
					break;
				default:
					response[0]=0x69;
					response[1]=0x85;
				}
				*lenr=2;
   			return 0;

			}
			else if(cmd[1]==0x18  && IsClass2())
			{
				uint8_t timeout=15;
				uint8_t *tag50[3];
				int taglen50[3];
				uint8_t *tag53;
				int taglen53;
   			uint8_t bMessageIndex=0;
				uint8_t bMessageArray[3];
				uint8_t btaglen50[3];
				uint8_t bNumberMessage;
				uint8_t *tag54;
				int taglen54;
				int TextCount=0;
				tag50[0]=NULL;
				taglen50[0]=0;

				memset(bMessageArray,0,3);



				if(CheckUpdate())
				{
//					MessageBox(NULL,"Bitte Leser updaten.\nPlease update your reader.","Warnung - Warning",MB_ICONSTOP | MB_OK);
					response[0]=0x6A;
					response[1]=0x03;
					*lenr=2;
					return 0;
				}
				if(Lc<8 || Le!=-1)
				{
					response[0]=0x67;
					response[1]=0x00;
					*lenr=2;
					return 0;
				}
				else
				{
					uint8_t *tag80;
					uint8_t *tag51;
					int taglen;
					if((tag80=GetTag(data_ptr,Lc,0x80,&taglen))!=NULL)
					{
						if(taglen!=1)
						{
							response[0]=0x67;
							response[1]=0x00;
							*lenr=2;
							return 0;
						}
						timeout=*tag80;
					}
					if((tag80=GetTag(data_ptr,Lc,0x52,&taglen))!=NULL)
					{
						uint8_t PinLength;
						uint8_t Min;
						uint8_t Max;
						uint8_t PinType;
						uint8_t Condition;
						uint8_t PinPosition;
						uint8_t PinLengthPosition=0;
						uint8_t PinLengthSize=0;
						int outlen;
						int inlen;
						int use_MV=0;
						uint8_t buffer[260];
						uint8_t rbuffer[260];
						uint8_t Prologue[3];
						int Res;
						if(taglen<6)
						{
							response[0]=0x67;
							response[1]=0x00;
							*lenr=2;
							return 0;
						}
						memcpy(buffer,tag80+2,taglen-2);
						PinLength=tag80[0]>>4;
						if(tag80[1]>5)
							PinPosition=tag80[1]-6;
						else
						{
							response[0]=0x67;
							response[1]=0x00;
							*lenr=2;
							return 0;
						}
						if(taglen==6)
						{
							buffer[4]=0;
							taglen++;
						}
						else
						{
							if(PinPosition>taglen-7)
							{
								response[0]=0x67;
								response[1]=0x00;
								*lenr=2;
								return 0;
							}
						}
						switch(tag80[0] & 3)
						{

						case 0:
							PinType=1;
							if(taglen==7)
							{
								if(PinLength==0)
								{
									Min=1;
									Max=30;
									Condition=2;
									PinLength=15;
									buffer[4]=0;
									outlen=5;
								}
								else
								{
									Min=PinLength;
									Max=PinLength;
									PinLength=PinLength/2+(PinLength & 1);
									Condition=1;
									//memset(buffer+5,0xff,PinLength);
									outlen=5;
									buffer[4]=0;
								}
							}
							else
							{
								if(PinLength==0)
								{
									Min=1;
									Max=30;
									Condition=2;
									PinLength=15;
									outlen=(taglen-2);
									buffer[4]=0;
								}
								else
								{
/*									if(PinLength/2+(PinLength & 1)+PinPosition>taglen-7)
									{
										response[0]=0x67;
										response[1]=0x00;
										*lenr=2;
										return 0;
									}*/
									Min=PinLength;
									Max=PinLength;
									PinLength=PinLength/2+(PinLength & 1);
									Condition=1;
									buffer[4]=0;
									outlen=taglen-2;
								}
							}
							break;
						case 1:
							PinType=2;
							if(taglen==7)
							{
								if(PinLength==0)
								{
									Min=1;
									Max=15;
									Condition=2;
									PinLength=15;
									outlen=5;
								}
								else
								{
									Min=PinLength;
									Max=PinLength;
									Condition=1;
									outlen=5;
								}
							}
							else
							{
								if(PinLength==0)
								{
									Min=1;
									Max=15;
									Condition=2;
									PinLength=15;
									buffer[4]=0;
									outlen=(taglen-2);
								}
								else
								{
									Min=PinLength;
									Max=PinLength;
									Condition=1;
									buffer[4]=0;
									outlen=taglen-2;
								}
							}
							break;
						case 2:
							PinType=1;
							PinLengthPosition=4;
							PinLengthSize=4;
							PinPosition++;
							if(PinPosition>1)
							{
								use_MV=1;
							}
							if(buffer[4]<PinPosition+7)
								buffer[4]=PinPosition+7;
							buffer[4+PinPosition]=0x2f;
							memset(buffer+5+PinPosition,0xff,7);
							outlen=buffer[4]+5;
							if(PinLength==0)
							{
								Min=4;
								Max=12;
								Condition=2;
								PinLength=7;
							}
							else
							{
								Min=PinLength;
								Max=PinLength;
								if(PinLength<4 || PinLength>12)
								{
									response[0]=0x67;
									response[1]=0x00;
									*lenr=2;
									return 0;
								}
								PinLength=7;
								Condition=1;
							}
							break;
						default:
							response[0]=0x6A;
							response[1]=0x00;
							*lenr=2;
							return 0;
						}
						Prologue[0]=0;
/*						if(((cjccidHANDLE)hDevice)->PCB_seq&0x01)
							Prologue[1]=0x40;
						else
							Prologue[1]=0;
						Prologue[2]=outlen;*/
						inlen=sizeof(rbuffer);
						if((tag51=GetTag(data_ptr,Lc,0x51,&taglen))!=NULL)
						{
							if(taglen==2)
							{
								if(tag51[0]>=4 || (tag80[0] & 3)!=2)
									Min=tag51[0];
								if(tag51[1]>=Min)
									Max=tag51[1];
								if(Max>12 && (tag80[0] & 3)==2)
								{
									Max=12;
								}
							}
						}
						tag50[0]=GetTag(data_ptr,Lc,0x50,&(taglen50[0]));
						if(tag50[0])
						{
							TextCount=3;
							TransformText(tag50[0],taglen50[0]);
							btaglen50[2]=btaglen50[1]=btaglen50[0]=(uint8_t)taglen50[0];
							tag50[2]=tag50[1]=tag50[0];
						}


						tag53=GetTag(data_ptr,Lc,0x53,&taglen53);
						if(tag53)
						{
							if(taglen53==1)
							{
								bMessageIndex=tag53[0];
								bMessageArray[1]=tag53[0];
							}
						}

						if(use_MV)
							bNumberMessage=0xff;
						else
							bNumberMessage=0x01;
						tag54=GetTag(data_ptr,Lc,0x54,&taglen54);
						if(tag54)
						{
							if(taglen54==1)
							{
								bNumberMessage=tag54[0];
							}
						}
#ifdef IT_TEST
						switch(Res=cjccid_SecurePV(timeout,PinPosition,PinType,
							PinLengthSize,PinLength,
							PinLengthPosition,
							Min,Max,
							Condition,Prologue,
							buffer,outlen,rbuffer,&inlen,cmd[2]-1,tag50[0],taglen50[0],bMessageIndex,bNumberMessage))
#else
						switch(Res=((use_MV==0)?cjccid_SecurePV(timeout,PinPosition,PinType,
							PinLengthSize,PinLength,
							PinLengthPosition,
							Min,Max,
							Condition,Prologue,
							buffer,outlen,rbuffer,&inlen,tag50[0],taglen50[0],bMessageIndex,bNumberMessage):
						    cjccid_SecureMV(timeout,1,PinType,
							PinLengthSize,PinLength,
							PinLengthPosition,
							Min,Max,
							0,
							Condition,Prologue,
							0,PinPosition-1,
							buffer,outlen,rbuffer,&inlen,TextCount,tag50,btaglen50,bMessageArray,bNumberMessage)))
#endif
						{
						case CJ_SUCCESS:
						case CJ_ERR_PIN_EXTENDED:
							if(*lenr<inlen)
							{
								*lenr=0;
								return -11;
							}
							memcpy(response,rbuffer,inlen);
							*lenr=inlen;
							if(Res==CJ_SUCCESS)
							   *sad=0;
							return 0;
						case CJ_ERR_PIN_CANCELED:
							response[0]=0x64;
							response[1]=0x01;
							*lenr=2;
							return 0;
						case CJ_ERR_PIN_TIMEOUT:
							response[0]=0x64;
							response[1]=0x00;
							*lenr=2;
							return 0;
						case CJ_ERR_NO_ICC:
							response[0]=0x64;
							response[1]=0xA1;
							*lenr=2;
							return 0;
						case CJ_ERR_NO_ACTIVE_ICC:
							response[0]=0x64;
							response[1]=0xA2;
							*lenr=2;
							return 0;
						case CJ_ERR_WRONG_PARAMETER:
							response[0]=0x6B;
							response[1]=0x80;
							*lenr=2;
							return 0;
						case CJ_ERR_CONDITION_OF_USE:
							response[0]=0x69;
							response[1]=0x85;
							*lenr=2;
							return 0;
						case CJ_ERR_WRITE_DEVICE:
						case CJ_ERR_DEVICE_LOST:
							*lenr=0;
							return -128;

						case CJ_ERR_RBUFFER_TO_SMALL:
							*lenr=0;
							return -11;
						case CJ_ERR_PARITY:
						case CJ_ERR_TIMEOUT:
							response[0]=0x6f;
							response[1]=0x00;
							*lenr=2;
							return 0;
						default:
							response[0]=0x6b;
							response[1]=0x80;
							*lenr=2;
							return 0;
						}
					}
					else
					{
						response[0]=0x67;
						response[1]=0x00;
						*lenr=2;
						return 0;
					}
				}
			}
			else if(cmd[1]==0x19 && IsClass2())
			{
				uint8_t timeout=15;
				uint8_t *tag50[3];
				int taglen50[3];
				uint8_t btaglen50[3];
				int TextCount=0;
				uint8_t *cmd_ptr;
				int cmd_len;
				uint8_t *tag53;
				int taglen53;
   			uint8_t bMessageIndex[3];
				uint8_t bNumberMessage;
				uint8_t *tag54;
				int taglen54;

				memset(bMessageIndex,0,3);

				if(cmd[2]!=1 || cmd[3]!=0)
				{
					response[0]=0x6A;
					response[1]=0x00;
					*lenr=2;
					return 0;
				}
				if(CheckUpdate())
				{
//					MessageBox(NULL,"Bitte Leser updaten.\nPlease update your reader.","Warnung - Warning",MB_ICONSTOP | MB_OK);
					response[0]=0x6A;
					response[1]=0x03;
					*lenr=2;
					return 0;
				}
				if(Lc<9 || Le!=-1)
				{
					response[0]=0x67;
					response[1]=0x00;
					*lenr=2;
					return 0;
				}
				else
				{
					uint8_t *tag80;
					uint8_t *tag51;
					int taglen;
					if((tag80=GetTag(data_ptr,Lc,0x80,&taglen))!=NULL)
					{
						if(taglen!=1)
						{
							response[0]=0x67;
							response[1]=0x00;
							*lenr=2;
							return 0;
						}
						timeout=*tag80;
					}
					if((tag80=GetTag(data_ptr,Lc,0x52,&taglen))!=NULL)
					{
						uint8_t PinLength;
						uint8_t Min;
						uint8_t Max;
						uint8_t PinType;
						uint8_t Condition;
						uint8_t PinPositionOld;
						uint8_t PinPositionNew;
						uint8_t PinPosition=0;
						uint8_t PinLengthPosition=0;
						uint8_t PinLengthSize=0;
						int outlen;
						int inlen;
						uint8_t buffer[260];
						uint8_t rbuffer[260];
						uint8_t Prologue[3];
						uint8_t bConfirmPIN;
						int Res;
						if(taglen<7)
						{
							response[0]=0x67;
							response[1]=0x00;
							*lenr=2;
							return 0;
						}
						memcpy(buffer,tag80+3,taglen-3);
						PinLength=tag80[0]>>4;
						if(tag80[1]>5 && tag80[1]!=tag80[2])
						{
							PinPositionOld=tag80[1]-6;
							bConfirmPIN=2;
						}
						else if(tag80[2]>5 && tag80[1]==tag80[2])
						{
							PinPositionOld=0;
							bConfirmPIN=0;
						}
						else
						{
							response[0]=0x67;
							response[1]=0x00;
							*lenr=2;
							return 0;
						}
						if(tag80[2]>5)
						{
							PinPositionNew=tag80[2]-6;
						}
						else if(tag80[2]==0)
						{
							if((tag80[0] & 3)==2 && tag80[1]!=0)
								PinPositionNew=PinPositionOld+8;
							else
								PinPositionNew=0;
						}
						else
						{
							response[0]=0x67;
							response[1]=0x00;
							*lenr=2;
							return 0;
						}
						if(taglen==7)
						{
							buffer[4]=0;
							taglen++;
						}
						switch(tag80[0] & 3)
						{
						case 0:
							PinType=1;
							if(taglen==8)
							{
								if(PinLength==0)
								{
									Min=1;
									if(tag80[1]>5 && tag80[1]!=tag80[2])
									{
										Max=2*abs(PinPositionNew-PinPositionOld);
										if(Max>30)
											Max=30;
									}
									else
									   Max=30;
									if(Max==0)
									   Max=30;
									Condition=2;
									PinLength=(Max>>1)+(Max&1);
									buffer[4]=0;
									outlen=5;
								}
								else
								{
									Min=PinLength;
									Max=PinLength;
									PinLength=PinLength/2+(PinLength & 1);
									Condition=1;
									buffer[4]=0;
									outlen=5;
								}
							}
							else
							{
								if(PinLength==0)
								{
									Min=1;
									if(tag80[1]>5 && tag80[1]!=tag80[2])
									{
										Max=2*abs(PinPositionNew-PinPositionOld);

										if((taglen-8-max(PinPositionNew,PinPositionOld))*2>Max)
											Max=(taglen-8-max(PinPositionNew,PinPositionOld))*2;
										if(Max>30)
											Max=30;
									}
									else
									   Max=30;
									if(Max==0)
									   Max=30;
									PinLength=(Max>>1)+(Max&1);
									buffer[4]=0;
									Condition=2;
									outlen=(taglen-3);
								}
								else
								{
									Min=PinLength;
									Max=PinLength;
									PinLength=PinLength/2+(PinLength & 1);
									Condition=1;
									outlen=taglen-3;
									buffer[4]=0;
								}
							}
							break;
						case 1:
							PinType=2;
							if(taglen==8)
							{
								if(PinLength==0)
								{
									Min=1;
									if(tag80[1]>5 && tag80[1]!=tag80[2])
									{
										Max=abs(PinPositionNew-PinPositionOld);
										if(Max>15)
											Max=15;
									}
									else
									   Max=15;
									if(Max==0)
									   Max=15;
									PinLength=Max;
									Condition=2;
									buffer[4]=0;
									outlen=5;
								}
								else
								{
									Min=PinLength;
									Max=PinLength;
									Condition=1;
									buffer[4]=0;
									outlen=5;
								}
							}
							else
							{
								if(PinLength==0)
								{
									Min=1;
									if(tag80[1]>5 && tag80[1]!=tag80[2])
									{
										Max=abs(PinPositionNew-PinPositionOld);
										if(taglen-8-max(PinPositionNew,PinPositionOld)>Max)
											Max=taglen-8-max(PinPositionNew,PinPositionOld);

										if(Max>15)
											Max=15;
									}
									else
									   Max=15;
									if(Max==0)
									   Max=15;
									PinLength=Max;
									Condition=2;
									outlen=(taglen-3);
									buffer[4]=0;
								}
								else
								{
									Min=PinLength;
									Max=PinLength;
									Condition=1;
									buffer[4]=0;
									outlen=taglen-3;
								}
							}
							break;
						case 2:
							PinType=1;
							//                        PinPositionNew++;
							//                        PinPositionOld++;
							PinPosition=1;
							PinLengthPosition=4;
							PinLengthSize=4;



							if(PinPositionNew>240 || PinPositionOld>240)
							{
								response[0]=0x67;
								response[1]=0x00;
								*lenr=2;
								return 0;
							}
							if(buffer[4]<PinPositionOld+8)
								buffer[4]=PinPositionOld+8;
							if(buffer[4]<PinPositionNew+8)
								buffer[4]=PinPositionNew+8;
							if(bConfirmPIN & 2)
							{
								buffer[5+PinPositionOld]=0x2f;
								memset(buffer+6+PinPositionOld,0xff,7);
							}
							buffer[5+PinPositionNew]=0x2f;
							memset(buffer+6+PinPositionNew,0xff,7);

							outlen=buffer[4]+5;

							if(PinLength==0)
							{
								Min=4;
								Max=12;
								Condition=2;
								PinLength=7;
							}
							else
							{
								Min=PinLength;
								Max=PinLength;
								if(PinLength<4 || PinLength>12)
								{
									response[0]=0x67;
									response[1]=0x00;
									*lenr=2;
									return 0;
								}
								PinLength=7;
								Condition=1;
							}
							break;
						default:
							response[0]=0x6A;
							response[1]=0x00;
							*lenr=2;
							return 0;
						}
						Prologue[0]=0;
/*						if(((cjccidHANDLE)hDevice)->PCB_seq&0x01)
							Prologue[1]=0x40;
						else
							Prologue[1]=0;
						Prologue[2]=outlen;*/
						inlen=sizeof(rbuffer);
						if((tag51=GetTag(data_ptr,Lc,0x51,&taglen))!=NULL)
						{
							if(taglen==2)
							{
								if(tag51[0]>=4 || (tag80[0] & 3)!=2)
									Min=tag51[0];
								if(tag51[1]>=Min)
									Max=tag51[1];
								if(Max>12 && (tag80[0] & 3)==2)
								{
									Max=12;
								}
							}
						}
						cmd_ptr=data_ptr;
						cmd_len=Lc;
						int j=0;
						for(int i=0;i<5;i++)
						{
						   tag50[j]=GetTag(cmd_ptr,cmd_len,0x50,&(taglen50[j]));
							if(tag50[j])
							{
								cmd_ptr+=2+taglen50[j];
								if(cmd_len<2+taglen50[j])
									break;
								cmd_len-=2+taglen50[j];
   							if(i==0 || i==3 || i==4)
								{
		   						TextCount++;
	   							TransformText(tag50[j],taglen50[j]);
   								btaglen50[j]=(uint8_t)taglen50[j];
									j++;
								}
							}
							else 
								break;
						}
						tag53=GetTag(data_ptr,Lc,0x53,&taglen53);
						if(tag53)
						{
							if(taglen53==3)
							{
								memcpy(bMessageIndex,tag53,3);
									
							}
						}
						if(bMessageIndex[1]==0)
						{
							bMessageIndex[1]=1;
						}

						bNumberMessage=0xff;
						tag54=GetTag(data_ptr,Lc,0x54,&taglen54);
						if(tag54)
						{
							if(taglen54==1)
							{
								bNumberMessage=tag54[0];
							}
						}

#ifdef IT_TEST
						switch(Res=cjccid_SecureMV(timeout,PinPosition,PinType,
							PinLengthSize,PinLength,
							PinLengthPosition,
							Min,Max,
							(bConfirmPIN | 1),
							Condition,Prologue,
							PinPositionOld,PinPositionNew,
							buffer,outlen,rbuffer,&inlen,cmd[2]-1,TextCount,tag50,btaglen50,bMessageIndex,bNumberMessage))
#else						
						switch(Res=cjccid_SecureMV(timeout,PinPosition,PinType,
							PinLengthSize,PinLength,
							PinLengthPosition,
							Min,Max,
							(bConfirmPIN | 1),
							Condition,Prologue,
							PinPositionOld,PinPositionNew,
							buffer,outlen,rbuffer,&inlen,TextCount,tag50,btaglen50,bMessageIndex,bNumberMessage))
#endif
						{
						case CJ_SUCCESS:
						case CJ_ERR_PIN_EXTENDED:
/*							if(((cjccidHANDLE)hDevice)->Protokoll==1)
							{
								Res=PVMVT1(hDevice,Res,rbuffer,inlen,&inlen);
							}*/
							if(*lenr<inlen)
							{
								*lenr=0;
								return -11;
							}
							memcpy(response,rbuffer,inlen);
							*lenr=inlen;
							if(Res==CJ_SUCCESS)
							   *sad=0;
							return 0;
						case CJ_ERR_PIN_CANCELED:
							response[0]=0x64;
							response[1]=0x01;
							*lenr=2;
							return 0;
						case CJ_ERR_PIN_TIMEOUT:
							response[0]=0x64;
							response[1]=0x00;
							*lenr=2;
							return 0;
						case CJ_ERR_PIN_DIFFERENT:
							response[0]=0x64;
							response[1]=0x02;
							*lenr=2;
							return 0;
						case CJ_ERR_NO_ICC:
							response[0]=0x64;
							response[1]=0xA1;
							*lenr=2;
							return 0;
						case CJ_ERR_NO_ACTIVE_ICC:
							response[0]=0x64;
							response[1]=0xA2;
							*lenr=2;
							return 0;
						case CJ_ERR_WRONG_PARAMETER:
							response[0]=0x6B;
							response[1]=0x80;
							*lenr=2;
							return 0;
						case CJ_ERR_CONDITION_OF_USE:
							response[0]=0x69;
							response[1]=0x85;
							*lenr=2;
							return 0;

						case CJ_ERR_WRITE_DEVICE:
						case CJ_ERR_DEVICE_LOST:
							*lenr=0;
							return -128;

						case CJ_ERR_RBUFFER_TO_SMALL:
							*lenr=0;
							return -11;

						case CJ_ERR_PARITY:
						case CJ_ERR_TIMEOUT:
							response[0]=0x6f;
							response[1]=0x00;
							*lenr=2;
						default:
							response[0]=0x6b;
							response[1]=0x80;
							*lenr=2;
							return 0;
						}
					}
					else
					{
						response[0]=0x67;
						response[1]=0x00;
						*lenr=2;
						return 0;
					}
				}
			}
			else if(cmd[1]>=0x70 && cmd[1]<=0x76 && IsClass3() && FindModuleWithMask(MODULE_ID_KT_LIGHT,0xfffffeff))
			{
				switch(KTLightCall(sad,dad,cmd,lenc,Lc,data_ptr,Le,response,lenr))
				{
				case CJ_ERR_WRITE_DEVICE:
				case CJ_ERR_DEVICE_LOST:
					*lenr=0;
					return -128;
				case CJ_ERR_RBUFFER_TO_SMALL:
					*lenr=0;
					return -11;
				case CJ_SUCCESS:
					return 0;
				default:
					*lenr=0;
					return -10;
				}

			}
			else
			{
				response[0]=0x6D;
				response[1]=0x00;
				*lenr=2;
				return 0;
			}
		}
		else if(cmd[0]==0x80)				
		{
			if(cmd[1]==0x60)
			{
				uint8_t *tag44;
				int taglen;
				if(cmd[2]!=0x01 || cmd[3]!=0x00)
				{
					response[0]=0x6A;
					response[1]=0x00;


					*lenr=2;
					return 0;

				}
				if(cmd[4]<3)
				{
					response[0]=0x67;
					response[1]=0x00;
					*lenr=2;
					return 0;
				}
				if((tag44=GetTag(cmd+5,cmd[4],0x44,&taglen))!=NULL)
				{
					if(taglen!=1)
					{
						response[0]=0x67;
						response[1]=0x00;
						*lenr=2;
						return 0;
					}
					if((*tag44 & 0xf0)==0x90)
					{
						m_iic_offset_bytes=2;
					}
					else
					{
						m_iic_offset_bytes=1;
					}
					m_iic_pagesize=1<<(*tag44 & 0x0f);
					SetSyncParameters(m_iic_offset_bytes,m_iic_pagesize);
				}
				if((tag44=GetTag(cmd+5,cmd[4],0x45,&taglen))!=NULL)
				{
					if(taglen!=1)
					{
						response[0]=0x67;
						response[1]=0x00;
						*lenr=2;
						return 0;
					}
					m_iic_deviceaddr=*tag44;
				}
				if((tag44=GetTag(cmd+5,cmd[4],0x22,&taglen))!=NULL)
				{
					if(taglen!=1)
					{
						response[0]=0x67;
						response[1]=0x00;
						*lenr=2;
						return 0;
					}
					if(m_ActiveProtocol != SCARD_PROTOCOL_T0 && m_ActiveProtocol != SCARD_PROTOCOL_T1)
					{
						response[0]=0x90;
						response[1]=0x00;
						*lenr=2;
						return 0;
					}
					if(m_ActiveProtocol==SCARD_PROTOCOL_T0 && *tag44!=1 || m_ActiveProtocol==SCARD_PROTOCOL_T1 && *tag44!=2)
					{
						uint8_t atr[250];
						uint8_t historical[15];
						uint8_t hist_len=sizeof(historical);
						uint8_t atr_len=(uint8_t)sizeof(atr);
						IfdPower(SCARD_POWER_DOWN,0,0);


						switch(ctBcsReset(atr,&atr_len,historical,&hist_len,*tag44))
						{
						case CJ_SUCCESS:
							if(m_ActiveProtocol==SCARD_PROTOCOL_T0 && *tag44==1 || m_ActiveProtocol==SCARD_PROTOCOL_T1 && *tag44==2)
							{
								if(atr[0]==0x3b || atr[0]==0x3f)
								{
									response[0]=0x90;
									response[1]=0x01;
								}
								else
								{
									response[0]=0x90;
									response[1]=0x00;
								}
								*lenr=2;
							}
							else
							{
								response[0]=0x64;
								response[1]=0xA3;
							}
							break;
						default:
							response[0]=0x64;
							response[1]=0xA3;


						}
						*lenr=2;
						return 0;
					}
					response[0]=0x90;
					response[1]=0x01;
					*lenr=2;
					return 0;
				}
				response[0]=0x90;
				response[1]=0x00;
				*lenr=2;
				return 0;
			}
/*			else if(cmd[1]==0x61 && cmd[2]==0x81 && cmd[3]==0 && ((cjccidHANDLE)hDevice)->Protokoll==6 || ((cjccidHANDLE)hDevice)->Protokoll==7 && lenc>=6)
			{
				int le=0;
				if(lenc==cmd[4]+6)
				{
					le=cmd[lenc-1];
					if(le==0)
						le=256;
				}
				if(*lenr<le+2)
				{
					*lenr=0;
					return -11;
				}
			   switch(S7SyncCommand(hDevice,cmd[5],cmd[4]-1,cmd+6,le,response))

				{
				case CJ_ERR_RBUFFER_TO_SMALL:
					*lenr=0;
					return -11;
				case CJ_SUCCESS:
					response[le]=0x90;
					response[le+1]=0x00;
					*lenr=le+2;
					return 0;
				case CJ_ERR_PROT:
				case CJ_ERR_TIMEOUT:
				case CJ_ERR_PARITY:
					response[0]=0x6f;
					response[1]=0x00;
					*lenr=2;
					return 0;
				case CJ_ERR_NO_ICC:
					response[0]=0x64;
					response[1]=0xA1;
					*lenr=2;
					return 0;
				case CJ_ERR_NO_ACTIVE_ICC:
					response[0]=0x64;
					response[1]=0xA2;
					*lenr=2;
					return 0;
				case CJ_ERR_WRITE_DEVICE:
				case CJ_ERR_DEVICE_LOST:
					*lenr=0;
					return -128;

				default:
					*lenr=0;
					return -1;

				}
			}*/
/*			else if((((cjccidHANDLE)hDevice)->Info.Flags & 1)==0 && cmd[1]==0x61 && cmd[2]==0x82 && cmd[3]==0  && cmd[4]==0)
			{
				uint16_t Counter;
				if(*lenr<4)
				{
					*lenr=0;
					return -11;
				}
			    switch(cjccid_GetStackSignCounter(hDevice,&Counter))
				{
					case CJ_SUCCESS:
						response[0]=(uint8_t)(Counter>>8);
						response[1]=(uint8_t)(Counter);
						response[2]=0x90;
						response[3]=0x00;
						*lenr=4;
						return 0;
					default:
						response[0]=0x60;
						response[1]=0x00;
						*lenr=2;
						return 0;
				}
			}*/
			else
			{
				response[0]=0x6D;
				response[1]=0x00;
				*lenr=2;
				return 0;
			}

		}
		else
		{
			response[0]=0x6E;
			response[1]=0x00;
			*lenr=2;
			return 0;
		}
	}
	else if(*dad==0 && *sad==2)						
	{
		*dad=2;
		*sad=0;
		if(*lenr<2)
			return -11;

		switch(_IfdTransmit(cmd,lenc,response,lenr))
		{
		case STATUS_SUCCESS:
				return 0;
		case STATUS_IO_TIMEOUT:
		case STATUS_DEVICE_PROTOCOL_ERROR:
				*sad=1;
				response[0]=0x6f;
				response[1]=0x00;
				*lenr=2;
				return 0;
		case STATUS_INVALID_PARAMETER:
				*sad=1;
				response[0]=0x67;
				response[1]=0x00;
				*lenr=2;
				return 0;
		case STATUS_INVALID_DEVICE_STATE:
			if(m_ReaderState==SCARD_ABSENT)
			{
				*sad=1;
				response[0]=0x64;
				response[1]=0xA1;
				*lenr=2;
				return 0;
			}
			else
			{
				*sad=1;
				response[0]=0x64;
				response[1]=0xA2;
				*lenr=2;
				return 0;
			}

		case STATUS_BUFFER_TOO_SMALL:
				*lenr=0;
				return -11;
		case STATUS_DEVICE_NOT_CONNECTED:
				*lenr=0;
				return -128;
		default:
			*lenr=0;
			return -1;
		}
	}

	else
	{
		*lenr=0;
		return -1;
	}
}

int CCCIDReader::ExecuteSecureResult(CCID_Response *Response,uint8_t *in,int *in_len,int offs)
{
	if(Response->bStatus & 0x02)
		return CJ_ERR_NO_ICC;
	else if(Response->bStatus & 0x01)
		return CJ_ERR_NO_ACTIVE_ICC;
   if(Response->bStatus & 0x40)
   {
      if(Response->bError==XFR_PARITY_ERROR)
         return CJ_ERR_PARITY;
      else if(Response->bError==ICC_MUTE)
         return CJ_ERR_TIMEOUT;
      else if(Response->bError==PIN_TIMEOUT)
         return CJ_ERR_PIN_TIMEOUT;
      else if(Response->bError==PIN_CANCELED)
         return CJ_ERR_PIN_CANCELED;
      else if(Response->bError==PIN_DIFFERENT)
         return CJ_ERR_PIN_DIFFERENT;
      else if(Response->bError==EXT_ERROR)
		{
		   if(*in_len<(int)Response->dwLength)
				return CJ_ERR_RBUFFER_TO_SMALL;
		   memcpy(in,Response->Data.abData,Response->dwLength);
		   *in_len=Response->dwLength;
         return CJ_ERR_PIN_EXTENDED;
		}
 	   else if(Response->bError==5)
		  return CJ_ERR_WRONG_PARAMETER;
 	   else if(Response->bError==21+offs)
		  return CJ_ERR_WRONG_PARAMETER;
	   else if(Response->bError==26+offs)
		  return CJ_ERR_CONDITION_OF_USE;
      else if(Response->bError==DEACTIVATED_PROTOCOL)
		{
			if(Response->bStatus & 0x02)
				return CJ_ERR_NO_ICC;
			else if(Response->bStatus & 0x01)
				return CJ_ERR_NO_ACTIVE_ICC;
		}
      else
         return CJ_ERR_LEN;
   }
   if(*in_len<(int)Response->dwLength)
      return CJ_ERR_RBUFFER_TO_SMALL;
   memcpy(in,Response->Data.abData,Response->dwLength);
   *in_len=Response->dwLength;
   return CJ_SUCCESS;
}

#ifdef IT_TEST
int CCCIDReader::cjccid_SecurePV(uint8_t Timeout,
                    uint8_t PinPosition,uint8_t PinType,
                    uint8_t PinLengthSize,uint8_t PinLength,
                    uint8_t PinLengthPosition,
                    uint8_t Min, uint8_t Max,
                    uint8_t Condition,uint8_t *Prologue,
                    uint8_t *out,int out_len,uint8_t *in,int *in_len,uint8_t Slot,uint8_t *Text,uint8_t Textlen,uint8_t bMessageIndex,uint8_t bNumberMessage)
#else
int CCCIDReader::cjccid_SecurePV(uint8_t Timeout,
                    uint8_t PinPosition,uint8_t PinType,
                    uint8_t PinLengthSize,uint8_t PinLength,
                    uint8_t PinLengthPosition,
                    uint8_t Min, uint8_t Max,
                    uint8_t Condition,uint8_t *Prologue,
                    uint8_t *out,int out_len,uint8_t *in,int *in_len,uint8_t *Text,uint8_t Textlen,uint8_t bMessageIndex,uint8_t bNumberMessage)
#endif
{
	CCID_Message Message;
	CCID_Response Response;
	int Res;
	cj_ModuleInfo *Info;

   Message.bMessageType=PC_TO_RDR_SECURE;
   Message.dwLength=out_len+15;

#ifdef IT_TEST
   Message.bSlot=Slot;
#endif
   Message.Header.Secure.bBWI=0;
	Message.Header.Secure.wLevelParameter=HostToReaderShort(0);
   Message.Data.Secure.bPINOperation=0;
   Message.Data.Secure.bTimeOut=Timeout;
   Message.Data.Secure.bmFormatString=(uint8_t)(0x80 | (PinPosition<<3) | PinType);
   Message.Data.Secure.bmPINBlockString=(uint8_t)((PinLengthSize<<4) | PinLength);
   Message.Data.Secure.bmPINLengthFormat=PinLengthPosition;
   Message.Data.Secure.Data.Verify.wPINMaxExtraDigit=HostToReaderShort((((uint16_t)Min)<<8)+Max);
   Message.Data.Secure.Data.Verify.bEntryValidationCondition=Condition;
   Message.Data.Secure.Data.Verify.bNumberMessage=bNumberMessage;
	Message.Data.Secure.Data.Verify.wLangId=HostToReaderShort(0x0409);
   Message.Data.Secure.Data.Verify.bMsgIndex=bMessageIndex;
   memcpy(Message.Data.Secure.Data.Verify.bTeoPrologue,Prologue,3);
   memcpy(Message.Data.Secure.Data.Verify.abData,out,out_len);
#ifdef _INSERT_KEY_EVENTS
	{
		uint8_t buffer[2];
		buffer[0]=RDR_TO_PC_KEYEVENT;
		buffer[1]=0xa0;
		DoInterruptCallback(buffer,2);
	}
#endif
   Info=FindModule(MODULE_ID_KT_LIGHT_GC);
	if(Info!=NULL && Info->Status==MODULE_READY)
		SetSMModeAndCount(MODULE_ID_KT_LIGHT_GC,1);

	Res=Transfer(&Message,&Response);
#ifdef _INSERT_KEY_EVENTS
	if(Info!=NULL && Info->Status==MODULE_READY)
		SetSMModeAndCount(0,0);
   if(Res==CJ_SUCCESS)
   {
		if((Response.bStatus & 0x03)==0 && (((Response.bStatus & 0x40) && Response.bError==PIN_CANCELED) || (Response.bStatus & 0x40)==0))
		{
			uint8_t buffer[2];
			buffer[0]=RDR_TO_PC_KEYEVENT;
			if(Response.bError==PIN_CANCELED)
				buffer[1]=0x01;
			else 
				buffer[1]=0x02;
			DoInterruptCallback(buffer,2);
		}

   }
	{
		uint8_t buffer[2];
		buffer[0]=RDR_TO_PC_KEYEVENT;
		buffer[1]=0xa1;
		DoInterruptCallback(buffer,2);
	}
#endif
   if(Res)
	   return Res;
	return ExecuteSecureResult(&Response,in,in_len,0);
}

#ifdef IT_TEST
int CCCIDReader::cjccid_SecureMV(uint8_t Timeout,
                    uint8_t PinPosition,uint8_t PinType,
                    uint8_t PinLengthSize,uint8_t PinLength,
                    uint8_t PinLengthPosition,
                    uint8_t Min, uint8_t Max,
                    uint8_t bConfirmPIN,
                    uint8_t Condition,uint8_t *Prologue,
                    uint8_t OffsetOld,uint8_t OffsetNew,
                    uint8_t *out,int out_len,uint8_t *in,int *in_len,uint8_t Slot,int TextCount,uint8_t *Text[3],uint8_t Textlen[3],uint8_t bMessageIndex[3],uint8_t bNumberMessage)
#else
int CCCIDReader::cjccid_SecureMV(uint8_t Timeout,
                    uint8_t PinPosition,uint8_t PinType,
                    uint8_t PinLengthSize,uint8_t PinLength,
                    uint8_t PinLengthPosition,
                    uint8_t Min, uint8_t Max,
                    uint8_t bConfirmPIN,
                    uint8_t Condition,uint8_t *Prologue,
                    uint8_t OffsetOld,uint8_t OffsetNew,
                    uint8_t *out,int out_len,uint8_t *in,int *in_len,int TextCount,uint8_t *Text[3],uint8_t Textlen[3],uint8_t bMessageIndex[3],uint8_t bNumberMessage)
#endif
{
	CCID_Message Message;
	CCID_Response Response;
	int Res;
   //cj_ModuleInfo *Info=FindModule(MODULE_ID_KERNEL);

   Message.bMessageType=PC_TO_RDR_SECURE;
   Message.dwLength=out_len+20;
#ifdef IT_TEST
   Message.bSlot=Slot;
#else
   Message.bSlot=0;
#endif
   Message.Header.Secure.bBWI=0;
	Message.Header.Secure.wLevelParameter=HostToReaderShort(0);
   Message.Data.Secure.bPINOperation=1;
   Message.Data.Secure.bTimeOut=Timeout;
   Message.Data.Secure.bmFormatString=(uint8_t)(0x80 | (PinPosition<<3) | PinType);
   Message.Data.Secure.bmPINBlockString=(uint8_t)((PinLengthSize<<4) | PinLength);
   Message.Data.Secure.bmPINLengthFormat=PinLengthPosition;
   Message.Data.Secure.Data.Modify.bInsertionOffsetOld=OffsetOld;
   Message.Data.Secure.Data.Modify.bInsertionOffsetNew=OffsetNew;
	Message.Data.Secure.Data.Modify.wPINMaxExtraDigit=HostToReaderShort((((uint16_t)Min)<<8)+Max);
   Message.Data.Secure.Data.Modify.bConfirmPIN= bConfirmPIN;
   Message.Data.Secure.Data.Modify.bEntryValidationCondition=Condition;
   Message.Data.Secure.Data.Modify.bNumberMessage=bNumberMessage;
   Message.Data.Secure.Data.Modify.wLangId=HostToReaderShort(0x0409);
   Message.Data.Secure.Data.Modify.bMsgIndex1=bMessageIndex[0];
	Message.Data.Secure.Data.Modify.bMsgIndex2=bMessageIndex[1];
	Message.Data.Secure.Data.Modify.bMsgIndex3=bMessageIndex[2];
   memcpy(Message.Data.Secure.Data.Modify.bTeoPrologue,Prologue,3);
   memcpy(Message.Data.Secure.Data.Modify.abData,out,out_len);
#ifdef _INSERT_KEY_EVENTS
	{
		uint8_t buffer[2];
		buffer[0]=RDR_TO_PC_KEYEVENT;
		buffer[1]=0xa0;
		DoInterruptCallback(buffer,2);
	}
#endif

	Res=Transfer(&Message,&Response);
	if(Res!=0)
	{
#ifdef _INSERT_KEY_EVENTS
		uint8_t buffer[2];
		buffer[0]=RDR_TO_PC_KEYEVENT;
		buffer[1]=0xa1;
		DoInterruptCallback(buffer,2);
#endif
		return Res;
	}
#ifdef _INSERT_KEY_EVENTS
	if((Response.bStatus & 0x03)==0 && (((Response.bStatus & 0x40) && Response.bError==PIN_CANCELED) || (Response.bStatus & 0x40)==0))
	{
		uint8_t buffer[2];
		buffer[0]=RDR_TO_PC_KEYEVENT;
	   if(Response.bError==PIN_CANCELED)
			buffer[1]=0x01;
	   else 
			buffer[1]=0x02;
		DoInterruptCallback(buffer,2);
	}
	{
		uint8_t buffer[2];
		buffer[0]=RDR_TO_PC_KEYEVENT;
		buffer[1]=0xa1;
		DoInterruptCallback(buffer,2);
	}

#endif
	return ExecuteSecureResult(&Response,in,in_len,5);
}

RSCT_IFD_RESULT CCCIDReader::IfdVendor(uint32_t IoCtrlCode,uint8_t *Input,uint32_t InputLength,uint8_t *Output,uint32_t *OutputLength)
{
	uint8_t dad,sad;
	uint16_t Len;
	switch(IoCtrlCode)
	{
	case CJPCSC_VEN_IOCTRL_ESCAPE:
		switch(CCID_Escape(Input,InputLength,Output,OutputLength))
		{
		case CJ_SUCCESS:
			return STATUS_SUCCESS;
		case CJ_ERR_INTERNAL_BUFFER_OVERFLOW:
			return STATUS_INSUFFICIENT_RESOURCES;
		case CJ_ERR_DEVICE_LOST:
			return STATUS_DEVICE_NOT_CONNECTED;
		case CJ_ERR_RBUFFER_TO_SMALL:
			return STATUS_BUFFER_TOO_SMALL;
		case CJ_ERR_SEQ:
			return STATUS_DEVICE_PROTOCOL_ERROR;
		default:
			return STATUS_UNHANDLED_EXCEPTION;
		}
		break;
	case CJPCSC_VEN_IOCTRL_VERIFY_PIN_DIRECT:
		if(InputLength<sizeof(PIN_VERIFY_STRUCTURE))
         return STATUS_INVALID_BUFFER_SIZE;
		if(InputLength!=sizeof(PIN_VERIFY_STRUCTURE)-sizeof(uint8_t)+((PIN_VERIFY_STRUCTURE *)Input)->ulDataLength)
         return STATUS_INVALID_BUFFER_SIZE;
		return IfdVerifyPinDirect((PIN_VERIFY_STRUCTURE *)Input,Output,OutputLength);

	case CJPCSC_VEN_IOCTRL_MODIFY_PIN_DIRECT:
		if(InputLength<sizeof(PIN_MODIFY_STRUCTURE))
         return STATUS_INVALID_BUFFER_SIZE;
		if(InputLength!=sizeof(PIN_MODIFY_STRUCTURE)-sizeof(uint8_t)+((PIN_MODIFY_STRUCTURE *)Input)->ulDataLength)
         return STATUS_INVALID_BUFFER_SIZE;
		return IfdModifyPinDirect((PIN_MODIFY_STRUCTURE *)Input,Output,OutputLength);
  case CJPCSC_VEN_IOCTRL_SET_NORM:
		if(InputLength<1)
       return STATUS_INVALID_BUFFER_SIZE;
		else
			CtSetAPDUNorm((EApduNorm)(Input[0]));
		if(OutputLength!=NULL)
			*OutputLength=0;
		return STATUS_SUCCESS;
  case CJPCSC_VEN_IOCTRL_MCT_READERDIRECT:
	    dad=1;
		sad=2;
		if(*OutputLength>0xffff)
			Len=0xffff;
		else
		    Len=(USHORT)*OutputLength;
	    switch(CtData(&sad,&dad,Input,InputLength,Output,&Len))
		{
		case 0:
			*OutputLength=Len;
			return STATUS_SUCCESS;
		case -11:
			*OutputLength=0;
			return STATUS_BUFFER_TOO_SMALL;
		default:
			*OutputLength=0;
			return STATUS_DEVICE_NOT_CONNECTED;
		}
	default:
		return base::IfdVendor(IoCtrlCode,Input,InputLength,Output,OutputLength);
	}
}

CJ_RESULT CCCIDReader::CCID_Escape(uint8_t *Input,uint32_t InputLength,uint8_t *Output,uint32_t *OutputLength)
{
	CJ_RESULT Res;
	CCID_Message Message;
	CCID_Response Response;
	memset(&Message,0,sizeof(Message));
	Message.bMessageType=PC_TO_RDR_ESCAPE;
	Message.dwLength=InputLength;
	if(InputLength>sizeof(Message.Data.abData))
	{
      *OutputLength=0;
		return CJ_ERR_INTERNAL_BUFFER_OVERFLOW;
	}
	if(InputLength>0)
	   memcpy(&Message.Data.abData,Input,InputLength);
   if((Res=Transfer(&Message,&Response))!=CJ_SUCCESS)
	{
      *OutputLength=0;
		return Res;
	}

	if(Response.bMessageType!=RDR_TO_PC_ESCAPE)
	{
		delete m_pCommunicator;
		m_pCommunicator=NULL;
		return CJ_ERR_DEVICE_LOST;
	}
	else if(Response.dwLength>*OutputLength)
	{
      *OutputLength=0;
		return CJ_ERR_RBUFFER_TO_SMALL;
	}
	memcpy(Output,Response.Data.abData,Response.dwLength);
	*OutputLength=Response.dwLength;
	return CJ_SUCCESS;
}

bool CCCIDReader::PinDirectSupported()
{
   cj_ModuleInfo *Info=FindModule(MODULE_ID_KERNEL);
	if(Info==NULL)
		return false;
	if(Info->Version<0x30 || Info->Version==0x30 && Info->Revision<42)
		return false;
	return true;
}

RSCT_IFD_RESULT CCCIDReader::IfdVerifyPinDirect(PIN_VERIFY_STRUCTURE *Input,uint8_t *Output,uint32_t *OutputLength)
{
	CCID_Message Message;
	CCID_Response Response;
	int Res;
   cj_ModuleInfo *Info;

   if(!PinDirectSupported())
      return STATUS_NOT_SUPPORTED;
   Info=FindModule(MODULE_ID_KT_LIGHT_GC);
	if(Info!=NULL && Info->Status==MODULE_READY)
		SetSMModeAndCount(MODULE_ID_KT_LIGHT_GC,1);
   
	Message.bMessageType=PC_TO_RDR_SECURE;
   Message.dwLength=Input->ulDataLength+15;
	if(Input->ulDataLength>sizeof(Message.Data.Secure.Data.Verify.abData))
      return STATUS_INVALID_BUFFER_SIZE;
	if(*OutputLength<2)
      return STATUS_BUFFER_TOO_SMALL;
   Message.bSlot=0;
   Message.Header.Secure.bBWI=0;
	Message.Header.Secure.wLevelParameter=HostToReaderShort(0);
   Message.Data.Secure.bPINOperation=0;
   Message.Data.Secure.bTimeOut=Input->bTimerOut;
   Message.Data.Secure.bmFormatString=Input->bmFormatString;
   Message.Data.Secure.bmPINBlockString=Input->bmPINBlockString;
   Message.Data.Secure.bmPINLengthFormat=Input->bmPINLengthFormat;
   Message.Data.Secure.Data.Verify.wPINMaxExtraDigit=HostToReaderShort(Input->wPINMaxExtraDigit);
   Message.Data.Secure.Data.Verify.bEntryValidationCondition=Input->bEntryValidationCondition;
   Message.Data.Secure.Data.Verify.bNumberMessage=Input->bNumberMessage;
	Message.Data.Secure.Data.Verify.wLangId=HostToReaderShort(Input->wLangId);
   Message.Data.Secure.Data.Verify.bMsgIndex=Input->bMsgIndex;
   FillTeoPrologue(Message.Data.Secure.Data.Verify.bTeoPrologue);
   memcpy(Message.Data.Secure.Data.Verify.abData,Input->abData,Input->ulDataLength);
	Res=Transfer(&Message,&Response);

	if(Info!=NULL && Info->Status==MODULE_READY)
		SetSMModeAndCount(0,0);

	if(Res)
	{
		*OutputLength=0;
		if(Res==CJ_ERR_DEVICE_LOST)
		   return STATUS_DEVICE_NOT_CONNECTED;
		return STATUS_DEVICE_PROTOCOL_ERROR;
	}
   if(Response.bStatus & 0x40)
   {
      if(Response.bError==XFR_PARITY_ERROR)
		{
			*OutputLength=0;
         return STATUS_IO_TIMEOUT;
		}
      else if(Response.bError==ICC_MUTE)
		{
			*OutputLength=0;
         return STATUS_IO_TIMEOUT;
		}
      else if(Response.bError==PIN_TIMEOUT)
		{
			memcpy(Output,"\x64\x00",2);
			*OutputLength=2;
         return STATUS_SUCCESS;
		}
      else if(Response.bError==PIN_CANCELED)
		{
			memcpy(Output,"\x64\x01",2);
			*OutputLength=2;
         return STATUS_SUCCESS;
		}
      else if(Response.bError==PIN_DIFFERENT)
		{
			memcpy(Output,"\x64\x02",2);
			*OutputLength=2;
         return STATUS_SUCCESS;
		}
      else if(Response.bError==EXT_ERROR)
		{
		   if(*OutputLength<Response.dwLength)
			{
				*OutputLength=0;
				return STATUS_BUFFER_TOO_SMALL;
			}
		   memcpy(Output,Response.Data.abData,Response.dwLength);
		   *OutputLength=Response.dwLength;
         return STATUS_SUCCESS;
		}
 	   else if(Response.bError==5)
		{
			memcpy(Output,"\x6B\x80",2);
			*OutputLength=2;
         return STATUS_SUCCESS;
		}
 	   else if(Response.bError==21)
		{
			memcpy(Output,"\x64\x02",2);
			*OutputLength=2;
         return STATUS_SUCCESS;
		}
	   else if(Response.bError==26)
		{
			memcpy(Output,"\x69\x85",2);
			*OutputLength=2;
         return STATUS_SUCCESS;
		}
      else if(Response.bError==DEACTIVATED_PROTOCOL)
		{
			if(Response.bStatus & 0x02)
			{
				*OutputLength=0;
				return STATUS_NO_MEDIA;
			}
			else if(Response.bStatus & 0x01)
			{
				*OutputLength=0;
				return STATUS_INVALID_DEVICE_STATE;
			}
		}
      else
		{
			memcpy(Output,"\x6B\x80",2);
			*OutputLength=2;
         return STATUS_SUCCESS;
		}
   }
   else if(m_ActiveProtocol==SCARD_PROTOCOL_T1)
	   Res=PVMVT1(Res,Response.Data.abData,Response.dwLength,&Response.dwLength);
   if(*OutputLength<Response.dwLength)
	{
		*OutputLength=0;
		return STATUS_BUFFER_TOO_SMALL;
	}
   memcpy(Output,Response.Data.abData,Response.dwLength);
   *OutputLength=Response.dwLength;
   return STATUS_SUCCESS;
}

RSCT_IFD_RESULT CCCIDReader::PVMVT1(int Result,uint8_t *,uint32_t,uint32_t *)
{
	return Result;
}

void CCCIDReader::FillTeoPrologue(uint8_t *pbTeoPrologue)
{
	memset(pbTeoPrologue,0,3);
}

void CCCIDReader::CheckReaderDepended(CCID_Message &Message)
{}


RSCT_IFD_RESULT CCCIDReader::IfdModifyPinDirect(PIN_MODIFY_STRUCTURE *Input,uint8_t *Output,uint32_t *OutputLength)
{
	CCID_Message Message;
	CCID_Response Response;
	int Res;

   if(!PinDirectSupported())
      return STATUS_NOT_SUPPORTED;
//   cj_ModuleInfo *Info=FindModule(MODULE_ID_KERNEL);

   Message.bMessageType=PC_TO_RDR_SECURE;
   Message.dwLength=Input->ulDataLength+20;
	if(Input->ulDataLength>sizeof(Message.Data.Secure.Data.Modify.abData))
      return STATUS_INVALID_BUFFER_SIZE;
	if(*OutputLength<2)
      return STATUS_BUFFER_TOO_SMALL;
   Message.bSlot=0;
   Message.Header.Secure.bBWI=0;
	Message.Header.Secure.wLevelParameter=HostToReaderShort(0);
   Message.Data.Secure.bPINOperation=1;
   Message.Data.Secure.bTimeOut=Input->bTimerOut;
   Message.Data.Secure.bmFormatString=Input->bmFormatString;
   Message.Data.Secure.bmPINBlockString=Input->bmPINBlockString;
   Message.Data.Secure.bmPINLengthFormat=Input->bmPINLengthFormat;
	Message.Data.Secure.Data.Modify.bInsertionOffsetOld=Input->bInsertionOffsetOld;
	Message.Data.Secure.Data.Modify.bInsertionOffsetNew=Input->bInsertionOffsetNew;
   Message.Data.Secure.Data.Modify.wPINMaxExtraDigit=HostToReaderShort(Input->wPINMaxExtraDigit);
	Message.Data.Secure.Data.Modify.bConfirmPIN=Input->bConfirmPIN;
   Message.Data.Secure.Data.Modify.bEntryValidationCondition=Input->bEntryValidationCondition;
   Message.Data.Secure.Data.Modify.bNumberMessage=Input->bNumberMessage;
	Message.Data.Secure.Data.Modify.wLangId=HostToReaderShort(Input->wLangId);
   Message.Data.Secure.Data.Modify.bMsgIndex1=Input->bMsgIndex1;
   Message.Data.Secure.Data.Modify.bMsgIndex2=Input->bMsgIndex2;
   Message.Data.Secure.Data.Modify.bMsgIndex3=Input->bMsgIndex3;
   FillTeoPrologue(Message.Data.Secure.Data.Modify.bTeoPrologue);
   memcpy(Message.Data.Secure.Data.Modify.abData,Input->abData,Input->ulDataLength);
	Res=Transfer(&Message,&Response);

   if(Res)
	{
		*OutputLength=0;
		if(Res==CJ_ERR_DEVICE_LOST)
		   return STATUS_DEVICE_NOT_CONNECTED;
		return STATUS_DEVICE_PROTOCOL_ERROR;
	}
   if(Response.bStatus & 0x40)
   {
      if(Response.bError==XFR_PARITY_ERROR)
		{
			*OutputLength=0;
         return STATUS_IO_TIMEOUT;
		}
      else if(Response.bError==ICC_MUTE)
		{
			*OutputLength=0;
         return STATUS_IO_TIMEOUT;
		}
      else if(Response.bError==PIN_TIMEOUT)
		{
			memcpy(Output,"\x64\x00",2);
			*OutputLength=2;
         return STATUS_SUCCESS;
		}
      else if(Response.bError==PIN_CANCELED)
		{
			memcpy(Output,"\x64\x01",2);
			*OutputLength=2;
         return STATUS_SUCCESS;
		}
      else if(Response.bError==PIN_DIFFERENT)
		{
			memcpy(Output,"\x64\x02",2);
			*OutputLength=2;
         return STATUS_SUCCESS;
		}
      else if(Response.bError==EXT_ERROR)
		{
		   if(*OutputLength<Response.dwLength)
			{
				*OutputLength=0;
				return STATUS_BUFFER_TOO_SMALL;
			}
		   memcpy(Output,Response.Data.abData,Response.dwLength);
		   *OutputLength=Response.dwLength;
         return STATUS_SUCCESS;
		}
 	   else if(Response.bError==5)
		{
			memcpy(Output,"\x6B\x80",2);
			*OutputLength=2;
         return STATUS_SUCCESS;
		}
 	   else if(Response.bError==26)
		{
			memcpy(Output,"\x64\x02",2);
			*OutputLength=2;
         return STATUS_SUCCESS;
		}
	   else if(Response.bError==31)
		{
			memcpy(Output,"\x69\x85",2);
			*OutputLength=2;
         return STATUS_SUCCESS;
		}
      else if(Response.bError==DEACTIVATED_PROTOCOL)
		{
			if(Response.bStatus & 0x02)
			{
				*OutputLength=0;
				return STATUS_NO_MEDIA;
			}
			else if(Response.bStatus & 0x01)
			{
				*OutputLength=0;
				return STATUS_INVALID_DEVICE_STATE;
			}
		}
      else
		{
			memcpy(Output,"\x6B\x80",2);
			*OutputLength=2;
         return STATUS_SUCCESS;
		}
   }
   else if(m_ActiveProtocol==SCARD_PROTOCOL_T1)
	   Res=PVMVT1(Res,Response.Data.abData,Response.dwLength,&Response.dwLength);
   if(*OutputLength<Response.dwLength)
	{
		*OutputLength=0;
		return STATUS_BUFFER_TOO_SMALL;
	}
   memcpy(Output,Response.Data.abData,Response.dwLength);
   *OutputLength=Response.dwLength;
   return STATUS_SUCCESS;
}

CJ_RESULT CCCIDReader::SetSMModeAndCount(uint32_t ModuleID,uint32_t Count)
{
	return CJ_SUCCESS; 
}
