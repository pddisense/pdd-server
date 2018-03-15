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
import { noop } from 'lodash';

class TextInput extends React.Component {
  render() {
    return (
      <input
        className="pt-input pt-fill"
        type={this.props.type}
        value={this.props.value}
        placeholder={this.props.placeholder}
        required={this.props.required}
        onChange={this.props.onChange}
        minLength={this.props.minLength}
        maxLength={this.props.maxLength}
        style={{minWidth: '300px'}}
        dir="auto" />
    );
  }
}

TextInput.propTypes = {
  value: PropTypes.string,
  type: PropTypes.string,
  placeholder: PropTypes.string,
  required: PropTypes.bool,
  onChange: PropTypes.func,
  minLength: PropTypes.number,
  maxLength: PropTypes.number,
};

TextInput.defaultProps = {
  value: '',
  type: 'text',
  placeholder: null,
  required: false,
  onChange: noop,
  minLength: 0,
  maxLength: 80,
};

export default TextInput;
