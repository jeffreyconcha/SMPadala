LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := ndkLib
LOCAL_SRC_FILES := ndkLib.c

include $(BUILD_SHARED_LIBRARY)