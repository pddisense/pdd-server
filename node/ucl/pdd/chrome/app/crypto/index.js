// @flow

import Elliptic from 'elliptic';
import BN from 'bn.js';
import crypto from 'crypto';

export type KeyPair = {
  publicKey: string,
  privateKey: string,
};

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
 * @param command
 * @param pair
 * @param counters
 */
export function encryptCounters(command: any, pair: KeyPair, counters: any): Array<string> {
  // 1. Generate blinding factors.
  const key = importKeyPair(pair);
  const factors = generateBlindingFactors(command.groupKeys, key, command.clientIndex, counters.length, command.round || 1);

  // 2. Encrypt each counter by adding the blinding factor to its value.
  const encrypted = [];
  factors.forEach((factor, idx) => {
    encrypted.push(factor.add(new BN(counters[idx], 16)).mod(curve.n));
  });

  return encrypted.map(n => n.toString());
}

function generateBlindingFactors(groupKeys, key, clientIndex, L, round) {
  const factors = [];
  for (let l = 0; l < L; l++) {
    let K_il = curve.curve.zero.fromRed(); // TODO: We are not working on Red's?
    groupKeys.forEach(user => {
      const pubKey = importKey(user.publicKey);
      const share = key.derive(pubKey);
      const n = new BN(hash(share + l + round));
      if (user.index < clientIndex) {
        K_il = K_il.add(n); //.toRed(BN.red()));
      } else {
        K_il = K_il.sub(n); //.toRed(BN.red()));
      }
    });
    factors.push(K_il.mod(curve.n));
  }
  return factors;
}
