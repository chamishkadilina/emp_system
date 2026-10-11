package empsystem.db;

import java.util.prefs.Preferences;

/** The database server address chosen on the server settings screen. */
public final class ServerSettings {

    public static final String DEFAULT_HOST = "localhost";

    // Same node and key as earlier versions, so an already saved host is kept.
    private static final Preferences PREFS = Preferences.userRoot().node("empsystem.Server");
    private static final String HOST_KEY = "savedText";

    private ServerSettings() {
    }

    /** Returns the saved host, or an empty string when none has been saved. */
    public static String getSavedHost() {
        return PREFS.get(HOST_KEY, "").trim();
    }

    public static void saveHost(String host) {
        PREFS.put(HOST_KEY, host.trim());
    }

    /** Returns the host to connect to: the saved one, or localhost. */
    public static String getHost() {
        String host = getSavedHost();
        return host.isEmpty() ? DEFAULT_HOST : host;
    }
}
