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


#ifndef USE_USB1


#include <inttypes.h>
#include <string.h>
#include <stdlib.h>
#include <unistd.h>
#include <stdio.h>
#include <errno.h>
#include <time.h>
#include <usb.h>

#include "ausb_l.h"
#include "ausb_libusb0_l.h"


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



struct ausb3_extra {
  usb_dev_handle *uh;
};
typedef struct ausb3_extra ausb3_extra;






static int ausb3_start_interrupt(ausb_dev_handle *ah, int ep) {
  ausb3_extra *xh;

  xh=(ausb3_extra*)ah->extraData;
  if (xh) {
    int rv;

    DEBUGP(ah, "Halting interrupt pipe.");
    rv=usb_control_msg(xh->uh,
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



static int ausb3_stop_interrupt(ausb_dev_handle *ah) {
  return 0;
}



static int ausb3_bulk_write(ausb_dev_handle *ah, int ep,
			    char *bytes, int length, int timeout){
  ausb3_extra *xh;

  xh=(ausb3_extra*)ah->extraData;
  if (xh) {
    int rv;
    time_t tStart;

    tStart=time(NULL);
    do {
      time_t tEnd;

      tEnd=time(NULL);
      if (difftime(tEnd, tStart)>20.0) {
	DEBUGP(ah, "Timeout while sending data to bulkout pipe");
	return -1;
      }
      rv=usb_bulk_write(xh->uh, ep, bytes, length, timeout);
    } while (rv < 0 && errno == EINTR);
    return rv;
  }
  else
    return -1;
}



static int ausb3_bulk_read(ausb_dev_handle *ah, int ep,
			   char *bytes, int size, int timeout){
  ausb3_extra *xh;

  xh=(ausb3_extra*)ah->extraData;
  if (xh) {
    int rv;
    time_t tStart;

    tStart=time(NULL);
    for (;;) {
      do {
	time_t tEnd;

	tEnd=time(NULL);
	if (difftime(tEnd, tStart)>AUSB_MAX_TIMEOUT) {
	  DEBUGP(ah, "Timeout while waiting for data on bulkin pipe");
	  return -1;
	}
	rv=usb_bulk_read(xh->uh, ep, bytes, size, timeout);
      } while (rv < 0 && errno == EINTR);

      if (rv>=1) {
	if (bytes[0]==0x40 || /* RDR_TO_PC_KEYEVENT */
	    bytes[0]==0x50) { /* RDR_TO_PC_NOTIFYSLOTCHANGE */
	  DEBUGL(ah, "Interrupt URB received", bytes, rv);
	  if (ah->cb.handler) {
	    DEBUGP(ah, "Calling interrupt handler %p with %p",
		   ah->cb.handler, ah->cb.userdata);
	    ah->cb.handler((uint8_t*)bytes, rv, ah->cb.userdata);
	    DEBUGP(ah, "Calling interrupt handler: done");
	  }
	  else {
	    DEBUGP(ah, "No interrupt handler");
	  }
	}
	else
	  break;
      }
      else
	break;
    }

    return rv;
  }
  else
    return -1;
}



static int ausb3_claim_interface(ausb_dev_handle *ah, int interface){
  ausb3_extra *xh;

  xh=(ausb3_extra*)ah->extraData;
  if (xh)
    return usb_claim_interface(xh->uh, interface);
  else
    return -1;
}



static int ausb3_release_interface(ausb_dev_handle *ah, int interface){
  ausb3_extra *xh;

  xh=(ausb3_extra*)ah->extraData;
  if (xh)
    return usb_release_interface(xh->uh, interface);
  else
    return -1;
}



static int ausb3_set_configuration(ausb_dev_handle *ah, int configuration){
  ausb3_extra *xh;

  xh=(ausb3_extra*)ah->extraData;
  if (xh) {
    int ret;

    ret=usb_set_configuration(xh->uh, configuration);
    if (ret<0) {
      DEBUGP(ah, "usb_set_configuration returned %d (errno=%d:%s)\n", ret,
	     errno, strerror(errno));
    }
    return ret;
  }
  else
    return -1;
}



static int ausb3_reset(ausb_dev_handle *ah){
  ausb3_extra *xh;

  xh=(ausb3_extra*)ah->extraData;
  if (xh)
    return usb_reset(xh->uh);
  else
    return -1;
}



static int ausb3_reset_endpoint(ausb_dev_handle *ah, unsigned int ep){
  ausb3_extra *xh;

  xh=(ausb3_extra*)ah->extraData;
  if (xh)
    return usb_resetep(xh->uh, ep);
  else
    return -1;
}



static int ausb3_clear_halt(ausb_dev_handle *ah, unsigned int ep){
  ausb3_extra *xh;

  xh=(ausb3_extra*)ah->extraData;
  if (xh)
    return usb_clear_halt(xh->uh, ep);
  else
    return -1;
}



static int ausb3_reset_pipe(ausb_dev_handle *ah, int ep){
  ausb3_extra *xh;

  xh=(ausb3_extra*)ah->extraData;
  if (xh) {
    int rv;

    rv=usb_control_msg(xh->uh,
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

    rv=usb_clear_halt(xh->uh, ep);
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



static void ausb3_close(struct ausb_dev_handle *ah){
  ausb3_extra *xh;

  xh=(ausb3_extra*)ah->extraData;
  if (xh) {
    usb_close(xh->uh);
    free(xh);
  }
}



/* not static since this function is needed in ausb.c */
int ausb3_extend(ausb_dev_handle *ah){
  struct usb_device *dev;
  ausb3_extra *xh;

  DEBUGP(ah, "Extending AUSB handle as type 3");

  xh=malloc(sizeof *xh);
  if (xh==0) {
    DEBUGP(ah, "memory full\n");
    return -1;
  }
  memset(xh, 0, sizeof(*xh));

  /* get libusb representation of the given device */
  dev=ausb_libusb0_get_usbdev(&(ah->device));
  if (dev==NULL) {
    DEBUGP(ah, "libusb device not found");
    free(xh);
    return -1;
  }

  /* open the device */
  xh->uh=usb_open(dev);
  if (!xh->uh) {
    DEBUGP(ah, "usb_open() failed\n");
    fprintf(stderr, "usb_open() failed (%d=%s)\n", errno, strerror(errno));
    free(xh);
    return -1;
  }

  /* done */
  ah->extraData=xh;
  ah->closeFn=ausb3_close;
  ah->startInterruptFn=ausb3_start_interrupt;
  ah->stopInterruptFn=ausb3_stop_interrupt;
  ah->bulkWriteFn=ausb3_bulk_write;
  ah->bulkReadFn=ausb3_bulk_read;

  ah->claimInterfaceFn=ausb3_claim_interface;
  ah->releaseInterfaceFn=ausb3_release_interface;
  ah->setConfigurationFn=ausb3_set_configuration;

  ah->resetFn=ausb3_reset;
  ah->resetEndpointFn=ausb3_reset_endpoint;
  ah->clearHaltFn=ausb3_clear_halt;
  ah->resetPipeFn=ausb3_reset_pipe;

  return 0;
}


#endif /* ifndef USE_USB1 */


