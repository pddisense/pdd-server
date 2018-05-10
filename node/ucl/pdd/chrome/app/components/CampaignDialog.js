import React from 'react';
import PropTypes from 'prop-types';
import { Button, Dialog, Intent } from '@blueprintjs/core';
import showdown from 'showdown';
import autobind from 'autobind-decorator';
import { identity, noop } from 'lodash';

import JoinDialogContainer from './JoinDialogContainer';
import LeaveDialogContainer from './LeaveDialogContainer';
import { Storage } from '../browser';

const converter = new showdown.Converter();

class CampaignDialog extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      showConfirm: false,
    };
  }

  @autobind
  handleClick(e) {
    e.preventDefault();
    this.setState({ showConfirm: true });
  }

  render() {
    const { campaign } = this.props;
    const tracer = Storage.get(campaign.metadata.name);

    if (this.state.showConfirm) {
      if (tracer) {
        return <LeaveDialogContainer campaign={this.props.campaign}
                                     onConfirm={this.props.onConfirm}
                                     onCancel={this.props.onClose} />;
      } else {
        return <JoinDialogContainer campaign={this.props.campaign}
                                    onConfirm={this.props.onConfirm}
                                    onCancel={this.props.onClose} />;
      }
    }
    const activeQueries = campaign ? campaign.attrs.activeQueries.filter(identity).length : 0;
    const encryption = campaign ? campaign.attrs.rawStrategy.enabled ? 'disabled' : 'enabled' : '-';
    const html = converter.makeHtml(campaign.attrs.description);
    const action = tracer ? 'Leave the campaign' : 'Join the campaign';
    return <Dialog
      iconName="inbox"
      isOpen={true}
      onClose={this.props.onClose}
      title={campaign ? campaign.attrs.displayName : null}>
      <div className="pt-dialog-body">
        {campaign ? <div dangerouslySetInnerHTML={{ __html: html }} /> : null}
        <ul>
          <li><b>Number of tracked queries:</b> {activeQueries}</li>
          <li><b>Encryption:</b> {encryption}</li>
        </ul>
      </div>
      <div className="pt-dialog-footer">
        <div className="pt-dialog-footer-actions">
          <Button
            intent={tracer ? Intent.NONE : Intent.PRIMARY}
            onClick={this.handleClick}
            text={action} />
          <Button text="Close" onClick={this.props.onClose}/>
        </div>
      </div>
    </Dialog>;
  }
}

CampaignDialog.propTypes = {
  campaign: PropTypes.object,
  onConfirm: PropTypes.func,
  onClose: PropTypes.func,
};

CampaignDialog.defaultProps = {
  onConfirm: noop,
  onCancel: noop,
};

export default CampaignDialog;
