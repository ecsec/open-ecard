#include "Platform.h"
#include "RSCTCriticalSection.h"

#ifdef _WINDOWS
CRSCTCriticalSection::CRSCTCriticalSection(void)
{
	InitializeCriticalSection(&m_hCritSec);
}

CRSCTCriticalSection::~CRSCTCriticalSection(void)
{
	DeleteCriticalSection(&m_hCritSec);
}

void CRSCTCriticalSection::Enter(void)
{
	EnterCriticalSection(&m_hCritSec);
}

void CRSCTCriticalSection::Leave(void)
{
	LeaveCriticalSection(&m_hCritSec);
}

#elif defined(OS_LINUX)

CRSCTCriticalSection::CRSCTCriticalSection(void){
}



CRSCTCriticalSection::~CRSCTCriticalSection(void){
}



void CRSCTCriticalSection::Enter(void){
}



void CRSCTCriticalSection::Leave(void){
}

#elif defined(OS_DARWIN)

CRSCTCriticalSection::CRSCTCriticalSection(void)
//: m_CriticalRegionID(NULL)
{
//	MPCreateCriticalRegion(&m_CriticalRegionID);
}



CRSCTCriticalSection::~CRSCTCriticalSection(void)
{
//	MPDeleteCriticalRegion(m_CriticalRegionID);
//	m_CriticalRegionID = NULL;
}



void CRSCTCriticalSection::Enter(void)
{
//	if ( m_CriticalRegionID ) MPEnterCriticalRegion(m_CriticalRegionID, 0x7FFFFFFF);
}



void CRSCTCriticalSection::Leave(void)
{
//	if ( m_CriticalRegionID ) MPExitCriticalRegion(m_CriticalRegionID);
}

#endif
