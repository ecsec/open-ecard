#include "Platform.h"
#include "BaseTranslater.h"

CBaseTranslater::CBaseTranslater(CBaseCommunication *Communicator)
{
	m_Communicator=Communicator;
}

CBaseTranslater::~CBaseTranslater(void)
{
}
