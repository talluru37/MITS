package com.ers.jarm.api.search.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.ers.jarm.api.bean.DocumentBean;
import com.ers.jarm.api.bean.SearchBean;
import com.ers.jarm.api.bean.SearchResultsBean;
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
import com.ibm.jarm.api.core.Record;
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
	public List<SearchResultsBean> doContentSearch(SearchBean searchBean) throws ERSException {

		logger.info("Entered into doContentSearch method");

		RMDomain rmDomain = null;

		FilePlanRepository filePlanRepository = null;

		RMConnection connectionCode = null;

		String containerPath = null;

		SearchResultsBean resultsBean = null;

		List<SearchResultsBean> listResultsBean = null;

		String folderWhereClause = null;

		SearchResultsBean rosResultBean = null;

		Iterator<?> documentIter = null;

		DocumentBean documentBean = null;

		List<DocumentBean> documentBeanList = null;

		try {

			if(searchBean != null) {

				if(searchBean.getFolderIds() != null && searchBean.getSearchString() != null) {

					connectionCode = new RMConnection();

					rmDomain = connectionCode.getJarmDomainWithRMWSIConnection(ResourceLoader.getMessageProperties());

					logger.info("searchBean : " + searchBean.toString());

					listResultsBean = new ArrayList<SearchResultsBean>();

					filePlanRepository = connectionCode.getFilePlanRepositoryByName(ResourceLoader.getMessageProperties().getProperty(ERSConstants.FILEPLAN_OBJECTSTORE));

					//rosResultBean = new SearchResultsBean();

					StringBuilder recordMetaWhere = new StringBuilder();

					recordMetaWhere.append("WHERE ");

					String searchString = searchBean.getSearchString();

					List<String> folderIdsList = searchBean.getFolderIds();

					Iterator<String> iterFolderList = folderIdsList.iterator();

					recordMetaWhere.append("(");

					while(iterFolderList.hasNext()) {

						String recordFolderId = iterFolderList.next();

						try {

							Container container = RMFactory.Container.fetchInstance(filePlanRepository, EntityType.Container, recordFolderId, null);

							containerPath = container.getPathName();

						} catch (Exception objException) {

							logger.error("RMRuntimeException occured in doContentSearch method : " + objException.getMessage(), objException);
						}

						//logger.info("containerType : " + containerPath);

						if(containerPath != null) {

							recordMetaWhere.append("r.This INSUBFOLDER '" + containerPath + "' OR ");

						}
					}

					folderWhereClause = recordMetaWhere.substring(0, recordMetaWhere.length()-4) + ")";

					recordMetaWhere.delete(0, recordMetaWhere.length());

					recordMetaWhere.append(folderWhereClause);

					String recordMetaSelect = "SELECT r.Id, r.DocumentTitle";

					String recordMetaFrom = "FROM Record r";

					recordMetaWhere.append(" and r.IsDeleted = false");

					//recordMetaWhere.append(" and r.IsDeleted = false AND (");

					//List<String> recordIds = contentBaseSearchROS(searchBean);

					rosResultBean = contentBaseSearchROS(searchBean);

					//List<String> recordIds = rosResultBean.getRecordIds();

					List<String> documentIds = rosResultBean.getDocumentIds();

					/*if(recordIds != null && documentIds != null) {

						Iterator<String> recordIter = recordIds.iterator();

						while(recordIter.hasNext()) {

							String recordId = recordIter.next();

							//logger.info("recordId : " + recordId);

							recordMetaWhere.append("r.Id = '" + recordId + "' OR " );
						}
						String whereClause = recordMetaWhere.substring(0, recordMetaWhere.length()-4) + ")";

						logger.info("recordMetaWhere : " + whereClause);
					 */
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

					int pageNo = 1;

					Boolean continuable = Boolean.TRUE; 

					logger.info("searchDef buildSql Query : " + searchDef.buildSQLStmt());

					PageableSet<CBRResult> resultSet = jarmSearch.contentBasedRetrieval(searchDef, pageSize, continuable);

					CBRPageIterator<CBRResult> cbrPI =(CBRPageIterator<CBRResult>)(resultSet.pageIterator());

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
							for (CBRResult recordResults : currentPage)
							{
								RMProperties rowProps = recordResults.getResultRow().getProperties();

								Record record = RMFactory.Record.fetchInstance(filePlanRepository, rowProps.getGuidValue("Id"), null);

								List<Object> documentList =  record.getProperties().getObjectListValue("RecordedDocuments");

								if(documentList != null) {

									documentIter = documentList.iterator();

									documentBeanList = new ArrayList<DocumentBean>();

									while(documentIter.hasNext()) {

										P8CE_ContentItemImpl documentObject =  (P8CE_ContentItemImpl) documentIter.next();

										String documentId = documentObject.getProperties().getGuidValue("Id");

										boolean isSearchablePdf = documentObject.getProperties().getBooleanValue("IsSearchablePDF");

										logger.info("documentId : " + documentId + "isSearchablePdf : " + isSearchablePdf);

										if(isSearchablePdf) {

											if(documentId != null) {

												if(documentIds.contains(documentId)) {

													//logger.info("Documen Id : " + documentIds.contains(documentId));

													documentBean = new DocumentBean();

													documentBean.setDocumentId(documentId);

													documentBeanList.add(documentBean);
												}
											}
										}			
									}
								}
								resultsBean = new SearchResultsBean();

								resultsBean.setTitle(rowProps.getStringValue(ERSRecordProperty.DOCUMENT_TITLE));

								String recordId = rowProps.getGuidValue("Id");

								//logger.info("recordId : " + recordId);

								//Record record = RMFactory.Record.fetchInstance(filePlanRepository, recordId, null);

								resultsBean.setRecordId(recordId);

								resultsBean.setListDocumentBean(documentBeanList);

								//documentBeanList.clear();

								RMProperties rmProperties = record.getProperties();

								if (!rmProperties.getBooleanValue("IsDeleted")) {

									resultsBean.setFolRefNum(rmProperties.getStringValue(ERSRecordProperty.FDR_REF_NO));

									resultsBean.setVolRefNumber(rmProperties.getStringValue(ERSRecordProperty.VOL_NO));

									resultsBean.setRecRefNo(rmProperties.getStringValue(ERSRecordProperty.REC_REF_NO));

									resultsBean.setRecordType(rmProperties.getStringValue(ERSRecordProperty.RECORD_TYPE));

									resultsBean.setSecClass(rmProperties.getStringValue(ERSRecordProperty.SECURITY_CLASSIFICATION));

									resultsBean.setDescription(rmProperties.getStringValue(ERSRecordProperty.DESCR));

									resultsBean.setTags(rmProperties.getStringValue(ERSRecordProperty.TAG));

									resultsBean.setAuthor(rmProperties.getStringValue(ERSRecordProperty.AUTHOR));

									resultsBean.setAuthorOrganisation(rmProperties.getStringValue(ERSRecordProperty.AUTHOR_OU));

									resultsBean.setCreatorDesignation(rmProperties.getStringValue(ERSRecordProperty.CREATOR_DESIGNATION));

									logger.info("DocumentTitle : " + rowProps.getStringValue("DocumentTitle"));

									listResultsBean.add(resultsBean);
								}

							}
							logger.info("listResultsBean : " + listResultsBean.toString());

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


		return listResultsBean;
	}

	/**
	 * This method is used to perform content search on ros
	 * @param searchBean
	 * @return recordIds
	 * @throws ERSException
	 */
	public SearchResultsBean contentBaseSearchROS(SearchBean searchBean) throws ERSException {

		logger.info("Entered into contentBaseSearchROS method");

		logger.info("searchBean : " + searchBean.toString());

		ContentRepository contentRepository = null;

		RMConnection connectionCode = null;

		List<String> recordIds = null;

		List<String> documentIds = null;

		SearchResultsBean resultsBean = null;

		String searchQuery = null;

		try {
			connectionCode = new RMConnection();

			contentRepository = connectionCode.getRecordsRepositoryByName(ResourceLoader.getMessageProperties().getProperty(ERSConstants.RECORDS_OBJECTSTORE));

			if(searchBean != null) {

				String searchString = searchBean.getSearchString();

				recordIds = new ArrayList<String>();

				documentIds = new ArrayList<String>();

				resultsBean = new SearchResultsBean();

				RMSearch jarmSearch = new RMSearch(contentRepository);

				Integer pageSize = new Integer(100);

				Boolean continuable = Boolean.TRUE; 

				if(searchBean.isOnlySearchPDF()) {

					searchQuery = "SELECT  d.id, d.RecordInformation, c.Rank FROM [Document] d inner join contentsearch c on d.this = c.queriedobject  WHERE  (  d.RecordInformation IS not null AND d.IsSearchablePDF = " +  searchBean.isOnlySearchPDF() + " AND (contains(content, '" + searchString + "')) )  ORDER BY c.Rank desc";

				} else {

					searchQuery = "SELECT  d.id, d.RecordInformation, c.Rank FROM [Document] d inner join contentsearch c on d.this = c.queriedobject  WHERE  (  d.RecordInformation IS not null AND (contains(content, '" + searchString + "')) )  ORDER BY c.Rank desc";
				}
				logger.info("searchDef searchQuery : " + searchQuery);

				PageableSet<ResultRow> resultSet = jarmSearch.fetchRows(searchQuery, null, null, continuable);

				if(resultSet != null && resultSet.isEmpty() != true) {

					// Make use of special CBR paged iterator
					Iterator<ResultRow> resultRowIter = resultSet.iterator();

					logger.info("resultSet size" + resultSet.isEmpty());

					while ( resultRowIter.hasNext() )
					{
						ResultRow resultRowValue  = resultRowIter.next();

						//logger.info("resultRowValue : " + resultRowValue.getProperties().getGuidValue("Id"));

						//logger.info("Document Id" + resultRowValue.getProperties().getGuidValue("Id"));

						documentIds.add(resultRowValue.getProperties().getGuidValue("Id"));

						Record record = (Record) resultRowValue.getProperties().getObjectValue("RecordInformation");

						if(record != null) {

							if(record.getProperties() != null) {

								if(record.getProperties().getGuidValue("Id") != null) {

									recordIds.add(record.getProperties().getGuidValue("Id"));
								}
							}
						}
					}
					//resultsBean.setRecordIds(recordIds);

					resultsBean.setDocumentIds(documentIds);

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
		return resultsBean;
	}

	public static void main(String[] args) throws Exception {

		SearchOperationsImpl searchOperationsImpl = new SearchOperationsImpl();

		SearchBean searchBean = new SearchBean();

		searchBean.setSearchString("tetretttettrtetteteetr");

		//searchOperationsImpl.contentBaseSearchROS(searchBean);

	}



}
