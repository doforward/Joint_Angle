package com.example.joint_angle;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import org.achartengine.GraphicalView;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.R.string;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
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
	//TextView sendmessage;
	Button start,clear;
	//mag_protocol mag_protocol;
	  BluetoothAdapter adapter;
	  private final static String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";   //SPP����UUID��
	  BluetoothDevice _device = null;     //�����豸
	  BluetoothSocket _socket = null;      //����ͨ��socket


	  private OutputStream outstream;
	  myHandler mmhandler;
	  String rec="";
	  public String mydatabuffer="";
	  //lee ��stringΪchar
	  //˼��������Ҫ��ȫ�ֱ�����
	  //���߳��ѱ�
	  //public String databufferfrombluetooth;
	  private static String readMessage="";
		public double []data=new double[1000];//����1000���������ʾ
		public String mysubstring,sendstring;
		public static final String PACKHEAD = "6162";
//		private Timer timer = new Timer();
	    private GraphicalView chart;
	    private TextView textview;
//	    private TimerTask task;
	    private int addY = -1;
		private long addX;
		int ydata;
		double capvalue;
		double CapValueConvert;
		
		/**��������*/
	    private static final int SERIES_NR=1;
	    private static final String TAG = "message";
	    private TimeSeries series1;
	    private XYMultipleSeriesDataset dataset1;
	    private Handler handler;
	    private Random random=new Random();
	    
	    /**ʱ������*/
	    Date[] xcache = new Date[20];
	    //lee add date to compare time interval between two pakage 
	    Date prev_date,next_date;
	    String prev_data,final_data;
		/**����*/
	    int[] ycache = new int[20];
	    Canvas canvas;
	    
	    

	  public boolean flag_rec_thread=false;
	  public static byte[] result = new byte[1024];

	  private SurfaceHolder holder;
		private Paint paint;
		final int HEIGHT = 1000;
		final int WIDTH = 1500;
		final int X_OFFSET = 15;
		private int cx = X_OFFSET;
		//ʵ�ʵ�Y���λ��
		int centerY = HEIGHT / 2;
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

//			��ȡϵͳĬ������
			adapter = BluetoothAdapter.getDefaultAdapter();
			//lee delete
			//getThread.start();//�߳�����  
			//mmhandler = new myHandler();
			
			
			final SurfaceView surface = (SurfaceView)
					findViewById(R.id.show);
			
			
				// ��ʼ��SurfaceHolder����
				holder = surface.getHolder();
				paint = new Paint();
				paint.setColor(Color.GREEN);
				paint.setStrokeWidth(3);
				
				
				
		}
		
		
		public void onstart(View v)
		{
//			ֱ�Ӵ�����
			adapter.enable();
//			��ʼ����
		//	adapter.startDiscovery();
			//_device = adapter.getRemoteDevice("81:F2:6D:98:0E:A0");
			_device = adapter.getRemoteDevice("98:D3:31:40:0E:88");
	        // �÷���ŵõ�socket
	        try{
	        	_socket = _device.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
	        }catch(IOException e){
//	        	Toast.makeText(this, "����ʧ�ܣ�", Toast.LENGTH_SHORT).show();
	        }
	        try
			{	
				_socket.connect();
				Log.i("SOCKET", "����"+_device.getName()+"�ɹ���");
				result_text.setText("���ӳɹ�");
				//Toast.makeText(this, "����"+_device.getName()+"�ɹ���", Toast.LENGTH_SHORT).show();
			} catch (IOException e)
			{
				
	    		try
				{
//	    		Toast.makeText(this, "����ʧ�ܣ�", Toast.LENGTH_SHORT).show();
				_socket.close();
				result_text.setText("��������");
				_socket = null;
				} catch (IOException e1)
				{
				}            		
				return;
			}

	        //�򿪽����߳�
	        try{
	    		//blueStream = _socket.getInputStream();   //�õ���������������
	        	//getThread.start();//�߳�����
	        	new RecvData_Thread(_socket).start();
	        	prev_date = new Date();
				mmhandler = new myHandler();
	    		}catch(Exception e){
	    			return;
	    		}
	        
	        flag_rec_thread=true;
			if (flag_rec_thread)
			{
				result_text.setText("���ڽ���");
			}else {
				result_text.setText("ֹͣ����");
			}
			 
			
			
			drawBack(holder);
			paint.setColor(Color.RED);
			paint.setStrokeWidth((float) 2.0);				//�߿�

			cx = X_OFFSET;
			/*			
			if(task != null)
			{
				task.cancel();
			}

			task = new TimerTask()
			{
				public void run()
				{
					int cy = centerY-ydata;
					canvas = holder.lockCanvas(new Rect(cx , cy - 2  , cx + 2, cy + 2));
					//lee write(Integer.toString((cx-15))+"   "+Integer.toString(ydata/25));
					Calendar CD = Calendar.getInstance();
					int SS = CD.get(Calendar.SECOND);
					int MI = CD.get(Calendar.MILLISECOND);
					write(SS+"s"+MI+"ms: "+databufferfrombluetooth+"+");
					
//					canvas.drawLine(cx-1, cy-2, cx, cy, paint);

//					canvas.drawLine(0, 0, 25, 2, paint);
//					canvas.drawLine(25, 2, 50, 4, paint);
//					canvas.drawLine(50, 4, 75, 6, paint);
//					canvas.drawLine(75, 6, 100, 8, paint);
//					canvas.drawLine(100, 8, 125, 10, paint);
//					canvas.drawLine(150, 12, 150, 12, paint);
					canvas.drawPoint(cx , cy , paint);
					cx ++;
					if (cx > WIDTH)
					{
						task.cancel();
						task = null;

					}
					holder.unlockCanvasAndPost(canvas);
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
				// ���ļ�������
				FileInputStream fis = openFileInput(FILE_NAME);
				byte[] buff = new byte[1024];
				int hasRead = 0;
				StringBuilder sb = new StringBuilder("");
				// ��ȡ�ļ�����
				while ((hasRead = fis.read(buff)) > 0)
				{
					sb.append(new String(buff, 0, hasRead));
				}
				// �ر��ļ�������
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
				// ��׷��ģʽ���ļ������
				FileOutputStream fos = openFileOutput(FILE_NAME, MODE_APPEND);
				//lee debug
				//Log.d("file", "above can write data in file");
				// ��FileOutputStream��װ��PrintStream
				PrintStream ps = new PrintStream(fos);
				// ����ļ�����
				ps.println(content);
				// �ر��ļ������
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
		/*
		public static String ByteToString(byte[] bytes)
		{
			String returnString="";
	
			for (int i = 0; i < bytes.length; i++)
			{
				returnString+= Integer.toHexString(bytes[i]&0xff)+" ";
			}
			
				return returnString ;
		}
		*/
		public class myHandler extends Handler{

			  
			@Override
			public void handleMessage(Message msg) {

				
				String databufferfrombluetooth = "";
					if(msg.what == 0x123){
						
						databufferfrombluetooth = (String) msg.obj;
						//ydata = (Integer.parseInt(text))*25;
						//lee result_text.setText(text);
						//Log.d("@@@bluetooth@@@", databufferfrombluetooth);
						//while(databufferfrombluetooth[0] == "61" && databufferfrombluetooth[1] == "62") {
						//lee capvalue = Integer.parseInt(databufferfrombluetooth[0]) + (Integer.parseInt(databufferfrombluetooth[1]))/10 + Integer.parseInt(databufferfrombluetooth[2])/100;
						/*lee
						 * if(capvalue > 1.5 && capvalue < 2.5){
						CapValueConvert = -50.7464*capvalue*capvalue*capvalue+337.154*capvalue*capvalue-777.8272*capvalue+686.8225;
							//result_text.setText("����ֵΪ��" + databufferfrombluetooth[0] + "," + databufferfrombluetooth[1] + databufferfrombluetooth[2] + "   " + "�Ƕ�ֵΪ��" + String.valueOf(CapValueConvert));
							result_text.setText(databufferfrombluetooth,0,3);
							}
						else
						{
							result_text.setText("����ֵΪ��" + databufferfrombluetooth[0] + "," + databufferfrombluetooth[1] + databufferfrombluetooth[2] + "        �ؽڳ���Ԥ����Χ");
						}*/
						//
						result_text.setText(databufferfrombluetooth);
						//ver1
						new MyTask(databufferfrombluetooth).run();
						//ver2
						//timer.schedule(new MyTask(databufferfrombluetooth) , 0 , 20);
						//}
//						String substring = text.substring(0,3);
//						while(substring == PACKHEAD){
//							long xxx = Integer.parseInt(text.substring(4,5)) + Integer.parseInt(text.substring(6,7)) + Integer.parseInt(text.substring(8,9));
//						}
//						updateChart(ydata);
				
					}
				super.handleMessage(msg);
			}
		}
	//��ȡ�������ݵ����߳�
		
		/*
		private byte[] getHexBytes(String message) {
	        int len = message.length() / 2;
	        char[] chars = message.toCharArray();
	        String[] hexStr = new String[len];
	        byte[] bytes = new byte[len];
	        for (int i = 0, j = 0; j < len; i += 2, j++) {
	            hexStr[j] = "" + chars[i] + chars[i + 1];
	            bytes[j] = (byte) Integer.parseInt(hexStr[j], 16);
	        }
	        return bytes;
	    }
	    
//		���ݽ���
		private String dealwithstring(String databuffer){
			int length = databuffer.length();
			int index = databuffer.indexOf(PACKHEAD);
			if((length-index)>=4){
				mysubstring = databuffer.substring(index+5, index+7);
				mydatabuffer = "";
			}else{
				
			}
			return mysubstring;
		 }
		 */


		private void drawBack(SurfaceHolder holder)
		{
			Canvas canvas = holder.lockCanvas();
			// ���ư�ɫ����
			canvas.drawColor(Color.WHITE);
			Paint p = new Paint();
			p.setColor(Color.BLACK);
			p.setStrokeWidth(2);
			// ����������
			canvas.drawLine(X_OFFSET , centerY , WIDTH , centerY , p);
			canvas.drawLine(X_OFFSET , 40 , X_OFFSET , HEIGHT , p);
			
			//��������ɺ��
			paint.setAntiAlias(true);	//���û���Ϊ�޾��
			paint.setColor(Color.BLACK);	//���û�����ɫ 
			paint.setStrokeWidth((float) 1.0);				//�߿�
			paint.setStyle(Style.STROKE);
			paint.setTextSize(28);

			Path gridpath = new Path();						//Path����
			//���ݶ���
			//������������
			for(int i=0; i<30; i++)
			{
				gridpath.moveTo(X_OFFSET+i*50, 0);	
				gridpath.lineTo(X_OFFSET+i*50, HEIGHT);
				canvas.drawText(""+i*2, X_OFFSET+i*50, centerY, paint);
			}
			    canvas.drawPath(gridpath, paint);					//������������  
			    
			  //���ƺ���ɺ��
			    paint.setAntiAlias(true);	//���û���Ϊ�޾��
				paint.setColor(Color.BLACK);	//���û�����ɫ 
				paint.setStrokeWidth((float) 1.0);				//�߿�
				paint.setStyle(Style.STROKE);
				paint.setTextSize(28);
				Path gridpath2 = new Path();						//Path����
				//���ݶ���
				//������������
				for(int i=0; i<30; i++)
				{
					gridpath2.moveTo(0-HEIGHT,i*100 );	
					gridpath2.lineTo(WIDTH, i*100);
					canvas.drawText(""+(centerY-i*100)/25, 0, i*100, paint);
				}
				    canvas.drawPath(gridpath2, paint);					//������������  
				    
				    
			holder.unlockCanvasAndPost(canvas);
			holder.lockCanvas(new Rect(0 , 0 , 0 , 0));
			holder.unlockCanvasAndPost(canvas);
		}
		
		public class RecvData_Thread extends Thread {
			  private final BluetoothSocket _socket;      //����ͨ��socket
			  private InputStream blueStream;    //������������������������
			  
			  public RecvData_Thread(BluetoothSocket socket) {

				_socket = socket;
				InputStream tmpIn = null; //���涨���Ϊfinal����ʹ��temp��ʱ����
				
				try {
						tmpIn = socket.getInputStream(); //ʹ��getInputStream��Ϊһ��������
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
							int readCount = 0; // �Ѿ��ɹ���ȡ���ֽڵĸ���
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
							//��ȡ����һ�����������ݺ���post��ȥ
							Message message = mmhandler.obtainMessage();  
				            message.what = 0x123;  
				            message.obj = final_data;  
			
				            mmhandler.sendMessage(message);  
						}catch(IOException e) {  
			                break;  
						}				
			            
					//����ʱ��Ҫ����
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

		/*
		task = new TimerTask()
		{
			
		};
		*/
		
		public class MyTask extends TimerTask{
			String file_str = "";
			public MyTask(String str) {
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
}


