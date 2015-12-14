package com.ers.jarm.api.bean;

import java.util.Date;
import java.util.List;

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
 * This RecordBean class is used in Record Operations to 
 * create record, view record, update record, delete record
 * 
 * @class name  RecordBean.java
 * @date  sep,2014
 * @author MITS
 * @version 1.0
 */
public class RecordBean {
	
	private String folRefNum;
	
	private String volRefNumber;
	
	private String recRefNo;
	
	private String title;
	
	private String description;
	
	private String tags;
	
	private String author;
	
	private String authorOrganisation;
	
	private String creator;
	
	private String creatorDesignation;
	
	private String recordType;
	
	private String secClass;
	
	private String locationCode;
	
	private String ownerRole;
	
	private String ownerDepartment;
	
	private String recordClassName;
	
	private String recordFolderId;
	
	private String recordId;
	
	private String reasonToMove;
	
	private String reasonToDelete;
	
	private String sourceVolumeId;
	
	private String destVolumeId;
	
	private List<DocumentBean> documentBeanList;
	
	private String mediaNumber;
	
	private String microFilmRollNo;
	
	private String microFilmFrameNo;
	
	private String inventoryClassId;
	
	private String department;
	
	private String easLink;
	
	private String oldFrn;
	
	private Date writtenDate;
	
	private boolean isDeleted;
	
	private String mvStatus;
	
	private String mvLocation;
	
	private Date mvDate;
	
	private String mvUser;
	
	private String mukimNo;
	
	private String lotNo;	
	
	private String barcode;
	
	private boolean isNASPreserveReq;
	
	private String mediaType;
	
	private String mainRecordId;
	
	private List<String> subRecordIds;
	
	private String fnRecordType;
	
	private String linkName;
	
	private List<String> recordIdsList;
	
	private int recordCount;
	
	private List<String> documentIdsList;

	//private String locationId;

	public String getFolRefNum() {
		return folRefNum;
	}

	public void setFolRefNum(String folRefNum) {
		this.folRefNum = folRefNum;
	}

	public String getVolRefNumber() {
		return volRefNumber;
	}

	public void setVolRefNumber(String volRefNumber) {
		this.volRefNumber = volRefNumber;
	}

	public String getRecRefNo() {
		return recRefNo;
	}

	public void setRecRefNo(String recRefNo) {
		this.recRefNo = recRefNo;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
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

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getAuthorOrganisation() {
		return authorOrganisation;
	}

	public void setAuthorOrganisation(String authorOrganisation) {
		this.authorOrganisation = authorOrganisation;
	}
	
	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public String getCreatorDesignation() {
		return creatorDesignation;
	}

	public void setCreatorDesignation(String creatorDesignation) {
		this.creatorDesignation = creatorDesignation;
	}

	public String getRecordType() {
		return recordType;
	}

	public void setRecordType(String recordType) {
		this.recordType = recordType;
	}

	public String getSecClass() {
		return secClass;
	}

	public void setSecClass(String secClass) {
		this.secClass = secClass;
	}

	public String getLocationCode() {
		return locationCode;
	}

	public void setLocationCode(String locationCode) {
		this.locationCode = locationCode;
	}

	public String getRecordClassName() {
		return recordClassName;
	}

	public void setRecordClassName(String recordClassName) {
		this.recordClassName = recordClassName;
	}

	public String getRecordFolderId() {
		return recordFolderId;
	}

	public void setRecordFolderId(String recordFolderId) {
		this.recordFolderId = recordFolderId;
	}

	public String getRecordId() {
		return recordId;
	}

	public void setRecordId(String recordId) {
		this.recordId = recordId;
	}

	public String getReasonToMove() {
		return reasonToMove;
	}

	public void setReasonToMove(String reasonToMove) {
		this.reasonToMove = reasonToMove;
	}

	public String getReasonToDelete() {
		return reasonToDelete;
	}

	public void setReasonToDelete(String reasonToDelete) {
		this.reasonToDelete = reasonToDelete;
	}

	public String getSourceVolumeId() {
		return sourceVolumeId;
	}

	public void setSourceVolumeId(String sourceVolumeId) {
		this.sourceVolumeId = sourceVolumeId;
	}

	public String getDestVolumeId() {
		return destVolumeId;
	}

	public void setDestVolumeId(String destVolumeId) {
		this.destVolumeId = destVolumeId;
	}

	public List<DocumentBean> getDocumentBeanList() {
		return documentBeanList;
	}

	public void setDocumentBeanList(List<DocumentBean> documentBeanList) {
		this.documentBeanList = documentBeanList;
	}

	public String getMediaNumber() {
		return mediaNumber;
	}

	public void setMediaNumber(String mediaNumber) {
		this.mediaNumber = mediaNumber;
	}

	public String getInventoryClassId() {
		return inventoryClassId;
	}

	public void setInventoryClassId(String inventoryClassId) {
		this.inventoryClassId = inventoryClassId;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public String getEasLink() {
		return easLink;
	}

	public void setEasLink(String easLink) {
		this.easLink = easLink;
	}

	public String getOldFrn() {
		return oldFrn;
	}

	public void setOldFrn(String oldFrn) {
		this.oldFrn = oldFrn;
	}

	public Date getWrittenDate() {
		return writtenDate;
	}

	public void setWrittenDate(Date writtenDate) {
		this.writtenDate = writtenDate;
	}

	public boolean isDeleted() {
		return isDeleted;
	}

	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	public String getMvStatus() {
		return mvStatus;
	}

	public void setMvStatus(String mvStatus) {
		this.mvStatus = mvStatus;
	}

	public String getMvLocation() {
		return mvLocation;
	}

	public void setMvLocation(String mvLocation) {
		this.mvLocation = mvLocation;
	}

	public Date getMvDate() {
		return mvDate;
	}

	public void setMvDate(Date mvDate) {
		this.mvDate = mvDate;
	}

	public String getMvUser() {
		return mvUser;
	}

	public void setMvUser(String mvUser) {
		this.mvUser = mvUser;
	}

	public String getMukimNo() {
		return mukimNo;
	}

	public void setMukimNo(String mukimNo) {
		this.mukimNo = mukimNo;
	}

	public String getLotNo() {
		return lotNo;
	}

	public void setLotNo(String lotNo) {
		this.lotNo = lotNo;
	}

	public String getOwnerRole() {
		return ownerRole;
	}

	public void setOwnerRole(String ownerRole) {
		this.ownerRole = ownerRole;
	}

	public String getOwnerDepartment() {
		return ownerDepartment;
	}

	public void setOwnerDepartment(String ownerDepartment) {
		this.ownerDepartment = ownerDepartment;
	}

	public String getMicroFilmRollNo() {
		return microFilmRollNo;
	}

	public void setMicroFilmRollNo(String microFilmRollNo) {
		this.microFilmRollNo = microFilmRollNo;
	}

	public String getMicroFilmFrameNo() {
		return microFilmFrameNo;
	}

	public void setMicroFilmFrameNo(String microFilmFrameNo) {
		this.microFilmFrameNo = microFilmFrameNo;
	}

	public String getBarcode() {
		return barcode;
	}

	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}

	public boolean isNASPreserveReq() {
		return isNASPreserveReq;
	}

	public void setNASPreserveReq(boolean isNASPreserveReq) {
		this.isNASPreserveReq = isNASPreserveReq;
	}

	public String getMediaType() {
		return mediaType;
	}

	public void setMediaType(String mediaType) {
		this.mediaType = mediaType;
	}

	public String getMainRecordId() {
		return mainRecordId;
	}

	public void setMainRecordId(String mainRecordId) {
		this.mainRecordId = mainRecordId;
	}

	public List<String> getSubRecordIds() {
		return subRecordIds;
	}

	public void setSubRecordIds(List<String> subRecordIds) {
		this.subRecordIds = subRecordIds;
	}

	public String getFnRecordType() {
		return fnRecordType;
	}

	public void setFnRecordType(String fnRecordType) {
		this.fnRecordType = fnRecordType;
	}
	
	public String getLinkName() {
		return linkName;
	}

	public void setLinkName(String linkName) {
		this.linkName = linkName;
	}
	
	public List<String> getRecordIdsList() {
		return recordIdsList;
	}

	public void setRecordIdsList(List<String> recordIdsList) {
		this.recordIdsList = recordIdsList;
	}
	
	public int getRecordCount() {
		return recordCount;
	}

	public void setRecordCount(int recordCount) {
		this.recordCount = recordCount;
	}
	
	public List<String> getDocumentIdsList() {
		return documentIdsList;
	}

	public void setDocumentIdsList(List<String> documentIdsList) {
		this.documentIdsList = documentIdsList;
	}

	/*public String getLocationId() {
	return locationId;
}

	public void setLocationId(String locationId) {
		this.locationId = locationId;
	}*/

	@Override
	public String toString() {
		return "RecordBean [folRefNum=" + folRefNum + ", volRefNumber=" + volRefNumber + ", recRefNo=" + recRefNo
				+ ", title=" + title + ", description=" + description + ", tags=" + tags + ", author=" + author
				+ ", authorOrganisation=" + authorOrganisation + ", creator=" + creator + ", creatorDesignation="
				+ creatorDesignation + ", recordType=" + recordType + ", secClass=" + secClass + ", locationCode="
				+ locationCode + ", ownerRole=" + ownerRole + ", ownerDepartment=" + ownerDepartment
				+ ", recordClassName=" + recordClassName + ", recordFolderId=" + recordFolderId + ", recordId="
				+ recordId + ", reasonToMove=" + reasonToMove + ", reasonToDelete=" + reasonToDelete
				+ ", sourceVolumeId=" + sourceVolumeId + ", destVolumeId=" + destVolumeId + ", documentBeanList="
				+ documentBeanList + ", mediaNumber=" + mediaNumber + ", microFilmRollNo=" + microFilmRollNo
				+ ", microFilmFrameNo=" + microFilmFrameNo + ", inventoryClassId=" + inventoryClassId + ", department="
				+ department + ", easLink=" + easLink + ", oldFrn=" + oldFrn + ", writtenDate=" + writtenDate
				+ ", isDeleted=" + isDeleted + ", mvStatus=" + mvStatus + ", mvLocation=" + mvLocation + ", mvDate="
				+ mvDate + ", mvUser=" + mvUser + ", mukimNo=" + mukimNo + ", lotNo=" + lotNo + ", barcode=" + barcode
				+ ", isNASPreserveReq=" + isNASPreserveReq + ", mediaType=" + mediaType + ", mainRecordId="
				+ mainRecordId + ", subRecordIds=" + subRecordIds + ", fnRecordType=" + fnRecordType + ", linkName="
				+ linkName + ", recordIdsList=" + recordIdsList + ", recordCount=" + recordCount + ", documentIdsList="
				+ documentIdsList + "]";
	}
}
