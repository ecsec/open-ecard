#ifndef RFSREADER_H
#define RFSREADER_H

#include "ECPReader.h"

class CRFSReader :
	public CECPReader
{
public:
	CRFSReader(CReader *Owner,CBaseCommunication *Communicator);
	virtual ~CRFSReader(void);
	
	virtual void SetHWString(char *String);
	virtual void GetProductString(uint8_t *Product);
	virtual CJ_RESULT CtSetContrast(EContrast eContrast,uint32_t *Result);


protected:
  virtual bool ATRFilter(bool IsWarm);
	virtual bool HastModulestoreInfo();
	virtual RSCT_IFD_RESULT IfdPower(uint32_t Mode, uint8_t *ATR, uint32_t *ATR_Length, uint32_t Timeout);
	virtual RSCT_IFD_RESULT IfdSetProtocol(uint32_t *Protocol);
	virtual RSCT_IFD_RESULT _IfdTransmit(const uint8_t *cmd, uint16_t cmd_len,uint8_t *response,uint16_t *response_len);	
   virtual RSCT_IFD_RESULT IfdVendor(uint32_t IoCtrlCode,uint8_t *Input,uint32_t InputLength,uint8_t *Output,uint32_t *OutputLength);
	virtual CJ_RESULT BuildReaderInfo();
	virtual uint32_t GetReadersInputBufferSize();
	virtual CJ_RESULT CtSetSilentMode(bool boolMode,bool *pboolMode,uint32_t *Result);
	virtual CJ_RESULT CtGetSilentMode(bool *pboolMode,uint32_t *Result);
	virtual bool PinDirectSupported();
	virtual CJ_RESULT SpecialLess3_0_41();


private:
	uint8_t m_SerialNumber[10];
	int m_SerialNumberLength; 

};

#endif

