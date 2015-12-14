package com.ers.jarm.api.pe.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.ers.jarm.api.bean.PEOperationsBean;
import com.ers.jarm.api.bean.UserDetailsBean;
import com.ers.jarm.api.bean.WorkitemBean;
import com.ers.jarm.api.connection.RMConnection;
import com.ers.jarm.api.constants.ERSConstants;
import com.ers.jarm.api.constants.ERSWorkflowProperty;
import com.ers.jarm.api.exception.ERSException;
import com.ers.jarm.api.resources.ResourceLoader;

import filenet.vw.api.VWException;
import filenet.vw.api.VWFetchType;
import filenet.vw.api.VWFieldType;
import filenet.vw.api.VWParameter;
import filenet.vw.api.VWQueue;
import filenet.vw.api.VWQueueQuery;
import filenet.vw.api.VWSession;
import filenet.vw.api.VWStepElement;

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
 * This class is used for pe Operations to implement the launch workflow, 
 * retrieve workitems, dispatch workitem
 * 
 * @class name PEOperationsImpl.java
 * @date  sep,2014
 * @author MITS
 * @version 1.0
 */
public class PEOperationsImpl {

	private static Logger logger = Logger.getLogger(PEOperationsImpl.class);

	static Properties messageProperties = null;

	/**
	 * @constructor for PEOperationsImpl class  
	 * @throws Exception
	 */
	public PEOperationsImpl() throws ERSException
	{
		try
		{
			ResourceLoader.loadProperties();

		}
		catch (Exception objException) {

			logger.error("Exception occurred in PEOperationsImpl constructor [Properties are not loaded] : " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());
		}
	}	

	/**
	 * This method is used to launch the work items all work flows
	 * @param peOperationsBean
	 * @param userBean
	 * @return peOperationsBean
	 * @throws ERSException
	 */
	public PEOperationsBean lanchWorkflow(PEOperationsBean peOperationsBean, UserDetailsBean userBean) throws VWException, ERSException {

		VWStepElement stepElement = null;

		VWSession vwSession = null;

		RMConnection rmConnection = null;

		String approvingOfficerId = null;

		String actionOfficerId = null;

		String reportingOfficerId = null;

		String requestorId = null;

		String deptDirector = null;

		String externalDeptDirector = null;

		try {

			if(null!=peOperationsBean)
			{
				logger.info("PEOperationsBean Object Received from ERS : " + peOperationsBean.toString());

				if(null!=peOperationsBean.getWorkflowName())
				{
					rmConnection = new RMConnection();

					vwSession = rmConnection.getWSIVWsession();	

					if(null!=vwSession)	{

						requestorId = peOperationsBean.getRequestorId();

						approvingOfficerId = peOperationsBean.getApprovingOfficerId();

						actionOfficerId = peOperationsBean.getActionOfficerId();

						reportingOfficerId = peOperationsBean.getReportingOfficerId();

						deptDirector = peOperationsBean.getDeptDirector();

						externalDeptDirector = peOperationsBean.getExternalDeptDirector();

						if(approvingOfficerId != null) {

							if(approvingOfficerId.contains("\\")) {

								approvingOfficerId = approvingOfficerId.split("\\")[1];

							}
						}

						if(requestorId != null) {

							if(requestorId.contains("\\")) {

								requestorId = requestorId.split("\\")[1];
							}
						}

						if(actionOfficerId != null) {

							if(actionOfficerId.contains("\\")) {

								actionOfficerId = actionOfficerId.split("\\")[1];

							}
						}

						if(reportingOfficerId != null) {

							if(reportingOfficerId.contains("\\")) {

								reportingOfficerId = reportingOfficerId.split("\\")[1];
							}
						}

						if(deptDirector != null) {

							if(deptDirector.contains("\\")) {

								deptDirector = deptDirector.split("\\")[1];
							}
						}

						if(externalDeptDirector != null) {

							if(externalDeptDirector.contains("\\")) {

								externalDeptDirector = externalDeptDirector.split("\\")[1];
							}
						}
						if(peOperationsBean.getWorkflowName().equalsIgnoreCase(ERSWorkflowProperty.WORKFLOW_TYPE_CREATE_FOLDER))
						{
							// Validate mandatory properties
							if(null!=peOperationsBean.getFolderType())
							{
								if(peOperationsBean.getFolderType().equalsIgnoreCase(ERSConstants.FOLDER_TYPE_ELECTRONIC) || peOperationsBean.getFolderType().equalsIgnoreCase(ERSConstants.FOLDER_TYPE_PHYSICAL))
								{
									stepElement = vwSession.createWorkflow(ResourceLoader.getMessageProperties().getProperty(ERSConstants.WORKFLOW_CREATE_FOLDER));

									stepElement.setParameterValue(ERSWorkflowProperty.FOLDER_TYPE, peOperationsBean.getFolderType(), true);

									stepElement.setParameterValue(ERSWorkflowProperty.REQ_ID, peOperationsBean.getReqId(), true);

									stepElement.setParameterValue(ERSWorkflowProperty.REQUESTED_BY, peOperationsBean.getRequestedBy(), true);

									stepElement.setParameterValue(ERSWorkflowProperty.FOLDER_TITLE, peOperationsBean.getFolderTitle(), true);

									stepElement.setParameterValue(ERSWorkflowProperty.REQUESTOR_ID, requestorId, true);

									if(null!=peOperationsBean.getRequestDt())
									{
										stepElement.setParameterValue(ERSWorkflowProperty.REQUEST_DATE, peOperationsBean.getRequestDt(), true);
									}							

									stepElement.setParameterValue(ERSWorkflowProperty.REQUESTOR_REMARKS, peOperationsBean.getRequestorRemarks(), true);

								}
								else
								{
									logger.info("Invalid Folder Type property value ["+peOperationsBean.getFolderType()+"]. Workflow can not be launched as it is a mandatory property.");
								}
							}
							else
							{
								logger.info("Mandatory Folder Type property is null. Workflow can not be launched as it is a mandatory property.");
							}										
						}
						else if(peOperationsBean.getWorkflowName().equalsIgnoreCase(ERSWorkflowProperty.WORKFLOW_TYPE_UPDATE_FOLDER_METADATA))
						{
							stepElement = vwSession.createWorkflow(ResourceLoader.getMessageProperties().getProperty(ERSConstants.WORKFLOW_UPDATE_FOLDER));

							stepElement.setParameterValue(ERSWorkflowProperty.REQ_ID, peOperationsBean.getReqId(), true);

							stepElement.setParameterValue(ERSWorkflowProperty.FOLDER_TITLE, peOperationsBean.getFolderTitle(), true);

							stepElement.setParameterValue(ERSWorkflowProperty.FDR_REF_NUM, peOperationsBean.getFdrRefNum(), true);

							stepElement.setParameterValue(ERSWorkflowProperty.REQUESTOR_ID, requestorId, true);

							stepElement.setParameterValue(ERSWorkflowProperty.REQUESTED_BY, peOperationsBean.getRequestedBy(), true);

							if(null!=peOperationsBean.getRequestDt())
							{
								stepElement.setParameterValue(ERSWorkflowProperty.REQUEST_DATE, peOperationsBean.getRequestDt(), true);
							}

							stepElement.setParameterValue(ERSWorkflowProperty.REQUESTOR_REMARKS, peOperationsBean.getRequestorRemarks(), true);

							//stepElement.setParameterValue(ERSWorkflowProperty.DEADLINE_DAYS, peOperationsBean.getDeadlineDays(), true);

						}						
						else if(peOperationsBean.getWorkflowName().equalsIgnoreCase(ERSWorkflowProperty.WORKFLOW_TYPE_VIEW_ORIGINAL_RECORD))
						{
							stepElement = vwSession.createWorkflow(ResourceLoader.getMessageProperties().getProperty(ERSConstants.WORKFLOW_VIEW_ORIGINAL_DOCUMENT));

							stepElement.setParameterValue(ERSWorkflowProperty.REQ_ID, peOperationsBean.getReqId(), true);

							stepElement.setParameterValue(ERSWorkflowProperty.REQUESTED_BY, peOperationsBean.getRequestedBy(), true);

							if(null!=peOperationsBean.getRequestDt())
							{
								stepElement.setParameterValue(ERSWorkflowProperty.REQUEST_DATE, peOperationsBean.getRequestDt(), true);
							}

							stepElement.setParameterValue(ERSWorkflowProperty.FDR_REF_NUM, peOperationsBean.getFdrRefNum(), true);

							stepElement.setParameterValue(ERSWorkflowProperty.VOL_NO, peOperationsBean.getVolNo(), true);

							stepElement.setParameterValue(ERSWorkflowProperty.REC_REF_NO, peOperationsBean.getRecRefNo(), true);

							stepElement.setParameterValue(ERSWorkflowProperty.REQUESTOR_REMARKS, peOperationsBean.getRequestorRemarks(), true);

							stepElement.setParameterValue(ERSWorkflowProperty.REQUESTOR_ID, peOperationsBean.getRequestorId(), true);

							stepElement.setParameterValue(ERSWorkflowProperty.APPROVING_OFFICER_ID, peOperationsBean.getApprovingOfficerId(), true);

							stepElement.setParameterValue(ERSWorkflowProperty.RECORD_TITLE, peOperationsBean.getRecordTitle(), true);
						}
						else if(peOperationsBean.getWorkflowName().equalsIgnoreCase(ERSWorkflowProperty.WORKFLOW_TYPE_BORROW_PHYSICAL_ITEM))
						{
							stepElement = vwSession.createWorkflow(ResourceLoader.getMessageProperties().getProperty(ERSConstants.WORKFLOW_BORROW_PHYSICAL_OBJECT));

							stepElement.setParameterValue(ERSWorkflowProperty.REQ_ID, peOperationsBean.getReqId(), true);

							stepElement.setParameterValue(ERSWorkflowProperty.REQUESTED_BY, peOperationsBean.getRequestedBy(), true);

							if(null!=peOperationsBean.getRequestDt())
							{
								stepElement.setParameterValue(ERSWorkflowProperty.REQUEST_DATE, peOperationsBean.getRequestDt(), true);
							}

							stepElement.setParameterValue(ERSWorkflowProperty.FDR_REF_NUM, peOperationsBean.getFdrRefNum(), true);

							stepElement.setParameterValue(ERSWorkflowProperty.VOL_NO, peOperationsBean.getVolNo(), true);

							stepElement.setParameterValue(ERSWorkflowProperty.REC_REF_NO, peOperationsBean.getRecRefNo(), true);

							stepElement.setParameterValue(ERSWorkflowProperty.REQUESTOR_REMARKS, peOperationsBean.getRequestorRemarks(), true);

							stepElement.setParameterValue(ERSWorkflowProperty.REQUESTOR_ID, requestorId, true);

							stepElement.setParameterValue(ERSWorkflowProperty.RECORD_TITLE, peOperationsBean.getRecordTitle(), true);

						} else if(peOperationsBean.getWorkflowName().equalsIgnoreCase(ERSWorkflowProperty.WORKFLOW_TYPE_DELETE_RECORD))
						{
							stepElement = vwSession.createWorkflow(ResourceLoader.getMessageProperties().getProperty(ERSConstants.WORKFLOW_DELETE_RECORD_OBJECT));

							stepElement.setParameterValue(ERSWorkflowProperty.REQ_ID, peOperationsBean.getReqId(), true);

							stepElement.setParameterValue(ERSWorkflowProperty.REQUESTED_BY, peOperationsBean.getRequestedBy(), true);

							if(null!=peOperationsBean.getRequestDt())
							{
								stepElement.setParameterValue(ERSWorkflowProperty.REQUEST_DATE, peOperationsBean.getRequestDt(), true);
							}

							stepElement.setParameterValue(ERSWorkflowProperty.FDR_REF_NUM, peOperationsBean.getFdrRefNum(), true);

							stepElement.setParameterValue(ERSWorkflowProperty.VOL_NO, peOperationsBean.getVolNo(), true);

							stepElement.setParameterValue(ERSWorkflowProperty.REC_REF_NO, peOperationsBean.getRecRefNo(), true);

							stepElement.setParameterValue(ERSWorkflowProperty.REQUESTOR_REMARKS, peOperationsBean.getRequestorRemarks(), true);

							stepElement.setParameterValue(ERSWorkflowProperty.REQUESTOR_ID, peOperationsBean.getRequestorId(), true);

							stepElement.setParameterValue(ERSWorkflowProperty.APPROVING_OFFICER_ID, peOperationsBean.getApprovingOfficerId(), true);

							stepElement.setParameterValue(ERSWorkflowProperty.RECORD_TITLE, peOperationsBean.getRecordTitle(), true);
						}
						else if(peOperationsBean.getWorkflowName().equalsIgnoreCase(ERSWorkflowProperty.WORKFLOW_TYPE_DISPOSITION_WORKFLOW))
						{
							stepElement = vwSession.createWorkflow(ResourceLoader.getMessageProperties().getProperty(ERSConstants.WORKFLOW_DISPOSITION));

							stepElement.setParameterValue(ERSWorkflowProperty.REQ_ID, peOperationsBean.getReqId(), true);

							stepElement.setParameterValue(ERSWorkflowProperty.REQUESTED_BY, peOperationsBean.getRequestedBy(), true);

							if(null!=peOperationsBean.getRequestDt())
							{
								stepElement.setParameterValue(ERSWorkflowProperty.REQUEST_DATE, peOperationsBean.getRequestDt(), true);
							}

							stepElement.setParameterValue(ERSWorkflowProperty.FDR_REF_NUM, peOperationsBean.getFdrRefNum(), true);

							stepElement.setParameterValue(ERSWorkflowProperty.VOL_NO, peOperationsBean.getVolNo(), true);

							stepElement.setParameterValue(ERSWorkflowProperty.RND_PERIOD, peOperationsBean.getRndPeriod(), true);

							stepElement.setParameterValue(ERSWorkflowProperty.CREATED_BY, peOperationsBean.getCreatedBy(), true);

							stepElement.setParameterValue(ERSWorkflowProperty.MODIFIED_BY, peOperationsBean.getModifiedBy(), true);

							stepElement.setParameterValue(ERSWorkflowProperty.AD_HOC, peOperationsBean.getAdHoc(), true);

							stepElement.setParameterValue(ERSWorkflowProperty.REQUESTOR_REMARKS, peOperationsBean.getRequestorRemarks(), true);

							stepElement.setParameterValue(ERSWorkflowProperty.REQUESTOR_ID, requestorId, true);

							stepElement.setParameterValue(ERSWorkflowProperty.APPROVING_OFFICER_ID, approvingOfficerId, true);

						} else if(peOperationsBean.getWorkflowName().equalsIgnoreCase(ERSWorkflowProperty.WORKFLOW_TYPE_RECORD_TRANSFER)) {

							stepElement = vwSession.createWorkflow(ResourceLoader.getMessageProperties().getProperty(ERSConstants.WORKFLOW_RECORD_TRANSFER));

							stepElement.setParameterValue(ERSWorkflowProperty.REQ_ID, peOperationsBean.getReqId(), true);

							stepElement.setParameterValue(ERSWorkflowProperty.REQUESTED_BY, peOperationsBean.getRequestedBy(), true);

							if(null!=peOperationsBean.getRequestDt())
							{
								stepElement.setParameterValue(ERSWorkflowProperty.REQUEST_DATE, peOperationsBean.getRequestDt(), true);
							}

							stepElement.setParameterValue(ERSWorkflowProperty.FDR_REF_NUM, peOperationsBean.getFdrRefNum(), true);

							stepElement.setParameterValue(ERSWorkflowProperty.VOL_NO, peOperationsBean.getVolNo(), true);

							stepElement.setParameterValue(ERSWorkflowProperty.REC_REF_NO, peOperationsBean.getRecRefNo(), true);

							stepElement.setParameterValue(ERSWorkflowProperty.REQUESTOR_REMARKS, peOperationsBean.getRequestorRemarks(), true);

							stepElement.setParameterValue(ERSWorkflowProperty.REQUESTOR_ID, requestorId, true);

							stepElement.setParameterValue(ERSWorkflowProperty.DEPARTMENT_DIRECTOR, deptDirector, true);

							stepElement.setParameterValue(ERSWorkflowProperty.EXTERNAL_DEPARTMENT_DIRECTOR, externalDeptDirector, true);

							stepElement.setParameterValue(ERSWorkflowProperty.IS_INTERNAL_TRANSFER, peOperationsBean.isInternalTransfer(), true);

							stepElement.setParameterValue(ERSWorkflowProperty.RECORD_TITLE, peOperationsBean.getRecordTitle(), true);
						}
						else if(peOperationsBean.getWorkflowName().equalsIgnoreCase(ERSWorkflowProperty.WORKFLOW_TYPE_FOLDER_TRANSFER))
						{
							stepElement = vwSession.createWorkflow(ResourceLoader.getMessageProperties().getProperty(ERSConstants.WORKFLOW_FOLDER_TRANSFER));

							stepElement.setParameterValue(ERSWorkflowProperty.REQ_ID, peOperationsBean.getReqId(), true);

							stepElement.setParameterValue(ERSWorkflowProperty.REQUESTED_BY, peOperationsBean.getRequestedBy(), true);

							if(null!=peOperationsBean.getRequestDt())
							{
								stepElement.setParameterValue(ERSWorkflowProperty.REQUEST_DATE, peOperationsBean.getRequestDt(), true);
							}

							stepElement.setParameterValue(ERSWorkflowProperty.FDR_REF_NUM, peOperationsBean.getFdrRefNum(), true);

							stepElement.setParameterValue(ERSWorkflowProperty.REQUESTOR_REMARKS, peOperationsBean.getRequestorRemarks(), true);

							stepElement.setParameterValue(ERSWorkflowProperty.REQUESTOR_ID, requestorId, true);

							stepElement.setParameterValue(ERSWorkflowProperty.DEPARTMENT_DIRECTOR, deptDirector, true);

							stepElement.setParameterValue(ERSWorkflowProperty.EXTERNAL_DEPARTMENT_DIRECTOR, externalDeptDirector, true);

							stepElement.setParameterValue(ERSWorkflowProperty.IS_INTERNAL_TRANSFER, peOperationsBean.isInternalTransfer(), true);

							stepElement.setParameterValue(ERSWorkflowProperty.FOLDER_TITLE, peOperationsBean.getFolderTitle(), true);
						}
						else if(peOperationsBean.getWorkflowName().equalsIgnoreCase(ERSWorkflowProperty.WORKFLOW_TYPE_CLOSE_FOLDER))
						{
							stepElement = vwSession.createWorkflow(ResourceLoader.getMessageProperties().getProperty(ERSConstants.WORKFLOW_CLOSE_FOLDER));

							stepElement.setParameterValue(ERSWorkflowProperty.REQ_ID, peOperationsBean.getReqId(), true);

							stepElement.setParameterValue(ERSWorkflowProperty.REQUESTED_BY, peOperationsBean.getRequestedBy(), true);

							if(null!=peOperationsBean.getRequestDt())
							{
								stepElement.setParameterValue(ERSWorkflowProperty.REQUEST_DATE, peOperationsBean.getRequestDt(), true);
							}

							stepElement.setParameterValue(ERSWorkflowProperty.FDR_REF_NUM, peOperationsBean.getFdrRefNum(), true);

							stepElement.setParameterValue(ERSWorkflowProperty.REQUESTOR_REMARKS, peOperationsBean.getRequestorRemarks(), true);

							stepElement.setParameterValue(ERSWorkflowProperty.ACTION_OFFICER_ID, actionOfficerId, true);

							stepElement.setParameterValue(ERSWorkflowProperty.REPORTING_OFFICER_ID, reportingOfficerId, true);

							stepElement.setParameterValue(ERSWorkflowProperty.FOLDER_TITLE, peOperationsBean.getFolderTitle(), true);
						}
						else if(peOperationsBean.getWorkflowName().equalsIgnoreCase(ERSWorkflowProperty.WORKFLOW_TYPE_DELETE_FOLDER))
						{
							stepElement = vwSession.createWorkflow(ResourceLoader.getMessageProperties().getProperty(ERSConstants.WORKFLOW_DELETE_FOLDER));

							stepElement.setParameterValue(ERSWorkflowProperty.REQ_ID, peOperationsBean.getReqId(), true);

							stepElement.setParameterValue(ERSWorkflowProperty.REQUESTED_BY, peOperationsBean.getRequestedBy(), true);

							if(null!=peOperationsBean.getRequestDt())
							{
								stepElement.setParameterValue(ERSWorkflowProperty.REQUEST_DATE, peOperationsBean.getRequestDt(), true);
							}

							stepElement.setParameterValue(ERSWorkflowProperty.FDR_REF_NUM, peOperationsBean.getFdrRefNum(), true);

							stepElement.setParameterValue(ERSWorkflowProperty.REQUESTOR_REMARKS, peOperationsBean.getRequestorRemarks(), true);

							stepElement.setParameterValue(ERSWorkflowProperty.REQUESTOR_ID, requestorId, true);

							stepElement.setParameterValue(ERSWorkflowProperty.FOLDER_TITLE, peOperationsBean.getFolderTitle(), true);
						}
						else if(peOperationsBean.getWorkflowName().equalsIgnoreCase(ERSWorkflowProperty.WORKFLOW_TYPE_FREEZE_FOLDER))
						{
							stepElement = vwSession.createWorkflow(ResourceLoader.getMessageProperties().getProperty(ERSConstants.WORKFLOW_FREEZE_FOLDER));

							stepElement.setParameterValue(ERSWorkflowProperty.REQ_ID, peOperationsBean.getReqId(), true);

							stepElement.setParameterValue(ERSWorkflowProperty.REQUESTED_BY, peOperationsBean.getRequestedBy(), true);

							if(null!=peOperationsBean.getRequestDt())
							{
								stepElement.setParameterValue(ERSWorkflowProperty.REQUEST_DATE, peOperationsBean.getRequestDt(), true);
							}

							stepElement.setParameterValue(ERSWorkflowProperty.FDR_REF_NUM, peOperationsBean.getFdrRefNum(), true);

							stepElement.setParameterValue(ERSWorkflowProperty.REQUESTOR_REMARKS, peOperationsBean.getRequestorRemarks(), true);

							stepElement.setParameterValue(ERSWorkflowProperty.REQUESTOR_ID, requestorId, true);

							stepElement.setParameterValue(ERSWorkflowProperty.FOLDER_TITLE, peOperationsBean.getFolderTitle(), true);
						}
						if(null!=stepElement)
						{
							stepElement.doDispatch();	

							logger.info("Workflow Number : "+stepElement.getWorkflowNumber());

							peOperationsBean.setWobNumber(stepElement.getWorkflowNumber());

							logger.info("Workflow is launched successfully...............!");
						}
					}
					else
					{
						logger.error("VWSession is null. Connection could not established, no workflow can be launched.");
					}					
				}
				else
				{
					logger.error("PEOperationsBean Workflow Name property [mandatory] is missing. Workflow can not be launched");
				}		

			}
			else
			{
				logger.error("PEOperationsBean object is null. No action can be performed");
			}	

		} catch (VWException exceptionObj) {

			logger.error("VWException occured in lanchWorkflow method " + exceptionObj.getMessage(), exceptionObj);

			throw new ERSException(exceptionObj.getMessage());

		} catch (Exception exceptionObj) {

			logger.error("Exception occured in lanchWorkflow method " + exceptionObj.getMessage(), exceptionObj);

			throw new ERSException(exceptionObj.getMessage());
		}

		finally
		{
			if(null!=stepElement)
				stepElement=null;

			if(rmConnection != null) {
				rmConnection.closeRMConnection();
			}

			if(null!=vwSession)
			{
				rmConnection.closeVWSession(vwSession);
			}
		}
		return peOperationsBean;
	}

	/**
	 * This method is used to retrieve the work items from the Inbox 
	 * @param peOperationsBean
	 * @param userBean
	 * @return workitemBeanList
	 * @throws ERSException
	 */
	public List<WorkitemBean> retrieveWorkItemsFromQueue(PEOperationsBean peOperationsBean, UserDetailsBean userBean) throws VWException, ERSException {

		logger.info("Entered into retrieveWorkItemsFromQueue method");

		VWSession vwSession =null;

		VWQueueQuery queuequery = null;

		VWQueue vwQueue = null;

		RMConnection rmConnection = null;

		VWStepElement stepElement = null;

		WorkitemBean workitemBean = null;

		List<WorkitemBean> workitemBeanList = null;

		VWParameter[] vwParameters = null;

		try
		{
			if(null!=peOperationsBean)
			{
				logger.info("PEOperationsBean Object Received from ERS : " + peOperationsBean.toString());

				rmConnection = new RMConnection();

				vwSession = rmConnection.getWSIVWsession();

				if(null!=vwSession)
				{
					vwQueue = vwSession.getQueue(ResourceLoader.getMessageProperties().getProperty(ERSConstants.PROCESSING_QUEUE));

					workitemBeanList = new ArrayList<WorkitemBean>();

					if(null==peOperationsBean.getReportingOfficerId() && null==peOperationsBean.getActionOfficerId())
					{						
						logger.error("Action Officer and Reporting Officer both names are null OR blank. Either of the Officer Id is mandatory for all workflow retrieval.");
					}
					else
					{	
						if(peOperationsBean.getWobNumber() != null) {

							queuequery = vwQueue.createQuery(null, null, null, VWQueue.QUERY_NO_OPTIONS 

									+ VWQueue.QUERY_READ_BOUND

									+ VWQueue.QUERY_READ_LOCKED, 

									"F_WobNum=:A", new String[]{peOperationsBean.getWobNumber()}, VWFetchType.FETCH_TYPE_STEP_ELEMENT);

						} else if(null!=peOperationsBean.getReportingOfficerId()) {

							queuequery = vwQueue.createQuery(null, null, null, VWQueue.QUERY_NO_OPTIONS 
									+ VWQueue.QUERY_READ_BOUND
									+ VWQueue.QUERY_READ_LOCKED, "ReportingOfficerId=:A and IsRequestRejected = 'N'",
									new String[]{peOperationsBean.getReportingOfficerId()}, VWFetchType.FETCH_TYPE_STEP_ELEMENT);
						}

						while (queuequery.hasNext()) {

							stepElement = (VWStepElement) queuequery.next();

							logger.info("Step element : " + stepElement.getWorkObjectNumber() + " : " + stepElement.getWorkflowNumber());

							vwParameters = stepElement.getParameters(VWFieldType.ALL_FIELD_TYPES, VWStepElement.FIELD_USER_DEFINED);

							workitemBean = new WorkitemBean();

							for (int i = 0; i < vwParameters.length; i++) {								

								//logger.info(vwParameters[i].getName() + " : " + stepElement.getParameterValue(vwParameters[i].getName()));

								if(vwParameters[i].getName().equalsIgnoreCase("ReqId")) {

									workitemBean.setReqId(stepElement.getParameterValue(vwParameters[i].getName()) + "");

								} else if(vwParameters[i].getName().equalsIgnoreCase("FdrRefNum")) {

									workitemBean.setFdrRefNum((String)stepElement.getParameterValue(vwParameters[i].getName()));

								} else if(vwParameters[i].getName().equalsIgnoreCase("RecRefNo")) {

									workitemBean.setRecRefNo(Integer.parseInt(stepElement.getParameterValue(vwParameters[i].getName()).toString()));

								} else if(vwParameters[i].getName().equalsIgnoreCase("RequestedBy")) {

									workitemBean.setRequestedBy((String)stepElement.getParameterValue(vwParameters[i].getName()));

								} else if(vwParameters[i].getName().equalsIgnoreCase("ApprovedBy")) {

									workitemBean.setApprovedBy((String)stepElement.getParameterValue(vwParameters[i].getName()));

								} else if(vwParameters[i].getName().equalsIgnoreCase("RequstorRemarks")) {

									workitemBean.setRequestorRemarks((String)stepElement.getParameterValue(vwParameters[i].getName()));

								} else if(vwParameters[i].getName().equalsIgnoreCase("ApproverRemarks")) {

									workitemBean.setApproverRemarks((String)stepElement.getParameterValue(vwParameters[i].getName()));

								} else if(vwParameters[i].getName().equalsIgnoreCase("ActionOfficerId")) {

									workitemBean.setRequestorRemarks((String)stepElement.getParameterValue(vwParameters[i].getName()));

								} else if(vwParameters[i].getName().equalsIgnoreCase("ReportingOfficerId")) {

									workitemBean.setApproverRemarks((String)stepElement.getParameterValue(vwParameters[i].getName()));
								}
							}
							workitemBeanList.add(workitemBean);	

							workitemBean=null;
						}	

						List<WorkitemBean> AOWorItemList = retrieveWorkItemsForAO(peOperationsBean, userBean);

						workitemBeanList.addAll(AOWorItemList);

						logger.info("workitemBeanList size : " + workitemBeanList.size());

						if(null!=peOperationsBean.getReportingOfficerId())
						{
							logger.info("Total workflow retrieved from Reporting Officer's ["+peOperationsBean.getReportingOfficerId()+"] Inbox.");
						}
						else
						{
							logger.info("Total workflow retrieved from Action Officer's ["+peOperationsBean.getActionOfficerId()+"] Inbox.");
						}						
					}
				}
				else
				{
					logger.error("VWSession is null. Connection could not established, no workflow can be retrieved.");
				}	
			}
			else
			{
				logger.error("PEOperationsBean object is null. No action can be performed");
			}			

		} catch (VWException exceptionObj) {

			logger.error("VWException occured in retrieveWorkItemsFromQueue method " + exceptionObj.getMessage(), exceptionObj);

			throw new ERSException(exceptionObj.getMessage());

		} catch (Exception exceptionObj) {

			logger.error("Exception occured in retrieveWorkItemsFromQueue method "+ exceptionObj.getMessage(), exceptionObj);

			throw new ERSException(exceptionObj.getMessage());
		} 
		finally
		{
			if(null!=queuequery)
				queuequery=null;

			if(null!=vwQueue)
				vwQueue=null;

			if(null!=stepElement)
				stepElement=null;

			if(rmConnection != null) {
				rmConnection.closeRMConnection();
			}

			if(null!=vwSession)
			{
				rmConnection.closeVWSession(vwSession);
			}
		}
		return workitemBeanList;
	}

	/**
	 * This method is used to retrieve the work items from the Inbox
	 * @param peOperationsBean
	 * @param userBean
	 * @return workitemBeanList
	 * @throws ERSException
	 */
	public List<WorkitemBean> retrieveWorkItemsForAO(PEOperationsBean peOperationsBean, UserDetailsBean userBean) throws VWException, ERSException {

		logger.info("Entered into retrieveWorkItemsForAO method");

		VWSession vwSession =null;

		VWQueueQuery queuequery = null;

		VWQueue vwQueue = null;

		RMConnection rmConnection = null;

		VWStepElement stepElement = null;

		WorkitemBean workitemBean = null;

		List<WorkitemBean> workitemBeanList = null;

		VWParameter[] vwParameters = null;

		try
		{
			if(null!=peOperationsBean)
			{
				logger.info("PEOperationsBean Object Received from ERS : " + peOperationsBean.toString());

				rmConnection = new RMConnection();

				vwSession = rmConnection.getWSIVWsession();

				if(null!=vwSession)
				{
					vwQueue = vwSession.getQueue(ResourceLoader.getMessageProperties().getProperty(ERSConstants.PROCESSING_QUEUE));

					workitemBeanList = new ArrayList<WorkitemBean>();

					if(null==peOperationsBean.getActionOfficerId())
					{						
						logger.error("Action Officer and Reporting Officer both names are null OR blank. Either of the Officer Id is mandatory for all workflow retrieval.");
					}
					else
					{	

						if(null!=peOperationsBean.getActionOfficerId())
						{
							queuequery = vwQueue.createQuery(null, null, null, VWQueue.QUERY_NO_OPTIONS 
									+ VWQueue.QUERY_READ_BOUND
									+ VWQueue.QUERY_READ_LOCKED, "ActionOfficerId=:A and IsRequestRejected='Y'",
									new String[]{peOperationsBean.getActionOfficerId()}, VWFetchType.FETCH_TYPE_STEP_ELEMENT);
						}

						while (queuequery.hasNext()) {

							stepElement = (VWStepElement) queuequery.next();

							logger.info("Step element : " + stepElement.getWorkObjectNumber() + " : " + stepElement.getWorkflowNumber());

							vwParameters = stepElement.getParameters(VWFieldType.ALL_FIELD_TYPES, VWStepElement.FIELD_USER_DEFINED);

							workitemBean = new WorkitemBean();

							for (int i = 0; i < vwParameters.length; i++) {								

								//logger.info(vwParameters[i].getName() + " : " + stepElement.getParameterValue(vwParameters[i].getName()));

								if(vwParameters[i].getName().equalsIgnoreCase("ReqId")) {

									workitemBean.setReqId(stepElement.getParameterValue(vwParameters[i].getName()) + "");

								} else if(vwParameters[i].getName().equalsIgnoreCase("FdrRefNum")) {

									workitemBean.setFdrRefNum((String)stepElement.getParameterValue(vwParameters[i].getName()));

								} else if(vwParameters[i].getName().equalsIgnoreCase("RecRefNo")) {

									workitemBean.setRecRefNo(Integer.parseInt(stepElement.getParameterValue(vwParameters[i].getName()).toString()));

								} else if(vwParameters[i].getName().equalsIgnoreCase("RequestedBy")) {

									workitemBean.setRequestedBy((String)stepElement.getParameterValue(vwParameters[i].getName()));

								} else if(vwParameters[i].getName().equalsIgnoreCase("RejectedBy")) {

									workitemBean.setApprovedBy((String)stepElement.getParameterValue(vwParameters[i].getName()));

								} else if(vwParameters[i].getName().equalsIgnoreCase("RequstorRemarks")) {

									workitemBean.setRequestorRemarks((String)stepElement.getParameterValue(vwParameters[i].getName()));

								} else if(vwParameters[i].getName().equalsIgnoreCase("RejectorRemarks")) {

									workitemBean.setApproverRemarks((String)stepElement.getParameterValue(vwParameters[i].getName()));

								} else if(vwParameters[i].getName().equalsIgnoreCase("ActionOfficerId")) {

									workitemBean.setRequestorRemarks((String)stepElement.getParameterValue(vwParameters[i].getName()));

								} else if(vwParameters[i].getName().equalsIgnoreCase("ReportingOfficerId")) {

									workitemBean.setApproverRemarks((String)stepElement.getParameterValue(vwParameters[i].getName()));
								}
							}
							workitemBeanList.add(workitemBean);	
							workitemBean=null;
						}	
						if(null!=peOperationsBean.getReportingOfficerId())
						{
							logger.info("Total workflow retrieved from Reporting Officer's ["+peOperationsBean.getReportingOfficerId()+"] Inbox.");
						}
						else
						{
							logger.info("Total workflow retrieved from Action Officer's ["+peOperationsBean.getActionOfficerId()+"] Inbox.");
						}						
					}
				}
				else
				{
					logger.error("VWSession is null. Connection could not established, no workflow can be retrieved.");
				}	
			}
			else
			{
				logger.error("PEOperationsBean object is null. No action can be performed");
			}			

		} catch (VWException exceptionObj) {

			logger.error("VWException occured in retrieveWorkItemsForAO method " + exceptionObj.getMessage(), exceptionObj);

			throw new ERSException(exceptionObj.getMessage());

		} catch (Exception exceptionObj) {

			logger.error("Exception occured in retrieveWorkItemsForAO method "+ exceptionObj.getMessage(), exceptionObj);

			throw new ERSException(exceptionObj.getMessage());
		} 
		finally
		{
			if(null!=queuequery)
				queuequery=null;

			if(null!=vwQueue)
				vwQueue=null;

			if(null!=stepElement)
				stepElement=null;

			if(rmConnection != null) {
				rmConnection.closeRMConnection();
			}

			if(null!=vwSession)
			{
				rmConnection.closeVWSession(vwSession);
			}
		}
		return workitemBeanList;
	}

	/**
	 * This method is used to update and dispatch the work item 
	 * @param peOperationsBean
	 * @param userBean
	 * @throws ERSException
	 */
	public void updateAndDispatchWorkitem(PEOperationsBean peOperationsBean, UserDetailsBean userBean) throws VWException, ERSException {

		logger.info("Enter the updateAndDispatchWorkitem method " );	

		VWSession vwSession =null;

		VWQueueQuery queuequery = null;

		VWQueue vwQueue = null;

		RMConnection rmConnection = null;

		VWStepElement stepElement = null;

		VWStepElement vwStepElement = null;

		try
		{
			if(null!=peOperationsBean)
			{
				logger.info("peOperationsBean : " + peOperationsBean.toString());

				rmConnection = new RMConnection();
				vwSession = rmConnection.getWSIVWsession();
				if(null!=vwSession)
				{											
					if(null != peOperationsBean)
					{	
						if(null!=peOperationsBean.getStepRespose() && null!=peOperationsBean.getWobNumber())
						{
							if(peOperationsBean.getStepRespose().equalsIgnoreCase(ERSConstants.WORKFLOW_RESPONSE_APPROVE) || peOperationsBean.getStepRespose().equalsIgnoreCase(ERSConstants.WORKFLOW_RESPONSE_REJECT) || peOperationsBean.getStepRespose().equalsIgnoreCase(ERSConstants.WORKFLOW_RESPONSE_ARCHIVE) || peOperationsBean.getStepRespose().equalsIgnoreCase(ERSConstants.WORKFLOW_RESPONSE_DISPOSE) || peOperationsBean.getStepRespose().equalsIgnoreCase(ERSConstants.WORKFLOW_RESPONSE_EXTEND_RNDPERIOD) || peOperationsBean.getStepRespose().equalsIgnoreCase(ERSConstants.WORKFLOW_RESPONSE_COMPLETE))
							{
								vwQueue = vwSession.getQueue(ResourceLoader.getMessageProperties().getProperty(ERSConstants.PROCESSING_QUEUE));

								//filter = "F_WobNum = '" + peOperationsBean.getWobNumber() + "'";

								//logger.info("filter" + filter + "vwQueue : " + vwQueue + peOperationsBean.getStepRespose());

								queuequery = vwQueue.createQuery(null, null, null, VWQueue.QUERY_NO_OPTIONS 

										+ VWQueue.QUERY_READ_BOUND

										+ VWQueue.QUERY_READ_LOCKED, 

										"F_WobNum=:A", new String[]{peOperationsBean.getWobNumber()}, VWFetchType.FETCH_TYPE_STEP_ELEMENT);

								//logger.info("queuequery : " + queuequery.fetchCount());

								while(queuequery.hasNext()) {							

									vwStepElement = (VWStepElement)queuequery.next();

									vwStepElement.doLock(true);

									if(peOperationsBean.getStepRespose() != ERSConstants.WORKFLOW_RESPONSE_COMPLETE) {

										vwStepElement.setSelectedResponse(peOperationsBean.getStepRespose());									
									}
									if(peOperationsBean.getStepRespose().equalsIgnoreCase(ERSConstants.WORKFLOW_RESPONSE_REJECT))
									{
										logger.info("Selected response" + peOperationsBean.getStepRespose() + "::" + ERSConstants.WORKFLOW_RESPONSE_REJECT);
										
										vwStepElement.setSelectedResponse(ERSConstants.WORKFLOW_RESPONSE_REJECT);
										
										vwStepElement.setParameterValue(ERSWorkflowProperty.REJECTOR_REMARKS, peOperationsBean.getRejectorRemarks(), true);

										vwStepElement.setParameterValue(ERSWorkflowProperty.REJECTED_BY, peOperationsBean.getRejectedBy(), true);

										vwStepElement.doSave(false);
									}
									else if(peOperationsBean.getStepRespose().equalsIgnoreCase(ERSConstants.WORKFLOW_RESPONSE_APPROVE))	{

										vwStepElement.setSelectedResponse(peOperationsBean.getStepRespose());
										
										vwStepElement.setParameterValue(ERSWorkflowProperty.APPROVED_BY, peOperationsBean.getApprovedBy(), true);

										vwStepElement.setParameterValue(ERSWorkflowProperty.APPROVER_REMARKS, peOperationsBean.getApproverRemarks(), true);

										vwStepElement.setParameterValue(ERSWorkflowProperty.APPROVED_DATE, peOperationsBean.getApprovedDate(), true);

										vwStepElement.doSave(false);
									}								

									vwStepElement.doDispatch();

									logger.info("Workflow is dispatched successfully !!!!");
								}
							}
							else
							{
								logger.info("Response Value is not valid. Valid Response values are [Approve, Assign To AO and Reject]. Work request can not be processed.");
							}
						}
						else
						{
							logger.info("Response  and WobNumber are mandatory field for request dispatch which is null. Work request can not be processed.");
						}					
					}
				}
				else
				{
					logger.error("VWSession is null. Connection could not established, no workflow can be updated and dispatched.");
				}	
			}
			else
			{
				logger.error("PEOperationsBean object is null. No action can be performed");
			}			

		} catch (VWException exceptionObj) {

			logger.error("VWException occured in updateAndDispatchWorkitem method " + exceptionObj.getMessage(), exceptionObj);

			throw new ERSException(exceptionObj.getMessage());

		} catch (Exception exceptionObj) {

			logger.error("Exception occured in updateAndDispatchWorkitem method "+ exceptionObj.getMessage(), exceptionObj);

			throw new ERSException(exceptionObj.getMessage());
		} 
		finally
		{
			if(null!=queuequery)
				queuequery=null;

			if(null!=vwQueue)
				vwQueue=null;

			if(null!=stepElement)
				stepElement=null;

			if(rmConnection != null) {
				rmConnection.closeRMConnection();
			}

			if(null!=vwSession)
			{
				rmConnection.closeVWSession(vwSession);
			}
		}		
	}

	/**
	 * This method is used to retrieve workitems from CRAdmin queue
	 * @return workitemBeanList
	 * @throws ERSException
	 */
	public List<WorkitemBean> retrieveWorkItemsForCRAdmin() throws ERSException {

		logger.info("Entered into retrieveWorkItemsForCRAdmin method");

		VWSession vwSession =null;

		VWQueueQuery queuequery = null;

		VWQueue vwQueue = null;

		RMConnection rmConnection = null;

		VWStepElement stepElement = null;

		WorkitemBean workitemBean = null;

		List<WorkitemBean> workitemBeanList = null;

		VWParameter[] vwParameters = null;

		try {

			rmConnection = new RMConnection();

			vwSession = rmConnection.getWSIVWsession();

			if(null!=vwSession)
			{
				vwQueue = vwSession.getQueue(ResourceLoader.getMessageProperties().getProperty(ERSConstants.CRADMIN_QUEUE));

				workitemBeanList = new ArrayList<WorkitemBean>();

				queuequery = vwQueue.createQuery(null, null, null, VWQueue.QUERY_NO_OPTIONS 
						+ VWQueue.QUERY_READ_BOUND
						+ VWQueue.QUERY_READ_LOCKED, null,null, VWFetchType.FETCH_TYPE_STEP_ELEMENT);

				while (queuequery.hasNext()) {

					stepElement = (VWStepElement) queuequery.next();

					logger.debug("Step element : " + stepElement.getWorkObjectNumber() + " : " + stepElement.getWorkflowNumber());

					vwParameters = stepElement.getParameters(VWFieldType.ALL_FIELD_TYPES, VWStepElement.FIELD_USER_DEFINED);

					workitemBean = new WorkitemBean();

					for (int i = 0; i < vwParameters.length; i++) {								

						if(vwParameters[i].getName().equalsIgnoreCase("ReqId")) {

							workitemBean.setReqId(stepElement.getParameterValue(vwParameters[i].getName()) + "");

						} else if(vwParameters[i].getName().equalsIgnoreCase("FdrRefNum")) {

							workitemBean.setFdrRefNum((String)stepElement.getParameterValue(vwParameters[i].getName()));

						} else if(vwParameters[i].getName().equalsIgnoreCase("RecRefNo")) {

							workitemBean.setRecRefNo(Integer.parseInt(stepElement.getParameterValue(vwParameters[i].getName()).toString()));

						} else if(vwParameters[i].getName().equalsIgnoreCase("RequestedBy")) {

							workitemBean.setRequestedBy((String)stepElement.getParameterValue(vwParameters[i].getName()));

						} else if(vwParameters[i].getName().equalsIgnoreCase("ApprovedBy")) {

							workitemBean.setApprovedBy((String)stepElement.getParameterValue(vwParameters[i].getName()));

						} else if(vwParameters[i].getName().equalsIgnoreCase("RequstorRemarks")) {

							workitemBean.setRequestorRemarks((String)stepElement.getParameterValue(vwParameters[i].getName()));

						} else if(vwParameters[i].getName().equalsIgnoreCase("ApproverRemarks")) {

							workitemBean.setApproverRemarks((String)stepElement.getParameterValue(vwParameters[i].getName()));

						} else if(vwParameters[i].getName().equalsIgnoreCase("RequestorId")) {

							workitemBean.setRequestorRemarks((String)stepElement.getParameterValue(vwParameters[i].getName()));

						} else if(vwParameters[i].getName().equalsIgnoreCase("ApprovingOfficerId")) {

							workitemBean.setApproverRemarks((String)stepElement.getParameterValue(vwParameters[i].getName()));
							
						} else if(vwParameters[i].getName().equalsIgnoreCase("IsRequestRejected")) {

							workitemBean.setIsRequestRejected((String)stepElement.getParameterValue(vwParameters[i].getName()));
							
						} else if(vwParameters[i].getName().equalsIgnoreCase("F_Locked")) {

							workitemBean.setWorkitemLocked((Boolean)stepElement.getParameterValue(vwParameters[i].getName()));
						}
						workitemBean.setIsCRAdminRequest("Y");
					}
					workitemBeanList.add(workitemBean);	
					workitemBean=null;
				}	
			}
			else
			{
				logger.error("VWSession is null. Connection could not established, no workflow can be retrieved.");
			}	

		} catch (VWException exceptionObj) {

			logger.error("VWException occured in retrieveWorkItemsForCRAdmin method " + exceptionObj.getMessage(), exceptionObj);

			throw new ERSException(exceptionObj.getMessage());

		} catch (Exception exceptionObj) {

			logger.error("Exception occured in retrieveWorkItemsForCRAdmin method "+ exceptionObj.getMessage(), exceptionObj);

			throw new ERSException(exceptionObj.getMessage());
		} 
		finally
		{
			if(null!=queuequery)
				queuequery=null;

			if(null!=vwQueue)
				vwQueue=null;

			if(null!=stepElement)
				stepElement=null;

			if(rmConnection != null) {
				rmConnection.closeRMConnection();
			}

			if(null!=vwSession)
			{
				rmConnection.closeVWSession(vwSession);
			}
		}
		return workitemBeanList;
	}

	/**
	 * This method is used to retrieve the work items from the Inbox for requestor
	 * @param peOperationsBean
	 * @param userBean
	 * @return workitemBeanList
	 * @throws ERSException
	 */
	public List<WorkitemBean> retrieveWorkItemsForRequestor(PEOperationsBean peOperationsBean) throws VWException, ERSException {

		logger.info("Entered into retrieveWorkItemsForRequestor method");

		VWSession vwSession =null;

		VWQueueQuery queuequery = null;

		VWQueue vwQueue = null;

		RMConnection rmConnection = null;

		VWStepElement stepElement = null;

		WorkitemBean workitemBean = null;

		List<WorkitemBean> workitemBeanList = null;

		VWParameter[] vwParameters = null;

		try
		{
			if(null!=peOperationsBean)
			{
				logger.info("PEOperationsBean Object Received from ERS : " + peOperationsBean.toString());

				rmConnection = new RMConnection();

				vwSession = rmConnection.getWSIVWsession();

				if(null!=vwSession)
				{
					vwQueue = vwSession.getQueue(ResourceLoader.getMessageProperties().getProperty(ERSConstants.PROCESSING_QUEUE));

					workitemBeanList = new ArrayList<WorkitemBean>();

					if(null==peOperationsBean.getRequestorId())
					{						
						logger.error("Requestor Id is null OR blank. Requestor Id is mandatory for all workflow retrieval.");

					} else {	

						if(null!=peOperationsBean.getRequestorId()) {

							queuequery = vwQueue.createQuery(null, null, null, VWQueue.QUERY_NO_OPTIONS 
									+ VWQueue.QUERY_READ_BOUND
									+ VWQueue.QUERY_READ_LOCKED, "RequestorId=:A",
									new String[]{peOperationsBean.getRequestorId()}, VWFetchType.FETCH_TYPE_STEP_ELEMENT);
						} 

						while (queuequery.hasNext()) {

							stepElement = (VWStepElement) queuequery.next();

							logger.debug("Step element : " + stepElement.getWorkObjectNumber() + " : " + stepElement.getWorkflowNumber());

							vwParameters = stepElement.getParameters(VWFieldType.ALL_FIELD_TYPES, VWStepElement.FIELD_USER_DEFINED);

							workitemBean = new WorkitemBean();

							for (int i = 0; i < vwParameters.length; i++) {								

								//logger.info(vwParameters[i].getName() + " : " + stepElement.getParameterValue(vwParameters[i].getName()));

								if(vwParameters[i].getName().equalsIgnoreCase("ReqId")) {

									workitemBean.setReqId(stepElement.getParameterValue(vwParameters[i].getName()) + "");

								} else if(vwParameters[i].getName().equalsIgnoreCase("FdrRefNum")) {

									workitemBean.setFdrRefNum((String)stepElement.getParameterValue(vwParameters[i].getName()));

								} else if(vwParameters[i].getName().equalsIgnoreCase("RecRefNo")) {

									workitemBean.setRecRefNo(Integer.parseInt(stepElement.getParameterValue(vwParameters[i].getName()).toString()));

								} else if(vwParameters[i].getName().equalsIgnoreCase("RequestedBy")) {

									workitemBean.setRequestedBy((String)stepElement.getParameterValue(vwParameters[i].getName()));

								} else if(vwParameters[i].getName().equalsIgnoreCase("RejectedBy")) {

									workitemBean.setRejectedBy((String)stepElement.getParameterValue(vwParameters[i].getName()));

								} else if(vwParameters[i].getName().equalsIgnoreCase("RequstorRemarks")) {

									workitemBean.setRequestorRemarks((String)stepElement.getParameterValue(vwParameters[i].getName()));

								} else if(vwParameters[i].getName().equalsIgnoreCase("RejectorRemarks")) {

									workitemBean.setRejectorRemarks((String)stepElement.getParameterValue(vwParameters[i].getName()));

								} else if(vwParameters[i].getName().equalsIgnoreCase("RequestorId")) {

									workitemBean.setRequestorId((String)stepElement.getParameterValue(vwParameters[i].getName()));

								} else if(vwParameters[i].getName().equalsIgnoreCase("IsRequestRejected")) {

									workitemBean.setIsRequestRejected((String)stepElement.getParameterValue(vwParameters[i].getName()));
								}
								
								workitemBean.setIsCRAdminRequest("N");
							}
							workitemBeanList.add(workitemBean);	
							logger.debug("workitemBean : " + workitemBean.toString());
							workitemBean=null;
						}	
						
						List<WorkitemBean> requestorWorItemList = retrieveItemsForRequestorFromQueue(peOperationsBean);

						workitemBeanList.addAll(requestorWorItemList);

						logger.info("workitemBeanList size : " + workitemBeanList.size());
					}
				}
				else
				{
					logger.error("VWSession is null. Connection could not established, no workflow can be retrieved.");
				}	
			}
			else
			{
				logger.error("PEOperationsBean object is null. No action can be performed");
			}			

		} catch (VWException exceptionObj) {

			logger.error("VWException occured in retrieveWorkItemsForRequestor method " + exceptionObj.getMessage(), exceptionObj);

			throw new ERSException(exceptionObj.getMessage());

		} catch (Exception exceptionObj) {

			logger.error("Exception occured in retrieveWorkItemsForRequestor method "+ exceptionObj.getMessage(), exceptionObj);

			throw new ERSException(exceptionObj.getMessage());
		} 
		finally
		{
			if(null!=queuequery)
				queuequery=null;

			if(null!=vwQueue)
				vwQueue=null;

			if(null!=stepElement)
				stepElement=null;

			if(rmConnection != null) {
				rmConnection.closeRMConnection();
			}

			if(null!=vwSession)
			{
				rmConnection.closeVWSession(vwSession);
			}
		}
		return workitemBeanList;
	}
	
	/**
	 * This method is used to retrieve the work items from the queue for requestor
	 * @param peOperationsBean
	 * @param userBean
	 * @return workitemBeanList
	 * @throws ERSException
	 */
	public List<WorkitemBean> retrieveItemsForRequestorFromQueue(PEOperationsBean peOperationsBean) throws VWException, ERSException {

		logger.info("Entered into retrieveItemsForRequestorFromQueue method");

		VWSession vwSession =null;

		VWQueueQuery queuequery = null;

		VWQueue vwQueue = null;

		RMConnection rmConnection = null;

		VWStepElement stepElement = null;

		WorkitemBean workitemBean = null;

		List<WorkitemBean> workitemBeanList = null;

		VWParameter[] vwParameters = null;

		try
		{
			if(null!=peOperationsBean)
			{
				logger.info("PEOperationsBean Object Received from ERS : " + peOperationsBean.toString());

				rmConnection = new RMConnection();

				vwSession = rmConnection.getWSIVWsession();

				if(null!=vwSession)
				{
					vwQueue = vwSession.getQueue(ResourceLoader.getMessageProperties().getProperty(ERSConstants.CRADMIN_QUEUE));

					workitemBeanList = new ArrayList<WorkitemBean>();

					if(null==peOperationsBean.getRequestorId())
					{						
						logger.error("Requestor Id is null OR blank. Requestor Id is mandatory for all workflow retrieval.");

					} else {	

						if(null!= peOperationsBean.getRequestorId()) {

							queuequery = vwQueue.createQuery(null, null, null, VWQueue.QUERY_NO_OPTIONS 
									+ VWQueue.QUERY_READ_BOUND
									+ VWQueue.QUERY_READ_LOCKED, "RequestorId=:A",
									new String[]{peOperationsBean.getRequestorId()}, VWFetchType.FETCH_TYPE_STEP_ELEMENT);
							
							logger.info("queuequery : " + queuequery.toString());
						} 

						while (queuequery.hasNext()) {

							stepElement = (VWStepElement) queuequery.next();

							logger.debug("Step element : " + stepElement.getWorkObjectNumber() + " : " + stepElement.getWorkflowNumber());

							vwParameters = stepElement.getParameters(VWFieldType.ALL_FIELD_TYPES, VWStepElement.FIELD_USER_DEFINED);

							workitemBean = new WorkitemBean();

							for (int i = 0; i < vwParameters.length; i++) {								

								//logger.info(vwParameters[i].getName() + " : " + stepElement.getParameterValue(vwParameters[i].getName()));

								if(vwParameters[i].getName().equalsIgnoreCase("ReqId")) {

									workitemBean.setReqId(stepElement.getParameterValue(vwParameters[i].getName()) + "");

								} else if(vwParameters[i].getName().equalsIgnoreCase("FdrRefNum")) {

									workitemBean.setFdrRefNum((String)stepElement.getParameterValue(vwParameters[i].getName()));

								} else if(vwParameters[i].getName().equalsIgnoreCase("RecRefNo")) {

									workitemBean.setRecRefNo(Integer.parseInt(stepElement.getParameterValue(vwParameters[i].getName()).toString()));

								} else if(vwParameters[i].getName().equalsIgnoreCase("RequestedBy")) {

									workitemBean.setRequestedBy((String)stepElement.getParameterValue(vwParameters[i].getName()));

								} else if(vwParameters[i].getName().equalsIgnoreCase("RejectedBy")) {

									workitemBean.setRejectedBy((String)stepElement.getParameterValue(vwParameters[i].getName()));

								} else if(vwParameters[i].getName().equalsIgnoreCase("RequstorRemarks")) {

									workitemBean.setRequestorRemarks((String)stepElement.getParameterValue(vwParameters[i].getName()));

								} else if(vwParameters[i].getName().equalsIgnoreCase("RejectorRemarks")) {

									workitemBean.setRejectorRemarks((String)stepElement.getParameterValue(vwParameters[i].getName()));

								} else if(vwParameters[i].getName().equalsIgnoreCase("RequestorId")) {

									workitemBean.setRequestorId((String)stepElement.getParameterValue(vwParameters[i].getName()));

								} else if(vwParameters[i].getName().equalsIgnoreCase("IsRequestRejected")) {

									workitemBean.setIsRequestRejected((String)stepElement.getParameterValue(vwParameters[i].getName()));
									
								} else if(vwParameters[i].getName().equalsIgnoreCase("F_Locked")) {

									workitemBean.setWorkitemLocked((Boolean)stepElement.getParameterValue(vwParameters[i].getName()));
								}
								
								workitemBean.setIsCRAdminRequest("N");
							}
							workitemBeanList.add(workitemBean);	
							workitemBean=null;
						}	
					}
				}
				else
				{
					logger.error("VWSession is null. Connection could not established, no workflow can be retrieved.");
				}	
			}
			else
			{
				logger.error("PEOperationsBean object is null. No action can be performed");
			}			

		} catch (VWException exceptionObj) {

			logger.error("VWException occured in retrieveItemsForRequestorFromQueue method " + exceptionObj.getMessage(), exceptionObj);

			throw new ERSException(exceptionObj.getMessage());

		} catch (Exception exceptionObj) {

			logger.error("Exception occured in retrieveItemsForRequestorFromQueue method "+ exceptionObj.getMessage(), exceptionObj);

			throw new ERSException(exceptionObj.getMessage());
		} 
		finally
		{
			if(null!=queuequery)
				queuequery=null;

			if(null!=vwQueue)
				vwQueue=null;

			if(null!=stepElement)
				stepElement=null;

			if(rmConnection != null) {
				rmConnection.closeRMConnection();
			}

			if(null!=vwSession)
			{
				rmConnection.closeVWSession(vwSession);
			}
		}
		return workitemBeanList;
	}
	
	/**
	 * This method is used to retrieve the work items from the Inbox for RO and Director
	 * @param peOperationsBean
	 * @return workitemBeanList
	 * @throws ERSException
	 */
	public List<WorkitemBean> retrieveWorkItemsForDirectorORRO(PEOperationsBean peOperationsBean) throws VWException, ERSException {

		logger.info("Entered into retrieveWorkItemsForDirectorORRO method");

		VWSession vwSession =null;

		VWQueueQuery queuequery = null;

		VWQueue vwQueue = null;

		RMConnection rmConnection = null;

		VWStepElement stepElement = null;

		WorkitemBean workitemBean = null;

		List<WorkitemBean> workitemBeanList = null;

		VWParameter[] vwParameters = null;

		try
		{
			if(null!=peOperationsBean)
			{
				logger.info("PEOperationsBean Object Received from ERS : " + peOperationsBean.toString());

				rmConnection = new RMConnection();

				vwSession = rmConnection.getWSIVWsession();

				if(null!=vwSession)
				{
					vwQueue = vwSession.getQueue(ResourceLoader.getMessageProperties().getProperty(ERSConstants.PROCESSING_QUEUE));

					workitemBeanList = new ArrayList<WorkitemBean>();

					if(null==peOperationsBean.getApprovingOfficerId())
					{						
						logger.error("Approving Officer Id is null OR blank. Approving Officer Id is mandatory for all workflow retrieval.");

					} else {	

						if(null!=peOperationsBean.getRequestorId()) {

							queuequery = vwQueue.createQuery(null, null, null, VWQueue.QUERY_NO_OPTIONS 
									+ VWQueue.QUERY_READ_BOUND
									+ VWQueue.QUERY_READ_LOCKED, "ApprovingOfficerId=:A",
									new String[]{peOperationsBean.getApprovingOfficerId()}, VWFetchType.FETCH_TYPE_STEP_ELEMENT);
						} 

						while (queuequery.hasNext()) {

							stepElement = (VWStepElement) queuequery.next();

							logger.debug("Step element : " + stepElement.getWorkObjectNumber() + " : " + stepElement.getWorkflowNumber());

							vwParameters = stepElement.getParameters(VWFieldType.ALL_FIELD_TYPES, VWStepElement.FIELD_USER_DEFINED);

							workitemBean = new WorkitemBean();

							for (int i = 0; i < vwParameters.length; i++) {								

								//logger.info(vwParameters[i].getName() + " : " + stepElement.getParameterValue(vwParameters[i].getName()));

								if(vwParameters[i].getName().equalsIgnoreCase("ReqId")) {

									workitemBean.setReqId(stepElement.getParameterValue(vwParameters[i].getName()) + "");

								} else if(vwParameters[i].getName().equalsIgnoreCase("FdrRefNum")) {

									workitemBean.setFdrRefNum((String)stepElement.getParameterValue(vwParameters[i].getName()));

								} else if(vwParameters[i].getName().equalsIgnoreCase("RecRefNo")) {

									workitemBean.setRecRefNo(Integer.parseInt(stepElement.getParameterValue(vwParameters[i].getName()).toString()));

								} else if(vwParameters[i].getName().equalsIgnoreCase("RequestedBy")) {

									workitemBean.setRequestedBy((String)stepElement.getParameterValue(vwParameters[i].getName()));

								} else if(vwParameters[i].getName().equalsIgnoreCase("RejectedBy")) {

									workitemBean.setRejectedBy((String)stepElement.getParameterValue(vwParameters[i].getName()));

								} else if(vwParameters[i].getName().equalsIgnoreCase("RequstorRemarks")) {

									workitemBean.setRequestorRemarks((String)stepElement.getParameterValue(vwParameters[i].getName()));

								} else if(vwParameters[i].getName().equalsIgnoreCase("RejectorRemarks")) {

									workitemBean.setRejectorRemarks((String)stepElement.getParameterValue(vwParameters[i].getName()));

								} else if(vwParameters[i].getName().equalsIgnoreCase("RequestorId")) {

									workitemBean.setRequestorId((String)stepElement.getParameterValue(vwParameters[i].getName()));
									
								} else if(vwParameters[i].getName().equalsIgnoreCase("ApprovingOfficerId")) {

									workitemBean.setApprovingOfficerId((String)stepElement.getParameterValue(vwParameters[i].getName()));

								} else if(vwParameters[i].getName().equalsIgnoreCase("IsRequestRejected")) {

									workitemBean.setIsRequestRejected((String)stepElement.getParameterValue(vwParameters[i].getName()));
								}
								
								workitemBean.setIsCRAdminRequest("D");
							}
							workitemBeanList.add(workitemBean);	
							workitemBean=null;
						}	
					}
					
					List<WorkitemBean> AOWorItemList = retrieveWorkItemsForRequestor(peOperationsBean);

					workitemBeanList.addAll(AOWorItemList);
				}
				else
				{
					logger.error("VWSession is null. Connection could not established, no workflow can be retrieved.");
				}	
			}
			else
			{
				logger.error("PEOperationsBean object is null. No action can be performed");
			}			

		} catch (VWException exceptionObj) {

			logger.error("VWException occured in retrieveWorkItemsForDirectorORRO method " + exceptionObj.getMessage(), exceptionObj);

			throw new ERSException(exceptionObj.getMessage());

		} catch (Exception exceptionObj) {

			logger.error("Exception occured in retrieveWorkItemsForDirectorORRO method "+ exceptionObj.getMessage(), exceptionObj);

			throw new ERSException(exceptionObj.getMessage());
		} 
		finally
		{
			if(null!=queuequery)
				queuequery=null;

			if(null!=vwQueue)
				vwQueue=null;

			if(null!=stepElement)
				stepElement=null;

			if(rmConnection != null) {
				rmConnection.closeRMConnection();
			}

			if(null!=vwSession)
			{
				rmConnection.closeVWSession(vwSession);
			}
		}
		return workitemBeanList;
	}

	/**
	 * This method is used to retrieve the work item based on wobnumber
	 * @param peOperationsBean
	 * @throws ERSException
	 */
	public List<WorkitemBean> retrieveWorkitemUsingWobnumber(PEOperationsBean peOperationsBean) throws VWException, ERSException {

		logger.info("Entered into retrieveWorkitemUsingWobnumber method");

		VWSession vwSession =null;

		VWQueueQuery queuequery = null;

		VWQueue vwQueue = null;

		RMConnection rmConnection = null;

		VWStepElement stepElement = null;

		WorkitemBean workitemBean = null;

		List<WorkitemBean> workitemBeanList = null;

		VWParameter[] vwParameters = null;

		try
		{
			if(null!=peOperationsBean)
			{
				logger.info("PEOperationsBean Object Received from ERS : " + peOperationsBean.toString());

				rmConnection = new RMConnection();

				vwSession = rmConnection.getWSIVWsession();

				if(null!=vwSession)
				{
					workitemBeanList = new ArrayList<WorkitemBean>();

					if(null==peOperationsBean.getWobNumber() && peOperationsBean.getWorkflowName() == null) {						

						logger.error("WobNumber or Workflow name values are null OR blank. WobNumber or Workflow name are mandatory for workitem retrieval.");

					} else {	

						vwQueue = vwSession.getQueue(ResourceLoader.getMessageProperties().getProperty(ERSConstants.CRADMIN_QUEUE));

						if(null!=peOperationsBean.getWobNumber()) {

							queuequery = vwQueue.createQuery(null, null, null, VWQueue.QUERY_NO_OPTIONS 
									+ VWQueue.QUERY_READ_BOUND
									+ VWQueue.QUERY_READ_LOCKED, 
									"F_WobNum=:A", new String[]{peOperationsBean.getWobNumber()}, VWFetchType.FETCH_TYPE_STEP_ELEMENT);
						} 

						if(queuequery.fetchCount() > 0) {

							while (queuequery.hasNext()) {

								stepElement = (VWStepElement) queuequery.next();

								logger.debug("Step element : " + stepElement.getWorkObjectNumber() + " : " + stepElement.getWorkflowNumber());

								vwParameters = stepElement.getParameters(VWFieldType.ALL_FIELD_TYPES, VWStepElement.FIELD_USER_DEFINED);

								workitemBean = new WorkitemBean();

								for (int i = 0; i < vwParameters.length; i++) {								

									//logger.info(vwParameters[i].getName() + " : " + stepElement.getParameterValue(vwParameters[i].getName()));

									if(vwParameters[i].getName().equalsIgnoreCase("ReqId")) {

										workitemBean.setReqId(stepElement.getParameterValue(vwParameters[i].getName()) + "");

									} else if(vwParameters[i].getName().equalsIgnoreCase("FdrRefNum")) {

										workitemBean.setFdrRefNum((String)stepElement.getParameterValue(vwParameters[i].getName()));

									} else if(vwParameters[i].getName().equalsIgnoreCase("RecRefNo")) {

										workitemBean.setRecRefNo(Integer.parseInt(stepElement.getParameterValue(vwParameters[i].getName()).toString()));

									} else if(vwParameters[i].getName().equalsIgnoreCase("RequestedBy")) {

										workitemBean.setRequestedBy((String)stepElement.getParameterValue(vwParameters[i].getName()));

									} else if(vwParameters[i].getName().equalsIgnoreCase("RejectedBy")) {

										workitemBean.setRejectedBy((String)stepElement.getParameterValue(vwParameters[i].getName()));

									} else if(vwParameters[i].getName().equalsIgnoreCase("RequstorRemarks")) {

										workitemBean.setRequestorRemarks((String)stepElement.getParameterValue(vwParameters[i].getName()));

									} else if(vwParameters[i].getName().equalsIgnoreCase("RejectorRemarks")) {

										workitemBean.setRejectorRemarks((String)stepElement.getParameterValue(vwParameters[i].getName()));

									} else if(vwParameters[i].getName().equalsIgnoreCase("RequestorId")) {

										workitemBean.setRequestorId((String)stepElement.getParameterValue(vwParameters[i].getName()));

									} else if(vwParameters[i].getName().equalsIgnoreCase("ApprovingOfficerId")) {

										workitemBean.setApprovingOfficerId((String)stepElement.getParameterValue(vwParameters[i].getName()));

									} else if(vwParameters[i].getName().equalsIgnoreCase("IsRequestRejected")) {

										workitemBean.setIsRequestRejected((String)stepElement.getParameterValue(vwParameters[i].getName()));
									}
								}
								workitemBeanList.add(workitemBean);	
								workitemBean=null;
							}	
						} else {

							logger.info("Work item is not available in the CRAdmin queue");

							vwQueue = vwSession.getQueue(ResourceLoader.getMessageProperties().getProperty(ERSConstants.PROCESSING_QUEUE));

							if(null!=peOperationsBean.getWobNumber()) {

								queuequery = vwQueue.createQuery(null, null, null, VWQueue.QUERY_NO_OPTIONS 
										+ VWQueue.QUERY_READ_BOUND
										+ VWQueue.QUERY_READ_LOCKED, 
										"F_WobNum=:A", new String[]{peOperationsBean.getWobNumber()}, VWFetchType.FETCH_TYPE_STEP_ELEMENT);
							} 

							while (queuequery.hasNext()) {

								stepElement = (VWStepElement) queuequery.next();

								logger.debug("Step element : " + stepElement.getWorkObjectNumber() + " : " + stepElement.getWorkflowNumber());

								vwParameters = stepElement.getParameters(VWFieldType.ALL_FIELD_TYPES, VWStepElement.FIELD_USER_DEFINED);

								workitemBean = new WorkitemBean();

								for (int i = 0; i < vwParameters.length; i++) {								

									//logger.info(vwParameters[i].getName() + " : " + stepElement.getParameterValue(vwParameters[i].getName()));

									if(vwParameters[i].getName().equalsIgnoreCase("ReqId")) {

										workitemBean.setReqId(stepElement.getParameterValue(vwParameters[i].getName()) + "");

									} else if(vwParameters[i].getName().equalsIgnoreCase("FdrRefNum")) {

										workitemBean.setFdrRefNum((String)stepElement.getParameterValue(vwParameters[i].getName()));

									} else if(vwParameters[i].getName().equalsIgnoreCase("RecRefNo")) {

										workitemBean.setRecRefNo(Integer.parseInt(stepElement.getParameterValue(vwParameters[i].getName()).toString()));

									} else if(vwParameters[i].getName().equalsIgnoreCase("RequestedBy")) {

										workitemBean.setRequestedBy((String)stepElement.getParameterValue(vwParameters[i].getName()));

									} else if(vwParameters[i].getName().equalsIgnoreCase("RejectedBy")) {

										workitemBean.setRejectedBy((String)stepElement.getParameterValue(vwParameters[i].getName()));

									} else if(vwParameters[i].getName().equalsIgnoreCase("RequstorRemarks")) {

										workitemBean.setRequestorRemarks((String)stepElement.getParameterValue(vwParameters[i].getName()));

									} else if(vwParameters[i].getName().equalsIgnoreCase("RejectorRemarks")) {

										workitemBean.setRejectorRemarks((String)stepElement.getParameterValue(vwParameters[i].getName()));

									} else if(vwParameters[i].getName().equalsIgnoreCase("RequestorId")) {

										workitemBean.setRequestorId((String)stepElement.getParameterValue(vwParameters[i].getName()));

									} else if(vwParameters[i].getName().equalsIgnoreCase("ApprovingOfficerId")) {

										workitemBean.setApprovingOfficerId((String)stepElement.getParameterValue(vwParameters[i].getName()));

									} else if(vwParameters[i].getName().equalsIgnoreCase("IsRequestRejected")) {

										workitemBean.setIsRequestRejected((String)stepElement.getParameterValue(vwParameters[i].getName()));
									}
								}
								workitemBeanList.add(workitemBean);	
								workitemBean=null;
							}
						}
					}
				}
				else
				{
					logger.error("VWSession is null. Connection could not established, no workflow can be retrieved.");
				}	
			}
			else
			{
				logger.error("PEOperationsBean object is null. No action can be performed");
			}			

		} catch (VWException exceptionObj) {

			logger.error("VWException occured in retrieveWorkitemUsingWobnumber method " + exceptionObj.getMessage(), exceptionObj);

			throw new ERSException(exceptionObj.getMessage());

		} catch (Exception exceptionObj) {

			logger.error("Exception occured in retrieveWorkitemUsingWobnumber method "+ exceptionObj.getMessage(), exceptionObj);

			throw new ERSException(exceptionObj.getMessage());
		} 
		finally
		{
			if(null!=queuequery)
				queuequery=null;

			if(null!=vwQueue)
				vwQueue=null;

			if(null!=stepElement)
				stepElement=null;

			if(rmConnection != null) {
				rmConnection.closeRMConnection();
			}

			if(null!=vwSession)
			{
				rmConnection.closeVWSession(vwSession);
			}
		}
		return workitemBeanList;

	}

	/**
	 * This method is used to dispatch the work item for Requestor
	 * @param peOperationsBean
	 * @throws ERSException
	 */
	public void dispatchWorkitemForRequestor(PEOperationsBean peOperationsBean) throws VWException, ERSException {

		logger.info("Enter the dispatchWorkitemForRequestor method " );	

		VWSession vwSession =null;

		VWQueueQuery queuequery = null;

		VWQueue vwQueue = null;

		RMConnection rmConnection = null;

		VWStepElement stepElement = null;

		VWStepElement vwStepElement = null;

		try
		{
			if(null!=peOperationsBean)
			{
				logger.info("peOperationsBean : " + peOperationsBean.toString());

				rmConnection = new RMConnection();
				vwSession = rmConnection.getWSIVWsession();
				if(null!=vwSession)
				{											
					if(null != peOperationsBean)
					{	
						if(null!=peOperationsBean.getStepRespose() && null!=peOperationsBean.getWobNumber())
						{
							if(peOperationsBean.getStepRespose().equalsIgnoreCase(ERSConstants.WORKFLOW_RESPONSE_CANCEL) || peOperationsBean.getStepRespose().equalsIgnoreCase(ERSConstants.WORKFLOW_RESPONSE_RESUBMIT) || peOperationsBean.getStepRespose().equalsIgnoreCase(ERSConstants.WORKFLOW_RESPONSE_VOID) || peOperationsBean.getStepRespose().equalsIgnoreCase(ERSConstants.WORKFLOW_RESPONSE_COMPLETE))
							{
								if(peOperationsBean.getStepRespose().equalsIgnoreCase(ERSConstants.WORKFLOW_RESPONSE_CANCEL)) {
									
									if(peOperationsBean.getWorkflowName().equalsIgnoreCase(ERSWorkflowProperty.WORKFLOW_TYPE_CREATE_FOLDER) || peOperationsBean.getWorkflowName().equalsIgnoreCase(ERSWorkflowProperty.WORKFLOW_TYPE_DELETE_FOLDER) || peOperationsBean.getWorkflowName().equalsIgnoreCase(ERSWorkflowProperty.WORKFLOW_TYPE_UPDATE_FOLDER_METADATA) || peOperationsBean.getWorkflowName().equalsIgnoreCase(ERSWorkflowProperty.WORKFLOW_TYPE_BORROW_PHYSICAL_ITEM)) {
									
										vwQueue = vwSession.getQueue(ResourceLoader.getMessageProperties().getProperty(ERSConstants.CRADMIN_QUEUE));
										
									} else {
										
										vwQueue = vwSession.getQueue(ResourceLoader.getMessageProperties().getProperty(ERSConstants.PROCESSING_QUEUE));
									}
									
								} else if(peOperationsBean.getStepRespose().equalsIgnoreCase(ERSConstants.WORKFLOW_RESPONSE_RESUBMIT) || peOperationsBean.getStepRespose().equalsIgnoreCase(ERSConstants.WORKFLOW_RESPONSE_VOID) || peOperationsBean.getStepRespose().equalsIgnoreCase(ERSConstants.WORKFLOW_RESPONSE_COMPLETE)) {
									
									vwQueue = vwSession.getQueue(ResourceLoader.getMessageProperties().getProperty(ERSConstants.PROCESSING_QUEUE));
								}

								//filter = "F_WobNum = '" + peOperationsBean.getWobNumber() + "'";

								logger.info("Response : " + peOperationsBean.getStepRespose());

								queuequery = vwQueue.createQuery(null, null, null, VWQueue.QUERY_NO_OPTIONS 

										+ VWQueue.QUERY_READ_BOUND

										+ VWQueue.QUERY_READ_LOCKED, 

										"F_WobNum=:A", new String[]{peOperationsBean.getWobNumber()}, VWFetchType.FETCH_TYPE_STEP_ELEMENT);

								while(queuequery.hasNext()) {							

									vwStepElement = (VWStepElement)queuequery.next();

									vwStepElement.doLock(true);

									if(peOperationsBean.getStepRespose() != ERSConstants.WORKFLOW_RESPONSE_COMPLETE) {

										vwStepElement.setSelectedResponse(peOperationsBean.getStepRespose());									
									}
									if(peOperationsBean.getStepRespose().equalsIgnoreCase(ERSConstants.WORKFLOW_RESPONSE_CANCEL))
									{
										vwStepElement.setSelectedResponse(peOperationsBean.getStepRespose());	

										vwStepElement.doSave(false);

									} else if(peOperationsBean.getStepRespose().equalsIgnoreCase(ERSConstants.WORKFLOW_RESPONSE_RESUBMIT)) {

										vwStepElement.setSelectedResponse(peOperationsBean.getStepRespose());

										//vwStepElement.setParameterValue(ERSWorkflowProperty.REQUESTOR_REMARKS, peOperationsBean.getRequestorRemarks(), true);

										vwStepElement.setParameterValue(ERSWorkflowProperty.REQUESTED_BY, peOperationsBean.getRequestedBy(), true);

										vwStepElement.doSave(false);

									} else if(peOperationsBean.getStepRespose().equalsIgnoreCase(ERSConstants.WORKFLOW_RESPONSE_VOID)) {

										vwStepElement.setSelectedResponse(peOperationsBean.getStepRespose());	

										vwStepElement.doSave(false);
									}								

									vwStepElement.doDispatch();

									logger.info("Workitem is dispatched successfully !!!!");
									
									if (peOperationsBean.getApprovingOfficerId() != null) {
										
					                    reassignRequest(peOperationsBean);
					                  }

								}
							}
							else
							{
								logger.info("Response Value is not valid. Valid Response values are [Resubmit, Void To AO and Cancel]. Work request can not be processed.");
							}
						}
						else
						{
							logger.info("Response  and WobNumber are mandatory field for request dispatch which is null. Work request can not be processed.");
						}					
					}
				}
				else
				{
					logger.error("VWSession is null. Connection could not established, no workflow can be updated and dispatched.");
				}	
			}
			else
			{
				logger.error("PEOperationsBean object is null. No action can be performed");
			}			

		} catch (VWException exceptionObj) {

			logger.error("VWException occured in dispatchWorkitemForRequestor method " + exceptionObj.getMessage(), exceptionObj);

			throw new ERSException(exceptionObj.getMessage());

		} catch (Exception exceptionObj) {

			logger.error("Exception occured in dispatchWorkitemForRequestor method "+ exceptionObj.getMessage(), exceptionObj);

			throw new ERSException(exceptionObj.getMessage());
		} 
		finally
		{
			if(null!=queuequery)
				queuequery=null;

			if(null!=vwQueue)
				vwQueue=null;

			if(null!=stepElement)
				stepElement=null;

			if(null!=vwSession)
			{
				rmConnection.closeVWSession(vwSession);
			}
		}		
	}

	/**
	 * This method is used to dispatch the work item form CRAdmin queue
	 * @param peOperationsBean
	 * @param userBean
	 * @throws ERSException
	 */
	public void dispatchCRAdminQueue(PEOperationsBean peOperationsBean) throws VWException, ERSException {

		logger.info("Enter the dispatchCRAdminQueue method " );	

		VWSession vwSession =null;

		VWQueueQuery queuequery = null;

		VWQueue vwQueue = null;

		RMConnection rmConnection = null;

		VWStepElement stepElement = null;

		VWStepElement vwStepElement = null;

		try
		{
			if(null!=peOperationsBean)
			{
				logger.info("peOperationsBean : " + peOperationsBean.toString());

				rmConnection = new RMConnection();

				vwSession = rmConnection.getWSIVWsession();

				if(null!=vwSession)
				{											
					if(null != peOperationsBean)
					{	
						if(null!=peOperationsBean.getStepRespose() && null!=peOperationsBean.getWobNumber())
						{
							if(peOperationsBean.getStepRespose().equalsIgnoreCase(ERSConstants.WORKFLOW_RESPONSE_APPROVE) || peOperationsBean.getStepRespose().equalsIgnoreCase(ERSConstants.WORKFLOW_RESPONSE_REJECT))
							{
								vwQueue = vwSession.getQueue(ResourceLoader.getMessageProperties().getProperty(ERSConstants.CRADMIN_QUEUE));

								queuequery = vwQueue.createQuery(null, null, null, VWQueue.QUERY_NO_OPTIONS 

										+ VWQueue.QUERY_READ_BOUND

										+ VWQueue.QUERY_READ_LOCKED, 

										"F_WobNum=:A", new String[]{peOperationsBean.getWobNumber()}, VWFetchType.FETCH_TYPE_STEP_ELEMENT);

								logger.info("queuequery : " + queuequery.fetchCount());

								if(queuequery.fetchCount() == 1) {

									while(queuequery.hasNext()) {							

									vwStepElement = (VWStepElement)queuequery.next();

									vwStepElement.doLock(true);

									if(peOperationsBean.getStepRespose().equalsIgnoreCase(ERSConstants.WORKFLOW_RESPONSE_REJECT))
									{
										vwStepElement.setSelectedResponse(peOperationsBean.getStepRespose());

										vwStepElement.setParameterValue(ERSWorkflowProperty.REJECTOR_REMARKS, peOperationsBean.getRejectorRemarks(), true);

										vwStepElement.setParameterValue(ERSWorkflowProperty.REJECTED_BY, peOperationsBean.getRejectedBy(), true);

										vwStepElement.doSave(false);

									} else if(peOperationsBean.getStepRespose().equalsIgnoreCase(ERSConstants.WORKFLOW_RESPONSE_APPROVE))	{

										vwStepElement.setSelectedResponse(peOperationsBean.getStepRespose());

										vwStepElement.setParameterValue(ERSWorkflowProperty.APPROVED_BY, peOperationsBean.getApprovedBy(), true);

										vwStepElement.setParameterValue(ERSWorkflowProperty.APPROVER_REMARKS, peOperationsBean.getApproverRemarks(), true);

										vwStepElement.setParameterValue(ERSWorkflowProperty.APPROVED_DATE, peOperationsBean.getApprovedDate(), true);

										vwStepElement.doSave(false);
									}								

									vwStepElement.doDispatch();

										logger.info("Workflow is dispatched successfully !!!!");
									}
								} else {

									updateAndDispatchWorkitem(peOperationsBean, null);
								}
							}
							else
							{
								logger.info("Response Value is not valid. Valid Response value is [Approve or Reject]. Work request can not be processed.");
							}
						}
						else
						{
							logger.info("Response  and WobNumber are mandatory field for request dispatch which is null. Work request can not be processed.");
						}					
					}
				}
				else
				{
					logger.error("VWSession is null. Connection could not established, no workflow can be updated and dispatched.");
				}	
			}
			else
			{
				logger.error("PEOperationsBean object is null. No action can be performed");
			}			

		} catch (VWException exceptionObj) {

			logger.error("VWException occured in dispatchCRAdminQueue method " + exceptionObj.getMessage(), exceptionObj);

			throw new ERSException(exceptionObj.getMessage());

		} catch (Exception exceptionObj) {

			logger.error("Exception occured in dispatchCRAdminQueue method "+ exceptionObj.getMessage(), exceptionObj);

			throw new ERSException(exceptionObj.getMessage());
		} 
		finally
		{
			if(null!=queuequery)
				queuequery=null;

			if(null!=vwQueue)
				vwQueue=null;

			if(null!=stepElement)
				stepElement=null;

			if(null!=vwSession)
			{
				rmConnection.closeVWSession(vwSession);
			}
		}		
	}

	/**
	 * This method is used to Reassign the workitem to another user
	 * @param peOperationsBean
	 * @throws ERSException
	 */
	public void reassignRequest(PEOperationsBean peOperationsBean) throws ERSException {
		
		logger.info("Entered into reassignRequest method");
		
		VWSession vwSession =null;

		VWQueueQuery queuequery = null;

		VWQueue vwQueue = null;

		RMConnection rmConnection = null;

		VWStepElement vwStepElement = null;
		
		try {
			
			if(null!=peOperationsBean)
			{
				logger.debug("peOperationsBean : " + peOperationsBean.toString());

				rmConnection = new RMConnection();

				vwSession = rmConnection.getWSIVWsession();

				if(null!=vwSession) {											
					
					if(null != peOperationsBean) {	
						
						if(null!=peOperationsBean.getWobNumber() && peOperationsBean.getReassignedUserId() != null) {
							
							if(peOperationsBean.getWorkflowName().equalsIgnoreCase(ERSWorkflowProperty.WORKFLOW_TYPE_CREATE_FOLDER) || peOperationsBean.getWorkflowName().equalsIgnoreCase(ERSWorkflowProperty.WORKFLOW_TYPE_DELETE_FOLDER) || peOperationsBean.getWorkflowName().equalsIgnoreCase(ERSWorkflowProperty.WORKFLOW_TYPE_UPDATE_FOLDER_METADATA) || peOperationsBean.getWorkflowName().equalsIgnoreCase(ERSWorkflowProperty.WORKFLOW_TYPE_BORROW_PHYSICAL_ITEM)) {
								
								vwQueue = vwSession.getQueue(ResourceLoader.getMessageProperties().getProperty(ERSConstants.CRADMIN_QUEUE));
								
							} else {
								
								vwQueue = vwSession.getQueue(ResourceLoader.getMessageProperties().getProperty(ERSConstants.PROCESSING_QUEUE));
							}
							
							queuequery = vwQueue.createQuery(null, null, null, VWQueue.QUERY_NO_OPTIONS 

									+ VWQueue.QUERY_READ_BOUND

									+ VWQueue.QUERY_READ_LOCKED, 

									"F_WobNum=:A", new String[]{peOperationsBean.getWobNumber()}, VWFetchType.FETCH_TYPE_STEP_ELEMENT);

							while(queuequery.hasNext()) {							

								vwStepElement = (VWStepElement)queuequery.next();

								vwStepElement.doLock(true);
								
								vwStepElement.setParameterValue(ERSWorkflowProperty.APPROVING_OFFICER_ID, peOperationsBean.getReassignedUserId(), true);
								
								vwStepElement.doSave(false);

								vwStepElement.doReassign(peOperationsBean.getReassignedUserId(), false, null);

								logger.info("Workitem is reassign successfully !!!!");
							}
							
						} else {
							
							logger.info("WobNumber and ReassineUserId are mandatory field for request reassign which is null. Work request can not be processed.");
						}
					}
				}
			}
			
		} catch (VWException exceptionObj) {

			logger.error("VWException occured in reassignRequest method " + exceptionObj.getMessage(), exceptionObj);

			throw new ERSException(exceptionObj.getMessage());

		} catch (Exception exceptionObj) {

			logger.error("Exception occured in reassignRequest method "+ exceptionObj.getMessage(), exceptionObj);

			throw new ERSException(exceptionObj.getMessage());
		} 
		finally
		{
			if(null!=queuequery)
				queuequery=null;

			if(null!=vwQueue)
				vwQueue=null;

			if(null!=vwStepElement)
				vwStepElement=null;

			if(null!=vwSession)
			{
				rmConnection.closeVWSession(vwSession);
			}
		}
	}

	public static void main(String[] args) throws Exception {

		PEOperationsImpl peOperations = new PEOperationsImpl();

		PEOperationsBean peOperationsBean = new PEOperationsBean();

		peOperationsBean.setQueueName("Inbox(0)");

		peOperationsBean.setWobNumber("8497D9E94369C340B8B9BC2B5A967553");

		peOperationsBean.setStepRespose("Reject");

		//peOperationsBean.setReportingOfficerId("ERSAPPSVC02");

		//peOperationsBean.setActionOfficerId("ERSAPPSVC01");

		peOperationsBean.setApprovedDate(new Date());

		peOperationsBean.setApprovedBy("ERSAPPDEPTADM01");

		peOperationsBean.setApproverRemarks("Archive is approved");

		//peOperationsBean.setActionOfficerId("ERSAPPSVC02");

		UserDetailsBean userBean = new UserDetailsBean();

		userBean.setUserId("ersfnp8svc01");

		peOperations.retrieveWorkItemsFromQueue(peOperationsBean, userBean);

		//peOperations.updateAndDispatchWorkitem(peOperationsBean, userBean);

	}

}
