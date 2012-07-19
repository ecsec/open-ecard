/***************************************************************************
    begin       : Mon Jun 14 2010
    copyright   : (C) 2010 by Martin Preuss
    email       : martin@libchipcard.de

 ***************************************************************************
 *                                                                         *
 *   This library is free software; you can redistribute it and/or         *
 *   modify it under the terms of the GNU Lesser General Public            *
 *   License as published by the Free Software Foundation; either          *
 *   version 2.1 of the License, or (at your option) any later version.    *
 *                                                                         *
 *   This library is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU     *
 *   Lesser General Public License for more details.                       *
 *                                                                         *
 *   You should have received a copy of the GNU Lesser General Public      *
 *   License along with this library; if not, write to the Free Software   *
 *   Foundation, Inc., 59 Temple Place, Suite 330, Boston,                 *
 *   MA  02111-1307  USA                                                   *
 *                                                                         *
 ***************************************************************************/

#include "Platform.h"


#include "USBUnix.h"
#include "SerialUnix.h"
#include "config_l.h"




CBaseCommunication *rsct_platform_create_com(const char *deviceName, CReader *reader) {
  CBaseCommunication *com=NULL;

  // we allocated 10 bytex more in CReader::CReaderConstructor
  // so we can modify this const char here
  char *deviceNameDup = (char *) deviceName;
  if (strcasestr(deviceName, ":libudev:")!=NULL) {
    int idVendor, idProduct, bus_number, device_address = 0;
    sscanf(deviceName,"usb:%04x/%04x:libudev:0:/dev/bus/usb/%d/%d",&idVendor, &idProduct, &bus_number, &device_address);
    snprintf(deviceNameDup,strlen(deviceNameDup)+10,"usb:%04x/%04x:libusb:%03d:%03d",idVendor, idProduct, bus_number, device_address);
  }
  if (strcasestr(deviceName, ":libusb-1.0:")!=NULL) {
    int idVendor, idProduct, bus_number, device_address, interface = 0;
    sscanf(deviceName,"usb:%04x/%04x:libusb-1.0:%d:%d:%d",&idVendor, &idProduct, &bus_number, &device_address, &interface);
    snprintf(deviceNameDup,strlen(deviceNameDup)+10,"usb:%04x/%04x:libusb:%03d:%03d",idVendor, idProduct, bus_number, device_address);
  }
  if (strcasestr(deviceName, ":libusb:")!=NULL ||
      strstr(deviceName, ":libhal:")!=NULL)
# ifdef ENABLE_NONSERIAL
    com=new CUSBUnix(deviceName, reader);
# else
  /* libusb and libhal not supported for serial devices */
  return CJ_ERR_OPENING_DEVICE;
# endif
  else
    com=new CSerialUnix(deviceName, reader);

  return com;
}



const char *rsct_get_package_version(void) {
  const char *s;

  s=rsct_config_get_var("PackageVersion");
  if (s && *s)
    return s;

  /* for now */
  return "3.99.5";
}



uint32_t rsct_get_environment(const char *name, uint32_t defval) {
  const char *s;

  s=rsct_config_get_var(name);
  if (s && *s) {
    unsigned long int v;

    if (sscanf(s, "%lu", &v)==1) {
      return uint32_t(v);
    }
    else {
      fprintf(stderr, "CYBERJACK: Environment variable \"%s\" is not an integer\n", name);
    }
  }

  return defval;
}

