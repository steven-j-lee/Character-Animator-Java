package main.dependencies;

import javax.swing.JTextField;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import main.dependencies.geometry.BaseShapeControllers;
import mintools.parameters.BooleanParameter;

public class CharacterMaker {

	static public String name = "scene";
	static BooleanParameter loadFromFile = new BooleanParameter( "Load from file (otherwise by procedure)", false );
	static JTextField baseFileName = new JTextField("data/xmldata/character");
	
	/**
	 * Creates a character, either procedurally, or by loading from an xml file
	 * @return root node
	 */
	static public GraphNode create() {

		if ( loadFromFile.getValue() ) {
			return CharacterFromXML.load( baseFileName.getText() + ".xml");
		} else {
			FreeJoint rootNode = new FreeJoint("root");
			RotaryJoint rotar = new RotaryJoint("rotator", 2, (Math.PI * (-2)) , (Math.PI * 2),
					new Vector3d(1,1,1));
			SphericalJoint sphereJoint = new SphericalJoint("sp", new Vector3d(1.5,1.3,1.5));

			BaseShapeControllers sphere = new BaseShapeControllers("sphere");
			sphere.setCentre(new Vector3d(0.2,0.2,0.2));
			sphere.setScale(new Vector3d(1,1,1));
			sphere.setColors(new Vector3f(0.45f, 0.32f, 0.69f)  );

			BaseShapeControllers cube = new BaseShapeControllers("box");
			cube.setCentre(new Vector3d(1.5,1.3,1.5));
			cube.setScale(new Vector3d(1,1,1));
			cube.setColors(new Vector3f(0.32f, 0.78f, 0.54f)  );

			rootNode.add(rotar);
			rotar.add(sphere);
			rotar.add(sphereJoint);
			sphereJoint.add(cube);

			return rootNode;
		}
	}
}
