package main.dependencies;

import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;

import com.jogamp.opengl.GLAutoDrawable;

//import comp557.a2.geom.FancyAxis;
//import comp557.a2.geom.QuadWithTexCoords;
//import comp557.a2.geom.WireCube;
import main.dependencies.geometry.SimpleAxis;
import mintools.parameters.BooleanParameter;
import mintools.parameters.DoubleParameter;
import mintools.swing.VerticalFlowPanel;


public class PointLightCamera extends Camera {	

    public BooleanParameter debugLightFrustum = new BooleanParameter( "debug light frustum" , true );
    public DoubleParameter sigma = new DoubleParameter( "self shadowing offset", 0.0015, 0, 0.1 );
	    
    public PointLightCamera() {
    	super();
    	position.set( new Vector3d(3, 3, 3) );
    	fovy.setDefaultValue(55.0);
    }
    
    public void getPositionInWorld( Vector4d pos ) {
    	pos.set( position.x, position.y, position.z, 1 );
    }
    
    Matrix4d Vinv = new Matrix4d();
    Matrix4d Pinv = new Matrix4d();
    
    public void draw( GLAutoDrawable drawable, BasicPipeline pipeline ) throws Exception {
    	if ( !debugLightFrustum.getValue() ) return;
    	if(V.determinant() == 0.0){
    	    throw new Exception("Viewing Matrix is not invertible");
        } else{
            Vinv.invert(V);
        }
    	if(P.determinant() == 0.0){
            throw new Exception("Projection Matrix is not invertible");
        } else{
    	    Pinv.invert(P);
        }
        pipeline.push();
		pipeline.controlLighting(drawable, false);
		pipeline.matrixOp(drawable, Vinv);
		if(pipeline.debugMode){
            SimpleAxis.draw(drawable, pipeline);
        }
		pipeline.push();
		pipeline.matrixOp(drawable, Pinv);
		pipeline.pop();

		
    }
    
    /**
     * @return controls for the shadow mapped light
     */

    public JPanel getControls() {
        VerticalFlowPanel vfp = new VerticalFlowPanel();
        vfp.setBorder(new TitledBorder("Point Light Camera Controls"));
        vfp.add( super.getControls() );
        vfp.add( sigma.getControls() );
        vfp.add( debugLightFrustum.getControls() );        
        return vfp.getPanel();
    }
    
}


