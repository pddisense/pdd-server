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
import { noop } from 'lodash';
import { Icon } from '@blueprintjs/core';

import { formatQuery } from '../protocol/history';

class BlacklistTable extends React.Component {
  handleClick(e, idx) {
    e.preventDefault();
    this.props.onClick(idx);
  }

  render() {
    if (this.props.blacklist.queries.length === 0) {
      return <p style={{textAlign: 'center'}}>No keywords has been blacklisted.</p>;
    }
    const rows = this.props.blacklist.queries.map((item, idx) => {
      return (
        <tr key={idx}>
          <td>{formatQuery(item)}</td>
          <td style={{ textAlign: 'center' }}>
            <a onClick={e => this.handleClick(e, idx)}><Icon icon="confirm"/></a>
          </td>
        </tr>
      );
    });
    return (
      <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
        <table className="pt-html-table">
          <thead>
          <tr>
            <th>Keywords</th>
            <th style={{ textAlign: 'center' }}>Revoke</th>
          </tr>
          </thead>
          <tbody>{rows}</tbody>
        </table>
      </div>
    );
  }
}

BlacklistTable.propTypes = {
  blacklist: PropTypes.object.isRequired,
  onClick: PropTypes.func,
};

BlacklistTable.defaultProps = {
  blacklist: [],
  onClick: noop,
};

export default BlacklistTable;
