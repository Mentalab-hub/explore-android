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
import com.mentalab.exception.NoBluetoothException;
import com.mentalab.exception.NoConnectionException;
import com.mentalab.exception.OperationFailedException;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

  public static final String TAG = "Explore";

  private ExploreDevice connectedDevice;

  @RequiresApi(api = VERSION_CODES.R)
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    final String exploreDeviceID = "CA26";
    try {
      connect(exploreDeviceID);
      MentalabCommands.startDataAcquisition();
    } catch (NoBluetoothException e) {
      askToTurnOnBT(exploreDeviceID);
      return;
    } catch (IOException | NoConnectionException e) {
      askToTurnOnDevice(exploreDeviceID);
      return;
    } catch (OperationFailedException e) {
      createToastMsg(MainActivity.this, "Failed to initialize data acquisition.");
    }

    // device config API demonstration
    /*try {
      final Future<Boolean> formattedMemory = connectedDevice.formatMemory();
      if (!formattedMemory.get()) {
        createToastMsg(
            MainActivity.this,
            "Something went wrong when formatting the memory. Please try again.");
        throw new CommandFailedException("Failed to format memory");
      }

      final Future<Boolean> samplingRateCmd = connectedDevice.setSamplingRate(SamplingRate.SR_500);
      if (!samplingRateCmd.get()) {
        createToastMsg(
            MainActivity.this, "Something went wrong setting the sampling rate. Please try again.");
        throw new CommandFailedException("Failed to set the sampling rate");
      }

      final Future<Boolean> modulesCmd =
          connectedDevice.setModule(new InputSwitch(InputProtocol.ENVIRONMENT, false));
      if (!modulesCmd.get()) {
        createToastMsg(
            MainActivity.this,
            "Something went wrong when trying to turn of a module. Please try again.");
        throw new CommandFailedException("Failed to set the module");
      }

      Set<InputSwitch> channelSwitches = new HashSet<>();
      channelSwitches.add(new InputSwitch(InputProtocol.CHANNEL_0, false));
      channelSwitches.add(new InputSwitch(InputProtocol.CHANNEL_3, false));
      channelSwitches.add(new InputSwitch(InputProtocol.CHANNEL_4, true));
      final Future<Boolean> channelsSet =
              connectedDevice.setChannels(channelSwitches);
      if (!channelsSet.get()) {
        createToastMsg(
                MainActivity.this,
                "Something went wrong when trying to turn of channels. Please try again.");
        throw new CommandFailedException("Failed to set the module");
      }
    } catch (InvalidCommandException
        | InterruptedException
        | ExecutionException
        | CommandFailedException e) {
      e.printStackTrace();
    }*/
  }

  private void connect(String exploreDeviceID)
      throws NoConnectionException, IOException, NoBluetoothException {
    this.connectedDevice = MentalabCommands.connect(exploreDeviceID);
  }

  private void connectToDevice(String exploreDeviceID) {
    try {
      connect(exploreDeviceID);
    } catch (NoBluetoothException e) {
      askToTurnOnBT(exploreDeviceID);
    } catch (IOException | NoConnectionException e) {
      askToTurnOnDevice(exploreDeviceID);
    }
  }

  private void askToTurnOnBT(String exploreDeviceID) {
    ActivityResultLauncher<Intent> activityLauncher =
        registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
              if (result.getResultCode() == Activity.RESULT_OK) {
                Log.i(TAG, "Bluetooth activated.");
                connectToDevice(exploreDeviceID); // try again
              } else {
                closeWithPrompt(
                    MainActivity.this,
                    "Cannot Proceed",
                    "Explore Android cannot proceed without Bluetooth. Closing down.");
              }
            });
    activityLauncher.launch(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
  }

  private void askToTurnOnDevice(String exploreDeviceID) {
    new AlertDialog.Builder(MainActivity.this)
        .setIcon(android.R.drawable.ic_notification_clear_all)
        .setTitle("Connection error")
        .setMessage(
            "Unable to connect to: "
                + exploreDeviceID
                + ". "
                + "It is possible your device is switched off. If so, please switch it on and then click 'Reconnect'.")
        .setNegativeButton("Close", (dialog, which) -> finishAffinity())
        .setPositiveButton("Reconnect", (dialog, which) -> connectToDevice(exploreDeviceID))
        .show();
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
}
