package qut.belated.helpers;

import java.io.IOException;
import java.io.InputStream;

import qut.belated.PhysicalTravelMode;

public class HttpHelper {
	static String LOCATION_URL = "http://%s/REST/location";
	static String MEETINGS_URL = "http://%s/REST/meetings/%s";
	static String TRAVEL_PLAN_URL = "http://%s/REST/meeting/%s/travel";
	static String NEARBY_FILES_URL = "http://%s/REST/nearbyfiles";
	static String GOOGLE_DIRECTIONS_URL = "http://maps.google.com/maps?saddr=%s&daddr=%s&dirflag=%s";
	static String GOOGLE_DIRECTIONS_CAR_FLAG = "d";
	static String GOOGLE_DIRECTIONS_TRANSIT_FLAG = "r";
	static String GOOGLE_DIRECTIONS_WALK_FLAG = "w";
	
	public static final int LONG_TIMEOUT = 15000;
	public static final int SHORT_TIMEOUT = 7000;
	
	
	public static String getLocationUrl(String ip)
	{
		return String.format(LOCATION_URL, ip);
	}
	
	public static String getMeetingsUrl(String ip, String email)
	{
		return String.format(MEETINGS_URL, ip, email);
	}
	
	public static String getTravelPlanUrl(String ip, String meeting)
	{
		return String.format(TRAVEL_PLAN_URL, ip, meeting);
	}
	
	public static String getNearbyFilesUrl(String ip)
	{
		return String.format(NEARBY_FILES_URL, ip);
	}
	
	private static String getGoogleDirectionsFlag(PhysicalTravelMode travelMode)
	{
		switch (travelMode)
		{
			case Car:
				return GOOGLE_DIRECTIONS_CAR_FLAG;
			case Transit:
				return GOOGLE_DIRECTIONS_TRANSIT_FLAG;
			case Walk:
				return GOOGLE_DIRECTIONS_WALK_FLAG;
			default:
				return GOOGLE_DIRECTIONS_CAR_FLAG;
		}
	}
	
	public static String getGoogleDirectionsUrl(String origin, String destination, PhysicalTravelMode mode)
	{
		String directionFlag = getGoogleDirectionsFlag(mode);
		return String.format(TRAVEL_PLAN_URL, origin, destination, directionFlag);
	}
	
	
	public static String readString(InputStream stream)
			throws IOException
	{
		StringBuffer result = new StringBuffer();
		byte[] byteBuffer = new byte[4096];
		
		int bytesRead = 0;
		do 
		{
			bytesRead = stream.read(byteBuffer);
			if (bytesRead <= 0)
				break;
			
			String read = new String(byteBuffer, 0, bytesRead);
			result.append(read);
		}
		while (bytesRead > 0);
		
		return result.toString();
	}
}
