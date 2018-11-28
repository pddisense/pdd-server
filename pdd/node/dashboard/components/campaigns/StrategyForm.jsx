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
import { noop } from 'lodash';
import { NumericInput, Switch, Callout, Intent } from '@blueprintjs/core';
import autobind from 'autobind-decorator';

import FormGroup from '../form/FormGroup';

function attrsToState(campaign) {
  return {
    delay: campaign.delay,
    graceDelay: campaign.graceDelay,
    groupSize: campaign.groupSize,
    hasSamplingRate: !!campaign.samplingRate,
    samplingRate: campaign.samplingRate ? campaign.samplingRate * 100 : 100,
  };
}

function stateToAttrs(state) {
  return {
    delay: state.delay,
    graceDelay: state.graceDelay,
    groupSize: state.groupSize,
    samplingRate: state.hasSamplingRate ? state.samplingRate / 100 : null,
  };
}

class StrategyForm extends React.Component {
  constructor(props) {
    super(props);
    this.state = attrsToState(props.campaign);
  }

  @autobind
  handleDelayChange(delay) {
    this.setState({ delay });
  }

  @autobind
  handleGraceDelayChange(graceDelay) {
    this.setState({ graceDelay });
  }

  @autobind
  handleGroupSizeChange(groupSize) {
    this.setState({ groupSize });
  }

  @autobind
  handleSamplingRateChange(samplingRate) {
    this.setState({ samplingRate });
  }

  @autobind
  handleSamplingRateClick() {
    this.setState({ hasSamplingRate: !this.state.hasSamplingRate });
  }

  @autobind
  handleSubmit(e) {
    e.preventDefault();
    const obj = { ...this.props.campaign, ...stateToAttrs(this.state) };
    this.props.onSubmit(obj);
  }

  render() {
    if (this.props.campaign.started) {
      return (
        <Callout intent={Intent.DANGER}>
          The strategy cannot be modified anymore because this campaign has already started.
        </Callout>
      );
    }
    return (
      <form onSubmit={this.handleSubmit}>
        <p className="pt-ui-text-large" style={{ marginBottom: '25px' }}>
          This page allows to configure the collection strategy that is deployed by this campaign.
        </p>

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

        <FormGroup
          title="Group size"
          help="Expected number of users within groups. This value is only a hint.
                Larger groups increase privacy of users, but may decrease the accuracy of results.">
          <NumericInput
            value={this.state.groupSize}
            onValueChange={this.handleGroupSizeChange}
            min={2}/>
        </FormGroup>

        <div className="pt-form-group">
          <Switch
            checked={this.state.hasSamplingRate}
            label="Enable sampling"
            onChange={this.handleSamplingRateClick}/>
        </div>

        {this.state.hasSamplingRate ?
          <FormGroup
            title="Sampling rate (%)"
            help="When sampling is enabled, this specifies a proportion of users that will be uniformly sampled every day to contribute their data.">
            <NumericInput
              value={this.state.samplingRate}
              onValueChange={this.handleSamplingRateChange}
              min={0}
              max={100}
              stepSize={5}/>
          </FormGroup> : null}

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
