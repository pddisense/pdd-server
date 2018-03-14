import React from 'react';
import PropTypes from 'prop-types';

import Tabs from './Tabs';

class ViewCampaign extends React.Component {
  render() {
    const { item } = this.props;
    return (
      <div>
        <h2>{item.displayName ? item.displayName : 'Untitled campaign'}</h2>
        <Tabs item={item} />

        <div className="attr-row">
          <div className="attr-name">E-mail addresse(s)</div>
          <div className="attr-value">{item.email.join(', ')}</div>
        </div>

        <div className="attr-row">
          <div className="attr-name">Start date</div>
          <div className="attr-value">{item.startTime ? new Date(item.startTime).toLocaleDateString() : '–'}</div>
        </div>

        <div className="attr-row">
          <div className="attr-name">End date</div>
          <div className="attr-value">{item.endTime ? new Date(item.endTime).toLocaleDateString() : item.startTime ? 'never' : '–'}</div>
        </div>

        <div className="attr-row">
          <div className="attr-name">Delay</div>
          <div className="attr-value">
            {item.delay} day{item.delay > 1 ? 's' : ''} (+{item.graceDelay} day{item.graceDelay > 1 ? 's' : ''})
          </div>
        </div>

        <div className="attr-row">
          <div className="attr-name">Sampling rate</div>
          <div className="attr-value">{item.samplingRate}</div>
        </div>

        {item.collectEncrypted ?
          <div className="attr-row">
            <div className="attr-name">Group size</div>
            <div className="attr-value">{item.groupSize}</div>
          </div>: null}
      </div>
    );
  }
}

ViewCampaign.propTypes = {
  item: PropTypes.object.isRequired,
};

export default ViewCampaign;
