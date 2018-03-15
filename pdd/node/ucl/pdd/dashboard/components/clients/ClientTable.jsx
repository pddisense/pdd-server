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
import moment from 'moment';

class ClientTable extends React.Component {
  render() {
    const rows = this.props.clients.map((client, idx) => {
      const wrap = (el) => client.leaveTime ? <span className="pt-text-muted">{el}</span> : el;
      return (
        <tr key={idx}>
          <td>{wrap(client.name)}</td>
          <td>{wrap(client.browser)}</td>
          <td>{wrap(client.externalName ? client.externalName : '–')}</td>
          <td>{wrap(moment(client.createTime).fromNow())}</td>
        </tr>
      );
    });
    return (
        <table className="pt-html-table pt-html-table-striped">
          <thead>
          <tr>
            <th>Identifier</th>
            <th>Browser</th>
            <th>External name</th>
            <th>Created</th>
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
