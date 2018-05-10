import React from 'react';
import PropTypes from 'prop-types';
import autobind from 'autobind-decorator';
import { noop } from 'lodash';

import LeaveDialog from './LeaveDialog';
import LeaveCampaignHandler from '../handlers/LeaveCampaignHandler';

class LeaveDialogContainer extends React.Component {
  @autobind
  handleConfirm() {
    LeaveCampaignHandler({
      namespace: this.props.campaign.metadata.namespace,
      name: this.props.campaign.metadata.name,
    }).then(() => this.props.onConfirm());
  }

  render() {
    return (
      <LeaveDialog
        onCancel={this.props.onCancel}
        onConfirm={this.handleConfirm}
        campaign={this.props.campaign}/>
    );
  }
}

LeaveDialogContainer.propTypes = {
  campaign: PropTypes.object,
  onConfirm: PropTypes.func,
  onClose: PropTypes.func,
};

LeaveDialogContainer.defaultProps = {
  onConfirm: noop,
  onCancel: noop,
};

export default LeaveDialogContainer;
