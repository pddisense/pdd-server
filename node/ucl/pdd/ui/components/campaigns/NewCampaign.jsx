import React from 'react';
import PropTypes from 'prop-types';
import {noop} from 'lodash';

import CampaignForm from './CampaignForm';

class NewCampaign extends React.Component {
  render() {
    const item = {
      kind: 'Campaign',
      attrs: {
        displayName: 'Untitled campaign',
        privacyStrategy: {
          enabled: true,
          groupSize: 10,
          minGroupSize: 10,
        }
      },
    };
    return (
      <div>
        <h2>
          New campaign
          <span className="pt-text-muted">default</span>
        </h2>
        <CampaignForm item={item} onSubmit={this.props.onSubmit} />
      </div>
    );
  }
}

NewCampaign.propTypes = {
  onSubmit: PropTypes.func,
};

NewCampaign.defaultProps = {
  onSubmit: noop,
};

export default NewCampaign;
