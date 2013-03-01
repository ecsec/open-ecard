/***************************************************************************
    begin       : Mon Aug 14 2006
    copyright   : (C) 2006 by Martin Preuss
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

#ifndef CYBERJACKAPI_H
#define CYBERJACKAPI_H

#include <inttypes.h>
#include <cjeca32.h>
#include <stdarg.h>


#define CT_MAX_DEVICES 256

#define CT_INVALID_CTN (0xffff)

#define CT_API_RV_OK		0
#define CT_API_RV_ERR_INVALID	-1
#define CT_API_RV_ERR_CT	-8
#define CT_API_RV_ERR_TRANS	-10
#define CT_API_RV_ERR_MEMORY	-11
#define CT_API_RV_ERR_HOST	-127
#define CT_API_RV_ERR_HTSI	-128

#define CT_API_AD_HOST		2
#define CT_API_AD_REMOTE	5

#define CT_API_AD_CT		1

/* 14 (0x0e) is normally used for chipcard slot 14, but no cyberjack has that many
 * slots, so we can safely use this value here */
#define CT_API_AD_DRIVER        14

#define CJ_CTAPI_MAX_LENC	(4+1+255+1)
#define CJ_CTAPI_MAX_LENR	(256+2)


#define CJ_KEY_DIGIT 1
#define CJ_KEY_CLEAR 2

#define CT_LOGFILE_LIMIT (10*1024*1024)



/* some special APDUs specific to cyberJack ECA and newer */
#define CJ_SPECIAL_CLA                0x30

/** Update the keys stored on the reader.
 * <table>
 *   <tr><th>CLA</th><th>INS</th><th>P1</th><th>P2</th><th>DATA</th><th>Response</th></tr>
 *
 *   <tr>
 *     <td>0x30</td>
 *     <td>0x10</td>
 *     <td>Bit 7=1: Last data block; Bit 6=1: abort; Bit 5=1: first data block; Bits 4-0: 0</th>
 *     <td>0x00</th>
 *     <td>Key data (max 255 bytes per command)</td>
 *     <td>SW1, SW2</td>
 *   </tr>
 * </table>
 */
#define CJ_SPECIAL_INS_KEYUPDATE      0x10

/** Delete all modules on the reader.
 * <table>
 *   <tr><th>CLA</th><th>INS</th><th>P1</th><th>P2</th><th>DATA</th><th>Response</th></tr>
 *
 *   <tr>
 *     <td>0x30</td>
 *     <td>0x20</td>
 *     <td>0x00</th>
 *     <td>0x00</th>
 *     <td>none</td>
 *     <td>SW1, SW2</td>
 *   </tr>
 * </table>
 */
#define CJ_SPECIAL_INS_DELETEALLMODS  0x20

/** Upload a module/kernel to the driver for later flashing.
 * <table>
 *   <tr><th>CLA</th><th>INS</th><th>P1</th><th>P2</th><th>DATA</th><th>Response</th></tr>
 *
 *   <tr>
 *     <td>0x30</td>
 *     <td>0x21</td>
 *     <td>Bit 6=1: abort; Bit 5=1: first data block; Bits 7, 4-0: 0</th>
 *     <td>0x00</th>
 *     <td>Module data (max 255 bytes per command)</td>
 *     <td>SW1, SW2</td>
 *   </tr>
 * </table>
 */
#define CJ_SPECIAL_INS_UPLOADMOD      0x21

/** Upload a module/kernel signature to the driver for later flashing.
 * <table>
 *   <tr><th>CLA</th><th>INS</th><th>P1</th><th>P2</th><th>DATA</th><th>Response</th></tr>
 *
 *   <tr>
 *     <td>0x30</td>
 *     <td>0x22</td>
 *     <td>Bit 6=1: abort; Bit 5=1: first data block; Bits 7, 4-0: 0</th>
 *     <td>0x00</th>
 *     <td>Module data (max 255 bytes per command)</td>
 *     <td>SW1, SW2</td>
 *   </tr>
 * </table>
 */
#define CJ_SPECIAL_INS_UPLOADSIG      0x22

/** Flash the kernel/module uploaded via @ref CJ_SPECIAL_INS_UPLOADSIG and @ref CJ_SPECIAL_INS_UPLOADMOD.
 * <table>
 *   <tr><th>CLA</th><th>INS</th><th>P1</th><th>P2</th><th>DATA</th><th>Response</th></tr>
 *
 *   <tr>
 *     <td>0x30</td>
 *     <td>0x23</td>
 *     <td>0x00</th>
 *     <td>0x00</th>
 *     <td>none</td>
 *     <td>SW1, SW2</td>
 *   </tr>
 * </table>
 */
#define CJ_SPECIAL_INS_UPLOADFLASH    0x23

/** Get the module info of the module previously uploaded via @ref CJ_SPECIAL_INS_UPLOADMOD.
 * <table>
 *   <tr><th>CLA</th><th>INS</th><th>P1</th><th>P2</th><th>DATA</th><th>Response</th></tr>
 *
 *   <tr>
 *     <td>0x30</td>
 *     <td>0x24</td>
 *     <td>0x00</th>
 *     <td>0x00</th>
 *     <td>none</td>
 *     <td>Module info (cj_ModuleInfo) SW1, SW2</td>
 *   </tr>
 * </table>
 */
#define CJ_SPECIAL_INS_UPLOADINFO     0x24


#define CJ_SPECIAL_INS_SHOWAUTH       0x30

/** Get the number of available module infos.
 * <table>
 *   <tr><th>CLA</th><th>INS</th><th>P1</th><th>P2</th><th>DATA</th><th>Response</th></tr>
 *
 *   <tr>
 *     <td>0x30</td>
 *     <td>0x31</td>
 *     <td>0x00</th>
 *     <td>0x00</th>
 *     <td>none</td>
 *     <td>Number of modules (1 byte)  SW1, SW2</td>
 *   </tr>
 * </table>
 */
#define CJ_SPECIAL_INS_GETMODCOUNT    0x31

/** Get a module info.
 * <table>
 *   <tr><th>CLA</th><th>INS</th><th>P1</th><th>P2</th><th>DATA</th><th>Response</th></tr>
 *
 *   <tr>
 *     <td>0x30</td>
 *     <td>0x32</td>
 *     <td>Index (0 for first module)</th>
 *     <td>0x00</th>
 *     <td>none</td>
 *     <td>Module info (cj_ModuleInfo) SW1, SW2</td>
 *   </tr>
 * </table>
 */
#define CJ_SPECIAL_INS_GETMODINFO     0x32

/** Get a reader info.
 * <table>
 *   <tr><th>CLA</th><th>INS</th><th>P1</th><th>P2</th><th>DATA</th><th>Response</th></tr>
 *
 *   <tr>
 *     <td>0x30</td>
 *     <td>0x40</td>
 *     <td>0x00</th>
 *     <td>0x00</th>
 *     <td>none</td>
 *     <td>Reader info (cj_ReaderInfo) SW1, SW2</td>
 *   </tr>
 * </table>
 */
#define CJ_SPECIAL_INS_GETREADERINFO  0x40


#ifdef __cplusplus
extern "C" {
#endif

enum RSCT_KEY {
  RSCT_Key_Unknown=0,
  RSCT_Key_Digit,
  RSCT_Key_C,
  RSCT_Key_CLR,
  RSCT_Key_At,
  RSCT_Key_Ok,
  RSCT_Key_Up,
  RSCT_Key_Down
};



/**
 * @return <0 on error, 0 if reader should beep, 1 if this has been handled
 * by the callback function
 */
typedef int (*CT_KEY_CB)(uint16_t ctn,
                         int key,
			 void *user_data);

CJECA32_API
int8_t CT_init(uint16_t ctn, uint16_t pn);

CJECA32_API
int8_t CT_data(uint16_t ctn,
	       uint8_t *dad,
	       uint8_t *sad,
	       uint16_t cmd_len,
	       const uint8_t *cmd,
	       uint16_t *response_len,
	       uint8_t *response);

CJECA32_API
int8_t CT_close(uint16_t ctn);


CJECA32_API
int8_t rsct_setkeycb(uint16_t ctn, CT_KEY_CB cb, void *user_data);

CJECA32_API
int8_t rsct_init_name(uint16_t ctn, const char *devName);

CJECA32_API
void rsct_version(uint8_t *vmajor,
		  uint8_t *vminor,
		  uint8_t *vpatchlevel,
		  uint16_t *vbuild);


CJECA32_API
void rsct_log(uint16_t ctn,
	      unsigned int what,
	      const char *file, int line, const char *function,
	      const char *format, ...);
CJECA32_API
void rsct_vlog(uint16_t ctn,
	      unsigned int what,
	      const char *file, int line, const char *function,
	      const char *format, va_list args);

CJECA32_API
void rsct_log_bytes(uint16_t ctn,
		    unsigned int what,
		    const char *file, int line,
		    const char *function,
		    const char *hdr,
		    int datalen, const uint8_t *data);


CJECA32_API
uint32_t rsct_ifd_ioctl(uint16_t ctn,
			uint32_t IoCtrlCode,
			uint8_t *Input,
			uint32_t InputLength,
			uint8_t *Output,
			uint32_t *OutputLength);


#ifdef __cplusplus
}
#endif


#endif


