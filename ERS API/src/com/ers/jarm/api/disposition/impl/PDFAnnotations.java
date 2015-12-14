package com.ers.jarm.api.disposition.impl; 

import java.awt.Color;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.aspose.pdf.Document;
import com.itextpdf.text.pdf.PdfReader;


public class PDFAnnotations
{

	static Logger logger = Logger.getLogger(PDFAnnotations.class);

	/**
	 * @param height
	 * 
	 * @return
	 */
	private static float getASPHeight(float height){
		return height*72;
	}

	/**
	 * @param width
	 * @return
	 */
	private static float getASPWidth(float width){
		return width*72;
	}


	public void readIniFile(String filepath, String iniFileName, String documentName) {
		
		logger.info("Entered into readIniFile method");

		String fileName = filepath +"/"+ iniFileName + ".ini";
		Properties properties = null;
		BufferedReader br = null;
		FileInputStream file_Url = null;
		FileInputStream fis = null;

		ArrayList<String> listXvalues = null;

		ArrayList<String> listYvalues = null;

		ArrayList<String> listTextvalues = null;

		ArrayList<String> listPageValues = null;

		try {
			fis = new FileInputStream(fileName);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		br = new BufferedReader(new InputStreamReader(fis));

		String line = null;
		try {

			listXvalues = new ArrayList<String>();

			listYvalues = new ArrayList<String>();

			listTextvalues = new ArrayList<String>();

			listPageValues = new ArrayList<String>();

			while ((line = br.readLine()) != null) {

				properties = new Properties();				 
				//file_Url = new FileInputStream(fileName);

				/*if (line == "[REDACT]" || line.equals("[REDACT]"))
				{
					//System.out.println("************* In Redact Annotation block  **************");

					properties.load(file_Url);

					String   xvalue = properties.getProperty("X");
					String   yvalue = properties.getProperty("Y");
					//generateRedactAnnotationwithPDFBox("C://Documents/{D66A9AAA-A138-49C1-88D8-328393930E27}/{FAAA865F-EF07-420A-A3C6-A57C44B250F6}/");

					logger.info("X Value : " + xvalue + " Y Value : " + yvalue);				  				   				    										
				}*/

				if (line == "[TEXT]" || line.equals("[TEXT]")) {

					properties = new Properties();

					StringBuffer buffer = new StringBuffer();

					for (int i = 0; i < 15; i++) {

						buffer.append(br.readLine() + "\n");						
					}
					properties = new Properties();

					InputStream inputStream = new ByteArrayInputStream(buffer.toString().getBytes());

					properties.load(inputStream);

					listXvalues.add(properties.getProperty("X"));
					listYvalues.add(properties.getProperty("Y"));
					listTextvalues.add(properties.getProperty("TEXT"));
					listPageValues.add(properties.getProperty("PAGE"));

					drawPDFAnnotations(filepath + "/" + documentName, listXvalues, listYvalues, listTextvalues, listPageValues);

					logger.info("X Value : "+properties.getProperty("X") + " Y Value : "+properties.getProperty("Y") +" Text Value : "+properties.getProperty("TEXT"));
				}

				if (line == "[LINE]" || line.equals("[LINE]")) {

					properties = new Properties();

					file_Url = new FileInputStream(fileName);

					properties.load(file_Url);

					String   x1value = properties.getProperty("X1");
					String   y1value = properties.getProperty("Y1");

					String   x2value = properties.getProperty("X2");
					String   y2value = properties.getProperty("Y2");

					logger.info("X1 Value : " + x1value + " X1 Value : " + y1value + " X2 Value : " + x2value + " Y2 Value : "+y2value);
				}
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}				
	}

	public void drawPDFAnnotations(String filePath, ArrayList<String> listXvalues, ArrayList<String> listYvalues, ArrayList<String> listTextvalues, ArrayList<String> listPagevalues) {

		logger.info("Entered into drawPDFAnnotations method");
		
		com.aspose.pdf.Document pdfDocument = null;

		PdfReader reader = null;

		com.aspose.pdf.Page pdfPage = null;

		Color pdfColor = null;

		com.aspose.pdf.TextStamp textStamp = null;

		Color pdfConverted = null;

		try {

			pdfDocument = new com.aspose.pdf.Document(filePath);

			int pageCount = pdfDocument.getPages().size();

			reader = new PdfReader(filePath);

			for (int k = 0; k < listXvalues.size(); k++) {

				String xvalue = listXvalues.get(k);

				String yvalue = listYvalues.get(k);

				String textvalue = listTextvalues.get(k);

				String pageValue = listPagevalues.get(k);

				logger.info("textvalue : " + textvalue);

				for(int i=1; i<=pageCount; i++) {

					if(pageValue.equalsIgnoreCase(i + "")) {

						double height = new Double(reader.getPageSize(i).getHeight());

						double width = new Double(reader.getPageSize(i).getWidth());

						logger.info("Height : "+height + "Width : "+width);

						pdfPage = pdfDocument.getPages().get_Item(i);

						logger.info("....."+pdfPage.getRect());

						pdfColor = Color.decode("255");

						textvalue = (textvalue==null) ? "" : textvalue;

						textStamp = new com.aspose.pdf.TextStamp(textvalue);

						pdfConverted = new Color(pdfColor.getBlue(),pdfColor.getGreen(),pdfColor.getRed());

						textStamp.getTextState().setFont(com.aspose.pdf.FontRepository.findFont("Arial"));

						textStamp.getTextState().setFontSize(15);

						textStamp.getTextState().setForegroundColor(com.aspose.pdf.Color.fromRgb(pdfConverted));

						int x = Integer.parseInt(xvalue);
						int y = Integer.parseInt(yvalue);

						float xIndent = x/(float)200;
						float yIndent = y/(float)200;

						xIndent = (float) getASPWidth(xIndent);	
						yIndent = (float) (height-getASPHeight(yIndent));

						logger.info("XIndent : "+xIndent +" YIndent : "+yIndent);

						textStamp.setScale(true);
						textStamp.setXIndent(xIndent);
						textStamp.setYIndent(yIndent - 16);

						pdfDocument.getPages().get_Item(i).addStamp(textStamp);

						//Rectangle rectangle = new Rectangle(0,0,1653,2339);
						logger.info(pdfDocument.getPages().get_Item(i).getRect());
					}
				}
			}
			pdfDocument.save(filePath);
			logger.info("Done!");
		} catch(Exception exception) {
			exception.printStackTrace();
		} finally {

			//pdfDocument = null;

			reader = null;

			pdfPage = null;

			pdfColor = null;

			textStamp = null;

			pdfConverted = null;

		}		

		//return pdfDocument;
	}

	public static void main(String[] args) throws IOException, Exception
	{
		//String filename = "C:/Users/Rameshnaidu/Desktop/Records Manager/VenkataRavi/pdf-sample.pdf";
		//DrawAnnotationonPDF.generateAsposePDFWithAnnotations(filename);
		//DrawAnnotationonPDF.generateAsposePDFWithTextRedactions(filename);
		//DrawAnnotationonPDF.generateNonAsposePDFWithAnnotations("C://BurnAnnotations/Input data/JPG_image_toPDF.pdf", "169", "41", "38", "45");	

		//DrawAnnotationonPDF annotationonPDF = new DrawAnnotationonPDF();
		//annotationonPDF.readIniFile();

		//DrawAnnotationonPDF.drawTwoAnnotations("C://BurnAnnotations/Input data/JPG_image_toPDF.pdf", "561", "898", "BurnAnnotation");


	}

}