#include "Platform.h"
#include "BaseReader.h"
#include "Reader.h"
#include "ntstatus.h"


#define CASE_DEBUG(a) {\
	case a:\
		m_Owner->DebugResult("%s:%d --> %s", __FUNCTION__, __LINE__ ,#a);\
  }


#define RET_RESULT(a) {\
  m_Owner->DebugResult("%s --> %s", __FUNCTION__, #a);\
  return (a);\
  }

#define SET_CT_DATA_ERROR(a) {\
  *response_len=2;\
  *((uint16_t*)response)=htons(a);\
  m_Owner->DebugErrorSW1SW2("%s --> %d", __FUNCTION__, a);\
  }

CBaseReader::CBaseReader(CReader *Owner,CBaseCommunication *Communicator)
{
   m_Owner=Owner;
   m_pCommunicator=Communicator;
   m_pModuleInfo=NULL;
   m_ModuleInfoCount=0;
	m_ReaderState=0;
	m_ActiveProtocol=0;
	m_ATR_Length=0;
	m_KeyCallbackCtx=NULL;
	m_KeyIntCallback=NULL;
	m_ChangeCallbackCtx=NULL;
	m_ChangeIntCallback=NULL;
	m_ApduNorm=NORM_PCSC;
	m_bIsRF=false;
}

CJ_RESULT CBaseReader::PostCreate()
{
	CJ_RESULT Res;
	if((Res=BuildReaderInfo())==CJ_SUCCESS)
	   Res=BuildModuleInfo();
	return Res;
}

uint8_t *CBaseReader::GetTag(uint8_t *start,int len,uint8_t tagvalue,int *taglen)
{										
	uint8_t tag;
	uint8_t tlen;
	*taglen=0;
	while(len>2)     
	{
		tag=*start++;	
		tlen=*start++;   
		if(tag==tagvalue)   
		{
			*taglen=tlen;
			return start;
		}
		start+=tlen;   
		len-=tlen+2;  
	}
	return NULL;
}

void CBaseReader::Unconnect(void)
{
   CBaseCommunication *merk=m_pCommunicator;
	m_pCommunicator=0;
   if (merk)
      delete merk;
}



CBaseReader::~CBaseReader(void)
{
  Unconnect();
  if(m_pModuleInfo)
    delete [] m_pModuleInfo;
}

char CBaseReader::CtData(uint8_t *sad,uint8_t *dad,const uint8_t *cmd, uint16_t cmd_len,uint8_t *response,uint16_t *response_len)
{
	*sad=1;
	*dad=2;
	SET_CT_DATA_ERROR(0x6D00);
   return -10;
}

RSCT_IFD_RESULT CBaseReader::IfdPower(uint32_t Mode,uint8_t *ATR,uint32_t *ATR_Length)
{
	return IfdPower(Mode,ATR,ATR_Length,0);
}

RSCT_IFD_RESULT CBaseReader::IfdSetProtocol(uint32_t *Protocol)
{
	*Protocol=0;
	m_ActiveProtocol=0;
	RET_RESULT(STATUS_NO_MEDIA);
}


RSCT_IFD_RESULT CBaseReader::IfdGetState(uint32_t *State)
{
	*State=SCARD_UNKNOWN;
	return STATUS_SUCCESS;
}

RSCT_IFD_RESULT CBaseReader::IfdSetAttribute(const uint8_t *Input,uint32_t InputLength)
{
	RET_RESULT(STATUS_NOT_SUPPORTED);
}

RSCT_IFD_RESULT CBaseReader::IfdGetAttribute(uint32_t Tag,uint8_t *Attribute,uint32_t *AttributeLength)
{
	*AttributeLength=0;
	RET_RESULT(STATUS_NOT_SUPPORTED);
}

RSCT_IFD_RESULT CBaseReader::IfdSwallow()
{
	RET_RESULT(STATUS_NOT_SUPPORTED);
}

RSCT_IFD_RESULT CBaseReader::IfdEject()
{
	RET_RESULT(STATUS_NOT_SUPPORTED);
}

RSCT_IFD_RESULT CBaseReader::IfdVendor(uint32_t IoCtrlCode,uint8_t *Input,uint32_t InputLength,uint8_t *Output,uint32_t *OutputLength)
{
	*OutputLength=0;
	RET_RESULT(STATUS_NOT_SUPPORTED);
}

RSCT_IFD_RESULT CBaseReader::IfdTransmit(const uint8_t *cmd, uint16_t cmd_len,uint8_t *response,uint16_t *response_len)
{
	RET_RESULT(STATUS_NO_MEDIA);
}

RSCT_IFD_RESULT CBaseReader::IfdPower(uint32_t Mode,uint8_t *ATR,uint32_t *ATR_Length,uint32_t Timeout)
{
   m_ReaderState=0;	
	RET_RESULT(STATUS_NO_MEDIA);
}

RSCT_IFD_RESULT CBaseReader::IfdIoControl(uint32_t IoCtrlCode,uint8_t *Input,uint32_t InputLength,uint8_t *Output,uint32_t *OutputLength)
{
#ifdef _WINDOWS
	switch(IoCtrlCode)
	{
	CASE_DEBUG(IOCTL_SMARTCARD_POWER)
	{
		uint32_t mode;
		if(InputLength<sizeof(mode))
			return STATUS_INVALID_BUFFER_SIZE;
		memcpy(&mode,Input,sizeof(Input));
		return IfdPower(mode,Output,OutputLength);
	}
	CASE_DEBUG(IOCTL_SMARTCARD_SET_PROTOCOL)
	{
		RSCT_IFD_RESULT Result;
		uint32_t protocol;
		if(InputLength<sizeof(protocol) || *OutputLength<sizeof(protocol))
			return STATUS_INVALID_BUFFER_SIZE;
		memcpy(&protocol,Input,sizeof(Input));
		Result=IfdSetProtocol(&protocol);
		memcpy(Output,&protocol,sizeof(protocol));
		*OutputLength=sizeof(protocol);
		return Result;
	}

	CASE_DEBUG(IOCTL_SMARTCARD_TRANSMIT)
	{
		RSCT_IFD_RESULT Res;
		uint16_t length=(uint16_t)*OutputLength;

		Res= IfdTransmit(Input,(uint16_t)InputLength,Output,&length);
		*OutputLength=length;
		return Res;
	}

	CASE_DEBUG(IOCTL_SMARTCARD_GET_ATTRIBUTE)
		uint32_t Tag;
		if(InputLength<sizeof(Tag))
			return STATUS_INVALID_BUFFER_SIZE;
		memcpy(&Tag,Input,sizeof(Input));
		return IfdGetAttribute(Tag,Output,OutputLength);
	CASE_DEBUG(IOCTL_SMARTCARD_SET_ATTRIBUTE)
		return IfdSetAttribute(Input,InputLength);
	CASE_DEBUG(IOCTL_SMARTCARD_EJECT)
		return IfdEject();
	CASE_DEBUG(IOCTL_SMARTCARD_SWALLOW)
		return IfdSwallow();
	CASE_DEBUG(IOCTL_SMARTCARD_CONFISCATE)
		return STATUS_NOT_SUPPORTED;
	CASE_DEBUG(IOCTL_SMARTCARD_GET_STATE)
		if(*OutputLength<sizeof(uint32_t))
			return STATUS_INVALID_BUFFER_SIZE;
	   *OutputLength=sizeof(uint32_t);
		return IfdGetState((uint32_t*)Output);
	CASE_DEBUG(IOCTL_SMARTCARD_GET_LAST_ERROR)
	   return STATUS_INVALID_PARAMETER;
	   
#ifndef UNDER_CE
	CASE_DEBUG(IOCTL_SMARTCARD_GET_PERF_CNTR)
	   return STATUS_INVALID_PARAMETER;
#endif	
	
	CASE_DEBUG(IOCTL_SMARTCARD_IS_PRESENT)
	   return STATUS_INVALID_PARAMETER;
	CASE_DEBUG(IOCTL_SMARTCARD_IS_ABSENT)
	   return STATUS_INVALID_PARAMETER;
	default:
		if(IoCtrlCode>=SCARD_CTL_CODE(2048) && IoCtrlCode<=SCARD_CTL_CODE(4095))
		{
   		return IfdVendor(IoCtrlCode,Input,InputLength,Output,OutputLength);
		}
		else
		{
		   return STATUS_NOT_SUPPORTED;
		}
	}
#else
        return IfdVendor(IoCtrlCode,Input,InputLength,Output,OutputLength);
#endif
}

CJ_RESULT CBaseReader::CtLoadModule(uint8_t *pData,uint32_t DataLength,uint8_t *pSgn,uint32_t SgnLength,uint32_t *Result)
{
   RET_RESULT(SCARD_E_UNSUPPORTED_FEATURE);
}

CJ_RESULT CBaseReader::CtKeyUpdate(uint8_t *pData,uint32_t DataLength,uint32_t *Result)
{
   RET_RESULT(SCARD_E_UNSUPPORTED_FEATURE);
}

CJ_RESULT CBaseReader::CtIsKeyUpdateRecommended(uint8_t *pData,uint32_t DataLength,uint32_t *EstimatedUpdateTime)
{
   RET_RESULT(SCARD_E_UNSUPPORTED_FEATURE);
}

CJ_RESULT CBaseReader::CtDeleteModule(uint32_t ModuleID,uint32_t *Result)
{
   RET_RESULT(SCARD_E_UNSUPPORTED_FEATURE);
}


CJ_RESULT CBaseReader::CtActivateModule(uint32_t ModuleID,uint32_t *Result)
{
   RET_RESULT(SCARD_E_UNSUPPORTED_FEATURE);
}

CJ_RESULT CBaseReader::CtDeactivateModule(uint32_t ModuleID,uint32_t *Result)
{
   RET_RESULT(SCARD_E_UNSUPPORTED_FEATURE);
}

CJ_RESULT CBaseReader::CtDeleteALLModules(uint32_t *Result)
{
   RET_RESULT(SCARD_E_UNSUPPORTED_FEATURE);
}

CJ_RESULT CBaseReader::CtSetModulestoreInfo(uint8_t *Info,uint8_t InfoLength)
{
   RET_RESULT(SCARD_E_UNSUPPORTED_FEATURE);
}

CJ_RESULT CBaseReader::CtGetModulestoreInfo(uint8_t *Info,uint8_t *InfoLength)
{
	*InfoLength=0;
   RET_RESULT(SCARD_E_UNSUPPORTED_FEATURE);
}

CJ_RESULT CBaseReader::CtListModules(uint32_t *Count,cj_ModuleInfo *ModuleInfo)
{
	cj_ModuleInfo *Info=ModuleInfo;
	if(*Count==SCARD_AUTOALLOCATE)
	{
		Info=new cj_ModuleInfo[m_ModuleInfoCount];
		*Count=m_ModuleInfoCount;
		*(cj_ModuleInfo **)ModuleInfo=Info;
	}
	if(*Count<m_ModuleInfoCount)
	{
		*Count=m_ModuleInfoCount;
		RET_RESULT(SCARD_E_INSUFFICIENT_BUFFER);
	}
	*Count=m_ModuleInfoCount;
	memcpy(Info,m_pModuleInfo,m_ModuleInfoCount*sizeof(cj_ModuleInfo));
	return SCARD_S_SUCCESS;
}

cj_ModuleInfo *CBaseReader::FindModule(uint32_t ModuleID)
{
	cj_ModuleInfo *Info=m_pModuleInfo;
	uint32_t i;

	if(Info==NULL)
		return NULL;
	for(i=0;i<m_ModuleInfoCount;i++,Info++)
	{
		if(Info->ID==ModuleID)
		   break;
	}
	if(i==m_ModuleInfoCount)
		return NULL;
	return Info;
}
	
cj_ModuleInfo *CBaseReader::FindModuleWithMask(uint32_t ModuleID,uint32_t Mask)
{
	cj_ModuleInfo *Info=m_pModuleInfo;
	uint32_t i;

	if(Info==NULL)
		return NULL;
	for(i=0;i<m_ModuleInfoCount;i++,Info++)
	{
		if((Info->ID & Mask)==ModuleID)
		   break;
	}
	if(i==m_ModuleInfoCount)
		return NULL;
	return Info;
}

CJ_RESULT CBaseReader::CtGetModuleInfoFromFile(uint8_t *pData,uint32_t DataLength,cj_ModuleInfo *ModuleInfo,uint32_t *EstimatedUpdateTime)
{
   RET_RESULT(SCARD_E_UNSUPPORTED_FEATURE);
}

CJ_RESULT CBaseReader::CtFreeModuleInfoList(cj_ModuleInfo *pModuleInfo)
{
   RET_RESULT(SCARD_E_UNSUPPORTED_FEATURE);
}

CJ_RESULT CBaseReader::CtIsModuleUpdateRecommended(uint8_t *pData,uint32_t DataLength,uint32_t *EstimatedUpdateTime)
{
   RET_RESULT(SCARD_E_UNSUPPORTED_FEATURE);
}

CJ_RESULT CBaseReader::CtGetActiveModuleID(uint32_t *ID,uint32_t *Result)
{
   RET_RESULT(SCARD_E_UNSUPPORTED_FEATURE);
}

CJ_RESULT CBaseReader::CtGetActivationID(uint32_t *ID,uint32_t *Result)
{
   RET_RESULT(SCARD_E_UNSUPPORTED_FEATURE);
}

CJ_RESULT CBaseReader::CtApplicationData(uint32_t ApplicationID,uint16_t Function,uint8_t *InputData, uint32_t InputLen, uint32_t *Result, uint8_t *ResponseData, uint32_t *ResponseLen, uint8_t *ApplicationError,uint32_t *ApplicationErrorLength)
{
   RET_RESULT(SCARD_E_UNSUPPORTED_FEATURE);
}

CJ_RESULT CBaseReader::CtSelfTest(void)
{
   RET_RESULT(SCARD_E_UNSUPPORTED_FEATURE);
}

CJ_RESULT CBaseReader::CtShowAuth(void)
{
   RET_RESULT(SCARD_E_UNSUPPORTED_FEATURE);
}



CJ_RESULT CBaseReader::CtGetReaderInfo(cj_ReaderInfo *pReaderInfo)
{
	uint32_t min=(pReaderInfo->SizeOfStruct<sizeof(m_ReaderInfo))?pReaderInfo->SizeOfStruct:sizeof(m_ReaderInfo);
	memcpy(pReaderInfo,&m_ReaderInfo,min);
	pReaderInfo->SizeOfStruct=min;
	return SCARD_S_SUCCESS;
}

CJ_RESULT CBaseReader::InstallAndStartIFDHandler()
{
   RET_RESULT(SCARD_E_UNSUPPORTED_FEATURE);
}

CJ_RESULT CBaseReader::StopIFDHandler()
{
   RET_RESULT(SCARD_E_UNSUPPORTED_FEATURE);
}

CJ_RESULT CBaseReader::IntroduceReaderGroups()
{
   RET_RESULT(SCARD_E_UNSUPPORTED_FEATURE);
}

uint16_t CBaseReader::ReaderToHostShort(uint16_t Value)
{
	return HostToReaderShort(Value);
}

uint32_t CBaseReader::ReaderToHostLong(uint32_t Value)
{
	return HostToReaderLong(Value);
}

void CBaseReader::SetChangeInterruptCallback(fctChangeIntCallback ChangeIntCallback,ctxPtr ChangeCallbackCtx)
{
   m_ChangeIntCallback=ChangeIntCallback;
	m_ChangeCallbackCtx=ChangeCallbackCtx;
}

void CBaseReader::SetKeyInterruptCallback(fctKeyIntCallback KeyIntCallback,ctxPtr KeyCallbackCtx)
{
   m_KeyIntCallback=KeyIntCallback;
	m_KeyCallbackCtx=KeyCallbackCtx;
}

int CBaseReader::Write(void *Message,uint32_t len)
{
	int res; 
	if(m_pCommunicator)
	{
		if(len>GetReadersInputBufferSize())
			return CJ_ERR_INTERNAL_BUFFER_OVERFLOW;
	   res=m_pCommunicator->Write(Message,len);
		if(res!=CJ_SUCCESS)
			ConnectionError();
		return res;
	}
	return CJ_ERR_DEVICE_LOST;
}

int CBaseReader::Read(void *Response,uint32_t *ResponseLen)
{
	int res; 
	if(m_pCommunicator)
	{
   	res=m_pCommunicator->Read(Response,ResponseLen);
		if(res!=CJ_SUCCESS)
			ConnectionError();
		return res;
	}
	return CJ_ERR_DEVICE_LOST;
}

void CBaseReader::ConnectionError()
{
	if(m_pCommunicator)
	{
      delete m_pCommunicator;
		m_pCommunicator=NULL;
	}
}

bool CBaseReader::IsConnected()
{
   if(m_pCommunicator==NULL)
		return false;
	else if (m_pCommunicator->IsConnected()==false)
	{
		return false;
	}
	return true;
}

uint32_t CBaseReader::GetEnviroment(const char *Name,uint32_t Default)
{
  /* use platform specific function to get the value */
  return rsct_get_environment(Name, Default);
}

bool CBaseReader::IsNotSet(void *ptr,int len)
{
	uint8_t *p=(uint8_t *)ptr;
	while(len--)
	{
		if(*p++!=0xff)
			return false;
	}
	return true;
}

uint16_t CBaseReader::InversByteOrderShort(uint16_t Value)
{
	return (uint16_t)((Value << 8) | (Value >> 8));
}

uint32_t CBaseReader::InversByteOrderLong(uint32_t Value)
{
	return (uint32_t)((Value << 24) | ((Value << 8) & 0x00ff0000) | ((Value >> 8) & 0x0000ff00) | (Value >> 24));
}

static int GetBits(uint8_t Value)
{
	int Res=0;
	while(Value)
	{
		if(Value & 1)
			Res++;
		Value>>=1;
	}
	return Res;
}

int CBaseReader::check_len(uint8_t *atr,uint32_t buf_len,uint8_t **historical,uint32_t *hist_len)
{
	uint8_t *ptr;
	uint8_t len;
	uint8_t len1;
	uint8_t v;
	int t1=0;

	ptr=atr+1;
	v=*ptr;
	len1=(uint8_t)((*hist_len=(uint8_t)(v & 0x0f))+2);
	len=0;
	do
	{
		v=(uint8_t)GetBits((uint8_t)((*ptr) & 0xf0));
		len+=v;
		if(buf_len>=len && ((*ptr)&0x80))
		{
			ptr+=v;
			if(!t1 && ((*ptr) & 0x0f)!=0)
			{
				len1++;
				t1=1;
			}
		}
		else
		{
			*historical=ptr+v+1;
			break;
		}
	}while(buf_len>len);
	if(t1)
	{
		uint32_t i;
		uint8_t lrc=0;
		for(i=1;i<buf_len;i++)
		{
			lrc^=atr[i];
		}
		if(lrc)
			return 0;
	}
	else if(buf_len!=len+len1)
	{
		if(buf_len==len+len1+1)
		{
			uint32_t i;
			uint8_t lrc=0;
			for(i=1;i<buf_len;i++)
			{
				lrc^=atr[i];
			}
			if(lrc)
				return 0;
			return 1;
		}
		return 2;
	}
	return 1;
}

int CBaseReader::AnalyseATR(bool warm)
{
	uint8_t *ptr=m_ATR;
	int specific;
	int hasTA1;
	int error;
	int protocol=0;
	int TD1=0;
	uint8_t Dx;
   m_PossibleProtocols=0;
	specific=0;
	hasTA1=0;
	protocol=0x01;
	error=0;
   m_ReaderState=SCARD_POWERED;
	m_TA1=0x11; //speed
	m_TC1=0;    //XGT
	m_TC2=10;   //WT
	m_TA3=0x20; //IFSC
	m_TB3=0x4d; //BWI/CWI
	m_TC3=0;    //LRC/CRC

	m_ActiveProtocol=0;


	if(m_ATR_Length > 0 && (m_ATR[0]==0x3b || m_ATR[0]==0x3f))
	{
		if(check_len(m_ATR,m_ATR_Length,&m_Historical,&m_Historical_Length)==1)
		{
			ptr=m_ATR+1;
			Dx=*ptr++;
			if(Dx & 0x10)
			{
				m_TA1=*ptr++;
				hasTA1=1;
			}
			if(Dx & 0x20)
			{
				if(*ptr!=0 && !warm)
				{
					error=1;
				}
				ptr++;
			}
			else
			{
				if(!warm)
					error=1;
			}
			if(Dx & 0x40)
			{
				m_TC1=*ptr++;
			}
			if(Dx & 0x80)
			{
				TD1=Dx=*ptr++;
				protocol=1<<(Dx & 0x0f);
				if((Dx & 0x0f)>1)
					error=1;
				if(Dx & 0x10)
				{
					if(*ptr++ & 0x10)
						error=1;
  					specific=1;
				}
				if(Dx & 0x20)
				{
					error=1;
					ptr++;
				}
				if(Dx & 0x40)
				{
					if((m_TC2=*ptr++)==0)
						error=1;
				}
				if(Dx & 0x80)
				{
					Dx=*ptr++;
					if((Dx & 0x0f)!=1 && (protocol!=1 || (Dx & 0x0e)!=0x0e))
						error=1;
					protocol|=1<<(Dx & 0x0f);
					if((Dx & 0x0f)==1)
					{
						if(Dx & 0x10)
						{
							if((m_TA3=*ptr++)<0x10 || m_TA3==0xff)
								error=1;
						}
						if(Dx & 0x20)
						{
							//check this for emv
							m_TB3=*ptr;
							if(m_ApduNorm==NORM_EMV)
							{
								if(m_TB3>0x45 || 
									(m_TB3&0x0f)>0x05 || 
									((1<<(m_TB3&0x0f))<=m_TC1+1 && 
									 m_TC1!=0xff))
									error=1;
							}
						   ptr++;
						}
						else if(m_ApduNorm==NORM_EMV)
							error=1;
						if(Dx & 0x40)
						{
							m_TC3=*ptr;
							if(m_TC3!=0)
								error=1;
						}
					}
				}
				else if(protocol&2)
					error=1;
			}
		}
		else if(check_len(m_ATR,m_ATR_Length,&m_Historical,&m_Historical_Length)==2)
		{
			error=1;
		}
		else
		{
			error=2;
		}
		if(error==2 || (error==1 && warm))
		{
			IfdPower(SCARD_POWER_DOWN,0,0);
			error=2;
		}
	}
	else
		error=2;
	if(error==0)
	{
		if(specific)
		{
			if((TD1 & 0x0f)==0)
			{
				m_PossibleProtocols=SCARD_PROTOCOL_T0;
				m_ActiveProtocol=SCARD_PROTOCOL_T0;
   		   m_ReaderState=SCARD_SPECIFIC;
			}
			else if((TD1 & 0x0f)==1)
			{
				m_PossibleProtocols=SCARD_PROTOCOL_T1;
				m_ActiveProtocol=SCARD_PROTOCOL_T1;
   		   m_ReaderState=SCARD_SPECIFIC;
			}
			else
			{
				error=1;
			}
		}

		else 
		{
			if(protocol&1)
			{
				m_PossibleProtocols|=SCARD_PROTOCOL_T0;
				m_ReaderState=SCARD_NEGOTIABLE;
			}
			if(protocol&2)
			{
				m_PossibleProtocols|=SCARD_PROTOCOL_T1;
				m_ReaderState=SCARD_NEGOTIABLE;
			}
		}
	}
	else if(m_ATR_Length == 4 && (m_ATR[0]==0x2c || m_ATR[0]==0x92 || m_ATR[0]==0xa2  || m_ATR[0]==0x82))
	{
		m_Historical_Length=0;
		m_PossibleProtocols=SCARD_PROTOCOL_RAW;
		m_ActiveProtocol=SCARD_PROTOCOL_RAW;
	   m_ReaderState=SCARD_SPECIFIC;
		if(m_ATR[0]==0x82)
		{
			if((m_ATR[1]&0x07)>3)
				m_iic_pagesize=1<<((m_ATR[1]&0x07)-3);
			else
				m_iic_pagesize=1;
			if(m_iic_pagesize * (0x0080<<(((m_ATR[1] & 0x78)>>3)-1))>2048)
				m_iic_offset_bytes=2;
			else
				m_iic_offset_bytes=1;
		}
		SetSyncParameters(m_iic_offset_bytes,m_iic_pagesize);
	}
	else if(m_ATR_Length == 4 && (m_ATR[0]==0xff || (m_ATR[0] & 0xf0)==0x80))
	{
		m_Historical_Length=0;
		m_PossibleProtocols=SCARD_PROTOCOL_RAW;
		m_ActiveProtocol=SCARD_PROTOCOL_RAW;
		m_ReaderState=SCARD_NEGOTIABLE;
	}
	else if(m_ATR[0]==0x41)
	{
		m_Historical_Length=0;
		m_Historical=NULL;
		m_PossibleProtocols=SCARD_PROTOCOL_RAW;
		m_ActiveProtocol=SCARD_PROTOCOL_RAW;
	   m_ReaderState=SCARD_SPECIFIC;
	}
	return error;
}

CJ_RESULT CBaseReader::CtSetAPDUNorm(const EApduNorm Norm)
{
	m_ApduNorm=Norm;
	return CJ_SUCCESS;
}

CJ_RESULT CBaseReader::CtSetContrast(EContrast eContrast,uint32_t *Result)
{
   RET_RESULT(SCARD_E_UNSUPPORTED_FEATURE);
}

CJ_RESULT CBaseReader::CtSetBacklight(EBacklight eBacklight,uint32_t *Result)
{
   RET_RESULT(SCARD_E_UNSUPPORTED_FEATURE);
}


CJ_RESULT CBaseReader::CtSetSilentMode(bool boolMode,bool *pboolMode,uint32_t *Result)
{
   RET_RESULT(SCARD_E_UNSUPPORTED_FEATURE);
}

CJ_RESULT CBaseReader::CtGetSilentMode(bool *pboolMode,uint32_t *Result)
{
   RET_RESULT(SCARD_E_UNSUPPORTED_FEATURE);
}
