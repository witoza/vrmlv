package cindy.parser;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.Iterator;

import javax.vecmath.Vector3f;

public class VRBox extends VRNode{

	public Vector3f		size=new Vector3f(2,2,2);
	
	public Iterator iterator() {
		return new VRMLDefaultTreeDFSIterator(null,this);
	}
	
	public String toString(){
		if (name!=null)
			return name;
		return "Box";
	}
	
	public VRNode read(VRMLNodeParser parser) throws IOException {
		parser.st.nextToken(); //{
		while (parser.st.nextToken()!=StreamTokenizer.TT_EOF){
			if (parser.st.ttype!=StreamTokenizer.TT_WORD)
				break;
			String s=parser.st.sval;
			parser.print(s);
			if (s.equals("size"))			size=parser.readVector3f();
		}
		return this;
	}

}
