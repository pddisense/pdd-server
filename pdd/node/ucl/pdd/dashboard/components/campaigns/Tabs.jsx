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
import { withRouter } from 'react-router-dom';
import autobind from 'autobind-decorator';

const LINKS = [
  {
    title: 'Overview',
    action: 'view',
  },
  {
    title: 'Results',
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
