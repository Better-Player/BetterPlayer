package net.betterplayer.betterplayer;

import dev.array21.jdbd.DatabaseDriver;
import dev.array21.jdbd.datatypes.PreparedStatement;
import dev.array21.jdbd.datatypes.SqlRow;
import dev.array21.jdbd.exceptions.SqlException;
import net.betterplayer.betterplayer.utils.Utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Instant;

@SuppressWarnings("unused")
public class Migration {

    private final DatabaseDriver db;

    public Migration(DatabaseDriver db) {
        this.db = db;
    }

    public void migrate() throws SqlException {
        db.execute(new PreparedStatement("CREATE TABLE IF NOT EXISTS __betterplayer_migrations (`migration` INT NOT NULL PRIMARY KEY AUTO_INCREMENT, `ts` BIGINT NOT NULL)"));
        SqlRow[] rows = db.query(new PreparedStatement("SELECT migration FROM __betterplayer_migrations WHERE migration = (SELECT MAX(migration) FROM __betterplayer_migrations)"));
        if(rows.length > 1) {
            BetterPlayer.logError("Got more than one SqlRow when querying __betterplayer_migrations. This should not happen");
            System.exit(1);
            return;
        }

        int nextMigration = 1;
        if(rows.length != 0) {
            nextMigration = (int)(long) rows[0].getLong("migration") + 1;
        }

        applyMethod(nextMigration);
    }

    private void applyMethod(int method) throws SqlException {
        BetterPlayer.logDebug("Applying migration " + method);
        Method migrationMethod;
        try {
            migrationMethod = Migration.class.getDeclaredMethod(String.format("V%d", method));
        } catch(NoSuchMethodException e) {
            // Do nothing. This means we dont have anything more to migrate
            BetterPlayer.logInfo("Migrations up to date.");
            return;
        }

        BetterPlayer.logDebug("Invoking method for migration " + method);

        try {
            migrationMethod.invoke(this);
        } catch(InvocationTargetException e) {
            if(e.getCause().getClass().equals(SqlException.class)) {
                BetterPlayer.logError(String.format("Failed to apply migration V%d: %s", method, e.getMessage()));
                BetterPlayer.logDebug(Utils.getStackTrace(e));
            }

            BetterPlayer.logError("Failed to apply migrations.");
            BetterPlayer.logDebug(Utils.getStackTrace(e));

            System.exit(1);
            return;
        } catch(IllegalAccessException e) {
            // Unreachable
            e.printStackTrace();
        }

        BetterPlayer.logDebug("Updating __betterplayer_migrations for Migration " + method);
        long epochSeconds = Instant.now().toEpochMilli() / 1000L;
        PreparedStatement pr = new PreparedStatement("INSERT INTO __betterplayer_migrations (migration, ts) VALUES ('?', '?')");
        pr.bind(0, method);
        pr.bind(1, epochSeconds);
        db.execute(pr);

        applyMethod(++method);
    }

    private void V1() throws SqlException {
        db.execute(new PreparedStatement("CREATE TABLE guildConfigs (`guildid` BIGINT NOT NULL PRIMARY KEY, `commandprefix` VARCHAR(1) NOT NULL)"));
    }

    private void V2() throws SqlException {
        db.execute(new PreparedStatement("CREATE TABLE savedQueues (`savedQueueId` BIGINT NOT NULL PRIMARY KEY, `queuePosition` INT NOT NULL, `trackName` TEXT NOT NULL, `trackIdentifier` TEXT NOT NULL, `artistName` TEXT NOT NULL)"));
    }
}
