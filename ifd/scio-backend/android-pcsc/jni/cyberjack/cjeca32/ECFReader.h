#ifndef ECFReader
#define ECFReader
#include "EC30Reader.h"

class CECFReader :
	public CEC30Reader
{
public:
	CECFReader(CReader *Owner,CBaseCommunication *Communicator);
	virtual ~CECFReader(void);
	virtual CJ_RESULT CtSetContrast(EContrast eContrast,uint32_t *Result);


protected:
	virtual uint16_t HostToReaderShort(uint16_t Value);
	virtual uint32_t HostToReaderLong(uint32_t Value);
	virtual void SetHWString(char *String);
	virtual void GetProductString(uint8_t *Product);
	virtual CJ_RESULT SetFlashMask(void);
	virtual int GetWarmstartTimeout(void);


};
#endif
