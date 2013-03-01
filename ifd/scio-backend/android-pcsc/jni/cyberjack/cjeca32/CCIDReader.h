#ifndef ECA_CCIDREADER_H
#define ECA_CCIDREADER_H

#include "ccid.h"
#include "BaseReader.h"


class CCCIDReader : public CBaseReader
{
public:
	CCCIDReader(CReader *Owner,CBaseCommunication *Communication);
	virtual ~CCCIDReader(void);

public:
//	virtual char CtData(uint8_t *sad,uint8_t *dad,const uint8_t *cmd, uint16_t cmd_len,uint8_t *response,uint16_t *response_len);


public:
	virtual RSCT_IFD_RESULT IfdSetProtocol(uint32_t *Protocol);
	virtual RSCT_IFD_RESULT IfdGetState(uint32_t *State);
	virtual RSCT_IFD_RESULT IfdVendor(uint32_t IoCtrlCode,uint8_t *Input,uint32_t InputLength,uint8_t *Output,uint32_t *OutputLength);

	

//	virtual RSCT_IFD_RESULT IfdSetAttribute(const uint8_t *Input,uint32_t InputLength);
//	virtual RSCT_IFD_RESULT IfdGetAttribute(uint32_t Tag,uint32_t *Attribute);
//	virtual RSCT_IFD_RESULT IfdVendor(uint32_t IoCtrlCode,uint8_t *Input,uint32_t InputLength,uint8_t *Output,uint32_t *OutputLength);
//	virtual RSCT_IFD_RESULT IfdTransmit(const uint8_t *cmd, uint16_t cmd_len,uint8_t *response,uint16_t *response_len);
//	virtual RSCT_IFD_RESULT IfdIoControl(uint32_t IoCtrlCode,uint8_t *Input,uint32_t InputLength,uint8_t *Output,uint32_t *OutputLength);

	virtual char CtData(uint8_t *sad,uint8_t *dad,const uint8_t *cmd, uint16_t cmd_len,uint8_t *response,uint16_t *response_len);

protected:
//	virtual RSCT_IFD_RESULT IfdPower(uint32_t Mode,uint8_t *ATR,uint32_t *ATR_Length,uint32_t Timeout);
	virtual void DoInterruptCallback(uint8_t *Data,uint32_t ulDataLen);
	virtual void SetSerialNumber(void)=0;
	virtual void SetDate(uint8_t Nr)=0;
	virtual RSCT_IFD_RESULT _IfdTransmit(const uint8_t *cmd, uint16_t cmd_len,uint8_t *response,uint16_t *response_len)=0;
	virtual void FillTeoPrologue(uint8_t *pbTeoPrologue);

	RSCT_IFD_RESULT IfdVerifyPinDirect(PIN_VERIFY_STRUCTURE *Input,uint8_t *Output,uint32_t *OutputLength);
	RSCT_IFD_RESULT IfdModifyPinDirect(PIN_MODIFY_STRUCTURE *Input,uint8_t *Output,uint32_t *OutputLength);
	virtual void GetProductString(uint8_t *Product)=0;
	virtual bool IsClass2(void);
	virtual bool IsClass3(void);
	virtual bool CheckUpdate(void);
   void TransformText(uint8_t *tag,int len);
	virtual CJ_RESULT cjInput(uint8_t *key,uint8_t timeout,uint8_t *tag52,int tag52len)=0;
	virtual CJ_RESULT cjOutput(uint8_t timeout,uint8_t *tag50,int tag50len);
	virtual CJ_RESULT CCID_Escape(uint8_t *Input,uint32_t InputLength,uint8_t *Output,uint32_t *OutputLength);
   virtual CJ_RESULT KTLightCall(uint8_t *sad,uint8_t *dad,uint8_t *cmd, uint16_t lenc,int32_t Lc,uint8_t *data_ptr,int32_t Le,uint8_t *response,uint16_t *lenr);
	virtual int ExecuteSecureResult(CCID_Response *Response,uint8_t *in,int *in_len,int offs);
   virtual CJ_RESULT SetSMModeAndCount(uint32_t ModuleID,uint32_t Count);
	virtual bool PinDirectSupported();
    virtual RSCT_IFD_RESULT PVMVT1(int Result,uint8_t *rbuffer,uint32_t rlen,uint32_t *lenr);
    virtual void CheckReaderDepended(CCID_Message &Message);


private:
	int ctBcsReset(uint8_t *atr,uint8_t *atr_len,uint8_t *historical,uint8_t *hist_len,uint8_t prevered,int Timeout=0);
	char _CtData(uint8_t *sad,uint8_t *dad,uint8_t *cmd, uint16_t cmd_len,uint8_t *response,uint16_t *response_len);

	uint8_t *cmd_buffer;
	uint32_t cmd_buffer_len;
	CRSCTCriticalSection m_CriticalCallback;

protected:
	uint8_t *ifd_in_buffer;
	uint16_t ifd_in_buffer_len;
	bool CopyIfdInput(const uint8_t *Input,uint32_t Length);


protected:
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
                    uint8_t *out,int out_len,uint8_t *in,int *in_len,uint8_t Slot,int TextCount,uint8_t *Text[3],uint8_t Textlen[3],uint8_t bMessageIndex[3],uint8_t bNumberMessage);
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
	int Transfer(CCID_Message *Message, CCID_Response *Response);

protected:
	uint8_t m_SequenceNo;
};

#endif
