package thepack.soundapp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import thepack.soundapp.models.Navigation;

public class MainFragment extends Fragment{

    private Button loginButton, uploadButton, searchButton;
    private MainActivity act;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Store the calling activity as MainActivity for reference and changing views
        act = (MainActivity) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        loginButton = (Button) rootView.findViewById(R.id.login);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                act.displayView(Navigation.NAV_LOGIN);
            }
        });

        uploadButton = (Button) rootView.findViewById(R.id.upload);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                act.displayView(Navigation.NAV_UPLOAD);
            }
        });

        searchButton = (Button) rootView.findViewById(R.id.search);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                act.displayView(Navigation.NAV_SEARCH);
            }
        });
        return rootView;
    }
}
