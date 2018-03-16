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
import {noop} from 'lodash';
import {NumericInput, Switch} from '@blueprintjs/core';
import {DateInput} from '@blueprintjs/datetime';
import autobind from 'autobind-decorator';

import FormGroup from '../form/FormGroup';

function attrsToState(campaign) {
  return {
    startTime: campaign.startTime ? new Date(campaign.startTime) : null,
    endTime: campaign.endTime ? new Date(campaign.endTime) : null,
    delay: campaign.delay,
    graceDelay: campaign.graceDelay,
    collectRaw: campaign.collectRaw,
    collectEncrypted: campaign.collectEncrypted,
    groupSize: campaign.groupSize,
    samplingRate: campaign.samplingRate ? campaign.samplingRate * 100 : 100,
  };
}

function stateToAttrs(state) {
  return {
    startTime: state.startTime ? state.startTime.toISOString() : null,
    endTime: state.endTime ? state.endTime.toISOString() : null,
    delay: state.delay,
    graceDelay: state.graceDelay,
    collectRaw: state.collectRaw,
    collectEncrypted: state.collectEncrypted,
    groupSize: state.groupSize,
    samplingRate: state.samplingRate / 100,
  };
}

class StrategyForm extends React.Component {
  constructor(props) {
    super(props);
    this.state = attrsToState(props.campaign);
  }

  @autobind
  handleStartTimeChange(startTime) {
    this.setState({startTime});
  }

  @autobind
  handleEndTimeChange(endTime) {
    this.setState({endTime});
  }

  @autobind
  handleDelayChange(delay) {
    this.setState({delay});
  }

  @autobind
  handleGraceDelayChange(graceDelay) {
    this.setState({graceDelay});
  }

  @autobind
  handleGroupSizeChange(groupSize) {
    this.setState({groupSize});
  }

  @autobind
  handleSamplingRateChange(samplingRate) {
    this.setState({samplingRate});
  }

  @autobind
  handleCollectRawClick() {
    this.setState({collectRaw: !this.state.collectRaw});
  }

  @autobind
  handleCollectEncryptedClick() {
    this.setState({collectEncrypted: !this.state.collectEncrypted});
  }

  @autobind
  handleSubmit(e) {
    e.preventDefault();
    const obj = {...this.props.campaign, ...stateToAttrs(this.state)};
    this.props.onSubmit(obj);
  }

  render() {
    return (
      <form onSubmit={this.handleSubmit}>
        <FormGroup
          title="Start date"
          help="The start of the period of interest during which queries are aggregated.
                If left empty, the campaign will remain hidden.">
          <DateInput value={this.state.startTime}
                     onChange={this.handleStartTimeChange}
                     formatDate={date => date.toLocaleDateString()}
                     parseDate={str => new Date(str)}/>
        </FormGroup>

        <FormGroup
          title="End date"
          help="The end of the period of interest during which queries are aggregated.
                Because of delays that may be introduced by the system, users may still submit their
                data after this date.
                If left empty, it will be an open-ended campaign.">
          <DateInput value={this.state.endTime}
                     onChange={this.handleEndTimeChange}
                     formatDate={date => date.toLocaleDateString()}
                     parseDate={str => new Date(str)}/>
        </FormGroup>

        <FormGroup
          title="Delay"
          help="A delay to introduce before results are made available.
                Increasing this delay increases the expected accuracy. Expressed in days.">
          <NumericInput
            value={this.state.delay}
            onValueChange={this.handleDelayChange}
            min={0}/>
        </FormGroup>

        <FormGroup
          title="Grace delay"
          help="An additional delay during which results are already be made available but queries
                can still be submitted by the users. Increasing this delay increases the expected
                accuracy. Expressed in days.">
          <NumericInput
            value={this.state.graceDelay}
            onValueChange={this.handleGraceDelayChange}
            min={0}/>
        </FormGroup>

        <div className="pt-form-group">
          <Switch
            checked={this.state.collectRaw}
            label="Enable raw data collection"
            onChange={this.handleCollectRawClick}/>
        </div>

        <div className="pt-form-group">
          <Switch
            checked={this.state.collectEncrypted}
            label="Enable privacy-preserving data collection"
            onChange={this.handleCollectEncryptedClick}/>
        </div>

        {this.state.collectEncrypted ?
          <FormGroup
            title="Group size"
            help="Expected number of users within groups. This value is only a hint.
                  Larger groups increase privacy of users, but may decrease the accuracy of results.">
            <NumericInput
              value={this.state.groupSize}
              onValueChange={this.handleGroupSizeChange}
              min={2}/>
          </FormGroup> : null}

        <FormGroup
          title="Sampling rate (%)"
          help="Proportion of users to sample every day.">
          <NumericInput
            value={this.state.samplingRate}
            onValueChange={this.handleSamplingRateChange}
            min={0}
            max={100}
            stepSize={5}/>
        </FormGroup>

        <button type="submit" className="pt-button pt-intent-primary">Submit</button>
      </form>
    );
  }
}

StrategyForm.propTypes = {
  onSubmit: PropTypes.func,
  campaign: PropTypes.object.isRequired,
};

StrategyForm.defaultProps = {
  onSubmit: noop,
};

export default StrategyForm;
