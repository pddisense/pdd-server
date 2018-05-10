/*
 * PDD is a platform for privacy-preserving Web searches collection.
 * Copyright (C) 2016-2018 Vincent Primault <v.primault@ucl.ac.uk>
 *
 * PDD is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PDD is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PDD.  If not, see <http://www.gnu.org/licenses/>.
 */

import React from 'react';
import PropTypes from 'prop-types';
import { withRouter } from 'react-router-dom';
import autobind from 'autobind-decorator';
import moment from 'moment';
import { identity } from 'lodash';

@withRouter
class CampaignTable extends React.Component {
  @autobind
  handleClick(campaign) {
    this.props.history.push(`/campaigns/view/${campaign.name}`);
  }

  render() {
    const rows = this.props.campaigns.map((item, idx) => {
      const wrap = (el) => item.active ? el : <span className="pt-text-muted">{el}</span>;
      return (
        <tr onClick={() => this.handleClick(item)} key={idx}>
          <td>{wrap(item.displayName ? item.displayName : 'Untitled campaign')}</td>
          <td>{wrap(item.email ? item.email : '-')}</td>
          <td>{wrap(item.startTime ? moment(item.startTime).fromNow() : '–')}</td>
          <td>{wrap(item.endTime ? moment(item.endTime).fromNow() : item.startTime ? 'never' : '-')}</td>
          <td>{wrap(item.collectEncrypted ? 'enabled' : 'disabled')}</td>
        </tr>
      );
    });
    return (
      <table className="pt-html-table pt-interactive pt-html-table-striped"
             style={{ width: '100%' }}>
        <thead>
        <tr>
          <th>Name</th>
          <th>Owner</th>
          <th>Start time</th>
          <th>End time</th>
          <th>Encryption</th>
        </tr>
        </thead>
        <tbody>{rows}</tbody>
      </table>
    );
  }
}

CampaignTable.propTypes = {
  campaigns: PropTypes.array,
};

CampaignTable.defaultProps = {
  campaigns: [],
};

export default CampaignTable;
