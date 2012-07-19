


#ifdef HAVE_CONFIG_H
#include <config.h>
#endif

#include "ifd.h"
#include "Platform.h"
#include "cjeca32.h"
#include "Reader.h"
#define FEATURE_EXECUTE_PACE             0x20
#include "Debug.h"

#include "config_l.h"
#include "cyberjack_l.h"


#ifdef ENABLE_NONSERIAL
# include "ausb_l.h"
# include "usbdev_l.h"
#endif

#include <sys/stat.h>




/*
 * Not exported constants definition
 */

/* Maximum number of readers handled */
#define IFDH_MAX_READERS        32

/* Maximum number of slots per reader handled */
#define IFDH_MAX_SLOTS          1

#define PATHMAX 256

#define TAG_IFD_ATR_WIN32 0x90303


/* uncomment to disable sending commands to the reader */
/*#define PART10_DISABLE_APDU*/



#define WINDOWS_CTL_GET_FEATURE 0x313520
#define WINDOWS_CTL_GET_FEATURE2 0x42000c20


#define DEBUGLUN(MACRO_LUN, debug_mask, format, ...) {                       \
  char dbg_lun_buffer[32];                                                   \
  char dbg_buffer[256];                                                      \
                                                                             \
  snprintf(dbg_lun_buffer, sizeof(dbg_lun_buffer)-1, "LUN%X", (int) MACRO_LUN);  \
  snprintf(dbg_buffer, sizeof(dbg_buffer)-1,                                 \
  __FILE__":%5d: " format  , __LINE__ , ##__VA_ARGS__);                      \
  dbg_buffer[sizeof(dbg_buffer)-1]=0;                                        \
  Debug.Out(dbg_lun_buffer, debug_mask, dbg_buffer,0,0);                     \
}



#define DEBUGDEV(devName, debug_mask, format, ...) {\
  char dbg_buffer[256]; \
  \
  snprintf(dbg_buffer, sizeof(dbg_buffer)-1,\
  __FILE__":%5d: " format  , __LINE__ , ##__VA_ARGS__); \
  dbg_buffer[sizeof(dbg_buffer)-1]=0; \
  Debug.Out(devName, debug_mask, dbg_buffer,0,0); \
}



#ifdef HAVE_PTHREAD_H
#  define MUTEX_LOCK(MACRO_MUTEX) pthread_mutex_lock(&MACRO_MUTEX)
#  define MUTEX_UNLOCK(MACRO_MUTEX) pthread_mutex_unlock(&MACRO_MUTEX)
#else
# define MUTEX_LOCK(MACRO_MUTEX)
#  define MUTEX_UNLOCK(MACRO_MUTEX)
#endif




static int rsct_ifd_driver_initialized=0;
static IFDHandler rsct_ifd_handler;



/* Add new devices here */
static bool _isDeviceSupported(uint16_t vendorId, uint16_t productId) {
  return (vendorId==0xc4b) &&
    (productId==0x300 ||
     productId==0x400 ||
     productId==0x401 ||
     productId==0x500 ||
     productId==0x501);
}




#ifdef ENABLE_NONSERIAL
static rsct_usbdev_t *_findUsbDevByName(rsct_usbdev_t *d, const char *s) {
  int vendorId, productId, busId, busPos;

  if (strstr(s, ":libusb:")!=NULL) {
    if (sscanf(s, "usb:%04x/%04x:libusb:%03d:%03d",
	       &vendorId, &productId, &busId, &busPos)!=4) {
      DEBUGDEV(s,DEBUG_MASK_IFD, "Bad device string [%s]\n", s);
      return NULL;
    }

    while(d) {
      if ((d->busId==(uint32_t)busId) &&
	  (d->busPos==(uint32_t)busPos) &&
	  (d->vendorId==(uint32_t)vendorId) &&
	  (d->productId==(uint32_t)productId))
	break;
      d=d->next;
    }
  }
  else if (strstr(s, ":libhal:")!=NULL) {
    const char *t;

    t=strstr(s, ":libhal:");
    t+=8;

    while(d) {
      if (d->halUDI && strcasecmp(t, d->halUDI)==0)
	break;
      d=d->next;
    }
  }

  return d;
}



static void _logAusb(ausb_dev_handle *ah,
                     const char *text,
                     const void *pData, uint32_t ulDataLen) {
  rsct_debug_out("<USB>",
                 DEBUG_MASK_COMMUNICATION_IN,
                 (char*)text,
                 (char*)pData, ulDataLen);
}
#endif






IFDHandler::IFDHandler() {
 fprintf(stderr, "CYBERJACK: Started\n");
#ifdef HAVE_PTHREAD_H
  pthread_mutex_init(&m_contextMutex, NULL);
#endif
}



IFDHandler::~IFDHandler() {
  MUTEX_LOCK(m_contextMutex);

  /* the map contains pointers, so delete their targets first */
  for (ContextMap::iterator it=m_contextMap.begin();
       it!=m_contextMap.end();
       it++) {
    delete it->second;
    it->second=NULL;
  }
  /* then clear the map */
  m_contextMap.clear();

  MUTEX_UNLOCK(m_contextMutex);

  DEBUGDEV("DRIVER", DEBUG_MASK_IFD, "Driver deinitialized");
  rsct_config_fini();

#ifdef HAVE_PTHREAD_H
  pthread_mutex_destroy(&m_contextMutex);
#endif
}



int IFDHandler::init() {
  unsigned int nLevelMask=0;
  const char *s;

  rsct_config_init();


  /* generic debug */
  if (rsct_config_get_flags() &
      (CT_FLAGS_DEBUG_GENERIC |
       CT_FLAGS_DEBUG_READER)) {
    nLevelMask|=
      DEBUG_MASK_RESULTS |
      DEBUG_MASK_COMMUNICATION_ERROR;
  }

  /* ECA debugging */
  if (rsct_config_get_flags() & CT_FLAGS_DEBUG_ECA) {
    nLevelMask|=
      DEBUG_MASK_INPUT |
      DEBUG_MASK_OUTPUT |
      DEBUG_MASK_TRANSLATION;
  }

  /* USB debug */
  if (rsct_config_get_flags() &
      (CT_FLAGS_DEBUG_AUSB |
       CT_FLAGS_DEBUG_USB)) {
    nLevelMask|=
      DEBUG_MASK_COMMUNICATION_OUT |
      DEBUG_MASK_COMMUNICATION_IN |
      DEBUG_MASK_COMMUNICATION_ERROR |
      DEBUG_MASK_COMMUNICATION_INFO |
      DEBUG_MASK_COMMUNICATION_INT;
  }

  /* misc debug */
  if (rsct_config_get_flags() & CT_FLAGS_DEBUG_CTAPI) {
    nLevelMask|=DEBUG_MASK_CTAPI;
  }
  if (rsct_config_get_flags() & CT_FLAGS_DEBUG_IFD) {
    nLevelMask|=DEBUG_MASK_IFD;
  }

  /* set resulting debug mask */
  Debug.setLevelMask(nLevelMask);

  /* set log file */
  s=rsct_config_get_debug_filename();
  if (s) {
    struct stat st;

    Debug.setLogFileName(s);

    /* check for log file size */
    if (stat(s, &st)==0) {
      if (st.st_size>CT_LOGFILE_LIMIT) {
	if (truncate(s, 0)==0) {
	  DEBUGDEV("DRIVER", DEBUG_MASK_IFD, "Truncated log file");
	}
      }
    }
  }

#ifdef ENABLE_NONSERIAL
  ausb_set_log_fn(_logAusb);

  /* init usbdev interface */
  if (rsct_usbdev_init()<0) {
    DEBUGDEV("DRIVER", DEBUG_MASK_IFD, "Error on rsct_usbdev_init, maybe hald is not running?");
    return -1;
  }
#endif

  DEBUGDEV("DRIVER", DEBUG_MASK_IFD, "Driver initialized");
  return 0;
}



RESPONSECODE IFDHandler::createChannel(DWORD Lun, DWORD Channel) {
  unsigned short ctn, slot;
  ContextMap::iterator it;
  Context *ctx;
  rsct_usbdev_t *devs=NULL;
  rsct_usbdev_t *d;
  int rv;
  CReader *r;
  char ubuf[128];
  uint32_t busId;
  uint32_t busPos;

  ctn=((unsigned short) (Lun >> 16)) & 0xffff;
  slot=((unsigned short) (Lun & 0x0000FFFF)) % IFDH_MAX_SLOTS;
  if (ctn>=IFDH_MAX_READERS || slot>=IFDH_MAX_SLOTS) {
    DEBUGLUN(Lun, DEBUG_MASK_IFD, "Invalid LUN %X\n", (int)Lun);
    return IFD_COMMUNICATION_ERROR;
  }

  /* look for context */
  MUTEX_LOCK(m_contextMutex);
  it=m_contextMap.find(ctn);
  if (it!=m_contextMap.end()) {
    DEBUGLUN(Lun, DEBUG_MASK_IFD, "LUN %X is already in use when opening channel %d\n", (int) Lun, (int) Channel);
    MUTEX_UNLOCK(m_contextMutex);
    return IFD_COMMUNICATION_ERROR;
  }


  /* scan for devices */
  rv=rsct_usbdev_scan(&devs);
  if (rv<0) {
    DEBUGLUN(Lun, DEBUG_MASK_IFD, "Error on scan (%d)\n", rv);
    rsct_usbdev_list_free(devs);
    MUTEX_UNLOCK(m_contextMutex);
    return IFD_COMMUNICATION_ERROR;
  }

  /* find first supported device which is not used */
  d=devs;
  DEBUGLUN(Lun, DEBUG_MASK_IFD, "Looking for device (%d, %d)\n", (int) Lun, (int) Channel);
  while(d) {
    /* check supported cyberJacks only */
    if (_isDeviceSupported(d->vendorId, d->productId)) {
      ContextMap::iterator it;
      bool devInUse=false;

      /* check whether this device is in use */
      DEBUGLUN(Lun, DEBUG_MASK_IFD, "Device %04x:%04x at %03d/%03d supported, checking whether it is in use (%d, %d)\n",
               d->vendorId, d->productId,
               d->busId, d->busPos,
               (int) Lun, (int) Channel);
      for (it=m_contextMap.begin(); it!=m_contextMap.end(); it++) {
	if ((d->busId==it->second->busId) &&
	    (d->busPos==it->second->busPos)) {
	  devInUse=true;
	  DEBUGLUN(Lun, DEBUG_MASK_IFD, "Device %04x:%04x at %03d/%03d is already in use (%d, %d)\n",
		   d->vendorId, d->productId,
		   d->busId, d->busPos,
		   (int) Lun, (int) Channel);
	  break;
	}
      }
      if (!devInUse) {
	/* device is unused, use it */
	DEBUGLUN(Lun, DEBUG_MASK_IFD, "Device %04x:%04x at %03d/%03d is free (%d, %d)\n",
		 d->vendorId, d->productId,
		 d->busId, d->busPos,
		 (int) Lun, (int) Channel);
	break;
      }
    }
    else {
      DEBUGLUN(Lun, DEBUG_MASK_IFD, "Device %04x:%04x at %03d/%03d is not supported (%d, %d)\n",
	       d->vendorId, d->productId,
	       d->busId, d->busPos,
	       (int) Lun, (int) Channel);
    }
    d=d->next;
  }
  if (d==NULL) {
    DEBUGLUN(Lun, DEBUG_MASK_IFD, "Device not found (Lun=%d, Channel=%d)\n", (int)Lun, (int)Channel);
    rsct_usbdev_list_free(devs);
    MUTEX_UNLOCK(m_contextMutex);
    return IFD_COMMUNICATION_ERROR;
  }
  busId=d->busId;
  busPos=d->busPos;

  /* create device name from USB info */
  snprintf(ubuf, sizeof(ubuf),
           "usb:%04x/%04x:libusb:%03d:%03d",
           d->vendorId,
           d->productId,
           d->busId,
           d->busPos);
  rsct_usbdev_list_free(devs);

  /* ok, device found, create reader object and connect it to USB layer */
  r=new CReader(ubuf);
  rv=r->Connect();
  if (rv!=CJ_SUCCESS) {
    DEBUGLUN(Lun, DEBUG_MASK_IFD, "Unable to connect reader \"%s\" (%d)\n", ubuf, rv);
    delete r;
    MUTEX_UNLOCK(m_contextMutex);
    return IFD_COMMUNICATION_ERROR;
  }

  /* reader created and conected, create context */
  ctx=new Context(Lun, r);
  ctx->busId=busId;
  ctx->busPos=busPos;
  m_contextMap.insert(ContextMap::value_type(ctn, ctx));

  /* done */
  DEBUGLUN(Lun, DEBUG_MASK_IFD, "Device \"%s\" connected at channel %d\n", ubuf, (int) Channel);
  MUTEX_UNLOCK(m_contextMutex);
  return IFD_SUCCESS;
}



#ifdef OS_DARWIN
RESPONSECODE IFDHandler::createChannelByName(DWORD Lun, char *devName) {
  unsigned short ctn, slot;
  ContextMap::iterator it;
  Context *ctx;
  rsct_usbdev_t *devs=NULL;
  rsct_usbdev_t *d;
  int rv;
  CReader *r;
  char ubuf[128];
  uint32_t busId;
  uint32_t busPos;
  uint16_t wantedPid=0x400;

  ctn=((unsigned short) (Lun >> 16)) & 0xffff;
  slot=((unsigned short) (Lun & 0x0000FFFF)) % IFDH_MAX_SLOTS;
  if (ctn>=IFDH_MAX_READERS || slot>=IFDH_MAX_SLOTS) {
    DEBUGLUN(Lun, DEBUG_MASK_IFD, "Invalid LUN %X\n", (int)Lun);
    return IFD_COMMUNICATION_ERROR;
  }

  /* Darwin uses the friendly name here, so we need to extract the PID */
  if (strcasecmp(devName, "REINER SCT cyberJack pp_a")==0)
    wantedPid=0x300;
  else if (strcasecmp(devName, "REINER SCT cyberJack ecom_a")==0)
    wantedPid=0x400;
  else if (strcasecmp(devName, "REINER SCT cyberJack pp_a2")==0)
    wantedPid=0x401;
  else if (strcasecmp(devName, "REINER SCT cyberJack RFID standard")==0)
    wantedPid=0x500;
  else if (strcasecmp(devName, "REINER SCT cyberJack RFID komfort")==0)
    wantedPid=0x501;
  else if (strcasecmp(devName, "REINER SCT cyberJack compact")==0)
    wantedPid=0x502;

  /* look for context */
  MUTEX_LOCK(m_contextMutex);
  it=m_contextMap.find(ctn);
  if (it!=m_contextMap.end()) {
    DEBUGLUN(Lun, DEBUG_MASK_IFD, "LUN %X is already in use when opening channel\n", (int) Lun);
    MUTEX_UNLOCK(m_contextMutex);
    return IFD_COMMUNICATION_ERROR;
  }


  /* scan for devices */
  rv=rsct_usbdev_scan(&devs);
  if (rv<0) {
    DEBUGLUN(Lun, DEBUG_MASK_IFD, "Error on scan (%d)\n", rv);
    rsct_usbdev_list_free(devs);
    MUTEX_UNLOCK(m_contextMutex);
    return IFD_COMMUNICATION_ERROR;
  }

  /* find first supported device which is not used */
  d=devs;
  DEBUGLUN(Lun, DEBUG_MASK_IFD, "Looking for device (%d)\n", (int) Lun);
  while(d) {
    /* check supported cyberJacks only */
    if (d->vendorId==0xc4b && d->productId==wantedPid) {
      ContextMap::iterator it;
      bool devInUse=false;

      /* check whether this device is in use */
      DEBUGLUN(Lun, DEBUG_MASK_IFD, "Device %04x:%04x at %03d/%03d supported, checking whether it is in use (%d)\n",
               d->vendorId, d->productId,
               d->busId, d->busPos,
               (int) Lun);
      for (it=m_contextMap.begin(); it!=m_contextMap.end(); it++) {
	if ((d->busId==it->second->busId) &&
	    (d->busPos==it->second->busPos)) {
	  devInUse=true;
	  DEBUGLUN(Lun, DEBUG_MASK_IFD, "Device %04x:%04x at %03d/%03d is already in use (%d)\n",
		   d->vendorId, d->productId,
		   d->busId, d->busPos,
		   (int) Lun);
	  break;
	}
      }
      if (!devInUse) {
	/* device is unused, use it */
	DEBUGLUN(Lun, DEBUG_MASK_IFD, "Device %04x:%04x at %03d/%03d is free (%d)\n",
		 d->vendorId, d->productId,
		 d->busId, d->busPos,
		 (int) Lun);
	break;
      }
    }
    else {
      DEBUGLUN(Lun, DEBUG_MASK_IFD, "Device %04x:%04x at %03d/%03d is not supported (%d)\n",
	       d->vendorId, d->productId,
	       d->busId, d->busPos,
	       (int) Lun);
    }
    d=d->next;
  }
  if (d==NULL) {
    DEBUGLUN(Lun, DEBUG_MASK_IFD, "Device not found (Lun=%d)\n", (int)Lun);
    rsct_usbdev_list_free(devs);
    MUTEX_UNLOCK(m_contextMutex);
    return IFD_COMMUNICATION_ERROR;
  }
  busId=d->busId;
  busPos=d->busPos;

  /* create device name from USB info */
  snprintf(ubuf, sizeof(ubuf),
           "usb:%04x/%04x:libusb:%03d:%03d",
           d->vendorId,
           d->productId,
           d->busId,
           d->busPos);
  rsct_usbdev_list_free(devs);

  /* ok, device found, create reader object and connect it to USB layer */
  r=new CReader(ubuf);
  rv=r->Connect();
  if (rv!=CJ_SUCCESS) {
    DEBUGLUN(Lun, DEBUG_MASK_IFD, "Unable to connect reader \"%s\" (%d)\n", ubuf, rv);
    delete r;
    MUTEX_UNLOCK(m_contextMutex);
    return IFD_COMMUNICATION_ERROR;
  }

  /* reader created and conected, create context */
  ctx=new Context(Lun, r);
  ctx->busId=busId;
  ctx->busPos=busPos;
  m_contextMap.insert(ContextMap::value_type(ctn, ctx));

  /* done */
  DEBUGLUN(Lun, DEBUG_MASK_IFD, "Device \"%s\" connected\n", ubuf);
  MUTEX_UNLOCK(m_contextMutex);
  return IFD_SUCCESS;



}


#else


RESPONSECODE IFDHandler::createChannelByName(DWORD Lun, char *devName) {
  unsigned short ctn, slot;
  ContextMap::iterator it;
  Context *ctx;
  rsct_usbdev_t *devs=NULL;
  rsct_usbdev_t *d;
  int rv;
  CReader *r;
  uint32_t busId;
  uint32_t busPos;

  ctn=((unsigned short) (Lun >> 16)) & 0xffff;
  slot=((unsigned short) (Lun & 0x0000FFFF)) % IFDH_MAX_SLOTS;
  if (ctn>=IFDH_MAX_READERS || slot>=IFDH_MAX_SLOTS) {
    DEBUGLUN(Lun, DEBUG_MASK_IFD, "Invalid LUN %X\n", (int)Lun);
    return IFD_COMMUNICATION_ERROR;
  }

  /* look for context */
  MUTEX_LOCK(m_contextMutex);
  it=m_contextMap.find(ctn);
  if (it!=m_contextMap.end()) {
    DEBUGLUN(Lun, DEBUG_MASK_IFD, "LUN %X is already in use when opening \"%s\"\n", (int) Lun, devName);
    MUTEX_UNLOCK(m_contextMutex);
    return IFD_COMMUNICATION_ERROR;
  }


  /* scan for devices */
  rv=rsct_usbdev_scan(&devs);
  if (rv<0) {
    DEBUGLUN(Lun, DEBUG_MASK_IFD, "Error on scan (%d)\n", rv);
    rsct_usbdev_list_free(devs);
    MUTEX_UNLOCK(m_contextMutex);
    return IFD_COMMUNICATION_ERROR;
  }

  /* find requested device */
  d=_findUsbDevByName(devs, devName);
  if (d==NULL) {
    DEBUGLUN(Lun, DEBUG_MASK_IFD, "Device \"%s\" not found\n", devName);
    rsct_usbdev_list_free(devs);
    MUTEX_UNLOCK(m_contextMutex);
    return IFD_COMMUNICATION_ERROR;
  }
  busId=d->busId;
  busPos=d->busPos;
  rsct_usbdev_list_free(devs);

  /* ok, device found, create reader object and connect it to USB layer */
  r=new CReader(devName);
  rv=r->Connect();
  if (rv!=CJ_SUCCESS) {
    DEBUGLUN(Lun, DEBUG_MASK_IFD, "Unable to connect reader \"%s\" (%d)\n", devName, rv);
    delete r;
    MUTEX_UNLOCK(m_contextMutex);
    return IFD_COMMUNICATION_ERROR;
  }

  /* reader created and conected, create context */
  ctx=new Context(Lun, r);
  ctx->busId=busId;
  ctx->busPos=busPos;
  m_contextMap.insert(ContextMap::value_type(ctn, ctx));

  /* done */
  DEBUGLUN(Lun, DEBUG_MASK_IFD, "Device \"%s\" connected\n", devName);
  MUTEX_UNLOCK(m_contextMutex);
  return IFD_SUCCESS;
}
#endif



RESPONSECODE IFDHandler::closeChannel(DWORD Lun) {
  unsigned short ctn, slot;
  ContextMap::iterator it;
  Context *ctx;

  ctn=((unsigned short) (Lun >> 16)) & 0xffff;
  slot=((unsigned short) (Lun & 0x0000FFFF)) % IFDH_MAX_SLOTS;
  if (ctn>=IFDH_MAX_READERS || slot>=IFDH_MAX_SLOTS) {
    DEBUGLUN(Lun, DEBUG_MASK_IFD, "Invalid LUN %X\n", (int)Lun);
    return IFD_COMMUNICATION_ERROR;
  }

  /* get context */
  MUTEX_LOCK(m_contextMutex);
  it=m_contextMap.find(ctn);
  if (it==m_contextMap.end()) {
    DEBUGLUN(Lun, DEBUG_MASK_IFD, "LUN %X is not in use\n", (int) Lun);
    MUTEX_UNLOCK(m_contextMutex);
    return IFD_COMMUNICATION_ERROR;
  }
  ctx=it->second;

  /* remove context from map */
  it->second=NULL;
  m_contextMap.erase(it);

  /* disconnect reader, delete context */
  ctx->getReader()->Disonnect();
  delete ctx;

  DEBUGLUN(Lun, DEBUG_MASK_IFD, "Reader disconnected\n");
  MUTEX_UNLOCK(m_contextMutex);
  return IFD_SUCCESS;
}



RESPONSECODE IFDHandler::getCapabilities(DWORD Lun, DWORD Tag, PDWORD Length, PUCHAR Value) {
  unsigned short ctn, slot;
  ContextMap::iterator it;
  Context *ctx;
  RESPONSECODE rc;

  ctn=((unsigned short) (Lun >> 16)) & 0xffff;
  slot=((unsigned short) (Lun & 0x0000FFFF)) % IFDH_MAX_SLOTS;
  if (ctn>=IFDH_MAX_READERS || slot>=IFDH_MAX_SLOTS) {
    DEBUGLUN(Lun, DEBUG_MASK_IFD, "Invalid LUN %X\n", (int)Lun);
    return IFD_COMMUNICATION_ERROR;
  }

  MUTEX_LOCK(m_contextMutex);
  it=m_contextMap.find(ctn);
  if (it==m_contextMap.end()) {
    DEBUGLUN(Lun, DEBUG_MASK_IFD, "LUN %X is not in use\n", (int) Lun);
    MUTEX_UNLOCK(m_contextMutex);
    return IFD_COMMUNICATION_ERROR;
  }
  ctx=it->second;

  ctx->lock();
  MUTEX_UNLOCK(m_contextMutex);

  /* action */
  switch(Tag) {
  case TAG_IFD_ATR:
  case TAG_IFD_ATR_WIN32: {
    ICC_STATE *state=ctx->getState();

    if ((*Length)>=ctx->getAtrLength() && Value) {
      (*Length)=ctx->getAtrLength();
      memcpy (Value, state->ATR, (*Length));
      rc=IFD_SUCCESS;
    }
    else
      rc=IFD_ERROR_TAG;
    break;
  }

#ifdef HAVE_PTHREAD_H
  case TAG_IFD_SIMULTANEOUS_ACCESS:
    if (*Length>=1 && Value) {
      *Length=1;
      *Value=IFDH_MAX_READERS;
      rc=IFD_SUCCESS;
    }
    else
      rc=IFD_ERROR_TAG;
    break;

  case TAG_IFD_THREAD_SAFE:
    if (*Length>=1){
      *Length=1;
      *Value=1; /* allow mutliple readers at the same time */
      rc=IFD_SUCCESS;
    }
    else
      rc=IFD_ERROR_TAG;
    break;
#endif

  case TAG_IFD_SLOTS_NUMBER:
    if (*Length>=1 && Value) {
      *Length=1;
      *Value=1;
      rc=IFD_SUCCESS;
    }
    else
      rc=IFD_ERROR_TAG;
    break;

  case TAG_IFD_SLOT_THREAD_SAFE:
    if (*Length>=1 && Value){
      *Length=1;
      *Value=0; /* Can NOT talk to multiple slots at the same time */
      rc=IFD_SUCCESS;
    }
    else
      rc=IFD_ERROR_TAG;
    break;

  case SCARD_ATTR_VENDOR_IFD_VERSION:
    /* Vendor-supplied interface device version (DWORD in the form
     * 0xMMmmbbbb where MM = major version, mm = minor version, and
     * bbbb = build number). */
    if (*Length>=1 && Value){
      DWORD v;

      v=(CYBERJACK_VERSION_MAJOR<<24) |
        (CYBERJACK_VERSION_MINOR<<16) |
        (CYBERJACK_VERSION_BUILD & 0xffff);
      *Length=sizeof(DWORD);
      *(DWORD*)Value=v;
      rc=IFD_SUCCESS;
    }
    else
      rc=IFD_ERROR_TAG;
    break;

  case SCARD_ATTR_VENDOR_NAME:
#define VENDOR_NAME "Reiner SCT"
    if (*Length>=sizeof(VENDOR_NAME) && Value){
      *Length=sizeof(VENDOR_NAME);
      memcpy(Value, VENDOR_NAME, sizeof(VENDOR_NAME));
      rc=IFD_SUCCESS;
    }
    else
      rc=IFD_ERROR_TAG;
#undef VENDOR_NAME
    break;

  default:
    rc=IFD_ERROR_TAG;
  } /* switch */


  /* done */
  ctx->unlock();
  return rc;
}



RESPONSECODE IFDHandler::setCapabilities(DWORD Lun, DWORD Tag, DWORD Length, PUCHAR Value) {
  unsigned short ctn, slot;

  ctn=((unsigned short) (Lun >> 16)) & 0xffff;
  slot=((unsigned short) (Lun & 0x0000FFFF)) % IFDH_MAX_SLOTS;
  if (ctn>=IFDH_MAX_READERS || slot>=IFDH_MAX_SLOTS) {
    DEBUGLUN(Lun, DEBUG_MASK_IFD, "Invalid LUN %X\n", (int)Lun);
    return IFD_COMMUNICATION_ERROR;
  }

  return IFD_NOT_SUPPORTED;
}



RESPONSECODE IFDHandler::setProtocolParameters (DWORD Lun, DWORD Protocol, UCHAR Flags, UCHAR PTS1, UCHAR PTS2, UCHAR PTS3) {
  unsigned short ctn, slot;
  ContextMap::iterator it;
  Context *ctx;
  CReader *r;
  RSCT_IFD_RESULT rj;
  uint32_t l_proto;
  RESPONSECODE rc;

  ctn=((unsigned short) (Lun >> 16)) & 0xffff;
  slot=((unsigned short) (Lun & 0x0000FFFF)) % IFDH_MAX_SLOTS;
  if (ctn>=IFDH_MAX_READERS || slot>=IFDH_MAX_SLOTS) {
    DEBUGLUN(Lun, DEBUG_MASK_IFD, "Invalid LUN %X\n", (int)Lun);
    return IFD_COMMUNICATION_ERROR;
  }

  /* get context and lock it */
  MUTEX_LOCK(m_contextMutex);
  it=m_contextMap.find(ctn);
  if (it==m_contextMap.end()) {
    DEBUGLUN(Lun, DEBUG_MASK_IFD, "LUN %X is not in use\n", (int) Lun);
    MUTEX_UNLOCK(m_contextMutex);
    return IFD_COMMUNICATION_ERROR;
  }
  ctx=it->second;
  r=ctx->getReader();

  ctx->lock();
  MUTEX_UNLOCK(m_contextMutex);

  /* action */
  l_proto=Protocol;
  rj=r->IfdSetProtocol(&l_proto);
  switch (rj) {
  case STATUS_SUCCESS:
    DEBUGLUN(Lun, DEBUG_MASK_IFD, "Success (active protocol: %d)\n", (int)l_proto);
    rc=IFD_SUCCESS;
    break;

  case STATUS_NO_MEDIA:
    DEBUGLUN(Lun, DEBUG_MASK_IFD, "No media\n");
    rc=IFD_ERROR_POWER_ACTION;
    break;

  case STATUS_UNRECOGNIZED_MEDIA:
    DEBUGLUN(Lun, DEBUG_MASK_IFD, "Unrecognized media\n");
    rc=IFD_ERROR_POWER_ACTION;
    break;

  case STATUS_CANCELLED:
    DEBUGLUN(Lun, DEBUG_MASK_IFD, "Cancelled\n");
    rc=IFD_ERROR_POWER_ACTION;
    break;

  case STATUS_IO_TIMEOUT:
    DEBUGLUN(Lun, DEBUG_MASK_IFD, "Timeout\n");
    rc=IFD_RESPONSE_TIMEOUT;
    break;

  case STATUS_NOT_SUPPORTED:
    rc=IFD_NOT_SUPPORTED;
    break;

  default:
    DEBUGLUN(Lun, DEBUG_MASK_IFD, "Error (%d)\n", (int)rj);
    rc=IFD_COMMUNICATION_ERROR;
    break;
  };


  /* unlock context */
  ctx->unlock();

  return rc;
}



RESPONSECODE IFDHandler::powerICC(DWORD Lun, DWORD Action, PUCHAR Atr, PDWORD AtrLength) {
  unsigned short ctn, slot;
  ContextMap::iterator it;
  Context *ctx;
  CReader *r;
  RESPONSECODE rc;
  uint32_t l_atrLength;
  uint32_t mode;
  CJ_RESULT rj;

  ctn=((unsigned short) (Lun >> 16)) & 0xffff;
  slot=((unsigned short) (Lun & 0x0000FFFF)) % IFDH_MAX_SLOTS;
  if (ctn>=IFDH_MAX_READERS || slot>=IFDH_MAX_SLOTS) {
    DEBUGLUN(Lun, DEBUG_MASK_IFD, "Invalid LUN %X\n", (int)Lun);
    return IFD_COMMUNICATION_ERROR;
  }

  /* get context and lock it */
  MUTEX_LOCK(m_contextMutex);
  it=m_contextMap.find(ctn);
  if (it==m_contextMap.end()) {
    DEBUGLUN(Lun, DEBUG_MASK_IFD, "LUN %X is not in use\n", (int) Lun);
    MUTEX_UNLOCK(m_contextMutex);
    return IFD_COMMUNICATION_ERROR;
  }
  ctx=it->second;

  ctx->lock();
  MUTEX_UNLOCK(m_contextMutex);
  r=ctx->getReader();

  /* action */
  switch(Action) {
  case IFD_POWER_UP:
    mode=SCARD_COLD_RESET;
    break;

  case IFD_POWER_DOWN:
    mode=SCARD_POWER_DOWN;
    break;

  case IFD_RESET:
    mode=SCARD_COLD_RESET;
    break;

  default:
    DEBUGLUN(Lun, DEBUG_MASK_IFD, "Action %d not supported\n", (int)Action);
    return IFD_NOT_SUPPORTED;
  }

  if (AtrLength)
    l_atrLength=*AtrLength;
  else
    l_atrLength=0;
  rj=r->IfdPower(mode, Atr, &l_atrLength);
  switch (rj) {
  case STATUS_SUCCESS:
    DEBUGLUN(Lun, DEBUG_MASK_IFD, "Success (ATR: %d bytes)\n", (int)l_atrLength);
    rc=IFD_SUCCESS;
    if (AtrLength)
      *AtrLength=l_atrLength;
    break;

  case STATUS_NO_MEDIA:
    DEBUGLUN(Lun, DEBUG_MASK_IFD, "No media\n");
    rc=IFD_ERROR_POWER_ACTION;
    break;

  case STATUS_UNRECOGNIZED_MEDIA:
    DEBUGLUN(Lun, DEBUG_MASK_IFD, "Unrecognized media\n");
    rc=IFD_ERROR_POWER_ACTION;
    break;

  case STATUS_CANCELLED:
    DEBUGLUN(Lun, DEBUG_MASK_IFD, "Cancelled\n");
    rc=IFD_ERROR_POWER_ACTION;
    break;

  case STATUS_IO_TIMEOUT:
    DEBUGLUN(Lun, DEBUG_MASK_IFD, "Timeout\n");
    rc=IFD_RESPONSE_TIMEOUT;
    break;

  default:
    DEBUGLUN(Lun, DEBUG_MASK_IFD, "Error (%d)\n", (int)rj);
    rc=IFD_COMMUNICATION_ERROR;
    break;
  };

  /* unlock context */
  ctx->unlock();

  return rc;
}



RESPONSECODE IFDHandler::transmitToICC(DWORD Lun, SCARD_IO_HEADER SendPci,
                                       PUCHAR TxBuffer, DWORD TxLength,
                                       PUCHAR RxBuffer, PDWORD RxLength, PSCARD_IO_HEADER RecvPci) {
  unsigned short ctn, slot;
  ContextMap::iterator it;
  Context *ctx;
  CReader *r;
  char rj;
  RESPONSECODE rc;
  uint16_t l_rxlength;
  uint8_t sad;
  uint8_t dad;

  ctn=((unsigned short) (Lun >> 16)) & 0xffff;
  slot=((unsigned short) (Lun & 0x0000FFFF)) % IFDH_MAX_SLOTS;
  if (ctn>=IFDH_MAX_READERS || slot>=IFDH_MAX_SLOTS) {
    DEBUGLUN(Lun, DEBUG_MASK_IFD, "Invalid LUN %X\n", (int)Lun);
    return IFD_COMMUNICATION_ERROR;
  }

  /* get context and lock it */
  MUTEX_LOCK(m_contextMutex);
  it=m_contextMap.find(ctn);
  if (it==m_contextMap.end()) {
    DEBUGLUN(Lun, DEBUG_MASK_IFD, "LUN %X is not in use\n", (int) Lun);
    MUTEX_UNLOCK(m_contextMutex);
    return IFD_COMMUNICATION_ERROR;
  }
  ctx=it->second;
  r=ctx->getReader();

  ctx->lock();
  MUTEX_UNLOCK(m_contextMutex);

  /* action */
  if (RxLength) {
    if (*RxLength>65535)
      *RxLength = 65535;
    l_rxlength=*RxLength;
  }
  else
    l_rxlength=0;
  dad=0; /* slot 0 */
  sad=CT_API_AD_HOST;
  rj=r->CtData(&dad, &sad, TxLength, TxBuffer, &l_rxlength, RxBuffer);
  switch (rj) {
  case CT_API_RV_OK:
    DEBUGLUN(Lun, DEBUG_MASK_IFD, "Success (response length: %d)\n", (int)l_rxlength);
    if (RxLength)
      *RxLength=l_rxlength;
    rc=IFD_SUCCESS;
    break;

  case CT_API_RV_ERR_INVALID:
    DEBUGLUN(Lun, DEBUG_MASK_IFD, "Invalid parameter\n");
    rc=IFD_COMMUNICATION_ERROR;
    break;

  case CT_API_RV_ERR_CT:
    DEBUGLUN(Lun, DEBUG_MASK_IFD, "Terminal error\n");
    rc=IFD_COMMUNICATION_ERROR;
    break;

  case CT_API_RV_ERR_TRANS:
    DEBUGLUN(Lun, DEBUG_MASK_IFD, "Transport error\n");
    rc=IFD_COMMUNICATION_ERROR;
    break;

  case CT_API_RV_ERR_MEMORY:
    DEBUGLUN(Lun, DEBUG_MASK_IFD, "Memory error\n");
    rc=IFD_COMMUNICATION_ERROR;
    break;

  case CT_API_RV_ERR_HOST:
    DEBUGLUN(Lun, DEBUG_MASK_IFD, "Host error\n");
    rc=IFD_COMMUNICATION_ERROR;
    break;

  case CT_API_RV_ERR_HTSI:
    DEBUGLUN(Lun, DEBUG_MASK_IFD, "HTSI error\n");
    rc=IFD_COMMUNICATION_ERROR;
    break;

  default:
    DEBUGLUN(Lun, DEBUG_MASK_IFD, "Error (%d)\n", (int) rj);
    rc=IFD_COMMUNICATION_ERROR;
    break;
  }

  /* unlock context */
  ctx->unlock();
  return rc;
}



/* ============================================================================================================= Begin Part 10 Stuff */
RESPONSECODE IFDHandler::p10GetFeatures(Context *ctx,
                                        DWORD Lun,
                                        PUCHAR RxBuffer,
                                        DWORD RxLength,
                                        PDWORD RxReturned) {
  PCSC_TLV_STRUCTURE *pcsc_tlv = (PCSC_TLV_STRUCTURE *)RxBuffer;
  unsigned int len = 0;
  CReader *r;
  int rv;
  cj_ReaderInfo ri;
  
  r=ctx->getReader();
  memset(&ri, 0, sizeof(cj_ReaderInfo));
  ri.SizeOfStruct=sizeof(cj_ReaderInfo);
  rv=r->CtGetReaderInfo(&ri);
  if (rv!=CJ_SUCCESS) {
    DEBUGDEV("DRIVER", DEBUG_MASK_IFD, "Unable to get reader info (%d)\n", rv);
    return CT_API_RV_ERR_CT;
  }


  DEBUGLUN(Lun, DEBUG_MASK_IFD, "GetFeatures called\n");
  /* WATCHOUT: When supporting a new TLV the size must be adjusted here */
  if (RxLength<4*sizeof(PCSC_TLV_STRUCTURE)) {
    DEBUGLUN(Lun, DEBUG_MASK_IFD, "Buffer too small\n");
    return IFD_COMMUNICATION_ERROR;
  }

  DEBUGLUN(Lun, DEBUG_MASK_IFD, "  Reporting Feature FEATURE_VERIFY_PIN_DIRECT (%08x)", CJPCSC_VEN_IOCTRL_VERIFY_PIN_DIRECT);
  pcsc_tlv->tag = FEATURE_VERIFY_PIN_DIRECT;
  pcsc_tlv->length = 0x04; /* always 0x04 */
  //pcsc_tlv->value = htonl(IOCTL_FEATURE_VERIFY_PIN_DIRECT);
  pcsc_tlv->value = htonl(CJPCSC_VEN_IOCTRL_VERIFY_PIN_DIRECT);
  pcsc_tlv++;
  len+=sizeof(PCSC_TLV_STRUCTURE);

  DEBUGLUN(Lun, DEBUG_MASK_IFD, "  Reporting Feature FEATURE_MODIFY_PIN_DIRECT (%08x)", CJPCSC_VEN_IOCTRL_MODIFY_PIN_DIRECT);
  pcsc_tlv->tag=FEATURE_MODIFY_PIN_DIRECT;
  pcsc_tlv->length=0x04; /* always 0x04 */
  //pcsc_tlv->value=htonl(IOCTL_FEATURE_MODIFY_PIN_DIRECT);
  pcsc_tlv->value=htonl(CJPCSC_VEN_IOCTRL_MODIFY_PIN_DIRECT);
  pcsc_tlv++;
  len+=sizeof(PCSC_TLV_STRUCTURE);

#if defined FEATURE_MCT_READER_DIRECT
  DEBUGLUN(Lun, DEBUG_MASK_IFD, "  Reporting Feature FEATURE_MCT_READER_DIRECT (%08x)", CJPCSC_VEN_IOCTRL_MCT_READERDIRECT);
  pcsc_tlv->tag = FEATURE_MCT_READER_DIRECT;
#else
  DEBUGLUN(Lun, DEBUG_MASK_IFD, "  Reporting Feature FEATURE_MCT_READERDIRECT (%08x)", CJPCSC_VEN_IOCTRL_MCT_READERDIRECT);
  pcsc_tlv->tag = FEATURE_MCT_READERDIRECT;
#endif
  pcsc_tlv->length = 0x04; /* always 0x04 */
  pcsc_tlv->value = htonl(CJPCSC_VEN_IOCTRL_MCT_READERDIRECT);
  pcsc_tlv++;
  len+=sizeof(PCSC_TLV_STRUCTURE);

  DEBUGLUN(Lun, DEBUG_MASK_IFD, "  Reporting Feature FEATURE_MCT_UNIVERSAL (%08x)", CJPCSC_VEN_IOCTRL_MCT_READERUNIVERSAL);
  pcsc_tlv->tag = FEATURE_MCT_UNIVERSAL;
  pcsc_tlv->length = 0x04; /* always 0x04 */
  pcsc_tlv->value = htonl(CJPCSC_VEN_IOCTRL_MCT_READERUNIVERSAL);
  pcsc_tlv++;
  len+=sizeof(PCSC_TLV_STRUCTURE);

  if (ri.HardwareMask & RSCT_READER_HARDWARE_MASK_PACE) {
    DEBUGLUN(Lun, DEBUG_MASK_IFD, "  Reporting Feature FEATURE_EXECUTE_PACE (%08x)", CJPCSC_VEN_IOCTRL_EXECUTE_PACE);
    pcsc_tlv->tag = FEATURE_EXECUTE_PACE;
    pcsc_tlv->length = 0x04; /* always 0x04 */
    pcsc_tlv->value = htonl(CJPCSC_VEN_IOCTRL_EXECUTE_PACE);
    pcsc_tlv++;
    len+=sizeof(PCSC_TLV_STRUCTURE);
  }

  *RxReturned=len;

  return IFD_SUCCESS;
}




RESPONSECODE IFDHandler::p10MctUniversal(Context *ctx,
                                         MCTUniversal_t *uni,
                                         PUCHAR RxBuffer,
                                         DWORD RxLength,
                                         PDWORD RxReturned) {
  DWORD Lun;
  CReader *r;
  char rj;
  RESPONSECODE rc;
  uint8_t sad;
  uint8_t dad;
  uint16_t lenc;
  uint16_t lenr;
  MCTUniversal_t *response;

  Lun=ctx->getLun();
  r=ctx->getReader();

  /* action */
  response=(MCTUniversal_t*)RxBuffer;
  /* need at least space for MCTUniversal_t with at least 2 bytes response */
  if (RxLength<(sizeof(MCTUniversal_t)+1)) {
    DEBUGLUN(Lun, DEBUG_MASK_IFD, "Buffer too small\n");
    return IFD_COMMUNICATION_ERROR;
  }

  dad=uni->DAD;
  sad=uni->SAD;
  lenc=uni->BufferLength; /* TODO: is this really little endian? */
  if (RxLength>65535)
    lenr=65535-(sizeof(MCTUniversal_t)-1);
  else
    lenr=RxLength-(sizeof(MCTUniversal_t)-1);

#ifdef PART10_DISABLE_APDU
  DEBUGPLUN(Lun, DEBUG_MASK_IFD, "Apdu's disabled");
  rj=-127;
#else
  if (uni->BufferLength>3 && uni->buffer==CJ_SPECIAL_CLA)
    /* special APDU, go handle it */
    rj=_special(ctx, lenc, &(uni->buffer), &lenr, &(response->buffer));
  else
    /* standard APDU */
    rj=r->CtData(&dad, &sad, lenc, &(uni->buffer), &lenr, &(response->buffer));
#endif
  if (rj==0) {
    response->BufferLength=lenr;
    *RxReturned=(sizeof(MCTUniversal_t)-1)+lenr;
    response->SAD=sad;
    response->DAD=dad;
    rc=IFD_SUCCESS;
  }
  else {
    *RxReturned=0;
    rc=IFD_COMMUNICATION_ERROR;
  }

  /* done */
  return rc;
}

/* ============================================================================================================= End Part 10 Stuff */



RESPONSECODE IFDHandler::control(DWORD Lun,
                                 DWORD controlCode,
                                 PUCHAR TxBuffer,
                                 DWORD TxLength,
                                 PUCHAR RxBuffer,
                                 DWORD RxLength,
                                 PDWORD RxReturned) {
  unsigned short ctn, slot;
  ContextMap::iterator it;
  Context *ctx;
  CReader *r;
  RSCT_IFD_RESULT rj;
  RESPONSECODE rc;
  uint32_t l_rxlength;

  ctn=((unsigned short) (Lun >> 16)) & 0xffff;
  slot=((unsigned short) (Lun & 0x0000FFFF)) % IFDH_MAX_SLOTS;
  if (ctn>=IFDH_MAX_READERS || slot>=IFDH_MAX_SLOTS) {
    DEBUGLUN(Lun, DEBUG_MASK_IFD, "Invalid LUN %X\n", (int)Lun);
    return IFD_COMMUNICATION_ERROR;
  }

  /* get context and lock it */
  MUTEX_LOCK(m_contextMutex);
  it=m_contextMap.find(ctn);
  if (it==m_contextMap.end()) {
    DEBUGLUN(Lun, DEBUG_MASK_IFD, "LUN %X is not in use\n", (int) Lun);
    MUTEX_UNLOCK(m_contextMutex);
    return IFD_COMMUNICATION_ERROR;
  }
  ctx=it->second;
  r=ctx->getReader();

  ctx->lock();
  MUTEX_UNLOCK(m_contextMutex);

  /* action */
  switch(controlCode) {
  case CM_IOCTL_GET_FEATURE_REQUEST:
    DEBUGLUN(Lun, DEBUG_MASK_IFD, "CM_IOCTL_GET_FEATURE_REQUEST\n");
    rc=p10GetFeatures(ctx, Lun, RxBuffer, RxLength, RxReturned);
    break;

  case WINDOWS_CTL_GET_FEATURE:
    DEBUGLUN(Lun, DEBUG_MASK_IFD, "WINDOWS_CTL_GET_FEATURE\n");
    rc=p10GetFeatures(ctx, Lun, RxBuffer, RxLength, RxReturned);
    break;

  case WINDOWS_CTL_GET_FEATURE2:
    DEBUGLUN(Lun, DEBUG_MASK_IFD, "WINDOWS_CTL_GET_FEATURE2\n");
    rc=p10GetFeatures(ctx, Lun, RxBuffer, RxLength, RxReturned);
    break;

  case CJPCSC_VEN_IOCTRL_MCT_READERUNIVERSAL:
    DEBUGLUN(Lun, DEBUG_MASK_IFD, "CJPCSC_VEN_IOCTRL_MCT_READERUNIVERSAL\n");
    /* directly send the command with SAD/DAD in TxBuffer */
    if (TxLength<(sizeof(MCTUniversal_t)-1)) {
      DEBUGLUN(Lun, DEBUG_MASK_IFD, "Too few bytes in TxBuffer (%d bytes)\n", (int)TxLength);
      rc=IFD_COMMUNICATION_ERROR;
    }
    else {
      MCTUniversal_t *uni=(MCTUniversal_t*) TxBuffer;

      /* check overall length including data length */
      if (TxLength<(sizeof(MCTUniversal_t)-1+uni->BufferLength)) {
        DEBUGLUN(Lun, DEBUG_MASK_IFD, "Too few bytes in TxBuffer (%d bytes, %d bytes data)\n",
                 (int)TxLength, uni->BufferLength);
        ctx->unlock();
        return IFD_COMMUNICATION_ERROR;
      }

      rc=p10MctUniversal(ctx, uni, RxBuffer, RxLength, RxReturned);
    }
    break;

#if 0 /* forward this to base driver */
  case CJPCSC_VEN_IOCTRL_MCT_READERDIRECT:
    DEBUGLUN(Lun, DEBUG_MASK_IFD, "CJPCSC_VEN_IOCTRL_MCT_READERDIRECT\n");
    /* directly send the command with DAD=01 and SAD=02 */
    // TODO
    rc=IFD_ERROR_NOT_SUPPORTED;
    break;
#endif

  default:
    DEBUGLUN(Lun, DEBUG_MASK_IFD, "Forwarding control call with fn %X to CJECA32\n", (int) controlCode);
    l_rxlength=RxLength;
    /* let base driver handle other codes */
    rj=r->IfdIoControl(controlCode, TxBuffer, TxLength, RxBuffer, &l_rxlength);
    switch (rj) {
    case STATUS_SUCCESS:
      DEBUGLUN(Lun, DEBUG_MASK_IFD, "Success (returned bytes: %d)\n", (int)l_rxlength);
      if (RxReturned)
        *RxReturned=l_rxlength;
      rc=IFD_SUCCESS;
      break;
  
    case STATUS_NO_MEDIA:
      DEBUGLUN(Lun, DEBUG_MASK_IFD, "No media\n");
      rc=IFD_ERROR_POWER_ACTION;
      break;
  
    case STATUS_UNRECOGNIZED_MEDIA:
      DEBUGLUN(Lun, DEBUG_MASK_IFD, "Unrecognized media\n");
      rc=IFD_ERROR_POWER_ACTION;
      break;
  
    case STATUS_CANCELLED:
      DEBUGLUN(Lun, DEBUG_MASK_IFD, "Cancelled\n");
      rc=IFD_ERROR_POWER_ACTION;
      break;
  
    case STATUS_IO_TIMEOUT:
      DEBUGLUN(Lun, DEBUG_MASK_IFD, "Timeout\n");
      rc=IFD_RESPONSE_TIMEOUT;
      break;
  
    case STATUS_NOT_SUPPORTED:
      rc=IFD_NOT_SUPPORTED;
      break;
  
    default:
      DEBUGLUN(Lun, DEBUG_MASK_IFD, "Error (%d)\n", (int)rj);
      rc=IFD_COMMUNICATION_ERROR;
      break;
    };
  }

  /* unlock context */
  ctx->unlock();
  return rc;
}



RESPONSECODE IFDHandler::iccPresence(DWORD Lun) {
  unsigned short ctn, slot;
  ContextMap::iterator it;
  Context *ctx;
  CReader *r;
  uint32_t state;
  RESPONSECODE rc;

  ctn=((unsigned short) (Lun >> 16)) & 0xffff;
  slot=((unsigned short) (Lun & 0x0000FFFF)) % IFDH_MAX_SLOTS;
  if (ctn>=IFDH_MAX_READERS || slot>=IFDH_MAX_SLOTS) {
    DEBUGLUN(Lun, DEBUG_MASK_IFD, "Invalid LUN %X\n", (int)Lun);
    return IFD_COMMUNICATION_ERROR;
  }

  /* get context and lock it */
  MUTEX_LOCK(m_contextMutex);
  it=m_contextMap.find(ctn);
  if (it==m_contextMap.end()) {
    DEBUGLUN(Lun, DEBUG_MASK_IFD, "LUN %X is not in use\n", (int) Lun);
    MUTEX_UNLOCK(m_contextMutex);
    return IFD_COMMUNICATION_ERROR;
  }
  ctx=it->second;

  ctx->lock();
  MUTEX_UNLOCK(m_contextMutex);
  r=ctx->getReader();

  /* action */
  if (r->IfdGetState(&state)==STATUS_DEVICE_NOT_CONNECTED) {
    DEBUGLUN(Lun, DEBUG_MASK_IFD, "Device is not connected\n");
    ctx->unlock();
    return IFD_COMMUNICATION_ERROR;
  }

  DEBUGLUN(Lun, DEBUG_MASK_IFD, "Status %u\n", (unsigned int) state);

  switch(state) {
  case SCARD_SPECIFIC:
  case SCARD_NEGOTIABLE:
    DEBUGLUN(Lun, DEBUG_MASK_IFD, "Card connected\n");
    rc=IFD_ICC_PRESENT;
    break;

  case SCARD_SWALLOWED:
    DEBUGLUN(Lun, DEBUG_MASK_IFD, "Card inserted\n");
    rc=IFD_ICC_PRESENT;
    break;

  case SCARD_ABSENT:
    DEBUGLUN(Lun, DEBUG_MASK_IFD, "Card absent\n");
    rc=IFD_ICC_NOT_PRESENT;
    break;

  default:
    DEBUGLUN(Lun, DEBUG_MASK_IFD, "Unexpected status %u\n", (unsigned int) state);
    ctx->unlock();
    return IFD_COMMUNICATION_ERROR;
  }


  /* unlock context */
  ctx->unlock();

  return rc;
}







IFDHandler::Context::Context(DWORD lun, CReader *r)
:m_lun(lun)
,m_reader(r)
,m_atr_length(0) {
#ifdef HAVE_PTHREAD_H
  pthread_mutex_init(&m_mutex, NULL);
#endif
  memset(&m_icc_state, 0, sizeof(m_icc_state));
}



IFDHandler::Context::~Context() {
  delete m_reader;

#ifdef HAVE_PTHREAD_H
  pthread_mutex_destroy(&m_mutex);
#endif
}




void IFDHandler::Context::lock() {
  MUTEX_LOCK(m_mutex);
}



void IFDHandler::Context::unlock() {
  MUTEX_UNLOCK(m_mutex);
}








extern "C" {
  CJECA32_EXPORT RESPONSECODE IFDHCreateChannel(DWORD Lun, DWORD Channel) {
    RESPONSECODE rc;

    if (rsct_ifd_driver_initialized==0) {
      int rv;

      rv=rsct_ifd_handler.init();
      if (rv<0) {
        fprintf(stderr, "CYBERJACK: Unable to init IFD handler.\n");
        return IFD_COMMUNICATION_ERROR;
      }
      rsct_ifd_driver_initialized++;
    }

    DEBUGLUN(Lun, DEBUG_MASK_IFD, "IFDHCreateChannel(%X, %d)\n", (int)Lun, (int)Channel);
    rc=rsct_ifd_handler.createChannel(Lun, Channel);
    return rc;
  }



  CJECA32_EXPORT RESPONSECODE IFDHCreateChannelByName(DWORD Lun, char *devName){
    RESPONSECODE rc;

    if (rsct_ifd_driver_initialized==0) {
      int rv;

      rv=rsct_ifd_handler.init();
      if (rv<0) {
        fprintf(stderr, "CYBERJACK: Unable to init IFD handler.\n");
        return IFD_COMMUNICATION_ERROR;
      }
      rsct_ifd_driver_initialized++;
    }

    DEBUGLUN(Lun, DEBUG_MASK_IFD, "IFDHCreateChannelByName(%X, %s)\n", (int)Lun, devName);
    rc=rsct_ifd_handler.createChannelByName(Lun, devName);
    return rc;
  }



  CJECA32_EXPORT RESPONSECODE IFDHCloseChannel(DWORD Lun){
    RESPONSECODE rc;

    DEBUGLUN(Lun, DEBUG_MASK_IFD, "IFDHCloseChannel(%X)\n", (int)Lun);
    rc=rsct_ifd_handler.closeChannel(Lun);
    return rc;
  }



  CJECA32_EXPORT RESPONSECODE IFDHGetCapabilities(DWORD Lun, DWORD Tag, PDWORD Length,
                                                  PUCHAR Value){
    RESPONSECODE rc;

    DEBUGLUN(Lun, DEBUG_MASK_IFD, "IFDHGetCapabilities(%X, %X, %p, %p)\n", (int)Lun, (int)Tag, Length, Value);
    rc=rsct_ifd_handler.getCapabilities(Lun, Tag, Length, Value);
    return rc;
  }



  CJECA32_EXPORT RESPONSECODE IFDHSetCapabilities (DWORD Lun, DWORD Tag, DWORD Length, PUCHAR Value){
    RESPONSECODE rc;

    DEBUGLUN(Lun, DEBUG_MASK_IFD, "IFDHSetCapabilities(%X, %X, %d, %p)\n", (int)Lun, (int)Tag, (int)Length, Value);
    rc=rsct_ifd_handler.setCapabilities(Lun, Tag, Length, Value);
    return rc;
  }



  CJECA32_EXPORT RESPONSECODE IFDHSetProtocolParameters(DWORD Lun, DWORD Protocol,
                                                        UCHAR Flags, UCHAR PTS1, UCHAR PTS2, UCHAR PTS3){
    RESPONSECODE rc;

    DEBUGLUN(Lun, DEBUG_MASK_IFD, "IFDHSetProtocolParameters(%X, %X, %02X, %02X, %02X, %02X)\n",
             (int)Lun, (int)Protocol, (int)Flags, (int)PTS1, (int)PTS2, (int)PTS3);
    rc=rsct_ifd_handler.setProtocolParameters(Lun, Protocol, Flags, PTS1, PTS2, PTS3);
    return rc;
  }



  CJECA32_EXPORT RESPONSECODE IFDHPowerICC(DWORD Lun, DWORD Action, PUCHAR Atr, PDWORD AtrLength){
    RESPONSECODE rc;

    DEBUGLUN(Lun, DEBUG_MASK_IFD, "IFDHPowerICC(%X, %X, %p, %p)\n", (int)Lun, (int)Action, Atr, AtrLength);
    rc=rsct_ifd_handler.powerICC(Lun, Action, Atr, AtrLength);
    return rc;
  }



  CJECA32_EXPORT RESPONSECODE IFDHTransmitToICC(DWORD Lun, SCARD_IO_HEADER SendPci,
                                                PUCHAR TxBuffer, DWORD TxLength,
                                                PUCHAR RxBuffer, PDWORD RxLength, PSCARD_IO_HEADER RecvPci){
    RESPONSECODE rc;

    DEBUGLUN(Lun, DEBUG_MASK_IFD, "IFDHTransmitToICC(%X)\n", (int)Lun);
    rc=rsct_ifd_handler.transmitToICC(Lun, SendPci, TxBuffer, TxLength, RxBuffer, RxLength, RecvPci);
    return rc;
  }



  CJECA32_EXPORT RESPONSECODE IFDHControl(DWORD Lun,
                                          DWORD controlCode,
                                          PUCHAR TxBuffer,
                                          DWORD TxLength,
                                          PUCHAR RxBuffer,
                                          DWORD RxLength,
                                          PDWORD RxReturned){
    RESPONSECODE rc;

    DEBUGLUN(Lun, DEBUG_MASK_IFD, "IFDHControl(%X, %X)\n", (int)Lun, (int)controlCode);
    rc=rsct_ifd_handler.control(Lun, controlCode, TxBuffer, TxLength, RxBuffer, RxLength, RxReturned);
    return rc;
  }



  CJECA32_EXPORT RESPONSECODE IFDHICCPresence(DWORD Lun){
    RESPONSECODE rc;

    DEBUGLUN(Lun, DEBUG_MASK_IFD, "IFDHICCPresence(%X)\n", (int)Lun);
    rc=rsct_ifd_handler.iccPresence(Lun);
    return rc;
  }






} /* extern C */




#include "ifd_special.cpp"



