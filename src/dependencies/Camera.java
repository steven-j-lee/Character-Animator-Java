package dependencies;

import javax.swing.JPanel;
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import mintools.parameters.DoubleParameter;
import mintools.parameters.Parameter;
import mintools.parameters.ParameterListener;
import mintools.parameters.Vec3Parameter;
import mintools.swing.VerticalFlowPanel;

/**
 * Camera class to be used both for viewing the scene, but also to draw the scene from 
 * a point light.
 */
public class Camera {

	/** Position of viewer */
	Vec3Parameter position = new Vec3Parameter("position", 0, 0, 10 );
	Vec3Parameter lookat = new Vec3Parameter("look at", 0, 0, 0 );
	Vec3Parameter up = new Vec3Parameter("up", 0, 1, 0 );

	DoubleParameter near = new DoubleParameter( "near plane", 1, 0.1, 10 );
	DoubleParameter far = new DoubleParameter( "far plane" , 40, 1, 100 );
	DoubleParameter fovy = new DoubleParameter( "fovy degrees" , 27, 14, 67 );

	Matrix4d V = new Matrix4d();
	Matrix4d P = new Matrix4d();

	public Camera() {
		near.addParameterListener( new ParameterListener<Double>() {
			@Override
			public void parameterChanged(Parameter<Double> parameter) {
				if ( near.getValue() >= far.getValue() ) {
					far.setValue( near.getValue() + 0.1 );
				}
			}
		});
		far.addParameterListener( new ParameterListener<Double>() {
			@Override
			public void parameterChanged(Parameter<Double> parameter) {
				if ( far.getValue() <= near.getValue() ) {
					near.setValue( far.getValue() - 0.1 );
				}
			}
		});
		setMaximumValues(position, 100.0);
		setMaximumValues(lookat, 100.0);
		setMaximumValues(up, 100.0);

	}

	private Vector3d tmp = new Vector3d();
	private Vector3d u = new Vector3d();
	private Vector3d v = new Vector3d();
	Vector3d w = new Vector3d();
	private Vector3d e = new Vector3d();
	private Matrix3d R = new Matrix3d();

	/** Frustum parameters */
	double n,f,l,r,t,b;

	/**
	 * Update the projection and viewing matrices
	 * We'll do this every time we draw, though we could choose to more efficiently do this only when parameters change.
	 * @param width of display window (for aspect ratio)
	 * @param height of display window (for aspect ratio)
	 */
	public void updateMatrix( double width, double height ) {
		e.set( position.x, position.y, position.z );
		w.set( -lookat.x, -lookat.y, -lookat.z );
		w.add(e);
		w.normalize();
		tmp.set(up.x,up.y,up.z);
		u.cross(tmp, w);
		u.normalize();
		v.cross(w, u);
		R.setRow(0, u);
		R.setRow(1, v);
		R.setRow(2, w);
		R.transform(e, tmp);
		tmp.scale(-1);
		V.set(R, tmp, 1);
		double n = near.getValue();
		double f = far.getValue();
		double theta = Math.PI/180*fovy.getValue();
		t = n*Math.tan(theta/2);
		b = -t;
		double aspect = width/height;
		l = b*aspect;
		r = t*aspect;
		P.setZero();
		P.m00 = (2*n)/(r-l);
		P.m02 = (r+l)/(r-l);
		P.m11 = (2*n)/(t-b);
		P.m12 = (t+b)/(t-b);
		P.m22 = (n+f)/(n-f);
		P.m23 = (2*n*f)/(n-f);
		P.m32 = -1;
	}

	public void setMaximumValues(Vec3Parameter params, double maxVal){
		params.xp.setMaximum(maxVal);
		params.yp.setMaximum(maxVal);
		params.zp.setMaximum(maxVal);
	}

	/**
	 * @return controls for the camera
	 */
	public JPanel getControls() {
		VerticalFlowPanel vfp = new VerticalFlowPanel();
		vfp.add( position );
		vfp.add( lookat );
		vfp.add( up );
		vfp.add( near.getControls() );
		vfp.add( far.getControls() );
		vfp.add( fovy.getControls() );
		return vfp.getPanel();
	}

}
