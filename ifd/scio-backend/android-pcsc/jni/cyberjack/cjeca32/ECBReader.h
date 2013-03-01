#ifndef ECBREADER_H
#define ECBREADER_H

#include "ECPReader.h"

class CECBReader :
	virtual public CECPReader
{
public:
	CECBReader(CReader *Owner,CBaseCommunication *Communicator);
	virtual ~CECBReader(void);
	
	virtual void SetHWString(char *String);
	virtual void GetProductString(uint8_t *Product);
};
#endif
