/*******************************************************************************
 * Copyright (c) 2017, Xavier Miret Andres <xavier.mires@gmail.com>
 *
 * Permission to use, copy, modify, and/or distribute this software for any 
 * purpose with or without fee is hereby granted, provided that the above 
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES 
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALLIMPLIED WARRANTIES OF 
 * MERCHANTABILITY  AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR 
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES 
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN 
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF 
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 *******************************************************************************/
package org.mp3stream.mp3;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;

public class Samples
{
    private static final Map<Version, int[]> sampleRates;
    private static final Table<Version, Layer, Integer> samplePerFrame;

    static
    {
        final ImmutableMap.Builder<Version, int[]> builder1 = ImmutableMap.builder();

        builder1.put(Version.MPEG_1, new int[] { 44100, 48000, 32000 });
        builder1.put(Version.MPEG_2, new int[] { 22050, 24000, 16000 });
        builder1.put(Version.MPEG_2_5, new int[] { 11025, 12000, 8000 });

        sampleRates = builder1.build();

        final ImmutableTable.Builder<Version, Layer, Integer> builder2 = ImmutableTable.builder();

        builder2.put(Version.MPEG_1, Layer.LAYER_1, 384);
        builder2.put(Version.MPEG_1, Layer.LAYER_2, 1152);
        builder2.put(Version.MPEG_1, Layer.LAYER_3, 1152);
        builder2.put(Version.MPEG_2, Layer.LAYER_1, 384);
        builder2.put(Version.MPEG_2, Layer.LAYER_2, 1152);
        builder2.put(Version.MPEG_2, Layer.LAYER_3, 576);
        builder2.put(Version.MPEG_2_5, Layer.LAYER_1, 384);
        builder2.put(Version.MPEG_2_5, Layer.LAYER_2, 1152);
        builder2.put(Version.MPEG_2_5, Layer.LAYER_3, 576);

        samplePerFrame = builder2.build();
    }

    /**
     * Obtains sample rate in Hz for the given parameters as present in the frame header.
     */
    public static int sampleRate(Version version, int index)
    {
        try
        {
            return sampleRates.get(version)[index];
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            throw new UnsupportedOperationException("Invalid header (sample rate index)");
        }
    }

    /**
     * Obtains samples per frame given parameters as present in the frame header.
     */
    public static int samplesPerFrame(Version version, Layer layer)
    {
        return samplePerFrame.get(version, layer);
    }
}
