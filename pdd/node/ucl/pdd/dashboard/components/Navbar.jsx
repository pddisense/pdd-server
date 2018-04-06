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
import { Link, withRouter } from 'react-router-dom';
import { noop } from 'lodash';

import { logout } from '../util/auth';

const LINKS = [
  { title: 'Dashboard', path: '/', icon: 'dashboard' },
  { title: 'Campaigns', path: '/campaigns', icon: 'projects' },
  { title: 'Clients', path: '/clients', icon: 'people' },
];

@withRouter
class Navbar extends React.Component {
  @autobind
  handleLogout() {
    logout();
    this.props.onLogout();
  }

  render() {
    const items = LINKS.map((link, idx) => {
      let active;
      if (link.path === '/') {
        active = this.props.location.pathname === '/';
      } else {
        active = this.props.location.pathname.indexOf(link.path) === 0;
      }
      return (
        <Link to={link.path} key={idx}
              className={`pt-button pt-minimal pt-icon-${link.icon} ${active ? 'pt-active' : ''}`}>
          {link.title}
        </Link>
      );
    });

    return (
      <nav className="pt-navbar pt-dark">
        <div className="container">
          <div className="pt-navbar-group pt-align-left">
            <div className="pt-navbar-heading">Private Data Donor</div>
            {items}
          </div>
          <div className="pt-navbar-group pt-align-right">
            <a className="pt-button pt-minimal" onClick={this.handleLogout}>Logout</a>
          </div>
        </div>
      </nav>
    );
  }
}

Navbar.propTypes = {
  onLogout: PropTypes.func.isRequired,
};

Navbar.defaultProps = {
  onLogout: noop,
};

export default Navbar;
