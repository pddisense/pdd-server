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
import { Spinner } from '@blueprintjs/core';
import { Route } from 'react-router-dom';
import autobind from 'autobind-decorator';

import Navbar from './Navbar';
import DashboardContainer from './dashboard/DashboardContainer';
import CampaignList from './campaigns/CampaignList';
import NewCampaignContainer from './campaigns/NewCampaignContainer';
import ViewCampaignContainer from './campaigns/ViewCampaignContainer';
import ViewResultsContainer from './campaigns/ViewResultsContainer';
import EditCampaignContainer from './campaigns/EditCampaignContainer';
import EditVocabularyContainer from './campaigns/EditVocabularyContainer';
import EditStrategyContainer from './campaigns/EditStrategyContainer';
import ClientList from './clients/ClientList';
import ViewClientContainer from './clients/ViewClientContainer';
import LoginDialog from './LoginDialog';
import { checkAuthenticated } from '../util/auth';

export default class App extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      loading: true,
      authenticated: false,
    };
  }

  componentDidMount() {
    checkAuthenticated().then(authenticated => this.setState({ authenticated, loading: false }));
  }

  @autobind
  handleLogin() {
    this.setState({ authenticated: true });
  }

  @autobind
  handleLogout() {
    this.setState({ authenticated: false });
  }

  render() {
    let content;
    if (this.state.loading) {
      content = <Spinner/>;
    } else if (!this.state.authenticated) {
      content = <LoginDialog onLogin={this.handleLogin}/>;
    } else {
      content = [
        <Route exact path="/" component={DashboardContainer}/>,
        <Route exact path="/campaigns" component={CampaignList}/>,
        <Route exact path="/campaigns/new" component={NewCampaignContainer}/>,
        <Route exact path="/campaigns/view/:name" component={ViewCampaignContainer}/>,
        <Route exact path="/campaigns/results/:name" component={ViewResultsContainer}/>,
        <Route exact path="/campaigns/edit/:name/metadata" component={EditCampaignContainer}/>,
        <Route exact path="/campaigns/edit/:name/vocabulary" component={EditVocabularyContainer}/>,
        <Route exact path="/campaigns/edit/:name/strategy" component={EditStrategyContainer}/>,
        <Route exact path="/clients" component={ClientList}/>,
        <Route exact path="/clients/view/:name" component={ViewClientContainer}/>,
      ];
      content = React.Children.map(content, (route, idx) => React.cloneElement(route, { key: idx }));
    }
    return (
      <div className="page">
        <Navbar onLogout={this.handleLogout}/>
        <div className="container content">
          {content}
        </div>
      </div>
    );
  }
};
