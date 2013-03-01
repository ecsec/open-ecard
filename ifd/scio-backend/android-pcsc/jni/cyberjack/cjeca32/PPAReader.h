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


#ifndef ECA_PPAREADER_H
#define ECA_PPAREADER_H

#include "CCIDReader.h"




class CPPAReader : public CCCIDReader {
public:
  CPPAReader(CReader *Owner, CBaseCommunication *Communicator);
  virtual ~CPPAReader(void);

protected:
  virtual void SetHWString(char *String);
  virtual void GetProductString(uint8_t *Product);

  /** @name From BaseReader
   *
   */
  /*@{*/
  virtual CJ_RESULT SetSyncParameters(uint8_t AddrByteCount, uint16_t PageSize);
  virtual uint32_t GetReadersInputBufferSize();

  virtual CJ_RESULT BuildReaderInfo();
  virtual CJ_RESULT BuildModuleInfo();

  virtual uint16_t HostToReaderShort(uint16_t Value);
  virtual uint32_t HostToReaderLong(uint32_t Value);
  /*@}*/


  /** @name From CCIDReader
   *
   */
  /*@{*/
  virtual RSCT_IFD_RESULT IfdPower(uint32_t Mode, uint8_t *ATR, uint32_t *ATR_Length, uint32_t Timeout);
  virtual void SetSerialNumber(void);
  virtual void SetDate(uint8_t Nr);
  virtual RSCT_IFD_RESULT _IfdTransmit(const uint8_t *cmd, uint16_t cmd_len,uint8_t *response,uint16_t *response_len);
  virtual CJ_RESULT cjInput(uint8_t *key,uint8_t timeout,uint8_t *tag52,int tag52len);

  /*@}*/

  RSCT_IFD_RESULT ccidTransmit(uint8_t BWI,const uint8_t *cmd, uint16_t cmd_len,uint8_t *response,uint16_t *response_len,uint16_t wLevelParameter);
  RSCT_IFD_RESULT IfdSetProtocol(uint32_t *pProtocol);
  RSCT_IFD_RESULT APDU2TPDU_T1(uint16_t lenc,const uint8_t *cmd,uint16_t *lenr,uint8_t *response);
  virtual RSCT_IFD_RESULT PVMVT1(int Result,uint8_t *rbuffer,uint32_t rlen,uint32_t *lenr);
	virtual bool PinDirectSupported();
	virtual CJ_RESULT SpecialLess3_0_41();
	virtual void FillTeoPrologue(uint8_t *pbTeoPrologue);
    virtual void CheckReaderDepended(CCID_Message &Message);



	virtual int cjccid_SecurePV(uint8_t Timeout,
                    uint8_t PinPosition,uint8_t PinType,
                    uint8_t PinLengthSize,uint8_t PinLength,
                    uint8_t PinLengthPosition,
                    uint8_t Min, uint8_t Max,
                    uint8_t Condition,uint8_t *Prologue,
                    uint8_t *out,int out_len,uint8_t *in,int *in_len,uint8_t *Text,uint8_t Textlen,uint8_t bMessageIndex,uint8_t bNumberMessage);

	virtual int cjccid_SecureMV(uint8_t Timeout,
                    uint8_t PinPosition,uint8_t PinType,
                    uint8_t PinLengthSize,uint8_t PinLength,
                    uint8_t PinLengthPosition,
                    uint8_t Min, uint8_t Max,
                    uint8_t bConfirmPIN,
                    uint8_t Condition,uint8_t *Prologue,
                    uint8_t OffsetOld,uint8_t OffsetNew,
                    uint8_t *out,int out_len,uint8_t *in,int *in_len,int TextCount,uint8_t *Text[3],uint8_t Textlen[3],uint8_t bMessageIndex[3],uint8_t bNumberMessage);

  uint8_t IFSC;
  uint8_t PCB_seq;
  uint8_t EDC; 
};

#endif

