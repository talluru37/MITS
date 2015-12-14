package com.ers.jarm.api.bean;

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
 * This RecordFolderBean class is used in Record Folder Operations to 
 * create folder, view folder, update folder, delete folder
 * 
 * @class name  RecordFolderBean.java
 * @date  sep,2014
 * @author MITS
 * @version 1.0
 */
public class VolumeBean {
	
	private String fdrRefNo;
	
	private String volNo;
	
	private String volLfcyStat;
	
	private int recordCount;
	
	private int rndPeriod;
	
	private String rndAction;
	
	private String approvedBy;
	
	private Date approvedDate;
	
	private String approverDesignation;
	
	private Date firstEnclDate;
	
	private Date lastEnclDate;
	
	private boolean isDeleted;
	
	private String volumeId;
	
	private String archivedPath;

	public String getFdrRefNo() {
		return fdrRefNo;
	}

	public void setFdrRefNo(String fdrRefNo) {
		this.fdrRefNo = fdrRefNo;
	}

	public String getVolNo() {
		return volNo;
	}

	public void setVolNo(String volNo) {
		this.volNo = volNo;
	}

	public String getVolLfcyStat() {
		return volLfcyStat;
	}

	public void setVolLfcyStat(String volLfcyStat) {
		this.volLfcyStat = volLfcyStat;
	}

	public int getRecordCount() {
		return recordCount;
	}

	public void setRecordCount(int recordCount) {
		this.recordCount = recordCount;
	}

	public int getRndPeriod() {
		return rndPeriod;
	}

	public void setRndPeriod(int rndPeriod) {
		this.rndPeriod = rndPeriod;
	}

	public String getRndAction() {
		return rndAction;
	}

	public void setRndAction(String rndAction) {
		this.rndAction = rndAction;
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

	public String getApproverDesignation() {
		return approverDesignation;
	}

	public void setApproverDesignation(String approverDesignation) {
		this.approverDesignation = approverDesignation;
	}

	public Date getFirstEnclDate() {
		return firstEnclDate;
	}

	public void setFirstEnclDate(Date firstEnclDate) {
		this.firstEnclDate = firstEnclDate;
	}

	public Date getLastEnclDate() {
		return lastEnclDate;
	}

	public void setLastEnclDate(Date lastEnclDate) {
		this.lastEnclDate = lastEnclDate;
	}

	public boolean isDeleted() {
		return isDeleted;
	}

	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	public String getVolumeId() {
		return volumeId;
	}

	public void setVolumeId(String volumeId) {
		this.volumeId = volumeId;
	}

	public String getArchivedPath() {
		return archivedPath;
	}

	public void setArchivedPath(String archivedPath) {
		this.archivedPath = archivedPath;
	}

	@Override
	public String toString() {
		return "VolumeBean [fdrRefNo=" + fdrRefNo + ", volNo=" + volNo
				+ ", volLfcyStat=" + volLfcyStat + ", recordCount="
				+ recordCount + ", rndPeriod=" + rndPeriod + ", rndAction="
				+ rndAction + ", approvedBy=" + approvedBy + ", approvedDate="
				+ approvedDate + ", approverDesignation=" + approverDesignation
				+ ", firstEnclDate=" + firstEnclDate + ", lastEnclDate="
				+ lastEnclDate + ", isDeleted=" + isDeleted + ", volumeId="
				+ volumeId + ", archivedPath=" + archivedPath + "]";
	}

}
