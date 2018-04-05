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
import { Icon  } from '@blueprintjs/core';

class Title extends React.Component {
  render() {
    let iconName;
    let iconTitle;
    if (this.props.campaign.completed) {
      // Finished.
      iconName = 'tick';
      iconTitle = 'Finished campaign';
    } else if (this.props.campaign.started) {
      // Running.
      iconName = 'repeat';
      iconTitle = 'Running campaign';
    } else if (this.props.campaign.startTime) {
      // Scheduled.
      iconName = 'time';
      iconTitle = 'Scheduled campaign';
    } else {
      // Pending.
      iconName = 'issue';
      iconTitle = 'Pending campaign';
    }
    return (
      <h2>
        <Icon icon={iconName}
              title={iconTitle}
              iconSize={20}
              style={{position: 'relative', top: '6px', marginRight: '15px'}}/>
        {this.props.campaign.displayName}
      </h2>
    );
  }
}

Title.propTypes = {
  campaign: PropTypes.object.isRequired,
};

export default Title;
