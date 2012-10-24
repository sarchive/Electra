package net.electra.compiler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import net.electra.io.DataBuffer;
import net.electra.math.InfixNotation;
import net.electra.math.PostfixNotation;

import org.apache.bcel.Constants;
import org.apache.bcel.generic.ArrayType;
import org.apache.bcel.generic.BranchInstruction;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.FieldGen;
import org.apache.bcel.generic.IF_ICMPLT;
import org.apache.bcel.generic.IINC;
import org.apache.bcel.generic.INVOKESPECIAL;
import org.apache.bcel.generic.INVOKEVIRTUAL;
import org.apache.bcel.generic.InstructionConstants;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.LocalVariableGen;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.Type;
import org.yaml.snakeyaml.Yaml;

// this is an experimental feature but i plan on completing it. the effort vs reward of implementing this is probably unequal but i'm going to eventually finish it.
// just remember the key thing about private servers, it's all in good fun.
// this is a little bit better than what i made when i started programming, only by a little though.
// i'd rather it be a mess than not work at all and worry about design in something that's only run once in a while.
public class Compiler
{
	private static final Type[] types = new Type[] { Type.BYTE, Type.SHORT, Type.INT, Type.LONG, Type.BOOLEAN, Type.STRING };
	private static final HashMap<Type, TypeDescriptor> typeReferences = new HashMap<Type, TypeDescriptor>();
	
	static
	{
		typeReferences.put(Type.BYTE, new TypeDescriptor("get", "()B", "put", "(I)Lnet/electra/io/DataBuffer;", 1));
		typeReferences.put(Type.SHORT, new TypeDescriptor("getShort", "()S", "putShort", "(I)Lnet/electra/io/DataBuffer;", 2));
		typeReferences.put(Type.INT, new TypeDescriptor("getInt", "()I", "putInt", "(I)Lnet/electra/io/DataBuffer;", 4));
		typeReferences.put(Type.LONG, new TypeDescriptor("getLong", "()J", "putLong", "(J)Lnet/electra/io/DataBuffer;", 8));
		typeReferences.put(Type.BOOLEAN, new TypeDescriptor("getBoolean", "()Z", "putBoolean", "(Z)Lnet/electra/io/DataBuffer;", 1));
		typeReferences.put(Type.STRING, new TypeDescriptor("getString", "()Ljava/lang/String;", "putString", "(Ljava/lang/String;)Lnet/electra/io/DataBuffer;", -1));
	}
	
	public static final class TypeDescriptor
	{
		private final String readMethod;
		private final String readMethodSignature;
		private final String writeMethod;
		private final String writeMethodSignature;
		private final int length;
		
		public TypeDescriptor(String readMethod, String readMethodSignature, String writeMethod, String writeMethodSignature, int length)
		{
			this.length = length;
			this.readMethod = readMethod;
			this.readMethodSignature = readMethodSignature;
			this.writeMethod = writeMethod;
			this.writeMethodSignature = writeMethodSignature;
		}
		
		public int length()
		{
			return length;
		}
		
		public String readMethod()
		{
			return readMethod;
		}
		
		public String readMethodSignature()
		{
			return readMethodSignature;
		}
		
		public String writeMethod()
		{
			return writeMethod;
		}
		
		public String writeMethodSignature()
		{
			return writeMethodSignature;
		}
	}
	
	private static final List<Map<String, Object>> builtEventsYaml = new ArrayList<Map<String, Object>>();
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception
	{
		// temporary hardcoding etc.
		args = new String[] { "events.yml" };
		
		Yaml parser = new Yaml();
		Object object = parser.load(new FileInputStream(new File(args[0])));
		List<Map<String, Object>> file = (List<Map<String, Object>>)object;
		JarOutputStream jarStream = new JarOutputStream(new FileOutputStream("./lib/events.jar"), new Manifest());
		
		for (Map<String, Object> f : file)
		{
			EventContainer container = EventContainer.parse(f);
			
			for (Event event : container.events())
			{
				compile(event, jarStream);
			}
		}

		JarEntry builtEvents = new JarEntry("built-events.yml");
		jarStream.putNextEntry(builtEvents);
		jarStream.write(new Yaml().dump(builtEventsYaml).getBytes());
		jarStream.closeEntry();
		
		jarStream.flush();
		jarStream.close();

		System.out.println("done");
	}
	
	public static int calculateLength(Event event)
	{
		int length = 0;
		
		for (EventField field : event.fields())
		{
			int newLength = 0;
			Type test = field.type();
			boolean array = false;
			
			if (test instanceof ArrayType)
			{
				test = ((ArrayType)test).getBasicType();
				array = true;
			}
			
			if (typeReferences.containsKey(test))
			{
				newLength = typeReferences.get(test).length();
			}
			else
			{
				for (Event evt : event.events())
				{
					if (field.type().getSignature().equalsIgnoreCase("L" + evt.namespace().replace(".", "/") + ";"))
					{
						newLength = calculateLength(evt);
						break;
					}
				}
			}
			
			if (array)
			{
				PostfixNotation notation = new PostfixNotation(new InfixNotation(field.arrayExpression()));
				
				if (notation.hasVariables())
				{
					newLength = -1;
				}
				else
				{
					newLength *= (int)notation.evaluate(); // TODO: support for multi-dimensional arrays
				}
			}
			
			if (newLength == -1)
			{
				length = -1;
				break;
			}
			
			length += newLength;
		}
		
		return length;
	}
	
	private static final List<String> alreadyCompiled = new ArrayList<String>();
	
	public static void compile(Event event, JarOutputStream stream)
	{
		for (Event subEvent : event.events())
		{
			compile(subEvent, stream);
		}
		
		if (alreadyCompiled.contains(event.namespace()))
		{
			return;
		}
		
		alreadyCompiled.add(event.namespace());
		
		ClassGen clazz = new ClassGen(event.namespace().replace(".", "/"), "net/electra/net/events/NetworkEvent", event.name() + ".java", Constants.ACC_PUBLIC | Constants.ACC_FINAL, null);
		ConstantPoolGen constants = clazz.getConstantPool();
		InstructionFactory factory = new InstructionFactory(constants);

		String[] fieldNames = new String[event.fields().size()];
		Type[] fieldTypes = new Type[event.fields().size()];
		int i = 0;
		
		for (EventField field : event.fields())
		{
			// declare field
			FieldGen fieldGen = new FieldGen(Constants.ACC_PRIVATE, field.type(), field.name(), constants);
			clazz.addField(fieldGen.getField());
			fieldNames[i] = field.name();
			fieldTypes[i++] = field.type();
			
			// declare accessor
			InstructionList fieldAccessorInstructions = new InstructionList();
			MethodGen fieldAccessorGen = new MethodGen(Constants.ACC_PUBLIC, field.type(), Type.NO_ARGS, null, field.name(), clazz.getClassName(), fieldAccessorInstructions, constants);
			fieldAccessorInstructions.append(InstructionConstants.THIS);
			fieldAccessorInstructions.append(factory.createGetField(clazz.getClassName(), field.name(), field.type()));
			fieldAccessorInstructions.append(InstructionFactory.createReturn(field.type()));
			fieldAccessorGen.setMaxStack();
			fieldAccessorGen.setMaxLocals();
			clazz.addMethod(fieldAccessorGen.getMethod());
		}
		
		// declare no argument constructor (for parsing)
		clazz.addEmptyConstructor(Constants.ACC_PUBLIC);
		
		// declare argument-rich constructor (for building)
		if (event.fields().size() > 0)
		{
			InstructionList clist = new InstructionList();
			MethodGen ctor = new MethodGen(Constants.ACC_PUBLIC, Type.VOID, fieldTypes, fieldNames, "<init>", clazz.getClassName(), clist, constants);
			
			clist.append(InstructionConstants.THIS);
			clist.append(new INVOKESPECIAL(constants.addMethodref(clazz.getSuperclassName(), "<init>", "()V")));
			
			LocalVariableGen[] ctorVariables = ctor.getLocalVariables();
			
			for (int f = 1; f < ctorVariables.length; f++)
			{
				clist.append(InstructionConstants.THIS);
				clist.append(InstructionFactory.createLoad(ctorVariables[f].getType(), ctorVariables[f].getIndex()));
				clist.append(factory.createPutField(clazz.getClassName(), ctorVariables[f].getName(), ctorVariables[f].getType()));
			}
			
			clist.append(InstructionConstants.RETURN);
			ctor.setMaxStack();
			ctor.setMaxLocals();
			clazz.addMethod(ctor.getMethod());
		}
		
		// declare parse method
		InstructionList ilist = new InstructionList();
		Type dataBufferType = Type.getType(DataBuffer.class);
		MethodGen mgen = new MethodGen(Constants.ACC_PUBLIC, Type.VOID, new Type[] { dataBufferType }, new String[] { "buffer" }, "parse", clazz.getClassName(), ilist, constants);
		//int length = calculateLength(event);
		
		String method = "";
		String signature = "";
		LocalVariableGen iterator = null;
		int lastField = 0;
		
		for (EventField field : event.fields())
		{
			Type test = field.type();
			boolean array = false;
			
			if (test instanceof ArrayType)
			{
				test = ((ArrayType)test).getBasicType();
				array = true;
			}
			
			if (typeReferences.containsKey(test))
			{
				TypeDescriptor descriptor = typeReferences.get(test);
				method = descriptor.readMethod();
				signature = descriptor.readMethodSignature();

				// TODO: add the support for multi-dimensional arrays
				if (array)
				{
					// TODO: we need to resolve any potential variables, SIZE, REMAINING, and CURRENT.
					PostfixNotation notation = new PostfixNotation(new InfixNotation(field.arrayExpression()));
					ilist.append(InstructionConstants.THIS); // load this onto stack
					
					if (notation.hasVariables())
					{
						System.out.println("warning unimplemented variable support");
						@SuppressWarnings("unused")
						String[] names = notation.variableNames();
						
						for (int resolveField = 0; resolveField < lastField; resolveField++)
						{
							// TODO: complete this
						}
					}
					else
					{
						int arrayLength = (int)notation.evaluate(); // 4, 9
						ilist.append(factory.createConstant(arrayLength)); // set array size
					}
					
					ilist.append(factory.createNewArray(test, (short)1)); // create array
					ilist.append(factory.createPutField(clazz.getClassName(), field.name(), field.type())); // put created array into class field
					
					if (iterator == null)
					{
						iterator = mgen.addLocalVariable("i", Type.INT, null, null); // if the iterator hasn't been created yet, create it.
					}
					
					ilist.append(factory.createConstant(0)); // push i value
					InstructionHandle tempHandle = ilist.append(InstructionFactory.createStore(Type.INT, iterator.getIndex())); // store it in i
					
					// l70 (jump to equality)
					InstructionHandle gotoHandle = ilist.append(InstructionFactory.createBranchInstruction(Constants.GOTO, tempHandle)); // goto equality
					
					// l72 (loop body)
					InstructionHandle loopHandle = ilist.append(InstructionConstants.THIS); // load this onto stack (stack = 1)
					ilist.append(factory.createGetField(clazz.getClassName(), field.name(), field.type())); // load from class field (stack = 0)
					ilist.append(InstructionFactory.createLoad(Type.INT, iterator.getIndex())); // load i's value (stack = 1)
					ilist.append(InstructionFactory.createLoad(dataBufferType, 1)); // load buffer (stack = 2)
					ilist.append(new INVOKEVIRTUAL(constants.addMethodref("net/electra/io/DataBuffer", method, signature))); // parse (stack = 1)
					ilist.append(InstructionFactory.createArrayStore(((ArrayType)field.type()).getElementType())); // store value (stack = 0)
					ilist.append(new IINC(iterator.getIndex(), 1)); // i++ (old value of i remains on stack, i is incremented)
					
					// l71 (equality body)
					InstructionHandle equalityHandle = ilist.append(InstructionFactory.createLoad(Type.INT, iterator.getIndex()));
					ilist.append(InstructionConstants.THIS); // load this onto stack
					ilist.append(factory.createGetField(clazz.getClassName(), field.name(), field.type())); // get field (array)
					ilist.append(InstructionConstants.ARRAYLENGTH); // get length
					ilist.append(new IF_ICMPLT(loopHandle)); // if i is less than array length
					((BranchInstruction)gotoHandle.getInstruction()).updateTarget(tempHandle, equalityHandle);
				}
				else
				{
					ilist.append(InstructionConstants.THIS); // load this onto stack
					ilist.append(InstructionFactory.createLoad(dataBufferType, 1)); // load buffer onto stack to invoke methods
					ilist.append(new INVOKEVIRTUAL(constants.addMethodref("net/electra/io/DataBuffer", method, signature)));
					ilist.append(factory.createPutField(clazz.getClassName(), field.name(), test));
				}
			}
			else
			{
				// create new instance of custom type, parse it.
				// TODO: add support for arrays
				String sign = test.getSignature();
				ObjectType objType = new ObjectType(sign.substring(1, sign.length() - 1));
				ilist.append(InstructionConstants.THIS); // load this onto stack
				ilist.append(factory.createNew(objType));
				ilist.append(InstructionConstants.DUP);
				ilist.append(new INVOKESPECIAL(constants.addMethodref(objType.getClassName(), "<init>", "()V")));
				ilist.append(InstructionConstants.DUP_X1);
				ilist.append(factory.createPutField(clazz.getClassName(), field.name(), test));
				ilist.append(InstructionFactory.createLoad(dataBufferType, 1)); // load buffer onto stack to invoke methods
				ilist.append(new INVOKEVIRTUAL(constants.addMethodref(objType.getClassName(), "parse", "(Lnet/electra/io/DataBuffer;)V")));
			}
			
			lastField++;
		}
		
		ilist.append(InstructionConstants.RETURN); // return nothing (it's a void)
		mgen.setMaxStack();
		mgen.setMaxLocals();
		clazz.addMethod(mgen.getMethod());
		
		// create length accessor
		InstructionList llist = new InstructionList();
		MethodGen lgen = new MethodGen(Constants.ACC_PUBLIC, Type.INT, null, null, "length", clazz.getClassName(), llist, constants);
		llist.append(factory.createConstant(calculateLength(event)));
		llist.append(InstructionConstants.IRETURN);
		lgen.setMaxStack();
		lgen.setMaxLocals();
		clazz.addMethod(lgen.getMethod());
		
		// create id accessor
		InstructionList dlist = new InstructionList();
		MethodGen dgen = new MethodGen(Constants.ACC_PUBLIC, Type.INT, null, null, "id", clazz.getClassName(), dlist, constants);
		dlist.append(factory.createConstant(event.operator()));
		dlist.append(InstructionConstants.IRETURN);
		dgen.setMaxStack();
		dgen.setMaxLocals();
		clazz.addMethod(dgen.getMethod());
		
		// declare parse method
		InstructionList blist = new InstructionList();
		MethodGen bgen = new MethodGen(Constants.ACC_PUBLIC, Type.VOID, new Type[] { dataBufferType }, new String[] { "buffer" }, "build", clazz.getClassName(), blist, constants);
		
		method = "";
		signature = "";
		iterator = null;
		lastField = 0;
		
		// TODO: implement something to chain the put methods without popping top of stack (savin' bytecodeZZZZZZZZZZZZZZZZZZZZZZZZ 'n' stuff)
		
		for (EventField field : event.fields())
		{
			Type test = field.type();
			boolean array = false;
			
			if (test instanceof ArrayType)
			{
				test = ((ArrayType)test).getBasicType();
				array = true;
			}
			
			if (typeReferences.containsKey(test))
			{
				TypeDescriptor descriptor = typeReferences.get(test);
				method = descriptor.writeMethod();
				signature = descriptor.writeMethodSignature();

				// TODO: add the support for multi-dimensional arrays
				if (array)
				{
					if (iterator == null)
					{
						iterator = bgen.addLocalVariable("i", Type.INT, null, null); // if the iterator hasn't been created yet, create it.
					}
					
					blist.append(factory.createConstant(0)); // push i value
					InstructionHandle tempHandle = blist.append(InstructionFactory.createStore(Type.INT, iterator.getIndex())); // store it in i

					// l70 (jump to equality)
					InstructionHandle gotoHandle = blist.append(InstructionFactory.createBranchInstruction(Constants.GOTO, tempHandle)); // goto equality
					
					// l72 (loop body)
					InstructionHandle loopHandle = blist.append(InstructionFactory.createLoad(dataBufferType, 1)); // load buffer onto stack to invoke methods
					blist.append(InstructionConstants.THIS); // load this onto stack;
					blist.append(factory.createGetField(clazz.getClassName(), field.name(), field.type())); // load from class field
					blist.append(InstructionFactory.createLoad(Type.INT, iterator.getIndex())); // load i's value
					blist.append(InstructionFactory.createArrayLoad(((ArrayType)field.type()).getElementType()));
					blist.append(new INVOKEVIRTUAL(constants.addMethodref("net/electra/io/DataBuffer", method, signature)));
					blist.append(InstructionConstants.POP);
					blist.append(new IINC(iterator.getIndex(), 1)); // i++ (old value of i remains on stack, i is incremented)
					
					// l71 (equality body)
					InstructionHandle equalityHandle = blist.append(InstructionFactory.createLoad(Type.INT, iterator.getIndex()));
					blist.append(InstructionConstants.THIS); // load this onto stack
					blist.append(factory.createGetField(clazz.getClassName(), field.name(), field.type())); // get field (array)
					blist.append(InstructionConstants.ARRAYLENGTH); // get length
					blist.append(new IF_ICMPLT(loopHandle)); // if i is less than array length
					((BranchInstruction)gotoHandle.getInstruction()).updateTarget(tempHandle, equalityHandle);
				}
				else
				{
					blist.append(InstructionFactory.createLoad(dataBufferType, 1)); // load buffer onto stack to invoke methods
					blist.append(InstructionConstants.THIS); // load this onto stack
					blist.append(factory.createGetField(clazz.getClassName(), field.name(), field.type()));
					blist.append(new INVOKEVIRTUAL(constants.addMethodref("net/electra/io/DataBuffer", method, signature)));
					blist.append(InstructionConstants.POP);
				}
			}
			else
			{
				// write custom type
				// TODO: add support for arrays
				String sign = test.getSignature();
				ObjectType objType = new ObjectType(sign.substring(1, sign.length() - 1));
				blist.append(InstructionConstants.THIS); // load this onto stack
				blist.append(factory.createGetField(clazz.getClassName(), field.name(), field.type()));
				blist.append(InstructionFactory.createLoad(dataBufferType, 1)); // load buffer onto stack to invoke methods
				blist.append(new INVOKEVIRTUAL(constants.addMethodref(objType.getClassName(), "build", "(Lnet/electra/io/DataBuffer;)V")));
			}
			
			lastField++;
		}
		
		blist.append(InstructionConstants.RETURN);
		bgen.setMaxStack();
		bgen.setMaxLocals();
		clazz.addMethod(bgen.getMethod());
		
		try
		{
			JarEntry toAdd = new JarEntry(clazz.getClassName() + ".class");
			stream.putNextEntry(toAdd);
			clazz.getJavaClass().dump(stream);
			stream.closeEntry();
			
			if (event.inbound())
			{
				Map<String, Object> yml = new HashMap<String, Object>();
				yml.put("namespace", event.namespace().replace("." + event.name(), ""));
				yml.put("operator", event.operator());
				yml.put("name", event.name());
				yml.put("length", calculateLength(event));
				builtEventsYaml.add(yml);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static class EventContainer
	{
		private final ArrayList<Event> events = new ArrayList<Event>();
		private final String namespace;
		
		public EventContainer(String namespace)
		{
			this.namespace = namespace;
		}
		
		@SuppressWarnings("unchecked")
		public static EventContainer parse(Map<String, Object> doc)
		{
			EventContainer container = new EventContainer((String)doc.get("namespace") + ".events");
			List<Map<String, Object>> events = (List<Map<String, Object>>)doc.get("events");
			
			if (events != null)
			{
				for (Map<String, Object> eventMap : events)
				{
					container.events().add(Event.parse(container, eventMap, false));
				}
			}
			
			return container;
		}
		
		public String namespace()
		{
			return namespace;
		}
		
		public ArrayList<Event> events()
		{
			return events;
		}
	}
	
	public static class Event extends EventContainer
	{
		private final ArrayList<EventField> fields = new ArrayList<EventField>();
		private final boolean inbound;
		private final int operator;
		private final String name;
		
		public Event(EventContainer container, int operator, String name, boolean inbound)
		{
			super(container.namespace() + "." + name);
			this.operator = operator;
			this.inbound = inbound;
			this.name = name;
		}
		
		@SuppressWarnings("unchecked")
		public static Event parse(EventContainer container, Map<String, Object> doc, boolean subEvent)
		{
			if (subEvent || !doc.containsKey("op"))
			{
				doc.put("op", -1);
			}
			
			boolean inbound = false;
			
			if (doc.containsKey("inbound") && (boolean)doc.get("inbound"))
			{
				inbound = true;
			}
			
			Event event = new Event(container, (int)doc.get("op"), (String)doc.get("name"), inbound);
			
			if (doc.containsKey("payload"))
			{
				List<Map<String, String>> payload = (List<Map<String, String>>)doc.get("payload");
				
				for (Map<String, String> payloadMap : payload)
				{
					event.fields().add(EventField.parse(container, payloadMap));
				}
			}
			
			if (doc.containsKey("subevents"))
			{
				List<Map<String, Object>> events = (List<Map<String, Object>>)doc.get("subevents");
				
				for (Map<String, Object> eventMap : events)
				{
					event.events().add(Event.parse(container, eventMap, true));
				}
			}
			
			return event;
		}
		
		public boolean inbound()
		{
			return inbound;
		}
		
		public String name()
		{
			return name;
		}
		
		public int operator()
		{
			return operator;
		}
		
		public ArrayList<EventField> fields()
		{
			return fields;
		}
	}
	
	public static class EventField
	{
		private final String arrayExpression;
		private final String name;
		private final Type type;
		
		public EventField(String name, Type type)
		{
			this(name, type, null);
		}
		
		public EventField(String name, Type type, String arrayExpression)
		{
			this.type = type;
			this.name = name;
			this.arrayExpression = arrayExpression;
		}
		
		public static EventField parse(EventContainer container, Map<String, String> doc)
		{
			String varName = doc.keySet().toArray(new String[0])[0];
			String namePart = doc.get(varName);
			String arrayPart = null;
			Type type = null;
			
			if (namePart.contains("["))
			{
				arrayPart = namePart.substring(namePart.indexOf("[") + 1, namePart.length() - 1);
				namePart = namePart.substring(0, namePart.indexOf("["));
			}
			
			if (namePart.equalsIgnoreCase("string"))
			{
				type = Type.STRING;
			}
			
			for (int i = 0; i < types.length; i++)
			{
				if (types[i].toString().equalsIgnoreCase(namePart))
				{
					type = types[i];
					break;
				}
			}
			
			if (type == null)
			{
				type = Type.getType("L" + (container.namespace() + "." + namePart).replace(".", "/") + ";");
			}
			
			if (arrayPart != null)
			{
				type = new ArrayType(type, 1);
			}
			
			return new EventField(varName, type, arrayPart);
		}
		
		public String arrayExpression()
		{
			return arrayExpression;
		}
		
		public String name()
		{
			return name;
		}
		
		public Type type()
		{
			return type;
		}
	}
}
