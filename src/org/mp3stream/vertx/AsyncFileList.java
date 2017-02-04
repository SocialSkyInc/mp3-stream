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
package org.mp3stream.vertx;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.AsyncFile;

import java.util.List;

/**
 * Allow processing a sequence of AsyncFiles in a Pump.
 */
public class AsyncFileList implements AsyncFile
{
    private AsyncFile onUse;
    private final List<AsyncFile> files;
    
    private Handler<Buffer> dataHandler;
    private Handler<Void> endHandler;
    
    public AsyncFileList(List<AsyncFile> files)
    {
        this.files = files;
        
        for (AsyncFile file : files)
            file.endHandler(processNext());
        
        assert !files.isEmpty() : "No files to process"; 
        onUse = files.remove(0);
    }
    
    private Handler<Void> processNext()
    {
        return handler -> 
        {
            if (!files.isEmpty())
            {
                onUse = files.remove(0);
                onUse.handler(dataHandler);
            }
            else
            {
                if (endHandler != null)
                {
                    endHandler.handle(null);
                }
            }
        };
    }
    
    @Override
    public boolean writeQueueFull()
    {
        return onUse.writeQueueFull();
    }

    @Override
    public AsyncFile handler(Handler<Buffer> handler)
    {
        dataHandler = handler;
        return onUse.handler(handler);
    }

    @Override
    public AsyncFile pause()
    {
        return onUse.pause();
    }

    @Override
    public AsyncFile resume()
    {
        return onUse.resume();
    }

    @Override
    public AsyncFile endHandler(Handler<Void> endHandler)
    {
        return this;
    }

    @Override
    public AsyncFile write(Buffer data)
    {
        return onUse.write(data);
    }

    @Override
    public AsyncFile setWriteQueueMaxSize(int maxSize)
    {
        return onUse.setWriteQueueMaxSize(maxSize);
    }

    @Override
    public AsyncFile drainHandler(Handler<Void> handler)
    {
        return onUse.drainHandler(handler);
    }

    @Override
    public AsyncFile exceptionHandler(Handler<Throwable> handler)
    {
        return this;
    }

    @Override
    public void end()
    {
        onUse.end();
    }

    @Override
    public void close()
    {
        onUse.close();
    }

    @Override
    public void close(Handler<AsyncResult<Void>> handler)
    {
        onUse.close(handler);
    }

    @Override
    public AsyncFile write(Buffer buffer, long position, Handler<AsyncResult<Void>> handler)
    {
        return onUse.write(buffer, position, handler);
    }

    @Override
    public AsyncFile read(Buffer buffer, int offset, long position, int length, Handler<AsyncResult<Buffer>> handler)
    {
        return onUse.read(buffer, offset, position, length, handler);
    }

    @Override
    public AsyncFile flush()
    {
        return onUse.flush();
    }

    @Override
    public AsyncFile flush(Handler<AsyncResult<Void>> handler)
    {
        return onUse.flush(handler);
    }

    @Override
    public AsyncFile setReadPos(long readPos)
    {
        return onUse.setReadPos(readPos);
    }

    @Override
    public AsyncFile setWritePos(long writePos)
    {
        return onUse.setWritePos(writePos);
    }

    @Override
    public AsyncFile setReadBufferSize(int readBufferSize)
    {
        return onUse.setReadBufferSize(readBufferSize);
    }

}
