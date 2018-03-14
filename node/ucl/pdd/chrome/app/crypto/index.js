// @flow
/*
 * Private Data Donor is a platform to collect search logs via crowd-sourcing.
 * Copyright (C) 2017-2018 Vincent Primault <v.primault@ucl.ac.uk>
 *
 * Private Data Donor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Private Data Donor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Private Data Donor.  If not, see <http://www.gnu.org/licenses/>.
 */

import Elliptic from 'elliptic';
import BN from 'bn.js';
import crypto from 'crypto';

import type { KeyPair } from '../types';

const curve = new Elliptic.ec('ed25519');

function exportKeyPair(pair): KeyPair {
  return {
    publicKey: pair.getPublic('hex'),
    privateKey: pair.getPrivate('hex'),
  };
}

function importKeyPair(struct: KeyPair): any {
  const pair = curve.keyFromPrivate(struct.privateKey, 'hex');
  pair.pub = importKey(struct.publicKey);
  return pair;
}

function importKey(str): any {
  return curve.keyFromPublic(str, 'hex').pub;
}

function hash(data: any): string {
  return crypto.createHash('sha256').update(data).digest('hex');
}

/**
 * Generate a pair of cryptographic keys.
 */
export function generateKeyPair(): KeyPair {
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
export function encryptCounters(publicKeys: Array<string>, round: number, keyPair: KeyPair, counters: Array<number>): Array<string> {
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

function generateBlindingFactors(publicKeys: Array<string>, key: any, clientIndex: number, L: number, round: number): Array<BN> {
  const factors = [];
  for (let l = 0; l < L; l++) {
    let K_il = curve.curve.zero.fromRed(); // TODO: We are not working on Red's?
    publicKeys.forEach((userKey, idx) => {
      const pubKey = importKey(userKey);
      const share = key.derive(pubKey);
      const n = new BN(hash(share + l + round));
      if (idx < clientIndex) {
        K_il = K_il.add(n); //.toRed(BN.red()));
      } else {
        K_il = K_il.sub(n); //.toRed(BN.red()));
      }
    });
    factors.push(K_il.mod(curve.n));
  }
  return factors;
}
