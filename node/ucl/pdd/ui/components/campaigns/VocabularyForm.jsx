import React from 'react';
import PropTypes from 'prop-types';
import { cloneDeep, some, noop } from 'lodash';
import { Toaster, Intent } from '@blueprintjs/core';
import autobind from 'autobind-decorator';

import TextInput from '../form/TextInput';

const toaster = Toaster.create();

function appendVocabulary(obj, newQuery) {
  if (!obj.attrs.vocabulary.queries) {
    obj.attrs.vocabulary.queries = [];
    obj.attrs.activeQueries = [];
  }
  if (newQuery.indexOf(',') > -1) {
    const terms = newQuery.split(',').map(s => s.trim()).filter(s => s.length > 0).sort();
    const previousIdx = obj.attrs.vocabulary.queries.findIndex(q => q.terms && q.terms === terms);
    if (previousIdx === -1) {
      obj.attrs.vocabulary.queries.push({terms});
      obj.attrs.activeQueries.push(true);
      return true;
    } else if (!obj.attrs.activeQueries[previousIdx]) {
      obj.attrs.activeQueries[previousIdx] = true;
      return true;
    }
  } else {
    const exact = newQuery;
    const previousIdx = obj.attrs.vocabulary.queries.findIndex(q => q.exact && q.exact === exact);
    if (previousIdx === -1) {
      obj.attrs.vocabulary.queries.push({ exact });
      obj.attrs.activeQueries.push(true);
      return true;
    } else if (!obj.attrs.activeQueries[previousIdx]) {
      obj.attrs.activeQueries[previousIdx] = true;
      return true;
    }
  }
  return false;
}

class VocabularyForm extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      newQuery: '',
    }
  }

  @autobind
  handleNewQueryChange(e) {
    this.setState({newQuery: e.target.value});
  }

  @autobind
  handleSubmit(e) {
    e.preventDefault();
    if (this.state.newQuery === '') {
      return;
    }
    const obj = cloneDeep(this.props.item);
    if (appendVocabulary(obj, this.state.newQuery)) {
      this.props.onChange(obj);
      this.setState({ newQuery: '' });
    } else {
      toaster.show({
        message: `Query "${this.state.newQuery}" is already tracked`,
        intent: Intent.DANGER,
      });
    }
  }

  render() {
    return (
      <form onSubmit={this.handleSubmit}>
        <div className="pt-control-group">
          <div className="pt-input-group">
            <TextInput
              placeholder="flu,influenza"
              value={this.state.newQuery}
              onChange={this.handleNewQueryChange} />
          </div>
          <button className="pt-button pt-intent-primary">Add query</button>
        </div>
      </form>
    );
  }
}

VocabularyForm.propTypes = {
  onChange: PropTypes.func,
  item: PropTypes.object.isRequired,
};

VocabularyForm.defaultProps = {
  onChange: noop,
};

export default VocabularyForm;
