package edu.uci.isr.archstudio4.comp.archipelago.types;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.draw2d.geometry.Rectangle;

import edu.uci.isr.archstudio4.comp.archipelago.ArchipelagoServices;
import edu.uci.isr.archstudio4.comp.archipelago.util.HintSupport;
import edu.uci.isr.bna4.IBNAModel;
import edu.uci.isr.bna4.assemblies.BoxAssembly;
import edu.uci.isr.bna4.assemblies.EndpointAssembly; //import edu.uci.isr.widgets.swt.constants.FontStyle;
import edu.uci.isr.xarchflat.ObjRef;

@Deprecated
public class TypesHintSupport{

	public static void writeHintsForTypes(ArchipelagoServices AS, ObjRef xArchRef, ObjRef rootRef, IBNAModel m, ObjRef archTypesRef){
		ObjRef hintsContextRef = AS.xarch.createContext(xArchRef, "hints3");
		ObjRef eltRef = AS.xarch.create(hintsContextRef, "hintedElement");

		HintSupport.getInstance().writeTarget(AS, xArchRef, eltRef, archTypesRef);

		ObjRef[] componentTypeRefs = AS.xarch.getAll(archTypesRef, "componentType");
		for(ObjRef element: componentTypeRefs){
			writeHintsForBrickType(AS, xArchRef, eltRef, m, element);
		}
		ObjRef[] connectorTypeRefs = AS.xarch.getAll(archTypesRef, "connectorType");
		for(ObjRef element: connectorTypeRefs){
			writeHintsForBrickType(AS, xArchRef, eltRef, m, element);
		}
		ObjRef[] interfaceTypeRefs = AS.xarch.getAll(archTypesRef, "interfaceType");
		for(ObjRef element: interfaceTypeRefs){
			writeHintsForInterfaceType(AS, xArchRef, eltRef, m, element);
		}

		AS.xarch.add(rootRef, "hintedElement", eltRef);
	}

	public static void writeHintsForBrickType(ArchipelagoServices AS, ObjRef xArchRef, ObjRef rootRef, IBNAModel m, ObjRef brickTypeRef){
		BoxAssembly brickTypeAssembly = TypesMapper.findBrickTypeAssembly(AS, m, brickTypeRef);
		if(brickTypeAssembly != null){
			ObjRef hintsContextRef = AS.xarch.createContext(xArchRef, "hints3");
			ObjRef eltRef = AS.xarch.create(hintsContextRef, "hintedElement");

			//xArchID
			HintSupport.getInstance().writeTarget(AS, xArchRef, eltRef, brickTypeRef);

			HintSupport.getInstance().writeProperty(AS, xArchRef, eltRef, "glassBoundingBox", brickTypeAssembly.getBoxGlassThing().getBoundingBox());
			HintSupport.getInstance().writeProperty(AS, xArchRef, eltRef, "boxColor", brickTypeAssembly.getBoxThing().getColor());
			HintSupport.getInstance().writeProperty(AS, xArchRef, eltRef, "labelColor", brickTypeAssembly.getBoxedLabelThing().getColor());
			//HintSupport.getInstance().writeProperty(AS, xArchRef, eltRef, 
			//	"labelFontName", brickTypeAssembly.getBoxedLabelThing().getFontName());
			//HintSupport.getInstance().writeProperty(AS, xArchRef, eltRef, 
			//	"labelFontSize", brickTypeAssembly.getBoxedLabelThing().getFontSize());
			//HintSupport.getInstance().writeProperty(AS, xArchRef, eltRef, 
			//	"labelFontStyle", brickTypeAssembly.getBoxedLabelThing().getFontStyle());

			ObjRef[] signatureRefs = AS.xarch.getAll(brickTypeRef, "signature");
			for(ObjRef element: signatureRefs){
				writeHintsForSignature(AS, xArchRef, eltRef, m, element);
			}
			AS.xarch.add(rootRef, "hintedElement", eltRef);
		}
	}

	public static void writeHintsForSignature(ArchipelagoServices AS, ObjRef xArchRef, ObjRef rootRef, IBNAModel m, ObjRef signatureRef){
		EndpointAssembly endpointAssembly = TypesMapper.findSignatureAssembly(AS, m, signatureRef);
		if(endpointAssembly != null){
			ObjRef hintsContextRef = AS.xarch.createContext(xArchRef, "hints3");
			ObjRef eltRef = AS.xarch.create(hintsContextRef, "hintedElement");

			//xArchID
			HintSupport.getInstance().writeTarget(AS, xArchRef, eltRef, signatureRef);

			HintSupport.getInstance().writeProperty(AS, xArchRef, eltRef, "glassAnchorPoint", endpointAssembly.getEndpointGlassThing().getAnchorPoint());
			//HintSupport.getInstance().writeProperty(AS, xArchRef, eltRef,
			//	"tagAnchorPoint", endpointAssembly.getTagThing().getAnchorPoint());
			//HintSupport.getInstance().writeProperty(AS, xArchRef, eltRef,
			//	"tagVisible", endpointAssembly.getTagThing().isVisible());
			//HintSupport.getInstance().writeProperty(AS, xArchRef, eltRef,
			//	"tagAngle", endpointAssembly.getTagThing().getAngle());
			AS.xarch.add(rootRef, "hintedElement", eltRef);
		}
	}

	public static void writeHintsForInterfaceType(ArchipelagoServices AS, ObjRef xArchRef, ObjRef rootRef, IBNAModel m, ObjRef interfaceTypeRef){
		EndpointAssembly endpointAssembly = TypesMapper.findInterfaceTypeAssembly(AS, m, interfaceTypeRef);
		if(endpointAssembly != null){
			ObjRef hintsContextRef = AS.xarch.createContext(xArchRef, "hints3");
			ObjRef eltRef = AS.xarch.create(hintsContextRef, "hintedElement");

			//xArchID
			HintSupport.getInstance().writeTarget(AS, xArchRef, eltRef, interfaceTypeRef);

			HintSupport.getInstance().writeProperty(AS, xArchRef, eltRef, "glassAnchorPoint", endpointAssembly.getEndpointGlassThing().getAnchorPoint());
			AS.xarch.add(rootRef, "hintedElement", eltRef);
		}
	}

	/* --------------------------------- */

	public static void readHintsForTypes(ArchipelagoServices AS, ObjRef xArchRef, ObjRef bundleRef, IBNAModel m, ObjRef archTypesRef){
		ObjRef[] componentTypeRefs = AS.xarch.getAll(archTypesRef, "componentType");
		for(ObjRef element: componentTypeRefs){
			readHintsForBrickType(AS, xArchRef, bundleRef, m, element);
		}
		ObjRef[] connectorTypeRefs = AS.xarch.getAll(archTypesRef, "connectorType");
		for(ObjRef element: connectorTypeRefs){
			readHintsForBrickType(AS, xArchRef, bundleRef, m, element);
		}
		ObjRef[] interfaceTypeRefs = AS.xarch.getAll(archTypesRef, "interfaceType");
		for(ObjRef element: interfaceTypeRefs){
			readHintsForInterfaceType(AS, xArchRef, bundleRef, m, element);
		}
	}

	public static void readHintsForBrickType(ArchipelagoServices AS, ObjRef xArchRef, ObjRef bundleRef, IBNAModel m, ObjRef brickTypeRef){
		BoxAssembly brickTypeAssembly = TypesMapper.findBrickTypeAssembly(AS, m, brickTypeRef);
		ObjRef eltRef = HintSupport.findHintedElementRef(AS, xArchRef, bundleRef, brickTypeRef);

		if(brickTypeAssembly != null && eltRef != null){
			Rectangle glassBoundingBox = (Rectangle)HintSupport.getInstance().readProperty(AS, xArchRef, eltRef, "glassBoundingBox");
			if(glassBoundingBox != null){
				brickTypeAssembly.getBoxGlassThing().setBoundingBox(glassBoundingBox);
			}

			RGB boxColor = (RGB)HintSupport.getInstance().readProperty(AS, xArchRef, eltRef, "boxColor");
			if(boxColor != null){
				brickTypeAssembly.getBoxThing().setColor(boxColor);
			}

			RGB labelColor = (RGB)HintSupport.getInstance().readProperty(AS, xArchRef, eltRef, "labelColor");
			if(labelColor != null){
				brickTypeAssembly.getBoxedLabelThing().setColor(labelColor);
			}

			/*
			 * String labelFontName =
			 * (String)HintSupport.getInstance().readProperty( AS, xArchRef,
			 * eltRef, "labelFontName"); if(labelFontName != null){
			 * brickTypeAssembly.getBoxedLabelThing().setFontName(labelFontName); }
			 * Integer labelFontSize =
			 * (Integer)HintSupport.getInstance().readProperty( AS, xArchRef,
			 * eltRef, "labelFontSize"); if((labelFontSize != null) &&
			 * (labelFontSize instanceof Integer)){
			 * brickTypeAssembly.getBoxedLabelThing().setFontSize(((Integer)labelFontSize).intValue()); }
			 * FontStyle labelFontStyle =
			 * (FontStyle)HintSupport.getInstance().readProperty( AS, xArchRef,
			 * eltRef, "labelFontStyle"); if(labelFontStyle != null){
			 * brickTypeAssembly.getBoxedLabelThing().setFontStyle(labelFontStyle); }
			 */

			ObjRef[] signatureRefs = AS.xarch.getAll(brickTypeRef, "signature");
			for(ObjRef element: signatureRefs){
				readHintsForSignature(AS, xArchRef, bundleRef, m, element);
			}
		}
	}

	public static void readHintsForSignature(ArchipelagoServices AS, ObjRef xArchRef, ObjRef bundleRef, IBNAModel m, ObjRef signatureRef){
		EndpointAssembly endpointAssembly = TypesMapper.findSignatureAssembly(AS, m, signatureRef);
		ObjRef eltRef = HintSupport.findHintedElementRef(AS, xArchRef, bundleRef, signatureRef);

		if(endpointAssembly != null && eltRef != null){

			Point glassAnchorPoint = (Point)HintSupport.getInstance().readProperty(AS, xArchRef, eltRef, "glassAnchorPoint");
			if(glassAnchorPoint != null){
				endpointAssembly.getEndpointGlassThing().setAnchorPoint(glassAnchorPoint);
			}

			//Point tagAnchorPoint = (Point)HintSupport.getInstance().readProperty(AS, xArchRef, eltRef, "tagAnchorPoint");
			//if(tagAnchorPoint != null){
			//	endpointAssembly.getTagThing().setAnchorPoint(tagAnchorPoint);
			//}
			//
			//Boolean tagVisible = (Boolean)HintSupport.getInstance().readProperty(AS, xArchRef, eltRef, "tagVisible");
			//if(tagVisible != null){
			//	endpointAssembly.getTagThing().setVisible(tagVisible);
			//}
			//
			//Integer tagAngle = (Integer)HintSupport.getInstance().readProperty(AS, xArchRef, eltRef, "tagAngle");
			//if(tagAngle != null){
			//	endpointAssembly.getTagThing().setAngle(tagAngle);
			//}
		}
	}

	public static void readHintsForInterfaceType(ArchipelagoServices AS, ObjRef xArchRef, ObjRef bundleRef, IBNAModel m, ObjRef interfaceTypeRef){
		EndpointAssembly endpointAssembly = TypesMapper.findInterfaceTypeAssembly(AS, m, interfaceTypeRef);
		ObjRef eltRef = HintSupport.findHintedElementRef(AS, xArchRef, bundleRef, interfaceTypeRef);

		if(endpointAssembly != null && eltRef != null){

			Point glassAnchorPoint = (Point)HintSupport.getInstance().readProperty(AS, xArchRef, eltRef, "glassAnchorPoint");
			if(glassAnchorPoint != null){
				endpointAssembly.getEndpointGlassThing().setAnchorPoint(glassAnchorPoint);
			}

		}
	}

}
