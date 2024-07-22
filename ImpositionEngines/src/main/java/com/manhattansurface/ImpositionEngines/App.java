package com.manhattansurface.ImpositionEngines;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hello world!
 *
 */
public class App 
{
	private static final Logger	logger	= LoggerFactory.getLogger(App.class);
	
    public static void main( String[] args )
    {
    	logger.info("Here we go...");

		// Now let's get the arguments, which there should be
		// 1. The job name
		// 2. The source PDF
		// 3. The output directory
		// 4. The number of pages per signature
		// 5. Whether or not to impose the job

		// If there are no arguments, then we should just return
		if (args.length < 5) {
			logger.error("Not enough arguments. We need 5: job name, source PDF, output directory, pages per signature, impose");
			logger.error("impose should always be true");
			return;
		}

		// Get the arguments
		String jobName = args[0];
		String sourcePDF = args[1];
		String outputDir = args[2];
		int pagesPerSignature = Integer.parseInt(args[3]);
		boolean impose = Boolean.parseBoolean(args[4]);

		// Now let's create the imposition engine  	
    	SignatureEngine1 se1 = new SignatureEngine1();
    	se1.impose(	jobName,
    				sourcePDF,
    				outputDir,
    				pagesPerSignature,
    				impose);
    	
        logger.info( "Hello World!" );
    }
}
