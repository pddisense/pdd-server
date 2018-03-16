import { generateKeyPair, encryptCounters } from './index';

global.crypto = require('crypto');

describe("crypto", function() {
  it("generates key pairs", function() {
    const array = new Uint32Array(10);
    crypto.randomBytes(array.length);
    const pair = generateKeyPair();
    console.log(pair);
    expect(pair.publicKey).toBeTruthy();
    expect(pair.privateKey).toBeTruthy();
  });
});
