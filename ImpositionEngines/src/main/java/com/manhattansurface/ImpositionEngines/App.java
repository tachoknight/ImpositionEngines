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

		// The user needs to provide an ini file with the job info
		
		// If there are no arguments, then we should just return
		if (args.length < 1) {
			logger.error("Need a job ini file");
			return;
		}

		// Get the arguments
		String jobFile = args[0];

		// Now let's create the imposition engine  	
    	SignatureEngine1 se1 = new SignatureEngine1(jobFile);
    	se1.impose();
    	
        logger.info( "*** F I N I S H E D ***" );
    }
}
