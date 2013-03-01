/****************************************************************************
 * Copyright (C) 2012 HS Coburg.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file is part of the Open eCard App.
 *
 * GNU General Public License Usage
 * This file may be used under the terms of the GNU General Public
 * License version 3.0 as published by the Free Software Foundation
 * and appearing in the file LICENSE.GPL included in the packaging of
 * this file. Please review the following information to ensure the
 * GNU General Public License version 3.0 requirements will be met:
 * http://www.gnu.org/copyleft/gpl.html.
 *
 * Other Usage
 * Alternatively, this file may be used in accordance with the terms
 * and conditions contained in a signed written agreement between
 * you and ecsec GmbH.
 *
 ***************************************************************************/

#include <jni.h>

#ifndef _Included_LibusbSocketCommunicator
#define _Included_LibusbSocketCommunicator

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT void JNICALL Java_org_openecard_android_activities_DeviceOpenActivity_startUnixSocketServer
  (JNIEnv *, jclass, jstring, jint);

JNIEXPORT jstring JNICALL Java_org_openecard_android_activities_DeviceOpenActivity_listenUnixSocketServer
  (JNIEnv *, jclass, jstring);

#ifdef __cplusplus
}
#endif

#endif
