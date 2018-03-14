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

export type KeyPair = {
  publicKey: string,
  privateKey: string,
};

export type Client = {
  name: string,
  keyPair: KeyPair,
  createTime: string,
  browser: string,
  externalName: ?string,
};

export type SubmitSketchCommand = {
  sketchName: string,
  startTime: string,
  endTime: string,
  vocabulary: Vocabulary,
  publicKeys: Array<string>,
  collectRaw: boolean,
  collectEncrypted: boolean,
  round: number,
};

export type Vocabulary = {
  queries: Array<VocabularyQuery>,
};

export type VocabularyQuery = {
  exact: ?string,
  terms: ?Array<string>,
};

export type WebSearch = {
  indices: Array<number>,
  query: string,
  lastTime: number,
  count: number,
};
