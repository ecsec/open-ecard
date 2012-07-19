#include "Platform.h"
#include "BaseCommunication.h"
#include <string.h>
#include "Debug.h"

#include "ECAReader.h"
#include "ECBReader.h"
#include "ECRReader.h"
#include "ECPReader.h"
#include "SECReader.h"
#include "ECFReader.h"
#include "ECFReader.h"
#include "EFBReader.h"
#include "PPAReader.h"
#include "RFKReader.h"
#include "RFSReader.h"
#include "CPTReader.h"





CBaseCommunication::CBaseCommunication(const char *cDeviceName,CReader *Owner)
{
	m_cDeviceName=strdup(cDeviceName);
	m_Owner=Owner;
	m_Reader=NULL;
	m_InterruptPipeState=UnInit;
        m_pid=0;
}


CBaseCommunication::~CBaseCommunication(void)
{
	free(m_cDeviceName);
}

void CBaseCommunication::Close()
{}



int CBaseCommunication::Write(void *Message,uint32_t len)
{
	if(IsConnected())
   {
      Debug.Out(m_cDeviceName,DEBUG_MASK_COMMUNICATION_OUT,"CCID OUT:",Message,len);
	}
      
	return ((!IsConnected())?CJ_ERR_DEVICE_LOST:CJ_SUCCESS);
}

int CBaseCommunication::Read(void *Response,uint32_t *len)
{
   if(IsConnected())
   {
      Debug.Out(m_cDeviceName,DEBUG_MASK_COMMUNICATION_IN,"CCID IN:",Response,*len);
	}
      
	return ((!IsConnected())?CJ_ERR_DEVICE_LOST:CJ_SUCCESS);
}

void CBaseCommunication::FreeIFDHandlerDeviceName(char *DeviceName)
{
	if(DeviceName!=NULL)
	   delete DeviceName;
}



CBaseReader *CBaseCommunication::_buildUsbReaderObject(uint16_t pid, const char *readerName) {
  int len;
  char *ptr;

  len=strlen(readerName);
  ptr=strdup(readerName);

  Debug.varLog(m_cDeviceName, DEBUG_MASK_COMMUNICATION_INFO, "Reader Productstring: [%s]", ptr);

  switch(pid) {
#ifndef _WINDOWS
  case 0x300:
    if (len>=18 && memcmp(ptr, "cyberJack pinpad(a)", 19)==0) {
      ptr[18]='\0';
      m_Reader = new CPPAReader(m_Owner, this);
      Debug.varLog(m_cDeviceName, DEBUG_MASK_COMMUNICATION_INFO,
                   "Recognized device %04x [%s]", pid, ptr);
    }
    else {
      Debug.varLog(m_cDeviceName, DEBUG_MASK_COMMUNICATION_ERROR,
                   "Device [%s] ist not a known cyberJack 0x300, assuming pinpad(a)", ptr);
      m_Reader = new CPPAReader(m_Owner, this);
    }

    m_pid=pid;
    m_productString=ptr;
    free(ptr);
    return m_Reader;
#endif
  case 0x400:
    if (len>=18 && memcmp(ptr, "cyberJack e-com(a)", 18)==0) {
      ptr[18]='\0';
      m_Reader = new CECAReader(m_Owner, this);
      Debug.varLog(m_cDeviceName, DEBUG_MASK_COMMUNICATION_INFO,
             "Recognized device %04x [%s]", pid, ptr);
    }
    else if(len>=24 && memcmp(ptr, "cyberJack e-com plus DUO", 24)==0){
      ptr[24]='\0';
      m_Reader = new CECBReader(m_Owner, this);
      Debug.varLog(m_cDeviceName, DEBUG_MASK_COMMUNICATION_INFO,
             "Recognized device %04x [%s]", pid, ptr);
    }
    else if(len>=24 && memcmp(ptr, "cyberJack e-com plus BIO", 24)==0){
      ptr[24]='\0';
      m_Reader = new CECBReader(m_Owner, this);
      Debug.varLog(m_cDeviceName, DEBUG_MASK_COMMUNICATION_INFO,
             "Recognized device %04x [%s]", pid, ptr);
    }
    else if(len>=25 && memcmp(ptr, "cyberJack e-com plus RFID", 25)==0){
      ptr[25]='\0';
      m_Reader = new CECRReader(m_Owner, this);
      Debug.varLog(m_cDeviceName, DEBUG_MASK_COMMUNICATION_INFO,
             "Recognized device %04x [%s]", pid, ptr);
    }
    else if(len>=20 && memcmp(ptr, "cyberJack e-com plus", 20)==0) {
      ptr[20]='\0';
      m_Reader = new CECPReader(m_Owner, this);
      Debug.varLog(m_cDeviceName, DEBUG_MASK_COMMUNICATION_INFO,
             "Recognized device %04x [%s]", pid, ptr);
    }
    else if(len>=17 && memcmp(ptr, "cyberJack Secoder", 17)==0){
      ptr[17]='\0';
      m_Reader = new CSECReader(m_Owner, this);
      Debug.varLog(m_cDeviceName, DEBUG_MASK_COMMUNICATION_INFO,
             "Recognized device %04x [%s]", pid, ptr);
    }
    else {
      Debug.varLog(m_cDeviceName, DEBUG_MASK_COMMUNICATION_ERROR,
             "Device [%s] ist not a known cyberJack 0x400, assuming e-com(a)", ptr);
      m_Reader = new CECAReader(m_Owner, this);
    }

    m_pid=pid;
    m_productString=ptr;
    free(ptr);
    return m_Reader;
  
  case 0x401:
    if(len>=18 && memcmp(ptr, "cyberJack e-com(f)", 18)==0) {
      ptr[18]='\0';
      m_Reader = new CECFReader(m_Owner, this);
      Debug.varLog(m_cDeviceName, DEBUG_MASK_COMMUNICATION_INFO,
             "Recognized device %04x [%s]", pid, ptr);
    }
    else if (len>=19 && memcmp(ptr, "cyberJack e-com BIO", 19)==0){
      ptr[19]='\0';
      m_Reader = new CEFBReader(m_Owner, this);
      Debug.varLog(m_cDeviceName, DEBUG_MASK_COMMUNICATION_INFO,
             "Recognized device %04x [%s]", pid, ptr);
    }
    else {
      Debug.varLog(m_cDeviceName, DEBUG_MASK_COMMUNICATION_ERROR,
             "Device [%s] ist not a known cyberJack 0x401, assuming e-com(f)", ptr);
      m_Reader=new CECFReader(m_Owner, this);
    }

    m_pid=pid;
    m_productString=ptr;
    free(ptr);
    return m_Reader;

  case 0x0500:
    if (len>=23 && memcmp(ptr, "cyberJack RFID standard", 23)==0) {
      ptr[23]='\0';
      m_Reader = new CRFSReader(m_Owner, this);
      Debug.varLog(m_cDeviceName, DEBUG_MASK_COMMUNICATION_INFO,
             "Recognized device %04x [%s]", pid, ptr);
    }
    else {
      Debug.varLog(m_cDeviceName, DEBUG_MASK_COMMUNICATION_ERROR,
             "Device [%s] ist not a known cyberJack 0x500, assuming RFID standard", ptr);
      m_Reader = new CRFSReader(m_Owner, this);
    }
    m_pid=pid;
    m_productString=ptr;
    free(ptr);
    return m_Reader;

  case 0x0501:
    if (len>=22 && memcmp(ptr, "cyberJack RFID komfort", 22)==0) {
      ptr[22]='\0';
      m_Reader = new CRFKReader(m_Owner, this);
      Debug.varLog(m_cDeviceName, DEBUG_MASK_COMMUNICATION_INFO,
             "Recognized device %04x [%s]", pid, ptr);
    }
    else {
      Debug.varLog(m_cDeviceName, DEBUG_MASK_COMMUNICATION_ERROR,
             "Device [%s] ist not a known cyberJack 0x501, assuming RFID standard", ptr);
      m_Reader = new CRFSReader(m_Owner, this);
    }
    m_pid=pid;
    m_productString=ptr;
    free(ptr);
    return m_Reader;

  case 0x0502:
    if (len>=17 && memcmp(ptr, "cyberJack compact", 17)==0) {
      ptr[17]='\0';
      m_Reader = new CCPTReader(m_Owner, this);
      Debug.varLog(m_cDeviceName, DEBUG_MASK_COMMUNICATION_INFO,
             "Recognized device %04x [%s]", pid, ptr);
    }
    else {
      Debug.varLog(m_cDeviceName, DEBUG_MASK_COMMUNICATION_ERROR,
             "Device [%s] ist not a known cyberJack 0x502, assuming RFID standard", ptr);
      m_Reader = new CRFSReader(m_Owner, this);
    }
    m_pid=pid;
    m_productString=ptr;
    free(ptr);
    return m_Reader;

    /* add more readers here */

  default:
    Debug.varLog(m_cDeviceName, DEBUG_MASK_COMMUNICATION_ERROR,
                  "Device %04x [%s] ist not a known cyberJack\n", pid, ptr);
    free(ptr);
    return NULL;
  }
}



