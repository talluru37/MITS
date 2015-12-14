package com.ers.jarm.api.location.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.ers.jarm.api.bean.LocationBean;
import com.ers.jarm.api.connection.RMConnection;
import com.ers.jarm.api.constants.ERSConstants;
import com.ers.jarm.api.exception.ERSException;
import com.ers.jarm.api.location.ILocationOperations;
import com.ers.jarm.api.resources.ResourceLoader;
import com.ibm.jarm.api.constants.RMRefreshMode;
import com.ibm.jarm.api.core.FilePlanRepository;
import com.ibm.jarm.api.core.Location;
import com.ibm.jarm.api.core.RMFactory;
import com.ibm.jarm.api.exception.RMRuntimeException;

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
 * This Interface is used for Location Operations to create location, 
 * fetch locations and retrieve locations
 * 
 * @class name LocationOperationsImpl.java
 * @date  sep,2014
 * @author MITS
 * @version 1.0
 */
public class LocationOperationsImpl implements ILocationOperations {

	private static Logger logger = Logger.getLogger(LocationOperationsImpl.class);

	public LocationOperationsImpl() throws ERSException
	{

		try
		{
			ResourceLoader.loadProperties();

		}
		catch (Exception objException) 
		{

			logger.error("Exception occurred in LocationOperationsImpl constructor [Properties are not loaded] : " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());
		}
	}
	
	/**
	 * This method is used to fetch the locations
	 * @param locationBean
	 * @return location
	 * @throws ERSException
	 */
	public Location fetchPhysicalLoactions(LocationBean locationBean) throws ERSException {

		logger.info("Entered Into fetchPhysicalLoactions Method");  

		String barcodeId = null;

		List<String> locationList = null;

		Location locationInfo = null;

		Location location = null;

		RMConnection rmConnection = null;

		FilePlanRepository fileplanRepository = null;

		List<Location> locList = null;

		Iterator<Location> locIter = null;

		try {

			rmConnection = new RMConnection();

			fileplanRepository = rmConnection.getFilePlanRepositoryByName(ResourceLoader.getMessageProperties().getProperty(ERSConstants.FILEPLAN_OBJECTSTORE));

			locationList = new ArrayList<String>();

			locList = fileplanRepository.getLocations(null);

			locIter = locList.iterator();

			while(locIter.hasNext()) {

				locationInfo = locIter.next();

				logger.info("location : " + locationInfo.getProperties().getStringValue("BarcodeID"));

				barcodeId = locationInfo.getProperties().getStringValue("BarcodeID");

				locationList.add(barcodeId);

				if(locationBean.getBarcode().equalsIgnoreCase(barcodeId)) {

					location = locationInfo;

					break; 
				}
			}

			boolean isLocAvailable = locationList.contains(locationBean.getBarcode());

			if(!isLocAvailable) {

				logger.info("Create the new physical location");

				location = createPhysicalLocation(locationBean);
			}

		} catch (RMRuntimeException objException) {

			logger.error("RMRuntimeException occured in fetchPhysicalLoactions method : " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} catch (Exception objException) {

			logger.error("Exception occured in fetchPhysicalLoactions method " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());
			
		} finally {
			
			if(rmConnection != null) {				
				rmConnection.closeRMConnection();				
				rmConnection = null;
			}			
			if(fileplanRepository != null) {
				fileplanRepository = null;
			}	
			barcodeId = null;

			if(locationList != null) {
				locationList.clear();
				locationList = null;
			}
			locationInfo = null;
			
			if(locList != null) {
				locList = null;
			}
			
			if(locIter != null) {
				locIter = null;
			}
		}
		return location;
	}

	/**
	 * This method is to Create physical Location 
	 * @param locationBean
	 * @return location
	 * @throws ERSException
	 */
	public Location createPhysicalLocation(LocationBean locationBean) throws ERSException {

		logger.info("Entered into createPhysicalLocation method");

		Location location = null;
		
		FilePlanRepository fileplanRepository = null;
		
		RMConnection rmConnection = null;

		try {

			rmConnection = new RMConnection();

			fileplanRepository = rmConnection.getFilePlanRepositoryByName(ResourceLoader.getMessageProperties().getProperty(ERSConstants.FILEPLAN_OBJECTSTORE));

			location = RMFactory.Location.createInstance(fileplanRepository);

			location.setLocationName(locationBean.getLocationName());
			
			location.setBarcode(locationBean.getBarcode());
			
			location.setDescription(locationBean.getLocDescription());

			location.save(RMRefreshMode.Refresh);

			logger.info("location Id : " + location.getLocationName());

		} catch (RMRuntimeException objException) {

			logger.error("RMRuntimeException occured in createPhysicalLocation method : " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} catch (Exception objException) {

			logger.error("Exception occured in createPhysicalLocation method " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());
			
		} finally {		
			
			if(rmConnection != null) {				
				rmConnection.closeRMConnection();				
				rmConnection = null;
			}			
			if(fileplanRepository != null) {
				fileplanRepository = null;
			}			
		}
		return location;
	}

	/**
	 * This method is to get the physical Location by GUID
	 * @param locationBean
	 * @return location
	 * @throws ERSException
	 */
	public Location getLocationById(LocationBean locationBean) throws ERSException {
		
		logger.info("Entered into getLocationById method");
		
		RMConnection rmConnection = null;
		
		FilePlanRepository fileplanRepository = null;
		
		Location location = null;
		
		try {
			
			rmConnection = new RMConnection();

			fileplanRepository = rmConnection.getFilePlanRepositoryByName(ResourceLoader.getMessageProperties().getProperty(ERSConstants.FILEPLAN_OBJECTSTORE));
			
			location = RMFactory.Location.fetchInstance(fileplanRepository, locationBean.getLocationId(), null);
			
		} catch (RMRuntimeException objException) {

			logger.error("RMRuntimeException occured in createPhysicalLocation method : " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} catch (Exception objException) {

			logger.error("Exception occured in createPhysicalLocation method " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());
			
		} finally {			
			if(rmConnection != null) {				
				rmConnection.closeRMConnection();				
				rmConnection = null;
			}			
			if(fileplanRepository != null) {
				fileplanRepository = null;
			}			
		}
		return location;
	}
	
	public static void main(String[] args) throws ERSException {
		
		LocationOperationsImpl operationsImpl = new LocationOperationsImpl();
		
		LocationBean locationBean = new LocationBean();
		
		locationBean.setLocationName("SLA Demo Location");
		
		operationsImpl.fetchPhysicalLoactions(locationBean);
	}

}
