package com.example.joint_angle;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

//import org.achartengine.GraphicalView;
//import org.achartengine.model.TimeSeries;
//import org.achartengine.model.XYMultipleSeriesDataset;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
//import android.R.integer;
//import android.R.string;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Paint.Style;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
	TextView result_text;
	Button start,clear;
	BluetoothAdapter adapter;
	private final static String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";   //SPP服务UUID号
	BluetoothDevice _device = null;     //蓝牙设备
	BluetoothSocket _socket = null;      //蓝牙通信socket

	private OutputStream outstream;
	myHandler mmhandler;
	String rec="";
	public String mydatabuffer="";
	//lee 改string为char
	//思考后尽量不要用全局变量吧
	//多线程难保
	//public String databufferfrombluetooth;
	private static String readMessage="";
	//public double []data=new double[1000];//定义1000个点进行显示
	public String mysubstring,sendstring;
	public static final String PACKHEAD = "6162";
	//private Timer timer = new Timer();
	//private GraphicalView chart;
	private TextView textview;
	//private TimerTask task;
	//private int addY = -1;
	//private long addX;
	//int ydata;
	double capvalue;
	double CapValueConvert;
	int SV_w,SV_h;
	float pre_value;
	float nex_value;
	//float capvalue;
	//float CapValueConvert;
		
	/**曲线数量*/
	//private static final int SERIES_NR=1;
	//private static final String TAG = "message";
	//private TimeSeries series1;
	//private XYMultipleSeriesDataset dataset1;
	private Handler handler;
	private Random random=new Random();
	   
	/**时间数据*/
	//Date[] xcache = new Date[20];
	//lee add date to compare time interval between two pakage 
	Date prev_date,next_date;
	String prev_data,final_data;
	/**数据*/
	//int[] ycache = new int[20];
	//public Canvas canvas;
	    
	    

	public boolean flag_rec_thread=false;
	public static byte[] result = new byte[1024];
	private SurfaceHolder holder;
	//private Paint paint;
	//final int HEIGHT = 1000;
	//final int WIDTH = 1500;
	//final int X_OFFSET = 15;
	//private int cx = X_OFFSET;
	//实际的Y轴的位置
	//int centerY = HEIGHT / 2;
	Timer timer = new Timer();
	//TimerTask task = null;
	final String FILE_NAME = "data12";
	  
		@Override
		protected void onCreate(Bundle savedInstanceState)
		{
			// TODO Auto-generated method stub
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_main);
			result_text=(TextView)findViewById(R.id.result_text);
			start = (Button) findViewById(R.id.start);
			clear = (Button) findViewById(R.id.clear);
			//获取系统默认蓝牙
			adapter = BluetoothAdapter.getDefaultAdapter();
			//lee delete
			//getThread.start();//线程启动  
			//mmhandler = new myHandler();
			
			//get surface view in here
			final SurfaceView surface = (SurfaceView)findViewById(R.id.show);
			SV_w = surface.getWidth();
			SV_h = surface.getHeight();
			//初始化SurfaceHolder对象
			holder = surface.getHolder();
			holder.addCallback(new DoThings( ));
			
		}
		
		
		public void onstart(View v)
		{
//			直接打开蓝牙
			adapter.enable();
//			开始搜索
		//	adapter.startDiscovery();
			//_device = adapter.getRemoteDevice("81:F2:6D:98:0E:A0");
			_device = adapter.getRemoteDevice("98:D3:31:40:0E:88");
	        // 用服务号得到socket
	        try{
	        	_socket = _device.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
	        }catch(IOException e){
//	        	Toast.makeText(this, "连接失败！", Toast.LENGTH_SHORT).show();
	        }
	        try
			{	
				_socket.connect();
				Log.i("SOCKET", "连接"+_device.getName()+"成功！");
				result_text.setText("连接成功");
				//Toast.makeText(this, "连接"+_device.getName()+"成功！", Toast.LENGTH_SHORT).show();
			} catch (IOException e)
			{
				
	    		try
				{
//	    		Toast.makeText(this, "连接失败！", Toast.LENGTH_SHORT).show();
				_socket.close();
				result_text.setText("重新连接");
				_socket = null;
				} catch (IOException e1)
				{
				}            		
				return;
			}

	        //打开接收线程
	        try{
	    		//blueStream = _socket.getInputStream();   //得到蓝牙数据输入流
	        	//getThread.start();//线程启动
	        	new RecvData_Thread(_socket).start();
	        	prev_date = new Date();
				mmhandler = new myHandler();
	    		}catch(Exception e){
	    			return;
	    		}
	        
	        flag_rec_thread=true;
			if (flag_rec_thread)
			{
				result_text.setText("正在接受");
			}else {
				result_text.setText("停止接受");
			}
			 
			
			
			//drawBack(holder);
			//paint.setColor(Color.RED);
			//paint.setStrokeWidth((float) 2.0);				//线宽

			//cx = X_OFFSET;
			/*			
			if(task != null)
			{
				task.cancel();
			}

			task = new TimerTask()
			{
				public void run()
				{

				}
			};
			*/
			//timer.schedule(task , 0 , 200);
		}
		
		
		public void onread(View v){
			result_text.setText(read());
		}
		
		
		private String read()
		{
			try
			{
				// 打开文件输入流
				FileInputStream fis = openFileInput(FILE_NAME);
				byte[] buff = new byte[1024];
				int hasRead = 0;
				StringBuilder sb = new StringBuilder("");
				// 读取文件内容
				while ((hasRead = fis.read(buff)) > 0)
				{
					sb.append(new String(buff, 0, hasRead));
				}
				// 关闭文件输入流
				fis.close();
				return sb.toString();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			return null;
		}

		private void write(String content)
		{
			try
			{
				// 以追加模式打开文件输出流
				FileOutputStream fos = openFileOutput(FILE_NAME, MODE_APPEND);
				//lee debug
				//Log.d("file", "above can write data in file");
				// 将FileOutputStream包装成PrintStream
				PrintStream ps = new PrintStream(fos);
				// 输出文件内容
				ps.println(content);
				// 关闭文件输出流
				ps.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		
		public void onclear(View v){
			result_text.setText("");

		}

		public class myHandler extends Handler{
		  
			@Override
			public void handleMessage(Message msg) {

				
				String databufferfrombluetooth = "";
					if(msg.what == 0x123)
					{
						
						databufferfrombluetooth = (String) msg.obj;
						try {
							double temp = (new Double(databufferfrombluetooth)).doubleValue(); 
							capvalue = temp/100.0;
							//Log.d("@@@double success@@@", Integer.toString(i));
						} catch (Exception e) {
							// TODO: handle exception
							Log.d("@@@error@@@", "why error");
							e.printStackTrace();
						}
						//if(capvalue > 1.5 && capvalue < 2.5){
						CapValueConvert = -50.7464*capvalue*capvalue*capvalue+337.154*capvalue*capvalue-777.8272*capvalue+686.8225;
						//CapValueConvert = java.lang.Math.abs(CapValueConvert);
						//错误的修改 保证角度在0-180以内
						CapValueConvert = ((CapValueConvert%180)+180)%180;
						BigDecimal bg = new BigDecimal(CapValueConvert);  
			            double cap_acc2 = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();  
						String Show_Str = "电容值：" + String.valueOf(capvalue)+ " | 角度值：" + String.valueOf(cap_acc2);
						result_text.setText(Show_Str);
						new File_Task(Show_Str).run();
						nex_value = new Float(cap_acc2).floatValue();
						//new Chart_Task(cap_acc2).run();
						
						Log.d("$$$", "surface implements");
					}
				super.handleMessage(msg);
			}
		}

		/*
		private void drawBack(SurfaceHolder holder)
		{
			Canvas canvas = holder.lockCanvas();
			// 绘制白色背景
			canvas.drawColor(Color.WHITE);
			Paint paint = new Paint();
			paint.setColor(Color.BLACK);
			paint.setStrokeWidth(2);
			// 绘制坐标轴
			canvas.drawLine(X_OFFSET , centerY , WIDTH , centerY , paint);
			canvas.drawLine(X_OFFSET , 40 , X_OFFSET , HEIGHT , paint);
			
			//绘制纵向珊格
			paint.setAntiAlias(true);	//设置画笔为无锯齿
			//paint.setColor(Color.BLACK);	//设置画笔颜色 
			//paint.setStrokeWidth((float) 1.0);				//线宽
			paint.setStyle(Style.STROKE);
			paint.setTextSize(28);

			Path gridpath = new Path();						//Path对象
			//数据读入
			//绘制纵向坐标
			for(int i=0; i<30; i++)
			{
				gridpath.moveTo(X_OFFSET+i*50, 0);	
				gridpath.lineTo(X_OFFSET+i*50, HEIGHT);
				canvas.drawText(""+i*2, X_OFFSET+i*50, centerY, paint);
			}
			    canvas.drawPath(gridpath, paint);					//绘制任意多边形  
			    
			  //绘制横向珊格
			    paint.setAntiAlias(true);	//设置画笔为无锯齿
				paint.setColor(Color.BLACK);	//设置画笔颜色 
				paint.setStrokeWidth((float) 1.0);				//线宽
				paint.setStyle(Style.STROKE);
				paint.setTextSize(28);
				Path gridpath2 = new Path();						//Path对象
				//数据读入
				//绘制纵向坐标
				for(int i=0; i<30; i++)
				{
					gridpath2.moveTo(0-HEIGHT,i*100 );	
					gridpath2.lineTo(WIDTH, i*100);
					canvas.drawText(""+(centerY-i*100)/25, 0, i*100, paint);
				}
				    canvas.drawPath(gridpath2, paint);					//绘制任意多边形  
				    
				    
			//holder.unlockCanvasAndPost(canvas);
			//holder.lockCanvas(new Rect(0 , 0 , 0 , 0));
			holder.unlockCanvasAndPost(canvas);
		}
		*/
		
		
		public class RecvData_Thread extends Thread {
			  private final BluetoothSocket _socket;      //蓝牙通信socket
			  private InputStream blueStream;    //输入流，用来接收蓝牙数据
			  
			  public RecvData_Thread(BluetoothSocket socket) {

				_socket = socket;
				InputStream tmpIn = null; //上面定义的为final这是使用temp临时对象
				
				try {
						tmpIn = socket.getInputStream(); //使用getInputStream作为一个流处理
					} 
				catch (IOException e) { }
				blueStream = tmpIn;
				}
			@Override
			public void run() {
				
				while (!currentThread().isInterrupted()) {
					try{
							byte[] buffer =new byte[1024];
							int count = 0;
							while (count == 0) {
							   count = blueStream.available();
							}
							int readCount = 0; // 已经成功读取的字节的个数
						    while (readCount < count) {
							 	readCount += blueStream.read(buffer, readCount, count - readCount);
     						}
						    next_date = new Date();
						    long prev = prev_date.getTime();
						    long next = next_date.getTime();
						    
						    /*
							Calendar CD = Calendar.getInstance();
							int SS = CD.get(Calendar.SECOND);
							int MI = CD.get(Calendar.MILLISECOND);
							*/
						    
							String data_blth = "";
							for(int i = 0 ; i < count; i++)
							{ 		  
								data_blth += Integer.toHexString(buffer[i]&0xff);
							}
							//data_blth += "in"+SS+"s"+MI+"ms";
							
							
							if(count<10){
								if (next-prev<100) {
									final_data = prev_data+data_blth;
								}
								else{
									prev_data = data_blth;
								}
								prev_date = next_date;		
							}
							//获取到是一个完整的数据后在post回去
							Message message = mmhandler.obtainMessage();  
				            message.what = 0x123;  
				            message.obj = final_data;  
				            
			
				            mmhandler.sendMessage(message);  
						}catch(IOException e) {  
			                break;  
						}				
			            
					//先暂时不要休眠
					/*
					try
						{
							sleep(100);
						} catch (InterruptedException a)
						{
							// TODO Auto-generated catch block
							a.printStackTrace();
						}
						*/				
					}
					
				}
				
				
				 
			public void cancel() {
				try {
						_socket.close();
					} catch (IOException e) { }
				
			}
		}
		
		public class File_Task extends TimerTask{
			String file_str = "";
			public File_Task(String str) {
				file_str = str;
			}
			public void run()
			{
				//
				//Calendar CD = Calendar.getInstance();
				//int SS = CD.get(Calendar.SECOND);
				//int MI = CD.get(Calendar.MILLISECOND);
				write(file_str);
			}
		}
		
	    private class DoThings implements SurfaceHolder.Callback{
;
	    	float pre_x = 0.0f,pre_y = 0.0f;
	    	float step;
	    	float coo_x = 0,coo_y=0;
	    	float tmp;

	        @Override  
	        public void surfaceChanged(SurfaceHolder holder, int format, int width,  
	                int height) {  
	            //在surface的大小发生改变时激发  
	        	SV_w = width;
	        	step = (float) (SV_w/50.0);
	        	SV_h = height;
	            System.out.println("surfaceChanged");  
	        }  
	  
	        @Override  
	        public void surfaceCreated(SurfaceHolder holder1){  
	            new Thread(){  
	                public void run() {  
	                    while(true){  
	                        //1.这里就是核心了， 得到画布 ，然后在你的画布上画出要显示的内容  
	                        Canvas c = holder.lockCanvas();  
	                        //2.开画  
	                        Paint  p =new Paint();
	                        p.setStrokeWidth(5);
	                        p.setColor(Color.WHITE);  
	                        //c.drawLine(0, SV_h/2, SV_w/2, SV_h/2,p);
	                        /*
	                        Rect aa  =  new Rect( (int)(Math.random() * 100) ,  
	                                (int)(Math.random() * 100)   
	                                ,(int)(Math.random() * 500)   
	                                ,(int)(Math.random() * 500) );  
	                        c.drawRect(aa, p);
	                        */
	                        
	                        //pre_y = SV_h-(pre_value/180)*SV_h;
	                        coo_y = SV_h-(nex_value/180)*SV_h;
	                        
	                        
	                        c.drawLine(coo_x,tmp,coo_x,pre_y,p);
	                        c.drawLine(coo_x,pre_y,coo_x+step,coo_y,p);
	                        
	                        //c.drawLine(coo_x,step,coo_x,pre_y,p);
	                        //c.drawLine(coo_x,step,coo_x+step,step,p);
	                        
	                        tmp = pre_y;
	                        pre_value = nex_value;
	                        pre_y = coo_y;
	                        coo_x += step;
	                        coo_x = coo_x%SV_w;
	                        //if (coo_x>SV_w) {
								//coo_x = 0;
								//c.drawColor(Color.BLACK);
								p.setXfermode(new PorterDuffXfermode(Mode.CLEAR));  
							//}
	                        
	                  
	                        
	                        //3. 解锁画布   更新提交屏幕显示内容  
	                        holder.unlockCanvasAndPost(c);  
	                        
	                        try {  
	                            Thread.sleep(100);  
	                              
	                        } catch (Exception e) {  
	                        }
	                          
	                    }  
	                };  
	            }.start();  
	              
	        }

			@Override
			public void surfaceDestroyed(SurfaceHolder arg0) {
				// TODO Auto-generated method stub
				
			}  
	  
	    }
}


