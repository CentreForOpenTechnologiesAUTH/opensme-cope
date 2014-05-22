/**
 *	Utility functions	
 */

bool isCached(wchar_t* name, vector<wchar_t*>* cache)
{
	bool flag = false;
	if (cache)
	{
		unsigned i = 0;
		while ((!flag) && (i < cache -> size()))
		{
			if (wcscmp((*cache)[i], name) == 0)
				flag = true;
			else
				i++;
		}
	}
	return flag;	
}

bool intersection(vector<wchar_t*>* list1, vector<wchar_t*>* list2)
{
	bool flag = false;
	if (list1 && list2)
	{
		unsigned i = 0, j;
		while (!flag && (i < list1 -> size()))
		{
			j = 0;
			while (!flag && (j < list2 -> size()))
			{
				if (wcscmp((*list1)[i], (*list2)[j]) == 0)
					flag = true;
				else
					j++;
			}
			if (!flag)
				i++;
		}			
	}
	return flag;
}

void printVector(vector<wchar_t*>* v)
{
	if (v)
	{
		unsigned i;
		for (i = 0; i < v -> size(); i++)
		{
			if (i > 0)
				Coutput << " ";
			Coutput << (*v)[i];
		}
		Coutput << endl;
	}
}

/**
 *	GoF patterns
 */

void FindPrototype(MethodBodyTable* mb_table, GenTable* gen_table, AssocTable* assoc_table)
{
	vector<wchar_t*>* prototypes = NULL;
		   
	prototypes = gen_table -> getSuccessors(L"Cloneable", GenTable::IMPL);

	if (prototypes)
	{
		unsigned i;
		for (i = 0; i < prototypes -> size(); i++)
		{
		       unsigned j;
			for (j = 0; j < assoc_table -> getSize(); j++)
			{
				if ((assoc_table -> getKindAt(j) == Assoc::MR) 
				&& (wcscmp(assoc_table -> getTypeAt(j), (*prototypes)[i]) == 0))
				{
				 	wchar_t* factory = assoc_table -> getClassNameAt(j);
					wchar_t* method_name = assoc_table -> getMethodNameAt(j);
					wchar_t* var_name = assoc_table -> getName(Assoc::CF, Assoc::PRIVATE, (*prototypes)[i], factory);

					AstMethodDeclaration* method_declaration = dynamic_cast<AstMethodDeclaration*>(mb_table -> getAstLocation(factory, method_name));
					AstMethodBody* method_body = method_declaration -> method_body_opt;

					Coutput << L"Prototype Factory: " << factory << endl
						     << L"Make Method: " << method_name << endl
						     << L"Prototype Var: " << var_name <<endl << endl;

					Coutput << L"AST of " << method_name << endl;
					method_body -> Print();

					if ((method_body -> NumStatements() == 1)
					&& (method_body -> Statement(0) -> kind == Ast::RETURN))
					{
						AstReturnStatement* return_statement = dynamic_cast<AstReturnStatement*>(method_body -> Statement(0));
						if (return_statement -> expression_opt -> kind == Ast::CAST)
						{
							AstCastExpression* cast_expression = dynamic_cast<AstCastExpression*>(return_statement -> expression_opt);
							AstMethodInvocation* method_invocation =  dynamic_cast<AstMethodInvocation*>(cast_expression -> expression);

							if ((wcscmp(method_invocation -> identifier_token_string, L"clone") == 0)
							&& (wcscmp(dynamic_cast<AstName*>(method_invocation -> base_opt) -> identifier_token_string, var_name) == 0))
								Coutput << L"Yoohoo" << endl;
						}
					}
					Coutput << endl << endl;
				}
			}
		}
	}
}

void FindSingleton(MethodBodyTable* mb_table, GenTable* gen_table, AssocTable* assoc_table)
{
	map<wchar_t*, wchar_t*> candidates;

	unsigned i;
	for (i = 0; i < mb_table -> getSize(); i++) 
	{	
		if (mb_table -> getAstLocationAt(i) -> kind == Ast::CONSTRUCTOR)
		{
			vector<wchar_t*>* modifiers = mb_table -> getModifiersAt(i);
                     if (modifiers
			&& (wcscmp((*modifiers)[0], L"private") == 0))
			{
				candidates.insert(pair<wchar_t*, wchar_t*>(mb_table -> getClassNameAt(i), mb_table -> getPackageNameAt(i)));
                     }
		}
	}

	for (i = 0; i < gen_table -> getSize(); i++) 
	{
		if (gen_table -> getKindAt(i) == Gen::ABSTRACT)
		{
			candidates.insert(pair<wchar_t*, wchar_t*>(gen_table -> getClassNameAt(i), gen_table -> getPackageNameAt(i)));
		}
	}

	if (candidates.size() > 0)
	{
		map<wchar_t*, wchar_t*>::iterator p;

		for (p = candidates.begin(); p != candidates.end(); p++)
		{
			wchar_t* class_name = p -> first;	
			wchar_t* package_name = p -> second;
			char* file_name = gen_table -> getFileName(class_name, package_name);
			wchar_t* instance_name = assoc_table -> getName(Assoc::CF, Assoc::PRIVATE, class_name, class_name);
			wchar_t* get_method = assoc_table -> getName(Assoc::CM, Assoc::PUBLIC, class_name, class_name);

			if (instance_name && get_method)
			{
				AstMethodDeclaration *method_declaration = dynamic_cast<AstMethodDeclaration*>(mb_table -> getAstLocation(class_name, get_method));
				AstMethodBody *method_body = method_declaration -> method_body_opt;

				if (method_body -> returnsVar(instance_name))
				{
					EnvTable *env = new EnvTable();
					env -> addEnvironment(instance_name, Env::INIT);
					method_body -> simulate(env);
					if (env -> getState(instance_name) == Env::INIT)
						Coutput << ((method_declaration -> isSynchronized()) ? L"Multithreaded " : L"")
							      << L"Singleton Pattern"
							      << endl
							      << class_name << L" is a Singleton class"
							      << endl
							      << instance_name << L" is the Singleton instance"
							      << endl 
							      << get_method << L" returns a " << instance_name
							      << endl
							      << L"File location: " << file_name
							      << endl
							      << ((method_declaration -> isSynchronized()) ? L"Double-checked Locking not used.\n" : L"\n")							      
							      << endl;
					else
					{
						Coutput  << L"Singleton Pattern"
							      << endl
							      << class_name << L" is a Singleton class"
							      << endl
							      << instance_name << L" is the Singleton instance"
							      << endl 
							      << get_method << L" returns a " << instance_name
							      << endl
							      << L"File location: " << file_name
							      << endl;
						Coutput << L"Warning: " << instance_name << L" is modified more than once." << endl << endl;
					}
					delete env;
				}
				else
				{
				/*
					Coutput  << L"Singleton Pattern"
							      << endl
							      << class_name << L" is a Singleton class"
							      << endl
							      << instance_name << L" is the Singleton instance"
							      << endl 
							      << get_method << L" returns a " << instance_name
							      << endl
							      << L"File location: " << file_name
							      << endl;
					Coutput << L"Warning: " << instance_name << L" is not returned in " << get_method << endl << endl;
				*/
				}
			}
		}
	}
}

void FindChainOfResponsibility(MethodBodyTable* mb_table, GenTable* gen_table, AssocTable* assoc_table)
{
	vector<wchar_t*>* cache = NULL;
	unsigned i;
	for (i = 0; i< assoc_table -> getSize(); i++)
	{
		if ((assoc_table -> getKindAt(i) == Assoc::MI)	
		&& (wcscmp(assoc_table -> getNameAt(i), assoc_table -> getMethodNameAt(i)) == 0))
		{
			wchar_t* class_name = assoc_table -> getClassNameAt(i);

			if (!isCached(class_name, cache))
			{
				wchar_t* package_name = assoc_table -> getPackageNameAt(i);				
				wchar_t* method_name = assoc_table -> getMethodNameAt(i);
				wchar_t* var_name = assoc_table -> getTypeAt(i);
				wchar_t* type_name = (var_name)
					? assoc_table -> getType(Assoc::IM, Assoc::PRIVATE, var_name, package_name, class_name)
					: NULL;

		
				if ((var_name) && (type_name) && (gen_table -> getSuccessors(class_name, GenTable::SUBC)))
				{
					bool pass = false;
					if (wcscmp(class_name,  type_name) != 0)
					{
						vector<wchar_t*>* a1 = gen_table -> getAncestors(GenTable::SUBC, class_name, package_name);
						vector<wchar_t*>* a2 = gen_table -> getAncestors(GenTable::SUBC, type_name, package_name);
						vector<wchar_t*>* b1 = gen_table -> getAncestors(GenTable::IMPL, class_name, package_name);
						vector<wchar_t*>* b2 = gen_table -> getAncestors(GenTable::IMPL, type_name, package_name);

						if (intersection(a1, a2) ||intersection(b1, b2))
							pass = true;
						
						if (a1) delete a1;
						if (a2) delete a2;
						if (b1) delete b1;
						if (b2) delete b2;
					}
					else
						pass = true;

					AstMethodDeclaration* method_declaration = dynamic_cast<AstMethodDeclaration*>(mb_table -> getAstLocation(class_name, method_name));
					AstMethodBody* method_body = method_declaration -> method_body_opt;

					if ((pass)
					&& (method_body -> NumExecutionPaths() > 1)
					&& (method_body -> NumDelegation() == 1)
					&& (method_body -> NumDelegation(var_name, method_name, method_declaration -> NumFormalParameters()) == 1))
					{
						Coutput << L"Chain of Responsibility Pattern" << endl;
						Coutput << class_name << L" is a Chain of Responsibility Handler class" << endl;
						Coutput << method_name << L" is a handle operation" << endl;
						Coutput << var_name << L" of type " << type_name << L" propogates the request" << endl;
				
						char* file_name = gen_table -> getFileName(class_name, package_name);
						Coutput << L"File Location: " << file_name << endl << endl;


						//method_declaration -> Print();

						if (!cache)
							cache = new vector<wchar_t*>();
						cache -> push_back(class_name);
					}
				}
			}
		}
	}

}

void FindFlyweight(MethodBodyTable* mb_table, GenTable* gen_table, AssocTable* assoc_table)
{
	// Collecting possible flyweight pools
	vector<wchar_t*>* pools = NULL;
	unsigned i;
	for (i = 0; i < assoc_table -> getSize(); i++)
	{
		if ((assoc_table -> getKindAt(i) == Assoc::IM)
		&& (assoc_table -> getModeAt(i) == Assoc::PRIVATE)
		&& (wcscmp(assoc_table -> getTypeAt(i), L"Hashtable") == 0))
		{
			wchar_t* class_name = assoc_table -> getClassNameAt(i);

			if (!pools)
			{
				pools = new vector<wchar_t*>();
				pools -> push_back(class_name);
			}
			else if (!isCached(class_name, pools))
			{
				pools -> push_back(class_name);
			}				
		}
	}

	// Look for possible flyweight factories
	for (i = 0; i < assoc_table -> getSize(); i++)
	{
		if ((assoc_table -> getKindAt(i) == Assoc::MP)
		&& (isCached(assoc_table ->getTypeAt(i), pools)))
		{
			wchar_t* package_name = assoc_table -> getPackageNameAt(i);
			wchar_t* flyweight_factory = assoc_table -> getClassNameAt(i);
			wchar_t* get_flyweight = assoc_table -> getMethodNameAt(i);
			
			AstMethodDeclaration *method_declaration = dynamic_cast<AstMethodDeclaration*>
													(mb_table -> getAstLocation(flyweight_factory, get_flyweight));
			AstMethodBody *method_body = method_declaration -> method_body_opt;

			// Given:
			//   - type of flyweight class
			//   - flyweight pool type and var_name

		  	wchar_t* flyweight = method_declaration -> getReturnType();
			wchar_t* pool = assoc_table ->getTypeAt(i);
			wchar_t* pool_name = assoc_table -> getNameAt(i);

			if (method_body
			&& (!method_declaration -> isPrimitiveType(flyweight))
			&& (wcscmp(pool, flyweight_factory) != 0))
			{
				//Coutput  << "package name: " << package_name << endl
				//	<< "class name: " << flyweight_factory << endl
				//	<< "method name: " << get_flyweight << endl;
					


				// Check for variables of type "flyweight" declared in this method
				vector<wchar_t*>* vars = method_body -> getVariables(flyweight);

				if (vars && vars -> size() == 1)
				{
					wchar_t* temp = (*vars)[0];

					//Then make sure that this var is returned by the method
					if (method_body -> returnsVar(temp))
					{
						// If not, reject this class as a flyweight factory.
	
						// Look for a specific statechart on the var that gets returned from this method. 
						// (1) SET --yes--> RETURN
						// (2) SET --no --> CREATE ----> SET ----> RETURN


						Statechart* statechart = method_body -> getStatechart(temp);

						unsigned i = 0;
						if ((statechart -> getStateKindAt(0) == State::SET)
						&& (isCached( pool_name, statechart -> getStateParticipantsAt(0)))
						&& (statechart -> getStateKindAt(1) == State::CONDITION)
						&& (statechart -> getStateKindAt(2) == State::CREATE)
						&& ((statechart -> getStateKindAt(3) == State::SET))						
						&& (isCached( pool_name, statechart -> getStateParticipantsAt(3)))
						&& ((statechart -> getStateKindAt(4) == State::RETURN)))						
						{

						Coutput << "Flyweight Pattern." << endl;
						Coutput << flyweight_factory
							     <<  " is a Flyweight factory class. " 
							     << endl;

						Coutput << pool
							     << " is a flyweight object pool."
							     << endl;
						
						Coutput << get_flyweight
							     <<  " returns a flyweight object."
							     << endl;

						if (method_declaration -> isSynchronized())
							Coutput << "Consider using Double-checked Locking." << endl;


						Coutput << "File location: " << gen_table -> getFileName(flyweight_factory, package_name) << endl;
						Coutput << endl << endl;
						}
					}
				}
			}			
		}
	}
}

void FindStrategy(MethodBodyTable* mb_table, GenTable* gen_table, AssocTable* assoc_table) 
{
	unsigned i;
	vector<wchar_t*> candidates;
	vector<wchar_t*> strategies;

	// Collect a list of possible Strategy base classes.
	for (i = 0; i < gen_table -> getSize(); i++)
	{
		if ((gen_table -> getKindAt(i) == Gen::ABSTRACT)
		|| (gen_table -> getKindAt(i) == Gen::INTERFACE))
			strategies.push_back(gen_table -> getClassNameAt(i));			
	}
/*
if (assoc_table -> isInvoked(L"layoutMgr", L"Container"))
	Coutput << "*****yoohoo" << endl;
*/
	AstMethodDeclaration *method_declaration = dynamic_cast<AstMethodDeclaration*>
													(mb_table -> getAstLocation(L"Container", L"invalidate"));
	AstMethodBody *method_body = method_declaration -> method_body_opt;
	method_declaration -> Print();


	// Check if there's an association to these base classes.
	if (strategies.size() > 0)
	{
		for (i = 0; i < assoc_table -> getSize(); i++)
		{			
/*
			if ((assoc_table -> getKindAt(i) == Assoc::IM)
			&& (!isCached(assoc_table -> getClassNameAt(i), &candidates))
			&& (isCached(assoc_table -> getTypeAt(i), &strategies))
			&& (assoc_table -> isInvoked(assoc_table -> getNameAt(i), assoc_table -> getClassNameAt(i))))
			{
				Coutput << "Strategy pattern."
					<< endl
					<< assoc_table -> getClassNameAt(i)
					<< L" is the Context class"
					<< endl
					<< assoc_table -> getTypeAt(i)
					<< L" is the strategy class"
					<< endl
					<< L"File Location: "
					<< gen_table -> getFileName(assoc_table -> getClassNameAt(i), assoc_table -> getPackageNameAt(i))
					<< endl
					<< endl;

				candidates.push_back(assoc_table -> getClassNameAt(i));
			}
*/			
		}
	}
}

