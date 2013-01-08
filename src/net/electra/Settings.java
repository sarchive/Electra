package net.electra;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.util.Date;

public final class Settings
{
	@ConfigProperty(name = "fs_bindaddr")
    public static InetSocketAddress FILESERVER_BIND_ADDRESS = new InetSocketAddress("127.0.0.1", 43590);
    @ConfigProperty(name = "fs_ondemandblocksize")
    public static int FILESERVER_ONDEMAND_BLOCK_SIZE = 500;
    @ConfigProperty(name = "fs_cachepath")
    public static File FILESERVER_CACHE_PATH = new File("./.electra_file_store_32/");

	@ConfigProperty(name = "gs_bindaddr")
	public static InetSocketAddress GAMESERVER_BIND_ADDRESS = new InetSocketAddress("127.0.0.1", 43594);
    @ConfigProperty(name = "gs_cyclerate")
    public static int GAMESERVER_CYCLE_RATE = 600;
    
    @ConfigProperty(name = "cl_version")
	public static int CLIENT_VERSION = 317;
    @ConfigProperty(name = "cl_timeout")
    public static int CLIENT_TIMEOUT = 10000;
    @ConfigProperty(name = "cl_cachetime")
    public static int CLIENT_CACHE_TIME = 30;
    @ConfigProperty(name = "cl_buffersize")
    public static int CLIENT_BUFFER_SIZE = 4096;

    @ConfigProperty(name = "sv_name")
	public static String SERVER_NAME = "Electra";
    public static final double SERVER_VERSION = 0.3; // i'll keep this here for now.
    
    public final static void info()
    {
		Runtime runtime = Runtime.getRuntime();
    	System.out.println(Settings.SERVER_NAME + " ----------------------------------------------");
		System.out.println("Server started:     " + new Date());
		//System.out.println("Build date:         " + new Date(new File(getClass().getClassLoader().getResource(getClass().getCanonicalName().replace('.', '/') + ".class").toURI()).lastModified()));
		System.out.println("Client version:     " + Settings.CLIENT_VERSION);
		System.out.println("Server version:     " + Settings.SERVER_VERSION);
		System.out.println("Memory used:        " + (runtime.totalMemory() - runtime.freeMemory()) + " bytes");
		System.out.println("Memory (total/max): " + runtime.totalMemory() + "/" + runtime.maxMemory() + " bytes");
		System.out.println("---------------------------------------------- " + Settings.SERVER_NAME);
    }

	public final static void dump() throws IllegalArgumentException, IllegalAccessException
	{
		Field[] fields = Settings.class.getDeclaredFields();
		
		for (Field f : fields)
		{
			if (f.isAnnotationPresent(ConfigProperty.class))
			{
				ConfigProperty attr = f.getAnnotation(ConfigProperty.class);
				System.out.println(attr.name() + ": " + f.get(null));
			}
		}
	}
	
	public final static void load(File config) throws IOException, NumberFormatException, IllegalArgumentException, IllegalAccessException
	{
		Field[] fields = Settings.class.getDeclaredFields();
		BufferedReader reader = null;
		
		try
		{
    		config.createNewFile();
    		
    		if (config.exists())
    		{
    			reader = new BufferedReader(new FileReader(config));
    			String line;
				
				while ((line = reader.readLine()) != null)
				{
					if (line.startsWith("#") || !line.contains("="))
					{
						continue;
					}
					
					String[] split = line.split("=");
					String name = split[0].trim();
					
					if (split.length == 2)
					{
						Field field = null;
						
						for (Field f : fields)
						{
							if (f.isAnnotationPresent(ConfigProperty.class))
							{
								ConfigProperty attr = f.getAnnotation(ConfigProperty.class);
								
								if (attr.name().equalsIgnoreCase(name))
								{
									field = f;
									break;
								}
							}
						}
						
						//Field field = getFieldFor(split[0].trim());
						String value = split[1].trim();
						
						if (field != null)
						{
							Object inst = field.get(null);
							
							if (inst instanceof Integer)
							{
								field.setInt(null, Integer.parseInt(value));
							}
							else if (inst instanceof Long)
							{
								field.setLong(null, Long.parseLong(value));
							}
							else if (inst instanceof Boolean)
							{
								field.setBoolean(null, Boolean.parseBoolean(value));
							}
							else if (inst instanceof Double)
							{
								field.setDouble(null, Double.parseDouble(value));
							}
							else if (inst instanceof Float)
							{
								field.setDouble(null, Float.parseFloat(value));
							}
							else if (inst instanceof File)
							{
								field.set(null, new File(value));
							}
							else if (inst instanceof InetSocketAddress)
							{
								String[] values = value.split(":");
								field.set(null, new InetSocketAddress(values[0], Integer.parseInt(values[1])));
							}
							else
							{
								field.set(null, value);
							}	
						}
						else
						{
							System.out.println("Unmatched key: " + name + ", value: " + value);
						}
					}
					else
					{
						System.out.println("Unexpected line " + line);
					}
				}
    		}
			else
			{
				System.err.println("Config file could not be created.");
			}
		}
		finally
		{
			try
			{
				reader.close();
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	public @interface ConfigProperty
	{
		String name();
	}
}
