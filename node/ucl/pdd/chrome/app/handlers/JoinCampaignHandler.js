// @flow

import { HttpClient } from 'colossus-client';

import { generateKeyPair } from '../crypto';
import { Storage } from '../browser';

export type JoinCampaignCommand = {
  namespace: string,
  name: string,
  externalName: ?string,
};

export default function JoinCampaignHandler(command: JoinCampaignCommand) {
  return HttpClient.get('campaigns', command.namespace, command.name)
    .then(campaign => {
      const keyPair = generateKeyPair();
      const obj = {
        kind: 'Client',
        metadata: {
          namespace: campaign.metadata.namespace,
        },
        attrs: {
          campaignName: campaign.metadata.name,
          publicKey: keyPair.publicKey,
          externalName: command.externalName,
        },
      };
      return HttpClient.create(obj, 'clients')
        .then(client => {
          return Storage.set(command.name, {
            keyPair,
            namespace: campaign.metadata.namespace,
            clientName: client.metadata.name,
            joinTime: client.metadata.createTime,
            campaignName: campaign.metadata.name,
            vocabulary: campaign.attrs.vocabulary.queries,
            activeQueries: campaign.attrs.activeQueries,
          });
        });
    });
};
