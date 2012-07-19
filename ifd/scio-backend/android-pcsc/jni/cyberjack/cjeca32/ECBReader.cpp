#include "Platform.h"
#include "ECBReader.h"

#define base CECPReader


CECBReader::CECBReader(CReader *Owner,CBaseCommunication *Communicator)
			  :base(Owner,Communicator)
{
}

CECBReader::~CECBReader(void)
{
}

void CECBReader::SetHWString(char *String)
{
   strcpy(String,"ECB_");
}

void CECBReader::GetProductString(uint8_t *Product)
{
	memcpy(Product,"CJECB",5);
}

