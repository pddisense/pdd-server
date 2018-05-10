import React from 'react';
import ReactDOM from 'react-dom';
import Raven from 'raven-js';

import OptionsPage from './components/OptionsPage';
import RavenBoundary from './components/RavenBoundary';
import { Storage } from './browser';

// Import stylesheets.
import 'normalize.css/normalize.css';
import '@blueprintjs/core/lib/css/blueprint.css';
import '@blueprintjs/icons/lib/css/blueprint-icons.css';

// Install Raven.
Raven.config(process.env.SENTRY_DSN).install();

Raven.context(() => Storage.reload());

ReactDOM.render(
  <RavenBoundary><OptionsPage/></RavenBoundary>,
  document.getElementById('app')
);
