TOP_LOCAL_PATH := $(call my-dir)
include $(call all-subdir-makefiles)

LOCAL_PATH := $(TOP_LOCAL_PATH)

include $(CLEAR_VARS)
  
LOCAL_MODULE    := j2pcsc
LOCAL_SRC_FILES := j2pcsc.c

LOCAL_STATIC_LIBRARIES := libpcsclite 

LOCAL_C_INCLUDES := $(LOCAL_PATH)/pcsc-lite-1.7.4/src/PCSC

LOCAL_CFLAGS += -O2

include $(BUILD_SHARED_LIBRARY)
