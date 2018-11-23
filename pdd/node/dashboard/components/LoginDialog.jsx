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
              <a role="button" className="pt-button" href="https://ppd.cs.ucl.ac.uk">Exit</a>
              <button type="submit" className="pt-button pt-intent-primary">Log in</button>
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
