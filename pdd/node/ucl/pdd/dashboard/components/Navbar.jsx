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
import { withRouter, Link } from 'react-router-dom';

const LINKS = [
  { title: 'Dashboard', path: '/' },
  { title: 'Campaigns', path: '/campaigns' },
  { title: 'Clients', path: '/clients' },
];

@withRouter
export default class Navbar extends React.Component {
  render() {
    const items = LINKS.map((link, idx) => {
      let active;
      if (link.path === '/') {
        active = this.props.location.pathname === '/';
      } else {
        active = this.props.location.pathname.indexOf(link.path) === 0;
      }
      return (
        <Link to={link.path} key={idx} className={'pt-button pt-minimal ' + (active ? 'pt-active' : '')}>
          {link.title}
        </Link>
      );
    });

    /*
    <div className="pt-navbar-group pt-align-right">
          <input className="pt-input" placeholder="Search..." type="text" />
          <span className="pt-navbar-divider"></span>
          <button className="pt-button pt-minimal pt-icon-user"></button>
          <button className="pt-button pt-minimal pt-icon-notifications"></button>
          <button className="pt-button pt-minimal pt-icon-cog"></button>
        </div>
     */

    return (
      <nav className="pt-navbar">
        <div className="container">
          <div className="pt-navbar-group pt-align-left">
            <div className="pt-navbar-heading">Private Data Donor</div>
            {items}
          </div>
        </div>
      </nav>
    );
  }
};
