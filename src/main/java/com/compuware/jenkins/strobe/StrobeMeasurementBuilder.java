/**
 * The MIT License (MIT)
 * 
 * Copyright (c) 2015 - 2018 Compuware Corporation
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions: The above copyright notice and this permission notice
 * shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.compuware.jenkins.strobe;

import static com.cloudbees.plugins.credentials.CredentialsMatchers.filter;
import static com.cloudbees.plugins.credentials.CredentialsMatchers.withId;
import static com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.verb.GET;
import org.kohsuke.stapler.verb.POST;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.compuware.jenkins.common.configuration.CpwrGlobalConfiguration;
import com.compuware.jenkins.common.configuration.HostConnection;

import hudson.AbortException;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractProject;
import hudson.model.Item;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.security.ACL;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import hudson.util.ListBoxModel.Option;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;

public class StrobeMeasurementBuilder extends Builder implements SimpleBuildStep
{
	private static final String EQUAL = "=";

	private String connectionId;
	private String credentialsId;
	private String requestType;
	private String jobName;
	private String cesUrl;
	private String system;
	private String tags;
	private String profileName;
	private String emailto;
	private String duration;
	private String samples;
	private String limit;
	private String finalAction;
	private String hlq;
	private String transactionId;

	private String method;
	private String url;
	private String headers;
	private String body;
	
	@DataBoundConstructor
	public StrobeMeasurementBuilder(String connectionId, String credentialsId, String requestType, String jobName)
	{
		this.connectionId = StringUtils.trimToEmpty(connectionId);
		this.credentialsId = StringUtils.trimToEmpty(credentialsId);
		this.requestType = StringUtils.trimToEmpty(requestType);
		this.jobName = StringUtils.trimToEmpty(jobName);
		
		CpwrGlobalConfiguration globalConfig = CpwrGlobalConfiguration.get();
		HostConnection hostConnection = globalConfig.getHostConnection(connectionId);		
		if (hostConnection != null) {
			this.cesUrl = StringUtils.trimToEmpty(hostConnection.getCesUrl());
			this.system = StringUtils.trimToEmpty(hostConnection.getDescription());
		}
	}
    
	public String getConnectionId()
	{
		return connectionId;
	}
	
	public String getCredentialsId()
	{
		return credentialsId;
	}
	
	public String getRequestType()
	{
		return requestType;
	}
	
	public String getJobName()
	{
		return jobName;
	}

	public String getCesUrl() {
		return cesUrl;
	}

	public String getSystem() {
		return system;
	}
	
	public String getTags() {
		return tags;
	}

	@DataBoundSetter
	public void setTags(String tags) {
		this.tags = tags;
	}

	public String getProfileName() {
		return profileName;
	}

	@DataBoundSetter
	public void setProfileName(String profileName) {
		this.profileName = profileName;
	}

	public String getEmailto() {
		return emailto;
	}

	@DataBoundSetter
	public void setEmailto(String emailto) {
		this.emailto = emailto;
	}

	public String getDuration() {
		return duration;
	}

	@DataBoundSetter
	public void setDuration(String duration) {
		this.duration = duration;
	}

	public String getSamples() {
		return samples;
	}

	@DataBoundSetter
	public void setSamples(String samples) {
		this.samples = samples;
	}

	public String getLimit() {
		return limit;
	}

	@DataBoundSetter
	public void setLimit(String limit) {
		this.limit = limit;
	}

	public String getFinalAction() {
		return finalAction;
	}

	@DataBoundSetter
	public void setFinalAction(String finalAction) {
		this.finalAction = finalAction;
	}

	public String getHlq() {
		return hlq;
	}

	@DataBoundSetter
	public void setHlq(String hlq) {
		this.hlq = hlq;
	}

	public String getTransactionId() {
		return transactionId;
	}

	@DataBoundSetter
	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public String getMethod() {
		return method;
	}

	@DataBoundSetter
	public void setMethod(String method) {
		this.method = method;
	}

	public String getUrl() {
		return url;
	}

	@DataBoundSetter
	public void setUrl(String url) {
		this.url = url;
	}

	public String getHeaders() {
		return headers;
	}

	@DataBoundSetter
	public void setHeaders(String headers) {
		this.headers = headers;
	}

	public String getBody() {
		return body;
	}

	@DataBoundSetter
	public void setBody(String body) {
		this.body = body;
	}
	
    @Override
    public void perform(final Run<?,?> build, final FilePath workspaceFilePath, final Launcher launcher, final TaskListener listener) throws AbortException
    {    	
		try
		{
			// get the token from the selected credential
			List<StringCredentials> credentials = filter(lookupCredentials(StringCredentials.class, build.getParent(), ACL.SYSTEM, Collections.<DomainRequirement> emptyList()), withId(StringUtils.trimToEmpty(credentialsId)));
			Secret token = null;
			if (credentials != null && credentials.size() > 0) {
				StringCredentials credential = credentials.get(0);
				token = credential.getSecret();
			}

			validateParameters(launcher, listener, build.getParent());
			
			StrobeMeasurementRunner runner = new StrobeMeasurementRunner(this);
			boolean success = runner.run(build, launcher, workspaceFilePath, listener, token);
			if (success == false)
			{
				throw new AbortException(Messages.strobeMeasurementFailure());
			}
			else
			{
				listener.getLogger().println(Messages.strobeMeasurementSuccess());
				listener.getLogger().println("When the measurement is complete, view the generated profile at " + getCesUrl() + "/istrobe/jsp/myStrobe/myStrobe.jsp");
			}
					
		}
		catch (Exception e)
		{
			listener.getLogger().println(e.getMessage());
			throw new AbortException();
		}
    }
    
	public void validateParameters(final Launcher launcher, final TaskListener listener, final Item project)
	{
		if (getConnectionId().isEmpty() == true)
		{
			throw new IllegalArgumentException(Messages.errorMissingParameter(Messages.hostConnection())); 
		}
		
		if (getCredentialsId().isEmpty() == true)
		{
			throw new IllegalArgumentException(Messages.errorMissingParameter(Messages.credentials())); 
		}
		
		if (getRequestType().isEmpty() == false)
		{
			listener.getLogger().println(Messages.requestType() + EQUAL + getRequestType());
		}
		else
		{
			throw new IllegalArgumentException(Messages.errorMissingParameter(Messages.requestType()));
		}
		
		if (getJobName().isEmpty() == false)
		{
			listener.getLogger().println(Messages.jobName() + EQUAL + getJobName());
		}
		else
		{
			throw new IllegalArgumentException(Messages.errorMissingParameter(Messages.jobName()));
		}
		
		if (getCesUrl().isEmpty() == false)
		{
			if(!cesUrl.startsWith("http")) {
				throw new IllegalArgumentException(Messages.errorInvalidCesUrl());
			}
			
			listener.getLogger().println(Messages.cesUrl() + EQUAL + getCesUrl());			
		}
		else 
		{
			throw new IllegalArgumentException(Messages.errorMissingParameter(Messages.errorMissingCesUrl()));
		}
		
		if (getSystem().isEmpty() == false)
		{			
			listener.getLogger().println(Messages.system() + EQUAL + getSystem());
		}
		else 
		{
			throw new IllegalArgumentException(Messages.errorMissingParameter(Messages.errorMissingSystem()));
		}
		
		if (getTags().isEmpty() == false)
		{
			listener.getLogger().println(Messages.tags() + EQUAL + getTags());
		}
		
		if (getProfileName().isEmpty() == false)
		{
			listener.getLogger().println(Messages.profileName() + EQUAL + getProfileName());
		}
		
		if (getEmailto().isEmpty() == false)
		{
			listener.getLogger().println(Messages.emailTo() + EQUAL + getEmailto());
		}
		
		if (getDuration().isEmpty() == false)
		{
			listener.getLogger().println(Messages.duration() + EQUAL + getDuration());
		}
		
		if (getSamples().isEmpty() == false)
		{
			listener.getLogger().println(Messages.samples() + EQUAL + getSamples());
		}
		
		if (getLimit().isEmpty() == false)
		{
			listener.getLogger().println(Messages.limit() + EQUAL + getLimit());
		}
		
		if (getFinalAction().isEmpty() == false)
		{
			if (getFinalAction().equals("nolimit"))
			{
				listener.getLogger().println(Messages.finalAction() + EQUAL + "continue");
			}
			else {
				listener.getLogger().println(Messages.finalAction() + EQUAL + getFinalAction());
			}
		}

		if (getHlq().isEmpty() == false)
		{
			listener.getLogger().println(Messages.hlq() + EQUAL + getHlq());
		}

		if (getTransactionId().isEmpty() == false)
		{
			listener.getLogger().println(Messages.transactionId() + EQUAL + getTransactionId());
		}

		if (getUrl().isEmpty() == false)
		{
			if (getMethod().isEmpty() == false)
			{
				listener.getLogger().println(Messages.method() + EQUAL + getMethod());
			}
			
			listener.getLogger().println(Messages.url() + EQUAL + getUrl());

			if (getHeaders().isEmpty() == false)
			{
				listener.getLogger().println(Messages.headers() + EQUAL + getHeaders());
			}

			if (getBody().isEmpty() == false)
			{
				listener.getLogger().println(Messages.body() + EQUAL + getBody());
			}
		}
	}
	
	@Symbol("strobeMeasurement")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> 
    {
        /**
         * In order to load the persisted global configuration, you have to 
         * call load() in the constructor.
         */
        public DescriptorImpl()
        {
            load();
         }

		@SuppressWarnings("rawtypes")
		@Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass)
        {
            // Indicates that this builder can be used with all kinds of project types 
            return true;
        }

		@Override
        public String getDisplayName()
        {
            return Messages.displayName();
        }
		
        @Override
        public boolean configure(final StaplerRequest req, final JSONObject formData) throws FormException
        {
            save();
            return super.configure(req,formData);
        }

		@POST
		public ListBoxModel doFillConnectionIdItems(@AncestorInPath Jenkins context, @QueryParameter String connectionId,
				@AncestorInPath Item project)
		{
			CpwrGlobalConfiguration globalConfig = CpwrGlobalConfiguration.get();
			HostConnection[] hostConnections = globalConfig.getHostConnections();

			ListBoxModel model = new ListBoxModel();
			model.add(new Option(StringUtils.EMPTY, StringUtils.EMPTY, false));

			for (HostConnection connection : hostConnections)
			{
				boolean isSelected = false;
				if (connectionId != null)
				{
					isSelected = connectionId.matches(connection.getConnectionId());
				}

				model.add(new Option(connection.getDescription() + " [" + connection.getHostPort() + ']', //$NON-NLS-1$
						connection.getConnectionId(), isSelected));
			}

			return model;
		}

		@POST
		public static ListBoxModel doFillCredentialsIdItems(@AncestorInPath Jenkins context, @QueryParameter String credentialsId,
				@AncestorInPath Item project)
		{
			List<StringCredentials> creds = CredentialsProvider.lookupCredentials(
					StringCredentials.class, project, ACL.SYSTEM,
					Collections.<DomainRequirement> emptyList());

			StandardListBoxModel model = new StandardListBoxModel();

			model.add(new Option(StringUtils.EMPTY, StringUtils.EMPTY, false));

			for (StringCredentials c : creds) {
				boolean isSelected = false;

				if (credentialsId != null) {
					isSelected = credentialsId.matches(c.getId());
				}

				String description = StringUtils.trimToEmpty(c.getDescription());
				model.add(new Option(description, c.getId(), isSelected));
			}

			return model;
		}

		@GET
		public FormValidation doCheckConnectionId(@QueryParameter final String value)
		{
			if (Util.fixEmptyAndTrim(value) == null)
			{
				return FormValidation.error(Messages.checkConnectionIdError());
			}

			return FormValidation.ok();
		}

		@POST
		public FormValidation doCheckCredentialsId(@QueryParameter final String value)
		{
			if (Util.fixEmptyAndTrim(value) == null)
			{
				return FormValidation.error(Messages.checkLoginCredentialError());
			}

			return FormValidation.ok();
		}
		
		@POST
		public FormValidation doCheckJobName(@QueryParameter final String value)
		{
			if (Util.fixEmptyAndTrim(value) == null)
			{
				return FormValidation.error(Messages.checkJobNameError());
			}

			return FormValidation.ok();
		}
    }
}