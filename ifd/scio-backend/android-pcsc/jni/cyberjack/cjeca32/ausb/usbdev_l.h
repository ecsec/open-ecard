

#ifndef USBDEV_L_H
#define USBDEV_L_H

#include <inttypes.h>



typedef struct rsct_usbdev_t rsct_usbdev_t;
struct rsct_usbdev_t {
  rsct_usbdev_t *next;
  char path[256];
  char halPath[256];
  char usbPath[256];
  char serial[128];
  uint32_t busId;
  uint32_t busPos;
  uint32_t vendorId;
  uint32_t productId;
  char productName[256];
  char halUDI[256];
  char deviceNodePath[256];
  int port;
};


#ifdef __cplusplus
extern "C" {
#endif


int rsct_usbdev_init();
void rsct_usbdev_fini();

rsct_usbdev_t *rsct_usbdev_new();
rsct_usbdev_t *rsct_usbdev_dup(const rsct_usbdev_t *d);
void rsct_usbdev_free(rsct_usbdev_t *d);

void rsct_usbdev_list_add(rsct_usbdev_t **head, rsct_usbdev_t *d);
void rsct_usbdev_list_unlink(rsct_usbdev_t **head, rsct_usbdev_t *d);
void rsct_usbdev_list_free(rsct_usbdev_t *d);

rsct_usbdev_t *rsct_usbdev_list_findByBus(rsct_usbdev_t *list,
					  uint32_t busId,
					  uint32_t busPos);
rsct_usbdev_t *rsct_usbdev_list_findByUDI(rsct_usbdev_t *list, const char *devName);


rsct_usbdev_t *rsct_usbdev_getDevByIdx(int num);
rsct_usbdev_t *rsct_usbdev_getDevByBusPos(int busId, int devId);
rsct_usbdev_t *rsct_usbdev_getDevByName(const char *devName);



int rsct_usbdev_scan(rsct_usbdev_t **devList);

int rsct_enum_serials(const char *fname);
int rsct_enum_serials_with_devs(const char *fname, rsct_usbdev_t *devs);

int rsct_get_serial_for_port(int port,
			     const char *fname,
			     char *sbuff,
			     int blen);
int rsct_get_port_for_serial(const char *fname,
			     const char *serial);


#ifdef __cplusplus
}
#endif


#endif
