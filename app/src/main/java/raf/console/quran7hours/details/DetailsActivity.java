package raf.console.quran7hours.details;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import raf.console.quran7hours.R;
import raf.console.quran7hours.databinding.ActivityDetailsBinding;
import raf.console.quran7hours.ui.fragments.about_app.AppAboutFragment;

public class DetailsActivity extends AppCompatActivity {
    ActivityDetailsBinding b;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityDetailsBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(b.getRoot());

        AppAboutFragment appAboutFragment = new AppAboutFragment();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerFragment, new AppAboutFragment())
                .commit();

    }
}