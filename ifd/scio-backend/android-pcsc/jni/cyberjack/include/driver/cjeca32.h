
#ifndef CJECA32_H
#define CJECA32_H

#include "Platform.h"


#define CJ_SUCCESS                  0
#define CJ_ERR_OPENING_DEVICE      -1
#define CJ_ERR_WRITE_DEVICE        -2
#define CJ_ERR_DEVICE_LOST         -3
#define CJ_ERR_WRONG_ANSWER        -4
#define CJ_ERR_SEQ                 -5
#define CJ_ERR_WRONG_LENGTH        -6
#define CJ_ERR_NO_ICC              -7
#define CJ_ERR_OPEN_ICC            -8
#define CJ_ERR_PARITY              -9
#define CJ_ERR_TIMEOUT            -10
#define CJ_ERR_LEN                -11
#define CJ_ERR_RBUFFER_TO_SMALL   -12
#define CJ_ERR_PROT               -13
#define CJ_ERR_NO_ACTIVE_ICC      -14
#define CJ_ERR_SIGN               -15
#define CJ_ERR_WRONG_SIZE         -16
#define CJ_ERR_PIN_TIMEOUT        -17
#define CJ_ERR_PIN_CANCELED       -18
#define CJ_ERR_PIN_DIFFERENT      -19
#define CJ_ERR_FIRMWARE_OLD       -20
#define CJ_NOT_UPDATABLE          -21
#define CJ_ERR_NO_SIGN            -22
#define CJ_ERR_WRONG_PARAMETER    -23
#define CJ_ERR_INTERNAL_BUFFER_OVERFLOW -24
#define CJ_ERR_CHECK_RESULT		 -25
#define CJ_ERR_DATA_CORRUPT       -26
#define CJ_ERR_CONDITION_OF_USE   -27
#define CJ_ERR_PIN_EXTENDED       -28
#define CJ_ERR_CONNECT_TIMEOUT    -29

#define RSCT_MODULE_MASK_STATUS					0x00000001
#define RSCT_MODULE_MASK_ID						0x00000002
#define RSCT_MODULE_MASK_VARIANT					0x00000004
#define RSCT_MODULE_MASK_BASE_ADDR				0x00000008
#define RSCT_MODULE_MASK_CODE_SIZE			   0x00000010
#define RSCT_MODULE_MASK_VERSION			      0x00000020
#define RSCT_MODULE_MASK_REVISION		      0x00000040
#define RSCT_MODULE_MASK_REQUIRED_VERSION		0x00000080
#define RSCT_MODULE_MASK_REQUIRED_REVISION	0x00000100
#define RSCT_MODULE_MASK_HEAP_SIZE				0x00000200
#define RSCT_MODULE_MASK_DESCRIPTION			0x00000400
#define RSCT_MODULE_MASK_DATE						0x00000800

typedef struct _cj_ModuleInfo
{
	uint32_t SizeOfStruct;
	uint32_t ContentsMask;
   uint32_t Status;
   uint32_t ID;
   uint32_t Variant;
   uint32_t BaseAddr;
   uint32_t CodeSize;
   uint32_t Version;
   uint32_t Revision;
   uint32_t RequieredKernelVersion;
   uint32_t RequieredKernelRevision;
   uint32_t HeapSize;
   int8_t Description[17];
   int8_t Date[12];
   int8_t Time[6];
}cj_ModuleInfo;

#define RSCT_READER_MASK_PID					0x00000001
#define RSCT_READER_MASK_HARDWARE				0x00000002
#define RSCT_READER_MASK_VERSION				0x00000004
#define RSCT_READER_MASK_HARDWARE_VERSION		0x00000008
#define RSCT_READER_MASK_FLASH_SIZE				0x00000010
#define RSCT_READER_MASK_HEAP_SIZE				0x00000020
#define RSCT_READER_MASK_SERIALNUMBER			0x00000040
#define RSCT_READER_MASK_VENDOR_STRING			0x00000080
#define RSCT_READER_MASK_PRODUCT_STRING			0x00000100
#define RSCT_READER_MASK_PRODUCTION_DATE		0x00000200
#define RSCT_READER_MASK_TEST_DATE				0x00000400
#define RSCT_READER_MASK_COMMISSIONING_DATE		0x00000800
#define RSCT_READER_MASK_COM_TYPE				0x00001000
#define RSCT_READER_MASK_PORT_ID	            0x00002000
#define RSCT_READER_MASK_IFD_BRIDGE				0x00004000
#define RSCT_READER_MASK_HW_STRING				0x00008000

#define RSCT_READER_HARDWARE_MASK_ICC1				0x00000001
#define RSCT_READER_HARDWARE_MASK_ICC2				0x00000002
#define RSCT_READER_HARDWARE_MASK_ICC3				0x00000004
#define RSCT_READER_HARDWARE_MASK_ICC4				0x00000008
#define RSCT_READER_HARDWARE_MASK_ICC5				0x00000010
#define RSCT_READER_HARDWARE_MASK_ICC6				0x00000020
#define RSCT_READER_HARDWARE_MASK_ICC7				0x00000040
#define RSCT_READER_HARDWARE_MASK_ICC8				0x00000080
#define RSCT_READER_HARDWARE_MASK_KEYPAD			0x00000100
#define RSCT_READER_HARDWARE_MASK_DISPLAY			0x00000200
#define RSCT_READER_HARDWARE_MASK_BIOMETRIC			0x00000400
#define RSCT_READER_HARDWARE_MASK_BUZZER			0x00000800
#define RSCT_READER_HARDWARE_MASK_DISPLAY_ONOFF		0x00001000
#define RSCT_READER_HARDWARE_MASK_RFID				0x00002000
#define RSCT_READER_HARDWARE_MASK_PACE				0x00004000
#define RSCT_READER_HARDWARE_MASK_UPDATEABLE 		0x00010000
#define RSCT_READER_HARDWARE_MASK_MODULES			0x00020000
#define RSCT_READER_HARDWARE_BACKLIGHT				0x00040000



typedef struct _tKeyInfo
{
	uint8_t KNr;
	uint8_t Version;
}tKeyInfo;


typedef struct _tSecoderInfo
{
    uint32_t FixedModuleID;  
    uint8_t FixedModuleVersion;  
    uint8_t FixedModuleRevision;
}tSecoderInfo;

typedef struct _ReaderInfo
{
	uint32_t SizeOfStruct;
	uint32_t ContentsMask;
	uint32_t PID;
	uint32_t HardwareMask;
	uint32_t Version;
	uint32_t HardwareVersion;
	uint32_t FlashSize;
	uint32_t HeapSize;
	tKeyInfo Keys[2];
	int8_t SeriaNumber[11];
	int8_t VendorString[128];
	int8_t ProductString[128];
	int8_t ProductionDate[11];
	int8_t ProductionTime[6];
	int8_t TestDate[11];
	int8_t TestTime[6];
	int8_t CommissioningDate[11];
	int8_t CommissioningTime[6];
   int8_t CommunicationString[4];
	uint32_t PortID;
	int8_t IFDNameOfIfdBridgeDevice[256];
	int8_t HardwareString[128];	
	tSecoderInfo Info[1];
}cj_ReaderInfo;

typedef void* ctxPtr;
typedef void (RSCT_STDCALL *fctKeyIntCallback)(ctxPtr Context, uint8_t Key);
typedef void (RSCT_STDCALL *fctChangeIntCallback)(ctxPtr Context, uint8_t State);


typedef enum _EContrast{ContrastVeryLow,ContrastLow,ContrastMedium,ContrastHigh,ContrastVeryHigh}EContrast;

typedef enum _EBacklight{BacklightOff,BacklightVeryLow,BacklightLow,BacklightMedium,BacklightHigh,BacklightVeryHigh}EBacklight;

typedef int CJ_RESULT;
typedef uint32_t RSCT_IFD_RESULT;

typedef enum _EApduNorm{NORM_PCSC,NORM_ISO,NORM_EMV}EApduNorm;

#ifndef CJPCSC_VEN_IOCTRL_ESCAPE
# define CJPCSC_VEN_IOCTRL_ESCAPE	      SCARD_CTL_CODE(3103)
#endif


extern CJECA32_API int ncjeca32;

CJECA32_API int fncjeca32(void);


#ifdef __cplusplus
// Diese Klasse wird aus cjeca32.dll exportiert.
class CJECA32_API Ccjeca32 {
public:
	Ccjeca32(void);
	// TODO: Hier die Methoden hinzufÅgen.
};
#endif


#ifdef CJECA32_EXPORTS
#ifdef _EXP_CTAPI

#define CTAPI_RETURN CJECA32_API char _stdcall


#ifdef __cplusplus

extern "C"
{
#endif
   CTAPI_RETURN CT_init(WORD,WORD);
   CTAPI_RETURN CT_data(WORD,uint8_t *dad,uint8_t *sad, WORD cmd_len, const uint8_t *cmd, WORD *response_len, uint8_t *response);
   CTAPI_RETURN CT_close(WORD);
#ifdef __cplusplus
}
#endif
#endif
#endif



#endif
