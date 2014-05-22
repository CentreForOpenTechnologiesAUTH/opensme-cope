// $Id: control.cpp,v 1.88 2006/03/15 06:19:48 shini Exp $
//
// This software is subject to the terms of the IBM Jikes Compiler
// License Agreement available at the following URL:
// http://ibm.com/developerworks/opensource/jikes.
// Copyright (C) 1996, 2004 IBM Corporation and others.  All Rights Reserved.
// You must accept the terms of that agreement to use this software.
//
#include "control.h"
#include "scanner.h"
#include "parser.h"
#include "semantic.h"
#include "error.h"
#include "bytecode.h"
#include "case.h"
#include "option.h"
#include <fstream>
#include <iomanip>
#include <dlfcn.h>
#include <stdlib.h>

#ifdef HAVE_JIKES_NAMESPACE
namespace Jikes { // Open namespace Jikes block
#endif

int counter1, counter2, counter3;
int nSingleton, nCoR, nBridge, nStrategy, nState, nFlyweight, nComposite, nMediator, nTemplate, nFactoryMethod, nAbstractFactory, nVisitor, nDecorator, nObserver, nProxy, nAdapter, nFacade;
bool PINOT_DEBUG;

SymbolSet mediators;
int nMediatorFacadeDual = 0, nFlyweightGoFVersion = 0, nImmutable = 0;
/**
 *	Utility functions	
 */

ContainerType *Utility::IdentifyContainerType(VariableSymbol *vsym)
{
	TypeSymbol *type = vsym->Type();

	if (type->Primitive())
		return NULL;

	if (type->IsArray())
		// can be 2D, 3D, etc.
		return new ArrayContainer(vsym);
	
	if (strcmp(type->fully_qualified_name->value, "java/util/Vector") == 0)
		return new VectorContainer(vsym);
	else if (strcmp(type->fully_qualified_name->value, "java/util/ArrayList") == 0)
		return new ArrayListContainer(vsym);
	else if (strcmp(type->fully_qualified_name->value, "java/util/LinkedList") == 0)
		return new ArrayListContainer(vsym);
	

       if (type->supertypes_closure)
       {
		Symbol *sym= type->supertypes_closure->FirstElement();
		while(sym)
		{
			if (strcmp(sym->TypeCast()->fully_qualified_name->value, "java/util/Map") == 0)
				return new MapContainer(vsym);
			else if (strcmp(sym->TypeCast()->fully_qualified_name->value, "java/util/Collection") == 0)
				return new CollectionContainer(vsym);
			sym = type->supertypes_closure->NextElement();
		}
       }
	return NULL;	
}
void Utility::RemoveJavaBaseClass(SymbolSet& set)
{
	Symbol *sym = set.FirstElement();
	while(sym)
	{
		if (strcmp(sym->TypeCast()->fully_qualified_name->value, "java/lang/Object") == 0)
		{
			set.RemoveElement(sym);
			break;
		}
		sym = set.NextElement();
	}
}
void Utility::RemoveBuiltinInterfaces(SymbolSet& set)
{
	SymbolSet temp;
	Symbol *sym = set.FirstElement();
	while(sym)
	{
		if (sym->TypeCast()->file_symbol->IsJava())
			temp.AddElement(sym);
		sym = set.NextElement();
	}
	set.Intersection(temp);
}
TypeSymbol *Utility::GetTypeSymbol(Symbol *sym)
{
	if (sym->Kind() == Symbol::TYPE)
		return sym->TypeCast();
	else if (sym->Kind() == Symbol::VARIABLE)
		return sym->VariableCast()->Type();
	else if (sym->Kind() == Symbol::METHOD)
		return sym->MethodCast()->Type();
	else
		return NULL;
}
AstExpression *Utility::RemoveCasting(AstExpression *expr)
{
	if (expr->kind == Ast::CAST)
		return RemoveCasting(expr->CastExpressionCast()->expression);
	else if (expr->kind == Ast::PARENTHESIZED_EXPRESSION)
		return RemoveCasting(expr->ParenthesizedExpressionCast()->expression);
	else
		return expr;
}
void Utility::Intersection(vector<signed>& a, vector<signed>& b, vector<signed>& c)
{
	for (unsigned i = 0; i < a.size(); i++)
		for (unsigned j = 0; j < b.size(); j++)
			if (a[i] == b[j])
				c.push_back(a[i]);
}
void Utility::RemoveDuplicates(vector<signed>& a)
{
	vector<signed> b;

	for (unsigned i = 0; i < a.size(); i++)
	{
		unsigned j = 0;
		while ((j < b.size()) && (a[i] != b[j])) j++;
		if (j == b.size())
			b.push_back(a[i]);
	}
	a.swap(b);
}
bool Utility::Aliasing(VariableSymbol *v1, VariableSymbol *v2)
{
	if (!v1->aliases)
		return false;
	else if (v1->aliases->IsElement(v2))
		return true;
	else
	{
		Symbol *sym = v1->aliases->FirstElement();
		while(sym)
		{
			// reach two-hops only
			if (sym->VariableCast()->aliases && sym->VariableCast()->aliases->IsElement(v2))
				return true;
			sym = v1->aliases->NextElement();
		}
		return false;
	}
}

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
void PrintSingletonXMI(TypeSymbol *class_sym , VariableSymbol *instance_sym, MethodSymbol *method_sym);

void FindPrototype(MethodBodyTable* mb_table, GenTable* gen_table, AssocTable* assoc_table)
{
	vector<wchar_t*>* prototypes = NULL;
		   
	prototypes = gen_table -> getSuccessors(L"Cloneable", GenTable::IMPL);

	if (prototypes)
	{
		unsigned i;
		for (i = 0; i < prototypes -> size(); i++)
		{
		       int j;
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
void FindSingleton1(ClassSymbolTable *cs_table, StoragePool *ast_pool)
{
	if (PINOT_DEBUG)
		Coutput << "Identifying the Singleton Pattern" << endl;
	
	for (unsigned c = 0; c < cs_table -> size(); c++) 
	{		
		TypeSymbol *unit_type = (*cs_table)[c];

		if (PINOT_DEBUG)
			Coutput << "Analyzing class: " << unit_type->fully_qualified_name->value << endl;

		//if (unit_type->Anonymous()) break;
		
		bool instantiable = true; //for Singleton pattern, either class is abtract or ctor is private
		VariableSymbol *instance = NULL;
		MethodSymbol *GetInstance = NULL;
		
		if (unit_type -> ACC_ABSTRACT())
			instantiable = false;

		for (unsigned i = 0; i < unit_type->NumVariableSymbols(); i++)
		{
			VariableSymbol *vsym = unit_type->VariableSym(i);
			if (vsym->ACC_PRIVATE() && vsym->ACC_STATIC() && (vsym->Type() == unit_type))
			{
				instance = vsym;
				break;
			}			
		}

		for (unsigned i = 0; (instantiable || !GetInstance) && (i < unit_type->NumMethodSymbols()); i++)
		{
			MethodSymbol *msym = unit_type->MethodSym(i);
			if (msym->declaration)
			{
			if (msym->declaration->kind == Ast::CONSTRUCTOR)
			{
				if (msym->ACC_PRIVATE())
					instantiable = false;
			}
			else if (msym->declaration->kind == Ast::METHOD)
			{
				if (msym->ACC_PUBLIC() && msym->ACC_STATIC() && (msym->Type() == unit_type))
					GetInstance = msym;
			}
			}
		}

		if (!instantiable && instance && GetInstance)
		{
			// Do the behavioral analysis

			SingletonAnalysis singleton(instance, GetInstance, ast_pool);
			//Coutput << unit_type->file_symbol->FileName() << endl;
			if (singleton.ReturnsSingleton())
			{
				Coutput << ((GetInstance-> ACC_SYNCHRONIZED()) ? "" : "") << "Singleton Pattern." << endl
					      << unit_type->Utf8Name() << " is a Singleton class" << endl
					      << instance->Utf8Name() << " is the Singleton instance" << endl 
					      << GetInstance->Utf8Name() << " creates and returns " << instance->Utf8Name() << endl
					      << "File location: " << unit_type->file_symbol->FileName() << endl
					      << ((GetInstance->ACC_SYNCHRONIZED()) ? "Double-checked Locking not used.\n" : "\n")	 << endl;
				nSingleton++;
			}
			singleton.CleanUp();
		}
	}
}
void FindSingleton(ClassSymbolTable *cs_table, MethodSymbolTable* ms_table)
{
	vector<TypeSymbol*> candidates_t;
	
	for (unsigned i = 0; i<ms_table->size(); i++) 
	{	
		MethodSymbol *method = (*ms_table)[i];
		if (method -> declaration -> kind == Ast::CONSTRUCTOR)
		{
			if (method -> ACC_PRIVATE())
			{
				TypeSymbol *unit_type = method -> containing_type;
				candidates_t.push_back(unit_type);
			}
		}
	}

	unsigned c;
	for (c = 0; c < cs_table -> size(); c++) 
	{		
		TypeSymbol *unit_type = (*cs_table)[c];
		if (unit_type -> ACC_ABSTRACT())
		{
			candidates_t.push_back(unit_type);
		}
	}

	if (candidates_t.size() > 0)
	{
		unsigned i;
		for (i = 0; i < candidates_t.size(); i++)
		{
			AstClassBody *class_body = candidates_t[i] -> declaration;

			// find the class variable
			VariableSymbol *instance_sym = NULL;
			for (unsigned j = 0; !instance_sym && (j < class_body -> NumClassVariables()); j++)
			{
				AstFieldDeclaration* field_decl = class_body -> ClassVariable(j);
				TypeSymbol *type = field_decl -> type -> symbol;
				if ((type == candidates_t[i]) && (field_decl -> NumVariableDeclarators() == 1))
				{
					AstVariableDeclarator* vd = field_decl -> VariableDeclarator(0);
					if (vd -> symbol -> ACC_PRIVATE())
						instance_sym = vd -> symbol;
				}
			}

			// find the get_instance method
			MethodSymbol *get_method_sym = NULL;
			for (unsigned j = 0; !get_method_sym && (j < class_body -> NumMethods()); j++)
			{
				AstMethodDeclaration* method = class_body -> Method(j);
				if ((method -> method_symbol)
				&& (method -> method_symbol -> Type() == candidates_t[i])
				&& (method -> method_symbol -> ACC_STATIC())
				&& (method -> method_symbol -> ACC_PUBLIC()))
					get_method_sym = method -> method_symbol;

			}
			
			if (instance_sym && get_method_sym)
			{			
				AstMethodDeclaration *method_declaration = get_method_sym -> declaration -> MethodDeclarationCast();
				AstMethodBody *method_body = method_declaration -> method_body_opt;

				wchar_t *instance_name = const_cast<wchar_t*>(instance_sym -> Name());
				if (method_body -> returnsVar(instance_name))
				{
					EnvTable *env = new EnvTable();
					env -> addEnvironment(instance_name, Env::INIT);
					method_body -> simulate(env);
					if (env -> getState(instance_name) == Env::INIT)
					{
					/*
						char* file_name = method_sym -> containing_type -> file_symbol -> FileName();

						TypeSymbol *class_sym = NULL;
						for (unsigned i = 0; !class_sym && (i < candidates_t.size()); i++)
						if (wcscmp(candidates_t[i] -> Name(), class_name) == 0)
							class_sym = candidates_t[i];

						VariableSymbol *instance_sym = NULL;
						for (unsigned j = 0; !instance_sym && (j <  class_sym -> declaration -> NumClassVariables()); j++)
						{
							AstFieldDeclaration* field_decl = class_sym -> declaration -> ClassVariable(j);
							for (unsigned vi = 0; vi < field_decl -> NumVariableDeclarators(); vi++)
							{
								AstVariableDeclarator* vd = field_decl -> VariableDeclarator(vi);
								if (wcscmp(vd -> symbol -> Name(), instance_name) == 0)
									instance_sym = vd -> symbol;
						        }
						}
					*/
						//PrintSingletonXMI(class_sym , instance_sym, method_sym);
						Coutput << ((get_method_sym-> ACC_SYNCHRONIZED()) ? L"Multithreaded " : L"")
							      << L"Singleton Pattern."
							      << endl
							      << candidates_t[i] -> Utf8Name() << " is a Singleton class"
							      << endl
							      << instance_sym -> Utf8Name() << " is the Singleton instance"
							      << endl 
							      << get_method_sym -> Utf8Name() << " returns a " << instance_sym -> Utf8Name()
							      << endl
							      << "File location: " << candidates_t[i] -> file_symbol -> FileName()
							      << endl
							      << ((get_method_sym-> ACC_SYNCHRONIZED()) ? L"Double-checked Locking not used.\n" : L"\n")							      
							      << endl;
						nSingleton++;
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
						Coutput << L"Warning: " << instance_name << L" is modified more than once." << endl << endl;
					*/
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

void FindChainOfResponsibility(ClassSymbolTable *cs_table, MethodSymbolTable* ms_table, DelegationTable *d_table, StoragePool *ast_pool)
{
	if (PINOT_DEBUG)
		Coutput << "Identifying Cor and Decorator" << endl;

	SymbolSet CoR_cache;
	SymbolSet D_cache;
	
	vector<MethodSymbol*> cache;
	int i;
	for (i = 0; i< d_table -> size(); i++)
	{
		DelegationEntry *entry = d_table -> Entry(i);

		if (PINOT_DEBUG)
			Coutput << "Analyzing delegation: " << entry->enclosing->Utf8Name() << " -> " << entry->method->Utf8Name() << endl;

		if (entry-> vsym
		&& (entry->from->IsSubtype(entry->vsym->Type()) || entry->vsym->Type()->IsSubtype(entry->from))
		&& (!entry->vsym->IsLocal() || entry->from->Shadows(entry->vsym))
		&& ((strcmp(entry -> method -> Utf8Name(), entry -> enclosing -> Utf8Name()) == 0) || (entry->method == entry->enclosing))
		&& (strcmp(entry->enclosing->SignatureString(), entry->method->SignatureString()) == 0)
		)
		{
			unsigned j = 0;
			for (; (j < cache.size()) && (cache[j] != entry->enclosing) ; j++);
			if (j == cache.size())
			{
					ChainAnalysis chain_analysis(entry->vsym, entry->enclosing, ast_pool);
					ChainAnalysis::ResultTag result = chain_analysis.AnalyzeCallChain();
					if (result == ChainAnalysis::CoR)
					{
						Coutput << "Chain of Responsibility Pattern." << endl;
						Coutput << entry -> from -> Utf8Name() << " is a Chain of Responsibility Handler class" << endl;
						Coutput << entry -> enclosing -> Utf8Name() << " is a handle operation" << endl;
						Coutput << entry -> vsym -> Utf8Name()  << " of type " << entry -> vsym -> Type() -> Utf8Name() << " propogates the request" << endl;

						char* file_name = entry -> enclosing -> containing_type -> file_symbol -> FileName();						
						Coutput << L"File Location: " << file_name << endl << endl;
						cache.push_back(entry->enclosing);
						CoR_cache.AddElement(entry -> vsym -> Type());
						//nCoR++;
					}
					else if (result == ChainAnalysis::DECORATOR)
					{
							Coutput << "Decorator Pattern." << endl;
							Coutput << entry -> from -> Utf8Name() << " is a Decorator class" << endl;
							Coutput << entry -> enclosing -> Utf8Name() << " is a decorate operation" << endl;
							Coutput << entry -> vsym -> Utf8Name()  << " of type " << entry -> vsym -> Type() -> Utf8Name() << " is the Decoratee class" << endl;

							char* file_name = entry -> enclosing -> containing_type -> file_symbol -> FileName();						
							Coutput << L"File Location: " << file_name << endl << endl;
							cache.push_back(entry->enclosing);
							D_cache.AddElement(entry -> vsym -> Type());
							//nDecorator++;
					}

					chain_analysis.CleanUp();
			}
		}
	}
	nCoR += CoR_cache.Size();
	CoR_cache.Print();
	nDecorator += D_cache.Size();
	D_cache.Print();
}

void FindBridge(ClassSymbolTable *cs_table, DelegationTable *d_table)
{
	multimap<TypeSymbol*,TypeSymbol*> cache;
	multimap<TypeSymbol*,TypeSymbol*> negatives;
	
	int i;
	for (i = 0; i < d_table -> size(); i++)
	{
		DelegationEntry *entry = d_table -> Entry(i);

		if (//(entry -> from -> ACC_ABSTRACT()) &&
		entry->from->subtypes
		&& entry->from->subtypes->Size()
		&& entry->to->ACC_INTERFACE()
		&& entry->from->file_symbol->IsJava()
		&& entry->to->file_symbol->IsJava()		
		&& (!cs_table -> Converge(entry -> from, entry -> to)))
		{
			multimap<TypeSymbol*,TypeSymbol*>::iterator p = cache.begin();
			for (; (p != cache.end()) && ((p -> first != entry -> from) || (p -> second != entry -> to)); p++);
			if ((p == cache.end()) && !d_table -> DelegatesSuccessors(entry -> from, entry -> to))
			{
				nBridge++;
				cache.insert(pair<TypeSymbol*,TypeSymbol*>(entry -> from, entry -> to));
				Coutput << "Bridge Pattern." << endl;
				Coutput << entry -> from -> Utf8Name()
					<< " is abstract.\n"
					<< entry -> to -> Utf8Name()
					<< " is an interface.\n"
					<< entry -> from -> Utf8Name()
					<< " delegates "
					<< entry -> to -> Utf8Name()
					<< "."
					<< endl
					<< "File Location: "
					<< entry -> from -> file_symbol -> FileName()
					<< ",\n               "
					<< entry -> to -> file_symbol -> FileName()
					<< endl;
/*
				Coutput << "Subclasses of " << entry -> from -> Utf8Name() << ": ";
				cs_table -> PrintSubclasses(entry -> from);
				
				Coutput << "Subtypes of " << entry -> to -> Utf8Name() << ": ";				
				cs_table -> PrintSubtypes(entry -> to);
				
				Coutput << "Subinterfaces of " << entry -> to -> Utf8Name() << ": ";				
				cs_table -> PrintSubinterfaces(entry -> to);

				d_table -> Delegates(entry -> to, entry -> from);
				d_table -> ShowDelegations(entry -> from, entry -> to);
*/
				Coutput << endl;
				
			}
		}
	}
}

void FindFlyweight(MethodBodyTable* mb_table, GenTable* gen_table, AssocTable* assoc_table)
{
	// Collecting possible flyweight pools
	vector<wchar_t*>* pools = NULL;
	int i;
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

						//int i = 0;
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
						nFlyweight++;
						}
					}
				}
			}			
		}
	}
}

void FindFlyweight1(MethodSymbolTable *ms_table)
{
	if (PINOT_DEBUG)
		Coutput << "Identifying the Flyweight pattern" << endl;
	      	
	for (unsigned i=0; i<ms_table->size(); i++) 
	{
		MethodSymbol *msym = (*ms_table)[i];

		if (PINOT_DEBUG)
			Coutput << "Analyzing method: " << msym->Utf8Name() << endl;

		TypeSymbol *unit_type = msym->containing_type;
		if ((msym->declaration->kind==Ast::METHOD)
		&& msym->declaration->MethodDeclarationCast()->method_body_opt
		&& msym->Type()->file_symbol 
		&& !unit_type->IsFamily(msym->Type())
		)
		{
			FlyweightAnalysis flyweight(msym);
			msym->declaration->MethodDeclarationCast()->method_body_opt->Accept(flyweight);
			//flyweight.DumpSummary();
			if (flyweight.IsFlyweightFactory())
			{
				nFlyweight++;
				nFlyweightGoFVersion++;
				Coutput << "Flyweight Pattern." << endl;
				Coutput << unit_type->Utf8Name() << " is a flyweight factory." << endl;
				Coutput << flyweight.GetFlyweightPool()->Utf8Name() << " is the flyweight pool." << endl;
				Coutput << msym->Utf8Name() 
					<< " is the factory method, producing flyweight objects of type "
					<< msym->Type()->Utf8Name() << endl;
				Coutput << "File location: " << unit_type->file_symbol->FileName() << endl << endl;				
			}
		}
	}
}

void FindFlyweight2(ClassSymbolTable *cs_table, WriteAccessTable *w_table, ReadAccessTable *r_table)
{
	// This strategy looks for a variant flyweight implementation, where
	// flyweight factories and pools are not necessary:
	//
	//   1. classes that are defined immutable
	//       - class declared "final"
	//       - allows instantiation, thus public ctors (unlike java.lang.Math)
	//       - but internal fields should all be private and not written/modified by any non-private methods.
	//
	//   2. flyweight pools are represented as individual variable declarations
       //	  - such variables are typically declared "static final" and are initialized (pre-populated)

      	unsigned c;
	for (c= 0; c < cs_table -> size(); c++) 
	{
		TypeSymbol *unit_type = (*cs_table)[c];
		if (unit_type->ACC_FINAL())
		{
			AstClassBody *class_body = unit_type->declaration;
			if (!class_body -> default_constructor)
			{
				unsigned i, j;
				for (i=0; (i < class_body->NumConstructors()) && !class_body->Constructor(i)->constructor_symbol->ACC_PRIVATE(); i++)
					;
				for (j=0; (j < unit_type->NumVariableSymbols()) && unit_type->VariableSym(j)->ACC_PRIVATE(); j++)
					;
				if ((i ==  class_body->NumConstructors()) && (j ==  unit_type->NumVariableSymbols()))
				{
					bool flag = false;
					unsigned m, v;
					for (v = 0; !flag && (v < unit_type->NumVariableSymbols()); v++)
					{
						if (!unit_type->VariableSym(v)->ACC_FINAL())
						{
							for (m=0; !flag && (m < class_body->NumMethods()); m++)
							{
								if (class_body->Method(m)->method_symbol->ACC_PUBLIC())
									flag = w_table->IsWrittenBy(unit_type->VariableSym(v), class_body->Method(m)->method_symbol);
							}
						}
					}
					if (!flag)
					{
						nFlyweight++;
						Coutput << "Flyweight Pattern." << endl;
						Coutput << unit_type->Utf8Name() << " is immutable." << endl;
						Coutput << "File location: " <<  unit_type->file_symbol->FileName() << endl << endl;
						nImmutable++;
					}
				}
			}
		}
		else
		{
			unsigned i;
			for (i=0; i < unit_type->NumVariableSymbols(); i++)
			{
				if (unit_type->VariableSym(i)->Type()->file_symbol
				&& unit_type->VariableSym(i)->ACC_STATIC() 
				&& unit_type->VariableSym(i)->ACC_FINAL()
				//&& (unit_type != unit_type->VariableSym(i)->Type())
				)
				{
					if (unit_type->VariableSym(i)->ACC_PUBLIC() && unit_type->VariableSym(i)->declarator->variable_initializer_opt)
					{
						nFlyweight++;
						Coutput << "Flyweight Pattern." << endl;
						Coutput << unit_type->Utf8Name() << " is a flyweight factory." << endl;
						//Coutput << unit_type->VariableSym(i)->Utf8Name() << " is a flyweight object (declared public-static-final)." << endl;
						Coutput << unit_type->VariableSym(i)->Type()->Utf8Name() << " is a flyweight object (declared public-static-final)." << " object-name: "<< unit_type -> VariableSym(i) -> Utf8Name() << endl; //angor
						Coutput << "File location: " <<  unit_type->file_symbol->FileName() << endl << endl;
						goto done;
					}
					else 
					{
						VariableSymbol *vsym = unit_type->VariableSym(i);
						MethodSymbol *msym = NULL;
						multimap<VariableSymbol*,MethodSymbol*>::iterator p;
						for (p = r_table -> begin(); p != r_table -> end(); p++)
						{
							//Find the method that returns this static-final flyweight object.
							//NOTE: this  approach does not analyze method body, just the fact that a flyweight object can be returned.

							//VariableSymbol *t1 = p->first;
							//MethodSymbol *t2 = p->second;
							if (strcmp(vsym->Type()->fully_qualified_name->value, "java/lang/String")
							&& (p -> first == vsym))
								msym = p->second;						
							else if (Utility::Aliasing(p->first, vsym))
								msym = p->second;
						}
					
						if (msym)
						{
							nFlyweight++;
							Coutput << "Flyweight Pattern." << endl;
							Coutput << unit_type->Utf8Name() << " is a flyweight factory." << endl;
							//Coutput << vsym->Utf8Name() << " is a flyweight object." << endl; //pinot
							Coutput << vsym->Type()->Utf8Name() << " is a flyweight object." <<" object-name: "<<vsym->Utf8Name()<< endl; //angor
							Coutput << msym->Utf8Name() << " is the getFlyweight method." << endl;
							Coutput << "File location: " <<  unit_type->file_symbol->FileName() << endl << endl;
							goto done;
						}
					}
				}
			}
			done: ;
		}
	}
}

bool Connectivity(MethodSymbol* start, TypeSymbol *end, MethodSymbolTable *ms_table)
{
	if (!start->invokers || !end->subtypes || (end->subtypes->Size()==0))
		return false;
	ms_table->ClearMarks();

	Symbol *sym = end->subtypes->FirstElement();
	while(sym)
	{
		TypeSymbol *type =  sym->TypeCast();
		for (unsigned i = 0; i < type->NumMethodSymbols(); i++)
		{
			if (type->MethodSym(i)->declaration)
			{
				if ((type->MethodSym(i)->declaration->ConstructorDeclarationCast() 
					&& type->MethodSym(i)->declaration->ConstructorDeclarationCast()->constructor_body)
				|| (type->MethodSym(i)->declaration->MethodDeclarationCast() 
					&& type->MethodSym(i)->declaration->MethodDeclarationCast()->method_body_opt))
					type->MethodSym(i)->mark = 'B';
			}
		}
		sym = end->subtypes->NextElement();
	}
	if (start->mark == 'B')
	{
		Coutput << start->Utf8Name() << " is called by " << start->containing_type->Utf8Name() << "::" << start->Utf8Name() << " is the pivot point." <<endl;
		return true;
	}
	SymbolSet set(0);
	sym = start->invokers->FirstElement();
	while(sym)
	{
		MethodSymbol *msym = sym->MethodCast();
		if (msym->mark == 'B')
		{
			Coutput << start->Utf8Name() << " is called by " << msym->containing_type->Utf8Name() << "::" << msym->Utf8Name() << " is the pivot point." <<endl;
			return true;
		}
		else
		{
			msym->mark = 'R';
			if (msym->invokers)
				set.Union(*msym->invokers);
		}
		sym = start->invokers->NextElement();
	}
	while(set.Size())
	{
		sym = set.FirstElement();
		while(sym)
		{
			MethodSymbol *msym = sym->MethodCast();
			if (msym->mark == 'B')
			{
				Coutput << msym->containing_type->Utf8Name() << "::" << msym->Utf8Name() << " is the pivot point." <<endl;
				return true;
			}
			else if (msym->mark == 'R')
				set.RemoveElement(msym);
			else if (msym->mark == 'W')
			{
				msym->mark = 'R';
				set.RemoveElement(msym);
				if (msym->invokers)
					set.Union(*msym->invokers);
			}
			sym = set.NextElement();			
		}
	}
	return false;
}

bool DelegatesSuccessors(TypeSymbol *t1, TypeSymbol *t2)
{
	// pre-condition: t1 is concrete, while t2 is abstract

	if (t2->subtypes)
	{
		Symbol *sym = t2->subtypes->FirstElement();
		while (sym)
		{
			// checks for delegations to concrete classes.
			// the reason is to make sure that concrete strategies are not drectly exposed to the context class.
			if (!sym->TypeCast()->ACC_ABSTRACT() && sym->TypeCast()->call_dependents && sym->TypeCast()->call_dependents->IsElement(t1))
				return true;
			sym = t2->subtypes->NextElement();
		}
	}
	return false;
}

void FindStrategy(ClassSymbolTable *cs_table, DelegationTable *d_table, WriteAccessTable *w_table, ReadAccessTable *r_table, MethodSymbolTable *ms_table) 
{
	multimap<TypeSymbol*,TypeSymbol*> cache;
	
	int i;
	for (i = 0; i < d_table -> size(); i++)
	{
		DelegationEntry *entry = d_table -> Entry(i);

		if ((!entry -> from -> ACC_ABSTRACT())
		&& (!entry -> from -> Anonymous())
		&& (entry -> to -> ACC_ABSTRACT())
		&& (entry -> to -> file_symbol -> IsJava())
		&& (entry -> base_opt)
		&& !entry->from->IsFamily(entry->to)
		)
		{
			multimap<TypeSymbol*,TypeSymbol*>::iterator p = cache.begin();
			for (; (p != cache.end()) && ((p -> first != entry -> from) || (p -> second != entry -> to)) ; p++);
			if ((p == cache.end()) 
			&& (!DelegatesSuccessors(entry -> from, entry -> to))
			)
			{
				VariableSymbol *vsym = NULL;

				//
				// Change THIS
				// entry->base_opt should be unwrapped to vsym/this/super/class.
				//
				if (entry -> base_opt -> kind == Ast::NAME)
					vsym = entry -> base_opt -> symbol -> VariableCast();
				else if (entry -> base_opt -> kind == Ast::CALL)
				{
					AstMethodInvocation *call = (entry -> base_opt -> MethodInvocationCast() -> resolution_opt)
						? entry -> base_opt -> MethodInvocationCast() -> resolution_opt -> MethodInvocationCast()
						: entry -> base_opt -> MethodInvocationCast();
					MethodSymbol *msym = (MethodSymbol*) call -> symbol;
					multimap<VariableSymbol*,MethodSymbol*>::iterator p;

					//
					// Change THIS
					// a var can be returned by multiple methods.
					//
					for (p = r_table -> begin(); !vsym && (p != r_table -> end()); p++)
					{
						if (p -> second == msym)
							vsym = p -> first;
					}
				}

				if (vsym
				&& ((! vsym-> IsLocal())
				   ||(vsym = entry -> from -> Shadows(vsym))))
				{
					if ((entry -> from == vsym -> ContainingType())
					|| ((entry -> from -> IsInner()) && (entry -> from -> ContainingType() == vsym -> ContainingType())))
					{
						cache.insert(pair<TypeSymbol*,TypeSymbol*>(entry->from, entry -> to));

						// check if State pattern is implemented.
						MethodSymbol *dsym = NULL;
						// if previous didn't work, try the following
						if (!dsym)
						{
							multimap<VariableSymbol*, MethodSymbol*>::iterator p;
							for (p = w_table->begin(); !dsym && p!=w_table->end();p++)
							{
								//VariableSymbol *t1 = p->first;
								//MethodSymbol *t2 = p->second;
								if (p->first==vsym)
								{
									if (Connectivity(p->second, entry->to, ms_table)
									)
										dsym = p->second;
								}
							}
						}
						if (dsym)
						{
							nState++;
							Coutput << "State Pattern." << endl;
							Coutput << entry -> from -> Utf8Name()
								<< " is the Context class."
								<< endl
								<< entry -> to -> Utf8Name()						
								<< " is the State interface."
								<< endl;
							Coutput << "Concrete State classes: ";
								entry->to->subtypes->Print();
							Coutput << "Delegation through "
								<< vsym -> Utf8Name()
								<< " of type "
								<< vsym -> Type() -> Utf8Name()							
								<< endl
								<< dsym -> Utf8Name()
								<< " changes the state variable "
								<< vsym -> Utf8Name()
								<< endl;
							Coutput << dsym->Utf8Name()
								<< " is invoked by ";
							dsym->callers->Print();
							Coutput << "File Location: "
								<< entry -> from -> file_symbol -> FileName()
								<< ",\n               "
								<< entry -> to -> file_symbol -> FileName()
								<< endl
								<< endl;
						}
						else
						{
							nStrategy++;
							Coutput << "Strategy Pattern." << endl;
							Coutput << entry -> from -> Utf8Name()
								<< " is the Context class."
								<< endl
								<< entry -> to -> Utf8Name()						
								<< " is the Strategy interface."
								<< endl;
							Coutput << "Concrete Strategy classes: ";
								entry->to->subtypes->Print();
							Coutput << "Delegation through "
								<< vsym -> Utf8Name()
								<< " of type "
								<< vsym -> Type() -> Utf8Name()							
								<< endl
								<< "File Location: "
								<< entry -> from -> file_symbol -> FileName()
								<< ",\n               "
								<< entry -> to -> file_symbol -> FileName()
								<< endl
								<< endl;
						}						
					}
				}
			}
		}
	}	
}
void FindStrategy1(ClassSymbolTable *cs_table, DelegationTable *d_table, WriteAccessTable *w_table, ReadAccessTable *r_table, MethodSymbolTable *ms_table) 
{
	multimap<TypeSymbol*,TypeSymbol*> cache;

	unsigned c;
	for (c = 0; c < cs_table->size(); c++)
	{
		TypeSymbol *context = (*cs_table)[c];
		if (!context->ACC_ABSTRACT() && !context->Anonymous() && (!context->subtypes || !context->subtypes->Size()))
		{
		for (unsigned i = 0; i < context->NumVariableSymbols(); i++)
		{
			VariableSymbol *vsym = context->VariableSym(i);
			if (vsym->Type()->file_symbol 
			&& vsym->Type()->file_symbol->IsJava()
			&& vsym->Type()->ACC_ABSTRACT()
			&& !vsym->Type()->IsFamily(context)
			&& !vsym->Type()->IsArray()
			//&& !vsym->Type()->IsSelfContaining()
			)
			{
				MethodSymbol *dsym = NULL;
				multimap<VariableSymbol*, MethodSymbol*>::iterator p;
				bool flag = false;
				for (p = w_table->begin(); !dsym && p!=w_table->end();p++)
				{
					//VariableSymbol *t1 = p->first;
					//MethodSymbol *t2 = p->second;
					if (p->first==vsym)
					{
						flag = true;
						if (p->second->declaration 
						&& p->second->declaration->MethodDeclarationCast() 
						&& Connectivity(p->second, vsym->Type(), ms_table))
							dsym = p->second;
					}
				}			
				if (dsym)
				{
					nState++;
					Coutput << "State Pattern." << endl;
					Coutput << context->Utf8Name() << " is the Context class." << endl
						<< vsym->Type()->Utf8Name() << " is the State interface." << endl;
					Coutput << "Concrete State classes: ";
					vsym->Type()->subtypes->Print();
					Coutput << "Delegation through " << vsym->Utf8Name() << " of type " << vsym->Type()->Utf8Name() << endl
						<< dsym->Utf8Name() 	<< " changes the state variable " 	<< vsym->Utf8Name() 	<< endl;
					Coutput << dsym->Utf8Name() << " is invoked by ";
					dsym->invokers->Print();
					Coutput << "File Location: "
						<< context->file_symbol->FileName() << ",\n               "
						<< vsym->Type()->file_symbol->FileName()
						<< endl
						<< endl;
				}
				else if (flag)
				{
					nStrategy++;
					Coutput << "Strategy Pattern." << endl;
					Coutput << context->Utf8Name() << " is the Context class." << endl
						<< vsym->Type()->Utf8Name() << " is the Strategy interface." << endl;
					Coutput << "Concrete Strategy classes: ";
					vsym->Type()->subtypes->Print();
					Coutput << "Delegation through " << vsym->Utf8Name() << " of type " << vsym->Type()->Utf8Name() << endl;
					Coutput << "File Location: "
						<< context->file_symbol->FileName() << ",\n               "
						<< vsym->Type()->file_symbol->FileName()
						<< endl
						<< endl;
				}
				
			}
		}
		}
	}	
}

void FindComposite(ClassSymbolTable *cs_table, DelegationTable *d_table)
{
	unsigned c;
	for (c = 0; c < cs_table -> size(); c++)
	{
		if (!(*cs_table)[c] -> ACC_ABSTRACT() && (*cs_table)[c]->supertypes_closure && (*cs_table)[c]->supertypes_closure->Size())
		{
			TypeSymbol *unit_type = (*cs_table)[c];
			AstClassBody* class_body = unit_type -> declaration;
			for (unsigned i = 0; i < class_body -> NumInstanceVariables(); i++)
			{
				AstFieldDeclaration* field_decl  = class_body -> InstanceVariable(i);
				for (unsigned vi = 0; (vi < field_decl -> NumVariableDeclarators()); vi++)
				{
					AstVariableDeclarator* vd = field_decl -> VariableDeclarator(vi);
					ContainerType *container_type = Utility::IdentifyContainerType(vd->symbol);
					//TypeSymbol *contained_type = unit_type->IsOnetoMany(vd->symbol, d_table);
					if (!container_type)
						break;
					
					if (container_type->kind == ContainerType::ARRAY)
					{
						if ((unit_type != vd->symbol->Type()->base_type)
						&& unit_type -> IsSubtype(vd->symbol->Type()->base_type))
						{
							nComposite++;
							Coutput << "Composite Pattern." << endl;
							Coutput << unit_type->Utf8Name() << " is the composite class." << endl;
							Coutput << vd->symbol->Utf8Name() << " is the composite instance." << endl;
							Coutput << vd->symbol->Type()->base_type->Utf8Name() << " is the component class." << endl;
							Coutput << "File Location: " << unit_type->file_symbol->FileName() << endl;
							if (vd->symbol->Type()->base_type->file_symbol->IsClassOnly() && getenv("PINOT_HOME"))
								Coutput << "$PINOT_HOME/lib/rt.jar" << vd->symbol->Type()->base_type->fully_qualified_name->value << ".class" << endl << endl;
							else
								Coutput << "File Location: " << vd->symbol->Type()->base_type->file_symbol->FileName() << endl << endl;							
						}
					}
					else
					{
						SymbolSet set;
						set.Union(*unit_type->supertypes_closure);
						int ct = 0;
						for (int i = 0; i < d_table -> size(); i++)
						{
							DelegationEntry *entry = d_table -> Entry(i);
							if (entry->vsym
							&& (entry->vsym == vd->symbol)
							&& container_type->IsPutMethod(entry->call->symbol->MethodCast()))
							{
								TypeSymbol *type = container_type->GetPutType(entry->call);
								if (type && type->supertypes_closure)
								{
									type->supertypes_closure->AddElement(type);
									set.Intersection(*type->supertypes_closure);
									type->supertypes_closure->RemoveElement(type);
									ct++;
								}
							}
						}
						if (ct == 0)
							break;
						//remove java/lang/Object from set
						Utility::RemoveBuiltinInterfaces(set);
						if (set.Size() == 0)
							break;

						nComposite++;
						Coutput << "Composite pattern." << endl;
						Coutput << unit_type->Utf8Name() << " is the composite class." << endl;
						Coutput << vd->symbol->Utf8Name() << " is the composite instance." << endl;
						Coutput << set.FirstElement()->TypeCast()->Utf8Name() << " is the component class." << endl;
						Coutput << "File Location: " << unit_type->file_symbol->FileName() << endl;
						Coutput << "File Location: " << set.FirstElement()->TypeCast()->file_symbol->FileName() << endl << endl;

						
						/*
						if ((contained_type != unit_type) && contained_type && unit_type -> IsSubtype(contained_type))
						{
							nComposite++;
							Coutput << "Composite pattern." << endl;
							Coutput << unit_type -> Utf8Name() << " is the composite class." << endl;
							Coutput << vd -> symbol -> Utf8Name() << " is the composite instance." << endl;
							Coutput << contained_type -> Utf8Name() << " is the component class." << endl;
							Coutput << "File Location: " << unit_type -> file_symbol -> FileName() << endl;
							Coutput << "File Location: " << contained_type -> file_symbol -> FileName() << endl << endl;
						}
						*/
					}
					delete container_type;
				}
    			}
		}
	}
}

void FindMediator(ClassSymbolTable *cs_table, DelegationTable *d_table)
{
	unsigned c;
	for (c = 0; c < cs_table -> size(); c++)
	{
		TypeSymbol *unit_type = (*cs_table)[c];

		if (!unit_type -> ACC_INTERFACE()
		&& !unit_type -> IsInner()
		&& !unit_type -> ACC_PRIVATE())
		{
			SymbolSet colleagues;
			SymbolSet observers;
			SymbolSet *dset = unit_type -> references;
			Symbol *sym = (dset) ? dset->FirstElement() : NULL; 
			while (sym)
			{
				TypeSymbol *type = sym -> TypeCast();
				if (!type->Primitive()
				&& type -> ACC_ABSTRACT()
				&& !type -> ACC_INTERFACE()				
				&& type -> references 
				&& (unit_type != type) 
				&& type -> file_symbol-> IsJava() 
				&& type -> references -> IsElement(unit_type)
				&& unit_type -> IsOnetoMany(type))
				{
					if (d_table -> IsBidirectional(unit_type, type) == 3)
						colleagues.AddElement(type);
					else if (d_table -> IsBidirectional(unit_type, type) > 0)
						observers.AddElement(type);
				}
				sym = dset->NextElement();
			}
			if (!colleagues.IsEmpty())
			{
				nMediator++;
				Coutput << "Mediator pattern." << endl;
				Coutput << unit_type -> Utf8Name() << " is a Mediator class." << endl;
				Symbol *sym = NULL;
				Coutput << "Colleagues: ";
				sym = colleagues.FirstElement();
				while (sym)
				{
					Coutput << sym -> TypeCast() -> Utf8Name() << " ";
					sym = colleagues.NextElement();
				}
				Coutput << endl;
				Coutput << "File Location: " << unit_type -> file_symbol -> FileName() << endl << endl;
				colleagues.SetEmpty();
			}
			else if (!observers.IsEmpty())
			{
				nObserver++;
				Coutput << "Observer pattern." << endl;
				Coutput << unit_type -> Utf8Name() << " is a Subject class." << endl;
				Symbol *sym = NULL;
				Coutput << "Observers: ";
				sym = observers.FirstElement();
				while (sym)
				{
					Coutput << sym -> TypeCast() -> Utf8Name() << " ";
					sym = observers.NextElement();
				}
				Coutput << endl;
				Coutput << "File Location: " << unit_type -> file_symbol -> FileName() << endl << endl;
				observers.SetEmpty();
			}
		}
	}
}
bool IsJavaContainer(VariableSymbol *vsym)
{
	if (strcmp(vsym->Type()->fully_qualified_name->value, "java/util/Iterator") == 0)
		return true;
	if (vsym->Type()->supertypes_closure)
	{
		Symbol *sym = vsym->Type()->supertypes_closure->FirstElement();
		while (sym)
		{
			TypeSymbol *type = sym->TypeCast();
			if (strcmp(type->fully_qualified_name->value, "java/util/Iterator") == 0)
				return true;
			sym = vsym->Type()->supertypes_closure->NextElement();
		}
	}
	return false;
}
VariableSymbol *IteratorVar(AstExpression *expression)
{
	/* 
		1 - java.util.Iterator
		2 - array index
		3 - recursion
	  */

	AstExpression *resolved = Utility::RemoveCasting(expression);
	if (resolved->kind == Ast::CALL)
	{
		AstMethodInvocation *call = resolved->MethodInvocationCast();
		if (call->base_opt
		&& call->base_opt->symbol->VariableCast()
		&& IsJavaContainer(call->base_opt->symbol->VariableCast())
		&& (strcmp(call->symbol->MethodCast()->Utf8Name(), "next") == 0))
			return call -> base_opt -> NameCast() -> symbol -> VariableCast();
	}
	else if (resolved -> kind == Ast::ARRAY_ACCESS)
	{
		if (resolved -> ArrayAccessCast()->base-> kind == Ast::NAME)
			return resolved -> ArrayAccessCast()->base->symbol->VariableCast();
	}
	else if ((resolved->kind == Ast::NAME) && (resolved->NameCast()->symbol->Kind()==Symbol::VARIABLE))
	{
		return resolved->NameCast()->symbol ->VariableCast();
	}
	return 0;	
}
VariableSymbol *ListVar(VariableSymbol *vsym)
{
	if (!vsym->declarator->variable_initializer_opt
	|| !vsym->declarator->variable_initializer_opt->ExpressionCast())
		return NULL;

	AstExpression *var_initializer = Utility::RemoveCasting(vsym->declarator->variable_initializer_opt->ExpressionCast());
	
	// vsym -> IsLocal()
	// vsym is an iterator that implements java.util.Iterator
	if (strcmp( vsym->Type()->fully_qualified_name->value, "java/util/Iterator") == 0)
	{
		if (vsym -> declarator -> variable_initializer_opt -> kind == Ast::CALL)
		{
			AstMethodInvocation *init_call = vsym -> declarator -> variable_initializer_opt -> MethodInvocationCast();
			// iterator initialized at declaration
			if (strcmp(init_call -> symbol -> MethodCast() -> Utf8Name(), "iterator") == 0)
				return (init_call->base_opt->symbol->Kind() == Symbol::VARIABLE)
					? init_call->base_opt->symbol->VariableCast()
					: 0;		
			// iterator initialized later in an assignment statement
			// 	vsym->owner is a Symbol, but if vsym is local then the owner is a MethodSymbol
			//	verify assignment statement
			// if vsym is not local (which should be rare), and is initialized somewhere else (e.g. in other methods, also rare)
		}
	}
	else if (strcmp( vsym->Type()->fully_qualified_name->value, "java/util/ListIterator") == 0)
	{
		if (vsym -> declarator -> variable_initializer_opt -> kind == Ast::CALL)
		{
			AstMethodInvocation *init_call = vsym -> declarator -> variable_initializer_opt -> MethodInvocationCast();
			// iterator initialized at declaration
			if (strcmp(init_call -> symbol -> MethodCast() -> Utf8Name(), "listIterator") == 0)
				return init_call -> base_opt -> NameCast() -> symbol -> VariableCast();		
			// iterator initialized later in an assignment statement
			// 	vsym->owner is a Symbol, but if vsym is local then the owner is a MethodSymbol
			//	verify assignment statement
			// if vsym is not local (which should be rare), and is initialized somewhere else (e.g. in other methods, also rare)
		}
	}
	else if ((vsym->declarator->variable_initializer_opt->kind == Ast::NAME)
		&& (vsym->declarator->variable_initializer_opt->NameCast()->symbol->Kind()==Symbol::VARIABLE))
	{
		return vsym->declarator->variable_initializer_opt->NameCast()->symbol->VariableCast();
	}
	else if (var_initializer->kind == Ast::CALL)
	{
		AstMethodInvocation *init_call = var_initializer->MethodInvocationCast();
		if (init_call->base_opt && init_call->base_opt->symbol->VariableCast())
		{
			if (((strcmp(init_call->base_opt->symbol->VariableCast()->Type()->fully_qualified_name->value, "java/util/Vector") == 0)
				&& (strcmp(init_call->symbol->MethodCast()->Utf8Name(), "elementAt") == 0))
			|| ((strcmp(init_call->base_opt->symbol->VariableCast()->Type()->fully_qualified_name->value, "java/util/ArrayList") == 0)
				&& (strcmp(init_call->symbol->MethodCast()->Utf8Name(), "get") == 0))
			)
				return init_call->base_opt->symbol->VariableCast();
			else if ((strcmp(init_call->base_opt->symbol->VariableCast()->Type()->fully_qualified_name->value, "java/util/Iterator") == 0)
			&& (strcmp(init_call->symbol->MethodCast()->Utf8Name(), "next") == 0))
			{
				VariableSymbol *iterator = init_call->base_opt->symbol->VariableCast();
				AstMethodInvocation *i_init_call = iterator->declarator->variable_initializer_opt->MethodInvocationCast();
				// iterator initialized at declaration
				if (strcmp(i_init_call->symbol->MethodCast()->Utf8Name(), "iterator") == 0)
					return i_init_call->base_opt->symbol->VariableCast();		
			}
		}
			
	}
	return NULL;					
}
void FindObserver(ClassSymbolTable *cs_table, DelegationTable *d_table)
{
	vector<TypeSymbol*> cache;
	unsigned c;
	for (c = 0; c < cs_table ->size(); c++)
	{
		TypeSymbol *unit_type = (*cs_table)[c];
		if (!unit_type -> ACC_INTERFACE())
		{
			for (unsigned i = 0; i < unit_type -> declaration-> NumInstanceVariables(); i++)
 			{
   				AstFieldDeclaration* field_decl = unit_type -> declaration -> InstanceVariable(i);
 				for (unsigned vi = 0; vi < field_decl -> NumVariableDeclarators(); vi++)
 				{
					AstVariableDeclarator* vd = field_decl -> VariableDeclarator(vi);

					TypeSymbol *generic_type = unit_type -> IsOnetoMany(vd -> symbol, d_table) ;
					if (generic_type && generic_type -> file_symbol)
					{
						for (int j = 0; j < d_table -> size(); j++)
						{
							DelegationEntry* entry = d_table -> Entry(j);
							if ((unit_type == entry -> enclosing -> containing_type) && (generic_type == entry -> to))
							{
							/*
								if ((unit_type == generic_type) && (entry -> vsym == vd -> symbol) && (entry -> enclosing == entry -> method) && (entry -> enclosing -> callers -> Size() > 1))
								{
									nObserver++;
									Coutput << "Observer Pattern." << endl
										<< unit_type -> Utf8Name() << " is an observer iterator." << endl
										<< generic_type -> Utf8Name() << " is the generic type for the listeners." << endl
										<< entry -> enclosing -> Utf8Name() << " is the notify method." << endl
										<< entry -> method -> Utf8Name() << " is the update method." << endl;
									Coutput << "Subject class(es):";
									entry -> enclosing -> callers -> Print();
									Coutput << "File Location: " << unit_type->file_symbol->FileName() << endl << endl;									
								}
							*/
								if (!entry->enclosing->callers 
									|| (!entry->enclosing->callers -> IsElement(generic_type) 
										//&& !entry->enclosing->callers -> IsElement(unit_type)
									     )
								     )
								{
									VariableSymbol *iterator = 0;
									ControlAnalysis controlflow(entry -> call);
									if (entry -> enclosing -> declaration -> MethodDeclarationCast()
									&& entry -> enclosing -> declaration -> MethodDeclarationCast() -> method_body_opt)
										entry -> enclosing -> declaration -> MethodDeclarationCast() -> method_body_opt -> Accept(controlflow);

									if (controlflow.result 
									&& controlflow.IsRepeated()
									&& entry -> base_opt
									&& (iterator = IteratorVar(entry->base_opt))
									&& ((iterator == vd->symbol) 
										|| (vd->symbol == unit_type -> Shadows(iterator))
										|| (vd -> symbol == ListVar(iterator))))
									{
										// push-model observer pattern
										nObserver++;
										Coutput << "Observer Pattern." << endl
											<< unit_type -> Utf8Name() << " is an observer iterator." << endl
											<< generic_type -> Utf8Name() << " is the generic type for the listeners." << endl
											<< entry -> enclosing -> Utf8Name() << " is the notify method." << endl
											<< entry -> method -> Utf8Name() << " is the update method." << endl;
										Coutput << "Subject class(es):";
										if (entry -> enclosing -> callers)
											entry -> enclosing -> callers -> Print();
										else
											Coutput << endl;
										Coutput << "File Location: " << unit_type->file_symbol->FileName() << endl << endl;									
									}
								}							
							}
							else if ((generic_type == entry -> enclosing -> containing_type) && (unit_type == entry -> to))
							{
							    unsigned j = 0;
							    for (; (j < cache.size()) && (cache[j] != unit_type) ; j++);
							    if (j == cache.size())
							    {
							     		cache.push_back(unit_type);
											nMediator++;
											Coutput << "Mediator Pattern." << endl;
											Coutput << unit_type -> Utf8Name() << " is the mediator class." << endl
															<< vd -> symbol -> Utf8Name() << " controls a list of colleagues of type "
															<< generic_type -> Utf8Name() << "." << endl;
											Coutput << entry -> method -> Utf8Name() 
												<< " invokes the mediator. " << endl;
											Coutput << "Subtype(s) of colleague(s): ";
											generic_type -> subtypes -> Print();		
											Coutput << "File Location: " << unit_type->file_symbol->FileName() << endl << endl;									
							    }
							}
						}
					}
				}
			}
		}
	}
}
void FindMediator2(ClassSymbolTable *cs_table)
{
	map<TypeSymbol*, SymbolSet*> cache;
	vector<TypeSymbol*> ordered_cache;
	unsigned c;
	for (c = 0; c < cs_table ->size(); c++)
	{
		TypeSymbol *unit_type = (*cs_table)[c];
		// check if this facade class can be serving as a mediator for some of the hidden classes.
		unsigned i = 0;
		while (i < unit_type->NumMethodSymbols())
		{
			MethodSymbol *msym = unit_type->MethodSym(i);
			if (msym->callers && msym->invokees)
			{
				Symbol *sym1 = msym->callers->FirstElement();
				while (sym1)
				{
					TypeSymbol *caller = sym1->TypeCast();
					Symbol *sym2 = msym->invokees->FirstElement();	
					while(sym2)
					{
						TypeSymbol *callee = sym2->MethodCast()->containing_type;
						if (caller->file_symbol 
						&& caller->file_symbol->IsJava()
						&& caller->call_dependents
						&& callee->file_symbol 
						&& callee->file_symbol->IsJava()
						&& callee->call_dependents
						&& !caller->call_dependents->IsElement(callee)
						&& !callee->call_dependents->IsElement(caller)
						&& (caller != unit_type)
						&& (callee != unit_type)
						)
						{
							if (!unit_type->mediator_colleagues)
								unit_type->mediator_colleagues = new SymbolSet(0);
							unit_type->mediator_colleagues->AddElement(caller);
							unit_type->mediator_colleagues->AddElement(callee);
							
						       /* trying to get rid of STL map
							map<TypeSymbol*, SymbolSet*>::iterator m = cache.find(unit_type);
							if (m == cache.end())
							{
								SymbolSet *set = new SymbolSet(0);
								set->AddElement(caller);
								set->AddElement(callee);
								cache.insert(pair<TypeSymbol*, SymbolSet*>(unit_type, set));								
							}
							else
							{
								m->second->AddElement(caller);
								m->second->AddElement(callee);								
							}
							*/
							
						}
						sym2 = msym->invokees->NextElement();
					}
					sym1 = msym->callers->NextElement();						
				}
			}				
			i++;
		}
	}
	// Print results
	/*
	nMediator += cache.size();
	map<TypeSymbol*, SymbolSet*>::iterator pattern;
	for (pattern = cache.begin(); pattern != cache.end(); pattern++)
	{
		Coutput << "Mediator Pattern." << endl;
		Coutput << "Mediator: " << pattern->first->Utf8Name() << endl;
		Coutput << "Colleagues: ";
		pattern->second->Print();
		Coutput << "FileLocation: " << pattern->first->file_symbol->FileName() << endl << endl;	

		mediators.AddElement(pattern->first);
	}
	*/

	for (c = 0; c < cs_table ->size(); c++)
	{
		if ((*cs_table)[c]->mediator_colleagues)
		{
			nMediator++;
			Coutput << "Mediator Pattern." << endl;
			Coutput << "Mediator: " << (*cs_table)[c]->Utf8Name() << endl;
			Coutput << "Colleagues: ";
			(*cs_table)[c]->mediator_colleagues->Print();
			Coutput << "FileLocation: " << (*cs_table)[c]->file_symbol->FileName() << endl << endl;	

			mediators.AddElement((*cs_table)[c]);
		}
	}	
}
void FindTemplateMethod(DelegationTable *d_table)
{
  vector<TypeSymbol*> cache;
  for (int i = 0; i < d_table -> size(); i++)
    {
      DelegationEntry* entry = d_table -> Entry(i);	
/*
  if (strcmp(entry -> method -> Utf8Name(), "handleConnect") == 0)
  entry->method->declaration->MethodDeclarationCast()->Print();
  
  if (strcmp(entry -> method -> Utf8Name(), "target") == 0)
  entry->method->declaration->MethodDeclarationCast()->Print();
*/
      unsigned j = 0;
      for (; (j < cache.size()) && (cache[j] != entry -> from) ; j++);
      if (j == cache.size())
	{
	  if ((entry -> enclosing -> containing_type == entry -> method -> containing_type)
		//&& entry -> enclosing -> ACC_PUBLIC()
	      	&& entry -> enclosing -> ACC_FINAL()
	      	&& (entry -> enclosing -> declaration -> kind == Ast::METHOD)
	      	&& (entry -> method -> declaration -> kind == Ast::METHOD)
	      	&& (entry -> from == entry -> to))
	    {
	      AstMethodDeclaration* method_declaration = entry -> method -> declaration -> MethodDeclarationCast();
	      if (entry -> method -> ACC_ABSTRACT()
		  || (method_declaration -> method_body_opt == 0)
		  || ((method_declaration -> method_body_opt -> Statement(0) -> kind == Ast::RETURN) 
		      && (method_declaration -> method_body_opt -> Statement(0) -> ReturnStatementCast() -> expression_opt == 0 ))
		  )
		{
		  nTemplate++;
		  cache.push_back(entry -> from);
		  Coutput << "Template Method Pattern." << endl;
		  Coutput << entry -> from -> Utf8Name() << " is the template class" << endl;
		  Coutput << entry -> enclosing -> Utf8Name() << " is the template method" << endl;
		  Coutput << entry -> method -> Utf8Name() << " is a primitive method" << endl;			
		  Coutput << "File Location: " << entry -> from -> file_symbol -> FileName() << endl << endl;
		}
	    }
	}
    }
}

void FindFactory(ClassSymbolTable *cs_table, MethodSymbolTable *ms_table, StoragePool *ast_pool)
{
  SymbolSet abstract_factories;
  map<TypeSymbol*, TypeSymbol*> inheritance;
  map<TypeSymbol*, SymbolSet*> concrete_factories;
  
  for (unsigned i=0; i<ms_table->size(); i++)
    {
    	MethodSymbol *method = (*ms_table)[i];

	if (!method -> containing_type -> ACC_ABSTRACT()
	&& method->declaration
  	//&& method -> declaration -> kind == Ast::METHOD
       && !method -> ACC_PRIVATE()
       && !method -> Type() -> IsArray()
       && method -> Type() -> file_symbol
       && method->declaration->MethodDeclarationCast()
       && method->declaration->MethodDeclarationCast()->method_body_opt)
	{
		FactoryAnalysis factory(method, ast_pool);
		MethodSymbol *abstract_factory_method= NULL;
		if ((abstract_factory_method = method -> GetVirtual())
	      	&& (factory.IsFactoryMethod()))
		{
			abstract_factories.AddElement(abstract_factory_method->containing_type);
			inheritance.insert(pair<TypeSymbol*,TypeSymbol*>(method->containing_type, abstract_factory_method->containing_type));
			map<TypeSymbol*, SymbolSet*>::iterator ci = concrete_factories.find(method->containing_type);
			if (ci == concrete_factories.end())
			{
				SymbolSet *set = new SymbolSet();
				set->Union(factory.types);
				concrete_factories.insert(pair<TypeSymbol*, SymbolSet*>(method->containing_type, set));
			}
			else
			{
				ci->second->Union(factory.types);
			}
			
			Coutput << "Factory Method Pattern." << endl
				<< abstract_factory_method -> containing_type -> Utf8Name() << " is a Factory Method class." << endl;
		  
		  	Coutput << method -> containing_type -> Utf8Name()
				<< " is a concrete Factory Method class."
			  	<< endl
			  	<< method -> Utf8Name()
		         	<< " is a factory method returns ";
			factory.types.Print();
		       Coutput << " which extends "
		       	<< method -> Type() -> Utf8Name()
		         	<< endl			 
			  	<< "File Location: " << method->containing_type->file_symbol->FileName()
			  	<< endl << endl;
		}
		factory.CleanUp();
	}
    }
    nFactoryMethod += abstract_factories.Size();
    //check for family of products, Abstract Factory
    Symbol *sym = abstract_factories.FirstElement();
    while(sym)
    {
       vector<SymbolSet*> sets;
    	TypeSymbol *type = sym->TypeCast();
	map<TypeSymbol*, TypeSymbol*>::iterator ii;
	for (ii = inheritance.begin(); ii != inheritance.end(); ii++)
	{
		if (ii->second == type)
			sets.push_back((concrete_factories.find(ii->first))->second);
	}
	//check mutual isolation between sets
	bool flag = false;
	for (unsigned i=0; i<sets.size() && !flag; i++)
		for (unsigned j=0; j<sets.size() && !flag; j++)
			if (i != j)
				flag = sets[i]->Intersects(*sets[j]);
	if (!flag)
		nAbstractFactory++;
		
		Coutput << "Abstract Factory Pattern." << endl
				<< sym->TypeCast()->file_symbol->Utf8Name() << " is the creator class." << endl <<endl;
	sym = abstract_factories.NextElement();
    }
    
}

void FindVisitor(ClassSymbolTable *cs_table, MethodSymbolTable *ms_table)
{
	multimap<TypeSymbol*, TypeSymbol*> cache;

	for (unsigned i=0; i<ms_table->size(); i++)
	{
		MethodSymbol *method = (*ms_table)[i];
		// Recognizing the Accept(Visitor v) declaration.
		if ((method -> declaration -> kind == Ast::METHOD)
		&& method -> ACC_PUBLIC()
		)
		{
			bool flag1 = false;			
			unsigned i = 0;
			while (!flag1 && (i < method -> NumFormalParameters()))
			{					
				if (method -> FormalParameter(i) -> Type() -> ACC_ABSTRACT()
				&& method -> FormalParameter(i) -> Type() -> file_symbol
				&& method -> FormalParameter(i) -> Type() -> file_symbol -> IsJava()				
				&& !method -> containing_type -> IsFamily(method -> FormalParameter(i) -> Type())
				)
				{
					multimap<TypeSymbol*, TypeSymbol*>::iterator p = cache.begin();
					while ((p != cache.end())
						&& (!method -> containing_type -> IsSubtype(p -> first) && !method -> FormalParameter(i) -> Type() -> IsSubtype(p -> second)))
						p++;
					
					if (p == cache.end())
						{
					VariableSymbol *vsym = method -> FormalParameter(i);
					AstMethodDeclaration *method_declaration  = method -> declaration -> MethodDeclarationCast();
					if (method_declaration -> method_body_opt)
					{
						AstMethodBody *block = method_declaration -> method_body_opt;

						bool flag2 = false;
						unsigned j = 0;
						while (!flag2 && (j < block -> NumStatements()))
						{
							if ((block -> Statement(j) -> kind == Ast::EXPRESSION_STATEMENT)
							&& (block -> Statement(j) -> ExpressionStatementCast() -> expression -> kind == Ast::CALL))
							{
								// analyze the visitor.Accept(this) invocation
								AstMethodInvocation *call = (j < block -> NumStatements())
									? block -> Statement(j) -> ExpressionStatementCast() -> expression -> MethodInvocationCast()
									: NULL;
								if (call
								&& call -> base_opt
								&& (call -> base_opt -> kind == Ast::NAME)
								&& (call -> base_opt-> NameCast() -> symbol -> VariableCast() == vsym))
								{
									bool flag3 = false;
									unsigned k = 0;
									while (!flag3 && (k < call -> arguments -> NumArguments()))
									{
										if ((call -> arguments -> Argument(k) -> kind == Ast::THIS_EXPRESSION)
										|| ((call -> arguments -> Argument(k) -> kind == Ast::NAME)
											&& (call -> arguments -> Argument(k) -> NameCast() -> symbol -> VariableCast())
											&& (!call -> arguments -> Argument(k) -> NameCast() -> symbol -> VariableCast() -> IsLocal())))
										{
											nVisitor++;
											flag1 = flag2 = flag3 = true;
											Coutput << "Visitor Pattern." 
												<< endl
												<< method -> FormalParameter(i) -> Type() -> Utf8Name()
												<< " is an abstract Visitor class."
												<< endl
												<< method -> containing_type -> Utf8Name()
												<< " is a Visitee class."
												<< endl;
											TypeSymbol *super_visitee = method -> IsVirtual();
											if (super_visitee)
											{
												cache.insert(pair<TypeSymbol*, TypeSymbol*>(super_visitee, method -> FormalParameter(i) -> Type()));
												Coutput << super_visitee -> Utf8Name()
													<< " is an abstract Visitee class."
													<< endl;
												super_visitee -> subtypes -> Print();
											}
											else
											{
												cache.insert(pair<TypeSymbol*, TypeSymbol*>(method -> containing_type, method -> FormalParameter(i) -> Type()));
											}
											Coutput << method->Utf8Name() << " is the accept method." << endl;
											Coutput << call -> symbol -> MethodCast() -> Utf8Name() << " is the visit method." << endl;
											if (call -> arguments -> Argument(k) -> kind == Ast::THIS_EXPRESSION)
												Coutput << "THIS pointer is exposed to visitor ";
											else
												Coutput << call -> arguments -> Argument(k) -> NameCast() -> symbol -> VariableCast() -> Utf8Name() 
													<< " is exposed to visitor ";
											Coutput << method -> FormalParameter(i) -> Type() -> Utf8Name() << endl;
											Coutput << "File Location: "
												<< method -> containing_type -> file_symbol -> FileName() 
												<< endl << endl;
										}
										k++;
									}
								}
							}
							j++;
						}
					}
						}
				}
				i++;
			}
		}
	}
}

void FindProxy(ClassSymbolTable *cs_table, DelegationTable *d_table)
{
	unsigned c;
	for (c = 0; c < cs_table ->size(); c++)
	{
		TypeSymbol *unit_type = (*cs_table)[c];
		SymbolSet *parents = unit_type -> supertypes_closure;
		SymbolSet *instances = unit_type -> instances;

		if (!unit_type->Anonymous() && parents && parents -> Size() && instances)
		{
			Symbol *sym1 = instances -> FirstElement();
			bool flag = false;
			while (!flag && sym1)
			{
				VariableSymbol *vsym = sym1 -> VariableCast();
				TypeSymbol *real =  vsym -> Type();
				if (!real -> Primitive() && !real -> IsArray() && vsym->concrete_types)
				{
					Symbol *sym2 = parents -> FirstElement();
					while(!flag && sym2)
					{
						TypeSymbol *type = sym2 ->TypeCast();
						if ((real != unit_type)
						&& !type -> Primitive()
						&& (strcmp(type->fully_qualified_name->value, "java/lang/Object") != 0)
						&& type -> file_symbol
						//&& type -> file_symbol -> IsJava()
						&& real -> IsFamily(type)) // use IsFamily
						{
							SymbolSet real_set(0);
							if ((real == type) && type -> subtypes && type -> subtypes -> Size())
							{
								Symbol *sym3 = type -> subtypes -> FirstElement();
								while (sym3)
								{
									real = sym3 -> TypeCast();
									if ((unit_type != real)
									&& !real ->Anonymous()
									&& real->call_dependents
									&& real->call_dependents->IsElement(unit_type))
									{
										real->call_dependents->RemoveElement(unit_type);
										if (!unit_type->call_dependents || !unit_type->call_dependents->Intersects(*real->call_dependents))
											real_set.AddElement(real);
										real->call_dependents->AddElement(unit_type);
									}
									sym3 = type -> subtypes -> NextElement();
								}
							}
							else if (real->call_dependents && real -> call_dependents -> IsElement(unit_type))
							{
								real->call_dependents->RemoveElement(unit_type);
								if (!unit_type->call_dependents || !unit_type->call_dependents->Intersects(*real->call_dependents))
									real_set.AddElement(real);
								real->call_dependents->AddElement(unit_type);								
							}
							if (real_set.Size())
							{								
								flag = true;
								nProxy++;
								Coutput << "Proxy Pattern." << endl
									<< unit_type -> Utf8Name() << " is a proxy."<< endl
									<< type -> Utf8Name() << " is a proxy interface." << endl
									<< "The real object(s):";
								Symbol *sym = real_set.FirstElement();
								while (sym)
								{
									TypeSymbol *item = sym -> TypeCast();
									if (strcmp(item -> Utf8Name(), type -> Utf8Name()) == 0)
										Coutput << " " << item -> fully_qualified_name -> value;
									else
										Coutput << " " << item -> Utf8Name();
									sym = real_set.NextElement();
								}
								Coutput << endl;
								Coutput << "File Location: " << unit_type -> file_symbol -> FileName() << endl << endl;								
							}
							else
								sym2 = parents -> NextElement();
							
						}						
						else
							sym2 = parents -> NextElement();
					}
					if (!flag)
						sym1 = instances -> NextElement();
				}
				else
					sym1 = instances -> NextElement();
			}
		}
	}	
}

void FindAdapter(ClassSymbolTable *cs_table)
{
	unsigned c;
	for (c = 0; c < cs_table->size(); c++)
	{
		TypeSymbol *unit_type = (*cs_table)[c];
		if (!unit_type -> ACC_INTERFACE()
		&& !unit_type -> ACC_ABSTRACT()
		&& unit_type -> supertypes_closure && (unit_type -> supertypes_closure -> Size() > 1)
		&& unit_type -> instances)
		{
			Symbol *sym = unit_type -> instances -> FirstElement();
			while (sym)
			{
				TypeSymbol *ref_type = sym->VariableCast() -> Type();
				if (!ref_type -> IsArray()
				&& !ref_type -> Primitive()					
				&& (unit_type != ref_type)			
				//&& !ref_type -> ACC_INTERFACE()
				//&& !ref_type -> ACC_ABSTRACT()
				&& !unit_type -> IsFamily(ref_type)
				&& ref_type -> file_symbol
				&& ref_type -> file_symbol -> IsJava()
				&& ref_type -> call_dependents
				&& ref_type -> call_dependents -> IsElement(unit_type))
				{
					SymbolSet unit_dependents(0);
					if (unit_type->call_dependents)
						unit_dependents.Union(*unit_type->call_dependents);
					unsigned q;
					for (q = 0; q < cs_table->size(); q++)
					{
						TypeSymbol *type = (*cs_table)[q];
						if (unit_type->supertypes_closure->IsElement(type) && type->call_dependents)
							unit_dependents.Union(*type->call_dependents);
					}
					
					ref_type -> call_dependents -> RemoveElement(unit_type);
					if ((!unit_dependents.Intersects(*ref_type->call_dependents))
					&& sym->VariableCast()->concrete_types)
					{
						nAdapter++;
						Coutput << "Adapter Pattern." << endl
							<< "Adapting classes: ";
						unit_type->supertypes_closure->Print();
						Coutput << unit_type -> Utf8Name()
							<< " is an adapter class." << endl
							<< ref_type -> Utf8Name()
							<< " is the adaptee class." << endl 
							<< "File Location: " << unit_type -> file_symbol -> FileName() << endl
							<< "File Location: " << ref_type->file_symbol->FileName() << endl << endl;
					}
					ref_type -> call_dependents -> AddElement(unit_type);					
				}
				sym = unit_type -> instances -> NextElement();
			}
		}		
	}
}

void FindFacade(ClassSymbolTable *cs_table)
{
	unsigned p;
	for (p = 0; p < cs_table->size(); p++)
	{
		TypeSymbol *unit_type = (*cs_table)[p];
		SymbolSet all_dependents(0), hidden_types(0);
		if (!unit_type -> ACC_ABSTRACT() 
		&& unit_type -> call_dependents
		&& unit_type -> associates)
		{
			Symbol *sym = unit_type -> associates -> FirstElement();
				while(sym)
			{
				TypeSymbol *type = sym -> TypeCast();
				if (type -> file_symbol
				&& type -> file_symbol -> IsJava()
				&& type->call_dependents
				&& type->call_dependents->IsElement(unit_type)
				)
				{
					type->call_dependents->RemoveElement(unit_type);
					if (!unit_type -> IsFamily(type)
					&& !type -> IsNested()
					&& (!type -> call_dependents 
						||!unit_type -> call_dependents -> Intersects(*type -> call_dependents))
					)
					{
						//if (type -> call_dependents)
						//	all_dependents.Union(*type -> call_dependents);
						hidden_types.AddElement(type);
					}
					type->call_dependents->AddElement(unit_type);
				}
				sym = unit_type -> associates -> NextElement();
			}
			if ((hidden_types.Size() > 1) 
				//&& !unit_type -> call_dependents -> Intersects(all_dependents)
				)
			{
				nFacade++;
				Coutput << "Facade Pattern."
					<< endl
					<< unit_type -> Utf8Name()
					<< " is a facade class."
					<< endl;

				Coutput << "Hidden types: " << hidden_types.FirstElement()->TypeCast() -> Utf8Name();
				while (sym = hidden_types.NextElement())
					Coutput << " " << sym -> TypeCast() -> Utf8Name();
				Coutput << endl;

				Coutput << "Facade access types: " << unit_type->call_dependents->FirstElement()->TypeCast() -> Utf8Name();
				while (sym = unit_type->call_dependents->NextElement())
				{
					if ((sym != unit_type) && !sym->TypeCast()->IsNested())
						Coutput << " " << sym -> TypeCast() -> Utf8Name();
				}
				Coutput << endl;

				Coutput << "File Location: " << unit_type -> file_symbol -> FileName()
					<< endl << endl;

				if (mediators.IsElement(unit_type))
					nMediatorFacadeDual++;

			}
		}
	}
}

void FindThreadSafeInterface(DelegationTable *d_table)
{
	for (int i = 0; i < d_table -> size(); i++)
	{
		DelegationEntry *entry = d_table -> Entry(i);
		if (entry -> enclosing -> ACC_PRIVATE()
		&& (!entry -> base_opt || (entry -> base_opt -> kind == Ast::THIS_CALL))
		&& (entry -> enclosing == entry -> method))
		{
			Coutput << entry -> enclosing -> Utf8Name() << " is a private recursive function." << endl;
			Coutput << entry -> from -> file_symbol -> FileName() << endl;
		}
	}
}

/***
  *	XMI functions
  */

void PrintSingletonXMI(TypeSymbol *class_sym , VariableSymbol *instance_sym, MethodSymbol *method_sym)
{
	static ofstream fd("singleton.xmi");
	static int uid = 0;
	static int uuid = 32768;

	assert(fd.is_open());
	
	if (uid == 0)
	{
		uid++;
		fd << "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" << endl;
		fd << "<XMI xmi.version=\"1.0\">" << endl;
		fd << "  <XMI.header>" << endl;
		fd << "    <XMI.documentation>" << endl;
		fd << "      <XMI.exporter>Novosoft UML Library</XMI.exporter>" << endl;
		fd << "      <XMI.exporterVersion>0.4.20</XMI.exporterVersion>" << endl;
		fd << "    </XMI.documentation>" << endl;
		fd << "    <XMI.metamodel xmi.name=\"UML\" xmi.version=\"1.3\"/>" << endl;
		fd << "  </XMI.header>" << endl;
		fd << "  <XMI.content>" << endl;
		fd << "    <Model_Management.Model xmi.id=\"xmi." << uid++ << "\" xmi.uuid=\"-87--19-7--58-2b323e:102f1204eed:-8000\">" << endl;
		fd << "      <Foundation.Core.ModelElement.name>untitledModel</Foundation.Core.ModelElement.name>" << endl;
		fd << "      <Foundation.Core.Namespace.ownedElement>" << endl;
	}

	int cuid = uid, cuuid = uuid - 1;
	int iuid = cuid + 1, iuuid = cuuid - 1;
	int muid = iuid + 1, muuid = iuuid - 1;
	int ruid = muid + 1, ruuid = muuid - 1;

	uid += 4;
	uuid -= 4;

	if (1)
	{
		fd << "        <Foundation.Core.Class xmi.id=\"xmi." << cuid << "\" xmi.uuid=\"-87--19-7--58-2b323e:102f1204eed:-" << hex << cuuid << dec << "\">" << endl;
		fd << "          <Foundation.Core.ModelElement.name>" << class_sym -> Utf8Name() << "</Foundation.Core.ModelElement.name>" << endl;
		fd << "          <Foundation.Core.ModelElement.visibility xmi.value=\"public\"/>" << endl;
		fd << "          <Foundation.Core.GeneralizableElement.isAbstract xmi.value=\"" << ((class_sym -> ACC_ABSTRACT())?"true":"false") << "\"/>" << endl;
		fd << "          <Foundation.Core.Classifier.feature>" << endl;
		fd << "            <Foundation.Core.Attribute xmi.id=\"xmi." << iuid << "\" xmi.uuid=\"-87--19-7--58-2b323e:102f1204eed:-" << hex << iuuid << dec << "\">" << endl;
		fd << "              <Foundation.Core.ModelElement.name>" << instance_sym -> Utf8Name() << "</Foundation.Core.ModelElement.name>" << endl;
		fd << "              <Foundation.Core.ModelElement.visibility xmi.value=\"private\"/>" << endl;
		fd << "              <Foundation.Core.Feature.ownerScope xmi.value=\"classifier\"/>" << endl;
		fd << "              <Foundation.Core.Feature.owner>" << endl;
		fd << "                <Foundation.Core.Classifier xmi.idref=\"xmi." << cuid << "\"/>" << endl;
		fd << "              </Foundation.Core.Feature.owner>" << endl;
		fd << "              <Foundation.Core.StructuralFeature.type>" << endl;
		fd << "                <Foundation.Core.Classifier xmi.idref=\"xmi." << cuid << "\"/>" << endl;
		fd << "              </Foundation.Core.StructuralFeature.type>" << endl;
		fd << "            </Foundation.Core.Attribute>" << endl;
		fd << "            <Foundation.Core.Operation xmi.id=\"xmi." << muid << "\" xmi.uuid=\"-87--19-7--58-2b323e:102f1204eed:-" << hex << muuid << dec << "\">" << endl;
		fd << "              <Foundation.Core.ModelElement.name>" << method_sym -> Utf8Name() << "</Foundation.Core.ModelElement.name>" << endl;
		fd << "              <Foundation.Core.ModelElement.visibility xmi.value=\"public\"/>" << endl;
		fd << "              <Foundation.Core.Feature.ownerScope xmi.value=\"classifier\"/>" << endl;
		fd << "              <Foundation.Core.Operation.concurrency xmi.value=\"" << ((method_sym -> ACC_SYNCHRONIZED())?"concurrent":"sequential") << "\"/>" << endl;
		fd << "              <Foundation.Core.Feature.owner>" << endl;
		fd << "                <Foundation.Core.Classifier xmi.idref=\"xmi." << cuid << "\"/>" << endl;
		fd << "              </Foundation.Core.Feature.owner>" << endl;
		fd << "              <Foundation.Core.BehavioralFeature.parameter>" << endl;
		fd << "                <Foundation.Core.Parameter xmi.id=\"xmi." << ruid << "\" xmi.uuid=\"-87--19-7--58-2b323e:102f1204eed:-" << hex << ruuid << dec << "\">" << endl;
		fd << "                  <Foundation.Core.Parameter.kind xmi.value=\"return\"/>" << endl;
		fd << "                  <Foundation.Core.Parameter.type>" << endl;
		fd << "                    <Foundation.Core.Classifier xmi.idref=\"xmi." << cuid << "\"/>" << endl;
		fd << "                  </Foundation.Core.Parameter.type>" << endl;
		fd << "                </Foundation.Core.Parameter>" << endl;
		fd << "              </Foundation.Core.BehavioralFeature.parameter>" << endl;
		fd << "            </Foundation.Core.Operation>" << endl;
		fd << "          </Foundation.Core.Classifier.feature>" << endl;
		fd << "        </Foundation.Core.Class>" << endl;
	}
	if (uid == 14)
	{
		fd << "      </Foundation.Core.Namespace.ownedElement>" << endl;
		fd << "    </Model_Management.Model>" << endl;
		fd << "  </XMI.content>" << endl;
		fd << "</XMI>" << endl;
		fd.close();
	}
	
}

/*
 *     Data-Flow analysis
 */

void CreationAnalysis::visit(AstClassCreationExpression *class_creation)
{
	if (class_creation -> class_type -> symbol)
	{
		return_types.push_back(class_creation -> class_type -> symbol -> TypeCast());
	}
}

void CreationAnalysis::visit(AstBlock* block)
{
	// Assumption: isolated entry and exit
	int lstmt = block -> NumStatements() - 1;
	// check the last statement and see what type it returns
	if ( lstmt >= 0)		
	{
		if (block -> Statement(lstmt) -> kind == Ast::RETURN)
		{
			AstReturnStatement *return_stmt = block -> Statement(lstmt) -> ReturnStatementCast();
			AstExpression *expression = (return_stmt -> expression_opt -> kind == Ast::CAST) 
									? return_stmt -> expression_opt -> CastExpressionCast() -> expression
									: return_stmt -> expression_opt;
			
			if (expression -> kind == Ast::CLASS_CREATION)
			{
				expression -> ClassCreationExpressionCast() -> Accept(*this);
			}
			else if ((expression -> kind == Ast::NAME) && (expression -> NameCast() -> symbol -> Kind() == Symbol::VARIABLE))
			{
				// do the backward analysis on this returned vsym
				VariableSymbol *vsym = expression->symbol->VariableCast();
				if (vsym->declarator && vsym->declarator->variable_initializer_opt)
				{
					AstExpression *expr = (vsym->declarator->variable_initializer_opt -> kind == Ast::CAST) 
										? vsym->declarator->variable_initializer_opt -> CastExpressionCast() -> expression
										: vsym->declarator->variable_initializer_opt->ExpressionCast();
					if (expr->kind==Ast::CLASS_CREATION)
						expr->ClassCreationExpressionCast()->Accept(*this);
				}
				else
				{
					signed i = lstmt - 1;
					for (; i >= 0; i--)
					{
						AstExpressionStatement *expression_stmt;
						AstAssignmentExpression *assignment_stmt;

						// should also consider variable initialization upon declaration
						if ((block -> Statement(i) -> kind == Ast::EXPRESSION_STATEMENT)
						&& ((expression_stmt = block -> Statement(i) -> ExpressionStatementCast()) -> expression -> kind == Ast::ASSIGNMENT) 
						&& ((assignment_stmt = expression_stmt -> expression-> AssignmentExpressionCast()) -> lhs(vsym)))					
						{
							AstExpression *expr = (assignment_stmt -> expression -> kind == Ast::CAST) 
												? assignment_stmt -> expression -> CastExpressionCast() -> expression
												: assignment_stmt -> expression;
							if (expr-> kind == Ast::CLASS_CREATION)
								expr -> ClassCreationExpressionCast() -> Accept(*this);
							//else if (expr-> kind == Ast::CALL)
								//expr->MethodInvocationCast()->Accept(*this);
							else if (expr-> kind == Ast::NULL_LITERAL)
								return;
						}
					}
				}
			}
			else if (expression -> kind == Ast::CALL)
			{
				AstMethodInvocation *invocation = expression -> MethodInvocationCast();
				MethodSymbol *method = (invocation -> symbol -> Kind() == Symbol::METHOD) 
									? invocation -> symbol -> MethodCast()
									: NULL;
				if (method && !cache.IsElement(method))
				{
					AstMethodDeclaration *declaration = (method && method -> declaration && method -> declaration -> kind == Ast::METHOD) 
												? method -> declaration -> MethodDeclarationCast() 
												: NULL;
					if (declaration && declaration -> method_body_opt)
					{
						cache.AddElement(method);
						declaration -> method_body_opt -> Accept(*this);
					}
				}
			}
			else if (expression->kind == Ast::ASSIGNMENT)
			{
				if (expression->AssignmentExpressionCast()->expression->kind == Ast::CLASS_CREATION)
					expression->AssignmentExpressionCast()->expression->ClassCreationExpressionCast()->Accept(*this);
			}
		}
		else if (block->Statement(lstmt)->kind==Ast::TRY)
		{
			block->Statement(lstmt)->TryStatementCast()->block->Accept(*this);
		}
	}
}

void ControlAnalysis::visit(AstBlock* block)
{
	unsigned i = 0;
	for (; (i < block -> NumStatements()) && !result; i++)
	{
		visit(block -> Statement(i));
	}
}
void ControlAnalysis::visit(AstSynchronizedStatement *synch_statement)
{
	visit(synch_statement -> block);
	if (!containing_stmt && result)
		containing_stmt =  synch_statement;
	if (result)
		rt_stack.push_back(synch_statement);
}
void ControlAnalysis::visit(AstIfStatement* if_statement)
{
	flag = false;
	cond = if_statement -> expression;
	visit(if_statement -> true_statement);
	if (!result &&  if_statement -> false_statement_opt)
	{
		flag = true;
		visit(if_statement -> false_statement_opt);
	}
	if (!result)
	{
		flag = false;
		cond = 0;
		containing_stmt = 0;
	}
	if (!containing_stmt && result)
		containing_stmt = if_statement;
	if (result)
		rt_stack.push_back(if_statement);
}
void ControlAnalysis::visit(AstConditionalExpression* cond_expression)
{
	visit(cond_expression -> true_expression);
	if (!result)
		visit(cond_expression -> false_expression);

	if (!containing_stmt && result)
		containing_stmt = cond_expression;
	if (result)
		rt_stack.push_back(cond_expression);
}
void ControlAnalysis::visit(AstWhileStatement* while_statement)
{
	visit(while_statement -> statement);
	if (!containing_stmt && result)
		containing_stmt = while_statement;
	if (result)
		rt_stack.push_back(while_statement);
}
void ControlAnalysis::visit(AstForStatement* for_statement)
{
	visit(for_statement -> statement);
	if (!containing_stmt && result) 
		containing_stmt = for_statement;
	if (result)
		rt_stack.push_back(for_statement);		
}
void ControlAnalysis::visit(AstStatement *statement)
{
	switch(statement -> kind) 
	{
		case Ast::IF:
			visit(statement -> IfStatementCast());
			break;
		case Ast::WHILE:
			visit(statement -> WhileStatementCast());
			break;
		case Ast::FOR:
			visit(statement -> ForStatementCast());
			break;
		case Ast::EXPRESSION_STATEMENT:
			visit(statement -> ExpressionStatementCast() -> expression);
			break;
		case Ast::SYNCHRONIZED_STATEMENT:
			visit(statement -> SynchronizedStatementCast());
			break;
		case Ast::BLOCK:
			visit(statement -> BlockCast());
			break;			
		default:
			break;		
	}
}
void ControlAnalysis::visit(AstExpression *expression)
{
	result = (this -> expression == expression);
	if (!result)
	{
		switch(expression -> kind)
		{
			case Ast::PARENTHESIZED_EXPRESSION:
				visit(expression -> ParenthesizedExpressionCast() -> expression);
				break;
			case Ast::CAST:
				visit(expression -> CastExpressionCast() -> expression);
				break;
			case Ast::CONDITIONAL:
				visit(expression -> ConditionalExpressionCast());
				break;
			default:
				break;
		}		
	}
}

bool ControlAnalysis::IsConditional()
{
	for(unsigned i = 0; i < rt_stack.size(); i++)
	{
		if ((rt_stack[i]->kind == Ast::IF) ||(rt_stack[i]->kind == Ast::CONDITIONAL))
			return true;
	}
	return false;
}
bool ControlAnalysis::IsRepeated()
{
	for(unsigned i = 0; i < rt_stack.size(); i++)
	{
		if ((rt_stack[i]->kind == Ast::WHILE) ||(rt_stack[i]->kind == Ast::FOR))
			return true;
	}
	return false;
}
bool ControlAnalysis::IsSynchronized()
{
	for(unsigned i = 0; i < rt_stack.size(); i++)
	{
		if (rt_stack[i]->kind == Ast::SYNCHRONIZED_STATEMENT)
			return true;
	}
	return false;
}

void FlyweightAnalysis::visit(AstBlock* block)
{
	if (block->NumStatements())
	{
		unsigned lstmt = (block->NumStatements() == 1) ? 0 : (block->NumStatements() - 1);
		for (unsigned i = 0; i < lstmt; i++) visit(block -> Statement(i));
		visit(block->Statement(lstmt));
		UpdateSummary();
	}
}
void FlyweightAnalysis::visit(AstWhileStatement* while_statement)
{
	visit(while_statement->statement);
}
void FlyweightAnalysis::visit(AstForStatement* for_statement)
{
	UpdateSummary();
	conditions.push_back(for_statement->end_expression_opt);
	visit(for_statement->statement);	
	UpdateSummary();
	conditions.pop_back();
}
void FlyweightAnalysis::visit(AstTryStatement* try_statement)
{
	visit(try_statement->block);
}
void FlyweightAnalysis::visit(AstStatement *statement)
{
	switch(statement -> kind) 
	{
		case Ast::IF:
			visit(statement -> IfStatementCast());
			break;
		case Ast::WHILE:
			visit(statement -> WhileStatementCast());
			break;
		case Ast::FOR:
			visit(statement -> ForStatementCast());
			break;
		case Ast::TRY:
			visit(statement -> TryStatementCast());
			break;
		case Ast::EXPRESSION_STATEMENT:
			visit(statement -> ExpressionStatementCast() -> expression);
			break;
		case Ast::SYNCHRONIZED_STATEMENT:
			visit(statement -> SynchronizedStatementCast());
			break;
		case Ast::BLOCK:
			visit(statement -> BlockCast());
			break;			
		case Ast::RETURN:
			visit(statement -> ReturnStatementCast());
			break;
		case Ast::LOCAL_VARIABLE_DECLARATION:
			visit(statement -> LocalVariableStatementCast());
			break;
		default:
			break;		
	}
}
void FlyweightAnalysis::visit(AstExpression *expression)
{
	switch(expression -> kind)
	{
		case Ast::PARENTHESIZED_EXPRESSION:
			visit(expression->ParenthesizedExpressionCast() -> expression);
			break;
		case Ast::CAST:
			visit(expression->CastExpressionCast() -> expression);
			break;
		case Ast::CONDITIONAL:
			visit(expression->ConditionalExpressionCast());
			break;
		case Ast::ASSIGNMENT:
			visit(expression->AssignmentExpressionCast());
			break;
		case Ast::CALL:
			visit(expression->MethodInvocationCast());
			break;
		default:
			break;
	}
}
void FlyweightAnalysis::visit(AstMethodInvocation* call)
{
	// might want to check all participants in this method invocationo
	// e.g., base_opt, 	call->symbol->MethodCast()>Type(), call->arguments->Argument(i), etc

	if (call->NumArguments() > 1)
	{
		AstExpression *expression = *&call->arguments->Argument(1);
		expression = (expression->kind == Ast::CAST) ? expression->CastExpressionCast()->expression : expression;
		if (expression->symbol->VariableCast() && (expression->symbol->VariableCast()->Type() == flyweight))
		{
			statements.push_back(call);
		}
	}
}
void FlyweightAnalysis::visit(AstIfStatement* statement)
{
	UpdateSummary();
	conditions.push_back(statement->expression);
	visit(statement->expression);
	visit(statement->true_statement);
	UpdateSummary();
	conditions.pop_back();
	if (statement->false_statement_opt)
		visit(statement->false_statement_opt);
}
void FlyweightAnalysis::visit(AstAssignmentExpression *expression)
{
	if (expression->left_hand_side->symbol->VariableCast() 
	&& (expression->left_hand_side->symbol->VariableCast()->Type() == flyweight))
		statements.push_back(expression);
	else if (expression->left_hand_side->symbol->TypeCast()
	&& (expression->left_hand_side->symbol->TypeCast() == flyweight))
		statements.push_back(expression);
	// TODO also check for aliasing
}
void FlyweightAnalysis::visit(AstLocalVariableStatement* local_var)
{
	if (local_var->type->symbol == flyweight)
	{
		for (unsigned i=0; i < local_var->NumVariableDeclarators(); i++)
			visit(local_var->VariableDeclarator(i));
	}
}
void FlyweightAnalysis::visit(AstVariableDeclarator* var_declarator)
{
	if (var_declarator->variable_initializer_opt && (var_declarator->symbol->Type() == flyweight))
		statements.push_back(var_declarator);
}
void FlyweightAnalysis::visit(AstReturnStatement* statement)
{
	if (statement->expression_opt)
	{
		if (statement->expression_opt->symbol->VariableCast()
		&& (statement->expression_opt->symbol->VariableCast()->Type() == flyweight))
			statements.push_back(statement);
		else if (statement->expression_opt->symbol->TypeCast()
		&& (statement->expression_opt->symbol->TypeCast() == flyweight))
			statements.push_back(statement);
	}	
}
void FlyweightAnalysis::UpdateSummary()
{	
	if (statements.size())
	{
		Snapshot *snapshot = new Snapshot();
		snapshot->statements = new vector<Ast*>(statements);
		statements.clear();
		if (conditions.size())
		{
			snapshot->conditions = new vector<AstExpression*>(conditions);
		}
		snapshot->index = summary.size();
		summary.push_back(snapshot);
	}
}
void FlyweightAnalysis::DumpSummary()
{
	Coutput << GetFlyweight->Utf8Name() << endl;
	for (unsigned i = 0; i < summary.size(); i++)
	{
		Snapshot *snapshot = summary[i];
		Coutput << "Snapshot[" << i << "]" << endl;
		Coutput << "STATEMENTS:" << endl;
		unsigned j;
		for (j = 0; j < snapshot->statements->size(); j++)
		{
			if ((*snapshot->roles)[j]->vsym)
				Coutput << (*snapshot->roles)[j]->vsym->Utf8Name();
			else
				Coutput << (*snapshot->roles)[j]->array_access->base->symbol->VariableCast()->Utf8Name()
					<< "[" << (*snapshot->roles)[j]->array_access->expression->symbol->VariableCast()->Utf8Name() << "]";
			Coutput << ": " << (*snapshot->roles)[j]->TagName() << endl;
			(*snapshot->statements)[j]->Print();
		}
		Coutput << "CONDITIONS:" << endl;
		if (snapshot->conditions)
		{
			for (j = 0; j < snapshot->conditions->size(); j++)
				(*snapshot->conditions)[j]->Print();
		}
		Coutput << endl;
	}
}
char *Role::TagName()
{
	switch(tag)
	{
		case CREATE:
			return "CREATE";
		case REGISTER:
			return "REGISTER";
		case RETRIEVE:
			return "RETRIEVE";
		case ALLOCATE:
			return "ALLOCATE";
		case RETURN:
			return "RETURN";
		case NIL:
			return "NULL";
		default:
			return "N/A";
	}
}
bool MapContainer::IsGetMethod(MethodSymbol *msym)
{
	return (strcmp(msym->Utf8Name(), "get") == 0);
}
bool MapContainer::IsPutMethod(MethodSymbol *msym)
{
	return ((strcmp(msym->Utf8Name(), "put") == 0) && (strcmp(msym->SignatureString(), "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;") == 0));
}
VariableSymbol *MapContainer::GetPutValue(AstMethodInvocation *call)
{
	return (*&call->arguments->Argument(1))->CastExpressionCast()->expression->symbol->VariableCast();
}
TypeSymbol *MapContainer::GetPutType(AstMethodInvocation *call)
{
	Symbol *sym = ((*&call->arguments->Argument(1))->kind == Ast::CAST)
		? (*&call->arguments->Argument(1))->CastExpressionCast()->expression->symbol
		: (*&call->arguments->Argument(1))->symbol;

	return Utility::GetTypeSymbol(sym);
}
bool CollectionContainer::IsPutMethod(MethodSymbol *msym)
{
	// check signature too? or No. Arguments
	return ((strcmp(msym->Utf8Name(), "add") == 0) && (strcmp(msym->SignatureString(), "(Ljava/lang/Object;)Z") == 0));
}
VariableSymbol *CollectionContainer::GetPutValue(AstMethodInvocation *call)
{
	return (*&call->arguments->Argument(0))->CastExpressionCast()->expression->symbol->VariableCast();
}
TypeSymbol *CollectionContainer::GetPutType(AstMethodInvocation *call)
{
	Symbol *sym = ((*&call->arguments->Argument(0))->kind == Ast::CAST)
		? (*&call->arguments->Argument(0))->CastExpressionCast()->expression->symbol
		: (*&call->arguments->Argument(0))->symbol;
	if (sym->Kind() == Symbol::TYPE)
		return sym->TypeCast();
	else if (sym->Kind() == Symbol::VARIABLE)
		return sym->VariableCast()->Type();
	else if (sym->Kind() == Symbol::METHOD)
		return sym->MethodCast()->Type();
	else
		return NULL;
}

bool ArrayListContainer::IsPutMethod(MethodSymbol *msym)
{
	if ((strcmp(msym->Utf8Name(), "add") == 0)
	&& ((strcmp(msym->SignatureString(), "(Ljava/lang/Object;)Z") == 0) || (strcmp(msym->SignatureString(), "(I;Ljava/lang/Object;)V") == 0)))
		return true;
	else
		return false;
}
bool LinkedListContainer::IsPutMethod(MethodSymbol *msym)
{
	if ((strcmp(msym->Utf8Name(), "add") == 0)
	&& ((strcmp(msym->SignatureString(), "(Ljava/lang/Object;)Z") == 0) || (strcmp(msym->SignatureString(), "(I;Ljava/lang/Object;)V") == 0)))
		return true;
	else if (((strcmp(msym->Utf8Name(), "addFirst") == 0) || (strcmp(msym->Utf8Name(), "addLast") == 0))
	&& ((strcmp(msym->SignatureString(), "(Ljava/lang/Object;)V") == 0)))
		return true;
	else
		return false;
}
bool VectorContainer::IsPutMethod(MethodSymbol *msym)
{
	if ((strcmp(msym->Utf8Name(), "add") == 0)
	&& ((strcmp(msym->SignatureString(), "(Ljava/lang/Object;)Z") == 0) || (strcmp(msym->SignatureString(), "(I;Ljava/lang/Object;)V") == 0)))
		return true;
	else if ((strcmp(msym->Utf8Name(), "addElement") == 0)
	&& ((strcmp(msym->SignatureString(), "(Ljava/lang/Object;)V") == 0)))
		return true;
	else
		return false;
}
bool HashSetContainer::IsPutMethod(MethodSymbol *msym)
{
	if ((strcmp(msym->Utf8Name(), "add") == 0) 
	&& (strcmp(msym->SignatureString(), "(Ljava/lang/Object;)Z") == 0))
		return true;
	else
		return false;
}

VariableSymbol *ArrayListContainer::GetPutValue(AstMethodInvocation *call)
{
	if (call->arguments->NumArguments() == 1)
		return (*&call->arguments->Argument(0))->CastExpressionCast()->expression->symbol->VariableCast();
	else
		return (*&call->arguments->Argument(1))->CastExpressionCast()->expression->symbol->VariableCast();
}
VariableSymbol *LinkedListContainer::GetPutValue(AstMethodInvocation *call)
{
	if (call->arguments->NumArguments() == 1)
		return (*&call->arguments->Argument(0))->CastExpressionCast()->expression->symbol->VariableCast();
	else
		return (*&call->arguments->Argument(1))->CastExpressionCast()->expression->symbol->VariableCast();
}
VariableSymbol *VectorContainer::GetPutValue(AstMethodInvocation *call)
{
	if (call->arguments->NumArguments() == 1)
		return (*&call->arguments->Argument(0))->CastExpressionCast()->expression->symbol->VariableCast();
	else
		return (*&call->arguments->Argument(1))->CastExpressionCast()->expression->symbol->VariableCast();
}
VariableSymbol *HashSetContainer::GetPutValue(AstMethodInvocation *call)
{
	return (*&call->arguments->Argument(0))->CastExpressionCast()->expression->symbol->VariableCast();
}

TypeSymbol *ArrayListContainer::GetPutType(AstMethodInvocation *call)
{
	AstExpression *expr = (call->arguments->NumArguments() == 1)
		? (*&call->arguments->Argument(0))
		: (*&call->arguments->Argument(1));

	Symbol *sym = (expr->kind == Ast::CAST)
		? expr->CastExpressionCast()->expression->symbol
		: expr->symbol;

	return Utility::GetTypeSymbol(sym);
}
TypeSymbol *LinkedListContainer::GetPutType(AstMethodInvocation *call)
{
	AstExpression *expr = (call->arguments->NumArguments() == 1)
		? (*&call->arguments->Argument(0))
		: (*&call->arguments->Argument(1));

	Symbol *sym = (expr->kind == Ast::CAST)
		? expr->CastExpressionCast()->expression->symbol
		: expr->symbol;

	return Utility::GetTypeSymbol(sym);
}
TypeSymbol *VectorContainer::GetPutType(AstMethodInvocation *call)
{
	AstExpression *expr = (call->arguments->NumArguments() == 1)
		? (*&call->arguments->Argument(0))
		: (*&call->arguments->Argument(1));

	Symbol *sym = (expr->kind == Ast::CAST)
		? expr->CastExpressionCast()->expression->symbol
		: expr->symbol;

	return Utility::GetTypeSymbol(sym);
}
TypeSymbol *HashSetContainer::GetPutType(AstMethodInvocation *call)
{
	AstExpression *expr = (*&call->arguments->Argument(0));

	Symbol *sym = (expr->kind == Ast::CAST)
		? expr->CastExpressionCast()->expression->symbol
		: expr->symbol;

	return Utility::GetTypeSymbol(sym);
}

void FlyweightAnalysis::AssignRoles()
{
	for (unsigned i = 0; i < summary.size(); i++)
	{
		Snapshot *snapshot = summary[i];
		snapshot->roles = new vector<Role*>();
		for (unsigned j = 0; j < snapshot->statements->size(); j++)
		{
			Ast *stmt = (*snapshot->statements)[j];
			if (stmt->kind == Ast::VARIABLE_DECLARATOR)
			{
				AstVariableDeclarator *var_declarator = stmt->VariableDeclaratorCast();
				if (var_declarator->variable_initializer_opt)
				{
					if (var_declarator->variable_initializer_opt->kind == Ast::CLASS_CREATION)
					{
						//TODO: check parameters as well.
						snapshot->roles->push_back(new Role(var_declarator->symbol, Role::CREATE));
					}
					else if ((var_declarator->variable_initializer_opt->kind == Ast::PARENTHESIZED_EXPRESSION)
					&& (var_declarator->variable_initializer_opt->ParenthesizedExpressionCast()->expression->kind == Ast::CAST)
					&& (var_declarator->variable_initializer_opt->ParenthesizedExpressionCast()->expression->CastExpressionCast()->expression->kind == Ast::CALL))
					{
						AstMethodInvocation *call = var_declarator->variable_initializer_opt->ParenthesizedExpressionCast()->expression->CastExpressionCast()->expression->MethodInvocationCast();
						if (call->base_opt && call->base_opt->symbol->VariableCast())
						{
							if (!container_type)
								container_type = Utility::IdentifyContainerType(call->base_opt->symbol->VariableCast());
							if (container_type && container_type->IsGetMethod(call->symbol->MethodCast()))
								snapshot->roles->push_back(new Role(var_declarator->symbol, Role::RETRIEVE));								
						}
					}
					else if (var_declarator->variable_initializer_opt->kind == Ast::ARRAY_ACCESS)
					{
						if (!container_type)
							container_type = new ArrayContainer(var_declarator->variable_initializer_opt->ArrayAccessCast()->base->symbol->VariableCast());

						//TODO: check for method invocation from a hashtable/collection.
						snapshot->roles->push_back(new Role(var_declarator->symbol, Role::RETRIEVE));
					}
					else if (var_declarator->variable_initializer_opt->kind == Ast::NULL_LITERAL)
					{
						snapshot->roles->push_back(new Role(var_declarator->symbol, Role::NIL));
					}
				}
			}
			else if (stmt->kind == Ast::ASSIGNMENT)
			{
				AstAssignmentExpression *assignment = stmt->AssignmentExpressionCast();
				if (assignment->left_hand_side->kind == Ast::ARRAY_ACCESS)
				{
					if (!container_type)
						container_type = new ArrayContainer(assignment->left_hand_side->ArrayAccessCast()->base->symbol->VariableCast());

					if (assignment->expression->symbol->VariableCast())
					{
						snapshot->roles->push_back(new Role(assignment->expression->symbol->VariableCast(), Role::REGISTER));
					}
					else if (assignment->expression->kind == Ast::CLASS_CREATION)
					{
						snapshot->roles->push_back(new Role(assignment->left_hand_side->ArrayAccessCast(), Role::ALLOCATE));
					}
				}
				else if (assignment->left_hand_side->symbol->VariableCast())
				{
					if (assignment->expression->kind == Ast::CLASS_CREATION)
					{
						//TODO: check parameters as well.
						snapshot->roles->push_back(new Role(assignment->left_hand_side->symbol->VariableCast(), Role::CREATE));
					}
					else if (assignment->expression->kind == Ast::ARRAY_ACCESS)
					{
						if (!container_type)
							container_type = new ArrayContainer(assignment->expression->ArrayAccessCast()->base->symbol->VariableCast());
						snapshot->roles->push_back(new Role(assignment->left_hand_side->symbol->VariableCast(), Role::RETRIEVE));
					}
					else if ((assignment->expression->kind == Ast::PARENTHESIZED_EXPRESSION)
					&& (assignment->expression->ParenthesizedExpressionCast()->expression->kind == Ast::CAST)
					&& (assignment->expression->ParenthesizedExpressionCast()->expression->CastExpressionCast()->expression->kind == Ast::CALL))
					{
						AstMethodInvocation *call = assignment->expression->ParenthesizedExpressionCast()->expression->CastExpressionCast()->expression->MethodInvocationCast();
						if (call->base_opt && call->base_opt->symbol->VariableCast())
						{
							if (!container_type)
								container_type = Utility::IdentifyContainerType(call->base_opt->symbol->VariableCast());
							if (container_type && container_type->IsGetMethod(call->symbol->MethodCast()))
								snapshot->roles->push_back(new Role(assignment->left_hand_side->symbol->VariableCast(), Role::RETRIEVE));								
						}					
					}
				}
			}
			else if (stmt->kind == Ast::CALL)
			{
				AstMethodInvocation *call = stmt->MethodInvocationCast();
				if (call->base_opt && call->base_opt->symbol->VariableCast())
				{
					if (!container_type)
						container_type = Utility::IdentifyContainerType(call->base_opt->symbol->VariableCast());
					if (container_type && container_type->IsPutMethod(call->symbol->MethodCast()))
						snapshot->roles->push_back(new Role(container_type->GetPutValue(call), Role::REGISTER));								
				}
			}
			else if (stmt->kind == Ast::RETURN)
			{
				AstReturnStatement *return_stmt = stmt->ReturnStatementCast();
				if (return_stmt->expression_opt)
				{
					if (return_stmt->expression_opt->symbol->VariableCast())
					{
						snapshot->roles->push_back(new Role(return_stmt->expression_opt->symbol->VariableCast(), Role::RETURN));
					}
					else if (return_stmt->expression_opt->kind == Ast::ARRAY_ACCESS)
					{
						snapshot->roles->push_back(new Role(return_stmt->expression_opt->ArrayAccessCast(), Role::RETURN));
					}
				}
				traces.push_back(snapshot);
			}
		}
	}
	
}
bool FlyweightAnalysis::IsFlyweightFactory()
{
	AssignRoles();
       n = 0;

	for (unsigned t = 0; t < traces.size(); t++)
	{
		VariableSymbol *returned_var = NULL;
		AstArrayAccess *returned_ref = NULL;
		Snapshot *val_recorded = NULL;
		bool create_pending = false;

	for (unsigned i = traces[t]->index; i < summary.size(); i--)
	{
		Snapshot *snapshot = summary[i];
		vector<Role*> *roles = snapshot->roles;
		unsigned j = roles->size() - 1;
		for (; j < roles->size(); j--)
		{
			Role *role = (*roles)[j];
			if ((!returned_var && !returned_ref) && (role->tag == Role::RETURN))
			{
				if (role->vsym)
					returned_var = role->vsym;
				else
				{
					returned_ref = role->array_access;
					bitmap[n] = 'E';
				}
			}
			else if (role->tag == Role::ALLOCATE)
			{
				if ((returned_ref->base->symbol == role->array_access->base->symbol)
				&& (returned_ref->expression->symbol == returned_ref->expression->symbol))
				{
					bitmap[n] = 'N';
					/*
					Coutput << "returns new flyweight object in "
						<< returned_ref->base->symbol->VariableCast()->Utf8Name() << "["
						<< returned_ref->expression->symbol->VariableCast()->Utf8Name() << "]" << endl;
					*/
				}
			}
			else if ((returned_var == role->vsym) && (role->tag == Role::REGISTER))
			{				
				create_pending = true;
			}
			else if (create_pending && (returned_var == role->vsym) && (role->tag == Role::CREATE))
			{
				// the algorithm should reject if a CREATE occurs w/o create_pending
				bitmap[n] = 'N';
				//Coutput << "returns new flyweight object in " << returned_var->Utf8Name() << endl;
				create_pending = false;
				val_recorded = snapshot;				
			}
			else if ((returned_var == role->vsym) && (role->tag == Role::RETRIEVE))
			{
				if (val_recorded && val_recorded->conditions)
				{
					for (unsigned k = 0; k < val_recorded->conditions->size(); k++)
					{
						if ((*val_recorded->conditions)[k]->kind == Ast::BINARY)
						{
							AstBinaryExpression *expression = (*val_recorded->conditions)[k]->BinaryExpressionCast();
							if ((expression->left_expression->symbol == role->vsym)
							&& (expression->right_expression->kind == Ast::NULL_LITERAL)
							&& (expression->Tag()==AstBinaryExpression::EQUAL_EQUAL))
							{
								bitmap[++n] = 'E';
								//Coutput << "returns existing flyweight object in " << returned_var->Utf8Name() << endl;
								break;
							}
						}
					}
				}
				else
				{
					bitmap[n] = 'E';
					//Coutput << "returns existing flyweight object in " << returned_var->Utf8Name() << endl;
					val_recorded = snapshot;
				}
			}
		}
	}
	n++;
	}
	return (n==2) && (((bitmap[0] == 'E') && (bitmap[1] == 'N')) || ((bitmap[0] == 'N') && (bitmap[1] == 'E')));
}

ChainAnalysis::ResultTag ChainAnalysis::AnalyzeCallChain()
{
	flatten.BuildSummary();
	//flatten.DumpSummary();

	for (unsigned t = 0; t < flatten.traces.size(); t++)
	{
		Snapshot *snapshot = flatten.traces[t];
		path.clear();
		path.push_back(snapshot->index);
		TracePath(snapshot);
	}
	
	// analyze exec paths HERE

	// for footprints, check for duplicates
	for (unsigned i = 0; i < footprints.size(); i++)
		for (unsigned j = 0; j < footprints.size(); j++)
			if ((i != j) && (footprints[i] == footprints[j]))
				return NONE;

	unsigned occurrances = 0;
	for (unsigned i = 0; i < paths.size(); i++)
	{
		vector<signed> result;
		Utility::Intersection(footprints, paths[i], result);
		if (result.size() == 1)
			occurrances++;
	}
	if (occurrances == paths.size())
		return DECORATOR;
	else if (occurrances == 1)
		// check for deferral
		return CoR;
	else
		return NONE;
}
void ChainAnalysis::TraceBinaryExpression(AstBinaryExpression *expression, Snapshot *snapshot)
{
	if (expression->left_expression->MethodInvocationCast()
	&& expression->left_expression->MethodInvocationCast()->base_opt
	&& expression->left_expression->MethodInvocationCast()->symbol->MethodCast()
	&& (expression->left_expression->MethodInvocationCast()->base_opt->symbol == variable)
	&& ((expression->left_expression->MethodInvocationCast()->symbol == method) 
		|| (strcmp(expression->left_expression->MethodInvocationCast()->symbol->MethodCast()->Utf8Name(), method->Utf8Name()) == 0))
	&& (strcmp(expression->left_expression->MethodInvocationCast()->symbol->MethodCast()->SignatureString(), method->SignatureString()) == 0)
	)
		footprints.push_back(snapshot->index);
	else if (expression->right_expression->MethodInvocationCast()
	&& expression->right_expression->MethodInvocationCast()->base_opt
	&& expression->right_expression->MethodInvocationCast()->symbol->MethodCast()
	&& (expression->right_expression->MethodInvocationCast()->base_opt->symbol == variable)
	&& ((expression->right_expression->MethodInvocationCast()->symbol == method) 
		|| (strcmp(expression->right_expression->MethodInvocationCast()->symbol->MethodCast()->Utf8Name(), method->Utf8Name()) == 0))
	&& (strcmp(expression->right_expression->MethodInvocationCast()->symbol->MethodCast()->SignatureString(), method->SignatureString()) == 0)
	)
		footprints.push_back(snapshot->index);
	else if (expression->left_expression->kind == Ast::BINARY)
		TraceBinaryExpression(expression->left_expression->BinaryExpressionCast(), snapshot);
	else if (expression->right_expression->kind == Ast::BINARY)
		TraceBinaryExpression(expression->right_expression->BinaryExpressionCast(), snapshot);
}
void ChainAnalysis::TracePath(Snapshot *snapshot)
{	
	set<signed> next(snapshot->previous);

	for (unsigned j = snapshot->statements->size() - 1; j < snapshot->statements->size(); j--)
	{
		Ast *statement = (*snapshot->statements)[j];
		if (statement->kind == Ast::RETURN)
		{
			if (statement->ReturnStatementCast()->expression_opt)
			{
				AstExpression *expression = Utility::RemoveCasting(statement->ReturnStatementCast()->expression_opt);		
				if (expression->kind == Ast::CALL)
				{
					AstMethodInvocation *call = expression->MethodInvocationCast();
					VariableSymbol *vsym = (call->base_opt) 	?  call->base_opt->symbol->VariableCast() : NULL;
					MethodSymbol *msym = call->symbol->MethodCast();
					if ((vsym == variable) 
					&& ((msym == method) || (strcmp(msym->Utf8Name(), method->Utf8Name()) == 0))
					&& (strcmp(msym->SignatureString(), method->SignatureString()) == 0))
					{
						footprints.push_back(snapshot->index);
					}
				}
				else if (expression->kind == Ast::BINARY)
				{
					TraceBinaryExpression(expression->BinaryExpressionCast(), snapshot);
				}
			}
		}
		else if (statement->kind == Ast::CALL)
		{
			AstMethodInvocation *call = statement->MethodInvocationCast();
			VariableSymbol *vsym = (call->base_opt) 	?  call->base_opt->symbol->VariableCast() : NULL;
			MethodSymbol *msym = call->symbol->MethodCast();
			if ((vsym == variable) 
			&& ((msym == method) ||(strcmp(msym->Utf8Name(), method->Utf8Name()) == 0))
			&& (strcmp(msym->SignatureString(), method->SignatureString()) == 0))
			{
				footprints.push_back(snapshot->index);
			}
		}
	}
	set<signed>::iterator p;
	for (p = next.begin(); p != next.end(); p++)
	{
		if (*p >= 0)
		{
			path.push_back(*p);
			TracePath(flatten.summary[*p]);
			path.pop_back();
		}
		else
		{
			paths.push_back(path);
		}
	}	
}
		
SymbolSet SingletonAnalysis::visited;
bool SingletonAnalysis::ReturnsSingleton()
{
	visited.AddElement(method);
	flatten.BuildSummary();
	//flatten.DumpSummary();
	
	for (unsigned t = 0; t < flatten.traces.size(); t++)
	{
		Snapshot *snapshot = flatten.traces[t];
		path.clear();
		path.push_back(snapshot->index);
		TracePath(snapshot);
	}
	// footprints, remove duplicates
	Utility::RemoveDuplicates(footprints);

	if (fingerprints.size() == 0)
		return false;
	else if ((fingerprints.size() == 1) && (footprints.size() == 0))
	{
		return (variable->declarator->variable_initializer_opt && (variable->declarator->variable_initializer_opt->kind == Ast::CLASS_CREATION));
	}
	else if (footprints.size() != 1)
		return false;
	else
	{
		unsigned occurrances = 0;
		for (unsigned i = 0; i < exec_paths.size(); i++)
		{
			vector<signed> result;
			Utility::Intersection(footprints, exec_paths[i], result);
			if (result.size() == 1)
				occurrances++;
		}
		if (occurrances == exec_paths.size())
			return false;

		Snapshot *snapshot = flatten.summary[footprints[0]];
		vector<AstExpression*> conjoints;
		map<VariableSymbol*, AstExpression*> constraints;
		flatten.FlattenBoolean(conjoints, snapshot->condition);
		for (unsigned i = 0; i < conjoints.size(); i++)
		{
			// check whether there are other static variables to track
			// but if "instance == null" is in conjoints, then stop checking
			// otherwise, check if these additional variables are 
			// 1. modified so that this snapshot will never be entered again, and
			// 2. these vars are not changed anywhere besides snapshot (flow-insensitive)

			// consider BINARY and PRE_UNARY expressions
			if (conjoints[i]->kind == Ast::BINARY)
			{
				AstBinaryExpression *expression = (*&conjoints[i])->BinaryExpressionCast();
				if (expression->left_expression->symbol == variable)
				{
					if ((expression->Tag() == AstBinaryExpression::EQUAL_EQUAL)
					&& (expression->right_expression->kind == Ast::NULL_LITERAL))
						return true;
					else
						return false;
				}
				else if (expression->left_expression->symbol->VariableCast())
				{
					VariableSymbol *vsym = expression->left_expression->symbol->VariableCast();
					if (vsym->ACC_PRIVATE()
					&& vsym->ACC_STATIC()
					&& (strcmp(vsym->Type()->Utf8Name(), "boolean") == 0))
						constraints.insert(pair<VariableSymbol*, AstExpression*>(vsym, expression));
				}	
			}
			else if (conjoints[i]->kind == Ast::PRE_UNARY)
			{
				AstPreUnaryExpression *pre_unary = (*&conjoints[i])->PreUnaryExpressionCast();
				if (pre_unary->expression->symbol->VariableCast())
				{
					VariableSymbol *vsym = pre_unary->expression->symbol->VariableCast();
					if (vsym->ACC_PRIVATE()
					&& vsym->ACC_STATIC()
					&& (strcmp(vsym->Type()->Utf8Name(), "boolean") == 0))
						constraints.insert(pair<VariableSymbol*, AstExpression*>(vsym, pre_unary));
				}
				else if ((pre_unary->Tag() == AstPreUnaryExpression::NOT)
					&& (pre_unary->expression->kind == Ast::BINARY)
					&& (pre_unary->expression->BinaryExpressionCast()->left_expression->symbol == variable))
				{
					if ((pre_unary->expression->BinaryExpressionCast()->Tag() == AstBinaryExpression::NOT_EQUAL)
					&& (pre_unary->expression->BinaryExpressionCast()->right_expression->kind == Ast::NULL_LITERAL))
						return true;
					else
						return false;					
				}
			}
			else if (conjoints[i]->symbol->VariableCast())
			{
				VariableSymbol *vsym = conjoints[i]->symbol->VariableCast();
				if (vsym->ACC_PRIVATE()
				&& vsym->ACC_STATIC()
				&& (strcmp(vsym->Type()->Utf8Name(), "boolean") == 0))
					constraints.insert(pair<VariableSymbol*, AstExpression*>(vsym, conjoints[i]));
			}
		}
		if (constraints.size() == 0)
			return false;
		else
		{
			// analyze statements in snapshot, making sure that these control variables close the entrance to this snapshot
			for (unsigned j = (*snapshot->statements).size() - 1; j < (*snapshot->statements).size(); j--)
			{
				Ast *statement = (*snapshot->statements)[j];
				if (statement->kind == Ast::ASSIGNMENT)
				{
					AstAssignmentExpression *assignment = statement->AssignmentExpressionCast();
					if (assignment->left_hand_side->symbol->VariableCast())
					{
						VariableSymbol *vsym = assignment->left_hand_side->symbol->VariableCast();
						map<VariableSymbol*, AstExpression*>::iterator p = constraints.find(vsym);
						if (p != constraints.end())
						{
							// analyze right_hand_side expression
							if (assignment->expression->kind == Ast::TRUE_LITERAL)
							{
								if (((p->second->kind == Ast::PRE_UNARY)
									&& p->second->PreUnaryExpressionCast()->expression->symbol->VariableCast())
								||((p->second->kind == Ast::BINARY)
									&& p->second->BinaryExpressionCast()->left_expression->symbol->VariableCast()
									&& (p->second->BinaryExpressionCast()->right_expression->kind == Ast::FALSE_LITERAL)))
									goto Ugly;								
							}
							else if (assignment->expression->kind == Ast::FALSE_LITERAL)
							{
								if (p->second->symbol->VariableCast()
								||((p->second->kind == Ast::BINARY)
									&& p->second->BinaryExpressionCast()->left_expression->symbol->VariableCast()
									&& (p->second->BinaryExpressionCast()->right_expression->kind == Ast::TRUE_LITERAL)))
									goto Ugly;								
							}
						}
					}
				}
			}
			return false;
						
			// flow-insensitive analysis in summary
			Ugly: SymbolSet modified;
			for (unsigned i =0; i < flatten.summary.size(); i++)
			{
				Snapshot *snapshot = flatten.summary[i];
				if (snapshot->index != footprints[0])
				{
					for (unsigned j = 0; j < (*snapshot->statements).size(); j++)
					{
						Ast *statement = (*snapshot->statements)[j];
						if (statement->kind == Ast::ASSIGNMENT)
						{
							AstAssignmentExpression *assignment = statement->AssignmentExpressionCast();
							if (assignment->left_hand_side->symbol->VariableCast()
							&& (constraints.find(assignment->left_hand_side->symbol->VariableCast()) != constraints.end()))
								modified.AddElement(assignment->left_hand_side->symbol->VariableCast());
								// skip analysis on the right_hand_side expression							
						}
					}
				}
			}
			return (modified.Size() < constraints.size());
		}
	}
}
void SingletonAnalysis::TracePath(Snapshot* snapshot)
{
	set<signed> next(snapshot->previous);

	for (unsigned j = snapshot->statements->size() - 1; j < snapshot->statements->size(); j--)
	{
		Ast *statement = (*snapshot->statements)[j];
		if (statement->kind == Ast::RETURN)
		{
			AstReturnStatement *return_statement = statement->ReturnStatementCast();
			if (return_statement->expression_opt)
			{
				AstExpression *expression = Utility::RemoveCasting(return_statement->expression_opt);
				if (expression->symbol->VariableCast()
				&& (expression->symbol->VariableCast() == variable))
				{
					fingerprints.push_back(snapshot->index);
				}
			}
		}
		else if (statement->kind == Ast::ASSIGNMENT)
		{
			AstAssignmentExpression *assignment = statement->AssignmentExpressionCast();
			if (assignment->left_hand_side->symbol->VariableCast()
			&& (assignment->left_hand_side->symbol == variable))
			{
				AstExpression *expression = Utility::RemoveCasting(assignment->expression);
				if (expression->kind == Ast::CLASS_CREATION)
				{
					for (unsigned i = 0; i < expression->ClassCreationExpressionCast()->arguments->NumArguments(); i++)
						if (expression->ClassCreationExpressionCast()->arguments->Argument(i)->symbol == variable)
							goto pass;
					// check if this class creation negates the dominator condition

					footprints.push_back(snapshot->index);
					pass: ;
				}
				else if (expression->kind == Ast::CALL)
				{
					// Check: are we currnetly under the scope where condition says instance == null?
					// How to check a segment of code is only executed once, regardless of flag?
					
					AstMethodInvocation *call = expression->MethodInvocationCast();
					if ((strcmp(call->symbol->MethodCast()->Utf8Name(), "newInstance") == 0)
					&& (((call->base_opt->kind == Ast::NAME) && (strcmp(call->base_opt->symbol->VariableCast()->Type()->Utf8Name(), "Class") == 0))
						|| ((call->base_opt->kind == Ast::CALL) 
							&& (strcmp(call->base_opt->symbol->MethodCast()->Utf8Name(), "forName") == 0)
							&& (call->base_opt->MethodInvocationCast()->base_opt ->kind== Ast::NAME)
						&& (strcmp(call->base_opt->MethodInvocationCast()->base_opt->symbol->TypeCast()->Utf8Name(), "Class") == 0)))
					)
					{
						footprints.push_back(snapshot->index);
					}
					else
					{
						FactoryAnalysis factory(call->symbol->MethodCast(), ast_pool);
						if (factory.IsCreationMethod())
						{
							footprints.push_back(snapshot->index);
						}
					}
				}
			}
		}
	}
	set<signed>::iterator p;
	for (p = next.begin(); p != next.end(); p++)
	{
		if (*p >= 0)
		{
			path.push_back(*p);
			TracePath(flatten.summary[*p]);
			path.pop_back();
		}					
		else
		{
			//paths.push_back('E');
			exec_paths.push_back(path);
		}										
	}
}
bool SingletonAnalysis::ReturnsSingleton1()
{
#ifdef DONT_BOTHER
	visited.AddElement(method);
	method->declaration->MethodDeclarationCast()->method_body_opt->Accept(flatten);

	// TODO: The following should be included in Flatten.
	for(unsigned i = 0; i < flatten.summary.size(); i++)
	{
		set<signed>::iterator p;
		for (p = (flatten.summary[i]->next).begin(); p != (flatten.summary[i]->next).end(); p++)
			(flatten.summary[*p]->previous).insert(i);
	}
	flatten.DumpSummary();
	
	for (unsigned t = 0; t < flatten.traces.size(); t++)
	{
		Snapshot *return_snapshot = NULL;
		int return_path = -1;

		vector<unsigned> snapshots;
		snapshots.push_back(flatten.traces[t]->index);
		while(!snapshots.empty())
		//for (unsigned i = flatten.traces[t]->index; i < flatten.summary.size(); i--)
		{
			Snapshot *snapshot = flatten.summary[snapshots[snapshots.size() - 1]];
			snapshots.pop_back();
			/*
			if ((i == flatten.traces[t]->index)
			|| ((i < flatten.traces[t]->index) 
				&& (flatten.TransitionFlow(snapshot->condition, flatten.summary[i + 1]->condition) != Flatten::NOTRANSITION)))
			{
			*/
			for (unsigned j = snapshot->statements->size() - 1; j < snapshot->statements->size(); j--)
			{
				Ast *statement = (*snapshot->statements)[j];
				if (statement->kind == Ast::RETURN)
				{
					AstReturnStatement *return_statement = statement->ReturnStatementCast();
					if (return_statement->expression_opt)
					{
						AstExpression *expression = Utility::RemoveCasting(return_statement->expression_opt);
						if (expression->kind == Ast::NULL_LITERAL)
							goto stop_trace;
						else if (expression->symbol->VariableCast()
							&& (expression->symbol->VariableCast() == variable))
						{
							return_snapshot = snapshot;
							return_path = paths.size();
							paths.push_back('E');							
						}
						else
							return false;
					}
				}
				else if (return_snapshot && (statement->kind == Ast::ASSIGNMENT))
				{
					AstAssignmentExpression *assignment = statement->AssignmentExpressionCast();
					if (assignment->left_hand_side->symbol->VariableCast()
					&& (assignment->left_hand_side->symbol == variable))
					{
						AstExpression *expression = Utility::RemoveCasting(assignment->expression);
						if (expression->kind == Ast::CLASS_CREATION)
						{
							// check if this class creation negates the dominator condition
							vector<AstExpression*> conjoints;
							flatten.FlattenBoolean(conjoints, snapshot->condition);
							for (unsigned i = 0; i < conjoints.size(); i++)
							{
								if ((conjoints[i]->kind == Ast::BINARY)
								&& ((*&conjoints[i])->BinaryExpressionCast()->Tag() == AstBinaryExpression::EQUAL_EQUAL)
								&& ((*&conjoints[i])->BinaryExpressionCast()->left_expression->symbol == variable)
								&& ((*&conjoints[i])->BinaryExpressionCast()->right_expression->kind == Ast::NULL_LITERAL))
								{
									paths[return_path] = 'N';
									goto stop_trace;
								}
							}
						}
						else if (expression->kind == Ast::CALL)
						{
							AstMethodInvocation *call = expression->MethodInvocationCast();
							if ((strcmp(call->symbol->MethodCast()->Utf8Name(), "newInstance") == 0)
							//&& (call->base_opt->kind == Ast::CALL)
							&& (strcmp(call->base_opt->symbol->MethodCast()->Utf8Name(), "forName") == 0)
							//&& (call->base_opt->MethodInvocationCast()->base_opt ->kind== Ast::NAME)
							&& (strcmp(call->base_opt->MethodInvocationCast()->base_opt->symbol->TypeCast()->Utf8Name(), "Class") == 0))
							{
								paths[return_path] = 'N';
								goto stop_trace;
							}
						}
						else if (expression->kind == Ast::NULL_LITERAL)
						{
							paths[return_path] = 'X';
							goto stop_trace;
						}
					}
				}
			}
			/*
			}
			*/
			// check if it's possible to go to the previous snapshot.
			set<signed>::iterator p;
			for(p = (snapshot->previous).begin(); p != (snapshot->previous).end(); p++)
				snapshots.push_back(*p);
		}
		stop_trace: return_snapshot = NULL;
		return_path = -1;
	}

	int nc = 0, ne = 0;
	for (unsigned i = 0; i < paths.size(); i++)
	{
		if (paths[i] == 'N')
			nc++;
		else if (paths[i] == 'E')
			ne++;
	}
	if ((nc==1) && (ne == 1))
		return true;
	else if ((ne == 1) 
		&& variable->declarator->variable_initializer_opt 
		&& (variable->declarator->variable_initializer_opt->kind == Ast::CLASS_CREATION))
		return true;
	else
		return false;
#endif
return false;
}

SymbolSet FactoryAnalysis::visited;
SymbolSet FactoryAnalysis::types;
bool FactoryAnalysis::IsFactoryMethod()
{
	//Coutput << "Analyzing " << method->Utf8Name() << endl;
	
	visited.AddElement(method);
	method->declaration->MethodDeclarationCast()->method_body_opt->Accept(flatten);
	//flatten.DumpSummary();

	for (unsigned t = 0; t < flatten.traces.size(); t++)
	{
		VariableSymbol *returned_var = NULL;

		for (unsigned i = flatten.traces[t]->index; i < flatten.summary.size(); i--)
		{
			Snapshot *snapshot = flatten.summary[i];
			for (unsigned j = snapshot->statements->size() - 1; j < snapshot->statements->size(); j--)
			{
				Ast *stmt = (*snapshot->statements)[j];
				if (stmt->kind == Ast::RETURN)
				{
					AstReturnStatement *return_stmt = stmt->ReturnStatementCast();
					if (return_stmt->expression_opt)
					{
						AstExpression *expression = Utility::RemoveCasting(return_stmt->expression_opt);
						
						if (expression->symbol->VariableCast())
							returned_var = expression->symbol->VariableCast();
						else if (expression->symbol->MethodCast())
						{
							if (expression->kind == Ast::CLASS_CREATION)
							{
								types.AddElement(expression->symbol->MethodCast()->Type());
								break;
							}
							else if (expression->kind == Ast::CALL)
							{
								// inter-procedural
								if (!visited.IsElement(expression->symbol)
								&& expression->symbol->MethodCast()->declaration
								&& expression->symbol->MethodCast()->declaration->MethodDeclarationCast()
								&& expression->symbol->MethodCast()->declaration->MethodDeclarationCast()->method_body_opt)
								{
									FactoryAnalysis defer(expression->symbol->MethodCast(), ast_pool);
									if (defer.IsFactoryMethod())
										break;
								}
							}
						}
						// Jikes does not compile when returning empty_statement
						else if (expression->symbol->TypeCast())
						{
							if (expression->kind == Ast::NULL_LITERAL)
								return false;
						}
					}					
				}
				else if (stmt->kind == Ast::ASSIGNMENT)
				{
					AstAssignmentExpression *assignment = stmt->AssignmentExpressionCast();
					if (assignment->left_hand_side->symbol->VariableCast()
					&& (assignment->left_hand_side->symbol == returned_var))
					{
						AstExpression *expression = Utility::RemoveCasting(assignment->expression);
						if (expression->kind == Ast::CLASS_CREATION)
						{
							//types.AddElement(expression->symbol->MethodCast()->Type());
							types.AddElement(expression->ClassCreationExpressionCast()->class_type->symbol->TypeCast());
							break;
						}
						else if (expression->kind == Ast::NULL_LITERAL)
							return false;
						else if (expression->kind == Ast::CALL)
						{
							// inter-procedural
							if (!visited.IsElement(expression->symbol)
							&& expression->symbol->MethodCast()->declaration
							&& expression->symbol->MethodCast()->declaration->MethodDeclarationCast()
							&& expression->symbol->MethodCast()->declaration->MethodDeclarationCast()->method_body_opt)
							{
								FactoryAnalysis defer(expression->symbol->MethodCast(), ast_pool);
								if (defer.IsFactoryMethod())
									break;
							}
						}
						else if (expression->symbol->VariableCast())
						{
							// aliasing
							returned_var = expression->symbol->VariableCast();
						}
					}					
				}
				else if (stmt->kind == Ast::VARIABLE_DECLARATOR)
				{
					AstVariableDeclarator *var_declarator = stmt->VariableDeclaratorCast();
					if (var_declarator->symbol == returned_var)
					{
						if (var_declarator->variable_initializer_opt 
						&& var_declarator->variable_initializer_opt->ExpressionCast())
						{
							AstExpression *expression = Utility::RemoveCasting(var_declarator->variable_initializer_opt->ExpressionCast());
							if (expression->kind == Ast::CLASS_CREATION)
							{
								//types.AddElement(expression->symbol->MethodCast()->Type());
								types.AddElement(expression->ClassCreationExpressionCast()->class_type->symbol->TypeCast());								
								break;
							}
							else if (expression->kind == Ast::NULL_LITERAL)
								return false;						
							else if (expression->kind == Ast::CALL)
							{
								// inter-procedural
								if (!visited.IsElement(expression->symbol)
								&& expression->symbol->MethodCast()->declaration
								&& expression->symbol->MethodCast()->declaration->MethodDeclarationCast()
								&& expression->symbol->MethodCast()->declaration->MethodDeclarationCast()->method_body_opt)
								{
									FactoryAnalysis defer(expression->symbol->MethodCast(), ast_pool);
									if (defer.IsFactoryMethod())
										break;
								}
							}
							else if (expression->symbol->VariableCast())
							{
								// aliasing
								returned_var = expression->symbol->VariableCast();
							}
						}
						// variable unitialized and never assigned with a value is a Jikes error
						else if (!var_declarator->variable_initializer_opt)
							return false;
					}
				}
			}			
		}
	}
	return (types.Size() && !types.IsElement(method->Type()));

}
bool FactoryAnalysis::IsCreationMethod()
{
	//Coutput << "Analyzing " << method->Utf8Name() << endl;
	
	visited.AddElement(method);
	method->declaration->MethodDeclarationCast()->method_body_opt->Accept(flatten);
	//flatten.DumpSummary();

	for (unsigned t = 0; t < flatten.traces.size(); t++)
	{
		VariableSymbol *returned_var = NULL;

		for (unsigned i = flatten.traces[t]->index; i < flatten.summary.size(); i--)
		{
			Snapshot *snapshot = flatten.summary[i];
			for (unsigned j = snapshot->statements->size() - 1; j < snapshot->statements->size(); j--)
			{
				Ast *stmt = (*snapshot->statements)[j];
				if (stmt->kind == Ast::RETURN)
				{
					AstReturnStatement *return_stmt = stmt->ReturnStatementCast();
					if (return_stmt->expression_opt)
					{
						AstExpression *expression = Utility::RemoveCasting(return_stmt->expression_opt);
						
						if (expression->symbol->VariableCast())
							returned_var = expression->symbol->VariableCast();
						else if (expression->symbol->MethodCast())
						{
							if (expression->kind == Ast::CLASS_CREATION)
							{
								types.AddElement(expression->symbol->MethodCast()->Type());
								break;
							}
							else if (expression->kind == Ast::CALL)
							{
								// inter-procedural
								if (!visited.IsElement(expression->symbol)
								&& expression->symbol->MethodCast()->declaration
								&& expression->symbol->MethodCast()->declaration->MethodDeclarationCast()
								&& expression->symbol->MethodCast()->declaration->MethodDeclarationCast()->method_body_opt)
								{
									FactoryAnalysis defer(expression->symbol->MethodCast(), ast_pool);
									if (defer.IsCreationMethod())
										break;
								}
							}
						}
						// Jikes does not compile when returning empty_statement
						else if (expression->symbol->TypeCast())
						{
							if (expression->kind == Ast::NULL_LITERAL)
								return false;
						}
					}					
				}
				else if (stmt->kind == Ast::ASSIGNMENT)
				{
					AstAssignmentExpression *assignment = stmt->AssignmentExpressionCast();
					if (assignment->left_hand_side->symbol->VariableCast()
					&& (assignment->left_hand_side->symbol == returned_var))
					{
						AstExpression *expression = Utility::RemoveCasting(assignment->expression);
						if (expression->kind == Ast::CLASS_CREATION)
						{
							types.AddElement(expression->symbol->MethodCast()->Type());
							break;
						}
						else if (expression->kind == Ast::NULL_LITERAL)
							return false;
						else if (expression->kind == Ast::CALL)
						{
							// inter-procedural
							if (!visited.IsElement(expression->symbol)
							&& expression->symbol->MethodCast()->declaration
							&& expression->symbol->MethodCast()->declaration->MethodDeclarationCast()
							&& expression->symbol->MethodCast()->declaration->MethodDeclarationCast()->method_body_opt)
							{
								FactoryAnalysis defer(expression->symbol->MethodCast(), ast_pool);
								if (defer.IsCreationMethod())
									break;
							}
						}
						else if (expression->symbol->VariableCast())
						{
							// aliasing
							returned_var = expression->symbol->VariableCast();
						}
					}					
				}
				else if (stmt->kind == Ast::VARIABLE_DECLARATOR)
				{
					AstVariableDeclarator *var_declarator = stmt->VariableDeclaratorCast();
					if (var_declarator->symbol == returned_var)
					{
						if (var_declarator->variable_initializer_opt 
						&& var_declarator->variable_initializer_opt->ExpressionCast())
						{
							AstExpression *expression = var_declarator->variable_initializer_opt->ExpressionCast();
							if (expression->kind == Ast::CLASS_CREATION)
							{
								types.AddElement(expression->symbol->MethodCast()->Type());
								break;
							}
							else if (expression->kind == Ast::NULL_LITERAL)
								return false;						
							else if (expression->kind == Ast::CALL)
							{
								// inter-procedural
								if (!visited.IsElement(expression->symbol)
								&& expression->symbol->MethodCast()->declaration
								&& expression->symbol->MethodCast()->declaration->MethodDeclarationCast()
								&& expression->symbol->MethodCast()->declaration->MethodDeclarationCast()->method_body_opt)
								{
									FactoryAnalysis defer(expression->symbol->MethodCast(), ast_pool);
									if (defer.IsCreationMethod())
										break;
								}
							}
							else if (expression->symbol->VariableCast())
							{
								// aliasing
								returned_var = expression->symbol->VariableCast();
							}
						}
						// variable unitialized and never assigned with a value is a Jikes error
						else if (!var_declarator->variable_initializer_opt)
							return false;
					}
				}
			}			
		}
	}
	return ((types.Size() == 1) && types.IsElement(method->Type()));

}

void EmitExpressionAssociation(TypeSymbol * unit_type, MethodSymbol * enclosing_method, AstExpression * expression, DelegationTable * d_table, WriteAccessTable * w_table);
void EmitStatementAssociation(TypeSymbol * unit_type, MethodSymbol * enclosing_method, AstStatement * statement, DelegationTable * d_table, WriteAccessTable * w_table, ReadAccessTable *r_table);

void EmitGeneralization(GenTable * gen_table, TypeSymbol * unit_type)
{
	//AstClassBody* class_body = unit_type -> declaration;

    	wchar_t* package_name = unit_type -> FileLoc();
    	wchar_t* class_name = const_cast<wchar_t*>(unit_type -> Name());
	wchar_t* super_name = const_cast<wchar_t*>(unit_type -> super -> Name());
	
    	vector<wchar_t*>* interfaces = NULL;
	for (unsigned k = 0; k < unit_type -> NumInterfaces(); k++)
    	{
        	if (interfaces == NULL)
			interfaces = new vector<wchar_t*>();
  	 	interfaces -> push_back(const_cast<wchar_t*>(unit_type -> Interface(k) -> Name()));
    	}

	Gen::Kind kind;
	if (unit_type -> ACC_INTERFACE())
		kind = Gen::INTERFACE;
	else if (unit_type -> ACC_FINAL())
		kind = Gen::FINAL;
	else if (unit_type -> ACC_ABSTRACT())
		kind = Gen::ABSTRACT;
	else
		kind = Gen::CLASS;

	gen_table -> addGeneralization(package_name, class_name, super_name, interfaces, kind, const_cast<char*>(unit_type -> SignatureString()));
}

void EmitBlockAssociation(TypeSymbol * unit_type, MethodSymbol * enclosing_method, AstBlock * block, DelegationTable * d_table, WriteAccessTable * w_table, ReadAccessTable *r_table)
{	
	for (unsigned i = 0; i < block -> NumStatements(); i++)
		EmitStatementAssociation(unit_type, enclosing_method, block -> Statement(i), d_table, w_table, r_table);
}

void EmitDelegation(TypeSymbol * unit_type, MethodSymbol * enclosing_method, AstMethodInvocation * expression, DelegationTable * d_table, WriteAccessTable * w_table)
{
	//
	// If the method call was resolved into a call to another method, use the
    	// resolution expression.
    	//
    	AstMethodInvocation* method_call = expression -> resolution_opt
    		? expression -> resolution_opt -> MethodInvocationCast() : expression;	
	assert(method_call);

	MethodSymbol *msym = (MethodSymbol*) method_call -> symbol;
	VariableSymbol *vsym = (method_call -> base_opt 
							&& method_call -> base_opt -> kind == Ast::NAME 
							&& (method_call -> base_opt -> symbol -> Kind() == Symbol::VARIABLE)) 
						? method_call -> base_opt -> symbol -> VariableCast()
						: NULL;


	d_table -> InsertDelegation(unit_type, msym -> containing_type, method_call -> base_opt, vsym, msym, enclosing_method, method_call);
	if (!msym -> containing_type -> call_dependents)
		msym -> containing_type -> call_dependents = new SymbolSet(0);
	msym -> containing_type -> call_dependents -> AddElement(unit_type);
	if (!msym -> callers)
		msym -> callers = new SymbolSet(0);
	msym -> callers -> AddElement(unit_type);
	
	if (!msym -> invokers)
		msym -> invokers = new SymbolSet(0);
	msym -> invokers -> AddElement(enclosing_method);
	
	if (!enclosing_method -> invokees)
		enclosing_method -> invokees = new SymbolSet(0);
	enclosing_method -> invokees -> AddElement(msym);

	if (!unit_type -> associates)
		unit_type -> associates = new SymbolSet(0);
	unit_type -> associates -> AddElement(msym -> containing_type);
	
	if (method_call -> base_opt)
		EmitExpressionAssociation(unit_type, enclosing_method, method_call -> base_opt, d_table, w_table);
	
	AstArguments *args = expression -> arguments;
	for (unsigned i = 0; i < args -> NumArguments(); i++)
	{
		if (args->Argument(i)->symbol->VariableCast())
		{
			if (!msym->FormalParameter(i)->aliases)
				msym->FormalParameter(i)->aliases = new SymbolSet();
			msym->FormalParameter(i)->aliases->AddElement(args->Argument(i)->symbol->VariableCast());
		}
		EmitExpressionAssociation(unit_type, enclosing_method, args -> Argument(i), d_table, w_table);
	}
}

void EmitReadAccess(TypeSymbol * unit_type, MethodSymbol * enclosing_method, AstName * name, ReadAccessTable * r_table)
{
	if (name -> symbol -> Kind() == Symbol::VARIABLE)
	{
		VariableSymbol *vsym = name -> symbol -> VariableCast();
		if (vsym -> IsLocal())
			vsym = unit_type -> Shadows(vsym);
		if (vsym)
			r_table -> InsertReadAccess(vsym, enclosing_method);
	}
}

void EmitWriteAccess(TypeSymbol * unit_type, MethodSymbol * enclosing_method, AstAssignmentExpression * assignment, DelegationTable * d_table, WriteAccessTable * w_table)
{
	AstExpression *left_expression = assignment->left_hand_side;
	VariableSymbol *vsym = NULL;
	
	if (left_expression->kind == Ast::DOT)
		vsym = left_expression->FieldAccessCast()->symbol->VariableCast();
	else if ((left_expression -> kind == Ast::NAME) && (left_expression->symbol->Kind()==Symbol::VARIABLE))
	{
	/*
		left_expression = (left_expression->NameCast()->resolution_opt)
			? left_expression->NameCast()->resolution_opt
			: left_expression;
	*/
		vsym = left_expression -> symbol -> VariableCast();
	}
#ifdef GOF_CONSOLE
		if (vsym -> ContainingType() == unit_type)
			Coutput << vsym -> ContainingType() -> Utf8Name() 
				<< "::" 
				<< enclosing_method -> Utf8Name() 
				<< " accesses a private field " 
				<< vsym -> Utf8Name() 
				<< ": "
				<< vsym -> Type () -> Utf8Name()
				<< endl;
#endif
	if (vsym)
	{
		w_table -> InsertWriteAccess(vsym, enclosing_method);

		/*
		AstExpression *rhs_expression = (assignment-> expression -> kind == Ast::CAST) 
			? assignment-> expression -> CastExpressionCast() -> expression
			: assignment-> expression;
		*/

		AstExpression *rhs_expression = Utility::RemoveCasting(assignment-> expression);

		if (vsym && rhs_expression -> kind == Ast::CLASS_CREATION)
		{
				if (!vsym -> concrete_types)
					vsym -> concrete_types = new SymbolSet(0);
				vsym -> concrete_types -> AddElement(rhs_expression -> ClassCreationExpressionCast() -> class_type -> symbol -> TypeCast());
		}
		if (rhs_expression->symbol->VariableCast())
		{
			if (!vsym->aliases)
				vsym->aliases = new SymbolSet();
			vsym->aliases->AddElement(rhs_expression->symbol->VariableCast());
		}
		
	}
	EmitExpressionAssociation(unit_type, enclosing_method, assignment-> expression, d_table, w_table);
}

void EmitExpressionAssociation(TypeSymbol * unit_type, MethodSymbol * enclosing_method, AstExpression * expression, DelegationTable * d_table, WriteAccessTable * w_table)
{
	switch(expression -> kind)
	{
		case Ast::CLASS_CREATION:
			if (!unit_type -> associates)
				unit_type -> associates = new SymbolSet(0);
			if (expression->ClassCreationExpressionCast()->class_type->symbol)
				unit_type -> associates -> AddElement(((AstClassCreationExpression*)expression) -> class_type -> symbol -> TypeCast());
			else
				unit_type->associates->AddElement(expression->ClassCreationExpressionCast()->symbol->MethodCast()->containing_type);
			break;
		case Ast::CALL:
			EmitDelegation(unit_type, enclosing_method, (AstMethodInvocation*)expression, d_table, w_table);
			break;
		case Ast::ASSIGNMENT:
			EmitWriteAccess(unit_type, enclosing_method, (AstAssignmentExpression*)expression, d_table, w_table);
			EmitExpressionAssociation(unit_type, enclosing_method, ((AstAssignmentExpression*)expression) -> expression, d_table, w_table);			
			break;
		case Ast::CONDITIONAL:
			EmitExpressionAssociation(unit_type, enclosing_method, ((AstConditionalExpression*)expression) -> test_expression, d_table, w_table);
			EmitExpressionAssociation(unit_type, enclosing_method, ((AstConditionalExpression*)expression) -> true_expression, d_table, w_table);
			EmitExpressionAssociation(unit_type, enclosing_method, ((AstConditionalExpression*)expression )-> false_expression, d_table, w_table);
			break;
		case Ast::CAST:
			EmitExpressionAssociation(unit_type, enclosing_method, ((AstCastExpression*)expression )-> expression, d_table, w_table);
			break;
		case Ast::PARENTHESIZED_EXPRESSION:
			EmitExpressionAssociation(unit_type, enclosing_method, ((AstParenthesizedExpression*)expression )-> expression, d_table, w_table);			
			break;
		case Ast::BINARY:
			EmitExpressionAssociation(unit_type, enclosing_method, ((AstBinaryExpression*)expression )-> left_expression, d_table, w_table);			
			EmitExpressionAssociation(unit_type, enclosing_method, ((AstBinaryExpression*)expression )-> right_expression, d_table, w_table);			
			break;			
		default:
			break;
	}
}

void EmitStatementAssociation(TypeSymbol * unit_type, MethodSymbol * enclosing_method, AstStatement * statement, DelegationTable * d_table, WriteAccessTable * w_table, ReadAccessTable *r_table)
{
	switch (statement -> kind)
  {
	    	case Ast::METHOD_BODY:
    		case Ast::BLOCK: // JLS 14.2
       	{
					EmitBlockAssociation(unit_type, enclosing_method, (AstBlock*) statement, d_table, w_table, r_table);
				}
					break;
    		case Ast::LOCAL_VARIABLE_DECLARATION: // JLS 14.3
      	{
    			AstLocalVariableStatement *local = (AstLocalVariableStatement *)statement;
					for (unsigned i = 0; i < local -> NumVariableDeclarators(); i++)
			        EmitStatementAssociation(unit_type, enclosing_method, local -> VariableDeclarator(i), d_table, w_table, r_table);
			  }     
    			break;
	    	case Ast::EMPTY_STATEMENT: // JLS 14.5
       		break;
	    	case Ast::EXPRESSION_STATEMENT: // JLS 14.7
				{
					EmitExpressionAssociation(unit_type, enclosing_method, statement -> ExpressionStatementCast() -> expression, d_table, w_table);
        }
					break;
	    	case Ast::IF: // JLS 14.8 
	    	{
	       	AstIfStatement* if_statement = (AstIfStatement*) statement;
					EmitExpressionAssociation(unit_type, enclosing_method, if_statement -> expression, d_table, w_table);
					EmitBlockAssociation(unit_type, enclosing_method, if_statement -> true_statement, d_table, w_table, r_table);
					if (if_statement -> false_statement_opt)
						EmitBlockAssociation(unit_type, enclosing_method, if_statement -> false_statement_opt, d_table, w_table, r_table);
				}
					break;
	    	case Ast::SWITCH: // JLS 14.9
	   		{
					AstSwitchStatement *cp = statement -> SwitchStatementCast();
					EmitExpressionAssociation(unit_type, enclosing_method, cp -> expression, d_table, w_table);
					EmitBlockAssociation(unit_type, enclosing_method, cp-> switch_block, d_table, w_table, r_table);
				}
					break;
	    	case Ast::SWITCH_BLOCK: // JLS 14.9
	    	{
					EmitBlockAssociation(unit_type, enclosing_method, statement -> BlockCast(), d_table, w_table, r_table);
				}
					break;
	    	case Ast::SWITCH_LABEL:
					break;
				case Ast::WHILE: // JLS 14.10
				{
					AstWhileStatement* wp = statement -> WhileStatementCast();
					EmitExpressionAssociation(unit_type, enclosing_method, wp -> expression, d_table, w_table);
	       	EmitBlockAssociation(unit_type, enclosing_method, wp -> statement, d_table, w_table, r_table);
				}
					break;
				case Ast::DO: // JLS 14.11
				{
	       	AstDoStatement* sp = statement -> DoStatementCast();
					EmitExpressionAssociation(unit_type, enclosing_method, sp -> expression, d_table, w_table);					
					EmitBlockAssociation(unit_type, enclosing_method, sp -> statement, d_table, w_table, r_table);
				}
					break;
	    	case Ast::FOR: // JLS 14.12
	    	{
	       	AstForStatement* for_statement = statement -> ForStatementCast();
					if (for_statement -> end_expression_opt)
						EmitExpressionAssociation(unit_type, enclosing_method, for_statement -> end_expression_opt, d_table, w_table);
					unsigned i;
					for (i = 0; i < for_statement -> NumForInitStatements(); i++)
						EmitStatementAssociation(unit_type, enclosing_method, for_statement -> ForInitStatement(i), d_table, w_table, r_table);
					for (i = 0; i < for_statement -> NumForUpdateStatements(); i++)
						EmitStatementAssociation(unit_type, enclosing_method, for_statement -> ForUpdateStatement(i), d_table, w_table, r_table);
					EmitBlockAssociation(unit_type, enclosing_method, for_statement -> statement, d_table, w_table, r_table);
				}
					break;
	    	case Ast::FOREACH: // JSR 201
				case Ast::BREAK: // JLS 14.13
				case Ast::CONTINUE: // JLS 14.14
					break;
				case Ast::RETURN: // JLS 14.15
				{
					AstReturnStatement *rp = statement -> ReturnStatementCast();
					if (rp -> expression_opt)
					{
						if (rp -> expression_opt -> kind == Ast::NAME)
							EmitReadAccess(unit_type, enclosing_method, rp -> expression_opt -> NameCast(), r_table);
						else
							EmitExpressionAssociation(unit_type, enclosing_method, rp -> expression_opt, d_table, w_table);			
					}	
				}
					break;
				case Ast::SUPER_CALL:
	    	case Ast::THIS_CALL:
    		case Ast::THROW: // JLS 14.16
    			break;
		case Ast::SYNCHRONIZED_STATEMENT: // JLS 14.17
		{
			EmitBlockAssociation(unit_type, enclosing_method, statement -> SynchronizedStatementCast() -> block, d_table, w_table, r_table);
		}
			break;
		case Ast::TRY: // JLS 14.18
		{
			EmitBlockAssociation(unit_type, enclosing_method, statement -> TryStatementCast() -> block, d_table, w_table, r_table);
		}
			break;
		case Ast::CATCH:   // JLS 14.18
		case Ast::FINALLY: // JLS 14.18
		case Ast::ASSERT: // JDK 1.4 (JSR 41)
		case Ast::LOCAL_CLASS: // Class Declaration
		        //
		        // This is factored out by the front end; and so must be
		        // skipped here (remember, interfaces cannot be declared locally).
		        //
			break;
		case Ast::VARIABLE_DECLARATOR:
		{
			AstVariableDeclarator *vd = statement -> VariableDeclaratorCast();
			if (vd -> variable_initializer_opt && vd -> variable_initializer_opt -> ExpressionCast())
			{
				AstExpression *rhs_expression = Utility::RemoveCasting(vd->variable_initializer_opt->ExpressionCast());
				if (rhs_expression->symbol->VariableCast())
				{
					if (!vd->symbol->aliases)
						vd->symbol->aliases = new SymbolSet();
					vd->symbol->aliases->AddElement(rhs_expression->symbol->VariableCast());
				}

				EmitExpressionAssociation(unit_type, enclosing_method, vd -> variable_initializer_opt -> ExpressionCast(), d_table, w_table);
			}
		}
			break;
		default:
			break;
	}
}

void ExtractStructure(WriteAccessTable *w_table, ReadAccessTable *r_table, DelegationTable *d_table, ClassSymbolTable *cs_table, MethodBodyTable* mb_table, MethodSymbolTable *ms_table, GenTable* gen_table, AssocTable* assoc_table, TypeSymbol* unit_type, StoragePool* ast_pool)
{
	//Coutput << unit_type->fully_qualified_name->value << endl;
	if (unit_type->Anonymous() && (unit_type->NumInterfaces() || unit_type->super))
	{
		if (!unit_type->supertypes_closure)
			unit_type->supertypes_closure = new SymbolSet(0);
		if (unit_type->NumInterfaces())
			unit_type->supertypes_closure->AddElement(unit_type->Interface(0));
		if (unit_type->super)
			unit_type->supertypes_closure->AddElement(unit_type->super);
	}

    Semantic& semantic = *unit_type -> semantic_environment -> sem;
    LexStream *lex_stream = semantic.lex_stream;

    wchar_t* package_name = unit_type -> FileLoc();

    AstClassBody* class_body = unit_type -> declaration;

    class_body -> Lexify(*lex_stream);
	
    wchar_t* class_name = const_cast<wchar_t*>(unit_type -> Name());

    EmitGeneralization(gen_table, unit_type); // to be eliminated.
    cs_table -> AddClassSymbol(unit_type);
	
    unsigned i;

    if ((class_body -> NumClassVariables() + class_body -> NumInstanceVariables()) > 0)
    {
	unit_type -> instances = new SymbolSet();    	
	unit_type -> references = new SymbolSet();
    }
		
    //
    // Process static variables.
    //
    for (i = 0; i < class_body -> NumClassVariables(); i++)
    {
        AstFieldDeclaration* field_decl = class_body -> ClassVariable(i);

	 TypeSymbol *type = (field_decl -> type -> symbol -> IsArray()) 
	 					? field_decl -> type -> symbol -> base_type
	 					: field_decl -> type -> symbol;
  	 unit_type -> references -> AddElement(type);

        for (unsigned vi = 0;
             vi < field_decl -> NumVariableDeclarators(); vi++)
        {
		AstVariableDeclarator* vd = field_decl -> VariableDeclarator(vi);
	     	unit_type -> instances -> AddElement(vd -> symbol);
	     	field_decl -> PrintAssociation(assoc_table, package_name, class_name, *lex_stream);
            	//DeclareField(vd -> symbol);

		if (vd->variable_initializer_opt && vd->variable_initializer_opt->ExpressionCast())
		{
			AstExpression *rhs_expression = Utility::RemoveCasting(vd->variable_initializer_opt->ExpressionCast());
			if (rhs_expression->symbol->VariableCast())
			{
				if (!vd->symbol->aliases)
					vd->symbol->aliases = new SymbolSet();
				vd->symbol->aliases->AddElement(rhs_expression->symbol->VariableCast());
			}
		}
        }
    }

    //
    // Process instance variables.  We separate constant fields from others,
    // because in 1.4 or later, constant fields are initialized before the
    // call to super() in order to obey semantics of JLS 13.1.
    //
    Tuple<AstVariableDeclarator*> constant_instance_fields
        (unit_type -> NumVariableSymbols());
    for (i = 0; i < class_body -> NumInstanceVariables(); i++)
    {
        AstFieldDeclaration* field_decl  = class_body -> InstanceVariable(i);

	 TypeSymbol *type = (field_decl -> type -> symbol -> IsArray()) 
	 					? field_decl -> type -> symbol -> base_type
	 					: field_decl -> type -> symbol;
	 unit_type -> references -> AddElement(type);

        for (unsigned vi = 0;
             vi < field_decl -> NumVariableDeclarators(); vi++)
        {
            AstVariableDeclarator* vd = field_decl -> VariableDeclarator(vi);
	     field_decl -> PrintAssociation(assoc_table, package_name, class_name, *lex_stream);
            VariableSymbol* vsym = vd -> symbol;
	     unit_type -> instances -> AddElement(vsym);
            //DeclareField(vsym);
            if (vd -> variable_initializer_opt && vsym -> initial_value)
            {
                AstExpression* init;
                assert(init = vd -> variable_initializer_opt -> ExpressionCast());
                assert(init -> IsConstant() && vd -> symbol -> ACC_FINAL());
                constant_instance_fields.Next() = vd;

		  AstExpression *expr = (init -> kind == Ast::CAST) 
							? init -> CastExpressionCast() -> expression
							: init;
		  if (expr -> kind == Ast::CLASS_CREATION)
		  {
			if (!vsym -> concrete_types)
				vsym -> concrete_types = new SymbolSet(0);
			vsym -> concrete_types -> AddElement(expr -> ClassCreationExpressionCast() -> class_type -> symbol -> TypeCast());
		  }			
            }
            if (vd->variable_initializer_opt && vd->variable_initializer_opt->ExpressionCast())
	     {
			AstExpression *rhs_expression = Utility::RemoveCasting(vd->variable_initializer_opt->ExpressionCast());
			if (rhs_expression->symbol->VariableCast())
			{
				if (!vd->symbol->aliases)
					vd->symbol->aliases = new SymbolSet();
				vd->symbol->aliases->AddElement(rhs_expression->symbol->VariableCast());
			}
	     }			
        }
    }

    //
    // Process synthetic fields (this$0, local shadow parameters, $class...,
    // $array..., $noassert).
    //
    /*
    if (unit_type -> EnclosingType())
        DeclareField(unit_type -> EnclosingInstance());
    for (i = 0; i < unit_type -> NumConstructorParameters(); i++)
        DeclareField(unit_type -> ConstructorParameter(i));
    for (i = 0; i < unit_type -> NumClassLiterals(); i++)
        DeclareField(unit_type -> ClassLiteral(i));
    VariableSymbol* assert_variable = unit_type -> AssertVariable();
    if (assert_variable)
    {
        assert(! control.option.noassert);
        DeclareField(assert_variable);
        if (control.option.target < JikesOption::SDK1_4)
        {
            semantic.ReportSemError(SemanticError::ASSERT_UNSUPPORTED_IN_TARGET,
                                    unit_type -> declaration,
                                    unit_type -> ContainingPackageName(),
                                    unit_type -> ExternalName());
            assert_variable = NULL;
        }
    }
    */
    //
    // Process declared methods.
    //
    for (i = 0; i < class_body -> NumMethods(); i++)
    {
        AstMethodDeclaration* method = class_body -> Method(i);
        if (method -> method_symbol)
        {
            counter3++;
	     wchar_t* method_name = const_cast<wchar_t*>((*lex_stream).NameString(method -> method_declarator -> identifier_token));

            //int method_index = methods.NextIndex(); // index for method
            //BeginMethod(method_index, method -> method_symbol);
            if (method -> method_body_opt) // not an abstract method ?
            {
		  mb_table -> addMethodBodyAddr(package_name, class_name, method_name, method);  // to be eliminated.
		  method -> PrintAssociation(assoc_table, package_name, class_name, *lex_stream);
		  ms_table -> AddMethodSymbol(method -> method_symbol);

            	  assert(method -> method_body_opt -> NumStatements() > 0);
		  EmitBlockAssociation(unit_type, method -> method_symbol, method -> method_body_opt, d_table, w_table, r_table);

		  counter2++;
                //EmitBlockStatement(method -> method_body_opt);
            }
	     else
		 	counter1++;
            //EndMethod(method_index, method -> method_symbol);
        }
    }

    //
    // Process synthetic methods (access$..., class$).
    //
    /*
    for (i = 0; i < unit_type -> NumPrivateAccessMethods(); i++)
    {
        int method_index = methods.NextIndex(); // index for method
        MethodSymbol* method_sym = unit_type -> PrivateAccessMethod(i);
        AstMethodDeclaration* method = method_sym -> declaration ->
            MethodDeclarationCast();
        assert(method);
        BeginMethod(method_index, method_sym);
        EmitBlockStatement(method -> method_body_opt);
        EndMethod(method_index, method_sym);
    }
    MethodSymbol* class_literal_sym = unit_type -> ClassLiteralMethod();
    if (class_literal_sym)
    {
        int method_index = methods.NextIndex(); // index for method
        BeginMethod(method_index, class_literal_sym);
        GenerateClassAccessMethod();
        EndMethod(method_index, class_literal_sym);
    }
    */
    //
    // Process the instance initializer.
    //
    /*
    bool has_instance_initializer = false;
    if (unit_type -> instance_initializer_method)
    {
        AstMethodDeclaration* declaration = (AstMethodDeclaration*)
            unit_type -> instance_initializer_method -> declaration;
        AstBlock* init_block = declaration -> method_body_opt;
        if (! IsNop(init_block))
        {
            int method_index = methods.NextIndex(); // index for method
            BeginMethod(method_index,
                        unit_type -> instance_initializer_method);
            bool abrupt = EmitBlockStatement(init_block);
            if (! abrupt)
                PutOp(OP_RETURN);
            EndMethod(method_index, unit_type -> instance_initializer_method);
            has_instance_initializer = true;
        }
    }
    */
    //
    // Process all constructors (including synthetic ones).
    //

    if (!class_body -> default_constructor)
    {
    	for (i = 0; i < class_body -> NumConstructors(); i++)
    	{
   		//AstConstructorDeclaration *constructor = dynamic_cast<AstConstructorDeclaration*>(class_body -> Constructor(i) -> Clone(ast_pool, *lex_stream));
		AstConstructorDeclaration *constructor = class_body -> Constructor(i);
		mb_table -> addMethodBodyAddr(package_name, class_name, class_name, constructor);  // to be eliminated.
	   	ms_table -> AddMethodSymbol(constructor -> constructor_symbol);
		EmitBlockAssociation(unit_type, constructor -> constructor_symbol, constructor -> constructor_body, d_table, w_table, r_table);

          	// CompileConstructor(class_body -> Constructor(i), constant_instance_fields, has_instance_initializer);
    	}
    }
    /*
    for (i = 0; i < unit_type -> NumPrivateAccessConstructors(); i++)
    {
    
		Coutput << "private access class ctor: " << class_name << endl; 
	
          MethodSymbol* constructor_sym = unit_type -> PrivateAccessConstructor(i);
          AstConstructorDeclaration* constructor = constructor_sym -> declaration -> ConstructorDeclarationCast();

 	   mb_table -> addMethodBodyAddr(package_name, class_name, class_name, constructor);
	   constructor-> PrintAssociation(assoc_table, package_name, class_name, *lex_stream);
	   ms_table -> AddMethodSymbol(const_cast<char*>(constructor_sym -> Utf8Name()), constructor_sym);

         // CompileConstructor(constructor, constant_instance_fields, has_instance_initializer);
    }
    */
    //
    // Process the static initializer.
    //
    /*
    if (unit_type -> static_initializer_method)
    {
        AstMethodDeclaration* declaration = (AstMethodDeclaration*)
            unit_type -> static_initializer_method -> declaration;
        AstBlock* init_block = declaration -> method_body_opt;
        if (assert_variable || ! IsNop(init_block))
        {
            int method_index = methods.NextIndex(); // index for method
            BeginMethod(method_index, unit_type -> static_initializer_method);
            if (assert_variable)
                GenerateAssertVariableInitializer(unit_type -> outermost_type,
                                                  assert_variable);
            bool abrupt = EmitBlockStatement(init_block);
            if (! abrupt)
                PutOp(OP_RETURN);
            EndMethod(method_index, unit_type -> static_initializer_method);
        }
    }
    */
}

#ifdef DONTDOIT
void PrintRelation(MethodBodyTable* mb_table, GenTable* gen_table, AssocTable* assoc_table, TypeSymbol* unit_type, StoragePool* ast_pool)
{
	Semantic& semantic = *unit_type -> semantic_environment -> sem;
    	LexStream *lex_stream = semantic.lex_stream;

    	AstCompilationUnit* compilation_unit = semantic.compilation_unit;

     	unsigned i;
     	for (i = 0; i < compilation_unit -> NumTypeDeclarations(); i++)
     	{
		wchar_t* package_name = (compilation_unit -> package_declaration_opt)
			? const_cast<wchar_t*>((*lex_stream).NameString(compilation_unit -> package_declaration_opt -> name -> identifier_token))
			: NULL;

		if (compilation_unit -> TypeDeclaration(i) -> kind == Ast::CLASS)
         	{
             		AstClassDeclaration* class_declaration = dynamic_cast<AstClassDeclaration*> (compilation_unit -> TypeDeclaration(i));
             		class_declaration -> PrintGeneralization(gen_table, package_name, *lex_stream);

             		AstClassBody* class_body = class_declaration -> class_body;
             		unsigned j;
             		for (j = 0; j < class_body -> NumClassBodyDeclarations(); j++)
             		{
                 		switch(class_body -> ClassBodyDeclaration(j) -> kind)
                 		{
                     		case Ast::FIELD:
                             		class_body -> ClassBodyDeclaration(j) -> PrintAssociation(assoc_table, 
																		  package_name,
																		  const_cast<wchar_t*>((*lex_stream).NameString(class_body -> identifier_token)), 
																		  *lex_stream);
						break;
                     		case Ast::CONSTRUCTOR:
						{
						wchar_t* class_name = const_cast<wchar_t*>((*lex_stream).NameString(class_body -> identifier_token));

  	                                   AstConstructorDeclaration* ctor_declaration = dynamic_cast<AstConstructorDeclaration*> (class_body -> ClassBodyDeclaration(j));
						wchar_t* ctor_name = const_cast<wchar_t*>((*lex_stream).NameString(ctor_declaration -> constructor_declarator -> identifier_token));

						if (!ctor_declaration -> GoFTag)
						{
							AstConstructorDeclaration* cloned_declaration = dynamic_cast<AstConstructorDeclaration*>(ctor_declaration -> Clone(ast_pool, *lex_stream));
							mb_table -> addMethodBodyAddr(package_name, class_name, ctor_name, cloned_declaration);
							ctor_declaration -> GoFTag = true;
                     			}
                     			}
						break;
                     		case Ast::METHOD:
                         			{
						wchar_t* _className = const_cast<wchar_t*>((*lex_stream).NameString(class_body -> identifier_token));

  	                                   AstMethodDeclaration* method_declaration = DYNAMIC_CAST<AstMethodDeclaration*> (class_body -> ClassBodyDeclaration(j));
						wchar_t* _methodName = const_cast<wchar_t*>((*lex_stream).NameString(method_declaration -> method_declarator -> identifier_token));

/*
						if ((wcscmp(_className, L"MediaTracker") == 0)
						&& (wcscmp(_methodName, L"addImage") == 0))
							method_declaration -> method_body_opt -> Statement(0) -> Print(*lex_stream);
*/
						if (!method_declaration -> method_body_opt)
							counter1++;
						else if (method_declaration -> method_body_opt -> NumStatements() > 0)
							counter2++;

						else if ((method_declaration -> method_body_opt)
						&& (method_declaration -> method_body_opt -> NumStatements() == 0))
							Coutput << _className << "." << _methodName << endl;

						counter3++;

						AstMethodDeclaration* cloned_declaration = DYNAMIC_CAST<AstMethodDeclaration*>(method_declaration -> Clone(ast_pool, *lex_stream));
						mb_table -> addMethodBodyAddr(package_name, _className, _methodName, cloned_declaration);			
						
                              		class_body -> ClassBodyDeclaration(j) -> PrintAssociation(assoc_table, 
																		  package_name,
																		  const_cast<wchar_t*>((*lex_stream).NameString(class_body -> identifier_token)), 
																		  *lex_stream);
						method_declaration -> GoFTag = true;
                         			}
                         			break;
					case Ast::CLASS:
					case Ast::INTERFACE:
						{
							class_body -> ClassBodyDeclaration(j) -> PrintGeneralization(gen_table, package_name, *lex_stream);
						}
						break;
                     		default:
                         			break;
                 		} // switch
             		} // for
             		class_declaration -> GoFTag = true;
         	}
		else if (compilation_unit -> TypeDeclaration(i) -> kind == Ast::INTERFACE)
		{
			AstInterfaceDeclaration* interface_declaration = dynamic_cast<AstInterfaceDeclaration*> (compilation_unit -> TypeDeclaration(i));
			interface_declaration -> PrintGeneralization(gen_table, package_name, *lex_stream);
			interface_declaration -> GoFTag = true;
		}
		else
		{
			//Coutput << L"kind = " << compilation_unit -> TypeDeclaration(i) -> kind << endl;
		}
     	}
}
#endif

bool WriteAccessTable::IsWrittenBy(VariableSymbol *vsym, MethodSymbol *msym)
{
	multimap<VariableSymbol*, MethodSymbol*>::iterator p;
	for (p = table -> begin(); p != table -> end(); p++)
	{
		if ((p -> first == vsym) && (p -> second == msym))
			return true;
	}
	return false;
}

bool WriteAccessTable::IsWrittenBy(VariableSymbol *vsym, MethodSymbol *msym, DelegationTable *d_table)
{
	multimap<VariableSymbol*, MethodSymbol*>::iterator p;
	for (p = table -> begin(); p != table -> end(); p++)
	{
		if (p -> first == vsym)
		{ 
			if ((p -> second == msym) || (d_table -> TraceCall(p -> second, msym)))
				return true;				
		}		
	}
	return false;
}

int DelegationTable::IsBidirectional(TypeSymbol *t1,TypeSymbol *t2)
{
	//
	// return code: 
	//
	// 		3: bidirectional
	// 		2: -->
	// 		1: <--
	// 		0: no delegation
	//

	int forward = 0, backward = 0;
	for (unsigned i =0; i < table -> size(); i++)
	{
		DelegationEntry *entry = (*table)[i];
		forward =  ((forward == 0) && (entry->from == t1) && (entry -> to == t2)) ? 1 : forward;
		backward =  ((backward == 0) && (entry->from == t2) && (entry -> to == t1)) ?  1 : backward;
	}
	return (2*forward + backward);	
}

bool DelegationTable::TraceCall(MethodSymbol *start, MethodSymbol *target)
{
	for (unsigned i =0; i < table -> size(); i++)
	{
		DelegationEntry *entry = (*table)[i];
		if (entry -> method == start)
		{
			if ((entry -> enclosing == target) 
			||(entry -> enclosing -> Overrides(target))
			|| TraceCall(entry -> enclosing, target))
				return true;
		}
	}
	return false;
}

int DelegationTable::UniqueDirectedCalls ()
{
	multimap<TypeSymbol*, TypeSymbol*> stack;

	for (unsigned i = 0; i < table -> size(); i++)
	{
		DelegationEntry *entry = (*table)[i];

		if (entry -> to -> file_symbol
		&& (!entry -> to -> file_symbol -> IsClassOnly())
		&& (entry -> from != entry -> to)
		&& ((!entry -> from -> IsSubclass(entry -> to)) && (!entry -> to -> IsSubclass(entry -> from))))
		{
			if (stack.size() > 0)
			{
				multimap<TypeSymbol*, TypeSymbol*>::iterator p = stack.begin();
				while (((p->first != entry -> from) || (p->second != entry -> to))					
					&& (p != stack.end()))
					p++;
				if (p == stack.end())
					stack.insert(p, pair<TypeSymbol*, TypeSymbol*>(entry->from, entry->to));
			}
			else
				stack.insert(pair<TypeSymbol*, TypeSymbol*>(entry->from, entry->to));
		}
	}

	return stack.size();
}

bool DelegationTable::DelegatesSuccessors(TypeSymbol *from, TypeSymbol *to)
{
	assert (!from -> ACC_INTERFACE() && to -> ACC_INTERFACE());

	for (unsigned i = 0; i < table -> size(); i++)
	{
		DelegationEntry *entry = (*table)[i];
		if ((entry -> from == from)  && (entry -> to != to) && (entry -> to -> Implements(to)))
		{
			TypeSymbol *resolve = (entry -> base_opt) ? ResolveType(entry -> base_opt) : NULL;
			if (resolve && (to != resolve))
			{
#ifdef GOF_CONSOLE			
					Coutput << "From: " << from -> Utf8Name() 
						<< " To: " << to -> Utf8Name() 
						<< " RESOLVE: " << resolve -> Utf8Name() 
						<< " (" << entry -> method -> Utf8Name() << ")" 
						<< endl;
#endif			
					return true;
			}
		}
	}
	return false;
}

TypeSymbol *DelegationTable::ResolveType(AstExpression *expression)
{
	assert (expression);
	
	switch(expression -> kind)
	{
		case Ast::NAME:
			if (expression -> NameCast() -> resolution_opt)
				return ResolveType(expression -> NameCast() -> resolution_opt);
			else if (expression -> symbol -> Kind() == Symbol::TYPE)
				return expression -> symbol -> TypeCast();
			else if (expression -> symbol -> Kind() == Symbol::VARIABLE)
			{
				VariableSymbol *vsym = expression -> symbol -> VariableCast();
				VariableSymbol *svsym = vsym -> ContainingType() -> Shadows(vsym);
				if (svsym)
					return svsym -> Type();
				else
				{
//					Coutput << "\"" << vsym -> FileLoc() << "\"" << endl;
					if (vsym -> IsLocal() && (vsym -> declarator -> variable_initializer_opt))
						return ResolveType(vsym -> declarator -> variable_initializer_opt -> ExpressionCast());
					return vsym -> Type();
				}
			}
		case Ast::CALL:
			{
			AstMethodInvocation* method_call = expression -> MethodInvocationCast();
			if (method_call -> resolution_opt)
				method_call =  method_call -> resolution_opt -> MethodInvocationCast();
			return  ((MethodSymbol*) method_call -> symbol) -> Type();
			}
		case Ast::CAST:
			return ResolveType(expression -> CastExpressionCast() -> expression);			
		case Ast::PARENTHESIZED_EXPRESSION:
			return ResolveType(expression -> ParenthesizedExpressionCast() -> expression);
		default:
			return NULL;
	}
}

MethodSymbol *DelegationTable::Delegates(TypeSymbol *from, TypeSymbol *to)
{
	assert(from -> ACC_INTERFACE() && (! to -> ACC_INTERFACE()));
	
	unsigned i = 0;	
	while (i < table -> size())
	{
		DelegationEntry *entry = (*table)[i];
		if (((entry -> from == from) || entry -> from -> Implements(from))
		&& ((entry -> to == to) || entry -> to -> IsSubclass(to)))
		{
#ifdef GOF_CONSOLE	
			Coutput << entry -> from -> Utf8Name() 
				<< " delegates " 
				<< entry -> to -> Utf8Name() 
				<< "::"
				<< entry -> method -> Utf8Name()
				<< endl;
#endif
			return entry -> method;
		}
		i++;
	}
	return NULL;
}

void DelegationTable::InsertDelegation(TypeSymbol *from, TypeSymbol *to, AstExpression *base_opt, VariableSymbol *vsym, MethodSymbol *method, MethodSymbol *enclosing, AstMethodInvocation *call)
{
	unsigned i = 0;
	while ((i < table -> size())
		&& (((*table)[i] -> from != from) 
		   ||((*table)[i] -> to != to) 
		   ||((*table)[i] -> base_opt != base_opt)
		   ||((*table)[i] -> vsym != vsym)
		   ||((*table)[i] -> method != method) 
		   ||((*table)[i] -> enclosing != enclosing)
		   ||((*table)[i] -> call != call))) i++;	

	if (i == table -> size())
		table -> push_back(new DelegationEntry(from, to, base_opt, vsym, method, enclosing, call));
}

void DelegationTable::ShowDelegations(TypeSymbol *from, TypeSymbol *to)
{
	if (from -> ACC_INTERFACE() && to -> ACC_INTERFACE())
	{
		for (unsigned i = 0; i < table -> size(); i++)
		{
			DelegationEntry *entry = (*table)[i];
			bool flag1, flag2;

			flag1 = (entry -> from -> ACC_INTERFACE() && entry -> from -> IsSubinterface(from)) 
				? true 
				: (!entry -> from -> ACC_INTERFACE() && entry -> from -> Implements(from))
				? true
				: false;
				
			flag2 = (entry -> to -> ACC_INTERFACE() && entry -> to -> IsSubinterface(to)) 
				? true 
				: (!entry -> to -> ACC_INTERFACE() && entry -> to -> Implements(to))
				? true
				: false;

			if (flag1 && flag2)
			{
				Coutput << entry -> from -> Utf8Name() 
					<< " --> " 
					<< entry -> to -> Utf8Name()
					<< " (";

				if (entry -> base_opt == NULL)
					Coutput << "";
				else if (entry -> base_opt -> kind == Ast::THIS_CALL)
					Coutput << "this";
				else if ((entry -> base_opt -> kind == Ast::CAST) && (entry -> base_opt -> CastExpressionCast() -> expression -> kind == Ast::NAME))
					Coutput << entry -> base_opt -> CastExpressionCast() -> expression -> symbol -> VariableCast() -> Utf8Name();							
				else if ((entry -> base_opt -> kind == Ast::NAME) && (entry -> base_opt -> symbol -> Kind() == Symbol::VARIABLE))
					Coutput << entry -> base_opt -> symbol -> VariableCast() -> Utf8Name();
				else if ((entry -> base_opt -> kind == Ast::NAME) && (entry -> base_opt -> symbol -> Kind() == Symbol::TYPE))
					Coutput << entry -> base_opt -> symbol -> TypeCast() -> Utf8Name();
				else
					Coutput << "unknown";

				Coutput << "."
					<< entry -> method -> Utf8Name()
					<< ")"
					<< endl;
			}
		}
	}
	else if (!from -> ACC_INTERFACE() && to -> ACC_INTERFACE())
	{
		for (unsigned i = 0; i < table -> size(); i++)
		{
			DelegationEntry *entry = (*table)[i];
			bool flag1, flag2;

			flag1 = (!entry -> from -> ACC_INTERFACE() && entry -> from -> IsSubclass(from)) 
				? true 
				: false;
				
			flag2 = (entry -> to -> ACC_INTERFACE() && entry -> to -> IsSubinterface(to)) 
				? true 
				: (!entry -> to -> ACC_INTERFACE() && entry -> to -> Implements(to))
				? true
				: false;



			if (flag1 && flag2)
			{
				Coutput << entry -> from -> Utf8Name() 
					<< " --> " 
					<< entry -> to -> Utf8Name()
					<< " (";

				if (entry -> base_opt)
					Coutput << ResolveType(entry -> base_opt) -> Utf8Name();	
/*
				if (entry -> base_opt == NULL)
					Coutput << "";
				else if (entry -> base_opt -> kind == Ast::THIS_CALL)
					Coutput << "this";
				else if (entry -> base_opt -> kind == Ast::PARENTHESIZED_EXPRESSION)
					Coutput << "(...)";
				else if ((entry -> base_opt -> kind == Ast::CAST) && (entry -> base_opt -> CastExpressionCast() -> expression -> kind == Ast::NAME))
					Coutput << entry -> base_opt -> CastExpressionCast() -> expression -> symbol -> VariableCast() -> Utf8Name();							
				else if ((entry -> base_opt -> kind == Ast::NAME) && (entry -> base_opt -> symbol -> Kind() == Symbol::VARIABLE))
					Coutput << entry -> base_opt -> symbol -> VariableCast() -> Utf8Name();
				else if ((entry -> base_opt -> kind == Ast::NAME) && (entry -> base_opt -> symbol -> Kind() == Symbol::TYPE))
					Coutput << entry -> base_opt -> symbol -> TypeCast() -> Utf8Name();
				else
					Coutput << "unknown";
*/
				Coutput << "."
					<< entry -> method -> Utf8Name()
					<< ")"
					<< endl;
			}
		}
	}
}
void DelegationTable::ConcretizeDelegations()
{
	unsigned i;
	for (i = 0; i<table->size(); i++)
	{
		DelegationEntry *entry = (*table)[i];
		if (entry-> from && entry->vsym && entry->vsym->concrete_types)
		{
			Symbol *sym = entry -> vsym -> concrete_types -> FirstElement();
			while (sym)
			{
				if (!sym -> TypeCast() -> call_dependents)
					sym -> TypeCast() -> call_dependents = new SymbolSet(0);
				sym -> TypeCast() -> call_dependents -> AddElement(entry -> from);
				sym = entry -> vsym -> concrete_types -> NextElement();
			}
		}
	}
}
void DelegationTable::DumpTable()
{
	Coutput << "delegationTable::DumpTable" << endl;
	unsigned i;
	for (i = 0; i < table -> size(); i++)
	{
		DelegationEntry *entry = (*table)[i];
		Coutput << entry -> from -> Utf8Name() 
			<< " --> "
			<< entry -> to -> Utf8Name()
			<< " ("
			<< entry -> method -> Utf8Name()
			<< ") at "
			<< entry -> enclosing -> Utf8Name()
			<< endl
			<< endl;
	}
}

int ClassSymbolTable::ConcreteClasses()
{
	int ct = 0;

	for (unsigned c = 0; c < table->size(); c++)
	{
		if ((!(*table)[c] -> ACC_INTERFACE())
		&& (!(*table)[c] -> ACC_ABSTRACT())
		&& (!(*table)[c] -> ACC_SYNTHETIC())
		&& (!(*table)[c] ->IsInner()))
			ct++;
	}
	return ct;
}

bool ClassSymbolTable::Converge(TypeSymbol* super1, TypeSymbol* super2)
{
	bool flag1, flag2;
	flag1 = flag2 = false;

	for (unsigned c = 0; (!flag1 ||!flag2) && (c < table->size()); c++)
	{
		TypeSymbol *type = (*table)[c];
		if (type -> ACC_INTERFACE())
		{
			if (super1 -> ACC_INTERFACE())
				flag1 = type -> IsSubinterface(super1);
			else
				flag1 = false;

			if (super2 -> ACC_INTERFACE())
				flag2 = type -> IsSubinterface(super2);
			else
				flag2 = false;
		}
		else
		{
			if (super1 -> ACC_INTERFACE())
				flag1 = type -> Implements(super1);
			else
				flag1 = type -> IsSubclass(super1);

		
			if (super2 -> ACC_INTERFACE())
				flag2 = type -> Implements(super2);
			else
				flag2 = type -> IsSubclass(super2);
		}
	}

#ifdef GOF_CONSOLE	
	if (flag1 && flag2)
		Coutput << (--p) -> first << " converges " << super1->Utf8Name() << " and " << super2->Utf8Name() << endl;
#endif

	return (flag1 && flag2);
}

void ClassSymbolTable::AddClassSymbol(TypeSymbol *sym)
{
	table->push_back(sym);	
}

TypeSymbol *ClassSymbolTable::GetSymbol(wchar_t * cls)
{
	for (unsigned c = 0; c < table->size(); c++)
	{
		TypeSymbol *sym = (*table)[c];
		if (wcscmp(cls, sym-> Name()) == 0)
			return sym;			
	}
	return NULL;
}

bool ClassSymbolTable::IsFamily(TypeSymbol* t1, TypeSymbol *t2)
{
	if ((t1 -> ACC_INTERFACE() ||t1 -> ACC_ABSTRACT() || t1 -> ACC_SYNTHETIC())
	|| (t2 -> ACC_INTERFACE() ||t2 -> ACC_ABSTRACT() || t2 -> ACC_SYNTHETIC()))
		return false;

	for (unsigned c = 0; c < table->size(); c++)
	{
		TypeSymbol *type = (*table)[c];
		if (type->ACC_SYNTHETIC())
		{
			if (type -> ACC_INTERFACE())
				return (t1->Implements(type) && t2->Implements(type));
			else
				return (t1->IsSubclass(type) && t2->IsSubclass(type));
			
		}		
	}
	return false;
}

bool ClassSymbolTable::HasSubclasses(TypeSymbol *super)
{
	if (!super -> ACC_INTERFACE())
	{
		for (unsigned c = 0; c < table->size(); c++)
		{
			TypeSymbol *type = (*table)[c];
			if (type -> IsSubclass(super))
				return true;
		}
		return false;
	}
	return false;
}

vector<TypeSymbol*> *ClassSymbolTable::GetAncestors(TypeSymbol *type)
{
// Error: includes Object, which every class subclasses
	vector<TypeSymbol*> *a = new vector<TypeSymbol*>();

       for (; type; type = type -> super)
		a -> push_back(type);
	return a;
}

void ClassSymbolTable::PrintSubclasses(TypeSymbol* super)
{
	assert (! super -> ACC_INTERFACE());
	
	for (unsigned c = 0; c < table->size(); c++)
	{
		if ((!(*table)[c]->ACC_INTERFACE())
		&& ((*table)[c] -> IsSubclass(super)))
			Coutput << (*table)[c]->Utf8Name() << " ";
	}
	Coutput << endl;
}

void ClassSymbolTable::PrintSubinterfaces(TypeSymbol* inter)
{
	assert (inter -> ACC_INTERFACE());
	
	for (unsigned c = 0; c < table->size(); c++)
	{
		if (((*table)[c]->ACC_INTERFACE())
		&& ((*table)[c]->IsSubinterface(inter)))
			Coutput << (*table)[c]->Utf8Name() << " ";
	}
	Coutput << endl;
}

void ClassSymbolTable::PrintSubtypes(TypeSymbol *inter)
{
	assert (inter -> ACC_INTERFACE());
	
	for (unsigned c = 0; c < table->size(); c++)
	{
		if ((!(*table)[c]->ACC_INTERFACE()) && ((*table)[c]->Implements(inter)))
		{
			Coutput << (*table)[c]->Utf8Name();
			if ((*table)[c]->IsInner())
				Coutput << "(inner) ";
			else if ((*table)[c]->ACC_PRIVATE())
				Coutput << "(private) ";				
			else
				Coutput  << " ";
		}
	}
	Coutput << endl;
}

void ClassSymbolTable::ExpandSubtypes()
{
	unsigned p, q;
	for (p = 0; p < table->size(); p++)
	{
	/*
		Coutput << p->second->fully_qualified_name->value  << ": " << endl;
		if (p->second->subtypes_closure && p->second->subtypes_closure->Size())
		{
			Coutput << "subtypes_closure: ";
			p->second->subtypes_closure->Print();
			
		}
		if (p->second->subtypes && p->second->subtypes->Size())
		{
			Coutput << "subtypes: ";
			p->second->subtypes->Print();
			
		}
		Coutput << endl;
	*/
		if ((*table)[p]->subtypes)
		{
			for (q = 0; q < table->size(); q++)
			{
				if ((*table)[p] != (*table)[q]
				&& !(*table)[p]->subtypes->IsElement((*table)[q])
				&& (*table)[q]->IsSubtype((*table)[p])
				)
					(*table)[p]->subtypes->AddElement((*table)[q]);			
			}
		}
	}	
}

void MethodSymbolTable::AddMethodSymbol(MethodSymbol *sym)
{
	table -> push_back(sym);
}

MethodSymbol *MethodSymbolTable::GetSymbol(char *cls, char *mtd, char *fname)
{
	for (unsigned i=0; i<table->size(); i++)
	{
              MethodSymbol *method_symbol = (*table)[i];
		TypeSymbol *unit_type = method_symbol -> containing_type;

		if ((strcmp(method_symbol->Utf8Name(), mtd) == 0)
		&& (strcmp(unit_type -> Utf8Name(), cls) == 0)
		&& (strcmp(unit_type -> file_symbol -> FileName(), fname) == 0))
			return method_symbol;			
	}
	return NULL;
}

Ast *MethodSymbolTable::GetAstDeclaration(wchar_t *pkg, wchar_t *cls, wchar_t *mtd)
{
	for (unsigned i=0; i<table->size(); i++)
	{
              MethodSymbol *method_symbol = (*table)[i];
		TypeSymbol *unit_type = method_symbol -> containing_type;

		if ((wcscmp(method_symbol -> Name(), mtd) == 0)
		&& (wcscmp(unit_type -> Name(), cls) == 0)
		&& (wcscmp(unit_type -> FileLoc(), pkg) == 0))
			return method_symbol -> declaration;			
	}
	return NULL;
}

void MethodSymbolTable::PrintDeclaration(char *cls, char *mtd, char *fname)
{
	for (unsigned i=0; i<table->size(); i++)
	{
		TypeSymbol *unit_type = (*table)[i]->containing_type;
		if ((strcmp((*table)[i]->Utf8Name(), mtd) == 0)
		&& (strcmp(unit_type -> Utf8Name(), cls) == 0)
		&& (strcmp(unit_type -> file_symbol -> FileName(), fname) == 0))
		{
	       	MethodSymbol *method_symbol = (*table)[i];
			TypeSymbol *type = method_symbol -> containing_type;
			Coutput << "file name: " << type -> file_symbol -> FileName() << endl;
			Coutput << "class name: " << type -> Utf8Name() << endl;		
			method_symbol -> declaration -> Print();
			Coutput << mtd << ": " << dynamic_cast<AstMethodDeclaration*>(method_symbol -> declaration) -> method_body_opt -> NumStatements() << endl;
		}
	}		
}

void MethodSymbolTable::PrintBody(char *cls, char *mtd, char *fname)
{
	unsigned i=0;
	for (; i<table->size(); i++)
	{
		TypeSymbol *unit_type = (*table)[i]->containing_type;
		if ((strcmp((*table)[i]->Utf8Name(), mtd) == 0)
		&& (strcmp(unit_type -> Utf8Name(), cls) == 0)
		&& (strcmp(unit_type -> file_symbol -> FileName(), fname) == 0))
			break;
	}

	if (i<table->size())
	{
	       MethodSymbol *method_symbol = (*table)[i];
		dynamic_cast<AstMethodDeclaration*>(method_symbol -> declaration) -> method_body_opt -> Print();
	}		
}

void MethodSymbolTable::ExpandCallDependents()
{
	// does NOT expand typesymbol->call_dependents
	
	for (unsigned i=0; i<table->size(); i++)
	{
		MethodSymbol *method = (*table)[i];
		MethodSymbol *overridden = method->GetVirtual(); // TODO: consider mult-inheritance
		if (overridden)
		{
			if (overridden->invokers)
			{
				if (!method->invokers)
					method->invokers = new SymbolSet(0);
				method->invokers->Union(*overridden->invokers);

				Symbol *sym = overridden->invokers->FirstElement();
				while(sym)
				{
					MethodSymbol *msym = sym->MethodCast();
					msym->invokees->AddElement(method);
					sym = overridden->invokers->NextElement();
				}				
			}
			
			if (overridden ->callers)
			{
				if (!method->callers)
					method->callers = new SymbolSet(0);
				method->callers->Union(*overridden->callers);
			}
		}
	}	
}

void MethodSymbolTable::ClearMarks()
{
	for (unsigned i=0; i<table->size(); i++)
	{
		if ((*table)[i]->mark != 'W')
			(*table)[i]->mark = 'W';
	}	
}

Env::State EnvTable::getState(wchar_t* var)
{
	unsigned i = 0;
	while ((i < table -> size()) 
	&& (wcscmp((*table)[i] -> var, var) != 0))
		i++;
	return (*table)[i] -> state;
}

void EnvTable::changeState(wchar_t* var, Env::State state)
{
	unsigned i = 0;
	while ((i < table -> size()) 
	&& (wcscmp((*table)[i] -> var, var) != 0))
		i++;

	if (i < table -> size())
		(*table)[i] -> state = state;
}

AstDeclared* MethodBodyTable::getAstLocation(wchar_t* cls, wchar_t* mtd)
{
	MethodBodyAddr*  entry = NULL;
	bool flag = false;
	unsigned i = 0;
	while (!flag && (i < table -> size()))
	{
		entry = (*table)[i];

		if ((wcscmp(cls, entry -> class_name) == 0)
		&& (wcscmp(mtd, entry -> method_name) == 0))
			flag = true;
		else
			i++;
	}
	return (flag) ? entry -> ast_location : NULL;
}

vector<wchar_t*>* MethodBodyTable::getModifiersAt(int i)
{
	AstDeclared *decl = (*table)[i] -> ast_location;

       vector<wchar_t*>* modifiers = NULL;
	if (decl -> modifiers_opt)
	{
		modifiers = new vector<wchar_t*>();
		unsigned i;
		for (i = 0; i < decl -> modifiers_opt -> NumModifiers(); i++)
		{
			if (decl -> modifiers_opt -> Modifier(i) -> kind == Ast::MODIFIER_KEYWORD)
				modifiers -> push_back(dynamic_cast<AstModifierKeyword*>(decl -> modifiers_opt -> Modifier(i)) -> modifier_token_string);
			else
				decl -> Print();
		}
	}

	return modifiers;
}

vector<wchar_t*>* GenTable::getAncestors(GenTable::Kind kind, wchar_t* cls, wchar_t* pkg)
{
	bool flag = false;
	unsigned i = 0;
	while (!flag && (i < table -> size()))
	{
		Gen* entry = (*table)[i];
		if (wcscmp((entry -> class_name), cls) == 0)
		//*&& (wcscmp((entry -> package_name), pkg) == 0))
			flag = true;
		else
			i++;			
	}

	Gen* entry = (*table)[i];

	if (flag && (kind == GenTable::SUBC) && (entry -> kind != Gen::INTERFACE))
	{
		wchar_t* spr = entry -> super_name;
		vector<wchar_t*>* ancestors = new vector<wchar_t*>();
		ancestors -> push_back(cls);

		while (spr)
		{
			ancestors -> push_back(spr);
			spr = getSuper(spr, pkg);
		}
		return ancestors;
	}
	else if (flag && (kind == GenTable::IMPL))
	{
		vector<wchar_t*>* ancestors = new vector<wchar_t*>();
		if ((entry -> kind != Gen::INTERFACE) && (entry -> interfaces))
		{
			unsigned j;
			for (j = 0; j < entry -> interfaces -> size(); j++)			
				ancestors -> push_back((*entry -> interfaces)[j]);
		}

		if (entry -> kind == Gen::INTERFACE)
			ancestors -> push_back(entry -> class_name);
		return ancestors;			
	}
	return NULL;
}

wchar_t* GenTable::getSuper(wchar_t* cls, wchar_t* pkg)
{	
	unsigned i = 0;
	Gen*entry = NULL;
	while (!entry && (i < table -> size()))
	{		
		if (wcscmp(((*table)[i] -> class_name), cls) == 0)
		{
			if ((((*table)[i] -> package_name == NULL) && (pkg == NULL))
			|| (((*table)[i] -> package_name != NULL) && (pkg != NULL) && (wcscmp((*table)[i] -> package_name, pkg) == 0)))
				entry = (*table)[i];
			else
				i++;
		}
		else
			i++;			
	}
	return (entry)?entry -> super_name:NULL;
}

vector<wchar_t*>* GenTable::getSuccessors(wchar_t* super, GenTable::Kind kind)
{
	vector<wchar_t*>* list = NULL;
	unsigned i;
	if (kind == GenTable::IMPL)
	{		
		for  (i = 0; i < table -> size(); i++)
		{
			Gen* entry = (*table)[i];
			unsigned j;
			if (entry -> interfaces)
			{
				for (j = 0; j < entry -> interfaces -> size(); j++)
				{
					if (wcscmp(super, (*entry -> interfaces)[j]) == 0)
					{
						if (list == NULL)
							list = new vector<wchar_t*>();
						list -> push_back(entry -> class_name);
					}
				}
			}
		}
	}
	else
	{
		for  (i = 0; i < table -> size(); i++)
		{
			Gen* entry = (*table)[i];
			//unsigned j;
			if (entry -> super_name)
			{
				if (wcscmp(super, entry -> super_name) == 0)
				{
						if (list == NULL)
							list = new vector<wchar_t*>();
						list -> push_back(entry -> class_name);
				}
			}
		}	
	}
	return list;
}

Gen::Kind GenTable::getKind(wchar_t* cls, wchar_t* pkg)
{
	Gen* entry = NULL;
	bool flag = false;
	unsigned i = 0;
	while (!flag && (i < table -> size()))
	{
		entry = (*table)[i];

		if (wcscmp(entry -> class_name, cls) == 0)
		{
			if (((entry -> package_name == NULL) && (pkg == NULL))
			|| (((entry -> package_name != NULL) && (pkg != NULL)) && (wcscmp(entry -> package_name, pkg) == 0)))
				flag = true;
			else
				i++;
		}
		else
			i++;
	}
	assert(flag);
	return entry ->kind;
}

char* GenTable::getFileName(wchar_t* cls, wchar_t* pkg)
{
	Gen* entry = NULL;
	bool flag = false;
	unsigned i = 0;
	while (!flag && (i < table -> size()))
	{
		entry = (*table)[i];

		if (wcscmp(entry -> class_name, cls) == 0)
		{
			if (((entry -> package_name == NULL) && (pkg == NULL))
			|| ((entry -> package_name != NULL && (pkg != NULL)) && (wcscmp(entry -> package_name, pkg) == 0)))
				flag = true;
			else
				i++;
		}
		else
			i++;
	}
	return (flag) ? entry ->file_name : NULL;
}

vector<wchar_t*>* GenTable::getInterfaces(wchar_t* pkg, wchar_t* cls)
{
	Gen* entry = NULL;
	bool flag = false;
	unsigned i = 0;
	while (!flag && (i < table -> size()))
	{
		entry = (*table)[i];

		if ((wcscmp(entry -> package_name, pkg) == 0)
		&& (wcscmp(entry -> class_name, cls) == 0))
		{
			flag = true;
		}
		else
			i++;
	}
	return (flag) ? entry -> interfaces : NULL;
}

wchar_t* AssocTable::getName(Assoc::Kind kind, Assoc::Mode mode, wchar_t* type, wchar_t* cls)
{
	Assoc*  entry = NULL;
	bool flag = false;
	unsigned i = 0;
	while (!flag && (i < table -> size()))
	{
		entry = (*table)[i];

		if ((entry -> kind == kind)
		&& (entry -> mode == mode)
		&& (wcscmp(entry -> type, type) == 0)
		&& (wcscmp(entry -> class_name, cls) == 0))
			flag = true;
		else
			i++;
	}
	return (flag) ? entry -> name : NULL;	
}

wchar_t* AssocTable::getType(Assoc::Kind kind, Assoc::Mode mode, wchar_t* name, wchar_t* pkg, wchar_t* cls)
{
	Assoc*  entry = NULL;
	bool flag = false;
	unsigned i = 0;
	while (!flag && (i < table -> size()))
	{
		entry = (*table)[i];

		if ((entry -> kind == kind)
		&& (entry -> mode == mode)
		&& (wcscmp(entry -> name, name) == 0)
//		&& (wcscmp(entry -> package_name, pkg) == 0)
		&& (wcscmp(entry -> class_name, cls) == 0))
			flag = true;
		else
			i++;
	}
	return (flag) ? entry -> type : NULL;	
}

bool AssocTable::isInvoked(wchar_t* name, wchar_t* cls)
{
	unsigned i = 0;
	while (i < table -> size())
	{
		Assoc* entry = (*table)[i];
		if ((entry -> kind == Assoc::MI)
		&& (entry -> type)
		&& (wcscmp(entry -> type, name) == 0)
		&& (wcscmp(entry -> class_name, cls) == 0))
			return true;
		i++;
	}
	return false;
}

void EnvTable::addEnvironment(wchar_t* var, Env::State state)
{
	Env* entry = new Env(var, state);
	table -> push_back(entry);
}

void MethodBodyTable::addMethodBodyAddr(wchar_t* pkg, wchar_t* cls, wchar_t* mtd, AstDeclared* ptr)
{
	MethodBodyAddr* entry = new MethodBodyAddr(pkg, cls, mtd, ptr);
	table -> push_back(entry);
}

void GenTable::addGeneralization(wchar_t* pkg, wchar_t* cls, wchar_t* spr, vector<wchar_t*>* ifcs, Gen::Kind k, char* f)
{
	Gen* entry = new Gen(pkg, cls, spr, ifcs, k, f);
	table -> push_back(entry);
}

void AssocTable::addAssociation(Assoc::Kind kind, Assoc::Mode mode, wchar_t* name, wchar_t* type, wchar_t* pkg, wchar_t* cls, wchar_t* mtd)
{
	Assoc* entry = new Assoc(kind, mode, name, type, pkg, cls, mtd);
	table -> push_back(entry);
}

void MethodBodyTable::dumpTable()
{
	Coutput << L"Method Body Reference Table" << endl;
	unsigned i;
	for (i = 0; i < table -> size(); i++)
	{
		MethodBodyAddr* entry = (*table)[i];
		Coutput << entry -> class_name 
			     << "::"
			     << entry -> method_name 
			     << endl;

		if (entry -> ast_location -> kind == Ast::CONSTRUCTOR)
		{
			Coutput << L"***constructor***" << endl;
			dynamic_cast<AstConstructorDeclaration*>(entry -> ast_location) -> Print();			
		}
		else
		{
			Coutput << L"***method***" << endl;
			dynamic_cast<AstMethodDeclaration*>(entry -> ast_location) -> Print();
		}
	}
	Coutput << endl;
}

void GenTable::dumpTable()
{
       Coutput << L"Generalization Table" << endl;
	unsigned i;
	for (i = 0; i < table -> size(); i++)
	{
		Gen* entry = (*table)[i];
		if (entry -> kind == Gen::CLASS)
		{
			Coutput << entry -> class_name << " |--> " 
			 	     << ((entry -> super_name) ? entry -> super_name : L"java.lang.Object")
			     	     << " ";

			unsigned j;
			Coutput << "{";
			if (entry -> interfaces)
			{
				for (j = 0; j < entry -> interfaces -> size(); j++)
				{
					unsigned end = entry -> interfaces -> size() - 1;

					Coutput << (*(entry -> interfaces))[j];
			
					if (j < end)
						Coutput << ", ";
				}
			}
			Coutput << "}" << endl;
		}
		else
		{
			Coutput << entry -> class_name << "  " ;
			unsigned j;
			Coutput << "{";
			if (entry -> interfaces)
			{
				for (j = 0; j < entry -> interfaces -> size(); j++)
				{
					unsigned end = entry -> interfaces -> size() - 1;

					Coutput << (*(entry -> interfaces))[j];
			
					if (j < end)
						Coutput << ", ";
				}
			}
			Coutput << "} " << endl;
		}
		Coutput << entry -> package_name << endl << entry -> file_name << endl;
	}
}

void AssocTable::dumpTable()
{
       Coutput << L"Association Table" << endl;
	unsigned i;
	for (i = 0; i < table -> size(); i++)
	{
		Assoc* entry = (*table)[i];
		Coutput << entry -> class_name << " --> "
			     << entry -> type;

              Coutput << " (by ";
		switch(entry -> kind)
		{
			case Assoc::CF:
				if (entry -> mode == Assoc::PRIVATE)
					Coutput << L"private class member " << entry -> name << ")";
				else if (entry -> mode == Assoc::PROTECTED)
					Coutput << L"protected class member " << entry -> name << ")";
				else
					Coutput << L"public class member " << entry -> name << ")";
				break;
			case Assoc::IM:
				Coutput << L"instance member " << entry -> name << ")";
				break;
			case Assoc::MP:
				Coutput << L"parameter " << entry -> name << L" in " << entry -> method_name << "(...))";
				break;
			case Assoc::OC:
				break;
			case Assoc::MR:
				if (entry -> mode == Assoc::PRIVATE)
					Coutput << L"return type in private instance method " << entry -> method_name << "(...))";
				else if (entry -> mode == Assoc::PROTECTED)
					Coutput << L"return type in protected instance method " << entry -> method_name << "(...))";
				else
					Coutput << L"return type in public instance method " << entry -> method_name << "(...))";
				break;
			case Assoc::CM:
				if (entry -> mode == Assoc::PRIVATE)
					Coutput << L"return type in private static method " << entry -> method_name << "(...))";
				else if (entry -> mode == Assoc::PROTECTED)
					Coutput << L"return type in protected static method " << entry -> method_name << "(...))";
				else
					Coutput << L"return type in public static method " << entry -> method_name << "(...))";
				break;
			default:
				break;
		}
		Coutput << endl;
	}
	Coutput << endl;
}

void Statechart::Print()
{
	unsigned i;
	for (i = 0; i < statechart -> size(); i++)
	{
		switch((*statechart)[i] -> kind)
		{
			case State::GET:
				Coutput << "GET state.";
				break;
			case State::SET:
				Coutput << "SET state.";
				break;
			case State::CREATE:
				Coutput << "CREATE state.";
				break;
			case State::RETURN:
				Coutput << "RETURN state.";
				break;
			case State::CONDITION:
				Coutput << "CONDITION state.";
				break;
			default:
				break;			
		}
		Coutput << endl;

		if ((*statechart)[i] -> kind != State::RETURN)
		{
			unsigned j;
			for (j = 0; j < (*statechart)[i] -> participants -> size(); j++)
				Coutput << " " << (*(*statechart)[i] -> participants)[j];
			Coutput << endl;
		}
	}
}

/***
 *
 * Flatten data structure
 *
 */

void Flatten::BuildSummary()
{
	visit(method->declaration->MethodDeclarationCast()->method_body_opt);

	for(unsigned i = 0; i < summary.size(); i++)
	{
		set<signed>::iterator p;
		for (p = (summary[i]->next).begin(); p != (summary[i]->next).end(); p++)
			(summary[*p]->previous).insert(i);
	}
}	
bool Flatten::Compare(AstExpression *b1, AstExpression *b2)
{
	if (b1->symbol && (b1->symbol == b2->symbol))
		return true;
	else if (b1->kind == b2->kind)
	{
		if (b1->kind == Ast::BINARY)
		{
			return (Compare(b1->BinaryExpressionCast()->left_expression, b2->BinaryExpressionCast()->left_expression) 
				 && Compare(b1->BinaryExpressionCast()->right_expression, b2->BinaryExpressionCast()->right_expression));
		}
		else if (b1->kind == Ast::PRE_UNARY)
		{
			return Compare(b1->PreUnaryExpressionCast()->expression, b2->PreUnaryExpressionCast()->expression);
		}
	}
	return false;
}
void Flatten::FlattenBoolean(vector<AstExpression*>& list, AstExpression *expression)
{
	if (expression)
	{
	if (!expression->conjoint)
		list.push_back(expression);
	else
	{
		list.push_back(expression->BinaryExpressionCast()->right_expression);
		FlattenBoolean(list, expression->BinaryExpressionCast()->left_expression);
	}
	}
}
Flatten::TransitionTag Flatten::TransitionFlow(AstExpression *b1, AstExpression *b2)
{
	if (!b1 && !b2)
		return UNCONDITIONAL;
	else if (!b1 && b2)
		return UNCONDITIONAL;
	else if (b1 && !b2)
		return CONDITIONAL;
	else if (Compare(b1, b2))
		return UNCONDITIONAL;
	else
	{
		// pre and post are both not null
		vector<AstExpression*> v1, v2;
		FlattenBoolean(v1, b1);
		FlattenBoolean(v2, b2);

		for (unsigned i = 0; i < v1.size(); i++)
		{
			bool neg1 = ((v1[i]->kind == Ast::PRE_UNARY) && (v1[i]->PreUnaryExpressionCast()->Tag() == AstPreUnaryExpression::NOT));
			AstExpression *expr1 = (neg1) ? v1[i]->PreUnaryExpressionCast()->expression : v1[i];
			
			for (unsigned j = 0; j < v2.size(); j++)
			{
				bool neg2 = ((v2[j]->kind == Ast::PRE_UNARY) && (v2[j]->PreUnaryExpressionCast()->Tag() == AstPreUnaryExpression::NOT));
				AstExpression *expr2 = (neg2) ? v2[j]->PreUnaryExpressionCast()->expression : v2[j];
				if (Compare(expr1, expr2))
				{
					if ((neg1 && !neg2) || (!neg1 && neg2))
						return NOTRANSITION;
				}
			}
		}
		return CONDITIONAL;
	}
}

void Flatten::PushCondition(AstExpression *expression)
{
	if (!condition)
		condition = expression;
	else
	{
		AstExpression *temp = condition;
		condition = ast_pool->NewBinaryExpression(AstBinaryExpression::AND_AND);
		condition->conjoint = true;
		condition->BinaryExpressionCast()->left_expression = temp;
		condition->BinaryExpressionCast()->right_expression = expression;
	}	
}
void Flatten::PopCondition()
{
	if (condition)
	{
		if (condition->conjoint)
		{
			if (condition->kind == Ast::BINARY)
			{
				AstExpression *temp = condition->BinaryExpressionCast()->right_expression;
				if (temp->kind == Ast::PRE_UNARY)
				{					
					condition->BinaryExpressionCast()->right_expression = temp->PreUnaryExpressionCast()->expression;
				}
				else
				{
					condition->BinaryExpressionCast()->right_expression = ast_pool->NewPreUnaryExpression(AstPreUnaryExpression::NOT);
					condition->BinaryExpressionCast()->right_expression->conjoint = true;
					condition->BinaryExpressionCast()->right_expression->PreUnaryExpressionCast()->expression = temp;
				}

			}
		}
		else
		{
			AstExpression *temp = condition;
			condition = ast_pool->NewPreUnaryExpression(AstPreUnaryExpression::NOT);
			condition->conjoint = true;
			condition->PreUnaryExpressionCast()->expression = temp;
		}
	}
}
void Flatten::visit(AstBlock* block)
{
	if (block->NumStatements())
	{
		unsigned lstmt = (block->NumStatements() == 1) ? 0 : (block->NumStatements() - 1);
		for (unsigned i = 0; i < lstmt; i++)
			visit(block -> Statement(i));
		visit(block->Statement(lstmt));
		UpdateSummary();
	}
}
void Flatten::visit(AstWhileStatement* while_statement)
{
	UpdateSummary();
	if (summary.size())
	{
		pred.clear();
		pred.insert(summary.size()-1);
	}

	PushCondition(while_statement->expression);
	visit(while_statement->statement);
	UpdateSummary();
	pred.insert(summary.size()-1);

	PopCondition();
}
void Flatten::visit(AstForStatement* for_statement)
{
	UpdateSummary();
	
	PushCondition(for_statement->end_expression_opt);	
	visit(for_statement->statement);	
	UpdateSummary();

	PopCondition();
}
void Flatten::visit(AstTryStatement *try_statement)
{
	visit(try_statement->block);
}
void Flatten::visit(AstSynchronizedStatement* synch_statement)
{
	visit(synch_statement->block);
}
void Flatten::visit(AstStatement *statement)
{
	switch(statement -> kind) 
	{
		case Ast::IF:
			visit(statement -> IfStatementCast());
			break;
		case Ast::WHILE:
			visit(statement -> WhileStatementCast());
			break;
		case Ast::FOR:
			visit(statement -> ForStatementCast());
			break;
		case Ast::TRY:
			visit(statement -> TryStatementCast());
			break;
		case Ast::EXPRESSION_STATEMENT:
			visit(statement -> ExpressionStatementCast() -> expression);
			break;
		case Ast::SYNCHRONIZED_STATEMENT:
			visit(statement -> SynchronizedStatementCast());
			break;
		case Ast::BLOCK:
			visit(statement -> BlockCast());
			break;			
		case Ast::RETURN:
			visit(statement -> ReturnStatementCast());
			break;
		case Ast::LOCAL_VARIABLE_DECLARATION:
			visit(statement -> LocalVariableStatementCast());
			break;		
		default:
			statements.push_back(statement);
			break;
	}
}
void Flatten::visit(AstExpression *expression)
{
	switch(expression -> kind)
	{
		case Ast::PARENTHESIZED_EXPRESSION:
			visit(expression->ParenthesizedExpressionCast() -> expression);
			break;
		case Ast::CAST:
			visit(expression->CastExpressionCast() -> expression);
			break;
		case Ast::CONDITIONAL:
			statements.push_back(expression);
			//visit(expression->ConditionalExpressionCast());
			break;
		case Ast::ASSIGNMENT:
			visit(expression->AssignmentExpressionCast());
			break;
		case Ast::CALL:
			visit(expression->MethodInvocationCast());
			break;			
		default:
			statements.push_back(expression);
			break;
	}
}
void visit(AstConditionalExpression *cond_expression)
{
	// TODO: should add conditional BLOCK statements 
}
void Flatten::visit(AstMethodInvocation* call)
{
	// might want to check all participants in this method invocation
	// e.g., base_opt, 	call->symbol->MethodCast()>Type(), call->arguments->Argument(i), etc

	if ((strcmp(call->symbol->MethodCast()->containing_type->fully_qualified_name->value, "java/security/AccessController") == 0)
	&& (strcmp(call->symbol->MethodCast()->SignatureString(), "(Ljava/security/PrivilegedAction;)Ljava/lang/Object;") == 0))
	{
		AstClassCreationExpression *class_creation = Utility::RemoveCasting(call->arguments->Argument(0))->ClassCreationExpressionCast();
		visit(class_creation->symbol->TypeCast()->MethodSym(1)->declaration->MethodDeclarationCast()->method_body_opt);
	}
	else
		statements.push_back(call);
}
void Flatten::visit(AstIfStatement* statement)
{
	UpdateSummary();
	if (summary.size())
	{
		pred.clear();
		pred.insert(summary.size()-1);
	}
	set<signed> pred2(pred);
	AstExpression *top = condition;
	
	PushCondition(statement->expression);
	//visit(statement->expression);
	visit(statement->true_statement);
	UpdateSummary();

	if (statement->false_statement_opt)
	{
		pred.swap(pred2);
		condition = top;
		AstExpression *else_condition = ast_pool->NewPreUnaryExpression(AstPreUnaryExpression::NOT);
		else_condition->PreUnaryExpressionCast()->expression = statement->expression;
		PushCondition(else_condition);

		visit(statement->false_statement_opt);
		UpdateSummary();
		pred.clear();
		pred.insert(summary.size() - 2); // last block for the true branch
		pred.insert(summary.size() - 1); // last block for the false branch
		condition = top;
	}
	else
	{
		if ((*summary[summary.size() - 1]->statements)[summary[summary.size() - 1]->statements->size() - 1]->kind != Ast::RETURN) 
			pred.insert(summary.size() - 1);		
		// pred <<Union>> pred2
		set<signed>::iterator p;
		for (p = pred2.begin(); p != pred2.end(); p++)
			pred.insert(*p);
		condition = top;
		AstExpression *else_condition = ast_pool->NewPreUnaryExpression(AstPreUnaryExpression::NOT);
		else_condition->PreUnaryExpressionCast()->expression = statement->expression;
		PushCondition(else_condition);
	}
}
void Flatten::visit(AstAssignmentExpression *expression)
{
	statements.push_back(expression);
	// TODO also check for aliasing
}
void Flatten::visit(AstLocalVariableStatement* local_var)
{
	for (unsigned i=0; i < local_var->NumVariableDeclarators(); i++)
		visit(local_var->VariableDeclarator(i));
}
void Flatten::visit(AstVariableDeclarator* var_declarator)
{
	statements.push_back(var_declarator);
}
void Flatten::visit(AstReturnStatement* statement)
{
	statements.push_back(statement);
	capture_trace = true;
}
void Flatten::UpdateSummary()
{	
	if (statements.size())
	{
		Snapshot *snapshot = new Snapshot();
		snapshot->statements = new vector<Ast*>(statements);
		statements.clear();
		if (condition)
		{
			snapshot->condition = condition->Clone(ast_pool)->ExpressionCast();
		}
		snapshot->index = summary.size();
		set<signed>::iterator p;	
		for(p = pred.begin(); p != pred.end(); p++)
		{
			if (*p >= 0)
				(summary[*p]->next).insert(snapshot->index);
			else
				(snapshot->previous).insert(-1);
		}
		summary.push_back(snapshot);
		if (capture_trace)
		{
			traces.push_back(snapshot);
			capture_trace = false;
		}
	}
}
void Flatten::DumpSummary()
{
	Coutput << method->Utf8Name() << endl;
	for (unsigned i = 0; i < summary.size(); i++)
	{
		Snapshot *snapshot = summary[i];
		Coutput << "Snapshot[" << snapshot->index << "]" << endl;
		set<signed>::iterator p;
		//incoming edges
		Coutput << "In-coming snapshots: ";
		for (p = (snapshot->previous).begin(); p != (snapshot->previous).end(); p++)
			Coutput << *p << " ";
		Coutput << endl;		
		// outgoiong edges
		Coutput << "Out-going snapshots: ";
		for (p = (snapshot->next).begin(); p != (snapshot->next).end(); p++)
			Coutput << *p << " ";
		Coutput << endl;		
		Coutput << "STATEMENTS:" << endl;
		unsigned j;
		for (j = 0; j < snapshot->statements->size(); j++)
		{
			Coutput << "---Statement[" << j << "]---" << endl;
			(*snapshot->statements)[j]->Print();
		}
		Coutput << "CONDITIONS:" << endl;
		
		if (snapshot->condition)
			snapshot->condition->Print();
		Coutput << endl;
	}
}

Control::Control(char** arguments, Option& option_)
    : return_code(0)
    , option(option_)
    , dot_classpath_index(0)
    , system_table(NULL)
    , system_semantic(NULL)
    , semantic(1024)
    , needs_body_work(1024)
    , type_trash_bin(1024)
    , input_java_file_set(1021)
    , input_class_file_set(1021)
    , expired_file_set()
    , recompilation_file_set(1021)
    // Type and method cache. These variables are assigned in control.h
    // accessors, but must be NULL at startup.
    , Annotation_type(NULL)
    , AssertionError_type(NULL)
    , AssertionError_Init_method(NULL)
    , AssertionError_InitWithChar_method(NULL)
    , AssertionError_InitWithBoolean_method(NULL)
    , AssertionError_InitWithInt_method(NULL)
    , AssertionError_InitWithLong_method(NULL)
    , AssertionError_InitWithFloat_method(NULL)
    , AssertionError_InitWithDouble_method(NULL)
    , AssertionError_InitWithObject_method(NULL)
    , Boolean_type(NULL)
    , Boolean_TYPE_field(NULL)
    , Byte_type(NULL)
    , Byte_TYPE_field(NULL)
    , Character_type(NULL)
    , Character_TYPE_field(NULL)
    , Class_type(NULL)
    , Class_forName_method(NULL)
    , Class_getComponentType_method(NULL)
    , Class_desiredAssertionStatus_method(NULL)
    , ClassNotFoundException_type(NULL)
    , Cloneable_type(NULL)
    , Comparable_type(NULL)
    , Double_type(NULL)
    , Double_TYPE_field(NULL)
    , ElementType_type(NULL)
    , ElementType_TYPE_field(NULL)
    , ElementType_FIELD_field(NULL)
    , ElementType_METHOD_field(NULL)
    , ElementType_PARAMETER_field(NULL)
    , ElementType_CONSTRUCTOR_field(NULL)
    , ElementType_LOCAL_VARIABLE_field(NULL)
    , ElementType_ANNOTATION_TYPE_field(NULL)
    , ElementType_PACKAGE_field(NULL)
    , Enum_type(NULL)
    , Enum_Init_method(NULL)
    , Enum_ordinal_method(NULL)
    , Enum_valueOf_method(NULL)
    , Error_type(NULL)
    , Exception_type(NULL)
    , Float_type(NULL)
    , Float_TYPE_field(NULL)
    , Integer_type(NULL)
    , Integer_TYPE_field(NULL)
    , Iterable_type(NULL)
    , Iterable_iterator_method(NULL)
    , Iterator_type(NULL)
    , Iterator_hasNext_method(NULL)
    , Iterator_next_method(NULL)
    , Long_type(NULL)
    , Long_TYPE_field(NULL)
    , NoClassDefFoundError_type(NULL)
    , NoClassDefFoundError_Init_method(NULL)
    , NoClassDefFoundError_InitString_method(NULL)
    , Object_type(NULL)
    , Object_getClass_method(NULL)
    , Overrides_type(NULL)
    , Retention_type(NULL)
    , RetentionPolicy_type(NULL)
    , RetentionPolicy_SOURCE_field(NULL)
    , RetentionPolicy_CLASS_field(NULL)
    , RetentionPolicy_RUNTIME_field(NULL)
    , RuntimeException_type(NULL)
    , Serializable_type(NULL)
    , Short_type(NULL)
    , Short_TYPE_field(NULL)
    , String_type(NULL)
    , StringBuffer_type(NULL)
    , StringBuffer_Init_method(NULL)
    , StringBuffer_InitWithString_method(NULL)
    , StringBuffer_toString_method(NULL)
    , StringBuffer_append_char_method(NULL)
    , StringBuffer_append_boolean_method(NULL)
    , StringBuffer_append_int_method(NULL)
    , StringBuffer_append_long_method(NULL)
    , StringBuffer_append_float_method(NULL)
    , StringBuffer_append_double_method(NULL)
    , StringBuffer_append_string_method(NULL)
    , StringBuffer_append_object_method(NULL)
    , StringBuilder_type(NULL)
    , StringBuilder_Init_method(NULL)
    , StringBuilder_InitWithString_method(NULL)
    , StringBuilder_toString_method(NULL)
    , StringBuilder_append_char_method(NULL)
    , StringBuilder_append_boolean_method(NULL)
    , StringBuilder_append_int_method(NULL)
    , StringBuilder_append_long_method(NULL)
    , StringBuilder_append_float_method(NULL)
    , StringBuilder_append_double_method(NULL)
    , StringBuilder_append_string_method(NULL)
    , StringBuilder_append_object_method(NULL)
    , Target_type(NULL)
    , Throwable_type(NULL)
    , Throwable_getMessage_method(NULL)
    , Throwable_initCause_method(NULL)
    , Void_type(NULL)
    , Void_TYPE_field(NULL)
    // storage for all literals seen in source
    , int_pool(&bad_value)
    , long_pool(&bad_value)
    , float_pool(&bad_value)
    , double_pool(&bad_value)
    , Utf8_pool(&bad_value)
#ifdef JIKES_DEBUG
    , input_files_processed(0)
    , class_files_read(0)
    , class_files_written(0)
    , line_count(0)   
#endif // JIKES_DEBUG
    // Package cache.  unnamed and lang are initialized in constructor body.
    , annotation_package(NULL)
    , io_package(NULL)
    , util_package(NULL)
{
PINOT_DEBUG = (getenv("PINOT_DEBUG")) ? true :  false;
option.bytecode = false;
	
// breakpoint 0.
// getchar();
    r_table = new ReadAccessTable();
    w_table = new WriteAccessTable();
    d_table = new DelegationTable();
    cs_table = new ClassSymbolTable();
    ms_table = new MethodSymbolTable();

    nSingleton = nCoR = nBridge = nStrategy = nState = nFlyweight = nComposite = nMediator = nTemplate = nFactoryMethod = nAbstractFactory = nVisitor = nDecorator = nObserver = nProxy = nAdapter = nFacade = 0;


    mb_table = new MethodBodyTable();
    gen_table = new GenTable();
    assoc_table = new AssocTable();

    counter1 = 0;
    counter2 = 0;
    counter3 = 0;

    ProcessGlobals();
    ProcessUnnamedPackage();
    ProcessPath();
    ProcessSystemInformation();

    //
    // Instantiate a scanner and a parser and initialize the static members
    // for the semantic processors.
    //
    scanner = new Scanner(*this);
    parser = new Parser();
    SemanticError::StaticInitializer();

    //
    // Process all file names specified in command line
    //
    ProcessNewInputFiles(input_java_file_set, arguments);

    //
    // For each input file, copy it into the input_files array and process
    // its package declaration. Estimate we need 64 tokens.
    //
    StoragePool* ast_pool = new StoragePool(64);
    FileSymbol** input_files = new FileSymbol*[input_java_file_set.Size() + 1];
    int num_files = 0;
    FileSymbol* file_symbol;
    for (file_symbol = (FileSymbol*) input_java_file_set.FirstElement();
         file_symbol;
         file_symbol = (FileSymbol*) input_java_file_set.NextElement())
    {
        input_files[num_files++] = file_symbol;

	//Coutput << file_symbol->FileName() << endl;
#ifdef JIKES_DEBUG
        input_files_processed++;
#endif
        errno = 0;
        scanner -> Scan(file_symbol);
        if (file_symbol -> lex_stream) // did we have a successful scan!
        {
            AstPackageDeclaration* package_declaration =
                parser -> PackageHeaderParse(file_symbol -> lex_stream,
                                             ast_pool);

            ProcessPackageDeclaration(file_symbol, package_declaration);
            ast_pool -> Reset();
        }
        else
        {
            const char* std_err = strerror(errno);
            ErrorString err_str;
            err_str << '"' << std_err << '"' << " while trying to open "
                    << file_symbol -> FileName();
            general_io_errors.Next() = err_str.SafeArray();
        }
    }

    //
    //
    //
    FileSymbol* main_file_clone;
    if (num_files > 0)
        main_file_clone = input_files[0] -> Clone();
    else
    {
        //
        // Some name, any name !!! We use dot_name_symbol as a bad file name
        // because no file can be named ".".
        //
        FileSymbol* file_symbol = classpath[dot_classpath_index] ->
            RootDirectory() -> InsertFileSymbol(dot_name_symbol);
        file_symbol -> directory_symbol = classpath[dot_classpath_index] ->
            RootDirectory();
        file_symbol -> SetJava();

        main_file_clone = file_symbol -> Clone();
    }

    main_file_clone -> semantic = new Semantic(*this, main_file_clone);
    system_semantic = main_file_clone -> semantic;
    scanner -> SetUp(main_file_clone);

#ifdef WIN32_FILE_SYSTEM
    //
    //
    //
    if (option.BadMainDisk())
    {
        system_semantic -> ReportSemError(SemanticError::NO_CURRENT_DIRECTORY,
                                          BAD_TOKEN);
    }
#endif // WIN32_FILE_SYSTEM

    unsigned i;
    for (i = 0; i < bad_dirnames.Length(); i++)
    {
        system_semantic ->
            ReportSemError(SemanticError::CANNOT_OPEN_PATH_DIRECTORY,
                           BAD_TOKEN, bad_dirnames[i]);
    }
    for (i = 0; i < bad_zip_filenames.Length(); i++)
    {
        system_semantic -> ReportSemError(SemanticError::CANNOT_OPEN_ZIP_FILE,
                                          BAD_TOKEN, bad_zip_filenames[i]);
    }
    for (i = 0; i < general_io_warnings.Length(); i++)
    {
        system_semantic -> ReportSemError(SemanticError::IO_WARNING, BAD_TOKEN,
                                          general_io_warnings[i]);
        delete [] general_io_warnings[i];
    }
    for (i = 0; i < general_io_errors.Length(); i++)
    {
        system_semantic -> ReportSemError(SemanticError::IO_ERROR, BAD_TOKEN,
                                          general_io_errors[i]);
        delete [] general_io_errors[i];
    }

    //
    // Require the existence of java.lang.
    //
    if (lang_package -> directory.Length() == 0)
    {
        system_semantic -> ReportSemError(SemanticError::PACKAGE_NOT_FOUND,
                                          BAD_TOKEN,
                                          StringConstant::US_java_SL_lang);
    }

    //
    // When the -d option is specified, create the relevant
    // directories if they don't already exist.
    //
    if (option.directory)
    {
        if (! SystemIsDirectory(option.directory))
        {
            for (char* ptr = option.directory; *ptr; ptr++)
            {
                char delimiter = *ptr;
                if (delimiter == U_SLASH)
                {
                    *ptr = U_NULL;

                    if (! SystemIsDirectory(option.directory))
                        SystemMkdir(option.directory);

                    *ptr = delimiter;
                }
            }

            SystemMkdir(option.directory);

            if (! SystemIsDirectory(option.directory))
            {
                int length = strlen(option.directory);
                wchar_t* name = new wchar_t[length + 1];
                for (int j = 0; j < length; j++)
                    name[j] = option.directory[j];
                name[length] = U_NULL;
                system_semantic -> ReportSemError(SemanticError::CANNOT_OPEN_DIRECTORY,
                                                  BAD_TOKEN, name);
                delete [] name;
            }
        }
    }

    //
    //
    //
    for (i = 0; i < bad_input_filenames.Length(); i++)
    {
        system_semantic -> ReportSemError(SemanticError::BAD_INPUT_FILE,
                                          BAD_TOKEN, bad_input_filenames[i]);
    }

    //
    //
    //
    for (i = 0; i < unreadable_input_filenames.Length(); i++)
    {
        system_semantic -> ReportSemError(SemanticError::UNREADABLE_INPUT_FILE,
                                          BAD_TOKEN,
                                          unreadable_input_filenames[i]);
    }

    //
    //
    //
    if (system_semantic -> NumErrors() > 0)
    {
        system_semantic -> PrintMessages();
        return_code = system_semantic -> return_code;
    }
    else
    {
        //
        // There might be some warnings we want to print.
        //
        system_semantic -> PrintMessages();
        input_java_file_set.SetEmpty();
        for (int j = 0; j < num_files; j++)
        {
            FileSymbol* file_symbol = input_files[j];
            if (! input_java_file_set.IsElement(file_symbol))
                ProcessFile(file_symbol, ast_pool);
        }





//    mb_table -> dumpTable();
//	gen_table -> dumpTable();
//    assoc_table -> dumpTable();
//	d_table -> DumpTable();

    //FindPrototype(mb_table, gen_table, assoc_table);

// breakpoint 1.
// getchar();
// Coutput << "breakpoint 1." << endl;
	d_table->ConcretizeDelegations();
	ms_table->ExpandCallDependents();
	cs_table->ExpandSubtypes();
// breakpoint 2.
// getchar();	

    Coutput << endl;
    Coutput << "--------- Original GoF Patterns ----------" << endl << endl;

    //FindSingleton(cs_table, ms_table);
    FindSingleton1(cs_table, ast_pool);

    FindChainOfResponsibility(cs_table, ms_table, d_table, ast_pool);
    FindBridge(cs_table, d_table);
    FindStrategy1(cs_table, d_table, w_table, r_table, ms_table);	
    //FindFlyweight(mb_table, gen_table, assoc_table);
    FindFlyweight1(ms_table);
    FindFlyweight2(cs_table, w_table, r_table);
    FindComposite(cs_table, d_table);
    //FindMediator(cs_table, d_table);
    FindTemplateMethod(d_table);
    FindFactory(cs_table, ms_table, ast_pool);
    FindVisitor(cs_table, ms_table);
    FindObserver(cs_table, d_table);
    FindMediator2(cs_table);
    FindProxy(cs_table, d_table);
    FindAdapter(cs_table);
    FindFacade(cs_table);
    
    //    FindThreadSafeInterface(d_table);

#ifdef PLUGIN_ENABLED
    Coutput << endl;
    Coutput << "--------- User-defined Patterns ----------" << endl<< endl;
	

  void *handle = dlopen("/home/madonna/sandbox/pinot/src/plugins.dll", RTLD_LAZY);
  if (!handle)
  {
    printf("Error during dlopen(): %s\n", dlerror());
    exit(1);
  }

  void (*pattern)(DelegationTable*);
  pattern = (void (*)(DelegationTable*))dlsym(handle, "FindTemplateMethod");
  if (!pattern)
  {
    printf("Error locating 'FindTemplateMethod' - %s\n", dlerror());
    exit(1);
  }

  (*pattern)(d_table);
#endif

    // Print Statics


    Coutput << endl << "------------------------------------------" << endl << endl;
    Coutput << "Pattern Instance Statistics:" << endl << endl;

    Coutput << "Creational Patterns" << endl;
    Coutput << "==============================" << endl;
    Coutput << "Abstract Factory";
    Coutput.width(30 - sizeof("Abstract Factory"));
    Coutput << nAbstractFactory << endl;

    Coutput << "Factory Method";
    Coutput.width(30 - sizeof("Factory Method"));
    Coutput << nFactoryMethod<< endl;

    Coutput << "Singleton";
    Coutput.width(30 - sizeof("Singleton"));
    Coutput << nSingleton << endl;

		Coutput << "------------------------------" << endl;
    Coutput << "Structural Patterns" << endl;
    Coutput << "==============================" << endl;

    Coutput << "Adapter";
    Coutput.width(30 - sizeof("Adapter"));
    Coutput << nAdapter << endl;
    
    Coutput << "Bridge";
    Coutput.width(30 - sizeof("Bridge"));
    Coutput << nBridge<< endl;

    Coutput << "Composite";
    Coutput.width(30 - sizeof("Composite"));
    Coutput << nComposite << endl;

    Coutput << "Decorator";
    Coutput.width(30 - sizeof("Decorator"));
    Coutput << nDecorator << endl;

    Coutput << "Facade";
    Coutput.width(30 - sizeof("Facade"));
    Coutput << nFacade << endl;

    Coutput << "Flyweight";
    Coutput.width(30 - sizeof("Flyweight"));
    Coutput << nFlyweight << endl;

    Coutput << "Proxy";
    Coutput.width(30 - sizeof("Proxy"));
    Coutput << nProxy << endl;
		Coutput << "------------------------------" << endl;
    Coutput << "Behavioral Patterns" << endl;
    Coutput << "==============================" << endl;
    
    Coutput << "Chain of Responsibility";
    Coutput.width(30 - sizeof("Chain of Responsibility"));
    Coutput << nCoR<< endl;

    Coutput << "Mediator";
    Coutput.width(30 - sizeof("Mediator"));
    Coutput << nMediator << endl;

    Coutput << "Observer";
    Coutput.width(30 - sizeof("Observer"));
    Coutput << nObserver << endl;

    Coutput << "State";
    Coutput.width(30 - sizeof("State"));
    Coutput << nState << endl;

    Coutput << "Strategy";
    Coutput.width(30 - sizeof("Strategy"));
    Coutput << nStrategy<< endl;

    Coutput << "Template Method";
    Coutput.width(30 - sizeof("Template Method"));
    Coutput << nTemplate << endl;

    Coutput << "Visitor";
    Coutput.width(30 - sizeof("Visitor"));
    Coutput << nVisitor << endl;
		Coutput << "------------------------------" << endl;
		Coutput << endl;	

    Coutput << "Number of classes processed: " << gen_table -> getSize() << endl;
    Coutput << "Number of files processed: " << num_files << endl;
    Coutput << "Size of DelegationTable: " << d_table -> size() << endl;
    Coutput << "Size of concrete class nodes: " << cs_table -> ConcreteClasses() << endl;
    Coutput << "Size of undirected invocation edges: " << d_table -> UniqueDirectedCalls () << endl;

	Coutput << endl << endl;
	Coutput << "nMediatorFacadeDual/nMediator = " << nMediatorFacadeDual << "/" << nMediator << endl;
	Coutput << "nImmutable/nFlyweight = " << nImmutable << "/" << nFlyweight << endl;
	Coutput << "nFlyweightGoFVersion = " << nFlyweightGoFVersion << endl;

        //
        // Clean up all the files that have just been compiled in this new
        // batch.
        //
        FileSymbol* file_symbol;
        for (file_symbol = (FileSymbol*) input_java_file_set.FirstElement();
             file_symbol;
             file_symbol = (FileSymbol*) input_java_file_set.NextElement())
        {
            CleanUp(file_symbol);
        }

        //
        // If more messages were added to system_semantic, print them...
        //
        system_semantic -> PrintMessages();
        if (system_semantic -> return_code > 0 ||
            bad_input_filenames.Length() > 0 ||
            unreadable_input_filenames.Length() > 0)
        {
            return_code = 1;
        }

        //
        // If the incremental flag is on, check to see if the user wants us
        // to recompile.
        //
        if (option.incremental)
        {
            // The depend flag should only be in effect in the first pass
            option.depend = false;

            for (bool recompile = IncrementalRecompilation();
                 recompile; recompile = IncrementalRecompilation())
            {
                // Reset the return code as we may compile clean in this pass.
                return_code = 0;
                system_semantic -> return_code = 0;

                //
                //
                //
                for (i = 0; i < bad_input_filenames.Length(); i++)
                {
                    system_semantic ->
                        ReportSemError(SemanticError::BAD_INPUT_FILE,
                                       BAD_TOKEN, bad_input_filenames[i]);
                }

                //
                //
                //
                for (i = 0; i < unreadable_input_filenames.Length(); i++)
                {
                    system_semantic ->
                        ReportSemError(SemanticError::UNREADABLE_INPUT_FILE,
                                       BAD_TOKEN,
                                       unreadable_input_filenames[i]);
                }

                FileSymbol* file_symbol;

                num_files = 0;
                delete [] input_files; // delete previous copy
                input_files = new FileSymbol*[recompilation_file_set.Size()];
                for (file_symbol = (FileSymbol*) recompilation_file_set.FirstElement();
                     file_symbol;
                     file_symbol = (FileSymbol*) recompilation_file_set.NextElement())
                {
                    input_java_file_set.RemoveElement(file_symbol);
                    input_files[num_files++] = file_symbol;

                    LexStream* lex_stream = file_symbol -> lex_stream;
                    if (lex_stream)
                    {
                        AstPackageDeclaration* package_declaration = parser ->
                            PackageHeaderParse(lex_stream, ast_pool);
                        ProcessPackageDeclaration(file_symbol,
                                                  package_declaration);
                        ast_pool -> Reset();
                    }
                }

                //
                // If a file was erased, remove it from the input file set.
                //
                for (file_symbol = (FileSymbol*) expired_file_set.FirstElement();
                     file_symbol;
                     file_symbol = (FileSymbol*) expired_file_set.NextElement())
                {
                    input_java_file_set.RemoveElement(file_symbol);
                }

                //
                // Reset the global objects before recompiling this new batch.
                //
                expired_file_set.SetEmpty();
                recompilation_file_set.SetEmpty();
                type_trash_bin.Reset();

                //
                // For each file that should be recompiled, process it if it
                // has not already been dragged in by dependence.
                //
                for (int j = 0; j < num_files; j++)
                {
                    FileSymbol* file_symbol = input_files[j];
                    if (! input_java_file_set.IsElement(file_symbol))
                        ProcessFile(file_symbol, ast_pool);
                }

                //
                // Clean up all the files that have just been compiled in
                // this new batch.
                //
                for (file_symbol = (FileSymbol*) input_java_file_set.FirstElement();
                    // delete file_symbol
                     file_symbol;
                     file_symbol = (FileSymbol*) input_java_file_set.NextElement())
                {
                    // delete file_symbol
                    CleanUp(file_symbol);
                }

                //
                // If any system error or warning was detected, print it...
                //
                system_semantic -> PrintMessages();
                if (system_semantic -> return_code > 0 ||
                    bad_input_filenames.Length() > 0 ||
                    unreadable_input_filenames.Length() > 0)
                {
                    return_code = 1;
                }
            }
        }

        //
        // Are we supposed to generate Makefiles?
        //
        if (option.makefile)
        {
            if (option.dependence_report)
            {
                FILE* outfile = SystemFopen(option.dependence_report_name,
                                            "w");
                if (outfile == NULL)
                    Coutput << "*** Cannot open dependence output file "
                            << option.dependence_report_name << endl;
                else
                {
                    SymbolSet types_in_new_files;
                    FileSymbol* file_symbol;
                    for (file_symbol = (FileSymbol*) input_java_file_set.FirstElement();
                         file_symbol;
                         file_symbol = (FileSymbol*) input_java_file_set.NextElement())
                    {
                        char* java_name = file_symbol -> FileName();

                        for (i = 0; i < file_symbol -> types.Length(); i++)
                        {
                            TypeSymbol* type = file_symbol -> types[i];
                            fprintf(outfile, "%s : %s\n", java_name,
                                    type -> SignatureString());

                            TypeSymbol* static_parent;
                            for (static_parent = (TypeSymbol*) type -> static_parents -> FirstElement();
                                 static_parent;
                                 static_parent = (TypeSymbol*) type -> static_parents -> NextElement())
                            {
                                if (! type -> parents ->
                                    IsElement(static_parent))
                                {
                                    // Only a static ref to static_parent?
                                    fprintf(outfile, "   !%s\n",
                                            static_parent -> SignatureString());

                                    //
                                    // If the type is contained in a type that
                                    // is not one of the input files, save it
                                    //
                                    if (static_parent -> file_symbol &&
                                        static_parent -> file_symbol -> IsClass())
                                    {
                                        types_in_new_files.AddElement(static_parent);
                                    }
                                }
                            }

                            TypeSymbol* parent;
                            for (parent = (TypeSymbol*) type -> parents -> FirstElement();
                                 parent;
                                 parent = (TypeSymbol*) type -> parents -> NextElement())
                            {
                                fprintf(outfile, "    %s\n",
                                        parent -> SignatureString());

                                //
                                // If the type is contained in a type that is
                                // not one of the input files, save it
                                //
                                if (parent -> file_symbol &&
                                    parent -> file_symbol -> IsClass())
                                {
                                    types_in_new_files.AddElement(parent);
                                }
                            }
                        }
                    }

                    //
                    // Print the list of class files that are referenced.
                    //
                    TypeSymbol* type;
                    for (type = (TypeSymbol*) types_in_new_files.FirstElement();
                         type;
                         type = (TypeSymbol*) types_in_new_files.NextElement())
                    {
                        char* class_name = type -> file_symbol -> FileName();
                        fprintf(outfile, "%s : %s\n", class_name,
                                type -> SignatureString());
                    }

                    fclose(outfile);
                }
            }
            else
            {
                SymbolSet* candidates =
                    new SymbolSet(input_java_file_set.Size() +
                                  input_class_file_set.Size());
                *candidates = input_java_file_set;
                candidates -> Union(input_class_file_set);

                TypeDependenceChecker* dependence_checker =
                    new TypeDependenceChecker(this, *candidates,
                                              type_trash_bin);
                dependence_checker -> PartialOrder();
                dependence_checker -> OutputDependences();
                delete dependence_checker;

                delete candidates;
            }
        }
    }

	
    delete ast_pool;
    delete main_file_clone; // delete the clone of the main source file...
    delete [] input_files;


    delete cs_table;
    delete ms_table;

    delete mb_table;
    delete gen_table;
    delete assoc_table;
}


Control::~Control()
{
    unsigned i;
    for (i = 0; i < bad_zip_filenames.Length(); i++)
        delete [] bad_zip_filenames[i];
    for (i = 0; i < bad_input_filenames.Length(); i++)
        delete [] bad_input_filenames[i];
    for (i = 0; i < unreadable_input_filenames.Length(); i++)
        delete [] unreadable_input_filenames[i];
    for (i = 0; i < system_directories.Length(); i++)
        delete system_directories[i];

    delete scanner;
    delete parser;
    delete system_semantic;
    delete system_table;

#ifdef JIKES_DEBUG
    if (option.debug_dump_lex || option.debug_dump_ast ||
        option.debug_unparse_ast)
    {
        Coutput << line_count << " source lines read" << endl
                << class_files_read << " \".class\" files read" << endl
                << class_files_written << " \".class\" files written" << endl
                << input_files_processed << " \".java\" files processed"
                << endl;
    }
#endif // JIKES_DEBUG
}


PackageSymbol* Control::ProcessPackage(const wchar_t* name)
{
    int name_length = wcslen(name);
    wchar_t* package_name = new wchar_t[name_length];
    int length;
    for (length = 0;
         length < name_length && name[length] != U_SLASH; length++)
    {
         package_name[length] = name[length];
    }
    NameSymbol* name_symbol = FindOrInsertName(package_name, length);

    PackageSymbol* package_symbol =
        external_table.FindPackageSymbol(name_symbol);
    if (! package_symbol)
    {
        package_symbol = external_table.InsertPackageSymbol(name_symbol, NULL);
        FindPathsToDirectory(package_symbol);
    }

    while (length < name_length)
    {
        int start = ++length;
        for (int i = 0;
             length < name_length && name[length] != U_SLASH;
             i++, length++)
        {
             package_name[i] = name[length];
        }
        name_symbol = FindOrInsertName(package_name, length - start);
        PackageSymbol* subpackage_symbol =
            package_symbol -> FindPackageSymbol(name_symbol);
        if (! subpackage_symbol)
        {
            subpackage_symbol =
                package_symbol -> InsertPackageSymbol(name_symbol);
            FindPathsToDirectory(subpackage_symbol);
        }
        package_symbol = subpackage_symbol;
    }

    delete [] package_name;
    return package_symbol;
}


//
// When searching for a subdirectory in a zipped file, it must already be
// present in the hierarchy.
//
DirectorySymbol* Control::FindSubdirectory(PathSymbol* path_symbol,
                                           wchar_t* name, int name_length)
{
    wchar_t* directory_name = new wchar_t[name_length + 1];

    DirectorySymbol* directory_symbol = path_symbol -> RootDirectory();
    for (int start = 0, end;
         directory_symbol && start < name_length;
         start = end + 1)
    {
        end = start;
        for (int i = 0; end < name_length && name[end] != U_SLASH; i++, end++)
             directory_name[i] = name[end];
        NameSymbol* name_symbol = FindOrInsertName(directory_name,
                                                   end - start);
        directory_symbol =
            directory_symbol -> FindDirectorySymbol(name_symbol);
    }

    delete [] directory_name;
    return directory_symbol;
}


//
// When searching for a directory in the system, if it is not already present
// in the hierarchy insert it and attempt to read it from the system...
//
#ifdef UNIX_FILE_SYSTEM
DirectorySymbol* Control::ProcessSubdirectories(wchar_t* source_name,
                                                int source_name_length,
                                                bool source_dir)
{
    int name_length = (source_name_length < 0 ? 0 : source_name_length);
    char* input_name = new char[name_length + 1];
    for (int i = 0; i < name_length; i++)
        input_name[i] = source_name[i];
    input_name[name_length] = U_NULL;

    DirectorySymbol* directory_symbol = NULL;
    struct stat status;
    if (SystemStat(input_name, &status) == 0 &&
        (status.st_mode & JIKES_STAT_S_IFDIR))
    {
        directory_symbol = system_table ->
            FindDirectorySymbol(status.st_dev, status.st_ino);
    }

    if (! directory_symbol)
    {
        if (input_name[0] == U_SLASH) // file name starts with '/'
        {
            directory_symbol =
                new DirectorySymbol(FindOrInsertName(source_name, name_length),
                                    classpath[dot_classpath_index],
                                    source_dir);
            directory_symbol -> ReadDirectory();
            system_directories.Next() = directory_symbol;
            system_table -> InsertDirectorySymbol(status.st_dev,
                                                  status.st_ino,
                                                  directory_symbol);
        }
        else
        {
            wchar_t* name = new wchar_t[name_length + 1];
            for (int i = 0; i < name_length; i++)
                name[i] = source_name[i];
            name[name_length] = U_NULL;

            // Start at the dot directory.
            directory_symbol =
                classpath[dot_classpath_index] -> RootDirectory();

            wchar_t* directory_name = new wchar_t[name_length];
            int end = 0;
            for (int start = end; start < name_length; start = end)
            {
                int length;
                for (length = 0;
                     end < name_length && name[end] != U_SLASH;
                     length++, end++)
                {
                    directory_name[length] = name[end];
                }

                if (length != 1 || directory_name[0] != U_DOT)
                {
                    // Not the current directory.
                    if (length == 2 && directory_name[0] == U_DOT &&
                        directory_name[1] == U_DOT)
                    {
                        // keep the current directory
                        if (directory_symbol -> Identity() == dot_name_symbol ||
                            directory_symbol -> Identity() == dot_dot_name_symbol)
                        {
                            DirectorySymbol* subdirectory_symbol =
                                directory_symbol -> FindDirectorySymbol(dot_dot_name_symbol);
                            if (! subdirectory_symbol)
                                subdirectory_symbol =
                                    directory_symbol -> InsertDirectorySymbol(dot_dot_name_symbol,
                                                                              source_dir);
                            directory_symbol = subdirectory_symbol;
                        }
                        else directory_symbol = directory_symbol -> owner -> DirectoryCast();
                    }
                    else
                    {
                        NameSymbol* name_symbol =
                            FindOrInsertName(directory_name, length);
                        DirectorySymbol* subdirectory_symbol =
                            directory_symbol -> FindDirectorySymbol(name_symbol);
                        if (! subdirectory_symbol)
                            subdirectory_symbol =
                                directory_symbol -> InsertDirectorySymbol(name_symbol,
                                                                          source_dir);
                        directory_symbol = subdirectory_symbol;
                    }
                }

                for (end++;
                     end < name_length && name[end] == U_SLASH;
                     end++); // skip all extra '/'
            }

            //
            // Insert the new directory into the system table to avoid
            // duplicates, in case the same directory is specified with
            // a different name.
            //
            if (directory_symbol !=
                classpath[dot_classpath_index] -> RootDirectory())
            {
                // Not the dot directory.
                system_table -> InsertDirectorySymbol(status.st_dev,
                                                      status.st_ino,
                                                      directory_symbol);
                directory_symbol -> ReadDirectory();
            }

            delete [] directory_name;
            delete [] name;
        }
    }

    delete [] input_name;
    return directory_symbol;
}
#elif defined(WIN32_FILE_SYSTEM)
DirectorySymbol* Control::ProcessSubdirectories(wchar_t* source_name,
                                                int source_name_length,
                                                bool source_dir)
{
    DirectorySymbol* directory_symbol =
        classpath[dot_classpath_index] -> RootDirectory();

    int name_length = (source_name_length < 0 ? 0 : source_name_length);
    wchar_t* name = new wchar_t[name_length + 1];
    char* input_name = new char[name_length + 1];
    for (int i = 0; i < name_length; i++)
        input_name[i] = name[i] = source_name[i];
    input_name[name_length] = name[name_length] = U_NULL;

    if (name_length >= 2 && Case::IsAsciiAlpha(input_name[0]) &&
        input_name[1] == U_COLON) // a disk was specified
    {
        char disk = input_name[0];
        option.SaveCurrentDirectoryOnDisk(disk);
        if (SetCurrentDirectory(input_name))
        {
            // First, get the right size.
            DWORD directory_length = GetCurrentDirectory(0, input_name);
            char* full_directory_name = new char[directory_length + 1];
            DWORD length = GetCurrentDirectory(directory_length, full_directory_name);
            if (length <= directory_length)
            {
                // Turn '\' to '/'.
                for (char* ptr = full_directory_name; *ptr; ptr++)
                    *ptr = (*ptr != U_BACKSLASH ? *ptr : (char) U_SLASH);

                char* current_directory = option.GetMainCurrentDirectory();
                int prefix_length = strlen(current_directory);
                int start = (prefix_length <= (int) length &&
                             Case::StringSegmentEqual(current_directory,
                                                      full_directory_name,
                                                      prefix_length) &&
                             (full_directory_name[prefix_length] == U_SLASH ||
                              full_directory_name[prefix_length] == U_NULL)
                             ? prefix_length + 1
                             : 0);

                if (start > (int) length)
                    name_length = 0;
                else if (start <= (int) length) // note that we can assert that (start != length)
                {
                    delete [] name;
                    name_length = length - start;
                    name = new wchar_t[name_length + 1];
                    for (int k = 0, i = start; i < (int) length; i++, k++)
                        name[k] = full_directory_name[i];
                    name[name_length] = U_NULL;
                }
            }

            delete [] full_directory_name;
        }

        // Reset the current directory on this disk.
        option.ResetCurrentDirectoryOnDisk(disk);
        option.SetMainCurrentDirectory(); // Reset the real current directory.
    }

    int end;
    if (name_length > 2 && Case::IsAsciiAlpha(name[0]) &&
        name[1] == U_COLON && name[2] == U_SLASH)
    {
        end = 3;
    }
    else
    {
        for (end = 0;
             end < name_length && name[end] == U_SLASH;
             end++); // keep all extra leading '/'
    }

    wchar_t* directory_name = new wchar_t[name_length];
    int length;
    if (end > 0)
    {
        for (length = 0; length < end; length++)
            directory_name[length] = name[length];
        NameSymbol* name_symbol = FindOrInsertName(directory_name, length);
        DirectorySymbol* subdirectory_symbol =
            directory_symbol -> FindDirectorySymbol(name_symbol);
        if (! subdirectory_symbol)
            subdirectory_symbol =
                directory_symbol -> InsertDirectorySymbol(name_symbol,
                                                          source_dir);
        directory_symbol = subdirectory_symbol;
    }

    for (int start = end; start < name_length; start = end)
    {
        for (length = 0;
             end < name_length && name[end] != U_SLASH;
             length++, end++)
        {
            directory_name[length] = name[end];
        }

        if (length != 1 || directory_name[0] != U_DOT)
        {
            // Not the current directory.
            if (length == 2 && directory_name[0] == U_DOT &&
                directory_name[1] == U_DOT)
            {
                // Keep the current directory.
                if (directory_symbol -> Identity() == dot_name_symbol ||
                    directory_symbol -> Identity() == dot_dot_name_symbol)
                {
                    DirectorySymbol* subdirectory_symbol =
                        directory_symbol -> FindDirectorySymbol(dot_dot_name_symbol);
                    if (! subdirectory_symbol)
                        subdirectory_symbol =
                            directory_symbol -> InsertDirectorySymbol(dot_dot_name_symbol,
                                                                      source_dir);
                    directory_symbol = subdirectory_symbol;
                }
                else directory_symbol = directory_symbol -> owner -> DirectoryCast();
            }
            else
            {
                NameSymbol* name_symbol = FindOrInsertName(directory_name,
                                                           length);
                DirectorySymbol* subdirectory_symbol =
                    directory_symbol -> FindDirectorySymbol(name_symbol);
                if (! subdirectory_symbol)
                    subdirectory_symbol =
                        directory_symbol -> InsertDirectorySymbol(name_symbol,
                                                                  source_dir);
                directory_symbol = subdirectory_symbol;
            }
        }

        for (end++;
             end < name_length && name[end] == U_SLASH;
             end++); // skip all extra '/'
    }

    directory_symbol -> ReadDirectory();

    delete [] directory_name;
    delete [] name;
    delete [] input_name;
    return directory_symbol;
}
#endif // WIN32_FILE_SYSTEM


void Control::ProcessNewInputFiles(SymbolSet& file_set, char** arguments)
{
    unsigned i;
    for (i = 0; i < bad_input_filenames.Length(); i++)
        delete [] bad_input_filenames[i];
    bad_input_filenames.Reset();
    for (i = 0; i < unreadable_input_filenames.Length(); i++)
        delete [] unreadable_input_filenames[i];
    unreadable_input_filenames.Reset();

    //
    // Process all file names specified in command line. By this point, only
    // filenames should remain in arguments - constructing the Option should
    // have filtered out all options and expanded @files.
    //
    if (arguments)
    {
        int j = 0;
        while (arguments[j])
        {
            char* file_name = arguments[j++];
            unsigned file_name_length = strlen(file_name);

            wchar_t* name = new wchar_t[file_name_length + 1];
            for (unsigned i = 0; i < file_name_length; i++)
                name[i] = (file_name[i] != U_BACKSLASH ? file_name[i]
                           : (wchar_t) U_SLASH); // Change '\' to '/'.
            name[file_name_length] = U_NULL;

            //
            // File must be of the form xxx.java where xxx is a
            // character string consisting of at least one character.
            //
            if (file_name_length < FileSymbol::java_suffix_length ||
                (! FileSymbol::IsJavaSuffix(&file_name[file_name_length - FileSymbol::java_suffix_length])))
            {
                bad_input_filenames.Next() = name;
            }
            else
            {
                FileSymbol* file_symbol =
                    FindOrInsertJavaInputFile(name,
                                              file_name_length - FileSymbol::java_suffix_length);

                if (! file_symbol)
                    unreadable_input_filenames.Next() = name;
                else
                {
                    delete [] name;
                    file_set.AddElement(file_symbol);
                }
            }
        }
    }
}


FileSymbol* Control::FindOrInsertJavaInputFile(DirectorySymbol* directory_symbol,
                                               NameSymbol* file_name_symbol)
{
    FileSymbol* file_symbol = NULL;

    int length = file_name_symbol -> Utf8NameLength() +
        FileSymbol::java_suffix_length;
    char* java_name = new char[length + 1]; // +1 for \0
    strcpy(java_name, file_name_symbol -> Utf8Name());
    strcat(java_name, FileSymbol::java_suffix);

    DirectoryEntry* entry = directory_symbol -> FindEntry(java_name, length);
    if (entry)
    {
        file_symbol = directory_symbol -> FindFileSymbol(file_name_symbol);

        if (! file_symbol)
        {
            file_symbol =
                directory_symbol -> InsertFileSymbol(file_name_symbol);
            file_symbol -> directory_symbol = directory_symbol;
            file_symbol -> SetJava();
        }

        file_symbol -> mtime = entry -> Mtime();
    }

    delete [] java_name;
    return file_symbol;
}


FileSymbol* Control::FindOrInsertJavaInputFile(wchar_t* name, int name_length)
{
    FileSymbol* file_symbol = NULL;

    //
    // The name has been preprocessed so that if it contains any
    // slashes it is a forward slash. In the loop below we look
    // for the occurrence of the first slash (if any) that separates
    // the file name from its directory name.
    //
    DirectorySymbol* directory_symbol;
    NameSymbol* file_name_symbol;
#ifdef UNIX_FILE_SYSTEM
    int len;
    for (len = name_length - 1; len >= 0 && name[len] != U_SLASH; len--)
        ;
    directory_symbol = ProcessSubdirectories(name, len, true);
    file_name_symbol = FindOrInsertName(&name[len + 1],
                                        name_length - (len + 1));
#elif defined(WIN32_FILE_SYSTEM)
    int len;
    for (len = name_length - 1;
         len >= 0 && name[len] != U_SLASH && name[len] != U_COLON;
         len--);

    directory_symbol = ProcessSubdirectories(name,
                                             (name[len] == U_COLON ? len + 1
                                              : len),
                                             true);
    file_name_symbol = FindOrInsertName(&name[len + 1],
                                        name_length - (len + 1));
#endif // WIN32_FILE_SYSTEM

    for (unsigned i = 1; i < classpath.Length(); i++)
    {
        if (i == dot_classpath_index) // the current directory (.).
        {
            file_symbol = FindOrInsertJavaInputFile(directory_symbol,
                                                    file_name_symbol);
            if (file_symbol)
                break;
        }
        else if (classpath[i] -> IsZip())
        {
            DirectorySymbol* directory_symbol = FindSubdirectory(classpath[i],
                                                                 name, len);
            if (directory_symbol)
            {
                file_symbol =
                    directory_symbol -> FindFileSymbol(file_name_symbol);
                if (file_symbol && file_symbol -> IsJava())
                     break;
                else file_symbol = NULL;
            }
        }
    }

    //
    // If the file was found, return it; otherwise, in case the (.) directory
    // was not specified in the classpath, search for the file in it...
    //
    return file_symbol ? file_symbol
        : FindOrInsertJavaInputFile(directory_symbol, file_name_symbol);
}


PackageSymbol* Control::FindOrInsertPackage(LexStream* lex_stream,
                                            AstName* name)
{
    PackageSymbol* package;

    if (name -> base_opt)
    {
        package = FindOrInsertPackage(lex_stream, name -> base_opt);
        NameSymbol* name_symbol =
            lex_stream -> NameSymbol(name -> identifier_token);
        PackageSymbol* subpackage = package -> FindPackageSymbol(name_symbol);
        if (! subpackage)
            subpackage = package -> InsertPackageSymbol(name_symbol);
        package = subpackage;
    }
    else
    {
        NameSymbol* name_symbol =
            lex_stream -> NameSymbol(name -> identifier_token);
        package = external_table.FindPackageSymbol(name_symbol);
        if (! package)
            package = external_table.InsertPackageSymbol(name_symbol, NULL);
    }

    FindPathsToDirectory(package);
    return package;
}


void Control::ProcessFile(FileSymbol* file_symbol, StoragePool* ast_pool)
{
    ProcessHeaders(file_symbol);

    //
    // As long as there are new bodies, ...
    //
    for (unsigned i = 0; i < needs_body_work.Length(); i++)
    {
        assert(semantic.Length() == 0);

        //
        // These bodies are not necessarily in file_symbol; they
        // might be in another FileSymbol used by file_symbol.
        //
        ProcessBodies(needs_body_work[i], ast_pool);
    }
    needs_body_work.Reset();
}


void Control::ProcessHeaders(FileSymbol* file_symbol)
{
    if (file_symbol -> semantic)
        return;
    input_java_file_set.AddElement(file_symbol);

    bool initial_invocation = (semantic.Length() == 0);

    if (option.verbose)
    {
        Coutput << "[read "
                << file_symbol -> FileName()
                << "]" << endl;
    }

    if (! file_symbol -> lex_stream)
         scanner -> Scan(file_symbol);
    else file_symbol -> lex_stream -> Reset();

    if (file_symbol -> lex_stream) // do we have a successful scan!
    {
        if (! file_symbol -> compilation_unit)
            file_symbol -> compilation_unit =
                parser -> HeaderParse(file_symbol -> lex_stream);
        //
        // If we have a compilation unit, analyze it, process its types.
        //
        if (file_symbol -> compilation_unit)
        {
            assert(! file_symbol -> semantic);

            if (! file_symbol -> package)
                ProcessPackageDeclaration(file_symbol,
                                          file_symbol -> compilation_unit -> package_declaration_opt);
            file_symbol -> semantic = new Semantic(*this, file_symbol);
            semantic.Next() = file_symbol -> semantic;
            file_symbol -> semantic -> ProcessTypeNames();
        }
    }

    if (initial_invocation)
        ProcessMembers();
}


void Control::ProcessMembers()
{
    Tuple<TypeSymbol*> partially_ordered_types(1024);
    SymbolSet needs_member_work(101);
    TypeCycleChecker cycle_checker(partially_ordered_types);
    TopologicalSort topological_sorter(needs_member_work,
                                       partially_ordered_types);

    unsigned start = 0;
    while (start < semantic.Length())
    {
        needs_member_work.SetEmpty();

        do
        {
            //
            // Check whether or not there are cycles in this new batch of
            // types. Create a partial order of the types (cycles are ordered
            // arbitrarily) and place the result in partially_ordered_types.
            //
            cycle_checker.PartialOrder(semantic, start);
            start = semantic.Length(); // next starting point

            //
            // Process the extends and implements clauses.
            //
            for (unsigned j = 0; j < partially_ordered_types.Length(); j++)
            {
                TypeSymbol* type = partially_ordered_types[j];
                needs_member_work.AddElement(type);
                type -> ProcessTypeHeaders();
                type -> semantic_environment -> sem ->
                    types_to_be_processed.AddElement(type);
            }
        } while (start < semantic.Length());

        //
        // Partially order the collection of types in needs_member_work and
        // place the result in partially_ordered_types. This reordering is
        // based on the complete "supertype" information computed in
        // ProcessTypeHeaders.
        //
        topological_sorter.Sort();
        for (unsigned i = 0; i < partially_ordered_types.Length(); i++)
        {
            TypeSymbol* type = partially_ordered_types[i];
            needs_body_work.Next() = type;
            type -> ProcessMembers();
        }
    }

    semantic.Reset();
}


void Control::CollectTypes(TypeSymbol* type, Tuple<TypeSymbol*>& types)
{
    types.Next() = type;

    for (unsigned j = 0; j < type -> NumAnonymousTypes(); j++)
        CollectTypes(type -> AnonymousType(j), types);

    if (type -> local)
    {
        for (TypeSymbol* local_type = (TypeSymbol*) type -> local -> FirstElement();
             local_type;
             local_type = (TypeSymbol*) type -> local -> NextElement())
        {
            CollectTypes(local_type, types);
        }
    }

    if (type -> non_local)
    {
        for (TypeSymbol* non_local_type = (TypeSymbol*) type -> non_local -> FirstElement();
             non_local_type;
             non_local_type = (TypeSymbol*) type -> non_local -> NextElement())
        {
            CollectTypes(non_local_type, types);
        }
    }
}


void Control::ProcessBodies(TypeSymbol* type, StoragePool* ast_pool)
{
    Semantic* sem = type -> semantic_environment -> sem;

    if (type -> declaration &&
        ! sem -> compilation_unit -> BadCompilationUnitCast())
    {
#ifdef WIN32_FILE_SYSTEM
        if (! type -> file_symbol -> IsZip())
        {
            int length = type -> Utf8NameLength() +
                FileSymbol::class_suffix_length;
            char* classfile_name = new char[length + 1]; // +1 for "\0"
            strcpy(classfile_name, type -> Utf8Name());
            strcat(classfile_name, FileSymbol::class_suffix);

            DirectorySymbol* directory =
                type -> file_symbol -> OutputDirectory();
            DirectoryEntry* entry =
                directory -> FindCaseInsensitiveEntry(classfile_name, length);

            //
            // If an entry is found and it is not identical (in a
            // case-sensitive test) to the name of the type, issue an
            // appropriate message.
            //
            if (entry && strcmp(classfile_name, entry -> name) != 0)
            {
                wchar_t* entry_name = new wchar_t[entry -> length + 1];
                for (int i = 0; i < length; i++)
                    entry_name[i] = entry -> name[i];
                entry_name[entry -> length] = U_NULL;
                sem -> ReportSemError(SemanticError::FILE_FILE_CONFLICT,
                                      type -> declaration -> identifier_token,
                                      type -> Name(), entry_name,
                                      directory -> Name());
                delete [] entry_name;
            }
            delete [] classfile_name;
        }
#endif // WIN32_FILE_SYSTEM

        if (! parser -> InitializerParse(sem -> lex_stream,
                                         type -> declaration))
        {
            // Mark that syntax errors were detected.
            sem -> compilation_unit -> MarkBad();
        }
        else
        {
            type -> CompleteSymbolTable();
            if (! parser -> BodyParse(sem -> lex_stream, type -> declaration))
            {
                // Mark that syntax errors were detected.
                sem -> compilation_unit -> MarkBad();
            }
            else type -> ProcessExecutableBodies();
        }

        if (sem -> NumErrors() == 0 &&
            sem -> lex_stream -> NumBadTokens() == 0 &&
            ! sem -> compilation_unit -> BadCompilationUnitCast())
        {
            Tuple<TypeSymbol*>* types = new Tuple<TypeSymbol*>(1024);
            CollectTypes(type, *types);

	     //
	     // Get Structural info for pattern detection.
	     //
	     for (unsigned k = 0; k < types -> Length(); k++)
	     {
	     		TypeSymbol *type = (*types)[k];			
	      		ExtractStructure(w_table, r_table, d_table, cs_table, mb_table, ms_table, gen_table, assoc_table, type, ast_pool);
	     }

            //
            // If we are supposed to generate code, do so now !!!
            //
            if (option.bytecode)
            {
                for (unsigned k = 0; k < types -> Length(); k++)
                {
                    TypeSymbol* type = (*types)[k];
                    // Make sure the literal is available for bytecode.
                    type -> file_symbol -> SetFileNameLiteral(this);
                    ByteCode* code = new ByteCode(type);
                    code -> GenerateCode();
                    delete code;
                }
            }

            //
            // If no error was detected while generating code, then
            // start cleaning up.
            //
            if (! option.nocleanup)
            {
                if (sem -> NumErrors() == 0)
                {
                    for (unsigned k = 0; k < types -> Length(); k++)
                    {
                        TypeSymbol* type = (*types)[k];
                        delete type -> semantic_environment;
                        type -> semantic_environment = NULL;
                        type -> declaration -> semantic_environment = NULL;
                    }
                }
                delete types;
            }
        }
    }

    sem -> types_to_be_processed.RemoveElement(type);

    if (sem -> types_to_be_processed.Size() == 0)
    {
        // All types belonging to this compilation unit have been processed.
        CheckForUnusedImports(sem);
        if (! option.nocleanup)
        {
            CleanUp(sem -> source_file_symbol);
        }
    }
}

void Control::CheckForUnusedImports(Semantic* sem)
{
    if (sem -> NumErrors() != 0 ||
        sem -> lex_stream -> NumBadTokens() != 0 ||
        sem -> compilation_unit -> BadCompilationUnitCast())
    {
        //
        // It's not worth checking for unused imports if compilation
        // wasn't successful; we may well have just not got round to
        // compiling the relevant code, and if there were errors, the
        // user has more important things to worry about than unused
        // imports!
        //
        return;
    }

    for (unsigned i = 0;
         i < sem -> compilation_unit -> NumImportDeclarations(); ++i)
    {
        AstImportDeclaration* import_declaration =
            sem -> compilation_unit -> ImportDeclaration(i);
        Symbol* symbol = import_declaration -> name -> symbol;
        if (import_declaration -> star_token_opt)
        {
            PackageSymbol* package = symbol -> PackageCast();
            if (package &&
                ! sem -> referenced_package_imports.IsElement(package))
            {
                sem -> ReportSemError(SemanticError::UNUSED_PACKAGE_IMPORT,
                                      import_declaration,
                                      package -> PackageName());
            }
        }
        else
        {
            TypeSymbol* import_type = symbol -> TypeCast();
            if (import_type &&
                ! sem -> referenced_type_imports.IsElement(import_type))
            {
                sem -> ReportSemError(SemanticError::UNUSED_TYPE_IMPORT,
                                      import_declaration,
                                      import_type -> ContainingPackage() -> PackageName(),
                                      import_type -> ExternalName());
            }
        }
    }
}

//
// Introduce the main package and the current package.
// This procedure is invoked directly only while doing
// an incremental compilation.
//
void Control::ProcessPackageDeclaration(FileSymbol* file_symbol,
                                        AstPackageDeclaration* package_declaration)
{
    file_symbol -> package = (package_declaration
                              ? FindOrInsertPackage(file_symbol -> lex_stream,
                                                    package_declaration -> name)
                              : unnamed_package);

    for (unsigned i = 0; i < file_symbol -> lex_stream -> NumTypes(); i++)
    {
        TokenIndex identifier_token = file_symbol -> lex_stream ->
            Next(file_symbol -> lex_stream -> Type(i));
        if (file_symbol -> lex_stream -> Kind(identifier_token) ==
            TK_Identifier)
        {
            NameSymbol* name_symbol =
                file_symbol -> lex_stream -> NameSymbol(identifier_token);
            if (! file_symbol -> package -> FindTypeSymbol(name_symbol))
            {
                TypeSymbol* type = file_symbol -> package ->
                    InsertOuterTypeSymbol(name_symbol);
                type -> file_symbol = file_symbol;
                type -> outermost_type = type;
                type -> supertypes_closure = new SymbolSet;
                type -> subtypes = new SymbolSet;
                type -> SetOwner(file_symbol -> package);
                type -> SetSignature(*this);
                type -> MarkSourcePending();

                //
                // If this type is contained in the unnamed package add it to
                // the set unnamed_package_types if a type of similar name was
                // not already there.
                //
                if (! package_declaration &&
                    unnamed_package_types.Image(type -> Identity()) == NULL)
                {
                    unnamed_package_types.AddElement(type);
                }
            }
        }
    }
}


void Control::CleanUp(FileSymbol* file_symbol)
{
    Semantic* sem = file_symbol -> semantic;

    if (sem)
    {
#ifdef JIKES_DEBUG
        if (option.debug_dump_lex)
        {
            sem -> lex_stream -> Reset(); // rewind input and ...
            sem -> lex_stream -> Dump();  // dump it!
        }
        if (option.debug_dump_ast)
            sem -> compilation_unit -> Print(*sem -> lex_stream);
        if (option.debug_unparse_ast)
        {
            if (option.debug_unparse_ast_debug)
              {
                // which of these is correct?
                sem -> compilation_unit -> debug_unparse = true;
                Ast::debug_unparse = true;
              }
            sem -> compilation_unit -> Unparse(sem -> lex_stream,
                                               "unparsed/");
        }
#endif // JIKES_DEBUG
        //sem -> PrintMessages();
        if (sem -> return_code > 0)
            return_code = 1;

        file_symbol -> CleanUp();
    }
}

 
#ifdef HAVE_JIKES_NAMESPACE
} // Close namespace Jikes block
#endif

