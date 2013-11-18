package walk.around.route;

import org.json.JSONException;
import org.json.JSONObject;

public interface JSONObjectParser {
	public void parseJSON(JSONObject json) throws JSONException;
}
