

#ifndef AUSB_LIBUSB1_L_H
#define AUSB_LIBUSB1_L_H


#ifdef USE_USB1

#include <libusb.h>

#include "usbdev_l.h"


int ausb_libusb1_init(void);
int ausb_libusb1_fini(void);

libusb_device *ausb_libusb1_get_usbdev(const rsct_usbdev_t *d);
int ausb_libusb1_handle_events();


#endif


#endif

