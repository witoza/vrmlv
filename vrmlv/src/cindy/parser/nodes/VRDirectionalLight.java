package cindy.parser.nodes;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.Iterator;

import javax.vecmath.Vector3f;

import cindy.parser.VRMLDefaultTreeDFSIterator;
import cindy.parser.VRMLNodeFactory;
import cindy.parser.VRMLNodeParser;
import cindy.parser.VRMLParserException;
import cindy.parser.VRNode;

public class VRDirectionalLight extends VRNode{

	static public final String VRNODENAME = "DirectionalLight";
	
	public String getNodeInternalName(){
		return VRNODENAME;
	}
		
	public float ambientIntensity = 0;
	public Vector3f color = new Vector3f(1,1,1);
	public Vector3f direction = new Vector3f(0,0,-1);
	public float intensity = 1.0f;
	public boolean on = true;
	

	public VRNode clone(VRMLNodeFactory nf) {
		VRDirectionalLight nd = (VRDirectionalLight)nf.createDirectionalLight();
		nd.model = model;
		nd.name = name;
		
		nd.ambientIntensity = ambientIntensity;
		nd.color = color;
		nd.direction = direction;
		nd.intensity = intensity;
		nd.on = on;
		return nd;
	}	
		
	public Iterator iterator() {
		return new VRMLDefaultTreeDFSIterator(null,this);
	}

	public VRNode read(VRMLNodeParser parser) throws IOException {
		parser.st.nextToken(); //{
		while (parser.st.nextToken()!=StreamTokenizer.TT_EOF){
			if (parser.st.ttype!=StreamTokenizer.TT_WORD)
				break;
			String s=parser.st.sval;
			parser.print(s);
			
			if (s.equals("ambientIntensity"))	ambientIntensity=parser.readFloat();
			else if (s.equals("color"))			color=parser.readVector3f();
			else if (s.equals("direction"))		direction=parser.readVector3f();
			else if (s.equals("intensity"))		intensity=parser.readFloat();
			else if (s.equals("on"))			on=parser.readBoolean();
			else {
				throw new VRMLParserException(s + " phrase not possible in "+ getNodeInternalName() + " node! ");
			}
		}
		return this;	
	}

}
