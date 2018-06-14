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
import { DateInput } from '@blueprintjs/datetime';
import { isToday, isBefore1am } from '../util/dates';

function getMomentFormatter(format: string) {
  return {
    formatDate: (date) => moment(date).format(format),
    parseDate: (str) => moment(str, format).toDate(),
    placeholder: format,
  }
}

class HistorySection extends React.Component {
  handleHistoryClick(e) {
    e.preventDefault();
    chrome.tabs.create({ 'url': 'chrome://history', 'active': true });
  }

  render() {
    const today = isToday(this.props.date);
    const yesterday = isBefore1am(this.props.date);
    const total = sum(this.props.searches.map(item => item.count));
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
        <div>
          <label className="pt-label pt-inline">
            Display history for
            <DateInput {...getMomentFormatter("YYYY-MM-DD")}
                       canClearSelection={false}
                       maxDate={new Date()}
                       value={this.props.date}
                       keepFocus={false}
                       onChange={this.props.onChange}/>
          </label>
        </div>
        {today ? <p>
          Here are the search queries that Private Data Donor has detected for {yesterday ? 'yesterday' : 'today'}.
          Those that are of interest will be automatically sent {yesterday ? '' : 'tomorrow'} at 1am.
          If you wish to delete some of them, please remove the corresponding activity
          from <a onClick={this.handleHistoryClick}>your browsing history</a>.
        </p> : null}
        <p>
          <b>{total} search{total === 1 ? '' : 'es'}</b> {total === 1 ? 'has' : 'have'} been detected{today ? ' so far' : ''}.
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
  onChange: PropTypes.func.isRequired,
  date: PropTypes.object.isRequired,
};

export default HistorySection;
