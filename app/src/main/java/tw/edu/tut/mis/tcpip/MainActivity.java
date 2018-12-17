package tw.edu.tut.mis.tcpip;

import android.os.Handler;
import android.os.Message;
import android.os.Trace;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class MainActivity extends AppCompatActivity {
    final String Tag = "wbj";
    Handler mhander = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //在主執行續裡直接建Handler會因為生命週期的關係，導致發生記憶體洩漏，另建一個靜態的方法
        mhander = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == 1){

                }
                if (msg.what==2){

                }
            }
        };

        UDPReceiveTread xxx = new UDPReceiveTread();
        xxx.run();
        UDPSendRunnable ooo = new UDPSendRunnable();
        ooo.Port = 9527;
        ooo.IP = "192.168.64.255";
        ooo.Message = "這是wbj的測試訊息";
        while (true) {
            new Thread(ooo).start();
        }
    }

    //Android要求不能在主執行緒上執行網路傳輸
    //建立一個用來接收UDP的執行續
    class UDPReceiveTread extends Thread{
        boolean keepReceive = true;
        @Override
        public void run() {
            super.run();
            //建立一個socket(插座) -> 準備一個封包的物件(packet) -> 接收封包存入packet
            try (DatagramSocket socket = new DatagramSocket(9527)) { //java獨有的try catch寫法
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                while (keepReceive) {
                    socket.receive(packet); //阻塞式等待: 程式等待到接收到封包為止
                    final String msg = new String(packet.getData(), 0, packet.getLength());
                    Log.d(Tag, msg);
                    //使用Handler的作法
                    mhander.sendEmptyMessage(1);
//建一個UIThread的作法
//                    MainActivity.this.runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            ((TextView)findViewById(R.id.textView)).setText(msg);
//                        }
//                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class  UDPSendThread extends Thread{
        @Override
        public void run() {
            super.run();
            try(DatagramSocket socket = new DatagramSocket()){
                for(int i=1;i<255;i++) {
                    String msg="192.168.64."+i;
                    byte[] buffer = ("我是柏君!!嘿嘿!我知道你的IP是: "+msg).getBytes();
                    InetAddress ip = InetAddress.getByName(msg);
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, ip, 9527);
                    socket.send(packet);
                    Log.d(Tag, msg);
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    //TODO:無法成功發送
    class  UDPSendRunnable implements Runnable{
        String Message;
        String IP;
        int Port;
        @Override
        public void run() {
            try(DatagramSocket socket = new DatagramSocket()){
                byte[] buffer = Message.getBytes();
                InetAddress ip = InetAddress.getByName(IP);
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, ip, Port);
                socket.send(packet);
                Log.d(Tag, Message);
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}
