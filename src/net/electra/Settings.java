package net.electra;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.math.BigInteger;

// some men just want to see the world burn
// in reality, some things are better left as a global state, and i feel this is one of them.
// if you don't agree then you can fu- go implement it differently
public class Settings
{
	@ConfigProperty(name = "host-port", description = "The port that the server will bind to.")
	public static int PORT = 43594;
	
	@ConfigProperty(name = "host-address", description = "The IP address the server will listen for connections on.")
	public static String ADDRESS = "0.0.0.0";
	
	@ConfigProperty(name = "cycle-rate", description = "The rate at which this server ticks.")
	public static int CYCLE_RATE = 600;
	
	@ConfigProperty(name = "connections-per-tick", description = "The maximum amount of connections that will be accepted at any given time (0 accepts as many as it can within 'accept-duration' time).")
	public static int MAX_NEW_CONNECTIONS = 0;
	
	@ConfigProperty(name = "accept-duration", description = "The maximum amount of time spent accepting connections to prevent resource hogging due to a lot of simultaneous connections.")
	public static int MAX_NEW_CONNECTION_TIME = 100;
	
	@ConfigProperty(name = "socket-timeout", description = "The maximum amount of time spent waiting for data from a client before timing them out.")
	public static int SOCKET_TIMEOUT = 10000;
	
	@ConfigProperty(name = "server-name", description = "The knwon name of the server being hosted.")
	public static String SERVER_NAME = "Electra";

	@ConfigProperty(name = "client-version", description = "The minimum required client revision number required to communicate with this server.")
	public static int MINIMUM_CLIENT_VERSION = 317;
	
	@ConfigProperty(name = "max-players", description = "The maximum amount of players this server can service.")
	public static int PLAYER_CAPACITY = 2000;
	
	@ConfigProperty(name = "data-cache-expire", description = "The amount of time exceeded since an Entity was last seen, in seconds, at which cached Entity data will be counted as 'expired' and be discarded.")
	public static int DATA_CACHE_EXPIRE = 60;
	
	@ConfigProperty(name = "client-buffer-size", description = "The maximum amount of data the client can receive in a single packet. This is used to prevent read errors in the client.")
	public static int CLIENT_BUFFER_SIZE = 5000; // no need to account for id and short length of the player update packet since it doesn't store them in the buffer
	
	@ConfigProperty(name = "game-cache-path", description = "The location of the cache on the local file system.")
	public static String GAME_CACHE_PATH = "./Client/bin/.electra_file_store_32/";

	public static final BigInteger PRIVATE_RSA_MODULUS = new BigInteger("108172373405425877450997899437663512684493355481918555388146121498017789105183911355218563280687972272899246858446379251130319783396375114126872366667929628111006417838977184384781806606324598094371481693694130104301621043707333604739514038561112409908405804810204666951233129646018734035516899880450280932571");
	public static final BigInteger PRIVATE_RSA_EXPONENT = new BigInteger("98564497158527422644670805237034260394391034315855905115561191256475430584328889794898022870585515849801660518470237932168060426710071201232896077756718867612363395051057961867335755029896879707960329325758626522910595155051891263683482277143395164730057232044604024350544471248509475021031582758238500070209");
	public static final BigInteger PUBLIC_RSA_MODULUS = new BigInteger("108172373405425877450997899437663512684493355481918555388146121498017789105183911355218563280687972272899246858446379251130319783396375114126872366667929628111006417838977184384781806606324598094371481693694130104301621043707333604739514038561112409908405804810204666951233129646018734035516899880450280932571");
	public static final BigInteger PUBLIC_RSA_EXPONENT = new BigInteger("65537"); // just keeping all the keys together
	public static final double SERVER_VERSION = 0.1; // i'll keep this here for now.
	
	public static void load(File config)
	{
		Field[] fields = Settings.class.getDeclaredFields();
		BufferedReader reader = null;
		BufferedWriter writer = null;
		
		try
		{
			config.createNewFile();
			
			if (config.exists())
			{
				try
				{
					reader = new BufferedReader(new FileReader(config));
					String line;
					
					while ((line = reader.readLine()) != null)
					{
						if (line.startsWith("//") || line.startsWith("\\\\") || !line.contains("="))
						{
							continue;
						}
						
						try
						{
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
						catch (Exception ex)
						{
							ex.printStackTrace();
						}
					}
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
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
				
				// now that we're done reading, write any settings the file is missing.
				// this is to keep the configuration file clean. this eliminates duplicate entries, outdated/missing fields, etc.
				
				try
				{
					writer = new BufferedWriter(new FileWriter(config));
					
					for (Field f : fields)
					{
						try
						{
							if (f.isAnnotationPresent(ConfigProperty.class))
							{
								ConfigProperty attr = f.getAnnotation(ConfigProperty.class);
								writer.write("// " + attr.description() + " \\\\");
								writer.newLine();
								writer.write(attr.name() + " = " + f.get(null));
								writer.newLine();
								writer.newLine();
							}
						}
						catch (Exception ex)
						{
							ex.printStackTrace();
						}
					}
					
					writer.flush();
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
				finally
				{
					try
					{
						writer.close();
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
			}
			else
			{
				System.out.println("Config file could not be created.");
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	@Retention(RetentionPolicy.RUNTIME)
	@interface ConfigProperty
	{
		String name();
		String description();
	}
}