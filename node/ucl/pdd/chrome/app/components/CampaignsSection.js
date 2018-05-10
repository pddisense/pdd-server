import React from 'react';
import moment from 'moment';
import autobind from 'autobind-decorator';
import { identity } from 'lodash';

import { Storage } from '../browser';
import CampaignDialog from './CampaignDialog';

function isActive(campaign) {
  return campaign.attrs.startTime;
}

export default class CampaignsSection extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      showDialog: null,
    };
  }

  @autobind
  handleClose() {
    this.setState({ showDialog: null });
  }

  @autobind
  handleClick(e, campaign) {
    e.preventDefault();
    this.setState({ showDialog: campaign });
  }

  render() {
    const rows = !this.props.results.items ? [] : this.props.results.items
      .filter(isActive)
      .map((campaign, idx) => {
        const tracer = Storage.get(campaign.metadata.name);
        return (
          <tr key={idx}>
            <td>
              <a onClick={e => this.handleClick(e, campaign)}>{campaign.attrs.displayName}</a>
            </td>
            <td>{moment(campaign.attrs.startTime).fromNow()}</td>
            <td>{campaign.attrs.endTime ? moment(campaign.attrs.endTime).fromNow() : 'never'}</td>
            <td>{tracer ? moment(campaign.joinTime).fromNow() : '-'}</td>
          </tr>
        );
      });

    return (
      <div>
        <h1>Campaigns</h1>

        <p>
          Search queries are monitored through collection campaigns, created by researchers.
          From this page, you can join the campaigns you want to contribute to.
          You may also choose at any time to leave a campaign you are part of.
        </p>

        <table className="pt-html-table">
          <thead>
          <tr>
            <th>Campaign name</th>
            <th>Campaign start</th>
            <th>Campaign end</th>
            <th>Joined</th>
          </tr>
          </thead>
          <tbody>{rows}</tbody>
        </table>

        {this.state.showDialog ?
          <CampaignDialog
            onConfirm={this.handleClose}
            onClose={this.handleClose}
            campaign={this.state.showDialog}/> : null}
      </div>
    );
  }
}
