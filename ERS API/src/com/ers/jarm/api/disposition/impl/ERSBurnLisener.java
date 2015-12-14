package com.ers.jarm.api.disposition.impl;

import java.io.IOException;

import javax.servlet.ServletResponse;

import ji.burn.jiBurner;
import ji.burn.jiBurnerEvent;
import ji.burn.jiBurnerListener;

import org.apache.log4j.Logger;

public class ERSBurnLisener implements jiBurnerListener {
	
    private static final Logger logger = Logger.getLogger(ERSBurnLisener.class);

    private ServletResponse response = null;

    /**
     * Constructor for the BurnListener. Simply takes the ServletResponse and PrintWriter references
     * and stores them locally.
     * 
     * @param aResponse
     *            the ServletResponse object used to talk to the client.
     */
    public ERSBurnLisener(ServletResponse aResponse) {
        response = aResponse;
    }

    /**
     * This method takes a burner event and sends a message to the viewer with the event text and
     * the percentage of the burn completed.
     * 
     * @param jibeEvent
     *            the burner event.
     */
    public void burnUpdate(jiBurnerEvent jibeEvent) {
        try
        {
            // Let the viewer know that there has been an update in burn progress.
            jiBurner.sendProgressText(response.getOutputStream(),
                    jibeEvent.getPercent(),
                    jibeEvent.getText());
            logger.debug(jibeEvent.getPercent()+"---"+jibeEvent.getText());
        } catch (IOException ioException) 
        {
        	logger.error("Exception Message:: "+ioException.getMessage(),ioException);
        }
    }
}
