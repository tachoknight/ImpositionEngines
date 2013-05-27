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
    	
    	SignatureEngine1 se1 = new SignatureEngine1();
    	se1.impose(	"testjob",
    				"/Users/ron/git/kernelomicron/shells.pdf",
    				"/Users/ron/Desktop/digital_comp/",
    				4,
    				true);
    	
        logger.info( "Hello World!" );
    }
}
