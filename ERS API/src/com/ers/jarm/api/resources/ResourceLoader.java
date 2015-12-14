package com.ers.jarm.api.resources;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.ers.jarm.api.constants.ERSConstants;
import com.ers.jarm.api.exception.ERSException;

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
 * This class is used for ResourceLoader to load the property file
 * 
 * @class name ResourceLoader.java
 * @date  sep,2014
 * @author MITS
 * @version 1.0
 */
public class ResourceLoader {

	//To read from Properties file
	private static Properties messageProperties = null;

	//To write messages to logger file
	private static final Logger logger = Logger.getLogger(ResourceLoader.class);

	/**
	 * 
	 * @return messageProperties
	 * @throws IOException
	 */
	public static void loadProperties() throws Exception {		
		InputStream inputStream=null;		
		try
		{
			logger.debug("inside getInstance in ResourceLoader");		

			messageProperties = new Properties();

			inputStream = new ResourceLoader().getClass().getResourceAsStream(ERSConstants.PROPERTY_FILE_PATH);

			messageProperties.load(inputStream);
			
		}		
        catch(Exception e)
        {
        	  logger.error("Exception loading ResourceLoader and Message Properties: "+e.getMessage(),e);
              throw new ERSException("Exception loading ResourceLoader and Message Properties: "+e.getMessage(),e);
        }
		finally
		{
			try
			{
				if(null!=inputStream)
				{
					inputStream.close();
				}
			}
			catch(IOException e)
	        {
				logger.error("Exception loading ResourceLoader and Message Properties [Inputstaream did not close properly]: "+e.getMessage(),e); 
				throw new ERSException("Exception loading ResourceLoader and Message Properties [Inputstaream did not close properly]: "+e.getMessage(),e);
	        }			
		}			
	}

	public static Properties getMessageProperties() {
		return messageProperties;
	}

	public static void setMessageProperties(Properties messageProperties) {
		ResourceLoader.messageProperties = messageProperties;
	}		
	
}
