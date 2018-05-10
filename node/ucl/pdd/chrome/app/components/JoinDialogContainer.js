import React from 'react';
import PropTypes from 'prop-types';
import autobind from 'autobind-decorator';
import { noop } from 'lodash';

import JoinDialog from './JoinDialog';
import JoinCampaignHandler from '../handlers/JoinCampaignHandler';

class JoinDialogContainer extends React.Component {
  @autobind
  handleConfirm(attrs) {
    JoinCampaignHandler({
      namespace: this.props.campaign.metadata.namespace,
      name: this.props.campaign.metadata.name,
      ...attrs,
    }).then(() => this.props.onConfirm());
  }

  render() {
    return (
      <JoinDialog
        onCancel={this.props.onCancel}
        onConfirm={this.handleConfirm}
        campaign={this.props.campaign}/>
    );
  }
}

JoinDialogContainer.propTypes = {
  campaign: PropTypes.object,
  onConfirm: PropTypes.func,
  onCancel: PropTypes.func,
};

JoinDialogContainer.defaultProps = {
  onConfirm: noop,
  onCancel: noop,
};

export default JoinDialogContainer;
