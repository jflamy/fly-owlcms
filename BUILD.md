The production build and deploy of this application is done using the fly CLI.

- `fly auth login` to select the account to which deployment will take place
- edit `deploy.sh` to confirm the application name
  - if `--local-only` is present, start local Docker
  - for a remote build, remove `--local-only`

- run `deploy.sh`
  - fly will notice the presence of a Dockerfile 
  - fly will use the Docker file to build the application on a remote docker serve
  - fly will deploy the application under the name indicated in deploy.sh

For development, the application is like all the other owlcms apps, but must run on Linux

- If running on Windows, open a WSL window and go to the git repository
  - run `code .` to start VSCode (at the bottom left you should see it is running on WSL)
  - Run the `app.owlcms.fly.Main` class.