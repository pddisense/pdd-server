import React from 'react';
import PropTypes from 'prop-types';
import { noop } from 'lodash';

import CampaignForm from './CampaignForm';
import Tabs from './Tabs';

class EditCampaign extends React.Component {
  render() {
    const { item } = this.props;
    return (
      <div>
        <h2>{this.props.item.displayName ? this.props.item.displayName : 'Untitled campaign'}</h2>
        <Tabs item={item}/>
        <CampaignForm item={item} onSubmit={this.props.onSubmit} />
      </div>
    );
  }
}

EditCampaign.propTypes = {
  onSubmit: PropTypes.func,
  item: PropTypes.object.isRequired,
};

EditCampaign.defaultProps = {
  onSubmit: noop,
};

export default EditCampaign;
