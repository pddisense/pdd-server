import React from 'react';
import { Icon } from '@blueprintjs/core';

export default class FaqSection extends React.Component {
  render() {
    return (
      <div>
        <p>
          <Icon iconName="help"/>&nbsp;
          <i>What personal data will be collected by the PDD extension?</i>
        </p>
        <p>
          While the extension is enabled, search queries performed on Google will be monitored by
          the extension and sent periodically (usually once a day) to our servers.
        </p>

        <p>
          <Icon iconName="help"/>&nbsp;
          <i>How will my data be protected by the PDD extension?</i>
        </p>
        <p>
          In the <i>encrypted mode</i>, the collected data is aggregated across multiple volunteers
          in order to protect the privacy of individual clients, thanks to an encryption protocol.
          Please note that this privacy-preserving mode is not enabled for all campaigns, please
          check before joining a campaign.<br />
          In the <i>raw mode</i>, the collected data is only aggregated locally inside the browser
          of each volunteer. The search queries are grouped by keyword and only the number of times
          that each keyword was searched is sent to our servers.
        </p>

        <p>
          <Icon iconName="help"/>&nbsp;
          <i>Does the PDD extension track all my search queries?</i>
        </p>
        <p>
          Only the search queries that contain specific keywords related to infectious diseases
          will specifically be collected. The specific keywords that are monitored by PDD cannot be
          directly visualized, as they can be numerous and ever-changing. However, it is possible
          to see the search queries that are about to be sent to our server in the "Search history"
          tab of the extension.
        </p>

        <p>
          <Icon iconName="help"/>&nbsp;
          <i>If I disable the extension, will my queries still be monitored?</i>
        </p>
        <p>
          Once the extension is disabled your search queries will no longer be monitored, and data
          that has not yet be sent will never be sent to our servers.
        </p>

        <p>
          <Icon iconName="help"/>&nbsp;
          <i>How will my data be used?</i>
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
