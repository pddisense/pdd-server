import React from 'react';
import fetchCollection from '../fetchCollection';
import ClientTable from './ClientTable';

export default fetchCollection('clients')(ClientTable);
