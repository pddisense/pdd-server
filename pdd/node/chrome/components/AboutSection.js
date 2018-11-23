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

export default class AboutSection extends React.Component {
  render() {
    return (
      <div>
        <h1>About Private Data Donor</h1>

        <p>
          Privacy-friendly Data Donor is a tool connecting volunteers with researchers.
          The purpose of this extension is to study the spread of infectious diseases like flu.
          No information you provide will ever be used for commercial purposes or published elsewhere.
          Towards this purpose, we monitor search queries that you make on Google.
          We are only interested in specific queries related to the diseases we are interested in.
        </p>

        <p>
          <b>About us</b>.
          We are a group of researchers from the University College London.
          You can find out more about our project on the <a
          target="_blank" href="https://ppd.cs.ucl.ac.uk/">Private Data Donor website</a>.
        </p>

        <p>If you need more information, please contact us via e-mail: <a href="mailto:ppdisense@cs.ucl.ac.uk">ppdisense@cs.ucl.ac.uk</a>.</p>
      </div>
    );
  }
}
