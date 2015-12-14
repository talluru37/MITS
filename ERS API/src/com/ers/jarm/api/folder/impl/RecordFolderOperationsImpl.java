package com.ers.jarm.api.folder.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.ers.jarm.api.bean.RecordBean;
import com.ers.jarm.api.bean.RecordFolderBean;
import com.ers.jarm.api.bean.VolumeBean;
import com.ers.jarm.api.connection.RMConnection;
import com.ers.jarm.api.constants.ERSConstants;
import com.ers.jarm.api.constants.ERSFolderProperty;
import com.ers.jarm.api.constants.ERSVolumeProperty;
import com.ers.jarm.api.exception.ERSException;
import com.ers.jarm.api.folder.IRecordFolderOperations;
import com.ers.jarm.api.resources.ResourceLoader;
import com.ers.jarm.api.util.ERSUtil;
import com.ibm.jarm.api.collection.PageableSet;
import com.ibm.jarm.api.constants.DeleteMode;
import com.ibm.jarm.api.constants.DomainType;
import com.ibm.jarm.api.constants.RMRefreshMode;
import com.ibm.jarm.api.constants.SchedulePropagation;
import com.ibm.jarm.api.core.DispositionSchedule;
import com.ibm.jarm.api.core.FilePlanRepository;
import com.ibm.jarm.api.core.Location;
import com.ibm.jarm.api.core.RMFactory;
import com.ibm.jarm.api.core.RecordCategory;
import com.ibm.jarm.api.core.RecordContainer;
import com.ibm.jarm.api.core.RecordFolder;
import com.ibm.jarm.api.core.RecordFolderContainer;
import com.ibm.jarm.api.core.RecordVolume;
import com.ibm.jarm.api.exception.RMRuntimeException;
import com.ibm.jarm.api.meta.RMClassDescription;
import com.ibm.jarm.api.meta.RMPropertyDescription;
import com.ibm.jarm.api.property.RMProperties;
import com.ibm.jarm.api.property.RMPropertyFilter;

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
 * This class is used for Record Operations implementations to create record, 
 * view record, update record and delete record
 * 
 * @class name RecordFolderOperationsImpl.java
 * @date  sep,2014
 * @author MITS
 * @version 1.0
 */
public class RecordFolderOperationsImpl implements IRecordFolderOperations {

	static Logger logger = Logger.getLogger(RecordFolderOperationsImpl.class);

	static Properties messageProperties = null;

	public RecordFolderOperationsImpl() throws ERSException
	{
		try
		{
			ResourceLoader.loadProperties();

		}
		catch (Exception objException) 
		{

			logger.error("Exception occurred in RecordFolderOperationsImpl constructor [Properties are not loaded] : " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());
		}
	}	

	/**
	 * This method is used to create record folder
	 * @param folderBean	 
	 * @param volumeBean
	 * @return folderBean
	 * @throws ERSException
	 */
	public RecordFolderBean createRecordFolder(RecordFolderBean folderBean, VolumeBean volumeBean) throws ERSException {

		logger.info("folderBean : " + folderBean.toString());

		logger.info("volumeBean : " + volumeBean.toString());

		String folderType = null;

		try {
			folderType = folderBean.getFolderType();

			if(folderType.equalsIgnoreCase("P"))
			{
				return createPhysicalRecordFolder(folderBean, volumeBean);
			}
			else if(folderType.equalsIgnoreCase("E"))
			{
				return createElectronicRecordFolder(folderBean, volumeBean);
			}
			else
			{
				logger.info("Invalid Folder Type ["+folderType+"]");
			}			
		} catch (RMRuntimeException objException) {

			logger.error("RMRuntimeException occured in createRecordFolder method : " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} catch(Exception objException) {

			logger.error("Exception occured in createRecordFolder method " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());
		} 
		return  folderBean;

	}

	/**
	 * This method is used to create electronic folder
	 * @param folderBean	 
	 * @param volumeBean
	 * @return folderBean
	 * @throws ERSException
	 */
	public RecordFolderBean createElectronicRecordFolder(RecordFolderBean folderBean, VolumeBean volumeBean) throws ERSException {

		RMConnection rmConnection = null;

		RecordCategory recordCategory = null;

		RMProperties rmProperties = null;

		RecordFolder newRecordFolder = null;

		FilePlanRepository  fileplanRepository = null;

		ERSUtil ersUtil = null;

		String dispScheduleId = null;	

		DispositionSchedule dispositionSchedule = null;

		String volumeId = null;
		
		Location location = null;

		try	{

			if(null!=folderBean && null!=volumeBean)
			{
				if(null!=folderBean.getRecordCategoryId())
				{
					if(!folderBean.getRecordCategoryId().equalsIgnoreCase(""))
					{
						if(null!=folderBean.getFolderTitle() || null!=folderBean.getFdrRefNo() || null!=folderBean.getTaxonomyReferenceNo())
						{
							rmConnection = new RMConnection();

							fileplanRepository = rmConnection.getFilePlanRepositoryByName(ResourceLoader.getMessageProperties().getProperty(ERSConstants.FILEPLAN_OBJECTSTORE));

							//logger.info("fileplanRepository" + fileplanRepository.getDisplayName());

							recordCategory = RMFactory.RecordCategory.fetchInstance(fileplanRepository, folderBean.getRecordCategoryId(), null);

							rmProperties = RMFactory.RMProperties.createInstance(DomainType.P8_CE);
							
							location=RMFactory.Location.getInstance(fileplanRepository, ResourceLoader.getMessageProperties().getProperty(ERSConstants.LOCATION_IDENTIFIER));
							if(null!=location)
							{
								rmProperties.putObjectValue(ERSFolderProperty.HOME_LOCATION, location);									
							}
							else
							{
								location=RMFactory.Location.createInstance(fileplanRepository, folderBean.getLocationCode());
								rmProperties.putObjectValue(ERSFolderProperty.HOME_LOCATION, location);
							}

							rmProperties.putStringValue(ERSFolderProperty.RECORD_FOLDER_NAME, folderBean.getFolderTitle());

							rmProperties.putStringValue(ERSFolderProperty.RECORD_FOLDER_IDENTIFIER, folderBean.getFolderTitle());

							rmProperties.putStringValue(ERSFolderProperty.FDR_REF_NO, folderBean.getFdrRefNo());

							rmProperties.putStringValue(ERSFolderProperty.TAXONOMY_REFERENCE_NUMBER, folderBean.getTaxonomyReferenceNo());

							rmProperties.putStringValue(ERSFolderProperty.DESCRIPTION, folderBean.getDescription());

							rmProperties.putStringValue(ERSFolderProperty.TAG, folderBean.getTags());

							rmProperties.putStringValue(ERSFolderProperty.FDR_LFCY_ST, folderBean.getFdrLfcyStat());

							rmProperties.putDateTimeValue(ERSFolderProperty.FIRST_ENCL_DATE, folderBean.getFirstEnclDate());

							rmProperties.putDateTimeValue(ERSFolderProperty.LAST_ENCL_DATE, folderBean.getLastEnclDate());

							rmProperties.putStringValue(ERSFolderProperty.SEC_CLASS, folderBean.getSecClass());

							rmProperties.putIntegerValue(ERSFolderProperty.RANDD_PERIOD, folderBean.getRandDPeriod());

							rmProperties.putStringValue(ERSFolderProperty.OWNER_ROLE, folderBean.getOwnerRole());

							rmProperties.putStringValue(ERSFolderProperty.OWNER_OU, folderBean.getOwnerOU());

							newRecordFolder = recordCategory.addRecordFolder(ERSFolderProperty.ERS_HYBRID_RECORD_FOLDER, rmProperties, null);						

							ersUtil = new ERSUtil();

							Map<Integer, String> dispositionMap = ersUtil.getDispositionSchedule(folderBean.getRandDPeriod());

							dispScheduleId = dispositionMap.get(folderBean.getRandDPeriod());

							logger.info("dispScheduleId : " + dispScheduleId);

							if(dispScheduleId == null) {

								dispScheduleId = ersUtil.createDispositionSchedule(folderBean.getRandDPeriod());
							}

							dispositionSchedule = RMFactory.DispositionSchedule.fetchInstance(fileplanRepository, dispScheduleId, null);			

							logger.info("Created new record folder"+ newRecordFolder.getRecordFolderName());	

							newRecordFolder.assignDispositionSchedule(dispositionSchedule, SchedulePropagation.ToAllInheritors);

							newRecordFolder.save(RMRefreshMode.Refresh);

							//logger.info(" Get Volume Name of electronic record" + recordVolume.getVolumeName());

							folderBean.setRecordFolderId(newRecordFolder.getProperties().getGuidValue("Id"));

							logger.info("Electronic Record Folder Id : " + newRecordFolder.getProperties().getGuidValue("Id"));

							volumeId = addPropertiesToVolume(newRecordFolder, volumeBean);

							folderBean.setRecordVolumeId(volumeId);
						}
						else
						{
							logger.error("Folder Title, Folder Reference No and Taxonomy Reference No. are mandatory fields for Folder creation. Either 1 of OR all mandatory fields are missing. Folder can not be created");
						}						
					}
					else
					{
						logger.error("Mandatory Taxonomy Node id is blank which is must for Folder creation. Folder can not be created");
					}
				}
				else
				{
					logger.error("Mandatory Taxonomy Node id is null which is must for Folder creation. Folder can not be created");
				}					
			}
			else
			{
				logger.error("Mandatory FolderBean and VolumeBean are missing. Folder can not be created");
			}			

		} catch (RMRuntimeException objException) {

			logger.error("RMRuntimeException occured in createRecordFolder method : " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} catch(Exception objException) {

			logger.error("Exception occured in createRecordFolder method " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} finally {

			recordCategory = null;

			rmProperties = null;

			newRecordFolder = null;			

			fileplanRepository = null;

			ersUtil = null;

			dispScheduleId = null;

			dispositionSchedule = null;	

			if(null!=rmConnection)
				rmConnection.closeRMConnection();

		}
		return folderBean;
	}

	/**
	 * This method is used to create physical folder
	 * @param folderBean
	 * @param volumeBean
	 * @return folderBean
	 * @throws ERSException
	 */
	public RecordFolderBean createPhysicalRecordFolder(RecordFolderBean folderBean, VolumeBean volumeBean) throws ERSException {

		RMConnection rmConnection = null;

		RecordCategory recordCategory = null;

		RMProperties rmProperties = null;

		Location location=null;

		RecordFolder newRecordFolder = null;

		FilePlanRepository  fileplanRepository = null;

		ERSUtil ersUtil = null;

		String dispScheduleId = null;	

		DispositionSchedule dispositionSchedule = null;

		String volumeId = null;

		try	{

			if(null!=folderBean && null!=volumeBean)
			{
				if(null!=folderBean.getRecordCategoryId())
				{
					if(!folderBean.getRecordCategoryId().equalsIgnoreCase(""))
					{
						if(null!=folderBean.getFolderTitle() || null!=folderBean.getFdrRefNo() || null!=folderBean.getTaxonomyReferenceNo() || null!=folderBean.getLocationCode() || null!=folderBean.getReviewer())
						{
							if((!folderBean.getFolderTitle().equalsIgnoreCase("")) || (!folderBean.getFdrRefNo().equalsIgnoreCase("")) || (!folderBean.getTaxonomyReferenceNo().equalsIgnoreCase("")) || (!folderBean.getLocationCode().equalsIgnoreCase("")) || (!folderBean.getReviewer().equalsIgnoreCase("")))
							{
								rmConnection = new RMConnection();

								fileplanRepository = rmConnection.getFilePlanRepositoryByName(ResourceLoader.getMessageProperties().getProperty(ERSConstants.FILEPLAN_OBJECTSTORE));

								recordCategory = RMFactory.RecordCategory.fetchInstance(fileplanRepository, folderBean.getRecordCategoryId(), null);

								rmProperties = RMFactory.RMProperties.createInstance(DomainType.P8_CE);

								// Need to define the locations in IER corresponding to FRS same like taxonomy and store in ERS DB
								// Developers need to pass the GUID of the locations in order to get the location and bind with record and folder
								// folderBean.getLocationCode() is IER GUID of the location not name OR barcode

								location=RMFactory.Location.fetchInstance(fileplanRepository, ResourceLoader.getMessageProperties().getProperty(ERSConstants.LOCATION_IDENTIFIER), null);
								if(null!=location)
								{
									rmProperties.putObjectValue(ERSFolderProperty.HOME_LOCATION, location);									
								}
								else
								{
									location=RMFactory.Location.createInstance(fileplanRepository, folderBean.getLocationCode());
									rmProperties.putObjectValue(ERSFolderProperty.HOME_LOCATION, location);
								}								

								rmProperties.putStringValue(ERSFolderProperty.RECORD_FOLDER_NAME, folderBean.getFolderTitle());

								rmProperties.putStringValue(ERSFolderProperty.RECORD_FOLDER_IDENTIFIER, folderBean.getFolderTitle());

								rmProperties.putStringValue(ERSFolderProperty.FDR_REF_NO, folderBean.getFdrRefNo());

								rmProperties.putStringValue(ERSFolderProperty.TAXONOMY_REFERENCE_NUMBER, folderBean.getTaxonomyReferenceNo());

								rmProperties.putStringValue(ERSFolderProperty.DESCRIPTION, folderBean.getDescription());

								rmProperties.putStringValue(ERSFolderProperty.TAG, folderBean.getTags());

								rmProperties.putStringValue(ERSFolderProperty.FDR_LFCY_ST, folderBean.getFdrLfcyStat());

								rmProperties.putDateTimeValue(ERSFolderProperty.FIRST_ENCL_DATE, folderBean.getFirstEnclDate());

								rmProperties.putDateTimeValue(ERSFolderProperty.LAST_ENCL_DATE, folderBean.getLastEnclDate());

								rmProperties.putStringValue(ERSFolderProperty.SEC_CLASS, folderBean.getSecClass());

								rmProperties.putIntegerValue(ERSFolderProperty.RANDD_PERIOD, folderBean.getRandDPeriod());

								rmProperties.putStringValue(ERSFolderProperty.OWNER_ROLE, folderBean.getOwnerRole());

								rmProperties.putStringValue(ERSFolderProperty.OWNER_OU, folderBean.getOwnerOU());		

								rmProperties.putStringValue(ERSFolderProperty.REVIEWER, folderBean.getReviewer());	

								newRecordFolder = recordCategory.addRecordFolder(ERSFolderProperty.ERS_HYBRID_RECORD_FOLDER, rmProperties, null);						

								ersUtil = new ERSUtil();

								Map<Integer, String> dispositionMap = ersUtil.getDispositionSchedule(folderBean.getRandDPeriod());

								dispScheduleId = dispositionMap.get(folderBean.getRandDPeriod());

								logger.info("dispScheduleId : " + dispScheduleId);

								if(dispScheduleId == null) {

									dispScheduleId = ersUtil.createDispositionSchedule(folderBean.getRandDPeriod());
								}

								dispositionSchedule = RMFactory.DispositionSchedule.fetchInstance(fileplanRepository, dispScheduleId, null);			

								logger.info("Created new record folder"+ newRecordFolder.getRecordFolderName());	

								newRecordFolder.assignDispositionSchedule(dispositionSchedule, SchedulePropagation.ToAllInheritors);

								newRecordFolder.save(RMRefreshMode.Refresh);

								//logger.info(" Get Volume Name of electronic record" + recordVolume.getVolumeName());

								folderBean.setRecordFolderId(newRecordFolder.getProperties().getGuidValue("Id"));

								logger.info("Physical Record Folder Id : " + newRecordFolder.getProperties().getGuidValue("Id"));

								volumeId = addPropertiesToVolume(newRecordFolder, volumeBean);

								folderBean.setRecordVolumeId(volumeId);
							}
							else
							{
								logger.error("Folder Title, Folder Reference No, Taxonomy Reference No. Location and Reviewer are mandatory fields for Folder creation. Either 1 of OR all mandatory fields are blank. Folder can not be created");
							}							
						}
						else
						{
							logger.error("Folder Title, Folder Reference No, Taxonomy Reference No., Location and Reviewer are mandatory fields for Folder creation. Either 1 of OR all mandatory fields are null. Folder can not be created");
						}						
					}
					else
					{
						logger.error("Mandatory Taxonomy Node id is blank which is must for Folder creation. Folder can not be created");
					}
				}
				else
				{
					logger.error("Mandatory Taxonomy Node id is null which is must for Folder creation. Folder can not be created");
				}					
			}
			else
			{
				logger.error("Mandatory FolderBean and VolumeBean are missing. Folder can not be created");
			}			

		} catch (RMRuntimeException objException) {

			logger.error("RMRuntimeException occured in createRecordFolder method : " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} catch(Exception objException) {

			logger.error("Exception occured in createRecordFolder method " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} finally {

			recordCategory = null;

			rmProperties = null;

			newRecordFolder = null;			

			fileplanRepository = null;

			ersUtil = null;

			dispScheduleId = null;

			dispositionSchedule = null;	

			if(null!=rmConnection)
				rmConnection.closeRMConnection();

		}
		return folderBean;
	}


	/**This method is used to executing update folder meta data
	 * @param folderBean
	 * @throws Exception
	 */
	public boolean updateRecordFolderMetadata(RecordFolderBean folderBean) throws ERSException {

		try {
			if(null!=folderBean)
			{
				if(null!=folderBean.getFolderType())
				{
					if(folderBean.getFolderType().equalsIgnoreCase("P"))
					{
						return updatePhysicalRecordFolderMetadata(folderBean);
					}
					else if(folderBean.getFolderType().equalsIgnoreCase("E"))
					{
						return updateElectronicRecordFolderMetadata(folderBean);
					}
					else
					{
						logger.info("Invalid Folder Type ["+folderBean.getFolderType()+"]");
					}	
				}
				else
				{
					logger.error("Mandatory RecordFolderBean FolderType is null. Update Record Folder properties can not be updated");
				}
			}
			else
			{
				logger.error("Mandatory RecordFolderBean is null. Update Record Folder properties can not be updated");
			}						
		} catch (RMRuntimeException objException) {

			logger.error("RMRuntimeException occured in updateRecordFolderMetadata method : " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} catch(Exception objException) {

			logger.error("Exception occured in updateRecordFolderMetadata method " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());
		} 
		return true;
	}

	/**This method is used to update electronic folder meta data
	 * @param folderBean
	 * @throws Exception
	 */
	public boolean updateElectronicRecordFolderMetadata(RecordFolderBean folderBean) throws ERSException	{

		RecordFolder recordFolder = null;

		FilePlanRepository fileplanRepository = null;

		RMConnection rmConnection=null;

		RMProperties rmProperties=null;

		try
		{
			logger.info("Enter the updateElectronicRecordFolderMetadata() method");

			if(folderBean != null) {

				logger.info("RecordFolderBean : " + folderBean.toString());

				rmConnection = new RMConnection();

				fileplanRepository = rmConnection.getFilePlanRepositoryByName(ResourceLoader.getMessageProperties().getProperty(ERSConstants.FILEPLAN_OBJECTSTORE));	

				logger.info("FilePlanRepository name : " + fileplanRepository);				

				recordFolder = RMFactory.RecordFolder.fetchInstance(fileplanRepository, folderBean.getRecordFolderId(), null);

				rmProperties = recordFolder.getProperties();

				rmProperties.putStringValue(ERSFolderProperty.RECORD_FOLDER_NAME, folderBean.getFolderTitle());

				rmProperties.putStringValue(ERSFolderProperty.DESCRIPTION, folderBean.getDescription());

				rmProperties.putStringValue(ERSFolderProperty.TAG, folderBean.getTags());

				rmProperties.putStringValue(ERSFolderProperty.FDR_LFCY_ST, folderBean.getFdrLfcyStat());

				rmProperties.putStringValue(ERSFolderProperty.SEC_CLASS, folderBean.getSecClass());

				rmProperties.putStringValue(ERSFolderProperty.OWNER_ROLE, folderBean.getOwnerRole());

				rmProperties.putIntegerValue(ERSFolderProperty.RANDD_PERIOD, folderBean.getRandDPeriod());

				rmProperties.putStringValue(ERSFolderProperty.OWNER_OU, folderBean.getOwnerOU());

				rmProperties.putStringValue(ERSFolderProperty.REVIEWER, folderBean.getReviewer());	

				recordFolder.save(RMRefreshMode.Refresh);

				// Update Volume properties if there is any change in R&D period OR R&D action. Only these properties are allowed to update at volume level
				// All volumes properties will be updated if there is any change in R&D period OR R&D action of the given RecordFolder
				updatePropertiesToVolume(recordFolder,folderBean, null);

			}
			else
			{
				logger.error("Mandatory RecordFolderBean is null. Update Record Folder properties can not be updated");
			}	
			return true;

		} catch (RMRuntimeException objException) {

			logger.error("RMRuntimeException occured in updateElectronicRecordFolderMetadata : " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());
		}
		catch (Exception exceptionObj) 
		{
			logger.error("Exception occured in updateElectronicRecordFolderMetadata method " + exceptionObj.getMessage(), exceptionObj);

			throw new ERSException(exceptionObj.getMessage());
		}
		finally
		{
			recordFolder = null;

			fileplanRepository = null;				

			folderBean = null;		

			if(null!=rmConnection)
				rmConnection.closeRMConnection();
		}			
	}
    
	/**This method is used to update physical folder meta data
	 * @param folderBean
	 * @throws Exception
	 */
	public boolean updatePhysicalRecordFolderMetadata(RecordFolderBean folderBean) throws ERSException	{

		RecordFolder recordFolder = null;

		FilePlanRepository fileplanRepository = null;

		RMConnection rmConnection=null;

		RMProperties rmProperties=null;

		//Location location=null;

		try
		{
			logger.info("Enter the updatePhysicalRecordFolderMetadata() method");

			if(folderBean != null) {

				logger.info("RecordFolderBean : " + folderBean.toString());

				rmConnection = new RMConnection();

				fileplanRepository = rmConnection.getFilePlanRepositoryByName(ResourceLoader.getMessageProperties().getProperty(ERSConstants.FILEPLAN_OBJECTSTORE));	

				logger.info("FilePlanRepository name : " + fileplanRepository);				

				recordFolder = RMFactory.RecordFolder.fetchInstance(fileplanRepository, folderBean.getRecordFolderId(), null);

				rmProperties = recordFolder.getProperties();
				
				/*location=RMFactory.Location.fetchInstance(fileplanRepository, ResourceLoader.getMessageProperties().getProperty(ERSConstants.LOCATION_IDENTIFIER), null);
				if(null!=location)
				{
					rmProperties.putObjectValue(ERSFolderProperty.HOME_LOCATION, location);									
				}
				else
				{
					location=RMFactory.Location.createInstance(fileplanRepository, folderBean.getLocationCode());
					rmProperties.putObjectValue(ERSFolderProperty.HOME_LOCATION, location);
				}*/								

				rmProperties.putStringValue(ERSFolderProperty.RECORD_FOLDER_NAME, folderBean.getFolderTitle());			

				rmProperties.putStringValue(ERSFolderProperty.DESCRIPTION, folderBean.getDescription());

				rmProperties.putStringValue(ERSFolderProperty.TAG, folderBean.getTags());

				rmProperties.putStringValue(ERSFolderProperty.FDR_LFCY_ST, folderBean.getFdrLfcyStat());

				rmProperties.putStringValue(ERSFolderProperty.SEC_CLASS, folderBean.getSecClass());

				rmProperties.putIntegerValue(ERSFolderProperty.RANDD_PERIOD, folderBean.getRandDPeriod());

				rmProperties.putStringValue(ERSFolderProperty.OWNER_ROLE, folderBean.getOwnerRole());

				rmProperties.putStringValue(ERSFolderProperty.OWNER_OU, folderBean.getOwnerOU());		

				rmProperties.putStringValue(ERSFolderProperty.REVIEWER, folderBean.getReviewer());	

				recordFolder.save(RMRefreshMode.Refresh);

				// Update Volume properties if there is any change in R&D period OR R&D action. Only these properties are allowed to update at volume level
				// All volumes properties will be updated if there is any change in R&D period OR R&D action of the given RecordFolder
				updatePropertiesToVolume(recordFolder, folderBean, null);
			}
			else
			{
				logger.error("Mandatory RecordFolderBean is null. Update Record Folder properties can not be updated");
			}	

			return true;

		} catch (RMRuntimeException objException) {

			logger.error("RMRuntimeException occured in updatePhysicalRecordFolderMetadata : " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());
		}
		catch (Exception exceptionObj) 
		{
			logger.error("Exception occured in updatePhysicalRecordFolderMetadata method " + exceptionObj.getMessage(), exceptionObj);

			throw new ERSException(exceptionObj.getMessage());
		}
		finally
		{
			recordFolder = null;

			fileplanRepository = null;				

			folderBean = null;		

			if(null!=rmConnection)
				rmConnection.closeRMConnection();
		}			
	}

	/**
	 * This method is used to view the record folder meta data
	 * @param folderBean
	 * @return folderBean
	 * @throws ERSException
	 */
	public RecordFolderBean viewRecordFolderMetadata(RecordFolderBean folderBean) throws ERSException
	{
		logger.info("folderBean : " + folderBean.toString());

		RMPropertyDescription rmPropertyDescription = null;

		RMConnection rmConnection=null;

		RecordFolder folder = null;

		String propertyName = null;

		Object propertyValue = null;

		FilePlanRepository  fileplanRepository = null;

		Map<String, Object> dataMap = null;

		RMClassDescription rmClassDescription = null;

		List<RMPropertyDescription> listRMPropertyDescription = null;

		Iterator<RMPropertyDescription> listIterator = null;

		try
		{
			logger.info("Enter the viewRecordFolderMetadata() in viewEectronicRecordFolderMetadata class here");

			logger.info("folderBean : " + folderBean.toString());

			if(null!=folderBean && null!=folderBean.getRecordFolderId())
			{
				rmConnection= new RMConnection();

				fileplanRepository = rmConnection.getFilePlanRepositoryByName(ResourceLoader.getMessageProperties().getProperty(ERSConstants.FILEPLAN_OBJECTSTORE));	

				folder = RMFactory.RecordFolder.fetchInstance(fileplanRepository, folderBean.getRecordFolderId(), null);

				rmClassDescription = folder.getClassDescription();

				listRMPropertyDescription = rmClassDescription.getPropertyDescriptions();

				listIterator = listRMPropertyDescription.iterator();

				dataMap=new HashMap<String, Object>();

				while(listIterator.hasNext()) {

					rmPropertyDescription = (RMPropertyDescription) listIterator.next();

					if(!rmPropertyDescription.isSystemGenerated() && !rmPropertyDescription.isHidden() && !rmPropertyDescription.isRMSystemProperty() && !rmPropertyDescription.getDisplayName().equalsIgnoreCase("Pblication Source") && !rmPropertyDescription.isValueRequired()) {

						propertyName = rmPropertyDescription.getSymbolicName();

						propertyValue = folder.getProperties().get(propertyName).getObjectValue();

						logger.info("propertyName : " + propertyName + ":" + propertyValue);

						dataMap.put(propertyName, propertyValue);

					}
				}

				if(rmClassDescription.getName().equalsIgnoreCase(ERSFolderProperty.ERS_ELECTRONIC_RECORD_FOLDER))
				{
					folderBean.setFolderTitle((String)dataMap.get(ERSFolderProperty.RECORD_FOLDER_NAME));

					folderBean.setDescription((String)dataMap.get(ERSFolderProperty.DESCRIPTION));

					folderBean.setTags((String)dataMap.get(ERSFolderProperty.TAG));

					folderBean.setCreator((String)dataMap.get(ERSFolderProperty.CREATOR));

					folderBean.setFdrRefNo((String)dataMap.get(ERSFolderProperty.FDR_REF_NO));

					folderBean.setFdrLfcyStat((String)dataMap.get(ERSFolderProperty.FDR_LFCY_ST));

					folderBean.setDateCreated((Date)dataMap.get(ERSFolderProperty.DATE_CREATED));

					folderBean.setFirstEnclDate((Date)dataMap.get(ERSFolderProperty.FIRST_ENCL_DATE));

					folderBean.setLastEnclDate((Date)dataMap.get(ERSFolderProperty.LAST_ENCL_DATE));

					folderBean.setSecClass((String)dataMap.get(ERSFolderProperty.SEC_CLASS));

					folderBean.setFolderOwner((String)dataMap.get(ERSFolderProperty.OWNER_ROLE));

					folderBean.setRandDPeriod(Integer.parseInt(dataMap.get(ERSFolderProperty.RANDD_PERIOD).toString()));

					folderBean.setRandDAction((String)dataMap.get(ERSFolderProperty.RANDD_ACTION));
				}
				else if(rmClassDescription.getName().equalsIgnoreCase(ERSFolderProperty.ERS_PHYSICAL_RECORD_FOLDER))
				{
					folderBean.setFolderTitle((String)dataMap.get(ERSFolderProperty.RECORD_FOLDER_NAME));

					folderBean.setDescription((String)dataMap.get(ERSFolderProperty.DESCRIPTION));

					folderBean.setTags((String)dataMap.get(ERSFolderProperty.TAG));

					folderBean.setCreator((String)dataMap.get(ERSFolderProperty.CREATOR));

					folderBean.setFdrRefNo((String)dataMap.get(ERSFolderProperty.FDR_REF_NO));

					folderBean.setFdrLfcyStat((String)dataMap.get(ERSFolderProperty.FDR_LFCY_ST));

					folderBean.setDateCreated((Date)dataMap.get(ERSFolderProperty.DATE_CREATED));

					folderBean.setFirstEnclDate((Date)dataMap.get(ERSFolderProperty.FIRST_ENCL_DATE));

					folderBean.setLastEnclDate((Date)dataMap.get(ERSFolderProperty.LAST_ENCL_DATE));

					folderBean.setSecClass((String)dataMap.get(ERSFolderProperty.SEC_CLASS));

					folderBean.setFolderOwner((String)dataMap.get(ERSFolderProperty.OWNER_ROLE));

					folderBean.setRandDPeriod(Integer.parseInt(dataMap.get(ERSFolderProperty.RANDD_PERIOD).toString()));

					folderBean.setRandDAction((String)dataMap.get(ERSFolderProperty.RANDD_ACTION));

					// Some more Physical Folder properties can be added here

					logger.info("folderBean : " + folderBean.toString());
				}
				else
				{
					logger.error("Invalid Folder Class Type. Properties can not be retrived.");
				}
			}
			else
			{
				logger.error("Either mandatory FolderBean OR Record Folder Id is null. Folder properties could not be extracted.");
			}			

		} catch (RMRuntimeException objException) {

			logger.error("RMRuntimeException occured in viewRecordFolderMetadata method : " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} catch (Exception exceptionObj) {

			logger.error("Exception occured in viewRecordFolderMetadata method"+ exceptionObj.getMessage(), exceptionObj);

			throw new ERSException(exceptionObj.getMessage());
		}

		finally
		{
			if(null!=rmPropertyDescription)
				rmPropertyDescription = null;

			if(null!=folder)
				folder = null;

			if(null!=propertyName)
				propertyName = null;

			if(null!=propertyValue)
				propertyValue = null;

			if(null!=fileplanRepository)
				fileplanRepository = null;

			if(null!=rmClassDescription)
				rmClassDescription = null;

			if(null!=dataMap)
			{
				dataMap.clear();
				dataMap = null;
			}
			if(null!=listRMPropertyDescription)
			{
				listRMPropertyDescription.clear();
				listRMPropertyDescription = null;
			}
			if(null!=listIterator)
				listIterator = null;

			if(null!=rmConnection)
				rmConnection.closeRMConnection();
		}
		return folderBean;
	}

	/**
	 * This method is used to move the record folder from one record category to another record category
	 * @param folderBean
	 * @throws ERSException
	 */
	public void moveRecordFolder(RecordFolderBean folderBean) throws ERSException {		

		logger.info("Enter the MoveRecordFolder method");
		
		RecordFolderContainer recordContainer = null;

		RecordFolder recordFolder = null;

		FilePlanRepository fileplanRepository = null;

		String recordFolderId = null;

		String recordCategoryId = null;

		String reasonToMove = null;

		RMConnection rmConnection = null;

		try {

			if(null!=folderBean && null!=folderBean.getRecordFolderId())
			{
				logger.info("folderBean : " + folderBean.toString());

				rmConnection= new RMConnection();

				fileplanRepository = rmConnection.getFilePlanRepositoryByName(ResourceLoader.getMessageProperties().getProperty(ERSConstants.FILEPLAN_OBJECTSTORE));	

				recordFolderId = folderBean.getRecordFolderId();

				recordCategoryId = folderBean.getDestRecordCategoryId();

				recordFolder = RMFactory.RecordFolder.fetchInstance(fileplanRepository, recordFolderId, null);

				recordContainer = RMFactory.RecordFolder.fetchInstance(fileplanRepository, recordCategoryId, null);

				reasonToMove = folderBean.getReasonToMove();

				recordFolder.move(recordContainer, reasonToMove);

				recordFolder.save(RMRefreshMode.Refresh);

				logger.info("End of  the MoveRecordFolder class here");

			} else {

				logger.error("Either mandatory FolderBean OR Record Folder Id is null. Folder movement could not happened.");
			}

		} catch (RMRuntimeException objException) {

			logger.error("RMRuntimeException occured in moveRecordFolder method " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} catch (Exception objException) {

			logger.error("Exception occurd in moveRecordFolder method " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} finally {

			recordContainer = null;

			recordFolderId = null;

			recordCategoryId = null;

			reasonToMove = null;

			folderBean = null;

			recordFolder = null;

			fileplanRepository = null;				

			folderBean = null;		

			if(null!=rmConnection)
				rmConnection.closeRMConnection();
		}
	}

	/**
	 * This method is used to close the record folder
	 * @param recordFolderBean
	 * @throws ERSException
	 */
	public void closeRecordFolder(RecordFolderBean recordFolderBean) throws ERSException {

		logger.info("Entered into manualCloseRecordFolder method");		

		RecordContainer recordClose = null;

		FilePlanRepository  fileplanRepository = null;

		String recordFolderId = null;

		String reasonToClose = null;

		RMConnection rmConnection = null;

		try {

			if(null!=recordFolderBean && null!=recordFolderBean.getRecordFolderId())
			{
				logger.info("folderBean : " + recordFolderBean.toString());

				rmConnection = new RMConnection();

				fileplanRepository = rmConnection.getFilePlanRepositoryByName(ResourceLoader.getMessageProperties().getProperty(ERSConstants.FILEPLAN_OBJECTSTORE));	

				logger.info("folderBean : " + recordFolderBean.toString());

				recordFolderId = recordFolderBean.getRecordFolderId();

				recordClose = RMFactory.RecordFolder.fetchInstance(fileplanRepository, recordFolderId, null);

				reasonToClose = recordFolderBean.getReasonForClose();

				recordClose.close(reasonToClose);

				logger.info("End manually closed folder");

			} else {

				logger.error("Either mandatory RecordFolderBean OR Record Folder Id is null. Folder close will not happened.");
			}
		} catch (RMRuntimeException objException) {

			logger.error("RMRuntimeException occured in closeRecordFolder method : " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} catch (Exception objException) {

			logger.error("Exception occured in closeRecordFolder method : " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} finally {

			recordClose = null;

			recordFolderId = null;

			fileplanRepository = null;

			recordFolderBean = null;

			if(null!=rmConnection)
				rmConnection.closeRMConnection();
		}
	}

	/**
	 * This method is used to modify the record folder status.
	 * @param folderBean
	 * @throws ERSException
	 */
	public void freezeRecordFolder(RecordFolderBean folderBean) throws ERSException {

		logger.info("Entered into freezeRecordFolder method");		

		RecordContainer recordContainer = null;

		FilePlanRepository  fileplanRepository = null;

		String recordFolderId = null;

		RMConnection rmConnection = null;

		try {

			if(null!=folderBean && null!=folderBean.getRecordFolderId())
			{
				logger.info("folderBean : " + folderBean.toString());

				rmConnection = new RMConnection();

				fileplanRepository = rmConnection.getFilePlanRepositoryByName(ResourceLoader.getMessageProperties().getProperty(ERSConstants.FILEPLAN_OBJECTSTORE));	

				recordFolderId = folderBean.getRecordFolderId();

				recordContainer = RMFactory.RecordFolder.fetchInstance(fileplanRepository, recordFolderId, null);

				recordContainer.setInactive("SetInactive");

			} else {

				logger.error("Either mandatory RecordFolderBean OR Record Folder Id is null. Freeze Folder will not happened.");
			}

		} catch (RMRuntimeException objException) {

			logger.error("RMRuntimeException occured in freezeRecordFolder method : " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} catch(Exception objException) {

			logger.error("Exception occured in freezeRecordFolder method : " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} finally {

			recordContainer = null;

			fileplanRepository = null;

			recordFolderId = null;

			folderBean = null;

			if(null!=rmConnection)
				rmConnection.closeRMConnection();
		}
	}

	/**
	 * This method is used to modify the record folder status.
	 * @param folderBean
	 * @throws ERSException
	 */
	public void unFreezeRecordFolder(RecordFolderBean folderBean) throws ERSException {

		logger.info("Entered into unFreezeRecordFolder method");		

		RecordContainer recordContainer = null;

		FilePlanRepository  fileplanRepository = null;

		String recordFolderId = null;

		RMConnection rmConnection = null;

		try {

			if(null!=folderBean && null!=folderBean.getRecordFolderId())
			{
				logger.info("folderBean : " + folderBean.toString());

				rmConnection = new RMConnection();

				fileplanRepository = rmConnection.getFilePlanRepositoryByName(ResourceLoader.getMessageProperties().getProperty(ERSConstants.FILEPLAN_OBJECTSTORE));	

				recordFolderId = folderBean.getRecordFolderId();

				recordContainer = RMFactory.RecordFolder.fetchInstance(fileplanRepository, recordFolderId, null);

				recordContainer.setActive();

			} else {

				logger.error("Either mandatory RecordFolderBean OR Record Folder Id is null. UnFreeze Folder will not happened.");
			}

		} catch (RMRuntimeException objException) {

			logger.error("RMRuntimeException occured in unFreezeRecordFolder method : " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} catch(Exception objException) {

			logger.error("Exception occured in unFreezeRecordFolder method : " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} finally {

			recordContainer = null;

			fileplanRepository = null;

			recordFolderId = null;

			folderBean = null;

			if(null!=rmConnection)
				rmConnection.closeRMConnection();
		}
	}

	/**
	 * This method is used to delete the record folder
	 * @param folderBean
	 * @throws ERSException
	 */
	public void deleteRecordFolder(RecordFolderBean folderBean) throws ERSException {		

		logger.info("Entered into deleteRecordFolder method");
		
		RecordContainer recordContainer = null;

		FilePlanRepository  fileplanRepository = null;

		String recordFolderId = null;

		RMConnection rmConnection = null;

		try {

			if(null!=folderBean && null!=folderBean.getRecordFolderId())
			{
				logger.info("folderBean : " + folderBean.toString());

				rmConnection = new RMConnection();

				fileplanRepository = rmConnection.getFilePlanRepositoryByName(ResourceLoader.getMessageProperties().getProperty(ERSConstants.FILEPLAN_OBJECTSTORE));	

				recordFolderId = folderBean.getRecordFolderId();

				recordContainer = RMFactory.RecordFolder.fetchInstance(fileplanRepository, recordFolderId, null);

				recordContainer.delete(true, DeleteMode.CheckRetainMetadata, null);

				logger.info("Record Folder Deleted successfully");

			} else {

				logger.error("Either mandatory RecordFolderBean OR Record Folder Id is null. delete Folder will not happened.");
			}

		} catch (RMRuntimeException objException) {

			logger.error("RMRuntimeException occured in deleteRecordFolder method : " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} catch(Exception objException) {

			logger.error("Exception occured in deleteRecordFolder method : " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		}  finally {

			recordContainer = null;

			fileplanRepository = null;

			recordFolderId = null;

			if(null!=folderBean)
				folderBean = null;

			if(null!=rmConnection)
				rmConnection.closeRMConnection();
		}
	}

	/**
	 * This method is used to create the record volume once record count reaches 300 in volume
	 * @param folderBean
	 * @throws ERSException
	 */
	public RecordFolderBean createRecordVolume(RecordFolderBean folderBean, VolumeBean volumeBean) throws ERSException {		

		logger.info("Entered into createRecordVolume method");
		
		String recordFolderId = null;

		String volumeRefNumber = null;

		FilePlanRepository fileplanRepository = null;

		RecordVolume recordVolume = null;

		RMConnection rmConnection = null;

		RMProperties rmProperties = null;

		try {

			if(null!=folderBean && null!=folderBean.getRecordFolderId())
			{
				if(volumeBean != null) {

					logger.info("folderBean : " + folderBean.toString());

					logger.info("volumeBean : " + volumeBean.toString());

					rmConnection = new RMConnection();

					fileplanRepository = rmConnection.getFilePlanRepositoryByName(ResourceLoader.getMessageProperties().getProperty(ERSConstants.FILEPLAN_OBJECTSTORE));	

					recordFolderId = folderBean.getRecordFolderId();

					volumeRefNumber = folderBean.getVolNo();

					RecordFolder recordFolder = RMFactory.RecordFolder.fetchInstance(fileplanRepository, recordFolderId, null);

					rmProperties = RMFactory.RMProperties.createInstance(DomainType.P8_CE);

					rmProperties.putStringValue(ERSVolumeProperty.FDR_REF_NO, volumeBean.getFdrRefNo());

					//rmProperties.putStringValue(ERSVolumeProperty.VOLUME_NAME, volumeBean.getVolNo());

					rmProperties.putStringValue(ERSVolumeProperty.VOL_LFCY_ST, volumeBean.getVolLfcyStat());

					rmProperties.putDateTimeValue(ERSVolumeProperty.FIRST_ENCL_DATE, volumeBean.getFirstEnclDate());

					// When first volume is created, by default first enclosure creation date will be same as last enclosure date
					rmProperties.putDateTimeValue(ERSVolumeProperty.LAST_ENCL_DATE, volumeBean.getLastEnclDate());

					rmProperties.putIntegerValue(ERSVolumeProperty.RANDD_PERIOD, volumeBean.getRndPeriod());

					rmProperties.putStringValue(ERSVolumeProperty.RANDD_ACTION, volumeBean.getRndAction());

					recordVolume = recordFolder.addRecordVolume(ERSFolderProperty.ERS_VOLUME, volumeRefNumber , rmProperties, null);

					String volumeId = recordVolume.getProperties().getGuidValue("Id");

					folderBean.setRecordVolumeId(volumeId);

					logger.info("volumeId : " + volumeId);

					recordVolume.save(RMRefreshMode.Refresh);

					addPropertiesToVolume(recordFolder, volumeBean);

				} else {

					logger.error("Record Volume Bean Id is null. create volume will not happened.");
				}

			} else {

				logger.error("Either mandatory RecordFolderBean OR Record Folder Id is null. create volume will not happened.");
			}

		} catch (RMRuntimeException objException) {

			logger.error("RMRuntimeException occured in createRecordVolume method : " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} catch(Exception objException) {

			logger.error("Exception occured in createRecordVolume method : " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		}  finally {

			recordFolderId = null;

			volumeRefNumber = null;

			fileplanRepository = null;

			recordVolume = null;

			if(null!=rmConnection)
				rmConnection.closeRMConnection();
		}

		return folderBean;
	}

	/**
	 * This method is used to add properties to volume while creating the record folder
	 * @param recordFolder
	 * @param volumeBean
	 * @return volumeId
	 * @throws ERSException
	 */
	public String addPropertiesToVolume(RecordFolder recordFolder, VolumeBean volumeBean) throws ERSException {

		logger.info("Entered into addPropertiesToVolume method");		

		RecordVolume recordVolume = null;

		RMProperties rmProperties = null;

		String volumeId = null;

		try {

			if(null!=recordFolder && null!=volumeBean)
			{

				logger.info("volumeBean : " + volumeBean.toString());

				recordVolume = recordFolder.getActiveRecordVolume();

				rmProperties = recordVolume.getProperties();

				rmProperties.putStringValue(ERSVolumeProperty.FDR_REF_NO, volumeBean.getFdrRefNo());

				rmProperties.putStringValue(ERSVolumeProperty.VOLUME_NAME, volumeBean.getVolNo());

				rmProperties.putStringValue(ERSVolumeProperty.VOL_LFCY_ST, volumeBean.getVolLfcyStat());

				rmProperties.putDateTimeValue(ERSVolumeProperty.FIRST_ENCL_DATE, volumeBean.getFirstEnclDate());

				// When first volume is created, by default first enclosure creation date will be same as last enclosure date
				rmProperties.putDateTimeValue(ERSVolumeProperty.LAST_ENCL_DATE, volumeBean.getLastEnclDate());

				//rmProperties.putDoubleValue(ERSVolumeProperty.RECORD_COUNT, Double.parseDouble(volumeBean.getRecordCount()+""));

				rmProperties.putIntegerValue(ERSVolumeProperty.RANDD_PERIOD, volumeBean.getRndPeriod());

				rmProperties.putStringValue(ERSVolumeProperty.RANDD_ACTION, volumeBean.getRndAction());

				// Approval properties not required at the time of volume creation, it will be updated when Archival and Disposal process initiated

				//rmProperties.putDateTimeValue(ERSVolumeProperty.APPROVED_DATE, volumeBean.getApprovedDate());

				//rmProperties.putStringValue(ERSVolumeProperty.APPROVED_BY, volumeBean.getApprovedBy());

				//rmProperties.putStringValue(ERSVolumeProperty.APPROVER_DESIGNATION, volumeBean.getApproverDesignation());

				recordVolume.save(RMRefreshMode.Refresh);

				volumeId = recordVolume.getProperties().getGuidValue("Id");

			} else {

				logger.error("Either mandatory Record Folder OR Volume Bean is null. create volume will not happened.");
			}

		} catch (RMRuntimeException objException) {

			logger.error("RMRuntimeException occurred in addPropertiesToVolume method : " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} catch(Exception objException) {

			logger.error("Exception occurred in addPropertiesToVolume method : " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());
		}  	
		finally
		{
			if(null!=recordVolume)
			{
				recordVolume=null;
			}
			if(null!=rmProperties)
			{
				rmProperties=null;
			}
		}
		return volumeId;
	}

	/**
	 * This method is used to update the volume properties base on the folder properties and record properties
	 * @param recordFolder
	 * @param folderBean
	 * @param recordBean
	 * @throws ERSException
	 */
	public void updatePropertiesToVolume(RecordFolder recordFolder, RecordFolderBean folderBean, RecordBean recordBean) throws ERSException {

		logger.info("Entered into updatePropertiesToVolume method");		

		RMProperties rmProperties = null;

		PageableSet<RecordVolume> recordVolumeList = null;

		RecordVolume activeVolume = null;

		try {

			if(null!=recordFolder && null!=folderBean)
			{
				if(null!=folderBean.getRandDAction() && (folderBean.getRandDPeriod()>0))
				{
					logger.info("RecordFolder : " + recordFolder.toString());

					recordVolumeList = recordFolder.fetchRecordVolumes(RMPropertyFilter.MinimumPropertySet, 0);

					for(RecordVolume recordVolume : recordVolumeList)
					{
						rmProperties = recordVolume.getProperties();				

						rmProperties.putIntegerValue(ERSVolumeProperty.RANDD_PERIOD, folderBean.getRandDPeriod());

						rmProperties.putStringValue(ERSVolumeProperty.RANDD_ACTION, folderBean.getRandDAction());

						recordVolume.save(RMRefreshMode.Refresh);

						rmProperties = null;
					}
				}
				else
				{
					logger.error("Invalid R&D period OR action. Both the mandatory properties for updating volume properties. Volume update properties action can not be performed.");
				}
			}
			else if(null!=recordFolder && null!= recordBean)
			{
				recordFolder.getProperties().putDateTimeValue(ERSFolderProperty.LAST_ENCL_DATE, new Date());
				
				if((recordBean.getRecordCount()>=0))
				{
					activeVolume = recordFolder.getActiveRecordVolume();

					rmProperties = activeVolume.getProperties();

					rmProperties.putDoubleValue(ERSVolumeProperty.RECORD_COUNT, Double.parseDouble(recordBean.getRecordCount()+""));

					rmProperties.putDateTimeValue(ERSVolumeProperty.LAST_ENCL_DATE, new Date());

					activeVolume.save(RMRefreshMode.Refresh);

					rmProperties=null;
				}
				else
				{
					logger.error("Invalid Record count. Both the mandatory properties for updating volume properties. Volume update properties action can not be performed.");
				}
			}
			else
			{
				logger.error("Either mandatory RecordFolder OR RecordBean OR FolderBean objects are null. Volume property update can not be performed.");
			}			
		} catch (RMRuntimeException objException) {

			logger.error("RMRuntimeException occurred in addPropertiesToVolume method : " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} catch(Exception objException) {

			logger.error("Exception occurred in addPropertiesToVolume method : " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());
		}  	
		finally
		{			
			if(null!=rmProperties)
			{
				rmProperties=null;
			}
			if(null!=recordVolumeList)
			{
				recordVolumeList=null;
			}
		}
	}

	public static void main(String[] args) throws Exception {

		RecordFolderOperationsImpl folderOperationsImpl = new RecordFolderOperationsImpl();

		RecordFolderBean folderBean = null;

		VolumeBean volumeBean = null;

		folderOperationsImpl.createRecordFolder(folderBean, volumeBean);
	}
}