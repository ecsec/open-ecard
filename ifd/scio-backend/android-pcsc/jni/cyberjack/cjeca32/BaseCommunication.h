
#ifndef ECA_BASECOMM_H
#define ECA_BASECOMM_H

#include "cjeca32.h"
#include "RSCTCriticalSection.h"

#include <string>


class CBaseReader;
class CReader;


class CBaseCommunication
{
public:
	CBaseCommunication(const char *cDeviceName,CReader *Owner);
	virtual ~CBaseCommunication(void);
public:

// Transmitting Data to the Reader:
	virtual int Write(void *Message,uint32_t len)=0;

// Receiving Data from the Reader
	virtual int Read(void *Response,uint32_t *ResponseLen)=0;

// Filling all Parts of the cj_ReaderInfo struct that can be retrieved by the driver without communication with the reader
	virtual void SetCommunicationString(cj_ReaderInfo *ReaderInfo)=0;

// Depending on the driver information, this function has to create the correct instance of an CBaseReader object:
//	This function has to set the m_Reader member.
// For USB - devices the PID can be used for this decision
// For serial devives the devicename and a database can be used for this decision
// For LPT Readers same as PID 100 for USB Readers
	virtual CBaseReader *BuildReaderObject()=0; 

// Determing wether the connection is still alive
	virtual bool IsConnected()=0;

// Next functions will be implemented later
//	virtual int InstallIFDHandler();
//	virtual int RemoveIFDHandler();
//	virtual int GetIFDHandlerDeviceName(char *DeviceName);

	void FreeIFDHandlerDeviceName(char *DeviceName);
	virtual int StartInterruptPipe()=0;
	virtual int HaltInterruptPipe()=0;


   virtual int Open()=0;
	virtual void Close();
public:
	char *m_cDeviceName;

protected:

  /**
   * build a reader object depending on pid and readerName. This method can be used by
   * @ref BuildReaderObject.
   */
  CBaseReader *_buildUsbReaderObject(uint16_t pid, const char *readerName);

protected:
// Used in the derived classes for synconisation
	CRSCTCriticalSection m_CritSec;
	CRSCTCriticalSection m_CritClose;

// Pointer to the reader implementation object
	CBaseReader *m_Reader;

// Pointer to the reader interface object
	CReader *m_Owner;
	typedef enum _tInterruptPipeState{UnInit,Running,HaltRequest,Halted}tInterruptPipeState;
	tInterruptPipeState m_InterruptPipeState;

        uint16_t m_pid;
        std::string m_productString;

};

#endif

