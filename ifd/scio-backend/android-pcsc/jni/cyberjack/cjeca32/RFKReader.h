#ifndef RFKREADER_H
#define RFKREADER_H

#include "RFSReader.h"

class CRFKReader :
	public CRFSReader
{
public:
	CRFKReader(CReader *Owner,CBaseCommunication *Communicator);
	virtual ~CRFKReader(void);
	
	virtual void SetHWString(char *String);
	virtual void GetProductString(uint8_t *Product);
	virtual CJ_RESULT CtSetBacklight(EBacklight eBacklight,uint32_t *Result);	

};

#endif

