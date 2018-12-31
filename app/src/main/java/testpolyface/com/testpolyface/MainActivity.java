package testpolyface.com.testpolyface;

import android.content.Intent;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {


    ImageView bgapp, clover;
    Animation bganim, clovernim;
    LinearLayout textsplash, texthome, menus;
    Animation frombottom;
    ImageView camera, realTime;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        frombottom = AnimationUtils.loadAnimation(this, R.anim.frombottom);

        camera = (ImageView) findViewById(R.id.camera);
        realTime = (ImageView) findViewById(R.id.realtime);

        realTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent aboutActivity = new Intent(MainActivity.this, RealtimeActivity.class);
                startActivity(aboutActivity);
            }
        });


        bgapp = (ImageView) findViewById(R.id.bgapp);
        clover = (ImageView) findViewById(R.id.clover);
        textsplash = (LinearLayout) findViewById(R.id.textsplash);
        texthome = (LinearLayout) findViewById(R.id.texthome);
        menus = (LinearLayout) findViewById(R.id.menus);


        bgapp.animate().translationY(-2300).setDuration(800).setStartDelay(800);
        clover.animate().translationY(-2250).setDuration(1200).setStartDelay(1000);
        textsplash.animate().translationY(140).alpha(0).setDuration(800).setStartDelay(1000);

        texthome.startAnimation(frombottom);
        menus.startAnimation(frombottom);
    }
}
