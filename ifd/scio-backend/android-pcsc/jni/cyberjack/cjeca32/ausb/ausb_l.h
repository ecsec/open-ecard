/* USB support for the Cyberjack family of readers.
 *
 * Previous version were (C) 2004-2005 by Harald Welte <laforge@gnumonks.org>
 * This version is a rewrite (asynchronous USB is no longer needed).
 *
 * (C) 2007 Martin Preuss <martin@libchipcard.de>
 *
 * Distributed and licensed under the terms of GNU LGPL, Version 2.1
 */


#ifndef _AUSB_L_H
#define _AUSB_L_H

#define AUSB_CYBERJACK_VENDOR_ID 0xc4b
#define AUSB_MAX_TIMEOUT 600

#ifdef ENABLE_NONSERIAL

#include <inttypes.h>

#include "usbdev_l.h"



typedef struct ausb_dev_handle ausb_dev_handle;


/* virtual functions */
typedef void (*AUSB_CLOSE_FN)(ausb_dev_handle *ah);
typedef int (*AUSB_START_INTERRUPT_FN)(ausb_dev_handle *ah, int ep);
typedef int (*AUSB_STOP_INTERRUPT_FN)(ausb_dev_handle *ah);
typedef int (*AUSB_BULK_WRITE_FN)(ausb_dev_handle *ah, int ep,
				  char *bytes, int size,
				  int timeout);
typedef int (*AUSB_BULK_READ_FN)(ausb_dev_handle *ah, int ep,
				 char *bytes, int size,
				 int timeout);

typedef int (*AUSB_CLAIM_INTERFACE_FN)(ausb_dev_handle *ah, int interface);
typedef int (*AUSB_RELEASE_INTERFACE_FN)(ausb_dev_handle *ah, int interface);
typedef int (*AUSB_SET_CONFIGURATION_FN)(ausb_dev_handle *ah, int interface);

typedef int (*AUSB_RESET_FN)(ausb_dev_handle *ah);
typedef int (*AUSB_HALT_FN)(ausb_dev_handle *ah, unsigned int ep);
typedef int (*AUSB_CLEAR_HALT_FN)(ausb_dev_handle *ah, unsigned int ep);
typedef int (*AUSB_RESET_ENDPOINT_FN)(ausb_dev_handle *ah, unsigned int ep);
typedef int (*AUSB_RESET_PIPE)(ausb_dev_handle *ah, int ep);


typedef int (*AUSB_GET_KERNEL_DRIVER_NAME)(ausb_dev_handle *ah, int interface, char *name,
					   unsigned int namelen);
typedef int (*AUSB_DETACH_KERNEL_DRIVER_FN)(ausb_dev_handle *dev, int interface);
typedef int (*AUSB_REATTACH_KERNEL_DRIVER_FN)(ausb_dev_handle *dev, int interface);


typedef void (*AUSB_LOG_FN)(ausb_dev_handle *ah,
			    const char *text,
			    const void *pData, uint32_t ulDataLen);


typedef void (*AUSB_CALLBACK)(const uint8_t *data,
			      uint32_t dlength,
			      void *userdata);


/* structures */
struct ausb_callback {
  AUSB_CALLBACK handler;
  void *userdata;
};


struct ausb_dev_handle {
  rsct_usbdev_t device;
  struct ausb_callback cb;
  void *extraData;
  uint16_t pid;

  AUSB_CLOSE_FN closeFn;
  AUSB_START_INTERRUPT_FN startInterruptFn;
  AUSB_STOP_INTERRUPT_FN stopInterruptFn;
  AUSB_BULK_WRITE_FN bulkWriteFn;
  AUSB_BULK_READ_FN bulkReadFn;

  AUSB_CLAIM_INTERFACE_FN claimInterfaceFn;
  AUSB_RELEASE_INTERFACE_FN releaseInterfaceFn;
  AUSB_SET_CONFIGURATION_FN setConfigurationFn;

  AUSB_RESET_FN resetFn;
  AUSB_CLEAR_HALT_FN clearHaltFn;
  AUSB_RESET_ENDPOINT_FN resetEndpointFn;
  AUSB_RESET_PIPE resetPipeFn;

  AUSB_GET_KERNEL_DRIVER_NAME getKernelDriverNameFn;
  AUSB_DETACH_KERNEL_DRIVER_FN detachKernelDriverFn;
  AUSB_REATTACH_KERNEL_DRIVER_FN reattachKernelDriverFn;

};



#ifdef __cplusplus
extern "C" {
#endif



/** @name Functions used commonly by all implementations
 */
/*@{*/

/* intitialization */ 
int ausb_init(void);


/**
 * Register a callback which is called as soon as an URB request is
 * finished.
 * @param ah ausb handle obtained via @ref ausb_open
 * @param callback callback function to be used. This function receives a
 *  pointer to the received interrupt data and caller-specified user data
 * @param userdata userdata to be passed to the callback function
 */
int ausb_register_callback(ausb_dev_handle *ah,
			   AUSB_CALLBACK callback,
			   void *userdata);

int ausb_claim_interface(ausb_dev_handle *ah, int interface);
int ausb_release_interface(ausb_dev_handle *ah, int interface);
int ausb_set_configuration(ausb_dev_handle *dev, int configuration);

int ausb_get_kernel_driver_name(ausb_dev_handle *ah, int interface, char *name,
				unsigned int namelen);
int ausb_detach_kernel_driver(ausb_dev_handle *dev, int interface);
int ausb_reattach_kernel_driver(ausb_dev_handle *dev, int interface);

int ausb_reset(ausb_dev_handle *ah);
int ausb_clear_halt(ausb_dev_handle *ah, unsigned int ep);
int ausb_reset_endpoint(ausb_dev_handle *ah, unsigned int ep);
int ausb_reset_pipe(ausb_dev_handle *ah, int ep);

void ausb_set_log_fn(AUSB_LOG_FN fn);

void ausb_log(ausb_dev_handle *ah, const char *text,
	      const void *pData, uint32_t ulDataLen);

/*@}*/

#ifdef USE_USB1
int ausb11_extend(ausb_dev_handle *ah);
int ausb31_extend(ausb_dev_handle *ah);
#else
int ausb1_extend(ausb_dev_handle *ah);
int ausb3_extend(ausb_dev_handle *ah);
#endif


/** @name Functions which are implemented differently for
 * different configurations.
 */
/*@{*/
/**
 * Open the given USB device. This creates the necessary
 * ausb handle which must be free'd when it is no longer
 * needed by calling @ref ausb_close.
 * @return ausb handle to be used by other functions in this group
 * @param dev usb device object obtained from libusb
 *
 */
ausb_dev_handle *ausb_open(rsct_usbdev_t *dev, int t);


int ausb_close(ausb_dev_handle *ah);

int ausb_start_interrupt(ausb_dev_handle *ah, int ep);
int ausb_stop_interrupt(ausb_dev_handle *ah);

int ausb_bulk_write(ausb_dev_handle *ah, int ep, char *bytes, int size,
		    int timeout);
int ausb_bulk_read(ausb_dev_handle *ah, int ep, char *bytes, int size, 
		   int timeout);

/*@}*/

#ifdef __cplusplus
}
#endif

#endif

#endif /* _AUSB_H */


