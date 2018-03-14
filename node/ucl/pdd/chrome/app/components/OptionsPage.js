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
import { Tab, Tabs } from '@blueprintjs/core';

import FaqSection from './FaqSection';
import AboutSection from './AboutSection';
import HistorySectionContainer from './HistorySectionContainer';

export default class OptionsPage extends React.Component {
  render() {
    return (
      <div>
        <Tabs id="tabs" renderActiveTabPanelOnly={true}>
          <Tab id="about" title="About PDD" panel={<AboutSection/>}/>
          <Tab id="history" title="Search history" panel={<HistorySectionContainer/>}/>
          <Tab id="faq" title="FAQ" panel={<FaqSection/>}/>
          <Tabs.Expander/>
        </Tabs>
      </div>
    );
  }
}
