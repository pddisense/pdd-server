import React from 'react';
import PropTypes from 'prop-types';
import { noop } from 'lodash';

import Tabs from './Tabs';
import StrategyForm from './StrategyForm';

class EditStrategy extends React.Component {
  render() {
    return (
      <div>
        <h2>{this.props.campaign.displayName}</h2>

        <Tabs campaign={this.props.campaign}/>

        <StrategyForm {...this.props} />
      </div>
    );
  }
}

EditStrategy.propTypes = {
  onSubmit: PropTypes.func.isRequired,
  isLoading: PropTypes.bool.isRequired,
  errors: PropTypes.array.isRequired,
  campaign: PropTypes.object.isRequired,
};

export default EditStrategy;
