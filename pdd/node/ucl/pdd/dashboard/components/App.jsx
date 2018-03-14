import React from 'react';
import { Route } from 'react-router-dom';

import Navbar from './Navbar';
import Dashboard from './Dashboard';
import CampaignList from './campaigns/CampaignList';
import NewCampaignContainer from './campaigns/NewCampaignContainer';
import ViewCampaignContainer from './campaigns/ViewCampaignContainer';
import EditCampaignContainer from './campaigns/EditCampaignContainer';
import EditVocabularyContainer from './campaigns/EditVocabularyContainer';
import EditStrategyContainer from './campaigns/EditStrategyContainer';
import ClientList from './clients/ClientList';

export default class App extends React.Component {
  render() {
    return (
      <div className="page">
        <Navbar/>
        <div className="container content">
          <Route exact path="/" component={Dashboard}/>

          <Route exact path="/campaigns" component={CampaignList}/>
          <Route exact path="/campaigns/new" component={NewCampaignContainer}/>
          <Route exact path="/campaigns/view/:name" component={ViewCampaignContainer}/>
          <Route exact path="/campaigns/edit/:name/metadata" component={EditCampaignContainer}/>
          <Route exact path="/campaigns/edit/:name/vocabulary" component={EditVocabularyContainer}/>
          <Route exact path="/campaigns/edit/:name/strategy" component={EditStrategyContainer}/>

          <Route exact path="/clients" component={ClientList}/>
        </div>
      </div>
    );
  }
};
