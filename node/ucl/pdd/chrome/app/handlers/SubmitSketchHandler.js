// @flow

import moment from 'moment';
import { HttpClient } from 'colossus-client';

import { encryptCounters } from '../crypto';
import { Storage, History } from '../browser';
import type { Tracer } from '../browser/storage';

export type SubmitSketchCommand = {
  item: Tracer,
  command: {
    sketchName: string,
    startTime: string,
    endTime: string,
    vocabulary: Array<any>,
    clientIndex: number,
    activeQueries: Array<boolean>,
    groupKeys: Array<any>,
    collectRaw: boolean,
    collectEncrypted: boolean,
  },
};

export default function SubmitSketchHandler(command: SubmitSketchCommand): Promise<any> {
  // 1. Update the locally stored vocabulary if an update was sent as part of the command.
  // 2. Generate encrypted/raw counter values.
  // 3. Update the sketch status with those values.
  return updateVocabulary(command)
    .then(vocabulary => encryptValues(vocabulary, command.item.keyPair, command))
    .then(values => {
      const obj = {
        kind: 'Sketch',
        metadata: {
          namespace: command.item.namespace,
          name: command.command.sketchName,
        },
        status: {
          state: 'Submitted',
          submitTime: moment(),
          values,
        }
      };
      return HttpClient.replace(obj, 'sketches', 'status');
    });
}

function updateVocabulary(command: SubmitSketchCommand) {
  if (command.command.vocabulary.length > 0) {
    const vocabulary = command.item.vocabulary.concat(command.command.vocabulary);
    return Storage.set(command.item.campaignName, { vocabulary }).then(() => vocabulary);
  } else {
    return Promise.resolve(command.item.vocabulary);
  }
}

function encryptValues(vocabulary, keyPair, command) {
  const startTime = moment(command.command.startTime).valueOf();
  const endTime = moment(command.command.startTime).valueOf();
  const counters = History.aggregate(startTime, endTime, vocabulary);
  const encrypted = command.command.collectEncrypted ? encryptCounters(command, keyPair, counters) : [];
  return counters.map((v, idx) => {
    return {
      encryptedValue: command.command.collectEncrypted ? encrypted[idx] : null,
      rawValue: command.command.collectRaw ? v : null,
    };
  });
}
