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
 * This DocumentBean class is used in Records operations to 
 * create document for electronic record
 * 
 * @class name  DocumentBean.java
 * @date  sep,2014
 * @author MITS
 * @version 1.0
 */
public class DocumentBean {
	
	private String fileName;
	
	private String mimeType;
	
	private byte[] documentByteArray;
	
	private boolean isSearchablePDF;
	
	private String documentId;
	
	private String fdrRefNo;
	
	private int volNo;
	
	private int recRefNo;
	
	private List<String> documentIdsList;
	
	private Date dateCreated;
	

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public byte[] getDocumentByteArray() {
		return documentByteArray;
	}

	public void setDocumentByteArray(byte[] documentByteArray) {
		this.documentByteArray = documentByteArray;
	}

	/*public boolean getIsSearchablePdf() {
		return isSearchablePdf;
	}

	public void setIsSearchablePdf(boolean isSearchablePdf) {
		this.isSearchablePdf = isSearchablePdf;
	}*/

	public String getDocumentId() {
		return documentId;
	}

	public void setDocumentId(String documentId) {
		this.documentId = documentId;
	}

	public String getFdrRefNo() {
		return fdrRefNo;
	}

	public void setFdrRefNo(String fdrRefNo) {
		this.fdrRefNo = fdrRefNo;
	}

	public int getVolNo() {
		return volNo;
	}

	public void setVolNo(int volNo) {
		this.volNo = volNo;
	}

	public int getRecRefNo() {
		return recRefNo;
	}

	public void setRecRefNo(int recRefNo) {
		this.recRefNo = recRefNo;
	}
	
	public boolean isSearchablePDF() {
		return isSearchablePDF;
	}

	public void setSearchablePDF(boolean isSearchablePDF) {
		this.isSearchablePDF = isSearchablePDF;
	}
	
	public List<String> getDocumentIdsList() {
		return documentIdsList;
	}

	public void setDocumentIdsList(List<String> documentIdsList) {
		this.documentIdsList = documentIdsList;
	}
	
	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	@Override
	public String toString() {
		return "DocumentBean [fileName=" + fileName + ", mimeType=" + mimeType
				+ ", documentByteArray=" + ((documentByteArray == null) ? "" : documentByteArray.length)
				+ ", isSearchablePDF=" + isSearchablePDF + ", documentId="
				+ documentId + ", fdrRefNo=" + fdrRefNo + ", volNo=" + volNo
				+ ", recRefNo=" + recRefNo + "]";
	}
	/*@Override
	public String toString() {
		return "DocumentBean [fileName=" + fileName + ", mimeType=" + mimeType
				+ ", documentByteArray=" + 				
				((documentByteArray == null) ? "" : documentByteArray.length)
				+ ", documentId=" + documentId + ", fdrRefNo=" + fdrRefNo
				+ ", volNo=" + volNo + ", recRefNo=" + recRefNo + "]";
	}*/
	
	

}
