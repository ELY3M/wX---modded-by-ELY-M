LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

#include bzip2/Android.mk
#LOCAL_PATH:= $(call my-dir)
#include $(CLEAR_VARS)

LOCAL_MODULE    := radial
LOCAL_CFLAGS    := -Werror
LOCAL_SRC_FILES :=  blocksort.c compress.c decode8BitAndGenRadials.c genCircle.c genCircleWithColor.c genMercato.c huffman.c level2Decompress.c randtable.c bzlib.c crctable.c decompress.c genIndex.c genTriangle.c level2Decode.c level2GenRadials.c colorGen.c genTriangleUp.c
#LOCAL_LDLIBS    := -llog 

include $(BUILD_SHARED_LIBRARY)
