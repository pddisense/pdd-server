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

export function appendToVocabulary(campaign, newQuery, asTerm = false) {
  if (!campaign.vocabulary.queries) {
    campaign.vocabulary.queries = [];
  }
  if (asTerm || newQuery.indexOf(',') > -1) {
    const terms = newQuery.split(',').map(s => s.trim()).filter(s => s.length > 0).sort();
    const previousIdx = campaign.vocabulary.queries.findIndex(q => q.terms && q.terms === terms);
    if (previousIdx === -1) {
      campaign.vocabulary.queries.push({ terms });
      return true;
    }
  } else {
    const exact = newQuery;
    const previousIdx = campaign.vocabulary.queries.findIndex(q => q.exact && q.exact === exact);
    if (previousIdx === -1) {
      campaign.vocabulary.queries.push({ exact });
      return true;
    }
  }
  return false;
}
