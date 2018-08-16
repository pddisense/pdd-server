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
import { Spinner } from '@blueprintjs/core';

import { searchHistory } from '../browser/history';
import { aggregateCounters } from '../protocol/history';
import { isBefore1am } from '../util/dates';

export default function withSearchHistory(WrappedComponent) {
  return class HistorySectionContainer extends React.Component {
    constructor(props) {
      super(props);
      this.state = {
        history: null,
      };
    }

    fetchData() {
      if (!this.props.localData.vocabulary) {
        // The vocabulary is not yet available, likely because the first ping has not been
        // realised. We cannot display anything meaningful, so the spinner will remain.
        return;
      }
      if (this.props.localData.vocabulary.queries.length === 0) {
        // There is a vocabulary but it is empty. There is no need to retrieve the search history,
        // we simply set it empty. The spinner will disappear as a consequence.
        this.setState({ history: [] });
      } else {
        // Retrieve the search history only if the vocabulary is non-empty.
        const now = moment();
        let startTime = now.clone().startOf('day');
        if (isBefore1am(now)) {
          // When we are between midnight and 1 o'clock, we display yesterday's searches.
          startTime = startTime.subtract(1, 'day');
        }
        const endTime = startTime.clone().add(1, 'day');
        searchHistory(startTime, endTime).then(data => {
          // Filter the search history to only include keywords actually tracked. However, there is
          // one drawback with this method, which is that vocabulary updates are not taken into
          // account immediately but only at the next ping (i.e., roughly the next day). It may
          // create issues if new keywords are suddenly tracked, and users are not able to prevent
          // them from being sent on the first day after they have been added.
          //
          // However, for now there is no easy fix and we consider that this situation is best than
          // displaying all search queries without indicating which ones are actually tracked.
          // TODO: a possible fix would be to delay the activation of new keywords by one day.
          const blacklist = this.props.localData.blacklist || { queries: [] };
          const history = aggregateCounters(data, this.props.localData.vocabulary, blacklist);
          this.setState({ history })
        });
      }
    }

    componentDidMount() {
      this.fetchData();
    }

    componentDidUpdate(prevProps) {
      if (!prevProps.localData.vocabulary && this.props.localData.vocabulary) {
        // A vocabulary has been created.
        this.fetchData();
      }
      if (!prevProps.localData.blacklist && this.props.localData.blacklist) {
        // A blacklist has been created.
        this.fetchData();
      }
      if (prevProps.localData.blacklist && prevProps.localData.blacklist.queries.length !== this.props.localData.blacklist.queries.length) {
        // The blacklist has changed.
        this.fetchData();
      }
    }

    render() {
      if (this.state.history === null) {
        return <div style={{textAlign: 'center'}}><Spinner/></div>;
      }
      return <WrappedComponent {...this.props} {...this.state}/>;
    }
  };
}
