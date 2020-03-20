/**
 * The MIT License (MIT)
 * 
 * Copyright (c) 2015 - 2019 Compuware Corporation
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

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.Secret;

public class StrobeMeasurementRunner
{	
	private final StrobeMeasurementBuilder smBuilder;
	
	/**
	 * Constructor
	 * 
	 * @param smBuilder
	 * 			  An instance of <code>StrobeMeasurementBuilder</code> containing the arguments.
	 */
	public StrobeMeasurementRunner(StrobeMeasurementBuilder smBuilder)
	{
		this.smBuilder = smBuilder;
	}
	
	/**
	 * Submits the Strobe measurement
	 * 
	 * @param build
	 *			  The current running Jenkins build
	 * @param launcher
	 *            The machine that the files will be checked out.
	 * @param workspaceFilePath
	 *            a directory to check out the source code.
	 * @param listener
	 *            Build listener
	 *            
	 * @return <code>boolean</code> if the build was successful
	 * 
	 * @throws IOException
	 * 			If an error occurred during the Strobe measurement submit
	 */
	public boolean run(final Run<?,?> build, final Launcher launcher, final FilePath workspaceFilePath, final TaskListener listener, Secret token) throws IOException
	{
		String url = smBuilder.getCesUrl() + "/strobe/measurement";
        listener.getLogger().println("Posting to URL " + url);
		HttpPost post = new HttpPost(url);

		// do this so that we will try and send an email since the notification code requires us to attempt to send an email
		String DEFAULT_EMAIL = "dummy@compuware.com"; 
		String emailToUse = smBuilder.getEmailto().isEmpty() ? DEFAULT_EMAIL : StringUtils.trimToEmpty(smBuilder.getEmailto());
				
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"reqType\":\"" + StringUtils.trimToEmpty(smBuilder.getRequestType()) + "\",");
        json.append("\"jobName\":\"" + StringUtils.trimToEmpty(smBuilder.getJobName()) + "\",");
        json.append("\"system\":\"" + StringUtils.trimToEmpty(smBuilder.getSystem()) + "\",");
        json.append("\"tags\":\"" + StringUtils.trimToEmpty(smBuilder.getTags()) + "\",");
        json.append("\"profileName\":\"" + StringUtils.trimToEmpty(smBuilder.getProfileName()) + "\",");
        json.append("\"emailto\":\"" + emailToUse + "\",");
        json.append("\"duration\":\"" + StringUtils.trimToEmpty(smBuilder.getDuration()) + "\",");
        json.append("\"samples\":\"" + StringUtils.trimToEmpty(smBuilder.getSamples()) + "\",");
        json.append("\"limit\":\"" + StringUtils.trimToEmpty(smBuilder.getLimit()) + "\",");
        json.append("\"finalAction\":\"" + StringUtils.trimToEmpty(smBuilder.getFinalAction()) + "\",");
        json.append("\"hlq\":\"" + StringUtils.trimToEmpty(smBuilder.getHlq()) + "\",");
        json.append("\"tranid\":\"" + StringUtils.trimToEmpty(smBuilder.getTransactionId()) + "\",");
        json.append("\"initBy\":\"CI\",");     
        
        // adding api notification json
        if (smBuilder.getUrl() != null && !smBuilder.getUrl().equals("")) {
	        json.append("\"apiNotificationData\":{");
	        json.append("\"method\":\"" + smBuilder.getMethod() + "\",");
	        json.append("\"returnURL\":\"" + smBuilder.getUrl() + "\",");
	        json.append("\"httpHeaders\":[" + getFormattedHeaders(smBuilder.getHeaders()) + "],");
	        json.append("\"body\":\"" + smBuilder.getBody().replaceAll("\"","\\\\\"").replaceAll("\\r|\\n", "") + "\",");
	        json.append("\"triggerType\":0,");
	        json.append("\"events\":\"*\",");
	        json.append("\"productID\":\"STROBE\",");
	        json.append("\"productInstance\":\"strobe\",");
	        json.append("\"resource\":\"\"");
	        json.append("}");
    	}
        else
        {
			listener.getLogger().println("No callback configured");
        }
        json.append("}");
        
        post.setEntity(new StringEntity(json.toString()));
        post.addHeader("Content-Type", "application/json");
        post.addHeader("Authorization", token.getPlainText());
        
        int returnCode = 99999;
        try (CloseableHttpClient httpClient = HttpClients.createDefault();
                CloseableHttpResponse response = httpClient.execute(post))
        {
        	String results = EntityUtils.toString(response.getEntity());        	
			returnCode = getReturnCode(results);
			listener.getLogger().println("Return Code=" + returnCode);
			listener.getLogger().println("Session Request Number=" + getSessionRequestNumber(results));
        }
        
		return returnCode < 5; // got the <5 criteria from the processResponse function in StrobeService.java
	}

	// instead of importing dependencies and other files to parse this json result into an object, we just process the string
	private int getReturnCode(String results) {
		String returnCodeString = "returnCode\":";
		int returnCodeIndex = results.indexOf(returnCodeString);	
		int commaIndex = results.indexOf(",", returnCodeIndex);		
		String returnCode = results.substring(returnCodeIndex + returnCodeString.length(), commaIndex);
		return Integer.parseInt(returnCode);
	}
	
	private int getSessionRequestNumber(String results) {
		String sessionRequestNumberString = "@number\":";
		int sessionRequestNumberIndex = results.indexOf(sessionRequestNumberString);
		int commaIndex = results.indexOf(",", sessionRequestNumberIndex);
		String sessionRequestNumber = results.substring(sessionRequestNumberIndex + sessionRequestNumberString.length(), commaIndex);    
		
		// sometimes theres quotes around the number and sometimes not
		if (sessionRequestNumber.length() == 6) {
			sessionRequestNumber = sessionRequestNumber.substring(1, sessionRequestNumber.length() - 1);
		}
		
		return Integer.parseInt(sessionRequestNumber);
	}

	// need to take the entered headers and format them so the api accepts them
	private String getFormattedHeaders(String headers) {
	    StringBuilder headerJson = new StringBuilder("");
	    
	    if (!headers.contentEquals("")) {
		    String[] headersArray = headers.split(";");	    
		    for (String header : headersArray) {
		        String[] keyValuePair = header.split(":");
		        
		        if (!headerJson.toString().equals("")) {
		            headerJson.append(",");
		        }	        
		        headerJson.append("{");
		        headerJson.append("\"name\":\"" + keyValuePair[0] + "\"");
		        headerJson.append(",");
		        headerJson.append("\"value\":\"" + keyValuePair[1] + "\"");
		        headerJson.append("}");
		    }
	    }
    
	    return headerJson.toString();
	}
}