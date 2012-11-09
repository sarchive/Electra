package net.electra.io.fs;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import net.electra.io.DataBuffer;

public class Cache implements Closeable
{
	public static final int DATA_BLOCK_SIZE = 512;
	public static final int DATA_HEADER_SIZE = 8;
	public static final int DATA_SIZE = DATA_HEADER_SIZE + DATA_BLOCK_SIZE;

	private final RandomAccessFile dataFile;
	private final CacheIndex[] indices;
	
	public Cache(File directory) throws IOException
	{
		System.out.println("Loading cache from " + directory.getPath());
		File cacheFile = new File(directory.getPath() + "/main_file_cache.dat");
		this.dataFile = new RandomAccessFile(cacheFile, "r");
		System.out.println("Cache found: " + cacheFile.getPath());
		ArrayList<CacheIndex> temp = new ArrayList<CacheIndex>();
		int totalEntries = 0;
		
		for (int i = 0; i < 255; i ++)
		{
			File indexFile = new File(directory.getPath() + "/main_file_cache.idx" + i);
			
			if (indexFile.exists())
			{
				CacheIndex idx = new CacheIndex(i);
				FileInputStream fis = new FileInputStream(indexFile);
				byte[] data = new byte[fis.available()];
				fis.read(data);
				fis.close();
				idx.build(new DataBuffer(data));
				temp.add(idx);
				System.out.println("\tIndex " + idx.id() + " found and built with " + idx.size() + " entries");
				totalEntries += idx.size();
			}
		}
		
		System.out.println("Total files indexed in cache: " + totalEntries);
		this.indices = temp.toArray(new CacheIndex[0]);
	}
	
	public CacheIndex index(int index)
	{
		return indices[index];
	}
	
	public CacheFile get(int index, int identifier) throws IOException
	{
		return get(index(index).get(identifier));
	}
	
	public CacheFile get(CacheFileDescriptor descriptor) throws IOException
	{
		int expectedIndexID = descriptor.index().id() + 1;
		DataBuffer fileBuffer = new DataBuffer();
		int currentBlockID = descriptor.startBlock();
		int remaining = descriptor.size();
		int nextPartID = 0;
		
		while (remaining > 0)
		{
			dataFile.seek(currentBlockID * DATA_SIZE);
			byte[] tempData = new byte[DATA_HEADER_SIZE];
			dataFile.read(tempData);
			DataBuffer tempBuffer = new DataBuffer(tempData);
			int currentFileID = tempBuffer.getShort();
			int currentPartID = tempBuffer.getShort();
			int nextBlockID = tempBuffer.getTribyte();
			int nextIndexID = tempBuffer.get();
			
			if (currentFileID != descriptor.identifier())
			{
				throw new IOException("Different file ID, index and data appear to be corrupt.");
			}
			else if (currentPartID != nextPartID)
			{
				throw new IOException("Block ID out of order or wrong file being accessed.");
			}
			else if (nextIndexID != expectedIndexID)
			{
				throw new IOException("Wrong index ID, must be a different type of file.");
			}
			
			byte[] block = new byte[remaining > DATA_BLOCK_SIZE ? DATA_BLOCK_SIZE : remaining];
			dataFile.read(block);
			fileBuffer.put(block);
			remaining -= block.length;
			currentBlockID = nextBlockID;
			nextPartID++;
		}
		
		return new CacheFile(descriptor, fileBuffer);
	}
	
	public long dataLength() throws IOException
	{
		return dataFile.length();
	}
	
	public int indexLength()
	{
		return indices.length;
	}
	
	@Override
	public void close() throws IOException
	{
		dataFile.close();
	}
}
