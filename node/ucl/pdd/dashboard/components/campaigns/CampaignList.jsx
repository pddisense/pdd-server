import React from 'react';
import { Link } from 'react-router-dom';

import CampaignTableContainer from './CampaignTableContainer';

class CampaignList extends React.Component {
  render() {
    return (
      <div>
        <Link
          to="/campaigns/new"
          className="pt-button pt-icon-add pt-intent-primary"
          style={{ float: 'right' }}>
          New campaign
        </Link>

        <h2>Campaigns</h2>
        <CampaignTableContainer />
      </div>
    );
  }
}

export default CampaignList;
