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

export default class AboutSection extends React.Component {
  render() {
    return (
      <div>
        <h1>About Private Data Donor</h1>

        <p>
          Privacy-friendly Data Donor is a tool connecting volunteers with researchers.
          The purpose of this extension is to study the spread of infectious diseases like flu.
          No information you provide will ever be used for commercial purposes or published elsewhere.
          If you would like to learn more about our project, please visit <a
          target="_blank" href="https://ppd.cs.ucl.ac.uk/">our website</a>.
        </p>

        <p>
          Towards this purpose, we monitor search queries that you make on Google.
          We are only interested in specific queries related to the diseases we are interested in.
          This data collection works in two different modes.
        </p>

        <p>
          <b>Encrypted mode</b>.
          We are currently testing a privacy-preserving tool to collect how many users search for
          certain keywords on Google, but not who. This makes it impossible for us to see individual
          data related to each user: We can only see aggregated data from groups of users.
        </p>

        <p>
          <b>Raw mode</b>.
          PDD also allows to collect raw individual search queries. Seeing raw data will help us
          understand how our system works in practice and improve its operation before it is released
          to the wider public. Even in this mode, we do not record the exact queries or the time at
          which they were made, but only the number of times some specific keywords were searched.
        </p>

        <p>If you need more information, please contact us via e-mail: <a href="mailto:ppdisense@cs.ucl.ac.uk">ppdisense@cs.ucl.ac.uk</a>.</p>

        <p>
          <b>About us</b>.
          We are a group of researchers from the University College London.
          You can find out more about our project on the <a
          target="_blank" href="https://ppd.cs.ucl.ac.uk/">Private Data Donor website</a>.
        </p>
      </div>
    );
  }
}
