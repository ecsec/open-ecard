/***************************************************************************
    begin       : Sat Nov 20 2010
    copyright   : (C) 2010 by Martin Preuss
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


#include "Platform.h"
#include <string.h>
#include "PPAReader.h"
#include "BaseCommunication.h"


#define base CCCIDReader

#ifdef _WINDOWS
#define DEBUGP(a,b,c,...)
#else
#define DEBUGP(devName, debug_mask, format, ...) {\
  char dbg_buffer[256]; \
  \
  snprintf(dbg_buffer, sizeof(dbg_buffer)-1,\
  __FILE__":%5d: " format  , __LINE__ , ##__VA_ARGS__); \
  dbg_buffer[sizeof(dbg_buffer)-1]=0; \
  Debug.Out(devName, debug_mask, dbg_buffer,0,0); \
}
#endif

#define CCID_VOLTAGE_50         1




CPPAReader::CPPAReader(CReader *Owner, CBaseCommunication *Communicator)
  :base(Owner, Communicator)
{

}



CPPAReader::~CPPAReader(void) {
}



void CPPAReader::SetHWString(char *String) {
  /* uuh, this is really ugly... there should at least be an indicator
   * for the length of the buffer provided, and strings should be null-terminated...
   * Or, even better, std::string should be used...
   * Anyway, this API is defined by the Windoze code, so we need to stick to it
   * even on Linux/MacOSX */
  strcpy(String,"PPA_");
}



void CPPAReader::GetProductString(uint8_t *Product) {
  /* the same here as in SetHWString... */
  memcpy(Product, "CJPPA", 5);
  if (GetEnviroment("pinpad_a_ident", 0)!=0) {
    memcpy(Product,"PPAUSB", 6);
  }
}



CJ_RESULT CPPAReader::SetSyncParameters(uint8_t AddrByteCount, uint16_t PageSize) {
  /* TODO: Currently not supported, but there is no matching error code for this... */
  DEBUGP("PPAReader", DEBUG_MASK_COMMUNICATION_ERROR, "SetSyncParameters called.");
  return CJ_ERR_CHECK_RESULT;
}



uint32_t CPPAReader::GetReadersInputBufferSize() {
  /* TODO: How many bytes is this really? */
  return 1024;
}



CJ_RESULT CPPAReader::BuildReaderInfo() {
  memset(&m_ReaderInfo, 0, sizeof(m_ReaderInfo));
  m_ReaderInfo.SizeOfStruct=sizeof(m_ReaderInfo);

  m_ReaderInfo.ContentsMask=
    RSCT_READER_MASK_HARDWARE |
    RSCT_READER_MASK_VERSION |
    /*RSCT_READER_MASK_HARDWARE_VERSION |*/
    RSCT_READER_MASK_FLASH_SIZE |
    RSCT_READER_MASK_HEAP_SIZE |
    /*RSCT_READER_MASK_SERIALNUMBER |*/
    /*RSCT_READER_MASK_PRODUCTION_DATE |*/
    /*RSCT_READER_MASK_TEST_DATE |*/
    /*RSCT_READER_MASK_COMMISSIONING_DATE |*/
    RSCT_READER_MASK_HW_STRING |
    0;

  m_ReaderInfo.HardwareMask=
    RSCT_READER_HARDWARE_MASK_ICC1 |
    RSCT_READER_HARDWARE_MASK_KEYPAD |
    /*RSCT_READER_HARDWARE_MASK_DISPLAY |*/
    /*RSCT_READER_HARDWARE_MASK_UPDATEABLE |*/
    /*RSCT_READER_HARDWARE_MASK_MODULES |*/
    0;

  /* TODO: What values do we have to use here? */
  m_ReaderInfo.FlashSize=32*1024;
  m_ReaderInfo.HeapSize=0;
  m_ReaderInfo.Version=0x30;

  SetHWString((char*)m_ReaderInfo.HardwareString);
  strcat((char*)m_ReaderInfo.HardwareString, (const char*)m_ReaderInfo.CommunicationString);


  /* TODO: Currently not supported, but there is no matching error code for this... */
  return CJ_SUCCESS;
}



CJ_RESULT CPPAReader::BuildModuleInfo() {
  /* TODO: Currently not supported, but there is no matching error code for this... */
  m_ModuleInfoCount=0;
  DEBUGP("PPAReader", DEBUG_MASK_COMMUNICATION_ERROR, "BuildModuleInfo called, returning no module.");
  return CJ_SUCCESS;
}



uint16_t CPPAReader::HostToReaderShort(uint16_t Value) {
  return InversByteOrderShort(htons(Value));
}



uint32_t CPPAReader::HostToReaderLong(uint32_t Value) {
  return InversByteOrderLong(htonl(Value));
}



void CPPAReader::SetSerialNumber(void) {
  /* TODO: How should this be done? */
}



void CPPAReader::SetDate(uint8_t Nr) {
  /* TODO: How should this be done? */
}



CJ_RESULT CPPAReader::cjInput(uint8_t *key,uint8_t timeout,uint8_t *tag52,int tag52len) {
  /* TODO: Currently not supported, but there is no matching error code for this... */
  return CJ_ERR_CHECK_RESULT;
}



RSCT_IFD_RESULT CPPAReader::ccidTransmit(uint8_t BWI, const uint8_t *cmd, uint16_t cmd_len,uint8_t *response,uint16_t *response_len,uint16_t wLevelParameter) {
  CCID_Message Message;
  CCID_Response Response;

  if (cmd_len>1014) {
    *response_len=0;
    return STATUS_BUFFER_OVERFLOW;
  }
  memset(&Message, 0, sizeof(Message));
  Message.bMessageType=PC_TO_RDR_XFRBLOCK;
  Message.dwLength=cmd_len;
  Message.Header.XfrBlock.bBWI=BWI;
  Message.Header.XfrBlock.wLevelParameter=HostToReaderShort(wLevelParameter);

  memcpy(Message.Data.abData, cmd,cmd_len);
  if (Transfer(&Message,&Response)==CJ_SUCCESS) {
    if (Response.bMessageType!=RDR_TO_PC_DATABLOCK) {
      DEBUGP("PPAReader", DEBUG_MASK_COMMUNICATION_ERROR, "Unexpected response (%02x)", (int)(Response.bMessageType));
      IfdPower(SCARD_POWER_DOWN, NULL, NULL,0);
      *response_len=0;
      return STATUS_DEVICE_PROTOCOL_ERROR;
    }
    if(Response.bStatus & 0x02)
       return STATUS_NO_MEDIA;
    if(Response.bStatus & 0x01)
       return STATUS_INVALID_DEVICE_STATE;
    if (Response.bStatus & 0x40) {
      if(Response.bError==ICC_MUTE) {
        DEBUGP("PPAReader", DEBUG_MASK_COMMUNICATION_ERROR, "Card is mute");
        IfdPower(SCARD_POWER_DOWN, NULL, NULL, 0);
        *response_len=0;
        return STATUS_IO_TIMEOUT;
      }
      else if(Response.bError==XFR_PARITY_ERROR) {
        DEBUGP("PPAReader", DEBUG_MASK_COMMUNICATION_ERROR, "Card is mute");
        IfdPower(SCARD_POWER_DOWN, NULL, NULL, 0);
        *response_len=0;
        return STATUS_PARITY_ERROR;
      }
      else {
        DEBUGP("PPAReader", DEBUG_MASK_COMMUNICATION_ERROR, "Unexpected error (%02x)", (int)(Response.bError));
        IfdPower(SCARD_POWER_DOWN, NULL, NULL, 0);
        *response_len=0;
        return STATUS_DEVICE_PROTOCOL_ERROR;
      }
    }

    if(Response.dwLength>*response_len) {
      *response_len=0;
      return STATUS_BUFFER_TOO_SMALL;
    }
    *response_len=(uint16_t)Response.dwLength;
    memcpy(response,Response.Data.abData, Response.dwLength);
    return STATUS_SUCCESS;
  }
  return STATUS_DEVICE_NOT_CONNECTED;
}

RSCT_IFD_RESULT CPPAReader::APDU2TPDU_T1(uint16_t lenc,const uint8_t *cmd,uint16_t *lenr,uint8_t *response)
{
	uint8_t lrc;
	uint16_t crc;
	int i;
	int error;
	uint8_t *sblock;
	uint8_t rblock[6];
	uint16_t slen;
	uint8_t sbuffer[259];
	uint8_t rbuffer[259];
	uint8_t xwt=0;
	uint16_t rlen;
	uint8_t INF;
	int maxresp;
	RSCT_IFD_RESULT Result=STATUS_SUCCESS;

	error=0;
	maxresp=*lenr;
	*lenr=0;
	while(lenc)
	{
		INF=(lenc>IFSC)?IFSC:lenc;
		sbuffer[0]=0;
		if(PCB_seq&0x01)
			sbuffer[1]=0x40;
		else
			sbuffer[1]=0;
		if(lenc>INF)
			sbuffer[1]|=0x20;
		sbuffer[2]=INF;
		memcpy(sbuffer+3,cmd,INF);
		sblock=sbuffer;
		slen=(uint16_t)(INF+3);
		for(;;)
		{
			if(EDC==0)
			{
				lrc=0;
				for(i=0;i<slen;i++)
				{
					lrc^=sblock[i];
				}
				sblock[slen++]=lrc;
			}
			else
			{
				/*crc berechnung*/
				crc=0;
				sblock[slen++]=(uint8_t)(crc>>8);
				sblock[slen++]=(uint8_t)crc;
			}
			rlen=sizeof(rbuffer);
			Result=ccidTransmit(xwt,sblock,slen,rbuffer,&rlen,0);
			if(Result==STATUS_SUCCESS || Result==STATUS_PARITY_ERROR || Result==STATUS_IO_TIMEOUT)
			{
				if(Result==STATUS_SUCCESS)
				{
					if(EDC==0)
					{
						lrc=0;
						for(i=0;i<rlen;i++)
						{
							lrc^=rbuffer[i];
						}
						if(lrc)
							Result=STATUS_PARITY_ERROR;
					}
					else
					{
						/*crc berechnung*/
					}
				}
				if((sblock[1]&0xe0)==0xc0)
				{
					if(Result==STATUS_PARITY_ERROR || rbuffer[0]!=0 || (rbuffer[1]&0xe0)!=0xe0 || (rbuffer[1]&0xdf)!=sblock[1] || rbuffer[2]!=sblock[2] || memcmp(rbuffer+3,sblock+3,sblock[2])!=0)
					{
						if(++error>2)
						{
							IfdPower(SCARD_POWER_DOWN,0,0,0);
							Result=STATUS_DEVICE_PROTOCOL_ERROR;
							break;
						}
					}
					else
					{
						sblock=sbuffer;
						slen=(uint16_t)(INF+3);
						error=0;
					}
				}
				else
				{
					if(Result==STATUS_SUCCESS)
					{
						if(rbuffer[0]!=0 ||  //Falsches NAD
							(rbuffer[1]&0xc0)==0x80 && (rbuffer[2]!=0 || (rbuffer[1]&0x20)==0x20) ||  //Unsinniger R-Block
							(rbuffer[1]&0xe0)==0xe0 || //SResponse ohne Request
							(rbuffer[1]&0xe0)==0xc0 &&
							((((rbuffer[1]& 0x1f)==0 || (rbuffer[1]& 0x1f)==2) && rbuffer[2]!=0) ||
							((rbuffer[1]& 0x1f)==1 && (rbuffer[2]!=1 || rbuffer[3]<0x10 || rbuffer[3]==255)) ||
							((rbuffer[1]& 0x1f)==3 && rbuffer[2]!=1) ||
							(rbuffer[1]& 0x1f)>3 ))
						{
							if(++error>2)
							{
    							IfdPower(SCARD_POWER_DOWN,0,0,0);
								Result=STATUS_DEVICE_PROTOCOL_ERROR;
								break;
							}
							if((sblock[1]&0xc0)!=0x80)
							{
								rblock[0]=0x00;
								rblock[1]=(uint8_t)(0x82 | (PCB_seq & 0x10));
								rblock[2]=0;
								sblock=rblock;
							}
							slen=3;
						}
						else if((rbuffer[1]&0x80)==0) /*I-Block*/
						{
							if((rbuffer[1]&0x40)!=((PCB_seq&0x10)<<2) ||
								(sbuffer[1]&0x20)==0x20 || rbuffer[2]==0 || rbuffer[2]==255)
							{
								if(++error>2)
								{
         							IfdPower(SCARD_POWER_DOWN,0,0,0);
									Result=STATUS_DEVICE_PROTOCOL_ERROR;
									break;
								}
								if((sblock[1]&0xc0)!=0x80)
								{
									rblock[0]=0x00;
									rblock[1]=(uint8_t)(0x82 | (PCB_seq & 0x10));
									rblock[2]=0;
									sblock=rblock;
								}
								slen=3;
							}
							else
							{
								error=0;
								PCB_seq^=0x10;
								if(lenc)
								{
									PCB_seq^=0x01;
									lenc-=INF;
									cmd+=INF;
								}
								if(maxresp>=rlen-4-EDC)
								{
									memcpy(response+(*lenr),rbuffer+3,rlen-4-EDC);
									maxresp-=rlen-4-EDC;
								}
								*lenr+=(uint16_t)(rlen-4-EDC);
								if(rbuffer[1] & 0x20)
								{
									rblock[0]=0x00;
									rblock[1]=(uint8_t)(0x80 | (PCB_seq & 0x10));
									rblock[2]=0;
									sblock=rblock;
									slen=3;
								}
								else
								{
									break;
								}
							}
						}
						else if((rbuffer[1]&0xE0)==0xC0) /*S-Block*/
						{
							error=0;
							memcpy(rblock,rbuffer,rlen);
							rblock[1]|=0x20;
							sblock=rblock;
							slen=(uint16_t)(rlen-1);
							if((rbuffer[1]& 0x1f)==0)
							{
								PCB_seq=0;
							}
							else if((rbuffer[1]& 0x1f)==1)
							{
								IFSC=rbuffer[3];
							}
							else if((rbuffer[1]& 0x1f)==2)
							{
     							IfdPower(SCARD_POWER_DOWN,0,0,0);
								Result=STATUS_DEVICE_PROTOCOL_ERROR;
								break;
							}
							else if((rbuffer[1]& 0x1f)==3)
							{
								xwt=rbuffer[3];
							}
						}
						else   /*R-Block*/
						{
							//                     if((rbuffer[1]&0x10)==((((cjccidHANDLE)hDevice)->PCB_seq<<4) & 0x10))
							if((rbuffer[1]&0x10)==((sbuffer[1]&0x40)>>2))
							{
								if(++error>2)
								{
         							IfdPower(SCARD_POWER_DOWN,0,0,0);
									Result=STATUS_DEVICE_PROTOCOL_ERROR;
								}
								sblock=sbuffer;
								slen=3+sbuffer[2];
							}
							else if(sbuffer[1] & 0x20)
							{
								error=0;
								PCB_seq^=0x01;
								cmd+=INF;
								lenc-=INF;
								break;
							}
							else if((sblock[1] & 0xC0)==0x80)
							{
								if(++error>2)
								{
									IfdPower(SCARD_POWER_DOWN,0,0,0);
									Result=STATUS_DEVICE_PROTOCOL_ERROR;
									break;
								}
								slen=3;
							}
							else
							{
								if(++error>2)
								{
									IfdPower(SCARD_POWER_DOWN,0,0,0);
									Result=STATUS_DEVICE_PROTOCOL_ERROR;
									break;
								}
								if((sblock[1]&0xc0)!=0x80)
								{
									rblock[0]=0x00;
									rblock[1]=(uint8_t)(0x82 | (PCB_seq & 0x10));
									rblock[2]=0;
									sblock=rblock;
								}
								slen=3;
							}
						}
					}
					if(Result==STATUS_PARITY_ERROR)
					{
						if(++error>2)
						{
							IfdPower(SCARD_POWER_DOWN,0,0,0);
							Result=STATUS_DEVICE_PROTOCOL_ERROR;
							break;
						}
						if((sblock[1]&0xc0)!=0x80)
						{
							rblock[0]=0x00;
							rblock[1]=(uint8_t)(0x81 | (PCB_seq & 0x10));
							rblock[2]=0;
							/*                     rblock[0]=0x00;
							rblock[1]=0xE3;
							rblock[2]=01;
							rblock[3]=01;*/
							sblock=rblock;
						}
						slen=3;
						//                  slen=4;

					}
					if(Result==STATUS_IO_TIMEOUT)
					{
						//                  if(++error>4)
						{
							//                     cjccid_iccPowerOff(hDevice);
							Result=STATUS_DEVICE_PROTOCOL_ERROR;
							break;
						}
						rblock[0]=0x00;
						rblock[1]=(uint8_t)(0x82 | (PCB_seq & 0x10));
						rblock[2]=0;
						sblock=rblock;
						slen=3;
					}
				}
			}
			else
				return Result;
		}
		if(Result==STATUS_DEVICE_PROTOCOL_ERROR || (rbuffer[1] & 0xA0)==0)
			break;
	}
	return Result;
}


RSCT_IFD_RESULT CPPAReader::_IfdTransmit(const uint8_t *cmd, uint16_t cmd_len,uint8_t *response,uint16_t *response_len) {
  RSCT_IFD_RESULT Result;
  int l;
  const char *str;
  //	uint16_t rest=*response_len;

  if(cmd_len==5 && cmd[0]==0xff && cmd[1]==0x9a && cmd[2]==0x01  && cmd[4]==0) {
    switch(cmd[3]) {
    case 1:
      if(*response_len>=12) {
        memcpy(response,"REINER SCT\x90\x00",12);
        *response_len=12;
        return STATUS_SUCCESS;
      }
      else {
        *response_len=0;
        return STATUS_BUFFER_TOO_SMALL;
      }
      break;

    case 2:
      if(*response_len>=6) {
        memcpy(response,"0C4B\x90\x00",6);
        *response_len=6;
        return STATUS_SUCCESS;
      }
      else {
        *response_len=0;
        return STATUS_BUFFER_TOO_SMALL;
      }
      break;

    case 3:
      if (*response_len>=(l=strlen((const char*)(m_ReaderInfo.ProductString)))+2) {
        memcpy(response,m_ReaderInfo.ProductString,l);
        memcpy(response+l,"\x90\x00",2);
        *response_len=l+2;
        return STATUS_SUCCESS;
      }
      else {
        *response_len=0;
        return STATUS_BUFFER_TOO_SMALL;
      }
      break;

    case 4:
      if(*response_len>=6) {
        sprintf((char *)response,"%04X\x90",m_ReaderInfo.PID);
        *response_len=6;
        return STATUS_SUCCESS;
      }
      else {
        *response_len=0;
        return STATUS_BUFFER_TOO_SMALL;
      }
      break;

    case 6:
      if (*response_len>=5) {
        sprintf((char *)response,"%1d.%1d\x90",(int)(m_ReaderInfo.Version>>4),(int)(m_ReaderInfo.Version & 0x0f));
        *response_len=5;
        return STATUS_SUCCESS;
      }
      else {
        *response_len=0;
        return STATUS_BUFFER_TOO_SMALL;
      }
      break;

    case 7:
      str=rsct_get_package_version();

      if(*response_len>=(l=strlen(str))+2) {
        memcpy(response,str,l);
        memcpy(response+l,"\x90\x00",2);
        *response_len=l+2;
        return STATUS_SUCCESS;
      }
      else {
        *response_len=0;
        return STATUS_BUFFER_TOO_SMALL;
      }
      break;

    case 8:

      if ((*response_len>=7  && GetReadersInputBufferSize()<=99999) ||
          (*response_len>=6 && GetReadersInputBufferSize()<=9999)) {
        sprintf((char *)response,"%d",768/*(int)GetReadersInputBufferSize()*/);
        memcpy(response+(l=strlen((char *)response)),"\x90\x00",2);
        *response_len=l+2;
        return STATUS_SUCCESS;
      }
      else {
        *response_len=0;
        return STATUS_BUFFER_TOO_SMALL;
      }
      break;

    case 9:
      if(*response_len>=8) {
        memcpy(response,"424250\x90\x00",8);
        *response_len=18;
        return STATUS_SUCCESS;
      }
      else {
        *response_len=0;
        return STATUS_BUFFER_TOO_SMALL;
      }
      break;
    default:;
    }
  }
  
  if(m_ActiveProtocol==SCARD_PROTOCOL_T0 && m_bIsRF==false) {
    if (cmd_len==4) {
      uint8_t sbuffer[5];

      memcpy(sbuffer,cmd,4);
      sbuffer[4]=0;
      return ccidTransmit(0,sbuffer, 5, response, response_len,0);
    }
    else if(cmd_len==5) {
      uint8_t rbuffer[258];
      uint8_t sbuffer[5];
      uint8_t Le;
      uint8_t La;
      uint16_t ges_len=0;
      uint16_t rlen=2;
	  uint16_t bLevelParam=0;

      if(cmd[0]==0 && cmd[1]==0xb4 && cmd[4]==0)
	  	 bLevelParam=(uint16_t)GetEnviroment("PPA_HandleAT88SC1616",0);
  
      memcpy(sbuffer,cmd,4);
      rbuffer[0]=0x61;
      rbuffer[1]=cmd[4];

      while(rbuffer[rlen-2]==0x61) { //while chaining
        rbuffer[0]=0x6C;
        Le=rbuffer[rlen-1];
        rbuffer[1]=Le;
        rlen=2;

        while(rlen==2 && rbuffer[0]==0x6C) { //while retransmit with other Length;
          rlen=sizeof(rbuffer);
          sbuffer[4]=La=rbuffer[1];
          if((Result=ccidTransmit(0,sbuffer,5,rbuffer,&rlen,bLevelParam))!=STATUS_SUCCESS) {
            return Result;
          }
          if(m_ApduNorm==NORM_PCSC)
            break;
          if(rlen<2) {
            *response_len=0;
            return STATUS_IO_TIMEOUT;
          }
          if(Le!=0 && Le<La) {
            memmove(rbuffer+Le,rbuffer+La,2);
            rlen=Le+2;
          }
        }
        if(ges_len+rlen>*response_len) {
          *response_len=0;
          return STATUS_BUFFER_TOO_SMALL;
        }
        memcpy(response,rbuffer,rlen);
        response+=rlen-2;
        ges_len+=rlen-2;
        memcpy(sbuffer,"\x00\xc0\x00\x00",4);
        if(m_ApduNorm==NORM_PCSC)
          break;
      }
      *response_len=ges_len+2;
      return STATUS_SUCCESS;
    }
    else if(cmd_len==5+cmd[4] && cmd[4]!=0) {
      return ccidTransmit(0,cmd,cmd_len,response,response_len,0);
    }
    else if(cmd_len==6+cmd[4] && cmd[4]!=0) {
      uint8_t sbuffer[5];
      uint8_t rbuffer[258];
      uint16_t rlen=sizeof(rbuffer);
      unsigned int tot_size=0;
      unsigned int rest_size=sizeof(rbuffer);
      uint8_t *rptr=rbuffer;
      if((Result=ccidTransmit(0,cmd,cmd_len-1,rbuffer,&rlen,0))!=STATUS_SUCCESS) {
        *response_len=0;
        return Result;
      }
      //			sbuffer[0]=cmd[0];
      sbuffer[0]=0;
      memcpy(sbuffer+1,"\xC0\x00\x00",3);
      rptr+=rlen-2;
      rest_size-=rlen-2;
      tot_size+=rlen-2;
      if (rlen==2 &&
          (((rbuffer[0] & 0xf0)==0x90 && (rbuffer[0]!=0x90 || rbuffer[1]!=0x00)) || rbuffer[0]==0x62 || rbuffer[0]==0x63)) {
        sbuffer[4]=cmd[cmd_len-1];
        rlen=rest_size;
        if((Result=ccidTransmit(0,sbuffer,5,rbuffer,&rlen,0))!=STATUS_SUCCESS) {
          *response_len=0;
          return Result;
        }
        rest_size-=rlen-2;
        tot_size+=rlen-2;
      }
      if(rlen >=2 && (rptr[rlen-2]==0x61 || rptr[rlen-2]==0x6C)) {
        while(rlen >=2 && (rptr[rlen-2]==0x61 || rptr[rlen-2]==0x6C)) {
          rptr+=rlen-2;
          if(cmd[cmd_len-1]<rptr[rlen-1] && cmd[cmd_len-1]!=0)
            sbuffer[4]=cmd[cmd_len-1];
          else
            sbuffer[4]=rptr[1];
          rlen=rest_size;
          if((Result=ccidTransmit(0,sbuffer,5,rptr,&rlen,0))!=STATUS_SUCCESS) {
            *response_len=0;
            return Result;
          }
          rest_size-=rlen-2;
          tot_size+=rlen-2;
        }
      }
      if(tot_size+2>*response_len) {
        *response_len=0;
        return STATUS_BUFFER_TOO_SMALL;
      }
      memcpy(response,rbuffer,tot_size+2);
      *response_len=tot_size+2;
      return STATUS_SUCCESS;
    }
    /*   else if(lenc==7 && cmd[4]==0)
     {
     }
     else if(lenc==7+(((uint16_t)cmd[5])<<8)+cmd[6] && cmd[4]==0 && lenc!=7)
     {
     }
     else if(lenc==9+(((uint16_t)cmd[5])<<8)+cmd[6] && cmd[4]==0 && lenc!=9)
     {
     }*/
    else
      return STATUS_INVALID_PARAMETER;
  }
  else {
    return APDU2TPDU_T1(cmd_len,cmd,response_len,response);
  }
}



/*
 * Differences between this method and the implementation in CECA30:
 * - PC_TO_RDR_ICCPOWERON has no data (i.e. Message.dwLength=0)
 * - bPowerSelect is set to 5.0 Volts explicitly
 */
RSCT_IFD_RESULT CPPAReader::IfdPower(uint32_t Mode, uint8_t *ATR, uint32_t *ATR_Length, uint32_t Timeout) {
  CCID_Message Message;
  CCID_Response Response;

  bool warm=false;
  bool first=true;

  Timeout=HostToReaderLong(Timeout);

  switch(Mode) {
  case SCARD_COLD_RESET:
    DEBUGP("PPAReader", DEBUG_MASK_COMMUNICATION_INFO, "COLD RESET");
    break;

  case SCARD_WARM_RESET:
    DEBUGP("PPAReader", DEBUG_MASK_COMMUNICATION_INFO, "WARM RESET");
    break;

  case SCARD_POWER_DOWN:
    DEBUGP("PPAReader", DEBUG_MASK_COMMUNICATION_INFO, "POWER DOWN");
    break;

  default:
    DEBUGP("PPAReader", DEBUG_MASK_COMMUNICATION_INFO, "Unknown power mode");
    return STATUS_INVALID_PARAMETER;
  }

  switch(Mode) {
  case SCARD_COLD_RESET:
  case SCARD_WARM_RESET:
    DEBUGP("PPAReader", DEBUG_MASK_COMMUNICATION_INFO, "Reset requested, powering down");
    *ATR_Length=0;
    IfdPower(SCARD_POWER_DOWN, NULL, NULL, 0);
    DEBUGP("PPAReader", DEBUG_MASK_COMMUNICATION_INFO, "Continueing with reset request");
    break;

  case SCARD_POWER_DOWN:
    break;

  default:
    return STATUS_INVALID_PARAMETER;
  }

  do
    {
      memset(&Message, 0, sizeof(Message));
      Message.dwLength=0; /* (no payload data) */
      Message.Header.iccPowerOn.bPowerSelect=1;
      switch(Mode) {
      case SCARD_COLD_RESET:
      case SCARD_WARM_RESET:
        *ATR_Length=0;
        Message.bMessageType=PC_TO_RDR_ICCPOWERON;
        Message.Header.iccPowerOn.bPowerSelect=CCID_VOLTAGE_50;
        break;

      case SCARD_POWER_DOWN:
        Message.bMessageType=PC_TO_RDR_ICCPOWEROFF;
        break;
      }

      if(first)
        first=false;
      else
        warm=true;

      if (Transfer(&Message, &Response)==CJ_SUCCESS) {
        switch(Mode) {
        case SCARD_COLD_RESET:
        case SCARD_WARM_RESET:
          if(Response.bMessageType!=RDR_TO_PC_DATABLOCK)
            return STATUS_DEVICE_NOT_CONNECTED;
          break;

        case SCARD_POWER_DOWN:
          if(Response.bMessageType!=RDR_TO_PC_SLOTSTATUS)
            return STATUS_DEVICE_NOT_CONNECTED;
          break;
        }

        if(Response.bStatus & 0x40) {
          switch(Response.bError) {
          case 0xfe:
            return STATUS_NO_MEDIA;
          case 0xf6:
            return STATUS_UNRECOGNIZED_MEDIA;
          case 0xef:
            return STATUS_CANCELLED;
          default:
            return STATUS_IO_TIMEOUT;
          }
        }
        if(Response.dwLength>33)
          Response.dwLength=33;
        switch(Mode) {
        case SCARD_COLD_RESET:
        case SCARD_WARM_RESET:
          m_ATR_Length=Response.dwLength;
          memcpy(m_ATR,Response.Data.abData,Response.dwLength);
          *ATR_Length=m_ATR_Length;
          memcpy(ATR,m_ATR,m_ATR_Length);
        default:;
        }
      }
      else
	return STATUS_DEVICE_NOT_CONNECTED;
    } while(Mode!=SCARD_POWER_DOWN && AnalyseATR(warm)==1);
	IFSC=m_TA3;
	PCB_seq=0;
	EDC=(unsigned char)(m_TC3&1);


  return STATUS_SUCCESS;
}



RSCT_IFD_RESULT CPPAReader::IfdSetProtocol(uint32_t *pProtocol) {
#if 0
  *pProtocol=m_ActiveProtocol;
  return STATUS_SUCCESS;
#endif

  CCID_Message Message;
  CCID_Response Response;
  uint32_t Protocol;

  memset(&Message,0,sizeof(Message));

  Protocol=*pProtocol;

  DEBUGP("PPAReader", DEBUG_MASK_COMMUNICATION_INFO,
         "Possible protocol %d (state=%d, TA1=%02x, TC1=%02x, TC2=%02x, TA3=%02x, TB3=%02x, TC3=%02x)",
         (int) Protocol,
         (int) m_ReaderState,
         (int) m_TA1,
         (int) m_TC1,
         (int) m_TC2,
         (int) m_TA3,
         (int) m_TB3,
         (int) m_TC3);

  *pProtocol=0;

  Message.bMessageType=PC_TO_RDR_SETPARAMETERS;
  if(m_ReaderState==SCARD_ABSENT)
    return STATUS_NO_MEDIA;
  if (m_ReaderState==SCARD_NEGOTIABLE) {
    char bufferTA1[128];
    char bufferTC1[128];
    char bufferc[3];

	uint8_t vTA1;
	uint8_t vTC1;

	


    if(m_ATR[0]==0xff || (m_ATR[0] & 0xf0)==0x80) {
      *pProtocol=SCARD_PROTOCOL_RAW;
      return STATUS_SUCCESS;
    }


    sprintf(bufferTA1,"ReplaceTA1_%02X",(int)m_TA1);
    strcpy(bufferTC1,"ReplaceTC1_");
    for(unsigned int i=0;i<m_ATR_Length;i++) {
      sprintf(bufferc,"%02X",(int)(m_ATR[i]));
      strcat(bufferTC1,bufferc);
    }
	vTA1=GetEnviroment(bufferTA1, m_TA1);
	vTC1=GetEnviroment(bufferTC1, m_TC1);
	bufferTC1[9]='2';
	m_TC2=GetEnviroment(bufferTC1, m_TC2);
	bufferTA1[8]='B';
	bufferTA1[9]='3';
	m_TB3=GetEnviroment(bufferTA1, m_TB3);

	if((vTA1 & 0x0f)==0x06) // D factor of 32 is to fast
	{
	    vTA1=(vTA1 & 0xf0) | 0x05;
	}
	if((vTA1 & 0x0f)==0x07) // D factor of 64 is to fast
	{
		vTA1=(vTA1 & 0xf0) | 0x05;
	}

    DEBUGP("PPAReader", DEBUG_MASK_COMMUNICATION_INFO,
         "Setting protocol %d (state=%d, TA1=%02x, TC1=%02x, TC2=%02x, TA3=%02x, TB3=%02x, TC3=%02x)",
         (int) Protocol,
         (int) m_ReaderState,
         (int) vTA1,
         (int) vTC1,
         (int) m_TC2,
         (int) m_TA3,
         (int) m_TB3,
         (int) m_TC3);

    if (Protocol & (SCARD_PROTOCOL_DEFAULT | SCARD_PROTOCOL_OPTIMAL))
      Protocol|=(SCARD_PROTOCOL_T0 | SCARD_PROTOCOL_T1);
    /*		if((Protocol & (SCARD_PROTOCOL_T0 | SCARD_PROTOCOL_T1)==(SCARD_PROTOCOL_T0 | SCARD_PROTOCOL_T1))
     Protocol|=SCARD_PROTOCOL_OPTIMAL;*/

    if((Protocol & SCARD_PROTOCOL_T0) && (m_PossibleProtocols & SCARD_PROTOCOL_T0)) {
      Message.dwLength=5;
      Message.Header.SetParameters.bProtocolNum=0;
      Message.Data.SetParameters.T0.bGuardTimeT0=vTC1;
      Message.Data.SetParameters.T0.bmFindexDindex=vTA1;
      Message.Data.SetParameters.T0.bWaitingIntegerT0=m_TC2;
    }
    else if((Protocol & SCARD_PROTOCOL_T1) && (m_PossibleProtocols & SCARD_PROTOCOL_T1)) {
      Message.dwLength=7;
      Message.Header.SetParameters.bProtocolNum=1;
      Message.Data.SetParameters.T1.bGuardTimeT1=vTC1;
      Message.Data.SetParameters.T1.bmFindexDindex=vTA1;
      Message.Data.SetParameters.T1.bWaitingIntegerT1=m_TB3;
      Message.Data.SetParameters.T1.bIFSC=m_TA3;
      DEBUGP("PPAReader", DEBUG_MASK_COMMUNICATION_INFO,
             "Some values: bmFindexDindex=%02x, bWaitingIntegerT1=%02x, bIFSC=%02x",
             Message.Data.SetParameters.T1.bmFindexDindex,
             Message.Data.SetParameters.T1.bWaitingIntegerT1,
             Message.Data.SetParameters.T1.bIFSC);
    }
    else {
      return STATUS_INVALID_DEVICE_REQUEST;
    }

    switch(Transfer(&Message, &Response)) {
    case CJ_SUCCESS:
      DEBUGP("PPAReader", DEBUG_MASK_COMMUNICATION_INFO, "Response: Success, checking further.");
      if ((Response.bStatus&3)==2) {
        DEBUGP("PPAReader", DEBUG_MASK_COMMUNICATION_ERROR, "No media.");
        return STATUS_NO_MEDIA;
      }
      else if((Response.bStatus&3)==1 || (Response.bStatus & 0x40)) {
        DEBUGP("PPAReader", DEBUG_MASK_COMMUNICATION_ERROR, "Timeout.");
        return STATUS_IO_TIMEOUT;
      }
      else {
        if (Message.Header.SetParameters.bProtocolNum==0) {
          DEBUGP("PPAReader", DEBUG_MASK_COMMUNICATION_INFO, "Selected protocol: T=0");
          m_ActiveProtocol=SCARD_PROTOCOL_T0;
        }
        else {
	  DEBUGP("PPAReader", DEBUG_MASK_COMMUNICATION_INFO, "Selected protocol: T=1");
          m_ActiveProtocol=SCARD_PROTOCOL_T1;
        }
        *pProtocol=m_ActiveProtocol;
        m_ReaderState=SCARD_SPECIFIC;

        if (1) {
	  CCID_Message msg;
	  CCID_Response rsp;

	  DEBUGP("PPAReader", DEBUG_MASK_COMMUNICATION_INFO, "Sending protocol info to reader");
	  memset(&msg, 0, sizeof(msg));

	  msg.dwLength=4;
	  msg.bMessageType=PC_TO_RDR_XFRBLOCK;
	  msg.Data.abData[0]=0xff;
	  if (m_ActiveProtocol==SCARD_PROTOCOL_T1)
	    msg.Data.abData[1]=0x11;
	  else
	    msg.Data.abData[1]=0x10;
	  msg.Data.abData[2]=vTA1;
	  msg.Data.abData[3]=msg.Data.abData[0] ^ msg.Data.abData[1] ^ msg.Data.abData[2]; /* CRC */

	  switch(Transfer(&msg, &rsp)) {
	  case CJ_SUCCESS:
	    DEBUGP("PPAReader", DEBUG_MASK_COMMUNICATION_INFO, "Response: Success, checking further.");
	    if ((rsp.bStatus&3)==2) {
	      DEBUGP("PPAReader", DEBUG_MASK_COMMUNICATION_ERROR, "No media.");
	      return STATUS_NO_MEDIA;
	    }
	    else if((rsp.bStatus&3)==1 || (rsp.bStatus & 0x40)) {
	      DEBUGP("PPAReader", DEBUG_MASK_COMMUNICATION_ERROR, "Timeout.");
	      return STATUS_IO_TIMEOUT;
	    }
	    DEBUGP("PPAReader", DEBUG_MASK_COMMUNICATION_INFO, "Ok, data sent.");
	    break;

	  default:
	    return STATUS_DEVICE_NOT_CONNECTED;
	  }
	}

	if (m_ActiveProtocol==SCARD_PROTOCOL_T1) {
	  CCID_Message msg;
	  CCID_Response rsp;

	  DEBUGP("PPAReader", DEBUG_MASK_COMMUNICATION_INFO, "Sending IFSD to reader for T=1");
	  memset(&msg, 0, sizeof(msg));

	  msg.dwLength=5;
	  msg.bMessageType=PC_TO_RDR_XFRBLOCK;
	  msg.Data.abData[0]=0x00;
	  msg.Data.abData[1]=0xc1;
	  msg.Data.abData[2]=0x01;
	  msg.Data.abData[3]=0xfe;
	  msg.Data.abData[4]=0x3e;

	  switch(Transfer(&msg, &rsp)) {
	  case CJ_SUCCESS:
	    DEBUGP("PPAReader", DEBUG_MASK_COMMUNICATION_INFO, "Response: Success, checking response.");
	    if ((rsp.bStatus&3)==2) {
	      DEBUGP("PPAReader", DEBUG_MASK_COMMUNICATION_ERROR, "No media.");
	      return STATUS_NO_MEDIA;
	    }
	    else if((rsp.bStatus&3)==1 || (rsp.bStatus & 0x40)) {
	      DEBUGP("PPAReader", DEBUG_MASK_COMMUNICATION_ERROR, "Timeout.");
	      return STATUS_IO_TIMEOUT;
	    }
	    if (rsp.dwLength>4 && 0==memcmp(rsp.Data.abData, "\x00\xE1\x01\xfe\x1e", 5)) {
	      DEBUGP("PPAReader", DEBUG_MASK_COMMUNICATION_INFO, "Protocol change acknowledged by reader.");
	    }
	    else {
	      DEBUGP("PPAReader",
		     DEBUG_MASK_COMMUNICATION_ERROR,
		     "Unexpected response from reader (%d): %02x %02x %02x %02x %02x\n",
		     rsp.dwLength,
		     rsp.Data.abData[0],
		     rsp.Data.abData[1],
		     rsp.Data.abData[2],
		     rsp.Data.abData[3],
		     rsp.Data.abData[4]);
	      return STATUS_IO_TIMEOUT;
	    }
	    break;

	  default:
	    return STATUS_DEVICE_NOT_CONNECTED;
	  }
	}

        DEBUGP("PPAReader", DEBUG_MASK_COMMUNICATION_INFO, "Protocol successfully changed.");
        return STATUS_SUCCESS;
      }

    default:
      return STATUS_DEVICE_NOT_CONNECTED;
    }
  }
  else if (m_ReaderState==SCARD_SPECIFIC) {
    if(Protocol & (SCARD_PROTOCOL_DEFAULT | SCARD_PROTOCOL_OPTIMAL))
      Protocol|=(SCARD_PROTOCOL_T0 | SCARD_PROTOCOL_T1 | SCARD_PROTOCOL_RAW);
    if(m_ActiveProtocol & Protocol) {
      *pProtocol=m_ActiveProtocol;
      return STATUS_SUCCESS;
    }
    else
      return STATUS_NOT_SUPPORTED;
  }
  else
    return STATUS_IO_TIMEOUT;
}

RSCT_IFD_RESULT CPPAReader::PVMVT1(int Result,uint8_t *rbuffer,uint32_t rlen,uint32_t *lenr)
{
	uint8_t lrc;
	uint8_t sblock[6];
	uint16_t len;
	uint32_t i;
	int slen;
	uint8_t xwt=0;
	if(Result!=STATUS_SUCCESS)
		return Result;
	if(EDC==0)
	{
		lrc=0;
		for(i=0;i<rlen;i++)
		{
			lrc^=rbuffer[i];
		}
		if(lrc)
		{
	    	IfdPower(SCARD_POWER_DOWN,0,0,0);
			return STATUS_PARITY_ERROR;
		}
	}
	else
	{
		//crc berechnung
	}
	while((rbuffer[1]&0x80)!=0) //I-Block
	{
		if((rbuffer[1]&0xE0)==0xC0 && rbuffer[2]<=1) /*S-Block*/
		{
			if((rbuffer[1]& 0x1f)==0)
			{
				PCB_seq=0;
			}
			else if((rbuffer[1]& 0x1f)==3 && rbuffer[2]==1)
			{
				xwt=rbuffer[3];
			}
			else
			{
         	    IfdPower(SCARD_POWER_DOWN,0,0,0);
			    return STATUS_DEVICE_PROTOCOL_ERROR;
			}
			memcpy(sblock,rbuffer,rlen);
			sblock[1]|=0x20;
			slen=rlen;
			sblock[slen-1]^=0x20;
			len=260;

			if(Result=ccidTransmit(xwt,sblock,(uint16_t)(slen),rbuffer,&len,0)!=STATUS_SUCCESS)
			{
				return Result;
			}
			rlen=len;
		}
		else
		{
    	   IfdPower(SCARD_POWER_DOWN,0,0,0);
		   return STATUS_DEVICE_PROTOCOL_ERROR;
		}
	}
	if((rbuffer[1]&0x40)!=((PCB_seq&0x10)<<2))
	{
   	    IfdPower(SCARD_POWER_DOWN,0,0,0);
		return STATUS_DEVICE_PROTOCOL_ERROR;
	}
	else
	{
		PCB_seq^=0x11;
		memmove(rbuffer,rbuffer+3,rlen-4-EDC);
		*lenr=(unsigned short)(rlen-4-EDC);
		return STATUS_SUCCESS;
	}
}

void CPPAReader::FillTeoPrologue(uint8_t *pbTeoPrologue)
{
   pbTeoPrologue[0]=0;
   if(PCB_seq&0x01)
      pbTeoPrologue[1]=0x40;
   else
      pbTeoPrologue[1]=0;
   pbTeoPrologue[2]=0;
}


int CPPAReader::cjccid_SecurePV(uint8_t Timeout,
                    uint8_t PinPosition,uint8_t PinType,
                    uint8_t PinLengthSize,uint8_t PinLength,
                    uint8_t PinLengthPosition,
                    uint8_t Min, uint8_t Max,
                    uint8_t Condition,uint8_t *Prologue,
                    uint8_t *out,int out_len,uint8_t *in,int *in_len,uint8_t *Text,uint8_t Textlen,uint8_t bMessageIndex,uint8_t bNumberMessage)
{
	CCID_Message Message;
	CCID_Response Response;
	int Res;

   Message.bMessageType=PC_TO_RDR_SECURE;
   Message.dwLength=out_len+15;

   Message.Header.Secure.bBWI=0;
	Message.Header.Secure.wLevelParameter=HostToReaderShort(0);
   Message.Data.Secure.bPINOperation=0;
   Message.Data.Secure.bTimeOut=Timeout;
   Message.Data.Secure.bmFormatString=(uint8_t)(0x80 | (PinPosition<<3) | PinType);
   Message.Data.Secure.bmPINBlockString=(uint8_t)((PinLengthSize<<4) | PinLength);
   Message.Data.Secure.bmPINLengthFormat=PinLengthPosition;
   Message.Data.Secure.Data.Verify.wPINMaxExtraDigit=HostToReaderShort((((uint16_t)Min)<<8)+Max);
   Message.Data.Secure.Data.Verify.bEntryValidationCondition=Condition;
   Message.Data.Secure.Data.Verify.bNumberMessage=bNumberMessage;
	Message.Data.Secure.Data.Verify.wLangId=HostToReaderShort(0x0409);
   Message.Data.Secure.Data.Verify.bMsgIndex=bMessageIndex;
	Message.Header.Secure.wLevelParameter=HostToReaderShort(0);
	FillTeoPrologue(Message.Data.Secure.Data.Verify.bTeoPrologue);
   memcpy(Message.Data.Secure.Data.Verify.abData,out,out_len);
#ifdef _INSERT_KEY_EVENTS
	{
		uint8_t buffer[2];
		buffer[0]=RDR_TO_PC_KEYEVENT;
		buffer[1]=0xa0;
		DoInterruptCallback(buffer,2);
	}
#endif
	Res=Transfer(&Message,&Response);
    if(Res==CJ_SUCCESS && m_ActiveProtocol==SCARD_PROTOCOL_T1)
	   Res=PVMVT1(Res,Response.Data.abData,Response.dwLength,&Response.dwLength);
#ifdef _INSERT_KEY_EVENTS
   if(Res==CJ_SUCCESS)
   {

        if((Response.bStatus & 0x03)==0 && (((Response.bStatus & 0x40) && Response.bError==PIN_CANCELED) || (Response.bStatus & 0x40)==0))
		{
			uint8_t buffer[2];
			buffer[0]=RDR_TO_PC_KEYEVENT;
			if(Response.bError==PIN_CANCELED)
				buffer[1]=0x01;
			else 
				buffer[1]=0x02;
			DoInterruptCallback(buffer,2);
		}

   }
	{
		uint8_t buffer[2];
		buffer[0]=RDR_TO_PC_KEYEVENT;
		buffer[1]=0xa1;
		DoInterruptCallback(buffer,2);
	}
#endif
   if(Res)
	   return Res;
	return ExecuteSecureResult(&Response,in,in_len,0);
}

int CPPAReader::cjccid_SecureMV(uint8_t Timeout,
                    uint8_t PinPosition,uint8_t PinType,
                    uint8_t PinLengthSize,uint8_t PinLength,
                    uint8_t PinLengthPosition,
                    uint8_t Min, uint8_t Max,
                    uint8_t bConfirmPIN,
                    uint8_t Condition,uint8_t *Prologue,
                    uint8_t OffsetOld,uint8_t OffsetNew,
                    uint8_t *out,int out_len,uint8_t *in,int *in_len,int TextCount,uint8_t *Text[3],uint8_t Textlen[3],uint8_t bMessageIndex[3],uint8_t bNumberMessage)
{
	CCID_Message Message;
	CCID_Response Response;
	int Res;
   //cj_ModuleInfo *Info=FindModule(MODULE_ID_KERNEL);

   Message.bMessageType=PC_TO_RDR_SECURE;
   Message.dwLength=out_len+20;
   Message.bSlot=0;
   Message.Header.Secure.bBWI=0;
	Message.Header.Secure.wLevelParameter=HostToReaderShort(0);
   Message.Data.Secure.bPINOperation=1;
   Message.Data.Secure.bTimeOut=Timeout;
   Message.Data.Secure.bmFormatString=(uint8_t)(0x80 | (PinPosition<<3) | PinType);
   Message.Data.Secure.bmPINBlockString=(uint8_t)((PinLengthSize<<4) | PinLength);
   Message.Data.Secure.bmPINLengthFormat=PinLengthPosition;
   Message.Data.Secure.Data.Modify.bInsertionOffsetOld=OffsetOld;
   Message.Data.Secure.Data.Modify.bInsertionOffsetNew=OffsetNew;
	Message.Data.Secure.Data.Modify.wPINMaxExtraDigit=HostToReaderShort((((uint16_t)Min)<<8)+Max);
   Message.Data.Secure.Data.Modify.bConfirmPIN= bConfirmPIN;
   Message.Data.Secure.Data.Modify.bEntryValidationCondition=Condition;
   Message.Data.Secure.Data.Modify.bNumberMessage=bNumberMessage;
   Message.Data.Secure.Data.Modify.wLangId=HostToReaderShort(0x0409);
   Message.Data.Secure.Data.Modify.bMsgIndex1=bMessageIndex[0];
	Message.Data.Secure.Data.Modify.bMsgIndex2=bMessageIndex[1];
	Message.Data.Secure.Data.Modify.bMsgIndex3=bMessageIndex[2];
	FillTeoPrologue(Message.Data.Secure.Data.Modify.bTeoPrologue);
   memcpy(Message.Data.Secure.Data.Modify.abData,out,out_len);
#ifdef _INSERT_KEY_EVENTS
	{
		uint8_t buffer[2];
		buffer[0]=RDR_TO_PC_KEYEVENT;
		buffer[1]=0xa0;
		DoInterruptCallback(buffer,2);
	}
#endif

	Res=Transfer(&Message,&Response);
    if(Res==CJ_SUCCESS && m_ActiveProtocol==SCARD_PROTOCOL_T1)
	   Res=PVMVT1(Res,Response.Data.abData,Response.dwLength,&Response.dwLength);
	if(Res!=0)
	{
#ifdef _INSERT_KEY_EVENTS
		uint8_t buffer[2];
		buffer[0]=RDR_TO_PC_KEYEVENT;
		buffer[1]=0xa1;
		DoInterruptCallback(buffer,2);
#endif
		return Res;
	}
#ifdef _INSERT_KEY_EVENTS
	if((Response.bStatus & 0x03)==0 && (((Response.bStatus & 0x40) && Response.bError==PIN_CANCELED) || (Response.bStatus & 0x40)==0))
	{
		uint8_t buffer[2];
		buffer[0]=RDR_TO_PC_KEYEVENT;
	   if(Response.bError==PIN_CANCELED)
			buffer[1]=0x01;
	   else 
			buffer[1]=0x02;
		DoInterruptCallback(buffer,2);
	}
	{
		uint8_t buffer[2];
		buffer[0]=RDR_TO_PC_KEYEVENT;
		buffer[1]=0xa1;
		DoInterruptCallback(buffer,2);
	}

#endif
	return ExecuteSecureResult(&Response,in,in_len,5);
}

bool CPPAReader::PinDirectSupported()
{
	return true;
}

CJ_RESULT CPPAReader::SpecialLess3_0_41()
{
	return CJ_SUCCESS;
}


void CPPAReader::CheckReaderDepended(CCID_Message &Message)
{
	if(Message.bMessageType==PC_TO_RDR_SECURE)
	{
		if((Message.Data.Secure.bPINOperation==0 && Message.dwLength==19) ||
		   (Message.Data.Secure.bPINOperation==1 && Message.dwLength==24))
		{
			Message.dwLength++;
		}

		if((Message.Data.Secure.bPINOperation==0 && Message.dwLength>19) ||
		   (Message.Data.Secure.bPINOperation==1 && Message.dwLength>24))
		{
			Message.Data.abData[19+5*Message.Data.Secure.bPINOperation]=0;
		}
	}
}
