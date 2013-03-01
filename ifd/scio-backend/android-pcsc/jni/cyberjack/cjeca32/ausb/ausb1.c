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


#ifndef USE_USB1



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
#include <usb.h>

#ifdef CONFIG_COMPAT
#undef CONFIG_COMPAT
#endif
#ifndef __user
# define __user
#endif
#include <linux/usbdevice_fs.h>

#include "ausb_l.h"
#include "ausb_libusb0_l.h"

#ifndef USBDEVFS_CONNECT
# define USBDEVFS_CONNECT _IO('U', 23)
#endif


#define MAX_READ_WRITE	4096
#define CT_MAX_DEVICES  256

#define AUSB_MAX_INTURB_SIZE 256


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




struct ausb1_extra {
  usb_dev_handle *uh;
  struct usbdevfs_urb intUrb;
  char intUrbBuffer[AUSB_MAX_INTURB_SIZE];
};
typedef struct ausb1_extra ausb1_extra;




int ausb1_get_fd(ausb_dev_handle *ah) {
  ausb1_extra *xh;

  xh=(ausb1_extra*)ah->extraData;
  if (xh)
    return *((int*)xh->uh);
  else
    return -1;
}




int ausb1_get_kernel_driver_name(ausb_dev_handle *ah, int interface, char *name,
				 unsigned int namelen){
  ausb1_extra *xh;

  DEBUGP(ah, "ausb_get_driver_np\n");

  xh=(ausb1_extra*)ah->extraData;
  if (xh) {
    if (namelen<2) {
      DEBUGP(ah, "Buffer too small (%d)\n", namelen);
      return -1;
    }
    else {
#ifdef LIBUSB_HAS_GET_DRIVER_NP
      int ret;

      ret=usb_get_driver_np(xh->uh, interface, name, namelen-1);
      if (ret<0) {
	DEBUGP(ah, "usb_get_driver_np: %d (%s)\n",
	       errno, strerror(errno));
	return 0;
      }
      else if (ret==0) {
	name[namelen-1]=0;
	return 1;
      }
#endif
    }
  }
  return -1;
}



int ausb1_detach_kernel_driver(ausb_dev_handle *ah, int interface){
  ausb1_extra *xh;

  DEBUGP(ah, "ausb_detach_kernel_driver_np\n");
  xh=(ausb1_extra*)ah->extraData;
  if (xh) {
#ifdef LIBUSB_HAS_DETACH_KERNEL_DRIVER_NP
    return usb_detach_kernel_driver_np(xh->uh, interface);
#endif
  }
  return -1;
}



int ausb1_reattach_kernel_driver(ausb_dev_handle *ah, int interface){
  ausb1_extra *xh;

  xh=(ausb1_extra*)ah->extraData;
  if (xh) {
    struct usbdevfs_ioctl command;
    int ret;

    command.ifno = interface;
    command.ioctl_code = USBDEVFS_CONNECT;
    command.data = NULL;

    ret=ioctl(ausb1_get_fd(ah), USBDEVFS_IOCTL, &command);
    if (ret<0) {
      DEBUGP(ah, "IOCTL(USBDEVFS_CONNECT): %d (%s)\n",
	     errno, strerror(errno));
    }
    return ret;
  }
  return -1;
}



static int ausb1_claim_interface(ausb_dev_handle *ah, int interface){
  ausb1_extra *xh;

  xh=(ausb1_extra*)ah->extraData;
  if (xh)
    return usb_claim_interface(xh->uh, interface);
  else
    return -1;
}



static int ausb1_release_interface(ausb_dev_handle *ah, int interface){
  ausb1_extra *xh;

  xh=(ausb1_extra*)ah->extraData;
  if (xh)
    return usb_release_interface(xh->uh, interface);
  else
    return -1;
}



static int ausb1_set_configuration(ausb_dev_handle *ah, int configuration){
  ausb1_extra *xh;

  xh=(ausb1_extra*)ah->extraData;
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



static int ausb1_reset(ausb_dev_handle *ah){
  ausb1_extra *xh;

  xh=(ausb1_extra*)ah->extraData;
  if (xh)
    return usb_reset(xh->uh);
  else
    return -1;
}



static int ausb1_reset_endpoint(ausb_dev_handle *ah, unsigned int ep){
  ausb1_extra *xh;

  xh=(ausb1_extra*)ah->extraData;
  if (xh)
    return usb_resetep(xh->uh, ep);
  else
    return -1;
}



static int ausb1_clear_halt(ausb_dev_handle *ah, unsigned int ep){
  ausb1_extra *xh;

  xh=(ausb1_extra*)ah->extraData;
  if (xh)
    return usb_clear_halt(xh->uh, ep);
  else
    return -1;
}



static int ausb1_reset_pipe(ausb_dev_handle *ah, int ep){
  ausb1_extra *xh;

  xh=(ausb1_extra*)ah->extraData;
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




static void ausb1_fill_bulk_urb(struct usbdevfs_urb *uurb, unsigned char endpoint,
				void *buffer, int buffer_length){
  uurb->type=USBDEVFS_URB_TYPE_BULK;
  uurb->endpoint = endpoint; /* | USB_DIR_IN; */
  uurb->flags = 1 ; /* USBDEVFS_URB_QUEUE_BULK; */
  uurb->buffer = buffer;
  uurb->buffer_length = buffer_length;
  uurb->actual_length=0;
  uurb->signr = 0;;
  uurb->start_frame = -1;
}



static void ausb1_fill_int_urb(struct usbdevfs_urb *uurb, unsigned char endpoint,
			       void *buffer, int buffer_length){
  uurb->type=USBDEVFS_URB_TYPE_INTERRUPT;
  uurb->endpoint = endpoint; /* | USB_DIR_IN; */
  uurb->flags = 0 ; /* USBDEVFS_URB_QUEUE_BULK; */
  uurb->buffer = buffer;
  uurb->buffer_length = buffer_length;
  uurb->actual_length=0;
  uurb->signr = 0;;
  uurb->start_frame = -1;
}



static int ausb1_submit_urb(ausb_dev_handle *ah, struct usbdevfs_urb *uurb){
  int ret;

  uurb->actual_length=0;

  /* save ausb_dev_handle in opaque usercontext field */
  uurb->usercontext = ah;

  do {
    ret = ioctl(ausb1_get_fd(ah), USBDEVFS_SUBMITURB, uurb);
  } while (ret < 0 && errno == EINTR);

  return ret;
}



static int ausb1_discard_urb(ausb_dev_handle *ah, struct usbdevfs_urb *uurb){
  int ret;

  do {
    ret = ioctl(ausb1_get_fd(ah), USBDEVFS_DISCARDURB, uurb);
  } while (ret < 0 && errno == EINTR);

  return ret;
}



static void handle_urb(struct usbdevfs_urb *uurb){
  struct ausb_dev_handle *ah = uurb->usercontext;

  if (!ah) {
    DEBUGP(NULL, "cant't call handler because of a missing ah ptr\n");
    return;
  }

  if (!ah->cb.handler) {
    DEBUGP(NULL, "received URB type %u, no handler\n", uurb->type);
    return;
  }
  ah->cb.handler(uurb->buffer, uurb->actual_length, ah->cb.userdata);
}



static int ausb1_start_interrupt(ausb_dev_handle *ah, int ep) {
  ausb1_extra *xh;
  int ret;

  xh=(ausb1_extra*)ah->extraData;

  DEBUGP(ah, "Starting interrupt pipe for endpoint %d", ep);
  ausb1_fill_int_urb(&xh->intUrb,
		     ep,
		     xh->intUrbBuffer,
		     AUSB_MAX_INTURB_SIZE);
  ret=ausb1_submit_urb(ah, &xh->intUrb);
  if (ret<0) {
    DEBUGP(ah, "unable to submit interrupt urb (%d: %s)\n",
	   errno, strerror(errno));
    return -1;
  }

  /* read all pending interrupts */
  DEBUGP(ah, "Clearing interrupt pipe");
  for (;;) {
    int ret;
    struct usbdevfs_urb *rurb;
    time_t tStart;

    tStart=time(NULL);
    do {
      time_t tEnd;

      tEnd=time(NULL);
      if (difftime(tEnd, tStart)>10.0) {
	DEBUGP(ah, "Timeout while clearing interrupt pipe");
        return -1;
      }
      rurb=NULL;
      ret = ioctl(ausb1_get_fd(ah), USBDEVFS_REAPURBNDELAY, &rurb);
    } while (ret < 0 && errno == EINTR);

    if (ret<0) {
      if (errno==EAGAIN) {
	/*fprintf(stderr, "No pending interrupt\n");*/
	break;
      }
      else {
	DEBUGP(ah, "error getting URB [%s]\n", strerror(errno));
        return -1;
      }
    }

    if (rurb==&xh->intUrb) {
      /* handle interrupt urb */
      DEBUGP(ah, "answer for interrupt urb\n");
      if (ausb1_submit_urb(ah, &xh->intUrb)) {
	DEBUGP(ah, "unable to submit interrupt urb\n");
	return -1;
      }
    }
    else {
      DEBUGP(ah, "returned unknown urb %p\n", rurb);
    }
  }

  DEBUGP(ah, "Interrupt pipe started");
  return 0;
}



static int ausb1_stop_interrupt(ausb_dev_handle *ah) {
  ausb1_extra *xh;

  xh=(ausb1_extra*)ah->extraData;
  ausb1_discard_urb(ah, &xh->intUrb);
  return 0;
}






static int ausb1_bulk_write(ausb_dev_handle *ah, int ep,
			    char *bytes, int length,
			    int timeout){
  struct usbdevfs_bulktransfer bulk;
  int ret, sent = 0;
  ausb1_extra *xh;
  time_t tStart;

  xh=(ausb1_extra*)ah->extraData;

  DEBUGP(ah, "bulk write (ah=%p, ep=0x%x, bytes=%p, size=%d, timeout=%d\n",
	 ah, ep, bytes, length, timeout);

  /* Ensure the endpoint address is correct */
  ep &= ~USB_ENDPOINT_IN;

  tStart=time(NULL);
  do {
    time_t tEnd;

    tEnd=time(NULL);
    if (difftime(tEnd, tStart)>20.0) {
      DEBUGP(ah, "Timeout while sending data to bulkout pipe");
      return -1;
    }

    bulk.ep=ep;
    bulk.len=length-sent;
    if (bulk.len>MAX_READ_WRITE)
      bulk.len=MAX_READ_WRITE;
    bulk.timeout=timeout;
    bulk.data=(unsigned char*)bytes+sent;

    ret=ioctl(ausb1_get_fd(ah), USBDEVFS_BULK, &bulk);
    if (ret<0) {
      DEBUGP(ah, "Error writing to bulk endpoint %d: %d (%s)\n",
	     ep, errno, strerror(errno));
      return ret;
    }
    sent+=ret;
  } while(ret>0 && sent<length);

  return sent;
}



static int ausb1_bulk_read(ausb_dev_handle *ah, int ep,
			   char *bytes, int size,
			   int timeout){
  int ret;
  struct usbdevfs_urb *uurb;
  struct usbdevfs_urb *rurb;
  ausb1_extra *xh;
  time_t tStart;

  xh=(ausb1_extra*)ah->extraData;

  DEBUGP(ah, "bulk read (ah=%p, ep=0x%x, bytes=%p, size=%d, timeout=%d\n",
         ah, ep, bytes, size, timeout);

  uurb=malloc(sizeof(*uurb));
  if (!uurb) {
    DEBUGP(ah, "unable to allocate urb\n");
    return -1;
  }

  ausb1_fill_bulk_urb(uurb, ep, bytes, size);
  if (ausb1_submit_urb(ah, uurb)) {
    DEBUGP(ah, "unable to submit bulk urb\n");
    return -1;
  }

  tStart=time(NULL);
  for (;;) {
    do {
      time_t tEnd;

      tEnd=time(NULL);
      if (difftime(tEnd, tStart)>AUSB_MAX_TIMEOUT) {
	DEBUGP(ah, "Timeout while waiting for data on bulkin pipe");
	ausb1_discard_urb(ah, uurb);
        return -1;
      }
      rurb=NULL;
      //ret = ioctl(ausb1_get_fd(ah), USBDEVFS_REAPURBNDELAY, &rurb);
      DEBUGP(ah, "sending ioctl...\n");
      ret=ioctl(ausb1_get_fd(ah), USBDEVFS_REAPURB, &rurb);
      DEBUGP(ah, "sending ioctl... done (%d)\n", ret);
    } while (ret < 0 && (errno == EINTR || errno==EAGAIN));

    if (ret<0) {
      DEBUGP(ah, "ioctl returned %d (errno=%d:%s)\n", ret,
	     errno, strerror(errno));
      ausb1_discard_urb(ah, uurb);
      return -1;
    }

    DEBUGP(ah, "ioctl returned urb %p\n", rurb);
    if (rurb==&xh->intUrb) {
      /* handle interrupt urb */
      DEBUGP(ah, "answer for interrupt urb\n");
      handle_urb(rurb);

      if (ausb1_submit_urb(ah, &xh->intUrb)) {
	DEBUGP(ah, "unable to submit interrupt urb\n");
	return -1;
      }
    }
    else if (rurb==uurb) {
      DEBUGP(ah, "answer for bulk urb\n");
      ret=rurb->actual_length;

      if (ah->pid!=0x100 &&
	  (bytes[0]==0x40 || /* RDR_TO_PC_KEYEVENT */
	   bytes[0]==0x50)) { /* RDR_TO_PC_NOTIFYSLOTCHANGE */
	DEBUGP(ah, "interrupt event received via bulk-in\n");
	handle_urb(rurb);
	ausb1_fill_bulk_urb(uurb, ep, bytes, size);
	if (ausb1_submit_urb(ah, uurb)) {
	  DEBUGP(ah, "unable to submit bulk urb\n");
	  return -1;
	}
      }
      else {
	DEBUGP(ah, "Received %d bytes\n", ret);
	free(uurb);
	return ret;
      }
    }
    else {
      DEBUGP(ah, "returned unknown uurb %p\n", rurb);
      ausb1_discard_urb(ah, uurb);
      return -1;
    }
  }
}



static void ausb1_close(struct ausb_dev_handle *ah){
  ausb1_extra *xh;

  xh=(ausb1_extra*)ah->extraData;
  if (xh) {
    usb_close(xh->uh);
    free(xh);
  }
}



/* not static since this function is needed in ausb.c */
int ausb1_extend(ausb_dev_handle *ah){
  struct usb_device *dev;
  ausb1_extra *xh;

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
  ah->closeFn=ausb1_close;
  ah->startInterruptFn=ausb1_start_interrupt;
  ah->stopInterruptFn=ausb1_stop_interrupt;
  ah->bulkWriteFn=ausb1_bulk_write;
  ah->bulkReadFn=ausb1_bulk_read;

  ah->claimInterfaceFn=ausb1_claim_interface;
  ah->releaseInterfaceFn=ausb1_release_interface;
  ah->setConfigurationFn=ausb1_set_configuration;

  ah->resetFn=ausb1_reset;
  ah->resetEndpointFn=ausb1_reset_endpoint;
  ah->clearHaltFn=ausb1_clear_halt;
  ah->resetPipeFn=ausb1_reset_pipe;

  ah->getKernelDriverNameFn=ausb1_get_kernel_driver_name;
  ah->detachKernelDriverFn=ausb1_detach_kernel_driver;
  ah->reattachKernelDriverFn=ausb1_reattach_kernel_driver;

  return 0;
}



#endif /* ifndef USE_USB1 */



