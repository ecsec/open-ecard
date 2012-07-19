/* USB support for the Cyberjack family of readers.
 *
 * Previous version were (C) 2004-2005 by Harald Welte <laforge@gnumonks.org>
 * This version is a rewrite (asynchronous USB is no longer needed).
 *
 * (C) 2007 Martin Preuss <martin@libchipcard.de>
 *
 * Distributed and licensed under the terms of GNU LGPL, Version 2.1
 */


//#ifdef HAVE_CONFIG_H
# include <config.h>
//#endif

#include <inttypes.h>

#include "ausb_l.h"

#include "ausb_libusb0_l.h"
#include "ausb_libusb1_l.h"

#include <string.h>
#include <stdlib.h>
#include <unistd.h>
#include <stdio.h>
#include <signal.h>
#include <errno.h>
#include <sys/ioctl.h>
#include <time.h>
#include <assert.h>

#ifdef HAVE_HAL
# include <hal/libhal.h>
# include <dbus/dbus.h>
#endif


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



static AUSB_LOG_FN ausb_log_fn=NULL;



void ausb_set_log_fn(AUSB_LOG_FN fn) {
  ausb_log_fn=fn;
}



void ausb_log(ausb_dev_handle *ah, const char *text,
	      const void *pData, uint32_t ulDataLen) {
  if (ausb_log_fn)
    ausb_log_fn(ah, text, pData, ulDataLen);
}



int ausb_register_callback(ausb_dev_handle *ah,
			   AUSB_CALLBACK callback,
			   void *userdata){
  DEBUGP(ah, "registering callback:%p\n", callback);

  ah->cb.handler=callback;
  ah->cb.userdata=userdata;

  return 0;
}



int ausb_claim_interface(ausb_dev_handle *ah, int interface){
  DEBUGP(ah, "ausb_claim_interface\n");
  if (ah->claimInterfaceFn)
    return ah->claimInterfaceFn(ah, interface);
  DEBUGP(ah, "callback for ausb_claim_interface not set\n");
  return -1;
}



int ausb_release_interface(ausb_dev_handle *ah, int interface){
  DEBUGP(ah, "ausb_release_interface\n");
  if (ah->releaseInterfaceFn)
    return ah->releaseInterfaceFn(ah, interface);
  DEBUGP(ah, "callback for ausb_release_interface not set\n");
  return -1;
}



int ausb_set_configuration(ausb_dev_handle *ah, int configuration){
  DEBUGP(ah, "ausb_set_configuration\n");
  if (ah->setConfigurationFn)
    return ah->setConfigurationFn(ah, configuration);
  DEBUGP(ah, "callback for ausb_set_configuration not set\n");
  return -1;
}



ausb_dev_handle *ausb_open(rsct_usbdev_t *dev, int t) {
  ausb_dev_handle *ah=NULL;
  int rv;

  /*fprintf(stderr, "Opening device...\n");*/
  ah=malloc(sizeof *ah);
  if (ah==0) {
    DEBUGP(ah, "memory full\n");
    return 0;
  }
  memset(ah, 0, sizeof(*ah));

  ah->pid=dev->productId;
  ah->device=*dev;

  switch(t) {
  case 1:
#ifdef USE_USB1
    rv=ausb11_extend(ah);
#else
    rv=ausb1_extend(ah);
#endif
    break;

  case 2:
    DEBUGP(ah, "This type is no longer supported.\n");
    rv=-1;
    break;

  case 3:
#ifdef USE_USB1
    rv=ausb31_extend(ah);
#else
    rv=ausb3_extend(ah);
#endif
    break;

  default:
    DEBUGP(ah, "Invalid type %d\n", t);
    rv=-1;
    break;
  }

  if (rv) {
    DEBUGP(ah, "Could not extend as type %d (%d)\n", t, rv);
    free(ah);
    return 0;
  }

  return ah;
}



int ausb_close(ausb_dev_handle *ah) {
  DEBUGP(ah, "ausb_close\n");
  if (ah->closeFn)
    ah->closeFn(ah);
  free(ah);
  return 0;
}



int ausb_start_interrupt(ausb_dev_handle *ah, int ep) {
  DEBUGP(ah, "ausb_start_interrupt\n");
  if (ah->startInterruptFn)
    return ah->startInterruptFn(ah, ep);
  return 0;
}



int ausb_stop_interrupt(ausb_dev_handle *ah) {
  DEBUGP(ah, "ausb_stop_interrupt\n");
  if (ah->stopInterruptFn)
    return ah->stopInterruptFn(ah);
  return 0;
}



int ausb_bulk_write(ausb_dev_handle *ah, int ep,
		    char *bytes, int size,
		    int timeout) {
  DEBUGL(ah, "Write:", bytes, size);

  if (ah->bulkWriteFn)
    return ah->bulkWriteFn(ah, ep, bytes, size, timeout);
  return -1;
}



int ausb_bulk_read(ausb_dev_handle *ah, int ep,
		   char *bytes, int size,
		   int timeout) {
  if (ah->bulkReadFn) {
    int rv;

    DEBUGP(ah, "Reading up to %d bytes", size);
    rv=ah->bulkReadFn(ah, ep, bytes, size, timeout);
    if (rv>=0) {
      DEBUGL(ah, "Read:", bytes, rv);
    }
    return rv;
  }
  return -1;
}



int ausb_reset(ausb_dev_handle *ah){
  DEBUGP(ah, "ausb_reset\n");
  if (ah->resetFn)
    return ah->resetFn(ah);
  else
    return -1;
}



int ausb_reset_endpoint(ausb_dev_handle *ah, unsigned int ep){
  DEBUGP(ah, "ausb_reset_endpoint\n");
  if (ah->resetEndpointFn)
    return ah->resetEndpointFn(ah, ep);
  else
    return -1;
}



int ausb_clear_halt(ausb_dev_handle *ah, unsigned int ep){
  DEBUGP(ah, "ausb_clear_halt\n");
  if (ah->clearHaltFn)
    return ah->clearHaltFn(ah, ep);
  else
    return -1;
}



int ausb_reset_pipe(ausb_dev_handle *ah, int ep){
  DEBUGP(ah, "ausb_reset_pipe\n");
  if (ah->resetPipeFn)
    return ah->resetPipeFn(ah, ep);
  else
    return -1;
}



int ausb_get_kernel_driver_name(ausb_dev_handle *ah, int interface, char *name,
				unsigned int namelen){
  DEBUGP(ah, "ausb_get_kernel_driver_name\n");
  if (ah->getKernelDriverNameFn)
    return ah->getKernelDriverNameFn(ah, interface, name, namelen);
  return -1;
}



int ausb_detach_kernel_driver(ausb_dev_handle *ah, int interface){
  DEBUGP(ah, "ausb_detach_kernel_driver\n");
  if (ah->detachKernelDriverFn)
    return ah->detachKernelDriverFn(ah, interface);
  return -1;
}



int ausb_reattach_kernel_driver(ausb_dev_handle *ah, int interface){
  DEBUGP(ah, "ausb_reattach_kernel_driver\n");
  if (ah->reattachKernelDriverFn)
    return ah->reattachKernelDriverFn(ah, interface);
  return -1;
}



int ausb_init(void) {
#ifdef USE_USB1
  return ausb_libusb1_init();
#else
  return ausb_libusb0_init();
#endif
}



int ausb_fini(void){
#ifdef USE_USB1
  return ausb_libusb1_fini();
#else
  return ausb_libusb0_fini();
#endif
}




