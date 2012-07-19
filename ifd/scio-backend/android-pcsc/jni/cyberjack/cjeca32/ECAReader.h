#ifndef ECA_ECAREADER_H
#define ECA_ECAREADER_H

#include "EC30Reader.h"

class CECAReader :
	public CEC30Reader
{
public:
	CECAReader(CReader *Owner,CBaseCommunication *Communicator);
	virtual ~CECAReader(void);
	virtual CJ_RESULT CtSetContrast(EContrast eContrast,uint32_t *Result);


protected:
	virtual RSCT_IFD_RESULT IfdPower(uint32_t Mode, uint8_t *ATR, uint32_t *ATR_Length, uint32_t Timeout);
	virtual uint16_t HostToReaderShort(uint16_t Value);
	virtual uint32_t HostToReaderLong(uint32_t Value);
	virtual void SetHWString(char *String);
	virtual void GetProductString(uint8_t *Product);

};

#endif

