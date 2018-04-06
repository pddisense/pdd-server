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
import { Dialog, Intent } from '@blueprintjs/core';
import autobind from 'autobind-decorator';
import { noop } from 'lodash';

import FormGroup from './form/FormGroup';
import TextInput from './form/TextInput';
import toaster from './toaster';
import { authenticate } from '../util/auth';

class LoginDialog extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      password: '',
    };
  }

  @autobind
  handleChange(e) {
    this.setState({ password: e.target.value });
  }

  @autobind
  handleSubmit(e) {
    e.nativeEvent.preventDefault();
    authenticate(this.state.password).then(authenticated => {
      if (authenticated) {
        toaster.show({
          message: 'You have been successfully authenticated. Welcome back!',
          intent: Intent.SUCCESS,
        });
        this.props.onLogin();
      } else {
        toaster.show({
          message: 'Wrong password, please try again.',
          intent: Intent.DANGER,
        });
      }
    })
  }

  render() {
    return (
      <Dialog isOpen={true}
              canEscapeKeyClose={false}
              canOutsideClickClose={false}
              isCloseButtonShown={false}
              title="Authentication required">
        <form onSubmit={this.handleSubmit}>
          <div className="pt-dialog-body">
            <FormGroup title="Enter your password">
              <TextInput required
                         type="password"
                         autoFocus
                         onChange={this.handleChange}
                         value={this.state.password}/>
            </FormGroup>
          </div>
          <div className="pt-dialog-footer">
            <div className="pt-dialog-footer-actions">
              <button type="submit" className="pt-button pt-intent-primary">Log in</button>
              <a role="button" className="pt-button" href="https://ppd.cs.ucl.ac.uk">Exit</a>
            </div>
          </div>
        </form>
      </Dialog>
    );
  }
}

LoginDialog.propTypes = {
  onLogin: PropTypes.func.isRequired,
};

LoginDialog.defaultProps = {
  onLogin: noop,
};

export default LoginDialog;
