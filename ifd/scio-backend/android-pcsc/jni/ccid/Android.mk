LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_SRC_FILES := \
	src/ccid.c \
	src/commands.c \
	src/ccid_usb.c \
	src/debug.c \
	src/ifdhandler.c \
	src/utils.c \
	src/tokenparser.c \
	src/strlcpy.c \
	src/towitoko/atr.c \
	src/towitoko/pps.c \
	src/openct/proto-t1.c \
	src/openct/buffer.c \
	src/openct/checksum.c

LOCAL_STATIC_LIBRARIES := libusb libpcsclite

LOCAL_C_INCLUDES := \
	$(LOCAL_PATH)/src \
	$(LOCAL_PATH)/../libusbx/libusb \
	$(LOCAL_PATH)/../pcsc-lite/src/PCSC 

LOCAL_CFLAGS := -O2 -DHAVE_CONFIG_H

LOCAL_LDLIBS := -llog

LOCAL_MODULE := ccid

include $(BUILD_SHARED_LIBRARY)
