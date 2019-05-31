# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)


# 定义常量
set(SRC_DIR src/main/cpp/lamemp3)
# 指定关联的头文件目录
include_directories(src/main/cpp/lamemp3)

# 查找在某个路径下的所有源文件
aux_source_directory(src/main/cpp/lamemp3 SRC_LIST)


# 设置 *.so 文件输出路径，要放在在add_library之前，不然不会起作用
#set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${PROJECT_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI})

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.
# 声明库名称、类型、源码文件
add_library( # Sets the name of the library.
        mp3converter111

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        src/main/cpp/mp3converter.cpp

        ${SRC_LIST})

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

# 定位某个NDK库，这里定位的是log库
find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

# 将NDK库链接到native库中，这样native库才能调用NDK库中的函数
target_link_libraries( # Specifies the target library.
        mp3converter111

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})