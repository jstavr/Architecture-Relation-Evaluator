package edu.uci.isr.archstudio4.comp.archipelago.generic.logics.mapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.uci.isr.bna4.BNAUtils;
import edu.uci.isr.bna4.IThing;
import edu.uci.isr.bna4.assemblies.AssemblyUtils;
import edu.uci.isr.bna4.assemblies.IAssembly;

public class BNAPath{

	private static final String[] getPartNames(IThing thing){
		List<String> partList = new ArrayList<String>();
		do{
			String part = AssemblyUtils.getPartName(thing);
			if(part == null){
				break;
			}
			partList.add(part);
			IAssembly assembly = AssemblyUtils.getAssemblyWithPart(thing);
			thing = assembly != null ? assembly.getRootThing() : null;
		} while(thing != null);
		return partList.toArray(new String[partList.size()]);
	}

	private final String[] partNames;
	private final int partNameOffset;

	private final int pathLength;

	private final Object propertyName;

	private final int hashCode;

	public BNAPath(String[] partNames, int partNameOffset, int pathLength, Object propertyName){
		this.partNames = partNames;
		this.partNameOffset = partNameOffset;
		this.pathLength = pathLength;
		this.propertyName = propertyName;
		this.hashCode = Arrays.asList(partNames).subList(partNameOffset, partNameOffset + pathLength).hashCode() ^ propertyName.hashCode();
	}

	@Override
	public int hashCode(){
		return hashCode;
	}

	@Override
	public boolean equals(Object obj){
		if(obj == this){
			return true;
		}
		if(obj instanceof BNAPath){
			BNAPath o = (BNAPath)obj;
			if(pathLength == o.pathLength && propertyName.equals(o.propertyName)){
				return Arrays.asList(partNames).subList(partNameOffset, partNameOffset + pathLength).equals(Arrays.asList(o.partNames).subList(o.partNameOffset, o.partNameOffset + o.pathLength));
			}
		}
		return false;
	}

	public BNAPath(String[] partNames, Object propertyName){
		this(partNames, 0, partNames.length, propertyName);
	}

	public BNAPath(IThing thing, Object propertyName){
		this(getPartNames(thing), propertyName);
	}

	public int getLength(){
		return pathLength;
	}

	public String getPartName(int index){
		if(index < 0 || index >= pathLength){
			throw new IndexOutOfBoundsException();
		}
		return partNames[partNameOffset + index];
	}

	public Object getPropertyName(){
		return propertyName;
	}

	public boolean hasPropertyPartNames(Object propertyName, String... partNames){
		return BNAUtils.nulleq(this.propertyName, propertyName) && hasPartNames(partNames);
	}

	public boolean hasPartNames(String... partNames){
		if(partNames.length == pathLength){
			int iP = partNameOffset;
			int iPP = 0;
			int c = pathLength;
			while(c-- > 0){
				String partName = partNames[iP++];
				String partsPropertyName = partNames[iPP++];
				if(!partName.equals(partsPropertyName)){
					return false;
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public String toString(){
		return "" + Arrays.asList(partNames) + ":" + propertyName;
	}
}
