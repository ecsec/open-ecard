LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES := \
src/pcscdaemon.c \
src/debuglog.c \
src/readerfactory.c \
src/hotplug_libusb.c \
src/dyn_unix.c \
src/sys_unix.c \
src/ifdwrapper.c \
src/eventhandler.c \
src/prothandler.c \
src/powermgt_generic.c \
src/utils.c \
src/atrhandler.c \
src/configfile.c \
src/winscard.c \
src/winscard_msg_srv.c \
src/winscard_svc.c \
src/winscard_msg.c \
src/simclist.c \
src/tokenparser.c \
src/sd-daemon.c

LOCAL_C_INCLUDES += \
$(LOCAL_PATH)/src/ \
$(LOCAL_PATH)/src/PCSC/ \
$(LOCAL_PATH)/../libusbx-1.0.12/libusb

LOCAL_CFLAGS := -O2 -DHAVE_CONFIG_H -DHAVE_LIBUSB -DDISABLE_SYSTEMD

LOCAL_STATIC_LIBRARIES := libusb

LOCAL_MODULE := pcscd

include $(BUILD_EXECUTABLE)


include $(CLEAR_VARS)

LOCAL_SRC_FILES := \
src/winscard_clnt.c \
src/debug.c \
src/error.c \
src/strlcat.c \
src/strlcpy.c \
src/winscard_msg.c \
src/simclist.c \
src/sys_unix.c \
src/utils.c

LOCAL_C_INCLUDES += \
$(LOCAL_PATH)/src/ \
$(LOCAL_PATH)/src/PCSC/ \
$(LOCAL_PATH)/../libusb/libusb

LOCAL_CFLAGS := -O2 -DHAVE_CONFIG_H -DHAVE_LIBUSB

LOCAL_STATIC_LIBRARIES := libusb

LOCAL_MODULE := libpcsclite

include $(BUILD_STATIC_LIBRARY)


