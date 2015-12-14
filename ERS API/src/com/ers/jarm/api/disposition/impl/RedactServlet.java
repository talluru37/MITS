package com.ers.jarm.api.disposition.impl;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.ers.jarm.api.constants.ERSConstants;
import com.ers.jarm.api.exception.ERSException;
import com.ers.jarm.api.resources.ResourceLoader;

import ji.burn.jiBurner;

/**
 * Servlet implementation class RedactServlet
 */
public class RedactServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static Logger logger=Logger.getLogger(RedactServlet.class);

	protected ServletContext application = null;

	//private static Properties messageProperties = null;

	private String burnLibraryAbsolutePath = null;;
	private String burnWorkingFolderPath = null;
	private String burnLoggerPath = null;
	protected ServletConfig config = null;

	public void init() throws ServletException {
		try {

			application = getServletContext();		

			ResourceLoader.loadProperties();

		} catch (Exception objException) {

			logger.error("exception in Redact Servlet init method : " +  objException.getMessage(), objException);
		}
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		try {

			redactAndSaveDocumentToFN(request, response);

		} catch (Exception exceptionobj) {

			logger.error("Exception occured in doPost method in Redact Servlet " + exceptionobj.getMessage(), exceptionobj);
		} 
	}

	/***
	 * Method to Read the request and send the response to service method after while performing redaction
	 * @param request
	 * @param response
	 * @exception IOException, RedactException
	 * */
	private void redactAndSaveDocumentToFN(HttpServletRequest request, HttpServletResponse response) throws IOException, ERSException {

		logger.info("Entered into the serviceHelper method of the class RedactServlet");

		OutputStream objOutStream = null;

		FileWriter objFileWriter = null;

		PrintWriter burnLogger = null;

		String configLicensePath = null;

		String burnfileName = null;

		String licensePath = null;

		jiBurner objBurner = null;

		String configPath = null;

		ERSBurnLisener burnListener = null;		

		try {

			configPath = ResourceLoader.getMessageProperties().getProperty(ERSConstants.RESOURCE_PATH);

			// Resource library path for burner
			burnLibraryAbsolutePath = application.getRealPath(configPath);

			logger.info("burnLibraryAbsolutePath : " + burnLibraryAbsolutePath);

			// working path and burn logger path for burner
			burnWorkingFolderPath = request.getParameter("WorkingPath");

			logger.info("burnWorkingFolderPath : " + burnWorkingFolderPath);	

			burnLoggerPath = ResourceLoader.getMessageProperties().getProperty(ERSConstants.LOG_FILE);

			logger.info("burnLoggerPath : " + burnLoggerPath);

			//licence file path for burner
			configLicensePath = ResourceLoader.getMessageProperties().getProperty(ERSConstants.LICENSE_PATH);

			logger.info("configLicensePath : " + configLicensePath);

			licensePath = application.getRealPath(configLicensePath);

			if(licensePath != null) {

				objOutStream = response.getOutputStream();

				objFileWriter = new FileWriter(burnLoggerPath);

				burnLogger = new PrintWriter(objFileWriter);

				String iniFileName = request.getParameter("IniFileName");

				logger.info("iniPath1 : " + iniFileName);

				//String iniFilePath = burnWorkingFolderPath + "/" + iniFileName;

				burnListener = new ERSBurnLisener(response);

				// Creating burner object
				objBurner = loadAndInitilizeBurnLibrary(burnListener, burnLogger, burnLibraryAbsolutePath, burnWorkingFolderPath, licensePath);

				logger.info("Object Burner : " + objBurner);

				if(null != objBurner)  {

					String imageFileName = request.getParameter("DocumentName");

					String imageFilePath = burnWorkingFolderPath + "/" + imageFileName;

					logger.info("image Path : "+imageFilePath);

					//save document to local drive
					//localSavePath = objEDRMRedactHelper.storeLocalFile(objObjectStore, strGuid);
					burnfileName = burnINI(objOutStream, iniFileName, objBurner, imageFilePath);
					logger.info("burner file Name  : " + burnfileName);			
					burnLogger.flush();

				} else {

					logger.error("CreateBurner returns null value");
				}

			} else {

				logger.error("License file path is null");
			}
		} 
		catch (IOException ioException)
		{
			logger.error("Exception :: "+ioException.getMessage(),ioException);

			throw new ERSException(ioException.getMessage());
		} 
		catch (Exception exception) 
		{
			logger.error(" Exception :: "+exception.getMessage(),exception);

			throw new ERSException(exception.getMessage());
		}
		finally 
		{
			releaseBurnResources(objBurner);

			if(objOutStream != null) 
			{
				objOutStream.close();
			}
			if(objFileWriter != null)
			{
				objFileWriter.close();
			}
			objOutStream =null;
			objFileWriter=null;
			burnfileName = null;
			configLicensePath = null;		
			licensePath =null;
			objBurner =null;

		}

		logger.debug("End of the serviceHelper method of the class EDRMRedactServlet");
	}

	/**
	 * method to release the resources of a burner after burning
	 * @param burner
	 * **/
	private void releaseBurnResources(jiBurner burner) throws ERSException
	{
		logger.debug("Entered into  releaseBurnResources the method of the class EDRMRedactServlet");
		try 
		{
			if (burner != null) 
			{
				burner.releaseResources();
			}
		} 
		catch (Exception exception) 
		{
			logger.error("Exception Messsage "+exception.getMessage(),exception);

			throw new ERSException(exception.getMessage());
		}
		logger.debug("End of the releaseBurnResources method of the class EDRMRedactServlet");	
	}

	public String burnINI(OutputStream outStream,String sINIFileName, jiBurner burner, String fileName) throws Exception
	{
		logger.info("Entered into the burnINI method of the class BurnHandler");

		//logger.info("sINIFileName : " + sINIFileName + "fileName : " + fileName);

		String burnfilePath = "";
		
		try 
		{
			if((sINIFileName != null) && (burner != null) && null != fileName && outStream != null)
			{
				logger.info("FileName :"+fileName+" Burner :"+burner + "sINIFileName :" + sINIFileName + "outStream : " + outStream);
				burnfilePath = burner.burnAnnotationsFromIni(fileName, sINIFileName);
				if(burnfilePath != null) {
					logger.error("burnfilePath : " + burnfilePath);					
				}
				logger.info(" BurnerFilePath :: "+burnfilePath);
			}
			else 
			{
				logger.error(" Error while retrieving burn annotations XML from the applet. ");
				jiBurner.sendCompletionFailed(outStream,"Error while retrieving burn annotations XML from the applet.");
				throw new ERSException("Error while retrieving burn annotations XML from the applet.");
			}
		} 
		catch (IOException ioException) 
		{
			logger.error("IOException :: "+ioException.getMessage() ,ioException);
			jiBurner.sendCompletionFailed(outStream,ioException.getLocalizedMessage());
			throw new ERSException(ioException.getMessage());
		} 
		catch (IllegalStateException iiIllegalStateException)
		{
			logger.error("IllegalStateException :: "+iiIllegalStateException.getMessage() ,iiIllegalStateException);
			jiBurner.sendCompletionFailed(outStream,iiIllegalStateException.getLocalizedMessage());
			throw new ERSException(iiIllegalStateException.getMessage(),iiIllegalStateException);
		}
		catch (Exception exception) 
		{
			logger.error("Exception ::"+exception.getMessage() ,exception);
			jiBurner.sendCompletionFailed(outStream,exception.getLocalizedMessage());
			exception.printStackTrace();
			throw new ERSException(exception.getMessage(),exception);
		}

		logger.info("End of the burnINI method of the class BurnHandler");
		return burnfilePath;
	}

	private jiBurner loadAndInitilizeBurnLibrary(ERSBurnLisener burnListener, PrintWriter burnLogger, String burnLibraryAbsolutePath,
			String burnWorkingFolderPath,String licensePath) throws IOException, ERSException {

		logger.info("In Create loadAndInitilizeBurnLibrary Method");
		jiBurner burner = null;
		try {
			burner =  new jiBurner(burnLibraryAbsolutePath, burnWorkingFolderPath, licensePath, burnListener, burnLogger);
			if (burner != null) {
				burner.setExtendedLogging(true);
				burner.setBurnPDFToPDF(true);
				burner.setBurnAnyFileFormat(true);
				burner.setBurnColor(true);
				burner.setIgnoreInvalidAnnotations(true);
				Properties props = new Properties();
				props.setProperty(ResourceLoader.getMessageProperties().getProperty(ERSConstants.BURNER_PROPERTIES_TIFF_SAVEFORMAT_KEYNAME), 
						ResourceLoader.getMessageProperties().getProperty(ERSConstants.BURNER_PROPERTIES_TIFF_SAVEFORMAT_VALUE));
				props.setProperty(ResourceLoader.getMessageProperties().getProperty(ERSConstants.BURNER_PROPERTIES_TIFF_SAVEMONOG4_KEYNAME),
						ResourceLoader.getMessageProperties().getProperty(ERSConstants.BURNER_PROPERTIES_TIFF_SAVEMONOG4_VALUE));
				props.setProperty(ResourceLoader.getMessageProperties().getProperty(ERSConstants.BURNER_PROPERTIES_TIFF_SAVECOLOR_KEYNAME), 
						ResourceLoader.getMessageProperties().getProperty(ERSConstants.BURNER_PROPERTIES_TIFF_SAVECOLOR_VALUE));
				props.setProperty(ResourceLoader.getMessageProperties().getProperty(ERSConstants.BURNER_PROPERTIES_CONVERT_TO_TIFF_ONSAVE_KEYNAME),
						ResourceLoader.getMessageProperties().getProperty(ERSConstants.BURNER_PROPERTIES_CONVERT_TO_TIFF_ONSAVE_VALUE)); 
				props.setProperty(ResourceLoader.getMessageProperties().getProperty(ERSConstants.BURNER_PROPERTIES_TIFF_JPEG_WRT_QUALITY_KEYNAME),
						ResourceLoader.getMessageProperties().getProperty(ERSConstants.BURNER_PROPERTIES_TIFF_JPEG_WRT_QUALITY_VALUE));
				burner.setOutputFormat(ResourceLoader.getMessageProperties().getProperty(ERSConstants.BURNER_PROPERTIES_OUTFORMAT_KEYNAME),props);
				burner.setIgnoreEmptyAnnotations(true);
			}
		} catch (FileNotFoundException fileNotFoundException) {
			//response.getWriter().write("Burner initialization failed. " + fileNotFoundException.getMessage());
			logger.error("Exception Message ::" + fileNotFoundException.getMessage());
			//jiBurner.sendCompletionFailed(response.getOutputStream(),	"Burner initialization failed. " + fileNotFoundException.getMessage());
			throw new ERSException(fileNotFoundException.getMessage(), fileNotFoundException);
		} catch(Exception exception){
			//response.getWriter().write("Burner initialization failed. " + exception.getMessage());
			logger.error("Exception Message ::" + exception.getMessage());
			//jiBurner.sendCompletionFailed(response.getOutputStream(),"Burner initialization failed. " + exception.getMessage());
			throw new ERSException(exception.getMessage(), exception);
		}
		return burner;
	}

}
