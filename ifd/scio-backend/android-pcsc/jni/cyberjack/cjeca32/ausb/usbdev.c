
#ifdef HAVE_CONFIG_H
# include <config.h>
#endif


#include "usbdev_l.h"

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <ctype.h>
#include <assert.h>
#include <sys/stat.h>
#include <errno.h>


/* #define DEBUG_USBDEV_C */


#define RSCT_LIST_ADD(typ, sr, head) {\
  typ *curr;                \
                            \
  assert(sr);               \
                            \
  curr=*head;               \
  if (!curr) {              \
    *head=sr;               \
  }                         \
  else {                    \
    while(curr->next) {     \
      curr=curr->next;      \
    }                       \
    curr->next=sr;          \
  }\
  }


#define RSCT_LIST_DEL(typ, sr, head) {\
  typ *curr;                   \
                               \
  assert(sr);                  \
  curr=*head;                  \
  if (curr) {                  \
    if (curr==sr) {            \
      *head=curr->next;        \
    }                          \
    else {                     \
      while(curr->next!=sr) {  \
	curr=curr->next;       \
      }                        \
      if (curr)                \
	curr->next=sr->next;   \
    }                          \
  }                            \
  sr->next=0;\
  }





#ifdef HAVE_HAL
# include "usbdev_hal.c"
#elif defined(USE_USB1)
#  include "usbdev_libusb1.c"
#else
# error "Neither HAL nor USB1 found!. Please install at least either of them."
#endif




rsct_usbdev_t *rsct_usbdev_new() {
  rsct_usbdev_t *d;

  d=(rsct_usbdev_t*) malloc(sizeof(rsct_usbdev_t));
  if (d==NULL)
    return NULL;
  memset(d, 0, sizeof(rsct_usbdev_t));
  d->port=-1;
  return d;
}



rsct_usbdev_t *rsct_usbdev_dup(const rsct_usbdev_t *od) {
  rsct_usbdev_t *d;

  d=rsct_usbdev_new();
  *d=*od;
  return d;
}



void rsct_usbdev_free(rsct_usbdev_t *d) {
  if (d) {
    free(d);
  }
}



void rsct_usbdev_list_add(rsct_usbdev_t **h, rsct_usbdev_t *d) {
  RSCT_LIST_ADD(rsct_usbdev_t, d, h);
}



void rsct_usbdev_list_unlink(rsct_usbdev_t **h, rsct_usbdev_t *d) {
  RSCT_LIST_DEL(rsct_usbdev_t, d, h);
}



void rsct_usbdev_list_free(rsct_usbdev_t *d) {
  while(d) {
    rsct_usbdev_t *dNext;

    dNext=d->next;
    rsct_usbdev_free(d);
    d=dNext;
  }
}



rsct_usbdev_t *rsct_usbdev_list_findByBus(rsct_usbdev_t *head,
					  uint32_t busId,
					  uint32_t busPos) {
  rsct_usbdev_t *d;

  d=head;
  while(d) {
    if (d->busId==busId &&
	d->busPos==busPos)
      break;
    d=d->next;
  }

  return d;
}



rsct_usbdev_t *rsct_usbdev_list_findByUDI(rsct_usbdev_t *list, const char *devName) {
  rsct_usbdev_t *d;

  d=list;
  while(d) {
    if (d->halUDI && strcasecmp(d->halUDI, devName)==0)
      break;
    d=d->next;
  }

  return d;
}








int rsct_get_serial_for_port(int port,
			     const char *fname,
			     char *sbuff,
			     int blen) {
  FILE *f;

  f=fopen(fname, "r");
  if (f==NULL)
    return -1;
  else {
    char lbuf[256];
    int idx=1;

    while(!feof(f)) {
      int llen;

      lbuf[0]=0;
      if (0==fgets(lbuf, sizeof(lbuf), f)) {
	if (ferror(f)) {
	  fprintf(stderr, "RSCT: fgets: %s\n", strerror(errno));
	  fclose(f);
	  return -1;
	}
	else
          break;
      }

      /* remove possibly trailing CR */
      llen=strlen(lbuf);
      if (llen && lbuf[llen-1]=='\n')
        lbuf[llen-1]=0;

      if (idx==port) {
	if (blen<(strlen(lbuf)+1)) {
	  fprintf(stderr, "RSCT: Buffer too small for serial number\n");
	  fclose(f);
	  return -1;
	}
	strcpy(sbuff, lbuf);
	fclose(f);
        /* success */
        return 0;
      }
      idx++;
    } /* while !feof */
    fclose(f);
  }

  /* not found */
  return 1;
}



int rsct_get_port_for_serial(const char *fname,
			     const char *serial) {
  FILE *f;

  f=fopen(fname, "r");
  if (f==NULL)
    /* no port */
    return 0;
  else {
    char lbuf[256];
    int idx=1;

    while(!feof(f)) {
      int llen;

      lbuf[0]=0;
      if (NULL==fgets(lbuf, sizeof(lbuf), f)) {
	if (ferror(f)) {
	  fprintf(stderr, "RSCT: fgets: %s\n", strerror(errno));
	  fclose(f);
	  return -1;
	}
	else
          break;
      }

      /* remove possibly trailing CR */
      llen=strlen(lbuf);
      if (llen && lbuf[llen-1]=='\n')
        lbuf[llen-1]=0;

      if (strcasecmp(serial, lbuf)==0) {
	fclose(f);
        /* success */
	return idx;
      }
      idx++;
    } /* while !feof */
    fclose(f);
  }

  /* not found */
  return 0;
}



int rsct_enum_serials_with_devs(const char *fname, rsct_usbdev_t *devs) {
  int rv;
  rsct_usbdev_t *d;

  d=devs;
  while(d) {
    if (d->vendorId==0xc4b && d->serial[0]) {
      rv=rsct_get_port_for_serial(fname, d->serial);
      if (rv==0) {
	FILE *f;

	/* new device, serial number is unknown, add it */
	f=fopen(fname, "a+");
	if (f==NULL) {
	  fprintf(stderr, "RSCT: fopen(%s): %s\n",
		  fname, strerror(errno));
	  return -1;
	}
	fprintf(f, "%s\n", d->serial);
	if (fclose(f)) {
	  fprintf(stderr, "RSCT: fclose(%s): %s\n",
		  fname, strerror(errno));
	  return -1;
	}
      }
    }
    d=d->next;
  }

  return 0;
}



int rsct_enum_serials(const char *fname) {
  int rv;
  rsct_usbdev_t *devs=NULL;

  /* sample all devices */
  rv=rsct_usbdev_scan(&devs);
  if (rv) {
    rsct_usbdev_list_free(devs);
    return rv;
  }

  rv=rsct_enum_serials_with_devs(fname, devs);
  rsct_usbdev_list_free(devs);
  return rv;
}



rsct_usbdev_t *rsct_usbdev_getDevByIdx(int num) {
  int rv;
  rsct_usbdev_t *devs=NULL;
  rsct_usbdev_t *d;

  rv=rsct_usbdev_scan(&devs);
  if (rv<0) {
    fprintf(stderr, "RSCT: Error scanning USB bus\n");
    return NULL;
  }

  d=devs;
  while(d) {
    if (num--==0)
      break;
    d=d->next;
  }

  if (d)
    rsct_usbdev_list_unlink(&devs, d);

  rsct_usbdev_list_free(devs);
  return d;
}



rsct_usbdev_t *rsct_usbdev_getDevByBusPos(int busId, int devId) {
  int rv;
  rsct_usbdev_t *devs=NULL;
  rsct_usbdev_t *d;

  rv=rsct_usbdev_scan(&devs);
  if (rv<0) {
    fprintf(stderr, "RSCT: Error scanning USB bus\n");
    return NULL;
  }

  d=devs;
  while(d) {
    if (d->busId==busId && d->busPos==devId)
      break;
    d=d->next;
  }

  if (d)
    rsct_usbdev_list_unlink(&devs, d);

  rsct_usbdev_list_free(devs);
  return d;
}



rsct_usbdev_t *rsct_usbdev_getDevByName(const char *devName) {
  int rv;
  rsct_usbdev_t *devs=NULL;
  rsct_usbdev_t *d;

  rv=rsct_usbdev_scan(&devs);
  if (rv<0) {
    fprintf(stderr, "RSCT: Error scanning USB bus\n");
    return NULL;
  }

  d=devs;
  while(d) {
    if ((d->halPath && strcasecmp(d->halPath, devName)==0) ||
	(d->path && strcasecmp(d->path, devName)==0))
      break;
    d=d->next;
  }

  if (d)
    rsct_usbdev_list_unlink(&devs, d);

  rsct_usbdev_list_free(devs);
  return d;
}







