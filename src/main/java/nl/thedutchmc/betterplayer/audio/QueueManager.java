package nl.thedutchmc.betterplayer.audio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class QueueManager {

	private HashMap<Long, List<String>> queues = new HashMap<>();
	
	public List<String> getFullQueue(long guildId) {
		return queues.get(guildId);
	}
	
	public void clearQueue(long guildId) {
		queues.remove(guildId);
	}
	
	public void addToQueue(String newQueueItem, long guildId) {
		List<String> currentQueue = queues.get(guildId);
		if(currentQueue == null) {
			currentQueue = new ArrayList<>();
		}
		
		currentQueue.add(newQueueItem);
	}
}
