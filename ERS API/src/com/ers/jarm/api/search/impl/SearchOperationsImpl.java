package com.ers.jarm.api.search.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.ers.jarm.api.bean.DocumentBean;
import com.ers.jarm.api.bean.SearchBean;
import com.ers.jarm.api.connection.RMConnection;
import com.ers.jarm.api.constants.ERSConstants;
import com.ers.jarm.api.constants.ERSRecordProperty;
import com.ers.jarm.api.exception.ERSException;
import com.ers.jarm.api.resources.ResourceLoader;
import com.ers.jarm.api.search.ISearchOperations;
import com.ibm.jarm.api.collection.CBRPageIterator;
import com.ibm.jarm.api.collection.PageableSet;
import com.ibm.jarm.api.constants.EntityType;
import com.ibm.jarm.api.core.Container;
import com.ibm.jarm.api.core.ContentRepository;
import com.ibm.jarm.api.core.FilePlanRepository;
import com.ibm.jarm.api.core.RMDomain;
import com.ibm.jarm.api.core.RMFactory;
import com.ibm.jarm.api.exception.RMRuntimeException;
import com.ibm.jarm.api.property.RMProperties;
import com.ibm.jarm.api.query.CBRResult;
import com.ibm.jarm.api.query.RMContentSearchDefinition;
import com.ibm.jarm.api.query.RMContentSearchDefinition.AndOrOper;
import com.ibm.jarm.api.query.RMContentSearchDefinition.ContentSearchOption;
import com.ibm.jarm.api.query.RMContentSearchDefinition.OrderBy;
import com.ibm.jarm.api.query.RMContentSearchDefinition.SortOrder;
import com.ibm.jarm.api.query.RMSearch;
import com.ibm.jarm.api.query.ResultRow;
import com.ibm.jarm.ral.p8ce.P8CE_ContentItemImpl;

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
 * This class is used for search operation to search the content
 * 
 * @class name SearchOperationsImpl.java
 * @date  sep,2014
 * @author MITS
 * @version 1.0
 */
public class SearchOperationsImpl implements ISearchOperations {

	private static Logger logger = Logger.getLogger(SearchOperationsImpl.class);

	static Properties messageProperties = null;

	public SearchOperationsImpl() throws ERSException
	{

		try
		{
			ResourceLoader.loadProperties();

		}
		catch (Exception objException) 
		{

			logger.error("Exception occurred in SearchOperationsImpl constructor [Properties are not loaded] : " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());
		}

	}	

	/**This method used to Search the content in the repository 
	 * @param searchBean
	 * @return searchBeanList
	 * @throws ERSException 
	 */
	public List<DocumentBean> doContentSearch(SearchBean searchBean) throws ERSException {

		logger.info("Entered into doContentSearch method");

		RMDomain rmDomain = null;

		FilePlanRepository filePlanRepository = null;

		RMConnection connectionCode = null;

		String containerPath = null;

		//List<SearchResultsBean> listResultsBean = null;

		String folderWhereClause = null;

		Iterator<?> documentIter = null;

		List<DocumentBean> documentBeanList = null;

		StringBuffer contentSearchQuery = null;
		
		String contentSearch = null;

		CBRResult setRecordResults = null;

		try {

			if(searchBean != null) {

				if(searchBean.getFolderIds() != null && searchBean.getSearchString() != null) {

					connectionCode = new RMConnection();

					long startTime = System.currentTimeMillis();

					rmDomain = connectionCode.getJarmDomainWithRMWSIConnection(ResourceLoader.getMessageProperties());

					long endTime = System.currentTimeMillis();

					logger.info("Time taken for domain : " + (endTime - startTime));

					logger.info("searchBean : " + searchBean.toString());

					contentSearchQuery = new StringBuffer();

					//listResultsBean = new ArrayList<SearchResultsBean>();

					filePlanRepository = connectionCode.getFilePlanRepositoryByName(ResourceLoader.getMessageProperties().getProperty(ERSConstants.FILEPLAN_OBJECTSTORE));

					//rosResultBean = new SearchResultsBean();

					StringBuilder recordMetaWhere = new StringBuilder();

					recordMetaWhere.append("WHERE ");

					String searchString = searchBean.getSearchString();

					List<String> folderIdsList = searchBean.getFolderIds();

					Set<CBRResult> hashSet = new TreeSet<CBRResult>(new Comparator<CBRResult>(){
						
						@Override
						public int compare(CBRResult cbr1,CBRResult cbr2){
							
							return cbr1.getResultRow().getProperties().getGuidValue("Id").equalsIgnoreCase(cbr2.getResultRow().getProperties().getGuidValue("Id"))?0:1;
						}
						
					});					

					Iterator<String> iterFolderList = folderIdsList.iterator();

					recordMetaWhere.append("(");

					long fstartTime = System.currentTimeMillis();

					while(iterFolderList.hasNext()) {

						String recordFolderId = iterFolderList.next();

						try {

							Container container = RMFactory.Container.fetchInstance(filePlanRepository, EntityType.Container, recordFolderId, null);

							containerPath = container.getPathName();

						} catch (Exception objException) {

							logger.error("RMRuntimeException occured in doContentSearch method : " + objException.getMessage());
						}

						//logger.info("containerType : " + containerPath);

						if(containerPath != null) {

							recordMetaWhere.append("r.This INSUBFOLDER '" + containerPath + "' OR ");

						}
					}

					long fendTime = System.currentTimeMillis();

					logger.info("Folder query preparing Time taken : " + (fendTime - fstartTime));

					folderWhereClause = recordMetaWhere.substring(0, recordMetaWhere.length()-4) + ")";

					recordMetaWhere.delete(0, recordMetaWhere.length());

					recordMetaWhere.append(folderWhereClause);

					String recordMetaSelect = "SELECT r.Id, r.DocumentTitle, r.RecordedDocuments";

					String recordMetaFrom = "FROM Record r";

					recordMetaWhere.append(" and r.IsDeleted = false");

					//recordMetaWhere.append(" and r.IsDeleted = false AND (");

					//List<String> recordIds = contentBaseSearchROS(searchBean);

					RMContentSearchDefinition searchDef = RMContentSearchDefinition.createInstance(rmDomain);

					searchDef.setCBRConditionOnly(true);

					searchDef.setContentSearchOption(ContentSearchOption.content);

					searchDef.setOperBtwContentAndMetadataSearch(AndOrOper.and);

					searchDef.setOrderBy(OrderBy.none);

					searchDef.setSelectClause(recordMetaSelect);

					searchDef.setFromClause(recordMetaFrom);

					searchDef.setWhereClause(recordMetaWhere.toString());			

					searchDef.setSqlAlias("r");

					searchDef.setContentSearch(searchString);

					searchDef.setSortOrder(SortOrder.desc);

					RMSearch jarmSearch = new RMSearch(filePlanRepository);

					Integer pageSize = new Integer(searchBean.getPageSize());

					contentSearchQuery = contentSearchQuery.append("SELECT d.id, d.DocumentTitle, d.FolderReferenceNumber, d.VolumeReferenceNumber, d.RecordReferenceNumber, d.IsSearchablePDF, c.Rank FROM [Document] d inner join contentsearch c on d.this = c.queriedobject WHERE ( d.RecordInformation IS not null AND d.IsSearchablePDF = " +  searchBean.isOnlySearchPDF() + " AND (contains(content, '" + searchBean.getSearchString() + "') AND ");

					int pageNo = 1;

					Boolean continuable = Boolean.TRUE; 

					logger.debug("searchDef buildSql Query : " + searchDef.buildSQLStmt());

					long cstartTime = System.currentTimeMillis();

					PageableSet<CBRResult> resultSet = jarmSearch.contentBasedRetrieval(searchDef, pageSize, continuable);

					CBRPageIterator<CBRResult> cbrPI =(CBRPageIterator<CBRResult>)(resultSet.pageIterator());

					long cendTime = System.currentTimeMillis();

					logger.info("Folder FPOS query Time taken : " + (cendTime - cstartTime));

					int currpage = 1;

					//logger.info("Current page size : " + cbrPI.getCurrentPage().size());

					if(cbrPI.hasNextPage()) {

						while (cbrPI.hasNextPage())
						{
							List<CBRResult> currentPage = cbrPI.getNextPage();

							//logger.info("currpage : " + currpage + "pageNo : " + pageNo);

							if(currpage < pageNo) {

								currpage++;

								continue;
							}

							ListIterator<CBRResult> cbrIterator = currentPage.listIterator();

							while(cbrIterator.hasNext()) {

								CBRResult recordResults = (CBRResult)cbrIterator.next();

								hashSet.add(recordResults);
							}

							Iterator<CBRResult> setIter = hashSet.iterator();

							while(setIter.hasNext()) {

								setRecordResults = (CBRResult)setIter.next();

								logger.info("Record Id from hashset" + setRecordResults.getResultRow().getProperties().getGuidValue("Id"));

								RMProperties rowProps = setRecordResults.getResultRow().getProperties();

								long rstartTime = System.currentTimeMillis();

								//logger.info("RecordId : " + rowProps.getGuidValue("Id"));

								List<Object> documentList = rowProps.getObjectListValue("RecordedDocuments");

								if(documentList != null) {

									documentIter = documentList.iterator();

									documentBeanList = new ArrayList<DocumentBean>();

									while(documentIter.hasNext()) {

										P8CE_ContentItemImpl documentObject = (P8CE_ContentItemImpl) documentIter.next();

										if(documentObject.getProperties().getBooleanValue("IsSearchablePDF")) {

											String documentId = documentObject.getProperties().getGuidValue("Id");

											logger.info("documentId : " + documentId);

											contentSearchQuery = contentSearchQuery.append("d.Id = '" + documentId + "' OR "); 
										}
									}
								}
								long rendTime = System.currentTimeMillis();

								logger.info("Additional data provided : " + (rendTime - rstartTime));

								//break;
							}
							//recordMetaWhere.substring(0, recordMetaWhere.length()-4) + ")";
							contentSearch = contentSearchQuery.substring(0, contentSearchQuery.length()-4) + ")) ORDER BY c.Rank desc OPTIONS (FULLTEXTROWLIMIT 300)";

							logger.debug("contentSearch query: " + contentSearch);

							long ROSstartTime = System.currentTimeMillis();

							documentBeanList = contentBaseSearchROS(contentSearch);

							long ROSendTime = System.currentTimeMillis();

							logger.info("ROS query Time taken : " + (ROSendTime - ROSstartTime));

							//logger.debug("listResultsBean : " + documentBeanList.toString());

							break;
						}
					} else {

						logger.error("The keyword is not available in selected folder records");

						return null;
					}

				} else {

					logger.error("The keyword is not available in any document in selected folder");

					return null;
				}

				/*} else {

					logger.error("Mandatory parameters FolderIds or searchString is null. Search can not be performed");
				}
				 */			} else {

					 logger.error("Mandatory parameter Serach Bean is null");
				 }

		} catch (RMRuntimeException objException) {

			logger.error("RMRuntimeException occured in doContentSearch method : " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} catch (Exception exceptionObj) 
		{
			logger.error("Exception occured in doContentSearch method " + exceptionObj.getMessage(), exceptionObj);

			throw new ERSException(exceptionObj.getMessage());

		} finally {

			rmDomain = null;

			filePlanRepository = null;

			connectionCode = null;

			containerPath = null;
		}

		return documentBeanList;
	}

	/**
	 * This method is used to perform content search on ros
	 * @param searchBean
	 * @return recordIds
	 * @throws ERSException
	 */
	public List<DocumentBean> contentBaseSearchROS(String contentSearch) throws ERSException {

		logger.debug("Entered into contentBaseSearchROS method");

		logger.debug("contentSearch : " + contentSearch);

		ContentRepository contentRepository = null;

		RMConnection connectionCode = null;

		RMProperties docProperties = null;

		DocumentBean documentBean = null;

		List<DocumentBean> documentBeanList = null;

		try {
			connectionCode = new RMConnection();

			long conCstartTime = System.currentTimeMillis();

			contentRepository = connectionCode.getRecordsRepositoryByName(ResourceLoader.getMessageProperties().getProperty(ERSConstants.RECORDS_OBJECTSTORE));

			long conCendTime = System.currentTimeMillis();

			logger.info("ROS Connection time : " + (conCendTime - conCstartTime));

			if(contentSearch != null) {

				//String searchString = searchBean.getSearchString();

				//resultsBean = new SearchResultsBean();

				RMSearch jarmSearch = new RMSearch(contentRepository);

				//Integer pageSize = new Integer(100);

				Boolean continuable = Boolean.TRUE; 

				documentBeanList = new ArrayList<DocumentBean>();

				/*if(searchBean.isOnlySearchPDF()) {

					searchQuery = "SELECT  d.id, d.RecordInformation, c.Rank FROM [Document] d inner join contentsearch c on d.this = c.queriedobject  WHERE  (  d.RecordInformation IS not null AND d.IsSearchablePDF = " +  searchBean.isOnlySearchPDF() + " AND (contains(content, '" + searchString + "')) )  ORDER BY c.Rank desc";

				} else {

					searchQuery = "SELECT  d.id, d.RecordInformation, c.Rank FROM [Document] d inner join contentsearch c on d.this = c.queriedobject  WHERE  (  d.RecordInformation IS not null AND (contains(content, '" + searchString + "')) )  ORDER BY c.Rank desc";
				}*/
				//logger.info("searchDef searchQuery : " + searchQuery);

				PageableSet<ResultRow> resultSet = jarmSearch.fetchRows(contentSearch, null, null, continuable);

				if(resultSet != null && resultSet.isEmpty() != true) {

					// Make use of special CBR paged iterator
					Iterator<ResultRow> resultRowIter = resultSet.iterator();

					//logger.info("resultSet size" + resultSet.isEmpty());

					while ( resultRowIter.hasNext() )
					{
						ResultRow resultRowValue  = resultRowIter.next();

						documentBean = new DocumentBean();

						//logger.info("resultRowValue : " + resultRowValue.getProperties().getGuidValue("Id"));

						//ContentItem document = RMFactory.ContentItem.fetchInstance(contentRepository, resultRowValue.getProperties().getGuidValue("Id"), null);

						docProperties = resultRowValue.getProperties();

						documentBean.setFdrRefNo(docProperties.getStringValue(ERSRecordProperty.FDR_REF_NO));

						documentBean.setVolNo(Integer.parseInt(docProperties.getStringValue(ERSRecordProperty.VOL_NO)));

						documentBean.setRecRefNo(Integer.parseInt(docProperties.getStringValue(ERSRecordProperty.REC_REF_NO)));

						documentBean.setFileName(docProperties.getStringValue(ERSRecordProperty.DOCUMENT_TITLE));

						documentBean.setDocumentId(docProperties.getGuidValue("Id"));
						
						documentBean.setSearchablePDF(docProperties.getBooleanValue("IsSearchablePDF"));
						
						documentBean.setDateCreated(docProperties.getDateTimeValue("DateCreated"));

						documentBeanList.add(documentBean);

						logger.debug("DocumentTitle : " + docProperties.getStringValue("DocumentTitle"));
					}
					//resultsBean.setRecordIds(recordIds);

					//resultsBean.setDocumentIds(documentIds);
					
					//resultsBean.setListDocumentBean(documentBeanList);
					
					//logger.info("resultsBean in ROS : " + resultsBean.toString());

				} else {

					logger.info("The keyword is not available in selected folder");
				}
			} else {

				logger.info("Mandatory parameter search bean is null. Search can not be performed");
			}
		} catch (RMRuntimeException objException) {

			logger.error("RMRuntimeException occured in contentBaseSearchROS method : " + objException.getMessage(), objException);

			throw new ERSException(objException.getMessage());

		} catch (Exception exceptionObj) {

			logger.error("Exception occured in contentBaseSearchROS method " + exceptionObj.getMessage(), exceptionObj);

			throw new ERSException(exceptionObj.getMessage());

		} finally {

			contentRepository = null;

			connectionCode = null;
		}
		return documentBeanList;
	}

	public static void main(String[] args) throws Exception {

		//SearchOperationsImpl searchOperationsImpl = new SearchOperationsImpl();

		SearchBean searchBean = new SearchBean();

		searchBean.setSearchString("tetretttettrtetteteetr");

		//searchOperationsImpl.contentBaseSearchROS(searchBean);

	}



}
