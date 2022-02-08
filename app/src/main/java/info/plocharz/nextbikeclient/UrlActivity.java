package info.plocharz.nextbikeclient;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class UrlActivity extends Activity {
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        String action = intent.getAction();
        
        String message = intent.getStringExtra(MainActivity.INTENT_ACTION);
        if(message != null) {
            throw new RuntimeException("Serious Fuck up");
        }
        
        if(Intent.ACTION_VIEW.equals(action)){
            String bikeId = null;
            String path = intent.getData().getPath();
            Pattern p1 = Pattern.compile("/b/(\\d{5})");
            Pattern p2 = Pattern.compile("/(\\d{5})");
            Matcher m1 = p1.matcher(path);
            Matcher m2 = p2.matcher(path);
            if(m1.matches()){
                bikeId = m1.group(1);
            }
            
            if(m2.matches()){
                bikeId = m2.group(1);
            }
            
            if(bikeId != null){
                Intent targetIntent = new Intent(this, MainActivity.class);
                targetIntent.putExtra(MainActivity.INTENT_ACTION, MainActivity.ACTION_RENT);
                targetIntent.putExtra(MainActivity.RENT_ID, bikeId);
                startActivity(targetIntent);
            } else {
                Logger.e("Pattern not recognized" + path);
            }
        }
        super.onCreate(savedInstanceState);
    }
}
