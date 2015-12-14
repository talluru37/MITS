package com.ers.jarm.api.folder;

import com.ers.jarm.api.bean.RecordFolderBean;
import com.ers.jarm.api.bean.VolumeBean;
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
 * This Interface is used for Record folder Operations to create folder, 
 * view folder, update folder and delete folder
 * 
 * @class name IRecordFolderOperations.java
 * @date  sep,2014
 * @author MITS
 * @version 1.0
 */
public interface IRecordFolderOperations {

	public RecordFolderBean createRecordFolder(RecordFolderBean recordFolderBean, VolumeBean volumeBean) throws ERSException;

	public RecordFolderBean viewRecordFolderMetadata(RecordFolderBean recordFolderBean) throws ERSException;

	public boolean updateRecordFolderMetadata(RecordFolderBean folderBean) throws ERSException;

	public void moveRecordFolder(RecordFolderBean recordFolderBean) throws ERSException;

	public void closeRecordFolder(RecordFolderBean recordFolderBean) throws ERSException;

	public void freezeRecordFolder(RecordFolderBean recordFolderBean) throws ERSException;
	
	public void unFreezeRecordFolder(RecordFolderBean folderBean) throws ERSException;

	public void deleteRecordFolder(RecordFolderBean recordFolderBean) throws ERSException;
	
	public RecordFolderBean createRecordVolume(RecordFolderBean recordFolderBean, VolumeBean volumeBean) throws ERSException;

}
