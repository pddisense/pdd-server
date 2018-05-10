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
