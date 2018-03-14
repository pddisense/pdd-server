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
