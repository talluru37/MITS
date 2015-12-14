package com.ers.jarm.api.record;

import java.util.List;

import com.ers.jarm.api.bean.RecordBean;
import com.ers.jarm.api.exception.ERSException;

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
 * This Interface is used for Record Operations to create record, 
 * view record, update record and delete record
 * 
 * @class name IRecordOperations.java
 * @date  sep,2014
 * @author MITS
 * @version 1.0
 */
public interface IRecordOperations {

	public RecordBean createRecord(RecordBean recordBean) throws ERSException;

	public void updateRecordMetadata(RecordBean recordBean) throws ERSException;

	public RecordBean viewRecordMetadata(RecordBean recordBean) throws ERSException;

	public List<RecordBean> createRecordsUsingBDS(List<RecordBean> recordBeanList) throws ERSException;

	public void moveRecord(RecordBean recordBean) throws ERSException;

	public void deleteRecord(RecordBean recordBean) throws ERSException;
	
	public void linkRecords(RecordBean RecordBean) throws ERSException;
	
	public void moveMultipleRecords(RecordBean recordBean) throws ERSException;

}
