package cindy.parser.nodes;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.Iterator;
import java.util.LinkedList;

import cindy.parser.VRMLDefaultTreeDFSIterator;
import cindy.parser.VRMLNodeFactory;
import cindy.parser.VRMLNodeParser;
import cindy.parser.VRMLParserException;
import cindy.parser.VRNode;

public class VRWorldInfo extends VRNode{

	static public final String VRNODENAME = "WorldInfo";
	
	public String getNodeInternalName(){
		return VRNODENAME;
	}
	
	public LinkedList<String> info;
	public String title = ""; 
	
	public VRNode clone(VRMLNodeFactory nf) {
		VRWorldInfo nd =(VRWorldInfo) nf.createWorldInfo();
		nd.model = model;
		nd.name = name;
		
		nd.info = info;
		nd.title = title;
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
			if (s.equals("title"))			title=parser.readString('"');
			else if (s.equals("info"))		info=parser.readStrings('"');
			else {
				throw new VRMLParserException(s + " phrase not possible in "+ getNodeInternalName() + " node! ");
			}
		}
		return this;	
	}


}
