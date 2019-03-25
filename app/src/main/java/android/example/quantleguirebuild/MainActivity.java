package android.example.quantleguirebuild;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity
        implements BottomNavigationView.OnNavigationItemSelectedListener
        , HistoryFragment.OnHistoryFragmentInteractionListener
        , CaptureTalkFragment.OnCaptureTalkFragmentInteractionListener {

    private TextView mTextMessage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextMessage = findViewById(R.id.message);
        BottomNavigationView navigation = findViewById(R.id.bottomNavigationView);
        navigation.setOnNavigationItemSelectedListener(this);
        loadFragment(new CaptureTalkFragment(), false);
    }


    private boolean loadFragment(Fragment fragment, boolean addToStack) {
        if (fragment != null) {
            if (addToStack) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .addToBackStack(null)
                        .replace(R.id.fragment_container, fragment)
                        .commit();
            } else {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit();
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        Fragment fragment = null;
        switch (menuItem.getItemId()) {
            case R.id.navigation_record_audio:
                fragment = new CaptureTalkFragment();
                break;
            case R.id.navigation_history:
                fragment = new HistoryFragment();
                break;
            default:
                break;
        }
        return loadFragment(fragment, true);
    }

    @Override
    public void onHistoryFragmentInteraction(Uri uri) {

    }

    @Override
    public void OnCaptureTalkFragmentInteraction(Uri uri) {

    }
}
