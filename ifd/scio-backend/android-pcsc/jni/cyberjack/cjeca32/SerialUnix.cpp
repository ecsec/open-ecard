/***************************************************************************
    begin       : Mon Dec 10 2007
    copyright   : (C) 2007 by Martin Preuss
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

#include "Platform.h"

#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>
#include <errno.h>

#include <termios.h>
#include <fcntl.h>

#include <termios.h>
#include <sys/ioctl.h>

#include <string>

#include "SerialUnix.h"
#include "ccid.h"
#include "ECAReader.h"




#define DEBUGP(format, ...) {\
  char dbg_buffer[256]; \
  \
  snprintf(dbg_buffer, sizeof(dbg_buffer)-1,\
  __FILE__":%5d: " format  , __LINE__ , ##__VA_ARGS__); \
  dbg_buffer[sizeof(dbg_buffer)-1]=0; \
  Debug.Out("serial", \
	    DEBUG_MASK_COMMUNICATION_ERROR, \
            dbg_buffer, 0, 0); \
}




char *CSerialUnix::createDeviceName(int num) {
  char buffer[256];
  int rv;

  rv=snprintf(buffer, sizeof(buffer)-1, "/dev/ttyS%d", num);
  if (rv<0 || rv>=(int)(sizeof(buffer)-1)) {
    DEBUGP("Buffer too small (%d)", rv);
    return NULL;
  }

  return strdup(buffer);
}



CSerialUnix::CSerialUnix(const char *cDeviceName, CReader *Owner)
:CBaseCommunication(cDeviceName, Owner)
,m_refcounter(1)
,m_devHandle(-1)
{
}



CSerialUnix::~CSerialUnix(void) {
  m_refcounter=0;
  Close();
}



int CSerialUnix::_readFd(int fd, void *buf, size_t l) {
  ssize_t rv;

  rv=read(fd, buf, l);
  if (rv>=0) {
    Debug.Out(m_cDeviceName,
	      DEBUG_MASK_COMMUNICATION_OUT,
	      "SERIAL IN",
	      buf, rv);
  }
  else {
    DEBUGP("read: %s", strerror(errno));
  }

  return rv;
}



int CSerialUnix::_writeFd(int fd, const void *buf, size_t l) {
  ssize_t rv;

  Debug.Out(m_cDeviceName,
	    DEBUG_MASK_COMMUNICATION_OUT,
	    "SERIAL OUT",
	    (void*)buf, l);
  rv=write(fd, buf, l);
  if (rv<0) {
    DEBUGP("write: %s", strerror(errno));
  }

  return rv;
}



int CSerialUnix::_writeLowlevel(void *Message, uint32_t len) {
  uint8_t *p;
  uint8_t crc[2];
  uint32_t i;

  if (m_devHandle<0) {
    DEBUGP("Device is not open");
    return CJ_ERR_DEVICE_LOST;
  }

  /* calculate checksum */
  p=(uint8_t*) Message;
  crc[0]=0;
  crc[1]=0;
  for (i=0; i<len; i++) {
    crc[0]+=p[i];
    crc[1]^=p[i];
  }

  /* send data */
  while (len>0) {
    ssize_t rv;

    rv=_writeFd(m_devHandle, p, len);
    if (rv<1) {
      if (errno!=EINTR) {
	DEBUGP("write: %s", strerror(errno));
	Close();
	return CJ_ERR_DEVICE_LOST;
      }
    }

    len-=rv;
    p+=rv;
  }

  /* send checksums */
  p=crc;
  len=2;
  while (len>0) {
    ssize_t rv;

    rv=_writeFd(m_devHandle, p, len);
    if (rv<1) {
      if (errno!=EINTR) {
	DEBUGP("write: %s", strerror(errno));
	Close();
	return CJ_ERR_DEVICE_LOST;
      }
    }

    len-=rv;
    p+=rv;
  }

  /* make sure data gets written */
 /* if (tcdrain(m_devHandle)) {
    DEBUGP("tcdrain: %s", strerror(errno));
    Close();
    return CJ_ERR_DEVICE_LOST;
  }*/

  return CJ_SUCCESS;
}



int CSerialUnix::Write(void *Message, uint32_t len) {
  for (;;) {
    int res;
    uint8_t buf[1];

    res=_writeLowlevel(Message, len);
    if (res!=CJ_SUCCESS)
      return res;

    DEBUGP("Reading ACK byte");
    if (_readForced(buf, 1)) {
      Close();
      return CJ_ERR_DEVICE_LOST;
    }

    DEBUGP("Reading ACK byte: %02x", buf[0]);

    if (buf[0]==0xff)
      break;

    DEBUGP("Transmission error, resending");
    /* forget whatever is in the buffers */
    tcflush(m_devHandle, TCIOFLUSH);
    sleep(1);
  }

  return CJ_SUCCESS;
}



int CSerialUnix::_readForced(uint8_t *buf, uint32_t len) {
  while(len) {
    ssize_t rv;

    rv=_readFd(m_devHandle, buf, len);
    if (rv<0) {
      if (errno!=EINTR) {
	DEBUGP("read: %s", strerror(errno));
	return -1;
      }
    }
    else if (rv==0) {
      DEBUGP("EOF met");
      return -1;
    }
    else {
      len-=rv;
      buf+=rv;
    }
  }

  return 0;
}



int CSerialUnix::_writeAck(uint8_t c) {
  uint8_t buf[1];
  ssize_t rv;

  buf[0]=c;
  do {
    rv=_writeFd(m_devHandle, buf, 1);
  } while(rv<0 && errno==EINTR);

  if (rv<1) {
    DEBUGP("write: %s", strerror(errno));
    Close();
    return CJ_ERR_DEVICE_LOST;
  }

  return CJ_SUCCESS;
}



int CSerialUnix::_readLowlevel(uint8_t *Response, uint32_t *ResponseLen) {
  uint32_t toRead;
  uint8_t buffer[32];
  uint32_t inBuffer=0;
  uint8_t crc1=0;
  uint8_t crc2=0;
  uint32_t i;

  if (m_devHandle<0) {
    DEBUGP("Device is not open");
    return CJ_ERR_DEVICE_LOST;
  }

  DEBUGP("reading up to %d bytes", *ResponseLen);

  /* read first byte */
  if (_readForced(buffer, 1)) {
    Close();
    return CJ_ERR_DEVICE_LOST;
  }

  inBuffer=1;
  if(buffer[0]==RDR_TO_PC_NOTIFYSLOTCHANGE ||
     buffer[0]==RDR_TO_PC_HARWAREERROR ||
     buffer[0]==RDR_TO_PC_KEYEVENT) {
    /* interrupt */
    toRead=1;
  }
  else if (buffer[0]==0x00 ||
	   buffer[0]==0xff) {
    /* ACK byte received */
    toRead=0;
  }
  else {
    /* read CCID header */
    if (_readForced(buffer+1, 9)) {
      Close();
      return CJ_ERR_DEVICE_LOST;
    }
    toRead=buffer[2];
    toRead<<=8;
    toRead+=buffer[1];
    inBuffer+=9;
  }

  if ((inBuffer+toRead)>*ResponseLen) {
    DEBUGP("Buffer too small (%d<%d)",
	   (inBuffer+toRead), *ResponseLen);
    Close();
    return CJ_ERR_DEVICE_LOST;
  }

  /* copy reveiced bytes */
  memmove(Response, buffer, inBuffer);

  /* possibly read more bytes */
  if (toRead) {
    if (_readForced(Response+inBuffer, toRead)) {
      Close();
      return CJ_ERR_DEVICE_LOST;
    }
  }

  /* calculate checksums */
  for (i=0; i<(inBuffer+toRead); i++) {
    crc1+=Response[i];
    crc2^=Response[i];
  }

  /* read checksums */
  if (_readForced(buffer+1, 2)) {
    Close();
    return CJ_ERR_DEVICE_LOST;
  }

  /* compare checksums */
  if (buffer[1]!=crc1) {
    DEBUGP("Bad additive CRC (%02x != %02x)", buffer[1], crc1);
  }

  if (buffer[2]!=crc2) {
    DEBUGP("Bad XOR CRC (%02x != %02x)", buffer[2], crc2);
  }

  /* write ACK/NAK, but nor for interrupt messages */
  if(buffer[0]!=RDR_TO_PC_NOTIFYSLOTCHANGE &&
     buffer[0]!=RDR_TO_PC_HARWAREERROR &&
     buffer[0]!=RDR_TO_PC_KEYEVENT) {
    if (buffer[1]==crc1 && buffer[2]==crc2) {
      int res;

      /* ACK */
      res=_writeAck(0xff);
      if (res!=CJ_SUCCESS)
	return res;
    }
    else {
      int res;

      /* NAK */
      res=_writeAck(0x00);
      if (res!=CJ_SUCCESS)
	return res;
      return CJ_ERR_DATA_CORRUPT;
    }
  }

  *ResponseLen=inBuffer+toRead;

  return CJ_SUCCESS;
}



int CSerialUnix::Read(void *Response, uint32_t *ResponseLen) {
  int res;
  uint32_t l;

  do {
    l=*ResponseLen;
    res=_readLowlevel((uint8_t*)Response, &l);
  } while(res==CJ_ERR_DATA_CORRUPT);

  if (res==CJ_SUCCESS)
    *ResponseLen=l;

  return res;
}



CBaseReader *CSerialUnix::BuildReaderObject() {
  m_Reader=new CECAReader(m_Owner, this);
  return m_Reader;
}



void CSerialUnix::SetCommunicationString(cj_ReaderInfo *ReaderInfo) {
  ReaderInfo->PID=0x0400;
  memcpy(ReaderInfo->CommunicationString, "COM", 4);

  ReaderInfo->ContentsMask=
    RSCT_READER_MASK_PID |
    RSCT_READER_MASK_COM_TYPE;
}



bool CSerialUnix::IsConnected() {
  return (m_devHandle!=-1);
}



int CSerialUnix::Open() {
  int fd;
  struct termios tios;
  int modemLines=0;

  DEBUGP("Opening device [%s]", m_cDeviceName);

  /* open serial device */
  fd=open(m_cDeviceName, O_RDWR | O_NOCTTY);
  if (fd<0) {
    DEBUGP("open: %s", strerror(errno));
    return 0;
  }

  /* get SIO attributes as a template */
  if (tcgetattr(fd, &tios)<0) {
    DEBUGP("tcgetattr: %s", strerror(errno));
    close(fd);
    return 0;
  }

  /* modify to work with the reader */

  /* ignore paritiy and breaks at input */
  tios.c_iflag=IGNBRK|IGNPAR;

  /* ignore parity at output */
  tios.c_oflag=IGNPAR;

  /* ignore modem status */
  tios.c_cflag|=CLOCAL;

  /* enable reading */
  tios.c_cflag|=CREAD;

  /* 8n1 */
  tios.c_cflag&=~PARENB;
  tios.c_cflag&=~CSTOPB;
  tios.c_cflag&=~CSIZE;
  tios.c_cflag|=CS8;

  /* disable hardware flow control */
  tios.c_cflag&=~CRTSCTS;

  /* raw */
  tios.c_lflag &=~(ICANON|ECHO|ECHOE|ISIG);
  tios.c_oflag &=~OPOST;
  tios.c_iflag &=~(IXON|IXOFF|IXANY);

  /* read one byte minimum, no timeout */
  tios.c_cc[VMIN]=1;
  tios.c_cc[VTIME]=0;

 // cfsetspeed(&tios, B115200);

  /* set new attributes */
  if (tcsetattr(fd, TCSANOW, &tios)<0) {
    DEBUGP("tcsetattr: %s", strerror(errno));
    close(fd);
    return 0;
  }

  /* get modem lines */
  if (ioctl(fd, TIOCMGET, &modemLines)<0) {
    DEBUGP("ioctl(TIOCMGET): %s, ignoring", strerror(errno));
    //close(fd);
    //return 0;
  }
  else {
    /* set RTS */
    modemLines&=~TIOCM_RTS;
    if (ioctl(fd, TIOCMSET, &modemLines)<0) {
      DEBUGP("ioctl(TIOCMSET): %s, ignoring.", strerror(errno));
      //close(fd);
      //return 0;
    }
  }

  if (tcflush(fd, TCIOFLUSH)<0) {
    DEBUGP("tcflush: %s", strerror(errno));
    close(fd);
    return 0;
  }

  m_devHandle=fd;

  return 1;
}



void CSerialUnix::Close() {
  if (m_devHandle<0) {
    DEBUGP("Device is not open");
  }
  else {
    close(m_devHandle);
    m_devHandle=-1;
  }
}



int CSerialUnix::StartInterruptPipe() {
  return 0;
}



int CSerialUnix::HaltInterruptPipe() {
  return 0;
}






