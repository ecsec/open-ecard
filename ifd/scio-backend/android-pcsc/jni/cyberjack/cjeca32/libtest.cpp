

#include "Platform.h"
#include "Reader.h"
#include "USBUnix.h"

#include <stdio.h>


int test1(int argc, char **argv) {
#ifdef ENABLE_NONSERIAL
  CReader *r;
  int idx;
  char *devName;
  int rv;

  if (argc<2) {
    fprintf(stderr, "Missing argument: Reader index\n");
    return 1;
  }
  idx=atoi(argv[1]);

  devName=CUSBUnix::createDeviceName(idx);
  if (devName==NULL) {
    fprintf(stderr, "Device %d not found\n", idx);
    return 2;
  }

  r=new CReader(devName);
  rv=r->Connect();
  if (rv!=CJ_SUCCESS) {
    fprintf(stderr, "Could not connect to reader (%d)\n", rv);
    return 2;
  }

  fprintf(stderr, "Reader connected.\n");
  fprintf(stderr, "Hit the ENTER key:\n");
  getchar();

  rv=r->Disonnect();
  if (rv!=CJ_SUCCESS) {
    fprintf(stderr, "Could not connect to reader (%d)\n", rv);
    return 2;
  }

  fprintf(stderr, "Reader disconnected.\n");

  delete r;
  free(devName);
#endif

  return 0;
}



int readFile(const char *fname, uint8_t *buffer) {
  FILE *f;
  uint8_t *p;
  int len;

  f=fopen(fname, "r");
  if (f==NULL)
    return -1;

  p=buffer;
  len=0;
  while(!feof(f)) {
    int rv;

    rv=fread(p, 1, 1024, f);
    if (rv==0)
      break;
    p+=rv;
    len+=rv;
  }
  fclose(f);
  return len;
}



int test2(int argc, char **argv) {
#ifdef ENABLE_NONSERIAL
  CReader *r;
  int idx;
  char *devName;
  const char *fname1;
  const char *fname2;
  uint8_t buffer1[64*1024];
  uint8_t buffer2[64*1024];
  int len1;
  int len2;
  int rv;
  uint32_t result;

  if (argc<4) {
    fprintf(stderr, "Usage:\n %s IDX NAME1 NAME2\n", argv[0]);
    return 1;
  }
  idx=atoi(argv[1]);
  fname1=argv[2];
  fname2=argv[3];

  len1=readFile(fname1, buffer1);
  if (len1<1) {
    fprintf(stderr, "Error reading file \"%s\"\n", fname1);
    return 2;
  }

  len2=readFile(fname2, buffer2);
  if (len2<1) {
    fprintf(stderr, "Error reading file \"%s\"\n", fname2);
    return 2;
  }

  devName=CUSBUnix::createDeviceName(idx);
  if (devName==NULL) {
    fprintf(stderr, "Device %d not found\n", idx);
    return 2;
  }

  r=new CReader(devName);
  rv=r->Connect();
  if (rv!=CJ_SUCCESS) {
    fprintf(stderr, "Could not connect to reader (%d)\n", rv);
    return 2;
  }

  rv=r->CtLoadModule(buffer1, len1, buffer2, len2, &result);
  if (rv!=CJ_SUCCESS) {
    fprintf(stderr, "Unable to flash reader (%d)\n", rv);
    return 2;
  }

  fprintf(stderr, "Reader flashed.\n");

  rv=r->Disonnect();
  if (rv!=CJ_SUCCESS) {
    fprintf(stderr, "Could not connect to reader (%d)\n", rv);
    return 2;
  }

  fprintf(stderr, "Reader disconnected.\n");

  delete r;
  free(devName);
#endif
  return 0;
}



int main(int argc, char **argv) {
  return test1(argc, argv);
  //return test2(argc, argv);
}




