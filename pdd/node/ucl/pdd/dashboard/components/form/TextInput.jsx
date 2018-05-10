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
        autoFocus={this.props.autoFocus}
        dir="auto" />
    );
  }
}

TextInput.propTypes = {
  value: PropTypes.string.isRequired,
  type: PropTypes.string.isRequired,
  placeholder: PropTypes.string,
  required: PropTypes.bool.isRequired,
  onChange: PropTypes.func,
  minLength: PropTypes.number.isRequired,
  maxLength: PropTypes.number.isRequired,
  autoFocus: PropTypes.bool.isRequired,
};

TextInput.defaultProps = {
  value: '',
  type: 'text',
  placeholder: null,
  required: false,
  onChange: null,
  minLength: 0,
  maxLength: 80,
  autoFocus: false,
};

export default TextInput;
