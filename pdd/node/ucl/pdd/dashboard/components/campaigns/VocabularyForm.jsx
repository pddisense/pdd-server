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

function appendVocabulary(obj, newQuery) {
  if (!obj.vocabulary.queries) {
    obj.vocabulary.queries = [];
  }
  if (newQuery.indexOf(',') > -1) {
    const terms = newQuery.split(',').map(s => s.trim()).filter(s => s.length > 0).sort();
    const previousIdx = obj.vocabulary.queries.findIndex(q => q.terms && q.terms === terms);
    if (previousIdx === -1) {
      obj.vocabulary.queries.push({terms});
      return true;
    }
  } else {
    const exact = newQuery;
    const previousIdx = obj.vocabulary.queries.findIndex(q => q.exact && q.exact === exact);
    if (previousIdx === -1) {
      obj.vocabulary.queries.push({ exact });
      return true;
    }
  }
  return false;
}

class VocabularyForm extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      newQuery: '',
    }
  }

  @autobind
  handleChange(e) {
    this.setState({newQuery: e.target.value});
  }

  @autobind
  handleSubmit(e) {
    e.preventDefault();
    if (this.state.newQuery === '') {
      return;
    }
    const obj = cloneDeep(this.props.campaign);
    if (appendVocabulary(obj, this.state.newQuery)) {
      this.props.onSubmit(obj);
      this.setState({ newQuery: '' });
    } else {
      toaster.show({
        message: `Query "${this.state.newQuery}" is already tracked`,
        intent: Intent.DANGER,
      });
    }
  }

  render() {
    return (
      <form onSubmit={this.handleSubmit}>
        <div className="pt-control-group">
          <div className="pt-input-group">
            <TextInput
              placeholder="flu,influenza"
              value={this.state.newQuery}
              onChange={this.handleChange} />
          </div>
          <button className="pt-button pt-intent-primary">Add query</button>
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
