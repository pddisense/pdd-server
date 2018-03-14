import React from 'react';

import ClientTableContainer from './ClientTableContainer';

class ClientList extends React.Component {
  render() {
    return (
      <div>
        <h2>Clients</h2>
        <ClientTableContainer/>
      </div>
    );
  }
}
export default ClientList;
