#include "Platform.h"
#include "ECMReader.h"

CECMReader::CECMReader(CReader *Owner,CBaseCommunication *Communicator):CECPReader(Owner,Communicator),CECRReader(Owner,Communicator),CECBReader(Owner,Communicator)
{}

CECMReader::~CECMReader(void)
{}


	
void CECMReader::SetHWString(char *String)
{
   strcpy(String,"ECM_");
}

void CECMReader::GetProductString(uint8_t *Product)
{
	memcpy(Product,"CJECM",5);
}

RSCT_IFD_RESULT CECMReader::IfdPower(uint32_t Mode, uint8_t *ATR, uint32_t *ATR_Length, uint32_t Timeout)
{
	return CECRReader::IfdPower(Mode,ATR,ATR_Length,Timeout);
}

RSCT_IFD_RESULT CECMReader::IfdSetProtocol(uint32_t *Protocol)
{
	return CECRReader::IfdSetProtocol(Protocol);
}

RSCT_IFD_RESULT CECMReader::_IfdTransmit(const uint8_t *cmd, uint16_t cmd_len,uint8_t *response,uint16_t *response_len)
{
	return CECRReader::_IfdTransmit(cmd,cmd_len,response,response_len);
}

RSCT_IFD_RESULT CECMReader::IfdVendor(uint32_t IoCtrlCode,uint8_t *Input,uint32_t InputLength,uint8_t *Output,uint32_t *OutputLength)
{
	return CECRReader::IfdVendor(IoCtrlCode,Input,InputLength,Output,OutputLength);
}

CJ_RESULT CECMReader::BuildReaderInfo()
{
	return CECRReader::BuildReaderInfo();
}

uint32_t CECMReader::GetReadersInputBufferSize()
{
	return CECRReader::GetReadersInputBufferSize();
}

