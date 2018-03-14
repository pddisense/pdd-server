import React from 'react';
import PropTypes from 'prop-types';
import {noop} from 'lodash';

import CampaignForm from './CampaignForm';

class NewCampaign extends React.Component {
  render() {
    const item = {
      displayName: 'Untitled campaign',
    };
    return (
      <div>
        <h2>New campaign</h2>
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
