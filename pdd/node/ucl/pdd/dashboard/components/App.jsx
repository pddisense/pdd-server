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
import { Spinner } from '@blueprintjs/core';
import { Route } from 'react-router-dom';
import autobind from 'autobind-decorator';

import Navbar from './Navbar';
import Dashboard from './dashboard/Dashboard';
import CampaignList from './campaigns/CampaignList';
import NewCampaignContainer from './campaigns/NewCampaignContainer';
import ViewCampaignContainer from './campaigns/ViewCampaignContainer';
import ViewResultsContainer from './campaigns/ViewResultsContainer';
import EditCampaignContainer from './campaigns/EditCampaignContainer';
import EditVocabularyContainer from './campaigns/EditVocabularyContainer';
import EditStrategyContainer from './campaigns/EditStrategyContainer';
import ClientList from './clients/ClientList';
import { checkAuthenticated } from '../util/auth';
import LoginDialog from './LoginDialog';

export default class App extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      loading: true,
      authenticated: false,
    };
  }

  componentDidMount() {
    checkAuthenticated()
      .then(authenticated => this.setState({ authenticated, loading: false }));
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
      content = (
        <div className="container content">
          <Route exact path="/" component={Dashboard}/>
          <Route exact path="/campaigns" component={CampaignList}/>
          <Route exact path="/campaigns/new" component={NewCampaignContainer}/>
          <Route exact path="/campaigns/view/:name" component={ViewCampaignContainer}/>
          <Route exact path="/campaigns/results/:name" component={ViewResultsContainer}/>
          <Route exact path="/campaigns/edit/:name/metadata" component={EditCampaignContainer}/>
          <Route exact path="/campaigns/edit/:name/vocabulary"
                 component={EditVocabularyContainer}/>
          <Route exact path="/campaigns/edit/:name/strategy" component={EditStrategyContainer}/>
          <Route exact path="/clients" component={ClientList}/>
        </div>
      );
    }
    return (
      <div className="page">
        <Navbar onLogout={this.handleLogout}/>
        {content}
      </div>
    );
  }
};
