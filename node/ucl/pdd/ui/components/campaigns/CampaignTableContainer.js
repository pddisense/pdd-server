import React from 'react';
import fetchCollection from '../fetchCollection';
import CampaignTable from './CampaignTable';

export default fetchCollection('campaigns')(CampaignTable);
