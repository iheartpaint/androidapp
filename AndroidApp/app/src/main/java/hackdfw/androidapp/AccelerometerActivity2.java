package hackdfw.androidapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.util.Log;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;
import com.ilumi.sdk.IlumiSDK;

import java.text.DecimalFormat;
import java.util.Locale;
import java.util.UUID;



public class AccelerometerActivity2 extends Activity {
    private static final String TAG = "PebblePointer";

    private byte[] macAddressBytes;

    // The tuple key corresponding to a vector received from the watch
    private static final int PP_KEY_CMD = 128;
    private static final int PP_KEY_X   = 1;
    private static final int PP_KEY_Y   = 2;
    private static final int PP_KEY_Z   = 3;
    private static final int PP_KEY_COLOR = 4;

    private static final int PP_CMD_INVALID = 0;
    private static final int PP_CMD_VECTOR  = 1;
    private static final int PP_CMD_SELECT  = 2;

    public static final int VECTOR_INDEX_X  = 0;
    public static final int VECTOR_INDEX_Y  = 1;
    public static final int VECTOR_INDEX_Z  = 2;
    //public static final int VECTOR_INDEX_COLOR = 3;

    public float saturation = 0.5f;

    private static int vector[] = new int[3];

    public int currentColor = 0;
    public int hue = 0;
    private int color = 0;

    private static int lastTimeStamp = -1;

    private PebbleKit.PebbleDataReceiver dataReceiver;

    // This UUID identifies the PebblePointer app.
    private static final UUID PEBBLEPOINTER_UUID = UUID.fromString("0ef85252-8f67-409f-9bff-75da0fb79ae5");
    private PebbleKit.PebbleDataLogReceiver dataloggingReceiver;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        macAddressBytes = getIntent().getByteArrayExtra("macAddressBytes");

        Log.i(TAG, "onCreate: ");

        setContentView(R.layout.activity_accelerometer);
        // Hey so what are vectors really??
        vector[VECTOR_INDEX_X] = 0;
        vector[VECTOR_INDEX_Y] = 0;
        vector[VECTOR_INDEX_Z] = 0;

        PebbleKit.startAppOnPebble(getApplicationContext(), PEBBLEPOINTER_UUID);

        Button resetButton = (Button) findViewById(R.id.resetButton);
        final IlumiSDK.IlumiColor resetColor = new IlumiSDK.IlumiColor(0, 0, 0, 0, 0xFF);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IlumiSDK.sharedManager().setColor(macAddressBytes, resetColor);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.i(TAG, "onPause: ");

        setContentView(R.layout.activity_accelerometer);

        if (dataReceiver != null) {
            unregisterReceiver(dataReceiver);
            dataReceiver = null;
        }
        PebbleKit.closeAppOnPebble(getApplicationContext(), PEBBLEPOINTER_UUID);
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.i(TAG, "onResume: ");

        final Handler handler = new Handler();

        boolean connected = PebbleKit.isWatchConnected(getApplicationContext()); Log.i(getLocalClassName(), "Pebble is " + (connected ? "connected" : "not connected"));
        dataReceiver = new PebbleKit.PebbleDataReceiver(PEBBLEPOINTER_UUID) {

            @Override
            public void receiveData(final Context context, final int transactionId, final PebbleDictionary dict) {
                Log.i(TAG, "data received");

                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        PebbleKit.sendAckToPebble(context, transactionId);

                        final Long cmdValue = dict.getInteger(PP_KEY_CMD);
                        if (cmdValue == null) {
                            return;
                        }

                        if (cmdValue.intValue() == PP_CMD_VECTOR) {

                            // Capture the received vector.
                            final Long xValue = dict.getInteger(PP_KEY_X);
                            if (xValue != null) {
                                vector[VECTOR_INDEX_X] = xValue.intValue();
                            }
                            Log.i(TAG, "vector x" + vector[VECTOR_INDEX_X]);
                            //Application.SynchronizationContext.Post (_ => {findviewbyid(R.id.xaxis).settext(vector[x]);}, null);

                            final Long yValue = dict.getInteger(PP_KEY_Y);
                            if (yValue != null) {
                                vector[VECTOR_INDEX_Y] = yValue.intValue();
                            }
                            Log.i(TAG, "vector y" + vector[VECTOR_INDEX_Y]);

                            long timeelapsed = Math.min(System.currentTimeMillis() - lastTimeStamp, 1000);
                            float RC = 0.3f;
                            float alpha = timeelapsed / (RC + timeelapsed);
                            saturation = ((alpha * vector[VECTOR_INDEX_Y]) + (1.0f - alpha)) / 800;
                            Log.i(TAG, "saturation " + saturation);
                            //saturation = saturation + (vector[VECTOR_INDEX_Y] / 1000);
                            saturation = Math.max(0.5f, Math.min(1, saturation));

                            final Long zValue = dict.getInteger(PP_KEY_Z);
                            if (zValue != null) {
                                vector[VECTOR_INDEX_Z] = zValue.intValue();
                            }
                            Log.i(TAG, "vector z" + vector[VECTOR_INDEX_Z]);

                            final Long colorValue = dict.getInteger(PP_KEY_COLOR);
                            if (colorValue != null) {
                                currentColor = colorValue.intValue();
                            }
                            Log.i(TAG, "color: " + currentColor);

                            switch(currentColor) {
                                case 1: currentColor = 0;
                                    hue = 0;
                                    break;
                                case 2: currentColor = 1;
                                    hue = 50;
                                    break;
                                case 3: currentColor = 2;
                                    hue = 102;
                                    break;
                                case 4: currentColor = 3;
                                    hue = 180;
                                    break;
                                case 5: currentColor = 4;
                                    hue = 231;
                                    break;
                                case 6: currentColor = 5;
                                    hue = 281;
                                    break;
                            }

                            color = Color.HSVToColor(new float[] {hue,saturation,1f});

                            IlumiSDK.IlumiColor satColor = new IlumiSDK.IlumiColor(Color.red(color), Color.green(color), Color.blue(color), 0, 0xFF);
                            IlumiSDK.sharedManager().setColor(macAddressBytes, satColor);

                            /*
                            if(cmdValue.intValue() == PP_CMD_SELECT) {
                                Log.i(TAG, "Finalized selection.");
                                String hexColor = String.format("%06X", (0xFFFFFF & color));
                                hexColor = hexColor.substring(2);
                                Log.i(TAG, "HEX: " + hexColor);
                                final Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://iheartpaint.azurewebsites.net/add.php?color=" + hexColor));
                                startActivity(intent);
                            }
                            */
                            //TextView backgroundColor = (TextView) findViewById(R.id.backgroundColor);
                            //backgroundColor.setBackgroundColor(color);

                        }
                        if(cmdValue.intValue() == PP_CMD_SELECT) {
                            Log.i(TAG, "Finalized selection.");
                            String hexColor = String.format("%06X", (0xFFFFFF & color));
                            hexColor = hexColor.substring(2);
                            Log.i(TAG, "HEX: " + hexColor);
                            final Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://iheartpaint.azurewebsites.net/add.php?color=" + hexColor));
                            startActivity(intent);
                        }
                    }
                });
            }
        };

        PebbleKit.registerReceivedDataHandler(this, dataReceiver);
    }
}
