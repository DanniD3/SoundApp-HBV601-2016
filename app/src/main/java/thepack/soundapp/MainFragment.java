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

        searchButton = (Button) rootView.findViewById(R.id.search);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                act.displayView(Navigation.NAV_SEARCH);
            }
        });

        uploadButton = (Button) rootView.findViewById(R.id.upload);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                act.displayView(Navigation.NAV_UPLOAD);
            }
        });

        String username;
        try {
            username = getArguments().getString("username");
        } catch (NullPointerException e) {
            username = null;
        }
        loginButton = (Button) rootView.findViewById(R.id.login);
        if (username != null) {
            loginButton.setText(R.string.main_fragment_logout);
            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    act.logout();
                }
            });
        } else {
            loginButton = setLoginListener(loginButton);
        }
        return rootView;
    }

    /**
     * Signs out the user and update the buttons
     */
    public void logout() {
        updateButtonText(R.string.main_fragment_login);
        loginButton.setOnClickListener(null);
        loginButton = setLoginListener(loginButton);
    }

    /**
     * Updates the text on the loginButton with the resource with id {@param stringID}
     *
     * @param stringID is the ID of the string resource
     */
    private void updateButtonText(final int stringID) {
        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loginButton.setText(stringID);
            }
        });
    }

    /**
     * Sets the login onClick listener on button {@param b}
     *
     * @param b is the Button object to be clicked
     * @return a {@param b} that listens to click
     */
    private Button setLoginListener(Button b) {
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                act.displayView(Navigation.NAV_LOGIN);
            }
        });
        return b;
    }
}
