# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
             native-lib

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             src/main/cpp/native-lib.cpp
             src/main/cpp/Crypto/AES128.cpp
             src/main/cpp/Crypto/AES192.cpp
             src/main/cpp/Crypto/AES256.cpp
             src/main/cpp/Crypto/AESCommon.cpp
             src/main/cpp/Crypto/AuthenticatedCipher.cpp
             src/main/cpp/Crypto/BigNumberUtil.cpp
             src/main/cpp/Crypto/BLAKE2b.cpp
             src/main/cpp/Crypto/BLAKE2s.cpp
             src/main/cpp/Crypto/BlockCipher.cpp
             src/main/cpp/Crypto/CBC.cpp
             src/main/cpp/Crypto/CFB.cpp
             src/main/cpp/Crypto/ChaCha.cpp
             src/main/cpp/Crypto/ChaChaPoly.cpp
             src/main/cpp/Crypto/Cipher.cpp
             src/main/cpp/Crypto/Crypto.cpp
             src/main/cpp/Crypto/CTR.cpp
             src/main/cpp/Crypto/Curve25519.cpp
             src/main/cpp/Crypto/EAX.cpp
             src/main/cpp/Crypto/Ed25519.cpp
             src/main/cpp/Crypto/GCM.cpp
             src/main/cpp/Crypto/GF128.cpp
             src/main/cpp/Crypto/GHASH.cpp
             src/main/cpp/Crypto/Hash.cpp
             src/main/cpp/Crypto/KeccakCore.cpp
             src/main/cpp/Crypto/NoiseSource.cpp
             src/main/cpp/Crypto/OFB.cpp
             src/main/cpp/Crypto/OMAC.cpp
             src/main/cpp/Crypto/P521.cpp
             src/main/cpp/Crypto/Poly1305.cpp
             src/main/cpp/Crypto/RNG.cpp
             src/main/cpp/Crypto/SHA3.cpp
             src/main/cpp/Crypto/SHA256.cpp
             src/main/cpp/Crypto/SHA512.cpp
             src/main/cpp/Crypto/SHAKE.cpp
             src/main/cpp/Crypto/Speck.cpp
             src/main/cpp/Crypto/SpeckSmall.cpp
             src/main/cpp/Crypto/SpeckTiny.cpp
             src/main/cpp/Crypto/XOF.cpp
             src/main/cpp/Crypto/XTS.cpp)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
                       native-lib

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} )