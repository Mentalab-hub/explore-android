package com.mentalab.service.decode;

import com.mentalab.utils.commandtranslators.Command;
import com.mentalab.utils.commandtranslators.CommandTranslator;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class MentalabCodec {

  // runs independently of all else
  private static final ExecutorService DECODE_EXECUTOR = Executors.newSingleThreadExecutor();
  private static final ParseRawDataTask DECODER_TASK = new ParseRawDataTask();

  private MentalabCodec() {}

  public static MentalabCodec getInstance() {
    return MentalabCodec.InstanceHolder.INSTANCE;
  }

  /**
   * Encodes a command
   *
   * @return byte[] encoded commands that can be sent to the device
   */
  public static byte[] encodeCommand(Command command) {
    final CommandTranslator translator = command.createCommandTranslator();
    return translator.translateCommand();
  }

  /**
   * Tells the ExploreExecutor to decode the raw data.
   *
   * @param rawData InputStream of device bytes
   */
  public void decodeInputStream(InputStream rawData) {
    DECODER_TASK.setInputStream(rawData);
    DECODE_EXECUTOR.submit(DECODER_TASK);
  }

  public void shutdown() {
    Thread.currentThread().interrupt();
    DECODE_EXECUTOR.shutdownNow();
  }

  private static class InstanceHolder { // Initialization-on-demand synchronization
    private static final MentalabCodec INSTANCE = new MentalabCodec();
  }
}
