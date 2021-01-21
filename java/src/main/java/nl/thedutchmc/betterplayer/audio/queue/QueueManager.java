package nl.thedutchmc.betterplayer.audio.queue;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import nl.thedutchmc.betterplayer.audio.BetterAudioManager;

public class QueueManager {

	private HashMap<Long, List<QueueItem>> queues = new HashMap<>();
	private HashMap<Long, Integer> queueIndexes = new HashMap<>();
	@SuppressWarnings("unused")
	private BetterAudioManager betterAudioManager;
	
	public QueueManager(BetterAudioManager betterAudioManager) {
		this.betterAudioManager = betterAudioManager;
	}
	
	public List<QueueItem> getFullQueue(long guildId) {
		return queues.get(guildId);
	}
	
	public void clearQueue(long guildId) {
		queues.remove(guildId);
	}
	
	public QueueItem getQueueItemAtIndex(long guildId, int index) {
		List<QueueItem> queue = queues.get(guildId);
		if(index >= queue.size()) {
			return null;
		}
		
		return queue.get(index);
	}
	
	public void setQueue(long guildId, List<QueueItem> newQueue) {
		queues.put(guildId, newQueue);
	}
	
	public QueueItem getCurrentQueueItem(long guildId) {
		List<QueueItem> queue = queues.get(guildId);
		int queueIndex = queueIndexes.get(guildId);
				
		if(queueIndex > (queue.size() -1))  {
			return null;
		}
				
		return queue.get(queueIndex);
	}
	
	public boolean deleteItemFromQueue(long guildId, int queueIndex) {
		List<QueueItem> queue = queues.get(guildId);
		
		if(queueIndex >= queue.size()) {
			return false;
		}
		
		queue.remove(queueIndex);
		queues.put(guildId, queue);
		
		return true;
	}
	
	public void incrementQueueIndex(long guildId) {
		
		int queueIndex = queueIndexes.get(guildId);
		queueIndexes.put(guildId, (queueIndex +1));		
	}
	
	public void addToQueue(QueueItem newQueueItem, long guildId) {
		List<QueueItem> currentQueue = queues.get(guildId);
		if(currentQueue == null) {
			currentQueue = new LinkedList<>();
			queueIndexes.put(guildId, 0);
		}
		
		currentQueue.add(newQueueItem);
		queues.put(guildId, currentQueue);
	}
	
	public int getQueueIndex(long guildId) {
		Integer queueIndex = queueIndexes.get(guildId);
		if(queueIndex == null) {
			return -1;
		}
		
		return queueIndex;
	}
}