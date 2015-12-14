package com.ers.jarm.api.taxonomy.impl;

import java.util.Properties;

import org.apache.log4j.Logger;

import com.ers.jarm.api.bean.TaxonomyOperationsBean;
import com.ers.jarm.api.connection.RMConnection;
import com.ers.jarm.api.constants.ERSConstants;
import com.ers.jarm.api.constants.ERSTaxonomyProperty;
import com.ers.jarm.api.exception.ERSException;
import com.ers.jarm.api.resources.ResourceLoader;
import com.ibm.jarm.api.constants.DeleteMode;
import com.ibm.jarm.api.constants.DomainType;
import com.ibm.jarm.api.constants.EntityType;
import com.ibm.jarm.api.constants.RMRefreshMode;
import com.ibm.jarm.api.core.Container;
import com.ibm.jarm.api.core.FilePlan;
import com.ibm.jarm.api.core.FilePlanRepository;
import com.ibm.jarm.api.core.RMFactory;
import com.ibm.jarm.api.core.RecordCategory;
import com.ibm.jarm.api.exception.RMRuntimeException;
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
 * This class is used for taxonomy operations to 
 * create taxonomy and delete taxonomy
 * 
 * @class name TaxonomyOperationsImpl.java
 * @date  sep,2014
 * @author MITS
 * @version 1.0
 */
public class TaxonomyOperationsImpl {

	private static Logger logger = Logger.getLogger(TaxonomyOperationsImpl.class);

	static Properties messageProperties = null;

	public TaxonomyOperationsImpl() throws ERSException {

		try
		{
			ResourceLoader.loadProperties();

		}
		catch (Exception objException) 
		{

			logger.error("Exception occurred in TaxonomyOperationsImpl constructor [Properties are not loaded] : " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());
		}

	}	

	/**
	 * This method is used to create the new record category
	 * @param taxonomyOperationsBean
	 * @throws ERSException
	 */
	public TaxonomyOperationsBean createRecordCategory(TaxonomyOperationsBean taxonomyOperationsBean) throws ERSException {

		logger.info("Entered into createRecordCategory method");
		
		logger.info("taxonomyOperationsBean : " + taxonomyOperationsBean.toString());

		FilePlanRepository fileplanRepository = null;

		RMConnection rmConnectionCode = null;

		String containerType = null;

		RecordCategory recordCategory = null;

		RecordCategory newRecordCategory = null;

		RMProperties rmProperties = null;

		if(null!=taxonomyOperationsBean ) {

			String parentrecordCategoryId = taxonomyOperationsBean.getParentRecordCategoryId();

			String recordCategoryName = taxonomyOperationsBean.getRecordCategoryName();

			String recordCategoryIdentifier = taxonomyOperationsBean.getRecordCategoryIdentifier();

			String securityClassfication = taxonomyOperationsBean.getSecurityClassfication();

			String ownerRole = taxonomyOperationsBean.getOwnerRole();

			parentrecordCategoryId = (parentrecordCategoryId == null) ? "" : parentrecordCategoryId;

			recordCategoryName = (recordCategoryName == null) ? "" : recordCategoryName;

			recordCategoryIdentifier = (recordCategoryIdentifier == null) ? "" : recordCategoryIdentifier;

			securityClassfication = (securityClassfication == null) ? "" : securityClassfication;

			ownerRole = (ownerRole == null) ? "" : ownerRole;

			try {

				rmConnectionCode = new RMConnection();

				fileplanRepository = rmConnectionCode.getFilePlanRepositoryByName(ResourceLoader.getMessageProperties().getProperty(ERSConstants.FILEPLAN_OBJECTSTORE));

				Container container = RMFactory.Container.fetchInstance(fileplanRepository, EntityType.Container, parentrecordCategoryId, null);

				logger.info("container : " + container.getClassName());

				rmProperties = RMFactory.RMProperties.createInstance(DomainType.P8_CE);

				rmProperties.putStringValue(ERSTaxonomyProperty.ERS_RECORD_CATEGORY_NAME, recordCategoryName);

				rmProperties.putStringValue(ERSTaxonomyProperty.ERS_RECORD_CATEGORY_IDENTIFIER, recordCategoryIdentifier);

				rmProperties.putStringValue(ERSTaxonomyProperty.OWNER_ROLE, ownerRole);
				
				containerType = container.getClassName();

				if(containerType.equalsIgnoreCase("RecordCategory")) {

					recordCategory = RMFactory.RecordCategory.fetchInstance(fileplanRepository, parentrecordCategoryId, null);

					newRecordCategory = recordCategory.addRecordCategory("RecordCategory", rmProperties, null);

				} else if(containerType.equalsIgnoreCase("ClassificationScheme")) {

					FilePlan filePlan  = RMFactory.FilePlan.fetchInstance(fileplanRepository, parentrecordCategoryId, null);

					newRecordCategory = filePlan.addRecordCategory("RecordCategory", rmProperties, null);

				}

				newRecordCategory.save(RMRefreshMode.Refresh);
				
				String newRecordCategoryId = newRecordCategory.getProperties().getGuidValue("Id");

				taxonomyOperationsBean.setRecordCategoryId(newRecordCategoryId);
				
				logger.info("newRecordCategoryId : " + newRecordCategoryId);

			} catch (RMRuntimeException objException) {

				logger.error("RMRuntimeException occured in createRecordCategory method : " + objException.getMessage(), objException);

				throw new ERSException(objException.getMessage());

			} catch (Exception objException) {

				logger.error("Exception occured in createRecordCategory method : " + objException.getMessage(), objException);

				throw new ERSException(objException.getMessage());

			} finally {

				fileplanRepository = null;

				rmConnectionCode = null;

				containerType = null;

				recordCategory = null;

				rmProperties = null;
			}
		} else {

			logger.info("The input taxonomyOperationsBean is null. ");
		}
		
		return taxonomyOperationsBean;
	}

	/**
	 * This method is used to delete the record category
	 * @param taxonomyOperationsBean
	 * @throws ERSException
	 */
	public void deleteRecordCategory(TaxonomyOperationsBean taxonomyOperationsBean) throws ERSException {

		logger.info("Entered into deleteRecordCategory method");
		
		logger.info("taxonomyOperationsBean : " + taxonomyOperationsBean.toString());

		if(taxonomyOperationsBean != null) {

			FilePlanRepository fileplanRepository = null;

			RMConnection rmConnectionCode = null;

			String recordCategoryId = taxonomyOperationsBean.getRecordCategoryId();

			recordCategoryId = (recordCategoryId == null) ? "" : recordCategoryId;

			try {

				rmConnectionCode = new RMConnection();

				fileplanRepository = rmConnectionCode.getFilePlanRepositoryByName(ResourceLoader.getMessageProperties().getProperty(ERSConstants.FILEPLAN_OBJECTSTORE));

				RecordCategory recordCategory = RMFactory.RecordCategory.fetchInstance(fileplanRepository, recordCategoryId, null);

				recordCategory.delete(true, DeleteMode.CheckRetainMetadata, null);

			} catch (RMRuntimeException objException) {

				logger.error("RMRuntimeException occured in deleteRecordCategory method : " + objException.getMessage(), objException);

				throw new ERSException(objException.getMessage());

			} catch (Exception objException) {

				logger.error("Exception occured in deleteRecordCategory method : " + objException.getMessage(), objException);

				throw new ERSException(objException.getMessage());

			} finally {

				fileplanRepository = null;

				rmConnectionCode = null;
			}

		} else {

			logger.info("The input taxonomyOperationsBean is null");
		}
	}

	public static void main(String[] args) throws Exception {

			TaxonomyOperationsImpl operationsImpl = new TaxonomyOperationsImpl();

			TaxonomyOperationsBean taxonomyOperationsBean = new TaxonomyOperationsBean();
			
			taxonomyOperationsBean.setRecordCategoryIdentifier("Land Allocation");
			
			taxonomyOperationsBean.setRecordCategoryName("Land Allocation");
			
			taxonomyOperationsBean.setOwnerRole("ERSFNP8SVC01");

			taxonomyOperationsBean.setRecordCategoryId("{92734D5E-D29E-4695-9397-50EA8F656A6C}");

			operationsImpl.deleteRecordCategory(taxonomyOperationsBean);
		}

}
