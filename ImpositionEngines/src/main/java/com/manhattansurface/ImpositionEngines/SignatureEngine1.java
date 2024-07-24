package com.manhattansurface.ImpositionEngines;

import java.io.FileOutputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextpdf.awt.geom.AffineTransform;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;

public class SignatureEngine1 {
	private static final Logger logger = LoggerFactory.getLogger(SignatureEngine1.class);

	private static float getLetterScale(float width, float height) {
		float scaleX = PageSize.LETTER.getWidth() / width;
		float scaleY = PageSize.LETTER.getHeight() / height;
		return Math.min(scaleX, scaleY);
	}

	public SignatureEngine1() {
	}

	public void impose(String jobName,
			String fileName,
			String outputDirectory,
			int sheetsPerSignature,
			boolean makeSinglePDF) {
		logger.debug("Sheets Per Signature: " + sheetsPerSignature);

		try {
			BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA,
					BaseFont.CP1252,
					BaseFont.NOT_EMBEDDED);

			PdfReader reader = new PdfReader(fileName);

			int numOfPages = reader.getNumberOfPages();
			int origNumOfPages = numOfPages;
			logger.debug("There are " + numOfPages + " pages in this document");

			if (numOfPages % 2 != 0) {
				logger.debug("Odd number of pages ... gonna be inserting one");
				numOfPages = numOfPages + 1;
			}

			logger.debug("We are now going to say that there are " + numOfPages
					+ " in this document");

			/* The size of our document */
			Rectangle psize = reader.getPageSize(1);
			float width = psize.getHeight();
			float height = psize.getWidth();
			logger.debug("Source width: " + width + " Height: " + height);

			/*
			 * For the number of sheets (actual physical pieces of paper) in the
			 * signature PAGES_PER_SHEET is the number of printed pages on a
			 * single piece of paper, front and back
			 */
			final int PAGES_PER_SHEET = 4; // make a parameter at some point
			logger.debug("PAGES_PER_SHEET = " + PAGES_PER_SHEET);

			/*
			 * This is the total number of signatures we're going to end up
			 * with, which translates into the number of PDFs made from the book
			 * which need to be printed sequentially
			 */
			float realNumOfSigs = ((float) numOfPages / PAGES_PER_SHEET) / sheetsPerSignature;
			logger.debug("Real number of sigs is " + realNumOfSigs);

			/*
			 * realNumOfSigs above is a float because there may be more pages
			 * than cleanly map to signatures, so we'll be creating an
			 * additional signature to include them, so we want to make sure we
			 * include that.
			 */
			double numberOfSignatures = Math.ceil(realNumOfSigs);

			logger.debug("Number of Signatures: " + numberOfSignatures);

			/* How many pages we can put in a single signature (PDF) */
			int maxPageNumInSig = PAGES_PER_SHEET * sheetsPerSignature;
			logger.debug("Max Pages per Signature: " + maxPageNumInSig);

			if (numOfPages < maxPageNumInSig) {
				logger.error("*** Source PDF has fewer pages than a signature (source has " + numOfPages
						+ " page"
						+ (numOfPages > 1 ? "s" : "\"\"")
						+ " and there are "
						+ maxPageNumInSig
						+ " pages per signature).");
				logger.error("*** Please adjust the source to fit " + maxPageNumInSig + " pages");
				logger.error("*** NOT CONTINUING ***");
				return;
			}

			/* Now loop for the total number of signatures we're going to make */
			int firstPageOfSig = 0;

			/*
			 * This is going to be our page counter, compared against the number
			 * of pages in the PDF (via numOfPages). We use this to determine
			 * when we've run out of pages in the source document, and if we
			 * have, but we have *not* yet run out of pages in the current
			 * signature, we'll pad the signature with blank pages
			 */
			int currentPageInDoc = 1;

			/*
			 * The PDF we will be outputting to...this can be either one
			 * (makeSinglePDF == true) or multiple, one per signature
			 */
			Document document = null;
			PdfWriter outputPDFWriter = null;
			PdfContentByte cb = null;
			if (makeSinglePDF) {
				document = new Document(new Rectangle(width, height));
				String outputFileName = outputDirectory + "/" + jobName + "_master" + ".pdf";
				logger.info("Our output: " + outputFileName);
				outputPDFWriter = PdfWriter.getInstance(document,
						new FileOutputStream(outputFileName));

				/*
				 * Make sure that if there are empty pages in the source, to
				 * have empty pages in the destination
				 */
				outputPDFWriter.setPageEmpty(false);

				document.open();

				cb = outputPDFWriter.getDirectContent();
			}

			for (int sigNum = 0; sigNum < numberOfSignatures; ++sigNum) {
				logger.debug("On SigNum " + (sigNum + 1) + " of " + numberOfSignatures);

				/* New PDF for each signature if that's what we want */
				if (makeSinglePDF == false) {
					document = new Document(new Rectangle(width, height));
					/* Set the name of the PDF */
					outputPDFWriter = PdfWriter.getInstance(document,
							new FileOutputStream(outputDirectory + jobName
									+ "sig"
									+ (sigNum + 1)
									+ ".pdf"));
					document.open();

					cb = outputPDFWriter.getDirectContent();
				} else {
					/*
					 * We're making a single PDF, but we need to be able to tell
					 * what signature we're on, so we're going to add a dummy
					 * page to indicate that
					 */
					document.newPage();

					cb.beginText();
					cb.setFontAndSize(bf, 32);
					cb.showTextAligned(PdfContentByte.ALIGN_CENTER,
							"Front separator page for SIGNATURE " + (sigNum + 1),
							width / 2,
							height / 2,
							0);
					cb.endText();

					/*
					 * And this is the *BACK* of the dummy page so duplexing
					 * doesn't get all confused
					 */
					document.newPage();

					cb.beginText();
					cb.setFontAndSize(bf, 32);
					cb.showTextAligned(PdfContentByte.ALIGN_CENTER,
							"Back separator page for SIGNATURE " + (sigNum + 1),
							width / 2,
							height / 2,
							0);
					cb.endText();

				}

				/*
				 * How we do all our calculations. This is the maximum pages per
				 * signature + 1;
				 */
				int magicNumber = maxPageNumInSig + 1;
				logger.debug("Our magic number is " + magicNumber);

				int leftPageNum = 0;
				int rightPageNum = 0;

				/*
				 * Now for each of the pages we're going to handle in this
				 * signature
				 */
				for (int handledPages = 0; handledPages < (maxPageNumInSig / 2); ++handledPages) {
					/* This is a page in the new document */
					document.newPage();

					logger.debug("Currently working on page " + currentPageInDoc
							+ " of "
							+ numOfPages);

					/************************************************************************
					 * Check whether we've run out of pages in our source
					 * document and if so, we need to add blank pages so that we
					 * close out the signature correctly, with the right number
					 * of pages
					 ************************************************************************/

					if (currentPageInDoc > numOfPages) {
						logger.debug("-->" + currentPageInDoc
								+ " > "
								+ numOfPages
								+ " so we added the page but not going further");
						cb.beginText();
						cb.setFontAndSize(bf, 19);
						cb.showTextAligned(PdfContentByte.ALIGN_CENTER,
								"",
								width / 2,
								height / 2,
								0);
						cb.endText();
						continue;
					}

					/************************************************************************
					 * Here's where we find out what our left page and right
					 * page numbers from the source document are
					 ************************************************************************/

					/* Now we get the right pages */
					if ((handledPages + 1) % 2 == 0) {
						logger.debug("We're on even, so it will be small / big");
						rightPageNum = magicNumber - (handledPages + 1) + firstPageOfSig;
						leftPageNum = magicNumber - rightPageNum + (firstPageOfSig * 2);

						logger.debug("LPN: " + leftPageNum + " RPN: " + rightPageNum);
					} else {
						logger.debug("We're on odd, so it will be big / small");
						leftPageNum = magicNumber - (handledPages + 1) + firstPageOfSig;
						rightPageNum = magicNumber - leftPageNum + (firstPageOfSig * 2);

						logger.debug("LPN: " + leftPageNum + " RPN: " + rightPageNum);
					}

					/*
					 * This is the page counter to determine when we've
					 * exhausted the number of pages in the source document. We
					 * multiply it by 2 because we handle two pages per sheet
					 */
					currentPageInDoc += 2;

					/************************************************************************
					 * Now we're going to actually get the pages we want from
					 * the source PDF and add them to our output PDF
					 ************************************************************************/

					/*
					 * First we'll do the right side (somewhat arbitrary here
					 * ... could be left)
					 */
					if (rightPageNum <= numOfPages) {
						logger.debug("On right side, adding page " + rightPageNum);
						PdfImportedPage rightPage = outputPDFWriter.getImportedPage(reader,
								rightPageNum);

						// cb.addTemplate(rightPage, 0.5f, 0, 0, .5f, width / 2 + 30, 140);
						cb.addTemplate(rightPage, 0.75f, 0, 0, 0.75f, (width / 2) - 40, 20);
					}

					if (leftPageNum <= numOfPages) {
						logger.debug("On left side, adding page " + leftPageNum);
						if (leftPageNum > origNumOfPages) {
							logger.warn("...but there is no page " + leftPageNum
									+ " in the document, so adding an empty");
							document.newPage();
						} else {
							PdfImportedPage leftPage = outputPDFWriter.getImportedPage(reader,
									leftPageNum);

							/* Now we actually add the content to our output PDF */
							// cb.addTemplate(leftPage, .5f, 0, 0, .5f, 60, 140);
							cb.addTemplate(leftPage, 0.75f, 0, 0, 0.75f, -30, 20);
						}
					}
				}

				/*
				 * Are we making individual signatures? If so, we need to close
				 * the PDF now or it won't be readable
				 */
				if (makeSinglePDF == false)
					document.close();

				/*
				 * Now position us to the first page of the next signature for
				 * the next go-around
				 */
				firstPageOfSig = firstPageOfSig + maxPageNumInSig;
				logger.debug("firstPageOfSig: " + firstPageOfSig);
			}

			/* And if we were making one big PDF, we're finally done with it */
			if (makeSinglePDF)
				document.close();
		} catch (IOException e) {
			logger.error("Whoops, got an IOException: " + e.getLocalizedMessage(), e);
		} catch (DocumentException e) {
			logger.error("Whoops, got a DocumentException: " + e.getLocalizedMessage(), e);
		}
	}
}
