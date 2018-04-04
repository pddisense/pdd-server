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

export function appendToVocabulary(campaign, newQuery) {
  if (!campaign.vocabulary.queries) {
    campaign.vocabulary.queries = [];
  }
  if (newQuery.indexOf(',') > -1) {
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
