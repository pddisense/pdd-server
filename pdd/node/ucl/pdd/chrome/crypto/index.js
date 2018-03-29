/*
 * Copyright 2017-2018 UCL / Vincent Primault <v.primault@ucl.ac.uk>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import Elliptic from 'elliptic';
import BN from 'bn.js';
import crypto from 'crypto';

const curve = new Elliptic.ec('ed25519');

function exportKeyPair(pair) {
  return {
    publicKey: pair.getPublic('hex'),
    privateKey: pair.getPrivate('hex'),
  };
}

function importKeyPair(struct) {
  const pair = curve.keyFromPrivate(struct.privateKey, 'hex');
  pair.pub = importKey(struct.publicKey);
  return pair;
}

function importKey(str) {
  return curve.keyFromPublic(str, 'hex').pub;
}

function hash(data) {
  return crypto.createHash('sha256').update(data).digest('hex');
}

/**
 * Generate a pair of cryptographic keys.
 */
export function generateKeyPair() {
  return exportKeyPair(curve.genKeyPair());
}

/**
 * Encrypt a list of counters.
 *
 * @param publicKeys
 * @param round
 * @param keyPair
 * @param counters
 */
export function encryptCounters(publicKeys, round, keyPair, counters) {
  // Import client's key and check it is present within the group.
  const key = importKeyPair(keyPair);
  const clientIndex = publicKeys.findIndex(pkey => pkey === keyPair.publicKey);
  if (-1 === clientIndex) {
    console.log('Unable to find the client\'s public key among those sent');
    return [];
  }

  // Generate blinding factors.
  const factors = generateBlindingFactors(publicKeys, key, clientIndex, counters.length, round || 1);

  // Encrypt each counter by adding the blinding factor to its value.
  const encrypted = [];
  factors.forEach((factor, idx) => {
    encrypted.push(factor.add(new BN(counters[idx], 16)).mod(curve.n));
  });

  return encrypted.map(n => n.toString());
}

function generateBlindingFactors(publicKeys, key, clientIndex, L, round) {
  const factors = [];
  for (let l = 0; l < L; l++) {
    let K_il = curve.curve.zero.fromRed(); // TODO: We are not working on Red's?
    publicKeys.forEach((userKey, idx) => {
      const pubKey = importKey(userKey);
      const share = key.derive(pubKey);
      const n = new BN(hash(share + l + round));
      if (idx < clientIndex) {
        K_il = K_il.add(n); //.toRed(BN.red()));
      } else if (idx > clientIndex) {
        K_il = K_il.sub(n); //.toRed(BN.red()));
      }
    });
    factors.push(K_il.mod(curve.n));
  }
  return factors;
}
