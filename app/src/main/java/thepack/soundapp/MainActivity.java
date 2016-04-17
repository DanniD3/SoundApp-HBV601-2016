package thepack.soundapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import thepack.soundapp.models.Navigation;

public class MainActivity extends AppCompatActivity implements NavigationDrawerFragment.FragmentDrawerListener {

    private Toolbar toolbar;
    private NavigationDrawerFragment drawer;
    private Navigation nav = new Navigation();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        drawer = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        drawer.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.layout_main), toolbar);
        drawer.setDrawerListener(this);

        // Start up with MainFragment
        displayView(Navigation.NAV_HOME);
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDrawerItemSelected(View view, int position) {
        displayView(position);
    }

    /**
     * Displays the selected View from Navigation
     *
     * @param position is the position of the item selected from the Nav bar
     */
    public void displayView(int position) {
        Fragment fragment = null;
        String title = getString(R.string.app_name);

        // Check for selected view
        switch (position) {
            case Navigation.NAV_HOME:
                fragment = new MainFragment();
                if (nav.getUsername() != null) {
                    Bundle data = new Bundle();
                    data.putString("username", nav.getUsername());
                    fragment.setArguments(data);
                }
                break;
            case Navigation.NAV_SEARCH:
                fragment = new SoundClipFragment();
                title = getString(R.string.title_search);
                break;
            case Navigation.NAV_UPLOAD:
                fragment = new UploadFragment();
                title = getString(R.string.title_upload);
                break;
            case Navigation.NAV_LOGIN:
                if (nav.getUsername() == null) {
                    fragment = new LoginFragment();
                    title = getString(R.string.title_login);
                } else {
                    logout();
                }
                break;
            default:
                break;
        }

        // Swap the selected fragment into view
        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container_body, fragment);
            fragmentTransaction.commit();

            // set the toolbar title
            getSupportActionBar().setTitle(title);
            nav.setNavState(position);
        }
    }

    /**
     * Displays MainFragment and updates User without other Fragments or Activities
     * knowing the existence of Navigation
     */
    public void displayHome(String username) {
        nav.setUsername(username);
        drawer.updateUser(username);
        displayView(Navigation.NAV_HOME);
    }

    /**
     * Signs out the signed in User and update Navigation model and navigation bar
     */
    public void logout() {
        if (nav.getNavState() == Navigation.NAV_HOME) {
            ((MainFragment) getSupportFragmentManager().findFragmentById(R.id.container_body)).logout();
        }
        nav.logout();
        drawer.updateUser(null);
        Toast.makeText(this, R.string.success_sign_out, Toast.LENGTH_LONG).show();
    }

    /**
     * Returns the name of the current user
     */
    public String getUser() {
        return nav.getUsername();
    }

    @Override
    public void onBackPressed() {
        // Displays MainFragment if in other Fragment, otherwise quit
        if (nav.isAtHome())
            super.onBackPressed();
        else {
            displayView(Navigation.NAV_HOME);
        }
    }
}
