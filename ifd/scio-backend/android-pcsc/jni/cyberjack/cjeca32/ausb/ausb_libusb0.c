
//#ifdef HAVE_CONFIG_H
# include <config.h>
//#endif


#ifndef USE_USB1


#include "ausb_l.h"
#include "ausb_libusb0_l.h"

#include <stdio.h>
#include <string.h>



static int ausb_libusb0_was_init=0;



int ausb_libusb0_init(void){
  if (!ausb_libusb0_was_init) {
    usb_init();
    ausb_libusb0_was_init=1;
  }

  usb_find_busses();
  usb_find_devices();
  return 0;
}



int ausb_libusb0_fini(void) {
  return 0;
}



struct usb_device *ausb_libusb0_get_usbdev(const rsct_usbdev_t *d) {
  struct usb_bus *busses, *bus;
  struct usb_device *dev;
  char tname[PATH_MAX+1];
  char filename[PATH_MAX+1];
  int nlen;

  ausb_libusb0_init();

  snprintf(tname, PATH_MAX, "%03d/%03d",
	   d->busId, d->busPos);
  nlen=strlen(tname);

  busses = usb_get_busses();

  for (bus = busses; bus; bus = bus->next) {
    for (dev = bus->devices; dev; dev = dev->next) {
      int flen;

      strncpy(filename, bus->dirname, PATH_MAX );
      strncat(filename, "/", PATH_MAX );
      strncat(filename, dev->filename, PATH_MAX );
      flen=strlen(filename);
      if (flen>=nlen) {
	if (strncmp(filename+(flen-nlen), tname, nlen)==0) {
	  if (dev->descriptor.idVendor == AUSB_CYBERJACK_VENDOR_ID)
	    return dev;
	  else {
	    fprintf(stderr, "RSCT: Device at %s is not a cyberjack\n", filename);
	    return NULL;
	  }
	}
      }
    }
  }
  return NULL;
}


#endif

