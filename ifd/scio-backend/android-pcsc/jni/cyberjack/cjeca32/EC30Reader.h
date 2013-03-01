#ifndef ECA_EC30READER_H
#define ECA_EC30READER_H

#include "CCIDReader.h"

class CEC30Reader :
	public CCCIDReader
{
public:
	CEC30Reader(CReader *Owner,CBaseCommunication *Communicator);
	virtual CJ_RESULT PostCreate();

public:
	~CEC30Reader(void);
	virtual CJ_RESULT CtLoadModule(uint8_t *pData,uint32_t DataLength,uint8_t *pSgn,uint32_t SgnLength,uint32_t *Result);
	virtual CJ_RESULT CtGetModuleInfoFromFile(uint8_t *pData,uint32_t DataLength,cj_ModuleInfo *ModuleInfo,uint32_t *EstimatedUpdateTime);
	virtual CJ_RESULT CtDeleteALLModules(uint32_t *Result);
	virtual CJ_RESULT CtDeleteModule(uint32_t ModuleID,uint32_t *Result);
	virtual CJ_RESULT CtActivateModule(uint32_t ModuleID,uint32_t *Result);
	virtual CJ_RESULT CtDeactivateModule(uint32_t ModuleID,uint32_t *Result);
	virtual CJ_RESULT CtKeyUpdate(uint8_t *pData,uint32_t DataLength,uint32_t *Result);
	virtual CJ_RESULT CtIsKeyUpdateRecommended(uint8_t *pData,uint32_t DataLength,uint32_t *EstimatedUpdateTime);
	virtual CJ_RESULT CtSelfTest(void);
	virtual CJ_RESULT CtShowAuth(void);


	virtual RSCT_IFD_RESULT IfdTransmit(const uint8_t *cmd, uint16_t cmd_len,uint8_t *response,uint16_t *response_len);
	virtual CJ_RESULT CtSetSilentMode(bool boolMode,bool *pboolMode,uint32_t *Result);
	virtual CJ_RESULT CtGetSilentMode(bool *pboolMode,uint32_t *Result);
	virtual CJ_RESULT CtSetModulestoreInfo(uint8_t *Info,uint8_t InfoLength);
	virtual CJ_RESULT CtGetModulestoreInfo(uint8_t *Info,uint8_t *InfoLength);







protected:
	virtual RSCT_IFD_RESULT IfdPower(uint32_t Mode, uint8_t *ATR, uint32_t *ATR_Length, uint32_t Timeout);
  virtual bool ATRFilter(bool IsWarm);
	virtual int Escape(uint32_t ApplicationID,uint16_t Function,uint8_t *InputData, uint32_t InputLen, uint32_t *Result, uint8_t *ResponseData, uint32_t *ResponseLen);
	virtual CJ_RESULT CtApplicationData(uint32_t ApplicationID,uint16_t Function,uint8_t *InputData, uint32_t InputLen, uint32_t *Result, uint8_t *ResponseData, uint32_t *ResponseLen, uint8_t *ApplicationError,uint32_t *ApplicationErrorLength);
	virtual CJ_RESULT BuildReaderInfo();
	virtual CJ_RESULT BuildModuleInfo();
	virtual bool HastModulestoreInfo();
	virtual void SetSerialNumber(void);
	virtual void SetDate(uint8_t Nr);
	bool SetReaderConstants(void);
	virtual RSCT_IFD_RESULT _IfdTransmit(const uint8_t *cmd, uint16_t cmd_len,uint8_t *response,uint16_t *response_len);
	virtual RSCT_IFD_RESULT ccidTransmit(const uint8_t *cmd, uint16_t cmd_len,uint8_t *response,uint16_t *response_len);
	CJ_RESULT _CtSetContrast(uint8_t Value,uint32_t *Result);
	CJ_RESULT _CtSetBacklight(uint8_t Value,uint32_t *Result);
	virtual bool IsClass3(void);
	virtual CJ_RESULT cjInput(uint8_t *key,uint8_t timeout,uint8_t *tag52,int tag52len);
	virtual CJ_RESULT cjOutput(uint8_t timeout,uint8_t *tag52,int tag52len);
	virtual CJ_RESULT SetSyncParameters(uint8_t AddrByteCount, uint16_t PageSize);
   virtual CJ_RESULT KTLightCall(uint8_t *sad,uint8_t *dad,uint8_t *cmd, uint16_t lenc,int32_t Lc,uint8_t *data_ptr,int32_t Le,uint8_t *response,uint16_t *lenr);
	virtual int ExecuteSecureResult(CCID_Response *Response,uint8_t *in,int *in_len,int offs);
	virtual CJ_RESULT SetFlashMask(void);
	virtual int GetWarmstartTimeout(void);
   virtual CJ_RESULT SetSMModeAndCount(uint32_t ModuleID,uint32_t Count);
	virtual uint32_t GetReadersInputBufferSize();
	virtual CJ_RESULT SpecialLess3_0_41();




#ifdef IT_TEST
	virtual int cjccid_SecurePV(uint8_t Timeout,
                    uint8_t PinPosition,uint8_t PinType,
                    uint8_t PinLengthSize,uint8_t PinLength,
                    uint8_t PinLengthPosition,
                    uint8_t Min, uint8_t Max,
                    uint8_t Condition,uint8_t *Prologue,
                    uint8_t *out,int out_len,uint8_t *in,int *in_len,uint8_t Slot,uint8_t *Text,uint8_t Textlen,uint8_t bMessageIndex,uint8_t bNumberMessage);
	virtual int cjccid_SecureMV(uint8_t Timeout,
                    uint8_t PinPosition,uint8_t PinType,
                    uint8_t PinLengthSize,uint8_t PinLength,
                    uint8_t PinLengthPosition,
                    uint8_t Min, uint8_t Max,
                    uint8_t bConfirmPIN,
                    uint8_t Condition,uint8_t *Prologue,
                    uint8_t OffsetOld,uint8_t OffsetNew,
                    uint8_t *out,int out_len,uint8_t *in,int *in_len,uint8_t Slot, int TextCount,uint8_t *Text[3],uint8_t Textlen[3],uint8_t bMessageIndex[3],uint8_t bNumberMessage);
#else
	virtual int cjccid_SecurePV(uint8_t Timeout,
                    uint8_t PinPosition,uint8_t PinType,
                    uint8_t PinLengthSize,uint8_t PinLength,
                    uint8_t PinLengthPosition,
                    uint8_t Min, uint8_t Max,
                    uint8_t Condition,uint8_t *Prologue,
                    uint8_t *out,int out_len,uint8_t *in,int *in_len,uint8_t *Text,uint8_t Textlen,uint8_t bMessageIndex,uint8_t bNumberMessage);
	virtual int cjccid_SecureMV(uint8_t Timeout,
                    uint8_t PinPosition,uint8_t PinType,
                    uint8_t PinLengthSize,uint8_t PinLength,
                    uint8_t PinLengthPosition,
                    uint8_t Min, uint8_t Max,
                    uint8_t bConfirmPIN,
                    uint8_t Condition,uint8_t *Prologue,
                    uint8_t OffsetOld,uint8_t OffsetNew,
                    uint8_t *out,int out_len,uint8_t *in,int *in_len,int TextCount,uint8_t *Text[3],uint8_t Textlen[3],uint8_t bMessageIndex[3],uint8_t bNumberMessage);
#endif





	uint8_t *m_pApplicationResponse;
	uint32_t m_nApplicationResponseLength;





private:
	CJ_RESULT GetReaderInfo(cjeca_Info *Info);
	CJ_RESULT GetKeyInfo(tKeyInfo *Keys,uint32_t len);
	CJ_RESULT GetSecoderInfo(tSecoderInfo *Info,uint32_t len);
	CJ_RESULT GetModuleIDs(uint32_t *Count,uint32_t *IDs);
	CJ_RESULT GetModuleInfo(uint32_t ID,cj_ModuleInfo *Info);
    bool _CtIsKeyUpdateRecommended(uint8_t *pData,uint32_t DataLength,uint32_t *EstimatedUpdateTime,uint8_t *KV,CJ_RESULT &Res);
	int ExecuteApplSecureResult(uint8_t Error,uint32_t ErrorLength,uint8_t *in,int *in_len,uint8_t *RespData,uint32_t RespDataLen,int offs);

	unsigned char *SecoderBuffer;
	uint32_t SecoderBufferLen;






};

#endif
