package com.nathanrjones.castkeys;

import com.google.cast.MessageStream;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by nathan on 11/3/13.
 */
public class CastKeysMessageStream extends MessageStream {

    private static final String CASTKEYS_NAMESPACE = "com.nathanrjones.castkeys";
    private static final String TEST_NAMESPACE = "Test";

    // Commands
    private static final String CHANGE_THEME = "theme";
    private static final String TYPE_KEYS = "string";

    protected CastKeysMessageStream() {
        super(TEST_NAMESPACE);
    }

    @Override
    public void onMessageReceived(JSONObject jsonObject) {

    }

    public final void sendCastKeysMessage(String string){
        JSONObject payload = new JSONObject();

        try {
            payload.put(TYPE_KEYS, string);
            sendMessage(payload);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public final void changeTheme(String themeName){
        JSONObject payload = new JSONObject();

        try {
            payload.put(CHANGE_THEME, themeName);
            sendMessage(payload);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
