package comp557.a1;
//steven lee 260803947
import com.jogamp.opengl.GLAutoDrawable;

import mintools.parameters.DoubleParameter;

public class FreeJoint extends GraphNode {

	DoubleParameter tx;
	DoubleParameter ty;
	DoubleParameter tz;
	DoubleParameter rx;
	DoubleParameter ry;
	DoubleParameter rz;
		
	public FreeJoint( String name ) {
		super(name);
		dofs.add( tx = new DoubleParameter( name+" tx", 0, -10, 10 ) );
		dofs.add( ty = new DoubleParameter( name+" ty", 0,  -10, 10 ) );
		dofs.add( tz = new DoubleParameter( name+" tz", 0, -10, 10 ) );
		dofs.add( rx = new DoubleParameter( name+" rx", 0, (Math.PI * (-2)) , (Math.PI * 2) ));		
		dofs.add( ry = new DoubleParameter( name+" ry", 0, (Math.PI * (-2)) , (Math.PI * 2) ));
		dofs.add( rz = new DoubleParameter( name+" rz", 0, (Math.PI * (-2)) , (Math.PI * 2) ) );
	}
	
	@Override
	public void display( GLAutoDrawable drawable, BasicPipeline pipeline ) {
		pipeline.push();

		pipeline.translate(tx.getValue(), ty.getValue(), tz.getValue());
		pipeline.rotate(Math.PI, rx.getValue(), ry.getValue(), rz.getValue());
		// TODO: Objective 3: Freejoint, transformations must be applied before drawing children

		super.display( drawable, pipeline );		
		pipeline.pop();
	}
	
}
