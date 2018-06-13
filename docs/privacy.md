---
layout: page
title: Privacy policy
---

This privacy policy formally clarifies what kind of information is collected by the Private Data Donor and how it is processed.

* TOC
{:toc}

## What data is collected by this website?

No personal data is collected by this website (i.e., there are no trackers whatsoever).
We make use of Google Fonts, whose [own privacy policy](https://developers.google.com/fonts/faq#what_does_using_the_google_fonts_api_mean_for_the_privacy_of_my_users) 
states that only minimal information is collected.

## What data is collected by the Chrome extension?

The goal of the extension is to monitor the Web searches performed by volunteers.
We followed a privacy-by-design approach to minimise the amount of information that is collected. 
Even if the extension is installed through the Chrome Web Store, we have no way to link the data we received to a specific Google account.
Each extension installation is only represented internally as a randomly generated identifier.

Our researchers maintain a list of keywords that they want to monitor, e.g., "winter flu", "throat hurts" or "influenza symptom". 
Every day, the extension will automatically retrieve the last list of monitored keywords, and for each of them count how many times it was looked for on Google.
The extension also computes the total number of searches that were performed on Google, regardless of the keywords.
This list of counts is then sent to our server.
It means that we do *not* collect information about individual searches (such as the precise content of the search or the time at which it was performed),
but only the number of times some keywords where looked for. 

## Who has access to the collected Web searches?

The collected data is used by a small number of researchers from [University College London](https://www.ucl.ac.uk/).
One of the main purpose is to use this data to train machine learning models, whose goal is to predict the spread if infectious diseases such as flu.
We also use this data to understanding and improve the effectiveness of the measures we took to protect the privacy of our volunteers.
The collected data is never shared as-is outside of this pool of researchers;
artifacts obtained from this data (e.g., machine learning models, statistical distributions) may be disseminated to the larger public (e.g., outreach, scientific papers). 

## How securely is the data stored?

The collected data is stored on servers hosted inside University College London's Computer Science department.
The data transits from the browser of volunteers to our servers through an [HTTPS connection](https://en.wikipedia.org/wiki/HTTPS), 
essentially meaning that it is encrypted and can only be decrypted by our servers, even if sent through an unsecured network such as a public Wi-Fi.

## How long is the data stored?

The collected Web searches are stored for a period up to 5 years. 

## How to modify or suppress my data?

Uninstalling the Chrome extension instantaneously stops the data collection.
Data that has already been sent will not be removed from our servers, but no more data will be sent in the future.

If you have questions about this policy, or whish to exercise your right of modification or removal of your data, you may contact us: [pddisense@ucl.ac.uk](mailto:pddisense@ucl.ac.uk).
