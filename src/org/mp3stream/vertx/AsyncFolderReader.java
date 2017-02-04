package org.mp3stream.vertx;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.ReadStream;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Iterator;

/**
 * This class allows streaming the contents of a folder to a pump asynchronously.
 */
public class AsyncFolderReader implements ReadStream<Buffer>
{
    private static final int BUFFER_SIZE = 8192;

    private Handler<Throwable> exceptionHandler;
    private Handler<Buffer> dataHandler;
    private Handler<Void> endHandler;

    private volatile boolean paused = false;
    private volatile boolean closed = false;

    private AsynchronousFileChannel fileChannel = null;

    // position within the path
    private volatile long pos;

    private Iterator<File> contents;

    public AsyncFolderReader(File folder)
    {
        assert folder.isDirectory() : "Folder must be a directory";

        final File[] files = folder.listFiles();
        if (files.length > 0)
        {
            contents = Arrays.asList(files).iterator();
            try
            {
                fileChannel = AsynchronousFileChannel.open(Paths.get(contents.next().getAbsolutePath()), StandardOpenOption.READ);
            }
            catch (IOException e)
            {
                throw new RuntimeException("Unable to read the file.", e);
            }
        }
        else
            closed = true;
    }

    @Override
    public ReadStream<Buffer> exceptionHandler(Handler<Throwable> handler)
    {
        exceptionHandler = handler;
        return this;
    }

    @Override
    public synchronized ReadStream<Buffer> handler(Handler<Buffer> handler)
    {
        dataHandler = handler;
        if (dataHandler != null && !paused && !closed)
        {
            read();
        }
        return this;
    }

    private synchronized void read()
    {
        final ByteBuffer bb = ByteBuffer.allocate(BUFFER_SIZE);
        fileChannel.read(bb, pos, bb, new CompletionHandler<Integer, ByteBuffer>()
        {
            @Override
            public void completed(Integer result, ByteBuffer attachment)
            {
                if (result != -1)
                {
                    pos += result;
                    final Buffer buffer = Buffer.buffer();
                    buffer.appendBytes(attachment.array(), 0, result);
                    dataHandler.handle(buffer);
                    if (dataHandler != null && !paused && !closed) read();
                }
                else
                {
                    if (contents.hasNext())
                    {
                        try
                        {
                            fileChannel.close();
                            fileChannel = AsynchronousFileChannel.open(Paths.get(contents.next().getAbsolutePath()), StandardOpenOption.READ);
                            pos = 0;
                            read();
                        }
                        catch (IOException e)
                        {
                            throw new RuntimeException("Unable to read the file.", e);
                        }
                    }
                    else
                    {
                        closed = true;
                        if (endHandler != null) endHandler.handle(null);
                    }
                }
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment)
            {
                exceptionHandler.handle(exc);
            }
        });
     }

    @Override
    public synchronized ReadStream<Buffer> pause()
    {
        paused = true;
        return this;
    }

    @Override
    public synchronized ReadStream<Buffer> resume()
    {
        if (paused)
        {
            paused = false;
            if (dataHandler != null && !closed) read();
        }
        return this;
    }

    @Override
    public ReadStream<Buffer> endHandler(Handler<Void> endHandler)
    {
        this.endHandler = endHandler;
        return this;
    }
}
