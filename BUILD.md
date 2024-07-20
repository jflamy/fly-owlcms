The production build and deploy of this application is done using the fly CLI.

- `fly auth login` to select the account to which deployment will take place
- edit `deploy.sh` to confirm the application name
- run `deploy.sh`
  - fly will notice the presence of a Dockerfile 
  - fly will use the Docker file to build the application on a remote docker serve
  - fly will deploy the application under the name indicated in deploy.sh

For development, the application is like all the other owlcms apps. 

- Run the `app.owlcms.fly.Main` class.