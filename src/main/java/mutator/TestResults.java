package mutator;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.crawljax.browser.EmbeddedBrowser;
import com.crawljax.core.CrawlSession;
import com.crawljax.core.plugin.OnNewStatePlugin;
import com.crawljax.util.Helper;


public class TestResults implements OnNewStatePlugin{
	private static final Logger LOGGER = LoggerFactory.getLogger(TestResults.class);
	static SimpleDateFormat sdf = new SimpleDateFormat("ddMMyy-hhmmss");
	private String dir;
	private String resultFilenameAndPath;
	public TestResults(String outputFolder, String dirName){
		String folder=Helper.addFolderSlashIfNeeded(outputFolder);
		dir=folder + Helper.addFolderSlashIfNeeded(dirName);
		resultFilenameAndPath=dir+String.format("%s", sdf.format(new Date())) +".png";
	}
	@Override
	public void onNewState(CrawlSession session) {	

		try {
			LOGGER.info("taking snapshot of the browser");
			Helper.directoryCheck(dir);
			File file=new File(resultFilenameAndPath);
			EmbeddedBrowser browser=session.getBrowser();
			browser.saveScreenShot(file);
		
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}
