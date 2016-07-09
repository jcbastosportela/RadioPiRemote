package apps.porty.radiopiremote;

/**
 * Created by porty on 7/6/16.
 */
/* for TCP comm */
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import android.os.AsyncTask;
import android.util.Log;

public class TCPConn extends AsyncTask<Void, Void, Void>
{
    public interface CallBack
    {
        void TCPResult( boolean bRes );
    }

    private CallBack connectionEvent;
    String dstAddress;
    int dstPort;
    String response = "";
    Socket socket;
    OutputStream os;

    TCPConn(String addr, int port, CallBack connCB)
    {
        connectionEvent = connCB;
        dstAddress = addr;
        dstPort = port;
        Thread ct = new Thread(new ConnectThread());
        ct.start();
    }

    public void reconnect()
    {
        Thread ct = new Thread(new ConnectThread());
        ct.start();
    }

    public void reconnect(String addr, int port)
    {
        dstAddress = addr;
        dstPort = port;
        Thread ct = new Thread(new ConnectThread());
        ct.start();
    }
    class ConnectThread implements Runnable
    {
        public void run()
        {
            Log.d("TCP", "Connection thread");
            try
            {
                socket = new Socket(dstAddress, dstPort);
                os = socket.getOutputStream();
                connectionEvent.TCPResult(true);
                return;
            }catch (IOException e)
            {
                e.printStackTrace();
                response = "IOException: " + e.toString();
            }
            connectionEvent.TCPResult(false);
        }
    }

    public void send( String msg)
    {
        try {
            byte[] snd = msg.getBytes();
            os.write(snd);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Void doInBackground(Void... arg0)
    {
        /*
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
            byte[] buffer = new byte[1024];

            int bytesRead;
            InputStream inputStream = socket.getInputStream();

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
                response += byteArrayOutputStream.toString("UTF-8");
            }

        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            response = "UnknownHostException: " + e.toString();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            response = "IOException: " + e.toString();
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        */
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
    }

}