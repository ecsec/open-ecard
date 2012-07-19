#ifndef CCID_H
#define CCID_H

#define PC_TO_RDR_ICCPOWERON		0x62
#define PC_TO_RDR_ICCPOWEROFF		0x63
#define PC_TO_RDR_GETSLOTSTATUS		0x65
#define PC_TO_RDR_XFRBLOCK  		0x6F
#define PC_TO_RDR_GETPARAMETERS		0x6C
#define PC_TO_RDR_RESETPARAMETERS	0x6D
#define PC_TO_RDR_SETPARAMETERS		0x61
#define PC_TO_RDR_ESCAPE    		0x6B
#define PC_TO_RDR_ICCCLOCK    		0x6E
#define PC_TO_RDR_TAPDU             0x6A
#define PC_TO_RDR_SECURE            0x69
#define PC_TO_RDR_MECHANICAL        0x71
#define PC_TO_RDR_ABORT             0x72
#define PC_TO_RDR_SETDATARATEANDCLOCKFREQUENCY    0x73


#define LOOPBACK                    0x11

#define RDR_TO_PC_DATABLOCK			0x80
#define RDR_TO_PC_SLOTSTATUS		0x81
#define RDR_TO_PC_PARAMETERS		0x82
#define RDR_TO_PC_ESCAPE			0x83
#define RDR_TO_PC_DATARATEANDCLOCKFREQUENCY			0x84
#define RDR_TO_PC_STATUSCLOCK       0x85
#define RDR_TO_PC_NOTIFYSLOTCHANGE  0x50 
#define RDR_TO_PC_HARWAREERROR      0x51 
#define RDR_TO_PC_KEYEVENT          0x40

#define CCID_ESCAPE_INPUT           0x00
#define CCID_ESCAPE_UPDATE          0x01
#define CCID_ESCAPE_VERIFY          0x02
#define CCID_ESCAPE_STATUS          0x03
#define CCID_ESCAPE_UPDATE_START    0x04
#define CCID_ESCAPE_GET_INFO        0x05
#define CCID_ESCAPE_SET_DATE_TIME   0x06
#define CCID_ESCAPE_SET_SERNUMBER   0x07
//#define CCID_ESCAPE_VERIFYDATA      0x08
#define CCID_ESCAPE_GET_SECODERINFO 0x09
#define CCID_ESCAPE_GET_STACKSIGNCOUNTER 0x0c

#define CCID_ESCAPE_MODULE_DELETE  0x10
// #define CCID_ESCAPE_MODULE_DEFRAG  0x11
#define CCID_ESCAPE_MODULE_ENUM    0x12
#define CCID_ESCAPE_MODULE_INFO    0x13
#define CCID_ESCAPE_MODULE_REACTIVATE 0x14
#define CCID_ESCAPE_MODULE_DEACTIVATE 0x15
#define CCID_ESCAPE_MODULE_DELALL 0x16
#define CCID_ESCAPE_MODULE_SET_SILENT_MODE 0x17
#define CCID_ESCAPE_MODULE_GET_SILENT_MODE 0x18
#define CCID_ESCAPE_MODULE_SET_FLASH_MASK 0x19


#define CCID_ESCAPE_GET_KEYINFO   0x20
#define CCID_ESCAPE_UPDATE_KEY    0x21
#define CCID_ESCAPE_VERIFY_KEY    0x22
#define CCID_ESCAPE_SELF_TEST     0x23
#define CCID_ESCAPE_SHOW_AUTH     0x24

#define CCID_ESCAPE_SET_MODULESTORE_INFO 0x26
#define CCID_ESCAPE_GET_MODULESTORE_INFO 0x27

#define CCID_ESCAPE_DSP_CONTRAST 0x30
#define	CCID_ESCAPE_DSP_BACKLIGHT 0x33

#define CCID_ESCAPE_SELECT_SM_MODULE 0x50

#define CCID_ESCAPE_DO_PACE 0xf0




#define CMD_ABORTED 				0xff
#define ICC_MUTE 					0xfe
#define XFR_PARITY_ERROR            0xfd
#define XFR_OVERRUN					0xfc
#define HW_ERROR					0xfb
#define EXT_ERROR					0xc0

#define BAD_ATR_TS					0xf8
#define BAD_ATR_TCK					0xf7
#define ICC_PROTOCOL_NOT_SUPPORTED  0xf6
#define ICC_CLASS_NOT_SUPPORTED     0xf5
#define PROCEDURE_BYTE_CONFLICT     0xf4
#define DEACTIVATED_PROTOCOL        0xf3
#define BUSY_WITH_AUTOSEQUENCE      0xf2

#define PIN_TIMEOUT					0xf0
#define PIN_CANCELED				0xef
#define PIN_DIFFERENT				0xee

#define CMD_SLOT_BUSY               0xe0
 

typedef struct _cjeca_DateTime
{
   uint8_t ProductionDate[11];
   uint8_t Reserved1;
   uint8_t ProductionTime[6];
   uint8_t Reserved2;
   uint8_t Reserved3;
}cjeca_DateTime;

#define PRODUCTION_DATE 0
#define TEST_DATE 1
#define FIRST_USE_DATE 2

typedef struct _cjeca_ReaderConst
{
   uint8_t Config;
   uint8_t Flags;
   uint16_t MaskOption;  
   uint32_t HardwareVersion;
   cjeca_DateTime dtDate[3];
   uint8_t Seriennummer[20];
	uint32_t IsForSale;
}cjeca_ReaderConst;

typedef struct _cjeca_Info
{
   uint32_t ActiveApplication;
   uint8_t KernelVersion;
   uint8_t KernelRevision;   
   uint8_t Reserved1;   
   uint8_t Reserved2;   
   cjeca_ReaderConst ReaderConst;
}cjeca_Info;

typedef struct _cjeca_ModuleInfo
{
   uint32_t Status;
   uint32_t ModuleID;
   uint32_t ModuleBaseAddr;
   uint32_t ModuleCodeSize;
   cjeca_DateTime DateTime;
   uint8_t Version;
   uint8_t Revision;
   uint8_t RequieredKernelVersion;
   uint8_t RequieredKernelRevision;
   uint8_t GlobalHeapSize;
   uint8_t Variante;
   uint8_t Reserved1;
   uint8_t Reserved2;
   int8_t Description[16];
}cjeca_ModuleInfo;


#pragma pack(1)

typedef struct _SMSelect
{
  unsigned long ModuleID;
  unsigned long Count;
}tSMSelect;

typedef struct _CCID_Message
{
	uint8_t bMessageType;
	uint32_t dwLength;
	uint8_t bSlot;
	uint8_t bSeq;
	union _Header
	{
		uint8_t abRFU[3];
		struct _iccPowerOn
		{
			uint8_t bPowerSelect;
			uint8_t abRFU[2];
		}iccPowerOn;
		struct _XfrBlock
		{
			uint8_t bBWI;
			uint16_t wLevelParameter;
		}XfrBlock; 
		struct _SetParameters
		{
			uint8_t bProtocolNum;
			uint8_t abRFU[2];
		}SetParameters; 
		struct _iccClock
		{
			uint8_t bClockCommand;
			uint8_t abRFU[2];
		}iccClock; 
		struct _T0APDU
		{
			uint8_t bChanges;
			uint8_t bClassGetResponse;
			uint8_t bClassEnvelope;
		}T0APDU; 
		struct _Secure
		{
			uint8_t bBWI;
			uint16_t wLevelParameter;
		}Secure; 
		struct _Mechanical
		{
			uint8_t bFunction;
			uint8_t abRFU[2];
		}Mechanical; 
	}Header;
	union _Data
	{
		uint8_t abData[5120];
		union _SetParameters
		{ 
			struct _T0
			{
				uint8_t bmFindexDindex;
				uint8_t bmTCCKST0;
				uint8_t bGuardTimeT0;
				uint8_t bWaitingIntegerT0;
				uint8_t bClockStop;
			}T0;
			struct _T1
			{
				uint8_t bmFindexDindex;
				uint8_t bmTCCKST1;
				uint8_t bGuardTimeT1;
				uint8_t bWaitingIntegerT1;
				uint8_t bClockStop;
				uint8_t bIFSC;
				uint8_t bNadValue;
			}T1;
         struct _Sync
         {
            unsigned char AddrByteCount;         
            unsigned short PageSize;
         }Sync;
		}SetParameters;
		struct _Secure
		{
			uint8_t bPINOperation;
			uint8_t bTimeOut;
			uint8_t bmFormatString;
			uint8_t bmPINBlockString;
			uint8_t bmPINLengthFormat;
			union _Data
			{
				struct _Verify
				{
					uint16_t wPINMaxExtraDigit;
					uint8_t bEntryValidationCondition;
					uint8_t bNumberMessage;
					uint16_t wLangId;
					uint8_t bMsgIndex;
					uint8_t bTeoPrologue[3];
					uint8_t abData[245];
				}Verify;
				struct _Modify
				{
					uint8_t bInsertionOffsetOld;
					uint8_t bInsertionOffsetNew;
					uint16_t wPINMaxExtraDigit;
					uint8_t bConfirmPIN;
					uint8_t bEntryValidationCondition;
					uint8_t bNumberMessage;
					uint16_t wLangId;
					uint8_t bMsgIndex1;
					uint8_t bMsgIndex2;
					uint8_t bMsgIndex3;
					uint8_t bTeoPrologue[3];
					uint8_t abData[240];
				}Modify;
				struct _Next
				{
					uint8_t bTeoPrologue[3];
				}Next;
			}Data;
		}Secure; 
		struct _SetDataRateAndClockFrequency
		{
			uint32_t dwClockFrequency;
			uint32_t dwDataRate;
		}SetDataRateAndClockFrequency; 
		struct _Escape
		{
			union _Reader
			{
				struct _EC30
				{
					uint32_t dwApplication;
					uint16_t wFunction;
					union _Data
					{
						struct _UpdateStart
						{
							uint8_t ModuleHeader[256];
						}UpdateStart;
						struct _UpdateData
						{
							uint16_t bLength;
							uint8_t Data[256];
						}UpdateData;
						struct _UpdateVerify
						{
							uint32_t len;
							uint8_t Sign[768];
						}UpdateVerify;
						struct _ModuleDelete
						{
							uint32_t Application;
						}ModuleDelete;


						struct _SetDateTime
						{
							uint8_t Nr;
							cjeca_DateTime dtDate;
						}SetDateTime;
						struct _SetSerNumber
						{
							uint8_t SerNumber[20];
						}SetSerNumber;
						struct _Input
						{
							uint8_t Timeout;
						}Input;
						uint32_t ModuleID;
						struct _UpdateKey
						{
							uint32_t len;
							uint8_t Key[800];
						}UpdateKey;
						struct _VerifyKey
						{
							uint32_t len;
							uint8_t Key[800];
						}VerifyKey;
						uint8_t abDate[1008];
					}Data;
				}EC30;
			}Reader;
		}Escape; 
	}Data;
}CCID_Message;

typedef struct _CCID_Response
{
   uint8_t bMessageType;
   uint32_t dwLength;
   uint8_t bSlot;
   uint8_t bSeq;
   uint8_t bStatus;
   uint8_t bError;
   union _Header
   {
      uint8_t bRFU;
      uint8_t bChainParameter;
      uint8_t bClockStatus;
      uint8_t bProtocolNum;
      
   }Header;
   union _Data
   {
      uint8_t abData[5120];
      union _Parameters
      { 
         struct _T0
         {
            uint8_t bmFindexDindex;
            uint8_t bmTCCKST0;
            uint8_t GuardTimeT0;
            uint8_t bWaitingIntegerT0;
            uint8_t bClockStop;
         }T0;
         struct _T1
         {
            uint8_t bmFindexDindex;
            uint8_t bmTCCKST1;
            uint8_t GuardTimeT1;
            uint8_t bWaitingIntegerT1;
            uint8_t bClockStop;
            uint8_t bIFSC;
            uint8_t bNadValue;
         }T1;
      }Parameters;
      struct _DataRateAndClockFrequency
      {
         uint32_t dwClockFrequency;
         uint32_t dwDataRate;
      }DataRateAndClockFrequency; 
      struct _Escape
      {
         uint32_t Result;
         union _Function
         {
            cjeca_ModuleInfo ModuleInfo;
            struct _ModuleEnummeration
            {
              uint32_t ModuleID[32];
            }ModuleEnummeration;
            cjeca_Info ReaderInfo;
            uint8_t abData[1010];
         }Function;
      }Escape; 
   }Data;
   
}CCID_Response;
   	

typedef struct _CCID_Interrupt
{
   uint8_t bMessageType;
   union _Data
   {
      struct _NotifySlotChange
      {
         uint8_t bmSlotICCState;
      }NotifySlotChange;
      struct _HardwareError
      {
         uint8_t bSlot;
         uint8_t bSeq;
         uint8_t bHardwareErrorCode;
      }HardwareError;
      struct _KeyEvent
      {
         uint8_t KeyStatus;
      }KeyEvent;
      
   }Data;
}CCID_Interrupt;

typedef union _CCID_transfer
{
   CCID_Message Message;
   CCID_Response Response;
   CCID_Interrupt Interrupt;
}CCID_transfer;
#pragma pack()

#endif

