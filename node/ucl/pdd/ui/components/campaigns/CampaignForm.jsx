import React from 'react';
import PropTypes from 'prop-types';
import autobind from 'autobind-decorator';
import {noop, isEqual} from 'lodash';

import FormGroup from '../form/FormGroup';
import TextInput from '../form/TextInput';
import Textarea from '../form/Textarea';

function attrsToState(attrs) {
  return {
    displayName: attrs.displayName ? attrs.displayName : '',
    description: attrs.description ? attrs.description : '',
    email: attrs.email ? attrs.email.join(' ') : [],
  };
}

function stateToAttrs(state) {
  return {
    displayName: state.displayName,
    description: state.description,
    email: state.email.split(' ').map(s => s.trim()).filter(s => s.length > 0),
  };
}

class CampaignForm extends React.Component {
  constructor(props) {
    super(props);
    this.state = attrsToState(props.item.attrs);
  }

  componentWillReceiveProps(nextProps) {
    this.setState(attrsToState(nextProps.item.attrs));
  }

  @autobind
  handleTextChange(event, field) {
    this.setState({ [field]: event.target.value });
  }

  @autobind
  handleSubmit(e) {
    e.preventDefault();
    const obj = {
      ...this.props.item,
      attrs: { ...this.props.item.attrs, ...stateToAttrs(this.state) },
    };
    this.props.onSubmit(obj);
  }

  render() {
    return (
      <div>
        <form onSubmit={this.handleSubmit}>
          <FormGroup
            title="Display name"
            required={true}
            minLength={1}
            help="The name of the campaign, as displayed to the volunteers.">
            <TextInput
              value={this.state.displayName}
              required={true}
              onChange={e => this.handleTextChange(e, 'displayName')} />
          </FormGroup>

          <FormGroup
            title="E-mail address(es)"
            help="These addresses will be used by PDD to send notifications.
                  They will not be publicly displayed.">
            <TextInput value={this.state.email} onChange={e => this.handleTextChange(e, 'email')}/>
          </FormGroup>

          <FormGroup
            title="Description"
            help="This should present the purpose of the campaign, to let users decide whether
                  they want to join. Markdown is allowed.">
            <Textarea value={this.state.description} onChange={e => this.handleTextChange(e, 'description')} />
          </FormGroup>

          <button type="submit" className="pt-button pt-intent-primary">Submit</button>
        </form>
      </div>
    );
  }
}

CampaignForm.propTypes = {
  item: PropTypes.object.isRequired,
  onSubmit: PropTypes.func,
};

CampaignForm.defaultProps = {
  onSubmit: noop,
};

export default CampaignForm;
