import React from 'react';
import PropTypes from 'prop-types';
import { noop, cloneDeep } from 'lodash';

import Tabs from './Tabs';
import VocabularyForm from './VocabularyForm';
import VocabularyTable from './VocabularyTable';

class EditVocabulary extends React.Component {
  render() {
    return (
      <div>
        <h2>{this.props.item.displayName ? this.props.item.displayName : 'Untitled campaign'}</h2>
        <Tabs item={this.props.item}/>

        <VocabularyForm item={this.props.item} onChange={this.props.onChange}/>

        <div className="rythmed">
          <VocabularyTable item={this.props.item} />
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
