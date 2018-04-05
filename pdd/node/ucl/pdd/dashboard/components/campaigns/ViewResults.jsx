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
import PropTypes from 'prop-types';
import { AnchorButton, Intent, Callout } from '@blueprintjs/core';

import Tabs from './Tabs';
import Title from './Title';
import ResultTableContainer from './ResultTableContainer';

class ViewResults extends React.Component {
  render() {
    return (
      <div>
        <Title campaign={this.props.campaign}/>

        <Tabs campaign={this.props.campaign}/>

        {this.props.campaign.started ?
          <div>
            <div style={{ marginBottom: '15px' }}>
              <a role="button"
                 className="pt-button pt-intent-primary"
                 href={`/api/campaigns/${this.props.campaign.name}/results?download=1`}>
                Download as CSV
              </a>
              <a role="button"
                 className="pt-button"
                 style={{ marginLeft: '10px' }}
                 href={`/api/campaigns/${this.props.campaign.name}/results`}>
                Download as JSON
              </a>
            </div>

            <ResultTableContainer campaign={this.props.campaign}/>
          </div> :
          <Callout intent={Intent.DANGER}>
            This campaign has not yet started. Results will be available once the campaign will be
            running.
          </Callout>}
      </div>
    );
  }
}

ViewResults.propTypes = {
  campaign: PropTypes.object.isRequired,
};

export default ViewResults;
