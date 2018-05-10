import React from 'react';
import { HttpClient } from 'colossus-client';

import CampaignsSection from './CampaignsSection';

export default class CampaignsSectionContainer extends React.Component {
  constructor(props) {
    super(props);
    this.state = { data: { items: [] } };
  }

  loadData() {
    HttpClient.list('campaigns', 'default').then(data => this.setState({ data }));
  }

  componentDidMount() {
    this.loadData();
  }

  render() {
    return <CampaignsSection results={this.state.data} />;
  }
}
