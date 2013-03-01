LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_SRC_FILES := \
	libusb/core.c \
	libusb/descriptor.c \
	libusb/io.c \
	libusb/sync.c \
	libusb/os/linux_usbfs.c \
	libusb/os/threads_posix.c

LOCAL_C_INCLUDES += \
    $(LOCAL_PATH)/libusb/os \
	$(LOCAL_PATH)/libusb 

LOCAL_CFLAGS += -O2 -DHAVE_GETTIMEOFDAY

LOCAL_LDLIBS := -llog

LOCAL_MODULE := libusb

include $(BUILD_STATIC_LIBRARY)
