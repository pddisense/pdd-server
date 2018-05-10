import React from 'react';
import PropTypes from 'prop-types';
import { some, noop } from 'lodash';
import { Switch, Icon } from '@blueprintjs/core';
import autobind from 'autobind-decorator';

import TextInput from '../form/TextInput';

function filterVocabulary(item, state) {
  let queries = item.attrs.vocabulary.queries.map((q, idx) => {
    q.active = item.attrs.activeQueries[idx];
    q.index = idx;
    return q;
  });
  if (!state.showInactive) {
    queries = queries.filter(q => q.active);
  }
  if (state.filterQuery !== '') {
    queries = queries.filter(q => {
      return(q.exact && q.exact.indexOf(state.filterQuery) > -1)
        || (q.terms && some(q.terms, t => t.indexOf(state.filterQuery) > -1));
    });
  }
  return queries;
}

class VocabularyTable extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      showInactive: false,
      filterQuery: '',
    }
  }

  @autobind
  handleFilterQueryChange(e) {
    this.setState({ filterQuery: e.target.value });
  }

  @autobind
  handleSwitchClick() {
    this.setState({ showInactive: !this.state.showInactive });
  }

  render() {
    const rows = filterVocabulary(this.props.item, this.state).map(q => {
      const wrap = (el) => q.active ? el : <span className="pt-text-muted">{el}</span>;
      return (
        <tr key={q.index} onClick={() => this.props.onClick ? this.props.onClick(q) : noop()}>
          <td>{wrap(q.terms && q.terms.length > 0 ? q.terms.join(',') : q.exact)}</td>
          <td>{wrap(q.terms && q.terms.length > 0 ? 'terms' : 'exact')}</td>
        </tr>
      );
    });
    return (
      <div>
        <div style={{float: 'left'}}>
          <div className="pt-control-group">
            <div className="pt-input-group">
              <Icon iconName="search" iconSize="inherit"/>
              <TextInput
                placeholder="Search by query..."
                value={this.state.filterQuery}
                onChange={this.handleFilterQueryChange} />
            </div>
          </div>
        </div>

        <div style={{float: 'left', marginLeft: '15px'}}>
          <Switch
            checked={this.state.showInactive}
            label="Show inactive queries" onChange={this.handleSwitchClick} />
        </div>

        <div style={{clear: 'both', height: 0}}>&nbsp;</div>

        <table className={'pt-html-table pt-small pt-html-table-striped' + (this.props.onClick ? ' pt-interactive' : '')}>
          <thead>
            <tr>
              <th style={{width: '400px'}}>Query</th>
              <th style={{width: '100px'}}>Type</th>
            </tr>
          </thead>
          <tbody>
            {rows}
          </tbody>
        </table>
      </div>
    );
  }
}

VocabularyTable.propTypes = {
  onClick: PropTypes.func,
  item: PropTypes.object.isRequired,
};

export default VocabularyTable;
