#include "Platform.h"

#include <stdio.h>
#include <stdarg.h>

#include "Reader.h"
#include "BaseReader.h"
#include "ECAReader.h"
#include "Debug.h"

#ifdef _EXP_CTAPI

extern "C"
{
static CReader *myReader=NULL;
CTAPI_RETURN CT_init(uint16_t,uint16_t)
{
	if(myReader!=0)
		return -127;
#ifdef _EXP_CTAPI_COM
   myReader=new CReader("\\\\.\\com1");
#else
   myReader=new CReader("\\\\.\\cjusbu01");
#endif
	if(myReader->Connect()!=CJ_SUCCESS)
	{
		delete myReader;
		myReader=NULL;
		return -128;
	}
	else
		return 0;

}

CTAPI_RETURN CT_data(uint16_t,uint8_t *dad,uint8_t *sad, uint16_t cmd_len, const uint8_t *cmd, uint16_t *response_len, uint8_t *response)
{
	char Res;
	if(myReader==0)
		return -128;
	

	if (*dad==1 &&
		cmd_len > 5 &&
		cmd[0]==0xFF &&
		cmd[1]==0x80 &&
		cmd[2]==0x04 &&
		cmd[3]==0x00 &&
		cmd_len > 13 &&
		*response_len >= 4)
	{
		uint32_t nRspLen= *response_len - 4;
		uint32_t nIoCtrl= *((uint32_t*)&cmd[5]);
		uint32_t nStat=0;

		nStat=myReader->IfdIoControl (nIoCtrl,(BYTE*)(cmd+13),cmd_len-13,response+4,&nRspLen);
		*((uint32_t*)response)=nStat;
		*response_len=(USHORT)(nRspLen+sizeof (nStat));
		Res=0;
	}
	else
	{
		Res=myReader->CtData(dad,sad,cmd_len,cmd,response_len,response);
	};

	switch(Res)
	{
	case 0:
	case -1:
	case -11:
		break;
	default:
		delete myReader;
		myReader=NULL;
	}
	return Res;
}

CTAPI_RETURN CT_close(uint16_t)
{
	if(myReader==0)
		return -128;
	delete myReader;
	myReader=NULL;
	return 0;

}
}
#endif

CReader::CReader(char *cDeviceName)
{
	CReaderConstructor((const char *)cDeviceName);
}

CReader::CReader(const char *cDeviceName)
{
	CReaderConstructor(cDeviceName);
}

void CReader::CReaderConstructor(const char *cDeviceName)
{
	m_Reader=NULL;
	CritSec=new CRSCTCriticalSection();
	m_cDeviceName=strdup(cDeviceName);
       //we allocate 10 bytes more because we modify the device name later in rsct_platform_create_com
       //m_cDeviceName=strdup(cDeviceName);
       m_cDeviceName=(char *) calloc(1,strlen(cDeviceName)+10);
       m_cDeviceName=strcpy(m_cDeviceName, cDeviceName);
}

CJ_RESULT CReader::Connect()
{
	// ::MessageBox (0,L"CReader::Connect(1)",L"cjeca32.dll",MB_ICONSTOP);
	CJ_RESULT Res;
	CBaseCommunication *com=NULL;
	CritSec->Enter();

        com=rsct_platform_create_com(m_cDeviceName,this);
	if(com)
	{ 
		// ::MessageBox (0,L"CReader::Connect(3)",L"cjeca32.dll",MB_ICONSTOP);
 
		if(com->Open())
		{
			// ::MessageBox (0,L"CReader::Connect(4)",L"cjeca32.dll",MB_ICONSTOP);

			m_Reader=com->BuildReaderObject();
			if(m_Reader)
			{
				// ::MessageBox (0,L"CReader::Connect(5a)",L"cjeca32.dll",MB_ICONSTOP);

				Res=m_Reader->PostCreate();
				// ::MessageBox (0,L"CReader::Connect(6a)",L"cjeca32.dll",MB_ICONSTOP);
				CheckcJResult(Res);
				// ::MessageBox (0,L"CReader::Connect(7a)",L"cjeca32.dll",MB_ICONSTOP);
				CritSec->Leave();

				// ::MessageBox (0,L"CReader::Connect(8a)",L"cjeca32.dll",MB_ICONSTOP);

				return Res;
			}
			else
			{
//				::MessageBox (0,L"CReader::Connect(25)",L"cjeca32.dll",MB_ICONSTOP);

				CritSec->Leave();
				return CJ_ERR_DEVICE_LOST;
			}
		}
		else
		{
//			::MessageBox (0,L"CReader::Connect(50)",L"cjeca32.dll",MB_ICONSTOP);

			CritSec->Leave();
			return CJ_ERR_OPENING_DEVICE;
		}
	}
	else
	{
//		::MessageBox (0,L"CReader::Connect(100)",L"cjeca32.dll",MB_ICONSTOP);

		CritSec->Leave();
		return CJ_ERR_OPENING_DEVICE;
	}
}



CJ_RESULT CReader::CreateVirtualReaderObject(const char *cReaderName)
{
  if (strcasecmp(cReaderName, "ecom(a)")==0)
    {
      m_Reader=new CECAReader(this, NULL);
      return CJ_SUCCESS;
    }
  return CJ_ERR_OPENING_DEVICE;
}

CJ_RESULT CReader::Disonnect()
{
	CritSec->Enter();
	if(m_Reader)
	{
		m_Reader->IfdPower(SCARD_POWER_DOWN,0,0);
		m_Reader->Unconnect();
	   delete m_Reader;
	}
	m_Reader=NULL;
  	CritSec->Leave();
	return CJ_SUCCESS;
}


CReader::~CReader()
{
	Disonnect();
	free(m_cDeviceName);
	delete CritSec;
}

char CReader::CtData(uint8_t *dad,uint8_t *sad, uint16_t cmd_len, const uint8_t *cmd, uint16_t *response_len, uint8_t *response)
{
	char res;
	if(m_Reader==NULL)
		return -128;
	CritSec->Enter();
	Debug.Out(m_cDeviceName,DEBUG_MASK_INPUT,"CtData Cmd:",(void *)cmd,cmd_len);
	res=m_Reader->CtData(sad,dad,cmd,cmd_len,response,response_len);
	Debug.Out(m_cDeviceName,DEBUG_MASK_OUTPUT,"CtData Rsp",(void *)response,*response_len);
	switch(res)
	{
	case 0:
	case -1:
	case -11:
		break;
	default:
		m_Reader->Unconnect();
		delete m_Reader;
		m_Reader=NULL;
	}
	CritSec->Leave();
	return res;
}


RSCT_IFD_RESULT CReader::IfdPower(uint32_t Mode,uint8_t *ATR,uint32_t *ATR_Length)
{
	RSCT_IFD_RESULT res;
	if(m_Reader==NULL)
	{
		*ATR_Length=0;
		return STATUS_DEVICE_NOT_CONNECTED;
	}
	CritSec->Enter();
	res=m_Reader->IfdPower(Mode,ATR,ATR_Length);
	switch(res)
	{
	case STATUS_DEVICE_NOT_CONNECTED:
/*		delete m_Reader;
		m_Reader=NULL;*/
		break;
	default:;
	}
	CritSec->Leave();
	return res;
}

RSCT_IFD_RESULT CReader::IfdSetProtocol(uint32_t *Protocol)
{
	RSCT_IFD_RESULT res;
	if(m_Reader==NULL)
	{
		*Protocol=0;
		return STATUS_DEVICE_NOT_CONNECTED;
	}
	CritSec->Enter();
	res=m_Reader->IfdSetProtocol(Protocol);
	switch(res)
	{
	case STATUS_DEVICE_NOT_CONNECTED:
		m_Reader->Unconnect();
		delete m_Reader;
		m_Reader=NULL;
		break;
	default:;
	}
	CritSec->Leave();
	return res;
}

RSCT_IFD_RESULT CReader::IfdGetState(uint32_t *State)
{
	RSCT_IFD_RESULT res;
	if(m_Reader==NULL)
	{
		*State=0;
		return STATUS_DEVICE_NOT_CONNECTED;
	}
	CritSec->Enter();
	res=m_Reader->IfdGetState(State);
	switch(res)
	{
	case STATUS_DEVICE_NOT_CONNECTED:
		m_Reader->Unconnect();
		delete m_Reader;
		m_Reader=NULL;
		break;
	default:;
	}
	CritSec->Leave();
	return res;
}

RSCT_IFD_RESULT CReader::IfdSetAttribute(const uint8_t *Input,uint32_t InputLength)
{
	RSCT_IFD_RESULT res;
	if(m_Reader==NULL)
	{
		return STATUS_DEVICE_NOT_CONNECTED;
	}
	CritSec->Enter();
	res=m_Reader->IfdSetAttribute(Input,InputLength);
	switch(res)
	{
	case STATUS_DEVICE_NOT_CONNECTED:
		m_Reader->Unconnect();
		delete m_Reader;
		m_Reader=NULL;
		break;
	default:;
	}
	CritSec->Leave();
	return res;
}

RSCT_IFD_RESULT CReader::IfdGetAttribute(uint32_t Tag,uint8_t *Attribute,uint32_t *AttributeLength)
{
	RSCT_IFD_RESULT res;
	if(m_Reader==NULL)
	{
		*Attribute=0;
		return STATUS_DEVICE_NOT_CONNECTED;
	}
	CritSec->Enter();
	res=m_Reader->IfdGetAttribute(Tag,Attribute,AttributeLength);
	switch(res)
	{
	case STATUS_DEVICE_NOT_CONNECTED:
		m_Reader->Unconnect();
		delete m_Reader;
		m_Reader=NULL;
		break;
	default:;
	}
	CritSec->Leave();
	return res;
}

RSCT_IFD_RESULT CReader::IfdSwallow()
{
	RSCT_IFD_RESULT res;
	if(m_Reader==NULL)
	{
		return STATUS_DEVICE_NOT_CONNECTED;
	}
	CritSec->Enter();
	res=m_Reader->IfdSwallow();
	switch(res)
	{
	case STATUS_DEVICE_NOT_CONNECTED:
		m_Reader->Unconnect();
		delete m_Reader;
		m_Reader=NULL;
		break;
	default:;
	}
	CritSec->Leave();
	return res;
}

RSCT_IFD_RESULT CReader::IfdEject()
{
	RSCT_IFD_RESULT res;
	if(m_Reader==NULL)
	{
		return STATUS_DEVICE_NOT_CONNECTED;
	}
	CritSec->Enter();
	res=m_Reader->IfdEject();
	switch(res)
	{
	case STATUS_DEVICE_NOT_CONNECTED:
		m_Reader->Unconnect();
		delete m_Reader;
		m_Reader=NULL;
		break;
	default:;
	}
	CritSec->Leave();
	return res;
}

RSCT_IFD_RESULT CReader::IfdVendor(uint32_t IoCtrlCode,uint8_t *Input,uint32_t InputLength,uint8_t *Output,uint32_t *OutputLength)
{
	RSCT_IFD_RESULT res;
	if(m_Reader==NULL)
	{
		*OutputLength=0;
		return STATUS_DEVICE_NOT_CONNECTED;
	}
	CritSec->Enter();
	res=m_Reader->IfdVendor(IoCtrlCode,Input,InputLength,Output,OutputLength);
	switch(res)
	{
	case STATUS_DEVICE_NOT_CONNECTED:
		m_Reader->Unconnect();
		delete m_Reader;
		m_Reader=NULL;
		break;
	default:;
	}
	CritSec->Leave();
	return res;
}

RSCT_IFD_RESULT CReader::IfdTransmit(const uint8_t *cmd, uint16_t cmd_len,uint8_t *response,uint16_t *response_len)
{
	RSCT_IFD_RESULT res;
	if(m_Reader==NULL)
	{
		*response_len=0;
		return STATUS_DEVICE_NOT_CONNECTED;
	}
	CritSec->Enter();
	res=m_Reader->IfdTransmit(cmd,cmd_len,response,response_len);
	switch(res)
	{
	case STATUS_DEVICE_NOT_CONNECTED:
		m_Reader->Unconnect();
		delete m_Reader;
		m_Reader=NULL;
		break;
	default:;
	}
	CritSec->Leave();
	return res;
}

RSCT_IFD_RESULT CReader::IfdIoControl(uint32_t IoCtrlCode,uint8_t *Input,uint32_t InputLength,uint8_t *Output,uint32_t *OutputLength)
{
	RSCT_IFD_RESULT res;
	if(m_Reader==NULL)
	{
		*OutputLength=0;
		return STATUS_DEVICE_NOT_CONNECTED;
	}
	CritSec->Enter();
	res=m_Reader->IfdIoControl(IoCtrlCode,Input,InputLength,Output,OutputLength);
	switch(res)
	{
	case STATUS_DEVICE_NOT_CONNECTED:
		m_Reader->Unconnect();
		delete m_Reader;
		m_Reader=NULL;
		break;
	default:;
	}
	CritSec->Leave();
	return res;
}


	
void CReader::CheckcJResult(CJ_RESULT Result)
{
	switch(Result)
	{
	case CJ_ERR_DEVICE_LOST:
	case CJ_ERR_CONNECT_TIMEOUT:
		m_Reader->Unconnect();
      delete m_Reader;
		m_Reader=NULL;
		break;
	default:;
	}
}

CJ_RESULT CReader::CtLoadModule(uint8_t *pData,uint32_t DataLength,uint8_t *pSgn,uint32_t SgnLength,uint32_t *Result)
{
	CJ_RESULT res;
	if(m_Reader==NULL)
	{
		return CJ_ERR_DEVICE_LOST;
	}
	CritSec->Enter();
	res=m_Reader->CtLoadModule(pData,DataLength,pSgn,SgnLength,Result);
	CheckcJResult(res);
	CritSec->Leave();
	return res;
}

CJ_RESULT CReader::CtDeleteModule(uint32_t ModuleID,uint32_t *Result)
{
	CJ_RESULT res;
	if(m_Reader==NULL)
	{
		return CJ_ERR_DEVICE_LOST;
	}
	CritSec->Enter();
	res=m_Reader->CtDeleteModule(ModuleID,Result);
	CheckcJResult(res);
	CritSec->Leave();
	return res;
}

CJ_RESULT CReader::CtSelfTest(void)
{
	CJ_RESULT res;
	if(m_Reader==NULL)
	{
		return CJ_ERR_DEVICE_LOST;
	}
	CritSec->Enter();
	res=m_Reader->CtSelfTest();
	CheckcJResult(res);
	CritSec->Leave();
	return res;
}

CJ_RESULT CReader::CtShowAuth(void)
{
	CJ_RESULT res;
	if(m_Reader==NULL)
	{
		return CJ_ERR_DEVICE_LOST;
	}
	CritSec->Enter();
	res=m_Reader->CtShowAuth();
	CheckcJResult(res);
	CritSec->Leave();
	return res;
}

CJ_RESULT CReader::CtActivateModule(uint32_t ModuleID,uint32_t *Result)
{
	CJ_RESULT res;
	if(m_Reader==NULL)
	{
		return CJ_ERR_DEVICE_LOST;
	}
	CritSec->Enter();
	res=m_Reader->CtActivateModule(ModuleID,Result);
	CheckcJResult(res);
	CritSec->Leave();
	return res;
}

CJ_RESULT CReader::CtDeactivateModule(uint32_t ModuleID,uint32_t *Result)
{
	CJ_RESULT res;
	if(m_Reader==NULL)
	{
		return CJ_ERR_DEVICE_LOST;
	}
	CritSec->Enter();
	res=m_Reader->CtDeactivateModule(ModuleID,Result);
	CheckcJResult(res);
	CritSec->Leave();
	return res;
}

CJ_RESULT CReader::CtSetSilentMode(bool boolMode,bool *pboolMode,uint32_t *Result)
{
	CJ_RESULT res;
	if(m_Reader==NULL)
	{
		return CJ_ERR_DEVICE_LOST;
	}
	CritSec->Enter();
	res=m_Reader->CtSetSilentMode(boolMode,pboolMode,Result);
	CheckcJResult(res);
	CritSec->Leave();
	return res;
}

CJ_RESULT CReader::CtGetSilentMode(bool *pboolMode,uint32_t *Result)
{
	CJ_RESULT res;
	if(m_Reader==NULL)
	{
		return CJ_ERR_DEVICE_LOST;
	}
	CritSec->Enter();
	res=m_Reader->CtGetSilentMode(pboolMode,Result);
	CheckcJResult(res);
	CritSec->Leave();
	return res;
}

CJ_RESULT CReader::CtSetModulestoreInfo(uint8_t *Info,uint8_t InfoLength)
{
	CJ_RESULT res;
	if(m_Reader==NULL)
	{
		return CJ_ERR_DEVICE_LOST;
	}
	CritSec->Enter();
	res=m_Reader->CtSetModulestoreInfo(Info,InfoLength);
	CheckcJResult(res);
	CritSec->Leave();
	return res;
}

CJ_RESULT CReader::CtGetModulestoreInfo(uint8_t *Info,uint8_t *InfoLength)
{
	CJ_RESULT res;
	if(m_Reader==NULL)
	{
		return CJ_ERR_DEVICE_LOST;
	}
	CritSec->Enter();
	res=m_Reader->CtGetModulestoreInfo(Info,InfoLength);
	CheckcJResult(res);
	CritSec->Leave();
	return res;
}

CJ_RESULT CReader::CtDeleteALLModules(uint32_t *Result)
{
	CJ_RESULT res;
	if(m_Reader==NULL)
	{
		return CJ_ERR_DEVICE_LOST;
	}
	CritSec->Enter();
	res=m_Reader->CtDeleteALLModules(Result);
	CheckcJResult(res);
	CritSec->Leave();
	return res;
}

CJ_RESULT CReader::CtListModules(uint32_t *Count,cj_ModuleInfo *ModuleInfo)
{
	CJ_RESULT res;
	if(m_Reader==NULL)
	{
		*Count=0;
		return CJ_ERR_DEVICE_LOST;
	}
	CritSec->Enter();
	res=m_Reader->CtListModules(Count,ModuleInfo);
	CheckcJResult(res);
	CritSec->Leave();
	return res;
}

CJ_RESULT CReader::CtGetModuleInfoFromFile(uint8_t *pData,uint32_t DataLength,cj_ModuleInfo *ModuleInfo,uint32_t *EstimatedUpdateTime)
{
	CJ_RESULT res;
	if(m_Reader==NULL)
	{
		*EstimatedUpdateTime=0;
		return CJ_ERR_DEVICE_LOST;
	}
	CritSec->Enter();
	res=m_Reader->CtGetModuleInfoFromFile(pData,DataLength,ModuleInfo,EstimatedUpdateTime);
	CheckcJResult(res);
	CritSec->Leave();
	return res;
}

CJ_RESULT CReader::CtFreeModuleInfoList(cj_ModuleInfo *pModuleInfo)
{
	CJ_RESULT res;
	if(m_Reader==NULL)
	{
		return CJ_ERR_DEVICE_LOST;
	}
	CritSec->Enter();
	res=m_Reader->CtFreeModuleInfoList(pModuleInfo);
	CheckcJResult(res);
	CritSec->Leave();
	return res;
}
	
CJ_RESULT CReader::CtKeyUpdate(uint8_t *pData,uint32_t DataLength,uint32_t *Result)
{
	CJ_RESULT res;
	if(m_Reader==NULL)
	{
		return CJ_ERR_DEVICE_LOST;
	}
	CritSec->Enter();
	res=m_Reader->CtKeyUpdate(pData,DataLength,Result);
	CheckcJResult(res);
	CritSec->Leave();
	return res;
}

CJ_RESULT CReader::CtIsKeyUpdateRecommended(uint8_t *pData,uint32_t DataLength,uint32_t *EstimatedUpdateTime)
{
	CJ_RESULT res;
	if(m_Reader==NULL)
	{
		*EstimatedUpdateTime=0;
		return CJ_ERR_DEVICE_LOST;
	}
	CritSec->Enter();
	res=m_Reader->CtIsKeyUpdateRecommended(pData,DataLength,EstimatedUpdateTime);
	CheckcJResult(res);
	CritSec->Leave();
	return res;
}

CJ_RESULT CReader::CtIsModuleUpdateRecommended(uint8_t *pData,uint32_t DataLength,uint32_t *EstimatedUpdateTime)
{
	CJ_RESULT res;
	if(m_Reader==NULL)
	{
		*EstimatedUpdateTime=0;
		return CJ_ERR_DEVICE_LOST;
	}
	CritSec->Enter();
	res=m_Reader->CtIsModuleUpdateRecommended(pData,DataLength,EstimatedUpdateTime);
	CheckcJResult(res);
	CritSec->Leave();
	return res;
}

CJ_RESULT CReader::CtGetActiveModuleID(uint32_t *ID,uint32_t *Result)
{
	CJ_RESULT res;
	if(m_Reader==NULL)
	{
		*ID=0;
		return CJ_ERR_DEVICE_LOST;
	}
	CritSec->Enter();
	res=m_Reader->CtGetActiveModuleID(ID,Result);
	CheckcJResult(res);
	CritSec->Leave();
	return res;
}

CJ_RESULT CReader::CtGetActivationID(uint32_t *ID,uint32_t *Result)
{
	CJ_RESULT res;
	if(m_Reader==NULL)
	{
		*ID=0;
		return CJ_ERR_DEVICE_LOST;
	}
	CritSec->Enter();
	res=m_Reader->CtGetActivationID(ID,Result);
	CheckcJResult(res);
	CritSec->Leave();
	return res;
}

CJ_RESULT CReader::CtApplicationData(uint32_t ApplicationID,uint16_t Function,uint8_t *InputData, uint32_t InputLen, uint32_t *Result, uint8_t *ResponseData, uint32_t *ResponseLen)
{
	CJ_RESULT res;
	if(m_Reader==NULL)
	{
		*ResponseLen=0;
		*Result=0;
		return CJ_ERR_DEVICE_LOST;
	}
	CritSec->Enter();
	res=m_Reader->CtApplicationData(ApplicationID,Function,InputData,InputLen,Result,ResponseData,ResponseLen,0,0);
	CheckcJResult(res);
	CritSec->Leave();
	return res;
}

CJ_RESULT CReader::CtApplicationDataEx(uint32_t ApplicationID,uint16_t Function,uint8_t *InputData, uint32_t InputLen, uint32_t *Result, uint8_t *ResponseData, uint32_t *ResponseLen, uint8_t *ApplicationError,uint32_t *ApplicationErrorLength)
{
	CJ_RESULT res;
	if(m_Reader==NULL)
	{
		*ResponseLen=0;
		*Result=0;
		return CJ_ERR_DEVICE_LOST;
	}
	CritSec->Enter();
	res=m_Reader->CtApplicationData(ApplicationID,Function,InputData,InputLen,Result,ResponseData,ResponseLen,ApplicationError,ApplicationErrorLength);
	CheckcJResult(res);
	CritSec->Leave();
	return res;
}

CJ_RESULT CReader::CtSetContrast(EContrast eContrast,uint32_t *Result)
{
	CJ_RESULT res;
	if(m_Reader==NULL)
	{
		*Result=0;
		return CJ_ERR_DEVICE_LOST;
	}
	CritSec->Enter();
	res=m_Reader->CtSetContrast(eContrast,Result);
	CheckcJResult(res);
	CritSec->Leave();
	return res;
}

CJ_RESULT CReader::CtSetBacklight(EBacklight eBacklight,uint32_t *Result)
{
	CJ_RESULT res;
	if(m_Reader==NULL)
	{
		*Result=0;
		return CJ_ERR_DEVICE_LOST;
	}
	CritSec->Enter();
	res=m_Reader->CtSetBacklight(eBacklight,Result);
	CheckcJResult(res);
	CritSec->Leave();
	return res;
}


CJ_RESULT CReader::InstallAndStartIFDHandler()
{
	CJ_RESULT res;
	if(m_Reader==NULL)
	{
		return CJ_ERR_DEVICE_LOST;
	}
	CritSec->Enter();
	res=m_Reader->InstallAndStartIFDHandler();
	CheckcJResult(res);
	CritSec->Leave();
	return res;
}
	
CJ_RESULT CReader::StopIFDHandler()
{
	CJ_RESULT res;
	if(m_Reader==NULL)
	{
		return CJ_ERR_DEVICE_LOST;
	}
	CritSec->Enter();
	res=m_Reader->StopIFDHandler();
	CheckcJResult(res);
	CritSec->Leave();
	return res;
}

CJ_RESULT CReader::IntroduceReaderGroups()
{
	CJ_RESULT res;
	if(m_Reader==NULL)
	{
		return CJ_ERR_DEVICE_LOST;
	}
	CritSec->Enter();
	res=m_Reader->IntroduceReaderGroups();
	CheckcJResult(res);
	CritSec->Leave();
	return res;
}



CJ_RESULT CReader::CtGetReaderInfo(cj_ReaderInfo *pReaderInfo)
{
	CJ_RESULT res;
	if(m_Reader==NULL)
	{
		return CJ_ERR_DEVICE_LOST;
	}
	CritSec->Enter();
	res=m_Reader->CtGetReaderInfo(pReaderInfo);
	CheckcJResult(res);
	CritSec->Leave();
	return res;
}

void CReader::SetChangeInterruptCallback(fctChangeIntCallback ChangeIntCallback,ctxPtr ChangeOwner)
{
	CritSec->Enter();
	m_Reader->SetChangeInterruptCallback(ChangeIntCallback,ChangeOwner);
	CritSec->Leave();
}

void CReader::SetKeyInterruptCallback(fctKeyIntCallback KeyIntCallback,ctxPtr KeyOwner)
{
	CritSec->Enter();
	m_Reader->SetKeyInterruptCallback(KeyIntCallback,KeyOwner);
	CritSec->Leave();
}

CJ_RESULT CReader::CtSetAPDUNorm(const EApduNorm Norm)
{
	CJ_RESULT Res;
	CritSec->Enter();
	Res=m_Reader->CtSetAPDUNorm(Norm);
	CritSec->Leave();
	return Res;
}

void CReader::DebugResult(const char *format, ...)
{
#ifndef RSCT_NO_VARGS
	va_list args;
	char dbg_buffer[256];

	va_start(args, format);
	vsnprintf(dbg_buffer, sizeof(dbg_buffer)-1, format, args);
	dbg_buffer[sizeof(dbg_buffer)-1] = 0; 
	DebugLeveled(DEBUG_MASK_RESULTS, "Functionresult: %s", dbg_buffer);
	va_end(args);
#endif
}

void CReader::DebugErrorSW1SW2(const char *format, ...)
{
#ifndef RSCT_NO_VARGS
	va_list args;
	char dbg_buffer[256];

	va_start(args, format);
	vsnprintf(dbg_buffer, sizeof(dbg_buffer)-1, format, args);
	dbg_buffer[sizeof(dbg_buffer)-1] = 0; 
	DebugLeveled(DEBUG_MASK_TRANSLATION, "DLL sets SW1SW2: %s", dbg_buffer);
	va_end(args);
#endif
}

void CReader::DebugLeveled(uint32_t Mask, const char *format, ...)
{
#ifndef RSCT_NO_VARGS
	va_list args;
	char dbg_buffer[256];

	va_start(args, format);
	vsnprintf(dbg_buffer, sizeof(dbg_buffer)-1, format, args);
	dbg_buffer[sizeof(dbg_buffer)-1] = 0; 
	Debug.Out(m_cDeviceName,Mask,dbg_buffer,0,0);
	va_end(args);
#endif
}



