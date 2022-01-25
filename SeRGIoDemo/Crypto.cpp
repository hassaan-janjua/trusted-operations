/*
 * curve25519.cpp
 *
 *  Created on: 08 Mar 2018
 *      Author: hassaanjanjua
 */

#include <Crypto.h>
#include <Curve25519.h>
#include <RNG.h>
#include <string.h>
#include <Arduino.h>

#include "Log.h"
#include "TinyAES.h"
#include "TinySha256.h"

static uint8_t public_key[32];
static uint8_t secret_key[32];
static uint8_t shared_key[32];
static uint8_t session_key[32];
static uint8_t session_nonce[32];

struct AES_ctx ctx;

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

    if (message)
        Serial1.println(message);


    for (i=0;i<len;i++)
    {
        Serial1.print(data[i]);
        Serial1.print(" ");
    }
    Serial1.println();

}

void crypto_gen_shared_key(uint8_t foreign_public_key[32])
{
    memcpy(shared_key, foreign_public_key, 32);

    Curve25519::dh2(shared_key, secret_key);
}

void crypto_init_session(uint8_t nonce[32])
{
    SHA256_CTX sha256_ctx;

    sha256_init(&sha256_ctx);
    sha256_update(&sha256_ctx, (const unsigned char *)nonce, (unsigned int)32);
    sha256_update(&sha256_ctx, (const unsigned char *)shared_key, (unsigned int)32);
    sha256_final(&sha256_ctx, (unsigned char *)session_key);

    memcpy(session_nonce, nonce, 32);

    AES_init_ctx(&ctx, session_key);

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

    AES_ECB_encrypt(&ctx, iv);

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
    logm("elapsed: ", elapsed);

    log("Generate random k/f for Bob ... ");
    Serial1.flush();
    start = micros();
    Curve25519::dh1(bob_k, bob_f);
    elapsed = micros() - start;
    logm("elapsed: ", elapsed);

    log("Generate shared secret for Alice ... ");
    start = micros();
    Curve25519::dh2(bob_k, alice_f);
    elapsed = micros() - start;
    logm("elapsed: ", elapsed);

    log("Generate shared secret for Bob ... ");
    start = micros();
    Curve25519::dh2(alice_k, bob_f);
    elapsed = micros() - start;
    logm("elapsed: ", elapsed);

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


