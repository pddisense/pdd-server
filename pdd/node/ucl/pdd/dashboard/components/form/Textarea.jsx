import React from 'react';
import PropTypes from 'prop-types';
import { noop } from 'lodash';

class Textarea extends React.Component {
  render() {
    return (
      <textarea
        className="pt-input pt-fill"
        value={this.props.value}
        onChange={this.props.onChange}
        rows={this.props.rows}
        dir="auto"/>
    );
  }
}

Textarea.propTypes = {
  rows: PropTypes.number,
  value: PropTypes.string,
  onChange: PropTypes.func,
};

Textarea.defaultProps = {
  rows: 8,
  value: '',
  onChange: noop,
};

export default Textarea;
