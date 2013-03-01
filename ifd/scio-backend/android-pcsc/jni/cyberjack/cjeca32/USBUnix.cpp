/***************************************************************************
    begin       : Wed Apr 18 2007
    copyright   : (C) 2007-2010 by Martin Preuss
    email       : martin@libchipcard.de

 ***************************************************************************
 *                                                                         *
 *   This library is free software; you can redistribute it and/or         *
 *   modify it under the terms of the GNU Lesser General Public            *
 *   License as published by the Free Software Foundation; either          *
 *   version 2.1 of the License, or (at your option) any later version.    *
 *                                                                         *
 *   This library is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU     *
 *   Lesser General Public License for more details.                       *
 *                                                                         *
 *   You should have received a copy of the GNU Lesser General Public      *
 *   License along with this library; if not, write to the Free Software   *
 *   Foundation, Inc., 59 Temple Place, Suite 330, Boston,                 *
 *   MA  02111-1307  USA                                                   *
 *                                                                         *
 ***************************************************************************/

#ifdef HAVE_CONFIG_H
# include <config.h>
#endif


#ifdef ENABLE_NONSERIAL


#include "Platform.h"

#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>
#include <errno.h>

#include <string>

#include "USBUnix.h"
#include "ausb_l.h"
#include "ECAReader.h"
#include "ECBReader.h"
#include "ECRReader.h"
#include "ECPReader.h"
#include "SECReader.h"
#include "ECFReader.h"
#include "ECFReader.h"
#include "EFBReader.h"


#define USB_TIMEOUT		10000000
#define USB_READ_TIMEOUT	(USB_TIMEOUT*120)
#define USB_WRITE_TIMEOUT	USB_TIMEOUT

#define DEBUGP(devName, debug_mask, format, ...) {\
  char dbg_buffer[256]; \
  \
  snprintf(dbg_buffer, sizeof(dbg_buffer)-1,\
  __FILE__":%5d: " format  , __LINE__ , ##__VA_ARGS__); \
  dbg_buffer[sizeof(dbg_buffer)-1]=0; \
  Debug.Out(devName, debug_mask, dbg_buffer,0,0); \
}




static void logAusb(ausb_dev_handle *ah,
		    const char *text,
		    const void *pData, uint32_t ulDataLen) {

  rsct_debug_out("<USB>",
		 DEBUG_MASK_COMMUNICATION_IN,
		 (char*)text,
		 (char*)pData, ulDataLen);
}



extern "C" {
void usb_callback(const uint8_t *data,
		  uint32_t dlength,
		  void *userdata) {
  CUSBUnix *com;

  com=(CUSBUnix*) userdata;
  com->usbCallback(data, dlength);
}
}



char *CUSBUnix::createDeviceName(int num) {
  rsct_usbdev_t *dev;
  char *p;

  /* get device */
  dev=rsct_usbdev_getDevByIdx(num);
  if (dev==NULL) {
    Debug.Out("<no reader>",
	      DEBUG_MASK_COMMUNICATION_ERROR,
	      "Device not found",0,0);
    return NULL;
  }

  if (dev->path)
    p=strdup(dev->path);
  else if (dev->halPath)
    p=strdup(dev->halPath);
  else
    p=NULL;
  rsct_usbdev_free(dev);
  return p;
}



char *CUSBUnix::createDeviceName(int busId, int devId) {
  rsct_usbdev_t *dev;
  char *p;

  /* get device */
  dev=rsct_usbdev_getDevByBusPos(busId, devId);
  if (dev==NULL) {
    Debug.Out("<no reader>",
	      DEBUG_MASK_COMMUNICATION_ERROR,
	      "Device not found",0,0);
    return NULL;
  }

  if (dev->path)
    p=strdup(dev->path);
  else if (dev->halPath)
    p=strdup(dev->halPath);
  else
    p=NULL;
  rsct_usbdev_free(dev);
  return p;
}



CUSBUnix::CUSBUnix(const char *cDeviceName,CReader *Owner)
:CBaseCommunication(cDeviceName, Owner)
,m_refcounter(1)
,m_devHandle(NULL)
,m_bulkIn(0)
,m_bulkOut(0)
,m_intPipe(0)
{
  /* set log function */
  ausb_set_log_fn(logAusb);
}



CUSBUnix::~CUSBUnix(void) {
  m_refcounter=0;
  Close();
}



int CUSBUnix::Write(void *Message, uint32_t len) {
  int rv;

  rv=CBaseCommunication::Write(Message,len);
  if (rv==CJ_SUCCESS) {
    rv=ausb_bulk_write(m_devHandle, m_bulkOut,
		       (char*) Message, len,
		       USB_WRITE_TIMEOUT);
    if (rv<0) {
      Debug.Out(m_cDeviceName,
		DEBUG_MASK_COMMUNICATION_ERROR,
		"Error on write",0,0);
      Close();
      return CJ_ERR_DEVICE_LOST;
    }

    return CJ_SUCCESS;
  }
  else
    return rv;
}



int CUSBUnix::Read(void *Response, uint32_t *ResponseLen) {
  int rv;

  rv=ausb_bulk_read(m_devHandle, m_bulkIn,
		    (char*)Response, *ResponseLen,
		    USB_READ_TIMEOUT);
  if (rv<0) {
    Debug.Out(m_cDeviceName,
	      DEBUG_MASK_COMMUNICATION_ERROR,
	      "Error on read",0,0);
    Close();
    return CJ_ERR_DEVICE_LOST;
  }

  *ResponseLen=rv;
  return CBaseCommunication::Read(Response, ResponseLen);
}



CBaseReader *CUSBUnix::BuildReaderObject() {
  rsct_usbdev_t *dev;

  dev=rsct_usbdev_getDevByName(m_cDeviceName);
  if (dev==NULL) {
    Debug.Out(m_cDeviceName,DEBUG_MASK_COMMUNICATION_ERROR,"Device not found",0,0);
    return NULL;
  }

  if (dev->vendorId==AUSB_CYBERJACK_VENDOR_ID) {
    CBaseReader *r;

    DEBUGP(m_cDeviceName, DEBUG_MASK_COMMUNICATION_ERROR, "Product: %s", dev->productName);
    /* use the same constructor for every platform, so that we only need to adjust the list
     * of known readers in a single place: BaseCommunication.cpp */
    r=_buildUsbReaderObject(dev->productId, (const char*)(dev->productName));
    rsct_usbdev_free(dev);
    return r;
  }
  else {
    Debug.Out(m_cDeviceName, DEBUG_MASK_COMMUNICATION_ERROR, "Device is not a cyberjack",0,0);
    return NULL;
  }
}

void CUSBUnix::SetCommunicationString(cj_ReaderInfo *ReaderInfo) {
  ReaderInfo->PID=m_pid;
  memcpy(ReaderInfo->CommunicationString, "USB", 4);
  memcpy(ReaderInfo->VendorString, "REINER SCT", 11);
  memcpy(ReaderInfo->ProductString, m_productString.c_str(),m_productString.length());

  ReaderInfo->ContentsMask=
    RSCT_READER_MASK_PID |
    RSCT_READER_MASK_VENDOR_STRING |
    RSCT_READER_MASK_PRODUCT_STRING |
    RSCT_READER_MASK_COM_TYPE;

}



bool CUSBUnix::IsConnected() {
  return (m_devHandle!=NULL);
}



int CUSBUnix::Open() {
  rsct_usbdev_t *dev;
  int nConfig=0;
  int usbMode=1;
  int rv;

  m_bulkIn=0;
  m_bulkOut=0;
  m_intPipe=0;

  /* get device */
  dev=rsct_usbdev_getDevByName(m_cDeviceName);
  if (dev==NULL) {
    Debug.Out("<USB>",
	      DEBUG_MASK_COMMUNICATION_ERROR,
	      "Device not found",0,0);
    return 0;
  }

  /* get addresses of bulk endpoints */
  nConfig=0;
  switch(dev->productId) {
  case 0x300:
    Debug.Out("<USB>",
	      DEBUG_MASK_COMMUNICATION_ERROR,
	      "Using USB implementation 1", 0, 0);
    usbMode=1;

    m_bulkOut=0x04;
    m_bulkIn=0x85;
    m_intPipe=0x81;
    break;

  case 0x400:
    Debug.Out("<USB>",
	      DEBUG_MASK_COMMUNICATION_ERROR,
	      "Using USB implementation 3", 0, 0);
    usbMode=3;

    m_bulkOut=0x02;
    m_bulkIn=0x81;
    m_intPipe=0x83;
    break;

  case 0x401:
    Debug.Out("<USB>",
	      DEBUG_MASK_COMMUNICATION_ERROR,
	      "Using USB implementation 3", 0, 0);
    usbMode=3;

    m_bulkOut=0x02;
    m_bulkIn=0x82;
    m_intPipe=0x81;
    break;

  default:
    Debug.Out("<USB>",
	      DEBUG_MASK_COMMUNICATION_ERROR,
	      "Using USB implementation 1", 0, 0);
    usbMode=1;

    m_bulkOut=0x02;
    m_bulkIn=0x81;
    m_intPipe=0x83;
  }

  /* ok, we have all, open the device */
  m_devHandle=ausb_open(dev, usbMode);
  if (m_devHandle==NULL) {
    Debug.Out("<USB>",
	      DEBUG_MASK_COMMUNICATION_ERROR,
	      "Unable to open device",0,0);
    return 0;
  }

  /* set configuration, this syncs driver and device */
  rv=ausb_set_configuration(m_devHandle, 1);
  if (rv) {
    Debug.Out("<USB>",
	      DEBUG_MASK_COMMUNICATION_ERROR,
	      "Unable to set configuration",0,0);
    ausb_close(m_devHandle);
    m_devHandle=NULL;
    return 0;
  }

  Debug.Out("<USB>",
	    DEBUG_MASK_COMMUNICATION_ERROR,
	    "Claim interface", 0, 0);
  rv=ausb_claim_interface(m_devHandle, 0);
  if (rv<0) {
    Debug.Out("<USB>",
	      DEBUG_MASK_COMMUNICATION_ERROR,
	      "Still unable to claim interface",0,0);
    ausb_close(m_devHandle);
    m_devHandle=NULL;
    return 0;
  }

  /* This will synchronize the toggle bit */
//  ausb_reset_pipe(m_devHandle, m_bulkOut);
//  ausb_reset_pipe(m_devHandle, m_bulkIn);

  //fprintf(stderr, "register callback handler\n");
  ausb_register_callback(m_devHandle, usb_callback, (void*)this);

  //fprintf(stderr, "start interrupt\n");
  if (ausb_start_interrupt(m_devHandle, m_intPipe)) {
    Debug.Out("<USB>",
	      DEBUG_MASK_COMMUNICATION_ERROR,
	      "Unable to start receiving interrupts",0,0);
    ausb_close(m_devHandle);
    m_devHandle=NULL;

    return 0;
  }

  //fprintf(stderr, "device open.\n");

  return 1;
}



void CUSBUnix::Close() {
  //fprintf(stderr, "Closing communication\n");
  if (m_devHandle==NULL) {
    Debug.Out(m_cDeviceName,
	      DEBUG_MASK_COMMUNICATION_ERROR,
	      "Device not open",0,0);
  }
  else {
    ausb_stop_interrupt(m_devHandle);

#if 0
    ausb_reset(m_devHandle);
#endif
    ausb_release_interface(m_devHandle, 0);
    ausb_close(m_devHandle);
    m_devHandle=NULL;
  }
}



int CUSBUnix::StartInterruptPipe() {
  return 0;
}



int CUSBUnix::HaltInterruptPipe() {
  return 0;
}



void CUSBUnix::usbCallback(const uint8_t *data,
			    uint32_t dlength) {
  DEBUGP(m_cDeviceName, DEBUG_MASK_COMMUNICATION_INT, "USB Interrupt received: %d bytes", dlength);

  if (m_Reader) {
    Debug.Out(m_cDeviceName,
	      DEBUG_MASK_COMMUNICATION_INT,
	      "Calling device interrupt handler",
	      (uint8_t*)data,
	      dlength);
    m_Reader->DoInterruptCallback((uint8_t*)data, dlength);
  }
  else {
    Debug.Out(m_cDeviceName,
	      DEBUG_MASK_COMMUNICATION_ERROR,
	      "No reader",0,0);
  }
}



#endif





