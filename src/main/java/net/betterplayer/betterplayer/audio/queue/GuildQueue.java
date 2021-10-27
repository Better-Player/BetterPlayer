package net.betterplayer.betterplayer.audio.queue;

import java.util.LinkedList;
import java.util.Queue;

public class GuildQueue {

    private Queue<QueueItem> queue = new LinkedList<>();
    private boolean loopMode = false;

    public Queue<QueueItem> getQueue() {
        return this.queue;
    }

    public void setQueue(Queue<QueueItem> queue) {
        this.queue = queue;
    }

    public boolean isLoopMode() {
        return this.loopMode;
    }

    public void setLoopMode(boolean loopMode) {
        this.loopMode = loopMode;
    }

}
