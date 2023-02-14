### BMC Strobe Measurement Task

Strobe, the leading mainframe application performance management solution,
is traditionally used by Operations teams to pinpoint and resolve application
performance problems found in production. The shift left approach for testing allows
development teams to perform tests earlier in the cycle. The plugin allows Jenkins users 
to setup automated Strobe measurements and get custom callback notifications when complete.

### Prerequisites

The following are required to use this plugin:

-   Jenkins
-   Jenkins Credentials Plugin
-   Strobe license
-   BMC AMI Common Configuration plugin
-   An installation of BMC AMI Common Enterprise Services with Strobe installed.

	**Note**: CES must be version 20.02.x or greater to use the notification callback.
			  This version will also utilize the INITBY of CI (Continuous Integration) identifier,
			  which will allow zAdviser to track this activity.
			  This plugin can still submit measurements using previous CES versions. 
			  Strobe 18.02 PTF SBG422A is required for the INITBY of CI.

### Installing in a Jenkins Instance

Install the BMC Strobe Measurement Task according to the
Jenkins instructions for installing plugins. Dependent plugins will
automatically be installed. (You will still need to separately install 
Common Enterprise Services)
    
### Configuration

1.  In Common Enterprise Services, do the following:

	a. 	Navigate to the Host Connections page and define your host connection that's connected to your Strobe installation.

	b.	Navigate to the Security page and define a Personal Access Token for the above host connection.
    	
    **Note**: Sometimes Strobe on the mainframe can have difficulty determining
    		  the correct host of CES, so you may need to set the "ces.host.address" 
    		  property in ces.properties located at data/ces/config in the CES installation directory.
    		  e.g. ces.host.address=127.0.0.1 or ces.host.address=localhost

2.  In the Jenkins system Jenkins/Manage Jenkins/Configure System screen, go to the Common Configurations section.In the Host Connections section, add the same host connection defined in Common Enterprise Services.
    		
    		-	Make sure the description field matches the description in CES.
    		
    		-	Add the CES URL in the form scheme://host:port    e.g. https://myHost:48226

3.  In the Jenkins system, you should define the personal access token from step 1 as secret text in Credentials. 
	Refer to the Jenkins documentation for the Credentials Plugin.

### Executing a Measurement

1.  On the project Configuration page, in the Build section click Add build step button and select BMC Strobe Measurement Task.

2.  Here select your host connection and token you previously defined above. You can configure both from this screen too.

3.  Fill out the rest of the form as desired. See the help on the right of each individual field if you have any questions.

4.  Click **Save**.

Your project is now configured to start a measurement on the configured job.

## Product Assistance

BMC provides assistance to customers with its documentation, the BMC Support website, and via telephonic conversations with the Customer Support team.

### BMC Support Center

You can access information for BMC products via our Support site, [https://support.bmc.com](https://support.bmc.com/). Support Central provides access to critical information about your BMC products. You can review frequently asked questions, read or download documentation, access product fixes, or e-mail your questions or comments. The first time you access Support Central, you must register and obtain a password. The registration is free.

### Contacting Customer Support

At BMC, we strive to make our products and documentation the best in the industry. Feedback from our customers helps us maintain our quality standards. If you need support services, please obtain the following information before calling BMC's 24-hour telephone support:

- The Azure pipeline job output that contains any error messages or pertinent information.

- The name, release number, and build number of your product. This information is displayed in the installed extensions page. Apply the filter, BMC, to display all of the installed BMC extension.

- Environment information, such as the operating system and release on which the Workbench for Eclipse CLI is installed.

You can contact BMC in one of the following ways:


#### Web

You can report issues via the BMC Support site: [https://support.bmc.com](https://support.bmc.com/).

Note: Please report all high-priority issues by phone.

### Corporate Website

To access the BMC website, go to [https://www.bmc.com/](https://www.bmc.com/). The BMC site provides a variety of product and support information.