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
import { Intent, FileInput, Switch } from '@blueprintjs/core';
import { cloneDeep } from 'lodash';
import autobind from 'autobind-decorator';

import toaster from '../toaster';
import { appendToVocabulary } from '../../util/vocabulary';

class VocabularyUpload extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      disabled: false,
      asTerms: false,
    };
  }

  @autobind
  handleSwitchChange() {
    this.setState({ asTerms: !this.state.asTerms });
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
          if (appendToVocabulary(campaign, newQuery, this.state.asTerms)) {
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
          <Switch checked={this.state.asTerms} label="Force all keywords as terms" onChange={this.handleSwitchChange} />
          <div className="pt-form-helper-text">
            The selected file must be a CSV or TXT file containing one query per line.<br/>
            Commas are used to separate multiple terms.
            Spaces at the beginning and the end of the query and each term will be ignored.
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
