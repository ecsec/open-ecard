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

#ifndef RSCT_PLATFORM_WIN32_H
#define RSCT_PLATFORM_WIN32_H

#include <windows.h>
#include <tchar.h>
#include <winscard.h>
#include <Rpc.h>

#define uint32_t ULONG
#define uint16_t USHORT
#define uint8_t BYTE

#define int32_t LONG
#define int16_t SHORT
#define int8_t char

/*typedef ULONG uint32_t;
typedef USHORT uint16_t;
typedef BYTE uint8_t;

typedef LONG int32_t;
typedef SHORT int16_t;
typedef char int8_t;*/


#pragma warning (disable:4996)


#ifdef UNDER_CE
# define RSCT_NO_VARGS
#endif



#ifdef CJECA32_EXPORTS
# define CJECA32_API __declspec(dllexport)
#else
# define CJECA32_API __declspec(dllimport)
#endif
#define CJECA32_PRIVATE_API
#define CJECA32_PRIVATE_CLASS

#define RSCT_STDCALL _stdcall
#define strdup(m) _strdup(m)


#include "stdafx.h"

#ifndef strcasecmp
# define strcasecmp stricmp
#endif

#endif

