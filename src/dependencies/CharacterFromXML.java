package dependencies;
 		  	  				   
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Scanner;

import javax.vecmath.Tuple3d;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import dependencies.geometry.*;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Loads an articulated character hierarchy from an XML file.
 */
public class CharacterFromXML {

	public static GraphNode load(String filename ) {
		try {
			InputStream inputStream = new FileInputStream(new File(filename));
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(inputStream);
			return createScene( null, document.getDocumentElement() ); // we don't check the name of the document elemnet
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Failed to load simulation input file.", e);
		}
	}

	/**
	 * Load a subtree from a XML node.
	 * Returns the root on the call where the parent is null, but otherwise
	 * all children are added as they are created and all other deeper recursive
	 * calls will return null.
	 */
	public static GraphNode createScene( GraphNode parent, Node dataNode ) {
		NodeList nodeList = dataNode.getChildNodes();
		for ( int i = 0; i < nodeList.getLength(); i++ ) {
			Node n = nodeList.item(i);
			// skip all text, just process the ELEMENT_NODEs
			if ( n.getNodeType() != Node.ELEMENT_NODE ) continue;
			String nodeName = n.getNodeName();
			GraphNode node = null;
			if ( nodeName.equalsIgnoreCase( "node" ) ) {
				node = CharacterFromXML.createJoint( n );
			} else if ( nodeName.equalsIgnoreCase( "geom" ) ) {
				node = CharacterFromXML.createGeom( n ) ;
			} else {
				System.err.println("Unknown node " + nodeName );
				continue;
			}
			if ( node == null ) continue;
			// recurse to load any children of this node
			createScene( node, n );
			if ( parent == null ) {
				// if no parent, we can only have one root... ignore other nodes at root level
				return node;
			} else {
				parent.add( node );
			}
		}
		return null;
	}

	/**​‌​​​‌‌​​​‌‌​​​‌​​‌‌‌​​‌
	 * Create a joint
	 *
	 * TODO: Objective 8: XML, Adapt commented code in createJoint() to create your joint nodes when loading from xml
	 */
	public static GraphNode createJoint( Node dataNode ) {
		String type = dataNode.getAttributes().getNamedItem("type").getNodeValue();
		String name = dataNode.getAttributes().getNamedItem("name").getNodeValue();
		Tuple3d t;
		Vector3d translation = new Vector3d();
		Vector3d rotation = new Vector3d();

		//parameters for rotary joint
		int axisRot = 1;
		double minVal = Double.NaN;
		double maxVal = Double.NaN;


		if ( type.equals("freejoint") ) {
			FreeJoint joint = new FreeJoint( name );
			return joint;
		} else if ( type.equals("spherical") ) {
			//Spherical Joint -> param: String name, Vector3d parentTranslation
			// position is optional (ignored if missing) but should probably be a required attribute!​‌​​​‌‌​​​‌‌​​​‌​​‌‌‌​​‌
			// Could add optional attributes for limits (to all joints)

//			SphericalJoint joint = new SphericalJoint( name );
//			if ( (t=getTuple3dAttr(dataNode,"position")) != null ) joint.setPosition( t );
//			return joint;
			SphericalJoint sphericalJoint  = new SphericalJoint(name, translation);
			if ( (translation=getVector3dAttr(dataNode,"translation")) != null ) {
				sphericalJoint.translation = translation;
				return sphericalJoint;
			}

		} else if ( type.equals("rotary") ) {
			// position and axis are required... passing null to set methods
			// likely to cause an execption (perhaps OK)
			RotaryJoint rotaryJoint = new RotaryJoint(name, axisRot, minVal, maxVal, translation);
			if((axisRot=getIntAttr(dataNode, "definedAxisRot")) != 0 ) {
				axisRot = Integer.parseInt(dataNode.getAttributes().getNamedItem("definedAxisRot").getNodeValue());
				rotaryJoint.definedAxisRot = axisRot;
			}
			if((minVal=getDoubleAttr(dataNode, "setMinVal")) != Double.NaN ) {
				minVal = Double.parseDouble(dataNode.getAttributes().getNamedItem("setMinVal").getNodeValue());
				rotaryJoint.setMinVal = minVal;

			}
			if((maxVal=getDoubleAttr(dataNode, "setMaxVal")) != Double.NaN ) {
				maxVal = Double.parseDouble(dataNode.getAttributes().getNamedItem("setMaxVal").getNodeValue());
				rotaryJoint.setMaxVal = maxVal;
			}
			if ( (translation=getVector3dAttr(dataNode,"translation")) != null ) {
				rotaryJoint.translation = translation;
			}
			return rotaryJoint;

			//???
//			Hinge joint = new Hinge( name );
//			joint.setPosition( getTuple3dAttr(dataNode,"position") );
//			joint.setAxis( getTuple3dAttr(dataNode,"axis") );
//			return joint;

		} else {
			System.err.println("Unknown type " + type );
		}
		return null;
	}

	/**
	 * Creates a geometry DAG node
	 *
	 * TODO: Objective 5: Adapt commented code in greatGeom to create your geometry nodes when loading from xml
	 */
	public static GraphNode createGeom( Node dataNode ) {
		String type = dataNode.getAttributes().getNamedItem("type").getNodeValue();
		String name = dataNode.getAttributes().getNamedItem("name").getNodeValue();
		Vector3d t;
		Vector3f t2;
		//adapt to pseudo-code
		if ( type.equals("box" ) ) {
			BaseShapeControllers geom = new BaseShapeControllers( type );
			if ( (t=getVector3dAttr(dataNode,"translation")) != null ) geom.setCentre( t );
			if ( (t=getVector3dAttr(dataNode,"uniformScale")) != null ) geom.setScale( t );
			if ( (t2=getVector3fAttr(dataNode,"colors")) != null ) geom.setColors( t2);
			return geom;
		} else if ( type.equals( "sphere" )) {
			BaseShapeControllers geom = new BaseShapeControllers( type );
			if ( (t=getVector3dAttr(dataNode,"translation")) != null ) geom.setCentre( t );
			if ( (t=getVector3dAttr(dataNode,"uniformScale")) != null ) geom.setScale( t );
			if ( (t2=getVector3fAttr(dataNode,"colors")) != null ) geom.setColors( t2);
			return geom;
		} else {
			System.err.println("unknown type " + type );
		}
		return null;
	}

	/**
	 * Loads tuple3d attributes of the given name from the given node.
	 * @param dataNode
	 * @param attrName
	 * @return null if attribute not present
	 */
	public static Tuple3d getTuple3dAttr( Node dataNode, String attrName ) {
		Node attr = dataNode.getAttributes().getNamedItem( attrName);
		Vector3d tuple = null;
		if ( attr != null ) {
			Scanner s = new Scanner( attr.getNodeValue() );
			tuple = new Vector3d( s.nextDouble(), s.nextDouble(), s.nextDouble() );
			s.close();
		}
		return tuple;
	}

	//added another method for 3f
	public static Tuple3f getTuple3fAttr(Node dataNode, String attrName ) {
		Node attr = dataNode.getAttributes().getNamedItem( attrName);
		Vector3f tuple = null;
		if ( attr != null ) {
			Scanner s = new Scanner( attr.getNodeValue() );
			tuple = new Vector3f( s.nextFloat(), s.nextFloat(), s.nextFloat() );
			s.close();
		}
		return tuple;
	}

	//added method for Vector3 inputs
	public static Vector3d getVector3dAttr( Node dataNode, String attrName ) {
		Node attr = dataNode.getAttributes().getNamedItem( attrName);
		Vector3d tuple = null;
		if ( attr != null ) {
			Scanner s = new Scanner( attr.getNodeValue() );
			tuple = new Vector3d( s.nextDouble(), s.nextDouble(), s.nextDouble() );
			s.close();
		}
		return tuple;
	}

	public static Vector3f getVector3fAttr( Node dataNode, String attrName ) {
		Node attr = dataNode.getAttributes().getNamedItem( attrName);
		Vector3f tuple = null;
		if ( attr != null ) {
			Scanner s = new Scanner( attr.getNodeValue() );
			tuple = new Vector3f( s.nextFloat(), s.nextFloat(), s.nextFloat() );
			s.close();
		}
		return tuple;
	}

	//set up for inputs of type double
	public static double getDoubleAttr( Node dataNode, String attrName ) {
		Node attr = dataNode.getAttributes().getNamedItem( attrName);
		double input;
		if ( attr != null ) {
			Scanner s = new Scanner( attr.getNodeValue() );
			input = s.nextDouble();
			s.close();
		} else {
			return 1.0;
		}
		return input;
	}

	//set up for inputs of type int
	public static int getIntAttr( Node dataNode, String attrName ) {
		Node attr = dataNode.getAttributes().getNamedItem( attrName);
		int input;
		if ( attr != null ) {
			Scanner s = new Scanner( attr.getNodeValue() );
			input = s.nextInt();
			s.close();
		} else {
			return 1;
		}
		return input;
	}




}