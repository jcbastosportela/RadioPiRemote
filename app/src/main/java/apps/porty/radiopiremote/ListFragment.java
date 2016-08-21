package apps.porty.radiopiremote;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by porty on 7/5/16.
 */

public class ListFragment extends Fragment implements TCPConn.PlaylistAddCallBack {
    public static final String ARG_PAGE = "section_number";

    private int mPage;
    private LinearLayout rlButtons;
    private View rootView;

    public ListFragment()
    {
    }

    public static ListFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        ListFragment fragment = new ListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_list, container, false);

        rlButtons = (LinearLayout) rootView.findViewById(R.id.rl_buttons);

        MainActivity.conn.setPlaylistEntryCallBack( this );
        /* request playlist */
        MainActivity.conn.send("\u0002" + "cmd=gpls" + "\u001d\u0003");
        return rootView;
    }

    private static String sStrEntry;
    @Override
    public void addPlaylistEntry( String strEntry )
    {
        /* TODO implement the population of the playlist */
        sStrEntry = strEntry;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Button button = new Button(rootView.getContext());
                button.setText(sStrEntry);
                button.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                rlButtons.addView(button);
            }
        });

    }
}
