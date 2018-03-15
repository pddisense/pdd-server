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

class FormGroup extends React.Component {
  render() {
    return (
      <div className="pt-form-group">
        <label className="pt-label">
          {this.props.title}
          {this.props.required ? <span className="pt-text-muted">&nbsp;(required)</span> : null}
        </label>

        <div className="pt-form-content">
          {this.props.children}
          {this.props.help !== null ?
            <div className="pt-form-helper-text">{this.props.help}</div> :
            null}
        </div>
      </div>
    );
  }
}

FormGroup.propTypes = {
  title: PropTypes.string.isRequired,
  help: PropTypes.string,
  required: PropTypes.bool,
};

FormGroup.defaultProps = {
  help: null,
  required: false,
};

export default FormGroup;
