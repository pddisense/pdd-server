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
import { Icon } from '@blueprintjs/core';

export default class FaqSection extends React.Component {
  render() {
    return (
      <div>
        <p>
          <Icon icon="help"/> <i>What personal data will be collected by the PDD extension?</i>
        </p>
        <p>
          While the extension is enabled, search queries performed on Google will be monitored by
          the extension and sent periodically (usually once a day) to our servers.
        </p>

        <p>
          <Icon icon="help"/> <i>How will my data be protected by the PDD extension?</i>
        </p>
        <p>
          Our experiments may run in two different modes.
          In the encrypted mode, the collected data is summed across multiple users and encrypted in order to protect the privacy of individuals.
          Even when we decrypt the data we cannot tell which person in the group searched for what query.
          In the raw mode, the collected data is not summed across multiple users.
          Instead, we receive information on the number of times you searched for queries that contained specific keywords, e.g. "cough".
          We do not send the list of the exact queries you performed, but only the number of times some keyword or sentence appears.
          Although we enabled the encrypted mode as much as possible, it may not be the case for all our running experiments.
        </p>

        <p>
          <Icon icon="help"/> <i>Does the PDD extension monitor all my search queries?</i>
        </p>
        <p>
          Only the search queries that contain specific keywords related to infectious diseases
          will specifically be collected. The specific keywords that are monitored by PDD cannot be
          directly visualised, as they can be numerous and ever-changing.
        </p>

        <p>
          <Icon icon="help"/> <i>If I disable the extension, will my queries still be monitored?</i>
        </p>
        <p>
          Once the extension is disabled your search queries will no longer be monitored, and data
          that has not yet been sent will never be sent to our servers.
        </p>

        <p>
          <Icon icon="help"/> <i>How will my data be used?</i>
        </p>
        <p>
          The data you provide to the PDD will be used solely for research purposes.
          We received the approval of the <a href="https://ethics.grad.ucl.ac.uk/">University
          College London Research Ethics Committee</a>.
        </p>
      </div>
    );
  }
}
