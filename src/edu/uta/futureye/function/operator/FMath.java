package edu.uta.futureye.function.operator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.uta.futureye.algebra.intf.Vector;
import edu.uta.futureye.function.AbstractMathFunc;
import edu.uta.futureye.function.FPow;
import edu.uta.futureye.function.FSqrt;
import edu.uta.futureye.function.Variable;
import edu.uta.futureye.function.VariableArray;
import edu.uta.futureye.function.basic.FC;
import edu.uta.futureye.function.basic.FX;
import edu.uta.futureye.function.basic.SpaceVectorFunction;
import edu.uta.futureye.function.intf.MathFunc;
import edu.uta.futureye.function.intf.VectorFunction;
import edu.uta.futureye.util.Constant;
import edu.uta.futureye.util.FutureyeException;
import edu.uta.futureye.util.Utils;
import edu.uta.futureye.util.container.ObjList;

public class FMath {
	//--- Predefined static objects ------------------------
	/**
	 * Use "import static edu.uta.futureye.function.operator.FMath.*" to simplify
	 * the usage of these predefined static objects
	 */
	public final static FC C0 = new FC(0.0);
	public final static FC C1 = new FC(1.0);
	public final static FC Cm1 = new FC(-1.0);
	public final static FC PI = new FC(Math.PI);
	public final static FC E = new FC(Math.E);
	
	public final static FX X = new FX(Constant.x); 
	public final static FX Y = new FX(Constant.y); 
	public final static FX Z = new FX(Constant.z); 
	
	public final static FX R = new FX(Constant.r); 
	public final static FX S = new FX(Constant.s); 
	public final static FX T = new FX(Constant.t); 
	
	public static MathFunc C(double v) {
		return FC.c(v);
	}
	
	
	//--- Basic operations ------------------------
	
	public static MathFunc sqrt(final MathFunc f) {
		FSqrt sqrt = new FSqrt();
		Map<String, MathFunc> fInners = new HashMap<String, MathFunc>();
		fInners.put(sqrt.getVarName(), f);
		return sqrt.compose(fInners);
/*		return (MathFunc)new AbstractSimpleMathFunc(f.getVarNames()) {
			@Override
			public double apply(Variable v) {
				return Math.sqrt(f.apply(v));
			}
			@Override
			public MathFunc diff(String varName) {
				return FC.c(0.5).M(pow(f,-0.5)).M(f.diff(varName));
			}
			@Override
			public int getOpOrder() {
				return OP_ORDER1;
			}
			@Override
			public String toString() {
				return "sqrt("+f.toString()+")";
			}
		};*/
	}
	
	/**
	 * f^p
	 * 
	 * @param f
	 * @param p
	 * @return
	 */
	public static MathFunc pow(final MathFunc f, final double p) {
		FPow pow = new FPow(p);
		Map<String, MathFunc> fInners = new HashMap<String, MathFunc>();
		fInners.put(pow.getVarName(), f);
		return pow.compose(fInners);
		
		
//		return new AbstractMathFunc(f.getVarNames()) {
//			@Override
//			public double apply(Variable v) {
//				return Math.pow(f.apply(v),p);
//			}
//			@Override
//			public MathFunc diff(String varName) {
//				return FC.c(p).M(pow(f,p-1)).M(f.diff(varName));
//			}
//			@Override
//			public int getOpOrder() {
//				return OP_ORDER1;
//			}
//			@Override
//			public String toString() {
//				if( (f.isConstant() && Math.abs(f.apply()) < Constant.eps))
//					return "~0.0";
//				return "("+f.toString()+")^"+p+"";
//			}
//		};
	}

	/**
	 * f1^f2
	 * @param f1
	 * @param f2
	 * @return
	 */
	public static MathFunc pow(final MathFunc f1, final MathFunc f2) {
		return new AbstractMathFunc(Utils.mergeList(f1.getVarNames(), f2.getVarNames())) {
			@Override
			public double apply(Variable v) {
				return Math.pow(f1.apply(v),f2.apply(v));
			}
			@Override
			public int getOpOrder() {
				return OP_ORDER1;
			}
			@Override
			public String toString() {
				if( (f1.isConstant() && Math.abs(f1.apply()) < Constant.eps))
					return "~0.0";
				return "("+f1.toString()+")^("+f2.toString()+")";
			}
			@Override
			public double apply(double... args) {
				return Math.pow(f1.apply(args),f2.apply(args));
			}
		};
	}	
	
	public static MathFunc abs(final MathFunc f) {
		return new AbstractMathFunc(f.getVarNames()) {
			@Override
			public double apply(Variable v) {
				return Math.abs(f.apply(v));
			}
			@Override
			public int getOpOrder() {
				return OP_ORDER1;
			}
			@Override
			public String toString() {
				return "abs("+f.toString()+")";
			}
			@Override
			public double apply(double... args) {
				return Math.abs(f.apply(args));
			}
		};
	}
	
	/**
	 * \sum{f_i} = f_1 + f_2 + ... + f_N
	 * 
	 * @param fi
	 * @return
	 */
	public static MathFunc sum(MathFunc ...fi) {
		if(fi==null || fi.length==0) {
			throw new FutureyeException("Check parameter f="+fi);
		}
		MathFunc rlt = fi[0];
		for(int i=1;i<fi.length;i++) {
			rlt = rlt.A(fi[i]);
		}
		return rlt;
	}
	
	/**
	 * c1*f1 + c2*f2
	 * 
	 * @param c1
	 * @param f1
	 * @param c2
	 * @param f2
	 * @return
	 */
	public static MathFunc linearCombination(double c1, MathFunc f1,
			double c2,MathFunc f2) {
		return new FC(c1).M(f1).A(new FC(c2).M(f2));
	}
	
	static class FLinearCombination extends AbstractMathFunc{
		double []ci;
		MathFunc []fi;
		public FLinearCombination(double []ci, MathFunc []fi) {
			int len = ci.length;
			this.ci = new double[len];
			this.fi = new MathFunc[len];
			List<String> list = new ArrayList<String>();
			for(int i=0;i<fi.length;i++) {
				this.ci[i] = ci[i];
				this.fi[i] = fi[i];
				list = Utils.mergeList(list, fi[i].getVarNames());
			}
			this.setVarNames(list);
		}
		
		@Override
		public double apply(Variable v) {
			double rlt = 0.0;
			for(int i=0;i<fi.length;i++) {
				rlt += ci[i]*fi[i].apply(v);
			}
			return rlt;
		}
		
		@Override
		public double apply(Variable v, Map<Object,Object> cache) {
			double rlt = 0.0;
			for(int i=0;i<fi.length;i++) {
				rlt += ci[i]*fi[i].apply(v,cache);
			}
			return rlt;
		}
		
		@Override
		public double[] applyAll(VariableArray v, Map<Object,Object> cache) {
			int len = v.length();
			double[] rlt = fi[0].applyAll(v,cache);
			for(int j=0;j<len;j++) 
				rlt[j] *= ci[0];
			for(int i=1;i<fi.length;i++) {
				double[] vs = fi[i].applyAll(v,cache);
				for(int j=0;j<len;j++) {
					rlt[j] += ci[i]*vs[j];
				}
			}
			return rlt;
		}
		
		@Override
		public MathFunc diff(String varName) {
			MathFunc[] fdi = new MathFunc[fi.length];
			for(int i=0;i<fi.length;i++) {
				//fdi[i] = fi[i].diff(varName).setVarNames(fi[i].getVarNames());
				fdi[i] = fi[i].diff(varName);
			}
			return new FLinearCombination(ci,fdi);
		}
		
		@Override
		public int getOpOrder() {
			return OP_ORDER3;
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(ci[0]);
			sb.append("*");
			sb.append(fi[0].toString());
			for(int i=1;i<fi.length;i++) {
				sb.append(" + ");
				sb.append(ci[i]);
				sb.append("*");
				if(OP_ORDER2 < fi[i].getOpOrder())
					sb.append("(").append(fi[i].toString()).append(")");
				else
					sb.append(fi[i].toString());
			}
			return sb.toString();
		}

		@Override
		public MathFunc copy() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public double apply(double... args) {
			double rlt = 0.0;
			for(int i=0;i<fi.length;i++) {
				rlt += ci[i]*fi[i].apply(args);
			}
			return rlt;
		}
	}
	
	/**
	 * \sum{c_i*f_i} = c_1*f_1 + c_2*f_2 + ... + c_N*f_N
	 * 
	 * @param ci
	 * @param fi
	 * @return
	 */
	public static MathFunc linearCombination(double []ci, MathFunc []fi) {
		if(ci==null || ci.length==0 || fi==null || fi.length==0) {
			throw new FutureyeException("Check parameters ci="+ci+", fi="+fi);
		} else if(ci.length != fi.length) {
			throw new FutureyeException("(ci.length="+ci.length+") != (fi.lenght="+fi.length+")");
		}
		
//		Function rlt = new FC(ci[0]).M(fi[0]);
//		for(int i=1;i<fi.length;i++) {
//			rlt = rlt.A(new FC(ci[i]).M(fi[i]));
//		}
//		return rlt;

		return new FLinearCombination(ci,fi);
	}	
	
	/**
	 * Compute gradient of <code>fun</code>
	 * 
	 * @param fun
	 * @return
	 */
	public static VectorFunction grad(MathFunc fun) {
		List<String> names = fun.getVarNames();
		VectorFunction rlt = new SpaceVectorFunction(names.size());
		for(int i=0;i<names.size();i++)
			rlt.set(i+1, fun.diff(names.get(i)));
		return rlt;
	}
	
	/**
	 * Compute gradient of <code>fun</code> with respect to variables <code>varNames</code>
	 * in case of composition of functions.
	 * 
	 * @param vars
	 * @return
	 */
	public static VectorFunction grad(MathFunc fun, String ...varNames) {
		VectorFunction rlt = new SpaceVectorFunction(varNames.length);
		for(int i=0;i<varNames.length;i++)
			rlt.set(i+1, fun.diff(varNames[i]));
		return rlt;
	}
	
	/**
	 * Compute gradient of <code>fun</code> with respect to variables <code>varNames</code>
	 * in case of composition of functions.
	 * 
	 * @param fun
	 * @param varNames
	 * @return
	 */
	public static VectorFunction grad(MathFunc fun,ObjList<String> varNames) {
		VectorFunction rlt = new SpaceVectorFunction(varNames.size());
		for(int i=1;i<=varNames.size();i++)
			rlt.set(i, fun.diff(varNames.at(i)));
		return rlt;
	}
	
	/**
	 * Compute divergence of <code>vFun</code>
	 * 
	 * @param fun
	 * @return
	 */
	public static MathFunc div(VectorFunction vFun) {
		int dim = vFun.getDim();
		MathFunc rlt = FC.C0;
		for(int i=1; i<=dim; i++) {
			MathFunc fd = (MathFunc)vFun.get(i);
			rlt = rlt.A(fd.diff(vFun.varNames().get(i-1)));
		}
		return rlt;
	}

	/**
	 * Compute divergence of <code>vFun</code> with respect to variables <code>varNames</code>
	 * in case of composition of functions.
	 * 
	 * @param fun
	 * @param varNames
	 * @return
	 */
	public static MathFunc div(VectorFunction vFun,String ...varNames) {
		MathFunc rlt = new FC(0.0);
		for(int i=0;i<varNames.length;i++) {
			MathFunc fd = (MathFunc)vFun.get(i+1);
			rlt = rlt.A(fd.diff(varNames[i]));
		}
		return rlt;
	}
	
	/**
	 * Compute divergence of <code>vFun</code> with respect to variables <code>varNames</code>
	 * in case of composition of functions.
	 * 
	 * @param fun
	 * @param varNames
	 * @return
	 */
	public static MathFunc div(VectorFunction vFun,ObjList<String> varNames) {
		MathFunc rlt = new FC(0.0);
		for(int i=1;i<=varNames.size();i++) {
			MathFunc fd = (MathFunc)vFun.get(i);
			rlt = rlt.A(fd.diff(varNames.at(i)));
		}
		return rlt;
	}
	
	public static MathFunc curl(VectorFunction vFun) {
		//TODO
		return null;
	}
	public static MathFunc curl(VectorFunction vFun, String ...varNames) {
		//TODO
		return null;
	}	
	public static MathFunc curl(VectorFunction vFun, ObjList<String> varNames) {
		//TODO
		return null;
	}
	
	
	//--- Vectors operations----------------------------------
	
	/**
	 * Vector summation
	 * 
	 * @param vi Vectors
	 * @return Vector result = v1 + v2 + ... +vn
	 */
	public static Vector sum(Vector ...vi) {
		if(vi==null || vi.length==0) {
			throw new FutureyeException("Check parameters vi="+vi);
		}
		Vector rlt = vi[0].copy();
		for(int i=1;i<vi.length;i++) {
			rlt = rlt.add(vi[i]);
		}
		return rlt;
	}
	
	/**
	 * Sum all the components of a vector
	 * @param v
	 * @return v_1 + v_2 + ... + v_n
	 */
	public static double sum(Vector v) {
		if(v == null || v.getDim() == 0)
			throw new FutureyeException("It should be at least one value in vector v!");
		double rlt = 0.0;
		int dim = v.getDim();
		for(int i=1;i<=dim;i++) {
			rlt += v.get(i);
		}
		return rlt;
	}
	
	/**
	 * Returns the natural logarithm (base e) of each component of vector v
	 * @param v
	 * @return
	 */
	public static Vector log(Vector v) {
		Vector v2 = v.copy();
		for(int i=1;i<=v.getDim();i++) {
			v2.set(i,Math.log(v.get(i)));
		}
		return v2;
	}
	
	/**
	 * Returns the base 10 logarithm of each component of vector v
	 * @param v
	 * @return
	 */
	public static Vector log10(Vector v) {
		Vector v2 = v.copy();
		for(int i=1;i<=v.getDim();i++) {
			v2.set(i,Math.log10(v.get(i)));
		}
		return v2;		
	}
	
	public static Vector exp(Vector v) {
		Vector v2 = v.copy();
		for(int i=1;i<=v.getDim();i++) {
			v2.set(i,Math.exp(v.get(i)));
		}
		return v2;
	}
	
	public static Vector abs(Vector v) {
		Vector v2 = v.copy();
		for(int i=1;i<=v.getDim();i++) {
			v2.set(i,Math.abs(v.get(i)));
		}
		return v2;
	}
	
	public static double max(Vector v) {
		if(v == null || v.getDim() == 0)
			throw new FutureyeException("It should be at least one value in vector v!");
		double max = v.get(1);
		for(int i=2;i<=v.getDim();i++) {
			double val = v.get(i);
			if(val > max) max = val;
		}
		return max;
	}
	
	public static double max(double[] a) {
		if(a == null || a.length == 0)
			throw new FutureyeException("It should be at least one value in array a!");
		double max = a[0];
		for(int i=1;i<a.length;i++) {
			if(a[i] > max) max = a[i];
		}
		return max;
	}
	
	public static double min(Vector v) {
		if(v == null || v.getDim() == 0)
			throw new FutureyeException("It should be at least one value in vector v!");
		double min = v.get(1);
		for(int i=2;i<=v.getDim();i++) {
			double val = v.get(i);
			if(val < min) min = val; 
		}
		return min;
	}
	
	public static double min(double[] a) {
		if(a == null || a.length == 0)
			throw new FutureyeException("It should be at least one value in array a!");
		double min = a[0];
		for(int i=0;i<a.length;i++) {
			if(a[i] < min) min = a[i];
		}
		return min;
	}
	
	/**
	 * y=a*x
	 * @param a
	 * @param x
	 * @return
	 */
	public static Vector ax(double a, Vector x) {
		Vector rlt = x.copy();
		return rlt.ax(a);
	}
	
	/**
	 * z = a*x+y
	 * @param a
	 * @param x
	 * @param y
	 * @return
	 */
	public static Vector axpy(double a, Vector x, Vector y) {
		Vector rlt = x.copy();
		return rlt.axpy(a, y);
	}
	
	/**
	 * zi = a*xi*yi 
	 * @param a
	 * @param x
	 * @param y
	 * @return
	 */
	public static Vector axMuly(double a, Vector x, Vector y) {
		Vector rlt = x.copy();
		return rlt.axMuly(a, y);
	}
	
	/**
	 * zi = a*xi/yi
	 * @param a
	 * @param x
	 * @param y
	 * @return
	 */
	public static Vector axDivy(double a, Vector x, Vector y) {
		Vector rlt = x.copy();
		return rlt.axDivy(a, y);

	}
	
	/**
	 * xi^exp
	 * 
	 * @param x
	 * @param exp
	 * @return
	 */
	public static Vector pow(Vector x, double exp) {
		Vector rlt = x.copy();
		for(int i=1;i<=x.getDim();i++) {
			rlt.set(i,Math.pow(x.get(i), exp));
		}
		return rlt;
	}
	
	/**
	 * base^xi
	 * 
	 * @param base
	 * @param x
	 * @return
	 */
	public static Vector pow(double base, Vector x) {
		Vector rlt = x.copy();
		for(int i=1;i<=x.getDim();i++) {
			rlt.set(i,Math.pow(base, x.get(i)));
		}
		return rlt;
		
	}
	
	//------statistic functions--------------------------------
	
	public static double mean(Vector v) {
		double mn = 0.0;
		for(int i=v.getDim()+1; --i>0;)
			mn += v.get(i);
		mn /= v.getDim();
		return mn;
	}
	
	public static double variance(Vector v) {
		double mnv = mean(v);
		double varv = 0.0, tmp;
		for(int i=v.getDim()+1; --i>0;) {
			tmp = mnv - v.get(i);
			varv += tmp*tmp;
		}
		varv /= v.getDim();
		return varv;
	}
	
	/**
	 * Standard Deviation
	 * @param v
	 * @return
	 */
	public static double SD(Vector v) {
		return Math.sqrt(variance(v));
	}
	
	/**
	 * Sample Standard Deviation
	 * @param v
	 * @return
	 */
	public static double sampleSD(Vector v) {
		double mnv = mean(v);
		double varv = 0.0, tmp;
		for(int i=v.getDim()+1; --i>0;) {
			tmp = mnv - v.get(i);
			varv += tmp*tmp;
		}
		varv /= v.getDim()-1;
		return varv;
	}
	
	public static double averageAbsoluteDeviation(Vector v) {
		double mnv = mean(v);
		double aad = 0.0;
		for(int i=v.getDim()+1; --i>0;) {
			aad += Math.abs(mnv - v.get(i));
		}
		aad /= v.getDim();
		return aad;
	}

}
