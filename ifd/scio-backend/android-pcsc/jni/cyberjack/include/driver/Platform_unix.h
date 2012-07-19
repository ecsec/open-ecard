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

#ifndef RSCT_PLATFORM_UNIX_H
#define RSCT_PLATFORM_UNIX_H

#ifdef HAVE_CONFIG_H
# include <config.h>
#endif


// global headers special to Linux and/or Mac OS X
#include <inttypes.h>
#include <arpa/inet.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <ctype.h>
#include <string.h>

#ifndef OS_DARWIN
# include <malloc.h>
#endif

#ifdef __cplusplus
extern "C" {
#endif
# include <PCSC/winscard.h>
# include <PCSC/reader.h>
#ifdef __cplusplus
}
#endif



#ifndef SCARD_POWER_DOWN
# define SCARD_POWER_DOWN 0x0000
#endif

#ifndef SCARD_COLD_RESET
# define SCARD_COLD_RESET 0x0001
#endif

#ifndef SCARD_WARM_RESET
# define SCARD_WARM_RESET 0x0002
#endif

#ifndef SCARD_PROTOCOL_DEFAULT
# define SCARD_PROTOCOL_DEFAULT 0x80000000
#endif

#ifndef SCARD_PROTOCOL_OPTIMAL
# define SCARD_PROTOCOL_OPTIMAL 0x00000000
#endif




#define RSCT_STDCALL


#define max(a, b) ((a>b)?a:b)

#define Sleep(ms) usleep(ms*1000)


#ifdef BUILDING_CYBERJACK
# ifdef GCC_WITH_VISIBILITY_ATTRIBUTE
#  define CJECA32_API           __attribute__((visibility("default")))
#  define CJECA32_PRIVATE_API   __attribute__((visibility("hidden")))
#  define CJECA32_PRIVATE_CLASS __attribute__((visibility("hidden")))
# else
#  define CJECA32_API
#  define CJECA32_PRIVATE_API
#  define CJECA32_PRIVATE_CLASS
# endif
#else
# define CJECA32_API
# define CJECA32_PRIVATE_API
# define CJECA32_PRIVATE_CLASS
#endif
#
#ifdef GCC_WITH_VISIBILITY_ATTRIBUTE
# define CJECA32_EXPORT __attribute__((visibility("default")))
#else
# define CJECA32_EXPORT
#endif



#include "ntstatus.h"





#endif

