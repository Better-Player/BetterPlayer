package net.betterplayer.betterplayer.audio.queue;

import com.google.common.escape.Escaper;
import dev.array21.jdbd.datatypes.PreparedStatement;
import dev.array21.jdbd.datatypes.SqlRow;
import net.betterplayer.betterplayer.utils.Pair;
import net.betterplayer.betterplayer.utils.Utils;
import org.apache.commons.lang3.StringEscapeUtils;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public record QueueItem(String trackName, String trackIdentifier, String artistName) {

    public static Pair<Integer, QueueItem> fromRow(SqlRow row) {
        byte[] bTrackName = Utils.toPrimitive(row.getBytes("trackName"));
        byte[] bArtistName = Utils.toPrimitive(row.getBytes("artistName"));
        byte[] bTrackIdentifier = Utils.toPrimitive(row.getBytes("trackIdentifier"));

        return new Pair<>((int)(long) row.getLong("queuePosition"), new QueueItem(new String(bTrackName, StandardCharsets.UTF_8), new String(bTrackIdentifier, StandardCharsets.UTF_8), new String(bArtistName, StandardCharsets.UTF_8)));
    }

    public PreparedStatement toStmt(long savedQueueId, int position) {
        PreparedStatement pr = new PreparedStatement("INSERT INTO savedQueues (savedQueueId, queuePosition, trackName, trackIdentifier, artistName) VALUES (?, ?, ?, ?, ?)");
        pr.bind(0, savedQueueId);
        pr.bind(1, position);
        pr.bind(2, this.trackName.replaceAll(Pattern.quote("'"), ""));
        //pr.bind(2, this.trackName.replaceAll(Pattern.quote("'"), ""));
        pr.bind(3, this.trackIdentifier);
        pr.bind(4, this.artistName.replaceAll(Pattern.quote("'"), ""));

        return pr;
    }
}
