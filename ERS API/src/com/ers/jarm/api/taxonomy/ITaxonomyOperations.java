package com.ers.jarm.api.taxonomy;

import com.ers.jarm.api.bean.TaxonomyOperationsBean;
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
 * This Interface is used for taxonomy operations to 
 * create taxonomy and delete taxonomy
 * 
 * @class name ITaxonomyOperations.java
 * @date  sep,2014
 * @author MITS
 * @version 1.0
 */
public interface ITaxonomyOperations {

	public void createRecordCategory(TaxonomyOperationsBean taxonomyOperationsBean) throws ERSException;

	public void deleteRecordCategory(TaxonomyOperationsBean taxonomyOperationsBean) throws ERSException;
}
