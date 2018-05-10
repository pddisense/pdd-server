import React from 'react';
import PropTypes from 'prop-types';
import { noop } from 'lodash';
import { Switch, NumericInput } from '@blueprintjs/core';
import { DateInput } from '@blueprintjs/datetime';
import autobind from 'autobind-decorator';

import FormGroup from '../form/FormGroup';

function attrsToState(attrs) {
  return {
    startTime: attrs.startTime ? new Date(attrs.startTime) : null,
    endTime: attrs.endTime ? new Date(attrs.endTime) : null,
    delay: attrs.delay,
    graceDelay: attrs.graceDelay,
    rawStrategyEnabled: attrs.rawStrategy.enabled,
    privacyStrategyEnabled: attrs.privacyStrategy.enabled,
    groupSize: attrs.privacyStrategy.groupSize,
    minGroupSize: attrs.privacyStrategy.minGroupSize,
  };
}

function stateToAttrs(state) {
  return {
    startTime: state.startTime ? state.startTime.toISOString() : null,
    endTime: state.endTime ? state.endTime.toISOString() : null,
    delay: state.delay,
    graceDelay: state.graceDelay,
    rawStrategy: {
      enabled: state.rawStrategyEnabled,
    },
    privacyStrategy: {
      enabled: state.privacyStrategyEnabled,
      groupSize: state.groupSize,
      minGroupSize: state.minGroupSize,
    }
  };
}

class StrategyForm extends React.Component {
  constructor(props) {
    super(props);
    this.state = attrsToState(props.item.attrs);
  }

  @autobind
  handleStartTimeChange(startTime) {
    this.setState({ startTime });
  }

  @autobind
  handleEndTimeChange(endTime) {
    this.setState({ endTime });
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
  handleMinGroupSizeChange(minGroupSize) {
    this.setState({ minGroupSize });
  }

  @autobind
  handleRawStrategyEnabledClick() {
    this.setState({ rawStrategyEnabled: !this.state.rawStrategyEnabled });
  }

  @autobind
  handlePrivacyStrategyEnabledClick() {
    this.setState({ privacyStrategyEnabled: !this.state.privacyStrategyEnabled });
  }

  @autobind
  handleSubmit(e) {
    e.preventDefault();
    const obj = {
      ...this.props.item,
      attrs: { ...this.props.item.attrs, ...stateToAttrs(this.state) },
    };
    this.props.onSubmit(obj);
  }

  render() {
    return (
      <form onSubmit={this.handleSubmit}>
        <FormGroup
          title="Start date"
          help="The start of the period of interest during which queries are aggregated.
                If left empty, the campaign will remain hidden.">
          <DateInput value={this.state.startTime} onChange={this.handleStartTimeChange} />
        </FormGroup>

        <FormGroup
          title="End date"
          help="The end of the period of interest during which queries are aggregated.
                Because of delays that may be introduced by the system, users may still submit their
                data after this date.
                If left empty, it will be an open-ended campaign.">
          <DateInput value={this.state.endTime} onChange={this.handleEndTimeChange} />
        </FormGroup>

        <FormGroup
          title="Delay"
          help="A delay to introduce before results are made available.
                Increasing this delay increases the expected accuracy. Expressed in days.">
          <NumericInput
            value={this.state.delay}
            onValueChange={this.handleDelayChange}
            min={0} />
        </FormGroup>

        <FormGroup
          title="Grace delay"
          help="An additional delay during which results are already be made available but queries
                can still be submitted by the users. Increasing this delay increases the expected
                accuracy. Expressed in days.">
          <NumericInput
            value={this.state.graceDelay}
            onValueChange={this.handleGraceDelayChange}
            min={0} />
        </FormGroup>

        <div className="pt-form-group">
          <Switch
            checked={this.state.rawStrategyEnabled}
            label="Enable raw data collection"
            onChange={this.handleRawStrategyEnabledClick}/>
        </div>

        <div className="pt-form-group">
          <Switch
            checked={this.state.privacyStrategyEnabled}
            label="Enable privacy-preserving data collection"
            onChange={this.handlePrivacyStrategyEnabledClick}/>
        </div>

        {this.state.privacyStrategyEnabled ?
          <div>
            <FormGroup
              title="Group size"
              help="Expected number of users within groups. This value is only a hint.
                    Larger groups increase privacy of users, but may decrease the accuracy of results.">
              <NumericInput
                value={this.state.groupSize}
                onValueChange={this.handleGroupSizeChange}
                min={this.state.minGroupSize} />
            </FormGroup>

            <FormGroup
              title="Minimum group size"
              help="Minimum number of users within groups. The system will never create smaller
                    groups.">
              <NumericInput
                value={this.state.minGroupSize}
                onValueChange={this.handleMinGroupSizeChange}
                min={2}/>
            </FormGroup>
          </div> : null}

        <button type="submit" className="pt-button pt-intent-primary">Submit</button>
      </form>
    );
  }
}

StrategyForm.propTypes = {
  onSubmit: PropTypes.func,
  item: PropTypes.object.isRequired,
};

StrategyForm.defaultProps = {
  onSubmit: noop,
};

export default StrategyForm;
