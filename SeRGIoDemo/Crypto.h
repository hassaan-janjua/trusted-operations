/*
 * Curve25519.h
 *
 *  Created on: 08 Mar 2018
 *      Author: hassaanjanjua
 */

#ifndef CRYPTO_H_
#define CRYPTO_H_

void crypto_init();
void crypto_gen_shared_key(uint8_t foreign_public_key[32]);
void test_dh();
void test_crypto();






#endif /* CRYPTO_H_ */
