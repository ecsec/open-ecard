
#ifdef HAVE_CONFIG_H
# include <config.h>
#endif


#ifdef USE_USB1


#include "ausb_l.h"
#include "ausb_libusb1_l.h"

#include <stdio.h>
#include <string.h>



static libusb_context *ausb_libusb1_context=NULL;


int ausb_libusb1_init(void){
  if (ausb_libusb1_context==NULL) {
    int rv;

    rv=libusb_init(ausb_libusb1_context);
    if (rv) {
      fprintf(stderr, "RSCT: Error on libusb_init(): %d\n", rv);
      ausb_libusb1_context=NULL;
      return -1;
    }
  }

  return 0;
}



int ausb_libusb1_fini(void){
  if (ausb_libusb1_context!=NULL) {
    libusb_exit(ausb_libusb1_context);
    ausb_libusb1_context=NULL;
  }

  return 0;
}



libusb_device *ausb_libusb1_get_usbdev(const rsct_usbdev_t *d) {
  if (ausb_libusb1_init())
    return NULL;
  else {
    libusb_device **list;
    size_t cnt=libusb_get_device_list(ausb_libusb1_context, &list);
    size_t i;
    libusb_device *dev=NULL;

    for (i=0; i<cnt; i++) {
      dev=list[i];

      if (libusb_get_bus_number(dev)==d->busId &&
	  libusb_get_device_address(dev)==d->busPos)
	break;

    }

    if (dev)
      libusb_ref_device(dev);
    /* free devices and unref them */
    libusb_free_device_list(list, 1);
    return dev;
  }
}



int ausb_libusb1_handle_events(){
  if (ausb_libusb1_context!=NULL)
    return libusb_handle_events(ausb_libusb1_context);
  else {
    fprintf(stderr, "RSCT: No USB context.}n");
    return LIBUSB_ERROR_OTHER;
  }
}


#endif

