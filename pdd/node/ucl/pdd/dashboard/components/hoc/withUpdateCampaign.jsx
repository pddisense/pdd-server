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

import toaster from '../toaster';
import xhr from '../../util/xhr';

export default function withUpdateCampaign(options = {}) {
  options = { toast: true, redirect: true, ...options };

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
      onSubmit(campaign) {
        this.setState({ isLoading: true });
        return xhr(
          `/api/campaigns/${this.props.campaign.name}`,
          { method: 'PUT', body: JSON.stringify(campaign) }
        ).then(
          resp => {
            if (options.toast) {
              toaster.show({
                message: `Campaign "${resp.displayName}" has been updated.`,
                intent: Intent.SUCCESS,
              });
            }
            if (options.redirect) {
              this.props.history.push(`/campaigns/view/${resp.name}`);
            } else {
              this.setState({ campaign: resp, isLoading: false, errors: [] });
            }
          },
          resp => {
            toaster.show({ message: 'The campaign could not be updated.', intent: Intent.DANGER });
            this.setState({ errors: resp.errors || [], isLoading: false });
          });
      }

      @autobind
      onDelete() {
        this.setState({ isLoading: true });
        xhr(`/api/campaigns/${this.props.campaign.name}`, { method: 'DELETE' })
          .then(
            resp => {
              if (options.toast) {
                toaster.show({
                  message: `Campaign "${this.props.campaign.displayName}" has been deleted.`,
                  intent: Intent.SUCCESS,
                });
              }
              // Even if `options.redirect` is false, we have to redirect as the campaign doesn't
              // exist anymore.
              this.props.history.push('/campaigns');
            },
            resp => {
              toaster.show({
                message: 'The campaign could not be deleted.',
                intent: Intent.DANGER
              });
              this.setState({ errors: resp.errors || [], isLoading: false });
            });
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

    return withRouter(UpdateCampaignContainer);
  };
}
