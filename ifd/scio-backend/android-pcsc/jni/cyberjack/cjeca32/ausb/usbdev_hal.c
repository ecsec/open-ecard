


#include <hal/libhal.h>
#include <dbus/dbus.h>


struct RSCT_HAL_CONTEXT {
  DBusError dbus_error;
  DBusConnection *dbus_conn;
  LibHalContext *ctx;
};

static struct RSCT_HAL_CONTEXT *global_hal_context=NULL;



int rsct_usbdev_init() {
  if (global_hal_context==NULL) {
    struct RSCT_HAL_CONTEXT *uc;

    uc=(struct RSCT_HAL_CONTEXT*) malloc(sizeof(struct RSCT_HAL_CONTEXT));
    if (uc==NULL) {
      fprintf(stderr, "RSCT: Memory full at rsct_usbdev_init\n");
      return -1;
    }
    dbus_error_init(&(uc->dbus_error));
    uc->dbus_conn=dbus_bus_get (DBUS_BUS_SYSTEM, &(uc->dbus_error));
    if (dbus_error_is_set(&(uc->dbus_error))) {
      fprintf(stderr, "RSCT: Could not connect to system bus [%s]\n",
	      uc->dbus_error.message);
      free(uc);
      return -1;
    }

    uc->ctx=libhal_ctx_new();
    if (uc->ctx==NULL) {
      fprintf(stderr, "RSCT: Could not create HAL context\n");
      free(uc);
      return -1;
    }

    libhal_ctx_set_dbus_connection(uc->ctx, uc->dbus_conn);
    global_hal_context=uc;
  }

  return 0;
}



void rsct_usbdev_fini() {
  if (global_hal_context) {
    dbus_error_free(&(global_hal_context->dbus_error));
    if (global_hal_context->dbus_conn) {
      dbus_connection_unref(global_hal_context->dbus_conn);
      global_hal_context->dbus_conn = NULL;
    }
    /*libhal_ctx_shutdown(ctx, NULL);*/
    libhal_ctx_free(global_hal_context->ctx);

    free(global_hal_context);
    global_hal_context=NULL;
  }
}



static int rsct_usbdev_scan_nonserial(char **devices, int i_devices,
				      rsct_usbdev_t **usbdev_list) {
  int i;

  for (i=0; i<i_devices; i++) {
    const char *udi=devices[i];

    if (libhal_device_exists(global_hal_context->ctx, udi, &(global_hal_context->dbus_error))) {
      char *busType;

      busType=libhal_device_get_property_string(global_hal_context->ctx, udi, "info.subsystem", NULL);
      if (busType && (strcasecmp(busType, "usb")!=0)) {
	libhal_free_string(busType);
	busType=NULL; /* non-USB devices are handled below */
      }

      if (busType==NULL)
	busType=libhal_device_get_property_string(global_hal_context->ctx, udi, "info.bus", NULL);
      if (busType) {
	if (strcasecmp(busType, "usb")==0) {
	  /* USB device, look for LibUSB info */
	  if (libhal_device_property_exists(global_hal_context->ctx, udi, "usb.bus_number", NULL) &&
	      libhal_device_property_exists(global_hal_context->ctx, udi, "usb.linux.device_number", NULL)){
	    int busId;
	    int busPos;
	    int vendorId;
	    int productId;
	    char pbuff[256];
	    struct stat st;
	    int havePath=0;

	    busId=libhal_device_get_property_int(global_hal_context->ctx,
						 udi,
						 "usb.bus_number",
						 NULL);
	    busPos=libhal_device_get_property_int(global_hal_context->ctx,
						  udi,
						  "usb.linux.device_number",
						  NULL);
	    vendorId=libhal_device_get_property_int(global_hal_context->ctx,
						    udi,
						    "usb.vendor_id",
						    NULL);
	    productId=libhal_device_get_property_int(global_hal_context->ctx,
						     udi,
						     "usb.product_id",
						     NULL);

	    if (vendorId==0xc4b && rsct_usbdev_list_findByBus(*usbdev_list, busId, busPos)==NULL) {
	      rsct_usbdev_t *d;
	      char *serial=NULL;
	      char *productName=NULL;

	      d=rsct_usbdev_new();

	      d->busId=busId;
	      d->busPos=busPos;
	      d->vendorId=vendorId;
	      d->productId=productId;

	      snprintf(d->halPath, sizeof(d->halPath)-1,
		       "usb:%04x/%04x:libhal:%s",
		       d->vendorId,
		       d->productId,
		       udi);
	      d->halPath[sizeof(d->halPath)-1]=0;

	      /* set HAL UDI */
	      strncpy(d->halUDI, udi, sizeof(d->halUDI)-1);
	      d->halUDI[sizeof(d->halUDI)-1]=0;

	      /* determine path for LibUSB */
	      snprintf(pbuff, sizeof(pbuff)-1,
		       "/dev/bus/usb/%03d/%03d",
		       busId, busPos);
	      pbuff[sizeof(pbuff)-1]=0;
	      if (stat(pbuff, &st)==0) {
		havePath=1;
	      }
	      else {
		snprintf(pbuff, sizeof(pbuff)-1,
			 "/proc/bus/usb/%03d/%03d",
			 busId, busPos);
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

	      serial=libhal_device_get_property_string(global_hal_context->ctx,
						       udi,
						       "usb.serial",
						       NULL);
	      if (serial) {
		strncpy(d->serial, serial, sizeof(d->serial)-1);
		d->serial[sizeof(d->serial)-1]=0;
		libhal_free_string(serial);
	      }

	      /* get product name from parent.
	       * Please note: This udi refers to the *_ifX HAL device, and it's product name
	       * is the product name of the interface ("USB Vendor Specific Interface") rather than
	       * that of the device itself.
	       * Therefore we must access the parent in order to find the product name of the device.
	       */
	      if (1) {
		char *parent_udi;

		/* ttyUSB device, get USB info from parent */
		parent_udi=libhal_device_get_property_string(global_hal_context->ctx,
							     udi,
							     "info.parent",
							     NULL);
		if (parent_udi) {
		  productName=libhal_device_get_property_string(global_hal_context->ctx,
								parent_udi,
								"usb_device.product",
								NULL);
		  libhal_free_string(parent_udi);
		}
	      }

	      if (productName) {
		strncpy(d->productName, productName, sizeof(d->productName)-1);
		d->productName[sizeof(d->productName)-1]=0;
		libhal_free_string(productName);
	      }

	      /* all set, add device */
	      rsct_usbdev_list_add(usbdev_list, d);
	    }
	  }
	} /* if USB */
	libhal_free_string(busType);
      } /* if bus type */
    } /* if device exists */
  } /* for */

  return 0;
}



static int rsct_usbdev_scan_serial(char **devices, int i_devices,
				   rsct_usbdev_t **usbdev_list) {
  int i;

  for (i=0; i<i_devices; i++) {
    const char *udi=devices[i];

    if (libhal_device_exists(global_hal_context->ctx, udi, &(global_hal_context->dbus_error))) {
      char *busType;

      busType=libhal_device_get_property_string(global_hal_context->ctx, udi, "info.subsystem", NULL);
      if (busType==NULL)
	busType=libhal_device_get_property_string(global_hal_context->ctx, udi, "info.bus", NULL);
      if (busType) {
	if (strcasecmp(busType, "tty")==0) {
	  char *parent_udi;
  
	  /* ttyUSB device, get USB info from parent */
	  parent_udi=libhal_device_get_property_string(global_hal_context->ctx,
						       udi,
						       "info.parent",
						       NULL);
	  if (parent_udi) {
	    rsct_usbdev_t *d;
	    char *path;

            /* find device entry for parent */
	    d=rsct_usbdev_list_findByUDI(*usbdev_list, parent_udi);
	    if (d) {
              int port;

	      port=libhal_device_get_property_int(global_hal_context->ctx,
						  udi,
						  "serial.port",
						  NULL);
	      d->port=port;

	      path=libhal_device_get_property_string(global_hal_context->ctx,
						     udi,
						     "serial.device",
						     NULL);
	      if (path) {
		strncpy(d->deviceNodePath, path, sizeof(d->deviceNodePath)-1);
		d->deviceNodePath[sizeof(d->deviceNodePath)-1]=0;
		libhal_free_string(path);
	      }
	    }
	    libhal_free_string(parent_udi);
	  }
	  else {
	    fprintf(stderr, "RSCT: Parent for serial device not found\n");
	  }
	} /* if tty */
	libhal_free_string(busType);
      } /* if bus type */
    } /* if device exists */
  } /* for */

  return 0;
}



int rsct_usbdev_scan(rsct_usbdev_t **usbdev_list) {
  int rv;

  rv=rsct_usbdev_init();
  if (rv)
    return -1;
  else {
    char **devices;
    int i_devices;
    int rv;

    devices=libhal_get_all_devices(global_hal_context->ctx, &i_devices, &(global_hal_context->dbus_error));
    if (devices==NULL) {
      fprintf(stderr, "RSCT: HAL not running: %s\n", global_hal_context->dbus_error.message);
      return -1;
    }
    if (i_devices<1) {
      fprintf(stderr, "RSCT:  HAL returned an empty device list, this can't be right...\n");
    }

    rv=rsct_usbdev_scan_nonserial(devices, i_devices, usbdev_list);
    if (rv==0)
      rv=rsct_usbdev_scan_serial(devices, i_devices, usbdev_list);

    libhal_free_string_array(devices);
    return rv;
  }

  return 0;
}



