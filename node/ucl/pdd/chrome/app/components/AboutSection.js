/*
 * Private Data Donor is a platform to collect search logs via crowd-sourcing.
 * Copyright (C) 2017-2018 Vincent Primault <v.primault@ucl.ac.uk>
 *
 * Private Data Donor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Private Data Donor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Private Data Donor.  If not, see <http://www.gnu.org/licenses/>.
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
