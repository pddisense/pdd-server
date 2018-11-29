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
