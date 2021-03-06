package apps.porty.radiopiremote;

import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.app.AlertDialog;

public class MainActivity extends AppCompatActivity implements TCPConn.TCPResCallBack
{
    public static TCPConn conn;
    public static boolean bTCPConnected;
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onResume()
    {
        super.onResume();
        Log.d("Main", "onResume called");
        /* Check if we are being called via share menu */
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        String action = intent.getAction();
        try {
            if ((boolean) extras.get("used"))
                return;
        }catch (Exception err){}

        if (Intent.ACTION_SEND.equals(action))
        {
            /* lunch a thread that will wait the connection to be established and executes req */
            Thread execIntent = new Thread( new ActionSendThread(intent));
            execIntent.start();
            intent.setAction("");
            intent.removeExtra("key");
            intent.putExtra("used", true);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        /* init everything that must be called just once */
        if(null == savedInstanceState)
        {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

            bTCPConnected = false;
            Log.d("Main", "Run just once");
            /* init the TCP  */
            String IP = sp.getString(getText(R.string.radio_pi_IP).toString(), "");
            int Port = 0;
            try {
                Port = Integer.parseInt(sp.getString(getText(R.string.radio_pi_PORT).toString(), ""));
            }catch (Exception e)
            {
            }
            conn = new TCPConn(IP, Port, this);
        }
    }

    public class ActionSendThread implements Runnable
    {
        private Intent intent;

        ActionSendThread( Intent _intent )
        {
            intent = _intent;
        }
        @Override
        public void run()
        {
            /* wait for the connection to become ready. This is nasty, but who cares?! */
            while (!bTCPConnected)
            {
                try {
                    Thread.sleep(1000, 0);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }


            runOnUiThread(new Runnable() {
                public void run()
                {
                    /* ask what to do */
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder
                            .setTitle("Reproduce options")
                            .setMessage("Also add to playlist?")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog, int which)
                                {
                            /* get the youtube link from the intent */
                                    ClipData.Item item = null;
                                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                                        item = intent.getClipData().getItemAt(0);
                                    }
                                    String link = "";
                                    link = item.getText().toString();
                                    MainActivity.conn.send("\u0002" + "add=media" + "\u001d" + link + "\u001d" + "\u0003");
                                }
                            });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int which)
                        {
                    /* get the youtube link from the intent */
                            ClipData.Item item = null;
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                                item = intent.getClipData().getItemAt(0);
                            }
                            String link = "";
                            link = item.getText().toString();
                            MainActivity.conn.send("\u0002" + "src=media" + "\u001d" + link + "\u001d" + "\u0003");
                            //dialog.dismiss();
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            });
        }
    }
    public String parseUriToFilename(Uri uri) {
        String selectedImagePath = null;
        String filemanagerPath = uri.getPath();

        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);

        if (cursor != null) {
            // Here you will get a null pointer if cursor is null
            // This can be if you used OI file manager for picking the media
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            selectedImagePath = cursor.getString(column_index);
        }

        if (selectedImagePath != null) {
            return selectedImagePath;
        }
        else if (filemanagerPath != null) {
            return filemanagerPath;
        }
        return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            Intent intent = new Intent();
            intent.setClassName(this, "apps.porty.radiopiremote.PreferencesActivity");
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void btn_ctrl_onClick( View view )
    {
        Log.d("Main", "btn_ctrl_onClick: ");
        switch (view.getId() )
        {
            /* Main buttons */
            case R.id.btn_ctrl_pwr:
                MainFragment.pwr_onClick( view );
                break;

            case R.id.btn_play:
                MainFragment.play_onClick( view );
                break;

            case R.id.btn_ctrl_stop:
                MainFragment.stop_onClick( view );
                break;

            case R.id.btn_ctrl_volup:
                MainFragment.volup_onClick( view );
                break;

            case R.id.btn_ctrl_voldown:
                MainFragment.voldown_onClick( view );
                break;

            /* buttons from add_frame */
            case R.id.btn_radio_link_submit:
                AddFragment.radio_link_send_onClick( view );
                break;

            case R.id.btn_youtube_submit:
                AddFragment.youtube_send_onClick( view );
                break;

            default:
                Log.e("Err", "No valid button id");
        }
    }


    public interface ConnCallBack
    {
        void ConnStts( boolean bStts );
    }
    public static ConnCallBack connectionCB;
    public static void setConnCallback(ConnCallBack _ce)
    {
        connectionCB = _ce;
    }

    @Override
    public void TCPResult(boolean bRes)
    {
        Log.d("Main", "The TCP connection is " + bRes);
        bTCPConnected = bRes;

        if( null != connectionCB ) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    connectionCB.ConnStts(bTCPConnected);
                }
            });
        }
    }
}
