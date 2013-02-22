package runner;

import java.util.List;
import java.util.Random;

import com.crawljax.core.CandidateElement;
import com.crawljax.core.CrawlSession;
import com.crawljax.core.plugin.PreStateCrawlingPlugin;

/**
 * Modify candidate clickable list to only contain 1 random element. This plugin is usefull when
 * playing same-game.
 * 
 * @author Frank Groeneveld <frankgroeneveld@gmail.com>
 */
public class RandomClickable implements PreStateCrawlingPlugin {

	@Override
	public void preStateCrawling(CrawlSession session, List<CandidateElement> elements) {
		Random generator = new Random(session.hashCode());
		if (elements.size() > 0) {
			CandidateElement chosenOne = elements.get(generator.nextInt(elements.size()));
			elements.clear();
			elements.add(chosenOne);
		}
	}

}
