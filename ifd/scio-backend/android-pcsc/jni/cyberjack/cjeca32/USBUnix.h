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

#ifndef RSCT_USBLINUX_H
#define RSCT_USBLINUX_H

#include "BaseCommunication.h"


/* forward declaration */
typedef struct ausb_dev_handle ausb_dev_handle;


class CUSBUnix : public CBaseCommunication {

public:
  uint32_t m_refcounter;

protected:
  ausb_dev_handle *m_devHandle;
  uint8_t m_bulkIn;
  uint8_t m_bulkOut;
  uint8_t m_intPipe;

public:
  CUSBUnix(const char *cDeviceName,CReader *Owner);
  virtual ~CUSBUnix(void);

  static char *createDeviceName(int num);
  static char *createDeviceName(int busId, int devId);

  virtual int Write(void *Message, uint32_t len);
  virtual int Read(void *Response, uint32_t *ResponseLen);
  virtual CBaseReader *BuildReaderObject();
  virtual void SetCommunicationString(cj_ReaderInfo *ReaderInfo);
  virtual bool IsConnected();

  virtual int Open();
  virtual void Close();

  void usbCallback(const uint8_t *data, uint32_t dlength);

private:
  virtual int StartInterruptPipe();
  virtual int HaltInterruptPipe();

};


#endif

