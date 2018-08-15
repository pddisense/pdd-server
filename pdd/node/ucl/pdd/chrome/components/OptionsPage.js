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
import { Tab, Tabs } from '@blueprintjs/core';

import FaqSection from './FaqSection';
import AboutSection from './AboutSection';
import HistorySectionContainer from './HistorySectionContainer';
import BlacklistSectionContainer from './BlacklistSectionContainer';
import SettingsSectionContainer from './SettingsSectionContainer';

export default class OptionsPage extends React.Component {
  render() {
    return (
      <div>
        <Tabs id="tabs" renderActiveTabPanelOnly={true}>
          <Tab id="about" title="About PDD" panel={<AboutSection/>}/>
          <Tab id="history" title="History" panel={<HistorySectionContainer/>}/>
          <Tab id="blacklist" title="Blacklist" panel={<BlacklistSectionContainer/>}/>
          <Tab id="advanced" title="Advanced" panel={<SettingsSectionContainer/>}/>
          <Tab id="faq" title="FAQ" panel={<FaqSection/>}/>
          <Tabs.Expander/>
        </Tabs>
      </div>
    );
  }
}
