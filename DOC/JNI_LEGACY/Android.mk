LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

#include bzip2/Android.mk
#LOCAL_PATH:= $(call my-dir)
#include $(CLEAR_VARS)

LOCAL_MODULE    := radial
LOCAL_CFLAGS    := -Werror
LOCAL_SRC_FILES := geom.c radialv2.c city.c colorgen.c indexgen.c bzlib.c compress.c randtable.c crctable.c decompress.c huffman.c blocksort.c bzipwrap.c decode8bit.c rect8bitwx.c decode8bitwx.c radiallevel2.c
LOCAL_LDLIBS    := -llog 

include $(BUILD_SHARED_LIBRARY)



