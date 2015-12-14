package com.ers.jarm.api.disposition;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.ers.jarm.api.connection.CEConnection;
import com.ers.jarm.api.connection.RMConnection;
import com.ers.jarm.api.constants.ERSConstants;
import com.ers.jarm.api.resources.ResourceLoader;
import com.filenet.api.collection.AnnotationSet;
import com.filenet.api.core.Annotation;
import com.filenet.api.core.Document;
import com.filenet.api.core.Factory;
import com.filenet.api.core.ObjectStore;
import com.ibm.jarm.api.collection.PageableSet;
import com.ibm.jarm.api.collection.RMPageIterator;
import com.ibm.jarm.api.core.ContentItem;
import com.ibm.jarm.api.core.FilePlanRepository;
import com.ibm.jarm.api.core.RMFactory;
import com.ibm.jarm.api.core.Record;
import com.ibm.jarm.api.core.RecordVolume;
import com.ibm.jarm.api.property.RMPropertyFilter;

public class DownloadAnnotaionMetadaContent 
{
	static Logger logger=Logger.getLogger(DownloadAnnotaionMetadaContent.class);

	public static void uploadAnnotations()
	{

		logger.debug("Enter the uploadAnnotations() method in DownloadAnnotaionMetadaContent class ");

		FilePlanRepository fpRepos=null;

		RMConnection rmCode=null;

		RecordVolume sorcevol =null;

		RMPropertyFilter filter=null;

		PageableSet<Record> rcResultSet=null;

		RMPageIterator<Record> rcPI=null;

		Record firstTopRC =null;

		List<Record> rcPage =null;

		Record	fetchRecord =null;

		PageableSet<ContentItem> contentItm=null;

		Iterator it=null;

		ContentItem cItem=null;

		CEConnection ceCode=null;

		ObjectStore objectStore =null;

		Document document=null;

		InputStream inputStream =null;

		ByteArrayOutputStream stream=null;

		FileOutputStream out =null;

		AnnotationSet annotationSet =null;

		Iterator iterator=null;

		Annotation annotation=null;

		InputStream iSAnnotate=null;

		ByteArrayOutputStream buffer=null;

		FileOutputStream out1=null;

		try
		{
			rmCode = new RMConnection();

			fpRepos =rmCode.getFilePlanRepositoryByName(ResourceLoader.getMessageProperties().getProperty(ERSConstants.FILEPLAN_OBJECTSTORE));

			/*source container instance here*/

			sorcevol = RMFactory.RecordVolume.fetchInstance(fpRepos,"{E6CE0162-2852-448C-BEE9-A397A21AFB1D}",null);

			filter = RMPropertyFilter.MinimumPropertySet;

			Integer pageSize = Integer.valueOf(5); 

			// Can be null to use repository default.

			rcResultSet =sorcevol.getRecords(300);

			// For demonstration purposes, use the paging mechanism of this PageableSet.

			rcPI = rcResultSet.pageIterator();

			firstTopRC = null;

			while ( rcPI.nextPage() )
			{
				rcPage = rcPI.getCurrentPage();

				logger.debug("Size of records  in a volume:"+rcPage.size());

				for (Record record : rcPage)
				{
					firstTopRC = record; 

					logger.debug("Name of each record:::"+record.getName());

					fetchRecord = RMFactory.Record.fetchInstance(fpRepos,firstTopRC.getObjectIdentity(),null);

					contentItm = fetchRecord.getAssociatedContentItems();

					it =contentItm.iterator();

					while (it.hasNext())
					{
						cItem= (ContentItem) it.next();

						logger.debug("ContentItem Name:::::"+cItem.getName());

						System.out.println("ContentObjectIdentity::: "+cItem.getObjectIdentity());

						ceCode = new CEConnection();

						//objectStore  =CEConnectionCode.getCEConnection();

						document = Factory.Document.fetchInstance(objectStore, cItem.getObjectIdentity(),null);

						inputStream = document.accessContentStream(0);

						stream = new ByteArrayOutputStream();

						int read ; 

						byte[] data = new byte[1024];

						while ((read = inputStream.read(data, 0, data.length)) != -1)
						{
							stream.write(data, 0, read);
						}

						stream.flush();

						logger.info("Folder Path :::----");

						out = new FileOutputStream(new File("C:\\AnnotationData"+"\\"+record.getName()));

						out.write(stream.toByteArray());

						annotationSet = document.get_Annotations();

						iterator = annotationSet.iterator();

						while(iterator.hasNext())
						{
							annotation = (Annotation)iterator.next();

							iSAnnotate = annotation.accessContentStream(0);

							buffer = new ByteArrayOutputStream();

							int nRead;

							byte[] data1 = new byte[1024];

							while ((nRead = iSAnnotate.read(data1, 0, data.length)) != -1)
							{
								buffer.write(data1, 0, nRead);
							}

							buffer.flush();

							logger.info("Folder Path :::----");

							out1 = new FileOutputStream(new File("C:\\AnnotationData"+"\\"+cItem.getName()));

							out1.write(buffer.toByteArray());
						}				
					}						
				}
			}
		}
		catch (Exception exceptionObj)
		{
			logger.error("Getting th exception message here:::::"+exceptionObj.getMessage(),exceptionObj);
		}
		finally
		{

			fpRepos=null;

			rmCode=null;

			sorcevol =null;

			filter=null;

			rcResultSet=null;

			rcPI=null;

			firstTopRC =null;

			rcPage =null;

			fetchRecord =null;

			contentItm=null;

			it=null;

			cItem=null;

			ceCode=null;

			objectStore =null;

			document=null;

			inputStream =null;

			stream=null;

			out =null;

			annotationSet =null;

			iterator=null;

			annotation=null;

			iSAnnotate=null;

			buffer=null;

			out1=null;	
		}
		logger.debug("End the uploadAnnotations() method in DownloadAnnotaionMetadaContent class");
	}
//	/**
//	 * @param args
//	 */
//	public static void main(String[] args)
//	{
//		uploadAnnotations();
//	}
}
