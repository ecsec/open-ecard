#include "Platform.h"
#include <time.h>
#include <stdio.h>
#include "EC30Reader.h"
#include "Reader.h"

#include "eca_defines.h"
#include "eca_module_errors.h"

#define base CCCIDReader


#ifdef UNDER_CE

time_t time( time_t *inTT ) 
{ 
	SYSTEMTIME		sysTimeStruct; 
	FILETIME		fTime; 
	ULARGE_INTEGER	int64time; 
	time_t			locTT = 0; 

	if (inTT==NULL)
	{ 
		inTT = &locTT; 
	} 

	GetSystemTime(&sysTimeStruct); 
	if (SystemTimeToFileTime(&sysTimeStruct, &fTime))
	{ 
		memcpy( &int64time, &fTime, sizeof( FILETIME ) ); 
		int64time.QuadPart -= 0x19db1ded53e8000; 
		int64time.QuadPart /= 10000000; 
		*inTT = (time_t)int64time.QuadPart; 
	} 

	return *inTT; 
} 

tm _tmc;
tm * __cdecl localtime(const time_t *)
{
	SYSTEMTIME		sysTimeStruct; 
	GetSystemTime(&sysTimeStruct); 
	
	_tmc.tm_sec=sysTimeStruct.wSecond;
	_tmc.tm_min=sysTimeStruct.wMinute;
	_tmc.tm_hour=sysTimeStruct.wHour;
	_tmc.tm_mday=sysTimeStruct.wDayOfWeek;
	_tmc.tm_mon=sysTimeStruct.wMonth;
	_tmc.tm_year=sysTimeStruct.wYear;
	_tmc.tm_wday=0;
	_tmc.tm_yday=0;
	_tmc.tm_isdst=0;
	

	return &_tmc;
};
 
#endif

CEC30Reader::CEC30Reader(CReader *Owner,CBaseCommunication *Communicator)
				:base(Owner,Communicator)
  ,m_pApplicationResponse(NULL)
  ,m_nApplicationResponseLength(0)
{
	SecoderBufferLen=0;
	SecoderBuffer=NULL;
}

CEC30Reader::~CEC30Reader(void)
{
	if(m_pApplicationResponse!=NULL)
		delete m_pApplicationResponse;
	if(SecoderBufferLen)
		delete[] SecoderBuffer;
}

CJ_RESULT CEC30Reader::PostCreate()
{
	CJ_RESULT Res;
	if((Res=base::PostCreate())==CJ_SUCCESS)
   	if(SetReaderConstants())
	   	Res=BuildReaderInfo();
	return Res;
}

int CEC30Reader::Escape(uint32_t ApplicationID,uint16_t Function,uint8_t *InputData, uint32_t InputLen, uint32_t *Result, uint8_t *ResponseData, uint32_t *ResponseLen)
{
	int Res;
	uint32_t nResult;

	CCID_Message Message;
	CCID_Response Response;
	memset(&Message,0,sizeof(Message));
	Message.bMessageType=PC_TO_RDR_ESCAPE;
	Message.Data.Escape.Reader.EC30.dwApplication=HostToReaderLong(ApplicationID);
	Message.Data.Escape.Reader.EC30.wFunction=HostToReaderShort(Function);
	Message.dwLength=6+InputLen;
	if(InputLen>GetReadersInputBufferSize()-16)
		return CJ_ERR_INTERNAL_BUFFER_OVERFLOW;
	if(InputLen>0)
	   memcpy(&Message.Data.Escape.Reader.EC30.Data,InputData,InputLen);
   if((Res=Transfer(&Message,&Response))!=CJ_SUCCESS)
	{
		if(ResponseLen!=NULL)
			*ResponseLen=0;
		return Res;
	}
	if(Response.bMessageType!=RDR_TO_PC_ESCAPE)
	{
		if(ResponseLen!=NULL)
			*ResponseLen=0;
		delete m_pCommunicator;
		m_pCommunicator=NULL;
		return CJ_ERR_DEVICE_LOST;
	}
   nResult=ReaderToHostLong(Response.Data.Escape.Result);
   if(Result) 
	   *Result=nResult;
	if(ResponseLen)
	{
		if(Response.dwLength>*ResponseLen+4)
		{
			if(ResponseData==NULL)
			{
       		*ResponseLen=Response.dwLength-4;
			}
			else
			{
      		*ResponseLen=0;
	   		delete m_pCommunicator;
		   	m_pCommunicator=NULL;
			}
			return CJ_ERR_RBUFFER_TO_SMALL;
		}
		*ResponseLen=Response.dwLength-4;
		if(ResponseData)
		    memcpy(ResponseData,Response.Data.Escape.Function.abData,*ResponseLen);
	}
	else if(Response.dwLength!=4)
	{
		delete m_pCommunicator;
		m_pCommunicator=NULL;
		return CJ_ERR_RBUFFER_TO_SMALL;
	}
	if(nResult!=0)
	{
		if(ResponseLen!=NULL)
			*ResponseLen=0;
		return CJ_ERR_CHECK_RESULT;
	}
	return CJ_SUCCESS;
}

uint32_t CEC30Reader::GetReadersInputBufferSize()
{
	return 1024;
}


CJ_RESULT CEC30Reader::CtApplicationData(uint32_t ApplicationID,uint16_t Function,uint8_t *InputData, uint32_t InputLen, uint32_t *Result, uint8_t *ResponseData, uint32_t *ResponseLen, uint8_t *ApplicationError,uint32_t *ApplicationErrorLength)
{
   int Res;
	uint32_t Len;
	uint16_t wLenRsp=0;
	uint16_t wLenErr=0;
	if(ResponseLen!=0)
		wLenRsp=(uint16_t)*ResponseLen;
	if(ApplicationErrorLength!=NULL)
		wLenErr=(uint16_t)*ApplicationErrorLength;
	if(m_nApplicationResponseLength<(uint32_t)wLenRsp+wLenErr+4)
	{
		if(m_pApplicationResponse!=NULL)
			delete m_pApplicationResponse;
		m_nApplicationResponseLength=wLenRsp+wLenErr+4+1024;
      m_pApplicationResponse=new uint8_t[m_nApplicationResponseLength];
	}
	Len=4+wLenRsp+wLenErr;


	if((Res=Escape(ApplicationID,Function,InputData,InputLen,Result,m_pApplicationResponse,&Len)))
	{
		if(ResponseLen)
			*ResponseLen=0;
		if(ApplicationErrorLength)
			*ApplicationErrorLength=0;
      return Res;
	}
	memcpy(&wLenRsp,m_pApplicationResponse,sizeof(wLenRsp));
	wLenRsp=ReaderToHostShort(wLenRsp);
	memcpy(&wLenErr,m_pApplicationResponse+2,sizeof(wLenErr));
	wLenErr=ReaderToHostShort(wLenErr);
	if(ApplicationErrorLength)
	{
		if(wLenErr>*ApplicationErrorLength)
		{
			*ResponseLen=0;
			*ApplicationErrorLength=0;
			return CJ_ERR_RBUFFER_TO_SMALL;
		}
		*ApplicationErrorLength=wLenErr;
		if(wLenErr>0)
			memcpy(ApplicationError,m_pApplicationResponse+4+wLenRsp,wLenErr);
	}

   if(ResponseLen)
	{
		if(wLenRsp>*ResponseLen)
		{
			*ResponseLen=0;
			*ApplicationErrorLength=0;
			return CJ_ERR_RBUFFER_TO_SMALL;
		}
		*ResponseLen=wLenRsp;
		if(wLenRsp>0)
			memcpy(ResponseData,m_pApplicationResponse+4,wLenRsp);
	}
	return CJ_SUCCESS;
}


CJ_RESULT CEC30Reader::GetReaderInfo(cjeca_Info *Info)
{
	CJ_RESULT Result;
	uint32_t len=sizeof(cjeca_Info);
uint32_t Res;

	memset(Info,0xff,sizeof(cjeca_Info));
   if((Result=Escape(MODULE_ID_KERNEL,CCID_ESCAPE_GET_INFO,0,0,&Res,(uint8_t *)Info,&len))!=CJ_SUCCESS)
		m_Owner->DebugLeveled(DEBUG_MASK_COMMUNICATION_ERROR,"Can't get ReaderInfo");
	else
   	Info->ActiveApplication=ReaderToHostLong(Info->ActiveApplication);
   return Result;

}

CJ_RESULT CEC30Reader::GetKeyInfo(tKeyInfo *Keys,uint32_t len)
{
	uint32_t Res;
	memset(Keys,0xff,4);
   if((Res=Escape(MODULE_ID_KERNEL,CCID_ESCAPE_GET_KEYINFO,0,0,&Res,(uint8_t *)Keys,&len))!=CJ_SUCCESS)
		m_Owner->DebugLeveled(DEBUG_MASK_COMMUNICATION_ERROR,"Can't get Key - Info");
	return Res;
}

CJ_RESULT CEC30Reader::GetSecoderInfo(tSecoderInfo *Info,uint32_t len)
{
	uint32_t Res;
	memset(Info,0xff,6);
   if((Res=Escape(MODULE_ID_KERNEL,CCID_ESCAPE_GET_SECODERINFO,0,0,&Res,(uint8_t *)Info,&len))!=CJ_SUCCESS)
		m_Owner->DebugLeveled(DEBUG_MASK_COMMUNICATION_ERROR,"Can't get Secoder - Info");
	return Res;
}

CJ_RESULT CEC30Reader::BuildReaderInfo()
{
	cjeca_Info Info;
	CJ_RESULT Res;
	uint32_t Mask;

	memset(&m_ReaderInfo,0,sizeof(m_ReaderInfo));

	m_ReaderInfo.SizeOfStruct=sizeof(m_ReaderInfo);
	if((Res=GetReaderInfo(&Info))!=CJ_SUCCESS)
		return Res;
	if(m_pCommunicator==NULL)
		return CJ_ERR_DEVICE_LOST;
	m_pCommunicator->SetCommunicationString(&m_ReaderInfo);

	m_ReaderInfo.ContentsMask |=	RSCT_READER_MASK_HARDWARE |
										RSCT_READER_MASK_VERSION |
										RSCT_READER_MASK_HARDWARE_VERSION |
										RSCT_READER_MASK_FLASH_SIZE |
										RSCT_READER_MASK_HEAP_SIZE |
										RSCT_READER_MASK_SERIALNUMBER | 
										RSCT_READER_MASK_PRODUCTION_DATE |	
										RSCT_READER_MASK_TEST_DATE |
										RSCT_READER_MASK_COMMISSIONING_DATE |
										RSCT_READER_MASK_HW_STRING;

	Mask = ~Info.ReaderConst.MaskOption;	
	m_ReaderInfo.HardwareMask=RSCT_READER_HARDWARE_MASK_ICC1 |
									  RSCT_READER_HARDWARE_MASK_KEYPAD |
									  RSCT_READER_HARDWARE_MASK_DISPLAY |
									  RSCT_READER_HARDWARE_MASK_UPDATEABLE |
									  RSCT_READER_HARDWARE_MASK_MODULES|									  
									  (Mask << 18);

	
	m_ReaderInfo.Version=Info.KernelVersion;
	m_ReaderInfo.HardwareVersion=ReaderToHostLong(Info.ReaderConst.HardwareVersion);
	m_ReaderInfo.FlashSize=256*1024;
	m_ReaderInfo.HeapSize=16*48;
	GetKeyInfo(m_ReaderInfo.Keys,sizeof(m_ReaderInfo.Keys));
	for(int i=0;i<10;i++)
	{
		m_ReaderInfo.SeriaNumber[i]=Info.ReaderConst.Seriennummer[i*2];
	}
	m_ReaderInfo.SeriaNumber[10]='\0';

	memcpy(m_ReaderInfo.ProductionDate,Info.ReaderConst.dtDate[0].ProductionDate,10);
	m_ReaderInfo.ProductionDate[10]='\0';
	memcpy(m_ReaderInfo.ProductionTime,Info.ReaderConst.dtDate[0].ProductionTime,5);
	m_ReaderInfo.ProductionTime[5]='\0';

	memcpy(m_ReaderInfo.TestDate,Info.ReaderConst.dtDate[1].ProductionDate,10);
	m_ReaderInfo.TestDate[10]='\0';
	memcpy(m_ReaderInfo.TestTime,Info.ReaderConst.dtDate[1].ProductionTime,5);
	m_ReaderInfo.TestTime[5]='\0';

	memcpy(m_ReaderInfo.CommissioningDate,Info.ReaderConst.dtDate[2].ProductionDate,10);
	m_ReaderInfo.CommissioningDate[10]='\0';
	memcpy(m_ReaderInfo.CommissioningTime,Info.ReaderConst.dtDate[2].ProductionTime,5);
	m_ReaderInfo.CommissioningTime[5]='\0';
	SetHWString((char*)m_ReaderInfo.HardwareString);
	strcat((char*)m_ReaderInfo.HardwareString, (const char*)m_ReaderInfo.CommunicationString);
//	if(m_ReaderInfo.Keys[0].KNr<255 && m_ReaderInfo.Keys[1].KNr<255 && m_ReaderInfo.Keys[0].KNr>200 && m_ReaderInfo.Keys[1].KNr>200)
//   	strcat((char*)m_ReaderInfo.HardwareString, "_DEV");

	GetSecoderInfo(m_ReaderInfo.Info,sizeof(m_ReaderInfo.Info));
	return Res;
}

CJ_RESULT CEC30Reader::GetModuleIDs(uint32_t *Count,uint32_t *IDs)
{
	uint32_t help[33];
	uint32_t len=sizeof(help);
	uint32_t Res;
	*Count=0;
   if((Res=Escape(MODULE_ID_KERNEL,CCID_ESCAPE_MODULE_ENUM,0,0,&Res,(uint8_t *)help,&len))!=CJ_SUCCESS)
		m_Owner->DebugLeveled(DEBUG_MASK_COMMUNICATION_ERROR,"Can't enum modules");
	else
	{
		*Count=ReaderToHostLong(help[0]);
		if(*Count>32)
			*Count=32;
		for(unsigned int i=1;i<=*Count;i++)
		{
			*IDs++=ReaderToHostLong(help[i]);
		}
	}
	return Res;
}

CJ_RESULT CEC30Reader::GetModuleInfo(uint32_t ID,cj_ModuleInfo *Info)
{
	CJ_RESULT Result;
	cjeca_ModuleInfo ModuleInfo;
   uint32_t len=sizeof(ModuleInfo);
	uint32_t Res;
	ID=HostToReaderLong(ID);
	memset(Info,0xff,sizeof(cj_ModuleInfo));
   if((Result=Escape(MODULE_ID_KERNEL,CCID_ESCAPE_MODULE_INFO,(uint8_t *)&ID,sizeof(ID),&Res,(uint8_t *)&ModuleInfo,&len))!=CJ_SUCCESS)
		m_Owner->DebugLeveled(DEBUG_MASK_COMMUNICATION_ERROR,"Can't get module information");
	else
	{
		Info->BaseAddr=ReaderToHostLong(ModuleInfo.ModuleBaseAddr);
		Info->CodeSize=ReaderToHostLong(ModuleInfo.ModuleCodeSize);
		memcpy(Info->Date,ModuleInfo.DateTime.ProductionDate,11);
		Info->Date[11]='\0';
		memcpy(Info->Description,ModuleInfo.Description,16);
		Info->Description[16]='\0';
		Info->HeapSize=ModuleInfo.GlobalHeapSize;
		Info->ID=ReaderToHostLong(ModuleInfo.ModuleID);
		Info->RequieredKernelRevision=ModuleInfo.RequieredKernelRevision;
		Info->RequieredKernelVersion=ModuleInfo.RequieredKernelVersion;
		Info->Revision=ModuleInfo.Revision;
		Info->SizeOfStruct=sizeof(cj_ModuleInfo);
		Info->Status=ReaderToHostLong(ModuleInfo.Status);
		memcpy(Info->Time,ModuleInfo.DateTime.ProductionTime,5);
		Info->Time[5]='\0';
		Info->Variant=ModuleInfo.Variante;
		Info->Version=ModuleInfo.Version;
		Info->ContentsMask = RSCT_MODULE_MASK_STATUS |
									RSCT_MODULE_MASK_ID |
									RSCT_MODULE_MASK_VARIANT |
									RSCT_MODULE_MASK_BASE_ADDR |
									RSCT_MODULE_MASK_CODE_SIZE |
									RSCT_MODULE_MASK_VERSION |
									RSCT_MODULE_MASK_REVISION |
									RSCT_MODULE_MASK_REQUIRED_VERSION |
									RSCT_MODULE_MASK_REQUIRED_REVISION |
									RSCT_MODULE_MASK_HEAP_SIZE |
									RSCT_MODULE_MASK_DESCRIPTION |
									RSCT_MODULE_MASK_DATE;


	}
	return Result;
}


CJ_RESULT CEC30Reader::BuildModuleInfo()
{
	CJ_RESULT Res;
	uint32_t ID[33];
	if((Res=GetModuleIDs(&m_ModuleInfoCount,ID+1))==CJ_SUCCESS)
	{
		ID[0]=MODULE_ID_KERNEL;
		m_ModuleInfoCount++;
		if(m_pModuleInfo)
			delete m_pModuleInfo;
		m_pModuleInfo=new cj_ModuleInfo[m_ModuleInfoCount];
		for(unsigned int i=0;i<m_ModuleInfoCount;i++)
		{
			if((Res=GetModuleInfo(ID[i],m_pModuleInfo+i))!=CJ_SUCCESS)
				break;
		}
	}
	return Res;
}

void CEC30Reader::SetSerialNumber(void)
{
	uint32_t Res;
   unsigned long dwUuid;
	uint8_t SerNo[20];
#ifdef _WINDOWS
	#ifdef UNDER_CE
		SYSTEMTIME sysTimeStruct; 
 		GetSystemTime(&sysTimeStruct); 
		dwUuid=sysTimeStruct.wMilliseconds;	
	#else
	   UUID uid;
	   unsigned long Help;
		UuidCreate(&uid);
		dwUuid=uid.Data1;
		memcpy(&Help,&uid.Data2,4);
		dwUuid^=Help;
		memcpy(&Help,uid.Data4,4);
		dwUuid^=Help;
		memcpy(&Help,uid.Data4+4,4);
		dwUuid^=Help;
	 #endif
#else
	dwUuid=time(NULL)+InversByteOrderLong(clock());
#endif
   memset(SerNo,0,sizeof(SerNo));
   for(int i=0;i<20;i+=2)
   {
      SerNo[i]=(uint8_t)('0'+dwUuid%10);
      dwUuid/=10;
   }
   if(SetFlashMask()!=CJ_SUCCESS)
		m_Owner->DebugLeveled(DEBUG_MASK_COMMUNICATION_ERROR,"Can't set Flashmask");
   else if(Escape(MODULE_ID_KERNEL,CCID_ESCAPE_SET_SERNUMBER,SerNo,sizeof(SerNo),&Res,0,0)!=CJ_SUCCESS)
		m_Owner->DebugLeveled(DEBUG_MASK_COMMUNICATION_ERROR,"Can't set serial number");
}

bool CEC30Reader::SetReaderConstants(void)
{
	uint32_t LocalInfo;
	bool bRebuild=false;

	LocalInfo=GetEnviroment("pinpad2_info",0xffffffff);
	if((LocalInfo & 1) && IsNotSet(m_ReaderInfo.ProductionDate,10) && IsNotSet(m_ReaderInfo.ProductionTime,5))
   {
      SetDate(0);
		bRebuild=true;
   }
	if((LocalInfo & 2) && IsNotSet(m_ReaderInfo.TestDate,10) && IsNotSet(m_ReaderInfo.TestTime,5))
   {
      SetDate(1);
		bRebuild=true;
   }
	if((LocalInfo & 8) && IsNotSet(m_ReaderInfo.CommissioningDate,10) && IsNotSet(m_ReaderInfo.CommissioningTime,5))
   {
      SetDate(2);
		bRebuild=true;
   }
	if((LocalInfo & 4) && IsNotSet(m_ReaderInfo.SeriaNumber,10))
   {
		SetSerialNumber();
		bRebuild=true;
   }
	return bRebuild;
}


void CEC30Reader::SetDate(uint8_t Nr)
{
	uint32_t Res;
	struct _CCID_Message::_Data::_Escape::_Reader::_EC30::_Data::_SetDateTime DateTime;
	struct tm *t;
	time_t tim;

	time(&tim);
	t=localtime(&tim);

	DateTime.Nr=Nr;
	sprintf((char *)DateTime.dtDate.ProductionDate,"%02d.%02d.%04d",t->tm_mday,t->tm_mon+1,t->tm_year+1900);
	sprintf((char *)DateTime.dtDate.ProductionTime,"%02d:%02d",t->tm_hour,t->tm_min);

   if(SetFlashMask()!=CJ_SUCCESS)
		m_Owner->DebugLeveled(DEBUG_MASK_COMMUNICATION_ERROR,"Can't set Flashmask");
	else if(Escape(MODULE_ID_KERNEL,CCID_ESCAPE_SET_DATE_TIME,(uint8_t *)&DateTime,sizeof(DateTime),&Res,0,0)!=CJ_SUCCESS)
		m_Owner->DebugLeveled(DEBUG_MASK_COMMUNICATION_ERROR,"Can't set date");
}

typedef struct _tFileHeader
{
  unsigned long OffsetCrc16;
  unsigned long Status;
  unsigned long ModuleBaseAddr;
  unsigned long ModuleHeaderID;
  unsigned long ModuleID;
  unsigned long ModuleCodeSize;
  uint8_t Version;
  uint8_t Revision;
  uint8_t Variante;
  uint8_t RequieredKernelVersion;
  uint8_t RequieredKernelRevision;
  uint8_t GlobalHeapSize;
  uint8_t ExtraPages;
  uint8_t cReserved2;
  uint8_t DateOfCompilation[12];
  uint8_t TimeOfCompilation[12];
  int8_t Description[16];
}tFileHeader;


CJ_RESULT CEC30Reader::CtGetModuleInfoFromFile(uint8_t *pData,uint32_t DataLength,cj_ModuleInfo *Info,uint32_t *EstimatedUpdateTime)
{
   tFileHeader *header=(tFileHeader *)pData;

	*EstimatedUpdateTime=8000;

	if(DataLength<sizeof(tFileHeader))
	   return CJ_ERR_WRONG_SIZE;

	if(Info->SizeOfStruct<sizeof(cj_ModuleInfo))
	   return CJ_ERR_RBUFFER_TO_SMALL;

	Info->ContentsMask = RSCT_MODULE_MASK_ID |
								RSCT_MODULE_MASK_VARIANT |
								RSCT_MODULE_MASK_CODE_SIZE |
								RSCT_MODULE_MASK_VERSION |
								RSCT_MODULE_MASK_REVISION |
								RSCT_MODULE_MASK_REQUIRED_VERSION |
								RSCT_MODULE_MASK_REQUIRED_REVISION |
								RSCT_MODULE_MASK_HEAP_SIZE |
								RSCT_MODULE_MASK_DESCRIPTION |
								RSCT_MODULE_MASK_DATE;
	Info->ID=ReaderToHostLong(header->ModuleID);
	Info->CodeSize=ReaderToHostLong(header->ModuleCodeSize);
	memcpy(Info->Date,header->DateOfCompilation,11);
	Info->Date[11]='\0';
	memcpy(Info->Description,header->Description,16);
	Info->Description[16]='\0';
	Info->HeapSize=header->GlobalHeapSize;
	Info->RequieredKernelRevision=header->RequieredKernelRevision;
	Info->RequieredKernelVersion=header->RequieredKernelVersion;
	Info->Revision=header->Revision;
	Info->SizeOfStruct=sizeof(cj_ModuleInfo);
	memcpy(Info->Time,header->TimeOfCompilation,5);
	Info->Time[5]='\0';
	Info->Variant=header->Variante;
	Info->Version=header->Version;
	if(Info->ID==MODULE_ID_KERNEL)
		*EstimatedUpdateTime=8000;
	else
		*EstimatedUpdateTime=6000;

	return CJ_SUCCESS;
}

int CEC30Reader::GetWarmstartTimeout(void)
{
	return 4500;
}


CJ_RESULT CEC30Reader::CtLoadModule(uint8_t *pData,uint32_t DataLength,uint8_t *pSgn,uint32_t SgnLength,uint32_t *Result)
{
	uint32_t len;
	CJ_RESULT Res;
   tFileHeader *header=(tFileHeader *)pData;

	struct _CCID_Message::_Data::_Escape::_Reader::_EC30::_Data::_UpdateData Data;
	struct _CCID_Message::_Data::_Escape::_Reader::_EC30::_Data::_UpdateVerify Verify;

	if(DataLength<258)
	   return CJ_ERR_WRONG_SIZE;
   if((Res=SetFlashMask())!=CJ_SUCCESS)
	{
		m_Owner->DebugLeveled(DEBUG_MASK_COMMUNICATION_ERROR,"Can't set Flashmask");
		return Res;
	}
   if((Res=Escape(MODULE_ID_KERNEL,CCID_ESCAPE_UPDATE_START,pData,256,Result,0,0))!=CJ_SUCCESS)
	{
		m_Owner->DebugLeveled(DEBUG_MASK_COMMUNICATION_ERROR,"Can't begin update");
		return Res;
	}
	DataLength-=256;
	pData+=256;
	while(DataLength)
	{
		len=(DataLength>256)?256:DataLength;
		memcpy(Data.Data,pData,len);
		Data.bLength=(uint16_t)len;
		if((Res=SetFlashMask())!=CJ_SUCCESS)
		{
			m_Owner->DebugLeveled(DEBUG_MASK_COMMUNICATION_ERROR,"Can't set Flashmask");
			return Res;
		}
      if((Res=Escape(MODULE_ID_KERNEL,CCID_ESCAPE_UPDATE,(uint8_t *)&Data,sizeof(Data),Result,0,0))!=CJ_SUCCESS)
		{
	   	m_Owner->DebugLeveled(DEBUG_MASK_COMMUNICATION_ERROR,"Can't transmit update");
			return Res;
		}
   	DataLength-=len;
	   pData+=len;
	}
	Verify.len=HostToReaderLong(SgnLength);
	memcpy(Verify.Sign,pSgn,SgnLength);
   if((Res=SetFlashMask())!=CJ_SUCCESS)
	{
		m_Owner->DebugLeveled(DEBUG_MASK_COMMUNICATION_ERROR,"Can't set Flashmask");
		return Res;
	}
   if((Res=Escape(MODULE_ID_KERNEL,CCID_ESCAPE_VERIFY,(uint8_t *)&Verify,sizeof(Verify),Result,0,0))!=CJ_SUCCESS)
	{
	  	m_Owner->DebugLeveled(DEBUG_MASK_COMMUNICATION_ERROR,"Can't transmit signature");
		return Res;
	}
	if(header->ModuleID==MODULE_ID_KERNEL)
	{
	  //		m_pCommunicator->Close();
	  Sleep(GetWarmstartTimeout());
//		m_pCommunicator->Open();
	}
	BuildReaderInfo();
	BuildModuleInfo();
	return Res;

	
}

CJ_RESULT CEC30Reader::CtDeleteALLModules(uint32_t *Result)
{
	CJ_RESULT Res;
   if((Res=SetFlashMask())!=CJ_SUCCESS)
	{
		m_Owner->DebugLeveled(DEBUG_MASK_COMMUNICATION_ERROR,"Can't set Flashmask");
		return Res;
	}
   if((Res=Escape(MODULE_ID_KERNEL,CCID_ESCAPE_MODULE_DELALL,0,0,Result,0,0))!=CJ_SUCCESS)
	{
	  	m_Owner->DebugLeveled(DEBUG_MASK_COMMUNICATION_ERROR,"Not deleted");
	}
	BuildModuleInfo();
   return Res;
}

CJ_RESULT CEC30Reader::CtDeleteModule(uint32_t ModuleID,uint32_t *Result)
{
	CJ_RESULT Res;
	ModuleID=HostToReaderLong(ModuleID);
   if((Res=SetFlashMask())!=CJ_SUCCESS)
	{
		m_Owner->DebugLeveled(DEBUG_MASK_COMMUNICATION_ERROR,"Can't set Flashmask");
		return Res;
	}
   if((Res=Escape(MODULE_ID_KERNEL,CCID_ESCAPE_MODULE_DELETE,(uint8_t *)&ModuleID,sizeof(ModuleID),Result,0,0))!=CJ_SUCCESS)
	{
	  	m_Owner->DebugLeveled(DEBUG_MASK_COMMUNICATION_ERROR,"Not deleted");
	}
	BuildModuleInfo();
	return Res;
}

CJ_RESULT CEC30Reader::CtActivateModule(uint32_t ModuleID,uint32_t *Result)
{
	CJ_RESULT Res;
	ModuleID=HostToReaderLong(ModuleID);
   if((Res=SetFlashMask())!=CJ_SUCCESS)
	{
		m_Owner->DebugLeveled(DEBUG_MASK_COMMUNICATION_ERROR,"Can't set Flashmask");
		return Res;
	}
   if((Res=Escape(MODULE_ID_KERNEL,CCID_ESCAPE_MODULE_REACTIVATE,(uint8_t *)&ModuleID,sizeof(ModuleID),Result,0,0))!=CJ_SUCCESS)
	{
	  	m_Owner->DebugLeveled(DEBUG_MASK_COMMUNICATION_ERROR,"Not deleted");
	}
	BuildModuleInfo();
	return Res;
}

CJ_RESULT CEC30Reader::CtDeactivateModule(uint32_t ModuleID,uint32_t *Result)
{
	CJ_RESULT Res;
	ModuleID=HostToReaderLong(ModuleID);
   if((Res=SetFlashMask())!=CJ_SUCCESS)
	{
		m_Owner->DebugLeveled(DEBUG_MASK_COMMUNICATION_ERROR,"Can't set Flashmask");
		return Res;
	}
   if((Res=Escape(MODULE_ID_KERNEL,CCID_ESCAPE_MODULE_DEACTIVATE,(uint8_t *)&ModuleID,sizeof(ModuleID),Result,0,0))!=CJ_SUCCESS)
	{
	  	m_Owner->DebugLeveled(DEBUG_MASK_COMMUNICATION_ERROR,"Not deleted");
	}
	BuildModuleInfo();
	return Res;
}

CJ_RESULT CEC30Reader::_CtSetContrast(uint8_t Value,uint32_t *Result)
{
	CJ_RESULT Res;
   if((Res=SetFlashMask())!=CJ_SUCCESS)
	{
		m_Owner->DebugLeveled(DEBUG_MASK_COMMUNICATION_ERROR,"Can't set Flashmask");
		return Res;
	}
   if((Res=Escape(MODULE_ID_KERNEL,CCID_ESCAPE_DSP_CONTRAST,&Value,sizeof(Value),Result,0,0))!=CJ_SUCCESS)
	{
	  	m_Owner->DebugLeveled(DEBUG_MASK_COMMUNICATION_ERROR,"Not deleted");
	}
	return Res;
}

CJ_RESULT CEC30Reader::_CtSetBacklight(uint8_t Value,uint32_t *Result)
{
	CJ_RESULT Res;
   if((Res=SetFlashMask())!=CJ_SUCCESS)
	{
		m_Owner->DebugLeveled(DEBUG_MASK_COMMUNICATION_ERROR,"Can't set Flashmask");
		return Res;
	}
   if((Res=Escape(MODULE_ID_KERNEL,CCID_ESCAPE_DSP_BACKLIGHT,&Value,sizeof(Value),Result,0,0))!=CJ_SUCCESS)
	{
	  	m_Owner->DebugLeveled(DEBUG_MASK_COMMUNICATION_ERROR,"Not deleted");
	}
	return Res;
}

CJ_RESULT CEC30Reader::CtKeyUpdate(uint8_t *pData,uint32_t DataLength,uint32_t *Result)
{
	uint8_t KV[256];
	CJ_RESULT Res;
	uint8_t *ptr=pData;
	uint32_t len=DataLength;
	uint32_t EstimatedUpdateTime;
	uint16_t help;
	uint16_t help1;
	uint8_t help2;
	uint16_t help3;
	uint8_t *NewKey;
	uint32_t Ret;
	bool IsRoot=true;
	_CCID_Message::_Data::_Escape::_Reader::_EC30::_Data::_UpdateKey Key;
	*Result=0;

	if(_CtIsKeyUpdateRecommended(pData,DataLength,&EstimatedUpdateTime,KV,Res))
	{
		if(Res!=CJ_SUCCESS)
			return Res;
		for (; ; )
		{
			if (len == 0)
				break;
			ptr+=3;
			len-=3;
			memcpy(&help,ptr,2);
			help=ReaderToHostShort(help);
			ptr+=2;
			len-=2;
			NewKey=ptr;
			memcpy(&help1,ptr+12,2);
			help1=ReaderToHostShort(help1);
			help2=ptr[15+help1];
			ptr+=16+help1+help2;
			if(0xff==m_ReaderInfo.Keys[0].KNr && 0xff==m_ReaderInfo.Keys[0].Version && KV[NewKey[8]]==NewKey[9] || 0xff==m_ReaderInfo.Keys[1].KNr && 0xff==m_ReaderInfo.Keys[1].Version && KV[NewKey[8]]==NewKey[9])
			{
				Key.len=16+help1+help2;
				if(Key.len>800)
				{
					*Result=ECA_MODULE_ERR_OUT_OF_RESOURCE;
					return CJ_ERR_CHECK_RESULT;
				}

				memcpy(Key.Key,NewKey,Key.len);
				Key.len=HostToReaderLong(Key.len);
				if((Res=SetFlashMask())!=CJ_SUCCESS)
				{
					m_Owner->DebugLeveled(DEBUG_MASK_COMMUNICATION_ERROR,"Can't set Flashmask");
					return Res;
				}
				if((Ret=Escape(MODULE_ID_KERNEL,CCID_ESCAPE_UPDATE_KEY,(uint8_t *)&Key,sizeof(Key.len)+16+help1+help2,Result,0,0))!=CJ_SUCCESS)
				{
					m_Owner->DebugLeveled(DEBUG_MASK_COMMUNICATION_ERROR,"Update Key error");
					return Ret;
				}
				if((Res=SetFlashMask())!=CJ_SUCCESS)
				{
					m_Owner->DebugLeveled(DEBUG_MASK_COMMUNICATION_ERROR,"Can't set Flashmask");
					return Res;
				}
			   if((Ret=Escape(MODULE_ID_KERNEL,CCID_ESCAPE_VERIFY_KEY,(uint8_t *)&len,sizeof(len),Result,0,0))!=CJ_SUCCESS)
				{
					m_Owner->DebugLeveled(DEBUG_MASK_COMMUNICATION_ERROR,"Verify Key error");
					return Ret;
				}
				BuildReaderInfo();
			}
			else if(!IsRoot)
			{
				memcpy(&help3,ptr+2,2);
				help3=ReaderToHostShort(help3);
   			if(NewKey[8]==m_ReaderInfo.Keys[0].KNr && NewKey[9]>m_ReaderInfo.Keys[0].Version 
					|| NewKey[8]==m_ReaderInfo.Keys[1].KNr && NewKey[9]>m_ReaderInfo.Keys[1].Version)
	   		{
					if(ptr[6]==m_ReaderInfo.Keys[0].KNr && ptr[7]==m_ReaderInfo.Keys[0].Version ||
					   ptr[6]==m_ReaderInfo.Keys[1].KNr && ptr[7]==m_ReaderInfo.Keys[1].Version)
					{
						Key.len=16+help1+help2;
						if(Key.len>800 || help3>796)
						{
							*Result=ECA_MODULE_ERR_OUT_OF_RESOURCE;
							return CJ_ERR_CHECK_RESULT;
						}

						memcpy(Key.Key,NewKey,Key.len);
						Key.len=HostToReaderLong(Key.len);
						if((Res=SetFlashMask())!=CJ_SUCCESS)
						{
							m_Owner->DebugLeveled(DEBUG_MASK_COMMUNICATION_ERROR,"Can't set Flashmask");
							return Res;
						}
						if((Ret=Escape(MODULE_ID_KERNEL,CCID_ESCAPE_UPDATE_KEY,(uint8_t *)&Key,sizeof(Key.len)+Key.len,Result,0,0))!=CJ_SUCCESS)
						{
	  						m_Owner->DebugLeveled(DEBUG_MASK_COMMUNICATION_ERROR,"Update Key error");
							return Ret;
						}
						Key.len=help3+4;
						memcpy(Key.Key,ptr,Key.len);
						Key.len=HostToReaderLong(Key.len);
						if((Res=SetFlashMask())!=CJ_SUCCESS)
						{
							m_Owner->DebugLeveled(DEBUG_MASK_COMMUNICATION_ERROR,"Can't set Flashmask");
							return Res;
						}
					   if((Ret=Escape(MODULE_ID_KERNEL,CCID_ESCAPE_VERIFY_KEY,(uint8_t *)&Key,sizeof(Key.len)+help3+4,Result,0,0))!=CJ_SUCCESS)
						{
	  						m_Owner->DebugLeveled(DEBUG_MASK_COMMUNICATION_ERROR,"Verify Key error");
							return Ret;
						}
						BuildReaderInfo();
					}
					else
					{
						return CJ_ERR_DATA_CORRUPT;
					}
			   }
			}
         ptr=NewKey+help;			
			len-=help;
			IsRoot=false;
		}
	}
	return CJ_SUCCESS;
}

CJ_RESULT CEC30Reader::CtIsKeyUpdateRecommended(uint8_t *pData,uint32_t DataLength,uint32_t *EstimatedUpdateTime)
{
	CJ_RESULT Res;
	uint8_t KV[256];
	_CtIsKeyUpdateRecommended(pData,DataLength,EstimatedUpdateTime,KV,Res);
	return Res;
}

bool CEC30Reader::_CtIsKeyUpdateRecommended(uint8_t *pData,uint32_t DataLength,uint32_t *EstimatedUpdateTime,uint8_t *KV,CJ_RESULT &Res)
{
	uint8_t *ptr=pData;
	uint32_t len=DataLength;
	uint16_t help;
	uint16_t help1;
	uint8_t help2;
	uint16_t help3;
	uint16_t help4;
	bool IsRoot=true;
	bool IsRecommended=false;
	Res=CJ_SUCCESS;
	

	memset(KV,0,256);
	*EstimatedUpdateTime=0;
	for (; ; )
	{
		if (len == 0)
			break;
		if (len < 23 || (!IsRoot && len <36))
		{
			Res=CJ_ERR_DATA_CORRUPT;
			break;
		}
		if (memcmp(ptr,"\x7F\x21\x82",3)!=0)
		{
			Res=CJ_ERR_DATA_CORRUPT;
			break;
		}
		ptr+=3;
		len-=3;
		memcpy(&help,ptr,2);
		help=ReaderToHostShort(help);
		if (help < 18 || (!IsRoot && help<31))
		{
			Res=CJ_ERR_DATA_CORRUPT;
			break;
		}
		ptr+=2;
		len-=2;
		if (len < help)
		{
			Res=CJ_ERR_DATA_CORRUPT;
			break;
		}
		if (memcmp(ptr,"\x52\x04""ecom""\x51\x02",8)!=0)
		{
			Res=CJ_ERR_DATA_CORRUPT;
			break;
		}
		if (memcmp(ptr+10,"\x81\x82",2)!=0)
		{
			Res=CJ_ERR_DATA_CORRUPT;
			break;
		}
		if (KV[ptr[8]] >= ptr[9])
		{
			Res=CJ_ERR_DATA_CORRUPT;
			break;
		}
		if(ptr[8]==m_ReaderInfo.Keys[0].KNr && ptr[9]>m_ReaderInfo.Keys[0].Version || ptr[8]==m_ReaderInfo.Keys[1].KNr && ptr[9]>m_ReaderInfo.Keys[1].Version)
		{
			*EstimatedUpdateTime+=5000;
			IsRecommended=true;
		}
		if(0xff==m_ReaderInfo.Keys[0].KNr && 0xff==m_ReaderInfo.Keys[0].Version && KV[ptr[8]]==0 || 0xff==m_ReaderInfo.Keys[1].KNr && 0xff==m_ReaderInfo.Keys[1].Version && KV[ptr[8]]==0)
		{
			*EstimatedUpdateTime+=1000;
			IsRecommended=true;
		}
		memcpy(&help1,ptr+12,2);
		help1=ReaderToHostShort(help1);
		if (help < 17+help1 || (!IsRoot && help<30 + help1) || help1<1)
		{
			Res=CJ_ERR_DATA_CORRUPT;
			break;
		}
		help2=ptr[15+help1];
		if(ptr[14+help1]!=0x82 || IsRoot && 16+help1+help2!=help || !IsRoot && help < 29 + help1 + help2 || help2<1)
		{
			Res=CJ_ERR_DATA_CORRUPT;
			break;
		}
		KV[ptr[8]] = ptr[9];
	   ptr+=16+help1+help2;
		if(!IsRoot)
		{
		   if(memcmp(ptr,"\x83\x82",2)!=0)
		   {
			   Res=CJ_ERR_DATA_CORRUPT;
			   break;
		   }
		   memcpy(&help3,ptr+2,2);
		   help3=ReaderToHostShort(help3);
			if(help3<9 || 20+help1+help2+help3!=help)
			{
				Res=CJ_ERR_DATA_CORRUPT;
				break;
			}
		   if(memcmp(ptr+4,"\x51\x02",2)!=0)
		   {
			   Res=CJ_ERR_DATA_CORRUPT;
			   break;
			}
		   if(memcmp(ptr+8,"\x84\x82",2)!=0)
		   {
			   Res=CJ_ERR_DATA_CORRUPT;
			   break;
			}
		   memcpy(&help4,ptr+10,2);
		   help4=ReaderToHostShort(help4);
			if(help3!=help4 + 8)
			{
				Res=CJ_ERR_DATA_CORRUPT;
				break;
			}
			ptr+=4+help3;
		}
		
		len-=help;
		IsRoot=false;
	}
	return IsRecommended;
}

bool CEC30Reader::ATRFilter(bool IsWarm)
{
	return IsWarm;
}


RSCT_IFD_RESULT CEC30Reader::IfdPower(uint32_t Mode,uint8_t *ATR,uint32_t *ATR_Length,uint32_t Timeout)
{
//	RSCT_IFD_RESULT Result=STATUS_SUCCESS;
	CCID_Message Message;
	CCID_Response Response;
	Timeout=HostToReaderLong(Timeout);
	bool warm=false;
	bool first=true;

	switch(Mode)
	{
	case SCARD_COLD_RESET:
	case SCARD_WARM_RESET:
		*ATR_Length=0;
		IfdPower(SCARD_POWER_DOWN,NULL,NULL,0);
		break;
	case SCARD_POWER_DOWN:
		break;
	default:
   	return STATUS_INVALID_PARAMETER;
	}
	do
	{
		memset(&Message,0,sizeof(Message));
		Message.dwLength=4;
		Message.Header.iccPowerOn.bPowerSelect=1;
		switch(Mode)
		{
		case SCARD_COLD_RESET:
		case SCARD_WARM_RESET:
			*ATR_Length=0;
			Message.bMessageType=PC_TO_RDR_ICCPOWERON;
			break;
		case SCARD_POWER_DOWN:
			Message.bMessageType=PC_TO_RDR_ICCPOWEROFF;
			break;
		}
		memcpy(Message.Data.abData,&Timeout,4);
		if(first)
			first=false;
		else
			warm=true;
		if(Transfer(&Message,&Response)==CJ_SUCCESS)
		{
			switch(Mode)
			{
			case SCARD_COLD_RESET:
			case SCARD_WARM_RESET:
				if(Response.bMessageType!=RDR_TO_PC_DATABLOCK)
					return STATUS_DEVICE_NOT_CONNECTED;
				break;
			case SCARD_POWER_DOWN:
				if(Response.bMessageType!=RDR_TO_PC_SLOTSTATUS)
					return STATUS_DEVICE_NOT_CONNECTED;
				break;
			}
			if(Response.bStatus & 0x40)
			{
				switch(Response.bError)
				{
				case 0xfe:
					return STATUS_NO_MEDIA;
				case 0xf6:
					return STATUS_UNRECOGNIZED_MEDIA;
				case 0xef: 
					return STATUS_CANCELLED;
				default:
					return STATUS_IO_TIMEOUT;
				}
			}
			if(Response.dwLength>33)
				Response.dwLength=33;
			switch(Mode)
			{
			case SCARD_COLD_RESET:
			case SCARD_WARM_RESET:
				m_ATR_Length=Response.dwLength;
				memcpy(m_ATR,Response.Data.abData,Response.dwLength);
				warm=ATRFilter(warm);
				*ATR_Length=m_ATR_Length;
				memcpy(ATR,m_ATR,m_ATR_Length);
			default:;
			}
		}
		else
			return STATUS_DEVICE_NOT_CONNECTED;
	}while(Mode!=SCARD_POWER_DOWN && AnalyseATR(warm)==1);
	
	return STATUS_SUCCESS;

}

CJ_RESULT CEC30Reader::CtSelfTest(void)
{
	uint32_t Res;
	struct _CCID_Message::_Data::_Escape::_Reader::_EC30::_Data::_SetDateTime DateTime;
   struct tm *t;
   time_t tim;
	 CJ_RESULT Result;

   time(&tim);
   t=localtime(&tim);

	 DateTime.Nr=0;
   sprintf((char *)DateTime.dtDate.ProductionDate,"%02d.%02d.%04d",t->tm_mday,t->tm_mon+1,t->tm_year+1900);
   sprintf((char *)DateTime.dtDate.ProductionTime,"%02d:%02d",t->tm_hour,t->tm_min);

   if((Res=SetFlashMask())!=CJ_SUCCESS)
	{
		m_Owner->DebugLeveled(DEBUG_MASK_COMMUNICATION_ERROR,"Can't set Flashmask");
		return Res;
	}
  Result=Escape(MODULE_ID_KERNEL,CCID_ESCAPE_SELF_TEST,(uint8_t *)&DateTime,sizeof(DateTime),&Res,0,0);
	if(Result!=CJ_SUCCESS && Result!=CJ_ERR_CHECK_RESULT)
	{
		m_Owner->DebugLeveled(DEBUG_MASK_COMMUNICATION_ERROR,"Error Selftest");
		return CJ_ERR_DEVICE_LOST;
	}
   return Res;
}

CJ_RESULT CEC30Reader::CtShowAuth(void)
{
	uint32_t Res;
   if(Escape(MODULE_ID_KERNEL,CCID_ESCAPE_SHOW_AUTH,NULL,0,&Res,0,0)!=CJ_SUCCESS)
		m_Owner->DebugLeveled(DEBUG_MASK_COMMUNICATION_ERROR,"Error Show Auth");
   return Res;
}


RSCT_IFD_RESULT CEC30Reader::ccidTransmit(const uint8_t *cmd, uint16_t cmd_len,uint8_t *response,uint16_t *response_len)
{
	CCID_Message Message;
	CCID_Response Response;
	if(cmd_len>GetReadersInputBufferSize()-10)
	{
		*response_len=0;
		return STATUS_BUFFER_OVERFLOW;
	}
	memset(&Message,0,sizeof(Message));
	Message.bMessageType=PC_TO_RDR_XFRBLOCK;
	Message.dwLength=cmd_len;
	memcpy(Message.Data.abData,cmd,cmd_len);
	if(Transfer(&Message,&Response)==CJ_SUCCESS)
	{
		if(Response.bMessageType!=RDR_TO_PC_DATABLOCK)
		{
			IfdPower(SCARD_POWER_DOWN,NULL,NULL,0);
			*response_len=0;
			return STATUS_DEVICE_PROTOCOL_ERROR;
		}
		if(Response.bStatus & 0x40)
		{
			if(Response.bError==ICC_MUTE)
			{
   			IfdPower(SCARD_POWER_DOWN,NULL,NULL,0);
				*response_len=0;
				return STATUS_IO_TIMEOUT;
			}
			else
			{
				IfdPower(SCARD_POWER_DOWN,NULL,NULL,0);
				*response_len=0;
				return STATUS_DEVICE_PROTOCOL_ERROR;
			}
		}
		if(Response.dwLength>*response_len)
		{
			*response_len=0;
			return STATUS_BUFFER_TOO_SMALL;
		}
		*response_len=(uint16_t)Response.dwLength;
		memcpy(response,Response.Data.abData,Response.dwLength);
		return STATUS_SUCCESS;

	}
	return STATUS_DEVICE_NOT_CONNECTED;

}



RSCT_IFD_RESULT CEC30Reader::IfdTransmit(const uint8_t *cmd, uint16_t cmd_len,uint8_t *response,uint16_t *response_len)
{
	 SCARD_IO_REQUEST io_request;
	 RSCT_IFD_RESULT Res;

	 uint16_t ResponseLength=*response_len-sizeof(SCARD_IO_REQUEST);
	 if(cmd_len<=sizeof(SCARD_IO_REQUEST))
	 {
		 *response_len=0;
		 return STATUS_INVALID_PARAMETER;
	 }
	 memcpy(&io_request,cmd,sizeof(SCARD_IO_REQUEST));
	 if(cmd_len<=io_request.cbPciLength)
	 {
		 *response_len=0;
		 return STATUS_INVALID_PARAMETER;
	 }
	 if(io_request.dwProtocol!=m_ActiveProtocol)
	 {
		 *response_len=0;
		 return STATUS_INVALID_PARAMETER;
	 }
	 if(m_ReaderState!=SCARD_SPECIFIC)
	 {
		 *response_len=0;
		 return STATUS_INVALID_DEVICE_STATE;
	 }
	 cmd_len-=(uint16_t)io_request.cbPciLength;
	 cmd+=io_request.cbPciLength;
	 Res=_IfdTransmit(cmd,cmd_len,response+sizeof(io_request),&ResponseLength);
	 if(Res==STATUS_SUCCESS)
	 {
		 *response_len=ResponseLength+sizeof(io_request);
		 io_request.cbPciLength=sizeof(io_request);
		 memcpy(response,&io_request,sizeof(io_request));
	 }
	 else
	 {
		 *response_len=0;
	 }
	 return Res;

}

RSCT_IFD_RESULT CEC30Reader::_IfdTransmit(const uint8_t *cmd, uint16_t cmd_len,uint8_t *response,uint16_t *response_len)
{
	RSCT_IFD_RESULT Result;
	int l;
	const char *str;
	//	uint16_t rest=*response_len;
	if(cmd_len>7)
	{
		if(cmd[0]==0xff && cmd[1]==0x91 && cmd[4]==0)
		{
			int32_t Lc=-1;
			int32_t Le=-1;
			if(cmd_len==7+((uint32_t)cmd[5]<<8)+cmd[6])
			{
				Lc=((int32_t)cmd[5]<<8)+cmd[6];
			}
			else if(cmd_len==9+((uint32_t)cmd[5]<<8)+cmd[6] && (cmd[5]!=0 || cmd[6]!=0))
			{
				Lc=((int32_t)cmd[5]<<8)+cmd[6];
				Le=((int32_t)cmd[cmd_len-2]<<8)+cmd[cmd_len-1];
			}
			if(Lc>0)
			{
				if(cmd[3]==0x00 && (cmd[2] & 0xf0)==0 && IsClass3() && FindModuleWithMask(MODULE_ID_KT_LIGHT,0xfffffeff))
				{

					uint8_t sad=2;
					uint8_t dad=1;
					if(SecoderBufferLen<cmd_len)
					{
						if(SecoderBufferLen>0)
						{
							SecoderBufferLen=0;
							delete[] SecoderBuffer;
						}
						if((SecoderBuffer=new uint8_t[cmd_len+1024])!=NULL)
							SecoderBufferLen=cmd_len+1024;
						else
							return STATUS_BUFFER_OVERFLOW;
					}

					memcpy(SecoderBuffer,cmd,cmd_len);
					SecoderBuffer[0]=0x20;
					SecoderBuffer[1]=0x70 | (0x0f & SecoderBuffer[2]);
					SecoderBuffer[2]=0;
					SecoderBuffer[3]=0;
					return KTLightCall(&sad,&dad,SecoderBuffer,cmd_len,Lc,SecoderBuffer+7,Le,response,response_len);
				}
				else if(cmd[3]==1 && Le==-1)
				{
					if(SecoderBufferLen<cmd_len)
					{
						if(SecoderBufferLen>0)
						{
							SecoderBufferLen=0;
							delete[] SecoderBuffer;
						}
						if((SecoderBuffer=new uint8_t[cmd_len+1024])!=NULL)
							SecoderBufferLen=cmd_len+1024;
						else
							return STATUS_BUFFER_OVERFLOW;
					}

					memcpy(SecoderBuffer,cmd,cmd_len);
					if(cmd[2]==0)
					{
						uint32_t rlen=*response_len;
						RSCT_IFD_RESULT Res=IfdVendor(CJPCSC_VEN_IOCTRL_VERIFY_PIN_DIRECT,SecoderBuffer+7,cmd_len-7,response,&rlen);
						if(Res!=STATUS_SUCCESS)
						{
							*response_len=0;
							return Res;
						}
						if(rlen<65536)
						{
						   *response_len=(uint16_t)rlen;
							return Res;

						}
						else 
						{
							*response_len=0;
							return STATUS_BUFFER_OVERFLOW;
						}

					}
					if(cmd[2]==1)
					{
						uint32_t rlen=*response_len;
						RSCT_IFD_RESULT Res=IfdVendor(CJPCSC_VEN_IOCTRL_MODIFY_PIN_DIRECT,SecoderBuffer+7,cmd_len-7,response,&rlen);
						if(Res!=STATUS_SUCCESS)
						{
							*response_len=0;
							return Res;
						}
						if(rlen<65536)
						{
						   *response_len=(uint16_t)rlen;
							return Res;
						}
						else 
						{
							*response_len=0;
							return STATUS_BUFFER_OVERFLOW;
						}
					}
				}
			}
		}
	}

	if(cmd_len==5 && cmd[0]==0xff && cmd[1]==0x9a && cmd[2]==0x01  && cmd[4]==0)
	{
		switch(cmd[3])
		{
		case 1:
			if(*response_len>=12)
			{
				memcpy(response,"REINER SCT\x90\x00",12);
				*response_len=12;
				return STATUS_SUCCESS;
			}
			else
			{
				*response_len=0;
				return STATUS_BUFFER_TOO_SMALL;
			}
			break;
		case 2:
			if(*response_len>=6)
			{
				memcpy(response,"0C4B\x90\x00",6);
				*response_len=6;
				return STATUS_SUCCESS;
			}
			else
			{
				*response_len=0;
				return STATUS_BUFFER_TOO_SMALL;
			}
			break;
		case 3:
			if(*response_len>=(l=strlen((const char*)(m_ReaderInfo.ProductString)))+2)
			{
				memcpy(response,m_ReaderInfo.ProductString,l);
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
				sprintf((char *)response,"%04X\x90",m_ReaderInfo.PID);
				*response_len=6;
				return STATUS_SUCCESS;
			}
			else
			{
				*response_len=0;
				return STATUS_BUFFER_TOO_SMALL;
			}
			break;
		case 6:
			if(*response_len>=5)
			{
				sprintf((char *)response,"%1d.%1d\x90",(int)(m_ReaderInfo.Version>>4),(int)(m_ReaderInfo.Version & 0x0f));
				*response_len=5;
				return STATUS_SUCCESS;
			}
			else
			{
				*response_len=0;
				return STATUS_BUFFER_TOO_SMALL;
			}
			break;
		case 7:
			str=rsct_get_package_version();

			if(*response_len>=(l=strlen(str))+2)
			{
				memcpy(response,str,l);
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
		case 8:

			if(*response_len>=7  && GetReadersInputBufferSize()<=99999 || *response_len>=6 && GetReadersInputBufferSize()<=9999)
			{
				sprintf((char *)response,"%d",(int)GetReadersInputBufferSize());
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
		case 9:
			if(*response_len>=8)
			{
				memcpy(response,"424250\x90\x00",8);
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
	}

	if(m_ActiveProtocol==SCARD_PROTOCOL_T0 && m_bIsRF==false)
	{
		if(cmd_len==4)
		{
			uint8_t sbuffer[5];
			memcpy(sbuffer,cmd,4);
			sbuffer[4]=0;
			return ccidTransmit(sbuffer,5,response,response_len);
		}
		else if(cmd_len==5)
		{
			uint8_t rbuffer[258];
			uint8_t sbuffer[5];
			uint8_t Le;
			uint8_t La;
			uint16_t ges_len=0;
			uint16_t rlen=2;

			memcpy(sbuffer,cmd,4);
			rbuffer[0]=0x61;
			rbuffer[1]=cmd[4];

			while(rbuffer[rlen-2]==0x61) //while chaining
			{
				rbuffer[0]=0x6C;
				Le=rbuffer[rlen-1];
				rbuffer[1]=Le;
				rlen=2;

				while(rlen==2 && rbuffer[0]==0x6C) //while retransmit with other Length;
				{
					rlen=sizeof(rbuffer);
					sbuffer[4]=La=rbuffer[1];
					if((Result=ccidTransmit(sbuffer,5,rbuffer,&rlen))!=STATUS_SUCCESS)
					{
						return Result;
					}
					if(m_ApduNorm==NORM_PCSC)
						break;
					if(rlen<2)
					{
						*response_len=0;
						return STATUS_IO_TIMEOUT;
					}
					if(Le!=0 && Le<La)
					{
						memmove(rbuffer+Le,rbuffer+La,2);
						rlen=Le+2;
					}
				}
				if(ges_len+rlen>*response_len)
				{
					*response_len=0;
					return STATUS_BUFFER_TOO_SMALL;
				}
				memcpy(response,rbuffer,rlen);
				response+=rlen-2;
				ges_len+=rlen-2;
				memcpy(sbuffer,"\x00\xc0\x00\x00",4);
				if(m_ApduNorm==NORM_PCSC)
					break;
			}
			*response_len=ges_len+2;
			return STATUS_SUCCESS;
		}
		else if(cmd_len==5+cmd[4] && cmd[4]!=0)
		{
			if((Result=ccidTransmit(cmd,cmd_len,response,response_len))==STATUS_SUCCESS)
			{
				if(*response_len==2 && (response[0] & 0xf0)==0xc0)
					response[0]&=0x7f;
				else if(*response_len==2 && (response[0] & 0xf0)==0xb0)
					response[0]&=0xdf;
			}
			return Result;
		}
		else if(cmd_len==6+cmd[4] && cmd[4]!=0)
		{
			uint8_t sbuffer[5];
			uint8_t rbuffer[258];
			uint16_t rlen=sizeof(rbuffer);
			unsigned int tot_size=0;
			unsigned int rest_size=sizeof(rbuffer);
			uint8_t *rptr=rbuffer;
			if((Result=ccidTransmit(cmd,cmd_len-1,rbuffer,&rlen))!=STATUS_SUCCESS)
			{
				*response_len=0;
				return Result;
			}
			//			sbuffer[0]=cmd[0];
			sbuffer[0]=0;
			memcpy(sbuffer+1,"\xC0\x00\x00",3);
			rptr+=rlen-2;
			rest_size-=rlen-2;
			tot_size+=rlen-2;
			if(rlen==2 && (((rbuffer[0] & 0xf0)==0x90 && (rbuffer[0]!=0x90 || rbuffer[1]!=0x00)) || rbuffer[0]==0x62 || rbuffer[0]==0x63))
			{
				sbuffer[4]=cmd[cmd_len-1];
				rlen=rest_size;
				if((Result=ccidTransmit(sbuffer,5,rbuffer,&rlen))!=STATUS_SUCCESS)
				{
					*response_len=0;
					return Result;
				}
				rest_size-=rlen-2;
				tot_size+=rlen-2;
			}
			else if(rlen==2 && (rbuffer[0] & 0xf0)==0xc0)
				rbuffer[0]&=0x7f;
			else if(rlen==2 && (rbuffer[0] & 0xf0)==0xb0)
				rbuffer[0]&=0xdf;
			if(rlen >=2 && (rptr[rlen-2]==0x61 || rptr[rlen-2]==0x6C))
			{
				while(rlen >=2 && (rptr[rlen-2]==0x61 || rptr[rlen-2]==0x6C))
				{
					rptr+=rlen-2;
					if(cmd[cmd_len-1]<rptr[rlen-1] && cmd[cmd_len-1]!=0)
						sbuffer[4]=cmd[cmd_len-1];
					else
						sbuffer[4]=rptr[1];
					rlen=rest_size;
					if((Result=ccidTransmit(sbuffer,5,rptr,&rlen))!=STATUS_SUCCESS)
					{
						*response_len=0;
						return Result;
					}
					rest_size-=rlen-2;
					tot_size+=rlen-2;
				}
			}
			if(tot_size+2>*response_len)
			{
				*response_len=0;
				return STATUS_BUFFER_TOO_SMALL;
			}
			memcpy(response,rbuffer,tot_size+2);
			*response_len=tot_size+2;
			return STATUS_SUCCESS;
		}
		/*   else if(lenc==7 && cmd[4]==0)
		{
		}
		else if(lenc==7+(((uint16_t)cmd[5])<<8)+cmd[6] && cmd[4]==0 && lenc!=7)
		{
		}
		else if(lenc==9+(((uint16_t)cmd[5])<<8)+cmd[6] && cmd[4]==0 && lenc!=9)
		{
		}*/
		else
			return STATUS_INVALID_PARAMETER;
	}
	else
	{
		return ccidTransmit(cmd,cmd_len,response,response_len);
	}
}

CJ_RESULT CEC30Reader::CtSetSilentMode(bool boolMode,bool *pboolMode,uint32_t *Result)
{
	CJ_RESULT Res;
	uint8_t ResultMode;
	uint32_t Len;
	ResultMode=boolMode?1:0;
	Len=sizeof(ResultMode);
   if((Res=SetFlashMask())!=CJ_SUCCESS)
	{
		m_Owner->DebugLeveled(DEBUG_MASK_COMMUNICATION_ERROR,"Can't set Flashmask");
		return Res;
	}
   if((Res=Escape(MODULE_ID_KERNEL,CCID_ESCAPE_MODULE_SET_SILENT_MODE,&ResultMode,sizeof(ResultMode),Result,&ResultMode,&Len))!=CJ_SUCCESS)
	{
	  	m_Owner->DebugLeveled(DEBUG_MASK_COMMUNICATION_ERROR,"Silent mode not set");
	}
	*pboolMode=ResultMode?true:false;

	return Res;
}

CJ_RESULT CEC30Reader::CtGetSilentMode(bool *pboolMode,uint32_t *Result)
{
	uint32_t Res;
	uint8_t ResultMode;
	uint32_t Len;
	Len=sizeof(ResultMode);
   if((Res=Escape(MODULE_ID_KERNEL,CCID_ESCAPE_MODULE_SET_SILENT_MODE,&ResultMode,sizeof(ResultMode),Result,&ResultMode,&Len))!=CJ_SUCCESS)
	{
	  	m_Owner->DebugLeveled(DEBUG_MASK_COMMUNICATION_ERROR,"Error getting silent mode");
	}
	*pboolMode=ResultMode?true:false;
	return Res;
}

bool CEC30Reader::IsClass3(void)
{
	return true;
}

CJ_RESULT CEC30Reader::cjInput(uint8_t *key,uint8_t timeout,uint8_t *tag50,int tag50len)
{
	CJ_RESULT Res=CJ_ERR_WRONG_PARAMETER;
	uint32_t Len;
	uint32_t Result;
	uint8_t Error;
	uint32_t ErrorLen=sizeof(Error);

	Len=1;
	if(FindModule(MODULE_ID_MKT_COMP))
	{
		uint8_t buffer[65];
		if(tag50len>64)
			return CJ_ERR_RBUFFER_TO_SMALL;
		buffer[0]=timeout;
		if(tag50len)
		   memcpy(buffer+1,tag50,tag50len);
		if((Res=CtApplicationData(MODULE_ID_MKT_COMP,0,buffer,tag50len+1,&Result,key,&Len,&Error,&ErrorLen)))
		{
		  	m_Owner->DebugLeveled(DEBUG_MASK_COMMUNICATION_ERROR,"Error Input");
		}
	}
	return Res;
}

CJ_RESULT CEC30Reader::cjOutput(uint8_t timeout,uint8_t *tag50,int tag50len)
{
   CJ_RESULT Res;
	uint32_t Result;
	uint8_t Error;
	uint32_t ErrorLen=sizeof(Error);

	uint8_t buffer[65];
	if(tag50len>64)
		return CJ_ERR_RBUFFER_TO_SMALL;
	buffer[0]=timeout;
	memcpy(buffer+1,tag50,tag50len);
	if((Res=CtApplicationData(MODULE_ID_MKT_COMP,1,buffer,tag50len+1,&Result,0,0,&Error,&ErrorLen)))
	{
	  	m_Owner->DebugLeveled(DEBUG_MASK_COMMUNICATION_ERROR,"Error output");
	}

	return Res;
}

CJ_RESULT CEC30Reader::SpecialLess3_0_41()
{
   cj_ModuleInfo *Info=FindModule(MODULE_ID_KERNEL);
	if(Info==NULL)
		return CJ_ERR_LEN;
	if(Info->Version<0x30 || Info->Version==0x30 && Info->Revision<=40)
	{
			if(m_ReaderState==SCARD_ABSENT)
			  return CJ_ERR_NO_ICC;
			else if(m_ReaderState!=SCARD_SPECIFIC)
			  return CJ_ERR_NO_ACTIVE_ICC;
	}
	return CJ_SUCCESS;
}


int CEC30Reader::ExecuteApplSecureResult(uint8_t Error,uint32_t ErrorLength,uint8_t *in,int *in_len,uint8_t *RespData,uint32_t RespDataLen,int offs)
{
   CJ_RESULT Res=SpecialLess3_0_41();
   if(Res!=CJ_SUCCESS)
	   return Res;
   if(ErrorLength)
   {
      if(Error==XFR_PARITY_ERROR)
         return CJ_ERR_PARITY;
      else if(Error==ICC_MUTE)
         return CJ_ERR_TIMEOUT;
      else if(Error==PIN_TIMEOUT)
         return CJ_ERR_PIN_TIMEOUT;
      else if(Error==PIN_CANCELED)
         return CJ_ERR_PIN_CANCELED;
      else if(Error==PIN_DIFFERENT)
         return CJ_ERR_PIN_DIFFERENT;
      else if(Error==EXT_ERROR)
		{
		   if(*in_len<(int)RespDataLen)
				return CJ_ERR_RBUFFER_TO_SMALL;
		   memcpy(in,RespData,RespDataLen);
		   *in_len=RespDataLen;
         return CJ_ERR_PIN_EXTENDED;
		}
 	   else if(Error==5)
		  return CJ_ERR_WRONG_PARAMETER;
 	   else if(Error==21+offs)
		  return CJ_ERR_WRONG_PARAMETER;
	   else if(Error==26+offs)
		  return CJ_ERR_CONDITION_OF_USE;
      else if(Error==DEACTIVATED_PROTOCOL)
		{
			if(m_ReaderState==SCARD_ABSENT)
			  return CJ_ERR_NO_ICC;
			else if(m_ReaderState!=SCARD_SPECIFIC)
			  return CJ_ERR_NO_ACTIVE_ICC;
		}
      else
         return CJ_ERR_LEN;
   }
	if(*in_len<(int)RespDataLen)
	   return CJ_ERR_RBUFFER_TO_SMALL;
	memcpy(in,RespData,RespDataLen);
	*in_len=RespDataLen;
	return CJ_SUCCESS;
}

#ifdef IT_TEST
int CEC30Reader::cjccid_SecurePV(uint8_t Timeout,
                    uint8_t PinPosition,uint8_t PinType,
                    uint8_t PinLengthSize,uint8_t PinLength,
                    uint8_t PinLengthPosition,
                    uint8_t Min, uint8_t Max,
                    uint8_t Condition,uint8_t *Prologue,
                    uint8_t *out,int out_len,uint8_t *in,int *in_len,uint8_t Slot,uint8_t *Text,uint8_t Textlen,uint8_t bMessageIndex,uint8_t bNumberMessage)
#else
int CEC30Reader::cjccid_SecurePV(uint8_t Timeout,
                    uint8_t PinPosition,uint8_t PinType,
                    uint8_t PinLengthSize,uint8_t PinLength,
                    uint8_t PinLengthPosition,
                    uint8_t Min, uint8_t Max,
                    uint8_t Condition,uint8_t *Prologue,
                    uint8_t *out,int out_len,uint8_t *in,int *in_len,uint8_t *Text,uint8_t Textlen,uint8_t bMessageIndex,uint8_t bNumberMessage)
#endif
{
	if(Max>15)
		Max=15;
	if(Text==NULL || Textlen==0 || FindModule(MODULE_ID_MKT_COMP)==NULL)
#ifdef IT_TEST
		return base::cjccid_SecurePV(Timeout,
                    PinPosition,PinType,
                    PinLengthSize,PinLength,
                    PinLengthPosition,
                    Min,Max,
                    Condition,Prologue,
                    out,out_len,in,in_len,Slot,Text,Textlen,bMessageIndex,bNumberMessage);
#else
		return base::cjccid_SecurePV(Timeout,
                    PinPosition,PinType,
                    PinLengthSize,PinLength,
                    PinLengthPosition,
                    Min,Max,
                    Condition,Prologue,
                    out,out_len,in,in_len,Text,Textlen,bMessageIndex,bNumberMessage);
#endif
	else
	{
		uint32_t Result;
		uint8_t RespData[1000];
		uint8_t buffer[1000];
		uint32_t RespDataLen=sizeof(RespData);
		struct _CCID_Message::_Data::_Secure *Secure;
		uint8_t Error;
		uint32_t ErrorLength=sizeof(Error);
		int Res;

		buffer[0]=Textlen;
		memcpy(buffer+1,Text,Textlen);
		buffer[1+Textlen]=buffer[2+Textlen]=0;
		Secure=(struct _CCID_Message::_Data::_Secure *)(buffer+Textlen+3);

		Secure->bPINOperation=0;
		Secure->bTimeOut=Timeout;
		Secure->bmFormatString=(uint8_t)(0x80 | (PinPosition<<3) | PinType);
		Secure->bmPINBlockString=(uint8_t)((PinLengthSize<<4) | PinLength);
		Secure->bmPINLengthFormat=PinLengthPosition;
		Secure->Data.Verify.wPINMaxExtraDigit=HostToReaderShort((((uint16_t)Min)<<8)+Max);
		Secure->Data.Verify.bEntryValidationCondition=Condition;
		Secure->Data.Verify.bNumberMessage=bNumberMessage;
		Secure->Data.Verify.wLangId=HostToReaderShort(0x0409);
		Secure->Data.Verify.bMsgIndex=bMessageIndex;
		memcpy(Secure->Data.Verify.bTeoPrologue,Prologue,3);
		memcpy(Secure->Data.Verify.abData,out,out_len);
#ifdef _INSERT_KEY_EVENTS
		{
			uint8_t buffer[2];
			buffer[0]=RDR_TO_PC_KEYEVENT;
			buffer[1]=0xa0;
			DoInterruptCallback(buffer,2);
		}
#endif
		Res=CtApplicationData(MODULE_ID_MKT_COMP,2,buffer,15+out_len+Textlen+3,&Result,RespData,&RespDataLen,&Error,&ErrorLength);
#ifdef _INSERT_KEY_EVENTS
		if(Res==CJ_SUCCESS)
		{
			if(m_ReaderState==SCARD_SPECIFIC)
			{
				uint8_t buffer[2];
				buffer[0]=RDR_TO_PC_KEYEVENT;
			   if(ErrorLength==1 && Error==PIN_CANCELED)
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
		if(Res!=0)
			return Res;
		return ExecuteApplSecureResult(Error,ErrorLength,in,in_len,RespData,RespDataLen,0);
	}
}

#ifdef IT_TEST
int CEC30Reader::cjccid_SecureMV(uint8_t Timeout,
                    uint8_t PinPosition,uint8_t PinType,
                    uint8_t PinLengthSize,uint8_t PinLength,
                    uint8_t PinLengthPosition,
                    uint8_t Min, uint8_t Max,
                    uint8_t bConfirmPIN,
                    uint8_t Condition,uint8_t *Prologue,
                    uint8_t OffsetOld,uint8_t OffsetNew,
                    uint8_t *out,int out_len,uint8_t *in,int *in_len,uint8_t Slot,int TextCount,uint8_t *Text[3],uint8_t Textlen[3],uint8_t bMessageIndex[3],uint8_t bNumberMessage)
#else
int CEC30Reader::cjccid_SecureMV(uint8_t Timeout,
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
	if(Max>15)
		Max=15;
	if(Text==NULL || Textlen==0 || TextCount==0 || FindModule(MODULE_ID_MKT_COMP)==NULL)
	{
#ifdef IT_TEST
		return base::cjccid_SecureMV(Timeout,PinPosition,PinType,PinLengthSize,PinLength,
											  PinLengthPosition,Min,Max,bConfirmPIN,Condition,Prologue,OffsetOld,OffsetNew,
											  out,out_len,in,in_len,Slot,TextCount,Text,Textlen,bMessageIndex,bNumberMessage);
#else
		return base::cjccid_SecureMV(Timeout,PinPosition,PinType,PinLengthSize,PinLength,
											  PinLengthPosition,Min,Max,bConfirmPIN,Condition,Prologue,OffsetOld,OffsetNew,
											  out,out_len,in,in_len,TextCount,Text,Textlen,bMessageIndex,bNumberMessage);
#endif
	}
	else
	{
		uint32_t Result;
		uint8_t RespData[1000];
		uint8_t buffer[1000];
		uint32_t RespDataLen=sizeof(RespData);
		uint8_t *ptr;
		struct _CCID_Message::_Data::_Secure *Secure;
		uint8_t Error;
		uint32_t ErrorLength=sizeof(Error);
		int i;
   	int Res;
		uint32_t len=0;

		for(i=0,ptr=buffer;i<TextCount;i++)
		{
			*ptr++=Textlen[i];
			memcpy(ptr,Text[i],Textlen[i]);
			ptr+=Textlen[i];
			len+=Textlen[i];
		}
		for(;i<3;i++)
		{
			*ptr++=0;
		}

		Secure=(struct _CCID_Message::_Data::_Secure *)ptr;
	

		Secure->bPINOperation=1;
		Secure->bTimeOut=Timeout;
		Secure->bmFormatString=(uint8_t)(0x80 | (PinPosition<<3) | PinType);
		Secure->bmPINBlockString=(uint8_t)((PinLengthSize<<4) | PinLength);
		Secure->bmPINLengthFormat=PinLengthPosition;
		Secure->Data.Modify.bInsertionOffsetOld=OffsetOld;
		Secure->Data.Modify.bInsertionOffsetNew=OffsetNew;
		Secure->Data.Modify.wPINMaxExtraDigit=HostToReaderShort((((uint16_t)Min)<<8)+Max);
		Secure->Data.Modify.bConfirmPIN= bConfirmPIN;
		Secure->Data.Modify.bEntryValidationCondition=Condition;
		Secure->Data.Modify.bNumberMessage=bNumberMessage;
		Secure->Data.Modify.wLangId=HostToReaderShort(0x0409);
		Secure->Data.Modify.bMsgIndex1=bMessageIndex[0];
		Secure->Data.Modify.bMsgIndex2=bMessageIndex[1];
		Secure->Data.Modify.bMsgIndex3=bMessageIndex[2];
		memcpy(Secure->Data.Modify.bTeoPrologue,Prologue,3);
		memcpy(Secure->Data.Modify.abData,out,out_len);
#ifdef _INSERT_KEY_EVENTS
		{
			uint8_t buffer[2];
			buffer[0]=RDR_TO_PC_KEYEVENT;
			buffer[1]=0xa0;
			DoInterruptCallback(buffer,2);
		}
#endif
		Res=CtApplicationData(MODULE_ID_MKT_COMP,2,buffer,20+out_len+len+3,&Result,RespData,&RespDataLen,&Error,&ErrorLength);

	#ifdef _INSERT_KEY_EVENTS
		if(Res==CJ_SUCCESS)
		{
			if(m_ReaderState==SCARD_SPECIFIC)
			{
				uint8_t buffer[2];
				buffer[0]=RDR_TO_PC_KEYEVENT;
			   if(ErrorLength==1 && Error==PIN_CANCELED)
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
		if(Res!=0)
			return Res;
		return ExecuteApplSecureResult(Error,ErrorLength,in,in_len,RespData,RespDataLen,5);
	}
}

CJ_RESULT CEC30Reader::SetSyncParameters(uint8_t AddrByteCount, uint16_t PageSize)
{
	CCID_Message Message;
	CCID_Response Response;
	memset(&Message,0,sizeof(Message));
	Message.bMessageType=PC_TO_RDR_SETPARAMETERS;
	Message.Header.SetParameters.bProtocolNum=2;
	Message.dwLength=sizeof(Message.Data.SetParameters.Sync);
	Message.Data.SetParameters.Sync.AddrByteCount=AddrByteCount;
	Message.Data.SetParameters.Sync.PageSize=HostToReaderShort(PageSize);
	return Transfer(&Message,&Response);
}



CJ_RESULT CEC30Reader::KTLightCall(uint8_t *sad,uint8_t *dad,uint8_t *cmd, uint16_t lenc,int32_t Lc,uint8_t *data_ptr,int32_t Le,uint8_t *response,uint16_t *lenr)
{
	uint16_t Function=cmd[1];
	uint32_t Result;
	uint32_t ResponseLen=*lenr-2;
	uint8_t ApplicationError[6];
	uint32_t ApplicationErrorLength=sizeof(ApplicationError);
	CJ_RESULT Res;

	if(cmd[2]!=00 || cmd[3]!=00)
	{
		response[0]=0x6a;
		response[1]=0x00;
		*lenr=2;
		return 0;
	}
	if(Lc==-1)
	{
		response[0]=0x67;
		response[1]=0x00;
		*lenr=2;
		return 0;
	}
	if(cmd[1]==0x72 || cmd[1]==0x73 || cmd[1]==0x75 || cmd[1]==0x76)
	{
		if(Le!=-1)
		{
			response[0]=0x6c;
			response[1]=0x00;
			*lenr=2;
			return 0;
		}
	}
	else if(Le!=0)
	{
		response[0]=0x6c;
		response[1]=0x00;
		*lenr=2;
		return 0;
	}
	else
		lenc--;
	memmove(cmd,cmd+3,lenc-3);
	lenc-=3;
	memmove(cmd+1,data_ptr-3,Lc);
	lenc--;
	if(FindModule(MODULE_ID_KT_LIGHT))
	{
	   Res=CtApplicationData(MODULE_ID_KT_LIGHT,Function-0x70,cmd,Lc+1,&Result,response,&ResponseLen,ApplicationError,&ApplicationErrorLength);
	}
	else
	{
	   Res=CtApplicationData(MODULE_ID_KT_LIGHT_GC,Function-0x70,cmd,Lc+1,&Result,response,&ResponseLen,ApplicationError,&ApplicationErrorLength);
	}

	if(Res==CJ_ERR_CHECK_RESULT)
	{
		response[0]=0x69;
		response[1]=0x85;
		*lenr=2;
		return CJ_SUCCESS;
	}
	if(Res!=CJ_SUCCESS)
	{
		*lenr=0;
		return Res;
	}
	if(ApplicationErrorLength>2)
	{
		*lenr=0;
		return CJ_ERR_WRONG_ANSWER;
	}
	memcpy(response+ResponseLen,ApplicationError,ApplicationErrorLength);
	*lenr=(uint16_t)(ResponseLen+ApplicationErrorLength);
	*dad=2;
	*sad=1;
	return CJ_SUCCESS;
}	



int CEC30Reader::ExecuteSecureResult(CCID_Response *Response,uint8_t *in,int *in_len,int offs)
{
   cj_ModuleInfo *Info=FindModule(MODULE_ID_KERNEL);
	if(Info==NULL)
		return CJ_ERR_LEN;
	if(Info->Version<0x30 || Info->Version==0x30 && Info->Revision<=40)
		return base::ExecuteSecureResult(Response,in,in_len,offs);
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

CJ_RESULT CEC30Reader::SetFlashMask(void)
{
	return CJ_SUCCESS;
}

bool CEC30Reader::HastModulestoreInfo()
{
	cj_ModuleInfo *Info=FindModule(MODULE_ID_KERNEL);
	return (Info->Version>0x30 || Info->Version==0x30 && Info->Revision>=43);
}



CJ_RESULT CEC30Reader::CtSetModulestoreInfo(uint8_t *StoreInfo,uint8_t InfoLength)
{
	uint32_t Result;

	if(!HastModulestoreInfo())
		return base::CtSetModulestoreInfo(StoreInfo,InfoLength);
   if(SetFlashMask()!=CJ_SUCCESS)
		m_Owner->DebugLeveled(DEBUG_MASK_COMMUNICATION_ERROR,"Can't set Flashmask");
   return Escape(MODULE_ID_KERNEL,CCID_ESCAPE_SET_MODULESTORE_INFO,StoreInfo,InfoLength,&Result,0,0);

}

CJ_RESULT CEC30Reader::CtGetModulestoreInfo(uint8_t *StoreInfo,uint8_t *InfoLength)
{
	CJ_RESULT Res;
	uint32_t Result;
	uint32_t Len=*InfoLength;
	if(!HastModulestoreInfo())
		return base::CtGetModulestoreInfo(StoreInfo,InfoLength);
   Res=Escape(MODULE_ID_KERNEL,CCID_ESCAPE_GET_MODULESTORE_INFO,NULL,0,&Result,StoreInfo,&Len);
	if(Res==CJ_SUCCESS || Res==CJ_ERR_RBUFFER_TO_SMALL && StoreInfo==NULL && InfoLength!=NULL)
	   *InfoLength=(uint8_t)Len;
	return Res;
}

CJ_RESULT CEC30Reader::SetSMModeAndCount(uint32_t ModuleID,uint32_t Count)
{
	uint32_t Result;
	tSMSelect SMSelect;
	SMSelect.ModuleID=HostToReaderLong(ModuleID);
	SMSelect.Count=HostToReaderLong(Count);
	return Escape(MODULE_ID_KERNEL,CCID_ESCAPE_SELECT_SM_MODULE,(uint8_t *)&SMSelect,sizeof(SMSelect),&Result,0,0);
}

