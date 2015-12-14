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
public class RecordFolderBean {
	
	private String folderTitle;
	
	private String description;
	
	private String tags;
	
	private String creator;
	
	private String volNo;
	
	private String fdrRefNo;
	
	private String oldFdrRefNo;
	
	private String fdrLfcyStat;
	
	private String medianumber;
	
	private Date dateCreated;
	
	private Date firstEnclDate;
	
	private Date dateofFilingFirstEnclosure;
	
	private Date lastEnclDate;
	
	private Date dateofFilingLastEnclosure;
	
	private Date dateClosed;
	
	private String secClass;
	
	private String folderOwner;
	
	private String folderOwnerDepartment;
	
	private int randDPeriod;
	
	private String randDAction;
	
	private String locationCode;
	
	private String barcode;
	
	private String folderMovementStatus;
	
	private String folderClassName;
	
	private String recordCategoryId;
	
	private String taxonomyReferenceNo;
	
	private String recordFolderId;
	
	private String recordVolumeId;
	
	private String reasonToMove;
	
	private String reasonForClose;
	
	private String reasonToModifyStatus;
	
	private String reasonToDeletefolder;
	
	private String sourceRecordCategoryId;
	
	private String destRecordCategoryId;
	
	private String ownerRole;
	
	private String ownerOU;
	
	private String folderType;
	
	private String reviewer;
	
	public String getFolderTitle() {
		return folderTitle;
	}

	public void setFolderTitle(String folderTitle) {
		this.folderTitle = folderTitle;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}	

	public String getLocationCode() {
		return locationCode;
	}

	public void setLocationCode(String locationCode) {
		this.locationCode = locationCode;
	}

	public String getMedianumber() {
		return medianumber;
	}

	public void setMedianumber(String medianumber) {
		this.medianumber = medianumber;
	}

	public String getFolderMovementStatus() {
		return folderMovementStatus;
	}

	public void setFolderMovementStatus(String folderMovementStatus) {
		this.folderMovementStatus = folderMovementStatus;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public Date getFirstEnclDate() {
		return firstEnclDate;
	}

	public void setFirstEnclDate(Date firstEnclDate) {
		this.firstEnclDate = firstEnclDate;
	}

	public Date getDateofFilingFirstEnclosure() {
		return dateofFilingFirstEnclosure;
	}

	public void setDateofFilingFirstEnclosure(Date dateofFilingFirstEnclosure) {
		this.dateofFilingFirstEnclosure = dateofFilingFirstEnclosure;
	}

	public Date getLastEnclDate() {
		return lastEnclDate;
	}

	public void setLastEnclDate(Date lastEnclDate) {
		this.lastEnclDate = lastEnclDate;
	}

	public Date getDateofFilingLastEnclosure() {
		return dateofFilingLastEnclosure;
	}

	public void setDateofFilingLastEnclosure(Date dateofFilingLastEnclosure) {
		this.dateofFilingLastEnclosure = dateofFilingLastEnclosure;
	}

	public Date getDateClosed() {
		return dateClosed;
	}

	public void setDateClosed(Date dateClosed) {
		this.dateClosed = dateClosed;
	}

	public String getFolderOwner() {
		return folderOwner;
	}

	public void setFolderOwner(String folderOwner) {
		this.folderOwner = folderOwner;
	}

	public String getFolderOwnerDepartment() {
		return folderOwnerDepartment;
	}

	public void setFolderOwnerDepartment(String folderOwnerDepartment) {
		this.folderOwnerDepartment = folderOwnerDepartment;
	}

	public int getRandDPeriod() {
		return randDPeriod;
	}

	public void setRandDPeriod(int randDPeriod) {
		this.randDPeriod = randDPeriod;
	}

	public String getRandDAction() {
		return randDAction;
	}

	public void setRandDAction(String randDAction) {
		this.randDAction = randDAction;
	}

	

	public String getRecordVolumeId() {
		return recordVolumeId;
	}

	public void setRecordVolumeId(String recordVolumeId) {
		this.recordVolumeId = recordVolumeId;
	}

	public String getFolderClassName() {
		return folderClassName;
	}

	public void setFolderClassName(String folderClassName) {
		this.folderClassName = folderClassName;
	}

	public String getRecordCategoryId() {
		return recordCategoryId;
	}

	public void setRecordCategoryId(String recordCategoryId) {
		this.recordCategoryId = recordCategoryId;
	}

	public String getRecordFolderId() {
		return recordFolderId;
	}

	public void setRecordFolderId(String recordFolderId) {
		this.recordFolderId = recordFolderId;
	}

	public String getReasonToMove() {
		return reasonToMove;
	}

	public void setReasonToMove(String reasonToMove) {
		this.reasonToMove = reasonToMove;
	}

	public String getReasonForClose() {
		return reasonForClose;
	}

	public void setReasonForClose(String reasonForClose) {
		this.reasonForClose = reasonForClose;
	}

	public String getReasonToModifyStatus() {
		return reasonToModifyStatus;
	}

	public void setReasonToModifyStatus(String reasonToModifyStatus) {
		this.reasonToModifyStatus = reasonToModifyStatus;
	}

	public String getReasonToDeletefolder() {
		return reasonToDeletefolder;
	}

	public void setReasonToDeletefolder(String reasonToDeletefolder) {
		this.reasonToDeletefolder = reasonToDeletefolder;
	}

	public String getSourceRecordCategoryId() {
		return sourceRecordCategoryId;
	}

	public void setSourceRecordCategoryId(String sourceRecordCategoryId) {
		this.sourceRecordCategoryId = sourceRecordCategoryId;
	}

	public String getDestRecordCategoryId() {
		return destRecordCategoryId;
	}

	public void setDestRecordCategoryId(String destRecordCategoryId) {
		this.destRecordCategoryId = destRecordCategoryId;
	}

	public String getVolNo() {
		return volNo;
	}

	public void setVolNo(String volNo) {
		this.volNo = volNo;
	}

	public String getFdrRefNo() {
		return fdrRefNo;
	}

	public void setFdrRefNo(String fdrRefNo) {
		this.fdrRefNo = fdrRefNo;
	}

	public String getOldFdrRefNo() {
		return oldFdrRefNo;
	}

	public void setOldFdrRefNo(String oldFdrRefNo) {
		this.oldFdrRefNo = oldFdrRefNo;
	}

	public String getFdrLfcyStat() {
		return fdrLfcyStat;
	}

	public void setFdrLfcyStat(String fdrLfcyStat) {
		this.fdrLfcyStat = fdrLfcyStat;
	}

	public String getSecClass() {
		return secClass;
	}

	public void setSecClass(String secClass) {
		this.secClass = secClass;
	}

	public String getOwnerRole() {
		return ownerRole;
	}

	public void setOwnerRole(String ownerRole) {
		this.ownerRole = ownerRole;
	}

	public String getOwnerOU() {
		return ownerOU;
	}

	public void setOwnerOU(String ownerOU) {
		this.ownerOU = ownerOU;
	}
	
	public String getFolderType() {
		return folderType;
	}

	public void setFolderType(String folderType) {
		this.folderType = folderType;
	}
	
	public String getBarcode() {
		return barcode;
	}

	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}

	public String getTaxonomyReferenceNo() {
		return taxonomyReferenceNo;
	}

	public void setTaxonomyReferenceNo(String taxonomyReferenceNo) {
		this.taxonomyReferenceNo = taxonomyReferenceNo;
	}

	public String getReviewer() {
		return reviewer;
	}

	public void setReviewer(String reviewer) {
		this.reviewer = reviewer;
	}

	@Override
	public String toString() {
		return "RecordFolderBean [folderTitle=" + folderTitle
				+ ", description=" + description + ", tags=" + tags
				+ ", creator=" + creator + ", volNo=" + volNo + ", fdrRefNo="
				+ fdrRefNo + ", oldFdrRefNo=" + oldFdrRefNo + ", fdrLfcyStat="
				+ fdrLfcyStat + ", medianumber=" + medianumber
				+ ", dateCreated=" + dateCreated + ", firstEnclDate="
				+ firstEnclDate + ", dateofFilingFirstEnclosure="
				+ dateofFilingFirstEnclosure + ", lastEnclDate=" + lastEnclDate
				+ ", dateofFilingLastEnclosure=" + dateofFilingLastEnclosure
				+ ", dateClosed=" + dateClosed + ", secClass=" + secClass
				+ ", folderOwner=" + folderOwner + ", folderOwnerDepartment="
				+ folderOwnerDepartment + ", randDPeriod=" + randDPeriod
				+ ", randDAction=" + randDAction + ", locationCode="
				+ locationCode + ", barcode=" + barcode
				+ ", folderMovementStatus=" + folderMovementStatus
				+ ", folderClassName=" + folderClassName
				+ ", recordCategoryId=" + recordCategoryId
				+ ", taxonomyReferenceNo=" + taxonomyReferenceNo
				+ ", recordFolderId=" + recordFolderId + ", recordVolumeId="
				+ recordVolumeId + ", reasonToMove=" + reasonToMove
				+ ", reasonForClose=" + reasonForClose
				+ ", reasonToModifyStatus=" + reasonToModifyStatus
				+ ", reasonToDeletefolder=" + reasonToDeletefolder
				+ ", sourceRecordCategoryId=" + sourceRecordCategoryId
				+ ", destRecordCategoryId=" + destRecordCategoryId
				+ ", ownerRole=" + ownerRole + ", ownerOU=" + ownerOU
				+ ", folderType=" + folderType + ", reviewer=" + reviewer + "]";
	}
}
