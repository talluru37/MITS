package com.ers.jarm.api.bean;

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
 * This SearchBean class is used in Search Operations to 
 * search the content
 * 
 * @class name  SearchBean.java
 * @date  sep,2014
 * @author MITS
 * @version 1.0
 */
public class SearchBean {
	
	private String searchString;
	
	private List<String> folderIds;
	
	private boolean onlySearchPDF;
	
	private int pageSize;
	
	//private String folderPathToSearch;
	
	public String getSearchString() {
		return searchString;
	}

	public void setSearchString(String searchString) {
		this.searchString = searchString;
	}

	public List<String> getFolderIds() {
		return folderIds;
	}

	public void setFolderIds(List<String> folderIds) {
		this.folderIds = folderIds;
	}

	public boolean isOnlySearchPDF() {
		return onlySearchPDF;
	}

	public void setOnlySearchPDF(boolean onlySearchPDF) {
		this.onlySearchPDF = onlySearchPDF;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	@Override
	public String toString() {
		return "SearchBean [searchString=" + searchString + ", folderIds=" + folderIds + ", onlySearchPDF="
				+ onlySearchPDF + ", pageSize=" + pageSize + "]";
	}

}
