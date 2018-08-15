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

class HistoryTable extends React.Component {
  handleClick(e, idx) {
    e.preventDefault();
    this.props.onClick(idx - 1);
  }

  render() {
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
              <td style={{textAlign: 'center'}}>
                <a onClick={e => this.handleClick(e, idx)}><Icon icon="remove"/></a>
              </td>
            </tr>
          );
        }
      });
    }
    return (
      <div style={{display: 'flex', flexDirection: 'column', alignItems: 'center'}}>
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

HistoryTable.propTypes = {
  history: PropTypes.array.isRequired,
};

HistoryTable.defaultProps = {
  onClick: noop,
};

export default HistoryTable;
