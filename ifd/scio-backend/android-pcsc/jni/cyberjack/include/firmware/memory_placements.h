#ifndef memory_placements_H
#define memory_placements_H


#define FLASH_SIZE 0x00040000L

typedef struct
{
  unsigned short KeyNr;
  unsigned short KeyVersion;
  unsigned char Key[256];
}tSign;

#define MODULE_HEADER_ID 0x1077aa01L

typedef enum{FILTER_NEUTRAL,FILTER_ALLOW,FILTER_BLOCK,FILTER_OVERLOADED}FILTER_RESULT;
typedef enum{INTEREST_RESULT_NO,INTEREST_RESULT_YES}INTEREST_RESULT;
typedef enum{CARD_STATE_UNKNOWN,CARD_STATE_NO,CARD_STATE_INSERTED,CARD_STATE_POWER_PRCCESS,CARD_STATE_POWERED,CARD_STATE_SPECIFIC,USER_BREAK}ISO_POWER;

#define APPL_ATTR_ENTRY               1
#define APPL_ATTR_HIGH_SEC            2
#define APPL_ATTR_AUTO_LED            4
#define APPL_ATTR_LIB                 8

#define APPL_FLAG_DEFAULT_ON_RESET    1
#define APPL_FLAG_AUTO_SELECT         2
#define APPL_FLAG_NO_KEY_EVENT        4
#define APPL_FLAG_UNCONNECTED         8
#define APPL_FLAG_INSTALLER          16 
#define APPL_FLAG_ONE_TIME_ANY_TIME  32

typedef void (*fctPowerUp)(void);
typedef void (*fctSwitchTo)(void);
typedef INTEREST_RESULT (*fctPostATRParser)(unsigned char *ATR,unsigned int ATRLen);
typedef FILTER_RESULT (*fctPreICCCmdFilter)(unsigned long ActiveModule,unsigned char *APDU,unsigned int APDULen);
typedef int (*fctPreICCSMCmdFilter)(unsigned char *APDU,unsigned int *APDULen,unsigned int MaxAPDULen);
typedef void (*fctPostCmdParser)(const unsigned char *APDU,unsigned int APDULen,const unsigned char *RAPDU,unsigned int RAPDULen);
typedef FILTER_RESULT (*fctPreIFDCmdFilter)(unsigned long ActiveModule,unsigned long ModuleID,unsigned short ModuleFkt,unsigned char *Input,unsigned int InputLen,unsigned short *OverloadFctNo);
typedef FILTER_RESULT (*fctAllowSwitch)(unsigned long ModuleID);
typedef void (*fctApplicationProcedure)(unsigned char *Input,unsigned int InputLen,unsigned char *Output,unsigned short *OutputLen,unsigned char *Error,unsigned short *ErrorLen);
typedef FILTER_RESULT (*fctCheckCoExistens)(unsigned int ModuleCount,unsigned long *IDs);
typedef int (*fctCheckCmdAndGetTexte)(unsigned char *cmd,unsigned int len,const char **Texte,unsigned int modi,const unsigned char **Symbol);
typedef void (*fctRecognizePINResult)(unsigned short SW1SW2);
typedef void (*fctRecognizeCardState)(ISO_POWER State);
typedef const char *(*fctGetApplicationStdText)(void);
typedef int (*fctCheckMessageParams)(const unsigned char *cmd,unsigned int len,unsigned int modi,unsigned short wLangId,unsigned char bMessageNumber,const unsigned char *bMessageIndex,unsigned char bConfirmPIN);
typedef unsigned char (*fctGetExtApplicationError)(unsigned char error,unsigned int *ResponseLength,unsigned char *Response);
typedef struct
{
  fctApplicationProcedure Procedure;
  unsigned long Attributes;
}tApplicationProcedure;

typedef struct
{
#ifdef AT91_SAM3  
  unsigned long Status;
  unsigned long OffsetCrc16;
  unsigned long ModuleHeaderID;
  unsigned long ModuleBaseAddr;
#else  
  unsigned long OffsetCrc16;
  unsigned long Status;
  unsigned long ModuleBaseAddr;
  unsigned long ModuleHeaderID;
#endif  
  unsigned long ModuleID;
  unsigned long ModuleCodeSize;
  unsigned char Version;
  unsigned char Revision;
  unsigned char Variante;
  unsigned char RequieredKernelVersion;
  unsigned char RequieredKernelRevision;
  unsigned char GlobalHeapSize;
  unsigned char ExtraPages;
  unsigned char cReserved2;
  unsigned char DateOfCompilation[12];
  unsigned char TimeOfCompilation[12];
  char Description[16];
  fctPowerUp PowerUp;
  fctPostATRParser PostATRParser;
  fctPreICCCmdFilter PreICCCmdFilter;
  fctPreIFDCmdFilter PreIFDCmdFilter;
  fctPostCmdParser PostCmdParser;
  fctAllowSwitch AllowSwitch;
  const tApplicationProcedure *ApplicationProcedures;
  unsigned long ApplicationProcedureCount;
  unsigned long ApplicationFlags;
  fctSwitchTo SwitchTo;
  fctCheckCoExistens CheckCoExistens;
  fctCheckCmdAndGetTexte CheckCmdAndGetTexte;
  fctRecognizePINResult RecognizePINResult;
  fctRecognizeCardState RecognizeCardState;
  fctGetApplicationStdText GetApplicationStdText;
  unsigned long PINTimeout;
  unsigned long MinPINTimeout;
  fctCheckMessageParams CheckMessageParams;
  fctGetExtApplicationError GetExtApplicationError;
  unsigned long EncryptionKey;
  fctPreICCSMCmdFilter PreBuildSM;
  fctPreICCSMCmdFilter PostBuildSM;
  unsigned long Reserved[24];
}tModuleHeader;


#ifdef AT91_SAM3
#define APPLICATION_START ((void *)0x00100000)
#define APPLICATION_DATA  0x20000000
#define APPLICATION_DATA_SIZE  0x00001734
#define PRNG_DATA          0x0009ED00
#define PRNG_SIZE          0x00000300
#define KEY_DATA           0x0009F000
#define GLOB_DATA          0x0009FA00
#define GLOB_SIZE          0x00000100
#define PRNG_CONST         0x0009FB00
#define PRNG_CONST_SIZE    0x00000100
#define SAM_CONST          0x0009FBD0
#define SAM_CONST_SIZE    0x00000030
#define CONF_DATA          0x0009FC00
#define CONF_SIZE          0x00000100
#define KERNEL_HEADER_ADDR  0x00081000
#define KERNEL_EXPORTS_ADDR 0x00081200
#define KERNEL_UPDATE_ADDR 0x00100000
#define APPLICATION_HEADER_ADDR 0x00100000
#ifndef CJECA_MAX_MODULES
#define CJECA_MAX_MODULES 1
#endif
#else
#ifndef ECOM_F
#define APPLICATION_START ((void *)0x00202800)
#define APPLICATION_DATA  0x00201C00
#define APPLICATION_DATA_SIZE  0x00000C00
#define KEY_DATA           0x00000600
#define GLOB_DATA          0x00000F00
#define CONF_DATA          0x00001000
#define KERNEL_HEADER_ADDR 0x00001200
#define KERNEL_EXPORTS_ADDR 0x00001400
#define KERNEL_UPDATE_ADDR 0x00020000
#define APPLICATION_HEADER_ADDR 0x00202700
#ifndef CJECA_MAX_MODULES
#ifdef ECOM_S
   #define CJECA_MAX_MODULES 1
#else
   #define CJECA_MAX_MODULES 16
#endif
#endif
#else
#define APPLICATION_DATA  0x00001C00L
#define APPLICATION_DATA_SIZE  0x00000C00L
#define KEY_DATA           0xff8000L
#define GLOB_DATA          0xffc000L
#define CONF_DATA          0xffbc00L
#define KERNEL_HEADER_ADDR 0xfc0000L
#define KERNEL_EXPORTS_ADDR 0xfcfe00L
#define KERNEL_UPDATE_ADDR 0xfd0000L
//#define APPLICATION_HEADER_ADDR 0x00202700
#define __root
#define CJECA_MAX_MODULES 5
#endif
#endif


#ifndef AT91_SAM3
#ifndef PRODUKTION_KERNEL
#ifdef  RFID
#define KERNEL_VERSION    0x37
#else
#define KERNEL_VERSION    0x30
#endif
#define KERNEL_REVISION   75
#else
#define KERNEL_VERSION    0x28
#define KERNEL_REVISION   1
#endif
#else 
#ifndef PRODUKTION_KERNEL
#define  KERNEL_VERSION 0x12
#ifdef RF_STANDARD
#define  KERNEL_REVISION   7
#else
#define  KERNEL_REVISION   6
#endif
#else
#define  KERNEL_VERSION 0x01
#define  KERNEL_REVISION   0
#endif
#endif

#include "eca_defines.h"


#endif
