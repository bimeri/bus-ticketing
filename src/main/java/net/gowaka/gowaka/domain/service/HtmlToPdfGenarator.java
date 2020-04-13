package net.gowaka.gowaka.domain.service;

import org.springframework.stereotype.Service;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Service
public class HtmlToPdfGenarator {

	public File createPdf(String processedHtml, String filename) throws Exception {

		  FileOutputStream os = null;
		final File outputFile;
	        try {
	            outputFile = File.createTempFile(filename, ".pdf");
	            os = new FileOutputStream(outputFile);

	            ITextRenderer renderer = new ITextRenderer();
	            renderer.setDocumentFromString(processedHtml);
	            renderer.layout();
	            renderer.createPDF(os, false);
	            renderer.finishPDF();
	        }
	        finally {
	            if (os != null) {
	                try {
	                    os.close();
	                } catch (IOException e) { /*ignore*/ }
	            }
	        }
	        return outputFile;
	}
}
