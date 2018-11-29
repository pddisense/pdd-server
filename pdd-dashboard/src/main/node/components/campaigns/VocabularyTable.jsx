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
import { noop, some } from 'lodash';
import { Callout, Icon, Intent } from '@blueprintjs/core';

import TextInput from '../form/TextInput';

function filterVocabulary(campaign, state) {
  let queries = campaign.vocabulary.queries;
  queries.forEach((q, idx) => q.index = idx);

  if (state.filter !== '') {
    queries = queries.filter(q => {
      return (q.exact && q.exact.indexOf(state.filter) > -1)
        || (q.terms && some(q.terms, t => t.indexOf(state.filter) > -1));
    });
  }
  return queries;
}

class VocabularyTable extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      filter: '',
    }
  }

  handleFilterChange(e) {
    this.setState({ filter: e.target.value });
  }

  render() {
    const totalQueries = this.props.campaign.vocabulary.queries.length;
    if (totalQueries === 0) {
      return (
        <Callout intent={Intent.DANGER}>
          No queries have been added to this campaign. Please add some queries.
        </Callout>
      );
    }

    const rows = filterVocabulary(this.props.campaign, this.state).map(q => {
      return (
        <tr key={q.index} onClick={() => this.props.onClick ? this.props.onClick(q) : noop()}>
          <td>{q.index}</td>
          <td>{q.terms && q.terms.length > 0 ? q.terms.join(',') : q.exact}</td>
          <td>{q.terms && q.terms.length > 0 ? 'terms' : 'exact'}</td>
        </tr>
      );
    });
    const shownQueries = rows.length;
    let message;
    if (shownQueries < totalQueries) {
      message = `Displaying ${shownQueries} ${shownQueries === 1 ? 'query' : 'queries'} (out of ${totalQueries} total ${totalQueries === 1 ? 'query' : 'queries'}).`;
    } else {
      message = `Displaying all of the ${shownQueries} ${shownQueries === 1 ? 'query' : 'queries'}.`
    }
    return (
      <div>
        <div className="pt-control-group">
          <div className="pt-input-group">
            <Icon iconName="search" iconSize="inherit"/>
            <TextInput
              placeholder="Filter queries..."
              value={this.state.filter}
              onChange={this.handleFilterChange}/>
          </div>
        </div>

        <div style={{ marginTop: '10px', marginBottom: '10px' }}>
          <Callout intent={Intent.PRIMARY} icon={null}>{message}</Callout>
        </div>

        <table className="pt-html-table pt-small pt-html-table-striped">
          <thead>
          <tr>
            <th style={{ width: '80px' }}>#</th>
            <th style={{ width: '400px' }}>Query</th>
            <th style={{ width: '100px' }}>Type</th>
          </tr>
          </thead>
          <tbody>
          {rows}
          </tbody>
        </table>
      </div>
    );
  }
}

VocabularyTable.propTypes = {
  onClick: PropTypes.func,
  campaign: PropTypes.object.isRequired,
};

export default VocabularyTable;
