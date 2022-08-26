package com.mentalab;

import com.mentalab.utils.commandtranslators.Command;
import com.mentalab.utils.commandtranslators.CommandTranslator;

import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class MentalabCodec {

  // runs independently of all else
  private static final ExecutorService DECODE_EXECUTOR = Executors.newSingleThreadExecutor();
  private static final ParseRawDataTask DECODER_TASK = new ParseRawDataTask();

  private MentalabCodec() { // Static class
  }

  /**
   * Tells the ExploreExecutor to decode the raw data.
   *
   * @param rawData InputStream of device bytes
   */
  public static void decodeInputStream(InputStream rawData) {
    DECODER_TASK.setInputStream(rawData);
    DECODE_EXECUTOR.submit(DECODER_TASK);
  }

  /**
   * Encodes a command
   *
   * @return byte[] encoded commands that can be sent to the device
   */
  static byte[] encodeCommand(Command command) {
    final CommandTranslator translator = command.createCommandTranslator();
    return translator.translateCommand();
  }

  public static void shutdown() {
    Thread.currentThread().interrupt();
    DECODE_EXECUTOR.shutdownNow();
  }
}
