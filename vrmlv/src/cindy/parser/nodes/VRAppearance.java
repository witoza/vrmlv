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

public class VRAppearance extends VRNode{
	
	static public final String VRNODENAME = "Appearance";
	public String getNodeInternalName(){
		return VRNODENAME;
	}
	
	public VRMaterial material = new VRMaterial();
	public VRImageTexture texture;
	
	public Iterator iterator(){
		LinkedList args = new LinkedList();
		if (material!=null)	args.add(material);
		if (texture!=null)	args.add(texture);
		if (args.isEmpty()) args=null;
		return new VRMLDefaultTreeDFSIterator(args,this);
	}	

	public VRNode read(VRMLNodeParser parser) throws IOException {

		parser.st.nextToken(); //{
		while (parser.st.nextToken()!=StreamTokenizer.TT_EOF){
			if (parser.st.ttype!=StreamTokenizer.TT_WORD)
				break;
			String s=parser.st.sval;
			parser.print(s);
			if (s.equals("material")) material=(VRMaterial) parser.readNode(this);
			else if (s.equals("texture")) texture=(VRImageTexture) parser.readNode(this);
			else if (s.equals("textureTransform")){
				parser.st.nextToken();//TextureTransform
				parser.skip('{','}');
			}
			else {
				throw new VRMLParserException(s + " phrase not possible in "+ getNodeInternalName() + " node! ");
			}
		}
		return this;
	}
	
	public VRNode clone(VRMLNodeFactory nf) {
		VRAppearance nd = (VRAppearance)nf.createAppearance();
		nd.model = model;
		nd.name = name;
		
		nd.material = material;
		nd.texture = texture;
		return nd;
	}

}