import React from 'react';
import { Tab, Tabs } from '@blueprintjs/core';

import FaqSection from './FaqSection';
import AboutSection from './AboutSection';
import HistorySectionContainer from './HistorySectionContainer';
import CampaignsSectionContainer from './CampaignsSectionContainer';

export default class OptionsPage extends React.Component {
  render() {
    return (
      <div>
        <Tabs id="tabs" renderActiveTabPanelOnly={true}>
          <Tab id="about" title="About PDD" panel={<AboutSection/>}/>
          <Tab id="campaigns" title="Campaigns" panel={<CampaignsSectionContainer/>}/>
          <Tab id="history" title="Search history" panel={<HistorySectionContainer/>}/>
          <Tab id="faq" title="FAQ" panel={<FaqSection/>}/>
          <Tabs.Expander/>
        </Tabs>
      </div>
    );
  }
}
