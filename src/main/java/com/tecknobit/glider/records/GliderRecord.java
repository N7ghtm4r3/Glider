package com.tecknobit.glider.records;

import com.tecknobit.apimanager.formatters.JsonHelper;
import com.tecknobit.glider.records.Session.SessionKeys;
import org.json.JSONObject;

/**
 * The {@link GliderRecord} is class useful to create a {@code Glider}'s record and give the base structure
 * for all the records
 *
 * @author Tecknobit - N7ghtm4r3
 **/
public class GliderRecord {

    /**
     * {@code token} session value
     **/
    protected final Session session;

    /**
     * {@code hRecord} instance to manage JSON details
     **/
    protected final JsonHelper hRecord;

    /**
     * Constructor to init {@link GliderRecord} object
     *
     * @param session: session value
     **/
    public GliderRecord(Session session) {
        this.session = session;
        hRecord = null;
    }

    /**
     * Constructor to init {@link GliderRecord} object
     *
     * @param jRecord: record details as {@link JSONObject}
     **/
    public GliderRecord(JSONObject jRecord) {
        hRecord = new JsonHelper(jRecord);
        session = new Session(hRecord.getJSONObject(SessionKeys.token.name(), new JSONObject()));
    }

    /**
     * Method to get {@link #session} instance <br>
     * Any params required
     *
     * @return {@link #session} instance as {@link Session}
     **/
    public Session getSession() {
        return session;
    }

    /**
     * Returns a string representation of the object <br>
     * Any params required
     *
     * @return a string representation of the object as {@link JSONObject}
     */
    public JSONObject toJSON() {
        return new JSONObject(this);
    }

    /**
     * Returns a string representation of the object <br>
     * Any params required
     *
     * @return a string representation of the object as {@link String}
     */
    @Override
    public String toString() {
        return new JSONObject(this).toString();
    }

}
