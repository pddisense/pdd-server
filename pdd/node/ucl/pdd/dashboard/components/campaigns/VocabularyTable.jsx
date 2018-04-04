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
import { some, noop } from 'lodash';
import { Icon, Callout, Intent } from '@blueprintjs/core';
import autobind from 'autobind-decorator';

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

  @autobind
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
