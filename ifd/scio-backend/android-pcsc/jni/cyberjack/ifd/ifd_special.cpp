/***************************************************************************
    begin       : Tue Mar 24 2009
    copyright   : (C) 2009 by Martin Preuss
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

/* this file is included from ifd.cpp */



int8_t IFDHandler::_specialKeyUpdate(IFDHandler::Context *ctx,
                                     uint16_t cmd_len,
                                     const uint8_t *cmd,
                                     uint16_t *response_len,
                                     uint8_t *response) {
  uint8_t dataLen;
  CReader *r;

  r=ctx->getReader();
  if (r==NULL) {
    DEBUGDEV("DRIVER", DEBUG_MASK_IFD, "No reader");
    return CT_API_RV_ERR_INVALID;
  }

  if (cmd[2] & 0x20)
    /* first block */
    ctx->dataToFlash.erase();
  if (cmd[2] & 0x40) {
    /* abort */
    ctx->dataToFlash.erase();
    response[0]=0x90;
    response[1]=0x00;
    *response_len=2;
    return CT_API_RV_OK;
  }

  /* determine length of data */
  if (cmd_len<5) {
    DEBUGDEV("DRIVER", DEBUG_MASK_IFD, "APDU too short");
    return CT_API_RV_ERR_INVALID;
  }

  /* add data */
  dataLen=cmd[4];
  if (dataLen)
    ctx->dataToFlash+=std::string((const char*) (cmd+5), dataLen);

  if (cmd[2] & 0x80) {
    uint32_t result;
    int rv;

    /* finished */
    DEBUGDEV("DRIVER", DEBUG_MASK_IFD, "Updating key (%d bytes)", (int) (ctx->dataToFlash.size()));
    rv=r->CtKeyUpdate((uint8_t*) ctx->dataToFlash.data(), ctx->dataToFlash.size(), &result);
    if (rv!=CJ_SUCCESS) {
      DEBUGDEV("DRIVER", DEBUG_MASK_IFD, "Unable to update the keys (%d / %d)\n", rv, result);
      return CT_API_RV_ERR_CT;
    }
  }

  response[0]=0x90;
  response[1]=0x00;
  *response_len=2;
  return CT_API_RV_OK;
}



int8_t IFDHandler::_specialUploadMod(IFDHandler::Context *ctx,
                                     uint16_t cmd_len,
                                     const uint8_t *cmd,
                                     uint16_t *response_len,
                                     uint8_t *response) {
  uint8_t dataLen;
  CReader *r;

  r=ctx->getReader();
  if (r==NULL) {
    DEBUGDEV("DRIVER", DEBUG_MASK_IFD, "No reader");
    return CT_API_RV_ERR_INVALID;
  }

  DEBUGDEV("DRIVER", DEBUG_MASK_IFD, "Module Upload");

  if (cmd[2] & 0x20)
    /* first block */
    ctx->dataToFlash.erase();
  if (cmd[2] & 0x40) {
    /* abort */
    ctx->dataToFlash.erase();
    response[0]=0x90;
    response[1]=0x00;
    *response_len=2;
    return CT_API_RV_OK;
  }

  /* determine length of data */
  if (cmd_len<5) {
    DEBUGDEV("DRIVER", DEBUG_MASK_IFD, "APDU too short");
    return CT_API_RV_ERR_INVALID;
  }

  /* add data */
  dataLen=cmd[4];
  if (dataLen)
    ctx->dataToFlash+=std::string((const char*) (cmd+5), dataLen);

  response[0]=0x90;
  response[1]=0x00;
  *response_len=2;
  return CT_API_RV_OK;
}



int8_t IFDHandler::_specialUploadSig(IFDHandler::Context *ctx,
                                     uint16_t cmd_len,
                                     const uint8_t *cmd,
                                     uint16_t *response_len,
                                     uint8_t *response) {
  uint8_t dataLen;
  CReader *r;

  r=ctx->getReader();
  if (r==NULL) {
    DEBUGDEV("DRIVER", DEBUG_MASK_IFD, "No reader");
    return CT_API_RV_ERR_INVALID;
  }

  DEBUGDEV("DRIVER", DEBUG_MASK_IFD, "Signature Upload");

  if (cmd[2] & 0x20)
    /* first block */
    ctx->signatureToFlash.erase();
  if (cmd[2] & 0x40) {
    /* abort */
    ctx->signatureToFlash.erase();
    response[0]=0x90;
    response[1]=0x00;
    *response_len=2;
    return CT_API_RV_OK;
  }

  /* determine length of data */
  if (cmd_len<5) {
    DEBUGDEV("DRIVER", DEBUG_MASK_IFD, "APDU too short");
    return CT_API_RV_ERR_INVALID;
  }

  /* add data */
  dataLen=cmd[4];
  if (dataLen)
    ctx->signatureToFlash+=std::string((const char*) (cmd+5), dataLen);

  response[0]=0x90;
  response[1]=0x00;
  *response_len=2;
  return CT_API_RV_OK;
}



int8_t IFDHandler::_specialUploadFlash(IFDHandler::Context *ctx,
                                       uint16_t cmd_len,
                                       const uint8_t *cmd,
                                       uint16_t *response_len,
                                       uint8_t *response) {
  uint32_t result;
  int rv;
  CReader *r;

  r=ctx->getReader();
  if (r==NULL) {
    DEBUGDEV("DRIVER", DEBUG_MASK_IFD, "No reader");
    return CT_API_RV_ERR_INVALID;
  }

  if (ctx->dataToFlash.size()<1 || ctx->signatureToFlash.size()<1) {
    DEBUGDEV("DRIVER", DEBUG_MASK_IFD, "Please upload module and signature first");
    return CT_API_RV_ERR_INVALID;
  }

  /* flash data */
  DEBUGDEV("DRIVER", DEBUG_MASK_IFD, "Flashing module (%d bytes)\n", (int)(ctx->dataToFlash.size()));
  rv=r->CtLoadModule((uint8_t*) ctx->dataToFlash.data(), ctx->dataToFlash.size(),
                               (uint8_t*) ctx->signatureToFlash.data(), ctx->signatureToFlash.size(),
                               &result);
  if (rv!=CJ_SUCCESS) {
    DEBUGDEV("DRIVER", DEBUG_MASK_IFD, "Unable to flash the module (%d / %d)\n", rv, result);
    return CT_API_RV_ERR_CT;
  }

  response[0]=0x90;
  response[1]=0x00;
  *response_len=2;
  return CT_API_RV_OK;
}



int8_t IFDHandler::_specialUploadInfo(IFDHandler::Context *ctx,
                                      uint16_t cmd_len,
                                      const uint8_t *cmd,
                                      uint16_t *response_len,
                                      uint8_t *response) {
  int rv;
  int lr;
  uint32_t estimatedUpdateTime=0;
  cj_ModuleInfo mi;
  CReader *r;

  r=ctx->getReader();
  if (r==NULL) {
    DEBUGDEV("DRIVER", DEBUG_MASK_IFD, "No reader");
    return CT_API_RV_ERR_INVALID;
  }

  if (ctx->dataToFlash.size()<1) {
    DEBUGDEV("DRIVER", DEBUG_MASK_IFD, "Please upload module first");
    return CT_API_RV_ERR_INVALID;
  }

  mi.SizeOfStruct=sizeof(cj_ModuleInfo);
  rv=r->CtGetModuleInfoFromFile((uint8_t*) ctx->dataToFlash.data(), ctx->dataToFlash.size(),
                                          &mi, &estimatedUpdateTime);
  if (rv!=CJ_SUCCESS) {
    DEBUGDEV("DRIVER", DEBUG_MASK_IFD, "Unable to extract module info (%d)\n", rv);
    return CT_API_RV_ERR_CT;
  }

  if (*response_len<(2+sizeof(cj_ModuleInfo))) {
    DEBUGDEV("DRIVER", DEBUG_MASK_IFD, "Response buffer too short");
    return CT_API_RV_ERR_MEMORY;
  }

  memmove(response, (const void*) &mi, sizeof(cj_ModuleInfo));
  lr=sizeof(cj_ModuleInfo);
  response[lr++]=0x90;
  response[lr++]=0x00;
  *response_len=lr;
  return CT_API_RV_OK;
}



int8_t IFDHandler::_specialDeleteAllMods(IFDHandler::Context *ctx,
                                         uint16_t cmd_len,
                                         const uint8_t *cmd,
                                         uint16_t *response_len,
                                         uint8_t *response) {
  uint32_t result;
  int rv;
  CReader *r;

  r=ctx->getReader();
  if (r==NULL) {
    DEBUGDEV("DRIVER", DEBUG_MASK_IFD, "No reader");
    return CT_API_RV_ERR_INVALID;
  }

  /* delete all modules */
  DEBUGDEV("DRIVER", DEBUG_MASK_IFD, "Deleting all modules");
  rv=r->CtDeleteALLModules(&result);
  if (rv!=CJ_SUCCESS) {
    DEBUGDEV("DRIVER", DEBUG_MASK_IFD, "Unable to delete all modules (%d / %d)\n", rv, result);
    return CT_API_RV_ERR_CT;
  }

  response[0]=0x90;
  response[1]=0x00;
  *response_len=2;
  return CT_API_RV_OK;
}



int8_t IFDHandler::_specialShowAuth(IFDHandler::Context *ctx,
                                    uint16_t cmd_len,
                                    const uint8_t *cmd,
                                    uint16_t *response_len,
                                    uint8_t *response) {
  int rv;
  CReader *r;

  r=ctx->getReader();
  if (r==NULL) {
    DEBUGDEV("DRIVER", DEBUG_MASK_IFD, "No reader");
    return CT_API_RV_ERR_INVALID;
  }

  /* delete all modules */
  rv=r->CtShowAuth();
  if (rv!=CJ_SUCCESS) {
    DEBUGDEV("DRIVER", DEBUG_MASK_IFD, "Unable to show auth info (%d)\n", rv);
    return CT_API_RV_ERR_CT;
  }

  response[0]=0x90;
  response[1]=0x00;
  *response_len=2;
  return CT_API_RV_OK;
}



int8_t IFDHandler::_specialGetModuleCount(IFDHandler::Context *ctx,
                                          uint16_t cmd_len,
                                          const uint8_t *cmd,
                                          uint16_t *response_len,
                                          uint8_t *response) {
  int rv;
  CReader *r;

  r=ctx->getReader();
  if (r==NULL) {
    DEBUGDEV("DRIVER", DEBUG_MASK_IFD, "No reader");
    return CT_API_RV_ERR_INVALID;
  }

  if (*response_len<3) {
    DEBUGDEV("DRIVER", DEBUG_MASK_IFD, "Response buffer too short");
    return CT_API_RV_ERR_MEMORY;
  }

  if (ctx->moduleCount==SCARD_AUTOALLOCATE) {
    if (ctx->moduleList)
      free(ctx->moduleList);
    ctx->moduleList=NULL;
    rv=r->CtListModules(&(ctx->moduleCount), (cj_ModuleInfo*) &(ctx->moduleList));
    if (rv!=CJ_SUCCESS) {
      DEBUGDEV("DRIVER", DEBUG_MASK_IFD, "Unable to list module infos (%d)\n", rv);
      return CT_API_RV_ERR_CT;
    }
  }
  response[0]=(ctx->moduleCount<256)?ctx->moduleCount:255;

  response[1]=0x90;
  response[2]=0x00;
  *response_len=3;
  return CT_API_RV_OK;
}



int8_t IFDHandler::_specialGetModuleInfo(IFDHandler::Context *ctx,
                                         uint16_t cmd_len,
                                         const uint8_t *cmd,
                                         uint16_t *response_len,
                                         uint8_t *response) {
  int rv;
  unsigned int idx;
  int lr;
  CReader *r;

  r=ctx->getReader();
  if (r==NULL) {
    DEBUGDEV("DRIVER", DEBUG_MASK_IFD, "No reader");
    return CT_API_RV_ERR_INVALID;
  }


  if (ctx->moduleCount==SCARD_AUTOALLOCATE) {
    if (ctx->moduleList)
      free(ctx->moduleList);
    ctx->moduleList=NULL;
    /* this is really ugly */
    rv=r->CtListModules(&(ctx->moduleCount), (cj_ModuleInfo*) &(ctx->moduleList));
    if (rv!=CJ_SUCCESS) {
      DEBUGDEV("DRIVER", DEBUG_MASK_IFD, "Unable to list module infos (%d)\n", rv);
      return CT_API_RV_ERR_CT;
    }
  }

  lr=0;

  idx=cmd[2]; /* p1 */
  if (idx>=ctx->moduleCount) {
    /* EOF met */
    response[lr++]=0x62;
    response[lr++]=0x82;
    *response_len=lr;
    return CT_API_RV_OK;
  }

  if (*response_len<(2+sizeof(cj_ModuleInfo))) {
    DEBUGDEV("DRIVER", DEBUG_MASK_IFD, "Response buffer too short");
    return CT_API_RV_ERR_MEMORY;
  }

  memmove(response, (const void*) &(ctx->moduleList[idx]), sizeof(cj_ModuleInfo));
  lr+=sizeof(cj_ModuleInfo);
  response[lr++]=0x90;
  response[lr++]=0x00;
  *response_len=lr;
  return CT_API_RV_OK;
}



int8_t IFDHandler::_specialGetReaderInfo(IFDHandler::Context *ctx,
                                         uint16_t cmd_len,
                                         const uint8_t *cmd,
                                         uint16_t *response_len,
                                         uint8_t *response) {
  int rv;
  int lr;
  cj_ReaderInfo ri;
  CReader *r;

  r=ctx->getReader();
  if (r==NULL) {
    DEBUGDEV("DRIVER", DEBUG_MASK_IFD, "No reader");
    return CT_API_RV_ERR_INVALID;
  }

  lr=0;

  if (*response_len<(2+sizeof(cj_ReaderInfo))) {
    DEBUGDEV("DRIVER", DEBUG_MASK_IFD, "Response buffer too short");
    return CT_API_RV_ERR_MEMORY;
  }

  memset(&ri, 0, sizeof(cj_ReaderInfo));
  ri.SizeOfStruct=sizeof(cj_ReaderInfo);

  rv=r->CtGetReaderInfo(&ri);
  if (rv!=CJ_SUCCESS) {
    DEBUGDEV("DRIVER", DEBUG_MASK_IFD, "Unable to get reader info (%d)\n", rv);
    return CT_API_RV_ERR_CT;
  }

  memmove(response, (const void*) &ri, sizeof(cj_ReaderInfo));
  lr+=sizeof(cj_ReaderInfo);
  response[lr++]=0x90;
  response[lr++]=0x00;
  *response_len=lr;
  return CT_API_RV_OK;
}










int8_t IFDHandler::_special(IFDHandler::Context *ctx,
                            uint16_t cmd_len,
                            const uint8_t *cmd,
                            uint16_t *response_len,
                            uint8_t *response) {
  int8_t rv;

  DEBUGDEV("DRIVER", DEBUG_MASK_IFD, "Received special command %02x %02x %02x %02x",
           cmd[0], cmd[1], cmd[2], cmd[3]);

  if (cmd[0]!=CJ_SPECIAL_CLA) {
    DEBUGDEV("DRIVER", DEBUG_MASK_IFD, "Special command but no special CLA byte (%02x)", cmd[0]);
    return CT_API_RV_ERR_INVALID;
  }

  switch(cmd[1]) {
  case CJ_SPECIAL_INS_KEYUPDATE:
    rv=_specialKeyUpdate(ctx, cmd_len, cmd, response_len, response);
    break;
  case CJ_SPECIAL_INS_DELETEALLMODS:
    rv=_specialDeleteAllMods(ctx, cmd_len, cmd, response_len, response);
    break;
  case CJ_SPECIAL_INS_UPLOADMOD:
    rv=_specialUploadMod(ctx, cmd_len, cmd, response_len, response);
    break;
  case CJ_SPECIAL_INS_UPLOADSIG:
    rv=_specialUploadSig(ctx, cmd_len, cmd, response_len, response);
    break;
  case CJ_SPECIAL_INS_UPLOADFLASH:
    rv=_specialUploadFlash(ctx, cmd_len, cmd, response_len, response);
    break;
  case CJ_SPECIAL_INS_UPLOADINFO:
    rv=_specialUploadInfo(ctx, cmd_len, cmd, response_len, response);
    break;
  case CJ_SPECIAL_INS_SHOWAUTH:
    rv=_specialShowAuth(ctx, cmd_len, cmd, response_len, response);
    break;
  case CJ_SPECIAL_INS_GETMODCOUNT:
    rv=_specialGetModuleCount(ctx, cmd_len, cmd, response_len, response);
    break;
  case CJ_SPECIAL_INS_GETMODINFO:
    rv=_specialGetModuleInfo(ctx, cmd_len, cmd, response_len, response);
    break;
  case CJ_SPECIAL_INS_GETREADERINFO:
    rv=_specialGetReaderInfo(ctx, cmd_len, cmd, response_len, response);
    break;

  default:
    DEBUGDEV("DRIVER", DEBUG_MASK_IFD, "Invalid special command (%02x)", cmd[1]);
    rv=CT_API_RV_ERR_INVALID;
  }

  /* done */
  return rv;
}



