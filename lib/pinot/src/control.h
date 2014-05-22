// $Id: control.h,v 1.31 2006/03/14 02:37:19 shini Exp $ -*- c++ -*-
//
// This software is subject to the terms of the IBM Jikes Compiler
// License Agreement available at the following URL:
// http://ibm.com/developerworks/opensource/jikes.
// Copyright (C) 1996, 2004 IBM Corporation and others.  All Rights Reserved.
// You must accept the terms of that agreement to use this software.
//

#ifndef control_INCLUDED
#define control_INCLUDED

#include "platform.h"
#include "symbol.h"
#include "tuple.h"
#include "set.h"

#ifdef HAVE_JIKES_NAMESPACE
namespace Jikes { // Open namespace Jikes block
#endif

class StoragePool;
class Option;
class Scanner;
class Parser;
class Semantic;
class LexStream;
class AstPackageDeclaration;
class AstMethodDeclaration;
class AstDeclared;
class AstName;
class AstExpression;
class AstBinaryExpression;
class TypeDependenceChecker;
class AstTryStatement;
class AstStatement;
class AstMethodInvocation;
class AstAssignmentExpression;
class AstClassCreationExpression;
class AstIfStatement;
class AstWhileStatement;
class AstForStatement;
class AstReturnStatement;
class AstSynchronizedStatement;
class AstConditionalExpression;
class AstLocalVariableStatement;
class AstArrayAccess;

//AstBinaryExpression* NewBinaryExpression(AstBinaryExpression::BinaryExpressionTag tag);
//
// GoF IR declarations.
//

#include <vector>
#include <map>
#include <set>

class CreationAnalysis
{
	public:
		CreationAnalysis(){}
		void visit(AstBlock*);
		void visit(AstClassCreationExpression*);
		~CreationAnalysis(){}
		
		vector<TypeSymbol*> return_types;
	private:
		SymbolSet cache; // cache visited method symbols.
};

class ControlAnalysis
{
	private:
		AstExpression *expression;
	public:
		ControlAnalysis(AstExpression *e)
			: expression (e), flag(false), cond(0), containing_stmt(0), result (false) {}
		void visit(AstBlock*);
		void visit(AstIfStatement*);
		void visit(AstWhileStatement*);
		void visit(AstForStatement*);
		void visit(AstStatement*);
		void visit(AstExpression*);
		void visit(AstSynchronizedStatement*);
		void visit(AstConditionalExpression*);
		bool IsConditional();
		bool IsRepeated();
		bool IsSynchronized();		
		~ControlAnalysis(){}
		bool flag;
		AstExpression* cond;
		Ast* containing_stmt;
		bool result;
		vector<Ast*> rt_stack;
};

class Role
{
	friend class FlyweightAnalysis;
	friend class Snapshot;
	friend class Flatten;
	private:
		enum RoleTag
		{
			CREATE,
			REGISTER,
			RETRIEVE,
			ALLOCATE,
			RETURN,
			NIL
		};
	public:
		Role(AstArrayAccess *v, RoleTag t):array_access(v),tag(t){vsym = NULL;}
		Role(VariableSymbol *v, RoleTag t):vsym(v),tag(t){array_access = NULL;}
	private:
		VariableSymbol *vsym;
		AstArrayAccess *array_access;
		RoleTag tag;
		char *TagName();
};
class Snapshot
{
	friend class SingletonAnalysis;
	friend class FlyweightAnalysis;
	friend class FactoryAnalysis;
	friend class ChainAnalysis;
	friend class Flatten;
	public:
		Snapshot():statements(NULL),conditions(NULL),condition(NULL),roles(NULL),index(0), number(-1){}
	private:
		vector<Ast*> *statements;
		vector<AstExpression*> *conditions;
		AstExpression *condition;
		vector<Role*> *roles;
		signed index; // for IsFlyweightFactory
		signed number;
		set<signed> previous, next;
};
class Flatten
{
	friend class SingletonAnalysis;
	friend class FactoryAnalysis;
	friend class ChainAnalysis;
	private:
		vector<Ast*> statements;
		vector<AstExpression*> conditions;
		AstExpression *condition;
		vector<Snapshot*> summary;
		vector<Snapshot*> traces;
		MethodSymbol *method;
		bool capture_trace;
		StoragePool *ast_pool;
		set<signed> pred;
		bool multi_if;
	public:
		enum TransitionTag
		{
			UNCONDITIONAL,
			CONDITIONAL,
			NOTRANSITION
		};
		Flatten():condition(NULL), capture_trace(false), multi_if(false){ pred.insert(-1); }
		Flatten(MethodSymbol *msym, StoragePool *pool):condition(NULL),method(msym),capture_trace(false),ast_pool(pool), multi_if(false){ pred.insert(-1); }
		~Flatten(){}
		void init(MethodSymbol *msym, StoragePool *pool){ method = msym; ast_pool = pool; }
		void BuildSummary();
		void UpdateSummary();
		void DumpSummary();
		void PushCondition(AstExpression*);
		void PopCondition();
		bool Compare(AstExpression *, AstExpression *);
		void FlattenBoolean(vector<AstExpression*>&, AstExpression*);
		TransitionTag TransitionFlow(AstExpression *, AstExpression *);
		void visit(AstBlock*);
		void visit(AstIfStatement*);
		void visit(AstWhileStatement*);
		void visit(AstForStatement*);
		void visit(AstTryStatement*);
		void visit(AstStatement*);
		void visit(AstExpression*);
		void visit(AstAssignmentExpression*);
		void visit(AstLocalVariableStatement*);
		void visit(AstVariableDeclarator*);
		void visit(AstReturnStatement*);
		void visit(AstMethodInvocation*);
		void visit(AstSynchronizedStatement*);
		void visit(AstConditionalExpression*);
};

class ContainerType
{
	public:
		enum ContainerKind
		{
			ARRAY,
			MAP,
			COLLECTION,
			ARRAYLIST,
			HASHSET,
			LINKEDHASHSET,
			LINKEDLIST,
			TREESET,
			VECTOR
		};
		ContainerType(ContainerKind k, VariableSymbol *v):kind(k),container(v){}
		virtual bool IsGetMethod(MethodSymbol*){ return false; }
		virtual bool IsPutMethod(MethodSymbol*){ return false; }
		virtual VariableSymbol *GetPutValue(AstMethodInvocation*){ return NULL; }
		virtual TypeSymbol *GetPutType(AstMethodInvocation*){ return NULL; }
		virtual VariableSymbol *GetContainer(){ return container; }
		ContainerKind kind;
	private:
		VariableSymbol *container;
};
class MapContainer : public ContainerType
{
	public:
		MapContainer(VariableSymbol *v):ContainerType(MAP, v){}
		bool IsGetMethod(MethodSymbol*);
		bool IsPutMethod(MethodSymbol*);
		VariableSymbol *GetPutValue(AstMethodInvocation*);
		TypeSymbol *GetPutType(AstMethodInvocation*);
		//VariableSymbol *GetContainer(){ return container; }
};
class ArrayContainer : public ContainerType
{
	public:
		ArrayContainer(VariableSymbol *array_symbol):ContainerType(ARRAY, array_symbol){}
		//VariableSymbol *GetContainer(){ return container; }
};
class ArrayListContainer : public ContainerType
{
	public:
		ArrayListContainer(VariableSymbol *v):ContainerType(ARRAYLIST, v){}
		bool IsPutMethod(MethodSymbol*);
		VariableSymbol *GetPutValue(AstMethodInvocation*);
		TypeSymbol *GetPutType(AstMethodInvocation*);
};
class HashSetContainer : public ContainerType
{
	public:
		HashSetContainer(VariableSymbol *v):ContainerType(HASHSET, v){}
		bool IsPutMethod(MethodSymbol*);
		VariableSymbol *GetPutValue(AstMethodInvocation*);
		TypeSymbol *GetPutType(AstMethodInvocation*);
};
class LinkedHashSetContainer : public ContainerType
{
	public:
		LinkedHashSetContainer(VariableSymbol *v):ContainerType(LINKEDHASHSET, v){}
};
class LinkedListContainer : public ContainerType
{
	public:
		LinkedListContainer(VariableSymbol *v):ContainerType(LINKEDLIST, v){}
		bool IsPutMethod(MethodSymbol*);
		VariableSymbol *GetPutValue(AstMethodInvocation*);
		TypeSymbol *GetPutType(AstMethodInvocation*);
};
class TreeSetContainer : public ContainerType
{
	public:
		TreeSetContainer(VariableSymbol *v):ContainerType(TREESET, v){}
};
class VectorContainer : public ContainerType
{
	public:
		VectorContainer(VariableSymbol *v):ContainerType(VECTOR, v){}
		bool IsPutMethod(MethodSymbol*);
		VariableSymbol *GetPutValue(AstMethodInvocation*);
		TypeSymbol *GetPutType(AstMethodInvocation*);
};
class CollectionContainer : public ContainerType
{
	public:
		CollectionContainer(VariableSymbol *v):ContainerType(COLLECTION, v){}
		bool IsGetMethod(MethodSymbol*){ return false; }
		bool IsPutMethod(MethodSymbol*);
		bool IsPutType(AstMethodInvocation*, TypeSymbol*);
		VariableSymbol *GetPutValue(AstMethodInvocation*);
		TypeSymbol *GetPutType(AstMethodInvocation *);
		//VariableSymbol *GetContainer(){ return container; }
};
class Utility
{
	public:
		static ContainerType *IdentifyContainerType(VariableSymbol*);
		static void RemoveJavaBaseClass(SymbolSet&);
		static TypeSymbol *GetTypeSymbol(Symbol*);
		static void RemoveBuiltinInterfaces(SymbolSet&);
		static AstExpression *RemoveCasting(AstExpression*);
		static void Intersection(vector<signed>&, vector<signed>&, vector<signed>&);
		static void RemoveDuplicates(vector<signed>&);
		static bool Aliasing(VariableSymbol*, VariableSymbol*);
};
class SingletonAnalysis
{
	private:
		VariableSymbol *variable;
		MethodSymbol *method;
		Flatten flatten;
		static SymbolSet visited; // visited methods, avoid analysis on recursion methods.
		StoragePool *ast_pool;
		vector<vector<signed> > exec_paths;
		vector<signed> path;
		vector<signed> footprints; // point of creating the Singleton instance
		vector<signed> fingerprints; // point of returning the Singleton instance
	public:
		SingletonAnalysis(VariableSymbol *vsym, MethodSymbol *msym, StoragePool *pool)
			:variable(vsym),method(msym), ast_pool(pool){ flatten.init(msym, pool); }
		void TracePath(Snapshot*);
		bool ReturnsSingleton();
		bool ReturnsSingleton1();
		void CleanUp() { visited.SetEmpty(); }
		~SingletonAnalysis(){}	
};

class ChainAnalysis
{
	private:
		VariableSymbol *variable;
		MethodSymbol *method;
		Flatten flatten;
		//static SymbolSet visited; // visited methods, avoid analysis on recursion methods.
		StoragePool *ast_pool;
		vector<vector<signed> > paths;
		vector<signed> path;
		vector<signed> footprints;
	public:
		enum ResultTag
		{
			CoR,
			DECORATOR,
			NONE
		};
		ChainAnalysis(VariableSymbol *vsym, MethodSymbol *msym, StoragePool *pool)
			:variable(vsym),method(msym), ast_pool(pool){ flatten.init(msym, pool); }
		void TracePath(Snapshot*);
		void TraceBinaryExpression(AstBinaryExpression*, Snapshot*);
		ResultTag AnalyzeCallChain();
		void CleanUp() { paths.clear(); /*visited.SetEmpty();*/ }
		~ChainAnalysis(){}	
};
class FactoryAnalysis
{
	private:
		MethodSymbol *method;
		Flatten flatten;
		static SymbolSet visited; // visited methods, avoid analysis on recursion methods.
		StoragePool *ast_pool;
	public:
		static SymbolSet types;
		FactoryAnalysis(MethodSymbol *msym, StoragePool *pool):method(msym), ast_pool(pool){ flatten.init(msym, pool); }
		bool IsFactoryMethod();
		bool IsCreationMethod();
		void CleanUp() { types.SetEmpty(); visited.SetEmpty(); }
		~FactoryAnalysis(){}	
};
class FlyweightAnalysis
{
	private:
		MethodSymbol *GetFlyweight;
		TypeSymbol *flyweight;
		SymbolSet key;
		ContainerType *container_type;
		vector<Ast*> statements;
		vector<AstExpression*> conditions;
		vector<Snapshot*> summary;
		vector<Snapshot*> traces;
		//SymbolSet vcache;
		char bitmap[10];
		int n;
		// include some kind of summary for the resulting flow analysis
		void AssignRoles();
	public:
		FlyweightAnalysis(MethodSymbol *GetMethod)
		{
			container_type = NULL;
			GetFlyweight = GetMethod;
			flyweight = GetFlyweight->Type();
			for (unsigned i = 0; i < GetFlyweight->NumFormalParameters(); i++)
			key.AddElement(GetFlyweight->FormalParameter(i));
			for (int i = 0; i < 10; i++) bitmap[i] = 'X';
			n = 0;
		}
		~FlyweightAnalysis(){delete container_type;}
		bool IsFlyweightFactory();
		void UpdateSummary();
		void DumpSummary();
		VariableSymbol *GetFlyweightPool() { return (container_type) ? container_type->GetContainer(): NULL;}
		void visit(AstBlock*);
		void visit(AstIfStatement*);
		void visit(AstWhileStatement*);
		void visit(AstForStatement*);
		void visit(AstTryStatement*);
		void visit(AstStatement*);
		void visit(AstExpression*);
		void visit(AstAssignmentExpression*);
		void visit(AstLocalVariableStatement*);
		void visit(AstVariableDeclarator*);
		void visit(AstReturnStatement*);
		void visit(AstMethodInvocation*);
		void visit(AstSynchronizedStatement*){}
		void visit(AstConditionalExpression*){}	
};

class DelegationEntry
{
	public:
		DelegationEntry(TypeSymbol *f, TypeSymbol *t, 
							AstExpression *b, VariableSymbol *v, MethodSymbol *m, 
							MethodSymbol *e, AstMethodInvocation *c)
			:from(f), to(t), base_opt(b), vsym(v), method(m), enclosing(e), call(c) {}
		~DelegationEntry();
		TypeSymbol *from;
		TypeSymbol *to;
		AstExpression *base_opt;
		VariableSymbol *vsym;
		MethodSymbol *method;
		MethodSymbol *enclosing;
		AstMethodInvocation *call;
};

class DelegationTable
{
	public:
		DelegationTable() { table = new vector<DelegationEntry*>(); }
		int UniqueDirectedCalls();
		void InsertDelegation(TypeSymbol*, TypeSymbol*, AstExpression*, VariableSymbol*, MethodSymbol*, MethodSymbol*, AstMethodInvocation*);
		bool TraceCall(MethodSymbol*, MethodSymbol*);
		DelegationEntry *Entry(int i) { return (*table)[i]; }
		MethodSymbol *Delegates(TypeSymbol*, TypeSymbol*);
		int size() { return table ->size(); }
		TypeSymbol *ResolveType(AstExpression*);
		bool DelegatesSuccessors(TypeSymbol*, TypeSymbol*);
		void ShowDelegations(TypeSymbol*, TypeSymbol*);
		int IsBidirectional(TypeSymbol*,TypeSymbol*);
		void DumpTable();
		void ConcretizeDelegations();
		~DelegationTable() { delete table; }
	private:
		vector<DelegationEntry*>*table;
};

class ReadAccessTable
{
	// Actually records every method that returns nonlocal vars.
	public:
		ReadAccessTable() { table = new multimap<VariableSymbol*, MethodSymbol*>(); }
		~ReadAccessTable() { delete table; }
		void InsertReadAccess(VariableSymbol* vsym, MethodSymbol *msym)
		{ 
			table -> insert(pair<VariableSymbol*, MethodSymbol*>(vsym, msym)); 
		}
		int size() { return table -> size(); }
		multimap<VariableSymbol*, MethodSymbol*>::iterator begin() { return table -> begin(); }
		multimap<VariableSymbol*, MethodSymbol*>::iterator end() { return table -> end(); }		
		void DumpTable();
	private:
		multimap<VariableSymbol*, MethodSymbol*> *table;
};

class WriteAccessTable
{
	public:
		WriteAccessTable() { table = new multimap<VariableSymbol*, MethodSymbol*>(); }
		~WriteAccessTable() { delete table; }
		void InsertWriteAccess(VariableSymbol* vsym, MethodSymbol *msym)
		{ 
			table -> insert(pair<VariableSymbol*, MethodSymbol*>(vsym, msym)); 
		}
		bool IsWrittenBy(VariableSymbol*, MethodSymbol*);
		bool IsWrittenBy(VariableSymbol*, MethodSymbol*, DelegationTable*);
		int size() { return table -> size(); }
		multimap<VariableSymbol*, MethodSymbol*>::iterator begin() { return table -> begin(); }
		multimap<VariableSymbol*, MethodSymbol*>::iterator end() { return table -> end(); }		
		void DumpTable();
	private:
		multimap<VariableSymbol*, MethodSymbol*> *table;
};

class ClassSymbolTable
{
	public:
		ClassSymbolTable() { table = new vector<TypeSymbol*>(); }
		void AddClassSymbol(TypeSymbol *);
		unsigned size() { return table -> size(); }
		int ConcreteClasses();
		//multimap<char*, TypeSymbol*>::iterator begin() { return table -> begin(); }
		//multimap<char*, TypeSymbol*>::iterator end() { return table -> end(); }		
		TypeSymbol* operator[] (unsigned i) { return (*table)[i]; }
		TypeSymbol *GetSymbol(char*, char*, char*);
		TypeSymbol *GetSymbol(wchar_t*); // to be eleiminated.
		bool HasSubclasses(TypeSymbol*);
		bool HasSubtypes(TypeSymbol*);
		void PrintSubclasses(TypeSymbol*);
		void PrintSubinterfaces(TypeSymbol*);
		void PrintSubtypes(TypeSymbol*);
		bool Converge(TypeSymbol*, TypeSymbol*);
		bool IsFamily(TypeSymbol*, TypeSymbol*);
		vector<TypeSymbol*> *GetAncestors(TypeSymbol*);
		void ExpandSubtypes();
		void PrintDeclaration(char*, char*);
		~ClassSymbolTable() { delete table; }
	private:
		vector<TypeSymbol*> *table;
};

class MethodSymbolTable
{
	public:
		MethodSymbolTable() { table = new vector<MethodSymbol*>(); }
		void AddMethodSymbol(MethodSymbol *);
		unsigned size() { return table -> size(); }
		vector<MethodSymbol*>::iterator begin() { return table->begin(); }
		vector<MethodSymbol*>::iterator end() { return table->end(); }
		MethodSymbol* operator[] (unsigned i) { return (*table)[i]; }
		MethodSymbol *GetSymbol(char*, char*, char*);
		Ast *GetAstDeclaration(wchar_t*, wchar_t*, wchar_t*); // to be eliminated.
		void PrintDeclaration(char*, char*, char*);
		void PrintBody(char*, char*, char*);
		void ExpandCallDependents();
		void ClearMarks();
		~MethodSymbolTable() { delete table; }
	private:
		//multimap<char*, MethodSymbol*> *table;
		vector<MethodSymbol*> *table;
};

class State
{
friend class Statechart;

public:
	enum StateKind
	{
		SET, // target being changed
		GET, // target being used/read
		CONDITION, // target in condition of if statement
		CREATE, // target being created
		RETURN // target begin returned
	};
	State(StateKind k, vector<wchar_t*> *p) : kind(k), true_branch(false), false_branch(false), participants(p) {}
	~State(){}
	void addTrueBranch(State* tb) {true_branch = tb;}
	void addFalseBranch(State* fb) {false_branch = fb;}
private:
	StateKind kind;	
	State *true_branch, *false_branch;
	vector<wchar_t*> *participants;	
};

class Statechart
{
public:
	Statechart() {statechart = new vector<State*>(); }
	void addState(State* s) {statechart -> push_back(s);}
	State::StateKind getStateKindAt(int i) {return (*statechart)[i] -> kind;}
	vector<wchar_t*> *getStateParticipantsAt(int i) {return (*statechart)[i] -> participants;}
	int getSize() { return statechart -> size(); }
	void Print();
	~Statechart() {}
private:
	vector<State*> *statechart;
};

class Env
{
friend class EnvTable;

public:
    	enum State
    	{
       	NIL, // variable is assigned null
        	INIT,  // variable is initialized with a value
        	MOD // variable's value changed through assignment statement 
    	};

     Env(wchar_t* v, State s) : state(s), var(v) {}

private:
	State state;
	wchar_t* var;		
};

class EnvTable
{
public:
	EnvTable()
	{
		table = new vector<Env*>();
	}
	void addEnvironment(wchar_t*, Env::State);
	Env::State getState(wchar_t*);
	void changeState(wchar_t*, Env::State);
	~EnvTable() { delete table; }
private:
	vector<Env*> *table;
};

class MethodBodyAddr
{
friend class MethodBodyTable;

public:
     MethodBodyAddr(wchar_t* pkg, wchar_t* cls, wchar_t* mtd, AstDeclared* ptr)
         : ast_location(ptr), package_name(pkg), class_name(cls), method_name(mtd) {}

     ~MethodBodyAddr();	
private:
     AstDeclared* ast_location;
     wchar_t* package_name;
     wchar_t* class_name;
     wchar_t* method_name;     
};

class MethodBodyTable
{
public:
    MethodBodyTable()
    {
        table = new vector<MethodBodyAddr*>();
    }
    ~MethodBodyTable() { delete table; }

    void addMethodBodyAddr(wchar_t*, wchar_t*, wchar_t*, AstDeclared*);
    wchar_t* getClassNameAt(int i) {return (*table )[i] -> class_name;}
    wchar_t* getMethodNameAt(int i) {return (*table )[i] -> method_name;}
    AstDeclared* getAstLocationAt(int i) {return (*table)[i] -> ast_location;}
    wchar_t* getPackageNameAt(int i) {return (*table)[i] -> package_name;}
    vector<wchar_t*>* getModifiersAt(int i);
    AstDeclared* getAstLocation(wchar_t*, wchar_t*);
    int getSize() {return table -> size();}
    void dumpTable();
private:
    vector<MethodBodyAddr*>* table;
};

class Gen
{
friend class GenTable;

public:

    enum Kind
    {
    	CLASS,
	INTERFACE,
	ABSTRACT,
	FINAL
    };
	
    Gen(wchar_t* pkg, wchar_t* cls, wchar_t* spr, vector<wchar_t*>* ifcs, Kind k, char* f)
        : class_name(cls)
        , super_name(spr)
        , interfaces(ifcs)
        , kind(k)
        , package_name(pkg)
        , file_name(f)
    {}
    ~Gen();
private:
    wchar_t* class_name;
    wchar_t* super_name;
    vector<wchar_t*>* interfaces;
    Kind kind;
    wchar_t* package_name;
    char* file_name;
};

class GenTable
{
public:
    enum Kind
    {
        SUBC, // Subclass
        IMPL  // Implmentor
    };

    GenTable()
    {
        table = new vector<Gen*>();
    }
    ~GenTable() { delete table; }

    void addGeneralization(wchar_t*, wchar_t*, wchar_t*, vector<wchar_t*>*, Gen::Kind, char*);
    wchar_t* getSuper(wchar_t*, wchar_t*);
    vector<wchar_t*>* getAncestors(Kind, wchar_t*, wchar_t*);
    vector<wchar_t*>* getInterfaces(wchar_t*, wchar_t*);
    vector<wchar_t*>* getSuccessors(wchar_t*, Kind);
    bool hasSuccessors(wchar_t*, Kind);
    Gen::Kind getKind(wchar_t*, wchar_t*);
    wchar_t* getClassNameAt(int i) {return (*table)[i] -> class_name; }
    wchar_t* getPackageNameAt(int i) {return (*table)[i] -> package_name; }
    Gen::Kind getKindAt(int i) {return (*table)[i] -> kind; }
    char* getFileName(wchar_t*, wchar_t*);
    int getSize() {return table -> size();}
    void dumpTable();
    
private:
    vector<Gen*>* table;
};

class Assoc
{
friend class AssocTable;

public:
    enum Kind
    {
        CF, // Class Field, assocates with a mode
        IM, // Instance Member, assocates with a mode
        MP, // Method Parameter, assicoates with a method
        MI, // Method Invocation, called method is in name, invoker is in type
        OC, // Object Creation, assicoates with a method. No name entry
        MR, // Method Return Type, associates with a method
        CM  // Class Method Return Type, associates with a method
    };

    enum Mode
    {
    	PRIVATE,
	PROTECTED,
	PUBLIC
    };

    Assoc(Kind k, Mode m, wchar_t* n, wchar_t* t, wchar_t* pkg, wchar_t* cls, wchar_t* mtd)
        : kind(k)
        , mode(m)
        , name(n)
        , type(t)
        , package_name(pkg)
        , class_name(cls)
        , method_name(mtd)        
    {}
    ~Assoc();
private:
    Kind kind;
    Mode mode;
    wchar_t* name;
    wchar_t* type;
    wchar_t* package_name;
    wchar_t* class_name;
    wchar_t* method_name;    
};

class AssocTable
{
public:
    AssocTable()
    {
        table = new vector<Assoc*>();
    }
    ~AssocTable() { delete table; }

    void addAssociation(Assoc::Kind, Assoc::Mode, wchar_t*, wchar_t*, wchar_t*, wchar_t*, wchar_t*);
    bool isAssociated(wchar_t*, wchar_t*);
    Assoc::Kind getKindAt(int i) {return (*table)[i] -> kind;}
    Assoc::Mode getModeAt(int i) {return (*table)[i] -> mode;}
    wchar_t* getNameAt(int i) {return (*table)[i] -> name;}
    wchar_t* getTypeAt(int i) {return (*table)[i] -> type;}
    wchar_t* getClassNameAt(int i) {return (*table)[i] -> class_name;}
    wchar_t* getMethodNameAt(int i) {return (*table)[i] -> method_name;}
    wchar_t* getPackageNameAt(int i) {return (*table)[i] -> package_name;}
    wchar_t* getName(Assoc::Kind, Assoc::Mode, wchar_t*, wchar_t*);
    bool isInvoked(wchar_t*, wchar_t*);
    wchar_t* getType(Assoc::Kind, Assoc::Mode, wchar_t*, wchar_t*, wchar_t*);
    int getSize() {return table -> size();}
    void dumpTable();
private:
    vector<Assoc*>* table;
};

//
// This class represents the control information common across all compilation
// units.  It provides a cache for essential classes and objects, as well as
// the command line options in force.
//
class Control : public StringConstant
{
public:
    StoragePool* gof_pool;
    int return_code;
    Option& option;
    SymbolTable classpath_table;
    SymbolTable external_table;

    unsigned dot_classpath_index;
    Tuple<PathSymbol*> classpath;
    Tuple<wchar_t*> bad_dirnames;
    Tuple<wchar_t*> bad_zip_filenames;
    Tuple<wchar_t*> bad_input_filenames;
    Tuple<wchar_t*> unreadable_input_filenames;
    Tuple<const wchar_t*> general_io_errors;
    Tuple<const wchar_t*> general_io_warnings;

    SystemTable* system_table;
    Tuple<DirectorySymbol*> system_directories;

    Semantic* system_semantic;
    Tuple<Semantic*> semantic;
    Tuple<TypeSymbol*> needs_body_work;
    Tuple<TypeSymbol*> type_trash_bin;

    NameSymbolMap unnamed_package_types;

    SymbolSet input_java_file_set;
    SymbolSet input_class_file_set;
    SymbolSet expired_file_set;
    SymbolSet recompilation_file_set;

    Parser* parser;
    Scanner* scanner;

    //
    // Tables for hashing everything we've seen so far.
    //
    LiteralLookupTable string_table;
    LiteralLookupTable int_table;
    LiteralLookupTable long_table;
    LiteralLookupTable char_table;
    LiteralLookupTable float_table;
    LiteralLookupTable double_table;
    NameLookupTable name_table;
    TypeLookupTable type_table;

    //
    // This cache of name symbols is initialized in system.cpp.
    //
    NameSymbol* access_name_symbol;
    NameSymbol* array_name_symbol;
    NameSymbol* assert_name_symbol;
    NameSymbol* block_init_name_symbol;
    NameSymbol* class_name_symbol;
    NameSymbol* clinit_name_symbol;
    NameSymbol* clone_name_symbol;
    NameSymbol* dot_name_symbol;
    NameSymbol* dot_dot_name_symbol;
    NameSymbol* Enum_name_symbol;
    NameSymbol* equals_name_symbol;
    NameSymbol* false_name_symbol;
    NameSymbol* hashCode_name_symbol;
    NameSymbol* init_name_symbol;
    NameSymbol* length_name_symbol;
    NameSymbol* null_name_symbol;
    NameSymbol* Object_name_symbol;
    NameSymbol* package_info_name_symbol;
    NameSymbol* question_name_symbol;
    NameSymbol* serialPersistentFields_name_symbol;
    NameSymbol* serialVersionUID_name_symbol;
    NameSymbol* this_name_symbol;
    NameSymbol* true_name_symbol;
    NameSymbol* val_name_symbol;

    Utf8LiteralValue* ConstantValue_literal;
    Utf8LiteralValue* Exceptions_literal;
    Utf8LiteralValue* InnerClasses_literal;
    Utf8LiteralValue* Synthetic_literal;
    Utf8LiteralValue* Deprecated_literal;
    Utf8LiteralValue* LineNumberTable_literal;
    Utf8LiteralValue* LocalVariableTable_literal;
    Utf8LiteralValue* Code_literal;
    Utf8LiteralValue* SourceFile_literal;
    Utf8LiteralValue* EnclosingMethod_literal;

    //
    // The primitive types.
    //
    TypeSymbol* byte_type;
    TypeSymbol* short_type;
    TypeSymbol* int_type;
    TypeSymbol* long_type;
    TypeSymbol* char_type;
    TypeSymbol* float_type;
    TypeSymbol* double_type;
    TypeSymbol* boolean_type;
    TypeSymbol* void_type;
    TypeSymbol* null_type;
    TypeSymbol* no_type;

    //
    // System package accessors.
    //
    inline PackageSymbol* AnnotationPackage()
    {
        if (! annotation_package)
            annotation_package = ProcessPackage(US_java_SL_lang_SL_annotation);
        return annotation_package;
    }
    inline PackageSymbol* IoPackage()
    {
        if (! io_package)
            io_package = ProcessPackage(US_java_SL_io);
        return io_package;
    }
    inline PackageSymbol* LangPackage()
    {
        assert(lang_package);
        return lang_package;
    }
    inline PackageSymbol* UtilPackage()
    {
        if (! util_package)
            util_package = ProcessPackage(US_java_SL_util);
        return util_package;
    }
    inline PackageSymbol* UnnamedPackage()
    {
        assert(unnamed_package);
        return unnamed_package;
    }

    //
    // System type, method, and field accessors. Useful boilerplate macros
    // reduce the chance for typos, but be sure to update Control::Control to
    // initialize the field created by the macros to NULL.
    //

    // TYPE_ACCESSOR(classname, expression);
#define TYPE_ACCESSOR(type, package)                           \
private:                                                       \
    TypeSymbol* type ## _type;                                 \
public:                                                        \
    inline TypeSymbol* type()                                  \
    {                                                          \
        if (! type ## _type)                                   \
            type ## _type = ProcessSystemType(package, #type); \
        return type ## _type;                                  \
    }

    // METHOD_ACCESSOR(class_methodname, expression, "name", "descriptor");
#define METHOD_ACCESSOR(method, type, name, descriptor)                      \
private:                                                                     \
    MethodSymbol* method ## _method;                                         \
public:                                                                      \
    inline MethodSymbol* method ## Method()                                  \
    {                                                                        \
        if (! method ## _method)                                             \
            method ## _method = ProcessSystemMethod(type, name, descriptor); \
        return method ## _method;                                            \
    }

    // FIELD_ACCESSOR(classname, fieldname, "descriptor");
#define FIELD_ACCESSOR(type, name, descriptor)                  \
private:                                                        \
    VariableSymbol* type ## _ ## name ## _field;                \
public:                                                         \
    inline VariableSymbol* type ## _ ## name ## _Field()        \
    {                                                           \
        if (! type ## _ ## name ## _field)                      \
            type ## _ ## name ## _field =                       \
                ProcessSystemField(type (), #name, descriptor); \
        return type ## _ ## name ## _field;                     \
    }

    TYPE_ACCESSOR(Annotation, AnnotationPackage());
    TYPE_ACCESSOR(AssertionError, lang_package);
    METHOD_ACCESSOR(AssertionError_Init, AssertionError(), "<init>", "()V");
    METHOD_ACCESSOR(AssertionError_InitWithChar, AssertionError(),
                    "<init>", "(C)V");
    METHOD_ACCESSOR(AssertionError_InitWithBoolean, AssertionError(),
                    "<init>", "(Z)V");
    METHOD_ACCESSOR(AssertionError_InitWithInt, AssertionError(),
                    "<init>", "(I)V");
    METHOD_ACCESSOR(AssertionError_InitWithLong, AssertionError(),
                    "<init>", "(J)V");
    METHOD_ACCESSOR(AssertionError_InitWithFloat, AssertionError(),
                    "<init>", "(F)V");
    METHOD_ACCESSOR(AssertionError_InitWithDouble, AssertionError(),
                    "<init>", "(D)V");
    METHOD_ACCESSOR(AssertionError_InitWithObject, AssertionError(),
                    "<init>", "(Ljava/lang/Object;)V");
    TYPE_ACCESSOR(Boolean, lang_package);
    FIELD_ACCESSOR(Boolean, TYPE, "java/lang/Class");
    TYPE_ACCESSOR(Byte, lang_package);
    FIELD_ACCESSOR(Byte, TYPE, "java/lang/Class");
    TYPE_ACCESSOR(Character, lang_package);
    FIELD_ACCESSOR(Character, TYPE, "java/lang/Class");
    TYPE_ACCESSOR(Class, lang_package);
    METHOD_ACCESSOR(Class_forName, Class(),
                    "forName", "(Ljava/lang/String;)Ljava/lang/Class;");
    METHOD_ACCESSOR(Class_getComponentType, Class(),
                    "getComponentType", "()Ljava/lang/Class;");
    METHOD_ACCESSOR(Class_desiredAssertionStatus, Class(),
                    "desiredAssertionStatus", "()Z");
    TYPE_ACCESSOR(ClassNotFoundException, lang_package);
    TYPE_ACCESSOR(Cloneable, lang_package);
    TYPE_ACCESSOR(Comparable, lang_package);
    TYPE_ACCESSOR(Double, lang_package);
    FIELD_ACCESSOR(Double, TYPE, "java/lang/Class");
    TYPE_ACCESSOR(ElementType, AnnotationPackage());
    FIELD_ACCESSOR(ElementType, TYPE, "java/lang/annotation/ElementType");
    FIELD_ACCESSOR(ElementType, FIELD, "java/lang/annotation/ElementType");
    FIELD_ACCESSOR(ElementType, METHOD, "java/lang/annotation/ElementType");
    FIELD_ACCESSOR(ElementType, PARAMETER, "java/lang/annotation/ElementType");
    FIELD_ACCESSOR(ElementType, CONSTRUCTOR,
                   "java/lang/annotation/ElementType");
    FIELD_ACCESSOR(ElementType, LOCAL_VARIABLE,
                   "java/lang/annotation/ElementType");
    FIELD_ACCESSOR(ElementType, ANNOTATION_TYPE,
                   "java/lang/annotation/ElementType");
    FIELD_ACCESSOR(ElementType, PACKAGE, "java/lang/annotation/ElementType");
    TYPE_ACCESSOR(Enum, lang_package);
    METHOD_ACCESSOR(Enum_Init, Enum(), "<init>", "(Ljava/lang/String;I)V");
    METHOD_ACCESSOR(Enum_ordinal, Enum(), "ordinal", "()I");
    METHOD_ACCESSOR(Enum_valueOf, Enum(), "valueOf",
                    "(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/Enum;");
    TYPE_ACCESSOR(Error, lang_package);
    TYPE_ACCESSOR(Exception, lang_package);
    TYPE_ACCESSOR(Float, lang_package);
    FIELD_ACCESSOR(Float, TYPE, "java/lang/Class");
    TYPE_ACCESSOR(Integer, lang_package);
    FIELD_ACCESSOR(Integer, TYPE, "java/lang/Class");
    TYPE_ACCESSOR(Iterable, lang_package);
    METHOD_ACCESSOR(Iterable_iterator, Iterable(),
                    "iterator", "()Ljava/util/Iterator;");
    TYPE_ACCESSOR(Iterator, UtilPackage());
    METHOD_ACCESSOR(Iterator_hasNext, Iterator(), "hasNext", "()Z");
    METHOD_ACCESSOR(Iterator_next, Iterator(), "next", "()Ljava/lang/Object;");
    TYPE_ACCESSOR(Long, lang_package);
    FIELD_ACCESSOR(Long, TYPE, "java/lang/Class");
    TYPE_ACCESSOR(NoClassDefFoundError, lang_package);
    METHOD_ACCESSOR(NoClassDefFoundError_Init, NoClassDefFoundError(),
                    "<init>", "()V");
    METHOD_ACCESSOR(NoClassDefFoundError_InitString, NoClassDefFoundError(),
                    "<init>", "(Ljava/lang/String;)V");
    TYPE_ACCESSOR(Object, lang_package);
    METHOD_ACCESSOR(Object_getClass, Object(),
                    "getClass", "()Ljava/lang/Class;");
    TYPE_ACCESSOR(Overrides, lang_package);
    TYPE_ACCESSOR(Retention, AnnotationPackage());
    TYPE_ACCESSOR(RetentionPolicy, AnnotationPackage());
    FIELD_ACCESSOR(RetentionPolicy, SOURCE,
                   "java/lang/annotation/RetentionPolicy");
    FIELD_ACCESSOR(RetentionPolicy, CLASS,
                   "java/lang/annotation/RetentionPolicy");
    FIELD_ACCESSOR(RetentionPolicy, RUNTIME,
                   "java/lang/annotation/RetentionPolicy");
    TYPE_ACCESSOR(RuntimeException, lang_package);
    TYPE_ACCESSOR(Serializable, IoPackage());
    TYPE_ACCESSOR(Short, lang_package);
    FIELD_ACCESSOR(Short, TYPE, "java/lang/Class");
    TYPE_ACCESSOR(String, lang_package);
    TYPE_ACCESSOR(StringBuffer, lang_package);
    METHOD_ACCESSOR(StringBuffer_Init, StringBuffer(), "<init>", "()V");
    METHOD_ACCESSOR(StringBuffer_InitWithString, StringBuffer(),
                    "<init>", "(Ljava/lang/String;)V");
    METHOD_ACCESSOR(StringBuffer_toString, StringBuffer(),
                    "toString", "()Ljava/lang/String;");
    METHOD_ACCESSOR(StringBuffer_append_char, StringBuffer(),
                    "append", "(C)Ljava/lang/StringBuffer;");
    METHOD_ACCESSOR(StringBuffer_append_boolean, StringBuffer(),
                    "append", "(Z)Ljava/lang/StringBuffer;");
    METHOD_ACCESSOR(StringBuffer_append_int, StringBuffer(),
                    "append", "(I)Ljava/lang/StringBuffer;");
    METHOD_ACCESSOR(StringBuffer_append_long, StringBuffer(),
                    "append", "(J)Ljava/lang/StringBuffer;");
    METHOD_ACCESSOR(StringBuffer_append_float, StringBuffer(),
                    "append", "(F)Ljava/lang/StringBuffer;");
    METHOD_ACCESSOR(StringBuffer_append_double, StringBuffer(),
                    "append", "(D)Ljava/lang/StringBuffer;");
    METHOD_ACCESSOR(StringBuffer_append_string, StringBuffer(),
                    "append", "(Ljava/lang/String;)Ljava/lang/StringBuffer;");
    METHOD_ACCESSOR(StringBuffer_append_object, StringBuffer(),
                    "append", "(Ljava/lang/Object;)Ljava/lang/StringBuffer;");
    TYPE_ACCESSOR(StringBuilder, lang_package);
    METHOD_ACCESSOR(StringBuilder_Init, StringBuilder(), "<init>", "()V");
    METHOD_ACCESSOR(StringBuilder_InitWithString, StringBuilder(),
                    "<init>", "(Ljava/lang/String;)V");
    METHOD_ACCESSOR(StringBuilder_toString, StringBuilder(),
                    "toString", "()Ljava/lang/String;");
    METHOD_ACCESSOR(StringBuilder_append_char, StringBuilder(),
                    "append", "(C)Ljava/lang/StringBuilder;");
    METHOD_ACCESSOR(StringBuilder_append_boolean, StringBuilder(),
                    "append", "(Z)Ljava/lang/StringBuilder;");
    METHOD_ACCESSOR(StringBuilder_append_int, StringBuilder(),
                    "append", "(I)Ljava/lang/StringBuilder;");
    METHOD_ACCESSOR(StringBuilder_append_long, StringBuilder(),
                    "append", "(J)Ljava/lang/StringBuilder;");
    METHOD_ACCESSOR(StringBuilder_append_float, StringBuilder(),
                    "append", "(F)Ljava/lang/StringBuilder;");
    METHOD_ACCESSOR(StringBuilder_append_double, StringBuilder(),
                    "append", "(D)Ljava/lang/StringBuilder;");
    METHOD_ACCESSOR(StringBuilder_append_string, StringBuilder(),
                    "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
    METHOD_ACCESSOR(StringBuilder_append_object, StringBuilder(),
                    "append", "(Ljava/lang/Object;)Ljava/lang/StringBuilder;");
    TYPE_ACCESSOR(Target, AnnotationPackage());
    TYPE_ACCESSOR(Throwable, lang_package);
    METHOD_ACCESSOR(Throwable_getMessage, Throwable(),
                    "getMessage", "()Ljava/lang/String;");
    METHOD_ACCESSOR(Throwable_initCause, Throwable(), "initCause",
                    "(Ljava/lang/Throwable;)Ljava/lang/Throwable;");
    TYPE_ACCESSOR(Void, lang_package);
    FIELD_ACCESSOR(Void, TYPE, "java/lang/Class");

#undef TYPE_ACCESSOR
#undef METHOD_ACCESSOR
#undef FIELD_ACCESSOR

    IntLiteralTable int_pool;
    LongLiteralTable long_pool;
    FloatLiteralTable float_pool;
    DoubleLiteralTable double_pool;
    Utf8LiteralTable Utf8_pool;

    Control(char**, Option&);
    ~Control();

    Utf8LiteralValue* ConvertUnicodeToUtf8(const wchar_t* source)
    {
        // Should be big enough for the worst case.
        char* target = new char[wcslen(source) * 3 + 1];
        int length = ConvertUnicodeToUtf8(source, target);
        Utf8LiteralValue* literal = Utf8_pool.FindOrInsert(target, length);
        delete [] target;
        return literal;
    }

    static int ConvertUtf8ToUnicode(wchar_t*, const char*, int);

    NameSymbol* ConvertUtf8ToUnicode(const char* source, int length)
    {
        wchar_t* name = new wchar_t[length + 1];
        int name_length = ConvertUtf8ToUnicode(name, source, length);
        NameSymbol* name_symbol = FindOrInsertName(name, name_length);
        delete [] name;
        return name_symbol;
    }

    void FindPathsToDirectory(PackageSymbol*);

    DirectoryEntry* FindInputFile(FileSymbol*);
    void FindMoreRecentInputFiles(SymbolSet&);
    void RemoveTrashedTypes(SymbolSet&);
    void RereadDirectory(DirectorySymbol*);
    void RereadDirectories();
    void ComputeRecompilationSet(TypeDependenceChecker&);
    bool IncrementalRecompilation();

    //
    // The one and only bad value constant.
    //
    LiteralValue* BadValue() { return &bad_value; }

    //
    // Note that only names are converted here and not literals, since
    // no error can occur in a name.
    // A literal is converted during the semantic pass so that an
    // accurate diagnostic can be issued in case it is invalid.
    //
    NameSymbol* FindOrInsertName(const wchar_t* name, int len)
    {
        NameSymbol* name_symbol = name_table.FindOrInsertName(name, len);
        if (! name_symbol -> Utf8_literal)
            name_symbol -> Utf8_literal =
                ConvertUnicodeToUtf8(name_symbol -> Name());
        return name_symbol;
    }

    //
    // Make up a parameter name of the form $(num) and return its name symbol.
    //
    NameSymbol* MakeParameter(int num)
    {
        IntToWstring value(num);
        wchar_t str[13];
        str[0] = U_DOLLAR; // '$'
        wcscpy(&str[1], value.String());
        return FindOrInsertName(str, value.Length() + 1);
    }

    //
    //
    //
    static DirectorySymbol* GetOutputDirectory(FileSymbol*);
    static FileSymbol* GetJavaFile(PackageSymbol*, const NameSymbol*);
    static FileSymbol* GetFile(Control&, PackageSymbol*, const NameSymbol*);
    static FileSymbol* GetFileFirst(Control&, PackageSymbol*,
                                    const NameSymbol*);
    static FileSymbol* GetFileBoth(Control&, PackageSymbol*,
                                   const NameSymbol*);

    PackageSymbol* FindOrInsertPackage(LexStream*, AstName*);
    void ProcessPackageDeclaration(FileSymbol*, AstPackageDeclaration*);
    void CleanUp(FileSymbol*);

    inline bool IsSimpleIntegerValueType(const TypeSymbol* type)
    {
        return type == byte_type || type == short_type ||
            type == int_type || type == char_type;
    }

    inline bool IsIntegral(const TypeSymbol* type)
    {
        return IsSimpleIntegerValueType(type) || type == long_type;
    }

    inline bool IsFloatingPoint(const TypeSymbol* type)
    {
        return type == float_type || type == double_type;
    }

    inline bool IsNumeric(const TypeSymbol* type)
    {
        return IsIntegral(type) || IsFloatingPoint(type);
    }

    inline bool IsDoubleWordType(const TypeSymbol* type)
    {
        return type == long_type || type == double_type;
    }

    inline bool IsPrimitive(const TypeSymbol* type)
    {
        return IsNumeric(type) || type == boolean_type;
    }

    inline void ProcessBadType(TypeSymbol* type_symbol)
    {
        type_trash_bin.Next() = type_symbol;
    }

    void ProcessHeaders(FileSymbol*);

#ifdef JIKES_DEBUG
    int input_files_processed,
        class_files_read,
        class_files_written,
        line_count;
#endif // JIKES_DEBUG

    PackageSymbol* ProcessPackage(const wchar_t*);

    DirectorySymbol* FindSubdirectory(PathSymbol*, wchar_t*, int);
    DirectorySymbol* ProcessSubdirectories(wchar_t*, int, bool);

private:

    //
    // GoF IRs
    //
    WriteAccessTable *w_table;
    ReadAccessTable *r_table;
    DelegationTable *d_table;
    ClassSymbolTable* cs_table;
    MethodSymbolTable* ms_table;

    MethodBodyTable* mb_table;
    GenTable* gen_table;
    AssocTable* assoc_table;
	
    LiteralValue bad_value;

    //
    // Cache of system packages. lang and unnamed are always valid, because of
    // ProcessUnnamedPackage and ProcessSystemInformation in system.cpp, the
    // constructor initializes the rest to NULL in control.cpp; see accessor
    // methods above for assignment.
    //
    PackageSymbol* annotation_package;
    PackageSymbol* io_package;
    PackageSymbol* lang_package;
    PackageSymbol* util_package;
    PackageSymbol* unnamed_package;

    static int ConvertUnicodeToUtf8(const wchar_t*, char*);
    NameSymbol* FindOrInsertSystemName(const char* name);

    void ProcessGlobals();
    void ProcessUnnamedPackage();
    void ProcessPath();
    void ProcessBootClassPath();
    void ProcessExtDirs();
    void ProcessClassPath();
    void ProcessSourcePath();
    TypeSymbol* GetPrimitiveType(const char*, char);
    void ProcessSystemInformation();
    TypeSymbol* ProcessSystemType(PackageSymbol*, const char*);
    MethodSymbol* ProcessSystemMethod(TypeSymbol*, const char*, const char*);
    VariableSymbol* ProcessSystemField(TypeSymbol*, const char*, const char*);

    void ProcessFile(FileSymbol*, StoragePool*);
    void ProcessMembers();
    void CollectTypes(TypeSymbol*, Tuple<TypeSymbol*>&);
    void ProcessBodies(TypeSymbol*, StoragePool*);
    void CheckForUnusedImports(Semantic *);

    void ProcessNewInputFiles(SymbolSet&, char**);

    FileSymbol* FindOrInsertJavaInputFile(DirectorySymbol*, NameSymbol*);
    FileSymbol* FindOrInsertJavaInputFile(wchar_t*, int);
/*
    void ExtractStructure(WriteAccessTable *w_table, DelegationTable *d_table, ClassSymbolTable *cs_table, MethodBodyTable* mb_table, MethodSymbolTable *ms_table, GenTable* gen_table, AssocTable* assoc_table, TypeSymbol* unit_type, StoragePool* ast_pool);
    void EmitGeneralization(GenTable * gen_table, TypeSymbol * unit_type);
    void EmitBlockAssociation(TypeSymbol * unit_type, MethodSymbol * enclosing_method, AstBlock * block, DelegationTable * d_table, WriteAccessTable * w_table);
    void EmitStatementAssociation(TypeSymbol * unit_type, MethodSymbol * enclosing_method, AstStatement * statement, DelegationTable * d_table, WriteAccessTable * w_table);
    void EmitExpressionAssociation(TypeSymbol * unit_type, MethodSymbol * enclosing_method, AstExpression * expression, DelegationTable * d_table, WriteAccessTable * w_table);
    void EmitDelegation(TypeSymbol * unit_type, MethodSymbol * enclosing_method, AstMethodInvocation * expression, DelegationTable * d_table, WriteAccessTable * w_table);
    void EmitWriteAccess(TypeSymbol * unit_type, MethodSymbol * enclosing_method, AstAssignmentExpression * expression, DelegationTable * d_table, WriteAccessTable * w_table);
    void EmitReadAccess(TypeSymbol * unit_type, MethodSymbol * enclosing_method, AstName * name, ReadAccessTable * r_table);
*/
};

#ifdef HAVE_JIKES_NAMESPACE
} // Close namespace Jikes block
#endif

#endif // control_INCLUDED

