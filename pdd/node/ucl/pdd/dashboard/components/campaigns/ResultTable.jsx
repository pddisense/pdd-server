/*
 * Copyright 2017-2018 UCL / Vincent Primault <v.primault@ucl.ac.uk>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
      <table className="pt-html-table pt-interactive pt-html-table-striped">
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
