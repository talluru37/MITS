package com.ers.jarm.api.record.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.ers.jarm.api.bean.DocumentBean;
import com.ers.jarm.api.bean.RecordBean;
import com.ers.jarm.api.connection.EncryptionUtil;
import com.ers.jarm.api.connection.RMConnection;
import com.ers.jarm.api.constants.ERSConstants;
import com.ers.jarm.api.constants.ERSFolderProperty;
import com.ers.jarm.api.constants.ERSRecordProperty;
import com.ers.jarm.api.exception.ERSException;
import com.ers.jarm.api.folder.impl.RecordFolderOperationsImpl;
import com.ers.jarm.api.record.IRecordOperations;
import com.ers.jarm.api.resources.ResourceLoader;
import com.ers.jarm.api.util.ERSUtil;
import com.filenet.rm.bds.BDSConstants;
import com.filenet.rm.bds.BatchResultItem;
import com.filenet.rm.bds.BulkDeclarationService;
import com.filenet.rm.bds.DocumentDefinition;
import com.filenet.rm.bds.RecordDefinition;
import com.filenet.rm.bds.ResultItemStatus;
import com.filenet.rm.bds.exception.BDSException;
import com.filenet.rm.bds.impl.BulkDeclarationFactory;
import com.ibm.jarm.api.constants.DeleteMode;
import com.ibm.jarm.api.constants.DomainType;
import com.ibm.jarm.api.constants.RMClassName;
import com.ibm.jarm.api.constants.RMPropertyName;
import com.ibm.jarm.api.constants.RMRefreshMode;
import com.ibm.jarm.api.core.ContentRepository;
import com.ibm.jarm.api.core.FilePlanRepository;
import com.ibm.jarm.api.core.Location;
import com.ibm.jarm.api.core.RMFactory;
import com.ibm.jarm.api.core.RMLink;
import com.ibm.jarm.api.core.Record;
import com.ibm.jarm.api.core.RecordContainer;
import com.ibm.jarm.api.core.RecordFolder;
import com.ibm.jarm.api.exception.RMRuntimeException;
import com.ibm.jarm.api.meta.RMClassDescription;
import com.ibm.jarm.api.meta.RMPropertyDescription;
import com.ibm.jarm.api.property.RMProperties;
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
 * This class is used for implementing Record Operations to create record, 
 * view record, update record and delete record
 * 
 * @class name RecordOperationsImpl.java
 * @date  sep,2014
 * @author MITS
 * @version 1.0
 */
public class RecordOperationsImpl implements IRecordOperations {

	private static Logger logger = Logger.getLogger(RecordOperationsImpl.class);

	public RecordOperationsImpl() throws ERSException {

		try
		{
			ResourceLoader.loadProperties();

		}
		catch (Exception objException) 
		{

			logger.error("Exception occurred in RecordOperationsImpl constructor [Properties are not loaded] : " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());
		}
	}

	/**
	 * This method is used to create the record 
	 * @param recordBean
	 * @return recordBean
	 * @throws ERSException
	 */
	public RecordBean createRecord(RecordBean recordBean) throws ERSException {

		logger.info("Entered into createRecord method");

		try {

			logger.info("recordBean : " + recordBean.toString());

			if(null != recordBean && recordBean.getFnRecordType() != null) {

				if(recordBean.getFnRecordType().equalsIgnoreCase(ERSConstants.RECORD_TYPE_ELECTRONIC)) {	

					return electronicRecordCreation(recordBean);			

				} else if(recordBean.getFnRecordType().equalsIgnoreCase(ERSConstants.RECORD_TYPE_PHYSICAL)) {

					return createPhysicalFileRecord(recordBean);					

				} else if(recordBean.getFnRecordType().equalsIgnoreCase(ERSConstants.RECORD_TYPE_MICRO_FILM) ) {

					return createMicroFilmRecords(recordBean);

				} else if(recordBean.getFnRecordType().equalsIgnoreCase(ERSConstants.RECORD_TYPE_ARCHIVAL_MEDIA) ) {

					return createArchivalMediaRecords(recordBean);

				} else if(recordBean.getFnRecordType().equalsIgnoreCase(ERSConstants.RECORD_TYPE_CARTON) ) {

					return createCartonRecords(recordBean);
				}
			} else {

				logger.error("Either mandatory Record OR Filenet Record Type is null. Record can not be created.");
			}
		} catch (Exception objException) {

			logger.error("Exception occured in createRecord method " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} 
		return recordBean;
	}

	/**
	 * This method is used to create the electronic records 
	 * @param recordBean
	 * @return recordBean
	 * @throws ERSException
	 */
	public RecordBean createElectronicRecord(RecordBean recordBean) throws ERSException {

		logger.info("Entered into createElectronicRecord method");

		ArrayList<DocumentDefinition> documentDefinitions = null;

		DocumentDefinition documentDefinition = null;

		RecordDefinition recordDefinition = null;

		BatchResultItem[] results = null;

		BatchResultItem batchResultItem = null;

		ResultItemStatus resultItemStatus = null;

		String message = null;

		String documentId = null;

		DocumentBean documentBean = null;

		Map<String, String> contextInfo =null;

		EncryptionUtil encryptionUtil=null;

		try {

			if(null != recordBean && recordBean.getDocumentBeanList() != null) {

				if(null != recordBean.getRecordFolderId() && !recordBean.getRecordFolderId().equalsIgnoreCase("")) {

					if(null != recordBean.getFolRefNum() && null != recordBean.getVolRefNumber() && null != recordBean.getRecRefNo() && null != recordBean.getSecClass() && null != recordBean.getRecordType()) {

						if(!recordBean.getFolRefNum().equalsIgnoreCase("") && !recordBean.getVolRefNumber().equalsIgnoreCase("") && !recordBean.getRecRefNo().equalsIgnoreCase("") && !recordBean.getSecClass().equalsIgnoreCase("") && !recordBean.getRecordType().equalsIgnoreCase("")) {

							logger.info("recordBean : " + recordBean.toString());

							contextInfo = new HashMap<String, String>();

							encryptionUtil = new EncryptionUtil();
							
							contextInfo.put(BDSConstants.CONTEXT_TRANSPORT_TYPE,BDSConstants.TRANSPORT_TYPE_BDP40_WSI);

							contextInfo.put(BDSConstants.CONTEXT_PROTOCOL, ResourceLoader.getMessageProperties().getProperty(ERSConstants.CONTEXT_PROTOCOL));

							contextInfo.put(BDSConstants.CONTEXT_SERVER, ResourceLoader.getMessageProperties().getProperty(ERSConstants.CONTEXT_SERVER));

							contextInfo.put(BDSConstants.CONTEXT_PORT, ResourceLoader.getMessageProperties().getProperty(ERSConstants.CONTEXT_PORT));

							contextInfo.put(BDSConstants.CONTEXT_SERVICE, ResourceLoader.getMessageProperties().getProperty(ERSConstants.CONTEXT_SERVICE));

							contextInfo.put(BDSConstants.CONTEXT_ENDPOINT, ResourceLoader.getMessageProperties().getProperty(ERSConstants.CONTEXT_ENDPOINT));

							contextInfo.put(BDSConstants.CONTEXT_BINDING, ResourceLoader.getMessageProperties().getProperty(ERSConstants.CONTEXT_BINDING));

							contextInfo.put(BDSConstants.CONTEXT_USERNAME, ResourceLoader.getMessageProperties().getProperty(ERSConstants.USER_NAME));

							contextInfo.put(BDSConstants.CONTEXT_PASSWORD, ResourceLoader.getMessageProperties().getProperty(ERSConstants.PASS_WORD));

							logger.debug("************* BDS Loading parameters ************");
							logger.debug("BDSConstants.CONTEXT_TRANSPORT_TYPE :"+contextInfo.get(BDSConstants.CONTEXT_TRANSPORT_TYPE));
							logger.debug("BDSConstants.CONTEXT_PROTOCOL :"+contextInfo.get(BDSConstants.CONTEXT_PROTOCOL));
							logger.debug("BDSConstants.CONTEXT_SERVER :"+contextInfo.get(BDSConstants.CONTEXT_SERVER));
							logger.debug("BDSConstants.CONTEXT_PORT :"+contextInfo.get(BDSConstants.CONTEXT_PORT));
							logger.debug("BDSConstants.CONTEXT_SERVICE :"+contextInfo.get(BDSConstants.CONTEXT_SERVICE));
							logger.debug("BDSConstants.CONTEXT_ENDPOINT :"+contextInfo.get(BDSConstants.CONTEXT_ENDPOINT));
							logger.debug("BDSConstants.CONTEXT_BINDING :"+contextInfo.get(BDSConstants.CONTEXT_BINDING));
							logger.debug("BDSConstants.CONTEXT_USERNAME :"+contextInfo.get(BDSConstants.CONTEXT_USERNAME));
							logger.debug("BDSConstants.CONTEXT_PASSWORD :"+contextInfo.get(BDSConstants.CONTEXT_PASSWORD));
							logger.debug("**************************************************");

							/*Map<String, String> contextMap = new HashMap<String, String>();
							
							contextMap.put(BDSConstants.CONTEXT_TRANSPORT_TYPE,BDSConstants.TRANSPORT_TYPE_BDP40_JACE);

							contextMap.put(BDSConstants.CONTEXT_URI, ResourceLoader.getMessageProperties().getProperty(ERSConstants.EJB_CONNECTION_URI));

							contextMap.put(BDSConstants.CONTEXT_USERNAME, ResourceLoader.getMessageProperties().getProperty(ERSConstants.USER_NAME));

							contextMap.put(BDSConstants.CONTEXT_PASSWORD, ResourceLoader.getMessageProperties().getProperty(ERSConstants.PASS_WORD));*/
							
							BulkDeclarationService bds = BulkDeclarationFactory.getBulkDeclarationService(contextInfo);

							bds.startBatch("ERSBatchOperation");		
							
							ERSUtil ersUtil = new ERSUtil();

							documentDefinitions = new ArrayList<DocumentDefinition>();

							List<DocumentBean>  documentBeanList = recordBean.getDocumentBeanList();

							Iterator<DocumentBean> documentBeanIter = documentBeanList.iterator();

							String recordType = recordBean.getRecordType();

							while(documentBeanIter.hasNext()) {

								documentBean = documentBeanIter.next();

								documentDefinition = ersUtil.createDocumentDefinition(recordType, documentBean, recordBean);

								documentDefinitions.add(documentDefinition);

								documentId = documentDefinition.getId();

								documentBean.setDocumentId(documentId);
							}

							recordDefinition = ersUtil.createRecordDefinition(ERSRecordProperty.ERS_ELECTRONIC_RECORD, recordBean);

							bds.createDocumentAndDeclareRecord("Record Declaration", documentDefinitions, recordDefinition);

							/*String recordId = recordDefinition.getId();

							logger.info("recordDefinition : " + recordDefinition.getPropertyValues().toString());

							logger.info("recordId : " + recordId);

							recordBean.setRecordId(recordId);*/

							recordBean.setDocumentBeanList(documentBeanList);

							try	{

								results = bds.executeBatch();

								String recordId = recordDefinition.getId();

								recordBean.setRecordId(recordId);
								
								documentBeanIter = documentBeanList.iterator();
								
								while(documentBeanIter.hasNext()) {

									documentBean = documentBeanIter.next();

									documentId = documentBean.getDocumentId();

									logger.info("documentId : " + documentId);

									ersUtil.fileDocumentinROSFolder(documentId, documentBean.getFileName());								
								}
							} catch (BDSException bdsException) {

								logger.error("BDSException: " + bdsException.getLocalizedMessage());

								results = bds.getBatchResultItems();						

							}
							catch (Exception ex) {

								logger.error("Exception occured in createBulkElectronicRecords" + ex.getLocalizedMessage());
							}
							if ( results != null ) {

								for (int r = 0; r < results.length; r++) {

									batchResultItem = results[r];

									logger.info("batchResultItem : " + results[r]);

									message = " Operation " + batchResultItem.getOperationIdent();

									resultItemStatus = batchResultItem.getResultStatus();

									if ( ResultItemStatus.EXECUTION_SUCCEEDED.equals(resultItemStatus) ) {

										message = message + " succeeded.";
									}
								}
							}
						} else {

							logger.error("Folder Reference, Volume No, Record reference, Security Classfication, Record type are mandatory. Either 1 of OR all mandatory fields are blank. Record can not be created");
						}
					} else {

						logger.error("Folder Reference, Volume No, Record reference, Security Classfication, Record type are mandatory. Either 1 of OR all mandatory fields are null. Record can not be created");
					}
				} else {

					logger.error("Record Folder Id is null which is must for record creation. Record can not be created");
				}

			} else {

				logger.error("Either mandatory Record Bean OR Document Bean is null. Record can not be created.");
			}

		} catch (RMRuntimeException objException) {

			logger.error("RMRuntimeException occured in createElectronicRecord method : " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} catch (Exception objException) {

			logger.error("Exception occured in createElectronicRecord method " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} finally {

			if(documentDefinitions != null) {
				documentDefinitions.clear();
				documentDefinitions = null;
			}

			if(documentDefinition != null) {
				documentDefinition = null;
			}

			if(contextInfo != null)
				contextInfo.clear();				
			contextInfo = null;

			recordDefinition = null;

			batchResultItem = null;

			resultItemStatus = null;

			message = null;

		}

		logger.info("recordBean : " + recordBean.toString());
		return recordBean;
	}
	
	public RecordBean electronicRecordCreation(RecordBean recordBean) throws ERSException {

		logger.info("Entered into electronicRecordCreation method");

		logger.info("recordBean : " + recordBean.toString());

		RecordFolder primaryContainer = null;

		RecordContainer secondaryContainer = null;

		FilePlanRepository fileplanRepository = null;

		List<RecordContainer> additionalContainers = null;

		RMConnection rmConnection = null;

		String recordFolderId = null;

		RMProperties rmProperties = null;

		String recordId = null;

		RecordFolderOperationsImpl folderOperationsImpl = null;
		
		ContentRepository contentRepository = null;
		
		ERSUtil ersUtil = null;
		
		String documentId = null;
		
		List<String> listDocumentIds = null;
		
		List<DocumentBean> listDocumentBeans = null;
		
		Iterator<DocumentBean> documentBeanIter = null;
		
		DocumentBean documentBean = null;

		try {

			if(null != recordBean) {

				if(null != recordBean.getRecordFolderId() && !recordBean.getRecordFolderId().equalsIgnoreCase("")) {

					if(null != recordBean.getFolRefNum() && null != recordBean.getVolRefNumber() && null != recordBean.getRecRefNo() && null != recordBean.getSecClass()) {

						if(!recordBean.getFolRefNum().equalsIgnoreCase("") && !recordBean.getVolRefNumber().equalsIgnoreCase("") && !recordBean.getRecRefNo().equalsIgnoreCase("") && !recordBean.getSecClass().equalsIgnoreCase("")) {

							rmConnection = new RMConnection();

							fileplanRepository = rmConnection.getFilePlanRepositoryByName(ResourceLoader.getMessageProperties().getProperty(ERSConstants.FILEPLAN_OBJECTSTORE));

							contentRepository = rmConnection.getRecordsRepositoryByName(ResourceLoader.getMessageProperties().getProperty(ERSConstants.RECORDS_OBJECTSTORE));
							
							recordFolderId = recordBean.getRecordFolderId();

							folderOperationsImpl = new RecordFolderOperationsImpl();
							
							ersUtil = new ERSUtil();

							primaryContainer = RMFactory.RecordFolder.fetchInstance(fileplanRepository, recordFolderId, null);

							logger.info("Primary Container :" + primaryContainer.getName());

							additionalContainers = new ArrayList<RecordContainer>(1);

							additionalContainers.add(secondaryContainer);

							rmProperties = RMFactory.RMProperties.createInstance(DomainType.P8_CE);

							if(recordBean.getFnRecordType().equalsIgnoreCase(ERSConstants.RECORD_TYPE_ELECTRONIC)) {

								rmProperties.putStringValue(ERSRecordProperty.DOCUMENT_TITLE, recordBean.getTitle());

								rmProperties.putStringValue(ERSRecordProperty.FDR_REF_NO, recordBean.getFolRefNum());

								rmProperties.putStringValue(ERSRecordProperty.VOL_NO, recordBean.getVolRefNumber());

								rmProperties.putStringValue(ERSRecordProperty.REC_REF_NO, recordBean.getRecRefNo());

								rmProperties.putStringValue(ERSRecordProperty.RECORD_DESC, recordBean.getDescription());

								rmProperties.putStringValue(ERSRecordProperty.TAG, recordBean.getTags());

								rmProperties.putStringValue(ERSRecordProperty.CREATOR_DESIGNATION, recordBean.getCreatorDesignation());

								rmProperties.putStringValue(ERSRecordProperty.SECURITY_CLASSIFICATION, recordBean.getSecClass());

								rmProperties.putStringValue(ERSRecordProperty.OLD_FRN, recordBean.getOldFrn());

								rmProperties.putBooleanValue(ERSRecordProperty.IS_DELETED, recordBean.isDeleted());

								rmProperties.putStringValue(ERSRecordProperty.RECORD_TYPE, recordBean.getRecordType());

								rmProperties.putDateTimeValue(ERSRecordProperty.WRITTEN_DT, recordBean.getWrittenDate());

								rmProperties.putStringValue(ERSRecordProperty.AUTHOR, recordBean.getAuthor());

								rmProperties.putStringValue(ERSRecordProperty.AUTHOR_OU, recordBean.getAuthorOrganisation());

								rmProperties.putStringValue(ERSRecordProperty.EAS_LINK, recordBean.getEasLink());
								
								recordBean = ersUtil.createElectronicDocuments(recordBean);
								
								listDocumentIds = recordBean.getDocumentIdsList();

								Record newRecord = primaryContainer.declare(ERSRecordProperty.ERS_ELECTRONIC_RECORD, rmProperties, null, null, contentRepository, listDocumentIds);

								newRecord.save(RMRefreshMode.Refresh);

								recordId = newRecord.getProperties().getGuidValue(ERSRecordProperty.OBJECT_GUID);

								logger.info("Successfully declared new Electronic record: " +recordId);

								recordBean.setRecordId(recordId);

								recordBean.setRecordCount(recordBean.getRecordCount() + 1);

								folderOperationsImpl.updatePropertiesToVolume(primaryContainer, null, recordBean);
								
								listDocumentBeans = recordBean.getDocumentBeanList();
								
								documentBeanIter = listDocumentBeans.iterator();

								while(documentBeanIter.hasNext()) {

									documentBean = documentBeanIter.next();

									documentId = documentBean.getDocumentId();

									logger.info("documentId : " + documentId);

									ersUtil.fileDocumentinROSFolder(documentId, documentBean.getFileName());								
								}

							} else {

								logger.error("Record Type is not Electronic record type. Electronic record can not be created.");
							}

						} else {

							logger.error("Folder Reference, Volume No, Record reference, Security Classfication, Record type are mandatory. Either 1 of OR all mandatory fields are blank. Record can not be created");
						}
					} else {

						logger.error("Folder Reference, Volume No, Record reference, Security Classfication, Record type are mandatory. Either 1 of OR all mandatory fields are null. Record can not be created");
					}
				} else {

					logger.error("Record Folder Id is null which is must for record creation. Record can not be created");
				}
			} else {

				logger.error("Either mandatory Record Bean OR Document Bean is null. Record can not be created.");
			}

		} catch (RMRuntimeException objException) {

			logger.error("RMRuntimeException occured in electronicRecordCreation method : " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} catch (Exception objException) {

			logger.error("Exception occured in electronicRecordCreation method " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} finally {

		}

		logger.info("RecordBean in Impl : " + recordBean.toString());
		return recordBean;

	}

	/**
	 * This method is used to create the electronic records using Bulk Declaration Services
	 * @param recordBeanList
	 * @return recordBeanList
	 * @throws ERSException
	 */
	public List<RecordBean> createRecordsUsingBDS(List<RecordBean> recordBeanList) throws ERSException {

		logger.info("Entered into createRecord method");

		RecordBean recordBean = null;

		try {

			logger.info("recordBean : " + recordBeanList.toString());

			if(null != recordBeanList ) {

				Iterator<RecordBean> recordBeanIter = recordBeanList.iterator();			

				while(recordBeanIter.hasNext()) {

					recordBean = recordBeanIter.next();

					if(null != recordBean.getFnRecordType()) {

						if(recordBean.getFnRecordType().equalsIgnoreCase(ERSConstants.RECORD_TYPE_ELECTRONIC)) {	

							return createElectronicRecordsUsingBDS(recordBeanList);			

						} else if(recordBean.getFnRecordType().equalsIgnoreCase(ERSConstants.RECORD_TYPE_PHYSICAL)) {

							return createPhysicalRecordsUsingBDS(recordBeanList);
						}
					} else {

						logger.error("Either Filenet Record Type is null. Record can not be created.");
					}
				}
			} else {

				logger.error("Either mandatory RecordBean OR Filenet Record Type is null. Record can not be created.");
			}
		} catch (Exception objException) {

			logger.error("Exception occured in createRecord method " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} 
		return recordBeanList;
	}

	/**
	 * This method is used to create the bulk electronic records
	 * @param recordBeanList
	 * @return recordBeanList
	 * @throws ERSException
	 */
	public List<RecordBean> createElectronicRecordsUsingBDS(List<RecordBean> recordBeanList) throws ERSException {

		logger.info("Entered into createElectronicRecordsUsingBDS method");

		logger.info("recordBeanList : " + recordBeanList.toString());

		ArrayList<DocumentDefinition> documentDefinitions = null;

		DocumentDefinition documentDefinition = null;

		RecordDefinition recordDefinition = null;

		BatchResultItem[] results = null;

		BatchResultItem batchResultItem = null;

		ResultItemStatus resultItemStatus = null;

		String message = null;

		DocumentBean documentBean = null;

		List<DocumentBean>  documentBeanList = null;

		Iterator<DocumentBean> documentBeanIter = null;

		RecordBean recordBean = null;

		String documentId = null;

		BulkDeclarationService bds = null;

		ERSUtil ersUtil = null;

		Map<String, String> contextInfo = null;

		try {

			if(null != recordBeanList ) {

				contextInfo = new HashMap<String, String>();

				EncryptionUtil encryptionUtil = new EncryptionUtil();

				contextInfo.put(BDSConstants.CONTEXT_TRANSPORT_TYPE,BDSConstants.TRANSPORT_TYPE_BDP40_WSI);

				contextInfo.put(BDSConstants.CONTEXT_PROTOCOL, ResourceLoader.getMessageProperties().getProperty(ERSConstants.CONTEXT_PROTOCOL));

				contextInfo.put(BDSConstants.CONTEXT_SERVER, ResourceLoader.getMessageProperties().getProperty(ERSConstants.CONTEXT_SERVER));

				contextInfo.put(BDSConstants.CONTEXT_PORT, ResourceLoader.getMessageProperties().getProperty(ERSConstants.CONTEXT_PORT));

				contextInfo.put(BDSConstants.CONTEXT_SERVICE, ResourceLoader.getMessageProperties().getProperty(ERSConstants.CONTEXT_SERVICE));

				contextInfo.put(BDSConstants.CONTEXT_ENDPOINT, ResourceLoader.getMessageProperties().getProperty(ERSConstants.CONTEXT_ENDPOINT));

				contextInfo.put(BDSConstants.CONTEXT_BINDING, ResourceLoader.getMessageProperties().getProperty(ERSConstants.CONTEXT_BINDING));

				contextInfo.put(BDSConstants.CONTEXT_USERNAME, ResourceLoader.getMessageProperties().getProperty(ERSConstants.USER_NAME));

				contextInfo.put(BDSConstants.CONTEXT_PASSWORD, ResourceLoader.getMessageProperties().getProperty(ERSConstants.PASS_WORD));

				ersUtil = new ERSUtil();

				Iterator<RecordBean> recordBeanIter = recordBeanList.iterator();

				while(recordBeanIter.hasNext()) {

					recordBean = recordBeanIter.next();

					bds = BulkDeclarationFactory.getBulkDeclarationService(contextInfo);

					bds.startBatch("ERS Batch Operation");			

					if(null != recordBean.getRecordFolderId() && !recordBean.getRecordFolderId().equalsIgnoreCase("")) {

						if(null != recordBean.getFolRefNum() && null != recordBean.getVolRefNumber() && null != recordBean.getRecRefNo() && null != recordBean.getSecClass() && null != recordBean.getRecordType()) {

							if(!recordBean.getFolRefNum().equalsIgnoreCase("") && !recordBean.getVolRefNumber().equalsIgnoreCase("") && !recordBean.getRecRefNo().equalsIgnoreCase("") && !recordBean.getSecClass().equalsIgnoreCase("") && !recordBean.getRecordType().equalsIgnoreCase("")) {

								logger.info("recordBean : " + recordBean.toString());

								logger.info("Inside recordBeanIter while" + recordBean.getDescription());

								documentBeanList = recordBean.getDocumentBeanList();

								documentBeanIter = documentBeanList.iterator();

								documentDefinitions = new ArrayList<DocumentDefinition>();

								String recordType = recordBean.getRecordType();

								while(documentBeanIter.hasNext()) {

									documentBean = documentBeanIter.next();

									documentDefinition = ersUtil.createDocumentDefinition(recordType, documentBean, recordBean);

									documentDefinitions.add(documentDefinition);

									documentId = documentDefinition.getId();

									logger.info("documentId : " + documentId);

									documentBean.setDocumentId(documentId);

								}
								//documentBeanList.add(documentBean);

								recordDefinition = ersUtil.createRecordDefinition(ERSRecordProperty.ERS_ELECTRONIC_RECORD, recordBean);

								bds.createDocumentAndDeclareRecord("Record Declaration", documentDefinitions, recordDefinition);

								recordBean.setDocumentBeanList(documentBeanList);

							} else {

								logger.error("Folder Reference, Volume No, Record reference, Security Classfication, Record type are mandatory. Either 1 of OR all mandatory fields are blank. Record can not be created");
							}
						} else {

							logger.error("Folder Reference, Volume No, Record reference, Security Classfication, Record type are mandatory. Either 1 of OR all mandatory fields are null. Record can not be created");
						}
					} else {

						logger.error("Record Folder Id is null which is must for record creation. Record can not be created");
					}

					try
					{
						results = bds.executeBatch();

						logger.info("results length : " + results.length);

						String recordId = recordDefinition.getId();

						//logger.info("recordId : " + recordId);

						recordBean.setRecordId(recordId);

						documentBeanIter = documentBeanList.iterator();

						while(documentBeanIter.hasNext()) {

							documentBean = documentBeanIter.next();

							documentId = documentBean.getDocumentId();

							logger.info("documentId : " + documentId);

							ersUtil.fileDocumentinROSFolder(documentId, documentBean.getFileName());
						}
					}

					catch (BDSException bdsException) 
					{
						logger.error("BDSException: " + bdsException.getLocalizedMessage());

						results = bds.getBatchResultItems();
					}
					catch (Exception ex) {

						logger.error("Exception occured in createBulkElectronicRecords" + ex.getLocalizedMessage());
					}
					if ( results != null ) {

						for (int r = 0; r < results.length; r++) {

							batchResultItem = results[r];

							message = " Operation " + batchResultItem.getOperationIdent();

							resultItemStatus = batchResultItem.getResultStatus();

							if ( ResultItemStatus.EXECUTION_SUCCEEDED.equals(resultItemStatus) ) {

								message = message + " succeeded.";
							}
						}
					}
				}
			} else {

				logger.error("mandatory Record Bean is null. Record can not be created.");
			}

		} catch (RMRuntimeException objException) {

			logger.error("RMRuntimeException occured in createElectronicRecordsUsingBDS method : " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} catch (Exception objException) {

			logger.error("Exception occured in createElectronicRecordsUsingBDS method " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} finally {

			if(documentDefinitions != null) {
				documentDefinitions.clear();
				documentDefinitions = null;
			}

			if(documentDefinition != null) 
				documentDefinition = null;

			if(contextInfo != null) {
				contextInfo.clear();				
				contextInfo = null;
			}

			if(recordDefinition != null)
				recordDefinition = null;

			batchResultItem = null;

			resultItemStatus = null;

			message = null;

		}
		logger.info("recordBeanList" + recordBeanList.toString());
		return recordBeanList;
	}

	/**
	 * This method is used to create physical records
	 * @param recordBean
	 * @return recordBean
	 * @throws ERSException
	 */
	public RecordBean createPhysicalFileRecord(RecordBean recordBean) throws ERSException {	

		logger.info("Entered into createPhysicalFileRecord method");	

		logger.info("recordBean : " + recordBean.toString());

		RecordFolder primaryContainer = null;

		RecordContainer secondaryContainer = null;

		FilePlanRepository fileplanRepository = null;

		List<RecordContainer> additionalContainers = null;

		RMConnection rmConnection = null;

		String recordFolderId = null;

		RMProperties rmProperties = null;

		String recordId = null;
		
		Location actualLocation = null;
		
		RecordFolderOperationsImpl folderOperationsImpl = null;

		try {

			if(null != recordBean) {

				if(null != recordBean.getRecordFolderId() && !recordBean.getRecordFolderId().equalsIgnoreCase("")) {

					if(null != recordBean.getFolRefNum() && null != recordBean.getVolRefNumber() && null != recordBean.getRecRefNo() && null != recordBean.getSecClass()) {

						if(!recordBean.getFolRefNum().equalsIgnoreCase("") && !recordBean.getVolRefNumber().equalsIgnoreCase("") && !recordBean.getRecRefNo().equalsIgnoreCase("") && !recordBean.getSecClass().equalsIgnoreCase("")) {

							rmConnection = new RMConnection();

							fileplanRepository = rmConnection.getFilePlanRepositoryByName(ResourceLoader.getMessageProperties().getProperty(ERSConstants.FILEPLAN_OBJECTSTORE));

							recordFolderId = recordBean.getRecordFolderId();
							
							folderOperationsImpl = new RecordFolderOperationsImpl();

							primaryContainer = RMFactory.RecordFolder.fetchInstance(fileplanRepository, recordFolderId, null);

							logger.info("Primary Container :" + primaryContainer.getName());

							additionalContainers = new ArrayList<RecordContainer>(1);

							additionalContainers.add(secondaryContainer);

							rmProperties = RMFactory.RMProperties.createInstance(DomainType.P8_CE);

							if(recordBean.getFnRecordType().equalsIgnoreCase(ERSConstants.RECORD_TYPE_PHYSICAL)) {

								rmProperties.putStringValue(ERSRecordProperty.DOCUMENT_TITLE, recordBean.getTitle());

								/*Location actualLocation = RMFactory.Location.getInstance(fileplanRepository, recordBean.getLocationCode());

								rmProperties.putObjectValue(ERSRecordProperty.HOME_LOCATION, actualLocation);*/
								
								actualLocation = RMFactory.Location.fetchInstance(fileplanRepository, ResourceLoader.getMessageProperties().getProperty(ERSConstants.LOCATION_IDENTIFIER), null);
								
								if(null!=actualLocation)
								{
									rmProperties.putObjectValue(ERSFolderProperty.HOME_LOCATION, actualLocation);	
									
									rmProperties.putObjectValue(ERSRecordProperty.LOCATION, actualLocation);
								}
								else
								{
									actualLocation = RMFactory.Location.createInstance(fileplanRepository, recordBean.getLocationCode());
									
									rmProperties.putObjectValue(ERSFolderProperty.HOME_LOCATION, actualLocation);
									
									rmProperties.putObjectValue(ERSRecordProperty.LOCATION, actualLocation);
								}

								rmProperties.putStringValue(ERSRecordProperty.FDR_REF_NO, recordBean.getFolRefNum());

								rmProperties.putStringValue(ERSRecordProperty.VOL_NO, recordBean.getVolRefNumber());

								rmProperties.putStringValue(ERSRecordProperty.REC_REF_NO, recordBean.getRecRefNo());

								rmProperties.putStringValue(ERSRecordProperty.RECORD_DESC, recordBean.getDescription());

								rmProperties.putStringValue(ERSRecordProperty.TAG, recordBean.getTags());

								rmProperties.putStringValue(ERSRecordProperty.CREATOR_DESIGNATION, recordBean.getCreatorDesignation());

								rmProperties.putStringValue(ERSRecordProperty.SECURITY_CLASSIFICATION, recordBean.getSecClass());

								rmProperties.putStringValue(ERSRecordProperty.OWNER_ROLE, recordBean.getOwnerRole());

								rmProperties.putStringValue(ERSRecordProperty.OWNER_OU, recordBean.getOwnerDepartment());

								rmProperties.putStringValue(ERSRecordProperty.MUKIM_NO, recordBean.getMukimNo());

								rmProperties.putStringValue(ERSRecordProperty.LOT_NO, recordBean.getLotNo());

								rmProperties.putStringValue(ERSRecordProperty.OLD_FRN, recordBean.getOldFrn());
								
								rmProperties.putStringValue(ERSRecordProperty.BAR_CODE, recordBean.getBarcode());
								
								Record newRecord = primaryContainer.declare(ERSRecordProperty.ERS_PHYSICAL_RECORD, rmProperties, null, null);

								newRecord.save(RMRefreshMode.Refresh);

								recordId = newRecord.getProperties().getGuidValue(ERSRecordProperty.OBJECT_GUID);

								logger.info("Successfully declared new physical record: " +recordId);

								recordBean.setRecordId(recordId);
								
								recordBean.setRecordCount(recordBean.getRecordCount() + 1);
								
								folderOperationsImpl.updatePropertiesToVolume(primaryContainer, null, recordBean);

							} else {

								logger.error("Record Type is not Physcical File type. Physicla file record can not be created.");
							}

						} else {

							logger.error("Folder Reference, Volume No, Record reference, Security Classfication, Record type are mandatory. Either 1 of OR all mandatory fields are blank. Record can not be created");
						}
					} else {

						logger.error("Folder Reference, Volume No, Record reference, Security Classfication, Record type are mandatory. Either 1 of OR all mandatory fields are null. Record can not be created");
					}
				} else {

					logger.error("Record Folder Id is null which is must for record creation. Record can not be created");
				}
			} else {

				logger.error("Either mandatory Record Bean OR Document Bean is null. Record can not be created.");
			}

		} catch (RMRuntimeException objException) {

			logger.error("RMRuntimeException occured in createPhysicalRecord method : " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} catch (Exception objException) {

			logger.error("Exception occured in creationPhysicalRecord method " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} finally {

			if(null!=fileplanRepository)
				fileplanRepository = null;

			if(null!=rmConnection)
				rmConnection = null;

			primaryContainer = null;

			secondaryContainer = null;

			recordFolderId = null;

			rmProperties = null;

		}

		logger.info("recordBean : " + recordBean.toString());
		return recordBean;
	}

	/**
	 * This method is used to create Archival media records
	 * @param recordBean
	 * @return recordBean
	 * @throws ERSException
	 */
	public RecordBean createArchivalMediaRecords(RecordBean recordBean) throws ERSException {

		logger.info("Entered into createArchivalMediaRecords records");

		logger.info("recordBean : " + recordBean.toString());

		RecordFolder primaryContainer = null;

		RecordContainer secondaryContainer = null;

		FilePlanRepository fileplanRepository = null;

		List<RecordContainer> additionalContainers = null;

		RMConnection rmConnection = null;

		String recordFolderId = null;

		RMProperties rmProperties = null;

		String recordId = null;

		Location actualLocation = null;

		RecordFolderOperationsImpl folderOperationsImpl = null;
		
		ERSUtil ersUtil = null;
		
		try {

			if(null != recordBean) {

				if(null != recordBean.getRecordFolderId() && !recordBean.getRecordFolderId().equalsIgnoreCase("")) {

					if(null != recordBean.getFolRefNum() && null != recordBean.getVolRefNumber() && null != recordBean.getRecRefNo() && null != recordBean.getSecClass()) {

						if(!recordBean.getFolRefNum().equalsIgnoreCase("") && !recordBean.getVolRefNumber().equalsIgnoreCase("") && !recordBean.getRecRefNo().equalsIgnoreCase("") && !recordBean.getSecClass().equalsIgnoreCase("")) {

							rmConnection = new RMConnection();
							
							ersUtil = new ERSUtil();

							fileplanRepository = rmConnection.getFilePlanRepositoryByName(ResourceLoader.getMessageProperties().getProperty(ERSConstants.FILEPLAN_OBJECTSTORE));

							recordFolderId = recordBean.getRecordFolderId();

							folderOperationsImpl = new RecordFolderOperationsImpl();

							primaryContainer = RMFactory.RecordFolder.fetchInstance(fileplanRepository, recordFolderId, null);

							logger.info("Primary Container :" + primaryContainer.getName());

							additionalContainers = new ArrayList<RecordContainer>(1);

							additionalContainers.add(secondaryContainer);

							rmProperties = RMFactory.RMProperties.createInstance(DomainType.P8_CE);

							if(recordBean.getFnRecordType().equalsIgnoreCase(ERSConstants.RECORD_TYPE_ARCHIVAL_MEDIA)) {	

								rmProperties.putStringValue(ERSRecordProperty.DOCUMENT_TITLE, recordBean.getTitle());

								/*Location actualLocation = RMFactory.Location.getInstance(fileplanRepository, recordBean.getLocationCode());

								rmProperties.putObjectValue(ERSRecordProperty.HOME_LOCATION, actualLocation);

								rmProperties.putObjectValue(ERSRecordProperty.LOCATION_CODE, actualLocation);*/

								actualLocation = RMFactory.Location.fetchInstance(fileplanRepository, ResourceLoader.getMessageProperties().getProperty(ERSConstants.LOCATION_IDENTIFIER), null);

								if(null!=actualLocation)
								{
									rmProperties.putObjectValue(ERSFolderProperty.HOME_LOCATION, actualLocation);	

									rmProperties.putObjectValue(ERSRecordProperty.LOCATION, actualLocation);
								}
								else
								{
									actualLocation = RMFactory.Location.createInstance(fileplanRepository, recordBean.getLocationCode());

									rmProperties.putObjectValue(ERSFolderProperty.HOME_LOCATION, actualLocation);

									rmProperties.putObjectValue(ERSRecordProperty.LOCATION, actualLocation);
								}

								rmProperties.putStringValue(ERSRecordProperty.FDR_REF_NO, recordBean.getFolRefNum());

								rmProperties.putStringValue(ERSRecordProperty.VOL_NO, recordBean.getVolRefNumber());

								rmProperties.putStringValue(ERSRecordProperty.REC_REF_NO, recordBean.getRecRefNo());

								rmProperties.putStringValue(ERSRecordProperty.RECORD_DESC, recordBean.getDescription());

								rmProperties.putStringValue(ERSRecordProperty.TAG, recordBean.getTags());

								rmProperties.putStringValue(ERSRecordProperty.CREATOR_DESIGNATION, recordBean.getCreatorDesignation());

								rmProperties.putStringValue(ERSRecordProperty.SECURITY_CLASSIFICATION, recordBean.getSecClass());

								rmProperties.putStringValue(ERSRecordProperty.MEDIA_TYPE, recordBean.getMediaType());

								rmProperties.putBooleanValue(ERSRecordProperty.NAS_PRESERVE_IND, recordBean.isNASPreserveReq());

								rmProperties.putStringValue(ERSRecordProperty.OWNER_ROLE, recordBean.getOwnerRole());

								rmProperties.putStringValue(ERSRecordProperty.OWNER_OU, recordBean.getOwnerDepartment());
								
								recordBean.setInventoryClassId(ResourceLoader.getMessageProperties().getProperty(ERSConstants.ARCHIVAL_MEDIA_CLASS_ID));

								ersUtil.createInventoryDocument(recordBean, recordBean.getDocumentBeanList());
								
								Record newRecord = primaryContainer.declare(ERSRecordProperty.ERS_ARCHIVAL_MEDIA, rmProperties, null, null);

								newRecord.save(RMRefreshMode.Refresh);

								recordId = newRecord.getProperties().getGuidValue(ERSRecordProperty.OBJECT_GUID);

								logger.info("Successfully declared new Archival Media Record record: " +recordId);

								recordBean.setRecordId(recordId);

								recordBean.setRecordCount(recordBean.getRecordCount() + 1);

								folderOperationsImpl.updatePropertiesToVolume(primaryContainer, null, recordBean);
								
								/*listDocumentBeans = recordBean.getDocumentBeanList();
								
								documentBeanIter = listDocumentBeans.iterator();

								while(documentBeanIter.hasNext()) {

									documentBean = documentBeanIter.next();

									documentId = documentBean.getDocumentId();

									logger.info("documentId : " + documentId);

									ersUtil.fileDocumentinROSFolder(documentId, documentBean.getFileName());								
								}*/

							} else {

								logger.error("Record Type is not Archival Media. Archival Media Record can not be created");
							}

						} else {

							logger.error("Folder Reference, Volume No, Record reference, Security Classfication, Record type are mandatory. Either 1 of OR all mandatory fields are blank. Record can not be created");
						}
					} else {

						logger.error("Folder Reference, Volume No, Record reference, Security Classfication, Record type are mandatory. Either 1 of OR all mandatory fields are null. Record can not be created");
					}
				} else {

					logger.error("Record Folder Id is null which is must for record creation. Record can not be created");
				}

			} else {

				logger.error("Either mandatory Record Bean OR Document Bean is null. Record can not be created.");
			}


		} catch (RMRuntimeException objException) {

			logger.error("RMRuntimeException occured in createPhysicalRecord method : " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} catch (Exception objException) {

			logger.error("Exception occured in creationPhysicalRecord method " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} finally {

			if(null!=fileplanRepository)
				fileplanRepository = null;

			if(null!=rmConnection)
				rmConnection = null;

			primaryContainer = null;

			secondaryContainer = null;

			recordFolderId = null;

			rmProperties = null;

		}
		logger.info("recordBean : " + recordBean.toString());
		
		return recordBean;

	}

	/**
	 * This method is used to create microfilm records
	 * @param recordBean
	 * @return recordBean
	 * @throws ERSException
	 */
	public RecordBean createMicroFilmRecords(RecordBean recordBean) throws ERSException {

		logger.info("Entered into createMicroFilmRecords records");

		logger.info("recordBean : " + recordBean.toString());

		RecordFolder primaryContainer = null;

		RecordContainer secondaryContainer = null;

		FilePlanRepository fileplanRepository = null;

		List<RecordContainer> additionalContainers = null;

		RMConnection rmConnection = null;

		String recordFolderId = null;

		RMProperties rmProperties = null;

		String recordId = null;

		Location actualLocation = null;

		RecordFolderOperationsImpl folderOperationsImpl = null;
		
		ERSUtil ersUtil = null;
		
		try {

			if(null != recordBean) {

				if(null != recordBean.getRecordFolderId() && !recordBean.getRecordFolderId().equalsIgnoreCase("")) {

					if(null != recordBean.getFolRefNum() && null != recordBean.getVolRefNumber() && null != recordBean.getRecRefNo() && null != recordBean.getSecClass()) {

						if(!recordBean.getFolRefNum().equalsIgnoreCase("") && !recordBean.getVolRefNumber().equalsIgnoreCase("") && !recordBean.getRecRefNo().equalsIgnoreCase("") && !recordBean.getSecClass().equalsIgnoreCase("")) {

							rmConnection = new RMConnection();

							fileplanRepository = rmConnection.getFilePlanRepositoryByName(ResourceLoader.getMessageProperties().getProperty(ERSConstants.FILEPLAN_OBJECTSTORE));

							recordFolderId = recordBean.getRecordFolderId();
							
							ersUtil = new ERSUtil();

							folderOperationsImpl = new RecordFolderOperationsImpl();

							primaryContainer = RMFactory.RecordFolder.fetchInstance(fileplanRepository, recordFolderId, null);

							logger.info("Primary Container :" + primaryContainer.getName());

							additionalContainers = new ArrayList<RecordContainer>(1);

							additionalContainers.add(secondaryContainer);

							rmProperties = RMFactory.RMProperties.createInstance(DomainType.P8_CE);

							if(recordBean.getFnRecordType().equalsIgnoreCase(ERSConstants.RECORD_TYPE_MICRO_FILM)) {	

								rmProperties.putStringValue(ERSRecordProperty.DOCUMENT_TITLE, recordBean.getTitle());

								/*Location actualLocation = RMFactory.Location.getInstance(fileplanRepository, recordBean.getLocationCode());

								rmProperties.putObjectValue(ERSRecordProperty.HOME_LOCATION, actualLocation);*/

								actualLocation = RMFactory.Location.fetchInstance(fileplanRepository, ResourceLoader.getMessageProperties().getProperty(ERSConstants.LOCATION_IDENTIFIER), null);

								if(null!=actualLocation)
								{
									rmProperties.putObjectValue(ERSFolderProperty.HOME_LOCATION, actualLocation);	

									rmProperties.putObjectValue(ERSRecordProperty.LOCATION, actualLocation);
								}
								else
								{
									actualLocation = RMFactory.Location.createInstance(fileplanRepository, recordBean.getLocationCode());

									rmProperties.putObjectValue(ERSFolderProperty.HOME_LOCATION, actualLocation);

									rmProperties.putObjectValue(ERSRecordProperty.LOCATION, actualLocation);
								}


								rmProperties.putStringValue(ERSRecordProperty.FDR_REF_NO, recordBean.getFolRefNum());

								rmProperties.putStringValue(ERSRecordProperty.VOL_NO, recordBean.getVolRefNumber());

								rmProperties.putStringValue(ERSRecordProperty.REC_REF_NO, recordBean.getRecRefNo());

								rmProperties.putStringValue(ERSRecordProperty.RECORD_DESC, recordBean.getDescription());

								rmProperties.putStringValue(ERSRecordProperty.TAG, recordBean.getTags());

								rmProperties.putStringValue(ERSRecordProperty.CREATOR_DESIGNATION, recordBean.getCreatorDesignation());

								rmProperties.putStringValue(ERSRecordProperty.SECURITY_CLASSIFICATION, recordBean.getSecClass());

								rmProperties.putStringValue(ERSRecordProperty.MF_FRAMENO, recordBean.getMicroFilmFrameNo());

								rmProperties.putStringValue(ERSRecordProperty.MF_ROLLNO, recordBean.getMicroFilmRollNo());

								rmProperties.putStringValue(ERSRecordProperty.OWNER_ROLE, recordBean.getOwnerRole());

								rmProperties.putStringValue(ERSRecordProperty.OWNER_OU, recordBean.getOwnerDepartment());
								
								recordBean.setInventoryClassId(ResourceLoader.getMessageProperties().getProperty(ERSConstants.MICRO_FILM_CLASS_ID));
								
								ersUtil.createInventoryDocument(recordBean, recordBean.getDocumentBeanList());

								Record newRecord = primaryContainer.declare(ERSRecordProperty.ERS_MICRO_FILM_RECORD, rmProperties, null, null);

								newRecord.save(RMRefreshMode.Refresh);

								recordId = newRecord.getProperties().getGuidValue(ERSRecordProperty.OBJECT_GUID);

								logger.info("Successfully declared new Micro Film Record record: " +recordId);

								recordBean.setRecordId(recordId);

								recordBean.setRecordCount(recordBean.getRecordCount() + 1);

								folderOperationsImpl.updatePropertiesToVolume(primaryContainer, null, recordBean);
								
								/*listDocumentBeans = recordBean.getDocumentBeanList();
								
								documentBeanIter = listDocumentBeans.iterator();

								while(documentBeanIter.hasNext()) {

									documentBean = documentBeanIter.next();

									documentId = documentBean.getDocumentId();

									logger.info("documentId : " + documentId);

									ersUtil.fileDocumentinROSFolder(documentId, documentBean.getFileName());								
								}*/

							} else {

								logger.error("Record Type is not Micro Film Record. Micro Film Record Record can not be created");
							}

						} else {

							logger.error("Folder Reference, Volume No, Record reference, Security Classfication, Record type are mandatory. Either 1 of OR all mandatory fields are blank. Record can not be created");
						}
					} else {

						logger.error("Folder Reference, Volume No, Record reference, Security Classfication, Record type are mandatory. Either 1 of OR all mandatory fields are null. Record can not be created");
					}
				} else {

					logger.error("Record Folder Id is null which is must for record creation. Record can not be created");
				}

			} else {

				logger.error("Either mandatory Record Bean OR Document Bean is null. Record can not be created.");
			}


		} catch (RMRuntimeException objException) {

			logger.error("RMRuntimeException occured in createPhysicalRecord method : " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} catch (Exception objException) {

			logger.error("Exception occured in creationPhysicalRecord method " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} finally {

			if(null!=fileplanRepository)
				fileplanRepository = null;

			if(null!=rmConnection)
				rmConnection = null;

			primaryContainer = null;

			secondaryContainer = null;

			recordFolderId = null;

			rmProperties = null;

		}

		logger.info("recordBean : " + recordBean.toString());
		
		return recordBean;

	}

	/**
	 * This method is used to create carton records
	 * @param recordBean
	 * @return recordBean
	 * @throws ERSException
	 */
	public RecordBean createCartonRecords(RecordBean recordBean) throws ERSException {

		logger.info("Entered into createCartonRecords records");

		logger.info("recordBean : " + recordBean.toString());

		RecordFolder primaryContainer = null;

		RecordContainer secondaryContainer = null;

		FilePlanRepository fileplanRepository = null;

		List<RecordContainer> additionalContainers = null;

		RMConnection rmConnection = null;

		String recordFolderId = null;

		RMProperties rmProperties = null;

		String recordId = null;

		ERSUtil ersUtil = null;

		Location actualLocation = null;

		RecordFolderOperationsImpl folderOperationsImpl = null;
		
		try {

			if(null != recordBean) {

				if(null != recordBean.getRecordFolderId() && !recordBean.getRecordFolderId().equalsIgnoreCase("")) {

					if(null != recordBean.getFolRefNum() && null != recordBean.getVolRefNumber() && null != recordBean.getRecRefNo() && null != recordBean.getSecClass()) {

						if(!recordBean.getFolRefNum().equalsIgnoreCase("") && !recordBean.getVolRefNumber().equalsIgnoreCase("") && !recordBean.getRecRefNo().equalsIgnoreCase("") && !recordBean.getSecClass().equalsIgnoreCase("")) {

							rmConnection = new RMConnection();

							ersUtil = new ERSUtil();

							fileplanRepository = rmConnection.getFilePlanRepositoryByName(ResourceLoader.getMessageProperties().getProperty(ERSConstants.FILEPLAN_OBJECTSTORE));

							recordFolderId = recordBean.getRecordFolderId();

							folderOperationsImpl = new RecordFolderOperationsImpl();

							primaryContainer = RMFactory.RecordFolder.fetchInstance(fileplanRepository, recordFolderId, null);

							logger.info("Primary Container :" + primaryContainer.getName());

							additionalContainers = new ArrayList<RecordContainer>(1);

							additionalContainers.add(secondaryContainer);

							rmProperties = RMFactory.RMProperties.createInstance(DomainType.P8_CE);

							if(recordBean.getFnRecordType().equalsIgnoreCase(ERSConstants.RECORD_TYPE_CARTON)) {	

								rmProperties.putStringValue(ERSRecordProperty.DOCUMENT_TITLE, recordBean.getTitle());

								/*Location actualLocation = RMFactory.Location.getInstance(fileplanRepository, recordBean.getLocationCode());

								rmProperties.putObjectValue(ERSRecordProperty.HOME_LOCATION, actualLocation);*/

								actualLocation = RMFactory.Location.fetchInstance(fileplanRepository, ResourceLoader.getMessageProperties().getProperty(ERSConstants.LOCATION_IDENTIFIER), null);

								if(null!=actualLocation)
								{
									rmProperties.putObjectValue(ERSFolderProperty.HOME_LOCATION, actualLocation);	

									rmProperties.putObjectValue(ERSRecordProperty.LOCATION, actualLocation);
								}
								else
								{
									actualLocation = RMFactory.Location.createInstance(fileplanRepository, recordBean.getLocationCode());

									rmProperties.putObjectValue(ERSFolderProperty.HOME_LOCATION, actualLocation);

									rmProperties.putObjectValue(ERSRecordProperty.LOCATION, actualLocation);
								}

								rmProperties.putStringValue(ERSRecordProperty.FDR_REF_NO, recordBean.getFolRefNum());

								rmProperties.putStringValue(ERSRecordProperty.VOL_NO, recordBean.getVolRefNumber());

								rmProperties.putStringValue(ERSRecordProperty.REC_REF_NO, recordBean.getRecRefNo());

								rmProperties.putStringValue(ERSRecordProperty.RECORD_DESC, recordBean.getDescription());

								rmProperties.putStringValue(ERSRecordProperty.TAG, recordBean.getTags());

								rmProperties.putStringValue(ERSRecordProperty.CREATOR_DESIGNATION, recordBean.getCreatorDesignation());

								rmProperties.putStringValue(ERSRecordProperty.SECURITY_CLASSIFICATION, recordBean.getSecClass());

								rmProperties.putStringValue(ERSRecordProperty.OWNER_ROLE, recordBean.getOwnerRole());

								rmProperties.putStringValue(ERSRecordProperty.OWNER_OU, recordBean.getOwnerDepartment());
								
								recordBean.setInventoryClassId(ResourceLoader.getMessageProperties().getProperty(ERSConstants.CARTONS_CLASS_ID));
								
								ersUtil.createInventoryDocument(recordBean, recordBean.getDocumentBeanList());

								Record newRecord = primaryContainer.declare(ERSRecordProperty.ERS_CARTON_RECORD, rmProperties, null, null);

								newRecord.save(RMRefreshMode.Refresh);

								recordId = newRecord.getProperties().getGuidValue(ERSRecordProperty.OBJECT_GUID);

								logger.info("Successfully declared new Carton Records Record record: " +recordId);

								recordBean.setRecordId(recordId);

								recordBean.setRecordCount(recordBean.getRecordCount() + 1);

								folderOperationsImpl.updatePropertiesToVolume(primaryContainer, null, recordBean);
								
							} else {

								logger.error("Record Type is not Carton Records Record. Carton Records Record can not be created");
							}

						} else {

							logger.error("Folder Reference, Volume No, Record reference, Security Classfication, Record type are mandatory. Either 1 of OR all mandatory fields are blank. Record can not be created");
						}
					} else {

						logger.error("Folder Reference, Volume No, Record reference, Security Classfication, Record type are mandatory. Either 1 of OR all mandatory fields are null. Record can not be created");
					}
				} else {

					logger.error("Record Folder Id is null which is must for record creation. Record can not be created");
				}

			} else {

				logger.error("Either mandatory Record Bean OR Document Bean is null. Record can not be created.");
			}


		} catch (RMRuntimeException objException) {

			logger.error("RMRuntimeException occured in createCartonRecords method : " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} catch (Exception objException) {

			logger.error("Exception occured in createCartonRecords method " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} finally {

			if(null!=fileplanRepository)
				fileplanRepository = null;

			if(null!=rmConnection)
				rmConnection = null;

			primaryContainer = null;

			secondaryContainer = null;

			recordFolderId = null;

			rmProperties = null;

		}

		//logger.info("recordBean : " + recordBean.toString());
		
		return recordBean;

	}

	/**
	 * This method is used to create physical records using bulk declaration services
	 * @param recordBeanList
	 * @return recordBeanList
	 * @throws ERSException
	 */
	public List<RecordBean> createPhysicalRecordsUsingBDS(List<RecordBean> recordBeanList) throws ERSException {

		logger.info("Entered into createPhysicalRecordsUsingBDS method");

		RecordDefinition recordDefinition = null;

		BatchResultItem[] results = null;

		BatchResultItem batchResultItem = null;

		ResultItemStatus resultItemStatus = null;

		String message = null;

		RecordBean recordBean = null;

		try {

			if(null != recordBeanList) {

				logger.info("recordBeanList : " + recordBeanList.toString());

				Map<String, String> contextInfo = new HashMap<String, String>();

				EncryptionUtil encryptionUtil = new EncryptionUtil();

				contextInfo.put(BDSConstants.CONTEXT_TRANSPORT_TYPE,BDSConstants.TRANSPORT_TYPE_BDP40_WSI);

				contextInfo.put(BDSConstants.CONTEXT_PROTOCOL, ResourceLoader.getMessageProperties().getProperty(ERSConstants.CONTEXT_PROTOCOL));

				contextInfo.put(BDSConstants.CONTEXT_SERVER, ResourceLoader.getMessageProperties().getProperty(ERSConstants.CONTEXT_SERVER));

				contextInfo.put(BDSConstants.CONTEXT_PORT, ResourceLoader.getMessageProperties().getProperty(ERSConstants.CONTEXT_PORT));

				contextInfo.put(BDSConstants.CONTEXT_SERVICE, ResourceLoader.getMessageProperties().getProperty(ERSConstants.CONTEXT_SERVICE));

				contextInfo.put(BDSConstants.CONTEXT_ENDPOINT, ResourceLoader.getMessageProperties().getProperty(ERSConstants.CONTEXT_ENDPOINT));

				contextInfo.put(BDSConstants.CONTEXT_BINDING, ResourceLoader.getMessageProperties().getProperty(ERSConstants.CONTEXT_BINDING));

				contextInfo.put(BDSConstants.CONTEXT_USERNAME, ResourceLoader.getMessageProperties().getProperty(ERSConstants.USER_NAME));

				contextInfo.put(BDSConstants.CONTEXT_PASSWORD, ResourceLoader.getMessageProperties().getProperty(ERSConstants.PASS_WORD));

				BulkDeclarationService bds = BulkDeclarationFactory.getBulkDeclarationService(contextInfo);

				//Location actualLocation = RMFactory.Location.getInstance(fileplanRepository, ResourceLoader.getMessageProperties().getProperty(ERSConstants.LOCATION_IDENTIFIER));

				bds.startBatch("ERSPhysicalRecordBatchOperation");			

				ERSUtil ersUtil = new ERSUtil();

				Iterator<RecordBean> recordBeanIter = recordBeanList.iterator();			

				while(recordBeanIter.hasNext()) {

					recordBean = recordBeanIter.next();

					if(null != recordBean) {

						if(null != recordBean.getRecordFolderId() && !recordBean.getRecordFolderId().equalsIgnoreCase("")) {

							if(null != recordBean.getFolRefNum() && null != recordBean.getVolRefNumber() && null != recordBean.getRecRefNo() && null != recordBean.getSecClass() && null != recordBean.getRecordType()) {

								if(!recordBean.getFolRefNum().equalsIgnoreCase("") && !recordBean.getVolRefNumber().equalsIgnoreCase("") && !recordBean.getRecRefNo().equalsIgnoreCase("") && !recordBean.getSecClass().equalsIgnoreCase("") && !recordBean.getRecordType().equalsIgnoreCase("")) {

									logger.info("Inside recordBeanIter while" + recordBean.getDescription());

									recordDefinition = ersUtil.createRecordDefinition(ERSRecordProperty.ERS_PHYSICAL_RECORD, recordBean);

									String recordId = recordDefinition.getId();

									logger.info("recordId : " + recordId);

									recordBean.setRecordId(recordId);

									bds.declareRecord("Record Declaration", recordDefinition);

									recordBeanList.add(recordBean);

								} else {

									logger.error("Folder Reference, Volume No, Record reference, Security Classfication, Record type are mandatory. Either 1 of OR all mandatory fields are blank. Record can not be created");
								}
							} else {

								logger.error("Folder Reference, Volume No, Record reference, Security Classfication, Record type are mandatory. Either 1 of OR all mandatory fields are null. Record can not be created");
							}
						} else {

							logger.error("Record Folder Id is null which is must for record creation. Record can not be created");
						}
					} else {

						logger.error("Record Bean is null which is must for record creation. Record can not be created");
					}
				}

				try
				{
					results = bds.executeBatch();
				}

				catch (BDSException bdsException) 
				{
					logger.error("BDSException: " + bdsException.getLocalizedMessage());

					results = bds.getBatchResultItems();
				}
				catch (Exception ex) {

					logger.error("Exception occured in createBulkElectronicRecords" + ex.getLocalizedMessage());
				}
				if ( results != null ) {

					for (int r = 0; r < results.length; r++) {

						batchResultItem = results[r];

						message = " Operation " + batchResultItem.getOperationIdent();

						resultItemStatus = batchResultItem.getResultStatus();

						if ( ResultItemStatus.EXECUTION_SUCCEEDED.equals(resultItemStatus) ) {

							message = message + " succeeded.";
						}
					}
				}
			} else {

				logger.error("Either mandatory Record Bean OR Document Bean is null. Record can not be created.");
			}
		} catch (RMRuntimeException objException) {

			logger.error("RMRuntimeException occured in createRecordsUsingBDS method : " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} catch (Exception objException) {

			logger.error("Exception occured in createRecordsUsingBDS method " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} finally {

			recordDefinition = null;

			batchResultItem = null;

			resultItemStatus = null;

			message = null;
		}

		return recordBeanList;		
	}

	/**
	 * This method is used to view the properties of record
	 * @param recordBean
	 * @return recordBean
	 * @throws ERSException
	 */
	public RecordBean viewRecordMetadata(RecordBean recordBean) throws ERSException {

		Record record  = null;

		String propertyName = null;

		Object propertyValue = null;

		FilePlanRepository  fileplanRepository = null;

		RMClassDescription rmClassDescription = null;

		Map<String, Object> dataMap = null;

		List<RMPropertyDescription> listRMPropertyDescriptions = null;

		Iterator<RMPropertyDescription> listIterator = null;

		String recordId = null;

		RMPropertyDescription rmPropertyDescription = null;

		RMConnection rmConnection = null;

		try {

			logger.info("Entered into viewRecordMetadata method");

			if(null != recordBean && recordBean.getRecordId() != null) {

				logger.info("recordBean : " + recordBean.toString());

				rmConnection = new RMConnection();

				fileplanRepository = rmConnection.getFilePlanRepositoryByName(ResourceLoader.getMessageProperties().getProperty(ERSConstants.FILEPLAN_OBJECTSTORE));	

				logger.info("FilePlanRepository name : " + fileplanRepository);

				dataMap = new HashMap<String, Object>();

				recordId = recordBean.getRecordId();

				record = RMFactory.Record.fetchInstance(fileplanRepository, recordId, null);

				/*PageableSet<ContentItem> contentItemSet =  record.getAssociatedContentItems();

				Iterator<ContentItem> contentItemIter = contentItemSet.iterator();

				while(contentItemIter.hasNext()) {

					ContentItem contentItem = contentItemIter.next();

					logger.info("contentItem : " + contentItem.getContentElements().size());					
				}*/

				rmClassDescription = record.getClassDescription();

				listRMPropertyDescriptions = rmClassDescription.getPropertyDescriptions();

				listIterator = listRMPropertyDescriptions.iterator();

				while(listIterator.hasNext()) {

					rmPropertyDescription = listIterator.next();

					if(!rmPropertyDescription.isSystemGenerated() && !rmPropertyDescription.isHidden()) {

						propertyName = rmPropertyDescription.getSymbolicName();

						propertyValue = record.getProperties().get(propertyName).getObjectValue();

						dataMap.put(propertyName, propertyValue);
					}
				}

				if(rmClassDescription.getName().equalsIgnoreCase(ERSRecordProperty.ERS_ELECTRONIC_RECORD)) {

					recordBean.setFolRefNum((String)dataMap.get(ERSRecordProperty.FDR_REF_NO));

					recordBean.setVolRefNumber((String)dataMap.get(ERSRecordProperty.VOL_NO));

					recordBean.setRecRefNo((String)dataMap.get(ERSRecordProperty.REC_REF_NO));

					recordBean.setRecordType((String)dataMap.get(ERSRecordProperty.RECORD_TYPE));

					recordBean.setTitle((String)dataMap.get(ERSRecordProperty.RECORD_TITLE));

					recordBean.setDescription((String)dataMap.get(ERSRecordProperty.DESCR));

					recordBean.setTags((String)dataMap.get(ERSRecordProperty.TAG));

					recordBean.setSecClass((String)dataMap.get(ERSRecordProperty.SECURITY_CLASSIFICATION));

					recordBean.setCreator((String)dataMap.get(ERSRecordProperty.CREATOR));

					recordBean.setCreatorDesignation((String)dataMap.get(ERSRecordProperty.CREATOR_DESIGNATION));

					recordBean.setEasLink((String)dataMap.get(ERSRecordProperty.EAS_LINK));

					recordBean.setOldFrn((String)dataMap.get(ERSRecordProperty.OLD_FRN));

					recordBean.setAuthor((String)dataMap.get(ERSRecordProperty.AUTHOR));

					recordBean.setAuthorOrganisation((String)dataMap.get(ERSRecordProperty.AUTHOR_OU));

					recordBean.setWrittenDate((Date)dataMap.get(ERSRecordProperty.WRITTEN_DT));

				} if(rmClassDescription.getName().equalsIgnoreCase(ERSRecordProperty.ERS_PHYSICAL_RECORD)) {

					recordBean.setFolRefNum((String)dataMap.get(ERSRecordProperty.FDR_REF_NO));

					recordBean.setVolRefNumber((String)dataMap.get(ERSRecordProperty.VOL_NO));

					recordBean.setRecRefNo((String)dataMap.get(ERSRecordProperty.REC_REF_NO));

					recordBean.setRecordType((String)dataMap.get(ERSRecordProperty.RECORD_TYPE));

					recordBean.setTitle((String)dataMap.get(ERSRecordProperty.RECORD_TITLE));

					recordBean.setDescription((String)dataMap.get(ERSRecordProperty.DESCR));

					recordBean.setTags((String)dataMap.get(ERSRecordProperty.TAG));

					recordBean.setSecClass((String)dataMap.get(ERSRecordProperty.SECURITY_CLASSIFICATION));

					recordBean.setCreator((String)dataMap.get(ERSRecordProperty.CREATOR));

					recordBean.setCreatorDesignation((String)dataMap.get(ERSRecordProperty.CREATOR_DESIGNATION));

					recordBean.setMvStatus((String)dataMap.get(ERSRecordProperty.MV_STATUS));

					recordBean.setMvLocation((String)dataMap.get(ERSRecordProperty.MV_LOC));

					recordBean.setMvDate((Date)dataMap.get(ERSRecordProperty.MV_DT));

					recordBean.setMvUser((String)dataMap.get(ERSRecordProperty.MV_USER));

					recordBean.setMukimNo((String)dataMap.get(ERSRecordProperty.MUKIM_NO));

					recordBean.setLotNo((String)dataMap.get(ERSRecordProperty.LOT_NO));					
				}
				else
				{
					logger.error("Invalid Record Class Type. Properties can not be retrived.");
				}
			}
			else
			{
				logger.error("Either mandatory RecordBean OR Record Id is null. Record properties could not be extracted.");
			}	
		} catch (RMRuntimeException objException) {

			logger.error("RMRuntimeException occured in createPhysicalRecord : " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} catch (Exception exceptionObj) {

			logger.error("Exception occured in createPhysicalRecord "+exceptionObj.getMessage(), exceptionObj);

			throw new ERSException(exceptionObj.getMessage());
		}
		finally
		{
			recordId = null;

			if(null!=record)
				record = null;

			if(null!=rmPropertyDescription)
				rmPropertyDescription = null;

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
			if(null!=listRMPropertyDescriptions)
			{
				listRMPropertyDescriptions.clear();
				listRMPropertyDescriptions = null;
			}
			if(null!=listIterator)
				listIterator = null;

			if(null!=rmConnection)
				rmConnection.closeRMConnection();
		}

		return recordBean;
	}

	public void updateRecordMetadata(RecordBean recordBean) throws ERSException {

		logger.info("Entered into createRecord method");

		try {

			if(null != recordBean && recordBean.getFnRecordType() != null) {

				logger.info("recordBean : " + recordBean.toString());

				if(recordBean.getFnRecordType().equalsIgnoreCase(ERSConstants.RECORD_TYPE_ELECTRONIC)) {	

					updateElectronicRecordMetadata(recordBean);			

				} else if(recordBean.getFnRecordType().equalsIgnoreCase(ERSConstants.RECORD_TYPE_PHYSICAL)) {

					updatePhysicalRecordMetadata(recordBean);
					
				} else if(recordBean.getFnRecordType().equalsIgnoreCase(ERSConstants.RECORD_TYPE_ARCHIVAL_MEDIA)) {

					updateArchivalMediaRecord(recordBean);

				} else if(recordBean.getFnRecordType().equalsIgnoreCase(ERSConstants.RECORD_TYPE_MICRO_FILM)) {

					updateMicroFilmRecord(recordBean);

				} else if(recordBean.getFnRecordType().equalsIgnoreCase(ERSConstants.RECORD_TYPE_CARTON)) {

					updateCartonRecord(recordBean);
				}
			} else {

				logger.error("Either mandatory Record OR Filenet Record Type is null. Record can not be updated.");
			}
		} catch (Exception objException) {

			logger.error("Exception occured in createRecord method " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());
		} 
	}

	/**
	 * This method is used to update the Electronic record meta data
	 * @param recordBean 
	 * @throws ERSException
	 */
	public void updateElectronicRecordMetadata(RecordBean recordBean) throws ERSException {

		Record record = null;

		FilePlanRepository  fileplanRepository = null;

		RMConnection rmConnection = null;

		RMProperties rmProperties = null;

		try	{

			logger.info("Enter into the updateElectronicRecordMetadata method");

			if(recordBean != null && recordBean.getRecordId() != null) {
				
				if(recordBean.getSecClass() != null) {

				logger.info("recordBean : " + recordBean.toString());

				rmConnection = new RMConnection();

				fileplanRepository = rmConnection.getFilePlanRepositoryByName(ResourceLoader.getMessageProperties().getProperty(ERSConstants.FILEPLAN_OBJECTSTORE));	

				record = RMFactory.Record.fetchInstance(fileplanRepository, recordBean.getRecordId(), null);

				rmProperties = record.getProperties();			

				rmProperties.putStringValue(ERSRecordProperty.RECORD_TITLE, recordBean.getTitle());

				rmProperties.putStringValue(ERSRecordProperty.DESCR, recordBean.getDescription());

				rmProperties.putStringValue(ERSRecordProperty.TAG, recordBean.getTags());

				rmProperties.putStringValue(ERSRecordProperty.AUTHOR, recordBean.getAuthor());

				rmProperties.putStringValue(ERSRecordProperty.AUTHOR_OU, recordBean.getAuthorOrganisation());

				rmProperties.putStringValue(ERSRecordProperty.CREATOR_DESIGNATION, recordBean.getCreatorDesignation());

				rmProperties.putBooleanValue(ERSRecordProperty.IS_DELETED, recordBean.isDeleted());				

				//Security classification should verify on Folder level before update the value on record
				rmProperties.putStringValue(ERSRecordProperty.SECURITY_CLASSIFICATION, recordBean.getSecClass());

				rmProperties.putStringValue(ERSRecordProperty.EAS_LINK, recordBean.getEasLink());

				rmProperties.putStringValue(ERSRecordProperty.OLD_FRN, recordBean.getOldFrn());

				record.save(RMRefreshMode.Refresh);
				
			} else {

				logger.error("Mandatory field secClass is null. Update Record properties can not be updated");
			}	

			} else {

				logger.error("Either Mandatory RecordBean or Record Id is null. Update Record properties can not be updated");
			}	

		} catch (RMRuntimeException objException) {

			logger.error("RMRuntimeException occured in updateElectronicRecordMetadata method : " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());
		}
		catch (Exception exceptionobj) 
		{
			logger.error("Exception occured in updateElectronicRecordMetadata method "+ exceptionobj.getMessage(),exceptionobj);

			throw new ERSException(exceptionobj.getMessage());
		}
		finally
		{
			record = null;

			if(null!=fileplanRepository)
				fileplanRepository = null;

			if(null!=rmConnection)
				rmConnection = null;

			rmProperties = null;

			logger.info("End the updateRecordMetadata method here");
		}
	}

	/**
	 * This method is used to update the physical record meta data
	 * @param recordBean 
	 * @throws ERSException
	 */
	public void updatePhysicalRecordMetadata(RecordBean recordBean) throws ERSException {

		Record record = null;

		Location location = null;

		FilePlanRepository  fileplanRepository = null;

		RMProperties rmProperties = null;

		RMConnection rmConnection = null;

		try	{

			if(recordBean != null && recordBean.getRecordId() != null) {
				
				if(recordBean.getSecClass() != null && recordBean.getLocationCode() != null) {

				logger.info("Enter into the updateRecordMetadata method");

				logger.info("recordBean : " + recordBean.toString());

				rmConnection = new RMConnection();

				fileplanRepository = rmConnection.getFilePlanRepositoryByName(ResourceLoader.getMessageProperties().getProperty(ERSConstants.FILEPLAN_OBJECTSTORE));	

				record = RMFactory.Record.fetchInstance(fileplanRepository, recordBean.getRecordId(), null);

				rmProperties = record.getProperties();			

				rmProperties.putStringValue(ERSRecordProperty.RECORD_TITLE, recordBean.getTitle());

				rmProperties.putStringValue(ERSRecordProperty.DESCR, recordBean.getDescription());

				rmProperties.putStringValue(ERSRecordProperty.TAG, recordBean.getTags());

				//rmProperties.putStringValue(ERSRecordProperty.AUTHOR, recordBean.getAuthor());

				//rmProperties.putStringValue(ERSRecordProperty.AUTHOR_OU, recordBean.getAuthorOrganisation());

				rmProperties.putStringValue(ERSRecordProperty.CREATOR_DESIGNATION, recordBean.getCreatorDesignation());

				rmProperties.putBooleanValue(ERSRecordProperty.IS_DELETED, recordBean.isDeleted());				

				//Security classification should verify on Folder level before update the value on record
				rmProperties.putStringValue(ERSRecordProperty.SECURITY_CLASSIFICATION, recordBean.getSecClass());

				rmProperties.putStringValue(ERSRecordProperty.MV_STATUS, recordBean.getMvStatus());

				rmProperties.putDateTimeValue(ERSRecordProperty.MV_DT, recordBean.getMvDate());

				rmProperties.putStringValue(ERSRecordProperty.MV_USER, recordBean.getMvUser());

				rmProperties.putStringValue(ERSRecordProperty.MUKIM_NO, recordBean.getMukimNo());

				rmProperties.putStringValue(ERSRecordProperty.LOT_NO, recordBean.getLotNo());
				
				rmProperties.putStringValue(ERSRecordProperty.OWNER_ROLE, recordBean.getOwnerRole());

				rmProperties.putStringValue(ERSRecordProperty.OWNER_OU, recordBean.getOwnerDepartment());
				
				rmProperties.putStringValue(ERSRecordProperty.OWNER_OU, recordBean.getOwnerDepartment());

				location=RMFactory.Location.fetchInstance(fileplanRepository, ResourceLoader.getMessageProperties().getProperty(ERSConstants.LOCATION_IDENTIFIER), null);

				if(null!=location)
				{
					rmProperties.putObjectValue(ERSFolderProperty.LOCATION, location);									
				}
				else
				{
					location=RMFactory.Location.createInstance(fileplanRepository, recordBean.getLocationCode());
					rmProperties.putObjectValue(ERSFolderProperty.LOCATION, location);
				}				

				record.save(RMRefreshMode.Refresh);
				
			} else {

				logger.error("Either Mandatory fields secClass or location code is null. Update Record properties can not be updated");
			}	

			} else {

				logger.error("Either Mandatory RecordBean or Record Id is null. Update Record properties can not be updated");
			}	

		} catch (RMRuntimeException objException) {

			logger.error("RMRuntimeException occured in updateRecordMetadata method : " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());
		}
		catch (Exception exceptionobj) 
		{
			logger.error("Exception occured in updatePhyRecordMetaData method "+ exceptionobj.getMessage(),exceptionobj);

			throw new ERSException(exceptionobj.getMessage());
		}
		finally
		{
			record = null;

			if(null!=fileplanRepository)
				fileplanRepository = null;

			if(null!=rmConnection)
				rmConnection = null;

			rmProperties = null;

		}
	}
	
	/**
	 * This method is used to update the archival media record meta data
	 * @param recordBean 
	 * @throws ERSException
	 */
	public void updateArchivalMediaRecord(RecordBean recordBean) throws ERSException {

		Record record = null;

		Location location = null;

		FilePlanRepository  fileplanRepository = null;

		RMProperties rmProperties = null;

		RMConnection rmConnection = null;

		try	{

			if(recordBean != null && recordBean.getRecordId() != null) {
				
				if(recordBean.getSecClass() != null && recordBean.getLocationCode() != null) {

				logger.info("Enter into the updateArchivalMediaRecord method");

				logger.info("recordBean : " + recordBean.toString());

				rmConnection = new RMConnection();

				fileplanRepository = rmConnection.getFilePlanRepositoryByName(ResourceLoader.getMessageProperties().getProperty(ERSConstants.FILEPLAN_OBJECTSTORE));	

				record = RMFactory.Record.fetchInstance(fileplanRepository, recordBean.getRecordId(), null);

				rmProperties = record.getProperties();			

				rmProperties.putStringValue(ERSRecordProperty.RECORD_TITLE, recordBean.getTitle());

				rmProperties.putStringValue(ERSRecordProperty.DESCR, recordBean.getDescription());

				rmProperties.putStringValue(ERSRecordProperty.TAG, recordBean.getTags());

				rmProperties.putStringValue(ERSRecordProperty.CREATOR_DESIGNATION, recordBean.getCreatorDesignation());

				rmProperties.putBooleanValue(ERSRecordProperty.IS_DELETED, recordBean.isDeleted());				

				//Security classification should verify on Folder level before update the value on record
				rmProperties.putStringValue(ERSRecordProperty.SECURITY_CLASSIFICATION, recordBean.getSecClass());

				rmProperties.putStringValue(ERSRecordProperty.MV_STATUS, recordBean.getMvStatus());

				rmProperties.putDateTimeValue(ERSRecordProperty.MV_DT, recordBean.getMvDate());

				rmProperties.putStringValue(ERSRecordProperty.MV_USER, recordBean.getMvUser());

				rmProperties.putBooleanValue(ERSRecordProperty.NAS_PRESERVE_IND, recordBean.isNASPreserveReq());
				
				rmProperties.putStringValue(ERSRecordProperty.OWNER_ROLE, recordBean.getOwnerRole());

				rmProperties.putStringValue(ERSRecordProperty.OWNER_OU, recordBean.getOwnerDepartment());

				location=RMFactory.Location.fetchInstance(fileplanRepository, ResourceLoader.getMessageProperties().getProperty(ERSConstants.LOCATION_IDENTIFIER), null);

				if(null!=location)
				{
					rmProperties.putObjectValue(ERSFolderProperty.LOCATION, location);									
				}
				else
				{
					location=RMFactory.Location.createInstance(fileplanRepository, recordBean.getLocationCode());
					rmProperties.putObjectValue(ERSFolderProperty.LOCATION, location);
				}				

				record.save(RMRefreshMode.Refresh);
				
			} else {

				logger.error("Either Mandatory fields secClass or location code is null. Update Record properties can not be updated");
			}	

			} else {

				logger.error("Either Mandatory RecordBean or Record Id is null. Update Record properties can not be updated");
			}	

		} catch (RMRuntimeException objException) {

			logger.error("RMRuntimeException occured in updateArchivalMediaRecord method : " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());
		}
		catch (Exception exceptionobj) 
		{
			logger.error("Exception occured in updateArchivalMediaRecord method "+ exceptionobj.getMessage(),exceptionobj);

			throw new ERSException(exceptionobj.getMessage());
		}
		finally
		{
			record = null;

			if(null!=fileplanRepository)
				fileplanRepository = null;

			if(null!=rmConnection)
				rmConnection = null;

			rmProperties = null;

		}
	}
	
	/**
	 * This method is used to update the microfilm record meta data
	 * @param recordBean 
	 * @throws ERSException
	 */
	public void updateMicroFilmRecord(RecordBean recordBean) throws ERSException {

		Record record = null;

		Location location = null;

		FilePlanRepository  fileplanRepository = null;

		RMProperties rmProperties = null;

		RMConnection rmConnection = null;

		try	{

			if(recordBean != null && recordBean.getRecordId() != null) {
				
				if(recordBean.getSecClass() != null && recordBean.getLocationCode() != null) {

				logger.info("Enter into the updateMicroFilmRecord method");

				logger.info("recordBean : " + recordBean.toString());

				rmConnection = new RMConnection();

				fileplanRepository = rmConnection.getFilePlanRepositoryByName(ResourceLoader.getMessageProperties().getProperty(ERSConstants.FILEPLAN_OBJECTSTORE));	

				record = RMFactory.Record.fetchInstance(fileplanRepository, recordBean.getRecordId(), null);

				rmProperties = record.getProperties();			

				rmProperties.putStringValue(ERSRecordProperty.RECORD_TITLE, recordBean.getTitle());

				rmProperties.putStringValue(ERSRecordProperty.DESCR, recordBean.getDescription());

				rmProperties.putStringValue(ERSRecordProperty.TAG, recordBean.getTags());

				rmProperties.putStringValue(ERSRecordProperty.CREATOR_DESIGNATION, recordBean.getCreatorDesignation());

				rmProperties.putBooleanValue(ERSRecordProperty.IS_DELETED, recordBean.isDeleted());				

				//Security classification should verify on Folder level before update the value on record
				rmProperties.putStringValue(ERSRecordProperty.SECURITY_CLASSIFICATION, recordBean.getSecClass());

				rmProperties.putStringValue(ERSRecordProperty.MV_STATUS, recordBean.getMvStatus());

				rmProperties.putDateTimeValue(ERSRecordProperty.MV_DT, recordBean.getMvDate());

				rmProperties.putStringValue(ERSRecordProperty.MV_USER, recordBean.getMvUser());

				rmProperties.putStringValue(ERSRecordProperty.MF_FRAMENO, recordBean.getMicroFilmFrameNo());

				rmProperties.putStringValue(ERSRecordProperty.MF_ROLLNO, recordBean.getMicroFilmRollNo());
				
				rmProperties.putStringValue(ERSRecordProperty.OWNER_ROLE, recordBean.getOwnerRole());

				rmProperties.putStringValue(ERSRecordProperty.OWNER_OU, recordBean.getOwnerDepartment());

				location=RMFactory.Location.fetchInstance(fileplanRepository, ResourceLoader.getMessageProperties().getProperty(ERSConstants.LOCATION_IDENTIFIER), null);

				if(null!=location)
				{
					rmProperties.putObjectValue(ERSFolderProperty.LOCATION, location);									
				}
				else
				{
					location=RMFactory.Location.createInstance(fileplanRepository, recordBean.getLocationCode());
					rmProperties.putObjectValue(ERSFolderProperty.LOCATION, location);
				}				

				record.save(RMRefreshMode.Refresh);
				
			} else {

				logger.error("Either Mandatory fields secClass or location code is null. Update Record properties can not be updated");
			}	

			} else {

				logger.error("Either Mandatory RecordBean or Record Id is null. Update Record properties can not be updated");
			}	

		} catch (RMRuntimeException objException) {

			logger.error("RMRuntimeException occured in updateMicroFilmRecord method : " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());
		}
		catch (Exception exceptionobj) 
		{
			logger.error("Exception occured in updateMicroFilmRecord method "+ exceptionobj.getMessage(),exceptionobj);

			throw new ERSException(exceptionobj.getMessage());
		}
		finally
		{
			record = null;

			if(null!=fileplanRepository)
				fileplanRepository = null;

			if(null!=rmConnection)
				rmConnection = null;

			rmProperties = null;

		}
	}
	
	/**
	 * This method is used to update the carton record meta data
	 * @param recordBean 
	 * @throws ERSException
	 */
	public void updateCartonRecord(RecordBean recordBean) throws ERSException {

		Record record = null;

		Location location = null;

		FilePlanRepository  fileplanRepository = null;

		RMProperties rmProperties = null;

		RMConnection rmConnection = null;

		try	{

			if(recordBean != null && recordBean.getRecordId() != null) {
				
				if(recordBean.getSecClass() != null && recordBean.getLocationCode() != null) {

				logger.info("Enter into the updateCartonRecord method");

				logger.info("recordBean : " + recordBean.toString());

				rmConnection = new RMConnection();

				fileplanRepository = rmConnection.getFilePlanRepositoryByName(ResourceLoader.getMessageProperties().getProperty(ERSConstants.FILEPLAN_OBJECTSTORE));	

				record = RMFactory.Record.fetchInstance(fileplanRepository, recordBean.getRecordId(), null);

				rmProperties = record.getProperties();			

				rmProperties.putStringValue(ERSRecordProperty.RECORD_TITLE, recordBean.getTitle());

				rmProperties.putStringValue(ERSRecordProperty.DESCR, recordBean.getDescription());

				rmProperties.putStringValue(ERSRecordProperty.TAG, recordBean.getTags());

				rmProperties.putStringValue(ERSRecordProperty.CREATOR_DESIGNATION, recordBean.getCreatorDesignation());

				rmProperties.putBooleanValue(ERSRecordProperty.IS_DELETED, recordBean.isDeleted());				

				//Security classification should verify on Folder level before update the value on record
				rmProperties.putStringValue(ERSRecordProperty.SECURITY_CLASSIFICATION, recordBean.getSecClass());

				rmProperties.putStringValue(ERSRecordProperty.MV_STATUS, recordBean.getMvStatus());

				rmProperties.putDateTimeValue(ERSRecordProperty.MV_DT, recordBean.getMvDate());

				rmProperties.putStringValue(ERSRecordProperty.MV_USER, recordBean.getMvUser());

				rmProperties.putStringValue(ERSRecordProperty.OWNER_ROLE, recordBean.getOwnerRole());

				rmProperties.putStringValue(ERSRecordProperty.OWNER_OU, recordBean.getOwnerDepartment());

				location=RMFactory.Location.fetchInstance(fileplanRepository, ResourceLoader.getMessageProperties().getProperty(ERSConstants.LOCATION_IDENTIFIER), null);

				if(null!=location)
				{
					rmProperties.putObjectValue(ERSFolderProperty.LOCATION, location);									
				}
				else
				{
					location=RMFactory.Location.createInstance(fileplanRepository, recordBean.getLocationCode());
					rmProperties.putObjectValue(ERSFolderProperty.LOCATION, location);
				}				

				record.save(RMRefreshMode.Refresh);
				
				} else {

					logger.error("Either Mandatory fields secClass or location code is null. Update Record properties can not be updated");
				}	

			} else {

				logger.error("Either Mandatory fields RecordBean or Record Id is null. Update Record properties can not be updated");
			}	

		} catch (RMRuntimeException objException) {

			logger.error("RMRuntimeException occured in updateCartonRecord method : " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());
		}
		catch (Exception exceptionobj) 
		{
			logger.error("Exception occured in updateCartonRecord method "+ exceptionobj.getMessage(),exceptionobj);

			throw new ERSException(exceptionobj.getMessage());
		}
		finally
		{
			record = null;

			if(null!=fileplanRepository)
				fileplanRepository = null;

			if(null!=rmConnection)
				rmConnection = null;

			rmProperties = null;
		}
	}

	/**
	 * This method is used to move the record from one record folder to another record folder
	 * @param recordBean
	 * @throws ERSException
	 */
	public void moveRecord(RecordBean recordBean) throws ERSException {

		RecordContainer sourceRecordContainer = null;

		RecordContainer destinationRecordContainer = null;

		String sourceVolumeId = null;

		String destVolumeId = null;

		FilePlanRepository fileplanRepository = null;

		String recordId = null;

		Record record = null;

		String reasonToMove = null;

		RMConnection rmConnection = null;

		try {

			logger.info("Enter the MoveRecordFolder class here");

			if(recordBean != null && recordBean.getRecordId() != null) {

				logger.info("recordBean : " + recordBean.toString());

				rmConnection = new RMConnection();

				fileplanRepository = rmConnection.getFilePlanRepositoryByName(ResourceLoader.getMessageProperties().getProperty(ERSConstants.FILEPLAN_OBJECTSTORE));

				reasonToMove = recordBean.getReasonToMove();

				recordId = recordBean.getRecordId();

				sourceVolumeId = recordBean.getSourceVolumeId();

				destVolumeId = recordBean.getDestVolumeId();

				reasonToMove = (reasonToMove == null) ? "" : reasonToMove;

				sourceVolumeId = (sourceVolumeId == null) ? "" : sourceVolumeId;

				destVolumeId = (destVolumeId == null) ? "" : destVolumeId;

				recordId = (recordId == null) ? "" : recordId;

				record = RMFactory.Record.fetchInstance(fileplanRepository, recordId, null);

				// fetch the record folder instance here
				sourceRecordContainer = RMFactory.RecordVolume.fetchInstance(fileplanRepository, sourceVolumeId, null);			

				destinationRecordContainer = RMFactory.RecordVolume.fetchInstance(fileplanRepository, destVolumeId, null);

				logger.info("sourceRecordFolder : " + sourceRecordContainer + "destinationRecordFolder : " + destinationRecordContainer);

				// move the record folder to record  folder container
				record.move(sourceRecordContainer, destinationRecordContainer, reasonToMove);

				//record.save(RMRefreshMode.Refresh);

				logger.info("End of  the MoveRecordFolder class here");

			} else {

				logger.error("Either Mandatory RecordBean or Record Id is null. Record movement can not be updated");
			}	

		} catch (RMRuntimeException objException) {

			logger.error("RMRuntimeException occured in moveRecord method : " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} catch (Exception objException) {

			logger.error("Exception occurd in moveRecord method " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} finally {

			sourceRecordContainer = null;

			destinationRecordContainer = null;

			sourceVolumeId = null;

			destVolumeId = null;

			if(null!=fileplanRepository)
				fileplanRepository = null;

			if(null!=rmConnection)
				rmConnection = null;

			recordId = null;

			record = null;

			reasonToMove = null;
		}

	}

	/**
	 * This method is used to delete the record
	 * @param recordBean
	 * @throws ERSException
	 */
	public void deleteRecord(RecordBean recordBean) throws ERSException {

		Record record = null;

		FilePlanRepository  fileplanRepository = null;

		String recordId = null;

		RMConnection rmConnection = null;

		try {

			if(recordBean != null && recordBean.getRecordId() != null) {

				logger.info("recordBean : " + recordBean.toString());

				rmConnection = new RMConnection();

				fileplanRepository = rmConnection.getFilePlanRepositoryByName(ResourceLoader.getMessageProperties().getProperty(ERSConstants.FILEPLAN_OBJECTSTORE));

				recordId = recordBean.getRecordId();

				String reasonToDelete = recordBean.getReasonToDelete();

				recordId = (recordId == null) ? "" : recordId;

				reasonToDelete = (reasonToDelete == null) ? "" : reasonToDelete;			

				record = RMFactory.Record.fetchInstance(fileplanRepository, recordId, null);

				record.delete(true, DeleteMode.ForceLogicalDelete, reasonToDelete);

				logger.info("End manually delete claass here");

			} else {

				logger.error("Either Mandatory RecordBean or Record Id is null. Record deletion can not be done");
			}	

		} catch (RMRuntimeException objException) {

			logger.error("RMRuntimeException occured in deleteRecord method : " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} catch(Exception objException) {

			logger.error("Exception occured in deleteRecord method : " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} finally {

			record = null;

			if(null!=fileplanRepository)
				fileplanRepository = null;

			if(null!=rmConnection) {
				rmConnection.closeRMConnection();
				rmConnection = null;
			}

			recordId = null;
		}

	}

	/**
	 * This method is used to link the records
	 * @param mainRecord
	 * @param subRecords
	 * @throws ERSException
	 */
	public void linkRecords(RecordBean recordBean) throws ERSException {

		logger.info("Entered into linkRecords method");

		if(recordBean != null  && recordBean.getMainRecordId() != null && recordBean.getSubRecordIds() != null) {

			logger.info("recordBean : " + recordBean.toString());

			FilePlanRepository filePlanRepository = null;

			RMConnection rmConnection = null;

			Record	parentRecord = null;

			Record	subRecord = null;

			String linkClass = null;

			RMLink newLink = null;

			RMProperties linkProps = null;

			Iterator<String> Iterator = null;

			String subRecordId = null;

			List<String> subRecords = null;

			try {

				rmConnection = new RMConnection();

				filePlanRepository = rmConnection.getFilePlanRepositoryByName(ResourceLoader.getMessageProperties().getProperty(ERSConstants.FILEPLAN_OBJECTSTORE));

				subRecords = recordBean.getSubRecordIds();

				Iterator = subRecords.iterator();

				while (Iterator.hasNext()) {

					subRecordId = (String) Iterator.next();

					subRecord = RMFactory.Record.fetchInstance(filePlanRepository, subRecordId, null);

					parentRecord = RMFactory.Record.fetchInstance(filePlanRepository, recordBean.getMainRecordId(), null);	

					logger.info("parentRecord" + parentRecord.getName());

					linkClass = RMClassName.RecordCopyLink;

					newLink = RMFactory.RMLink.createInstance(filePlanRepository, linkClass);

					linkProps = newLink.getProperties();

					linkProps.putObjectValue(RMPropertyName.Head, parentRecord);

					linkProps.putObjectValue(RMPropertyName.Tail, subRecord);

					linkProps.putStringValue(RMPropertyName.LinkName, recordBean.getLinkName());

					newLink.save(RMRefreshMode.Refresh);

					logger.info("Get Head  name here"+newLink.getHead());
				}

				logger.info("end of the in linkOnRecords() method in LinkOnRecords class");

			} catch (RMRuntimeException objException) {

				logger.error("RMRuntimeException occured in linkRecords method : " + objException.getMessage(), objException);

				throw new ERSException(objException.getMessage());

			} catch(Exception objException) {

				logger.error("Exception occured in linkRecords method : " + objException.getMessage(), objException);

				throw new ERSException(objException.getMessage());

			} finally {

				if(null!=filePlanRepository)
					filePlanRepository = null;

				if(null!=rmConnection)
					rmConnection = null;

				parentRecord  = null;

				subRecord = null;

				linkClass = null;

				newLink= null;

				linkProps = null;

				Iterator = null;

			}

		} else {

			logger.info("Either Mandatory Record Bean or Main Record or Sub Records list is null. Link Records can not be done");
		}
	}

	/**
	 * This method is used to move the record folder from one record category to another record category
	 * @param recordBean
	 * @throws ERSException
	 */
	public void moveMultipleRecords(RecordBean recordBean) throws ERSException { 

		RecordContainer sourceRecordContainer = null;

		RecordContainer destinationRecordContainer = null;

		String sourceVolumeId = null;

		String destVolumeId = null;

		FilePlanRepository fileplanRepository = null;

		List<String> recordIdsList = null;

		Record record = null;

		String recordId = null;

		String reasonToMove = null;

		RMConnection rmConnection = null;

		try {

			logger.info("Enter the MoveRecordFolder class here");

			if(recordBean != null && recordBean.getRecordIdsList() != null) {

				logger.info("recordBean : " + recordBean.toString());

				rmConnection = new RMConnection();

				fileplanRepository = rmConnection.getFilePlanRepositoryByName(ResourceLoader.getMessageProperties().getProperty(ERSConstants.FILEPLAN_OBJECTSTORE));

				reasonToMove = recordBean.getReasonToMove();

				recordIdsList = recordBean.getRecordIdsList();

				sourceVolumeId = recordBean.getSourceVolumeId();

				destVolumeId = recordBean.getDestVolumeId();

				reasonToMove = (reasonToMove == null) ? "" : reasonToMove;

				sourceVolumeId = (sourceVolumeId == null) ? "" : sourceVolumeId;

				destVolumeId = (destVolumeId == null) ? "" : destVolumeId;

				// fetch the record folder instance here
				sourceRecordContainer = RMFactory.RecordVolume.fetchInstance(fileplanRepository, sourceVolumeId, null);			

				destinationRecordContainer = RMFactory.RecordVolume.fetchInstance(fileplanRepository, destVolumeId, null);

				logger.info("sourceRecordFolder : " + sourceRecordContainer + "destinationRecordFolder : " + destinationRecordContainer);

				Iterator<String> listIter = recordIdsList.iterator();

				while(listIter.hasNext()) {

					recordId = listIter.next();

					record = RMFactory.Record.fetchInstance(fileplanRepository, recordId, null);

					// move the record folder to record folder container
					record.move(sourceRecordContainer, destinationRecordContainer, reasonToMove);

					//record.save(RMRefreshMode.Refresh);
				}

				logger.info("End of  the MoveRecordFolder class here");

			} else {

				logger.error("Either Mandatory RecordBean or Record Id is null. Record movement can not be updated");
			}	

		} catch (RMRuntimeException objException) {

			logger.error("RMRuntimeException occured in moveRecord method : " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} catch (Exception objException) {

			logger.error("Exception occurd in moveRecord method " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} finally {

			sourceRecordContainer = null;

			destinationRecordContainer = null;

			sourceVolumeId = null;

			destVolumeId = null;

			if(null!=fileplanRepository)
				fileplanRepository = null;

			if(null!=rmConnection)
				rmConnection = null;

			recordId = null;

			record = null;

			reasonToMove = null;

			if(null != recordIdsList){
				recordIdsList.clear();
				recordIdsList = null;
			}
		}

	}

}
