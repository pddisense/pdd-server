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
import moment from 'moment';
import { searchHistory } from '../browser/history';
import HistorySection from './HistorySection';
import { isBefore1am } from '../util/dates';

export default class HistorySectionContainer extends React.Component {
  constructor(props) {
    super(props);

    this.state = {
      data: [],
      date: new Date(),
    };
  }

  fetchData() {
    // Weirdly, it does not work when passing directory the JS date object.
    let startTime = moment(this.state.date.valueOf()).startOf('day');
    if (isBefore1am(this.state.date.valueOf())) {
      // We are between midnight and 1 o'clock. We hence display yesterday's searches.
      startTime = startTime.subtract(1, 'day');
    }
    const endTime = startTime.clone().add(1, 'day');
    searchHistory(startTime, endTime).then(data => this.setState({ data }));
  }

  handleChange(date) {
    this.setState({ date });
  }

  componentDidMount() {
    this.fetchData();
  }

  componentDidUpdate(prevProps, prevState) {
    if (prevState.date.valueOf() !== this.state.date.valueOf()) {
      this.fetchData();
    }
  }

  render() {
    return <HistorySection searches={this.state.data}
                           date={this.state.date}
                           onChange={date => this.handleChange(date)}/>;
  }
}
