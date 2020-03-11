### Overview

Strobe, the leading mainframe application performance management solution,
is traditionally used by Operations teams to pinpoint and resolve application
performance problems found in production. The shift left approach for testing allows
development teams to perform tests earlier in the cycle. The plugin allows Jenkins users 
to setup automated Strobe meausurements and get custom callback notifications when complete.

### Prerequisites

The following are required to use this plugin:

-   Jenkins
-   Jenkins Credentials Plugin
-   Strobe license
-   Compuware Common Configuration plugin
-   An installation of Compuware Enterprise Services with Strobe installed.

	**Note**: CES must be version 20.02.x or greater to use the notification callback.
			  This version will also utilize the INITBY of CI (Continuous Integration) identifier,
			  which will allow zAdviser to track this activity.
			  This plugin can still submit measurements using previous CES versions. 
			  Strobe 18.02 ptf SBG422A is required for the INITBY of CI.

### Installing in a Jenkins Instance

Install the Compuware Strobe Measurement plugin according to the
Jenkins instructions for installing plugins. Dependent plugins will
automatically be installed. (You will still need to separately install 
Compuware Enterprise Services)
    
### Configuration

1.  In Compuware Enterprise Services, do the following:
	
	- 	Navigate to the Host Connections page and define your host connection
    	that's connected to your Strobe installation. 
    	
    -	Navigate to the Security page and define a Personal Access Token
    	for the above host connection.
    	
    **Note**: Sometimes Strobe on the mainframe can have difficulty determining
    		  the correct host of CES, so you may need to set the "ces.host.address" 
    		  property in ces.properties located at data/ces/config in the CES installation directory.
    		  e.g. ces.host.address=127.0.0.1 or ces.host.address=localhost

2.  In the Jenkins system Jenkins/Manage Jenkins/Configure System
    screen, go to the Compuware Configurations section, do the following:
    
    -	In the Host Connections section, add the same host connection
    	as defined in Compuware Enterprise Services.
    		
    		-	Make sure the description field matches the description in CES.
    		
    		-	Add the CES URL in the form scheme://host:port    e.g. https://myHost:48226

3.  In the Jenkins system, you should define the personal access token from step 1 as secret text in Credentials. 
	Refer to the Jenkins documentation for the Credentials Plugin.

### Executing a Measurement

1.  On the project Configuration page, in the Build section click Add build step button and select Compuware Strobe Measurement.

2.  Here select your host connection and token you previously defined above. You can configure both from this screen too.

3.  Fill out the rest of the form as desired. See the help on the right of each individual field if you have any questions.

4.  Click **Save**.

Your project is now configured to start a measurement on the configured job.
	
	**Note**: We also support pipeline syntax, and Jenkins will help you generate it.
			  You will fill out the form the same way, but then click **Generate Pipeline Script**