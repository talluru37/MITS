package com.ers.jarm.api.connection;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.security.auth.Subject;

import org.apache.log4j.Logger;

import com.ers.jarm.api.constants.ERSConstants;
import com.ers.jarm.api.exception.ERSException;
import com.ers.jarm.api.resources.ResourceLoader;
import com.filenet.api.util.UserContext;
import com.ibm.jarm.api.constants.DomainType;
import com.ibm.jarm.api.core.ContentRepository;
import com.ibm.jarm.api.core.DomainConnection;
import com.ibm.jarm.api.core.FilePlanRepository;
import com.ibm.jarm.api.core.RMDomain;
import com.ibm.jarm.api.core.RMFactory;
import com.ibm.jarm.api.exception.RMRuntimeException;
import com.ibm.jarm.api.property.RMPropertyFilter;
import com.ibm.jarm.api.util.RMUserContext;

import filenet.vw.api.VWException;
import filenet.vw.api.VWSession;

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
 * This class is used for RM connection of file plan object store 
 * and records object store 
 * 
 * @class name RMConnectionCode.java
 * @date  sep,2014
 * @author MITS
 * @version 1.0
 */
public class RMConnection {

	static Logger logger = Logger.getLogger(RMConnection.class);
	
	public RMConnection() throws Exception {

		try
		{
			ResourceLoader.loadProperties();

		}
		catch (Exception objException) 
		{

			logger.error("Exception occurred in RMConnection constructor [Properties are not loaded] : " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());
		}
	}	

	/**
	 * This method is used to get the Jarm domain
	 * @return jarmDomain
	 * @throws ERSException
	 */
	public RMDomain getJarmDomainWithRMWSIConnection(Properties messageProperties) throws ERSException {

		RMUserContext jarmContext = null;

		Subject jaasSubject = null;	

		DomainConnection jarmConnection = null;		

		RMDomain jarmDomain =null;	

		EncryptionUtil encryptionUtil=null;
		
		logger.info("Entered into getJarmDomainWithRMWSIConnection method");

		try {

			if(null!=messageProperties && (!messageProperties.isEmpty()))
			{
				if(null!=messageProperties.getProperty(ERSConstants.WSI_CONNECTION_URI) && (!messageProperties.getProperty(ERSConstants.WSI_CONNECTION_URI).isEmpty()))
				{
					encryptionUtil = new EncryptionUtil();
					
					logger.info(messageProperties.getProperty(ERSConstants.WSI_CONNECTION_URI) + messageProperties.getProperty(ERSConstants.USER_NAME));
					
					jarmConnection = RMFactory.DomainConnection.createInstance(DomainType.P8_CE, messageProperties.getProperty(ERSConstants.WSI_CONNECTION_URI), null);
					
					jaasSubject =RMUserContext.createSubject(jarmConnection, messageProperties.getProperty(ERSConstants.USER_NAME), messageProperties.getProperty(ERSConstants.PASS_WORD), RMUserContext.P8_STANZA_WSI);	

					jarmContext = RMUserContext.get();	

					jarmContext.setSubject(jaasSubject);
					
					// For P8, domainIdent=null indicates 'local' domain.
					jarmDomain = RMFactory.RMDomain.fetchInstance(jarmConnection, null, RMPropertyFilter.MinimumPropertySet);	

					logger.info("JARM Domain Name" + jarmDomain.getName() + " JARM Current User : " + jarmDomain.fetchCurrentUser().getDisplayName());

					return jarmDomain;
				}
				else
				{
					logger.info("RM WSI connection URI is missing. RM connection can not be established");
				}
			}
			else
			{
				logger.info("Configuration Properties are blank. RM connection can not be established");
			}		

		} catch (RMRuntimeException e) {
			
			Throwable e2 = e.getCause();    
			
            throw new ERSException("Runtime exception connecting to IER: "+e.getMessage()+((e2!=null)?": "+e2.getMessage():""),e);		

		} catch (Exception objException) {

			logger.error("Exception occured in getJarmDomainWithRMWSIConnection method : " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());
		} 
		return jarmDomain;
	}	

	/**
	 * This method is used to get the file plan object store 
	 * @return fileplanRepository
	 * @throws ERSException
	 */
	public FilePlanRepository getFilePlanRepositoryByName(String FilePlanRepositorySymbolicName) throws ERSException {	

		List<FilePlanRepository> filePlanRepositories = null;

		FilePlanRepository fileplanRepository = null;

		logger.info("Entered into getFilePlanRepositoryByName method ");

		try	{

			RMDomain jarmDomain = getJarmDomainWithRMWSIConnection(ResourceLoader.getMessageProperties());			

			filePlanRepositories =jarmDomain.fetchFilePlanRepositories(RMPropertyFilter.MinimumPropertySet);

			logger.debug("File Plan Repositories : " + filePlanRepositories);

			Iterator<FilePlanRepository> fileplanIteraror = filePlanRepositories.iterator();		

			while(fileplanIteraror.hasNext()) {

				fileplanRepository = fileplanIteraror.next();
				
				if(fileplanRepository.getSymbolicName().equalsIgnoreCase(FilePlanRepositorySymbolicName))
				{
					break;
				}
				logger.info("Fileplan Repository : " + fileplanRepository.getSymbolicName());				
			}
			return fileplanRepository;

		} catch (RMRuntimeException objException) {

			logger.error("RMRuntimeException occured in getFilePlanRepositoryByName method : " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} catch (Exception objException) {

			logger.error("Exception occured in getFilePlanRepositoryByName method : " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());
		} 		
	}		

	/**
	 * This method is used to get the ROS object store
	 * @return contentRepositories
	 * @throws ERSException
	 */
	public ContentRepository getRecordsRepositoryByName(String RecordsRepositorySymbolicName) throws ERSException {

		logger.info("Entered into getRecordsRepositoryByName method " + RecordsRepositorySymbolicName);

		ContentRepository contentRepository = null;

		List<ContentRepository>  contentRepositories = null;

		try {

			RMDomain jarmDomain = getJarmDomainWithRMWSIConnection(ResourceLoader.getMessageProperties());

			contentRepositories =jarmDomain.fetchContentRepositories(false, RMPropertyFilter.MinimumPropertySet);

			//logger.info("Content Repositories : " + contentRepositories);

			Iterator<ContentRepository> iterContentRepository = contentRepositories.iterator();

			while(iterContentRepository.hasNext()) {

				contentRepository = iterContentRepository.next();
				
				logger.info("contentRepository : " + contentRepository.getSymbolicName());

				if(contentRepository.getSymbolicName().equalsIgnoreCase(RecordsRepositorySymbolicName))
				{
					break;
				}
				logger.info("Record Content Repository : " + contentRepository.getSymbolicName());
				//return contentRepository ;	
			}

		} catch (RMRuntimeException objException) {

			logger.error("RMRuntimeException occured in getFilePlanRepositories method : " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} catch (Exception objException) {

			logger.error("Exception occured in getFilePlanRepositories method : " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());
		} 	
		return contentRepository;
	}

	/**
	 * This method is used to get the VWsession
	 * @return vwSession
	 * @throws ERSException
	 */
	public VWSession getWSIVWsession() throws ERSException {

		logger.info("Entered into getWSIVWsession method");

		VWSession vwSession = null;
		
		EncryptionUtil encryptionUtil =null;

		try {		
			//creating a session by using the VWsession class
			vwSession = new VWSession();

			//setting the bootstrap by uri
			vwSession.setBootstrapCEURI(ResourceLoader.getMessageProperties().getProperty(ERSConstants.WSI_CONNECTION_URI));

			encryptionUtil= new EncryptionUtil();

			//calling the log on method  here
			vwSession.logon(ResourceLoader.getMessageProperties().getProperty(ERSConstants.USER_NAME), ResourceLoader.getMessageProperties().getProperty(ERSConstants.PASS_WORD), ResourceLoader.getMessageProperties().getProperty(ERSConstants.CONNECTION_POINT));

			logger.info("The PE connection is established" + vwSession.getConnectionPointName());

		} catch (VWException objException) {

			logger.error("VWException occured in getWSIVWsession : " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} catch (Exception objException) {

			logger.error("Exception occured in getWSIVWsession : " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());
		} 
		return vwSession;
	}
	
	public boolean closeVWSession(VWSession objVWSession) throws VWException
	{		
		try
		{
			if(null!=objVWSession)
			{
				if(objVWSession.isLoggedOn())
				{
					objVWSession.logoff();					
				}
				objVWSession=null;				
			}
			else
			{
				logger.info("VWSession object is already null. No logoff action required.");
			}
			return true;
		}
		catch(VWException objVWException)
		{
			logger.error(objVWException);
			throw objVWException;
		}
	}
	
	public void closeRMConnection()
	{
		RMUserContext userContext=RMUserContext.get();
		if(userContext!=null)
		{
			userContext.setSubject(null);
		}
		logger.info("RM Connection connection is closed successfully ...");
	}

	public static void main(String[] args) throws Exception {
		
		RMConnection rmConnection = new RMConnection();
		
		ContentRepository contentRepository = rmConnection.getRecordsRepositoryByName("ERSFNSITROS01");
		
		logger.info("contentRepository : " + contentRepository.getSymbolicName());
	}
}
