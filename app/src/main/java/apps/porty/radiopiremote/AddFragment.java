package apps.porty.radiopiremote;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

/**
 * Created by porty on 7/6/16.
 */

public class AddFragment extends Fragment {
    public static final String ARG_PAGE = "section_number";

    private int mPage;
    static private EditText mYoutubeLink;

    public AddFragment()
    {
    }

    public static AddFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        AddFragment fragment = new AddFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add, container, false);
        mYoutubeLink = (EditText)view.findViewById(R.id.txt_youtube_link);
        return view;
    }

    public static void youtube_send_onClick( View view )
    {
        String link = "";

        Log.d("Frame", "play_onClick: ");
        link = mYoutubeLink.getText().toString();
        MainActivity.conn.send("\u0002" + "src=youtube" +"\u001d" + "link="+ link +"\u001d"+"\u0003");
    }
}
