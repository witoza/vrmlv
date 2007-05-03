package malkoln.vrmlparser;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.Iterator;
import java.util.LinkedList;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

public class VRTransform extends VRNode{
	

	/**
	 * Returns Transformation Matrix that is to be applied to all children of Transform node
	 * @return Transformation Matrix 
	 */
	public malkoln.core.Matrix4f getTransformMatrix(){
		malkoln.core.Matrix4f m = new malkoln.core.Matrix4f();	
		m.LoadIdent();
		m.Translate(-center.x,-center.y,-center.z);
		m.Rotate(-scaleOrientation.w*180/(float)Math.PI,scaleOrientation.x,scaleOrientation.y,scaleOrientation.z);
		m.Scale(scale.x,scale.y,scale.z);
		m.Rotate(scaleOrientation.w*180/(float)Math.PI,scaleOrientation.x,scaleOrientation.y,scaleOrientation.z);
		m.Rotate(rotation.w*180/(float)Math.PI,rotation.x,rotation.y,rotation.z);
		m.Translate(center.x,center.y,center.z);
		m.Translate(translation.x,translation.y,translation.z);
		return m;
	}
	
	public Vector3f center				= new Vector3f(0,0,0);
	public Vector3f scale				= new Vector3f(1,1,1);
	public Vector3f translation			= new Vector3f(0,0,0);
	public Vector4f rotation			= new Vector4f(0,0,1,0);
	public Vector4f scaleOrientation	= new Vector4f(0,0,1,0);
	
	public LinkedList children;
		
	public Iterator iterator(){
		return new VRMLDefaultTreeDFSIterator(children, this);
	}
	
	public String toString(){
		if (name!=null)
			return name;
		return "Transform";		
	}	

	public VRNode read(VRMLNodeParser parser) throws IOException {
		parser.st.nextToken(); //{
		boolean anyTrans = false;
		while (parser.st.nextToken()!=StreamTokenizer.TT_EOF){
			if (parser.st.ttype!=StreamTokenizer.TT_WORD)
				break;
			String s=parser.st.sval;
			parser.print(s);
			if (s.equals("center"))					{anyTrans=true; center = parser.readVector3f();}
			else if (s.equals("scale"))				{anyTrans=true; scale = parser.readVector3f();}
			else if (s.equals("rotation"))			{anyTrans=true; rotation = parser.readVector4f();}
			else if (s.equals("scaleOrientation"))	{anyTrans=true; scaleOrientation = parser.readVector4f();}
			else if (s.equals("translation"))		{anyTrans=true; translation = parser.readVector3f();}
			else if (s.equals("children"))			children = parser.readNodes(this);
		}
		if (!anyTrans && children!=null && children.size()==1){
			return (VRNode)children.get(0);
		}
		return this;
	}
}