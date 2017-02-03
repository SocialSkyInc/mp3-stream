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

import com.google.common.collect.ImmutableTable;
import com.google.common.collect.ImmutableTable.Builder;
import com.google.common.collect.Table;

public class Bitrate
{
    private static final Table<Version, Layer, int[]> bitrates;

    static
    {
        final Builder<Version, Layer, int[]> builder = ImmutableTable.builder();

        builder.put(Version.MPEG_1, Layer.LAYER_1, new int[] { 32, 64, 96, 128, 160, 192, 224, 256, 288, 320, 352, 384, 416, 448 });
        builder.put(Version.MPEG_1, Layer.LAYER_2, new int[] { 32, 48, 56, 64, 80, 96, 112, 128, 160, 192, 224, 256, 320, 384 });
        builder.put(Version.MPEG_1, Layer.LAYER_3, new int[] { 32, 40, 48, 56, 64, 80, 96, 112, 128, 160, 192, 224, 256, 320 });

        builder.put(Version.MPEG_2, Layer.LAYER_1, new int[] { 32, 48, 56, 64, 80, 96, 112, 128, 144, 160, 176, 192, 224, 256 });
        builder.put(Version.MPEG_2, Layer.LAYER_2, new int[] { 8, 16, 24, 32, 40, 48, 56, 64, 80, 96, 112, 128, 144, 160 });
        builder.put(Version.MPEG_2, Layer.LAYER_3, new int[] { 8, 16, 24, 32, 40, 48, 56, 64, 80, 96, 112, 128, 144, 160 });

        builder.put(Version.MPEG_2_5, Layer.LAYER_1, new int[] { 32, 48, 56, 64, 80, 96, 112, 128, 144, 160, 176, 192, 224, 256 });
        builder.put(Version.MPEG_2_5, Layer.LAYER_2, new int[] { 8, 16, 24, 32, 40, 48, 56, 64, 80, 96, 112, 128, 144, 160 });
        builder.put(Version.MPEG_2_5, Layer.LAYER_3, new int[] { 8, 16, 24, 32, 40, 48, 56, 64, 80, 96, 112, 128, 144, 160 });

        bitrates = builder.build();
    }

    private Bitrate()
    {
    }

    /**
     * Obtains bitrate in kbps for the given parameters as present in the frame header.
     */
    public static int of(Version version, Layer layer, int index)
    {
        try
        {
            return bitrates.get(version, layer)[index - 1];
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            throw new UnsupportedOperationException("Invalid header (bitrate index)");
        }
    }
}
