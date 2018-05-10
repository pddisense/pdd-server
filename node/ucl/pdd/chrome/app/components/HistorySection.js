import React from 'react';
import PropTypes from 'prop-types';
import moment from 'moment';
import { flatMap } from 'lodash';

class HistorySection extends React.Component {
  handleHistoryClick(e) {
    e.preventDefault();
    chrome.tabs.create({ 'url': 'chrome://history', 'active': true });
  }

  render() {
    const yesterday = moment().hour() < 1;
    const rows = this.props.searches.map((item, idx) => {
      return (
        <tr key={idx}>
          <td>{item.query}</td>
          <td>{moment(item.lastTime).fromNow()}</td>
          <td>{item.count}</td>
        </tr>
      );
    });
    return (
      <div>
        <h1>History</h1>
        <p>
          Here are the search queries that Private Data Donor is monitoring that you made
          {yesterday ? 'yesterday' : 'today'}. They will be automatically
          sent {yesterday ? '' : 'tomorrow'} at 1am. If you which to delete some of them, please
          remove the corresponding activity from <a onClick={this.handleHistoryClick}>your browsing
          history</a>.
        </p>
        <p>
          <b>{this.props.searches.length} quer{this.props.searches.length === 1 ? 'y' : 'ies'}</b> have
          will be collected so far.
        </p>
        <table className="pt-html-table">
          <thead>
          <tr>
            <th>Query</th>
            <th>Last time</th>
            <th>Occurrences</th>
          </tr>
          </thead>
          <tbody>{rows}</tbody>
        </table>
      </div>
    );
  }
}

HistorySection.propTypes = {
  searches: PropTypes.array.isRequired,
};

export default HistorySection;
