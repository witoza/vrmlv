package cindy.parser.nodes;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.log4j.Logger;

import cindy.parser.VRMLDefaultTreeDFSIterator;
import cindy.parser.VRMLNodeFactory;
import cindy.parser.VRMLNodeParser;
import cindy.parser.VRMLParserException;
import cindy.parser.VRNode;

public class VRImageTexture extends VRNode{

	private static Logger _LOG = Logger.getLogger(VRBox.class);
	
	static public final String VRNODENAME = "ImageTexture";
	public String getNodeInternalName(){
		return VRNODENAME;
	}
	
	public LinkedList<String> url;
	public boolean repeatS = true;
	public boolean repeatT = true;
	
	public VRNode clone(VRMLNodeFactory nf) {
		VRImageTexture nd = (VRImageTexture)nf.createImageTexture();
		nd.model = model;
		nd.name = name;
		
		nd.url = url;
		nd.repeatS = repeatS;
		nd.repeatT = repeatT;
		return nd;
	}
	
	
	public Iterator iterator() {
		return new VRMLDefaultTreeDFSIterator(null,this);
	}
		
	public VRNode read(VRMLNodeParser parser) throws IOException {
		_LOG.info("read started");
		parser.st.nextToken(); //{
		while (parser.st.nextToken()!=StreamTokenizer.TT_EOF){
			if (parser.st.ttype!=StreamTokenizer.TT_WORD)
				break;
			String s=parser.st.sval;
			parser.print(s);
			if (s.equals("url")){
				url = parser.readStrings('"');
				parser.model.addPixmap(url.element());				
			}
			else if (s.equals("repeatS"))	repeatS=parser.readBoolean();
			else if (s.equals("repeatT"))	repeatS=parser.readBoolean();
			else {
				throw new VRMLParserException(s + " phrase not possible in "+ getNodeInternalName() + " node! ");
			}
		}
		return this;
	}

}