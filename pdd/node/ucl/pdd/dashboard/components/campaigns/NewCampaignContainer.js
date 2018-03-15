/*
 * Copyright 2017-2018 UCL / Vincent Primault <v.primault@ucl.ac.uk>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
