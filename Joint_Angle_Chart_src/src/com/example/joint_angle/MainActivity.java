package com.example.joint_angle;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.Legend.LegendForm;
import com.github.mikephil.charting.components.YAxis.AxisDependency;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.Highlight;
public class MainActivity extends Activity implements OnChartValueSelectedListener  {
	TextView result_text;
	Button start,clear;
	BluetoothAdapter adapter;
	private final static String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";   //SPP服务UUID号
	BluetoothDevice _device = null;     //蓝牙设备
	BluetoothSocket _socket = null;      //蓝牙通信socket

	myHandler mmhandler;
	String rec="";
	public String mydatabuffer="";
	public String mysubstring,sendstring;
	public static final String PACKHEAD = "6162";
	double capvalue;
	double CapValueConvert;
	int SV_w,SV_h;
	float pre_value;
	float nex_value;

	private Random random=new Random();
	   
	/**时间数据*/
	Date prev_date,next_date;
	String prev_data,final_data;
 
	public boolean flag_rec_thread=false;
	public static byte[] result = new byte[1024];
	private SurfaceHolder holder;
	Timer timer = new Timer();
	final String FILE_NAME = "data12";
	 
	private LineChart mChart;
	
	//init chart xy axis
    ArrayList<String> xVals = new ArrayList<String>();
    ArrayList<Entry> yVals = new ArrayList<Entry>();
    int x_count = 30;
	
		@Override
		protected void onCreate(Bundle savedInstanceState)
		{
			// TODO Auto-generated method stub
			super.onCreate(savedInstanceState);
			setContentView(R.layout.lee_chart);
			
			result_text=(TextView)findViewById(R.id.result_text);
			start = (Button) findViewById(R.id.start);
			clear = (Button) findViewById(R.id.clear);
			
			
			//获取系统默认蓝牙
			adapter = BluetoothAdapter.getDefaultAdapter();	
			//init chart
			
			mChart = (LineChart) findViewById(R.id.chart1);
			
	        mChart.setOnChartValueSelectedListener(this);
	        
	        
	        // no description text
	        mChart.setDescription("");
	        mChart.setNoDataTextDescription("You need to provide data for the chart.");

	        // enable value highlighting
	        mChart.setHighlightEnabled(true);

	        // enable touch gestures
	        mChart.setTouchEnabled(true);

	        // enable scaling and dragging
	        mChart.setDragEnabled(true);
	        mChart.setScaleEnabled(true);
	        mChart.setDrawGridBackground(false);

	        // if disabled, scaling can be done on x- and y-axis separately
	        mChart.setPinchZoom(true);

	        // set an alternative background color
	        mChart.setBackgroundColor(Color.LTGRAY);

	        
	        LineData data = new LineData();
	        data.setValueTextColor(Color.WHITE);

	        // add empty data
	        mChart.setData(data);

	        //Typeface tf = Typeface.createFromAsset(getAssets(), "OpenSans-Regular.ttf");

	        // get the legend (only possible after setting data)
	        Legend l = mChart.getLegend();

	        // modify the legend ...
	        // l.setPosition(LegendPosition.LEFT_OF_CHART);
	        l.setForm(LegendForm.LINE);
	        //l.setTypeface(tf);
	        l.setTextColor(Color.WHITE);

	        XAxis xl = mChart.getXAxis();
	        //xl.setTypeface(tf);
	        xl.setTextColor(Color.WHITE);
	        xl.setDrawGridLines(false);
	        xl.setAvoidFirstLastClipping(true);
	        xl.setSpaceBetweenLabels(5);
	        xl.setEnabled(false);

	        YAxis leftAxis = mChart.getAxisLeft();
	        //leftAxis.setTypeface(tf);
	        leftAxis.setTextColor(Color.WHITE);
	        leftAxis.setAxisMaxValue(190f);
	        leftAxis.setAxisMinValue(0f);
	        leftAxis.setStartAtZero(false);
	        leftAxis.setDrawGridLines(true);

	        YAxis rightAxis = mChart.getAxisRight();
	        rightAxis.setEnabled(false);
			
   			for (int i = 0; i < x_count; i++) {
   	            xVals.add((i) + "");
   	            //yVals.add(new Entry((float)i, i));
   	        }
		}
		
		
		public void onstart(View v)
		{
//			直接打开蓝牙
			adapter.enable();
//			开始搜索
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
						Update_Chart(nex_value);
						//new Chart_Task(cap_acc2).run();
						
						//Log.d("$$$", "surface implements");
					}
				super.handleMessage(msg);
			}
		}

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

		public void Update_Chart(Float arg_value)
		{
	        
			final Float value = arg_value;   
			runOnUiThread(new Runnable() {

                   @Override
                   public void run() {
                	   LineData data = mChart.getData();

           	        if (data != null) {

           	            LineDataSet set = data.getDataSetByIndex(0);
           	            // set.addEntry(...); // can be called as well

           	            if (set == null) {
           	                set = createSet();
           	                data.addDataSet(set);
           	            }
           	            
           	            //只有y轴增加-2015.07.02
               			try {
           					if (yVals.size()<x_count) {	
           						//xVals.add((yVals.size()) + "");
           						yVals.add(new Entry(value, yVals.size()));
           					}else{
           						//xVals.clear();
           						yVals.clear();
           						//yVals.remove(0);
           						//yVals.add(new Entry(temp, set.getEntryCount()));
           					}
           				} catch (Exception e) {
           					// TODO: handle exception
           					Log.d("@@@error@@@", "why error");
           					e.printStackTrace();
           				}
           						

           			// create a dataset and give it a type
           	        LineDataSet set1 = new LineDataSet(yVals, "DataSet 1");
           	        // set1.setFillAlpha(110);
           	        // set1.setFillColor(Color.RED);

           	        // set the line to be drawn like this "- - - - - -"
           	        set1.enableDashedLine(10f, 5f, 0f);
           	        set1.setColor(Color.BLACK);
           	        set1.setCircleColor(Color.BLACK);
           	        set1.setLineWidth(1f);
           	        set1.setCircleSize(3f);
           	        set1.setDrawCircleHole(false);
           	        set1.setValueTextSize(9f);
           	        set1.setFillAlpha(65);
           	        set1.setFillColor(Color.BLACK);
//           	        set1.setDrawFilled(true);
           	        // set1.setShader(new LinearGradient(0, 0, 0, mChart.getHeight(),
           	        // Color.BLACK, Color.WHITE, Shader.TileMode.MIRROR));

           	        ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
           	        dataSets.add(set1); // add the datasets

           	        // create a data object with the datasets
           	        LineData data1 = new LineData(xVals, dataSets);

           	        // set data
           	        mChart.setData(data1);
           	        
           	     // redraw
           	       mChart.invalidate();
           	        }
                   }
               });
			
			
			

		}
		
	    private LineDataSet createSet() {

	        LineDataSet set = new LineDataSet(null, "Dynamic Data");
	        set.setAxisDependency(AxisDependency.LEFT);
	        set.setColor(ColorTemplate.getHoloBlue());
	        set.setCircleColor(Color.WHITE);
	        set.setLineWidth(2f);
	        set.setCircleSize(4f);
	        set.setFillAlpha(65);
	        set.setFillColor(ColorTemplate.getHoloBlue());
	        set.setHighLightColor(Color.rgb(244, 117, 117));
	        set.setValueTextColor(Color.WHITE);
	        set.setValueTextSize(9f);
	        set.setDrawValues(false);
	        return set;
	    }
		
		@Override
		public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
			// TODO Auto-generated method stub
			Log.i("Entry selected", e.toString());		
		}

		@Override
		public void onNothingSelected() {
			// TODO Auto-generated method stub
			Log.i("Nothing selected", "Nothing selected.");
		}
}


