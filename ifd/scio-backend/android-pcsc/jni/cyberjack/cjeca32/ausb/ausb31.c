/* USB support for the Cyberjack family of readers.
 *
 * Previous version were (C) 2004-2005 by Harald Welte <laforge@gnumonks.org>
 * This version is a rewrite (asynchronous USB is no longer needed).
 *
 * (C) 2007 Martin Preuss <martin@libchipcard.de>
 *
 * Distributed and licensed under the terms of GNU LGPL, Version 2.1
 */

/*
 * This implementation expects the reader to not send any interrupt URB
 * whatsoever (as is the case  with newer firmware whose configuration 2
 * does not have an interrupt pipe).
 *
 * This implementation otherwise only uses plain libusb calls so it should
 * work on any system for which libusb is available.
 */


#ifdef HAVE_CONFIG_H
# include <config.h>
#endif


#ifdef USE_USB1


#include <inttypes.h>
#include <string.h>
#include <stdlib.h>
#include <unistd.h>
#include <stdio.h>
#include <errno.h>
#include <time.h>
#include <libusb.h>

#include "ausb_l.h"
#include "ausb_libusb1_l.h"


#define DEBUGP(ah, format, ...) {\
  char dbg_buffer[256]; \
  \
  snprintf(dbg_buffer, sizeof(dbg_buffer)-1,\
  __FILE__":%5d: " format  , __LINE__ , ##__VA_ARGS__); \
  dbg_buffer[sizeof(dbg_buffer)-1]=0; \
  ausb_log(ah, dbg_buffer, NULL, 0);\
}


#define DEBUGL(ah, text, pData, ulDataLen) {\
  char dbg_buffer[256]; \
  \
  snprintf(dbg_buffer, sizeof(dbg_buffer)-1,\
  __FILE__":%5d: %s", __LINE__ , text); \
  dbg_buffer[sizeof(dbg_buffer)-1]=0; \
  ausb_log(ah, dbg_buffer, pData, ulDataLen);\
}



struct ausb31_extra {
  libusb_device_handle *uh;
};
typedef struct ausb31_extra ausb31_extra;






static int ausb31_start_interrupt(ausb_dev_handle *ah, int ep) {
  ausb31_extra *xh;

  xh=(ausb31_extra*)ah->extraData;
  if (xh) {
    int rv;

    DEBUGP(ah, "Halting interrupt pipe.");
    rv=libusb_control_transfer(xh->uh,
			       0x02, /* host to device */
			       0x03, /* set feature */
			       0x00, /* halt */
			       ep,   /* endpoint */
			       NULL, 0,
			       1200);
    if (rv<0) {
      DEBUGP(ah, "unable to halt interrupt pipe (%d=%s)\n",
	     errno, strerror(errno));
      return -1;
    }
    return 0;
  }
  else
    return -1;
}



static int ausb31_stop_interrupt(ausb_dev_handle *ah) {
  return 0;
}



static int ausb31_bulk_write(ausb_dev_handle *ah, int ep,
			    char *bytes, int length, int timeout){
  ausb31_extra *xh;

  DEBUGP(ah, "bulk write (ah=%p, ep=0x%x, bytes=%p, size=%d, timeout=%d\n",
	 ah, ep, bytes, length, timeout);

  xh=(ausb31_extra*)ah->extraData;
  if (xh) {
    int transferred=0;
    int rv;

    /* Ensure the endpoint address is correct */
    ep &= ~LIBUSB_ENDPOINT_IN;

    rv=libusb_bulk_transfer(xh->uh, ep, (unsigned char*) bytes, length, &transferred, 0);
    if (rv) {
      DEBUGP(ah, "Error on libusb_bulk_transfer: %d", rv);
      return -1;
    }

    if (transferred!=length) {
      DEBUGP(ah, "not all data transferred (only %d bytes of %d)", transferred, length);
      return -1;
    }
    return transferred;
  }
  else
    return -1;
}



static int ausb31_bulk_read(ausb_dev_handle *ah, int ep,
			   char *bytes, int size, int timeout){
  ausb31_extra *xh;

  DEBUGP(ah, "bulk read (ah=%p, ep=0x%x, bytes=%p, size=%d, timeout=%d\n",
         ah, ep, bytes, size, timeout);

  xh=(ausb31_extra*)ah->extraData;
  if (xh) {
    for (;;) {
      int transferred=0;
      int rv;

      /* Ensure the endpoint address is correct */
      ep |= LIBUSB_ENDPOINT_IN;

      rv=libusb_bulk_transfer(xh->uh, ep, (unsigned char*) bytes, size, &transferred, 0);
      if (rv) {
	DEBUGP(ah, "Error on libusb_bulk_transfer: %d", rv);
	return -1;
      }
      if (ah->pid!=0x100 &&
	  (bytes[0]==0x40 || /* RDR_TO_PC_KEYEVENT */
	   bytes[0]==0x50)) { /* RDR_TO_PC_NOTIFYSLOTCHANGE */
	DEBUGL(ah, "Interrupt transfer received via bulk-in", bytes, rv);
	if (ah->cb.handler) {
	  DEBUGP(ah, "Calling interrupt handler %p with %p",
		 ah->cb.handler, ah->cb.userdata);
	  ah->cb.handler((uint8_t*)bytes, transferred, ah->cb.userdata);
	}
	else {
	  DEBUGP(ah, "No interrupt handler");
	}
      }
      else {
	return transferred;
      }
    }
  }
  else
    return -1;
}



static int ausb31_claim_interface(ausb_dev_handle *ah, int interface){
  ausb31_extra *xh;

  xh=(ausb31_extra*)ah->extraData;
  if (xh)
    return libusb_claim_interface(xh->uh, interface);
  else
    return -1;
}



static int ausb31_release_interface(ausb_dev_handle *ah, int interface){
  ausb31_extra *xh;

  xh=(ausb31_extra*)ah->extraData;
  if (xh)
    return libusb_release_interface(xh->uh, interface);
  else
    return -1;
}



static int ausb31_set_configuration(ausb_dev_handle *ah, int configuration){
  ausb31_extra *xh;

  xh=(ausb31_extra*)ah->extraData;
  if (xh) {
    int ret;

    ret=libusb_set_configuration(xh->uh, configuration);
    if (ret<0) {
      DEBUGP(ah, "usb_set_configuration returned %d (errno=%d:%s)\n", ret,
	     errno, strerror(errno));
    }
    return ret;
  }
  else
    return -1;
}



static int ausb31_reset(ausb_dev_handle *ah){
  ausb31_extra *xh;

  xh=(ausb31_extra*)ah->extraData;
  if (xh) {
    int rv;

    rv=libusb_reset_device(xh->uh);
    if (rv==0)
      return rv;
    else if (rv==LIBUSB_ERROR_NOT_FOUND) {
      DEBUGP(ah, "Device is usb-wise disconnected, sleeping for 5 secs\n");
      sleep(5);
      return 0;
    }
    else {
      DEBUGP(ah, "libusb_reset_device: %d\n", rv);
      return -1;
    }
  }
  else
    return -1;
}



static int ausb31_reset_endpoint(ausb_dev_handle *ah, unsigned int ep){
  return 0;
}



static int ausb31_clear_halt(ausb_dev_handle *ah, unsigned int ep){
  ausb31_extra *xh;

  xh=(ausb31_extra*)ah->extraData;
  if (xh)
    return libusb_clear_halt(xh->uh, ep);
  else
    return -1;
}



static int ausb31_reset_pipe(ausb_dev_handle *ah, int ep){
  ausb31_extra *xh;

  xh=(ausb31_extra*)ah->extraData;
  if (xh) {
    int rv;

    rv=libusb_control_transfer(xh->uh,
			       0x02, /* host to device */
			       0x03, /* set feature */
			       0x00, /* halt */
			       ep,   /* endpoint */
			       NULL, 0,
			       1200);
    if (rv<0) {
      DEBUGP(ah, "unable to reset endpoint %d (%d=%s)",ep,
	     errno, strerror(errno));
      return rv;
    }

    rv=libusb_clear_halt(xh->uh, ep);
    if (rv<0) {
      DEBUGP(ah, "unable to start endpoint %d (%d=%s)",ep,
	     errno, strerror(errno));
      return rv;
    }
    return rv;
  }
  else
    return -1;
}



static void ausb31_close(struct ausb_dev_handle *ah){
  ausb31_extra *xh;

  xh=(ausb31_extra*)ah->extraData;
  if (xh) {
    libusb_close(xh->uh);
    free(xh);
  }
}



/* not static since this function is needed in ausb.c */
int ausb31_extend(ausb_dev_handle *ah){
  struct libusb_device *dev;
  ausb31_extra *xh;
  int rv;

  DEBUGP(ah, "Extending AUSB handle as type 3");

  xh=malloc(sizeof *xh);
  if (xh==0) {
    DEBUGP(ah, "memory full\n");
    return -1;
  }
  memset(xh, 0, sizeof(*xh));

  /* get libusb representation of the given device */
  dev=ausb_libusb1_get_usbdev(&(ah->device));
  if (dev==NULL) {
    DEBUGP(ah, "libusb device not found");
    free(xh);
    return -1;
  }

  /* open the device */
  rv=libusb_open(dev, &(xh->uh));
  if (rv || !xh->uh) {
    DEBUGP(ah, "libusb_open() failed: rv\n");
    free(xh);
    return -1;
  }

  /* done */
  ah->extraData=xh;
  ah->closeFn=ausb31_close;
  ah->startInterruptFn=ausb31_start_interrupt;
  ah->stopInterruptFn=ausb31_stop_interrupt;
  ah->bulkWriteFn=ausb31_bulk_write;
  ah->bulkReadFn=ausb31_bulk_read;

  ah->claimInterfaceFn=ausb31_claim_interface;
  ah->releaseInterfaceFn=ausb31_release_interface;
  ah->setConfigurationFn=ausb31_set_configuration;

  ah->resetFn=ausb31_reset;
  ah->resetEndpointFn=ausb31_reset_endpoint;
  ah->clearHaltFn=ausb31_clear_halt;
  ah->resetPipeFn=ausb31_reset_pipe;

  return 0;
}


#endif /* ifdef USE_USB1 */


