package edu.uta.futureye.function.operator;

import java.util.Map;

import com.sun.org.apache.bcel.internal.Constants;
import com.sun.org.apache.bcel.internal.generic.ConstantPoolGen;
import com.sun.org.apache.bcel.internal.generic.InstructionFactory;
import com.sun.org.apache.bcel.internal.generic.InstructionHandle;
import com.sun.org.apache.bcel.internal.generic.InstructionList;
import com.sun.org.apache.bcel.internal.generic.MethodGen;
import com.sun.org.apache.bcel.internal.generic.Type;

import edu.uta.futureye.function.basic.FC;
import edu.uta.futureye.function.intf.MathFunc;

public class FSqrt extends FUniaryOp {
	/**
	 * Construct function : sqrt(g(x))
	 * 
	 * @param g
	 */
	public FSqrt(MathFunc g) {
		super(g);
	}

	@Override
	public double apply(double... args) {
		return Math.sqrt(arg.apply(args));
	}
	
	@Override
	public MathFunc diff(String varName) {
		MathFunc ret = FC.c(0.5).M(new FPow(arg,FC.c(-0.5))).M(arg.diff(varName));
		return ret.setArgIdx(this.getArgIdxMap());
	}
	
	@Override
	public InstructionHandle bytecodeGen(String clsName, MethodGen mg,
			ConstantPoolGen cp, InstructionFactory factory,
			InstructionList il, Map<String, Integer> argsMap, int argsStartPos, 
			Map<MathFunc, Integer> funcRefsMap) {
		arg.bytecodeGen(clsName, mg, cp, factory, il, argsMap, argsStartPos, funcRefsMap);
		return  il.append(factory.createInvoke("java.lang.Math", "sqrt",
				Type.DOUBLE, 
				new Type[] { Type.DOUBLE },
		Constants.INVOKESTATIC));
	}
	
	@Override
	public String getExpr() {
		return "sqrt("+arg.getExpr()+")";
	}
}
