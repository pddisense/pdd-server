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
import autobind from 'autobind-decorator';
import { noop } from 'lodash';

function attrsToState(client) {
  return {
    externalName: client.externalName || '',
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
    this.state = attrsToState(props.client);
  }

  componentWillReceiveProps(nextProps) {
    this.setState(attrsToState(nextProps.client));
  }

  @autobind
  handleExternalNameChange(event) {
    this.setState({ externalName: event.target.value });
  }

  @autobind
  handleBlur() {
    this.props.onChange(stateToAttrs(this.state));
  }

  render() {
    return (
      <div>
        <h1>Settings</h1>

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
            </div>
          </div>
        </div>

        <div className="pt-form-group">
          <label className="pt-label" htmlFor="external-name">
            Client name
          </label>
          <div className="pt-form-content">
            {this.props.client.name}
            <div className="pt-form-helper-text">
              This is the internal name that is used to identify you from our server.
              Should you encounter any issues, our support may ask you this name.
            </div>
          </div>
        </div>
      </div>
    );
  }
}

SettingsSection.propTypes = {
  client: PropTypes.object.isRequired,
  onChange: PropTypes.func,
};

SettingsSection.defaultProps = {
  onChange: noop,
};

export default SettingsSection;
