package net.electra.io.fs;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

import net.electra.io.DataBuffer;

public class ArchiveFileEntry
{
	private final ArchiveFileDescriptor descriptor;
	private final DataBuffer buffer;
	
	public ArchiveFileEntry(ArchiveFileDescriptor descriptor, DataBuffer buffer) throws IOException
	{
		this.descriptor = descriptor;
		
		if (descriptor.compressed())
		{
			BZip2CompressorInputStream decompressor = new BZip2CompressorInputStream(new ByteArrayInputStream(buffer.get(descriptor.compressedSize())));
			buffer = new DataBuffer();
			int b = -1;
			
			while ((b = decompressor.read()) != -1)
			{
				this.buffer.put(b);
			}
			
			decompressor.close();
			buffer.rewind();
		}

		this.buffer = buffer;
	}
	
	public DataBuffer buffer()
	{
		return buffer;
	}
	
	public ArchiveFileDescriptor descriptor()
	{
		return descriptor;
	}
}
