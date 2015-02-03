See http://code.google.com/p/futureye/

FuturEye is a Finite Element Methods Toolkit written in Java. It provides a concise, natural, easily understandable and mathematically appealing programming interface. It is also designed to solve inverse problems based on FEM. FuturEye, comes from the application in optical tomography achieved by solving inverse problems of the differential equation.

The essential components of FEM are abstracted out, such as nodes, elements, meshes, degrees of freedom and shape function etc. The data and operations of these classes are encapsulated together properly. The classes that different from other existing object-oriented FEM softwares or libraries are function classes. The behavior of the function classes in Futureye is very similar to that in mathematical context. For example algebra of functions, function derivatives and composition of functions. Especially in FEM environment, shape functions, Jacobin of coordinate transforms and numerical integration are all based on the function classes. This feature leads to a more close integration between theory and computer implementation.

FuturEye is designed to solve 1D,2D and 3D partial differential equations(PDE) of scalar and/or vector valued unknown functions. It is motivated by solving inverse problems of partial differential equations in application of optical tomography. In order to solve inverse problems, usually some forward problems must be solved first and many exterior data manipulations should be performed during the solving processes. There are many classes defined for those data operations. However, the data processes are complicated in actual applications, we can not write down all the tools for manipulating data. The design of the basic classes allows operations to all aspect of data structure directly or indirectly in an easily understanding way. This is important for users who need to write their own operations or algorithms on the bases of data structure in FuturEye. Some other existing FEM softwares or libraries may over encapsulate the data and operations for such inverse applications.

This toolkit can be used for various purposes:

*Teaching: The feature of close relation to mathematical theory of FEM will help a student to understand basic FEM concepts, e.g. shape functions, the Jacobian and assembly process.

*Research: FuturEye helps researchers quickly develop and test their models, experiment with data and algorithms. e.g. new equations, finite elements and solution methods without concerning too much about basic components in FEM programming.

*Engineering: The performance and efficiency may be unsatisfactory for real applications,if a finite element class defined in a mathematical manner is without optimization.Thanks to the interface conception in Java, we can implement the same interface in many different ways, thus a carefully optimized finite element class can be used in applications with a huge number of elements. 
