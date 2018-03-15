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
import ReactDOM from 'react-dom';
import { HashRouter } from 'react-router-dom';
import Raven from 'raven-js';

import App from './components/App';
import RavenBoundary from './components/RavenBoundary';

// Import stylesheets.
import 'normalize.css/normalize.css';
import '@blueprintjs/core/lib/css/blueprint.css';
import '@blueprintjs/icons/lib/css/blueprint-icons.css';
import '@blueprintjs/datetime/lib/css/blueprint-datetime.css';
import './app.css';

// Configure Sentry reporting. The environment variables are provided at build time.
Raven.config(process.env.SENTRY_DSN, {environment: process.env.NODE_ENV}).install();

ReactDOM.render(
  <RavenBoundary><HashRouter><App/></HashRouter></RavenBoundary>,
  document.getElementById('app')
);
