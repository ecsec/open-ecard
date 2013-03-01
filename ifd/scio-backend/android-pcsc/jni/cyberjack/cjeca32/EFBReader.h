#ifndef ECFREADER_H
#define ECFREADER_H

#include "ECFReader.h"

class CEFBReader :
	public CECFReader
{
public:
	CEFBReader(CReader *Owner,CBaseCommunication *Communicator);
	virtual ~CEFBReader(void);
	
	virtual void SetHWString(char *String);
	virtual void GetProductString(uint8_t *Product);
};
#endif
