package com.tecknobit.glider.helpers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GliderLauncher {

    /**
     * {@code SERVER_STATUS_KEY} server status key
     */
    public static final String SERVER_STATUS_KEY = "server_status";

    /**
     * {@code COLOR_PRIMARY_HEX} the primary color value as hex {@link String}
     */
    public static final String COLOR_PRIMARY_HEX = "#1E1E8D";

    /**
     * {@code COLOR_RED_HEX} the red color value as hex {@link String}
     */
    public static final String COLOR_RED_HEX = "#A81515";

    public void startService(String ur) throws SQLException, IOException {
        DatabaseManager databaseManager = new DatabaseManager(ur);
        databaseManager.insertNewPassword("gaga", "ggaga", new ArrayList<>(List.of("gaga", "gag")), "ga");
    }

}
