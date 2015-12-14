package com.ers.jarm.api.disposition.impl;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.ers.jarm.api.bean.VolumeBean;
import com.ers.jarm.api.connection.CEConnection;
import com.ers.jarm.api.connection.RMConnection;
import com.ers.jarm.api.constants.ERSConstants;
import com.ers.jarm.api.constants.ERSVolumeProperty;
import com.ers.jarm.api.disposition.IDispositionOperations;
import com.ers.jarm.api.exception.ERSException;
import com.ers.jarm.api.resources.ResourceLoader;
import com.filenet.api.collection.ContentElementList;
import com.filenet.api.collection.DocumentSet;
import com.filenet.api.core.ContentTransfer;
import com.filenet.api.core.Document;
import com.filenet.api.core.Factory;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;
import com.ibm.jarm.api.collection.PageableSet;
import com.ibm.jarm.api.collection.RMPageIterator;
import com.ibm.jarm.api.constants.DeleteMode;
import com.ibm.jarm.api.constants.RMRefreshMode;
import com.ibm.jarm.api.core.ContentItem;
import com.ibm.jarm.api.core.FilePlanRepository;
import com.ibm.jarm.api.core.RMContentElement;
import com.ibm.jarm.api.core.RMFactory;
import com.ibm.jarm.api.core.Record;
import com.ibm.jarm.api.core.RecordVolume;
import com.ibm.jarm.api.exception.RMRuntimeException;
import com.ibm.jarm.api.meta.RMClassDescription;
import com.ibm.jarm.api.meta.RMPropertyDescription;
import com.ibm.jarm.api.property.RMProperties;
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
 * @class name DispositionOperationsImpl.java
 * @date  sep,2014
 * @author MITS
 * @version 1.0
 */
public class DispositionOperationsImpl implements IDispositionOperations {

	static Logger logger=Logger.getLogger(DispositionOperationsImpl.class);

	public DispositionOperationsImpl() throws ERSException {

		try
		{
			ResourceLoader.loadProperties();

		}
		catch (Exception objException) {

			logger.error("Exception occurred in DispositionOperationsImpl constructor [Properties are not loaded] : " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());
		}
	}

	/**
	 * This method is used dispose the folder volume
	 * @param volumeBean
	 * @throws ERSException
	 */
	public void disposeFolderVolume(VolumeBean volumeBean) throws ERSException {

		logger.info("Entered into disposeFolderVolume method");

		logger.info("volumeBean : " + volumeBean.toString());

		FilePlanRepository filePlanRepository = null;

		RMConnection rmConnection = null;

		RecordVolume recordVolume = null;

		PageableSet<Record> rcResultSet = null;

		RMPageIterator<Record> rcPI = null;

		List<Record> rcPage =null;

		Record	fetchRecord =null;

		try {

			rmConnection = new RMConnection();

			filePlanRepository =rmConnection.getFilePlanRepositoryByName(ResourceLoader.getMessageProperties().getProperty(ERSConstants.FILEPLAN_OBJECTSTORE));

			recordVolume = RMFactory.RecordVolume.fetchInstance(filePlanRepository,volumeBean.getVolumeId(),null);

			rcResultSet = recordVolume.getRecords(300);

			rcPI = rcResultSet.pageIterator();

			while ( rcPI.nextPage() ) {

				rcPage = rcPI.getCurrentPage();

				for (Record record : rcPage) {

					logger.info("Name of each record:"+record.getName());

					fetchRecord = RMFactory.Record.fetchInstance(filePlanRepository, record.getObjectIdentity(), null);

					fetchRecord.delete(true, DeleteMode.ForceLogicalDelete, null);

					fetchRecord.save(RMRefreshMode.Refresh);		
				} 
			}  

			recordVolume.close("In Disposition volume is closed");

			recordVolume.getProperties().putStringValue(ERSVolumeProperty.VOL_LFCY_ST, ResourceLoader.getMessageProperties().getProperty(ERSConstants.VOLUME_STATUS_ARCHIVE));

		} catch (RMRuntimeException objException) {

			logger.error("RMRuntimeException occurred in disposeFolderVolume method : " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} catch(Exception objException) {

			logger.error("Exception occurred in disposeFolderVolume method : " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());
		}  	
		finally
		{
			if(filePlanRepository != null) {
				filePlanRepository = null;
			}

			if(rmConnection != null) {
				rmConnection.closeRMConnection();
				rmConnection = null;
			}
			recordVolume = null;

			rcResultSet = null;

			rcPI = null;

			rcPage = null;

			fetchRecord = null;
		}
	}

	/**
	 * This method is used to extend the retention period
	 * @param volumeBean
	 * @throws ERSException
	 */
	public void extendRetentionPeriod(VolumeBean volumeBean) throws ERSException {

		logger.info("Entered into extendRetentionPeriod method");

		logger.info("volumeBean : " + volumeBean.toString());

		FilePlanRepository filePlanRepository = null;

		RMConnection rmConnection = null;

		RecordVolume recordVolume = null;

		RMProperties rmProperties = null;

		//DispositionSchedule dispositionSchedule = null;

		Date lastEnclDate = null;

		int rndPeriod = 0;

		int rndPeriodInYears = 0;

		int rndPeriodInMonths = 0;

		int extendedYear = 0;

		int extendedMonth = 0;
		
		GregorianCalendar latestDispDate = null;
		
		Calendar calender = null;

		try {

			rmConnection = new RMConnection();

			filePlanRepository = rmConnection.getFilePlanRepositoryByName(ResourceLoader.getMessageProperties().getProperty(ERSConstants.FILEPLAN_OBJECTSTORE));

			recordVolume = RMFactory.RecordVolume.fetchInstance(filePlanRepository, volumeBean.getVolumeId(), null);

			logger.info("recordVolume : " + recordVolume.getVolumeName());

			rmProperties = recordVolume.getProperties();

			lastEnclDate = rmProperties.getDateTimeValue(ERSVolumeProperty.LAST_ENCL_DATE);

			rndPeriod = volumeBean.getRndPeriod();

			rndPeriodInYears = rndPeriod/12;

			rndPeriodInMonths = rndPeriod%12;

			calender = Calendar.getInstance();
			
			calender.setTime(lastEnclDate);
			
			extendedYear = calender.get(GregorianCalendar.YEAR) + rndPeriodInYears;
			
			extendedMonth = calender.get(GregorianCalendar.MONTH) + rndPeriodInMonths ;
			
			latestDispDate = new GregorianCalendar(extendedYear, extendedMonth, calender.get(Calendar.DATE));

			/*extendedYear = lastEnclDate.getYear() + rndPeriodInYears;

			extendedMonth = lastEnclDate.getMonth() + rndPeriodInMonths;

			Date latestDispDate = new Date(extendedYear, extendedMonth, lastEnclDate.getDate());*/

			logger.info("latestDispDate : " + latestDispDate.getTime());

			rmProperties.putIntegerValue(ERSVolumeProperty.CURRENT_PHASE_EXECUTION_STATUS, Integer.parseInt(ResourceLoader.getMessageProperties().getProperty(ERSConstants.CURRENT_PHASE_EXECUTION_STATUS)));

			rmProperties.putDateTimeValue(ERSVolumeProperty.DISPOSAL_DATE, latestDispDate.getTime());

			rmProperties.putDateTimeValue(ERSVolumeProperty.CURRENT_PHASE_EXECUTION_DATE, latestDispDate.getTime());

			rmProperties.putDateTimeValue(ERSVolumeProperty.CUTOFF_DATE, latestDispDate.getTime());

			rmProperties.putIntegerValue(ERSVolumeProperty.RANDD_PERIOD, rndPeriod);

			recordVolume.save(RMRefreshMode.Refresh);

			/*RecordFolder recordFolder = (RecordFolder) recordVolume.getParent();

			logger.info("RecordFolder : " + recordFolder.getFolderName());

			ERSUtil ersUtil = new ERSUtil();

			recordFolder.clearDispositionAssignment(SchedulePropagation.ToImmediateSubContainersAndAllInheritors);

			java.util.Map<Integer, String> dispositionMap = ersUtil.getDispositionSchedule(volumeBean.getRndPeriod());

			String dispScheduleId = dispositionMap.get(volumeBean.getRndPeriod());

			logger.info("dispScheduleId : " + dispScheduleId);

			if(dispScheduleId == null) {

				dispScheduleId = ersUtil.createDispositionSchedule(volumeBean.getRndPeriod());
			}

			dispositionSchedule = RMFactory.DispositionSchedule.fetchInstance(filePlanRepository, dispScheduleId, null);			

			recordFolder.assignDispositionSchedule(dispositionSchedule, SchedulePropagation.ToAllInheritors);*/

		} catch (RMRuntimeException objException) {

			logger.error("RMRuntimeException occurred in extendRetentionPeriod method : " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} catch(Exception objException) {

			logger.error("Exception occurred in extendRetentionPeriod method : " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());
		}  	
		finally
		{
			if(filePlanRepository != null) {
				filePlanRepository = null;
			}

			if(rmConnection != null) {
				rmConnection.closeRMConnection();
				rmConnection = null;
			}

			recordVolume = null;
		}
	}

	/**
	 * This method is used Archive Record Volume
	 * @param volumeBean
	 * @throws ERSException
	 */
	public void archiveRecordVolume(VolumeBean volumeBean) throws ERSException {

		logger.info("Entered into archiveRecordVolume method");

		FilePlanRepository filePlanRepository = null;

		RMConnection rmConnection = null;

		RecordVolume recordVolume = null;

		PageableSet<Record> rcResultSet = null;

		RMPageIterator<Record> rcPI = null;

		List<Record> rcPage = null;

		Record	fetchRecord = null;

		CEConnection ceConnection = null;

		ObjectStore objectStore = null;

		File file = null;

		File recordFile = null;

		RMClassDescription rmClassDescription = null;

		List<RMPropertyDescription> listRMPropertyDescriptions = null;

		Iterator<RMPropertyDescription> listIterator = null;

		RMPropertyDescription rmPropertyDescription = null;

		String propertyName = null;

		Object propertyValue = null;

		StringBuffer stringBuffer = null;

		FileOutputStream outputStream = null;

		String documentName = null;

		PDFAnnotations pdfAnnotations = null;

		try {

			rmConnection = new RMConnection();

			filePlanRepository = rmConnection.getFilePlanRepositoryByName(ResourceLoader.getMessageProperties().getProperty(ERSConstants.FILEPLAN_OBJECTSTORE));

			recordVolume = RMFactory.RecordVolume.fetchInstance(filePlanRepository, volumeBean.getVolumeId(), null);

			file = new File("C://Documents/" + volumeBean.getVolumeId());

			file.mkdir();

			rcResultSet =recordVolume.getRecords(300);

			ceConnection = new CEConnection();

			objectStore = ceConnection.getObjectStoreWithCEWSIConnection(ResourceLoader.getMessageProperties(), ResourceLoader.getMessageProperties().getProperty(ERSConstants.RECORDS_OBJECTSTORE));

			rcPI = rcResultSet.pageIterator();

			stringBuffer = new StringBuffer();

			while ( rcPI.nextPage() ) {

				rcPage = rcPI.getCurrentPage();

				for (Record record : rcPage) {

					logger.info("Name of record " + record.getName());

					fetchRecord = RMFactory.Record.fetchInstance(filePlanRepository, record.getObjectIdentity(), null);

					String recordId = fetchRecord.getProperties().getGuidValue("Id");

					recordFile = new File(file.getAbsolutePath() + "/" + recordId);

					recordFile.mkdir();

					rmClassDescription = fetchRecord.getClassDescription();

					listRMPropertyDescriptions = rmClassDescription.getPropertyDescriptions();

					listIterator = listRMPropertyDescriptions.iterator();

					while(listIterator.hasNext()) {

						rmPropertyDescription = listIterator.next();

						if(!rmPropertyDescription.isSystemGenerated() && !rmPropertyDescription.isHidden() && !rmPropertyDescription.isRMSystemProperty()) {

							propertyName = rmPropertyDescription.getSymbolicName();

							propertyValue = fetchRecord.getProperties().get(propertyName).getObjectValue();

							stringBuffer.append(propertyValue);

							stringBuffer.append(",");
						}
					}
					stringBuffer.append("\n");

					PageableSet<ContentItem> pageableSet = fetchRecord.getAssociatedContentItems();

					Iterator<ContentItem> iterContentItem = pageableSet.iterator();

					while(iterContentItem.hasNext()) {

						ContentItem contentItem = (ContentItem) iterContentItem.next();

						String documentId = contentItem.getProperties().getGuidValue("Id");

						logger.info("documentId : " + documentId);	

						documentName = retriveDocument(objectStore, documentId, recordFile.getAbsolutePath());

						String intFileName = retrieveAnnotation(documentId, recordFile.getAbsolutePath());

						List<RMContentElement> contentElementList = contentItem.getContentElements();

						Iterator<RMContentElement> iterContentElement = contentElementList.iterator();

						while(iterContentElement.hasNext()) {

							RMContentElement rmContentElement = iterContentElement.next();

							logger.info("Content Type : " + rmContentElement.getContentType());
						}

						String extension = documentName.substring(documentName.lastIndexOf(".")+1);

						if(extension.equalsIgnoreCase("pdf")) {

							pdfAnnotations = new PDFAnnotations();

							pdfAnnotations.readIniFile(recordFile.getAbsolutePath(), documentId, documentName);							

						} else {

							URL url = new URL( "http://localhost:8181/SLAERSProject/RedactServlet?WorkingPath=" + recordFile.getAbsolutePath() + "&IniFileName=" + intFileName + "&DocumentName=" + documentName);

							//URL url = new URL("http://localhost:8181/SLAERSProject/RedactServlet?WorkingPath=C://Documents/{8DF65A22-1B8A-4E10-A727-D966FD510381}/{E97F4694-0594-7CEB-86B3-0934FA896553}&IniFileName={ECD5B04E-75A7-7D0C-4A23-44F1CF72EDCA}.ini&DocumentName=Penguins.jpg");
							String urlValue = "http://localhost:8181/SLAERSProject/RedactServlet?WorkingPath=" + recordFile.getAbsolutePath() + "&IniFileName=" + intFileName + "&DocumentName=" + documentName;

							logger.info("urlValue : " + urlValue);

							BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream())); 

							String line = in.readLine(); 

							//System.out.println( line );	

							in.close();
						}
					}
				} 
				outputStream = new FileOutputStream(new File(file.getAbsolutePath() + "\\ Metadata.csv"));

				outputStream.write(stringBuffer.toString().getBytes());
			}  

		} catch (RMRuntimeException objException) {

			logger.error("RMRuntimeException occurred in archiveRecordVolume method : " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} catch(Exception objException) {

			logger.error("Exception occurred in archiveRecordVolume method : " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());
		}  	
		finally
		{
			if(filePlanRepository != null) {
				filePlanRepository = null;
			}

			if(rmConnection != null) {
				rmConnection.closeRMConnection();
				rmConnection = null;
			}

			recordVolume = null;

			rcResultSet = null;

			rcPI = null;

			rcPage = null;

			fetchRecord = null;
		}
	}

	/**
	 * This method is used for adhocDisposition for the volume
	 * @Param volumeBean
	 */
	public void adhocDisposition(VolumeBean volumeBean) throws ERSException {

		logger.info("Entered into adhocDisposition method");

		FilePlanRepository filePlanRepository = null;

		RMConnection rmConnection = null;

		RecordVolume recordVolume = null;

		RMProperties rmProperties = null;

		try {

			rmConnection = new RMConnection();

			filePlanRepository = rmConnection.getFilePlanRepositoryByName(ResourceLoader.getMessageProperties().getProperty(ERSConstants.FILEPLAN_OBJECTSTORE));

			recordVolume = RMFactory.RecordVolume.fetchInstance(filePlanRepository, volumeBean.getVolumeId(), null);

			rmProperties = recordVolume.getProperties();

			rmProperties.putIntegerValue(ERSVolumeProperty.CURRENT_PHASE_EXECUTION_STATUS, Integer.parseInt(ResourceLoader.getMessageProperties().getProperty(ERSConstants.CURRENT_PHASE_EXECUTION_STATUS)));

			rmProperties.putDateTimeValue(ERSVolumeProperty.DISPOSAL_DATE, new Date());

			rmProperties.putDateTimeValue(ERSVolumeProperty.CURRENT_PHASE_EXECUTION_DATE, new Date());

			rmProperties.putDateTimeValue(ERSVolumeProperty.CUTOFF_DATE, new Date());

			recordVolume.save(RMRefreshMode.Refresh);

		} catch (RMRuntimeException objException) {

			logger.error("RMRuntimeException occurred in archiveRecordVolume method : " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} catch(Exception objException) {

			logger.error("Exception occurred in archiveRecordVolume method : " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());
		}  	
		finally
		{
			if(filePlanRepository != null) {
				filePlanRepository = null;
			}

			if(rmConnection != null) {
				rmConnection.closeRMConnection();
				rmConnection = null;
			}
			recordVolume = null;
		}
	}

	public String retriveDocument(ObjectStore objectStore, String documentId, String recordPath) throws IOException	{

		String folderPath = recordPath + "\\";

		String mimeType = null;

		String documentName = null;

		String fileName = null;

		//String extension = null;

		try {

			Document doc = Factory.Document.fetchInstance(objectStore,documentId, null);

			logger.info("DOCUMENT MIME TYPE: "+doc.get_MimeType());

			mimeType = doc.get_MimeType();

			ContentElementList docContentList = doc.get_ContentElements();

			Iterator<?> iters = docContentList.iterator();

			while (iters.hasNext() ) {

				ContentTransfer ct = (ContentTransfer) iters.next();

				documentName = ct.get_RetrievalName();

				logger.info("CHK AND DELETE TEMP FILE START.........................."+(folderPath+documentName));

				//deleteTEMPFile(folderPath+fileName); // DELETING DUPLICATE FILE IF ANY EXIST..

				//extension = documentName.substring(documentName.lastIndexOf(".")+1);

				if(ct.get_ContentType().equalsIgnoreCase(mimeType)) {

					int docLen = ct.get_ContentSize().intValue();

					InputStream stream = ct.accessContentStream();

					try {

						ByteArrayOutputStream buffer = new ByteArrayOutputStream();

						int nRead;

						byte[] data = new byte[1024];

						while ((nRead = stream.read(data, 0, data.length)) != -1) {

							buffer.write(data, 0, nRead);
						}

						buffer.flush();

						fileName=folderPath+documentName;

						logger.info("Folder Path :::----"+documentName);

						//deleteTEMPFile(fileName);

						FileOutputStream out = new FileOutputStream(fileName);

						out.write(buffer.toByteArray());

						out.flush();

						out.close();

						stream.close();
					}
					catch(Exception ioe)
					{
						logger.info("Exception Occured in accessAnnotation::"+ioe.getMessage(),ioe);
					}

				}else {

					logger.error("Document mimetype is null");
				}
			}
		}catch(Exception ioe)
		{
			logger.info("Exception Occured in accessAnnotation::"+ioe.getMessage(),ioe);
		}

		return documentName;
	}


	public String retrieveAnnotation(String parentDocumentId, String annotationPath) throws IOException, ERSException {

		System.out.println("Entered into retrieveAnnotation method");

		Document document = null;

		InputStream inputStream = null;

		ByteArrayOutputStream stream = null;

		FileOutputStream fileOutputStream = null;

		annotationPath = annotationPath + "/";

		File file = new File(annotationPath);

		file.mkdir();

		byte[] byteArray = {};

		CEConnection ceConnection = null;

		SearchScope searchScope = null;

		SearchSQL searchSql = null;

		String iniFileName = null;

		try {
			logger.info("documentId : " + parentDocumentId );

			ceConnection = new CEConnection();

			String sqlQuery = "SELECT * FROM CustomAnnotation r WHERE RelatedDocumentRefId = '" + parentDocumentId + "' and [IsCurrentVersion] = TRUE";

			logger.info("The content serach query is : " + sqlQuery);

			searchScope = new SearchScope(ceConnection.getObjectStoreWithCEWSIConnection(ResourceLoader.getMessageProperties(), ResourceLoader.getMessageProperties().getProperty(ERSConstants.RECORDS_OBJECTSTORE)));

			// Perform a search that returns ResultRows 
			searchSql = new SearchSQL(sqlQuery);

			// Perform the search operation.
			Boolean continuable = Boolean.TRUE;

			DocumentSet documentSet = (DocumentSet)searchScope.fetchObjects(searchSql, 10, null, continuable);

			if(!documentSet.isEmpty()) {

				Iterator<?> documentIter = documentSet.iterator();

				while(documentIter.hasNext()) {

					document = (Document) documentIter.next();

					logger.info("document : " + document.get_MimeType());

					if((document.get_ContentElements().size() != 0) && (document.get_ContentSize()!= 0)) {

						Iterator<?> iterator = document.get_ContentElements().iterator();

						for(int i = 1; i <= document.get_ContentElements().size() && iterator.hasNext(); i++)
						{
							ContentTransfer contentTransfer = (ContentTransfer)iterator.next();

							iniFileName = contentTransfer.get_RetrievalName();

							//int docLen = contentTransfer.get_ContentSize().intValue();

							logger.info("iniFileName ******************************************************** : " + iniFileName);

							inputStream = document.accessContentStream(0);				

							stream = new ByteArrayOutputStream();

							//logger.info("inputStream : " + inputStream);

							int read ; 

							byte[] data = new byte[1024];

							while ((read = inputStream.read(data, 0, data.length)) != -1)
							{
								stream.write(data, 0, read);
							}

							fileOutputStream = new FileOutputStream(new File(annotationPath + parentDocumentId + ".ini"));

							if(stream != null) {

								fileOutputStream.write(stream.toByteArray());						
							} 

							stream.flush();
						} 
					}
				}
			} else {

				System.out.println("No Annotations are available for " + parentDocumentId + " parent Id");

				String sampleAntFile = "[LINE]\nX1 = 0\nY1 = 0\nX2 = 0\nY2 = 0\nPAGE = 1\nPAGEURL = null\nLINEWIDTH = 0\nCOLOR = 0, 0, 0\nLABEL = line2\nPAGESIZE = 0, 0\nEDIT = 1\nCREATEDATE = 18 JAN 2015, 19:51:15, IST\nMODIFIEDDATE = 18 JAN 2015, 19:51:15, IST";

				byteArray = sampleAntFile.getBytes();

				fileOutputStream = new FileOutputStream(new File(annotationPath + parentDocumentId + ".ini"));

				fileOutputStream.write(byteArray);
			}
		} catch (Exception objException) {

			logger.info("Exception occured in retrieveAnnotation method " + objException.getMessage(), objException);

			objException.printStackTrace();
		} finally {

			if(fileOutputStream !=  null) {
				fileOutputStream.close();
				fileOutputStream=null;				
			}
		}

		return iniFileName;
	}	

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException, ERSException {

		DispositionOperationsImpl dispositionOperationsImpl = new DispositionOperationsImpl();

		VolumeBean volumeBean = new VolumeBean();

		//volumeBean.setRndPeriod(108);

		//volumeBean.setVolumeId("{89ED19DC-2E80-4AD5-979C-48E5029F8A8D}");

		volumeBean.setVolumeId("{8DF65A22-1B8A-4E10-A727-D966FD510381}");

		dispositionOperationsImpl.archiveRecordVolume(volumeBean);

		//dispositionOperationsImpl.retrieveAnnotation("{E97F4694-0594-7CEB-86B3-0934FA896553}", "C//");
	}

}
