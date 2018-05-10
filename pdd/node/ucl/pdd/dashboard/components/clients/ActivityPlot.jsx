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

const DAYS_COUNT = 14;

class ActivityPlot extends React.Component {
  render() {
    const { activity } = this.props;
    const data = [];
    const beginTime = moment().startOf('day').subtract(DAYS_COUNT - 1, 'days');
    range(0, DAYS_COUNT).forEach(day => {
      const startTime = beginTime.clone().add(day, 'days');
      const endTime = startTime.clone().add(1, 'day');
      const count = activity.days.filter(item => moment(item.time).isBetween(startTime, endTime)).length;
      data.push({ x: startTime.valueOf(), y: count });
    });
    return (
      <div className="plot-container">
        <XYPlot xType="time" width={800} height={200}>
          <HorizontalGridLines/>
          <VerticalGridLines/>
          <XAxis tickTotal={DAYS_COUNT}/>
          <YAxis/>
          <LineSeries data={data}/>
        </XYPlot>
        <div className="title" style={{width: '800px'}}>
          Client activity over the past {DAYS_COUNT} days
        </div>
      </div>
    );
  }
}

ActivityPlot.propTypes = {
  activity: PropTypes.object.isRequired,
};

export default ActivityPlot;
