# owlcms - fly.io cloud application management

[fly.io](https://fly.io) is a cloud deployment platform that is a very good match for [owlcms](https://github.com/jflamy/owlcms4) .  
Running on fly.io is free because the monthly fees are well below the minimal charging fees of 5 US$ and no bill is emitted.

This application automates the owlcms deployment process so that it is easy to

- create an owlcms application and update it to the latest version when desired
- create a public results application and update it to the latest version when desired
- connect the two via a shared secret
- use the shared secret to connect an on-site (laptop) owlcms to the cloud publicresults



#### Release Candidate Version 1.0.0

This version is an early release. The application is deployed in the cloud at https://owlcms-cloud.fly.dev  Standard disclaimers apply: there is no warranty, expressed or implied, as to the fitness of the application for any purpose.  Do your own tests, and use the program if satisfied.

Fixes/changes

- cloud owlcms given the location of cloud publicresults; only necessary to configure if on-site.

Known issues:

- none.
