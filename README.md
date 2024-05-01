# EvalS
An electronically-driven performance evaluation system designed
to assist supervisors and employees to complete their annual
goal-setting and appraisal tasks.

[Wiki](https://wiki.library.oregonstate.edu/confluence/display/EVALS/EvalS+Home)

### Setup a local test environment
---
1. Using a docker container is recommended and can be found [here](https://github.sig.oregonstate.edu/ecs-data/ansible-private-roles/tree/master/roles/evals_liferay_server_setup). Otherwise instructions are as follows.

2. Install [blade CLI](https://learn.liferay.com/dxp/7.x/en/developing-applications/tooling/blade-cli/installing-and-updating-blade-cli.html) from Liferay

3. Create a new directory and use blade to initialize a workspace `blade init -v portal-7.3-ga6`

4. Initialize and run liferay server `blade server init` `blade server run`

5. (Optional) Setup test database or use existing oracle development server
    - I used [Mamp](https://www.mamp.info/en/) for a quick and dirty mysql server
    - Run evals/docroot/WEB-INF/src/edu/osu/cws/evals/tests/test-db.sql
    - Run evals/docroot/WEB-INF/src/edu/osu/cws/evals/tests/local-dev-env-data.sql

6. Test Liferay apache server
    - Open a browser to http://localhost:8080
    - Depending on the version of liferay you may not be asked to setup an admin account. In that case below is the default login information.
    - Default login information:
      - username: test@liferay.com
      - password: test

7. Clone and Configure EvalS
    - The evals repo should be cloned to $WORKSPACE/modules/evals
    - Navigate to `evals/src/main/java`
      - Copy `hibernate-sample.cfg.xml` to `hibernate.cfg.xml` and modify contents
      - Copy `evals.sample.properties` to `evals.properties` and modify contents

8. Compile EvalS
    - `blade deploy`
    - This will place a compiled evals.war in $WORKSPACE/bundles/deploy
    - If the server is running it will automatically be deployed and you can watch this happen by reading the logs

9. Running EvalS
    - EvalS is a portlet and won't run on its own
    - Open a browser to http://localhost:8080 and login with the user you setup earlier or the default user
    - The UI can differ depending on the liferay version you are using but the steps should remain the same. These steps are for liferay 7.3.5
    - Click the button next to "Home" to slide-out the side menu
    - Under site builder -> pages click the "+" button to add a new public page
    - Use the "Blank" template and name the page evals
    - You should now be editing the evals page you just created. On the right hand side click the "Fragments and Widgets" button and find evals on that list and drag it to the page.
    - Once evals shows up correctly on the page you can publish and begin testing evals
    - Since these settings are stored in the database you only need to do this when setting up a new liferay server
    - If EvalS is not on the list or it shows an error message, check the logs in `bundles/tomcat-${version}/logs/catalina.out`
    - If using a docker container use `docker logs CONTAINER_NAME` to get logs

### Production & Remote Server Deployment
  1. Remote server must be prepared before evals can be deployed, see wiki for more information
  2. Copy `deploy-example.sh` and replace export values to match the server you are deploying to
  3. run the copied sh script with 1 of 3 parameters
      - all: Deploys both evals and theme
      - evals: deploys just evals
      - theme: deploys just the theme

### Tests
---

1. In Intellij select File > New > Project from Existing Sources...

2. Make sure working directory is set correctly in Intellij
    - Should be the root directory of evals

3. In order for evals to compile in Intellij you need missing jars that are usually included with the liferay tomcat server.
    - Follow the instructions [here](#setup-a-local-test-environment) to get the `bundles` files.
    - Navigate to `bundles/tomcat-${version}/lib/ext/`
    - Copy `portlet.jar` and `portal-service.jar` to the evals repo
    - Sometimes you need `servlet-api.jar` which is one directory up in `bundles/tomcat-${version}/lib/`
    - Add the jars to the `lib` dependency in Intellij project settings
    ![Image](images/intellij-test-lib.png)

4. Follow the instructions in [Setup a local test environment](#setup-a-local-test-environment) to setup a mamp server
    - **NOTE THAT RUNNING TESTS WILL REMOVE ALL DATA FROM THE DATABASE AND REPLACE IT WITH TEST DATA**

5. In Intellij navigate to `evals/docroot/WEB-INF/src/edu/osu/cws/evals/`
    - Right click the `tests` folder and choose the option to run tests using Testng
