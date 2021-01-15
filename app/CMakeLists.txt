cmake_minimum_required(VERSION 3.4.1)

add_library(quiet-jni
        SHARED
        src/main/jni/system.c
        src/main/jni/profiles.c
        src/main/jni/encoder.c
        src/main/jni/decoder.c)

include_directories(src/main/jni/include/)

add_library(quiet SHARED IMPORTED)
set_target_properties(quiet PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libquiet.so)

add_library(liquid SHARED IMPORTED)
set_target_properties(liquid PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libliquid.so)

add_library(jansson SHARED IMPORTED)
set_target_properties(jansson PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libjansson.so)

add_library(complex SHARED IMPORTED)
set_target_properties(complex PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libcomplex.so)

add_library(fec SHARED IMPORTED)
set_target_properties(fec PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libfec.so)

target_link_libraries(quiet-jni quiet liquid jansson complex fec)
