#include <jni.h>
#include <string>
#include <string.h>
#include <android/log.h>

#include "Crypto/Crypto.h"
#include "Crypto/SHA256.h"
#include "Crypto/AES.h"
#include "Crypto/RNG.h"
#include "Crypto/Curve25519.h"
#include "Crypto/Log.h"

void test_crypto();
void crypto_init();
void crypto_gen_shared_key(uint8_t foreign_public_key[32]);
void crypto_init_session(uint8_t nonce[32]);
void crypto_encrypt(uint8_t *dst, uint8_t *src, unsigned int len, uint32_t counter);

AES256 aes256;

static uint8_t public_key[32];
static uint8_t secret_key[32];
static uint8_t shared_key[32];
static uint8_t session_key[32];
static uint8_t session_nonce[32];

extern "C" {

JNIEXPORT jstring
JNICALL
Java_be_kuleuven_dnet_sergiodemo_MainActivity_testCrypto(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";

    test_crypto();

    return env->NewStringUTF(hello.c_str());
}

JNIEXPORT void
JNICALL
Java_be_kuleuven_dnet_sergiodemo_MainActivity_initCrypto(
        JNIEnv *env, jobject thiz) {
    crypto_init();
}

JNIEXPORT jbyteArray
JNICALL
Java_be_kuleuven_dnet_sergiodemo_MainActivity_getPublicKey(
        JNIEnv *env, jobject thiz) {
    jbyteArray plainData = env->NewByteArray(32);
    jbyte *plData = env->GetByteArrayElements(plainData, false);

    memcpy((uint8_t *) plData, public_key,32);

    env->SetByteArrayRegion(plainData, 0, 32, plData);

    return plainData;
}

JNIEXPORT void
JNICALL
Java_be_kuleuven_dnet_sergiodemo_MainActivity_createSharedKey(
        JNIEnv *env, jobject thiz, jbyteArray remotePublicKey) {
    jboolean isCopy = true;
    jbyte *encData = env->GetByteArrayElements(remotePublicKey, &isCopy);

    crypto_gen_shared_key((uint8_t *)encData);
}

JNIEXPORT jbyteArray
JNICALL
Java_be_kuleuven_dnet_sergiodemo_MainActivity_getSessionNonce(
        JNIEnv *env, jobject thiz) {
    jbyteArray plainData = env->NewByteArray(32);
    jbyte *plData = env->GetByteArrayElements(plainData, false);
    uint8_t nonce[32];

    RNG.rand(nonce, 32);

    crypto_init_session(nonce);

    memcpy((uint8_t *) plData, session_nonce,32);

    env->SetByteArrayRegion(plainData, 0, 32, plData);

    return plainData;
}

JNIEXPORT jbyteArray
JNICALL
Java_be_kuleuven_dnet_sergiodemo_MainActivity_getSharedKey(
        JNIEnv *env, jobject thiz) {
    jbyteArray plainData = env->NewByteArray(32);
    jbyte *plData = env->GetByteArrayElements(plainData, false);
    uint8_t key[32];

    memcpy((uint8_t *) plData, shared_key,32);

    env->SetByteArrayRegion(plainData, 0, 32, plData);

    return plainData;
}


JNIEXPORT jbyteArray
JNICALL
Java_be_kuleuven_dnet_sergiodemo_MainActivity_getSessionKey(
        JNIEnv *env, jobject thiz) {
    jbyteArray plainData = env->NewByteArray(32);
    jbyte *plData = env->GetByteArrayElements(plainData, false);

    memcpy((uint8_t *) plData, session_key,32);

    env->SetByteArrayRegion(plainData, 0, 32, plData);

    return plainData;
}


JNIEXPORT jbyteArray
JNICALL
Java_be_kuleuven_dnet_sergiodemo_MainActivity_decryptData(
        JNIEnv *env, jobject thiz, jbyteArray encryptedData) {
    jboolean isCopy = true;
    jbyte *encData = env->GetByteArrayElements(encryptedData, &isCopy);
    jbyteArray plainData = env->NewByteArray(16);
    jbyte *plData = env->GetByteArrayElements(plainData, false);

    crypto_encrypt((uint8_t *) plData, (uint8_t *)(encData + 4), 16, *((uint32_t *) encData));

    env->SetByteArrayRegion(plainData, 0, 16, plData);

    return plainData;
}

}


void crypto_init()
{
    // Start the random number generator.  We don't initialise a noise
    // source here because we don't need one for testing purposes.
    // Real DH applications should of course use a proper noise source.
    RNG.begin("SeRGIo Demo Curve25519");

    Curve25519::dh1(public_key, secret_key);

}

uint8_t* crypto_get_public_key()
{
    return public_key;
}

void print_array(uint8_t *data, int len, char *message = 0)
{
    int i;
    char temp[256] = {0};
    char temp2[256] = {0};

    if (message)
        sprintf(temp, "%s\n", message);


    for (i=0;i<len;i++)
    {
        sprintf(temp2, "%d ", data[i]);
        strcat(temp, temp2);
    }
    strcat(temp, "\n");

    __android_log_print(ANDROID_LOG_INFO, "NATIVE_SeRGIo", "%s", temp);

}

void crypto_gen_shared_key(uint8_t foreign_public_key[32])
{
    memcpy(shared_key, foreign_public_key, 32);

    Curve25519::dh2(shared_key, secret_key);
}

void crypto_init_session(uint8_t nonce[32])
{
    SHA256 sha256;


    sha256.update((void *)nonce, (size_t)32);
    sha256.update((void *)shared_key, (size_t)32);
    sha256.finalize(session_key, 32);

    memcpy(session_nonce, nonce, 32);

    aes256.setKey(session_key, 32);

}

void crypto_encrypt(uint8_t *dst, uint8_t *src, unsigned int len, uint32_t counter)
{
    uint8_t iv[32];
    int i;

    if (len > 32) len = 32;
    if (dst == 0) dst = src;

    memcpy(iv, session_nonce, 32);


    for (i = 0; i < 32; i+=sizeof(uint32_t))
    {
        *((uint32_t*)(iv + i)) ^= counter;
    }

    aes256.encryptBlock(iv, iv);
    aes256.encryptBlock(iv + 16, iv + 16);

    i = 0;
    while (i < len)
    {
        dst[i] = iv[i] ^ src[i];
        i++;
    }
}

void test_crypto()
{
    static uint8_t public_key2[32];
    static uint8_t secret_key2[32];
    static uint8_t shared_key2[32];
    static uint8_t session_key2[32];
    static uint8_t nonce[32];
    static uint8_t data[32];
    int i;

    crypto_init();
    Curve25519::dh1(public_key2, secret_key2);


    print_array(public_key, 32, "public_key");
    print_array(secret_key, 32, "secret_key");
    print_array(public_key2, 32, "public_key2");
    print_array(secret_key2, 32, "secret_key2");

    crypto_gen_shared_key(public_key2);

    {
        memcpy(shared_key2, public_key, 32);

        Curve25519::dh2(shared_key2, secret_key2);
    }

    print_array(shared_key, 32, "secret_key");

    if (memcmp(shared_key, shared_key2, 32) == 0)
    {
        log("shared_key ok");
    }
    else
    {
        log("shared_key failed");
    }

    RNG.rand(nonce, 32);

    print_array(nonce, 32, "nonce: ");


    crypto_init_session(nonce);

    for (i=0;i<32;i++)
    {
        data[i] = i;
    }

    crypto_encrypt(0, data, 32, 1);

    print_array(data, 32, "Encrypted: ");

    crypto_encrypt(0, data, 32, 1);

    print_array(data, 32, "Decrypted: ");
}

void test_dh()
{
    static uint8_t alice_k[32];
    static uint8_t alice_f[32];
    static uint8_t bob_k[32];
    static uint8_t bob_f[32];
    logstartd();

    log("Diffie-Hellman key exchange:");
    log("Generate random k/f for Alice ... ");
    unsigned long start = micros();
    Curve25519::dh1(alice_k, alice_f);
    unsigned long elapsed = micros() - start;
    logp("elapsed: ", elapsed, 0);

    log("Generate random k/f for Bob ... ");

    start = micros();
    Curve25519::dh1(bob_k, bob_f);
    elapsed = micros() - start;
    logp("elapsed: ", elapsed, 0);

    log("Generate shared secret for Alice ... ");
    start = micros();
    Curve25519::dh2(bob_k, alice_f);
    elapsed = micros() - start;
    logp("elapsed: ", elapsed, 0);

    log("Generate shared secret for Bob ... ");
    start = micros();
    Curve25519::dh2(alice_k, bob_f);
    elapsed = micros() - start;
    logp("elapsed: ", elapsed, 0);

    log("Check that the shared secrets match ... ");
    if (memcmp(alice_k, bob_k, 32) == 0)
    {
        log("ok");
    }
    else
    {
        log("failed");
    }

    logendd();

}