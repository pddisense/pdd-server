import React from 'react';
import PropTypes from 'prop-types';
import moment from 'moment';

class ClientTable extends React.Component {
  render() {
    const { items } = this.props;
    const rows = items.map((client, idx) => {
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
  items: PropTypes.array.isRequired,
};

export default ClientTable;
