package com.ers.jarm.api.exception;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

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
 * This class is used for ERS Exception to handle the custom exception
 * 
 * @class name ERSException.java
 * @date  sep,2014
 * @author MITS
 * @version 1.0
 */
public class ERSException extends Exception {

	private static final long serialVersionUID = 1L;

	public ERSException(Exception exception) {

		super(exception);
	}

	public ERSException(String message, Throwable throwable) {

		super(message, throwable);
	}

	public ERSException(String message) {

		super(message);
	}

	public String getStackTrace(Throwable exception) throws IOException {

		StringWriter sw = null;

		PrintWriter pw = null;

		try {

			sw = new StringWriter();

			pw = new PrintWriter(sw);

			exception.printStackTrace(pw);

			return sw.toString();

		} finally {

			sw.close();

			sw = null;

			pw.close();

			pw = null;
		}
	}


}
