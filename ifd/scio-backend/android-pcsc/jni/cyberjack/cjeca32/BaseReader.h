
#ifndef ECA_BASEREADER_H
#define ECA_BASEREADER_H


#include "cjeca32.h"
#include "BaseCommunication.h"

class CReader;
class CBaseCommunication;


class CBaseReader
{
public:
	CBaseReader(CReader *Owner,CBaseCommunication *Communicator);
	virtual CJ_RESULT PostCreate();
	virtual ~CBaseReader(void);
	void Unconnect(void);


public:
	virtual char CtData(uint8_t *sad,uint8_t *dad,const uint8_t *cmd, uint16_t cmd_len,uint8_t *response,uint16_t *response_len);

public:
	RSCT_IFD_RESULT IfdPower(uint32_t Mode,uint8_t *ATR,uint32_t *ATR_Length);
	virtual RSCT_IFD_RESULT IfdSetProtocol(uint32_t *Protocol);
	virtual RSCT_IFD_RESULT IfdGetState(uint32_t *State);
	virtual RSCT_IFD_RESULT IfdSetAttribute(const uint8_t *Input,uint32_t InputLength);
	virtual RSCT_IFD_RESULT IfdGetAttribute(uint32_t Tag,uint8_t *Attribute,uint32_t *AttributeLength);
	virtual RSCT_IFD_RESULT IfdSwallow();
	virtual RSCT_IFD_RESULT IfdEject();
	virtual RSCT_IFD_RESULT IfdVendor(uint32_t IoCtrlCode,uint8_t *Input,uint32_t InputLength,uint8_t *Output,uint32_t *OutputLength);
	virtual RSCT_IFD_RESULT IfdTransmit(const uint8_t *cmd, uint16_t cmd_len,uint8_t *response,uint16_t *response_len);
	virtual RSCT_IFD_RESULT IfdIoControl(uint32_t IoCtrlCode,uint8_t *Input,uint32_t InputLength,uint8_t *Output,uint32_t *OutputLength);

protected:
	virtual RSCT_IFD_RESULT IfdPower(uint32_t Mode,uint8_t *ATR,uint32_t *ATR_Length,uint32_t Timeout);
	int Write(void *Message,uint32_t len);
	int Read(void *Response,uint32_t *ResponseLen);
   uint8_t *GetTag(uint8_t *start,int len,uint8_t tagvalue,int *taglen);
	virtual CJ_RESULT SetSyncParameters(uint8_t AddrByteCount, uint16_t PageSize)=0;
	virtual uint32_t GetReadersInputBufferSize()=0;



	
public:
	virtual CJ_RESULT CtLoadModule(uint8_t *pData,uint32_t DataLength,uint8_t *pSgn,uint32_t SgnLength,uint32_t *Result);
	virtual CJ_RESULT CtKeyUpdate(uint8_t *pData,uint32_t DataLength,uint32_t *Result);
	virtual CJ_RESULT CtIsKeyUpdateRecommended(uint8_t *pData,uint32_t DataLength,uint32_t *EstimatedUpdateTime);
	virtual CJ_RESULT CtDeleteModule(uint32_t ModuleID,uint32_t *Result);
	virtual CJ_RESULT CtActivateModule(uint32_t ModuleID,uint32_t *Result);
	virtual CJ_RESULT CtDeactivateModule(uint32_t ModuleID,uint32_t *Result);
	virtual CJ_RESULT CtDeleteALLModules(uint32_t *Result);
	CJ_RESULT CtListModules(uint32_t *Count,cj_ModuleInfo *ModuleInfo);
	virtual CJ_RESULT CtGetModuleInfoFromFile(uint8_t *pData,uint32_t DataLength,cj_ModuleInfo *ModuleInfo,uint32_t *EstimatedUpdateTime);
	CJ_RESULT CtFreeModuleInfoList(cj_ModuleInfo *pModuleInfo);
	virtual CJ_RESULT CtIsModuleUpdateRecommended(uint8_t *pData,uint32_t DataLength,uint32_t *EstimatedUpdateTime);
	virtual CJ_RESULT CtGetActiveModuleID(uint32_t *ID,uint32_t *Result);
	virtual CJ_RESULT CtGetActivationID(uint32_t *ID,uint32_t *Result);
	virtual CJ_RESULT CtApplicationData(uint32_t ApplicationID,uint16_t Function,uint8_t *InputData, uint32_t InputLen, uint32_t *Result, uint8_t *ResponseData, uint32_t *ResponseLen, uint8_t *ApplicationError,uint32_t *ApplicationErrorLength);
	virtual CJ_RESULT CtSelfTest(void);
	virtual CJ_RESULT CtShowAuth(void);
	virtual CJ_RESULT CtSetContrast(EContrast eContrast,uint32_t *Result);
	virtual CJ_RESULT CtSetBacklight(EBacklight eBacklight,uint32_t *Result);
	virtual CJ_RESULT CtSetSilentMode(bool boolMode,bool *pboolMode,uint32_t *Result);
	virtual CJ_RESULT CtGetSilentMode(bool *pboolMode,uint32_t *Result);
	virtual CJ_RESULT CtSetModulestoreInfo(uint8_t *Info,uint8_t InfoLength);
	virtual CJ_RESULT CtGetModulestoreInfo(uint8_t *Info,uint8_t *InfoLength);

   CJ_RESULT CtSetAPDUNorm(const EApduNorm Norm);


	
	virtual CJ_RESULT InstallAndStartIFDHandler();
	virtual CJ_RESULT StopIFDHandler();
	virtual CJ_RESULT IntroduceReaderGroups();



	CJ_RESULT CtGetReaderInfo(cj_ReaderInfo *pReaderInfo);

protected:
	virtual CJ_RESULT BuildReaderInfo()=0;
	virtual CJ_RESULT BuildModuleInfo()=0;

	virtual uint16_t HostToReaderShort(uint16_t Value) = 0;
	virtual uint32_t HostToReaderLong(uint32_t Value) = 0;
	virtual uint16_t ReaderToHostShort(uint16_t Value);
	virtual uint32_t ReaderToHostLong(uint32_t Value);
	uint16_t InversByteOrderShort(uint16_t Value);
	uint32_t InversByteOrderLong(uint32_t Value);
	virtual void SetHWString(char *String)=0;

   cj_ModuleInfo *FindModule(uint32_t ModuleID);
	cj_ModuleInfo *FindModuleWithMask(uint32_t ModuleID,uint32_t Mask);

	bool IsConnected();
	bool IsNotSet(void *ptr,int len);
	int AnalyseATR(bool warm);


	virtual void ConnectionError();
	bool m_bIsRF;


public:
	uint32_t GetEnviroment(const char *Name,uint32_t Default);
	void SetChangeInterruptCallback(fctChangeIntCallback ChangeIntCallback,ctxPtr ChangeOwner);
	void SetKeyInterruptCallback(fctKeyIntCallback KeyIntCallback,ctxPtr KeyOwner);
	virtual void DoInterruptCallback(uint8_t *Data,uint32_t ulDataLen)=0;


	
protected:
	CBaseCommunication *m_pCommunicator;
	cj_ReaderInfo m_ReaderInfo;
   cj_ModuleInfo *m_pModuleInfo;
	uint32_t m_ModuleInfoCount;
	CReader *m_Owner;
	uint32_t m_ReaderState;
	uint32_t m_ActiveProtocol;
	ctxPtr m_KeyCallbackCtx;
	fctKeyIntCallback m_KeyIntCallback;
	ctxPtr m_ChangeCallbackCtx;
	fctChangeIntCallback m_ChangeIntCallback;

	uint8_t m_ATR[33];
	uint32_t m_ATR_Length;
	uint8_t *m_Historical;
	uint32_t m_Historical_Length;
	uint8_t m_TA1; //speed
	uint8_t m_TC1; //XGT
	uint8_t m_TC2; //WT
	uint8_t m_TA3; //IFSC
	uint8_t m_TB3; //BWI/CWI
	uint8_t m_TC3; //LRC/CRC
	uint32_t m_PossibleProtocols;
	EApduNorm m_ApduNorm;

	uint8_t m_reader_path[10];
   uint8_t m_reader_file[2];
   uint8_t m_iic_deviceaddr;
   uint8_t m_iic_offset_bytes;
   uint8_t m_iic_pagesize;
   uint8_t m_reader_path_len;
private:
   int check_len(uint8_t *atr,uint32_t buf_len,uint8_t **historical,uint32_t *hist_len);




};

#endif

