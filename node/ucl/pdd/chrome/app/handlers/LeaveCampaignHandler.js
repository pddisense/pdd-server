// @flow

import { HttpClient } from 'colossus-client';
import { Storage } from '../browser';

export type LeaveCampaignCommand = {
  namespace: string,
  name: string,
};

export default function LeaveCampaignHandler(command: LeaveCampaignCommand) {
  const item = Storage.get(command.name);
  const obj = {
    kind: 'Client',
    metadata: {
      namespace: command.namespace,
      name: item.clientName,
    },
    status: {
      leaveTime: Date.now(),
    },
  };
  return HttpClient.replace(obj, 'clients', 'status')
    .then(() => Storage.remove(command.name));
};
