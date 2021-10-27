package net.betterplayer.betterplayer.audio.queue;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;

public class QueueManager {

	/**
	 * K = Guild Id<br>
	 * V = The GuildQueue associated with the guild
	 */
	private final HashMap<Long, GuildQueue> queues = new HashMap<>();
	
	/**
	 * K = Guild ID<br>
	 * V = QueueItem which is currently playing
	 */
	private final HashMap<Long, QueueItem> nowPlaying = new HashMap<>();
	
	/**
	 * Set the QueueItem which is currently playing
	 * @param guildId The ID of the Guild
	 * @param queueItem The QueueItem to set. Null is allowed.
	 */
	public void setNowPlaying(long guildId, QueueItem queueItem) {
		this.nowPlaying.put(guildId, queueItem);
	}

	/**
	 * Check if a Guild has Loop Mode enabled
	 * @param guildId The ID of the Guild to check
	 * @return Returns true if the Guild has Loop Mode enabled, false if it does not. Returns false if the Guild has no Queue
	 */
	public boolean isLoopMode(long guildId) {
		if(this.queues.containsKey(guildId)) {
			return this.queues.get(guildId).isLoopMode();
		}

		return false;
	}

	/**
	 * Set the Loop Mode for a Guild
	 * @param guildId The ID of the Guild
	 * @param loopMode The Loop Mode. True indicates looping is enabled
	 */
	public void setLoopMode(long guildId, boolean loopMode) {
		GuildQueue gc = this.queues.get(guildId);
		if(gc == null) {
			return;
		}

		gc.setLoopMode(loopMode);
		this.queues.put(guildId, gc);
	}
	
	/**
	 * Get the QueueItem which is currently playing
	 * @param guildId The ID of the Guild
	 * @return Returns the requested QueueItem. Empty if nothing is currently playing
	 */
	public Optional<QueueItem> getNowPlaying(long guildId) {
		return Optional.ofNullable(this.nowPlaying.get(guildId));
	}
	
	/**
	 * Poll the next item in queue. Removes the item from the queue!
	 * @param guildId The ID of the Guild
	 * @return Returns the requested QueueItem. Empty if the guild has no queue, or if the queue is empty
	 */
	public Optional<QueueItem> pollQueue(long guildId) {
		Queue<QueueItem> guildQueue = this.queues.get(guildId).getQueue();
		
		if(guildQueue == null) {
			return Optional.empty();
		}
		
		return Optional.ofNullable(guildQueue.poll());
	}
	
	/**
	 * Peek the next item in queue. Does not remove the item from the queue
	 * @param guildId The ID of the Guild
	 * @return Returns the requested QueueItem. Empty if the guild has no queue, or if the queue is empty.
	 */
	public Optional<QueueItem> peekQueue(long guildId) {
		Queue<QueueItem> guildQueue = this.queues.get(guildId).getQueue();
		
		if(guildQueue == null) {
			return Optional.empty();
		}

		return Optional.ofNullable(guildQueue.peek());
	}
	
	/**
	 * Get the full queue for a Guild. Does not clear the queue
	 * @param guildId The ID of the guild
	 * @return Returns the queue as a LinkedList. Empty if the Guild has no queue.
	 */
	public Optional<LinkedList<QueueItem>> getFullQueue(long guildId) {
		Queue<QueueItem> guildQueue = this.queues.get(guildId).getQueue();
		
		if(guildQueue == null) {
			return Optional.empty();
		}
		
		return Optional.ofNullable(new LinkedList<>(guildQueue));
	}
	
	/**
	 * Remove an item from the queue for a guild<br>
	 * If the guild has no queue, this method will just return
	 * @param guildId The ID of the Guild
	 * @param index The index in the queue, 0-based.
	 */
	public void removeFromQueue(long guildId, int index) {
		GuildQueue gc = this.queues.get(guildId);
		//Null-check
		if(gc == null) {
			return;
		}

		Queue<QueueItem> guildQueue = gc.getQueue();
		
		//Convert to a LinkedList, because you cannot remove items from a queue at a given index
		List<QueueItem> guildQueueAsList = new LinkedList<>(guildQueue);
		
		//Now remove the element
		guildQueueAsList.remove(index);
		
		//Next convert the LinkedList back into a LinkedList queue
		Queue<QueueItem> newGuildQueue = new LinkedList<>(guildQueueAsList);

		gc.setQueue(newGuildQueue);

		//Set the new queue in place of the old one
		this.queues.put(guildId, gc);
	}
	
	/**
	 * Add an item to the end of a Guild's queue<br>
	 * Returns without error if the Guild has no queue
	 * @param guildId The ID of the Guild
	 * @param queueItem The Item to add to the queue
	 */
	public void addToQueue(long guildId, QueueItem queueItem) {
		GuildQueue gc = this.queues.get(guildId);
		//Null-check
		if(gc == null) {
			return;
		}

		Queue<QueueItem> guildQueue = gc.getQueue();
		guildQueue.add(queueItem);

		gc.setQueue(guildQueue);
		this.queues.put(guildId, gc);
	}
	
	/**
	 * Add a QueueItem to the front of the queue
	 * @param guildId The ID of the Guild
	 * @param queueItem The QueueItem to add
	 */
	public void addToQueueFront(long guildId, QueueItem queueItem) {
		GuildQueue gc = this.queues.get(guildId);
		//Null-check
		if(gc == null) {
			return;
		}

		Queue<QueueItem> guildQueue = gc.getQueue();

		List<QueueItem> queueItemList = new LinkedList<>();
		queueItemList.add(queueItem);
		
		List<QueueItem> existingGuildQueueAsList = new LinkedList<>(guildQueue);
		queueItemList.addAll(existingGuildQueueAsList);
		
		//Set the new queue back in the hashmap
		Queue<QueueItem> newGuildQueue = new LinkedList<>(queueItemList);
		gc.setQueue(newGuildQueue);

		this.queues.put(guildId, gc);
	}
	
	/**
	 * Create a new Queue for a Guild
	 * @param guildId The ID of the guild
	 */
	public void createQueue(long guildId) {
		this.queues.put(guildId, new GuildQueue());
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
		GuildQueue gc = this.queues.get(guildId);
		//Null-check
		if(gc == null) {
			return;
		}

		Queue<QueueItem> guildQueue = gc.getQueue();
		this.nowPlaying.remove(guildId);
		
		guildQueue.clear();
		gc.setQueue(guildQueue);
		this.queues.put(guildId, gc);
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
	 * @return Returns the size of the queue. Empty if the guild has no queue
	 */
	public Optional<Integer> getQueueSize(long guildId) {
		GuildQueue gc = this.queues.get(guildId);
		//Null-check
		if(gc == null) {
			return Optional.empty();
		}
		Queue<QueueItem> guildQueue = gc.getQueue();
		return Optional.ofNullable(guildQueue.size());
	}
	
	/**
	 * Peek the queue at a specific index<br>
	 * Does not remove the item from the queue
	 * @param guildId The ID of the Guild
	 * @param index The index of the item to peek
	 * @return Returns the requested QueueItem. Null if the guild has no queue
	 */
	public Optional<QueueItem> peekQueueAtIndex(long guildId, int index) {
		GuildQueue gc = this.queues.get(guildId);
		//Null-check
		if(gc == null) {
			return Optional.empty();
		}
		Queue<QueueItem> guildQueue = gc.getQueue();
		
		//Null-check
		if(guildQueue == null) {
			return Optional.empty();
		}
		
		//Convert to a LinkedList, because you cannot remove items from a queue at a given index
		List<QueueItem> guildQueueAsList = new LinkedList<>(guildQueue);

		return Optional.ofNullable(guildQueueAsList.get(index));
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

		if(this.queues.containsKey(guildId)) {
			GuildQueue gc = this.queues.get(guildId);
			gc.setQueue(queue);
			this.queues.put(guildId, gc);
		} else {
			GuildQueue gc = new GuildQueue();
			gc.setQueue(queue);
			this.queues.put(guildId, gc);
		}
	}
}