/*
 * PDD is a platform for privacy-preserving Web searches collection.
 * Copyright (C) 2016-2018 Vincent Primault <v.primault@ucl.ac.uk>
 *
 * PDD is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PDD is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PDD.  If not, see <http://www.gnu.org/licenses/>.
 */

import React from 'react';
import PropTypes from 'prop-types';
import { noop } from 'lodash';

import BlacklistTable from './BlacklistTable';

class BlacklistSection extends React.Component {
  handleClick(idx) {
    const blacklist = { queries: this.props.localData.blacklist.queries.slice() };
    blacklist.queries.splice(idx, 1);
    console.log({ blacklist });
    this.props.onChange({ blacklist });
  }

  render() {
    const blacklist = this.props.localData.blacklist || { queries: [] };
    return (
      <div>
        <p>
          This page shows all keywords that you have previously blacklisted.
          It means that statistics about these keywords are not being monitored anymore by the
          Chrome extension and will never be shared.
          You can choose at any time to revoke a blacklisted keyword, allowing us to monitor again
          its usage.
        </p>
        <BlacklistTable blacklist={blacklist} onClick={idx => this.handleClick(idx)}/>
      </div>
    );
  }
}

BlacklistSection.propTypes = {
  localData: PropTypes.object.isRequired,
  onChange: PropTypes.func,
};

BlacklistSection.defaultProps = {
  onChange: noop,
};

export default BlacklistSection;
