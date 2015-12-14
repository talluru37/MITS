/**
 * 
 */
package com.ers.jarm.api.disposition.impl;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.aspose.pdf.FloatingBox;
import com.ers.jarm.api.resources.ResourceLoader;
import com.filenet.api.collection.AnnotationSet;
import com.filenet.api.collection.ContentElementList;
import com.filenet.api.core.Annotation;
import com.filenet.api.core.ContentTransfer;
import com.filenet.api.core.Document;
import com.filenet.api.core.Factory;
import com.filenet.api.core.ObjectStore;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.RandomAccessFileOrArray;
import com.itextpdf.text.pdf.codec.TiffImage;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfStamper;
import com.sun.media.jai.codec.ImageDecodeParam;
import com.sun.media.jai.codec.ImageDecoder;

/**
 * @author Yashwanth
 * @Class to draw annotation
 * @Date 11-6-2014
 *
 */
public class DrawAnnotation {

	private static String folderPath = null;
	
	private static Map imgData = null;
	
	static Logger logger = Logger.getLogger(DrawAnnotation.class);
	
	static Properties properties = null;
	
	public DrawAnnotation() throws Exception
	{
		try
		{
			ResourceLoader.loadProperties();
		}
		catch (Exception objException) 
		{
			
			logger.error("Exception occurred in DrawAnnotation constructor : " + objException.getMessage(), objException);
			
			throw objException;
		}
		
	}	
	

	/**
	 * @Method to get all the annotations
	 * @param os - ObjectStore
	 * @param documentGUID - Document Guid
	 * @param annotationList -Annotation list obtained from viewer file
	 */
	public  static void accessAnnotation(ObjectStore os ,String documentGUID, String recordPath) {
		
		logger.info("Entered into accessAnnotation method");

		String fileName = "";

		String ext = "";

		String mimeType="";

		try {
			
			folderPath = recordPath + "\\";

			Document doc = Factory.Document.fetchInstance(os,documentGUID, null);

			logger.info("DOCUMENT MIME TYPE: "+doc.get_MimeType());

			mimeType = doc.get_MimeType();

			ContentElementList docContentList = doc.get_ContentElements();

			Iterator<?> iters = docContentList.iterator();

			while (iters.hasNext() )
			{

				ContentTransfer ct = (ContentTransfer) iters.next();

				fileName = ct.get_RetrievalName();

				logger.info("CHK AND DELETE TEMP FILE START.........................."+(folderPath+fileName));

				deleteTEMPFile(folderPath+fileName); // DELETING DUPLICATE FILE IF ANY EXIST..

				logger.info("CHK AND DELETE TEMP FILE END..........................");

				ext = fileName.substring(fileName.lastIndexOf(".")+1);

				if(ct.get_ContentType().equalsIgnoreCase(mimeType)){

					int docLen = ct.get_ContentSize().intValue();

					InputStream stream = ct.accessContentStream();

					try
					{
						ByteArrayOutputStream buffer = new ByteArrayOutputStream();

						int nRead;

						byte[] data = new byte[docLen];

						while ((nRead = stream.read(data, 0, data.length)) != -1) {

							buffer.write(data, 0, nRead);
						}

						buffer.flush();

						fileName=folderPath + fileName;

						logger.info("Folder Path :::----"+fileName);

						deleteTEMPFile(fileName);

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
				}
			}

			AnnotationSet as = doc.get_Annotations();

			Iterator iter = as.iterator();

			List list = new ArrayList<Map>();

			while (iter.hasNext()) {

				Annotation annObject = (Annotation) iter.next();

				InputStream is = annObject.accessContentStream(0);

				DocumentBuilderFactory factory = null;

				DocumentBuilder builder = null;

				org.w3c.dom.Document ret = null;

				factory = DocumentBuilderFactory.newInstance();

				builder = factory.newDocumentBuilder();
				
				//need clarification here
				ret = builder.parse(new InputSource(is));

				XPath xpath = XPathFactory.newInstance().newXPath();
				
				//To get F_Text objects and evaluate
				javax.xml.xpath.XPathExpression expr = xpath.compile("//*[local-name()='F_TEXT']");

				Object res = expr.evaluate(ret, XPathConstants.NODESET);

				NodeList nodes1 = (NodeList) res;

				int sizeRS = nodes1.getLength();

				String content_text = null;

				if (sizeRS > 0) {

					content_text = nodes1.item(0).getTextContent();
				}

				expr =  xpath.compile("//*[local-name()='PropDesc']");

				res= expr.evaluate(ret, XPathConstants.NODESET);

				nodes1 = (NodeList)res;

				sizeRS = nodes1.getLength();

				imgData = new HashMap<String, String>();

				if(sizeRS > 0){

					NamedNodeMap nnm =  nodes1.item(0).getAttributes();

					for(int i=0;i<nnm.getLength();i++){

						String data = nnm.item(i).toString();

						if(data.indexOf("F_MULTIPAGETIFFPAGENUMBER")>-1)
							imgData.put("F_MULTIPAGETIFFPAGENUMBER", nnm.item(i).getNodeValue());

						if(data.indexOf("F_BACKCOLOR")>-1)
							imgData.put("F_BACKCOLOR", nnm.item(i).getNodeValue());

						if(data.indexOf("F_BORDER_BACKMODE")>-1)
							imgData.put("F_BORDER_BACKMODE", nnm.item(i).getNodeValue());

						if(data.indexOf("F_BORDER_COLOR")>-1)
							imgData.put("F_BORDER_COLOR", nnm.item(i).getNodeValue());

						if(data.indexOf("F_BORDER_STYLE")>-1)
							imgData.put("F_BORDER_STYLE", nnm.item(i).getNodeValue());

						if(data.indexOf("F_BORDER_WIDTH")>-1)
							imgData.put("F_BORDER_WIDTH", nnm.item(i).getNodeValue());

						if(data.indexOf("F_CLASSNAME")>-1)
							imgData.put("F_CLASSNAME", nnm.item(i).getNodeValue());

						if(data.indexOf("F_FONT_BOLD")>-1)
							imgData.put("F_FONT_BOLD", nnm.item(i).getNodeValue());

						if(data.indexOf("F_FONT_NAME")>-1)
							imgData.put("F_FONT_NAME", nnm.item(i).getNodeValue());		

						if(data.indexOf("F_TOP")>-1)
							imgData.put("F_TOP", nnm.item(i).getNodeValue());

						if(data.indexOf("F_WIDTH")>-1)
							imgData.put("F_WIDTH", nnm.item(i).getNodeValue());

						if(data.indexOf("F_ROTATION")>-1)
							imgData.put("F_ROTATION", nnm.item(i).getNodeValue());

						if(data.indexOf("F_LEFT")>-1)
							imgData.put("F_LEFT", nnm.item(i).getNodeValue());

						if(data.indexOf("F_HEIGHT")>-1)
							imgData.put("F_HEIGHT", nnm.item(i).getNodeValue());

						if(data.indexOf("F_HASBORDER")>-1)
							imgData.put("F_HASBORDER", nnm.item(i).getNodeValue());

						if(data.indexOf("F_FORECOLOR")>-1)
							imgData.put("F_FORECOLOR", nnm.item(i).getNodeValue());

						if(data.indexOf("F_FONT_SIZE")>-1)
							imgData.put("F_FONT_SIZE", nnm.item(i).getNodeValue());

						if(data.indexOf("F_SUBCLASS")>-1)
							imgData.put("F_SUBCLASS", nnm.item(i).getNodeValue());

						if(data.indexOf("F_LINE_END_X")>-1)
							imgData.put("F_LINE_END_X",  nnm.item(i).getNodeValue());

						if(data.indexOf("F_LINE_END_Y")>-1)
							imgData.put("F_LINE_END_Y",  nnm.item(i).getNodeValue());

						if(data.indexOf("F_LINE_START_X")>-1)
							imgData.put("F_LINE_START_X",  nnm.item(i).getNodeValue());

						if(data.indexOf("F_LINE_START_Y")>-1)
							imgData.put("F_LINE_START_Y",  nnm.item(i).getNodeValue());

						if(data.indexOf("F_BRUSHCOLOR")>-1)
							imgData.put("F_BRUSHCOLOR",  nnm.item(i).getNodeValue());

						if(data.indexOf("F_PAGENUMBER")>-1)
							imgData.put("F_PAGENUMBER",  nnm.item(i).getNodeValue());

					}
				}
				/**
				 * Convert hexadecimal to String having text / stamp
				 */
				if(null!=content_text)
				{
					int valu = content_text.length() / 4;

					String[] con_text = new String[valu];

					StringBuilder sb = new StringBuilder();

					for (int i = 0, j = 0; i < content_text.length(); i = i + 4, j++) {

						con_text[j] = content_text.substring(i, i + 4);

						String temp[];

						temp = con_text[j].split("00");

						con_text[j] = temp[1];

						sb.append(convertHexToString(con_text[j]));
					}
					if(sb.toString().trim().length()>1)
					{
						imgData.put("MSGTEXT", sb.toString());

						logger.info("sb.toString()"+sb.toString());
					}

					logger.info("FILENAME : "+ fileName + " AND MSG = " +sb.toString());
				}
				list.add(imgData);
			}

			/**
			 * Check The Mime Type - image/ tiff for the document downloaded.
			 */
			if(doc.get_MimeType().equalsIgnoreCase("image/tiff")||doc.get_MimeType().equalsIgnoreCase("image/tif") && list.size()>0)
			{

				logger.info("CALLINg PDF CONVERSION : " +fileName);

				convertToPdf(fileName);
			}
			/**
			 * check the document type which is MS Word document either it is of type doc/docx
			 */
			if(doc.get_MimeType().equalsIgnoreCase("application/msword")||doc.get_MimeType().equalsIgnoreCase("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
			{
				logger.info("Calling Doc --> Pdf Conversion.... "+fileName);

				convertWordToPdf(fileName);
			}

			logger.info("Extension is --- " +ext);

			if(null!=list && list.size()>0)
			{
				if(ext.equalsIgnoreCase("doc")||ext.equalsIgnoreCase("docx"))
				{
					logger.info("Document MIME------");

					fileName=fileName.substring(0,fileName.lastIndexOf("."))+".pdf";

					logger.info("Document MIME------File Name ------"+fileName);

					generateASPOSEPDFWithAnnotations(list,fileName);
					//generateNonAsposePDFWithAnnotations(list,fileName);
				}
				if(ext.equalsIgnoreCase("pdf"))
				{

					logger.info("PDF MIME");

					generateASPOSEPDFWithAnnotations(list, fileName);
					//generateNonAsposePDFWithAnnotations(list,fileName);
				}
				else if((ext.equalsIgnoreCase("tif"))||ext.equalsIgnoreCase("tiff"))
				{

					logger.info("TIFF MIME------");

					fileName =  fileName.substring(0, fileName.lastIndexOf("."))+".pdf";

					logger.info("FILENAME ::::::::::: " +fileName);

					generateASPOSEPDFWithAnnotations(list, fileName);
					//generateNonAsposePDFWithAnnotations(list,fileName);
				}
				else if(ext.equalsIgnoreCase("png")||ext.equalsIgnoreCase("jpeg")||ext.equalsIgnoreCase("jpg")||ext.equalsIgnoreCase("gif"))
				{
					logger.info(" Inside of png / jpeg / jpg/ gif ");

					//drawAnnotationsToImage(list,fileName,ext,annotationList);

					drawAnnotation(list,fileName,ext);
				}
			}

		}
		catch (Exception execeptionObject)
		{
			logger.error("Exception in accessAnnotation :: ---"+execeptionObject.getMessage(),execeptionObject);
		}
		finally
		{
			fileName = "";

			ext = "";

			mimeType="";
		}
	}
	/**
	 * 
	 * @param annotationList
	 * @param fileName
	 * @param strF_Top 
	 * @throws IOException
	 */
	private static void generateNonAsposePDFWithAnnotations(List annotationList, String fileName) throws IOException
	{
		String strSubClass = "";

		com.lowagie.text.pdf.PdfReader reader = new com.lowagie.text.pdf.PdfReader(fileName);

		String extn = fileName.substring(fileName.lastIndexOf("."), fileName.length());

		String temp = fileName.substring(0, fileName.lastIndexOf("."));

		logger.info("Temp ...."+temp +" ... Ext ..."+extn);

		String strF_Top = "";

		String strF_Left = "";

		String strF_Width = "";

		String strF_Height = "";

		String rotation = "";

		int iTop ;

		int iLeft ;

		int iWidth;

		int iHeight;

		PdfStamper stamper = null;

		com.lowagie.text.pdf.PdfContentByte over = null;

		try
		{
			logger.info("Start of  generateASPOSEPDFWithAnnotationsFreeWare ");

			stamper = new PdfStamper(reader, new FileOutputStream(temp+"O"+extn));

			logger.info("Annotation List ::-"+annotationList+"----------FileName ----"+fileName);

			int pageCount = reader.getNumberOfPages();



			logger.info("Page Count ::---"+pageCount);

			logger.info("File :: Extension ::-"+extn);

			logger.info("File :: TempFile Name ::--"+temp);

			BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);

			for (int i = 0; i < pageCount; i++)
			{
				logger.info("Page Number ::--"+(i));

				for(int j=0; j<annotationList.size(); j++){

					Map  data = (Map) annotationList.get(j);

					strF_Top = data.get("F_TOP").toString();

					strF_Left = data.get("F_LEFT").toString();

					strF_Width = data.get("F_WIDTH").toString();

					strF_Height = data.get("F_HEIGHT").toString();

					String pageNo =  data.get("F_PAGENUMBER").toString();

					logger.info("Page Number is ::--"+pageNo);

					if(null!=data.get("F_ROTATION"))
					{
						rotation = data.get("F_ROTATION").toString();
					}

					logger.info("::::::::::::::DrawAnnotation :: drawAnnotation :: str F_Top ..."+strF_Top +"......Left...."+strF_Left +"....Width...."+strF_Width+".....Height...."+strF_Height);

					if(strF_Top.length()>=4)
					{

						if(strF_Top.substring(0, strF_Top.lastIndexOf(".")).length()==2)

							strF_Top = strF_Top.substring(0, 5);

						else

							strF_Top = strF_Top.substring(0, 4);
					}
					else
					{
						strF_Top = strF_Top.substring(0,3);

						strF_Top =strF_Top+"0";
					}
					if(strF_Left.length()>=4)
					{
						if(strF_Left.substring(0, strF_Left.lastIndexOf(".")).length()==2)

							strF_Left = strF_Left.substring(0,5);

						else

							strF_Left = strF_Left.substring(0, 4);
					}
					else
					{
						strF_Left = strF_Left.substring(0,3);

						strF_Left= strF_Left+"0";
					}
					if(strF_Width.length()>=4)
					{
						if(strF_Width.substring(0,strF_Width.lastIndexOf(".")).length()==2)
							strF_Width =strF_Width.substring(0,5);
						else
							strF_Width = strF_Width.substring(0, 4);
					}
					else
					{	
						strF_Width = strF_Width.substring(0,3);

						strF_Width=strF_Width+"0";
					}
					if(strF_Height.length()>=4)
					{
						if(strF_Height.substring(0,strF_Height.lastIndexOf(".")).length()==2)

							strF_Height = strF_Height.substring(0,5);
						else

							strF_Height = strF_Height.substring(0, 4);
					}
					else
					{	
						strF_Height = strF_Height.substring(0,3);

						strF_Height=strF_Height+"0";
					}

					strF_Top = strF_Top.replace(".","");

					strF_Left = strF_Left.replace(".","");

					strF_Width = strF_Width.replace(".","");

					strF_Height = strF_Height.replace(".","");

					logger.info("StrF_Top ....."+data.get("F_TOP").toString()+"---Converted to ---"+strF_Top+"StrF_Left ..."+ data.get("F_LEFT").toString()+"......"+strF_Left);

					logger.info("strF_Width ....."+data.get("F_WIDTH").toString()+"---Converted to ---"+strF_Width+"strF_Height ..."+ data.get("F_HEIGHT").toString()+"......"+strF_Height);

					//iTop  = (int)(Integer.parseInt(strF_Top)*2/2.777777777777778);
					//iLeft = (int)(Integer.parseInt(strF_Left)*2/2.777777777777778);
					//iWidth = (int)(Integer.parseInt(strF_Width)*2/2.777777777777778);
					//iHeight=(int)(Integer.parseInt(strF_Height)*2/2.777777777777778);

					iTop  = (int)(Integer.parseInt(strF_Top)*2*0.35);

					iLeft = (int)(Integer.parseInt(strF_Left)*2*0.3827);

					iWidth = (int)(Integer.parseInt(strF_Width)*2*0.35);

					iHeight=(int)(Integer.parseInt(strF_Height)*2*0.3827);

					logger.info("GenerateASPOSEPDFWithAnnotationsFreeWare :: \n iTop :: "+iTop+" :: \n iLeft  :: "+iLeft+" :: \n  iWidth  :: "+iWidth +" \n :: iHeight ::"+iHeight);

					if(null!=data.get("F_MULTIPAGETIFFPAGENUMBER"))
					{
						int pageNumber = Integer.parseInt(data.get("F_MULTIPAGETIFFPAGENUMBER").toString());

						logger.info("F_MULTIPAGETIFFPAGENUMBER::--"+pageNumber);

						if(pageNumber==0)
							pageNumber=1;

						if(null!=data.get("F_SUBCLASS"))
						{
							strSubClass =data.get("F_SUBCLASS").toString();

							logger.info("Str Sub Class ::--"+strSubClass);
						}

						logger.info("msg Txt;;;"+data.get("MSGTEXT"));

						Color pdfColor = null;

						Color pdfConverted = null;

						logger.info("F_Fore Color ::"+data.get("F_FORECOLOR")+"     MSG Text :::--"+data.get("MSGTEXT"));

						if(null!=data.get("F_FORECOLOR"))
						{
							pdfColor = Color.decode(data.get("F_FORECOLOR").toString());

							pdfConverted = new Color(pdfColor.getBlue(),pdfColor.getGreen(),pdfColor.getRed());

						}
						if(null!=data.get("MSGTEXT"))
						{
							over = stamper.getOverContent(pageNumber);

							//logger.info("Page Size ::---"+over.getPdfDocument().getPageSize().getHeight());

							//float height =over.getPdfDocument().getPageSize().getHeight();

							//double x = height-getASPHeight(Double.parseDouble(data.get("F_TOP").toString()));

							//double y = getASPWidth(Double.parseDouble(data.get("F_LEFT").toString()));

							over.beginText();

							over.setColorFill(pdfConverted);

							over.setFontAndSize(bf, Integer.parseInt(data.get("F_FONT_SIZE").toString())+5);

							over.setTextMatrix(iTop,iLeft);

							//over.setTextMatrix((float)x,(float)y);

							over.showText(data.get("MSGTEXT").toString());

							over.endText();
						}
						String strClassName = data.get("F_CLASSNAME").toString();

						if(null!=data.get("F_SUBCLASS"))
							strSubClass =data.get("F_SUBCLASS").toString();

						logger.info("Class Name ----"+strClassName+"---Sub Class Name ::"+strSubClass);

						if(strClassName.equalsIgnoreCase("Highlight"))
						{
							logger.info("GenerateASPOSEPDFWithAnnotationsFreeWare :: Highlight ");

						}
						else if(strClassName.equalsIgnoreCase("Proprietary"))
						{
							logger.info(" Start of sub class for Pdf  "+strSubClass);

							logger.debug("Inside of Redact annotate  for Pdf ");

							if(strSubClass.equalsIgnoreCase("v1-Redaction"))
							{
								logger.debug("Start of Redact annotate for Pdf  ");

								logger.debug("End of Redact annotate for Pdf  ");
							}
							else if(strSubClass.equalsIgnoreCase("v1-Rectangle"))
							{
								logger.debug("Start of Rectalngele annotate for Pdf  ");

								logger.debug("End of Rectalngele annotate for Pdf ");
							}
							else if (strSubClass.equalsIgnoreCase("v1-Line"))
							{
								logger.debug("Start of v1-Line annotate for Pdf ");

								logger.debug("End of v1-Line annotate for Pdf ");
							}
							logger.debug("End of Redact annotate for Pdf ");
						}
					}
				}

			}
			stamper.close();
			logger.info("End of  generateASPOSEPDFWithAnnotationsFreeWare ");

		}
		catch(Exception exception)
		{
			logger.error("Exception in generateASPOSEPDFWithAnnotationsFreeWare "+exception.getMessage(),exception);
		}
		finally
		{
			reader =null;

			strSubClass = "";
		}
	}
	/*	private static void drawString(Graphics2D g, String text, int x, int y) 
	{
		for (String line : text.split("\n"))
			g.drawString(line, x, y += g.getFontMetrics().getHeight());
	}*/
	/**
	 * @Method to draw Annotations
	 */
	public static void drawAnnotation(List list,String fileName , String extension) 
	{
		Font font = null;

		String comment = null;

		BufferedImage buffered = null;

		Graphics2D g2Object = null;

		RenderedImage imageBuffer = null;

		String strF_Top= null;

		String strF_Left= null;

		String strF_Width = null;

		String strF_Height = null;

		int iTop= 0;

		int iLeft = 0;

		int iWidth = 0;

		int iHeight= 0;

		String strClassName = null;

		String strSubClass = null;

		String rotation = null;

		Color color = null;

		Color transformed = null;

		try
		{
			logger.info(" Start of Pdf :: drawAnnotation");

			for(int j=0; j<list.size(); j++){

				Map data = (Map) list.get(j);

				strF_Top = data.get("F_TOP").toString();

				strF_Left = data.get("F_LEFT").toString();

				strF_Width = data.get("F_WIDTH").toString();

				strF_Height = data.get("F_HEIGHT").toString();

				if(null!=data.get("F_ROTATION"))
				{
					rotation = data.get("F_ROTATION").toString();
				}

				logger.info("::::::::::::::DrawAnnotation :: drawAnnotation :: str F_Top ..."+strF_Top +"......Left...."+strF_Left +"....Width...."+strF_Width+".....Height...."+strF_Height);

				if(strF_Top.length()>=4)
				{

					if(strF_Top.substring(0, strF_Top.lastIndexOf(".")).length()==2)

						strF_Top = strF_Top.substring(0, 5);

					else

						strF_Top = strF_Top.substring(0, 4);
				}
				else
				{
					strF_Top = strF_Top.substring(0,3);

					strF_Top =strF_Top+"0";
				}
				if(strF_Left.length()>=4)
				{
					if(strF_Left.substring(0, strF_Left.lastIndexOf(".")).length()==2)

						strF_Left = strF_Left.substring(0,5);

					else

						strF_Left = strF_Left.substring(0, 4);
				}
				else
				{
					strF_Left = strF_Left.substring(0,3);

					strF_Left= strF_Left+"0";
				}
				if(strF_Width.length()>=4)
				{
					if(strF_Width.substring(0,strF_Width.lastIndexOf(".")).length()==2)

						strF_Width =strF_Width.substring(0,5);

					else

						strF_Width = strF_Width.substring(0, 4);
				}
				else
				{	
					strF_Width = strF_Width.substring(0,3);

					strF_Width=strF_Width+"0";
				}
				if(strF_Height.length()>=4)
				{
					if(strF_Height.substring(0,strF_Height.lastIndexOf(".")).length()==2)

						strF_Height = strF_Height.substring(0,5);
					else

						strF_Height = strF_Height.substring(0, 4);
				}
				else
				{	
					strF_Height = strF_Height.substring(0,3);

					strF_Height=strF_Height+"0";
				}

				strF_Top = strF_Top.replace(".","");

				strF_Left = strF_Left.replace(".","");

				strF_Width = strF_Width.replace(".","");

				strF_Height = strF_Height.replace(".","");

				logger.info("StrF_Top ....."+data.get("F_TOP").toString()+"---Converted to ---"+strF_Top+"StrF_Left ..."+ data.get("F_LEFT").toString()+"......"+strF_Left);

				logger.info("strF_Width ....."+data.get("F_WIDTH").toString()+"---Converted to ---"+strF_Width+"strF_Height ..."+ data.get("F_HEIGHT").toString()+"......"+strF_Height);

				iTop  = (int)(Integer.parseInt(strF_Top));

				iLeft = (int)(Integer.parseInt(strF_Left));

				iWidth = (int)(Integer.parseInt(strF_Width));

				iHeight=(int)(Integer.parseInt(strF_Height));

				if(extension.equalsIgnoreCase("png"))
					extension ="gif";
				/*
				if(Integer.toString(iTop).length()==2)
					iTop= iTop*10;
				if(Integer.toString(iLeft).length()==2)
					iLeft =iLeft*10;
				if(Integer.toString(iHeight).length()<2)
					iHeight = iHeight*10;
				//if(iHeight>10&&iHeight<99)

				if(Integer.toString(iWidth).length()==2)
					iWidth = iWidth*10;*/

				logger.info(".......Top.."+iTop+"....Left..."+iLeft+"......Width..."+iWidth+"......Height...."+iHeight);

				//logger.info(" F_FORECOLOR....................."+data.get("F_FORECOLOR").toString());

				if(null!=data.get("F_FORECOLOR"))
				{
					color = Color.decode(data.get("F_FORECOLOR").toString());

					transformed = new Color(color.getBlue(),color.getGreen(),color.getRed());

				}
				logger.info("DrawAnnotation :: drawAnnotation :: Color Transformed ::"+transformed);

				logger.info("DrawAnnotation :: drawAnnotation :: MSG TXT :::"+data.get("MSGTEXT"));

				if(null!=data.get("MSGTEXT"))
				{
					comment = data.get("MSGTEXT").toString();

					logger.info("Comment ...."+comment);

					imageBuffer = ImageIO.read(new File(fileName));

					buffered = (BufferedImage)imageBuffer;

					font = new Font(data.get("F_FONT_NAME").toString().trim(),Font.BOLD,Integer.parseInt(data.get("F_FONT_SIZE").toString())+5);

					logger.info("::::::::::::::DrawAnnotation :: drawAnnotation :: buffered....Height ---"+buffered.getHeight()+"-----Width-----"+buffered.getWidth());

					g2Object = (Graphics2D)buffered.getGraphics();

					g2Object.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

					g2Object.setFont(font);

					logger.info("data.get(F_FORECOLOR).toString()---------"+data.get("F_FORECOLOR").toString());

					//g2Object.setColor((Color)hex2Rgb(data.get("F_FORECOLOR").toString(),"image"));

					logger.info("Color :::---"+color +"----Transformed ------"+transformed);

					g2Object.setColor(transformed);

					logger.info("Rotation ::::-*******-------"+rotation);



					if(null!=rotation)
					{
						// get the current font
						Font theFont = g2Object.getFont();

						// Create a rotation transformation for the font.
						AffineTransform fontAT = new AffineTransform();

						fontAT.rotate(-Integer.parseInt(rotation.trim()));

						Font theDerivedFont = theFont.deriveFont(fontAT);

						g2Object.setFont(theDerivedFont);

						rotation = null;

						g2Object.drawString(comment,iLeft,iTop);

						// put the original font back
						g2Object.setFont(theFont);

						//drawString(g2Object,comment, iLeft,iTop);

						boolean flag= ImageIO.write(buffered, extension,new File(fileName));

						logger.info("Flag :::"+flag+"..............."+fileName);


						g2Object.dispose();

						imageBuffer=null;

						buffered = null;

					}
					else
					{
						g2Object.drawString(comment,iLeft,iTop);

						/*	//drawString(g2Object,comment, iLeft,iTop);

						if(extension.equalsIgnoreCase("png"))
							extension ="gif";*/

						boolean flag= ImageIO.write(buffered, extension,new File(fileName));

						logger.info("Flag :::"+flag+"..............."+fileName);

						g2Object.dispose();

						imageBuffer=null;

						buffered = null;
					}


				}

				else
				{
					logger.info(" Start of Pdf :: drawAnnotation :: Else");

					strClassName = data.get("F_CLASSNAME").toString();

					if(null!=data.get("F_SUBCLASS"))
						strSubClass =data.get("F_SUBCLASS").toString();

					logger.info("Class Name ----"+strClassName+"---Sub Class Name ::"+strSubClass);

					if(strClassName.equalsIgnoreCase("Highlight"))
					{
						color = Color.decode(data.get("F_BRUSHCOLOR").toString());

						transformed = new Color(color.getBlue(),color.getGreen(),color.getRed(),130);

						logger.info("DrawAnnotation :: drawAnnotation :: Highlight :: F_BrushColor ::"+data.get("F_BRUSHCOLOR").toString()+"::Transformed ::"+transformed);

						imageBuffer = ImageIO.read(new File(fileName));

						buffered = (BufferedImage)imageBuffer;

						logger.info("buffered....Height ---"+buffered.getHeight()+"-----Width-----"+buffered.getWidth());

						g2Object = (Graphics2D)buffered.getGraphics();

						g2Object.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

						logger.debug("Inside of Highlight annotate ");

						//Color myColour = new Color(255, 255,0, 110);

						g2Object.setPaint(transformed);

						//g2Object.setColor());

						logger.info("iLeft,iTop,iWidth,iHeight"+iLeft+",,,,,,,"+iTop+"......."+iWidth+"........"+iHeight);

						g2Object.fillRect(iLeft,iTop,iWidth,iHeight);

						if(extension.equalsIgnoreCase("png"))
							extension ="gif";

						boolean flag= ImageIO.write(buffered, extension,new File(fileName));

						logger.info("Flag :::"+flag+"..............."+fileName);

						g2Object.dispose();

						logger.debug("End of Highlight annotate ");

						imageBuffer=null;

						buffered = null;
					}
					else if (strClassName.equalsIgnoreCase("Arrow"))
					{
						logger.info("Start of Arrow ");

						imageBuffer = ImageIO.read(new File(fileName));

						buffered = (BufferedImage)imageBuffer;

						logger.info("buffered....Height ---"+buffered.getHeight()+"-----Width-----"+buffered.getWidth());

						g2Object = (Graphics2D)buffered.getGraphics();

						int length = 80;

						int barb = 15;

						double angle = Math.toRadians(20);

						Path2D.Double path = new Path2D.Double();

						path.moveTo(-length/2, 0);

						path.lineTo(length/2, 0);

						double x = length/2 - barb*Math.cos(angle);

						double y = barb*Math.sin(angle);

						path.lineTo(x, y);

						x = length/2 - barb*Math.cos(-angle);

						y = barb*Math.sin(-angle);

						path.moveTo(length/2, 0);

						path.lineTo(x, y);

						AffineTransform at = AffineTransform.getTranslateInstance(iLeft,iTop);

						at.scale(2.0, 2.0);

						Shape shape = at.createTransformedShape(path);

						g2Object.setPaint(transformed);

						g2Object.draw(shape);

						logger.info("End of Arrow");
					}
					//F_SUBCLASS="v1-Rectangle"
					else if(strClassName.equalsIgnoreCase("Proprietary"))
					{
						Color myColor = Color.RED;
						
						Color converted =Color.RED;
						
						logger.info(" Start of sub class "+strSubClass);

						imageBuffer = ImageIO.read(new File(fileName));

						buffered = (BufferedImage)imageBuffer;

						logger.info("buffered....Height ---"+buffered.getHeight()+"-----Width-----"+buffered.getWidth());

						g2Object = (Graphics2D)buffered.getGraphics();

						g2Object.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

						logger.debug("Inside of Redact annotate ");

						if(null!=data.get("F_LINE_COLOR"))
						{
								myColor = Color.decode(data.get("F_LINE_COLOR").toString());
							
								converted = new Color(myColor.getBlue(),myColor.getGreen(),myColor.getRed());

						}
						if(strSubClass.equalsIgnoreCase("v1-Redaction"))
						{

							Color myColour = new Color(0,0,0);
							
							g2Object.setColor(myColour);
							
							g2Object.setPaint(myColour);
							
							g2Object.fillRect(iLeft,iTop,iWidth,iHeight);
							
							g2Object.setPaint(myColour);
														
							g2Object.setColor(myColour);

							logger.debug("End of Redact annotate ");
						}
						else if(strSubClass.equalsIgnoreCase("v1-Rectangle"))
						{
							logger.debug("Start of Rectalngele annotate ");

							g2Object.setColor(converted);

							//g2Object.setBackground(myColour);

							float thickness = 2f;

							g2Object.setStroke(new BasicStroke(thickness));

							g2Object.drawRect(iLeft,iTop,iWidth,iHeight);


							logger.debug("End of Rectalngele annotate ");
						}
						else if (strSubClass.equalsIgnoreCase("v1-Line"))
						{

							g2Object.setColor(converted);

							String startX = data.get("F_LINE_START_X").toString();

							String endX = data.get("F_LINE_END_X").toString();

							String startY = data.get("F_LINE_START_Y").toString();

							String endY = data.get("F_LINE_END_Y").toString();

							startX = startX.substring(0, 4).replace(".","");

							endX=	endX.substring(0, 4).replace(".","");

							startY = startY.substring(0, 4).replace(".","");

							endY = endY.substring(0,4).replace(".","");

							logger.info("Start ::::X::"+startX+"---startY---"+startY+"---endX--"+endX+"-----endY---"+endY);

							g2Object.drawLine(Integer.parseInt(startX), Integer.parseInt(startY),Integer.parseInt(endX),Integer.parseInt(endY));

						}

						if(extension.equalsIgnoreCase("png"))
							extension ="gif";

						boolean flag= ImageIO.write(buffered, extension,new File(fileName));

						logger.info("Flag :::"+flag+"..............."+fileName);

						g2Object.dispose();

						imageBuffer=null;

						buffered = null;

						logger.debug("End of Redact annotate ");
					}

					/*if(!extension.equalsIgnoreCase("jpeg")||!extension.equalsIgnoreCase("jpg"))
						improveQualityOfImage(fileName,extension);*/
					logger.info(" End of Pdf :: drawAnnotation :: Else");
				}
			}	

			logger.info(" End of Pdf :: drawAnnotation");
		}

		catch(Exception execeptionObject)
		{
			logger.error("Exception in Draw Annotation :::"+execeptionObject.getMessage(),execeptionObject);
		}
		finally
		{
			font = null;

			comment = null;

			buffered = null;

			g2Object = null;

			imageBuffer = null;

			strF_Top= null;

			strF_Left= null;

			strF_Width = null;

			strF_Height = null;

			iTop= 0;

			iLeft = 0;

			iWidth = 0;

			iHeight= 0;

			strClassName = null;
		}
	}
	/**
	 * Method to improve the quality of the Image
	 * @param fileName
	 * @param extension
	 */
	private static void improveQualityOfImage(String fileName,String extension) {

		try{
			logger.info("Start of improveQualityOfImage");

			ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(fileName);

			Iterator<ImageWriter> iterator = ImageIO.getImageWritersByFormatName(extension);

			ImageWriter imageWriter = iterator.next();

			ImageWriteParam writeParam = imageWriter.getDefaultWriteParam();

			writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);

			//writeParam.setCompressionType("MODE_EXPLICIT");

			writeParam.setCompressionQuality(0.9f);

			imageWriter.setOutput(imageOutputStream);

			imageWriter.write(null,new IIOImage(ImageIO.read(new File(fileName)),null,null),writeParam);		

			logger.info("End of improveQualityOfImage");
		}
		catch(Exception exceptionObject)
		{
			logger.error("Exception in improveQualityOfImage :: "+exceptionObject.getMessage(),exceptionObject);

		}
	}
	/**
	 * @param list
	 * @param fileName
	 * @param extension
	 * @param annotationList
	 */
	private static void drawAnnotationsToImage(List list, String fileName,String extension,List annotationList) {

		ImageDecoder decoder = null;

		Font f = null;

		String comment = null;

		String xIndex = null;

		String yIndex = null;

		String height =null;

		String width = null;

		ParameterBlock paramBlock = null;

		BufferedImage buffered = null;

		Graphics2D g2 = null;

		String commentAppended = null;

		String commentAppendedFinal = "";

		RenderedImage imageBuffer = null;

		ImageDecodeParam decodeParam = null;

		int pageNo =0;

		String colorParam ="";

		int x =0;
		int y=0;
		int z= 0;


		List<String> outPutStreamList = new ArrayList<String>();

		try
		{
			logger.info("List . size :::"+annotationList.size());

			imageBuffer = ImageIO.read(new File(fileName));

			for (int i = 0; i < annotationList.size(); i++) 
			{

				logger.info("Iteration :::"+i);

				logger.info("Content Type ::"+extension);

				String value = annotationList.get(i).toString();

				logger.info("Value :::"+value);

				String[] valuesArray = value.split("\\|");

				xIndex = valuesArray[1];

				yIndex = valuesArray[2];

				width = valuesArray[3];

				height = valuesArray[4];

				String fontString = valuesArray[5];

				String fontHeight = valuesArray[6];

				colorParam = valuesArray[9].split("=")[1].trim();

				f = new Font(fontString,Font.BOLD,Integer.parseInt(fontHeight.split("=")[1].trim()));

				comment =valuesArray[7];

				pageNo = Integer.parseInt(valuesArray[8].split("=")[1].trim());

				logger.info("ZXindex ..."+xIndex+"---Page No ---"+pageNo);

				buffered = (BufferedImage)imageBuffer;

				logger.info("buffered.... Non Tiff"+buffered.getHeight());

				g2 = (Graphics2D)buffered.getGraphics();

				g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

				g2.setFont(f);

				String[] arr = colorParam.split(",");

				x=Integer.parseInt(arr[0].trim());

				y=Integer.parseInt(arr[1].trim());

				z=Integer.parseInt(arr[2].trim());

				Color color = new Color(x,y,z);

				logger.info("Color ..........."+color);

				g2.setColor(color);

				if(extension.equalsIgnoreCase("png"))
					extension = "gif";

				g2.drawString(comment.split("=")[1].trim(),Integer.parseInt(xIndex.split("=")[1].trim()),Integer.parseInt(yIndex.split("=")[1].trim()));

				boolean flag= ImageIO.write(buffered, extension,new File(fileName));

				logger.info("Flag :::"+flag+"..............."+fileName);

				g2.dispose();

			}
		}
		catch(Exception exceptionObject)
		{
			logger.error("Exception in drawAnnotationsToImage :::"+exceptionObject,exceptionObject);
		}
		finally{

		}
	}
	/**
	 * 
	 * @param annolationList
	 * @param fileName
	 * @throws IOException
	 */
	private static void generateASPOSEPDFWithAnnotations(List<Annotation> annolationList, String fileName) throws IOException {

		logger.info("ENTERED into generateASposePDFWithAnnotations method....");

		com.aspose.pdf.Document pdfDocument = null;

		PdfReader reader = null;

		String extn = null;

		String temp = null;

		//int xValue =0;

		//int yValue = 0 ;

		String strClassName = "";

		String strSubClass = "";

		try
		{

			pdfDocument = new com.aspose.pdf.Document(fileName);

			int pagecount = pdfDocument.getPages().size();		

			logger.info("PDF PAGE COUTN="+pagecount);

			reader = new PdfReader(fileName);

			for(int i = 1;i <= pagecount;i++){

				double height = new Double(reader.getPageSizeWithRotation(i).getHeight());

				com.aspose.pdf.Page pdfPage = pdfDocument.getPages().get_Item(i);

				logger.info("....."+pdfPage.getRect());

				logger.info(".........."+pdfPage.getPageInfo());

				logger.info("PAGE No : " +i);

				logger.info(annolationList.size());

				for(int j=0; j<annolationList.size(); j++){

					Map data = (Map) annolationList.get(j);
					
					if(null!=data.get("F_TOP"))
					{

					String	strF_Top = data.get("F_TOP").toString().trim().replace(".", "");

					String	strF_Left = data.get("F_LEFT").toString().trim().replace(".", "");

					String	strF_Width = data.get("F_WIDTH").toString().trim().replace(".", "");

					String	strF_Height = data.get("F_HEIGHT").toString().trim().replace(".", "");

					logger.info("Annotate Object Values ::--Top--"+strF_Top+"-----HEIGHT----"+strF_Height+
							"------LEFT-----"+strF_Left+"----F_WIDTH-------"+strF_Width);

					if(null!=data.get("F_MULTIPAGETIFFPAGENUMBER"))
					{
						int pageNumber = Integer.parseInt(data.get("F_MULTIPAGETIFFPAGENUMBER").toString());

						if(null!=data.get("F_SUBCLASS"))
							strSubClass =data.get("F_SUBCLASS").toString();

						if(pageNumber==0)
							pageNumber=1;

						if(pageNumber==i)
						{
							logger.info("Height :::: "+height +"00000000"+getASPHeight(Double.parseDouble(data.get("F_TOP").toString())));

							logger.info("......LEFT........."+Double.parseDouble(data.get("F_LEFT").toString()));

							double x = height-getASPHeight(Double.parseDouble(data.get("F_TOP").toString()));

							double y = getASPWidth(Double.parseDouble(data.get("F_LEFT").toString()));

							logger.info("msg Txt;;;"+data.get("MSGTEXT"));

							Color pdfColor = null;

							Color pdfConverted = null;

							if(null!=data.get("F_FORECOLOR"))
							{
								pdfColor = Color.decode(data.get("F_FORECOLOR").toString());

								pdfConverted = new Color(pdfColor.getBlue(),pdfColor.getGreen(),pdfColor.getRed());

							}

							if(null!=data.get("MSGTEXT"))
							{
								logger.info(" F_FORECOLOR....................."+data.get("F_FORECOLOR").toString());

								com.aspose.pdf.TextStamp textStamp = new com.aspose.pdf.TextStamp(data.get("MSGTEXT").toString());

								//com.aspose.pdf.TextStamp textStamp = new com.aspose.pdf.TextStamp(data.get("MSGTEXT").toString());

								logger.info(data.get("MSGTEXT").toString() +" = >  "+(Double.parseDouble(data.get("F_LEFT").toString())+" = "+x));

								textStamp.getTextState().setFont(new com.aspose.pdf.FontRepository().findFont("Arial"));

								textStamp.getTextState().setFontSize(Integer.parseInt(data.get("F_FONT_SIZE").toString())+5);

								if(data.get("F_CLASSNAME").toString()!=null && !data.get("F_CLASSNAME").toString().equals("Text")){

									textStamp.setRotateAngle(Integer.parseInt(data.get("F_ROTATION").toString()));
									//Rotation object
									if(Integer.parseInt(data.get("F_ROTATION").toString())>0 && x>500){
										x = x - (Double.parseDouble(data.get("F_TOP").toString())*50);

									}
									if(Integer.parseInt(data.get("F_ROTATION").toString())>0 && y>400){
										y = y - ((Double.parseDouble(data.get("F_LEFT").toString())*12));
									}
								}
								logger.info("x........"+x +"....y:::"+y);

								textStamp.setXIndent(y);

								textStamp.setYIndent(x);

								//String xIndex =data.get("F_TOP").toString().trim().replace(".", "").substring(0,3);

								//String yIndex =data.get("F_LEFT").toString().trim().replace(".", "").substring(0,3);

								//logger.info("Xindex :::"+xIndex+":::YIndex :::"+yIndex);

								//textStamp.setXIndent(Integer.parseInt(yIndex));

								//textStamp.setYIndent(Integer.parseInt(xIndex));

								//textStamp.getTextState().setForegroundColor((com.aspose.pdf.Color)hex2Rgb(data.get("F_FORECOLOR").toString(),"pdf"));

								textStamp.getTextState().setForegroundColor(com.aspose.pdf.Color.fromRgb(pdfConverted));

								pdfDocument.getPages().get_Item(i).addStamp(textStamp);
							}
							else
							{
								Color pdfColorHighlight = null;

								Color pdfConvertedHighlight = null;

								if(null!=data.get("F_FILLCOLOR"))
								{
									pdfColorHighlight = Color.decode(data.get("F_FILLCOLOR").toString());

									pdfConvertedHighlight = new Color(pdfColorHighlight.getBlue(),pdfColorHighlight.getGreen(),pdfColorHighlight.getRed());

								}

								logger.info(" Start of Pdf :: drawAnnotation :: Else");

								strClassName = data.get("F_CLASSNAME").toString();

								if(null!=data.get("F_SUBCLASS"))
									strSubClass =data.get("F_SUBCLASS").toString();

								logger.info("Class Name ----"+strClassName+"---Sub Class Name ::"+strSubClass);

								if(strClassName.equalsIgnoreCase("Highlight"))
								{
									logger.info(" Inside of Highlight......");

									logger.info("::----- Inside of Highlight ---------::");
									
									FloatingBox floatingBox = new FloatingBox();
									
									floatingBox.setBackgroundColor(com.aspose.pdf.Color.getYellow());
									
									
									
									aspose.pdf.FloatingBox floatingBox2 = new aspose.pdf.FloatingBox();
									
									
									
									/*int top = (int)(Integer.parseInt(strF_Top)/13.88888888888889);
									int left = (int)(Integer.parseInt(strF_Left)/13.88888888888889);
									int width = (int)(Integer.parseInt(strF_Width)/13.88888888888889);
									int height1 = (int)(Integer.parseInt(strF_Height)/13.88888888888889);
									logger.info("Highlight :::----Top------"+top+"-----LEFT--"+left+"---Width---"+width+"----Height-----"+height1);
									java.awt.Rectangle rectangle = new java.awt.Rectangle(100,100,100,100);
									logger.info("Rectangle ::----"+com.aspose.pdf.Rectangle.fromRect(rectangle));
									HighlightAnnotation  highlightAnnotation = new HighlightAnnotation(pdfDocument.getPages().get_Item(i),com.aspose.pdf.Rectangle.fromRect(rectangle));
									highlightAnnotation.setColor(com.aspose.pdf.Color.getYellow());
									highlightAnnotation.setName("dsdsdsdsds");
									highlightAnnotation.setOpacity(8f);
									AnnotationSelector annotationSelector = new AnnotationSelector(highlightAnnotation);
									annotationSelector.visit(highlightAnnotation);
									pdfDocument.getPages().get_Item(i).accept(annotationSelector);
									//highlightAnnotation.s(new com.aspose.pdf.BorderInfo(com.aspose.pdf.BorderSide.All, 1F));
									//pdfDocument.getPages().get_Item(i).setRect(com.aspose.pdf.Rectangle.fromRect(rectangle));
									//logger.info("HightLight Annotation ::----"+highlightAnnotation);
									//pdfDocument.getPages().get_Item(i).setRect(rectangle);
									 */
									
									pdfDocument.getPages().get_Item(i);
															
									//	java.awt.Rectangle rectangle = new java.awt.Rectangle(100,100,100,100);
									//
									//	logger.info("Rectangle ::----"+com.aspose.pdf.Rectangle.fromRect(rectangle));
									//													       
									// pdfDocument.getPages().get_Item(i).setRect(com.aspose.pdf.Rectangle.fromRect(rectangle));



								}

								//F_SUBCLASS="v1-Rectangle"
								else if(strClassName.equalsIgnoreCase("Proprietary"))
								{
									logger.info(" Start of sub class for Pdf  "+strSubClass);

									logger.debug("Inside of Redact annotate  for Pdf ");

									if(strSubClass.equalsIgnoreCase("v1-Redaction"))
									{
										logger.debug("Start of Redact annotate for Pdf  ");
										logger.debug("End of Redact annotate for Pdf  ");
									}
									else if(strSubClass.equalsIgnoreCase("v1-Rectangle"))
									{
										logger.debug("Start of Rectalngele annotate for Pdf  ");

										logger.debug("End of Rectalngele annotate for Pdf ");
									}
									else if (strSubClass.equalsIgnoreCase("v1-Line"))
									{
										logger.debug("Start of v1-Line annotate for Pdf  ");

										logger.debug("End of v1-Line annotate for Pdf ");
									}
									logger.debug("End of Redact annotate for Pdf ");
								}
							}
							extn = fileName.substring(fileName.lastIndexOf("."), fileName.length());

							temp = fileName.substring(0, fileName.lastIndexOf("."));
						}
					}
				}
				}
			}

			logger.info("Temp File Path ::::--"+temp+"O"+extn);

			pdfDocument.save(temp+"O"+extn);

			pdfDocument.close();

			reader.close();

			pdfDocument = null;

			reader = null;
		}
		catch (Exception e)
		{
			logger.info("ERROR WHILE WRITING ANNATATIONS on PDF.."+e.getMessage(),e);
		}
		finally
		{

			try{
				if(null!=pdfDocument)
				{

					pdfDocument.close();

					pdfDocument = null;
				}
				if(null!=reader)
				{
					reader.close();

					reader = null;
				}

				logger.info("ZZZZZZZZZZZZZZZZZZ : deleting "+fileName);

				deleteTEMPFile(fileName);

				renameCreatedFile(temp,extn);
			}
			catch (Exception e)
			{
				e.printStackTrace();

				logger.info("ERROR WHILE DELETING TEMP and closing streams.."+e.getMessage());

			}
		}
	}
	/**
	 * @param fileName
	 * @param extension
	 */
	private static void renameCreatedFile(String fileName,String extension) 
	{
		File file = null;

		try{
			logger.info("----------------------------Start of renameCreatedFile-----------------------------");

			logger.info("File Name :----"+fileName+"------Extension ----"+extension);

			file = new File (fileName+"O"+extension);

			file.renameTo(new File(fileName+extension));

			logger.info("----------------------------End of renameCreatedFile-------------------------------");
		}
		catch(Exception exceptionObject)
		{
			logger.error("Exception in renameCreatedFile :: Exception "+exceptionObject.getMessage(),exceptionObject);
		}
		finally{
			file = null;
		}

	}
	public static void main(String[] args) {
		//hex2Rgb("255", null);
		//System.out.println("......"+hex2Rgb("16777215", null));
		//System.out.println("......"+hex2Rgb("13026246", null));

		
		
	}
	/**
	 * @param colorStr
	 * @param type
	 * @return
	 */
	public static Object hex2Rgb(String colorStr,String type)
	{
		Color color = null;
		try
		{
			logger.info("Start of hex2Rgb ::-"+color.decode(colorStr));

			logger.info("ColorStr of hex2Rgb ::-"+Integer.parseInt(colorStr,16));

		}
		catch(Exception exception)
		{
			exception.printStackTrace();
		}
		return color;

	}
	/**
	 * 
	 * @param height
	 * @return
	 */
	private static double getASPHeight(double height){
		return height*73;
	}
	/**
	 * @param width
	 * @return
	 */
	private static double getASPWidth(double width){
		return width*79;
	}
	/**
	 * 
	 * @param hex
	 * @return
	 */
	private static String convertHexToString(String hex)
	{
		StringBuilder sb = new StringBuilder();

		StringBuilder temp = new StringBuilder();

		for (int i = 0; i < hex.length() - 1; i += 2) {

			String output = hex.substring(i, (i + 2));

			int decimal = Integer.parseInt(output, 16);

			if(decimal==10)
				sb.append("\\n");

			else
				sb.append((char) decimal);

			logger.info(" Decimal ::---"+i+"..."+(char)decimal);

			temp.append(decimal);
		}

		return sb.toString();
	}
	/**
	 * 
	 * @param fileName
	 */
	public static void convertWordToPdf(String fileName)
	{
		File file = null;

		String fileConverted = null;

		try{

			com.aspose.words.Document doc = new com.aspose.words.Document(fileName);

			fileConverted =fileName.substring(0,fileName.lastIndexOf("."));

			logger.info("convertWordToPdf ::-File Name :::"+fileConverted);

			doc.save(fileConverted+".pdf");

			file = new File(fileName);

			if(file.exists()){

				logger.info("convertWordToPdf ---FILE EXIST in TEMP LOCATION. DELETING FROM TEMP LOCATION..");

				file.delete();

				logger.info("convertWordToPdf ---Is FILE EXIST::"+file.exists());
			}

		}
		catch(Exception e)
		{
			logger.error("Exception in convertWordToPdf ::"+e.getMessage(),e);
		}
		finally
		{
			file=null;
		}
		logger.info("End of addAnnotationToWord");
	}
	/**
	 * 
	 * @param fileName
	 * @throws DocumentException
	 * @throws IOException
	 */
	public static void convertToPdf(String fileName) throws DocumentException, IOException{

		RandomAccessFileOrArray myTifFile = null;

		com.itextpdf.text.Document TiffToPDF= null;

		try{

			logger.info("Entered into convertToPdf");

			myTifFile = new RandomAccessFileOrArray(fileName);

			int numberOfPages = TiffImage.getNumberOfPages(myTifFile);

			TiffToPDF = new com.itextpdf.text.Document(PageSize.A4);			

			String temp = fileName.substring(0, fileName.lastIndexOf("."));

			logger.info("Temp Path is .... : " +temp+".pdf");

			PdfWriter.getInstance(TiffToPDF, new FileOutputStream(temp+".pdf"));

			TiffToPDF.open();

			for(int tiffImageCounter = 1;tiffImageCounter <= numberOfPages;tiffImageCounter++) {

				Image img = TiffImage.getTiffImage(myTifFile, tiffImageCounter);

				img.scaleToFit(595,840);

				TiffToPDF.add(img);

				TiffToPDF.newPage();
			} //close loop

			TiffToPDF.close();

			myTifFile.close();

			TiffToPDF = null;

			myTifFile = null;

			logger.info("Exiting from convertToPdf");
		}
		catch (Exception e)
		{
			logger.error("ERROR while convering PDF."+e.getMessage(),e);

		}
		finally
		{
			try{

				deleteTEMPFile(fileName);

				if(null!=TiffToPDF){

					TiffToPDF.close();

					TiffToPDF= null;
				}

				if(null!=myTifFile){

					myTifFile.close();

					myTifFile= null;
				}
			}
			catch(Exception e) {

				logger.info("ERROR WHILE CLOSING STREAMS.."+e.getMessage(),e);
			}
		}
	}
	/**
	 * 
	 * @param fileName
	 */
	private static void deleteTEMPFile(String fileName){

		File file = null;

		try
		{
			file = new File(fileName);

			if(file.exists()){
				logger.info("FILE EXIST in TEMP LOCATION. DELETING FROM TEMP LOCATION..");

				file.delete();

				logger.info("Is FILE EXIST::"+file.exists());
			}

		}
		catch (Exception e)
		{
			logger.info("ERROR while file deleting.."+e.getMessage(),e);
		}
		finally
		{
			file = null;
		}
	}
}

