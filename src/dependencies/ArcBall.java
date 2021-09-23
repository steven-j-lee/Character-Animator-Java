package main.dependencies;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.vecmath.AxisAngle4d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import mintools.parameters.DoubleParameter;
import mintools.swing.VerticalFlowPanel;

public class ArcBall {

	private DoubleParameter fit = new DoubleParameter( "Fit", 1, 0.5, 2 );
	private DoubleParameter gain = new DoubleParameter( "Gain", 1, 0.5, 2, true );

	private Vector3d v0 = new Vector3d();
	private Vector3d v1 = new Vector3d();
	private Vector3d axis = new Vector3d();

	private AxisAngle4d aa = new AxisAngle4d();

	Matrix4d R = new Matrix4d();
	private Matrix4d dR = new Matrix4d();

	public ArcBall() {
		R.setIdentity();
	}

	public void setVecFromMouseEvent( MouseEvent e, Vector3d v ) {
		Component c = e.getComponent();
		Dimension dim = c.getSize();
		double width = dim.getWidth();
		double height = dim.getHeight();
		double x = e.getX() - width/2;
		double y = - e.getY() + height/2;
		double radius = Math.min( dim.getWidth(), dim.getHeight() ) / 2 * fit.getValue();
		v.set( x, y, 0 );
		double vlen = v.length();
		if ( vlen > radius ) {
			v.normalize();
		} else {
			v.z = Math.sqrt( radius*radius - vlen*vlen );
			v.normalize();
		}
	}

	public void attach( Component c ) {
		c.addMouseMotionListener( new MouseMotionListener() {
			@Override
			public void mouseMoved( MouseEvent e ) {}
			@Override
			public void mouseDragged( MouseEvent e ) {
				setVecFromMouseEvent( e, v1 );
				if ( (e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) != 0 ) {
					axis.cross( v0, v1 );
					double dot = v0.dot(v1);
					if ( dot > 1 ) dot = 1;
					double angle = Math.acos( dot );
					aa.set( axis, angle * gain.getValue() );
					dR.set( aa );
					R.mul( dR, R );
				}
				v0.set( v1 );
			}
		});
		c.addMouseListener( new MouseListener() {
			@Override
			public void mouseReleased( MouseEvent e) {}
			@Override
			public void mousePressed( MouseEvent e) {
				System.out.println("mouse pressed");
				setVecFromMouseEvent( e, v0 );
			}
			@Override
			public void mouseExited(MouseEvent e) {}
			@Override
			public void mouseEntered(MouseEvent e) {}
			@Override
			public void mouseClicked(MouseEvent e) {}
		});
	}

	public JPanel getControls() {
		VerticalFlowPanel vfp = new VerticalFlowPanel();
		vfp.add( fit.getControls() );
		vfp.add( gain.getControls() );
		return vfp.getPanel();
	}

}
