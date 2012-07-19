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

#ifndef RSCT_PLATFORM_H
#define RSCT_PLATFORM_H


#ifdef _WINDOWS
# include "Platform_win32.h"
#else
# include "Platform_unix.h"
#endif

#include "Debug.h"

#ifndef CJPCSC_VEN_IOCTRL_ESCAPE
# define CJPCSC_VEN_IOCTRL_ESCAPE             SCARD_CTL_CODE(3103)
#endif

#define CJPCSC_VEN_IOCTRL_VERIFY_PIN_DIRECT   SCARD_CTL_CODE(3506)
#define CJPCSC_VEN_IOCTRL_MODIFY_PIN_DIRECT   SCARD_CTL_CODE(3507)

#define CJPCSC_VEN_IOCTRL_MCT_READERDIRECT    SCARD_CTL_CODE(3508)
#define CJPCSC_VEN_IOCTRL_MCT_READERUNIVERSAL SCARD_CTL_CODE(3509)

#define CJPCSC_VEN_IOCTRL_EXECUTE_PACE        SCARD_CTL_CODE(3532)
#define CJPCSC_VEN_IOCTRL_SET_NORM            SCARD_CTL_CODE(3154)


#ifdef __cplusplus

class CBaseCommunication;
class CReader;


CBaseCommunication *rsct_platform_create_com(const char *deviceName, CReader *reader);

const char *rsct_get_package_version(void);


uint32_t rsct_get_environment(const char *name, uint32_t defval);



#endif






#endif

