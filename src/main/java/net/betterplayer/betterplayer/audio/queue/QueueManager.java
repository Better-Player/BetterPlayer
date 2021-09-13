package net.betterplayer.betterplayer.audio.queue;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class QueueManager {

	/**
	 * K = Guild Id<br>
	 * V = The LinkedList Queue for the Guild
	 */
	private HashMap<Long, Queue<QueueItem>> queues = new HashMap<>();
	
	/**
	 * K = Guild ID<br>
	 * V = QueueItem which is currently playing
	 */
	private HashMap<Long, QueueItem> nowPlaying = new HashMap<>();
	
	/**
	 * Set the QueueItem which is currently playing
	 * @param guildId The ID of the Guild
	 * @param queueItem The QueueItem to set. Null is allowed.
	 */
	public void setNowPlaying(long guildId, QueueItem queueItem) {
		this.nowPlaying.put(guildId, queueItem);
	}
	
	/**
	 * Get the QueueItem which is currently playing
	 * @param guildId The ID of the Guild
	 * @return Returns the requested QueueItem or null if there is nothing currently playing
	 */
	public QueueItem getNowPlaying(long guildId) {
		return this.nowPlaying.get(guildId);
	}
	
	/**
	 * Poll the next item in queue. Removes the item from the queue!
	 * @param guildId The ID of the Guild
	 * @return Returns the requested QueueItem. Returns null if the guild has no queue, or if the queue is empty
	 */
	public QueueItem pollQueue(long guildId) {
		Queue<QueueItem> guildQueue = this.queues.get(guildId);
		
		if(guildQueue == null) {
			return null;
		}
		
		return guildQueue.poll();
	}
	
	/**
	 * Peek the next item in queue. Does not remove the item from the queue
	 * @param guildId The ID of the Guild
	 * @return Returns the requested QueueItem. Returns null if the guild has no queue, or if the queue is empty.
	 */
	public QueueItem peekQueue(long guildId) {
		Queue<QueueItem> guildQueue = this.queues.get(guildId);
		
		if(guildQueue == null) {
			return null;
		}
		
		return guildQueue.peek();
	}
	
	/**
	 * Get the full queue for a Guild. Does not clear the queue
	 * @param guildId The ID of the guild
	 * @return Returns the queue as a LinkedList. Returns null if the Guild has no queue.
	 */
	public LinkedList<QueueItem> getFullQueue(long guildId) {
		Queue<QueueItem> guildQueue = this.queues.get(guildId);
		
		if(guildQueue == null) {
			return null;
		}
		
		return new LinkedList<QueueItem>(guildQueue);	
	}
	
	/**
	 * Remove an item from the queue for a guild<br>
	 * If the guild has no queue, this method will just return
	 * @param guildId The ID of the Guild
	 * @param index The index in the queue, 0-based.
	 */
	public void removeFromQueue(long guildId, int index) {
		Queue<QueueItem> guildQueue = this.queues.get(guildId);
		
		//Null-check
		if(guildQueue == null) {
			return;
		}
		
		//Convert to a LinkedList, because you cannot remove items from a queue at a given index
		List<QueueItem> guildQueueAsList = new LinkedList<QueueItem>(guildQueue);
		
		//Now remove the element
		guildQueueAsList.remove(index);
		
		//Next convert the LinkedList back into a LinkedList queue
		Queue<QueueItem> newGuildQueue = new LinkedList<QueueItem>(guildQueueAsList);
	
		//Set the new queue in place of the old one
		this.queues.put(guildId, newGuildQueue);
	}
	
	/**
	 * Add an item to the end of a Guild's queue<br>
	 * Returns without error if the Guild has no queue
	 * @param guildId The ID of the Guild
	 * @param queueItem The Item to add to the queue
	 */
	public void addToQueue(long guildId, QueueItem queueItem) {
		Queue<QueueItem> guildQueue = this.queues.get(guildId);
		
		if(guildQueue == null) {
			return;
		}
		
		guildQueue.add(queueItem);
		
		this.queues.put(guildId, guildQueue);
	}
	
	/**
	 * Add a QueueItem to the front of the queue
	 * @param guildId The ID of the Guild
	 * @param queueItem The QueueItem to add
	 */
	public void addToQueueFront(long guildId, QueueItem queueItem) {
		Queue<QueueItem> guildQueue = this.queues.get(guildId);
		
		if(guildQueue == null) {
			return;
		}

		List<QueueItem> queueItemList = new LinkedList<>();
		queueItemList.add(queueItem);
		
		List<QueueItem> existingGuildQueueAsList = new LinkedList<QueueItem>(guildQueue);
		queueItemList.addAll(existingGuildQueueAsList);
		
		//Set the new queue back in the hashmap
		Queue<QueueItem> newGuildQueue = new LinkedList<QueueItem>(queueItemList);
		this.queues.put(guildId, newGuildQueue);
	}
	
	/**
	 * Create a new Queue for a Guild
	 * @param guildId The ID of the guild
	 */
	public void createQueue(long guildId) {
		this.queues.put(guildId, new LinkedList<QueueItem>());
	}
	
	/**
	 * Delete a Queue for a Guild<br>
	 * Returns without error if the Guild has no queue
	 * @param guildId The ID of the guild
	 */
	public void deleteQueue(long guildId) {
		this.queues.remove(guildId);
	}
	
	/**
	 * Clear the Queue for a Guild. Does not remove the queue.<br>
	 * Returns without error if the Guild has no queue
	 * @param guildId The ID of the guild
	 */
	public void clearQueue(long guildId) {
		Queue<QueueItem> guildQueue = this.queues.get(guildId);
		
		if(guildQueue == null) {
			return;
		}
		
		this.nowPlaying.remove(guildId);
		
		guildQueue.clear();
		this.queues.put(guildId, guildQueue);
	}
	
	/**
	 * Check if a Guild has a queue
	 * @param guildId The ID of the guild
	 * @return Returns true if the guild has a queue, false if it does not
	 */
	public boolean hasQueue(long guildId) {
		return this.queues.get(guildId) != null;
	}
	
	/**
	 * Get the size of a Guild's queue
	 * @param guildId The ID of the Guild
	 * @return Returns the size of the queue, or null if the guild has no queue
	 */
	public Integer getQueueSize(long guildId) {
		Queue<QueueItem> guildQueue = this.queues.get(guildId);
		
		if(guildQueue == null) {
			return null;
		}
		
		return guildQueue.size();
	}
	
	/**
	 * Peek the queue at a specific index<br>
	 * Does not remove the item from the queue
	 * @param guildId The ID of the Guild
	 * @param index The index of the item to peek
	 * @return Returns the requested QueueItem. Null if the guild has no queue
	 */
	public QueueItem peekQueueAtIndex(long guildId, int index) {
		Queue<QueueItem> guildQueue = this.queues.get(guildId);
		
		//Null-check
		if(guildQueue == null) {
			return null;
		}
		
		//Convert to a LinkedList, because you cannot remove items from a queue at a given index
		List<QueueItem> guildQueueAsList = new LinkedList<QueueItem>(guildQueue);
		
		return guildQueueAsList.get(index);
	}
	
	/**
	 * Set the queue for a Guild
	 * @param guildId The ID of the Guild
	 * @param queue The Queue to set, must be a LinkedList
	 */
	public void setQueue(long guildId, Queue<QueueItem> queue) {
		if(!(queue instanceof LinkedList<?>)) {
			throw new RuntimeException("Provided queue is not a LinkedList.");
		}
		
		this.queues.put(guildId, queue);
	}
}