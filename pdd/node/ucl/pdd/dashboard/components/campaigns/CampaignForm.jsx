import React from 'react';
import PropTypes from 'prop-types';
import autobind from 'autobind-decorator';
import { isEqual } from 'lodash';

import FormGroup from '../form/FormGroup';
import TextInput from '../form/TextInput';

function attrsToState(campaign) {
  return {
    displayName: campaign.displayName ? campaign.displayName : '',
    email: campaign.email ? campaign.email.join(' ') : '',
  };
}

function stateToAttrs(state) {
  return {
    displayName: state.displayName,
    email: state.email.split(' ').map(s => s.trim()).filter(s => s.length > 0),
  };
}

class CampaignForm extends React.Component {
  constructor(props) {
    super(props);
    this.state = attrsToState(props.campaign);
  }

  componentWillReceiveProps(nextProps) {
    this.setState(attrsToState(nextProps.campaign));
  }

  @autobind
  handleTextChange(event, field) {
    this.setState({ [field]: event.target.value });
  }

  @autobind
  handleSubmit(e) {
    e.preventDefault();
    const obj = { ...this.props.campaign, ...stateToAttrs(this.state) };
    this.props.onSubmit(obj);
  }

  render() {
    return (
      <div>
        <form onSubmit={this.handleSubmit}>
          <FormGroup
            title="Display name"
            required={true}
            minLength={1}
            help="The name of the campaign, as displayed to the volunteers.">
            <TextInput
              value={this.state.displayName}
              required={true}
              onChange={e => this.handleTextChange(e, 'displayName')} />
          </FormGroup>

          <FormGroup
            title="E-mail address(es)"
            help="These addresses will be used by PDD to send notifications.
                  They will not be publicly displayed.">
            <TextInput value={this.state.email} onChange={e => this.handleTextChange(e, 'email')}/>
          </FormGroup>

          <button type="submit" className="pt-button pt-intent-primary">Submit</button>
        </form>
      </div>
    );
  }
}

CampaignForm.propTypes = {
  campaign: PropTypes.object.isRequired,
  onSubmit: PropTypes.func.isRequired,
};

export default CampaignForm;
