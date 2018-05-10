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
