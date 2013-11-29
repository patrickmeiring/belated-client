package qut.belated;

import java.util.ArrayList;

import qut.belated.depositing.DepositedFile;
import qut.belated.depositing.DepositedFileAdapter;
import android.location.Location;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

public class FilesActivity extends Activity {

	ListView nearbyFiles;
	TextView title;
	ArrayList<DepositedFile> files;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_files);
		
		nearbyFiles = (ListView)findViewById(R.id.fileList);
		nearbyFiles.setAdapter(null);
		nearbyFiles.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				DepositedFile file = files.get(position);
				openFile(file);
			}
		});
		
		title = (TextView)findViewById(R.id.txtTitle);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	    refreshList();
	}
    
    private void refreshList()
    {
    	Location currentLocation = BackgroundLocationService.getLastReportedLocation();
    	if (currentLocation != null)
    		new GetNearbyFilesTask(this).execute(currentLocation);
    	else
    		onNoLocationAvailable();
    }
    
    private void onNoLocationAvailable()
    {
    	title.setText(R.string.title_no_location_available);
    	title.setVisibility(View.VISIBLE);
    	nearbyFiles.setVisibility(View.GONE);
    }
    
    private void onNoFilesAvailable()
    {
    	title.setText(R.string.title_no_files_available);
    	title.setVisibility(View.VISIBLE);
    	nearbyFiles.setVisibility(View.GONE);
    }
    
    private void onFilesAvailable()
    {
    	title.setVisibility(View.GONE);
    	nearbyFiles.setVisibility(View.VISIBLE);
    }
    
	private void openFile(DepositedFile file)
   	{
		try
		{
			Intent intent = new Intent(android.content.Intent.ACTION_VIEW, file.getUri());
			startActivity(intent);
		}
		catch (RuntimeException e)
		{
			Log.e("FileActivity", "Unable to open file, " + e);
		}
   	}
	
	public void onNearbyFilesAvailable(ArrayList<DepositedFile> files)
	{
		this.files = files;
		DepositedFileAdapter adapter = new DepositedFileAdapter(LayoutInflater.from(this), files);
		nearbyFiles.setAdapter(adapter);
		
		if (files.size() == 0)
			onNoFilesAvailable();
		else
			onFilesAvailable();
	}
}
