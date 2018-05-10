import Raven from 'raven-js';
import moment from 'moment';
import { HttpClient } from 'colossus-client';

import { Storage } from './browser';
import { SubmitSketchHandler } from './handlers';

// Configure Sentry reporting.
Raven.config(process.env.SENTRY_DSN).install();

// Open the options page just after the extension has been installed.
chrome.runtime.onInstalled.addListener((details) => {
  if (details.reason === chrome.runtime.OnInstalledReason.INSTALL) {
    chrome.runtime.openOptionsPage();
  }
});

// Open the options page when the browser button is clicked.
chrome.browserAction.onClicked.addListener(() => {
  chrome.runtime.openOptionsPage();
});


console.log('Starting PDD');
chrome.alarms.onAlarm.addListener(alarm => {
  if (alarm.name.startsWith('ping:')) {
    ping(alarm.name.substr(5));
  }
});

Storage.reload().then(joined => joined.forEach(item => ping(item.campaignName)));

function ping(campaignName) {
  console.log('Alarm has fired for ' + campaignName);
  Storage
    .get(campaignName)
    .then(item => {
      HttpClient
        .get('clients', item.namespace, item.clientName, 'ping', { vocabularySize: item.vocabulary.length })
        .then(data => {
          console.log(data);
          data.commands.forEach(command => {
            if (command.submitSketch) {
              SubmitSketchHandler({ item, command });
            } else if (command.submitRecovery) {
              console.log('Unsupported `submitRecovery` command')
            }
          });
          if (data.nextPingTime) {
            chrome.alarms.create('ping:' + item.clientName, { when: moment(data.nextPingTime).valueOf() });
          }
        }, data => console.log('Error while pinging', data));
    });
}
