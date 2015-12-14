package com.ers.jarm.api.disposition;

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
 * This Interface is used for Disposition Operations to dispose folder volume, 
 * archive folder volume, extend retention period and adhoc disposition.
 * 
 * @class name IDispositionOperations.java
 * @date  sep,2014
 * @author MITS
 * @version 1.0
 */
public interface IDispositionOperations {

	public void disposeFolderVolume(VolumeBean volumeBean) throws ERSException;
	
	public void extendRetentionPeriod(VolumeBean volumeBean) throws ERSException;
	
	public void archiveRecordVolume(VolumeBean volumeBean) throws ERSException;
	
	public void adhocDisposition(VolumeBean volumeBean) throws ERSException;
}
