import React from 'react';
import PropTypes from 'prop-types';
import { noop } from 'lodash';

import Tabs from './Tabs';
import StrategyForm from './StrategyForm';

class EditStrategy extends React.Component {
  render() {
    const { item } = this.props;
    return (
      <div>
        <h2>
          {item.attrs.displayName}
          <span className="pt-text-muted">{item.metadata.namespace}</span>
        </h2>
        <Tabs item={item}/>
        <StrategyForm {...this.props} />
      </div>
    );
  }
}

EditStrategy.propTypes = {
  onSubmit: PropTypes.func,
  item: PropTypes.object.isRequired,
};

EditStrategy.defaultProps = {
  onSubmit: noop,
};

export default EditStrategy;
