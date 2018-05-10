import React from 'react';
import PropTypes from 'prop-types';
import moment from 'moment';

import Tabs from './Tabs';

class ViewClients extends React.Component {
  render() {
    const { item, items } = this.props;
    const rows = items.map((client, idx) => {
      const wrap = (el) => client.status.leaveTime ? <span className="pt-text-muted">{el}</span> : el;
      return (
        <tr key={idx}>
          <td>{wrap(client.metadata.name)}</td>
          <td>{wrap(client.attrs.externalName)}</td>
          <td>{wrap(moment(client.metadata.createTime).fromNow())}</td>
          <td>
            {wrap(client.status.leaveTime
              ? 'left ' + moment(client.status.leaveTime).fromNow()
              : 'active ' + moment(client.status.activeTime).fromNow())}
          </td>
        </tr>
      );
    });
    return (
      <div>
        <h2>
          {item.attrs.displayName}
          <span className="pt-text-muted">{item.metadata.namespace}</span>
        </h2>
        <Tabs item={item}/>

        <table className="pt-html-table pt-small pt-html-table-striped">
          <thead>
          <tr>
            <th>Identifier</th>
            <th>External name</th>
            <th>Created</th>
            <th>Last seen</th>
          </tr>
          </thead>
          <tbody>{rows}</tbody>
        </table>
      </div>
    );
  }
}

ViewClients.propTypes = {
  item: PropTypes.object.isRequired,
  items: PropTypes.array.isRequired,
};

export default ViewClients;
