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

#ifndef CYBERJACKCONFIG_H
#define CYBERJACKCONFIG_H


#define CT_FLAGS_DEBUG_GENERIC   0x00000001
#define CT_FLAGS_DEBUG_READER    0x00000002

#define CT_FLAGS_DEBUG_CTAPI     0x00000004
#define CT_FLAGS_DEBUG_AUSB      0x00000008

#define CT_FLAGS_DEBUG_CJPPA     0x00000010
#define CT_FLAGS_DEBUG_ECOM      0x00000020

#define CT_FLAGS_DEBUG_TRANSFER  0x00000040

#define CT_FLAGS_DEBUG_USB       0x00000080
#define CT_FLAGS_DEBUG_IFD       0x00000100

#define CT_FLAGS_DEBUG_ECA       0x00000200

#define CT_FLAGS_DEBUG_CUSTOM_LOGGING   0x00000400


#define CT_FLAGS_NO_BEEP         0x00010000
#define CT_FLAGS_ECOM_KERNEL_OLD 0x00020000
#define CT_FLAGS_ALLOW_INPUT     0x00040000

#define CT_FLAGS_BEEP_NO_X11     0x00080000

#define CT_FLAGS_RESET_BEFORE    0x00100000
#define CT_FLAGS_ECOM_KERNEL     0x00200000


#define CT_FLAGS_DEFAULT        0


typedef struct CYBERJACK_CONFIG CYBERJACK_CONFIG;


#include "Platform.h"


#ifdef __cplusplus
extern "C" {
#endif

CJECA32_API int rsct_config_init();
CJECA32_API void rsct_config_fini();

CJECA32_API int rsct_config_save();

CJECA32_API unsigned int rsct_config_get_flags();
CJECA32_API void rsct_config_set_flags(unsigned int i);
CJECA32_API unsigned int rsct_config_get_debug_output_level();
CJECA32_API const char *rsct_config_get_debug_filename();
CJECA32_API const char *rsct_config_get_serial_filename();
CJECA32_API void rsct_config_set_serial_filename(const char *s);

CJECA32_API void rsct_config_set_var(const char *name, const char *val);
CJECA32_API const char *rsct_config_get_var(const char *name);


#ifdef __cplusplus
}
#endif


#endif


