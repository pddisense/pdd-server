import { createStore, applyMiddleware, compose } from 'redux';
import thunk from 'redux-thunk';

import reducer from './reducers';

const enableDevTools = process.env.NODE_ENV === 'devel' && typeof window === 'object' && window.__REDUX_DEVTOOLS_EXTENSION_COMPOSE__;
const composeEnhancers = enableDevTools ? window.__REDUX_DEVTOOLS_EXTENSION_COMPOSE__ : compose;

export default createStore(reducer, composeEnhancers(applyMiddleware(thunk)));
