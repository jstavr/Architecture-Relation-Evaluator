package edu.uci.isr.archstudio4.comp.xarchcs.changesetadt.resolvers;

import edu.uci.isr.archstudio4.comp.xarchcs.changesetadt.IObjRefResolver;
import edu.uci.isr.xarchflat.ObjRef;
import edu.uci.isr.xarchflat.XArchFlatQueryInterface;

public abstract class AbstractResolver implements IObjRefResolver {

	protected static boolean equals(Object o1, Object o2) {
		return o1 == null ? o2 == null : o1.equals(o2);
	}

	private final String referencePrefix;

	public AbstractResolver(String referencePrefix) {
		this.referencePrefix = referencePrefix;
	}

	public boolean canResolve(String reference) {
		if (reference != null && reference.startsWith(referencePrefix)) {
			return true;
		}
		return false;
	}

	public String getReference(XArchFlatQueryInterface xArch, ObjRef objRef) {
		return encode(getReferenceData(xArch, objRef));
	}

	public abstract boolean canResolve(XArchFlatQueryInterface xArch, ObjRef objRef);

	public ObjRef resolveObjRef(XArchFlatQueryInterface xArch, String reference, ObjRef parentRef, ObjRef[] childRefs) {
		for (ObjRef childRef : childRefs) {
			if (!canResolve(xArch, childRef)) {
				continue;
			}
			String childReference = getReference(xArch, childRef);
			if (equals(reference, childReference)) {
				return childRef;
			}
		}
		return null;
	}

	protected abstract String getReferenceData(XArchFlatQueryInterface xArch, ObjRef objRef);

	protected String encode(String referenceData) {
		if (referenceData == null) {
			return null; // referencePrefix + "\\";
		}

		StringBuffer sb = new StringBuffer(referencePrefix);
		for (int i = 0; i < referenceData.length(); i++) {
			char ch = referenceData.charAt(i);
			switch (ch) {
			case '\\':
				sb.append("\\\\");
				break;
			case '/':
				sb.append("\\/");
				break;
			default:
				sb.append(ch);
				break;
			}
		}
		return sb.toString();
	}

	protected String decode(String referenceData) {
		if (!referenceData.startsWith(referencePrefix)) {
			throw new IllegalArgumentException("reference data <" + referenceData + "> does not start with required prefix <" + referencePrefix + ">");
		}

		referenceData = referenceData.substring(referencePrefix.length());
		if (referenceData.equals("\\")) {
			return null;
		}

		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < referenceData.length(); i++) {
			char ch = referenceData.charAt(i);
			if (ch == '\\') {
				ch = referenceData.charAt(++i);
				switch (ch) {
				case '/':
				case '\\':
				default:
					sb.append(ch);
				}
			}
			else {
				sb.append(ch);
			}
		}
		return sb.toString();
	}

	public boolean isOldReference(XArchFlatQueryInterface xarch, ObjRef objRef, String oldReference) {
		return false;
	}
}
