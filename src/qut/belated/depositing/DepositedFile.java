package qut.belated.depositing;

import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;

public class DepositedFile {
	private DepositedFile()
	{
		name = "";
		description = "";
		uri = null;
	}
	
	String name;
	String description;
	Uri uri;
	
	public String getName()
	{
		return name;
	}
	
	public String getDescription()
	{
		return description;
	}
	
	public Uri getUri()
	{
		return uri;
	}
	
	public static DepositedFile FromJson(JSONObject json) throws JSONException
	{
		DepositedFile result = new DepositedFile();
		result.name = json.getString("name");
		result.description = json.getString("description");
		
		String uri = json.getString("path");
		result.uri = Uri.parse(uri);
		
		return result;
	}
}
