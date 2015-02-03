package edu.uta.futureye.test;

import edu.uta.futureye.core.CoordinateTransform;
import edu.uta.futureye.core.DOF;
import edu.uta.futureye.core.Element;
import edu.uta.futureye.core.Node;
import edu.uta.futureye.function.Variable;
import edu.uta.futureye.function.intf.MathFun;
import edu.uta.futureye.lib.shapefun.SFBilinearLocal2D;
import edu.uta.futureye.lib.shapefun.SFLinearLocal2DRS;
import edu.uta.futureye.util.container.NodeList;

public class ShapeFunctionTest {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		testSFLinearLocal2D();
		testSFBilinearLocal2D();

	}
	
	public static void testSFLinearLocal2D() {
		NodeList nodes = new NodeList();
		nodes.add(new Node(1, 0.0,0.0));
		nodes.add(new Node(2, 0.2,0.0));
		nodes.add(new Node(3, 0.0,0.2));
		
		Element e = new Element(nodes);
		
//		SFLinearLocal2D[] shapeFun = new SFLinearLocal2D[3];
//		shapeFun[0] = new SFLinearLocal2D(1);
//		shapeFun[1] = new SFLinearLocal2D(2);
//		shapeFun[2] = new SFLinearLocal2D(3);
		SFLinearLocal2DRS[] shapeFun = new SFLinearLocal2DRS[3];
		shapeFun[0] = new SFLinearLocal2DRS(1);
		shapeFun[1] = new SFLinearLocal2DRS(2);
		shapeFun[2] = new SFLinearLocal2DRS(3);
		
		//Test the derivatives of shape function
		
		shapeFun[0].assignElement(e);
		System.out.println(shapeFun[0]);
		MathFun SF0dx = shapeFun[0]._d("x");
		MathFun SF0dy = shapeFun[0]._d("y");
		System.out.println(SF0dx);
		System.out.println(SF0dx.apply(null));
		System.out.println(SF0dy);
		System.out.println(SF0dy.apply(null));
		
		shapeFun[1].assignElement(e);
		System.out.println(shapeFun[1]);
		MathFun SF1dx = shapeFun[1]._d("x");
		MathFun SF1dy = shapeFun[1]._d("y");
		System.out.println(SF1dx.apply(null));
		System.out.println(SF1dy.apply(null));
		
		shapeFun[2].assignElement(e);
		System.out.println(shapeFun[2]);
		MathFun SF2dx = shapeFun[2]._d("x");
		MathFun SF2dy = shapeFun[2]._d("y");
		System.out.println(SF2dx.apply(null));
		System.out.println(SF2dy.apply(null));		
		
	}
	
	public static void testSFBilinearLocal2D() {
		NodeList nodes = new NodeList();
		nodes.add(new Node(1, -1.0,-1.0));
		nodes.add(new Node(2, 1.0,-1.0));
		nodes.add(new Node(3, 1.0,1.0));
		nodes.add(new Node(4, -1.0,1.0));
		
		Element e = new Element(nodes);

		SFBilinearLocal2D[] shapeFun = new SFBilinearLocal2D[4];
		shapeFun[0] = new SFBilinearLocal2D(1);
		shapeFun[1] = new SFBilinearLocal2D(2);
		shapeFun[2] = new SFBilinearLocal2D(3);
		shapeFun[3] = new SFBilinearLocal2D(4);
		Variable v0 = new Variable();
		v0.set("r", 1.0);
		v0.set("s", 0.0);
		System.out.println(shapeFun[0].apply(v0));
		
		//Asign degree of freedom to nodes
		for(int j=1;j<=e.nodes.size();j++) {
			//Asign shape function to DOF
			DOF dof = new DOF(j,e.nodes.at(j).globalIndex,shapeFun[j-1]);
			e.addNodeDOF(j,dof);
		}
		
		//Coordinate transform and Jacbian on element e
		CoordinateTransform trans = new CoordinateTransform(2);
		trans.transformLinear2D(e);

		Variable v = new Variable();
		v.set("r", 0);
		v.set("s", 0);

		
		trans.computeJacobianMatrix(); //2011/11/26
		trans.computeJacobian2D(); //2011/11/26
		MathFun jac = trans.getJacobian();
		System.out.println(jac);
		System.out.println("jac="+jac.apply(v));
		
		//Test the derivatives of shape function
		
		shapeFun[0].assignElement(e);
		MathFun SF0dx = shapeFun[0]._d("x");
		MathFun SF0dy = shapeFun[0]._d("y");
		System.out.println(SF0dx);
		System.out.println("SF0dx("+v+")="+SF0dx.apply(v));
		System.out.println(SF0dy);
		System.out.println("SF0dy("+v+")="+SF0dy.apply(v));
		
		shapeFun[1].assignElement(e);
		MathFun SF1dx = shapeFun[1]._d("x");
		MathFun SF1dy = shapeFun[1]._d("y");
		System.out.println(SF1dx);
		System.out.println(SF1dy);
		
		shapeFun[2].assignElement(e);
		MathFun SF2dx = shapeFun[2]._d("x");
		MathFun SF2dy = shapeFun[2]._d("y");
		System.out.println(SF2dx);
		System.out.println(SF2dy);	
		
		shapeFun[3].assignElement(e);
		MathFun SF3dx = shapeFun[3]._d("x");
		MathFun SF3dy = shapeFun[3]._d("y");
		System.out.println(SF3dx);
		System.out.println(SF3dy);		
	}

}