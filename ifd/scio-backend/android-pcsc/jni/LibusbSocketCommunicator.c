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

#include "LibusbSocketCommunicator.h"
#include <fcntl.h>
#include <errno.h>
#include <sys/socket.h>
#include <pthread.h>
#include <unistd.h>
#include <arpa/inet.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/time.h>
#include <netinet/in.h>
#include <fcntl.h>
#include <stdio.h>
#include <android/log.h>

#define LOGI(...) /*((void)__android_log_print(ANDROID_LOG_INFO, "LibusbSocketCommunicator.c", __VA_ARGS__))*/
#define MAXLINE (1024)
#define CONTROLLEN  CMSG_LEN(sizeof(int))

struct sockaddr_un {
    unsigned short sun_family;
    char sun_path[108];
};

typedef struct fd_path_holder {
	int fd;
	char path[108];
} fd_path_holder_t;

static struct cmsghdr   *cmptr = NULL;  /* malloc'ed first time */
static struct cmsghdr   *cmptr2 = NULL;  /* malloc'ed first time */
const char* sockaddr2 = "/data/data/org.openecard.android/files/socket2";
int pathSocket;

int recv_fd(int fd)
{
   int             newfd, nr, status;
   char            *ptr;
   char            buf[MAXLINE];
   struct iovec    iov[1];
   struct msghdr   msg;

   status = -1;
   for ( ; ; ) {
       iov[0].iov_base = buf;
       iov[0].iov_len  = sizeof(buf);
       msg.msg_iov     = iov;
       msg.msg_iovlen  = 1;
       msg.msg_name    = NULL;
       msg.msg_namelen = 0;
       if (cmptr2 == NULL && (cmptr2 = malloc(CONTROLLEN)) == NULL)
           return(-1);
       msg.msg_control    = cmptr2;
       msg.msg_controllen = CONTROLLEN;
       if ((nr = recvmsg(fd, &msg, 0)) < 0) {
           LOGI( "UNIX recvmsg error\n");
           exit(1);
       } else if (nr == 0) {
           LOGI("connection closed by server\n");
           return(-1);
       }

       /*
        * See if this is the final data with null & status.  Null
        * is next to last byte of buffer; status byte is last byte.
        * Zero status means there is a file descriptor to receive.
        */
       for (ptr = buf; ptr < &buf[nr]; ) {
           if (*ptr++ == 0) {
               status = *ptr & 0xFF;  /* prevent sign extension */
               if (status == 0) {
                   newfd = *(int *)CMSG_DATA(cmptr2);
               } else {
                   newfd = -status;
               }
               nr -= 2;
           }
        }
        if (nr > 0)
            return(-1);
        if (status >= 0)    /* final data has arrived */
            return(newfd);  /* descriptor, or -status */
   }
}

int get_from_client(char * sockaddr, char* str) {
    int unlink_ret, t, len;
    struct sockaddr_un remote;

    int sock = -1;
if(pathSocket==0){
    if ((pathSocket = socket(AF_UNIX, SOCK_STREAM, 0)) == -1) {
        fprintf(stderr, "Can't start UNIX socket\n");
        exit(1);
    }
    LOGI("got path receive socket\n");

    remote.sun_family = AF_UNIX;
    strcpy(remote.sun_path, sockaddr);
    len = strlen(remote.sun_path) + sizeof(remote.sun_family);
    while ( connect(pathSocket, (struct sockaddr *)&remote, len) == -1) {
        usleep(10*1000); // 10 milliseconds
        //LOGI("waiting for connect on path recv sock\n%s\n", strerror(errno));
    }
} else {
    LOGI("path receive socket already exists, waiting for data\n");
}
    if((t=recv(pathSocket, str, 100, 0)) > 0) {
        str[t] = '\0';
        LOGI("received PATH:  %s\n", str);
        close(pathSocket);
        unlink_ret = unlink(sockaddr2);
        LOGI("return of unlink for pathsocket: %d\n", unlink_ret);
        pathSocket=0;
        return 0;
    } else {
        if (t < 0) perror("recv");
        else LOGI("Server closed connection\n");
        close(pathSocket);
        unlink_ret = unlink(sockaddr2);
        LOGI("return of unlink for pathsocket: %d\n", unlink_ret);
        pathSocket=0;
        return -1;
    }
}


/*
 * Pass a file descriptor to another process.
 * If fd<0, then -fd is sent back instead as the error status.
 */
int
send_fd(int fd, int fd_to_send)
{
    struct iovec    iov[1];
    struct msghdr   msg;
    char            buf[2]; /* send_fd()/recv_fd() 2-byte protocol */

    iov[0].iov_base = buf;
    iov[0].iov_len  = 2;
    msg.msg_iov     = iov;
    msg.msg_iovlen  = 1;
    msg.msg_name    = NULL;
    msg.msg_namelen = 0;
    if (fd_to_send < 0) {
        msg.msg_control    = NULL;
        msg.msg_controllen = 0;
        buf[1] = -fd_to_send;   /* nonzero status means error */
        if (buf[1] == 0)
            buf[1] = 1; /* -256, etc. would screw up protocol */
    } else {
        if (cmptr == NULL && (cmptr = malloc(CONTROLLEN)) == NULL)
            return(-1);
        cmptr->cmsg_level  = SOL_SOCKET;
        cmptr->cmsg_type   = SCM_RIGHTS;
        cmptr->cmsg_len    = CONTROLLEN;
        msg.msg_control    = cmptr;
        msg.msg_controllen = CONTROLLEN;
        *(int *)CMSG_DATA(cmptr) = fd_to_send;     /* the fd to pass */
        buf[1] = 0;          /* zero status means OK */
    }
    buf[0] = 0;              /* null byte flag to recv_fd() */
    if (sendmsg(fd, &msg, 0) != 2)
        return(-1);
    return(0);
}

void startAsync(void *ctx) {
	fd_path_holder_t * holder = (fd_path_holder_t *) ctx;
	int done, n, fdSocket, unlink_ret;
	unsigned int /*s,*/ s2, t;
	struct sockaddr_un local, remote;
	int len;


	fdSocket = socket(AF_UNIX, SOCK_STREAM, 0);

	if (fdSocket < 0) {
		LOGI("UNIX Socket returned an error %s\n", strerror(errno));
		return;
	}

	local.sun_family = AF_UNIX;  /* local is declared before socket() ^ */
	strcpy(local.sun_path, holder->path);

	unlink(local.sun_path);
	len = strlen(local.sun_path) + sizeof(local.sun_family);
	if (bind(fdSocket, (struct sockaddr *)&local, len) == -1) {
        LOGI("UNIX Bind error\n");
        return;
    }
    LOGI("Binding successfull\n");
	if (listen(fdSocket, 1) == -1) {
		LOGI("UNIX Listen error\n");
		return;
	}
    LOGI("listen successfull, waiting for accept\n");
	t = sizeof(remote);
	if ((s2 = accept(fdSocket, (struct sockaddr *)&remote, &t)) == -1) {
		LOGI("UNIX Accept error\n");
		return;
	}
    LOGI("conneted, Starting to send FD: %d\n", holder->fd);
	send_fd(s2, holder->fd);

	close(s2);
	close(fdSocket);
    unlink_ret = unlink(holder->path);
            LOGI("return of unlink for fdSocket: %d\n", unlink_ret);
    LOGI("Finished sending FD: %d\n", holder->fd);
	free(ctx);
}

JNIEXPORT void JNICALL Java_org_openecard_android_activities_DeviceOpenActivity_startUnixSocketServer
(JNIEnv * env, jclass class, jstring address, jint fd) {
	const char *path = (*env)->GetStringUTFChars(env, address, 0);
	fd_path_holder_t * ctx = (fd_path_holder_t *) malloc(sizeof(fd_path_holder_t));
	strcpy(ctx->path, path);
	ctx->fd = (int) fd;

	startAsync(ctx);

	(*env)->ReleaseStringUTFChars(env, address, path);
}

JNIEXPORT jstring JNICALL Java_org_openecard_android_activities_DeviceOpenActivity_listenUnixSocketServer
(JNIEnv * env, jclass class, jstring address) {
    LOGI("starting recieving device path\n");
    const char *path = (*env)->GetStringUTFChars(env, address, 0);
    char str[100];
    int ret = get_from_client(path, str);
    (*env)->ReleaseStringUTFChars(env, address, path);
    LOGI("finished recieving device path: %s\n", str);
    jstring answer = (*env)->NewStringUTF(env,str);
	return  answer;
}

