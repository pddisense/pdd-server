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
import autobind from 'autobind-decorator';
import { Intent } from '@blueprintjs/core';
import { withRouter } from 'react-router-dom';

import NewCampaign from './NewCampaign';
import toaster from '../toaster';
import xhr from '../../util/xhr';

@withRouter
class NewCampaignContainer extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      isCreating: false,
      errors: [],
    };
  }

  @autobind
  onCreateSuccess(resp) {
    toaster.show({
      message: `Campaign "${resp.displayName} has been created.`,
      intent: Intent.SUCCESS
    });
    this.props.history.push(`/campaigns/view/${resp.name}`);
  }

  @autobind
  onCreateError(resp) {
    toaster.show({ message: 'The campaign could not be created.', intent: Intent.DANGER });
    this.setState({ errors: resp.errors || [], isCreating: false });
  }

  @autobind
  onSubmit(campaign) {
    this.setState({ isCreating: true });
    return xhr(
      '/api/campaigns',
      { method: 'POST', body: JSON.stringify(campaign) }
    ).then(this.onCreateSuccess, this.onCreateError);
  }

  render() {
    return <NewCampaign onSubmit={this.onSubmit}
                        disabled={this.state.isCreating}
                        errors={this.state.errors}/>;
  }
}

export default NewCampaignContainer;
