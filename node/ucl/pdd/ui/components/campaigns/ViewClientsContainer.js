import React from 'react';
import fetchNamed from '../fetchNamed';
import fetchCollection from '../fetchCollection';
import ViewClients from './ViewClients';

const withClients = fetchCollection('clients', { fieldSelector: { 'attrs.campaignName': props => props.item.metadata.name } })(ViewClients);
export default fetchNamed('campaigns')(withClients);
