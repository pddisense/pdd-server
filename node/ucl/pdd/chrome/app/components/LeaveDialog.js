import React from 'react';
import PropTypes from 'prop-types';
import { capitalize, noop } from 'lodash';
import { Button, Dialog, Intent } from '@blueprintjs/core';

class LeaveDialog extends React.Component {
  render() {
    return (
      <Dialog
        iconName="inbox"
        isOpen={true}
        onClose={this.props.onCancel}
        title="Leave campaign">
        <div className="pt-dialog-body">
          <p>
            Are you sure you want to leave the
            campaign{this.props.campaign ? ' "' + this.props.campaign.attrs.displayName + '"' : ''}?
          </p>
        </div>

        <div className="pt-dialog-footer">
          <div className="pt-dialog-footer-actions">
            <Button
              intent={Intent.PRIMARY}
              onClick={this.props.onConfirm}
              text="Leave this campaign" />
            <Button text="Cancel" onClick={this.props.onCancel} />
          </div>
        </div>
      </Dialog>
    );
  }
}

LeaveDialog.propTypes = {
  campaign: PropTypes.object,
  onConfirm: PropTypes.func,
  onClose: PropTypes.func,
};

LeaveDialog.defaultProps = {
  onConfirm: noop,
  onCancel: noop,
};

export default LeaveDialog;
