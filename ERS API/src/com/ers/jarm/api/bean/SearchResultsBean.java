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
 * This SearchResultsBean class is used in Search Operations to 
 * return the search results
 * 
 * @class name  SearchResultsBean.java
 * @date  sep,2014
 * @author MITS
 * @version 1.0
 */
public class SearchResultsBean {
	
private String folRefNum;
	
	private String volRefNumber;
	
	private String recRefNo;
	
	private String title;
	
	private String description;
	
	private String tags;
	
	private String author;
	
	private String authorOrganisation;
	
	private String creatorDesignation;
	
	private String recordType;
	
	private String secClass;
	
	private String locationCode;
	
	private String ownerRole;
	
	private String ownerDepartment;
	
	private String mediaNumber;
	
	private String microFilimRollNo;
	
	private String microFilimFrameNo;
	
	private String inventory;
	
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
	
	private String recordId;
	
	private List<DocumentBean> listDocumentBean;
	
	private List<String> recordIds;
	
	private List<String> documentIds;
	
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

	public String getMediaNumber() {
		return mediaNumber;
	}

	public void setMediaNumber(String mediaNumber) {
		this.mediaNumber = mediaNumber;
	}

	public String getMicroFilimRollNo() {
		return microFilimRollNo;
	}

	public void setMicroFilimRollNo(String microFilimRollNo) {
		this.microFilimRollNo = microFilimRollNo;
	}

	public String getMicroFilimFrameNo() {
		return microFilimFrameNo;
	}

	public void setMicroFilimFrameNo(String microFilimFrameNo) {
		this.microFilimFrameNo = microFilimFrameNo;
	}

	public String getInventory() {
		return inventory;
	}

	public void setInventory(String inventory) {
		this.inventory = inventory;
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

	public String getRecordId() {
		return recordId;
	}

	public void setRecordId(String recordId) {
		this.recordId = recordId;
	}
	
	public List<DocumentBean> getListDocumentBean() {
		return listDocumentBean;
	}

	public void setListDocumentBean(List<DocumentBean> listDocumentBean) {
		this.listDocumentBean = listDocumentBean;
	}
	
	public List<String> getRecordIds() {
		return recordIds;
	}

	public void setRecordIds(List<String> recordIds) {
		this.recordIds = recordIds;
	}

	public List<String> getDocumentIds() {
		return documentIds;
	}

	public void setDocumentIds(List<String> documentIds) {
		this.documentIds = documentIds;
	}

	@Override
	public String toString() {
		return "SearchResultsBean [folRefNum=" + folRefNum + ", volRefNumber="
				+ volRefNumber + ", recRefNo=" + recRefNo + ", title=" + title
				+ ", description=" + description + ", tags=" + tags
				+ ", author=" + author + ", authorOrganisation="
				+ authorOrganisation + ", creatorDesignation="
				+ creatorDesignation + ", recordType=" + recordType
				+ ", secClass=" + secClass + ", locationCode=" + locationCode
				+ ", ownerRole=" + ownerRole + ", ownerDepartment="
				+ ownerDepartment + ", mediaNumber=" + mediaNumber
				+ ", microFilimRollNo=" + microFilimRollNo
				+ ", microFilimFrameNo=" + microFilimFrameNo + ", inventory="
				+ inventory + ", department=" + department + ", easLink="
				+ easLink + ", oldFrn=" + oldFrn + ", writtenDate="
				+ writtenDate + ", isDeleted=" + isDeleted + ", mvStatus="
				+ mvStatus + ", mvLocation=" + mvLocation + ", mvDate="
				+ mvDate + ", mvUser=" + mvUser + ", mukimNo=" + mukimNo
				+ ", lotNo=" + lotNo + ", barcode=" + barcode
				+ ", isNASPreserveReq=" + isNASPreserveReq + ", mediaType="
				+ mediaType + ", recordId=" + recordId + ", listDocumentBean="
				+ listDocumentBean + "]";
	}
}
