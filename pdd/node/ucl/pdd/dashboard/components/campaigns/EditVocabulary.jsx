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
import Title from './Title';
import VocabularyForm from './VocabularyForm';
import VocabularyUpload from './VocabularyUpload';
import VocabularyTable from './VocabularyTable';

class EditVocabulary extends React.Component {
  constructor(props) {
    super(props);
  }

  render() {
    return (
      <div>
        <Title campaign={this.props.campaign}/>

        <Tabs campaign={this.props.campaign}/>

        <p className="pt-ui-text-large" style={{marginBottom: '25px'}}>
          This page allows to configure the search queries that are actively monitored by this campaign.
          Queries can be either <i>exact</i> queries, which means that users have to type the exact specified string to be counted,
          or <i>terms</i> queries, which means that users have to type all of the terms, in any order, to be counted.
          Vocabulary is append-only, which means queries cannot be removed.
        </p>

        <VocabularyForm campaign={this.props.campaign} onSubmit={this.props.onSubmit}/>
        <VocabularyUpload campaign={this.props.campaign} onSubmit={this.props.onSubmit}/>

        <hr style={{marginTop: '10px', marginBottom: '10px'}}/>

        <div className="rythmed">
          <VocabularyTable campaign={this.props.campaign}/>
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
