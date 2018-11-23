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

export default class Dashboard extends React.Component {
  render() {
    return (
      <div className="stats-row">
        <div className="pt-card">
          <div className="figure">{this.props.stats.activeCampaigns}</div>
          <div className="label">active campaigns</div>
        </div>
        <div className="pt-card">
          <div className="figure">{this.props.stats.activeClients}</div>
          <div className="label">active clients</div>
        </div>
      </div>
    );
  }
};
