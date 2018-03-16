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
import moment from 'moment';

import { searchHistory } from '../browser/history';
import HistorySection from './HistorySection';

export default class HistorySectionContainer extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      data: [],
    };
  }

  componentDidMount() {
    const now = moment();
    let startTime;
    if (now.hour() < 1) {
      // We are between midnight and 1 o'clock. We hence display yesterday's searches.
      startTime = now.clone().subtract(1, 'day').startOf('day');
    } else {
      startTime = now.startOf('day');
    }
    const endTime = startTime.clone().endOf('day');
    // TODO: which vocabulary to use?
    searchHistory(startTime, endTime).then(data => this.setState({ data }));
  }

  render() {
    return <HistorySection searches={this.state.data} />;
  }
}
