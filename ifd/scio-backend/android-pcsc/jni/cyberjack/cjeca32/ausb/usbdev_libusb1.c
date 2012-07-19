#ifdef USE_USB1

#include <libusb.h>

static struct libusb_context *ausb_libusb1_context=NULL;

int rsct_usbdev_init(void){
  if (ausb_libusb1_context==NULL) {
    int rv;
    rv=libusb_init(&ausb_libusb1_context);
    if (rv) {
      fprintf(stderr, "RSCT: Error on libusb_init(): %d\n", rv);
      ausb_libusb1_context=NULL;
      return -1;
    }
  }
  return 0;
}

void rsct_usbdev_fini(void) {
  libusb_exit(ausb_libusb1_context);
  ausb_libusb1_context=NULL;
}


int rsct_usbdev_scan(rsct_usbdev_t **usbdev_list) {
  if (rsct_usbdev_init())
    return -1;
  else {
    libusb_device **list;
    size_t cnt=libusb_get_device_list(ausb_libusb1_context, &list);
    size_t i;
  
    for (i=0; i<cnt; i++) {
      libusb_device *dev;
      struct libusb_device_descriptor descr;
      int rv;
  
      dev=list[i];
      rv=libusb_get_device_descriptor(dev, &descr);
      if (rv==0) {
	if (descr.idVendor==0xc4b) {
	  rsct_usbdev_t *d;
	  char pbuff[256];
	  struct stat st;
	  int havePath=0;
  
	  d=rsct_usbdev_new();
	  d->busId=libusb_get_bus_number(dev);
	  d->busPos=libusb_get_device_address(dev);
	  d->vendorId=descr.idVendor;
	  d->productId=descr.idProduct;
  
	  /* determine path for LibUSB */
	  snprintf(pbuff, sizeof(pbuff)-1,
		   "/dev/bus/usb/%03d/%03d",
		   d->busId, d->busPos);
	  pbuff[sizeof(pbuff)-1]=0;
	  if (stat(pbuff, &st)==0) {
	    havePath=1;
	  }
	  else {
	    snprintf(pbuff, sizeof(pbuff)-1,
		     "/proc/bus/usb/%03d/%03d",
		     d->busId, d->busPos);
	    pbuff[sizeof(pbuff)-1]=0;
	    if (stat(pbuff, &st)==0) {
	      havePath=1;
	    }
	  }
  
	  if (havePath) {
	    strncpy(d->usbPath, pbuff, sizeof(d->usbPath)-1);
	    d->usbPath[sizeof(d->usbPath)-1]=0;
  
	    strncpy(d->deviceNodePath, pbuff, sizeof(d->deviceNodePath)-1);
	    d->deviceNodePath[sizeof(d->deviceNodePath)-1]=0;
	  }
  
	  /* generate path for CTAPI/IFD */
	  snprintf(d->path, sizeof(d->path)-1,
		   "usb:%04x/%04x:libusb:%03d:%03d",
		   d->vendorId,
		   d->productId,
		   d->busId,
		   d->busPos);
  
	  if (1) {
	    libusb_device_handle *dh;
  
	    rv=libusb_open(dev, &dh);
	    if (rv) {
	      fprintf(stderr, "RSCT: Error on libusb_open: %d\n", rv);
	    }
	    else {
	      /* get product string */
	      rv=libusb_get_string_descriptor_ascii(dh, descr.iProduct,
						    (unsigned char*) (d->productName),
						    sizeof(d->productName)-1);
	      if (rv<0) {
		fprintf(stderr, "RSCT: Error on libusb_get_string_descriptor_ascii: %d\n", rv);
		d->productName[0]=0;
	      }
	      else {
		d->productName[rv]=0;
	      }
  
	      if (descr.idProduct>=0x300) {
		/* get serial number for newer devices */
		rv=libusb_get_string_descriptor_ascii(dh, descr.iSerialNumber,
						      (unsigned char*) (d->serial),
						      sizeof(d->serial)-1);
		if (rv<0) {
		  fprintf(stderr, "RSCT: Error on libusb_get_string_descriptor_ascii: %d\n", rv);
		  d->serial[0]=0;
		}
		else {
		  d->serial[rv]=0;
		}
	      }
  
	      libusb_close(dh);
	    }
	  }
  
	  /* all set, add device */
	  rsct_usbdev_list_add(usbdev_list, d);
	}
      }
      else {
	fprintf(stderr, "RSCT: Error on libusb_get_device_descriptor: %d\n", rv);
      }
    }
  
    libusb_free_device_list(list, 1);
    rsct_usbdev_fini();
    return 0;
  }
}

#endif
