#pragma once



class CBaseReader;
class CRSCTCriticalSection;

#include "cjeca32.h"

#ifdef _MAC
# define CREADER_CLASS_EXPORT
#else
# define CREADER_CLASS_EXPORT  CJECA32_API
#endif

class CREADER_CLASS_EXPORT CReader
{
public:
	CReader(char *cDeviceName);
	CReader(const char *cDeviceName);
	virtual ~CReader(void);
	CJ_RESULT Connect();
   CJ_RESULT CreateVirtualReaderObject(const char *cReaderName);
	CJ_RESULT Disonnect();
private:
	void CReaderConstructor(const char *cDeviceName);

public:
	char CtData(uint8_t *dad,uint8_t *sad, uint16_t cmd_len, const uint8_t *cmd, uint16_t *response_len, uint8_t *response);

public:
	RSCT_IFD_RESULT IfdPower(uint32_t Mode,uint8_t *ATR,uint32_t *ATR_Length);
	RSCT_IFD_RESULT IfdSetProtocol(uint32_t *Protocol);
	RSCT_IFD_RESULT IfdGetState(uint32_t *State);
	RSCT_IFD_RESULT IfdSetAttribute(const uint8_t *Input,uint32_t InputLength);
	RSCT_IFD_RESULT IfdGetAttribute(uint32_t Tag,uint8_t *Attribute,uint32_t *AttributeLength);
	RSCT_IFD_RESULT IfdSwallow();
	RSCT_IFD_RESULT IfdEject();
	RSCT_IFD_RESULT IfdVendor(uint32_t IoCtrlCode,uint8_t *Input,uint32_t InputLength,uint8_t *Output,uint32_t *OutputLength);
	RSCT_IFD_RESULT IfdTransmit(const uint8_t *cmd, uint16_t cmd_len,uint8_t *response,uint16_t *response_len);
	RSCT_IFD_RESULT IfdIoControl(uint32_t IoCtrlCode,uint8_t *Input,uint32_t InputLength,uint8_t *Output,uint32_t *OutputLength);

	
public:
	CJ_RESULT CtLoadModule(uint8_t *pData,uint32_t DataLength,uint8_t *pSgn,uint32_t SgnLength,uint32_t *Result);
	CJ_RESULT CtKeyUpdate(uint8_t *pData,uint32_t DataLength,uint32_t *Result);
	CJ_RESULT CtIsKeyUpdateRecommended(uint8_t *pData,uint32_t DataLength,uint32_t *EstimatedUpdateTime);
	CJ_RESULT CtDeleteModule(uint32_t ModuleID,uint32_t *Result);
	CJ_RESULT CtActivateModule(uint32_t ModuleID,uint32_t *Result);
	CJ_RESULT CtDeactivateModule(uint32_t ModuleID,uint32_t *Result);
	CJ_RESULT CtDeleteALLModules(uint32_t *Result);
	CJ_RESULT CtListModules(uint32_t *Count,cj_ModuleInfo *ModuleInfo);
	CJ_RESULT CtGetModuleInfoFromFile(uint8_t *pData,uint32_t DataLength,cj_ModuleInfo *ModuleInfo,uint32_t *EstimatedUpdateTime);
	CJ_RESULT CtFreeModuleInfoList(cj_ModuleInfo *pModuleInfo);
	CJ_RESULT CtIsModuleUpdateRecommended(uint8_t *pData,uint32_t DataLength,uint32_t *EstimatedUpdateTime);
	CJ_RESULT CtGetActiveModuleID(uint32_t *ID,uint32_t *Result);
	CJ_RESULT CtGetActivationID(uint32_t *ID,uint32_t *Result);
	CJ_RESULT CtApplicationData(uint32_t ApplicationID,uint16_t Function,uint8_t *InputData, uint32_t InputLen, uint32_t *Result, uint8_t *ResponseData, uint32_t *ResponseLen);
	CJ_RESULT CtApplicationDataEx(uint32_t ApplicationID,uint16_t Function,uint8_t *InputData, uint32_t InputLen, uint32_t *Result, uint8_t *ResponseData, uint32_t *ResponseLen, uint8_t *ApplicationError,uint32_t *ApplicationErrorLength);
	CJ_RESULT CtSelfTest(void);
	CJ_RESULT CtShowAuth(void);
	CJ_RESULT CtSetAPDUNorm(const EApduNorm Norm);
	CJ_RESULT CtSetContrast(EContrast eContrast,uint32_t *Result);
	CJ_RESULT CtSetBacklight(EBacklight eBacklight,uint32_t *Result);
	CJ_RESULT CtSetSilentMode(bool boolMode,bool *pboolMode,uint32_t *Result);
	CJ_RESULT CtGetSilentMode(bool *pboolMode,uint32_t *Result);
	CJ_RESULT CtSetModulestoreInfo(uint8_t *Info,uint8_t InfoLength);
	CJ_RESULT CtGetModulestoreInfo(uint8_t *Info,uint8_t *InfoLength);


	
	CJ_RESULT InstallAndStartIFDHandler();
	CJ_RESULT StopIFDHandler();
	CJ_RESULT IntroduceReaderGroups();



	CJ_RESULT CtGetReaderInfo(cj_ReaderInfo *pReaderInfo);

public:
	void SetChangeInterruptCallback(fctChangeIntCallback ChangeIntCallback,ctxPtr ChangeOwner);
	void SetKeyInterruptCallback(fctKeyIntCallback KeyIntCallback,ctxPtr KeyOwner);

	void DebugResult(const char *format, ...);
	void DebugErrorSW1SW2(const char *format, ...);
	void DebugLeveled(uint32_t Mask,const char *format, ...);
	CRSCTCriticalSection *CritSec;

private:
	CBaseReader *m_Reader;
	void CheckcJResult(CJ_RESULT Result);
	char *m_cDeviceName;

};

