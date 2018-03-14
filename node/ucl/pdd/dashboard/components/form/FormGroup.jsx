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
