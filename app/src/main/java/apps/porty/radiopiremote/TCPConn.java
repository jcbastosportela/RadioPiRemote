package apps.porty.radiopiremote;

/**
 * Created by porty on 7/6/16.
 */
/* for TCP comm */
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import android.os.AsyncTask;
import android.util.Log;

public class TCPConn extends AsyncTask<Void, Void, Void>
{
    public interface TCPResCallBack
    {
        void TCPResult( boolean bRes );
    }

    public interface PlaylistAddCallBack
    {
        void addPlaylistEntry( String entry );
    }

    private TCPResCallBack connectionEvent;
    private PlaylistAddCallBack playlistEntryEvent;
    String dstAddress;
    int dstPort;
    String response = "";
    Socket socket;
    OutputStream os;

    TCPConn(String addr, int port, TCPResCallBack connCB)
    {
        connectionEvent = connCB;
        dstAddress = addr;
        dstPort = port;
        Thread ct = new Thread(new ConnectThread());
        ct.start();
    }

    public void setPlaylistEntryCallBack( PlaylistAddCallBack plsCB )
    {
        playlistEntryEvent = plsCB;
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
                doInBackground();
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
        try {
            while(true) {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
                byte[] buffer = new byte[1024];

                int bytesRead;
                InputStream inputStream = socket.getInputStream();

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                    response = byteArrayOutputStream.toString("UTF-8");
                    if( null != playlistEntryEvent ) {
                        playlistEntryEvent.addPlaylistEntry(response);
                    }
                }
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
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
    }

}