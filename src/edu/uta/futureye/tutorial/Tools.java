package edu.uta.futureye.tutorial;

import java.io.File;

import edu.uta.futureye.algebra.SparseVectorHashMap;
import edu.uta.futureye.algebra.intf.Matrix;
import edu.uta.futureye.algebra.intf.SparseVector;
import edu.uta.futureye.algebra.intf.Vector;
import edu.uta.futureye.algebra.solver.external.SolverJBLAS;
import edu.uta.futureye.core.Mesh;
import edu.uta.futureye.core.Node;
import edu.uta.futureye.function.FMath;
import edu.uta.futureye.function.Variable;
import edu.uta.futureye.function.basic.FX;
import edu.uta.futureye.function.basic.Vector2Function;
import edu.uta.futureye.function.intf.MathFunc;
import edu.uta.futureye.function.intf.VectorFunction;
import edu.uta.futureye.io.MeshReader;
import edu.uta.futureye.io.MeshWriter;
import edu.uta.futureye.lib.assembler.AssemblerScalar;
import edu.uta.futureye.lib.weakform.WeakFormDerivative;
import edu.uta.futureye.util.Utils;
import edu.uta.futureye.util.container.ElementList;
import edu.uta.futureye.util.container.NodeList;

public class Tools {
	public static void plotVector(Mesh mesh, String outputFolder, String fileName, Vector v, Vector ...vs) {
	    MeshWriter writer = new MeshWriter(mesh);
	    if(!outputFolder.isEmpty()) {
		    File file = new File("./"+outputFolder);
			if(!file.exists()) {
				file.mkdirs();
			}
	    }
	    writer.writeTechplot("./"+outputFolder+"/"+fileName, v, vs);
	}

	public static void plotFunction(Mesh mesh, String outputFolder, String fileName, MathFunc fun, MathFunc ...funs) {
	    NodeList list = mesh.getNodeList();
	    int nNode = list.size();
		Variable var = new Variable();
		Vector v = new SparseVectorHashMap(nNode);
		Vector[] vs = new SparseVector[funs.length];
		for(int i=0;i<funs.length;i++) {
			vs[i] = new SparseVectorHashMap(nNode);
		}
	    for(int i=1;i<=nNode;i++) {
	    	Node node = list.at(i);
	    	var.setIndex(node.globalIndex);
	    	for(int j=1;j<=node.dim();j++) {
	    		if(fun.getVarNames().size()==node.dim())
	    			var.set(fun.getVarNames().get(j-1), node.coord(j));
	    	}
	    	v.set(i, fun.apply(var));
	    	for(int j=0;j<funs.length;j++) {
	    		vs[j].set(i,funs[j].apply(var));
	    	}
	    }
	    plotVector(mesh,outputFolder,fileName,v,vs);
	}
	
	public static Vector computeDerivative(Mesh mesh, Vector U, String varName) {
		mesh.clearBorderNodeMark();
		
		WeakFormDerivative weakForm = new WeakFormDerivative(varName);
		weakForm.setParam(new Vector2Function(U));
		
		AssemblerScalar assembler = new AssemblerScalar(mesh, weakForm);
		assembler.assemble();
		Matrix stiff = assembler.getStiffnessMatrix();
		Vector load = assembler.getLoadVector();
		
		SolverJBLAS solver = new SolverJBLAS();
		Vector w = solver.solveDGESV(stiff, load);
		return w;
	}
	
	public static Vector computeLaplace2D(Mesh mesh, Vector U) {
		Vector ux = Tools.computeDerivative(mesh,U,"x");
		Vector uy = Tools.computeDerivative(mesh,U,"y");
		Vector uxx = Tools.computeDerivative(mesh,ux,"x");
		Vector uyy = Tools.computeDerivative(mesh,uy,"y");
		Vector LpU = FMath.axpy(1.0, uxx, uyy);
		return LpU;
	}
	
	public static void main(String[] args) {
        MeshReader reader = new MeshReader("triangle.grd");
        Mesh mesh = reader.read2DMesh();
        //fun(x,y)=x^2+y^2
        MathFunc fun = FX.x.M(FX.x).A(FX.y.M(FX.y));
        plotFunction(mesh,".","testPlotFun.dat",fun);
        VectorFunction gradFun = FMath.grad(fun);
        plotFunction(mesh,".","testPlotFunGrad.dat",
        			gradFun.get(1),gradFun.get(2));
        
        T02Laplace model = new T02Laplace();
        model.run();
        Vector u = model.u;
        Vector ux = computeDerivative(model.mesh, u, "x");
        Vector uy = computeDerivative(model.mesh, u, "y");
        Vector uLaplace = computeLaplace2D(model.mesh, u);
        plotVector(model.mesh,".","testPlotUx.dat", ux);
        plotVector(model.mesh,".","testPlotUy.dat", uy);
        plotVector(model.mesh,".","testPlotULaplace.dat", uLaplace);
        //method gaussSmooth() need to know neighbor nodes
        model.mesh.computeNeighborNodes();
        uLaplace = Utils.gaussSmooth(model.mesh, uLaplace, 1, 0.5);
        uLaplace = Utils.gaussSmooth(model.mesh, uLaplace, 1, 0.5);
        uLaplace = Utils.gaussSmooth(model.mesh, uLaplace, 1, 0.5);
        plotVector(model.mesh,".","testPlotULaplaceSmooth.dat", uLaplace);

	}
	
	/**
	 * Put values of vector v from piecewise constant on element to node if necessary
	 * 
	 * @param mesh
	 * @param v
	 * @return
	 */
	public static Vector valueOnElement2Node(Mesh mesh, Vector v) {
		if(v.getDim()==mesh.getElementList().size() && v.getDim() != mesh.getNodeList().size()) {
		    Vector pOnElement = v;
		    ElementList eList = mesh.getElementList();
		    Vector pOnNode = new SparseVectorHashMap(mesh.getNodeList().size());
		    for(int i=1;i<=eList.size();i++) {
		    	NodeList nList = eList.at(i).nodes;
		    	for(int j=1;j<=nList.size();j++) {
		    		if(pOnElement.get(eList.at(i).globalIndex) > pOnNode.get(nList.at(j).globalIndex))
		    		pOnNode.set(
		    				nList.at(j).globalIndex,
		    				pOnElement.get(eList.at(i).globalIndex)
		    				);
		    	}
		    }
		    return pOnNode;
		} else {
			return v;
		}
	}	
}
