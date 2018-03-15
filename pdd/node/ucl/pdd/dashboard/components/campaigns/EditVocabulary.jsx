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
import { cloneDeep } from 'lodash';

import Tabs from './Tabs';
import VocabularyForm from './VocabularyForm';
import VocabularyTable from './VocabularyTable';

class EditVocabulary extends React.Component {
  render() {
    return (
      <div>
        <h2>{this.props.campaign.displayName}</h2>

        <Tabs campaign={this.props.campaign}/>

        <VocabularyForm campaign={this.props.campaign} onChange={this.props.onChange}/>

        <div className="rythmed">
          <VocabularyTable campaign={this.props.campaign} />
        </div>
      </div>
    );
  }
}

EditVocabulary.propTypes = {
  onSubmit: PropTypes.func.isRequired,
  isLoading: PropTypes.bool.isRequired,
  errors: PropTypes.array.isRequired,
  campaign: PropTypes.object.isRequired,
};

export default EditVocabulary;
