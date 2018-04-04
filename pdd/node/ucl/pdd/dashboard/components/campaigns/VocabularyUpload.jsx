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
import { Intent, FileInput } from '@blueprintjs/core';
import { cloneDeep } from 'lodash';
import autobind from 'autobind-decorator';

import toaster from '../toaster';
import { appendToVocabulary } from '../../util/vocabulary';

class VocabularyUpload extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      disabled: false,
    };
  }

  @autobind
  handleUpload(e) {
    this.setState({ disabled: true });

    const reader = new FileReader();
    reader.onload = () => {
      const campaign = cloneDeep(this.props.campaign);
      const queries = reader.result.split('\n');
      let valid = 0;
      let invalid = 0;
      queries
        .map(s => s.trim())
        .filter(s => s.length > 0)
        .forEach(newQuery => {
          if (appendToVocabulary(campaign, newQuery)) {
            valid++;
          } else {
            invalid++;
          }
        });
      if (valid > 0) {
        this.props.onSubmit(campaign);
      } else {
        toaster.show({
          message: `All ${invalid} queries were already part of the vocabulary.`,
          intent: Intent.DANGER,
        });
      }
      this.setState({ disabled: false });
    };
    reader.onabort = () => {
      this.setState({ disabled: false });
      toaster.show({ message: 'File reading was aborted.', intent: Intent.DANGER, });
    };
    reader.onerror = () => {
      this.setState({ disabled: false });
      toaster.show({ message: 'File upload has failed.', intent: Intent.DANGER, });
    };
    reader.readAsText(e.target.files[0]);
  }

  render() {
    return (
      <div className="pt-form-group">
        <div className="pt-form-content">
          <FileInput inputProps={{ accept: '.csv,.txt' }} onInputChange={this.handleUpload}/>
          <div className="pt-form-helper-text">
            The selected file must be a CSV or TXT file containing one query per line.
            Commas are used to separate keywords in a multi-terms query.
            Spaces at the beginning and the end of each keyword will be ignored.
          </div>
        </div>
      </div>
    );
  }
}

VocabularyUpload.propTypes = {
  onSubmit: PropTypes.func.isRequired,
  campaign: PropTypes.object.isRequired,
};

export default VocabularyUpload;
