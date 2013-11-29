package qut.belated;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.json.JSONArray;
import org.json.JSONException;

import qut.belated.depositing.DepositedFile;
import qut.belated.helpers.HttpHelper;

import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;


public class GetNearbyFilesTask extends AsyncTask<Location, Void, ArrayList<DepositedFile>> {
	FilesActivity activity;
	BelatedPreferences preferences;
	String requestAddress;
	Location currentLocation;
	HttpEntity httpRequest;
	HttpClient client;
	HttpEntity httpResponse;
	ArrayList<DepositedFile> result;
	
	public GetNearbyFilesTask(FilesActivity activity)
	{
		this.activity = activity;
		preferences = new BelatedPreferences(activity);
	}
	
	@Override
	protected ArrayList<DepositedFile> doInBackground(Location... args) {
		currentLocation = args[0];
		getNearbyFiles();
		return result;
	}
	
	private void getNearbyFiles()
	{
		try
		{
			initialiseHttpClient();
			findRequestAddress();
			prepareRequest();
			performGetRequest();
			retreiveResult();
		} 
		catch (IllegalArgumentException e) {
			Log.e("GetNearbyFilesTask", "Service URL Invalid.");
		}
		catch (ClientProtocolException e) {
			Log.e("GetNearbyFilesTask", "Client protocol exception on HTTP Get.");
		} 
		catch (IOException e) {
			Log.e("GetNearbyFilesTask", "IO exception on HTTP Get. " + e.getMessage());
		}
	}
	
	private void initialiseHttpClient()
	{
		client = new DefaultHttpClient();
		HttpConnectionParams.setConnectionTimeout(client.getParams(), HttpHelper.SHORT_TIMEOUT); 
	}
	
	private void findRequestAddress()
	{
		requestAddress = HttpHelper.getNearbyFilesUrl(preferences.getServiceIP());
	}
	
	private void prepareRequest() throws UnsupportedEncodingException
	{
		ArrayList<BasicNameValuePair> requestParameters = new ArrayList<BasicNameValuePair>();
		
		requestParameters.add(new BasicNameValuePair("lat", Double.toString(currentLocation.getLatitude())));
		requestParameters.add(new BasicNameValuePair("lng", Double.toString(currentLocation.getLongitude())));
		
		httpRequest = new UrlEncodedFormEntity(requestParameters);
	}

	private void performGetRequest() throws ClientProtocolException, IOException
	{
		HttpPost request = new HttpPost(requestAddress);
		request.setEntity(httpRequest);
		
		HttpResponse response = client.execute(request);
		
		int statusCode = response.getStatusLine().getStatusCode();
		Log.v("GetNearbyFilesTask", "Nearby files retreived, status code: " + statusCode);
		
		httpResponse = response.getEntity();
	}
	
	
	private void retreiveResult() throws IOException
	{
		clearResult();
		processResponse();	
	}
	
	private void clearResult()
	{
		result = new ArrayList<DepositedFile>();
	}
	
	private void processResponse() throws IOException
	{
		if (httpResponse != null)
		{
			InputStream inStream = httpResponse.getContent();
			String jsonString = HttpHelper.readString(inStream);
			processFilesJson(jsonString);
		}
	}
	
	private void processFilesJson(String jsonString)
	{
		try
		{
			JSONArray fileArray = new JSONArray(jsonString);
			
			for (int i = 0; i < fileArray.length(); i++)
			{
				result.add(DepositedFile.FromJson(fileArray.getJSONObject(i)));
			}
		}
		catch (JSONException e)
		{
			Log.e("GetNearbyFilesTask", "Couldn't parse JSON: " + e.getMessage());
		}
	}
	
	@Override
	protected void onPostExecute(ArrayList<DepositedFile> results)
	{
		activity.onNearbyFilesAvailable(results);
	}
}
