import React from 'react';
import moment from 'moment';
import { flatMap } from 'lodash';

import { History, Storage } from '../browser';
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
    const vocabulary = flatMap(Storage.getAll(), item => {
      return item.vocabulary.filter((v, idx) => item.activeQueries[idx]);
    });
    History.search(startTime, endTime, vocabulary).then(data => this.setState({ data }));
  }

  render() {
    return <HistorySection searches={this.state.data} />;
  }
}
