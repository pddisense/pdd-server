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
import autobind from 'autobind-decorator';
import { Button } from '@blueprintjs/core';
import { noop } from 'lodash';

import sendPing from '../protocol/ping.js';

function attrsToState(localData) {
  return {
    externalName: localData.externalName || '',
  };
}

function stateToAttrs(state) {
  return {
    externalName: state.externalName.length > 0 ? state.externalName : null,
  };
}

class SettingsSection extends React.Component {
  constructor(props) {
    super(props);
    this.state = attrsToState(props.localData);
  }

  componentWillReceiveProps(nextProps) {
    this.setState(attrsToState(nextProps.localData));
  }

  @autobind
  handleExternalNameChange(event) {
    this.setState({ externalName: event.target.value });
  }

  @autobind
  handleBlur() {
    this.props.onChange(stateToAttrs(this.state));
  }

  @autobind
  handleClick() {
    if (this.props.localData.name) {
      sendPing(this.props.localData);
    }
  }

  render() {
    return (
      <div>
        <div className="pt-form-group">
          <label className="pt-label" htmlFor="external-name">
            Participant identifier
          </label>
          <div className="pt-form-content">
            <input id="external-name"
                   className="pt-input"
                   style={{'width': '300px'}}
                   type="text"
                   dir="auto"
                   value={this.state.externalName}
                   onChange={this.handleExternalNameChange}
                   onBlur={this.handleBlur}/>
            <div className="pt-form-helper-text">
              If you are participating to a study and have been assigned an identifier, please
              indicate it here.
              You should leave it empty if you have not been instructed to fill this field.
            </div>
          </div>
        </div>

        <div className="pt-form-group">
          <label className="pt-label" htmlFor="external-name">
            Client name
          </label>
          <div className="pt-form-content">
            {this.props.localData.name || '(not yet registered)'}
            <div className="pt-form-helper-text">
              This is the internal name that is used to identify the data you contribute.
              Should you encounter any issues, our technical support may ask you this value.
            </div>
          </div>
        </div>

        <div className="pt-form-group">
          <Button fill={true}
                  onClick={this.handleClick}
                  text="Force synchronization now"
                  disabled={!this.props.localData.name}
                  icon="refresh"/>
          <div className="pt-form-helper-text">
            By clicking on this button, you will force a data synchronization now.
            You should not need to use that button under normal conditions.
            It may be used by power users, or in case our technical support asks you to do so.
          </div>
        </div>
      </div>
    );
  }
}

SettingsSection.propTypes = {
  localData: PropTypes.object.isRequired,
  onChange: PropTypes.func,
};

SettingsSection.defaultProps = {
  onChange: noop,
};

export default SettingsSection;
