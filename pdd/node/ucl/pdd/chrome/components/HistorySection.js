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
import moment from 'moment';
import { sum } from 'lodash';
import { isBefore1am } from '../util/dates';

class HistorySection extends React.Component {
  handleHistoryClick(e) {
    e.preventDefault();
    chrome.tabs.create({ 'url': 'chrome://history', 'active': true });
  }

  render() {
    const yesterday = isBefore1am(moment());
    const total = this.props.history.length > 0 ? this.props.history.shift() : 0;
    const rows = [];
    this.props.history.forEach((v, idx) => {
      if (v > 0) {
        const query = this.props.vocabulary.queries[idx];
        let keywords;
        if (query.exact) {
          keywords = query.exact;
        } else if (query.terms) {
          keywords = query.terms.join(', ');
        }
        rows.push(<tr key={idx}><td>{keywords}</td><td>{v}</td></tr>);
      }
    });
    return (
      <div>
        <h1>History</h1>
        <p>
          Here are the search queries that Private Data Donor has detected
          for {yesterday ? 'yesterday' : 'today'}.
          Those that are of interest will be automatically sent {yesterday ? '' : 'tomorrow'} at
          1am.
          If you wish to delete some of them, please remove the corresponding activity
          from <a onClick={this.handleHistoryClick}>your browsing history</a>.
        </p>
        <p>
          <b>{total} search{total === 1 ? '' : 'es'}</b> {total === 1 ? 'has' : 'have'} been
          detected so far.
        </p>
        <table className="pt-html-table">
          <thead>
          <tr>
            <th>Keywords</th>
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
  history: PropTypes.array.isRequired,
  vocabulary: PropTypes.array.isRequired,
};

export default HistorySection;
