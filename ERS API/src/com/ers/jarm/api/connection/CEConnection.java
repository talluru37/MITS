package com.ers.jarm.api.connection;

import java.util.Properties;

import javax.security.auth.Subject;

import org.apache.log4j.Logger;

import com.ers.jarm.api.constants.ERSConstants;
import com.ers.jarm.api.exception.ERSException;
import com.ers.jarm.api.resources.ResourceLoader;
import com.filenet.api.core.Connection;
import com.filenet.api.core.Domain;
import com.filenet.api.core.Factory;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.util.UserContext;
/*
 * =========================================================================
 * Copyright 2014 NCS Pte. Ltd. All Rights Reserved
 *
 * This software is confidential and proprietary to NCS Pte. Ltd. You shall
 * use this software only in accordance with the terms of the licence
 * agreement you entered into with NCS. No aspect or part or all of this
 * software may be reproduced, modified or disclosed without full and
 * direct written authorisation from NCS.
 *
 * NCS SUPPLIES THIS SOFTWARE ON AN AS IS BASIS. NCS MAKES NO
 * REPRESENTATIONS OR WARRANTIES, EITHER EXPRESSLY OR IMPLIEDLY, ABOUT THE
 * SUITABILITY OR NON-INFRINGEMENT OF THE SOFTWARE. NCS SHALL NOT BE LIABLE
 * FOR ANY LOSSES OR DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING,
 * MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 * =========================================================================
 */
/**   
 * This class is used for CE connection of records object store 
 * 
 * @class name CEConnection.java
 * @date  sep,2014
 * @author MITS
 * @version 1.0
 */
public class CEConnection {

	static Logger logger = Logger.getLogger(CEConnection.class);
	
	public CEConnection() throws Exception {

		try	{
			
			ResourceLoader.loadProperties();

		} catch (Exception objException) {

			logger.error("Exception occurred in CEConnection constructor [Properties are not loaded] : " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());
		}
	}	

	public ObjectStore getObjectStoreWithCEWSIConnection(Properties messageProperties, String ObjectStoreName) throws Exception
	{	
		logger.debug("Entered into getObjectStoreWithCEWSIConnection method");			

		ObjectStore objectStore = null;

		Domain domain = null;

		Connection connection =null;

		Subject subject = null;

		UserContext userContext = null;

		try
		{		
			if(null!=messageProperties && (!messageProperties.isEmpty()))
			{
				if(null!=ObjectStoreName && (!ObjectStoreName.isEmpty()))
				{
					//creating the instance for connection and passing to the uri
					if(null!=messageProperties.getProperty(ERSConstants.WSI_CONNECTION_URI) && (!messageProperties.getProperty(ERSConstants.WSI_CONNECTION_URI).isEmpty()))
					{
						connection = Factory.Connection.getConnection(messageProperties.getProperty(ERSConstants.WSI_CONNECTION_URI));
						//user context for passing credentials
						userContext = UserContext.get();
						if(null==messageProperties.getProperty(ERSConstants.WSI_JAAS_STANZA) || messageProperties.getProperty(ERSConstants.WSI_JAAS_STANZA).isEmpty())
						{
							subject = UserContext.createSubject(connection, messageProperties.getProperty(ERSConstants.USER_NAME), messageProperties.getProperty(ERSConstants.PASS_WORD), "FileNetP8WSI");
						}
						else
						{
							subject = UserContext.createSubject(connection, messageProperties.getProperty(ERSConstants.USER_NAME), messageProperties.getProperty(ERSConstants.PASS_WORD), messageProperties.getProperty(ERSConstants.WSI_JAAS_STANZA));
						}
						//push the subject to user context
						userContext.pushSubject(subject);				
						domain = Factory.Domain.fetchInstance(connection, null, null);
						objectStore = Factory.ObjectStore.fetchInstance(domain, ObjectStoreName, null);
						
						//System.out.println("objectStore : " + objectStore.get_Id());
						
						//logger.info("Objectstore name" + objectStore);
					}
					else
					{
						logger.info("CE WSI connection URI is missing. CE connection can not be established");
					}									
				}
				else
				{
					logger.info("Target ObjectStore need to fetch is not given. ObjectStore can not be retrieved");
				}
			}
			else
			{
				logger.info("Configuration Properties are blank. CE connection can not be established");
			}
		}
		catch (Exception objException) {

			logger.error("Exception occured in getObjectStoreWithCEWSIConnection method" + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());
		}	
		//returning object store to the method
		return objectStore;
	}

	
	public void closeConnection()
	{
		UserContext userContext=UserContext.get();
		if(userContext!=null)
		{
			userContext.popSubject();
		}
		logger.debug("CEConnection connection is closed successfully ...");
	}
	
	public static void main(String[] args) throws Exception {
		
		CEConnection ceConnection = new CEConnection();
		
		ceConnection.getObjectStoreWithCEWSIConnection(ResourceLoader.getMessageProperties(), "ERSFNSITFPOS01");
	}
}
