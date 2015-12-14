package com.ers.jarm.api.location;

import com.ers.jarm.api.bean.LocationBean;
import com.ers.jarm.api.exception.ERSException;
import com.ibm.jarm.api.core.Location;
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
 * This Interface is used for Location Operations to create location, 
 * fetch locations and retrieve locations
 * 
 * @class name ILocationOperations.java
 * @date  sep,2014
 * @author MITS
 * @version 1.0
 */
public interface ILocationOperations {
	
	public Location fetchPhysicalLoactions(LocationBean locationBean) throws ERSException;
	
	public Location createPhysicalLocation(LocationBean locationBean) throws ERSException;
	
	public Location getLocationById(LocationBean locationBean) throws ERSException;
}
