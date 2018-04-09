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
import {
  HorizontalGridLines,
  LineSeries,
  VerticalGridLines,
  XAxis,
  XYPlot,
  YAxis
} from 'react-vis';
import 'react-vis/dist/style.css';
import moment from 'moment';
import { range } from 'lodash';

class ActivityPlot extends React.Component {
  render() {
    const { activity, days } = this.props;
    const data = [];
    const beginTime = moment().startOf('day').subtract(days, 'days');
    range(0, days + 1).forEach(day => {
      const startTime = beginTime.clone().add(day, 'days');
      const endTime = startTime.clone().add(1, 'day');
      console.log(startTime.toString() + ' - ' + endTime.toString());
      const count = activity.days.filter(item => moment(item.time).isBetween(startTime, endTime)).length;
      data.push({ x: startTime.valueOf(), y: count });
    });
    return (
      <div className="plot-container">
        <XYPlot xType="time" width={800} height={200}>
          <HorizontalGridLines/>
          <VerticalGridLines/>
          <XAxis tickTotal={days}/>
          <YAxis/>
          <LineSeries data={data}/>
        </XYPlot>
        <div className="title" style={{width: '800px'}}>
          Client activity over the past {days} days
        </div>
      </div>
    );
  }
}

ActivityPlot.propTypes = {
  activity: PropTypes.object.isRequired,
  days: PropTypes.number.isRequired,
};

ActivityPlot.defaultProps = {
  days: 14,
};

export default ActivityPlot;
