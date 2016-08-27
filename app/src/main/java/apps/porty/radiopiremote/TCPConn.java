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
import android.os.Debug;
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

    private final long TCP_TIMEOUT = 2000; // 2s
    public final String FRAME_HEAD = "\u0002";
    public final String FRAME_SEPARATOR = "\u001d";
    public final String FRAME_TAIL = "\u0003";

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
            if( os != null )
                os.write(snd);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Void doInBackground(Void... arg0)
    {
        boolean bFrame = false;        // flags frame rcv finished
        String strFrame = "";
        long timeBgn;
        byte[] buffer = new byte[1024];
        int bytesRead;
        int bufOS;

        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
            InputStream inputStream = socket.getInputStream();

            while(true) {
                bFrame = false;
                strFrame = "";
                timeBgn = System.currentTimeMillis();
                bufOS = 0;

                while( !bFrame )
                {
                    if( strFrame != "" && System.currentTimeMillis() - timeBgn > TCP_TIMEOUT+2 )
                    {
                        Log.d("TCP", "Timeout and some data: " + strFrame);
                        strFrame = "";
                    }
                    try {
                        bytesRead = inputStream.read(buffer, bufOS, 1);
                        if( 0 < bytesRead )
                        {
                            bufOS += bytesRead;
                            //strFrame += new String(buffer).substring(0, bytesRead);

                            //if( strFrame.startsWith(FRAME_HEAD) && strFrame.endsWith(FRAME_TAIL) )
                            if( (buffer[0] == FRAME_HEAD.getBytes()[0]) && (buffer[bufOS-1] == FRAME_TAIL.getBytes()[0]) )
                            {
                                strFrame += new String(buffer).substring(0, bufOS);
                                Log.d("TCP", "Frame received: " + strFrame);
                                bFrame = true;
                                bufOS = 0;
                            }
                            //else if( !strFrame.startsWith(FRAME_HEAD) )
                            else if( buffer[0] != FRAME_HEAD.getBytes()[0] )
                            {
                                strFrame += new String(buffer).substring(0, bytesRead);
                                Log.d("TCP", "Invalid HEAD: " + strFrame);
                                strFrame = "";
                                bufOS = 0;
                            }
                            timeBgn = System.currentTimeMillis();
                        }
                        else
                        {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException ie) {
                            ie.printStackTrace();
                        }
                    }
                }
                processFrame( strFrame );

/*                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                    response = byteArrayOutputStream.toString("UTF-8");
                    if( null != playlistEntryEvent ) {
                        Log.d("TCP","Received: " + response);
                        playlistEntryEvent.addPlaylistEntry(response);
                    }
                }*/
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

    private void processFrame( String frame )
    {
        if( frame.contains("cmd=radiopls") )
        {
            frame = frame.replace(FRAME_HEAD,"").replace(FRAME_SEPARATOR,"").replace(FRAME_TAIL,"").replace("cmd=pls","");
            if( null != playlistEntryEvent ) {
                Log.d("TCP","Radio Received: " + frame);
                playlistEntryEvent.addPlaylistEntry(frame);
            }
        }
        else if( frame.contains("cmd=youtbpls") )
        {
            frame = frame.replace(FRAME_HEAD,"").replace(FRAME_SEPARATOR,"").replace(FRAME_TAIL,"").replace("cmd=youtbpls","");
            if( null != playlistEntryEvent ) {
                Log.d("TCP","youtube Received: " + frame);
                playlistEntryEvent.addPlaylistEntry(frame);
            }
        }
    }
    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
    }

}