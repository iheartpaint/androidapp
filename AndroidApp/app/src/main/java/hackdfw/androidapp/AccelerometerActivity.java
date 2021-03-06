package hackdfw.androidapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
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



public class AccelerometerActivity extends Activity {
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

    public static final int VECTOR_INDEX_X  = 0;
    public static final int VECTOR_INDEX_Y  = 1;
    public static final int VECTOR_INDEX_Z  = 2;
    //public static final int VECTOR_INDEX_COLOR = 3;

    private static int vector[] = new int[3];

    private static int currentColor = 0;

    private PebbleKit.PebbleDataReceiver dataReceiver;

    // This UUID identifies the PebblePointer app.
    private static final UUID PEBBLEPOINTER_UUID = UUID.fromString("0ef85252-8f67-409f-9bff-75da0fb79ae5");
    private PebbleKit.PebbleDataLogReceiver dataloggingReceiver;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        macAddressBytes = getIntent().getByteArrayExtra("macAddressBytes");

        Log.i(TAG, "onCreate: ");

        setContentView(R.layout.activity_accelerometer_activity2);
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

        setContentView(R.layout.activity_accelerometer_activity2);

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

        dataReceiver = new PebbleKit.PebbleDataReceiver(PEBBLEPOINTER_UUID) {

            @Override
            public void receiveData(final Context context, final int transactionId, final PebbleDictionary dict) {

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
                        }
                    }
                });
            }
        };

        PebbleKit.registerReceivedDataHandler(this, dataReceiver);
    }
}
