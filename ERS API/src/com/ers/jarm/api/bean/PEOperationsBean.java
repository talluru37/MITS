package com.ers.jarm.api.bean;

import java.util.Arrays;
import java.util.Date;

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
 * This PEOperationsBean class is used in PE operations * 
 * 
 * @class name  PEOperationsBean.java
 * @date  sep,2014
 * @author MITS
 * @version 1.0
 */
public class PEOperationsBean {
	
	private String reqId;
	
	private String fdrRefNum;
	
	private int volNo;
	
	private String requestStatus;
			
	private String requestorRemarks;
	
	private String approverRemarks;
	
	private String requestedBy;
	
	private Date requestDt;
	
	private String approvedBy;
	
	private Date approvedDate;
	
	private String workflowName;
	
	private String queueName;
	
	private String stepRespose;
	
	private String folderTitle;
	
	private int recRefNo;
	
	private String wobNumber;
	
	private String userId;
	
	private String password;
	
	private String actionOfficerId;
	
	private String reportingOfficerId;
	
	private String requestorId;
	
	private String approvingOfficerId;
	
	private String folderType;
	
	private int rndPeriod;
	
	private String createdBy;
	
	private String modifiedBy;
	
	private String adHoc;
	
	private int deadlineDays;
	
	private String recordTitle;
	
	private String rejectorRemarks;
	
	private String rejectedBy;
	
	private String deptDirector;
	
	private String externalDeptDirector;
	
	private boolean isInternalTransfer;
	
	private String[] CRAdminGroup;
	
	private String reassignedUserId;
	
	public int getRecRefNo() {
		return recRefNo;
	}

	public void setRecRefNo(int recRefNo) {
		this.recRefNo = recRefNo;
	}

	public String getReqId() {
		return reqId;
	}

	public void setReqId(String reqId) {
		this.reqId = reqId;
	}

	public String getFdrRefNum() {
		return fdrRefNum;
	}

	public void setFdrRefNum(String fdrRefNum) {
		this.fdrRefNum = fdrRefNum;
	}

	public int getVolNo() {
		return volNo;
	}

	public void setVolNo(int volNo) {
		this.volNo = volNo;
	}

	public String getRequestStatus() {
		return requestStatus;
	}

	public void setRequestStatus(String requestStatus) {
		this.requestStatus = requestStatus;
	}

	public String getRequestorRemarks() {
		return requestorRemarks;
	}

	public void setRequestorRemarks(String requestorRemarks) {
		this.requestorRemarks = requestorRemarks;
	}

	public String getApproverRemarks() {
		return approverRemarks;
	}

	public void setApproverRemarks(String approverRemarks) {
		this.approverRemarks = approverRemarks;
	}

	public String getRequestedBy() {
		return requestedBy;
	}

	public void setRequestedBy(String requestedBy) {
		this.requestedBy = requestedBy;
	}

	public Date getRequestDt() {
		return requestDt;
	}

	public void setRequestDt(Date requestDt) {
		this.requestDt = requestDt;
	}

	public String getApprovedBy() {
		return approvedBy;
	}

	public void setApprovedBy(String approvedBy) {
		this.approvedBy = approvedBy;
	}

	public Date getApprovedDate() {
		return approvedDate;
	}

	public void setApprovedDate(Date approvedDate) {
		this.approvedDate = approvedDate;
	}

	public String getWorkflowName() {
		return workflowName;
	}

	public void setWorkflowName(String workflowName) {
		this.workflowName = workflowName;
	}

	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}

	public String getStepRespose() {
		return stepRespose;
	}

	public void setStepRespose(String stepRespose) {
		this.stepRespose = stepRespose;
	}

	public String getFolderTitle() {
		return folderTitle;
	}

	public void setFolderTitle(String folderTitle) {
		this.folderTitle = folderTitle;
	}

	public String getWobNumber() {
		return wobNumber;
	}

	public void setWobNumber(String wobNumber) {
		this.wobNumber = wobNumber;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getActionOfficerId() {
		return actionOfficerId;
	}

	public void setActionOfficerId(String actionOfficerId) {
		this.actionOfficerId = actionOfficerId;
	}

	public String getReportingOfficerId() {
		return reportingOfficerId;
	}

	public void setReportingOfficerId(String reportingOfficerId) {
		this.reportingOfficerId = reportingOfficerId;
	}
	
	public String getApprovingOfficerId() {
		return approvingOfficerId;
	}

	public void setApprovingOfficerId(String approvingOfficerId) {
		this.approvingOfficerId = approvingOfficerId;
	}

	public String getRequestorId() {
		return requestorId;
	}
	
	public void setRequestorId(String requestorId) {
		this.requestorId = requestorId;
	}
	
	public String getFolderType() {
		return folderType;
	}

	public void setFolderType(String folderType) {
		this.folderType = folderType;
	}
	
	public int getRndPeriod() {
		return rndPeriod;
	}

	public void setRndPeriod(int rndPeriod) {
		this.rndPeriod = rndPeriod;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public String getAdHoc() {
		return adHoc;
	}

	public void setAdHoc(String adHoc) {
		this.adHoc = adHoc;
	}
	
	public int getDeadlineDays() {
		return deadlineDays;
	}

	public void setDeadlineDays(int deadlineDays) {
		this.deadlineDays = deadlineDays;
	}
	
	public String getRecordTitle() {
		return recordTitle;
	}

	public void setRecordTitle(String recordTitle) {
		this.recordTitle = recordTitle;
	}
	
	public String getRejectorRemarks() {
		return rejectorRemarks;
	}

	public void setRejectorRemarks(String rejectorRemarks) {
		this.rejectorRemarks = rejectorRemarks;
	}
	
	public String getRejectedBy() {
		return rejectedBy;
	}

	public void setRejectedBy(String rejectedBy) {
		this.rejectedBy = rejectedBy;
	}
	
	public String getDeptDirector() {
		return deptDirector;
	}

	public void setDeptDirector(String deptDirector) {
		this.deptDirector = deptDirector;
	}

	public String getExternalDeptDirector() {
		return externalDeptDirector;
	}

	public void setExternalDeptDirector(String externalDeptDirector) {
		this.externalDeptDirector = externalDeptDirector;
	}

	public boolean isInternalTransfer() {
		return isInternalTransfer;
	}

	public void setInternalTransfer(boolean isInternalTransfer) {
		this.isInternalTransfer = isInternalTransfer;
	}

	public String[] getCRAdminGroup() {
		return CRAdminGroup;
	}

	public void setCRAdminGroup(String[] cRAdminGroup) {
		CRAdminGroup = cRAdminGroup;
	}
	
	public String getReassignedUserId() {
		return reassignedUserId;
	}

	public void setReassignedUserId(String reassignedUserId) {
		this.reassignedUserId = reassignedUserId;
	}

	@Override
	public String toString() {
		return "PEOperationsBean [reqId=" + reqId + ", fdrRefNum=" + fdrRefNum + ", volNo=" + volNo
				+ ", requestStatus=" + requestStatus + ", requestorRemarks=" + requestorRemarks + ", approverRemarks="
				+ approverRemarks + ", requestedBy=" + requestedBy + ", requestDt=" + requestDt + ", approvedBy="
				+ approvedBy + ", approvedDate=" + approvedDate + ", workflowName=" + workflowName + ", queueName="
				+ queueName + ", stepRespose=" + stepRespose + ", folderTitle=" + folderTitle + ", recRefNo="
				+ recRefNo + ", wobNumber=" + wobNumber + ", userId=" + userId + ", password=" + password
				+ ", actionOfficerId=" + actionOfficerId + ", reportingOfficerId=" + reportingOfficerId
				+ ", requestorId=" + requestorId + ", approvingOfficerId=" + approvingOfficerId + ", folderType="
				+ folderType + ", rndPeriod=" + rndPeriod + ", createdBy=" + createdBy + ", modifiedBy=" + modifiedBy
				+ ", adHoc=" + adHoc + ", deadlineDays=" + deadlineDays + ", recordTitle=" + recordTitle
				+ ", rejectorRemarks=" + rejectorRemarks + ", rejectedBy=" + rejectedBy + ", deptDirector="
				+ deptDirector + ", externalDeptDirector=" + externalDeptDirector + ", isInternalTransfer="
				+ isInternalTransfer + ", CRAdminGroup=" + Arrays.toString(CRAdminGroup) + ", reassignedUserId="
				+ reassignedUserId + "]";
	}

}
