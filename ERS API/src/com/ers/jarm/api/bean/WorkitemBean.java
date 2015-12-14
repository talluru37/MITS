package com.ers.jarm.api.bean;

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
 * This WorkitemBean class is used in PE operations 
 * to return the results in PE operations
 * 
 * @class name  WorkitemBean.java
 * @date  sep,2014
 * @author MITS
 * @version 1.0
 */
public class WorkitemBean {
	
	private String reqId;
	
	

	private String fdrRefNum;
	
	private String requestStatus;
			
	private String requestorRemarks;
	
	private String approverRemarks;
	
	private String requestedBy;
	
	private String approvedBy;
	
	private String workflowName;
	
	private int volNo;
	
	private int recRefNo;
	
	private String wobNumber;
	
	private String recordTitle;
	
	private String folderTitle;
	
	private String rejectedBy;
	
	private String rejectorRemarks;
	
	private String requestorId;
	
	private String approvingOfficerId;
	
	private String isRequestRejected;
	
	private String isCRAdminRequest; 
	
	private boolean isWorkitemLocked;
	
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

	public String getApprovedBy() {
		return approvedBy;
	}

	public void setApprovedBy(String approvedBy) {
		this.approvedBy = approvedBy;
	}

	public String getWorkflowName() {
		return workflowName;
	}

	public void setWorkflowName(String workflowName) {
		this.workflowName = workflowName;
	}

	public int getRecRefNo() {
		return recRefNo;
	}

	public void setRecRefNo(int recRefNo) {
		this.recRefNo = recRefNo;
	}

	public String getWobNumber() {
		return wobNumber;
	}

	public void setWobNumber(String wobNumber) {
		this.wobNumber = wobNumber;
	}
	
	public int getVolNo() {
		return volNo;
	}

	public void setVolNo(int volNo) {
		this.volNo = volNo;
	}

	public String getRecordTitle() {
		return recordTitle;
	}

	public void setRecordTitle(String recordTitle) {
		this.recordTitle = recordTitle;
	}

	public String getFolderTitle() {
		return folderTitle;
	}

	public void setFolderTitle(String folderTitle) {
		this.folderTitle = folderTitle;
	}

	public String getRejectedBy() {
		return rejectedBy;
	}

	public void setRejectedBy(String rejectedBy) {
		this.rejectedBy = rejectedBy;
	}

	public String getRejectorRemarks() {
		return rejectorRemarks;
	}

	public void setRejectorRemarks(String rejectorRemarks) {
		this.rejectorRemarks = rejectorRemarks;
	}
	
	public String getRequestorId() {
		return requestorId;
	}

	public void setRequestorId(String requestorId) {
		this.requestorId = requestorId;
	}
	
	public String getIsRequestRejected() {
		return isRequestRejected;
	}

	public void setIsRequestRejected(String isRequestRejected) {
		this.isRequestRejected = isRequestRejected;
	}
	
	public String getApprovingOfficerId() {
		return approvingOfficerId;
	}

	public void setApprovingOfficerId(String approvingOfficerId) {
		this.approvingOfficerId = approvingOfficerId;
	}

	public String getIsCRAdminRequest() {
		return isCRAdminRequest;
	}

	public void setIsCRAdminRequest(String isCRAdminRequest) {
		this.isCRAdminRequest = isCRAdminRequest;
	}

	public boolean isWorkitemLocked() {
		return isWorkitemLocked;
	}

	public void setWorkitemLocked(boolean isWorkitemLocked) {
		this.isWorkitemLocked = isWorkitemLocked;
	}

	@Override
	public String toString() {
		return "WorkitemBean [reqId=" + reqId + ", fdrRefNum=" + fdrRefNum + ", requestStatus=" + requestStatus
				+ ", requestorRemarks=" + requestorRemarks + ", approverRemarks=" + approverRemarks + ", requestedBy="
				+ requestedBy + ", approvedBy=" + approvedBy + ", workflowName=" + workflowName + ", volNo=" + volNo
				+ ", recRefNo=" + recRefNo + ", wobNumber=" + wobNumber + ", recordTitle=" + recordTitle
				+ ", folderTitle=" + folderTitle + ", rejectedBy=" + rejectedBy + ", rejectorRemarks="
				+ rejectorRemarks + ", requestorId=" + requestorId + ", approvingOfficerId=" + approvingOfficerId
				+ ", isRequestRejected=" + isRequestRejected + ", isCRAdminRequest=" + isCRAdminRequest
				+ ", isWorkitemLocked=" + isWorkitemLocked + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((reqId == null) ? 0 : reqId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WorkitemBean other = (WorkitemBean) obj;
		if (reqId == null) {
			if (other.reqId != null)
				return false;
		} else if (!reqId.equals(other.reqId))
			return false;
		return true;
	}
	
}
