package com.google.GFS;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;


public class RegisterInfo {
	private String filename="data/data/com.android.GFS/INFO.dat";
	private String II=null;
	private String Email=null;
	private String Passwd=null;
    private JSONParser jsonParser;
    private final String URL_VALIDATE="http://qiyemanage.web-149.com/validate.php";
    
    public RegisterInfo()
    {
    	jsonParser = new JSONParser();  
    }
	
	public void saveInfo(String II, String Mail, String Passwd)
	{
	    try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
			bw.write(II);
			bw.newLine();
			bw.write(Mail);
			bw.newLine();
			bw.write(Passwd);
			bw.flush();
			bw.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}  		
	}
	
	public void getContentFromFile()
	{
		try {
			BufferedReader br = new BufferedReader(new FileReader(filename));
			II=br.readLine();
			Email=br.readLine();
			Passwd=br.readLine();			
            br.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
	}
	
	public String getII()
	{
		return II;
	}
	public String getEmail()
	{
		return Email;
	}
	public String getPWD()
	{
		return Passwd;
	}
	
	
	public int checkExpireDaysLeft(String ii)
	{
    	SimpleDateFormat  sdf = new SimpleDateFormat("yyyy-MM-dd",Locale.CHINA); 
    	String today=sdf.format(new Date());
    	String expireday="2013-03-28";		 	
    	List<NameValuePair> data =new ArrayList<NameValuePair>();
    	data.add(new BasicNameValuePair("II",ii));
    	try{
    		expireday=jsonParser.makeHttpRequest(URL_VALIDATE,"POST", data).getString("date");
    		
    	}catch(Exception e){
            e.printStackTrace();       
        }
    	return countDays(today,expireday);
	}
	
	private static int countDays(String begin,String end){
		  int days = 0;
		  DateFormat df = new SimpleDateFormat("yyyy-MM-dd",Locale.CHINA);
		  Calendar c_b = Calendar.getInstance();
		  Calendar c_e = Calendar.getInstance();
		  try{
		   c_b.setTime(df.parse(begin));
		   c_e.setTime(df.parse(end));
		   while(c_b.before(c_e)){
		    days++;
		    c_b.add(Calendar.DAY_OF_YEAR, 1);
		   }
		  }catch(ParseException pe){
		   System.out.println("formt should be yyyy-MM-dd, eg: 2010-4-4.");
		  }
		  return days; 
		} 

}
