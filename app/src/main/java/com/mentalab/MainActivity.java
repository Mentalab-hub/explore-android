package com.mentalab;

import static android.os.SystemClock.sleep;

import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import com.mentalab.exception.CommandFailedException;
import com.mentalab.exception.InvalidCommandException;
import com.mentalab.exception.NoBluetoothException;
import com.mentalab.exception.NoConnectionException;

import com.mentalab.utils.constants.SamplingRate;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

  @RequiresApi(api = VERSION_CODES.R)
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    try {
      final ExploreDevice connect = MentalabCommands.connect("855E");
      connect.acquire();
      sleep(10);
      connect.calculateImpedance();
    }
    //catch (NoBluetoothException | NoConnectionException | IOException | ExecutionException | InterruptedException e) {
    catch (NoBluetoothException | NoConnectionException | IOException | ExecutionException | InterruptedException | InvalidCommandException | CommandFailedException e) {
    e.printStackTrace();
    }
  }
}
