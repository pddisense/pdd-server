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
    this.props.history.push(`/campaigns/view/${item.name}`);
  }

  render() {
    const rows = !this.props.items ? [] : this.props.items.map((item, idx) => {
      const wrap = (el) => item.startTime ? el : <span className="pt-text-muted">{el}</span>;
      return (
        <tr onClick={() => this.handleClick(item)} key={idx}>
          <td>{wrap(item.displayName ? item.displayName : 'Untitled campaign')}</td>
          <td>{wrap(item.email ? item.email : '-')}</td>
          <td>{wrap(item.startTime ? moment(item.startTime).fromNow() : '–')}</td>
          <td>{wrap(item.endTime ? moment(item.endTime).fromNow() : item.startTime ? 'never' : '-')}</td>
          <td>{wrap(item.collectEncrypted ? 'disabled' : 'enabled')}</td>
        </tr>
      );
    });
    return (
      <table className="pt-html-table pt-interactive pt-html-table-striped">
        <thead>
        <tr>
          <th>Name</th>
          <th>Owner</th>
          <th>Start time</th>
          <th>End time</th>
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
