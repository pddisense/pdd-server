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
import { noop } from 'lodash';

import HistoryTableContainer from './HistoryTableContainer';
import { isBefore1am } from '../util/dates';

class HistorySection extends React.Component {
  handleClick(idx) {
    const blacklist = this.props.localData.blacklist
      ? { queries: this.props.localData.blacklist.queries.slice() }
      : { queries: [] };
    blacklist.queries.push(this.props.localData.vocabulary.queries[idx]);
    this.props.onChange({ blacklist });
  }

  render() {
    const yesterday = isBefore1am(moment());
    return (
      <div>
        <p>
          This page shows the search queries that Private Data Donor has detected
          for {yesterday ? 'yesterday' : 'today'}.
          Those that are of interest will be automatically sent {yesterday ? '' : 'tomorrow'} after 1am, as soon as your browser has an Internet access.
          If you do not want some keywords to be monitored, you can choose to blacklist them by
          clicking on the button on the right-hand side of each keyword.
          They will be permanently blocked.
        </p>
        <HistoryTableContainer localData={this.props.localData}
                               onClick={idx => this.handleClick(idx)}/>
      </div>
    );
  }
}

HistorySection.propTypes = {
  localData: PropTypes.object.isRequired,
};

HistorySection.defaultProps = {
  onChange: noop,
};

export default HistorySection;
