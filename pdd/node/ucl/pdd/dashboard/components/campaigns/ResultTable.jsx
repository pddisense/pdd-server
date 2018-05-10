/*
 * PDD is a platform for privacy-preserving Web searches collection.
 * Copyright (C) 2016-2018 Vincent Primault <v.primault@ucl.ac.uk>
 *
 * PDD is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PDD is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PDD.  If not, see <http://www.gnu.org/licenses/>.
 */

import React from 'react';
import PropTypes from 'prop-types';
import { withRouter } from 'react-router-dom';
import autobind from 'autobind-decorator';
import moment from 'moment';
import { identity } from 'lodash';

class ResultTable extends React.Component {
  handleClick(item) {

  }

  render() {
    const rows = this.props.results.map((item, idx) => {
      //const wrap = (el) => item.startTime ? el : <span className="pt-text-muted">{el}</span>;
      const wrap = (el) => el;
      return (
        <tr onClick={() => this.handleClick(item)} key={idx}>
          <td>{wrap(item.day)}</td>
          <td>{wrap(item.stats.activeCount)}</td>
          <td>{wrap(item.stats.submittedCount)}</td>
          <td>{wrap(item.stats.decryptedCount)}</td>
        </tr>
      );
    });
    return (
      <table className="pt-html-table pt-small pt-interactive pt-html-table-striped">
        <thead>
        <tr>
          <th>Day</th>
          <th>Active users</th>
          <th>Submitted sketches</th>
          <th>Decrypted sketches</th>
        </tr>
        </thead>
        <tbody>{rows}</tbody>
      </table>
    );
  }
}

ResultTable.propTypes = {
  results: PropTypes.array,
};

ResultTable.defaultProps = {
  results: [],
};

export default ResultTable;
