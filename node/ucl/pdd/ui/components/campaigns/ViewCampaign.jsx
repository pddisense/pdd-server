import React from 'react';
import PropTypes from 'prop-types';
import showdown from 'showdown';
import { NonIdealState } from '@blueprintjs/core';

import Tabs from './Tabs';

const converter = new showdown.Converter();

class ViewCampaign extends React.Component {
  render() {
    const { item } = this.props;
    let description;
    if (item.attrs.description) {
      const html = converter.makeHtml(item.attrs.description);
      description = <div className="pt-running-text" dangerouslySetInnerHTML={{ __html: html }} />;
    } else {
      description = <NonIdealState
        title="Description is empty."
        visual="document"
        description={<span>You should create a description make the campaign more engaging by going to the &quot;Edit&quot; tab.</span>} />
    }
    return (
      <div>
        <h2>
          {item.attrs.displayName}
          <span className="pt-text-muted">{item.metadata.namespace}</span>
        </h2>
        <Tabs item={item} />
        {description}
      </div>
    );
  }
}

ViewCampaign.propTypes = {
  item: PropTypes.object.isRequired,
};

export default ViewCampaign;
