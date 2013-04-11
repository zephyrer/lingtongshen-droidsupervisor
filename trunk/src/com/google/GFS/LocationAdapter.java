package com.google.GFS;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
public class LocationAdapter {
    //private static final String GOOGLEMAP="http://maps.google.cn/maps/geo?output=csv&key=abcdef&q=%s,%s";
    //private static final String ANTTNAMAP="http://www.anttna.com/gps2addr/gps2addr.php?lat=%s&lon=%s";
	private static final String ANTTNAMAP="http://maps.google.cn/maps/geo?output=csv&key=abcdef&q=%s,%s";
    private static final String GOOGLEJSON="http://www.google.com/loc/json";
    private LocationManager locationManager;
    private Context context;
    private Location location;
    private TelephonyManager tm;
    public  String latnlong="经纬度未知";
    
    public LocationAdapter(Context context){
    	this.context=context;
    }
    
    /**
     *
     * 使用GPS或NetWOrk查找经纬�?
     * 
     * */
    public void startLoactionService() {
        String serviceName = Context.LOCATION_SERVICE;
        locationManager = (LocationManager) context.getSystemService(serviceName);
        // 使用GPS查找位置
        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
       // updateWithNewLocation(location);
        //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0, 0, locationListener);
        // 如果GPS查询不到
        if (location == null) {
            // 使用NetWork查找位置
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
          //  updateWithNewLocation(location);
           // locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        }
        
    }
    
    public String getLocationPos(){
        // 如果GPS和NetWork都无法获得经纬度，则使用基站获取
        if (location == null) {
            //stopLocationService();
            //return startJiZhanSearch();
        	return "未知";
        }
        // �?��查找服务.
        return startGPSSearch();	
    	
    }

    public String getLocationLatLon(){
    	return latnlong;
    }
    /**
     * 
     * 关闭GPS和NETWORK
     * 
     * */
    public void stopLocationService() {
            locationManager.removeUpdates(locationListener);
    }

    private String startGPSSearch() {
    	if (location != null){
    		double lat = (int)(location.getLatitude()*1E6)/1E6;
    		double lng = (int)(location.getLongitude()*1E6)/1E6;
    		Double D1=new Double(lat);
    		String slat=D1.toString();
    		Double D2=new Double(lng);
    		String slng=D2.toString();
    		//stopLocationService();
    		latnlong="经度:"+slat+","+"纬度:"+slng;
    		//return GetLocationName(ANTTNAMAP,slat,slng);
    		return "未知";
    	}else{
    		//stopLocationService();
    		latnlong="GPS 经纬度未知";
    		return "GPS 信号不好，不能获取经纬度!";
    	}
    }

    
    private void updateWithNewLocation(Location location) {
    	if (location != null){
    		this.location=location; 		
    	}
    }
    /**
     * 
     * 查找事件
     * 
     * */
    private final LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
        	//updateWithNewLocation(location);
        }

        public void onProviderDisabled(String provider) {
        	//updateWithNewLocation(null);
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider,
                                    int status,
                                    Bundle extras) {
        }
    };

    /**
     * 
     * 使用基站查询经纬�?
     * 
     * */

    public String startJiZhanSearch() {
    	
        tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        GsmCellLocation gcl = (GsmCellLocation) tm.getCellLocation();
        System.out.println("gcl:"+gcl);
        //Toast.makeText(context, "startJiZhanSearch:", 500).show();
        if(gcl!=null)
        {
        	try {
            	int cid = gcl.getCid();
            	int lac = gcl.getLac();
            	int mcc = Integer.valueOf(tm.getNetworkOperator().substring(0,3));
            	int mnc = Integer.valueOf(tm.getNetworkOperator().substring(3,5));
        		// 组装JSON查询字符�?
        		JSONObject holder = new JSONObject();
        		holder.put("version", "1.1.0");
        		holder.put("host", "maps.google.com");
        		holder.put("home_mobile_country_code", 460);
        		holder.put("home_mobile_network_code", 0);
        		holder.put("radio_type", "gsm");
        		holder.put("carrier", "test");
        		holder.put("request_address", true);
        		holder.put("address_language", "zh_CN");
        		
        		JSONArray array = new JSONArray();
        		JSONObject data = new JSONObject();
        		data.put("cell_id", cid); // 25070
        		data.put("location_area_code", lac);// 4474
        		data.put("mobile_country_code", mcc);// 460
        		data.put("mobile_network_code", mnc);// 0
        		array.put(data);
        		holder.put("cell_towers", array);
        		
        		// 创建连接，发送请求并接受回应
        		DefaultHttpClient client = new DefaultHttpClient();
        		HttpPost post = new HttpPost(GOOGLEJSON);
        		StringEntity se = new StringEntity(holder.toString());
        		post.setEntity(se);
        		HttpResponse resp = client.execute(post);
        		HttpEntity entity = resp.getEntity();
        		BufferedReader br = new BufferedReader(
        				new InputStreamReader(entity.getContent()));
        		StringBuffer sb = new StringBuffer();
        		String result = br.readLine();
        		while (result != null) {
        			sb.append(result);
        			result = br.readLine();
        		}
        		
        		JSONObject jsonObject = new JSONObject(sb.toString());  
                JSONObject jsonObject1 = new JSONObject(jsonObject.getString("location"));  
                latnlong="lat:"+jsonObject1.getString("latitude")+","+"lon:"+jsonObject1.getString("longitude")+"(BTS)";
 
        		return GetLocationName(ANTTNAMAP,jsonObject1.getString("latitude"),jsonObject1.getString("longitude"));
        	} catch (Exception ex) {
        		latnlong="Can not get latitude and longitude";
        		return "Location is unknown";
        	}
        	
        }
        latnlong="基站经纬度未知";
        return "基站未知!";
       // return GetLocationName(ANTTNAMAP,"31.939963","118.860154");
    }
    
    public void test(){
    	
    	System.out.println("test");
    	GetLocationName(ANTTNAMAP,"31.939963","118.860154");
    }
    
    public  String GetLocationName(String vendor,String latitude, String longitude) {  
       String addr = "";  
  	  String url = String.format(vendor,latitude, longitude);  
  	  URL myURL = null;  
  	  URLConnection httpsConn = null;  
  	  try {  
  		  myURL = new URL(url);  
  	  } catch (MalformedURLException e) {  
  		  e.printStackTrace();  
  		  return null;  
  	  }  
  	  try {  
  		  httpsConn = (URLConnection) myURL.openConnection();
  		  if (httpsConn != null) {  
  		  		  InputStreamReader insr = new InputStreamReader(httpsConn.getInputStream(), "GBK");  
  		  		  BufferedReader br = new BufferedReader(insr);  
  		  		  String data = null;  
  		  		  if ((data = br.readLine()) != null) { 
  		  			  addr=data;
  		  		  }  
  		  		  insr.close(); 
  		  	return  addr;  
  	      }  
  	  } catch (IOException e) {  
  		  e.printStackTrace();  
  		  
  	 }  
  	  	return "Location is unknown";  
    }
    
    
    public static byte[] getUTF8BytesFromGBKString(String gbkStr) {  
        int n = gbkStr.length();  
        byte[] utfBytes = new byte[3 * n];  
        int k = 0;  
        for (int i = 0; i < n; i++) {  
            int m = gbkStr.charAt(i);  
            if (m < 128 && m >= 0) {  
                utfBytes[k++] = (byte) m;  
                continue;  
            }  
            utfBytes[k++] = (byte) (0xe0 | (m >> 12));  
            utfBytes[k++] = (byte) (0x80 | ((m >> 6) & 0x3f));  
            utfBytes[k++] = (byte) (0x80 | (m & 0x3f));  
        }  
        if (k < utfBytes.length) {  
            byte[] tmp = new byte[k];  
            System.arraycopy(utfBytes, 0, tmp, 0, k);  
            return tmp;  
        }  
        return utfBytes;  
    }  
}
