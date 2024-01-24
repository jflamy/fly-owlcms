# owlcms - fly.io cloud application management

[fly.io](https://fly.io) is a cloud deployment platform that is a very good match for [owlcms](https://github.com/jflamy/owlcms4) .
Running on fly.io is free because the monthly fees are well below the minimal charging fees of 5 US$ and no bill is emitted.

This application automates the owlcms deployment process so that it is easy to

- create an owlcms application and update it to the latest version when desired
- create a public results application and update it to the latest version when desired
- connect the two via a shared secret
- use the shared secret to connect an on-site (laptop) owlcms to the cloud publicresults

The application is deployed in the cloud at https://owlcms-cloud.fly.dev 
