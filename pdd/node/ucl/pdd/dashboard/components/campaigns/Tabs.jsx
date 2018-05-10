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
import { withRouter } from 'react-router-dom';
import autobind from 'autobind-decorator';

const LINKS = [
  {
    title: 'Overview',
    action: 'view',
  },
  {
    title: 'Preview results',
    action: 'results',
  },
  {
    title: 'Edit metadata',
    action: 'edit',
    what: 'metadata',
  },
  {
    title: 'Edit vocabulary',
    action: 'edit',
    what: 'vocabulary',
  },
  {
    title: 'Edit strategy',
    action: 'edit',
    what: 'strategy',
  },
];

@withRouter
class Tabs extends React.Component {
  @autobind
  handleClick(e, idx) {
    e.preventDefault();
    const link = LINKS[idx];
    let url = `/campaigns/${link.action}/${this.props.campaign.name}`;
    if (link.what) {
      url += `/${link.what}`;
    }
    if (this.props.location.pathname !== url) {
      this.props.history.push(url);
    }
  }

  render() {
    const path = this.props.location.pathname.split('/');
    const tabs = LINKS.map((link, idx) => {
      const active = path[2] === link.action && link.what === path[4];
      return (
        <li
          className="pt-tab"
          role="tab"
          key={idx}
          onClick={e => this.handleClick(e, idx)}
          aria-selected={active}>
          {link.title}
        </li>
      );
    });
    return (
      <div className="pt-tabs rythmed">
        <ul className="pt-tab-list" role="tablist">
          {tabs}
        </ul>
      </div>
    );
  }
}

Tabs.propTypes = {
  campaign: PropTypes.object,
};

export default Tabs;
