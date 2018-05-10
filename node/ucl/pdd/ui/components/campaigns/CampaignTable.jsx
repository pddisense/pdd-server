import React from 'react';
import PropTypes from 'prop-types';
import { withRouter } from 'react-router-dom';
import autobind from 'autobind-decorator';
import moment from 'moment';
import {identity} from 'lodash';

@withRouter
class CampaignTable extends React.Component {
  @autobind
  handleClick(item) {
    this.props.history.push(`/campaigns/view/${item.metadata.namespace}/${item.metadata.name}`);
  }

  render() {
    const rows = !this.props.items ? [] : this.props.items.map((item, idx) => {
      const wrap = (el) => item.attrs.startTime ? el : <span className="pt-text-muted">{el}</span>;
      return (
        <tr onClick={() => this.handleClick(item)} key={idx}>
          <td>{wrap(item.attrs.displayName)}</td>
          <td>{wrap(item.attrs.email ? item.attrs.email : '-')}</td>
          <td>{wrap(item.attrs.startTime ? moment(item.attrs.startTime).fromNow() : '–')}</td>
          <td>{wrap(item.attrs.endTime ? moment(item.attrs.endTime).fromNow() : item.attrs.startTime ? 'never' : '-')}</td>
          <td>{wrap(item.attrs.activeQueries.filter(identity).length + ' / ' + item.attrs.activeQueries.length)}</td>
          <td>{wrap(item.attrs.rawStrategy.enabled ? 'disabled' : 'enabled')}</td>
        </tr>
      );
    });
    return (
      <table className="pt-html-table pt-interactive">
        <thead>
        <tr>
          <th>Name</th>
          <th>Owner</th>
          <th>Start time</th>
          <th>End time</th>
          <th>Tracked queries</th>
          <th>Encryption</th>
        </tr>
        </thead>
        <tbody>{rows}</tbody>
      </table>
    );
  }
}

CampaignTable.propTypes = {
  items: PropTypes.array,
};

CampaignTable.defaultProps = {
  items: [],
};

export default CampaignTable;
