package net.betterplayer.betterplayer.audio.queue;

import dev.array21.jdbd.datatypes.PreparedStatement;
import dev.array21.jdbd.datatypes.SqlRow;
import net.betterplayer.betterplayer.utils.Pair;

public record QueueItem(String trackName, String trackIdentifier, String artistName) {

    public static Pair<Integer, QueueItem> fromRow(SqlRow row) {
        return new Pair<>((int)(long) row.getLong("queuePosition"), new QueueItem(row.getString("trackName"), row.getString("trackIdentifier"), row.getString("artistName")));
    }

    public PreparedStatement toStmt(long savedQueueId, int position) {
        PreparedStatement pr = new PreparedStatement("INSERT INTO savedQueues (savedQueueId, queuePosition, trackName, trackIdentifier, artistName) VALUES ('?', '?', '?', '?', '?')");
        pr.bind(0, savedQueueId);
        pr.bind(1, position);
        pr.bind(2, this.trackName);
        pr.bind(3, this.trackIdentifier);
        pr.bind(4, this.artistName);

        return pr;
    }
}
