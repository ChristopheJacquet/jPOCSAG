# Introduction to receiving POCSAG #

POCSAG is a paging protocol originally developed by the British Post Office.

The binary data, in the form of a rectangular signal, is directly used to modulate the carrier wave in FSK, with a shift of ± 4.5 kHz. Concretely, if the carrier wave has a frequency of 145 MHz, then actually the transmitted signal is a sine wave, at times of 145,004.5 kHz, and at times of 144,995.5 kHz.

This means that an FM receiver should recover a rectangular signal after the discriminator stage. Unfortunately a narrowband FM (NFM) receiver such as an amateur radio rig will filter the signals at ± 2.5 kHz, so you don't get a nice rectangular signal out of the earphone plug when listening to POCSAG. One solution is to use (or add) a discriminator output, but it often proves impractical.

Considering the derivative of the signals and using a software [Schmitt trigger](http://en.wikipedia.org/wiki/Schmitt_trigger) with adaptive threshold, jPOCSAG achieves to reconstruct the original signals in the vast majority of the cases.

The signals are sampled by the computer using the sound card at 11.025 kHz, which is highly sufficient given the bitrates used by POCSAG (at most 1200 bit/s).

# Decoding POCSAG #

A POCSAG message starts with a 576-bit "synchronization sequence", consisting of alterning 0 and 1 bits. Even with NFM filtering, the sequence is easily recognizable (see figure below: the input signal is in blue at the top; the synchronization sequence is visible on the left). So it's easy to recover synchronization by determining the maximum correlation between the received signal and a square signal of the expected bit rate. Once synchronization has been recovered, we may reason on the sample instants only. These are represented by red cross markers on the blue signal at the top the figure.

![http://jpocsag.googlecode.com/svn/wiki/images/differential_schmitt.png](http://jpocsag.googlecode.com/svn/wiki/images/differential_schmitt.png)

The synchronization sequence is preserved by NFM filtering, but yet the data part of the frame (which is non-periodic) is significantly altered (see on the right). Indeed, measuring amplitudes are totally irrelevant, since a given amplitude may be a 0 as well as a 1. So we calculate the _derivative_ of the signal at the sample instants. The derivative is represented at the bottom of the figure, with red bars.

Looking at the derivative, it turns out that the signal is exploitable:
  * a difference greater than a given threshold means: “the next bit is a 0 (and the previous bit was a 1)”,
  * a difference less than a given threshold means: “the next bit is a 1 (and the previous bit was a 0)”,
  * in between, the next bit is the same as the previous bit.

(Note that the polarity of the above may be reversed, depending on your sound card.)

This is good news: it means that the signals may be reconstructed even without a discriminator output. One just has to use a Schmitt trigger, acting on the derivative. However, one question remains: _how to determine the thresholds of the Schmitt trigger?_

We know that after the 576-bit synchronization sequence, a POCSAG frame starts with a synchronization word, with special value `0x7CD215D8`. jPOCSAG determines the threshold values dynamically, by trying the different combinations of upper and lower threshold values. For each combination, jPOCSAG determines if the sync word, `0x7CD215D8`, is correctly decoding after the synchronization sequence. If it is the case, then the threshold combination is said to be “working” (for the sync word).

On the figure below, we have plotted the different combination of the upper threshold (horizontal axis) and lower threshold (vertical axis). For a given combination, if the combination is “working” the point is painted in green, otherwise, it is painted in red.

![http://jpocsag.googlecode.com/svn/wiki/images/threshold_plot.png](http://jpocsag.googlecode.com/svn/wiki/images/threshold_plot.png)

We notice that the set of “working combinations” determines a rectangular area, located between the “minimum working value of the upper threshold”, “maximum working value of the upper thresold”, “minimum working value of the lower threshold” and “maximum working value of the lower thresold”. To be as immune as possible, we decide to choose the effective threshold value at the center of this rectangle, at the place marked by a cross. This combination of threshold values is used for the decoding of the subsequent words of the frame.

The “threshold diagram” above is displayed on jPOCSAG's interface to give the user an indication about the available “margin”, and thus about the receiving conditions. For instance, it helps choose the correct output level, so as to have non-distorted signals.

# POCSAG information #

  * [POCSAG experiments](http://hem.passagen.se/communication/pocsag.html)
  * [The POCSAG paging protocol](http://www.braddye.com/epocsag.html)
  * [Wikipedia - POCSAG](http://en.wikipedia.org/wiki/POCSAG)
  * [The POCSAG Recommendation](http://www.braddye.com/pocsag.html)
  * [The POCSAG Paging Protocol](http://www.aaroncake.net/schoolpage/pocsag.htm)
  * [WAVECOM – POCSAG](http://www.wavecom.ch/onlinehelp/WCODE/default.htm?turl=WordDocuments%2Fpocsag.htm)


---

Christophe Jacquet, F8FTK, 2011-03-13.