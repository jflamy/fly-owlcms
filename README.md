# owlcms - fly.io application management

[fly.io](https://fly.io) is an affordable cloud deployment platform for [owlcms](https://github.com/jflamy/owlcms4) . 

- owlcms can be run in the Hobby plan which provides 3 small applications for free. 
- Even though owlcms requires a little bit more memory than allocated for free, the billed amount is smaller than the minimum amount for which a bill is emitted, so the usage remains free.

Unfortunately, fly.io is normally configured using a command-line interface, which makes deployment and management more difficult.  The command-line process is documented [here](https://owlcms.github.io/owlcms4/#/Fly)

This web application runs the command on behalf of users, so there is no need to install the command-line interface.  The only requirement for the user is to create an account on the fly.io site and get an access token.

It is then possible to

- create an owlcms application and update it to the latest version when desired
- create a public results application and update it to the latest version when desired
- connect the two via a shared secret
- use the shared secret to connect an on-site (laptop) owlcms to the cloud publicresults

