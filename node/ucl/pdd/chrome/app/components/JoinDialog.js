import React from 'react';
import PropTypes from 'prop-types';
import autobind from 'autobind-decorator';
import { capitalize, noop } from 'lodash';
import { Button, Dialog, Intent, Collapse, Icon } from '@blueprintjs/core';

class JoinDialog extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      externalName: '',
      showMore: false,
    };
  }

  @autobind
  handleSubmit(e) {
    e.preventDefault();
    this.props.onConfirm({ externalName: this.state.externalName });
  }

  @autobind
  handleToggle(e) {
    e.preventDefault();
    this.setState({ showMore: !this.state.showMore });
  }

  @autobind
  handleExternalNameChange(e) {
    this.setState({ externalName: e.target.value });
  }

  render() {
    return (
      <Dialog
        iconName="inbox"
        isOpen={true}
        onClose={this.props.onCancel}
        title="Join campaign">
        <div className="pt-dialog-body">
          <p>
            Are you sure you want to join the
            campaign{this.props.campaign ? ' "' + this.props.campaign.attrs.displayName + '"' : ''}?
          </p>

          <Collapse isOpen={this.state.showMore}>
            <div className="pt-form-group">
              <label className="pt-label">
                External reference
              </label>
              <div className="pt-form-content">
                <input
                  className="pt-input pt-fill"
                  type="text"
                  value={this.state.externalName}
                  onChange={this.handleExternalNameChange}
                  dir="auto"/>
                <div className="pt-form-helper-text">
                  If you are coming from an external campaign and have been assigned a
                  reference, please indicate it here. If you do not know what it is, you should
                  probably leave this field empty.
                </div>
              </div>
            </div>
          </Collapse>

          <p>
            <a onClick={this.handleToggle}>
              Show {this.state.showMore ? 'less' : 'more'} options
              <Icon iconName={this.state.showMore ? 'chevron-up' : 'chevron-down'}/>
            </a>
          </p>
        </div>

        <div className="pt-dialog-footer">
          <div className="pt-dialog-footer-actions">
            <Button
              intent={Intent.PRIMARY}
              onClick={this.handleSubmit}
              text="Join this campaign" />
            <Button text="Cancel" onClick={this.props.onCancel}/>
          </div>
        </div>
      </Dialog>
    );
  }
}

JoinDialog.propTypes = {
  campaign: PropTypes.object,
  onConfirm: PropTypes.func,
  onCancel: PropTypes.func,
};

JoinDialog.defaultProps = {
  onConfirm: noop,
  onCancel: noop,
};

export default JoinDialog;
