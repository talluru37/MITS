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
 * This TaxonomyOperationsBean class is used in Taxonomy OperationsBean to 
 * create taxonomy and delete taxonomy
 * 
 * @class name  TaxonomyOperationsBean.java
 * @date  sep,2014
 * @author MITS
 * @version 1.0
 */
public class TaxonomyOperationsBean {
	
	private String recordCategoryName;
	
	private String recordCategoryIdentifier;
	
	private String securityClassfication;
	
	private String ownerRole;
	
	private String parentRecordCategoryId;
	
	private String recordCategoryId;

	public String getRecordCategoryName() {
		return recordCategoryName;
	}

	public void setRecordCategoryName(String recordCategoryName) {
		this.recordCategoryName = recordCategoryName;
	}

	public String getRecordCategoryIdentifier() {
		return recordCategoryIdentifier;
	}

	public void setRecordCategoryIdentifier(String recordCategoryIdentifier) {
		this.recordCategoryIdentifier = recordCategoryIdentifier;
	}

	public String getSecurityClassfication() {
		return securityClassfication;
	}

	public void setSecurityClassfication(String securityClassfication) {
		this.securityClassfication = securityClassfication;
	}

	public String getOwnerRole() {
		return ownerRole;
	}

	public void setOwnerRole(String ownerRole) {
		this.ownerRole = ownerRole;
	}

	public String getRecordCategoryId() {
		return recordCategoryId;
	}

	public void setRecordCategoryId(String recordCategoryId) {
		this.recordCategoryId = recordCategoryId;
	}
	
	public String getParentRecordCategoryId() {
		return parentRecordCategoryId;
	}

	public void setParentRecordCategoryId(String parentRecordCategoryId) {
		this.parentRecordCategoryId = parentRecordCategoryId;
	}

	@Override
	public String toString() {
		return "TaxonomyOperationsBean [recordCategoryName=" + recordCategoryName + ", recordCategoryIdentifier="
				+ recordCategoryIdentifier + ", securityClassfication=" + securityClassfication + ", ownerRole="
				+ ownerRole + ", parentRecordCategoryId=" + parentRecordCategoryId + ", recordCategoryId="
				+ recordCategoryId + "]";
	}
	

}
