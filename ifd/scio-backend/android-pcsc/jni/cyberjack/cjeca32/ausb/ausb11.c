/* Wrapper/Extension code to libusb-0.1 to support asynchronous requests
 * on Linux platforns 
 *
 * (C) 2004-2005 by Harald Welte <laforge@gnumonks.org>
 * extended/modified by Martin Preuss <martin@libchipcard.de> (C) 2006,2007
 *
 * Distributed and licensed under the terms of GNU LGPL, Version 2.1
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
#include <signal.h>
#include <errno.h>
#include <time.h>
#include <sys/utsname.h>
#include <sys/ioctl.h>
#include <libusb.h>

#include "ausb_l.h"
#include "ausb_libusb1_l.h"

#ifndef USBDEVFS_CONNECT
# define USBDEVFS_CONNECT _IO('U', 23)
#endif


#define MAX_READ_WRITE	4096
#define CT_MAX_DEVICES  256

#define AUSB_MAX_URB_SIZE 256

#define AUSB_MAX_IRQWAIT 10

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




struct ausb11_extra {
  libusb_device_handle *uh;
  struct libusb_transfer *intUrb;
  struct libusb_transfer *bulkinUrb;

  int ioError;
  int intCounter;
  int dontFree;
  unsigned char intUrbBuffer[AUSB_MAX_URB_SIZE];
  unsigned char bulkinUrbBuffer[AUSB_MAX_URB_SIZE];
};
typedef struct ausb11_extra ausb11_extra;




int ausb11_get_kernel_driver_name(ausb_dev_handle *ah, int interface, char *name,
				  unsigned int namelen){
  ausb11_extra *xh;

  DEBUGP(ah, "ausb_get_driver_np\n");

  xh=(ausb11_extra*)ah->extraData;
  if (xh) {
    if (namelen<1 || name==NULL) {
      fprintf(stderr, "RSCT: no name buffer on ausb11_get_kernel_driver_name\n");
      return -1;
    }
    if (libusb_kernel_driver_active(xh->uh, interface)) {
      strncpy(name, "cyberjack", namelen-1);
      name[namelen-1]=0;
      return 1;
    }
    else {
      name[0]=0;
      return 0;
    }
  }
  return -1;
}



int ausb11_detach_kernel_driver(ausb_dev_handle *ah, int interface){
  ausb11_extra *xh;

  DEBUGP(ah, "ausb_detach_kernel_driver_np\n");
  xh=(ausb11_extra*)ah->extraData;
  if (xh) {
    return libusb_detach_kernel_driver(xh->uh, interface);
  }
  return -1;
}



int ausb11_reattach_kernel_driver(ausb_dev_handle *ah, int interface){
  ausb11_extra *xh;

  xh=(ausb11_extra*)ah->extraData;
  if (xh)
    return libusb_attach_kernel_driver(xh->uh, interface);
  return -1;
}



static int ausb11_claim_interface(ausb_dev_handle *ah, int interface){
  ausb11_extra *xh;

  xh=(ausb11_extra*)ah->extraData;
  if (xh)
    return libusb_claim_interface(xh->uh, interface);
  else
    return -1;
}



static int ausb11_release_interface(ausb_dev_handle *ah, int interface){
  ausb11_extra *xh;

  xh=(ausb11_extra*)ah->extraData;
  if (xh)
    return libusb_release_interface(xh->uh, interface);
  else
    return -1;
}



static int ausb11_set_configuration(ausb_dev_handle *ah, int configuration){
  ausb11_extra *xh;

  xh=(ausb11_extra*)ah->extraData;
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



static int ausb11_reset(ausb_dev_handle *ah){
  ausb11_extra *xh;

  xh=(ausb11_extra*)ah->extraData;
  if (xh) {
    int rv;

    xh->ioError=0;
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



static int ausb11_reset_endpoint(ausb_dev_handle *ah, unsigned int ep){
  return 0;
}



static int ausb11_clear_halt(ausb_dev_handle *ah, unsigned int ep){
  ausb11_extra *xh;

  xh=(ausb11_extra*)ah->extraData;
  if (xh) {
    if (xh->ioError) {
      DEBUGP(ah, "Previous IO error, aborting clear_halt");
      return -1;
    }
    else
      return libusb_clear_halt(xh->uh, ep);
  }
  else
    return -1;
}



static int ausb11_reset_pipe(ausb_dev_handle *ah, int ep){
  ausb11_extra *xh;

  xh=(ausb11_extra*)ah->extraData;
  if (xh) {
    if (xh->ioError) {
      DEBUGP(ah, "Previous IO error, aborting.");
      return -1;
    }
    else {
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
	DEBUGP(ah, "unable to clear halt on endpoint %d (%d=%s)",ep,
	       errno, strerror(errno));
	return rv;
      }
      return rv;
    }
  }
  else
    return -1;
}




static void ausb11_int_callback(struct libusb_transfer *xfer) {
  ausb_dev_handle *ah;
  ausb11_extra *xh;

  ah=(ausb_dev_handle *) xfer->user_data;
  if (!ah) {
    DEBUGP(NULL, "cant't call handler because of a missing ah ptr\n");
    return;
  }

  DEBUGP(ah, "received interrupt URB\n");
  xh=(ausb11_extra*)ah->extraData;
  if (xh) {
    int ret;
    xh->intCounter++;
  
    if (!ah->cb.handler) {
      DEBUGP(NULL, "received interrupt URB, no handler\n");
      return;
    }
    switch(xfer->status) {
    case LIBUSB_TRANSFER_COMPLETED:
      ah->cb.handler(xfer->buffer, xfer->actual_length, ah->cb.userdata);
      break;
    case LIBUSB_TRANSFER_ERROR:
      DEBUGP(ah, "Interrupt transfer status: Error");
      break;
    case LIBUSB_TRANSFER_TIMED_OUT:
      DEBUGP(ah, "Interrupt transfer status: Timed out");
      break;
    case LIBUSB_TRANSFER_CANCELLED:
      DEBUGP(ah, "Interrupt transfer status: Cancelled (not issueing new URB)");
      return;
    case LIBUSB_TRANSFER_STALL:
      DEBUGP(ah, "Interrupt transfer status: HALT condition detected (not issueing new URB)");
      return;
    case LIBUSB_TRANSFER_NO_DEVICE:
      DEBUGP(ah, "Interrupt transfer status: Device lost (not issueing new URB)");
      return;
    case LIBUSB_TRANSFER_OVERFLOW:
      DEBUGP(ah, "Interrupt transfer status: Overflow");
      break;
    default:
      DEBUGP(ah, "Interrupt transfer status: Unknown (%d)", xfer->status);
      break;
    }
  
    /* re-submit interrupt transfer */
    ret=libusb_submit_transfer(xh->intUrb);
    if (ret) {
      DEBUGP(ah, "Error on libusb_submit_transfer: %d\n", ret);
    }
  }
}



static int ausb11_start_interrupt(ausb_dev_handle *ah, int ep) {
  ausb11_extra *xh;
  int ret;

  xh=(ausb11_extra*)ah->extraData;

  DEBUGP(ah, "Starting interrupt pipe for endpoint %d", ep);

  if (xh->intUrb==NULL) {
    xh->intUrb=libusb_alloc_transfer(0);
    libusb_fill_interrupt_transfer(xh->intUrb,
				   xh->uh,
				   ep,
				   xh->intUrbBuffer,
				   sizeof(xh->intUrbBuffer)-1,
				   ausb11_int_callback,
				   (void*) ah,
				   0);       /* timeout [ms], 0=wait forever */
  }

  ret=libusb_submit_transfer(xh->intUrb);
  if (ret) {
    DEBUGP(ah, "Error on libusb_submit_transfer: %d\n", ret);
    return -1;
  }

  DEBUGP(ah, "Interrupt pipe started");
  return 0;
}



static int ausb11_stop_interrupt(ausb_dev_handle *ah) {
  ausb11_extra *xh;

  xh=(ausb11_extra*)ah->extraData;
  if (xh->intUrb) {
    int rv;

    xh->intCounter=0;
    rv=libusb_cancel_transfer(xh->intUrb);
    if (rv) {
      DEBUGP(ah, "Error on cancel_transfer: %d", rv);
    }
    else {
      int triesLeft=AUSB_MAX_IRQWAIT;

      /* wait for interrupt request to finish */
      DEBUGP(ah, "Waiting for cancellation of interrupt request to finish...");
      while(triesLeft && xh->intCounter==0) {
	rv=ausb_libusb1_handle_events();
	if (rv) {
	  DEBUGP(ah, "Error on handle_events (%d)", rv);
          return rv;
	}
        triesLeft--;
      }
      DEBUGP(ah, "Tries left while waiting for URB to return: %d out of %d", triesLeft, AUSB_MAX_IRQWAIT);
      if (triesLeft<1) {
	/* we risk memory leaks on purpose here, because it is better
	 * to leak a few bytes here than to make the application crash later
	 * because the interrupt handler doesn't find its buffer */
	DEBUGP(ah, "Interrupt URB did not return, this can't be good...");
        /* don't free here */
	xh->intUrb=NULL;
	xh->dontFree=1;
      }
    }
  }

  return 0;
}






static int ausb11_bulk_write(ausb_dev_handle *ah, int ep,
			     char *bytes, int length,
			     int timeout){
  ausb11_extra *xh;

  DEBUGP(ah, "bulk write (ah=%p, ep=0x%x, bytes=%p, size=%d, timeout=%d\n",
	 ah, ep, bytes, length, timeout);

  xh=(ausb11_extra*)ah->extraData;
  if (xh) {
    if (xh->ioError) {
      DEBUGP(ah, "Previous IO error, aborting transfer");
      return -1;
    }
    else {
      int l;
      int noDataFlag=0;

      l=length;
      while(l) {
	int transferred=0;
	int rv;

	/* Ensure the endpoint address is correct */
	ep &= ~LIBUSB_ENDPOINT_IN;

	rv=libusb_bulk_transfer(xh->uh, ep, (unsigned char*) bytes, length, &transferred, 0);
	if (rv && rv!=LIBUSB_ERROR_TIMEOUT) {
	  DEBUGP(ah, "Error on libusb_bulk_transfer: %d", rv);
	  xh->ioError=rv;
	  return -1;
	}

	if (transferred==0) {
	  if (noDataFlag) {
	    DEBUGP(ah, "no data transferred, aborting");
            return -1;
	  }
	  else {
	    DEBUGP(ah, "no data transferred, trying again");
            noDataFlag++;
	  }
	}

	l-=transferred;
	bytes+=transferred;

	if (l>0) {
	  DEBUGP(ah, "not all data transferred (only %d bytes of %d)", transferred, length);
	}
      }

      return length;
    }
  }
  else
    return -1;
}



static int ausb11_bulk_read(ausb_dev_handle *ah, int ep,
			   char *bytes, int size,
			   int timeout){
  ausb11_extra *xh;

  DEBUGP(ah, "bulk read (ah=%p, ep=0x%x, bytes=%p, size=%d, timeout=%d\n",
         ah, ep, bytes, size, timeout);

  xh=(ausb11_extra*)ah->extraData;
  if (xh) {
    if (xh->ioError) {
      DEBUGP(ah, "Previous IO error, aborting transfer");
      return -1;
    }
    else {
      int transferred=0;
      int rv;

      /* Ensure the endpoint address is correct */
      ep |= LIBUSB_ENDPOINT_IN;

      rv=libusb_bulk_transfer(xh->uh, ep, (unsigned char*) bytes, size, &transferred, 0);
      if (rv) {
	DEBUGP(ah, "Error on libusb_bulk_transfer: %d", rv);
        xh->ioError=rv;
	return -1;
      }
      if (ah->pid!=0x100 &&
	  (bytes[0]==0x40 || /* RDR_TO_PC_KEYEVENT */
	   bytes[0]==0x50)) { /* RDR_TO_PC_NOTIFYSLOTCHANGE */
	DEBUGP(ah, "interrupt event received via bulk-in\n");
	// TODO
      }

      return transferred;
    }
  }
  else
    return -1;
}



static void ausb11_close(struct ausb_dev_handle *ah){
  ausb11_extra *xh;

  xh=(ausb11_extra*)ah->extraData;
  if (xh) {
    if (xh->intUrb) {
      libusb_free_transfer(xh->intUrb);
      xh->intUrb=NULL;
    }
    if (xh->bulkinUrb) {
      libusb_free_transfer(xh->bulkinUrb);
      xh->bulkinUrb=NULL;
    }
    libusb_close(xh->uh);
    if (xh->dontFree==0)
      /* only free not prohibited (see ausb11_stop_interrupt) */
      free(xh);
  }
}



/* not static since this function is needed in ausb.c */
int ausb11_extend(ausb_dev_handle *ah){
  int rv;
  libusb_device *dev;
  ausb11_extra *xh;

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
  ah->closeFn=ausb11_close;
  ah->startInterruptFn=ausb11_start_interrupt;
  ah->stopInterruptFn=ausb11_stop_interrupt;
  ah->bulkWriteFn=ausb11_bulk_write;
  ah->bulkReadFn=ausb11_bulk_read;

  ah->claimInterfaceFn=ausb11_claim_interface;
  ah->releaseInterfaceFn=ausb11_release_interface;
  ah->setConfigurationFn=ausb11_set_configuration;

  ah->resetFn=ausb11_reset;
  ah->resetEndpointFn=ausb11_reset_endpoint;
  ah->clearHaltFn=ausb11_clear_halt;
  ah->resetPipeFn=ausb11_reset_pipe;

  ah->getKernelDriverNameFn=ausb11_get_kernel_driver_name;
  ah->detachKernelDriverFn=ausb11_detach_kernel_driver;
  ah->reattachKernelDriverFn=ausb11_reattach_kernel_driver;

  return 0;
}




#endif /* ifdef USE_USB1 */


