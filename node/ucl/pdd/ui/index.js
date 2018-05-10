import React from 'react';
import ReactDOM from 'react-dom';
import { Provider } from 'react-redux'
import { HashRouter } from 'react-router-dom';
import Raven from 'raven-js';

import App from './components/App';
import RavenBoundary from './components/RavenBoundary';
import store from './store';

// Import stylesheets.
import 'normalize.css/normalize.css';
import '@blueprintjs/core/lib/css/blueprint.css';
import '@blueprintjs/icons/lib/css/blueprint-icons.css';
import '@blueprintjs/datetime/lib/css/blueprint-datetime.css';
import './app.css';

// Install Raven.
Raven.config(process.env.SENTRY_DSN).install();

ReactDOM.render(
  <RavenBoundary>
    <Provider store={store}>
      <HashRouter><App/></HashRouter>
    </Provider>
  </RavenBoundary>,
  document.getElementById('app')
);
