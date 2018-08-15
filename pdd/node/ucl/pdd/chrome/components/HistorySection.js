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
import { Icon } from '@blueprintjs/core';
import { noop } from 'lodash';

import { isBefore1am } from '../util/dates';
import { formatQuery } from '../protocol/history';

class HistorySection extends React.Component {
  handleClick(e, idx) {
    e.preventDefault();
    const blacklist = this.props.localData.blacklist
      ? { queries: this.props.localData.blacklist.queries.slice() }
      : { queries: [] };
    blacklist.queries.push(this.props.localData.vocabulary.queries[idx]);
    this.props.onChange({ blacklist });
  }

  render() {
    const yesterday = isBefore1am(moment());
    const total = this.props.history.length > 0 ? this.props.history[0] : 0;
    const rows = [];
    if (this.props.localData.vocabulary) {
      // If the vocabulary is not available locally, we cannot extract the search history yet.
      this.props.history.forEach((v, idx) => {
        // Don't display first count, which is the total number of searches, and...
        // don't display keywords with a null count.
        if (idx > 0 && v > 0) {
          const query = this.props.localData.vocabulary.queries[idx - 1];
          rows.push(
            <tr key={idx}>
              <td>{formatQuery(query)}</td>
              <td>{v}</td>
              <td style={{textAlign: 'center'}}><a onClick={e => this.handleClick(e, idx - 1)}><Icon icon="remove"/></a></td>
            </tr>
          );
        }
      });
    }
    return (
      <div>
        <h1>History</h1>
        <p>
          Here are the search queries that Private Data Donor has detected
          for {yesterday ? 'yesterday' : 'today'}.
          Those that are of interest will be automatically sent {yesterday ? '' : 'tomorrow'} at
          1am.
          If you do not want some keywords to be monitored, you can choose to blacklist them by
          clicking on the button on the right-hand side of each keyword.
          They will be permanently blocked.
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
            <th style={{textAlign: 'center'}}>Blacklist</th>
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
  localData: PropTypes.object.isRequired,
};

HistorySection.defaultProps = {
  onChange: noop,
};

export default HistorySection;
