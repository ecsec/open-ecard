

#ifndef AUSB_LIBUSB0_L_H
#define AUSB_LIBUSB0_L_H


#ifndef USE_USB1

#include <usb.h>

#include "usbdev_l.h"


int ausb_libusb0_init(void);
int ausb_libusb0_fini(void);

struct usb_device *ausb_libusb0_get_usbdev(const rsct_usbdev_t *d);


#endif


#endif

