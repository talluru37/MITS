package com.ers.jarm.api.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.ers.jarm.api.bean.DocumentBean;
import com.ers.jarm.api.bean.LocationBean;
import com.ers.jarm.api.bean.RecordBean;
import com.ers.jarm.api.connection.CEConnection;
import com.ers.jarm.api.connection.RMConnection;
import com.ers.jarm.api.constants.CEClassesAndProperties;
import com.ers.jarm.api.constants.ERSConstants;
import com.ers.jarm.api.constants.ERSRecordProperty;
import com.ers.jarm.api.exception.ERSException;
import com.ers.jarm.api.folder.impl.RecordFolderOperationsImpl;
import com.ers.jarm.api.resources.ResourceLoader;
import com.filenet.api.collection.ContentElementList;
import com.filenet.api.collection.IndependentObjectSet;
import com.filenet.api.constants.AutoClassify;
import com.filenet.api.constants.AutoUniqueName;
import com.filenet.api.constants.CheckinType;
import com.filenet.api.constants.RefreshMode;
import com.filenet.api.core.Document;
import com.filenet.api.core.Factory;
import com.filenet.api.core.Folder;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.core.ReferentialContainmentRelationship;
import com.filenet.api.property.Properties;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;
import com.filenet.rm.bds.ContainerReference;
import com.filenet.rm.bds.ContentElement;
import com.filenet.rm.bds.ContentTransfer;
import com.filenet.rm.bds.DataType;
import com.filenet.rm.bds.DocumentDefinition;
import com.filenet.rm.bds.PropertyValue;
import com.filenet.rm.bds.RecordDefinition;
import com.filenet.rm.bds.impl.BulkDeclarationFactory;
import com.ibm.jarm.api.collection.PageableSet;
import com.ibm.jarm.api.constants.RMRefreshMode;
import com.ibm.jarm.api.core.DispositionAction;
import com.ibm.jarm.api.core.DispositionPhase;
import com.ibm.jarm.api.core.DispositionPhaseList;
import com.ibm.jarm.api.core.DispositionSchedule;
import com.ibm.jarm.api.core.DispositionTrigger;
import com.ibm.jarm.api.core.FilePlanRepository;
import com.ibm.jarm.api.core.Location;
import com.ibm.jarm.api.core.RMFactory;
import com.ibm.jarm.api.core.RecordFolder;
import com.ibm.jarm.api.core.RecordVolume;
import com.ibm.jarm.api.exception.RMRuntimeException;
import com.ibm.jarm.api.property.RMProperties;
import com.ibm.jarm.api.query.RMSearch;
import com.ibm.jarm.api.query.ResultRow;

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
 * This class is used for ERSUtil to 
 * create document definition, record definition and disposition
 * 
 * @class name ERSUtil.java
 * @date  sep,2014
 * @author MITS
 * @version 1.0
 */
public class ERSUtil {

	static Logger logger = Logger.getLogger(ERSUtil.class);

	/**
	 * @constructor for ERSUtil class  
	 * @throws ERSException
	 */
	public ERSUtil() throws ERSException
	{
		try
		{
			ResourceLoader.loadProperties();

		}
		catch (Exception objException) 
		{

			logger.error("Exception occurred in ERSUtil constructor [Properties are not loaded] : " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());
		}
	}	

	/**
	 * This method is used to get all disposition schedules 
	 * @param randPeriod
	 * @return scheduleMap
	 * @throws ERSException
	 */
	public Map<Integer, String> getDispositionSchedule(int randPeriod) throws ERSException {

		logger.info("Entered into getDispositionSchedule method : randPeriod : " + randPeriod);

		FilePlanRepository filePlanRepository = null;

		List<DispositionSchedule> dispositionSchedulesList = null;

		DispositionSchedule dispositionSchedule = null;

		Map<Integer, String> scheduleMap = null;

		String dispositionName = null;

		String dispositionId = null;

		RMConnection rmConnectionCode = null;

		int totalRndPeriod = 0;

		try	{

			scheduleMap = new HashMap<Integer, String>();

			rmConnectionCode = new RMConnection();

			filePlanRepository = rmConnectionCode.getFilePlanRepositoryByName(ResourceLoader.getMessageProperties().getProperty(ERSConstants.FILEPLAN_OBJECTSTORE));

			dispositionSchedulesList = filePlanRepository.getDispositionSchedules(null);

			Iterator<DispositionSchedule> dispIterator = dispositionSchedulesList.iterator();

			while(dispIterator.hasNext()) {

				dispositionSchedule = dispIterator.next();

				dispositionName = dispositionSchedule.getName();

				//logger.info(dispositionSchedule.getProperties().getObjectValue("Phases"));

				ArrayList<DispositionPhase> phasesList = (ArrayList<DispositionPhase>) dispositionSchedule.getProperties().getObjectValue("Phases");

				Iterator<DispositionPhase> iterPhaseList = phasesList.iterator();

				while(iterPhaseList.hasNext()) {

					DispositionPhase phase = iterPhaseList.next();

					totalRndPeriod = (phase.getProperties().getIntegerValue("RetentionPeriodYears")*12) + phase.getProperties().getIntegerValue("RetentionPeriodMonths");

					/*if(randPeriod != totalRndPeriod) {

						createDispositionSchedule(randPeriod);

					}*/

					dispositionId = dispositionSchedule.getProperties().getGuidValue("Id");

					scheduleMap.put(totalRndPeriod, dispositionId);
				}

			}

		} catch(RMRuntimeException objException) {

			logger.error("RMRuntimeException occured in getDispositionSchedule method " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} catch(Exception objException) {

			logger.error("Exception occured in getDispositionSchedule method " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} finally {

			filePlanRepository = null;

			dispositionSchedulesList = null;

			dispositionSchedule = null;

			dispositionName = null;

			dispositionId = null;
		}

		return scheduleMap;
	}

	/**
	 * This method is used to create disposition schedule
	 * @param rndPeriod
	 * @throws ERSException
	 */
	public String createDispositionSchedule(int rndPeriod) throws ERSException {

		logger.info("Entered into createDispositionSchedule method randPeriod : " + rndPeriod);

		FilePlanRepository filePlanRepository = null;

		RMConnection rmConnectionCode = null;

		DispositionSchedule dispositionSchedule = null;

		DispositionAction dispositionAction = null;

		DispositionTrigger dispositionTrigger = null;

		String dispScheduleId = null;

		int rndPeriodinMonths = 0;

		int rndPeriodinYears = 0;

		try {

			rmConnectionCode = new RMConnection();

			filePlanRepository = rmConnectionCode.getFilePlanRepositoryByName(ResourceLoader.getMessageProperties().getProperty(ERSConstants.FILEPLAN_OBJECTSTORE));

			String dispositionActionId = ResourceLoader.getMessageProperties().getProperty(ERSConstants.DISPOSITION_ACTION_ID);

			String dispositionTriggerId = ResourceLoader.getMessageProperties().getProperty(ERSConstants.DISPOSITION_TRIGGER_ID);

			dispositionAction = RMFactory.DispositionAction.fetchInstance(filePlanRepository, dispositionActionId, null);

			//logger.info("dispositionAction : " + dispositionAction.getActionName());

			dispositionTrigger = RMFactory.DispositionTrigger.fetchInstance(filePlanRepository, dispositionTriggerId, null);

			//logger.info("dispositionTrigger : " + dispositionTrigger.getTriggerName());		

			dispositionSchedule = RMFactory.DispositionSchedule.createInstance(filePlanRepository);

			String scheduleName = ResourceLoader.getMessageProperties().getProperty(ERSConstants.DISPOSITION_SCHEDULE);

			String phaseName = ResourceLoader.getMessageProperties().getProperty(ERSConstants.PHASE_NAME);

			dispositionSchedule.setScheduleName(scheduleName + rndPeriod);

			dispositionSchedule.setDispositionTigger(dispositionTrigger);

			dispositionSchedule.setScreeningRequired(false);			

			DispositionPhase dispositionPhase = dispositionSchedule.createNewPhase(phaseName);

			if(rndPeriod != 0) {

				rndPeriodinMonths = rndPeriod % 12;

				rndPeriodinYears = rndPeriod /12;

				dispositionPhase.setRetentionPeriod(rndPeriodinYears, rndPeriodinMonths, 0);
				
				dispositionPhase.setPhaseAction(dispositionAction);
			}

			dispositionPhase.setScreeningRequired(false);

			DispositionPhaseList dispositionPhaseList = dispositionSchedule.getDispositionPhases();

			dispositionPhaseList.add(dispositionPhase);

			dispositionSchedule.save(RMRefreshMode.Refresh);

			dispScheduleId = dispositionSchedule.getProperties().getGuidValue("Id");

		} catch(RMRuntimeException objException) {

			logger.error("RMRuntimeException occured in createDispositionSchedule method " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} catch(Exception objException) {

			logger.error("Exception occured in createDispositionSchedule method " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} finally {

			if(filePlanRepository != null)
				filePlanRepository = null;

			dispositionSchedule = null;

			dispositionAction = null;

			dispositionTrigger = null;
		}

		return dispScheduleId;
	}

	/**
	 * This method is used to create the document definition
	 * @param newDocumentClass
	 * @param documentBean
	 * @return documentDefinitions
	 * @throws IOException
	 * @throws ERSException
	 */
	public DocumentDefinition createDocumentDefinition(String newDocumentClass, DocumentBean documentBean, RecordBean recordBean) throws ERSException, IOException {

		logger.info("Entered into createDocumentDefinition method");

		logger.info("newDocumentClass : " + newDocumentClass + "documentBean : " + documentBean.toString() + "recordBean : " + recordBean.toString());

		DocumentDefinition documentDefinition = null;

		try {

			newDocumentClass = fetchRecordTypes(newDocumentClass);	

			logger.info("After getting CE class : " + newDocumentClass);

			String recordsObjectStore = ResourceLoader.getMessageProperties().getProperty(ERSConstants.RECORDS_OBJECTSTORE);

			documentDefinition = BulkDeclarationFactory.newDocumentDefinition(recordsObjectStore, newDocumentClass);

			setDocumentDefinitionProperties(documentDefinition, documentBean, recordBean);

		} catch(RMRuntimeException objException) {

			logger.error("RMRuntimeException occured in createDocumentDefinition method " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} catch (Exception objException) {

			logger.error("Exception occured in createDocumentDefinition method" + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} finally {


		}
		return documentDefinition;
	}

	/**
	 * This method is used to create the record definition
	 * @param recordClassName
	 * @param recordBean
	 * @return recordDefinition
	 * @throws ERSException
	 */
	public RecordDefinition createRecordDefinition(String recordClassName, RecordBean recordBean) throws ERSException {

		logger.info("Entered into createRecordDefinition method : " + recordClassName);

		logger.info("recordBean : " + recordBean.toString());

		RecordDefinition recordDefinition = null;

		String objectStoreName = null;

		RMConnection rmConnection = null;

		FilePlanRepository filePlanRepository = null;

		String filePath = null;

		RecordFolderOperationsImpl folderOperationsImpl = null;

		try {

			objectStoreName = ResourceLoader.getMessageProperties().getProperty(ERSConstants.FILEPLAN_OBJECTSTORE);

			rmConnection = new RMConnection();

			folderOperationsImpl = new RecordFolderOperationsImpl();

			filePlanRepository = rmConnection.getFilePlanRepositoryByName(ResourceLoader.getMessageProperties().getProperty(ERSConstants.FILEPLAN_OBJECTSTORE));

			RecordFolder recordFolder = RMFactory.RecordFolder.fetchInstance(filePlanRepository, recordBean.getRecordFolderId(), null);

			filePath = recordFolder.getProperties().getStringValue("PathName");

			recordDefinition = BulkDeclarationFactory.newRecordDefinition(objectStoreName, recordClassName);

			setRecordDefinitionProperties(recordDefinition, recordBean);

			setRecordContainer(objectStoreName, recordDefinition, recordClassName, filePath);

			recordBean.setRecordCount(recordBean.getRecordCount() + 1);

			folderOperationsImpl.updatePropertiesToVolume(recordFolder, null, recordBean);

		} catch(RMRuntimeException objException) {

			logger.error("RMRuntimeException occured in createRecordDefinition method " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} catch (Exception objException) {

			logger.error("Exception occured in createRecordDefinition method " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} finally {

			objectStoreName = null;

			if(filePlanRepository != null) 
				filePlanRepository = null;

			filePath = null;

			if(folderOperationsImpl != null) 
				folderOperationsImpl = null;
		}

		return recordDefinition;

	}

	/**
	 * This method is used to file the record into the folder
	 * @param objectStoreName
	 * @param recordDefinition
	 * @param filePlanPath
	 * @throws ERSException
	 */
	public void setRecordContainer(String objectStoreName, RecordDefinition recordDefinition, String recordClassName, String filePlanPath) throws ERSException {

		logger.info("Entered into setRecordContainer method");

		logger.info("objectStoreName : " + objectStoreName + "recordDefinition : " + recordDefinition + "recordClassName : " + recordClassName + "filePlanPath : " + filePlanPath);

		List<ContainerReference> containers = null;

		try {

			containers = recordDefinition.getContainers();

			ContainerReference containerRef = BulkDeclarationFactory.newContainerReference(objectStoreName, recordClassName, filePlanPath);

			containers.add(containerRef);

		} catch(RMRuntimeException objException) {

			logger.error("RMRuntimeException occured in setRecordContainer method " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} catch (Exception objException) {

			logger.error("Exception occured in setRecordContainer method " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());
		}
	}

	/**
	 * This method is used to set the properties to the document definition
	 * @param documentDefinition
	 * @param documentBean
	 * @throws ERSException
	 * @throws IOException
	 */
	public void setDocumentDefinitionProperties(DocumentDefinition documentDefinition, DocumentBean documentBean, RecordBean recordBean) throws ERSException, IOException {

		logger.info("Entered into setDocumentDefinitionProperties method");

		logger.info("documentDefinition : " + documentDefinition + "documentBean : " + documentBean.toString() + "recordBean : " + recordBean.toString());

		List<PropertyValue> documentPropertyValues = null;

		PropertyValue documentPropertyValue =  null;

		List<ContentElement> contentElements = null;

		String documentName = documentBean.getFileName();

		String mimeType = documentBean.getMimeType();

		String fdrRefNo = documentBean.getFdrRefNo();

		int volNo = documentBean.getVolNo();

		int recRefNo = documentBean.getRecRefNo();

		try {

			documentPropertyValues = documentDefinition.getPropertyValues();

			if(recordBean.getFnRecordType().equalsIgnoreCase(ERSConstants.RECORD_TYPE_ELECTRONIC)) {

				documentPropertyValue = BulkDeclarationFactory.newPropertyValue(CEClassesAndProperties.DOCUMENT_TITLE, DataType.TYPE_STRING, false);

				documentPropertyValue.setValue(documentName);

				documentPropertyValues.add(documentPropertyValue);

				documentPropertyValue = BulkDeclarationFactory.newPropertyValue(CEClassesAndProperties.FOLDER_REFERENCE_NUMBER, DataType.TYPE_STRING, false);

				documentPropertyValue.setValue(fdrRefNo);

				documentPropertyValues.add(documentPropertyValue);

				documentPropertyValue = BulkDeclarationFactory.newPropertyValue(CEClassesAndProperties.VOLUME_REFERENCE_NUMBER, DataType.TYPE_STRING, false);

				documentPropertyValue.setValue(volNo + "");

				documentPropertyValues.add(documentPropertyValue);

				documentPropertyValue = BulkDeclarationFactory.newPropertyValue(CEClassesAndProperties.RECORD_REFERENCE_NUMBER, DataType.TYPE_STRING, false);

				documentPropertyValue.setValue(recRefNo + "");

				documentPropertyValues.add(documentPropertyValue);

				// Add few more properties from record
				documentPropertyValue = BulkDeclarationFactory.newPropertyValue(CEClassesAndProperties.AUTHOR, DataType.TYPE_STRING, false);

				documentPropertyValue.setValue(recordBean.getAuthor());

				documentPropertyValues.add(documentPropertyValue);

				documentPropertyValue = BulkDeclarationFactory.newPropertyValue(CEClassesAndProperties.DESCRIPTION, DataType.TYPE_STRING, false);

				documentPropertyValue.setValue(recordBean.getDescription());

				documentPropertyValues.add(documentPropertyValue);

				documentPropertyValue = BulkDeclarationFactory.newPropertyValue(CEClassesAndProperties.IS_SEARCHABLE_PDF, DataType.TYPE_BOOLEAN, false);

				documentPropertyValue.setValue(documentBean.isSearchablePDF());

				documentPropertyValues.add(documentPropertyValue);

				documentPropertyValue = BulkDeclarationFactory.newPropertyValue(CEClassesAndProperties.SECURITY_CLASSIFICATION, DataType.TYPE_STRING, false);

				documentPropertyValue.setValue(recordBean.getSecClass());

				documentPropertyValues.add(documentPropertyValue);
			}

			if(recordBean.getFnRecordType().equalsIgnoreCase(ERSConstants.RECORD_TYPE_PHYSICAL)) {

				documentPropertyValue = BulkDeclarationFactory.newPropertyValue(CEClassesAndProperties.IS_CATEGORY_ONE, DataType.TYPE_BOOLEAN, false);

				documentPropertyValue.setValue(recordBean.getSecClass());

				documentPropertyValues.add(documentPropertyValue);
			}

			/*if(recordBean.getRecordClassName().equalsIgnoreCase(ERSRecordProperty.ERS_MICRO_FILM_RECORD)){

				documentPropertyValue = BulkDeclarationFactory.newPropertyValue(CEClassesAndProperties.MICROFILM_FRAME_NUMBER, DataType.TYPE_STRING, false);

				documentPropertyValue.setValue(recordBean.getMicroFilimFrameNo());

				documentPropertyValues.add(documentPropertyValue);

				documentPropertyValue = BulkDeclarationFactory.newPropertyValue(CEClassesAndProperties.MICROFILM_ROLL_NUMBER, DataType.TYPE_STRING, false);

				documentPropertyValue.setValue(recordBean.getMicroFilimRollNo());

				documentPropertyValues.add(documentPropertyValue);
			}
			 */
			if(mimeType == null || mimeType == "") {

				logger.error("MIME TYPE is Empty for Electronic  record:" );

				throw new ERSException("MIME TYPE is Empty for Electronic  record");

			} else {

				contentElements = documentDefinition.getContentElements();

				byte[] byteArray = documentBean.getDocumentByteArray();

				InputStream inputStream = new ByteArrayInputStream(byteArray); 

				//InputStream inputStream = new ByteArrayInputStream(convertInputStreamtoByteArray());

				ContentTransfer contentTransfer = BulkDeclarationFactory.newContentTransfer(inputStream, mimeType);

				contentTransfer.setFileName(documentName);	

				contentElements.add(contentTransfer);
			}

		} catch(RMRuntimeException objException) {

			logger.error("RMRuntimeException occured in setDocumentDefinitionProperties method " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} catch (Exception objException) {

			logger.error("Exception occured in setDocumentDefinitionProperties method " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} finally {

			documentPropertyValues = null;

			documentPropertyValue =  null;

			contentElements = null;
		}
	}

	/**
	 * This method is used to set the properties to record definition
	 * @param recordDefinition
	 * @param recordBean
	 * @throws ERSException
	 */
	public void setRecordDefinitionProperties(RecordDefinition recordDefinition, RecordBean recordBean) throws ERSException {

		logger.info("Entered into setRecordDefinitionProperties method");

		logger.info("recordDefinition : " + recordDefinition + "recordBean : " + recordBean.toString());

		List<PropertyValue> recordPropertyValues = null;

		String documentTitle = recordBean.getTitle();

		PropertyValue reocrdPropertyValue = null;

		//FilePlanRepository filePlanRepository = null;

		String title = recordBean.getTitle();

		String description = recordBean.getDescription();

		String tags = recordBean.getTags();

		String author = recordBean.getAuthor();

		String authorOrganisation = recordBean.getAuthorOrganisation();

		String fdrRefNo = recordBean.getFolRefNum();

		String recRefNo = recordBean.getRecRefNo();

		String volNo = recordBean.getVolRefNumber();

		String creatorDesignation = recordBean.getCreatorDesignation();

		String recordType = recordBean.getRecordType();

		String securityClassification = recordBean.getSecClass();

		String locationCode = recordBean.getLocationCode();

		//String recordClassName = recordBean.getRecordClassName();

		//Location actualLocation = RMFactory.Location.getInstance(fileplanRepository, recordBean.getLocationCode());

		String recordFolderId = recordBean.getRecordFolderId();

		String easLink = recordBean.getEasLink();

		String oldFrn = recordBean.getOldFrn();

		Date writtenDate = recordBean.getWrittenDate();

		boolean isDeleted = recordBean.isDeleted();

		String mvStatus = recordBean.getMvStatus();

		String mvLoc = recordBean.getMvLocation();

		String mvUser = recordBean.getMvUser();

		Date mvDate = recordBean.getMvDate();

		String mukimNo = recordBean.getMukimNo();

		String lotNo = recordBean.getLotNo();

		title = (title == null) ? "" : title;

		description = (description == null) ? "" : description;

		tags = (tags == null) ? "" : tags;

		author = (author == null) ? "" : author;

		authorOrganisation = (authorOrganisation == null) ? "" : authorOrganisation;

		creatorDesignation = (creatorDesignation == null) ? "" : creatorDesignation;

		recordType = (recordType == null) ? "" : recordType;

		securityClassification = (securityClassification == null) ? "" : securityClassification;

		locationCode = (locationCode == null) ? "" : locationCode;

		recordFolderId = (recordFolderId == null) ? "" : recordFolderId;

		easLink = (easLink == null) ? "" : easLink;

		oldFrn = (oldFrn == null) ? "" : oldFrn;

		fdrRefNo = (fdrRefNo == null) ? "" : fdrRefNo;

		try {

			//filePlanRepository = rmConnectionCode.getFilePlanRepositoryByName(ResourceLoader.getMessageProperties().getProperty(ERSConstants.FILE_PLAN_NAME));

			recordPropertyValues = recordDefinition.getPropertyValues();

			if(recordBean.getFnRecordType().equalsIgnoreCase(ERSConstants.RECORD_TYPE_ELECTRONIC) || recordBean.getFnRecordType().equalsIgnoreCase(ERSConstants.RECORD_TYPE_PHYSICAL)) {

				logger.info("properties are assigned here");

				reocrdPropertyValue = BulkDeclarationFactory.newPropertyValue(ERSRecordProperty.DOCUMENT_TITLE, DataType.TYPE_STRING, false);

				reocrdPropertyValue.setValue(documentTitle);

				recordPropertyValues.add(reocrdPropertyValue);				

				reocrdPropertyValue = BulkDeclarationFactory.newPropertyValue(ERSRecordProperty.DESCR, DataType.TYPE_STRING, false);

				reocrdPropertyValue.setValue(description);

				recordPropertyValues.add(reocrdPropertyValue);

				reocrdPropertyValue = BulkDeclarationFactory.newPropertyValue(ERSRecordProperty.TAG, DataType.TYPE_STRING, false);

				reocrdPropertyValue.setValue(tags);

				recordPropertyValues.add(reocrdPropertyValue);

				reocrdPropertyValue = BulkDeclarationFactory.newPropertyValue(ERSRecordProperty.FDR_REF_NO, DataType.TYPE_STRING, false);

				reocrdPropertyValue.setValue(fdrRefNo);

				recordPropertyValues.add(reocrdPropertyValue);

				reocrdPropertyValue = BulkDeclarationFactory.newPropertyValue(ERSRecordProperty.VOL_NO, DataType.TYPE_STRING, false);

				reocrdPropertyValue.setValue(volNo);

				recordPropertyValues.add(reocrdPropertyValue);

				reocrdPropertyValue = BulkDeclarationFactory.newPropertyValue(ERSRecordProperty.REC_REF_NO, DataType.TYPE_STRING, false);

				reocrdPropertyValue.setValue(recRefNo);

				recordPropertyValues.add(reocrdPropertyValue);

				reocrdPropertyValue = BulkDeclarationFactory.newPropertyValue(ERSRecordProperty.CREATOR_DESIGNATION, DataType.TYPE_STRING, false);

				reocrdPropertyValue.setValue(creatorDesignation);

				recordPropertyValues.add(reocrdPropertyValue);

				reocrdPropertyValue = BulkDeclarationFactory.newPropertyValue(ERSRecordProperty.SECURITY_CLASSIFICATION, DataType.TYPE_STRING, false);

				reocrdPropertyValue.setValue(securityClassification);

				recordPropertyValues.add(reocrdPropertyValue);

				reocrdPropertyValue = BulkDeclarationFactory.newPropertyValue(ERSRecordProperty.OLD_FRN, DataType.TYPE_STRING, false);

				reocrdPropertyValue.setValue(oldFrn);

				recordPropertyValues.add(reocrdPropertyValue);

				reocrdPropertyValue = BulkDeclarationFactory.newPropertyValue(ERSRecordProperty.IS_DELETED, DataType.TYPE_BOOLEAN, false);

				reocrdPropertyValue.setValue(isDeleted);

				recordPropertyValues.add(reocrdPropertyValue);

			} 

			if(recordBean.getFnRecordType().equalsIgnoreCase(ERSConstants.RECORD_TYPE_ELECTRONIC)) {

				reocrdPropertyValue = BulkDeclarationFactory.newPropertyValue(ERSRecordProperty.RECORD_TYPE, DataType.TYPE_STRING, false);

				reocrdPropertyValue.setValue(recordType);

				recordPropertyValues.add(reocrdPropertyValue);

				reocrdPropertyValue = BulkDeclarationFactory.newPropertyValue(ERSRecordProperty.WRITTEN_DT, DataType.TYPE_DATE, false);

				reocrdPropertyValue.setValue(writtenDate);

				recordPropertyValues.add(reocrdPropertyValue);

				reocrdPropertyValue = BulkDeclarationFactory.newPropertyValue(ERSRecordProperty.AUTHOR, DataType.TYPE_STRING, false);

				reocrdPropertyValue.setValue(author);

				recordPropertyValues.add(reocrdPropertyValue);

				reocrdPropertyValue = BulkDeclarationFactory.newPropertyValue(ERSRecordProperty.AUTHOR_OU, DataType.TYPE_STRING, false);

				reocrdPropertyValue.setValue(authorOrganisation);

				recordPropertyValues.add(reocrdPropertyValue);

				reocrdPropertyValue = BulkDeclarationFactory.newPropertyValue(ERSRecordProperty.EAS_LINK, DataType.TYPE_STRING, false);

				reocrdPropertyValue.setValue(easLink);

				recordPropertyValues.add(reocrdPropertyValue);

			}

			if(recordBean.getFnRecordType().equalsIgnoreCase(ERSConstants.RECORD_TYPE_PHYSICAL) ) {

				reocrdPropertyValue = BulkDeclarationFactory.newPropertyValue(ERSRecordProperty.LOCATION, DataType.TYPE_OBJECT, false);

				reocrdPropertyValue.setValue(BulkDeclarationFactory.newObjectReference(ResourceLoader.getMessageProperties().getProperty(ERSConstants.FILEPLAN_OBJECTSTORE), ERSRecordProperty.LOCATION, locationCode));

				recordPropertyValues.add(reocrdPropertyValue);

				reocrdPropertyValue = BulkDeclarationFactory.newPropertyValue(ERSRecordProperty.MV_STATUS, DataType.TYPE_STRING, false);

				reocrdPropertyValue.setValue(mvStatus);

				recordPropertyValues.add(reocrdPropertyValue);

				reocrdPropertyValue = BulkDeclarationFactory.newPropertyValue(ERSRecordProperty.MV_USER, DataType.TYPE_STRING, false);

				reocrdPropertyValue.setValue(mvUser);

				recordPropertyValues.add(reocrdPropertyValue);

				reocrdPropertyValue = BulkDeclarationFactory.newPropertyValue(ERSRecordProperty.MV_DT, DataType.TYPE_DATE, false);

				reocrdPropertyValue.setValue(mvDate);

				recordPropertyValues.add(reocrdPropertyValue);

			}

			if(recordBean.getFnRecordType().equalsIgnoreCase(ERSConstants.RECORD_TYPE_PHYSICAL)) {

				reocrdPropertyValue = BulkDeclarationFactory.newPropertyValue(ERSRecordProperty.MUKIM_NO, DataType.TYPE_STRING, false);

				reocrdPropertyValue.setValue(mukimNo);

				recordPropertyValues.add(reocrdPropertyValue);

				reocrdPropertyValue = BulkDeclarationFactory.newPropertyValue(ERSRecordProperty.LOT_NO, DataType.TYPE_STRING, false);

				reocrdPropertyValue.setValue(lotNo);

				recordPropertyValues.add(reocrdPropertyValue);

			}
		} catch(RMRuntimeException objException) {

			logger.error("RMRuntimeException occured in setRecordDefinitionProperties method " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} catch (Exception objException) {

			logger.error("Exception occured in setRecordDefinitionProperties method " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} finally {

			title = null;

			description = null;

			tags = null;

			author = null;

			authorOrganisation = null;

			creatorDesignation = null;

			recordType = null;

			securityClassification = null;

			locationCode = null;

		}

	}

	/**
	 * This method is used to convert Input Stream to byte array.
	 * @return arrayOutputStream
	 * @throws ERSException
	 * @throws IOException
	 */
	public byte[] convertInputStreamtoByteArray() throws ERSException, IOException {

		logger.info("Entered into convertInputStreamtoByteArray method");

		FileInputStream inputStream = null;

		ByteArrayOutputStream arrayOutputStream = null;

		int number;

		DocumentBean documentBean = null;

		try {

			String filePath = ResourceLoader.getMessageProperties().getProperty(ERSConstants.DOCUMENT_FILE_PATH);

			File file = new File(filePath);

			inputStream = new FileInputStream(file);

			arrayOutputStream = new ByteArrayOutputStream();

			documentBean = new DocumentBean();

			byte[] data = new byte[1024];

			while ((number = inputStream.read(data, 0, data.length)) != -1) {

				arrayOutputStream.write(data, 0, number);
			}

			arrayOutputStream.flush();

			documentBean.setDocumentByteArray(arrayOutputStream.toByteArray());

		} catch (IOException objException) {

			logger.error("IOException occured in convertInputStreamtoByteArray method " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} catch (Exception objException) {

			logger.error("Exception occured in convertInputStreamtoByteArray method " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} finally {

			inputStream.close();
		}

		return arrayOutputStream.toByteArray();

	}

	public Folder createROSDateFolder(String inputDate) throws ERSException {

		Folder monthFolder = null;

		Folder dateFolder = null;

		CEConnection ceConnection = null;

		try {

			logger.info("Entered into createROSFolder method" + inputDate);

			ceConnection = new CEConnection();

			String[] dateValues = inputDate.split("/");

			Folder folder = searchROSFolder("/", dateValues[0]);

			if(folder == null) {

				folder = createROSFolder(dateValues[0], ceConnection.getObjectStoreWithCEWSIConnection(ResourceLoader.getMessageProperties(), ResourceLoader.getMessageProperties().getProperty(ERSConstants.RECORDS_OBJECTSTORE)).get_RootFolder());
			}

			if(folder != null) {

				monthFolder = searchROSFolder("/" + dateValues[0], dateValues[1]);

			} if(monthFolder == null) {

				folder = searchROSFolder("/", dateValues[0]);

				if(folder.get_PathName().equalsIgnoreCase("/" + dateValues[0])) {

					monthFolder = createROSFolder(dateValues[1], folder);
				}
			}
			if(monthFolder != null) {

				dateFolder = searchROSFolder("/" + dateValues[0] + "/" + dateValues[1], dateValues[2]);

			}	if(dateFolder == null) {

				monthFolder = searchROSFolder("/" + dateValues[0], dateValues[1]);

				if(monthFolder.get_PathName().equalsIgnoreCase("/" + dateValues[0] + "/" + dateValues[1])) {

					dateFolder = createROSFolder(dateValues[2], monthFolder);
				}
			}
		}
		catch (Exception exceptionobj) {

			logger.error("Exception occured in createROSFolder method" + exceptionobj.getMessage(), exceptionobj);

			throw new ERSException(exceptionobj.getMessage());

		} finally {

			if(ceConnection != null) {
				ceConnection.closeConnection();
				ceConnection = null;
			}
		}

		return dateFolder;
	}	

	public Folder searchROSFolder(String folderPath, String searchFolder) throws Exception {

		Integer pageSize = null;

		Folder folder = null;

		CEConnection ceConnection = null;

		try {

			//logger.info("Entered into searchROSFolder method");

			logger.info("folderPath : " + folderPath + "searchFolder : " + searchFolder);

			ceConnection = new CEConnection();

			String sqlQuery = "SELECT r.this, PathName FROM Folder r WHERE r.This INSUBFOLDER '" + folderPath + "' and FolderName = '" + searchFolder + "'";

			logger.info("The content serach query is : " + sqlQuery);

			SearchScope searchScope = new SearchScope(ceConnection.getObjectStoreWithCEWSIConnection(ResourceLoader.getMessageProperties(), ResourceLoader.getMessageProperties().getProperty(ERSConstants.RECORDS_OBJECTSTORE)));

			// Perform a search that returns ResultRows 
			SearchSQL searchSql = new SearchSQL(sqlQuery);

			// Perform the search operation.
			Boolean continuable = Boolean.FALSE;

			IndependentObjectSet independentObjectSet = searchScope.fetchObjects(searchSql, pageSize, null, continuable);

			Iterator<Folder> independentObjIter = independentObjectSet.iterator();

			while(independentObjIter.hasNext()) {

				folder = (Folder) independentObjIter.next();

				if(folderPath.equalsIgnoreCase("/")) {

					if(folder.get_PathName().equalsIgnoreCase("/" + searchFolder)) {

						return folder;
					}
				} else {

					if(folder.get_PathName().equalsIgnoreCase(folderPath + "/" + searchFolder)) {

						return folder;
					}
				}
			} 
		} catch (Exception objException) {

			logger.error("Exception occured in searchROSFolder method " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} finally {

			if(ceConnection != null) {
				ceConnection.closeConnection();
				ceConnection = null;
			}
		}

		return null;

	}

	public Folder createROSFolder(String folderName, Folder parentFolder) throws Exception {

		logger.info("Entered into createROSFolder method" + "folderName : " + folderName + "parentFolder" );

		Folder folder = null;

		CEConnection ceConnection = null;

		try {

			ceConnection = new CEConnection();

			ObjectStore objectStore = ceConnection.getObjectStoreWithCEWSIConnection(ResourceLoader.getMessageProperties(), ResourceLoader.getMessageProperties().getProperty(ERSConstants.RECORDS_OBJECTSTORE));

			folder = Factory.Folder.createInstance(objectStore, "Folder");

			folder.set_FolderName(folderName);

			folder.set_Parent(parentFolder);

			folder.save(RefreshMode.REFRESH);

		} catch (Exception objException) {

			logger.error("Exception occured in createROSFolder method " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} finally {

			if(ceConnection != null) {				
				ceConnection.closeConnection();
				ceConnection = null;
			}
		}

		return folder;
	}

	/**
	 * This method is used to file the document in ROS folder
	 * @throws ERSException
	 */
	public void fileDocumentinROSFolder(String documentId, String documentName) throws ERSException {

		Document document = null;

		ReferentialContainmentRelationship relationship = null;

		CEConnection ceConnection = null;

		ERSUtil ersUtil = null;

		try {

			logger.info("Entered into fileDocumentinROSFolder method");

			ceConnection = new CEConnection();

			ersUtil = new ERSUtil();

			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");

			Date todayDate = new Date();

			String dateValue = dateFormat.format(todayDate);

			logger.info("dateValue : " + dateValue);

			Folder folder = ersUtil.createROSDateFolder(dateValue);

			logger.info("documentId : " + documentId);

			document = Factory.Document.fetchInstance(ceConnection.getObjectStoreWithCEWSIConnection(ResourceLoader.getMessageProperties(), ResourceLoader.getMessageProperties().getProperty(ERSConstants.RECORDS_OBJECTSTORE)), documentId, null);

			relationship = folder.file(document, AutoUniqueName.AUTO_UNIQUE, documentName, null);

			relationship.save(RefreshMode.REFRESH);

		} catch(RMRuntimeException objException) {

			logger.error("RMRuntimeException occured in fileDocumentinROSFolder method " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} catch (Exception objException) {

			logger.error("Exception occured in fileDocumentinROSFolder method " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} finally {

			if(ceConnection != null) {				
				ceConnection.closeConnection();
				ceConnection = null;
			}

			ersUtil = null;
		}

	}

	public void createInventoryDocument(RecordBean recordBean, List<DocumentBean> documentBeanList) throws ERSException {

		logger.info("Entered into createInventoryDocument method");

		logger.info("recordBean : " + recordBean.toString() + "documentBeanList : " + documentBeanList.size());

		CEConnection connectionCode = null;

		ObjectStore objectStore = null;

		Document document = null;

		String documentId = null;

		Properties properties = null;

		Iterator<DocumentBean> iterDocumentBean = null;

		DocumentBean documentBean = null;

		com.filenet.api.core.ContentTransfer contentTransfer = null;

		ContentElementList contentElementList = null;

		try {

			if(documentBeanList != null) {

				iterDocumentBean = documentBeanList.iterator();

				contentTransfer = Factory.ContentTransfer.createInstance();

				contentElementList = Factory.ContentElement.createList();

				while (iterDocumentBean.hasNext()) {

					documentBean = iterDocumentBean.next();

					if(documentBean.getMimeType() != null && documentBean.getDocumentByteArray() != null ) {

						connectionCode = new CEConnection();

						objectStore = connectionCode.getObjectStoreWithCEWSIConnection(ResourceLoader.getMessageProperties(), ResourceLoader.getMessageProperties().getProperty(ERSConstants.RECORDS_OBJECTSTORE));

						document = Factory.Document.createInstance(objectStore, recordBean.getInventoryClassId());

						document.set_MimeType(documentBean.getMimeType());

						InputStream inputStream = new ByteArrayInputStream(documentBean.getDocumentByteArray());

						contentTransfer.setCaptureSource(inputStream);

						contentElementList.add(contentTransfer);

						document.checkin(AutoClassify.AUTO_CLASSIFY, CheckinType.MAJOR_VERSION);

						document.set_ContentElements(contentElementList);					

						properties = document.getProperties();

						properties.putObjectValue(CEClassesAndProperties.DOCUMENT_TITLE, documentBean.getFileName());

						properties.putObjectValue(CEClassesAndProperties.AUTHOR, recordBean.getAuthor());

						properties.putObjectValue(CEClassesAndProperties.DESCRIPTION, recordBean.getDescription());

						properties.putObjectValue(CEClassesAndProperties.FOLDER_REFERENCE_NUMBER, documentBean.getFdrRefNo());

						properties.putObjectValue(CEClassesAndProperties.FRS_REFERENCE_NUMBER, recordBean.getOldFrn());

						properties.putObjectValue(CEClassesAndProperties.RECORD_REFERENCE_NUMBER, documentBean.getRecRefNo()+"");

						properties.putObjectValue(CEClassesAndProperties.VOLUME_REFERENCE_NUMBER, documentBean.getVolNo()+"");

						properties.putObjectValue(CEClassesAndProperties.SECURITY_CLASSIFICATION, recordBean.getSecClass());

						document.save(RefreshMode.REFRESH);

						documentId = document.get_Id().toString();

						documentBean.setDocumentId(documentId);

						fileDocumentinROSFolder(documentId, documentBean.getFileName());

					} else {

						logger.error("Mandatory Document mimetype or Byte array are null. Document can be created");
					}
				} 
			} else {

				logger.error("Mandatory DocumentBeanList is null. Document can be created");
			}
		} catch(RMRuntimeException objException) {

			logger.error("RMRuntimeException occured in createInventoryDocument method " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} catch (Exception objException) {

			logger.error("Exception occured in createInventoryDocument method " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} finally {

		}
	}

	public RecordBean createElectronicDocuments(RecordBean recordBean) throws ERSException {

		logger.info("Entered into createElectronicDocuments method");

		Iterator<DocumentBean> iterDocumentBean = null;

		DocumentBean documentBean = null;

		CEConnection ceConnection = null;

		ObjectStore rosObjectStore = null;

		String documentClassId = null;

		Document document = null;

		ContentElementList contentElementList = null;

		com.filenet.api.core.ContentTransfer contentTransfer = null;

		byte[] documentByteArray = null;

		String documentId = null;

		List<String> documentIdsList = null;

		List<DocumentBean> documentBeanList = null;

		List<DocumentBean> resultBeanList = null;

		try {

			if(recordBean != null) {

				documentBeanList = recordBean.getDocumentBeanList();

				resultBeanList = new ArrayList<DocumentBean>();

				if(documentBeanList != null) {

					ceConnection = new CEConnection();

					documentIdsList = new ArrayList<String>();

					documentClassId = fetchDocumentClassId(recordBean.getRecordType());

					rosObjectStore = ceConnection.getObjectStoreWithCEWSIConnection(ResourceLoader.getMessageProperties(), ResourceLoader.getMessageProperties().getProperty(ERSConstants.RECORDS_OBJECTSTORE));

					iterDocumentBean = documentBeanList.iterator();

					while(iterDocumentBean.hasNext()) {

						documentBean = iterDocumentBean.next();

						document = Factory.Document.createInstance(rosObjectStore, documentClassId);

						document.set_MimeType(documentBean.getMimeType());

						contentElementList = Factory.ContentElement.createList();		

						contentTransfer = Factory.ContentTransfer.createInstance();	

						documentByteArray = documentBean.getDocumentByteArray();

						InputStream inputStream = new ByteArrayInputStream(documentByteArray);

						contentTransfer.setCaptureSource(inputStream);

						contentElementList.add(contentTransfer);	

						document.set_ContentElements(contentElementList);

						Properties properties = document.getProperties();

						properties.putObjectValue(CEClassesAndProperties.DOCUMENT_TITLE, documentBean.getFileName());

						properties.putObjectValue(CEClassesAndProperties.FOLDER_REFERENCE_NUMBER, recordBean.getFolRefNum());

						properties.putObjectValue(CEClassesAndProperties.VOLUME_REFERENCE_NUMBER, recordBean.getVolRefNumber());

						properties.putObjectValue(CEClassesAndProperties.RECORD_REFERENCE_NUMBER, recordBean.getRecRefNo());

						properties.putObjectValue(CEClassesAndProperties.AUTHOR, recordBean.getAuthor());

						properties.putObjectValue(CEClassesAndProperties.DESCRIPTION, recordBean.getDescription());

						properties.putObjectValue(CEClassesAndProperties.IS_SEARCHABLE_PDF, documentBean.isSearchablePDF());

						properties.putObjectValue(CEClassesAndProperties.SECURITY_CLASSIFICATION, recordBean.getSecClass());

						document.checkin(AutoClassify.AUTO_CLASSIFY, CheckinType.MAJOR_VERSION);

						document.save(RefreshMode.REFRESH);

						logger.info("document : " + document.get_Id());

						documentId = document.get_Id().toString(); 

						documentIdsList.add(documentId);

						documentBean.setDocumentId(documentId);

						resultBeanList.add(documentBean);

					}

					recordBean.setDocumentBeanList(resultBeanList);

					recordBean.setDocumentIdsList(documentIdsList);

				} else {

					logger.info("DocumentBean list is null. Can not create documents in Records Object store");
				}

			} else {

				logger.info("RecordBean is null. Can not create documents in Records Object store");
			}


		} catch(RMRuntimeException objException) {

			logger.error("RMRuntimeException occured in createElectronicDocuments method " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} catch (Exception objException) {

			logger.error("Exception occured in createElectronicDocuments method " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} finally {

		}

		logger.info("RecordBean in Util : " + recordBean.toString());
		return recordBean;
	}


	public String fetchRecordTypes(String recordType) throws ERSException {

		logger.info("Entered into fetchRecordTypes method");

		try {

			if(recordType.equalsIgnoreCase(CEClassesAndProperties.ERS_CORRESPONDENCE_MEMOS)) {

				return CEClassesAndProperties.CORRESPONDENCE_MEMOS;		
			}			
			else if(recordType.equalsIgnoreCase(CEClassesAndProperties.ERS_PAPERS_REPORTS)) {

				return CEClassesAndProperties.PAPERS_REPORTS;
			}
			else if(recordType.equalsIgnoreCase(CEClassesAndProperties.ERS_MEETING_RECORDS)) {

				return CEClassesAndProperties.MEETING_RECORDS;
			}
			else if(recordType.equalsIgnoreCase(CEClassesAndProperties.ERS_POLICIES_AND_GUIDELINES)) {

				return CEClassesAndProperties.POLICIES_AND_GUIDELINES;
			}
			else if(recordType.equalsIgnoreCase(CEClassesAndProperties.ERS_LEGISLATION_REGULATIONS_CIRCULATIONS)) {

				return CEClassesAndProperties.LEGISLATION_REGULATIONS_CIRCULATIONS;
			}
			else if(recordType.equalsIgnoreCase(CEClassesAndProperties.ERS_CONTRACT_AND_AGREEMENTS)) {

				return CEClassesAndProperties.CONTRACT_AND_AGREEMENTS;
			}
			else if(recordType.equalsIgnoreCase(CEClassesAndProperties.ERS_LEGAL_BRIEFS)) {

				return CEClassesAndProperties.LEGAL_BRIEFS;
			}
			else if(recordType.equalsIgnoreCase(CEClassesAndProperties.ERS_TENDERS_QUOTATIONS)) {

				return CEClassesAndProperties.TENDERS_QUOTATIONS;
			}
			else if(recordType.equalsIgnoreCase(CEClassesAndProperties.ERS_LICENSES_PERMITS_CERTIFICATES)) {

				return CEClassesAndProperties.LICENSES_PERMITS_CERTIFICATES;
			}
			else if(recordType.equalsIgnoreCase(CEClassesAndProperties.ERS_FORMS)) {

				return CEClassesAndProperties.FORMS;
			}
			else if(recordType.equalsIgnoreCase(CEClassesAndProperties.ERS_NEWS_RELEASES)) {

				return CEClassesAndProperties.NEWS_RELEASES;
			}
			else if(recordType.equalsIgnoreCase(CEClassesAndProperties.ERS_PUBLICATIONS_AND_PROMOTIONS)) {

				return CEClassesAndProperties.PUBLICATIONS_AND_PROMOTIONS;
			}
			else if(recordType.equalsIgnoreCase(CEClassesAndProperties.ERS_PRESENTATIONS)) {

				return CEClassesAndProperties.PRESENTATIONS;
			}
			else if(recordType.equalsIgnoreCase(CEClassesAndProperties.PLANS)) {

				return CEClassesAndProperties.PLANS;
			}
			else if(recordType.equalsIgnoreCase(CEClassesAndProperties.ERS_VISUALS)) {

				return CEClassesAndProperties.VISUALS;
			}
			else if(recordType.equalsIgnoreCase(CEClassesAndProperties.ERS_FINANCE_RECORDS)) {

				return CEClassesAndProperties.FINANCE_RECORDS;
			}
			else if(recordType.equalsIgnoreCase(CEClassesAndProperties.ERS_TEMPLATES)) {

				return CEClassesAndProperties.TEMPLATES;
			}
		} catch(RMRuntimeException objException) {

			logger.error("RMRuntimeException occured in fetchRecordTypes method " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} catch (Exception objException) {

			logger.error("Exception occured in fetchRecordTypes method " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} finally {

		}

		return recordType;
	}

	public String fetchDocumentClassId(String recordType) throws ERSException {

		logger.info("Entered into fetchRecordTypes method");

		try {

			if(recordType.equalsIgnoreCase(CEClassesAndProperties.ERS_CORRESPONDENCE_MEMOS)) {

				return CEClassesAndProperties.CORRESPONDENCE_MEMOS_ID;		
			}			
			else if(recordType.equalsIgnoreCase(CEClassesAndProperties.ERS_PAPERS_REPORTS)) {

				return CEClassesAndProperties.PAPERS_REPORTS_ID;
			}
			else if(recordType.equalsIgnoreCase(CEClassesAndProperties.ERS_MEETING_RECORDS)) {

				return CEClassesAndProperties.MEETING_RECORDS_ID;
			}
			else if(recordType.equalsIgnoreCase(CEClassesAndProperties.ERS_POLICIES_AND_GUIDELINES)) {

				return CEClassesAndProperties.POLICIES_AND_GUIDELINES_ID;
			}
			else if(recordType.equalsIgnoreCase(CEClassesAndProperties.ERS_LEGISLATION_REGULATIONS_CIRCULATIONS)) {

				return CEClassesAndProperties.LEGISLATION_REGULATIONS_CIRCULATIONS_ID;
			}
			else if(recordType.equalsIgnoreCase(CEClassesAndProperties.ERS_CONTRACT_AND_AGREEMENTS)) {

				return CEClassesAndProperties.CONTRACT_AND_AGREEMENTS_ID;
			}
			else if(recordType.equalsIgnoreCase(CEClassesAndProperties.ERS_LEGAL_BRIEFS)) {

				return CEClassesAndProperties.LEGAL_BRIEFS_ID;
			}
			else if(recordType.equalsIgnoreCase(CEClassesAndProperties.ERS_TENDERS_QUOTATIONS)) {

				return CEClassesAndProperties.TENDERS_QUOTATIONS_ID;
			}
			else if(recordType.equalsIgnoreCase(CEClassesAndProperties.ERS_LICENSES_PERMITS_CERTIFICATES)) {

				return CEClassesAndProperties.LICENSES_PERMITS_CERTIFICATES_ID;
			}
			else if(recordType.equalsIgnoreCase(CEClassesAndProperties.ERS_FORMS)) {

				return CEClassesAndProperties.FORMS_ID;
			}
			else if(recordType.equalsIgnoreCase(CEClassesAndProperties.ERS_NEWS_RELEASES)) {

				return CEClassesAndProperties.NEWS_RELEASES_ID;
			}
			else if(recordType.equalsIgnoreCase(CEClassesAndProperties.ERS_PUBLICATIONS_AND_PROMOTIONS)) {

				return CEClassesAndProperties.PUBLICATIONS_AND_PROMOTIONS_ID;
			}
			else if(recordType.equalsIgnoreCase(CEClassesAndProperties.ERS_PRESENTATIONS)) {

				return CEClassesAndProperties.PRESENTATIONS_ID;
			}
			else if(recordType.equalsIgnoreCase(CEClassesAndProperties.PLANS)) {

				return CEClassesAndProperties.PLANS_ID;
			}
			else if(recordType.equalsIgnoreCase(CEClassesAndProperties.ERS_VISUALS)) {

				return CEClassesAndProperties.VISUALS_ID;
			}
			else if(recordType.equalsIgnoreCase(CEClassesAndProperties.ERS_FINANCE_RECORDS)) {

				return CEClassesAndProperties.FINANCE_RECORDS_ID;
			}
			else if(recordType.equalsIgnoreCase(CEClassesAndProperties.ERS_TEMPLATES)) {

				return CEClassesAndProperties.TEMPLATES_ID;
			}
		} catch(RMRuntimeException objException) {

			logger.error("RMRuntimeException occured in fetchRecordTypes method " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} catch (Exception objException) {

			logger.error("Exception occured in fetchRecordTypes method " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} finally {

		}

		return recordType;
	}

	/**
	 * This method is used to retrieve location information
	 * @param locationBean
	 * @throws ERSException
	 */
	public Location retrieveLocationInfo(LocationBean locationBean) throws ERSException {

		logger.info("Entered into retrieveLocationInfo method");

		RMConnection rmConnection = null;

		FilePlanRepository filePlanRepository = null;

		String sqlStatement = null;

		RMProperties rmProperties = null;

		Location location = null;

		try {

			if(locationBean != null) {

				if(locationBean.getBarcode() != null) {

					rmConnection = new RMConnection();

					filePlanRepository = rmConnection.getFilePlanRepositoryByName(ResourceLoader.getMessageProperties().getProperty(ERSConstants.FILEPLAN_OBJECTSTORE));

					RMSearch rmSearch = new RMSearch(filePlanRepository);

					sqlStatement = "SELECT [This], [BarcodeID], [LocationName] FROM [Location] WHERE ([BarcodeID] = '" + locationBean.getBarcode() + "')";

					logger.info("sqlStatement : " + sqlStatement);

					PageableSet<ResultRow> pagebleSet = rmSearch.fetchRows(sqlStatement, 10, null, true);

					if(!pagebleSet.isEmpty()) {

						Iterator<ResultRow> locIterator = pagebleSet.iterator();

						while(locIterator.hasNext()) {

							ResultRow resultRowValue  = locIterator.next();

							rmProperties = resultRowValue.getProperties();

							logger.info(rmProperties.getStringValue("BarcodeID"));

							location = (Location)resultRowValue.getProperties().getObjectValue("This");

							logger.info("location : " + location);

							return location;
						}
					}
				} else {

					logger.error("Barcode is mandatory to create physical items. Physical Object can not be created");
				}
			} else {

				logger.error("Location Bean is null while searching the location");
			}

		} catch(RMRuntimeException objException) {

			logger.error("RMRuntimeException occured in retrieveLocationInfo method " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} catch (Exception objException) {

			logger.error("Exception occured in retrieveLocationInfo method " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} finally {

			if(null!=rmConnection) {
				rmConnection.closeRMConnection();
			}
			
			filePlanRepository = null;

			sqlStatement = null;

			rmProperties = null;
		}

		return location;
	}

	public static void main(String[] args) throws Exception {

		ERSUtil ersUtil = new ERSUtil();

		//ersUtil.fileDocumentinROSFolder("{BBBAFDDC-802C-586A-8C02-C3BC68E0F687}", "SLADocument");

		ersUtil.createROSDateFolder("2014/12/30");
	}

}
