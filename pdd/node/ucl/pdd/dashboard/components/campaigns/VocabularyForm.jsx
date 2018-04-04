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
              Commas are used to separate keywords in a multi-terms query.
              Spaces at the beginning and the end of each keyword will be ignored.
              For example, "flu,influenza" will result in a multi-terms query tracking the keywords "flu" and "influenza", while "flu" will result in an exact query.
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
