package com.mentalab;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import com.mentalab.exception.InvalidCommandException;
import com.mentalab.exception.NoBluetoothException;
import com.mentalab.exception.NoConnectionException;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

  public static final String TAG = "Explore";

  private ExploreDevice connectedDevice;

  @RequiresApi(api = VERSION_CODES.R)
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    String exploreDeviceID = "1C32";

    Operator.doTry(
        new Operation() {
          @Override
          public void run() throws NoConnectionException, IOException, NoBluetoothException {
            ExploreDevice expl = MentalabCommands.connect(exploreDeviceID);
            setConnectedDevice(expl);
          }

          @Override
          public void handleException(Exception cause) {
            if (cause instanceof NoBluetoothException) {
              askToTurnOnBT(this);
            } else if (cause instanceof NoConnectionException) {
              createToastMsg(
                  MainActivity.this,
                  "Unable to connect to: " + exploreDeviceID + ". Please try again.");
            } else if (cause instanceof InvalidCommandException) {
              closeWithPrompt(
                  MainActivity.this,
                  "Something's gone wrong.",
                  "Very sorry - but there appears to have been a mistake when we tried to send that command."
                      + " Please try restarting. If the problem persists please contact us at: contact@mentalab.com");
            }
          }
        });

    try {
      connectedDevice.formatDeviceMemory();
    } catch (InvalidCommandException e) {
      e.printStackTrace();
    }
  }

  private void askToTurnOnBT(Operation operation) {
    ActivityResultLauncher<Intent> a =
        registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
              if (result.getResultCode() == Activity.RESULT_OK) {
                Log.i(TAG, "Bluetooth activated.");
                Operator.doTry(operation);
              } else {
                closeWithPrompt(
                    MainActivity.this,
                    "Cannot Proceed",
                    "Explore Desktop cannot proceed without Bluetooth. Closing down.");
              }
            });
    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
    a.launch(intent);
  }

  private void closeWithPrompt(Context context, String title, String msg) {
    new AlertDialog.Builder(context)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setTitle(title)
        .setMessage(msg)
        .setNeutralButton("Okay", (dialog, which) -> finishAffinity())
        .show();
  }

  private void createToastMsg(Context context, String msg) {
    int duration = Toast.LENGTH_LONG;
    Toast toast = Toast.makeText(context, msg, duration);
    toast.show();
  }

  private void setConnectedDevice(ExploreDevice device) {
    this.connectedDevice = device;
  }

  public static class Operator {
    public static void doTry(Operation operation) {
      try {
        operation.run();
      } catch (Exception e) {
        operation.handleException(e);
      }
    }
  }
}
