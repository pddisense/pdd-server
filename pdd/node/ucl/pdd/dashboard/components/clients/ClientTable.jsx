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
import moment from 'moment';
import { withRouter } from 'react-router-dom';

@withRouter
class ClientTable extends React.Component {
  handleClick(client) {
    this.props.history.push(`/clients/view/${client.name}`);
  }

  render() {
    const rows = this.props.clients.map((client, idx) => {
      return (
        <tr onClick={() => this.handleClick(client)} key={idx}>
          <td>{client.name}</td>
          <td>{client.browser}</td>
          <td>{client.externalName ? client.externalName : '–'}</td>
          <td>{moment(client.createTime).fromNow()}</td>
        </tr>
      );
    });
    return (
      <table className="pt-html-table pt-interactive pt-small pt-html-table-striped"
             style={{ width: '100%' }}>
        <thead>
        <tr>
          <th>Identifier</th>
          <th>Browser</th>
          <th>External name</th>
          <th>Join time</th>
        </tr>
        </thead>
        <tbody>{rows}</tbody>
      </table>
    );
  }
}

ClientTable.propTypes = {
  clients: PropTypes.array.isRequired,
};

export default ClientTable;
