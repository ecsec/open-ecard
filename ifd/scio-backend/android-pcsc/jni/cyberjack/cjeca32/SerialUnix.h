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

#ifndef RSCT_SERIALLINUX_H
#define RSCT_SERIALLINUX_H

#include "BaseCommunication.h"


class CSerialUnix : public CBaseCommunication {

public:
  uint32_t m_refcounter;

protected:
  int m_devHandle;

public:
  CSerialUnix(const char *cDeviceName,CReader *Owner);
  virtual ~CSerialUnix(void);

  static char *createDeviceName(int num);

  virtual int Write(void *Message, uint32_t len);
  virtual int Read(void *Response, uint32_t *ResponseLen);
  virtual CBaseReader *BuildReaderObject();
  virtual void SetCommunicationString(cj_ReaderInfo *ReaderInfo);
  virtual bool IsConnected();

  virtual int Open();
  virtual void Close();

private:
  virtual int StartInterruptPipe();
  virtual int HaltInterruptPipe();

  int _readForced(uint8_t *buf, uint32_t len);
  int _readLowlevel(uint8_t *Response, uint32_t *ResponseLen);
  int _writeAck(uint8_t c);
  int _writeLowlevel(void *Message, uint32_t len);

  int _readFd(int fd, void *buf, size_t l);
  int _writeFd(int fd, const void *buf, size_t l);

};


#endif

