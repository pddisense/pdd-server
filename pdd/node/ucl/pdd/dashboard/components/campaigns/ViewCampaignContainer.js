import React from 'react';
import fetchNamed from '../fetchNamed';
import ViewCampaign from './ViewCampaign';

export default fetchNamed('campaigns')(ViewCampaign);
