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
import { cloneDeep, some } from 'lodash';
import { Intent } from '@blueprintjs/core';

import TextInput from '../form/TextInput';
import toaster from '../toaster';
import { appendToVocabulary } from '../../util/vocabulary';

class VocabularyForm extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      newQuery: '',
    }
  }

  @autobind
  handleChange(e) {
    this.setState({ newQuery: e.target.value });
  }

  @autobind
  handleSubmit(e) {
    e.preventDefault();
    const newQuery = this.state.newQuery.trim();
    if (newQuery.length === 0) {
      return;
    }
    const campaign = cloneDeep(this.props.campaign);
    if (appendToVocabulary(campaign, newQuery)) {
      this.props.onSubmit(campaign);
      this.setState({ newQuery: '' });
    } else {
      toaster.show({
        message: `Query "${this.state.newQuery}" is already part of the vocabulary.`,
        intent: Intent.DANGER,
      });
    }
  }

  render() {
    return (
      <form onSubmit={this.handleSubmit}>
        <div className="pt-form-group">
          <div className="pt-form-content">
            <div className="pt-control-group">
              <div className="pt-input-group">
                <TextInput
                  placeholder="flu,influenza"
                  value={this.state.newQuery}
                  onChange={this.handleChange}/>
              </div>
              <button className="pt-button pt-intent-primary">Add query</button>
            </div>
            <div className="pt-form-helper-text">
              Commas are used to separate multiple terms.
              Spaces at the beginning and the end of the query and each term will be ignored.<br/>
              For example, "flu,influenza" will result in a terms query tracking the keywords "flu" and "influenza", while "flu" will result in an exact query.
            </div>
          </div>
        </div>
      </form>
    );
  }
}

VocabularyForm.propTypes = {
  onSubmit: PropTypes.func.isRequired,
  campaign: PropTypes.object.isRequired,
};

export default VocabularyForm;
