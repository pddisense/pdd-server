import React from 'react';
import PropTypes from 'prop-types';
import { noop, cloneDeep } from 'lodash';
import autobind from 'autobind-decorator';

import Tabs from './Tabs';
import VocabularyForm from './VocabularyForm';
import VocabularyTable from './VocabularyTable';

class EditVocabulary extends React.Component {
  @autobind
  handleToggle(query) {
    const obj = cloneDeep(this.props.item);
    obj.attrs.activeQueries[query.index] = !obj.attrs.activeQueries[query.index];
    this.props.onChange(obj);
  }

  render() {
    return (
      <div>
        <h2>
          {this.props.item.attrs.displayName}
          <span className="pt-text-muted">{this.props.item.metadata.namespace}</span>
        </h2>
        <Tabs item={this.props.item}/>

        <VocabularyForm item={this.props.item} onChange={this.props.onChange}/>

        <div className="rythmed">
          <VocabularyTable item={this.props.item} onClick={this.handleToggle} />
        </div>
      </div>
    );
  }
}

EditVocabulary.propTypes = {
  onChange: PropTypes.func,
  item: PropTypes.object.isRequired,
};

EditVocabulary.defaultProps = {
  onChange: noop,
};

export default EditVocabulary;
