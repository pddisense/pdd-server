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
import PropTypes from 'prop-types';
import autobind from 'autobind-decorator';
import { isEqual } from 'lodash';
import { TextArea } from '@blueprintjs/core';

import FormGroup from '../form/FormGroup';
import TextInput from '../form/TextInput';

function attrsToState(campaign) {
  return {
    displayName: campaign.displayName,
    email: campaign.email || '',
    notes: campaign.notes || '',
  };
}

function stateToAttrs(state) {
  return {
    displayName: state.displayName,
    email: state.email === '' ? null : state.email,
    notes: state.notes === '' ? null : state.notes,
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
            help="The name of the campaign, used to identify it in lists.">
            <TextInput
              value={this.state.displayName}
              required={true}
              onChange={e => this.handleTextChange(e, 'displayName')}/>
          </FormGroup>

          <FormGroup
            title="E-mail address"
            help="This address will be used by PDD to send notifications.">
            <TextInput value={this.state.email} onChange={e => this.handleTextChange(e, 'email')}/>
          </FormGroup>

          <FormGroup
            title="Notes"
            help="This is a notepad that can be used to describe the purpose of this campaign, any interesting outcomes, etc. Markdown is allowed.">
            <TextArea value={this.state.notes}
                      fill={true}
                      style={{height: '150px'}}
                      onChange={e => this.handleTextChange(e, 'notes')}/>
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
