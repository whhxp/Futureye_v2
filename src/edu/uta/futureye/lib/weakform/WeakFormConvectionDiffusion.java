package edu.uta.futureye.lib.weakform;

import edu.uta.futureye.core.Element;
import edu.uta.futureye.function.basic.FC;
import edu.uta.futureye.function.basic.SpaceVectorFunction;
import edu.uta.futureye.function.intf.MathFun;
import edu.uta.futureye.function.intf.VectorFunction;
import edu.uta.futureye.function.operator.FMath;
import edu.uta.futureye.util.Utils;

/**
 * Convection-diffusion equation
 *   \frac{\partial{c}}{\partial{t}} = 
 *   		\nabla\cdot(k\nabla{c}) - \mathbf{v}\cdot\nabla{c} + f
 * 
 * Time discrete form:
 *   Let:
 *   \frac{\partial{c}}{\partial{t}} = (c^{n+1} - c^n)/Dt
 *   We have,
 *   -Dt*\nabla\cdot(k\nabla{c^{n+1}}) + Dt*\mathbf{v}\dot\nabla{c^{n+1}} + c^{n+1} = Dt*f + c^n
 * 
 * Weak form:
 *   Let c^{n+1} := u
 *   Dt*(k*\nabla{u},\nabla{w}) + Dt*( (v1*u_x,w)+(v2*u_y,w)+(v3*u_z,w) ) + b*(u,w) = (Dt*f + c^n,w)
 *   
 * where
 *   c=c(x,y,z,t): particles or energy(e.g. salt density, Heat...) are transferred inside 
 *                 a physical system due to two processes: diffusion and convection
 *   k=k(x,y,z): the diffusion coefficient
 *   \mathbf{v}=(v1,v2,v3)':  the convection velocity vector
 *   f=f(x,y,z): the source term
 *   Dt: the time step size
 *   b: b=1 or b=b(x,y,z), if the equation has the term a*c, where a(x,y,z)=b(x,y,z)-1
 * 
 * Boundary condition
 *   c = c0,                  on \Gamma1 (Dirichlet)
 *   d*c + k*c_n = g,         on \Gamma2 (Robin)
 *
 * The following weak form just gives one step computation of c from c_n to c_n+1. 
 *   
 * @author liuyueming
 *
 */
public class WeakFormConvectionDiffusion extends AbstractScalarWeakForm {
	protected MathFun g_f = null;
	protected MathFun g_k = null;
	protected MathFun g_b = null;
	protected MathFun g_g = null;
	protected MathFun g_d = null;
	protected VectorFunction g_v = null;
	protected MathFun g_cn = null;
	protected double Dt;

	/**
	 * 
	 * @param k
	 * @param b
	 * @param cn: c_n
	 * @param Dt
	 */
	public void setParam(MathFun k,MathFun b, MathFun cn, double Dt) {
		this.g_k = k;
		this.g_b = b;
		this.g_cn = cn;
		this.Dt = Dt;
	}
	
	public void setConvectionVelocity(VectorFunction v) {
		this.g_v = v;
	}
	
	//right hand side function (source term)
	public void setF(MathFun f) {
		this.g_f = f;
	}
	
	//Robin:  d*u + k*u_n= g (自然边界：d==k, g=0)
	public void setRobin(MathFun g,MathFun d) {
		this.g_g = g;
		this.g_d = d;
	}

	@Override
	public MathFun leftHandSide(Element e, ItemType itemType) {
		 if(itemType==ItemType.Domain)  {
			 //Interplate functions on element e
			MathFun fk = Utils.interpolateOnElement(g_k,e);
			MathFun fb = Utils.interpolateOnElement(g_b,e);
			VectorFunction fv = new SpaceVectorFunction(g_v.getDim());
			for(int dim=1;dim<=g_v.getDim();dim++)
				fv.set(dim, Utils.interpolateOnElement(g_v.get(dim),e));
			
			//Dt*(k*\nabla{u},\nabla{w}) + 
			//Dt*( (v1*u_x,w)+(v2*u_y,w)+(v3*u_z,w) ) + 
			//b*(u,w)
			MathFun integrand = null;
			integrand = fk.M(
							FMath.grad(u,u.innerVarNames()).
								dot(
							FMath.grad(v,v.innerVarNames()))
						).A(
							fv.dot(FMath.grad(u,u.innerVarNames()))
						).M(FC.c(Dt)).A(
							fb.M(u).M(v)
						);
			return integrand;
		}
		else if(itemType==ItemType.Border) {
			if(g_d != null) {
				Element be = e;
				MathFun fd = Utils.interpolateOnElement(g_d, be);
				MathFun borderIntegrand = fd.M(u.M(v));
				return borderIntegrand;
			}
		}
		return null;
	}

	@Override
	public MathFun rightHandSide(Element e, ItemType itemType) {
		if(itemType==ItemType.Domain)  {
			//(Dt*f + c_n,w)
			MathFun ff = Utils.interpolateOnElement(g_f, e);
			MathFun fcn = Utils.interpolateOnElement(g_cn, e);
			MathFun integrand = ff.M(FC.c(Dt)).A(fcn).M(v);
			return integrand;
		} else if(itemType==ItemType.Border) {
			Element be = e;
			MathFun fq = Utils.interpolateOnElement(g_g, be);
			MathFun borderIntegrand = fq.M(v);
			return borderIntegrand;
		}
		return null;		
	}
}