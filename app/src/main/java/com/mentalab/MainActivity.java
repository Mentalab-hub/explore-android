package com.mentalab;

import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import com.mentalab.exception.InitializationFailureException;
import com.mentalab.exception.NoBluetoothException;
import com.mentalab.exception.NoConnectionException;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

  @RequiresApi(api = VERSION_CODES.R)
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    try {
      MentalabCommands.connect("CA4A").acquire();
    } catch (NoBluetoothException
        | NoConnectionException
        | IOException
        | ExecutionException
        | InterruptedException e) {
      e.printStackTrace();
    }
  }
}
