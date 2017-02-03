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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

/**
 * Iterates through the frames in a MP3.
 */
public class Mp3FrameIterator implements Iterator<byte[]>, AutoCloseable
{
    private final BufferedInputStream bis;
    byte[] headerBytes = new byte[4];
    byte[] currentFrame = null;

    public Mp3FrameIterator(InputStream is)
    {
        bis = new BufferedInputStream(is);

        try
        {
            bis.read(new byte[getId3Size()]); // ignore ID3 header
            currentFrame = nextFrame();
        }
        catch (IOException e)
        {
            throw new UnsupportedOperationException("Can't parse the provided stream.", e);
        }
    }

    // 10 bytes header { [ I ] [ D ] [ 3 ] [ major ] [ revision ] [ flags ] 4 * [ size ] }
    private int getId3Size() throws IOException
    {
        try
        {
            byte[] bb = new byte[10];

            bis.mark(10);
            bis.read(bb);

            if (bb[0] == 'I' && bb[1] == 'D' && bb[2] == '3')
            {
                // obtain footer flag within flags byte (xxxfxxxx)
                boolean footer = (bb[5] & (1 << 5)) != 0;

                // handle synch safe integers (0bbbbbbbb)
                int size = (bb[6] << 21) + (bb[7] << 14) + (bb[8] << 7) + bb[9];

                return footer ? size + 20 : size + 10;
            }
            return 0;
        }
        finally
        {
            bis.reset();
        }
    }

    // Parsing as defined in http://mpgedit.org/mpgedit/mpeg_format/mpeghdr.htm
    private byte[] nextFrame() throws IOException
    {
        while (bis.read(headerBytes) != -1)
        {
            final int header = (headerBytes[0] << 24 & 0xFF000000) | (headerBytes[1] << 16 & 0x00FF0000) | (headerBytes[2] << 8 & 0x0000FF00) | headerBytes[3];

            if (0x7FF == (header >>> 21)) // frame sync
            {
                final Version version = Version.of(header >>> 19 & 0x3);
                final Layer layer = Layer.of(header >>> 17 & 0x3);
                final int bitrate_bps = Bitrate.of(version, layer, header >>> 12 & 0xF) * 1000;
                final int samplerate_hz = SampleRate.of(version, header >>> 10 & 0x3);
                final int padding = header >>> 9 & 0x1;

                final int frameLength;
                switch (layer)
                {
                    case LAYER_1:
                        frameLength = 48 * bitrate_bps / samplerate_hz + 4 * padding;
                        break;
                    case LAYER_2:
                    case LAYER_3:
                        frameLength = 144 * bitrate_bps / samplerate_hz + padding;
                        break;
                    default:
                        frameLength = 0;
                }

                byte[] nextFrame = new byte[frameLength];
                System.arraycopy(headerBytes, 0, nextFrame, 0, headerBytes.length);
                bis.read(nextFrame, 4, frameLength - headerBytes.length);

                return nextFrame;
            }
        }
        return null;
    }
    
    @Override
    public boolean hasNext()
    {
        return currentFrame != null;
    }

    @Override
    public byte[] next()
    {
        try
        {
            byte[] current = currentFrame;
            byte[] next = nextFrame();
            currentFrame = next;
            return current;
        }
        catch (IOException e)
        {
            throw new UnsupportedOperationException("Can't parse the provided stream.", e);
        }
    }

    @Override
    public void close() throws Exception
    {
        bis.close();
    }
}
