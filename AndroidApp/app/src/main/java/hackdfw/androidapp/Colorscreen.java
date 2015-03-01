package hackdfw.androidapp;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.ilumi.sdk.IlumiPacking;
import com.ilumi.sdk.IlumiSDK;
import com.ilumi.sdk.callbacks.IsSuccessCallBack;


public class Colorscreen extends Activity {

    private byte[] macAddressBytes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_colorscreen);

        macAddressBytes = getIntent().getByteArrayExtra("macAddressBytes");

        IlumiSDK.sharedManager().turnOn(macAddressBytes);

        Button randomButton = (Button) findViewById( R.id.randomColorButton );
        randomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IlumiSDK.sharedManager().setRandomColor(macAddressBytes);
            }
        });

        Button colorButton = (Button) findViewById(R.id.setColorButton);
        final IlumiSDK.IlumiColor testColor = new IlumiSDK.IlumiColor(0xFF, 0, 0, 0, 0xFF);
        colorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IlumiSDK.sharedManager().setColor(macAddressBytes, testColor);
            }
        });

        Button turnOnButton = (Button) findViewById( R.id.turnOnButton );
        turnOnButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                IlumiSDK.sharedManager().turnOn(macAddressBytes);
            }
        });

        Button turnOffButton = (Button) findViewById( R.id.turnOffButton );
        turnOffButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                IlumiSDK.sharedManager().turnOff(macAddressBytes);
            }
        });
    }

}
