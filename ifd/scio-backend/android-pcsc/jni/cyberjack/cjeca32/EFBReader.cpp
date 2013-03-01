#include "Platform.h"
#include "EFBReader.h"

#define base CECFReader

CEFBReader::CEFBReader(CReader *Owner,CBaseCommunication *Communicator)
			  :base(Owner,Communicator)
{
}

CEFBReader::~CEFBReader(void)
{
}

void CEFBReader::SetHWString(char *String)
{
   strcpy(String,"EFB_");
}

void CEFBReader::GetProductString(uint8_t *Product)
{
	memcpy(Product,"CJEFB",5);
}
