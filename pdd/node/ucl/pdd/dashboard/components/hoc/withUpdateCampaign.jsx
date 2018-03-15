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

import withCampaign from './withCampaign';
import toaster from '../toaster';
import xhr from '../../util/xhr';

export default function withUpdateCampaign(options = {}) {
  options = { toast: true, redirect: true, ... options };

  return function updateCampaignWrapper(WrappedComponent) {
    class UpdateCampaignContainer extends React.Component {
      constructor(props) {
        super(props);
        this.state = {
          isLoading: false,
          errors: [],
          campaign: null,
        };
      }

      @autobind
      onSuccess(resp) {
        if (options.toast) {
          toaster.show({
            message: `Campaign "${resp.displayName} has been updated.`,
            intent: Intent.SUCCESS,
          });
        }
        if (options.redirect) {
          this.props.history.push(`/campaigns/view/${resp.name}`);
        } else {
          this.setState({ campaign: resp });
        }
      }

      @autobind
      onError(resp) {
        toaster.show({ message: 'The campaign could not be updated.', intent: Intent.DANGER });
        this.setState({ errors: resp.errors || [], isLoading: false });
      }

      @autobind
      onSubmit(campaign) {
        this.setState({ isLoading: true });
        return xhr(
          `/api/campaigns/${this.props.campaign.name}`,
          { method: 'PUT', body: JSON.stringify(campaign) }
        ).then(this.onSuccess, this.onError);
      }

      componentWillReceiveProps() {
        this.setState({ campaign: null });
      }

      render() {
        return <WrappedComponent campaign={this.state.campaign || this.props.campaign}
                                 onSubmit={this.onSubmit}
                                 isLoading={this.state.isLoading}
                                 errors={this.state.errors}/>;
      }
    }

    return withCampaign(withRouter(UpdateCampaignContainer));
  };
}
