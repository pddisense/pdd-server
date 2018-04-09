/*
 * Copyright 2017-2018 UCL / Vincent Primault <v.primault@ucl.ac.uk>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import React from 'react';
import PropTypes from 'prop-types';
import moment from 'moment';
import { flatMap } from 'lodash';

class HistorySection extends React.Component {
  handleHistoryClick(e) {
    e.preventDefault();
    chrome.tabs.create({ 'url': 'chrome://history', 'active': true });
  }

  render() {
    const yesterday = moment().hour() < 1;
    const rows = this.props.searches.map((item, idx) => {
      return (
        <tr key={idx}>
          <td>{item.query}</td>
          <td>{moment(item.lastTime).fromNow()}</td>
          <td>{item.count}</td>
        </tr>
      );
    });
    return (
      <div>
        <h1>History</h1>
        <p>
          Here are the search queries that Private Data Donor has detected for {yesterday ? 'yesterday' : 'today'}.
          Those that are of interest will be automatically sent {yesterday ? '' : 'tomorrow'} at 1am.
          If you wish to delete some of them, please remove the corresponding activity
          from <a onClick={this.handleHistoryClick}>your browsing history</a>.
        </p>
        <p>
          <b>{this.props.searches.length} quer{this.props.searches.length === 1 ? 'y' : 'ies'}</b> have
           been detected so far.
        </p>
        <table className="pt-html-table">
          <thead>
          <tr>
            <th>Query</th>
            <th>Last time</th>
            <th>Occurrences</th>
          </tr>
          </thead>
          <tbody>{rows}</tbody>
        </table>
      </div>
    );
  }
}

HistorySection.propTypes = {
  searches: PropTypes.array.isRequired,
};

export default HistorySection;
