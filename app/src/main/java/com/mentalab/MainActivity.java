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
import com.mentalab.exception.CommandFailedException;
import com.mentalab.exception.InvalidCommandException;
import com.mentalab.exception.NoBluetoothException;
import com.mentalab.exception.NoConnectionException;
import com.mentalab.utils.InputSwitch;
import com.mentalab.utils.constants.Protocol;
import com.mentalab.utils.constants.SamplingRate;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

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
            MentalabCommands.decodeInputStream();
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
      final Future<Boolean> formattedMemory = connectedDevice.formatDeviceMemory();
      if (!formattedMemory.get()) {
        createToastMsg(
                MainActivity.this,
                "Something went wrong when formatting the memory. Please try again.");
        throw new CommandFailedException("Failed to format memory");
      }

      final Future<Boolean> samplingRateSet = connectedDevice.postSamplingRate(SamplingRate.SR_500);
      if (!samplingRateSet.get()) {
        createToastMsg(
                MainActivity.this,
                "Something went wrong setting the sampling rate. Please try again.");
        throw new CommandFailedException("Failed to set the sampling rate");
      }

      final Future<Boolean> modulesSet = connectedDevice.postActiveModules(new InputSwitch(Protocol.ENVIRONMENT, false));
      if (!modulesSet.get()) {
        createToastMsg(
                MainActivity.this,
                "Something went wrong when trying to turn of a module. Please try again.");
        throw new CommandFailedException("Failed to set the module");
      }
    } catch (InvalidCommandException | InterruptedException | ExecutionException | CommandFailedException e) {
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
