LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_SRC_FILES := \
	cjeca32/ausb/ausb.c \
	cjeca32/ausb/ausb_libusb0.c \
	cjeca32/ausb/ausb_libusb1.c \
	cjeca32/ausb/ausb1.c \
	cjeca32/ausb/ausb3.c \
	cjeca32/ausb/ausb11.c \
	cjeca32/ausb/ausb31.c \
	cjeca32/ausb/usbdev.c \
	cjeca32/ausb/usbdev_libusb1.c \
	ifd/ifd.cpp \
	cjeca32/cjeca32.cpp \
	cjeca32/Debug.cpp \
	cjeca32/Reader.cpp \
	cjeca32/BaseReader.cpp \
	cjeca32/BaseCommunication.cpp \
	cjeca32/CCIDReader.cpp \
	cjeca32/RSCTCriticalSection.cpp \
	cjeca32/ECAReader.cpp \
	cjeca32/ECBReader.cpp \
	cjeca32/ECRReader.cpp \
	cjeca32/ECPReader.cpp \
	cjeca32/SECReader.cpp \
	cjeca32/ECFReader.cpp \
	cjeca32/EFBReader.cpp \
	cjeca32/PPAReader.cpp \
	cjeca32/RFKReader.cpp \
	cjeca32/RFSReader.cpp \
	cjeca32/CPTReader.cpp \
	cjeca32/EC30Reader.cpp \
	cjeca32/Platform_unix.cpp \
	cjeca32/USBUnix.cpp \
	cjeca32/config.cpp \
	cjeca32/SerialUnix.cpp

LOCAL_STATIC_LIBRARIES += libusb

LOCAL_C_INCLUDES += \
	$(LOCAL_PATH)/cjeca32/ausb \
	$(LOCAL_PATH)/../libusbx-1.0.12/libusb \
	$(LOCAL_PATH)/include/driver \
	$(LOCAL_PATH)/../pcsc-lite-1.7.4/src \
	$(LOCAL_PATH)/../pcsc-lite-1.7.4/src/PCSC \
	$(LOCAL_PATH)/cjeca32 \
	$(LOCAL_PATH)/include/firmware

LOCAL_CFLAGS += -O2 -DHAVE_CONFIG_H 

LOCAL_MODULE := cyberjack

include $(BUILD_SHARED_LIBRARY)
