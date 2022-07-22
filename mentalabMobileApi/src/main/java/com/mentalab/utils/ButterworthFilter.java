package com.mentalab.utils;

import uk.me.berndporr.iirj.Butterworth;

public class ButterworthFilter {
  /**
   * The Butterworth class implements low-pass, high-pass, band-pass and
   * band-stop filter using the Butterworth polynomials. Has the flattest pass-band but a poor
   * roll-off rate. Reference: https://en.wikipedia.org/wiki/Butterworth_filter
   */
  private double samplingFreq;

  /**
   * This constructor initialises the prerequisites required to use Butterworth filter.
   *
   * @param Fs Sampling frequency of input signal
   */
  public ButterworthFilter(double Fs) {
    this.samplingFreq = Fs;
  }

  /**
   * This method implements a low pass filter with given parameters, filters the signal and returns
   * it.
   *
   * @param signal Signal to be filtered
   * @param order Order of the filter
   * @param cutoffFreq The cutoff frequency for the filter in Hz
   * @return double[] Filtered signal
   */
  public double[] lowPassFilter(double[] signal, int order, double cutoffFreq) {
    double[] output = new double[signal.length];
    Butterworth lp = new Butterworth();
    lp.lowPass(order, this.samplingFreq, cutoffFreq);
    for (int i = 0; i < output.length; i++) {
      output[i] = lp.filter(signal[i]);
    }
    return output;
  }

  /**
   * This method implements a high pass filter with given parameters, filters the signal and returns
   * it.
   *
   * @param signal Signal to be filtered
   * @param order Order of the filter
   * @param cutoffFreq The cutoff frequency for the filter in Hz
   * @return double[] Filtered signal
   */
  public double[] highPassFilter(double[] signal, int order, double cutoffFreq) {
    double[] output = new double[signal.length];
    Butterworth hp = new Butterworth();
    hp.highPass(order, this.samplingFreq, cutoffFreq);
    for (int i = 0; i < output.length; i++) {
      output[i] = hp.filter(signal[i]);
    }
    return output;
  }

  /**
   * This method implements a band pass filter with given parameters, filters the signal and returns
   * it.
   *
   * @param signal Signal to be filtered
   * @param order Order of the filter
   * @param lowCutoff The lower cutoff frequency for the filter in Hz
   * @param highCutoff The upper cutoff frequency for the filter in Hz
   * @throws java.lang.IllegalArgumentException The lower cutoff frequency is greater than the
   *     higher cutoff frequency
   * @return double[] Filtered signal
   */
  public double[] bandPassFilter(double[] signal, int order, double lowCutoff, double highCutoff)
      throws IllegalArgumentException {
    if (lowCutoff >= highCutoff) {
      throw new IllegalArgumentException(
          "Lower Cutoff Frequency cannot be more than the Higher Cutoff Frequency");
    }
    double centreFreq = (highCutoff + lowCutoff) / 2.0;
    double width = Math.abs(highCutoff - lowCutoff);
    double[] output = new double[signal.length];
    Butterworth bp = new Butterworth();
    bp.bandPass(order, this.samplingFreq, centreFreq, width);
    for (int i = 0; i < output.length; i++) {
      output[i] = bp.filter(signal[i]);
    }
    return output;
  }

  /**
   * This method implements a band stop filter with given parameters, filters the signal and returns
   * it.
   *
   * @param signal Signal to be filtered
   * @param order Order of the filter
   * @param lowCutoff The lower cutoff frequency for the filter in Hz
   * @param highCutoff The upper cutoff frequency for the filter in Hz
   * @throws java.lang.IllegalArgumentException The lower cutoff frequency is greater than the
   *     higher cutoff frequency
   * @return double[] Filtered signal
   */
  public double[] bandStopFilter(double[] signal, int order, double lowCutoff, double highCutoff)
      throws IllegalArgumentException {
    if (lowCutoff >= highCutoff) {
      throw new IllegalArgumentException(
          "Lower Cutoff Frequency cannot be more than the Higher Cutoff Frequency");
    }
    double centreFreq = (highCutoff + lowCutoff) / 2.0;
    double width = Math.abs(highCutoff - lowCutoff);
    double[] output = new double[signal.length];
    Butterworth bs = new Butterworth();
    bs.bandStop(order, this.samplingFreq, centreFreq, width);
    for (int i = 0; i < output.length; i++) {
      output[i] = bs.filter(signal[i]);
    }
    return output;
  }
}
