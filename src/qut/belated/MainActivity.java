package qut.belated;

import java.util.Date;

import qut.belated.helpers.HttpHelper;
import qut.belated.helpers.HumanReadableDate;
import qut.belated.helpers.LocationHelper;
import qut.belated.helpers.MapHelper;
import qut.belated.helpers.UIHelper;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    
    static MainActivity instance;
    
	TextView txtMeetingTitle;
	TextView txtMeetingDate;
	TextView txtMeetingLocation;
	TextView txtMeetingAttendees;
	TextView txtMeetingDescription;
	Button btnWalkingDirections;
	Button btnDrivingDirections;
	Button btnTransitDirections;
	Button btnOnlineConference;
	Button btnDeclineMeeting;
	TextView txtWalkingDetail;
	TextView txtDrivingDetail;
	TextView txtTransitDetail;
	TextView txtDirectionsCopyright;
	RelativeLayout layoutOnline;
	LinearLayout contentLayout;
	MapFragment googleMapFragment;
	GoogleMap googleMap;
	
    Meeting meeting;
    boolean requireDirections;
    
	DirectionsCollection storedDirections;

    boolean contentLoading;
    
	final int TRAVEL_OPTION_COUNT = 3;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
   
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        txtMeetingTitle = (TextView) findViewById(R.id.txtMeetingTitle);
        txtMeetingDate = (TextView) findViewById(R.id.txtMeetingDate);
        txtMeetingLocation = (TextView) findViewById(R.id.txtMeetingLocation);
        txtMeetingAttendees = (TextView) findViewById(R.id.txtMeetingAttendees);
        txtMeetingDescription = (TextView) findViewById(R.id.txtMeetingDescription);

        btnWalkingDirections = (Button) findViewById(R.id.btnWalking);
        btnWalkingDirections.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				startTravel(PhysicalTravelMode.Walk);
			}
        });
        btnWalkingDirections.setEnabled(false);
        
        btnDrivingDirections = (Button) findViewById(R.id.btnDriving);
        btnDrivingDirections.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				startTravel(PhysicalTravelMode.Car);
			}
        });
        btnDrivingDirections.setEnabled(false);
        
        btnTransitDirections = (Button) findViewById(R.id.btnTransit);
        btnTransitDirections.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				startTravel(PhysicalTravelMode.Transit);
			}
        });
        btnTransitDirections.setEnabled(false);
        
        btnOnlineConference = (Button) findViewById(R.id.btnOnline);
        btnOnlineConference.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				joinConference();
			}
		});
        
        btnDeclineMeeting = (Button) findViewById(R.id.btnDecline);
        btnDeclineMeeting.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				toggleDeclineMeeting();
			}
		});
        
        txtWalkingDetail = (TextView) findViewById(R.id.txtWalkingDetail);
        txtDrivingDetail = (TextView) findViewById(R.id.txtDrivingDetail);
        txtTransitDetail = (TextView) findViewById(R.id.txtTransitDetail);
        txtDirectionsCopyright = (TextView) findViewById(R.id.txtDirectionsCopyright);
        
        layoutOnline = (RelativeLayout) findViewById(R.id.layoutOnline);
        contentLayout = (LinearLayout) findViewById(R.id.contentLayout);
        
        googleMapFragment = ((MapFragment) getFragmentManager().findFragmentById(R.id.map));
		googleMap = googleMapFragment.getMap();
		
		storedDirections = new DirectionsCollection();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId())
    	{
    		case R.id.action_settings:
    			openSettingsActivity();
    			return true;
    		case R.id.action_refresh:
    			refreshContent();
    			return true;
    		case R.id.action_depositedfiles:
    			openDepositedFilesActivity();
    			return true;
    		default:
    			return super.onOptionsItemSelected(item);
    	}
    }
    
    private void openSettingsActivity()
    {
        Intent openSettings = new Intent(this, SettingsActivity.class); 
        startActivity(openSettings);
    }
    
    private void openDepositedFilesActivity()
    {
        Intent openDepositedFiles = new Intent(this, FilesActivity.class); 
        startActivity(openDepositedFiles);
    }
    
    private void startTravel(PhysicalTravelMode travelMode)
    {
    	if (meeting != null)
    	{
    		updateTravelPlan(travelMode);
    		startNavigationToMeeting(travelMode);
    	}
    }
    
    private void startNavigationToMeeting(PhysicalTravelMode mode)
   	{
    	Location currentLocation = getMyLocation();
		
    	String origin = Double.toString(currentLocation.getLatitude()) + "," + Double.toString(currentLocation.getLongitude());
		String destination = Double.toString(meeting.getLatitude()) + "," + Double.toString(meeting.getLongitude());
		String directionsUrl = HttpHelper.getGoogleDirectionsUrl(origin, destination, mode);
		
		Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(directionsUrl));
		
		startActivity(intent);
   	}
     
    private void updateTravelPlan(PhysicalTravelMode travelMode)
    {
    	Directions directionsToMeeting = storedDirections.retreive(travelMode);
    	if (directionsToMeeting != null && directionsToMeeting.wereFound())
    	{
	    	Date eta = directionsToMeeting.computeEstimatedTimeOfArrival(); 
	    	
	    	TravelPlan plan = new TravelPlan(meeting.getId(), travelMode, eta);
	    	notifyTravelPlan(plan);
    	}
    }
    
    private void notifyTravelPlan(TravelPlan plan)
    {
    	plan.send();
    	meeting.myAttendee.updateTravelPlan(plan);
		updateAlternateTravelOptions();
    }
    
    private void updateTravelPlan(AlternateTravelMode travelMode)
    {
    	TravelPlan plan = new TravelPlan(meeting.getId(), travelMode);
    	notifyTravelPlan(plan);
    }
      
    private void joinConference()
    {
    	if (meeting != null)
    	{
    		updateTravelPlan(AlternateTravelMode.Online);
    		startConference();
    	}
    }
    
    private void startConference()
    {
    	if (meeting.hasConferenceURL())
    	{
    		Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
    			meeting.getConferenceURL());
    	
    		startActivity(intent);
    	}
    }
    
    private void toggleDeclineMeeting()
    {
    	if (meeting == null)
    		return;

		if (!meeting.myAttendee.testTravelMode(AlternateTravelMode.Decline))
			updateTravelPlan(AlternateTravelMode.Decline);
		else
			updateTravelPlan(AlternateTravelMode.Unspecified);
    }
    
    protected void onResume()
    {
    	super.onResume();
    	instance = this;
    	loadContent();
    }
    
    protected void onPause()
    {
    	instance = null;
    	super.onPause();
    }
    
    private void loadContent()
    {
    	contentLoading = true;
    	new GetMeetingsTask().execute();
    }
    
    private void finishedLoadingContent()
    {
    	contentLoading = false;
    }
    
    private void refreshContent()
    {
    	if (!contentLoading)
    		loadContent();
    }
    
    
    private static boolean isForeground()
    {
    	return instance != null;
    }
    
    protected void onMeetingReceived(Meeting m, boolean serviceAvailable)
    {
    	if (!isForeground())
    		return;
    	meeting = m;
    	
		if (meeting != null)
		{
			showMeeting();
		}
		else
		{
			showNoMeetings(serviceAvailable);
			finishedLoadingContent();
		}
    }
    
    private void showNoMeetings(boolean serviceAvailable)
    {
		txtMeetingTitle.setText(serviceAvailable ? R.string.title_no_meetings : R.string.title_service_unavailable);
		txtMeetingDate.setText("");
		txtMeetingLocation.setText("");
		txtMeetingAttendees.setText("");
		txtMeetingDescription.setText("");
		hideContent();
    }
    
    private void hideContent()
    {
		contentLayout.setVisibility(View.GONE);
    }
    
    private void showMeeting()
    {
    	updateTitle();
    	updateStartAndEndDates();
    	updateLocation();
    	updateAttendees();
    	updateDescription();
		updateAlternateTravelOptions();
		updateMap();
		updateDirections();
		showContent();
    }
    
    private void updateTitle()
    {
    	txtMeetingTitle.setText(meeting.getSubject());
    }
    
    private void updateStartAndEndDates()
    {
    	Date start = meeting.getStart();
		Date end = meeting.getEnd();

		String meetingDuration = HumanReadableDate.ToString(start, end);
		txtMeetingDate.setText(meetingDuration);
    }

    private void updateLocation()
    {
		String meetingLocation = meeting.getLocation();
		txtMeetingLocation.setText(meetingLocation);
		if (meetingLocation.equals(""))
			txtMeetingLocation.setVisibility(View.GONE);
		else
			txtMeetingLocation.setVisibility(View.VISIBLE);
    }

    
    private void updateAttendees()
    {
    	String concatenatedAttendees = "";
    	for (Attendee attendee : meeting.getOtherAttendees())
    	{
    		if (!concatenatedAttendees.equals(""))
    			concatenatedAttendees += ", ";
    		concatenatedAttendees += attendee.name;
    	}
    	if (!concatenatedAttendees.equals(""))
    	{
	    	String attendees = "with " + concatenatedAttendees;
	    	attendees = UIHelper.ensureNoLineBreaks(attendees);
	    	txtMeetingAttendees.setText(attendees);
	    	txtMeetingAttendees.setVisibility(View.VISIBLE);
    	}
    	else
    		txtMeetingAttendees.setVisibility(View.GONE);
    }

    private void updateDescription()
    {
    	String meetingDescription = meeting.getDescription();
    	meetingDescription = UIHelper.ensureNoLineBreaks(meetingDescription);
		txtMeetingDescription.setText(meetingDescription);
		if (meetingDescription.equals(""))
			txtMeetingDescription.setVisibility(View.GONE);
		else
			txtMeetingDescription.setVisibility(View.VISIBLE);
    }

    private void updateAlternateTravelOptions()
    {
		if (meeting.hasConferenceURL())
			layoutOnline.setVisibility(View.VISIBLE);
		else
			layoutOnline.setVisibility(View.GONE);
		
		if (meeting.myAttendee.testTravelMode(AlternateTravelMode.Decline))
			btnDeclineMeeting.setText(R.string.action_undo_decline_meeting);
		else
			btnDeclineMeeting.setText(R.string.action_decline_meeting);
    }

    private void updateMap()
    {
    	if (googleMap == null)
    		return;
    	googleMap.clear();
    	
    	addDestinationMarkerToMap();
    	zoomToDestinationOnMap();
    }
    
    private void addDestinationMarkerToMap()
    {
		LatLng meetingPosition = new LatLng(meeting.getLatitude(), meeting.getLongitude());
		
		MarkerOptions marker = new MarkerOptions();
		marker.title(meeting.getLocation());
		marker.position(meetingPosition);
		marker.draggable(false);
		googleMap.addMarker(marker);
    }

    private void zoomToDestinationOnMap()
    {
		LatLng meetingPosition = new LatLng(meeting.getLatitude(), meeting.getLongitude());
		
		CameraUpdate update = CameraUpdateFactory.newLatLngZoom(meetingPosition, 15);
		googleMap.moveCamera(update);
    }
    
    private void showContent()
    {
		contentLayout.setVisibility(View.VISIBLE);
    }
    
    private void clearDirectionsDetail()
    {
    	for (PhysicalTravelMode travelMode : PhysicalTravelMode.values())
    		getDetailTextViewForTravelOption(travelMode).setText("");
    }
    
    private void updateDirections()
    {
    	clearDirections();
    	requireDirections();
    }
    
    private void clearDirections()
    {
    	clearDirectionsDetail();
    	clearDirectionsCopyright();
    	setNavigationGoButtonsEnabled(false);
    	storedDirections.clear();
    }
    
    private Location getMyLocation()
    {
    	Location lastLocation = BackgroundLocationService.getLastReportedLocation();
		if (lastLocation == null)
			lastLocation = LocationHelper.getLastLocation(this);
		return lastLocation;
    }
    
    private void requireDirections()
    {
    	requireDirections = true;
    	
		Location currentLocation = getMyLocation();
		if (currentLocation != null)
		{
			onLocationAvailable(currentLocation);
		}
    }

    public static void locationUpdated(Location newLocation)
    {
    	if (isForeground())
    	{
    		instance.onLocationAvailable(newLocation);
    	}
    }
    
    protected void onLocationAvailable(Location currentLocation)
    {
    	if (meeting != null && requireDirections)
    	{
        	requireDirections = false;
        	
    		setNavigationGoButtonsEnabled(true);
    		requestDirectionsFrom(currentLocation);
    	}
    }
    
    private void setNavigationGoButtonsEnabled(boolean enabled)
    {
    	for (PhysicalTravelMode travelMode : PhysicalTravelMode.values())
    		getButtonForTravelOption(travelMode).setEnabled(enabled);
    }
    
    
    private void requestDirectionsFrom(Location startLocation)
    {
		Log.d("MainActivity", "Invoking for driving directions.");
		
		for (PhysicalTravelMode travelMode : PhysicalTravelMode.values())
		{
			GetDirectionsTask getDirections = new GetDirectionsTask();
			DirectionsRequest request = new DirectionsRequest(startLocation, meeting, travelMode);
			getDirections.execute(request);
		}
    }
    
    protected void onDirectionsFound(Directions directions)
    {
    	if (!isForeground())
    		return;
    	
    	showDirections(directions);
    	storedDirections.store(directions);

    	if (storedDirections.count() == TRAVEL_OPTION_COUNT)
    	{
    		zoomMapToShowAllDirections();
    		finishedLoadingContent();
    	}
    }
    
    private void zoomMapToShowAllDirections()
    {
    	if (googleMap == null)
    		return;
    	
    	LatLngBounds mapBounds = MapHelper.computeBoundsToShowAll(storedDirections);
    	if (mapBounds != null)
    	{
        	CameraUpdate update = CameraUpdateFactory.newLatLngBounds(mapBounds, 30);
			googleMap.moveCamera(update);	
    	}
    }
    
    private void showDirections(Directions directions) {
    	showDirectionDetail(directions);
    	showDirectionLine(directions);
    	showDirectionCopyright();
    }
    
    private void clearDirectionsCopyright()
    {
    	txtDirectionsCopyright.setText("");
    }
    
	private void showDirectionLine(Directions directions) {
    	if (googleMap == null)
    		return;
		if (directions.wereFound())
    	{
    		int lineColor = getDirectionModeColor(directions.getMode());
    		googleMap.addPolyline(MapHelper.createPolylineFor(directions, lineColor));
    	}
	}
	
	private void showDirectionDetail(Directions directions) {
    	TextView detailText = getDetailTextViewForTravelOption(directions.getMode());

    	if (directions.wereFound())
    		detailText.setText(directions.getDurationText() + " / " + directions.getDistanceText());
    	else
    		detailText.setText(R.string.subtitle_directions_not_available);
	}
	
	private void showDirectionCopyright() {
		String allCopyright = "";
		for (Directions directions : storedDirections)
		{
			String copyright = directions.getCopyright();
			if (!allCopyright.contains(copyright))
			{
				if (!allCopyright.equals(""))
					allCopyright += ", ";
				allCopyright += copyright;
			}
		}
		txtDirectionsCopyright.setText(allCopyright);
	}
	
	private TextView getDetailTextViewForTravelOption(PhysicalTravelMode travelMode)
    {
		switch (travelMode)
		{
			case Car:
				return txtDrivingDetail;
			case Transit:
				return txtTransitDetail;
			case Walk:
				return txtWalkingDetail;
			default:
				return null;
		}
    }
	
	private Button getButtonForTravelOption(PhysicalTravelMode travelMode)
	{
		switch (travelMode)
		{
			case Car:
				return btnDrivingDirections;
			case Transit:
				return btnTransitDirections;
			case Walk:
				return btnWalkingDirections;
			default:
				return null;
		}
	}
	
    private int getDirectionModeColor(PhysicalTravelMode travelMode)
    {
		switch (travelMode)
		{
			case Car:	
				return Color.parseColor("#BB00BB");
			case Transit:
	    		return Color.RED;
			case Walk:
	    		return Color.BLUE;
			default:
	    		return Color.BLACK;
		}
    }
	
    public static void showToast(String text)
    {
    	if (isForeground())
    	{
    		Toast.makeText(instance, text, Toast.LENGTH_SHORT).show();
    	}
    }
}
