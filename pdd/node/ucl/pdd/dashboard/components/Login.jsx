import React from 'react';

import FormGroup from './form/FormGroup';
import TextInput from './form/TextInput';

class Login extends React.Component {
  render() {
    return (
      <div>
        <form>
          <FormGroup title="Username">
            <TextInput required />
          </FormGroup>
          <FormGroup title="Password">
            <TextInput required type="password" />
          </FormGroup>
        </form>
      </div>
    );
  }
}

export default Login;
