// $Id: ast.cpp,v 1.9 2005/11/30 08:11:41 shini Exp $
//
// This software is subject to the terms of the IBM Jikes Compiler
// License Agreement available at the following URL:
// http://ibm.com/developerworks/opensource/jikes.
// Copyright (C) 1996, 2004 IBM Corporation and others.  All Rights Reserved.
// You must accept the terms of that agreement to use this software.
//

#include "ast.h"
#include "symbol.h"
#ifdef JIKES_DEBUG
# include "stream.h"
#endif // JIKES_DEBUG

#ifdef HAVE_JIKES_NAMESPACE
namespace Jikes { // Open namespace Jikes block
#endif

#ifdef JIKES_DEBUG
unsigned Ast::count = 0;
#endif

//
// Allocate another block of storage for the VariableSymbol array.
//
void VariableSymbolArray::AllocateMoreSpace()
{
    //
    //
    // The variable size always indicates the maximum number of
    // elements that has been allocated for the array.
    // Initially, it is set to 0 to indicate that the array is empty.
    // The pool of available elements is divided into segments of size
    // 2**log_blksize each. Each segment is pointed to by a slot in
    // the array base.
    //
    // By dividing size by the size of the segment we obtain the
    // index for the next segment in base. If base is full, it is
    // reallocated.
    //
    //
    size_t k = size >> log_blksize; /* which segment? */

    //
    // If the base is overflowed, reallocate it and initialize the new
    // elements to NULL.
    //
    if (k == base_size)
    {
        int old_base_size = base_size;
        T** old_base = base;

        base_size += base_increment;

        // There must be enough room to allocate base
        assert(base_size <= pool -> Blksize());

        base = (T**) pool -> Alloc(sizeof(T*) * base_size);

        if (old_base)
        {
            memcpy(base, old_base, old_base_size * sizeof(T*));
        }
        memset(&base[old_base_size], 0,
               (base_size - old_base_size) * sizeof(T*));
    }

    //
    // We allocate a new segment and place its adjusted address in
    // base[k]. The adjustment allows us to index the segment directly,
    // instead of having to perform a subtraction for each reference.
    // See operator[] below. There must be enough room to allocate block.
    //
    assert(Blksize() <= pool -> Blksize());

    base[k] = (T*) pool -> Alloc(sizeof(T) * Blksize());
    base[k] -= size;

    //
    // Finally, we update size.
    //
    size += Blksize();
}


VariableSymbolArray::VariableSymbolArray(StoragePool* p,
                                         unsigned estimate = 0)
    : pool(p)
{
    // There must be enough space in the storage pool to move !!!
    assert(pool -> Blksize() >= 256);

    if (estimate == 0)
        log_blksize = 6; // take a guess
    else
    {
        for (log_blksize = 1;
             ((1U << log_blksize) < estimate) && (log_blksize < 31);
             log_blksize++)
            ;
    }

    //
    // Increment a base_increment size that is big enough not to have to
    // be reallocated. Find a block size that is smaller that the block
    // size of the pool.
    //
    base_increment = (Blksize() > pool -> Blksize()
                      ? Blksize() / pool -> Blksize() : 1) * 2;
    while (Blksize() >= pool -> Blksize())
        log_blksize--;

    base_size = 0;
    size = 0;
    top = 0;
    base = NULL;
}


void AstCompilationUnit::FreeAst()
{
     delete ast_pool;
}

//
// This procedure uses a quick sort algorithm to sort the cases in a switch
// statement. Element 0 is not sorted, because it is the default case (and
// may be NULL).
//
void AstSwitchStatement::SortCases()
{
    int lower;
    int upper;
    int lostack[32];
    int histack[32];
    int top = 0;
    int i;
    int j;
    CaseElement pivot;
    CaseElement temp;

    lostack[top] = 1;
    histack[top] = num_cases;

    while (top >= 0)
    {
        lower = lostack[top];
        upper = histack[top];
        top--;

        while (upper > lower)
        {
            //
            // The array is most-likely almost sorted. Therefore,
            // we use the middle element as the pivot element.
            //
            i = (lower + upper) >> 1;
            pivot = *cases[i];
            *cases[i] = *cases[lower];

            //
            // Split the array section indicated by LOWER and UPPER
            // using ARRAY(LOWER) as the pivot.
            //
            i = lower;
            for (j = lower + 1; j <= upper; j++)
                if (*cases[j] < pivot)
                {
                    temp = *cases[++i];
                    *cases[i] = *cases[j];
                    *cases[j] = temp;
                }
            *cases[lower] = *cases[i];
            *cases[i] = pivot;

            top++;
            if ((i - lower) < (upper - i))
            {
                lostack[top] = i + 1;
                histack[top] = upper;
                upper = i - 1;
            }
            else
            {
                histack[top] = i - 1;
                lostack[top] = lower;
                lower = i + 1;
            }
        }
    }
}

//
// Performs a binary search to locate the correct case (including the
// default case) for a constant expression value. Returns NULL if the switch
// is a no-op for this constant.
//
CaseElement* AstSwitchStatement::CaseForValue(i4 value)
{
    unsigned lower = 1;
    unsigned upper = num_cases;
    while (lower <= upper)
    {
        unsigned mid = (lower + upper) >> 1;
        CaseElement* elt = cases[mid];
        if (elt -> value == value)
            return elt;
        if (elt -> value > value)
            upper = mid - 1;
        else
            lower = mid + 1;
    }
    return cases[0];
}


TypeSymbol* AstMemberValue::Type()
{
    return ! symbol ? (TypeSymbol*) NULL
        : symbol -> Kind() == Symbol::TYPE
        ? (TypeSymbol*) symbol
        : symbol -> Kind() == Symbol::VARIABLE
        ? ((VariableSymbol*) symbol) -> Type()
        : symbol -> Kind() == Symbol::METHOD
        ? ((MethodSymbol*) symbol) -> Type()
        : (TypeSymbol*) NULL;
}


Ast* AstBlock::Clone(StoragePool* ast_pool)
{
    AstBlock* clone = ast_pool -> GenBlock();
    clone -> CloneBlock(ast_pool, this);
    return clone;
}

void AstBlock::CloneBlock(StoragePool* ast_pool, AstBlock* orig)
{
    other_tag = orig -> other_tag;
    label_opt = orig -> label_opt;
	
    label_opt_string = orig -> label_opt_string;
	
    nesting_level = orig -> nesting_level;
    left_brace_token = orig -> left_brace_token;
    unsigned count = orig -> NumStatements();
    AllocateStatements(count);
    for (unsigned i = 0; i < count; i++)
        AddStatement((AstStatement*) orig -> Statement(i) -> Clone(ast_pool));
    right_brace_token = orig -> right_brace_token;
    no_braces = orig -> no_braces;
}

Ast* AstName::Clone(StoragePool* ast_pool)
{
    AstName* clone = ast_pool -> GenName(identifier_token);
	
    clone->identifier_token_string = identifier_token_string;
	
    clone -> base_opt = (base_opt) ? (AstName*) base_opt -> Clone(ast_pool) : NULL;
    clone -> resolution_opt =(resolution_opt)
		? (AstExpression*) resolution_opt -> Clone(ast_pool)
		: NULL;

    clone->symbol = symbol;

    return clone;
}

Ast* AstPrimitiveType::Clone(StoragePool* ast_pool)
{
    AstPrimitiveType *clone = ast_pool -> GenPrimitiveType(kind, primitive_kind_token);

    clone->primitive_kind_token_string = primitive_kind_token_string;

    return clone;
}

Ast* AstBrackets::Clone(StoragePool* ast_pool)
{
    AstBrackets* clone =
        ast_pool -> GenBrackets(left_bracket_token, right_bracket_token);
    clone -> dims = dims;
    return clone;
}

Ast* AstArrayType::Clone(StoragePool* ast_pool)
{
    return ast_pool -> GenArrayType((AstType*) type -> Clone(ast_pool),
                                    ((AstBrackets*) brackets ->
                                     Clone(ast_pool)));
}

Ast* AstWildcard::Clone(StoragePool* ast_pool)
{
    AstWildcard* clone = ast_pool -> GenWildcard(question_token);
    clone -> extends_token_opt = extends_token_opt;
    clone -> super_token_opt = super_token_opt;

    clone->question_token_string = question_token_string;
    clone->extends_token_opt_string= extends_token_opt_string;
    clone->super_token_opt_string = super_token_opt_string;
	

    if (bounds_opt)
        clone -> bounds_opt = (AstType*) bounds_opt -> Clone(ast_pool);
    return clone;
}

Ast* AstTypeArguments::Clone(StoragePool* ast_pool)
{
    AstTypeArguments* clone = ast_pool -> GenTypeArguments(left_angle_token,
                                                           right_angle_token);
    clone -> AllocateTypeArguments(NumTypeArguments());
    for (unsigned i = 0; i < NumTypeArguments(); i++)
        clone -> AddTypeArgument((AstType*) TypeArgument(i) ->
                                 Clone(ast_pool));
    return clone;
}

Ast* AstTypeName::Clone(StoragePool* ast_pool)
{
    AstTypeName* clone =
        ast_pool -> GenTypeName((AstName*) name -> Clone(ast_pool));
    if (base_opt)
        clone -> base_opt = (AstTypeName*) base_opt -> Clone(ast_pool);
    if (type_arguments_opt)
        clone -> type_arguments_opt =
            (AstTypeArguments*) type_arguments_opt -> Clone(ast_pool);
    return clone;
}

Ast* AstMemberValuePair::Clone(StoragePool* ast_pool)
{
    AstMemberValuePair* clone = ast_pool -> GenMemberValuePair();
    clone -> identifier_token_opt = identifier_token_opt;

    clone->identifier_token_opt_string = identifier_token_opt_string;
	
    clone -> member_value = (AstMemberValue*) member_value -> Clone(ast_pool);	
    return clone;
}

Ast* AstAnnotation::Clone(StoragePool* ast_pool)
{
    AstAnnotation* clone = ast_pool -> GenAnnotation();
    clone -> at_token = at_token;
    clone -> name = (AstName*) name -> Clone(ast_pool);
    clone -> AllocateMemberValuePairs(NumMemberValuePairs());
    for (unsigned i = 0; i < NumMemberValuePairs(); i++)
        clone -> AddMemberValuePair((AstMemberValuePair*)
                                    MemberValuePair(i) -> Clone(ast_pool));
    clone -> right_paren_token_opt = right_paren_token_opt;

    clone->symbol = symbol;
	
    return clone;
}

Ast* AstModifierKeyword::Clone(StoragePool* ast_pool)
{
    AstModifierKeyword *clone = ast_pool -> GenModifierKeyword(modifier_token);
    clone->modifier_token_string = modifier_token_string;
    return clone;
}

Ast* AstModifiers::Clone(StoragePool* ast_pool)
{
    AstModifiers* clone = ast_pool -> GenModifiers();
    clone -> AllocateModifiers(NumModifiers());
    for (unsigned i = 0; i < NumModifiers(); i++)
    {
        if (Modifier(i) -> ModifierKeywordCast())
            clone -> AddModifier((AstModifierKeyword*)
                                 Modifier(i) -> Clone(ast_pool));
        else clone -> AddModifier((AstAnnotation*)
                                  Modifier(i) -> Clone(ast_pool));
    }
    clone -> static_token_opt = static_token_opt;
    return clone;
}

Ast* AstPackageDeclaration::Clone(StoragePool* ast_pool)
{
    AstPackageDeclaration* clone = ast_pool -> GenPackageDeclaration();
    if (modifiers_opt)
        clone -> modifiers_opt =
            (AstModifiers*) modifiers_opt -> Clone(ast_pool);
    clone -> package_token = package_token;

    clone->package_token_string = package_token_string;
	
    clone -> name = (AstName*) name -> Clone(ast_pool);
    clone -> semicolon_token = semicolon_token;
    return clone;
}

Ast* AstImportDeclaration::Clone(StoragePool* ast_pool)
{
    AstImportDeclaration* clone = ast_pool -> GenImportDeclaration();
    clone -> import_token = import_token;
    clone -> static_token_opt = static_token_opt;
    clone -> name = (AstName*) name -> Clone(ast_pool);
    clone -> star_token_opt = star_token_opt;
    clone -> semicolon_token = semicolon_token;

    clone->import_token_string = import_token_string;
    clone->static_token_opt_string = static_token_opt_string;
    clone->star_token_opt_string = star_token_opt_string;
	
    return clone;
}

Ast* AstCompilationUnit::Clone(StoragePool* ast_pool)
{
    unsigned i;
    AstCompilationUnit* clone = ast_pool -> GenCompilationUnit();
    clone -> other_tag = other_tag;
    if (package_declaration_opt)
        clone -> package_declaration_opt = (AstPackageDeclaration*)
            package_declaration_opt -> Clone(ast_pool);
    clone -> AllocateImportDeclarations(NumImportDeclarations());
    for (i = 0; i < NumImportDeclarations(); i++)
        clone -> AddImportDeclaration((AstImportDeclaration*)
                                      ImportDeclaration(i) -> Clone(ast_pool));
    clone -> AllocateTypeDeclarations(NumTypeDeclarations());
    for (i = 0; i < NumTypeDeclarations(); i++)
        clone -> AddTypeDeclaration((AstDeclaredType*) TypeDeclaration(i) ->
                                    Clone(ast_pool));
    return clone;
}

Ast* AstEmptyDeclaration::Clone(StoragePool* ast_pool)
{
    AstEmptyDeclaration *clone = ast_pool -> GenEmptyDeclaration(semicolon_token);
	
    clone->semicolon_token_string = semicolon_token_string;

    return clone;
}

Ast* AstClassBody::Clone(StoragePool* ast_pool)
{
    AstClassBody* clone = ast_pool -> GenClassBody();
    clone -> identifier_token = identifier_token;

    clone->identifier_token_string = identifier_token_string;

    clone -> left_brace_token = left_brace_token;
    clone -> AllocateClassBodyDeclarations(NumClassBodyDeclarations());
    clone -> AllocateInstanceVariables(NumInstanceVariables());
    clone -> AllocateClassVariables(NumClassVariables());
    clone -> AllocateMethods(NumMethods());
    clone -> AllocateConstructors(NumConstructors());
    clone -> AllocateStaticInitializers(NumStaticInitializers());
    clone -> AllocateInstanceInitializers(NumInstanceInitializers());
    clone -> AllocateNestedClasses(NumNestedClasses());
    clone -> AllocateNestedEnums(NumNestedEnums());
    clone -> AllocateNestedInterfaces(NumNestedInterfaces());
    clone -> AllocateNestedAnnotations(NumNestedAnnotations());
    clone -> AllocateEmptyDeclarations(NumEmptyDeclarations());
    for (unsigned i = 0; i < NumClassBodyDeclarations(); i++)
        clone -> AddClassBodyDeclaration((AstDeclaredType*)
                                         ClassBodyDeclaration(i) ->
                                         Clone(ast_pool));
    clone -> right_brace_token = right_brace_token;
    return clone;
}

void AstClassBody::AddClassBodyDeclaration(AstDeclared* member)
{
    assert(class_body_declarations);
    AstFieldDeclaration* field_declaration = member -> FieldDeclarationCast();
    AstMethodDeclaration* method_declaration =
        member -> MethodDeclarationCast();
    AstConstructorDeclaration* constructor_declaration =
        member -> ConstructorDeclarationCast();
    AstInitializerDeclaration* initializer =
        member -> InitializerDeclarationCast();
    AstClassDeclaration* class_declaration = member -> ClassDeclarationCast();
    AstEnumDeclaration* enum_declaration = member -> EnumDeclarationCast();
    AstInterfaceDeclaration* interface_declaration =
        member -> InterfaceDeclarationCast();
    AstAnnotationDeclaration* annotation_declaration =
        member -> AnnotationDeclarationCast();

    class_body_declarations -> Next() = member;
    if (field_declaration)
    {
        if (field_declaration -> StaticFieldCast())
            AddClassVariable(field_declaration);
        else AddInstanceVariable(field_declaration);
    }
    else if (method_declaration)
        AddMethod(method_declaration);
    else if (constructor_declaration)
        AddConstructor(constructor_declaration);
    else if (initializer)
    {
        if (initializer -> StaticInitializerCast())
            AddStaticInitializer(initializer);
        else AddInstanceInitializer(initializer);
    }
    else if (class_declaration)
        AddNestedClass(class_declaration);
    else if (enum_declaration)
        AddNestedEnum(enum_declaration);
    else if (interface_declaration)
        AddNestedInterface(interface_declaration);
    else if (annotation_declaration)
        AddNestedAnnotation(annotation_declaration);
    else AddEmptyDeclaration((AstEmptyDeclaration*) member);
}

Ast* AstTypeParameter::Clone(StoragePool* ast_pool)
{
    AstTypeParameter* clone = ast_pool -> GenTypeParameter(identifier_token);

    clone->identifier_token_string = identifier_token_string;
	
    clone -> AllocateBounds(NumBounds());
    for (unsigned i = 0; i < NumBounds(); i++)
        clone -> AddBound((AstTypeName*) Bound(i) -> Clone(ast_pool));
    return clone;
}

Ast* AstTypeParameters::Clone(StoragePool* ast_pool)
{
    AstTypeParameters* clone = ast_pool -> GenTypeParameters();
    clone -> left_angle_token = left_angle_token;
    clone -> AllocateTypeParameters(NumTypeParameters());
    for (unsigned i = 0; i < NumTypeParameters(); i++)
        clone -> AddTypeParameter((AstTypeParameter*) TypeParameter(i) ->
                                  Clone(ast_pool));
    clone -> right_angle_token = right_angle_token;
    return clone;
}

Ast* AstClassDeclaration::Clone(StoragePool* ast_pool)
{
    AstClassDeclaration* clone = ast_pool -> GenClassDeclaration();
    if (modifiers_opt)
        clone -> modifiers_opt =
            (AstModifiers*) modifiers_opt -> Clone(ast_pool);
    clone -> class_token = class_token;

    clone->class_token_string = class_token_string;
	
    if (type_parameters_opt)
        clone -> type_parameters_opt =
            (AstTypeParameters*) type_parameters_opt -> Clone(ast_pool);
    if (super_opt)
        clone -> super_opt = (AstTypeName*) super_opt -> Clone(ast_pool);
    clone -> AllocateInterfaces(NumInterfaces());
    for (unsigned i = 0; i < NumInterfaces(); i++)
        clone -> AddInterface((AstTypeName*) Interface(i) -> Clone(ast_pool));
    clone -> class_body = (AstClassBody*) class_body -> Clone(ast_pool);
    clone -> class_body -> owner = clone;
    return clone;
}

Ast* AstArrayInitializer::Clone(StoragePool* ast_pool)
{
    AstArrayInitializer* clone = ast_pool -> GenArrayInitializer();
    clone -> left_brace_token = left_brace_token;
    clone -> AllocateVariableInitializers(NumVariableInitializers());
    for (unsigned i = 0; i < NumVariableInitializers(); i++)
        clone -> AddVariableInitializer((AstMemberValue*)
                                        VariableInitializer(i) ->
                                        Clone(ast_pool));
    clone -> right_brace_token = right_brace_token;

    clone->symbol = symbol;
	
    return clone;
}

Ast* AstVariableDeclaratorId::Clone(StoragePool* ast_pool)
{
    AstVariableDeclaratorId* clone = ast_pool -> GenVariableDeclaratorId();
    clone -> identifier_token = identifier_token;

    clone->identifier_token_string = identifier_token_string;

    if (brackets_opt)
        clone -> brackets_opt = (AstBrackets*) brackets_opt -> Clone(ast_pool);
    return clone;
}

Ast* AstVariableDeclarator::Clone(StoragePool* ast_pool)
{
    AstVariableDeclarator* clone = ast_pool -> GenVariableDeclarator();
    clone -> variable_declarator_name = (AstVariableDeclaratorId*)
        variable_declarator_name -> Clone(ast_pool);
    if (variable_initializer_opt)
        clone -> variable_initializer_opt =
            variable_initializer_opt -> Clone(ast_pool);
    return clone;
}

Ast* AstFieldDeclaration::Clone(StoragePool* ast_pool)
{
    AstFieldDeclaration* clone = ast_pool -> GenFieldDeclaration();
    clone -> other_tag = other_tag;
    if (modifiers_opt)
        clone -> modifiers_opt =
            (AstModifiers*) modifiers_opt -> Clone(ast_pool);
    clone -> type = (AstType*) type -> Clone(ast_pool);
    clone -> AllocateVariableDeclarators(NumVariableDeclarators());
    for (unsigned i = 0; i < NumVariableDeclarators(); i++)
        clone -> AddVariableDeclarator((AstVariableDeclarator*)
                                       VariableDeclarator(i) ->
                                       Clone(ast_pool));
    clone -> semicolon_token = semicolon_token;
    return clone;
}

Ast* AstFormalParameter::Clone(StoragePool* ast_pool)
{
    AstFormalParameter* clone = ast_pool -> GenFormalParameter();
    if (modifiers_opt)
        clone -> modifiers_opt =
            (AstModifiers*) modifiers_opt -> Clone(ast_pool);
    clone -> type = (AstType*) type -> Clone(ast_pool);
    clone -> ellipsis_token_opt = ellipsis_token_opt;

    clone -> ellipsis_token_opt_string = ellipsis_token_opt_string;
	
    clone -> formal_declarator =
        (AstVariableDeclarator*) formal_declarator -> Clone(ast_pool);
    return clone;
}

Ast* AstMethodDeclarator::Clone(StoragePool* ast_pool)
{
    AstMethodDeclarator* clone = ast_pool -> GenMethodDeclarator();
    clone -> identifier_token = identifier_token;

    clone -> identifier_token_string = identifier_token_string;
	
    clone -> left_parenthesis_token = left_parenthesis_token;
    clone -> AllocateFormalParameters(NumFormalParameters());
    for (unsigned i = 0; i < NumFormalParameters(); i++)
        clone -> AddFormalParameter((AstFormalParameter*)
                                    FormalParameter(i) -> Clone(ast_pool));
    clone -> right_parenthesis_token = right_parenthesis_token;
    if (brackets_opt)
        clone -> brackets_opt = (AstBrackets*) brackets_opt -> Clone(ast_pool);
    return clone;
}

Ast* AstMethodBody::Clone(StoragePool* ast_pool)
{
    AstMethodBody* clone = ast_pool -> GenMethodBody();
    clone -> CloneBlock(ast_pool, this);
    if (explicit_constructor_opt)
        clone -> explicit_constructor_opt =
            (AstStatement*) explicit_constructor_opt -> Clone(ast_pool);
    return clone;
}

Ast* AstMethodDeclaration::Clone(StoragePool* ast_pool)
{
    AstMethodDeclaration* clone = ast_pool -> GenMethodDeclaration();
    if (modifiers_opt)
        clone -> modifiers_opt =
            (AstModifiers*) modifiers_opt -> Clone(ast_pool);
    if (type_parameters_opt)
        clone -> type_parameters_opt =
            (AstTypeParameters*) type_parameters_opt -> Clone(ast_pool);
    clone -> type = (AstType*) type -> Clone(ast_pool);
    clone -> method_declarator =
        (AstMethodDeclarator*) method_declarator -> Clone(ast_pool);
    clone -> AllocateThrows(NumThrows());
    for (unsigned i = 0; i < NumThrows(); i++)
        clone -> AddThrow((AstTypeName*) Throw(i) -> Clone(ast_pool));
    if (default_value_opt)
        clone -> default_value_opt =
            (AstMemberValue*) default_value_opt -> Clone(ast_pool);
    if (method_body_opt)
        clone -> method_body_opt =
            (AstMethodBody*) method_body_opt -> Clone(ast_pool);
    clone -> semicolon_token_opt = semicolon_token_opt;
    return clone;
}

Ast* AstInitializerDeclaration::Clone(StoragePool* ast_pool)
{
    AstInitializerDeclaration* clone = ast_pool -> GenInitializerDeclaration();
    clone -> other_tag = other_tag;
    if (modifiers_opt)
        clone -> modifiers_opt =
            (AstModifiers*) modifiers_opt -> Clone(ast_pool);
    clone -> block = (AstMethodBody*) block -> Clone(ast_pool);
    return clone;
}

Ast* AstArguments::Clone(StoragePool* ast_pool)
{
    unsigned i;
    AstArguments* clone = ast_pool -> GenArguments(left_parenthesis_token,
                                                   right_parenthesis_token);
    clone -> AllocateArguments(NumArguments());
    for (i = 0; i < NumArguments(); i++)
        clone -> AddArgument((AstExpression*) Argument(i) -> Clone(ast_pool));
    clone -> AllocateLocalArguments(NumLocalArguments());
    for (i = 0; i < NumLocalArguments(); i++)
        clone -> AddLocalArgument((AstName*) LocalArgument(i) ->
                                  Clone(ast_pool));
    clone -> other_tag = other_tag;
    return clone;
}

Ast* AstThisCall::Clone(StoragePool* ast_pool)
{
    AstThisCall* clone = ast_pool -> GenThisCall();
    if (type_arguments_opt)
        clone -> type_arguments_opt =
            (AstTypeArguments*) type_arguments_opt -> Clone(ast_pool);
    clone -> this_token = this_token;

    clone -> this_token_string = this_token_string;
	
    clone -> arguments = (AstArguments*) arguments -> Clone(ast_pool);
    clone -> semicolon_token = semicolon_token;
    return clone;
}

Ast* AstSuperCall::Clone(StoragePool* ast_pool)
{
    AstSuperCall* clone = ast_pool -> GenSuperCall();
    if (base_opt)
        clone -> base_opt = (AstExpression*) base_opt -> Clone(ast_pool);
    if (type_arguments_opt)
        clone -> type_arguments_opt =
            (AstTypeArguments*) type_arguments_opt -> Clone(ast_pool);
    clone -> super_token = super_token;

    clone -> super_token_string = super_token_string;

    clone -> arguments = (AstArguments*) arguments -> Clone(ast_pool);
    clone -> semicolon_token = semicolon_token;
    return clone;
}

Ast* AstConstructorDeclaration::Clone(StoragePool* ast_pool)
{
    AstConstructorDeclaration* clone = ast_pool -> GenConstructorDeclaration();
    if (modifiers_opt)
        clone -> modifiers_opt =
            (AstModifiers*) modifiers_opt -> Clone(ast_pool);
    if (type_parameters_opt)
        clone -> type_parameters_opt =
            (AstTypeParameters*) type_parameters_opt -> Clone(ast_pool);
    clone -> constructor_declarator =
        (AstMethodDeclarator*) constructor_declarator -> Clone(ast_pool);
    clone -> AllocateThrows(NumThrows());
    for (unsigned i = 0; i < NumThrows(); i++)
        clone -> AddThrow((AstTypeName*) Throw(i) -> Clone(ast_pool));
    clone -> constructor_body =
        (AstMethodBody*) constructor_body -> Clone(ast_pool);
    return clone;
}

Ast* AstEnumDeclaration::Clone(StoragePool* ast_pool)
{
    unsigned i;
    AstEnumDeclaration* clone = ast_pool -> GenEnumDeclaration();

    clone -> enum_token_string = enum_token_string;
	
    if (modifiers_opt)
        clone -> modifiers_opt =
            (AstModifiers*) modifiers_opt -> Clone(ast_pool);
    clone -> AllocateInterfaces(NumInterfaces());
    for (i = 0; i < NumInterfaces(); i++)
        clone -> AddInterface((AstTypeName*) Interface(i) -> Clone(ast_pool));
    clone -> AllocateEnumConstants(NumEnumConstants());
    for (i = 0; i < NumEnumConstants(); i++)
        clone -> AddEnumConstant((AstEnumConstant*) EnumConstant(i) ->
                                 Clone(ast_pool));
    clone -> class_body = (AstClassBody*) class_body -> Clone(ast_pool);
    clone -> class_body -> owner = clone;
    return clone;
}

Ast* AstEnumConstant::Clone(StoragePool* ast_pool)
{
    AstEnumConstant* clone = ast_pool -> GenEnumConstant(identifier_token);

    clone->identifier_token_string = identifier_token_string;
 
    if (modifiers_opt)
        clone -> modifiers_opt =
            (AstModifiers*) modifiers_opt -> Clone(ast_pool);
    if (arguments_opt)
        clone -> arguments_opt =
            (AstArguments*) arguments_opt -> Clone(ast_pool);
    if (class_body_opt)
        clone -> class_body_opt =
            (AstClassBody*) class_body_opt -> Clone(ast_pool);
    return clone;
}

Ast* AstInterfaceDeclaration::Clone(StoragePool* ast_pool)
{
    AstInterfaceDeclaration* clone = ast_pool -> GenInterfaceDeclaration();
    if (modifiers_opt)
        clone -> modifiers_opt =
            (AstModifiers*) modifiers_opt -> Clone(ast_pool);
    clone -> interface_token = interface_token;

    clone -> interface_token_string = interface_token_string;
	
    if (type_parameters_opt)
        clone -> type_parameters_opt =
            (AstTypeParameters*) type_parameters_opt -> Clone(ast_pool);
    clone -> AllocateInterfaces(NumInterfaces());
    for (unsigned i = 0; i < NumInterfaces(); i++)
        clone -> AddInterface((AstTypeName*) Interface(i) -> Clone(ast_pool));
    clone -> class_body = (AstClassBody*) class_body -> Clone(ast_pool);
    clone -> class_body -> owner = clone;
    return clone;
}

Ast* AstAnnotationDeclaration::Clone(StoragePool* ast_pool)
{
    AstAnnotationDeclaration* clone =
        ast_pool -> GenAnnotationDeclaration(interface_token);
    if (modifiers_opt)
        clone -> modifiers_opt =
            (AstModifiers*) modifiers_opt -> Clone(ast_pool);
    clone -> class_body = (AstClassBody*) class_body -> Clone(ast_pool);
    clone -> class_body -> owner = clone;

    clone->interface_token_string = interface_token_string;
	
    return clone;
}

Ast* AstLocalVariableStatement::Clone(StoragePool* ast_pool)
{
    AstLocalVariableStatement* clone = ast_pool -> GenLocalVariableStatement();
    if (modifiers_opt)
        clone -> modifiers_opt =
            (AstModifiers*) modifiers_opt -> Clone(ast_pool);
    clone -> type = (AstType*) type -> Clone(ast_pool);
    clone -> AllocateVariableDeclarators(NumVariableDeclarators());
    for (unsigned i = 0; i < NumVariableDeclarators(); i++)
        clone -> AddVariableDeclarator((AstVariableDeclarator*)
                                       VariableDeclarator(i) ->
                                       Clone(ast_pool));
    clone -> semicolon_token_opt = semicolon_token_opt;
    return clone;
}

Ast* AstLocalClassStatement::Clone(StoragePool* ast_pool)
{
    Ast* p = declaration -> Clone(ast_pool);
    if (p -> ClassDeclarationCast())
        return ast_pool -> GenLocalClassStatement((AstClassDeclaration*) p);
    else return ast_pool -> GenLocalClassStatement((AstEnumDeclaration*) p);
}

Ast* AstIfStatement::Clone(StoragePool* ast_pool)
{
    AstIfStatement* clone = ast_pool -> GenIfStatement();
    clone -> if_token = if_token;

    clone -> if_token_string = if_token_string;

    clone -> expression = (AstExpression*) expression -> Clone(ast_pool);
    clone -> true_statement = (AstBlock*) true_statement -> Clone(ast_pool);
    if (false_statement_opt)
        clone -> false_statement_opt =
            (AstBlock*) false_statement_opt -> Clone(ast_pool);
    return clone;
}

Ast* AstEmptyStatement::Clone(StoragePool* ast_pool)
{
    AstEmptyStatement * clone = ast_pool -> GenEmptyStatement(semicolon_token);
    clone->semicolon_token_string = semicolon_token_string;
    return clone;
}

Ast* AstExpressionStatement::Clone(StoragePool* ast_pool)
{
    AstExpressionStatement* clone = ast_pool -> GenExpressionStatement();
    clone -> expression = (AstExpression*) expression -> Clone(ast_pool);
    clone -> semicolon_token_opt = semicolon_token_opt;
    return clone;
}

Ast* AstSwitchLabel::Clone(StoragePool* ast_pool)
{
    AstSwitchLabel* clone = ast_pool -> GenSwitchLabel();
    clone -> case_token = case_token;

    clone -> case_token_string = case_token_string;

    if (expression_opt)
        clone -> expression_opt =
            (AstExpression*) expression_opt -> Clone(ast_pool);
    clone -> colon_token = colon_token;
    clone -> map_index = map_index;
    return clone;
}

Ast* AstSwitchBlockStatement::Clone(StoragePool* ast_pool)
{
    AstSwitchBlockStatement* clone = ast_pool -> GenSwitchBlockStatement();
    clone -> CloneBlock(ast_pool, this);
    clone -> AllocateSwitchLabels(NumSwitchLabels());
    for (unsigned i = 0; i < NumSwitchLabels(); i++)
        clone -> AddSwitchLabel((AstSwitchLabel*) SwitchLabel(i) ->
                                Clone(ast_pool));
    return clone;
}

Ast* AstSwitchStatement::Clone(StoragePool* ast_pool)
{
    AstSwitchStatement* clone = ast_pool -> GenSwitchStatement();
    clone -> switch_token = switch_token;

    clone -> switch_token_string = switch_token_string;

    clone -> expression = (AstExpression*) expression -> Clone(ast_pool);
    clone -> switch_block = (AstBlock*) switch_block -> Clone(ast_pool);
    clone -> AllocateCases(NumCases());
    if (DefaultCase())
    {
        clone -> DefaultCase() = ast_pool -> GenCaseElement(0, 0);
        *clone -> DefaultCase() = *DefaultCase();
    }
    for (unsigned i = 0; i < NumCases(); i++)
    {
        CaseElement* elt = ast_pool -> GenCaseElement(0, 0);
        *elt = *Case(i);
        clone -> AddCase(elt);
    }
    return clone;
}

Ast* AstWhileStatement::Clone(StoragePool* ast_pool)
{
    AstWhileStatement* clone = ast_pool -> GenWhileStatement();
    clone -> while_token = while_token;

    clone -> while_token_string = while_token_string;

    clone -> expression = (AstExpression*) expression -> Clone(ast_pool);
    clone -> statement = (AstBlock*) statement -> Clone(ast_pool);
    return clone;
}

Ast* AstDoStatement::Clone(StoragePool* ast_pool)
{
    AstDoStatement* clone = ast_pool -> GenDoStatement();
    clone -> do_token = do_token;

    clone -> do_token_string = do_token_string;

    clone -> statement = (AstBlock*) statement -> Clone(ast_pool);
    clone -> while_token = while_token;

    clone -> while_token_string = while_token_string;

    clone -> expression = (AstExpression*) expression -> Clone(ast_pool);
    clone -> semicolon_token = semicolon_token;
    return clone;
}

Ast* AstForStatement::Clone(StoragePool* ast_pool)
{
    unsigned i;
    AstForStatement* clone = ast_pool -> GenForStatement();
    clone -> for_token = for_token;

    clone -> for_token_string = for_token_string;

    clone -> AllocateForInitStatements(NumForInitStatements());
    for (i = 0; i < NumForInitStatements(); i++)
        clone -> AddForInitStatement((AstStatement*)
                                     ForInitStatement(i) -> Clone(ast_pool));
    if (end_expression_opt)
        clone -> end_expression_opt =
            (AstExpression*) end_expression_opt -> Clone(ast_pool);
    clone -> AllocateForUpdateStatements(NumForUpdateStatements());
    for (i = 0; i < NumForUpdateStatements(); i++)
        clone -> AddForUpdateStatement((AstExpressionStatement*)
                                       ForUpdateStatement(i) ->
                                       Clone(ast_pool));
    clone -> statement = (AstBlock*) statement -> Clone(ast_pool);
    return clone;
}

Ast* AstForeachStatement::Clone(StoragePool* ast_pool)
{
    AstForeachStatement* clone = ast_pool -> GenForeachStatement();
    clone -> for_token = for_token;

    clone -> for_token_string = for_token_string;
	
    clone -> formal_parameter =
        (AstFormalParameter*) formal_parameter -> Clone(ast_pool);
    clone -> expression = (AstExpression*) expression -> Clone(ast_pool);
    clone -> statement = (AstBlock*) statement -> Clone(ast_pool);
    return clone;
}

Ast* AstBreakStatement::Clone(StoragePool* ast_pool)
{
    AstBreakStatement* clone = ast_pool -> GenBreakStatement();
    clone -> break_token = break_token;
    clone -> identifier_token_opt = identifier_token_opt;

    clone -> break_token_string = break_token_string;
    clone -> identifier_token_opt_string = identifier_token_opt_string;

    clone -> semicolon_token = semicolon_token;
    clone -> nesting_level = nesting_level;
    return clone;
}

Ast* AstContinueStatement::Clone(StoragePool* ast_pool)
{
    AstContinueStatement* clone = ast_pool -> GenContinueStatement();
    clone -> continue_token = continue_token;

    clone -> continue_token_string = continue_token_string;

    clone -> identifier_token_opt = identifier_token_opt;
    clone -> semicolon_token = semicolon_token;
    clone -> nesting_level = nesting_level;
    return clone;
}

Ast* AstReturnStatement::Clone(StoragePool* ast_pool)
{
    AstReturnStatement* clone = ast_pool -> GenReturnStatement();
    clone -> return_token = return_token;

    clone -> return_token_string = return_token_string;

    if (expression_opt)
        clone -> expression_opt =
            (AstExpression*) expression_opt -> Clone(ast_pool);
    clone -> semicolon_token = semicolon_token;
    return clone;
}

Ast* AstThrowStatement::Clone(StoragePool* ast_pool)
{
    AstThrowStatement* clone = ast_pool -> GenThrowStatement();
    clone -> throw_token = throw_token;

    clone -> throw_token_string = throw_token_string;

    clone -> expression = (AstExpression*) expression -> Clone(ast_pool);
    clone -> semicolon_token = semicolon_token;
    return clone;
}

Ast* AstSynchronizedStatement::Clone(StoragePool* ast_pool)
{
    AstSynchronizedStatement* clone = ast_pool -> GenSynchronizedStatement();
    clone -> synchronized_token = synchronized_token;

    clone -> synchronized_token_string = synchronized_token_string;

    clone -> expression = (AstExpression*) expression -> Clone(ast_pool);
    clone -> block = (AstBlock*) block -> Clone(ast_pool);
    return clone;
}

Ast* AstAssertStatement::Clone(StoragePool* ast_pool)
{
    AstAssertStatement* clone = ast_pool -> GenAssertStatement();
    clone -> assert_token = assert_token;

    clone -> assert_token_string = assert_token_string;

    clone -> condition = (AstExpression*) condition -> Clone(ast_pool);
    if (message_opt)
        clone -> message_opt = (AstExpression*) message_opt -> Clone(ast_pool);
    clone -> semicolon_token = semicolon_token;
    return clone;
}

Ast* AstCatchClause::Clone(StoragePool* ast_pool)
{
    AstCatchClause* clone = ast_pool -> GenCatchClause();
    clone -> catch_token = catch_token;

    clone -> catch_token_string = catch_token_string;

    clone -> formal_parameter =
        (AstFormalParameter*) formal_parameter -> Clone(ast_pool);
    clone -> block = (AstBlock*) block -> Clone(ast_pool);
    return clone;
}

Ast* AstFinallyClause::Clone(StoragePool* ast_pool)
{
    AstFinallyClause* clone = ast_pool -> GenFinallyClause();
    clone -> finally_token = finally_token;

    clone -> finally_token_string = finally_token_string;

    clone -> block = (AstBlock*) block -> Clone(ast_pool);
    return clone;
}

Ast* AstTryStatement::Clone(StoragePool* ast_pool)
{
    AstTryStatement* clone = ast_pool -> GenTryStatement();
    clone -> try_token = try_token;

    clone -> try_token_string = try_token_string;

    clone -> block = (AstBlock*) block -> Clone(ast_pool);
    clone -> AllocateCatchClauses(NumCatchClauses());
    for (unsigned i = 0; i < NumCatchClauses(); i++)
        clone -> AddCatchClause((AstCatchClause*) CatchClause(i) ->
                                Clone(ast_pool));
    if (finally_clause_opt)
        clone -> finally_clause_opt =
            (AstFinallyClause*) finally_clause_opt -> Clone(ast_pool);
    return clone;
}

Ast* AstIntegerLiteral::Clone(StoragePool* ast_pool)
{
    AstIntegerLiteral *clone = ast_pool -> GenIntegerLiteral(integer_literal_token);

    clone->integer_literal_token_string = integer_literal_token_string;
    clone->symbol = symbol;

    return clone;
}

Ast* AstLongLiteral::Clone(StoragePool* ast_pool)
{
    AstLongLiteral *clone = ast_pool -> GenLongLiteral(long_literal_token);

    clone->long_literal_token_string = long_literal_token_string;
    clone->symbol = symbol;

    return clone;
}

Ast* AstFloatLiteral::Clone(StoragePool* ast_pool)
{
    AstFloatLiteral *clone = ast_pool -> GenFloatLiteral(float_literal_token);

    clone->float_literal_token_string = float_literal_token_string;
    clone->symbol = symbol;

    return clone;
}

Ast* AstDoubleLiteral::Clone(StoragePool* ast_pool)
{
    AstDoubleLiteral*clone = ast_pool -> GenDoubleLiteral(double_literal_token);

    clone->double_literal_token_string = double_literal_token_string;
    clone->symbol = symbol;

    return clone;
}

Ast* AstTrueLiteral::Clone(StoragePool* ast_pool)
{
    AstTrueLiteral *clone = ast_pool -> GenTrueLiteral(true_literal_token);

    clone->true_literal_token_string = true_literal_token_string;
    clone->symbol = symbol;

    return clone;
}

Ast* AstFalseLiteral::Clone(StoragePool* ast_pool)
{
    AstFalseLiteral *clone = ast_pool -> GenFalseLiteral(false_literal_token);

    clone->false_literal_token_string = false_literal_token_string;
    clone->symbol = symbol;

    return clone;
}

Ast* AstStringLiteral::Clone(StoragePool* ast_pool)
{
    AstStringLiteral *clone = ast_pool -> GenStringLiteral(string_literal_token);

    clone->string_literal_token_string = string_literal_token_string;
    clone->symbol = symbol;
	
    return clone;
}

Ast* AstCharacterLiteral::Clone(StoragePool* ast_pool)
{
    AstCharacterLiteral *clone = ast_pool -> GenCharacterLiteral(character_literal_token);

    clone->character_literal_token_string = character_literal_token_string;
    clone->symbol = symbol;

    return clone;
}

Ast* AstNullLiteral::Clone(StoragePool* ast_pool)
{
	AstNullLiteral *clone = ast_pool -> GenNullLiteral(null_token);
	
	clone->null_token_string = null_token_string;
    	clone->symbol = symbol;
	
	return clone;
}

Ast* AstClassLiteral::Clone(StoragePool* ast_pool)
{
    AstClassLiteral* clone = ast_pool -> GenClassLiteral(class_token);

    clone->class_token_string = class_token_string;
    clone->symbol = symbol;
	
    clone -> type = (AstTypeName*) type -> Clone(ast_pool);
    if (resolution_opt)
        clone -> resolution_opt =
            (AstExpression*) resolution_opt -> Clone(ast_pool);
    return clone;
}

Ast* AstThisExpression::Clone(StoragePool* ast_pool)
{
    AstThisExpression* clone = ast_pool -> GenThisExpression(this_token);

    clone->this_token_string = this_token_string;
    clone->symbol = symbol;
	
    if (base_opt)
        clone -> base_opt = (AstTypeName*) base_opt -> Clone(ast_pool);
    if (resolution_opt)
        clone -> resolution_opt =
            (AstExpression*) resolution_opt -> Clone(ast_pool);
    return clone;
}

Ast* AstSuperExpression::Clone(StoragePool* ast_pool)
{
    AstSuperExpression* clone = ast_pool -> GenSuperExpression(super_token);

    clone->super_token_string = super_token_string;
    clone->symbol = symbol;
	
    if (base_opt)
        clone -> base_opt = (AstTypeName*) base_opt -> Clone(ast_pool);
    if (resolution_opt)
        clone -> resolution_opt =
            (AstExpression*) resolution_opt -> Clone(ast_pool);
    return clone;
}

Ast* AstParenthesizedExpression::Clone(StoragePool* ast_pool)
{
    AstParenthesizedExpression* clone =
        ast_pool -> GenParenthesizedExpression();
    clone -> left_parenthesis_token = left_parenthesis_token;
    clone -> expression = (AstExpression*) expression -> Clone(ast_pool);
    clone -> right_parenthesis_token = right_parenthesis_token;

    clone -> left_parenthesis_token_string = left_parenthesis_token_string;
    clone -> right_parenthesis_token_string = right_parenthesis_token_string;
    clone->symbol = symbol;
	
    return clone;
}

Ast* AstClassCreationExpression::Clone(StoragePool* ast_pool)
{
    AstClassCreationExpression* clone =
        ast_pool -> GenClassCreationExpression();
    if (base_opt)
        clone -> base_opt = (AstExpression*) base_opt -> Clone(ast_pool);
    clone -> new_token = new_token;

    clone -> new_token_string = new_token_string;
    clone->symbol = symbol;
	
    if (type_arguments_opt)
        clone -> type_arguments_opt =
            (AstTypeArguments*) type_arguments_opt -> Clone(ast_pool);
    clone -> class_type = (AstTypeName*) class_type -> Clone(ast_pool);
    clone -> arguments = (AstArguments*) arguments -> Clone(ast_pool);
    if (class_body_opt)
        clone -> class_body_opt =
            (AstClassBody*) class_body_opt -> Clone(ast_pool);
    if (resolution_opt)
        clone -> resolution_opt =
            (AstClassCreationExpression*) resolution_opt -> Clone(ast_pool);
    return clone;
}

Ast* AstDimExpr::Clone(StoragePool* ast_pool)
{
    AstDimExpr* clone = ast_pool -> GenDimExpr();
    clone -> left_bracket_token = left_bracket_token;
    clone -> expression = (AstExpression*) expression -> Clone(ast_pool);
    clone -> right_bracket_token = right_bracket_token;
    return clone;
}

Ast* AstArrayCreationExpression::Clone(StoragePool* ast_pool)
{
    AstArrayCreationExpression* clone =
        ast_pool -> GenArrayCreationExpression();
    clone -> new_token = new_token;

    clone -> new_token_string = new_token_string;
    clone->symbol = symbol;
	
    clone -> array_type = (AstType*) array_type -> Clone(ast_pool);
    clone -> AllocateDimExprs(NumDimExprs());
    for (unsigned i = 0; i < NumDimExprs(); i++)
        clone -> AddDimExpr((AstDimExpr*) DimExpr(i) -> Clone(ast_pool));
    if (brackets_opt)
        clone -> brackets_opt = (AstBrackets*) brackets_opt -> Clone(ast_pool);
    if (array_initializer_opt)
        clone -> array_initializer_opt =
            (AstArrayInitializer*) array_initializer_opt -> Clone(ast_pool);
    return clone;
}

Ast* AstFieldAccess::Clone(StoragePool* ast_pool)
{
    AstFieldAccess* clone = ast_pool -> GenFieldAccess();
    clone -> base = (AstExpression*) base -> Clone(ast_pool);
    clone -> identifier_token = identifier_token;

    clone -> identifier_token_string = identifier_token_string;
    clone->symbol = symbol;
	
    if (resolution_opt)
        clone -> resolution_opt =
            (AstExpression*) resolution_opt -> Clone(ast_pool);
    return clone;
}

Ast* AstMethodInvocation::Clone(StoragePool* ast_pool)
{
    AstMethodInvocation* clone =
        ast_pool -> GenMethodInvocation(identifier_token);
    if (base_opt)
        clone -> base_opt = (AstExpression*) base_opt -> Clone(ast_pool);
    if (type_arguments_opt)
        clone -> type_arguments_opt =
            (AstTypeArguments*) type_arguments_opt -> Clone(ast_pool);
    clone -> identifier_token = identifier_token;

    clone -> identifier_token_string = identifier_token_string;
    clone->symbol = symbol;
	
    clone -> arguments = (AstArguments*) arguments -> Clone(ast_pool);
    if (resolution_opt)
        clone -> resolution_opt =
            (AstExpression*) resolution_opt -> Clone(ast_pool);
    return clone;
}

Ast* AstArrayAccess::Clone(StoragePool* ast_pool)
{
    AstArrayAccess* clone = ast_pool -> GenArrayAccess();
    clone -> base = (AstExpression*) base -> Clone(ast_pool);
    clone -> left_bracket_token = left_bracket_token;
    clone -> expression = (AstExpression*) expression -> Clone(ast_pool);
    clone -> right_bracket_token = right_bracket_token;

    clone->symbol = symbol;
	
    return clone;
}

Ast* AstPostUnaryExpression::Clone(StoragePool* ast_pool)
{
    AstPostUnaryExpression* clone =
        ast_pool -> GenPostUnaryExpression((PostUnaryExpressionTag) other_tag);
    clone -> expression = (AstExpression*) expression -> Clone(ast_pool);
    clone -> post_operator_token = post_operator_token;

    clone -> post_operator_token_string = post_operator_token_string;
    clone->symbol = symbol;
	
    return clone;
}

Ast* AstPreUnaryExpression::Clone(StoragePool* ast_pool)
{
    AstPreUnaryExpression* clone =
        ast_pool -> GenPreUnaryExpression((PreUnaryExpressionTag) other_tag);
    clone -> pre_operator_token = pre_operator_token;

    clone -> pre_operator_token_string = pre_operator_token_string;
    clone->symbol = symbol;
	
    clone -> expression = (AstExpression*) expression -> Clone(ast_pool);
    return clone;
}

Ast* AstCastExpression::Clone(StoragePool* ast_pool)
{
    AstCastExpression* clone = ast_pool -> GenCastExpression();
    clone -> left_parenthesis_token = left_parenthesis_token;
    if (type)
        clone -> type = (AstType*) type -> Clone(ast_pool);
    clone -> right_parenthesis_token = right_parenthesis_token;
    clone -> expression = (AstExpression*) expression -> Clone(ast_pool);

    clone->symbol = symbol;
	
    return clone;
}

Ast* AstBinaryExpression::Clone(StoragePool* ast_pool)
{
    AstBinaryExpression* clone =
        ast_pool -> GenBinaryExpression((BinaryExpressionTag) other_tag);
    clone -> left_expression =
        (AstExpression*) left_expression -> Clone(ast_pool);
    clone -> binary_operator_token = binary_operator_token;

    clone -> binary_operator_token_string = binary_operator_token_string;
    clone->symbol = symbol;
    clone->conjoint = conjoint;
	
    clone -> right_expression =
        (AstExpression*) right_expression -> Clone(ast_pool);
    return clone;
}

Ast* AstInstanceofExpression::Clone(StoragePool* ast_pool)
{
    AstInstanceofExpression* clone = ast_pool -> GenInstanceofExpression();
    clone -> expression = (AstExpression*) expression -> Clone(ast_pool);
    clone -> instanceof_token = instanceof_token;

    clone -> instanceof_token_string = instanceof_token_string;
    clone->symbol = symbol;
	
    clone -> type = (AstType*) type -> Clone(ast_pool);
    return clone;
}

Ast* AstConditionalExpression::Clone(StoragePool* ast_pool)
{
    AstConditionalExpression* clone = ast_pool -> GenConditionalExpression();
    clone -> test_expression =
        (AstExpression*) test_expression -> Clone(ast_pool);
    clone -> question_token = question_token;
    clone -> true_expression =
        (AstExpression*) true_expression -> Clone(ast_pool);
    clone -> colon_token = colon_token;

    clone->symbol = symbol;

    clone -> false_expression =
        (AstExpression*) false_expression -> Clone(ast_pool);
    return clone;
}

Ast* AstAssignmentExpression::Clone(StoragePool* ast_pool)
{
    AstAssignmentExpression* clone = ast_pool ->
        GenAssignmentExpression((AssignmentExpressionTag) other_tag,
                                assignment_operator_token);
    clone -> left_hand_side =
        (AstExpression*) left_hand_side -> Clone(ast_pool);
    clone -> expression = (AstExpression*) expression -> Clone(ast_pool);

    clone->assignment_operator_token_string = assignment_operator_token_string;
    clone->symbol = symbol;
	
    return clone;
}

//
// GoF detection functions
//

bool Ast::isPrimitiveType(wchar_t* type)
{
	if ( (wcscmp(type, L"byte") == 0)
	   ||(wcscmp(type, L"short") == 0)
	   ||(wcscmp(type, L"int") == 0) 
   	   ||(wcscmp(type, L"long") == 0) 
	   ||(wcscmp(type, L"float") == 0) 
	   ||(wcscmp(type, L"double") == 0) 
	   ||(wcscmp(type, L"char") == 0) 
	   ||(wcscmp(type, L"boolean") == 0) 
	   ||(wcscmp(type, L"void") == 0) 
	   ||(wcscmp(type, L"String") == 0) )
	   	return true;
	else
		return false;          
}
void AstClassDeclaration::PrintGeneralization(GenTable* gen_table, wchar_t* package_name, LexStream& lex_stream)
{
	if (!GoFTag)
	{
	    	wchar_t* class_name =  const_cast<wchar_t*>(lex_stream.NameString(class_body -> identifier_token));
    		wchar_t* super_name = (super_opt) ? const_cast<wchar_t*>(lex_stream.NameString(DYNAMIC_CAST<AstName*> (super_opt -> name) -> identifier_token)) : NULL;

    		unsigned i;
    		vector<wchar_t*>* interfaces = NULL;
    		for (i = 0; i < NumInterfaces(); i++)
    		{
        		if (interfaces == NULL)
				interfaces = new vector<wchar_t*>();
  	 		interfaces -> push_back(const_cast<wchar_t*>(lex_stream.NameString(Interface(i) -> name -> identifier_token)));
    		}

		Gen::Kind kind;
		if (isAbstract(lex_stream))
			kind = Gen::ABSTRACT;
		else if (isFinal(lex_stream))
			kind = Gen::FINAL;
		else
			kind = Gen::CLASS;
		
    		gen_table -> addGeneralization(package_name, class_name, super_name, interfaces, kind, lex_stream.FileName());

    		unsigned j;
    		for (j = 0; j < class_body -> NumClassBodyDeclarations(); j++)
    		{
    			switch(class_body -> ClassBodyDeclaration(j) -> kind)
      			{
				case Ast::CLASS:
				case Ast::INTERFACE:
					class_body -> ClassBodyDeclaration(j) -> PrintGeneralization(gen_table, package_name, lex_stream);
					break;
             			default:
             				break;
       		}
    		}	
    		GoFTag = true;
	}
} 

void AstInterfaceDeclaration::PrintGeneralization(GenTable* gen_table, wchar_t* package_name, LexStream& lex_stream)
{
	if (!GoFTag)
	{
    		wchar_t* interface_name =  const_cast<wchar_t*>(lex_stream.NameString(class_body -> identifier_token));

    		unsigned i;
    		vector<wchar_t*>* interfaces = NULL;
    		for (i = 0; i < NumInterfaces(); i++)
    		{
        		if (interfaces == NULL)
				interfaces = new vector<wchar_t*>();
  	 		interfaces -> push_back(const_cast<wchar_t*>(lex_stream.NameString(Interface(i) -> name -> identifier_token)));
    		}

    		gen_table -> addGeneralization(package_name, interface_name, NULL, interfaces, Gen::INTERFACE, lex_stream.FileName());

    		unsigned j;
    		for (j = 0; j < class_body -> NumClassBodyDeclarations(); j++)
    		{
    			switch(class_body -> ClassBodyDeclaration(j) -> kind)
      			{
				case Ast::CLASS:
				case Ast::INTERFACE:
					class_body -> ClassBodyDeclaration(j) -> PrintGeneralization(gen_table, package_name, lex_stream);
					break;
             			default:
             				break;
       		}
    		}
    		GoFTag = true;
	}
} 

void AstFieldDeclaration::PrintAssociation(AssocTable* assoc_table, wchar_t* package_name, wchar_t* class_name, LexStream& lex_stream)
{
    wchar_t* type_name = const_cast<wchar_t*>(lex_stream.NameString(type -> IdentifierToken()));

    if (!isPrimitiveType(type_name))
    	{
	    unsigned i;
	    for (i = 0; i < NumVariableDeclarators(); i++)
	    {
        	wchar_t* var_name = const_cast<wchar_t*>(lex_stream.NameString(VariableDeclarator(i) -> variable_declarator_name -> identifier_token));
	        if (!modifiers_opt)
       	 {
        		assoc_table -> addAssociation(Assoc::IM, Assoc::PRIVATE, var_name, type_name, package_name, class_name, NULL);
	        }
		 else
	 	 {
	 		Assoc::Mode mode;
              	if (wcscmp(lex_stream.NameString(DYNAMIC_CAST<AstModifierKeyword*> (modifiers_opt -> Modifier(0)) -> modifier_token), L"private") == 0)
				mode = Assoc::PRIVATE;
			else if (wcscmp(lex_stream.NameString(DYNAMIC_CAST<AstModifierKeyword*> (modifiers_opt -> Modifier(0)) -> modifier_token), L"public") == 0)
				mode = Assoc::PUBLIC;
			else if (wcscmp(lex_stream.NameString(DYNAMIC_CAST<AstModifierKeyword*> (modifiers_opt -> Modifier(0)) -> modifier_token), L"protected") == 0)		
				mode = Assoc::PROTECTED;
			else
				mode = Assoc::PRIVATE;

			if ((modifiers_opt -> NumModifiers() == 2) && (wcscmp(lex_stream.NameString(DYNAMIC_CAST<AstModifierKeyword*> (modifiers_opt -> Modifier(1)) -> modifier_token), L"static") == 0))
				assoc_table -> addAssociation(Assoc::CF, mode, var_name, type_name, package_name, class_name, NULL);
			else if ((modifiers_opt -> NumModifiers() == 1) && (wcscmp(lex_stream.NameString(DYNAMIC_CAST<AstModifierKeyword*> (modifiers_opt -> Modifier(0)) -> modifier_token), L"static") == 0))
				assoc_table -> addAssociation(Assoc::CF, Assoc::PRIVATE, var_name, type_name, package_name, class_name, NULL);
			else
				assoc_table -> addAssociation(Assoc::IM, mode, var_name, type_name, package_name, class_name, NULL);
		 }
	    }
    	}
}

void AstConstructorDeclaration::PrintAssociation(AssocTable* assoc_table, wchar_t* package_name, wchar_t* class_name, LexStream& lex_stream)
{
    // Print(lex_stream);
}

void AstMethodDeclaration::PrintAssociation(AssocTable* assoc_table, wchar_t* package_name, wchar_t* class_name, LexStream& lex_stream)
{
if (!GoFTag)
{
	wchar_t* method_name = const_cast<wchar_t*>(lex_stream.NameString(method_declarator -> identifier_token));
    	wchar_t* type_name = const_cast<wchar_t*>(lex_stream.NameString(type -> IdentifierToken()));

	Assoc::Kind kind;
	Assoc::Mode mode;
    	if (!isPrimitiveType(type_name))
    	{
    		if (modifiers_opt)
    		{
    			if (wcscmp(lex_stream.NameString(dynamic_cast<AstModifierKeyword*>(modifiers_opt -> Modifier(0)) -> modifier_token), L"private") == 0)
	    			mode = Assoc::PRIVATE;
			else if (wcscmp(lex_stream.NameString(dynamic_cast<AstModifierKeyword*>(modifiers_opt -> Modifier(0)) -> modifier_token), L"public") == 0)
				mode = Assoc::PUBLIC;
			else if (wcscmp(lex_stream.NameString(dynamic_cast<AstModifierKeyword*>(modifiers_opt -> Modifier(0)) -> modifier_token), L"protected") == 0)
				mode = Assoc::PROTECTED;
			else if (wcscmp(lex_stream.NameString(dynamic_cast<AstModifierKeyword*>(modifiers_opt -> Modifier(0)) -> modifier_token), L"static") == 0)
			{
				mode = Assoc::PRIVATE;
				kind = Assoc::CM;
			}

			if ((modifiers_opt -> NumModifiers() > 1)
			&& (wcscmp(lex_stream.NameString(dynamic_cast<AstModifierKeyword*>(modifiers_opt ->Modifier(1)) -> modifier_token), L"static") == 0))
			{
				kind = Assoc::CM;
			}
			else
			{
				kind = Assoc::MR;			
			}
    		}			
		else
		{
			mode = Assoc::PRIVATE;
			kind = Assoc::MR;
		}

    		assoc_table -> addAssociation(kind, mode, method_name, type_name, package_name, class_name, method_name);
	}
    	method_declarator -> PrintAssociation(assoc_table, package_name, class_name, lex_stream);
	if (method_body_opt)
		method_body_opt -> PrintAssociation(assoc_table, package_name, class_name, method_name, lex_stream);
}
}

void AstMethodDeclarator::PrintAssociation(AssocTable* assoc_table, wchar_t* package_name, wchar_t* class_name, LexStream& lex_stream)
{
    wchar_t* method_name = const_cast<wchar_t*>(lex_stream.NameString(identifier_token));

    unsigned i;
    for (i = 0; i < NumFormalParameters(); i++)
    {
        wchar_t* type_name = const_cast<wchar_t*>(lex_stream.NameString(FormalParameter(i) -> type -> IdentifierToken()));
        wchar_t* arg_name = const_cast<wchar_t*>(lex_stream.NameString(FormalParameter(i) -> formal_declarator -> variable_declarator_name -> identifier_token));

    	if (!isPrimitiveType(type_name))
    		assoc_table -> addAssociation(Assoc::MP, Assoc::PRIVATE, arg_name, type_name, package_name, class_name, method_name);
    }
}

void AstMethodBody::PrintAssociation(AssocTable* assoc_table, wchar_t* package_name, wchar_t* class_name, wchar_t* method_name, LexStream& lex_stream)
{
	AstBlock::PrintAssociation(assoc_table, package_name, class_name, method_name, lex_stream);
}

void AstBlock::PrintAssociation(AssocTable* assoc_table, wchar_t* package_name, wchar_t* class_name, wchar_t* method_name, LexStream& lex_stream)
{
	if (NumStatements() > 0)
	{
    		unsigned i;
		for (i = 0; i <  NumStatements(); i++)
			Statement(i) -> PrintAssociation(assoc_table, package_name, class_name, method_name, lex_stream);			
	}
}

void AstSynchronizedStatement::PrintAssociation(AssocTable* assoc_table, wchar_t* package_name, wchar_t* class_name, wchar_t* method_name, LexStream& lex_stream)
{
	block -> PrintAssociation(assoc_table, package_name, class_name, method_name, lex_stream);		
}

void AstIfStatement::PrintAssociation(AssocTable* assoc_table, wchar_t* package_name, wchar_t* class_name, wchar_t* method_name, LexStream& lex_stream)
{
	true_statement -> PrintAssociation(assoc_table, package_name, class_name, method_name, lex_stream);		
}

void AstForStatement::PrintAssociation(AssocTable* assoc_table, wchar_t* package_name, wchar_t* class_name, wchar_t* method_name, LexStream& lex_stream)
{
	statement -> PrintAssociation(assoc_table, package_name, class_name, method_name, lex_stream);	
}

void AstWhileStatement::PrintAssociation(AssocTable* assoc_table, wchar_t* package_name, wchar_t* class_name, wchar_t* method_name, LexStream& lex_stream)
{
	statement -> PrintAssociation(assoc_table, package_name, class_name, method_name, lex_stream);	
}

void AstAssignmentExpression::PrintAssociation(AssocTable* assoc_table, wchar_t* package_name, wchar_t* class_name, wchar_t* method_name, LexStream& lex_stream)
{
	expression-> PrintAssociation(assoc_table, package_name, class_name, method_name, lex_stream);	
}

void AstReturnStatement::PrintAssociation(AssocTable* assoc_table, wchar_t* package_name, wchar_t* class_name, wchar_t* method_name, LexStream& lex_stream)
{
	if (expression_opt)
		expression_opt -> PrintAssociation(assoc_table, package_name, class_name, method_name, lex_stream);	
}

void AstCastExpression::PrintAssociation(AssocTable* assoc_table, wchar_t* package_name, wchar_t* class_name, wchar_t* method_name, LexStream& lex_stream)
{
	expression -> PrintAssociation(assoc_table, package_name, class_name, method_name, lex_stream);	
}

void AstConditionalExpression::PrintAssociation(AssocTable* assoc_table, wchar_t* package_name, wchar_t* class_name, wchar_t* method_name, LexStream& lex_stream)
{
	true_expression -> PrintAssociation(assoc_table, package_name, class_name, method_name, lex_stream);	
	false_expression -> PrintAssociation(assoc_table, package_name, class_name, method_name, lex_stream);	
}

void AstClassCreationExpression::PrintAssociation(AssocTable* assoc_table, wchar_t* package_name, wchar_t* class_name, wchar_t* method_name, LexStream& lex_stream)
{
	wchar_t* type_name = const_cast<wchar_t*>(lex_stream.NameString(class_type -> name -> identifier_token));
	if (!isPrimitiveType(type_name))
		assoc_table -> addAssociation( Assoc::OC, Assoc::PRIVATE, NULL, type_name, package_name, class_name, method_name);
}

void AstMethodInvocation::PrintAssociation(AssocTable* assoc_table, wchar_t* package_name, wchar_t* class_name, wchar_t* method_name, LexStream& lex_stream)
{
	wchar_t* caller_name = NULL;

	if (base_opt)
	{
		if (base_opt -> kind == Ast::NAME)
			caller_name = const_cast<wchar_t*>(lex_stream.NameString(dynamic_cast<AstName*>(base_opt) -> identifier_token));
		else if (base_opt -> kind == Ast::THIS_EXPRESSION)
			caller_name = const_cast<wchar_t*>(lex_stream.NameString(dynamic_cast<AstThisExpression*>(base_opt) -> this_token));
		else if (base_opt -> kind == Ast::CAST)
		{
			base_opt -> Print(lex_stream);
		}
	}

	wchar_t* call_method = const_cast<wchar_t*>(lex_stream.NameString(identifier_token));

	assoc_table -> addAssociation( Assoc::MI, Assoc::PRIVATE, call_method, caller_name, package_name, class_name, method_name);
}

bool AstBlock::returnsVar(wchar_t* var_name)
{
	if (NumStatements() > 0)
    	{
    		unsigned i = 0;
		while ((i <  NumStatements())  && (!Statement(i) -> returnsVar(var_name)))
			i++;
		return (i < NumStatements()) ? true : false;
    	}
    	return
		false;
}

bool AstIfStatement::returnsVar(wchar_t * var_name)
{
	return ((true_statement -> returnsVar(var_name)) || (false_statement_opt && false_statement_opt -> returnsVar(var_name)));
}

bool AstReturnStatement::returnsVar(wchar_t* var_name)
{
	if (expression_opt && (expression_opt -> kind == NAME))
	{
		AstName* name = dynamic_cast<AstName*>(expression_opt);
		return (wcscmp(name -> identifier_token_string, var_name) == 0) ? true : false;
	}
	else
		return false;
}

bool AstAssignmentExpression::lhs(VariableSymbol *vsym)
{
	if ((left_hand_side -> kind == Ast::NAME) && (left_hand_side -> NameCast() -> symbol -> Kind() == Symbol::VARIABLE))
		return (left_hand_side -> NameCast() -> symbol -> VariableCast() == vsym);
	return false;
}

TypeSymbol* AstBlock::returnsType()
{
  if (NumStatements() > 0)
    {
      TypeSymbol *type;
      unsigned i = 0;
      while ((i <  NumStatements())  && (!(type = Statement(i) -> returnsType())))
	i++;
      return (i < NumStatements()) ? type : NULL;
    }
  return NULL;
}

TypeSymbol* AstReturnStatement::returnsType()
{
  // check for two things, expression_opt is either a CLASS_CREATION (which checks for CAST first) or NAME
  if (expression_opt -> kind == Ast::CAST)
  	{
      		if (expression_opt -> CastExpressionCast() -> expression -> kind == Ast::CLASS_CREATION)
		 {
			AstClassCreationExpression *class_creation = 
				expression_opt -> CastExpressionCast() -> expression -> ClassCreationExpressionCast();
		  	return (class_creation -> class_type -> symbol) ? class_creation -> class_type -> symbol -> TypeCast() : NULL;
      		}
		else if (expression_opt -> CastExpressionCast() -> expression -> kind == Ast::NAME)
		{
			return expression_opt -> CastExpressionCast() -> expression -> Type();
		}
  	}
  return NULL;
}


int AstBlock::NumExecutionPaths()
{
	int ct = 0;
	unsigned i;
	for (i = 0; i< NumStatements(); i++)
	{
		ct += Statement(i) -> NumExecutionPaths();
	}
	return ct;
}

int AstIfStatement::NumExecutionPaths()
{
	return true_statement -> NumExecutionPaths() + ((false_statement_opt)? false_statement_opt -> NumExecutionPaths():0);
}

int AstReturnStatement::NumExecutionPaths()
{
	if (!expression_opt)
		return 1;
	else if ((expression_opt -> kind == Ast::NAME)
	|| (expression_opt -> kind == Ast::CALL)
	|| (expression_opt -> kind == Ast::CAST))
		return 1;
	else if (expression_opt -> kind == Ast::CONDITIONAL)
		return 2;
	else
		return 0;		
}

int AstBlock::NumDelegation(wchar_t* var_name, wchar_t* method_name, int argn)
{
	int ct = 0;
	unsigned i;
	for (i = 0; i< NumStatements(); i++)
	{
		ct += Statement(i) -> NumDelegation(var_name, method_name, argn);
	}
	return ct;
}

int AstIfStatement::NumDelegation(wchar_t* var_name, wchar_t* method_name, int argn)
{
	return true_statement -> NumDelegation(var_name, method_name, argn)
							+ ((false_statement_opt)? false_statement_opt -> NumDelegation(var_name, method_name, argn):0);
}

int AstReturnStatement::NumDelegation(wchar_t* var_name, wchar_t* method_name, int argn)
{
	if (expression_opt)
		return expression_opt -> NumDelegation(var_name, method_name, argn);
	else
		return 0;		
}

int AstCastExpression::NumDelegation(wchar_t* var_name, wchar_t* method_name, int argn)
{
	return expression -> NumDelegation(var_name, method_name, argn);
}

int AstConditionalExpression::NumDelegation(wchar_t* var_name, wchar_t* method_name, int argn)
{
	return true_expression -> NumDelegation(var_name, method_name, argn) 
				+ false_expression -> NumDelegation(var_name, method_name, argn);
}

int AstMethodInvocation::NumDelegation(wchar_t* var_name, wchar_t* method_name, int argn)
{
	wchar_t* caller_name = getCaller();
	wchar_t* callee_name = getCallee();

	if (((caller_name) && (wcscmp(caller_name, var_name) == 0))
	&& (callee_name && (wcscmp(callee_name, method_name) == 0))
	&& (NumArguments() == argn))
		return 1;
	else
		return 0;
}

int AstBlock::NumDelegation()
{
	int ct = 0;
	unsigned i;
	for (i = 0; i< NumStatements(); i++)
	{
		ct += Statement(i) -> NumDelegation();
	}
	return ct;
}

int AstIfStatement::NumDelegation()
{
	return true_statement -> NumDelegation()
							+ ((false_statement_opt)? false_statement_opt -> NumDelegation():0);
}

int AstReturnStatement::NumDelegation()
{
	if (expression_opt)
		return expression_opt -> NumDelegation();
	else
		return 0;		
}

int AstCastExpression::NumDelegation()
{
	return expression -> NumDelegation();
}

int AstConditionalExpression::NumDelegation()
{
	return true_expression -> NumDelegation() 
				+ false_expression -> NumDelegation();
}

int AstMethodInvocation::NumDelegation()
{
	return 1;
}



wchar_t* AstMethodInvocation::getCaller()
{
	if (base_opt)
	{
		if (base_opt -> kind == Ast::NAME)
			return dynamic_cast<AstName*>(base_opt) -> identifier_token_string;
		else if (base_opt -> kind == Ast::THIS_EXPRESSION)
			return dynamic_cast<AstThisExpression*>(base_opt) -> this_token_string;
	}
	return NULL;		
}

void AstBlock::simulate(EnvTable *env)
{
	unsigned i;
	if (NumStatements() > 0)
    	{
       	for (i = 0; i < NumStatements(); i++)
            		Statement(i) -> simulate(env);
	}
}

void AstIfStatement::simulate(EnvTable *env)
{
	if (expression -> eval(env))
		true_statement -> simulate(env);
	else if (false_statement_opt)
		false_statement_opt -> simulate(env);	
}

void AstAssignmentExpression::simulate(EnvTable *env)
{
	// Consider only x = expr

	if (left_hand_side -> kind == Ast::NAME)
	{
		wchar_t* var = dynamic_cast<AstName*>(left_hand_side) -> identifier_token_string;		
		env -> changeState(var, Env::MOD);
	}
	
}

bool AstBinaryExpression::eval(EnvTable* env)
{
	if ((left_expression -> kind == Ast::NAME)
	&& (wcscmp(binary_operator_token_string, L"==") == 0)
	&& (right_expression -> kind == Ast::NULL_LITERAL))
	{
		wchar_t* var = dynamic_cast<AstName*>(left_expression) -> identifier_token_string;
		return (env -> getState(var) == Env::NIL) ? true : false;
	}
	return false;
}

vector<wchar_t*>* AstMethodBody::getVariables(wchar_t* type_name)
{
	vector<wchar_t*>* variables = NULL;
	unsigned i = 0;
	while(!variables && (i < NumStatements()))
	{
		if (Statement(i) -> kind == Ast::LOCAL_VARIABLE_DECLARATION)
		{
			AstLocalVariableStatement* var_statement  = dynamic_cast<AstLocalVariableStatement*>(Statement(i));
			if (wcscmp(type_name, var_statement -> type -> IdentifierTokenString()) == 0)			
				variables = var_statement -> getVarNames();
			else
				i++;
		}
		else
			i++;
	}
	return variables;
}

Statechart* AstMethodBody::getStatechart(wchar_t* var)
{
	Statechart *statechart = new Statechart();
	unsigned i;
	for (i = 0; i < NumStatements(); i++)
	{
		State *state = Statement(i) -> getState(var);
		if (state)
			statechart -> addState(state);
	}
	return statechart;
}

State* AstBlock::getState(wchar_t* var)
{
	//unsigned i;
	if (NumStatements() > 0)
    	{
            		return Statement(0) -> getState(var);
	}
	return NULL;
}

State* AstIfStatement::getState(wchar_t *var)
{	
	if (expression -> kind == Ast::BINARY)
	{
		AstBinaryExpression* binexp = dynamic_cast<AstBinaryExpression*>(expression);
		if ((binexp -> left_expression -> kind == Ast::NAME)
		&& (wcscmp(dynamic_cast<AstName*>( binexp -> left_expression) -> identifier_token_string, var) == 0)
		&& (binexp -> right_expression -> kind == Ast::NULL_LITERAL))
		{
			vector<wchar_t*>* plist = new vector<wchar_t*>();
			plist -> push_back(binexp -> binary_operator_token_string);
			plist -> push_back(dynamic_cast<AstNullLiteral*>(binexp->right_expression) -> null_token_string);
			State *state = new State(State::CONDITION, plist);
			state -> addTrueBranch(true_statement -> getState(var));
			if (false_statement_opt)
				state -> addFalseBranch(false_statement_opt -> getState(var));	
			return state;
		}
	}
	return NULL;
}

State* AstAssignmentExpression::getState(wchar_t *var)
{
	// Consider only x = expr

	if (left_hand_side -> kind == Ast::NAME)
	{
		wchar_t* current = dynamic_cast<AstName*>(left_hand_side) -> identifier_token_string;
		if (wcscmp(var, current) == 0)
		{
			if (expression -> kind == Ast::CLASS_CREATION)
			{
				AstClassCreationExpression* creation = dynamic_cast<AstClassCreationExpression*>(expression);
				vector<wchar_t*> *plist = new vector<wchar_t*>();
				plist -> push_back(creation -> class_type -> name -> identifier_token_string);
				return new State(State::CREATE, plist);
			}
			else if (expression -> kind == Ast::CALL)
			{
				AstMethodInvocation* call = dynamic_cast<AstMethodInvocation*>(expression);
				vector<wchar_t*> *plist = new vector<wchar_t*>();
				plist -> push_back(call -> identifier_token_string);
				return new State(State::SET, plist);

			}
		}
	}
	return NULL;
}

State* AstMethodInvocation::getState(wchar_t* var)
{
	wchar_t* caller = (base_opt && (base_opt -> kind == Ast::NAME))
		? dynamic_cast<AstName*>(base_opt) -> identifier_token_string
		: NULL;

	vector<wchar_t*>* params = NULL;
	unsigned i;
	for (i = 0; i < arguments -> NumArguments(); i++)
	{
		if (arguments -> Argument(i) -> kind == Ast::NAME)
		{
			if (!params)
				params = new vector<wchar_t*>();
			params -> push_back(dynamic_cast<AstName*>(arguments -> Argument(i)) -> identifier_token_string);
		}
	}

	if (caller 
	&& (wcscmp(caller, var)==0)
	&& params)
	{
		return new State(State::SET, params);
	}
	else if (params)
	{
		i = 0;
		while ((i < params -> size()) && (wcscmp((*params)[i], var) != 0))
			i++;
		if (i < params -> size())
		{
			vector<wchar_t*> *plist = new vector<wchar_t*>();
			plist -> push_back(caller);
			return new State(State::GET, plist); 
		}	
	}
	return NULL;
}

State* AstReturnStatement::getState(wchar_t* var)
{
	if (expression_opt && (expression_opt -> kind == NAME))
	{
		AstName* name = dynamic_cast<AstName*>(expression_opt);
		if (wcscmp(name -> identifier_token_string, var) == 0)		
			return new State(State::RETURN, NULL);			
	}
	return NULL;
}

State* AstLocalVariableStatement::getState(wchar_t* var)
{	
	unsigned i = 0;
	while (i < NumVariableDeclarators())
	{
		if (wcscmp(VariableDeclarator(i) -> variable_declarator_name -> identifier_token_string, var) == 0)
		{
			return VariableDeclarator(i) -> getState(var);			
		}
		else
			i++;
	}
	return NULL;
}

State* AstVariableDeclarator::getState(wchar_t* var)
{
	if (variable_initializer_opt)
	{
		if ((variable_initializer_opt -> kind == Ast::CAST)
		&& (dynamic_cast<AstCastExpression*>(variable_initializer_opt) -> expression -> kind == Ast::CALL))
		{
			AstMethodInvocation* call = dynamic_cast<AstMethodInvocation*>(dynamic_cast<AstCastExpression*>(variable_initializer_opt) -> expression);

			if ((call -> base_opt) && (call -> base_opt -> kind == Ast::NAME))
			{
				vector<wchar_t*>* plist = new vector<wchar_t*>();
				plist -> push_back(dynamic_cast<AstName*>(call -> base_opt) -> identifier_token_string);
				return new State(State::SET, plist);					     
			}
		}
	}
	return NULL;
}

vector<wchar_t*>* AstLocalVariableStatement::getVarNames()
{
	vector<wchar_t*>* list = new vector<wchar_t*>();	
	unsigned i;
	for (i = 0; i < NumVariableDeclarators(); i++)
		list -> push_back(VariableDeclarator(i) -> variable_declarator_name -> identifier_token_string);
	return list;
}

bool AstMethodDeclaration::isSynchronized()
{
	if (modifiers_opt)
	{
		unsigned i = 0;
		while((i < modifiers_opt -> NumModifiers())
			&& (wcscmp(dynamic_cast<AstModifierKeyword*>(modifiers_opt -> Modifier(i)) -> modifier_token_string, L"synchronized") != 0))
			i++;
		return (i < modifiers_opt -> NumModifiers())? true:false;
		
	}
	else
		return false;
}

bool AstClassDeclaration::isAbstract(LexStream& lex_stream)
{
	if (modifiers_opt)
	{
		unsigned i = 0;
		while((i < modifiers_opt -> NumModifiers())
			&& (wcscmp(lex_stream.NameString(dynamic_cast<AstModifierKeyword*>(modifiers_opt -> Modifier(i)) -> modifier_token), L"abstract") != 0))
			i++;
		return (i < modifiers_opt -> NumModifiers())? true:false;
		
	}
	else
		return false;
	
}

bool AstClassDeclaration::isFinal(LexStream& lex_stream)
{
	if (modifiers_opt)
	{
		unsigned i = 0;
		while((i < modifiers_opt -> NumModifiers())
			&& (wcscmp(lex_stream.NameString(dynamic_cast<AstModifierKeyword*>(modifiers_opt -> Modifier(i)) -> modifier_token), L"final") != 0))
			i++;
		return (i < modifiers_opt -> NumModifiers())? true:false;		
	}
	else
		return false;

}

Ast* AstBlock::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstBlock* clone = ast_pool -> GenBlock();
    clone -> CloneBlock(ast_pool, this, lex_stream);



    clone -> label_opt_string = new wchar_t[wcslen(lex_stream.NameString(label_opt)) + 1];
    wcscpy(clone -> label_opt_string, lex_stream.NameString(label_opt));

    clone -> label_opt_string = const_cast<wchar_t*>(lex_stream.NameString(label_opt));

    return clone;
}

void AstBlock::CloneBlock(StoragePool* ast_pool, AstBlock* orig, LexStream& lex_stream)
{
    other_tag = orig -> other_tag;
    label_opt = orig -> label_opt;
    nesting_level = orig -> nesting_level;
    left_brace_token = orig -> left_brace_token;
    unsigned count = orig -> NumStatements();

/**** I think... this should initialized ****/
block_statements = NULL;	
block_symbol = NULL;
    AllocateStatements(count);
    for (unsigned i = 0; i < count; i++)
        AddStatement((AstStatement*) orig -> Statement(i) -> Clone(ast_pool, lex_stream));
    right_brace_token = orig -> right_brace_token;
    no_braces = orig -> no_braces;
}

Ast* AstName::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstName* clone = ast_pool -> GenName(identifier_token);
    if (base_opt)
        clone -> base_opt = (AstName*) base_opt -> Clone(ast_pool, lex_stream);
    if (resolution_opt)
        clone -> resolution_opt =
            (AstExpression*) resolution_opt -> Clone(ast_pool, lex_stream);


    clone -> identifier_token_string = new wchar_t[wcslen(lex_stream.NameString(identifier_token)) + 1];
    wcscpy(clone -> identifier_token_string, lex_stream.NameString(identifier_token));


    clone -> identifier_token_string = const_cast<wchar_t*>(lex_stream.NameString(identifier_token));

    return clone;
}

Ast* AstPrimitiveType::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
	AstPrimitiveType *clone = ast_pool -> GenPrimitiveType(kind, primitive_kind_token);
	clone -> primitive_kind_token_string = const_cast<wchar_t*>(lex_stream.NameString(primitive_kind_token));

    return clone;
}

Ast* AstBrackets::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstBrackets* clone =
        ast_pool -> GenBrackets(left_bracket_token, right_bracket_token);
    clone -> dims = dims;
    return clone;
}

Ast* AstArrayType::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    return ast_pool -> GenArrayType((AstType*) type -> Clone(ast_pool, lex_stream),
                                    ((AstBrackets*) brackets ->
                                     Clone(ast_pool, lex_stream)));
}

Ast* AstWildcard::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstWildcard* clone = ast_pool -> GenWildcard(question_token);
    clone -> extends_token_opt = extends_token_opt;
    clone -> super_token_opt = super_token_opt;
    
    clone -> bounds_opt = (bounds_opt)
		? (AstType*) bounds_opt -> Clone(ast_pool, lex_stream)
		: NULL;


    clone -> question_token_string = new wchar_t[wcslen(lex_stream.NameString(question_token)) + 1];
    clone -> extends_token_opt_string = new wchar_t[wcslen(lex_stream.NameString(extends_token_opt)) + 1];
    clone -> super_token_opt_string = new wchar_t[wcslen(lex_stream.NameString(super_token_opt)) + 1];
    wcscpy(clone -> question_token_string, lex_stream.NameString(question_token));
    wcscpy(clone -> extends_token_opt_string, lex_stream.NameString(extends_token_opt));
    wcscpy(clone -> super_token_opt_string, lex_stream.NameString(super_token_opt));


    clone -> question_token_string = const_cast<wchar_t*>(lex_stream.NameString(question_token));
    clone -> extends_token_opt_string = const_cast<wchar_t*>(lex_stream.NameString(extends_token_opt));
    clone -> super_token_opt_string = const_cast<wchar_t*>(lex_stream.NameString(super_token_opt));

    return clone;
}

Ast* AstTypeArguments::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstTypeArguments* clone = ast_pool -> GenTypeArguments(left_angle_token,
                                                           right_angle_token);
    clone -> AllocateTypeArguments(NumTypeArguments());
    for (unsigned i = 0; i < NumTypeArguments(); i++)
        clone -> AddTypeArgument((AstType*) TypeArgument(i) ->
                                 Clone(ast_pool, lex_stream));
    return clone;
}

Ast* AstTypeName::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstTypeName* clone =
        ast_pool -> GenTypeName((AstName*) name -> Clone(ast_pool, lex_stream));
    
    clone -> base_opt = (base_opt)
		? (AstTypeName*) base_opt -> Clone(ast_pool, lex_stream)
		: NULL;
    
    clone -> type_arguments_opt = (type_arguments_opt)
		? (AstTypeArguments*) type_arguments_opt -> Clone(ast_pool, lex_stream)
		: NULL;
    return clone;
}

Ast* AstMemberValuePair::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstMemberValuePair* clone = ast_pool -> GenMemberValuePair();
    clone -> identifier_token_opt = identifier_token_opt;
    clone -> member_value = (AstMemberValue*) member_value -> Clone(ast_pool, lex_stream);


    clone -> identifier_token_opt_string = new wchar_t[wcslen(lex_stream.NameString(identifier_token_opt)) + 1];
    wcscpy(clone -> identifier_token_opt_string, lex_stream.NameString(identifier_token_opt));


    clone -> identifier_token_opt_string = const_cast<wchar_t*>(lex_stream.NameString(identifier_token_opt));

    return clone;
}

Ast* AstAnnotation::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstAnnotation* clone = ast_pool -> GenAnnotation();
    clone -> at_token = at_token;
    clone -> name = (AstName*) name -> Clone(ast_pool, lex_stream);
    clone -> AllocateMemberValuePairs(NumMemberValuePairs());
    for (unsigned i = 0; i < NumMemberValuePairs(); i++)
        clone -> AddMemberValuePair((AstMemberValuePair*)
                                    MemberValuePair(i) -> Clone(ast_pool, lex_stream));
    clone -> right_paren_token_opt = right_paren_token_opt;
    return clone;
}

Ast* AstModifierKeyword::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
	AstModifierKeyword* clone = ast_pool -> GenModifierKeyword(modifier_token);
    	clone -> modifier_token_string = const_cast<wchar_t*>(lex_stream.NameString(modifier_token));

	return clone;
}

Ast* AstModifiers::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstModifiers* clone = ast_pool -> GenModifiers();
    clone -> modifiers = NULL;
    clone -> AllocateModifiers(NumModifiers());
    for (unsigned i = 0; i < NumModifiers(); i++)
    {
        if (Modifier(i) -> ModifierKeywordCast())
            clone -> AddModifier((AstModifierKeyword*)
                                 Modifier(i) -> Clone(ast_pool, lex_stream));
        else clone -> AddModifier((AstAnnotation*)
                                  Modifier(i) -> Clone(ast_pool, lex_stream));
    }
    clone -> static_token_opt = static_token_opt;
    return clone;
}

Ast* AstPackageDeclaration::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstPackageDeclaration* clone = ast_pool -> GenPackageDeclaration();
    
    clone -> modifiers_opt =(modifiers_opt)
		? (AstModifiers*) modifiers_opt -> Clone(ast_pool, lex_stream)
		: NULL;
    clone -> package_token = package_token;
    clone -> name = (AstName*) name -> Clone(ast_pool, lex_stream);
    clone -> semicolon_token = semicolon_token;


    clone -> package_token_string = new wchar_t[wcslen(lex_stream.NameString(package_token)) + 1];
    wcscpy(clone -> package_token_string, lex_stream.NameString(package_token));


    clone -> package_token_string = const_cast<wchar_t*>(lex_stream.NameString(package_token));

    return clone;
}

Ast* AstImportDeclaration::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstImportDeclaration* clone = ast_pool -> GenImportDeclaration();
    clone -> import_token = import_token;
    clone -> static_token_opt = static_token_opt;
    clone -> name = (AstName*) name -> Clone(ast_pool, lex_stream);
    clone -> star_token_opt = star_token_opt;
    clone -> semicolon_token = semicolon_token;


    clone -> import_token_string = new wchar_t[wcslen(lex_stream.NameString(import_token)) + 1];
    clone -> static_token_opt_string = new wchar_t[wcslen(lex_stream.NameString(static_token_opt)) + 1];
    clone -> star_token_opt_string = new wchar_t[wcslen(lex_stream.NameString(star_token_opt)) + 1];
    wcscpy(clone -> import_token_string, lex_stream.NameString(import_token));
    wcscpy(clone -> static_token_opt_string, lex_stream.NameString(static_token_opt));
    wcscpy(clone -> star_token_opt_string, lex_stream.NameString(star_token_opt));


    clone -> import_token_string = const_cast<wchar_t*>(lex_stream.NameString(import_token));
    clone -> static_token_opt_string = const_cast<wchar_t*>(lex_stream.NameString(static_token_opt));
    clone -> star_token_opt_string = const_cast<wchar_t*>(lex_stream.NameString(star_token_opt));

    return clone;
}

Ast* AstCompilationUnit::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    unsigned i;
    AstCompilationUnit* clone = ast_pool -> GenCompilationUnit();
    clone -> other_tag = other_tag;
    
    clone -> package_declaration_opt = (package_declaration_opt)
		? (AstPackageDeclaration*) package_declaration_opt -> Clone(ast_pool, lex_stream)
		: NULL;
    clone -> AllocateImportDeclarations(NumImportDeclarations());
    for (i = 0; i < NumImportDeclarations(); i++)
        clone -> AddImportDeclaration((AstImportDeclaration*)
                                      ImportDeclaration(i) -> Clone(ast_pool, lex_stream));
    clone -> AllocateTypeDeclarations(NumTypeDeclarations());
    for (i = 0; i < NumTypeDeclarations(); i++)
        clone -> AddTypeDeclaration((AstDeclaredType*) TypeDeclaration(i) ->
                                    Clone(ast_pool, lex_stream));

    clone -> file_name = lex_stream.FileName();	

    return clone;
}

Ast* AstEmptyDeclaration::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstEmptyDeclaration* clone = ast_pool -> GenEmptyDeclaration(semicolon_token);


    clone -> semicolon_token_string = new wchar_t[wcslen(lex_stream.NameString(semicolon_token)) +1];
    wcscpy(clone -> semicolon_token_string, lex_stream.NameString(semicolon_token));


    clone -> semicolon_token_string = const_cast<wchar_t*>(lex_stream.NameString(semicolon_token));

    return clone;
}

Ast* AstClassBody::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstClassBody* clone = ast_pool -> GenClassBody();
    clone -> identifier_token = identifier_token;
    clone -> left_brace_token = left_brace_token;
    clone -> AllocateClassBodyDeclarations(NumClassBodyDeclarations());
    clone -> AllocateInstanceVariables(NumInstanceVariables());
    clone -> AllocateClassVariables(NumClassVariables());
    clone -> AllocateMethods(NumMethods());
    clone -> AllocateConstructors(NumConstructors());
    clone -> AllocateStaticInitializers(NumStaticInitializers());
    clone -> AllocateInstanceInitializers(NumInstanceInitializers());
    clone -> AllocateNestedClasses(NumNestedClasses());
    clone -> AllocateNestedEnums(NumNestedEnums());
    clone -> AllocateNestedInterfaces(NumNestedInterfaces());
    clone -> AllocateNestedAnnotations(NumNestedAnnotations());
    clone -> AllocateEmptyDeclarations(NumEmptyDeclarations());
    for (unsigned i = 0; i < NumClassBodyDeclarations(); i++)
        clone -> AddClassBodyDeclaration((AstDeclaredType*)
                                         ClassBodyDeclaration(i) ->
                                         Clone(ast_pool, lex_stream));
    clone -> right_brace_token = right_brace_token;


    clone -> identifier_token_string = new wchar_t[wcslen(lex_stream.NameString(identifier_token)) + 1];
    wcscpy(clone -> identifier_token_string, lex_stream.NameString(identifier_token));


    clone -> identifier_token_string = const_cast<wchar_t*>(lex_stream.NameString(identifier_token));

    return clone;
}

Ast* AstTypeParameter::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstTypeParameter* clone = ast_pool -> GenTypeParameter(identifier_token);
    clone -> AllocateBounds(NumBounds());
    for (unsigned i = 0; i < NumBounds(); i++)
        clone -> AddBound((AstTypeName*) Bound(i) -> Clone(ast_pool, lex_stream));


    clone -> identifier_token_string = new wchar_t[wcslen(lex_stream.NameString(identifier_token)) + 1];
    wcscpy(clone -> identifier_token_string, lex_stream.NameString(identifier_token));


    clone -> identifier_token_string = const_cast<wchar_t*>(lex_stream.NameString(identifier_token));

    return clone;
}

Ast* AstTypeParameters::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstTypeParameters* clone = ast_pool -> GenTypeParameters();
    clone -> left_angle_token = left_angle_token;
    clone -> AllocateTypeParameters(NumTypeParameters());
    for (unsigned i = 0; i < NumTypeParameters(); i++)
        clone -> AddTypeParameter((AstTypeParameter*) TypeParameter(i) ->
                                  Clone(ast_pool, lex_stream));
    clone -> right_angle_token = right_angle_token;
    return clone;
}

Ast* AstClassDeclaration::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstClassDeclaration* clone = ast_pool -> GenClassDeclaration();
    
    clone -> modifiers_opt = (modifiers_opt)
		? (AstModifiers*) modifiers_opt -> Clone(ast_pool, lex_stream)
		: NULL;
    clone -> class_token = class_token;
    
    clone -> type_parameters_opt = (type_parameters_opt)
		? (AstTypeParameters*) type_parameters_opt -> Clone(ast_pool, lex_stream)
		: NULL;
    
    clone -> super_opt = (super_opt)
		? (AstTypeName*) super_opt -> Clone(ast_pool, lex_stream)
		: NULL;
    clone -> AllocateInterfaces(NumInterfaces());
    for (unsigned i = 0; i < NumInterfaces(); i++)
        clone -> AddInterface((AstTypeName*) Interface(i) -> Clone(ast_pool, lex_stream));
    clone -> class_body = (AstClassBody*) class_body -> Clone(ast_pool, lex_stream);
    clone -> class_body -> owner = clone;


    clone -> class_token_string = new wchar_t[wcslen(lex_stream.NameString(class_token)) + 1];
    wcscpy(clone -> class_token_string, lex_stream.NameString(class_token));


    clone -> class_token_string = const_cast<wchar_t*>(lex_stream.NameString(class_token));

    return clone;
}

Ast* AstArrayInitializer::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstArrayInitializer* clone = ast_pool -> GenArrayInitializer();
    clone -> left_brace_token = left_brace_token;
    clone -> AllocateVariableInitializers(NumVariableInitializers());
    for (unsigned i = 0; i < NumVariableInitializers(); i++)
        clone -> AddVariableInitializer((AstMemberValue*)
                                        VariableInitializer(i) ->
                                        Clone(ast_pool, lex_stream));
    clone -> right_brace_token = right_brace_token;
    return clone;
}

Ast* AstVariableDeclaratorId::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstVariableDeclaratorId* clone = ast_pool -> GenVariableDeclaratorId();
    clone -> identifier_token = identifier_token;
    
    clone -> brackets_opt = (brackets_opt)
		? (AstBrackets*) brackets_opt -> Clone(ast_pool, lex_stream)
		: NULL;

	
    clone -> identifier_token_string = new wchar_t[wcslen(lex_stream.NameString(identifier_token)) + 1];
    wcscpy(clone -> identifier_token_string, lex_stream.NameString(identifier_token));

    clone -> identifier_token_string = const_cast<wchar_t*>(lex_stream.NameString(identifier_token));

    return clone;
}

Ast* AstVariableDeclarator::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstVariableDeclarator* clone = ast_pool -> GenVariableDeclarator();
    clone -> variable_declarator_name = (AstVariableDeclaratorId*)
        variable_declarator_name -> Clone(ast_pool, lex_stream);
    
    clone -> variable_initializer_opt = (variable_initializer_opt)
		?  variable_initializer_opt -> Clone(ast_pool, lex_stream)
		: NULL;

    return clone;
}

Ast* AstFieldDeclaration::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstFieldDeclaration* clone = ast_pool -> GenFieldDeclaration();
    clone -> other_tag = other_tag;
    
    clone -> modifiers_opt = (modifiers_opt)
		?  (AstModifiers*) modifiers_opt -> Clone(ast_pool, lex_stream)
		: NULL;
    clone -> type = (AstType*) type -> Clone(ast_pool, lex_stream);
    clone -> AllocateVariableDeclarators(NumVariableDeclarators());
    for (unsigned i = 0; i < NumVariableDeclarators(); i++)
        clone -> AddVariableDeclarator((AstVariableDeclarator*)
                                       VariableDeclarator(i) ->
                                       Clone(ast_pool, lex_stream));
    clone -> semicolon_token = semicolon_token;
    return clone;
}

Ast* AstFormalParameter::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstFormalParameter* clone = ast_pool -> GenFormalParameter();
    
    clone -> modifiers_opt = (modifiers_opt)
		?  (AstModifiers*) modifiers_opt -> Clone(ast_pool, lex_stream)
		: NULL;
    clone -> type = (AstType*) type -> Clone(ast_pool, lex_stream);
    clone -> ellipsis_token_opt = ellipsis_token_opt;
    clone -> formal_declarator =
        (AstVariableDeclarator*) formal_declarator -> Clone(ast_pool, lex_stream);

	
    clone -> ellipsis_token_opt_string = new wchar_t[wcslen(lex_stream.NameString(ellipsis_token_opt)) + 1];
    wcscpy(clone -> ellipsis_token_opt_string, lex_stream.NameString(ellipsis_token_opt));


    clone -> ellipsis_token_opt_string = const_cast<wchar_t*>(lex_stream.NameString(ellipsis_token_opt));

    return clone;
}

Ast* AstMethodDeclarator::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstMethodDeclarator* clone = ast_pool -> GenMethodDeclarator();
    clone -> identifier_token = identifier_token;
    clone -> left_parenthesis_token = left_parenthesis_token;

/**** I think... this should initialized ****/
    clone -> formal_parameters = NULL;

    clone -> AllocateFormalParameters(NumFormalParameters());
    for (unsigned i = 0; i < NumFormalParameters(); i++)
        clone -> AddFormalParameter((AstFormalParameter*)
                                    FormalParameter(i) -> Clone(ast_pool, lex_stream));
    clone -> right_parenthesis_token = right_parenthesis_token;
    
    clone -> brackets_opt = (brackets_opt)
		 ? (AstBrackets*) brackets_opt -> Clone(ast_pool, lex_stream)
		 : NULL;


    clone -> identifier_token_string = new wchar_t[wcslen(lex_stream.NameString(identifier_token)) + 1];
    wcscpy(clone -> identifier_token_string, lex_stream.NameString(identifier_token));


    clone -> identifier_token_string = const_cast<wchar_t*>(lex_stream.NameString(identifier_token));

    return clone;
}

Ast* AstMethodBody::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstMethodBody* clone = ast_pool -> GenMethodBody();
    clone -> CloneBlock(ast_pool, this, lex_stream);
    
    clone -> explicit_constructor_opt = (explicit_constructor_opt)
		? (AstStatement*) explicit_constructor_opt -> Clone(ast_pool, lex_stream)
		: NULL;
    return clone;
}

Ast* AstMethodDeclaration::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstMethodDeclaration* clone = ast_pool -> GenMethodDeclaration();
    
    clone -> modifiers_opt = (modifiers_opt)
		 ? (AstModifiers*) modifiers_opt -> Clone(ast_pool, lex_stream)
		 : NULL;
   
    clone -> type_parameters_opt = (type_parameters_opt)
		 ? (AstTypeParameters*) type_parameters_opt -> Clone(ast_pool, lex_stream)
		 : NULL;
	
    clone -> type = (AstType*) type -> Clone(ast_pool, lex_stream);
    clone -> method_declarator =
        (AstMethodDeclarator*) method_declarator -> Clone(ast_pool, lex_stream);
    clone -> AllocateThrows(NumThrows());
    for (unsigned i = 0; i < NumThrows(); i++)
        clone -> AddThrow((AstTypeName*) Throw(i) -> Clone(ast_pool, lex_stream));

    clone -> default_value_opt = (default_value_opt)
		? (AstMemberValue*) default_value_opt -> Clone(ast_pool, lex_stream)
            	: NULL;
    
    clone -> method_body_opt = (method_body_opt)
		 ? (AstMethodBody*) method_body_opt -> Clone(ast_pool, lex_stream)
		 : NULL;
	
    clone -> semicolon_token_opt = semicolon_token_opt;
    return clone;
}

Ast* AstInitializerDeclaration::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstInitializerDeclaration* clone = ast_pool -> GenInitializerDeclaration();
    clone -> other_tag = other_tag;
    
    clone -> modifiers_opt =(modifiers_opt)
		? (AstModifiers*) modifiers_opt -> Clone(ast_pool, lex_stream)
		: NULL;
    clone -> block = (AstMethodBody*) block -> Clone(ast_pool, lex_stream);
    return clone;
}

Ast* AstArguments::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    unsigned i;
    AstArguments* clone = ast_pool -> GenArguments(left_parenthesis_token,
                                                   right_parenthesis_token);
    clone -> AllocateArguments(NumArguments());
    for (i = 0; i < NumArguments(); i++)
        clone -> AddArgument((AstExpression*) Argument(i) -> Clone(ast_pool, lex_stream));
    clone -> AllocateLocalArguments(NumLocalArguments());
    for (i = 0; i < NumLocalArguments(); i++)
        clone -> AddLocalArgument((AstName*) LocalArgument(i) ->
                                  Clone(ast_pool, lex_stream));
    clone -> other_tag = other_tag;
    return clone;
}

Ast* AstThisCall::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstThisCall* clone = ast_pool -> GenThisCall();
    
    clone -> type_arguments_opt = (type_arguments_opt)
		? (AstTypeArguments*) type_arguments_opt -> Clone(ast_pool, lex_stream)
		: NULL;
    clone -> this_token = this_token;
    clone -> arguments = (AstArguments*) arguments -> Clone(ast_pool, lex_stream);
    clone -> semicolon_token = semicolon_token;


    clone -> this_token_string = new wchar_t[wcslen(lex_stream.NameString(this_token)) + 1];
    wcscpy(clone -> this_token_string, lex_stream.NameString(this_token));


    clone -> this_token_string = const_cast<wchar_t*>(lex_stream.NameString(this_token));

    return clone;
}

Ast* AstSuperCall::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstSuperCall* clone = ast_pool -> GenSuperCall();
    
    clone -> base_opt = (base_opt)
		? (AstExpression*) base_opt -> Clone(ast_pool, lex_stream)
		: NULL;
    
    clone -> type_arguments_opt = (type_arguments_opt)
		?  (AstTypeArguments*) type_arguments_opt -> Clone(ast_pool, lex_stream)
		: NULL;
    clone -> super_token = super_token;
    clone -> arguments = (AstArguments*) arguments -> Clone(ast_pool, lex_stream);
    clone -> semicolon_token = semicolon_token;


    clone -> super_token_string = new wchar_t[wcslen(lex_stream.NameString(super_token)) + 1];
    wcscpy(clone -> super_token_string, lex_stream.NameString(super_token));


    clone -> super_token_string = const_cast<wchar_t*>(lex_stream.NameString(super_token));

    return clone;
}

Ast* AstConstructorDeclaration::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstConstructorDeclaration* clone = ast_pool -> GenConstructorDeclaration();
    
    clone -> modifiers_opt = (modifiers_opt)
		?  (AstModifiers*) modifiers_opt -> Clone(ast_pool, lex_stream)
		: NULL;
    
    clone -> type_parameters_opt = (type_parameters_opt)
		?  (AstTypeParameters*) type_parameters_opt -> Clone(ast_pool, lex_stream)
		: NULL;
    clone -> constructor_declarator =
        (AstMethodDeclarator*) constructor_declarator -> Clone(ast_pool, lex_stream);
    clone -> AllocateThrows(NumThrows());
    for (unsigned i = 0; i < NumThrows(); i++)
        clone -> AddThrow((AstTypeName*) Throw(i) -> Clone(ast_pool, lex_stream));
    clone -> constructor_body =
        (AstMethodBody*) constructor_body -> Clone(ast_pool, lex_stream);
    return clone;
}

Ast* AstEnumDeclaration::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    unsigned i;
    AstEnumDeclaration* clone = ast_pool -> GenEnumDeclaration();
    
    clone -> modifiers_opt = (modifiers_opt)
		?  (AstModifiers*) modifiers_opt -> Clone(ast_pool, lex_stream)
		: NULL;
    clone -> AllocateInterfaces(NumInterfaces());
    for (i = 0; i < NumInterfaces(); i++)
        clone -> AddInterface((AstTypeName*) Interface(i) -> Clone(ast_pool, lex_stream));
    clone -> AllocateEnumConstants(NumEnumConstants());
    for (i = 0; i < NumEnumConstants(); i++)
        clone -> AddEnumConstant((AstEnumConstant*) EnumConstant(i) ->
                                 Clone(ast_pool, lex_stream));
    clone -> class_body = (AstClassBody*) class_body -> Clone(ast_pool, lex_stream);
    clone -> class_body -> owner = clone;


    clone -> enum_token_string = new wchar_t[wcslen(lex_stream.NameString(enum_token)) + 1];
    wcscpy(clone -> enum_token_string, lex_stream.NameString(enum_token));


    clone -> enum_token_string = const_cast<wchar_t*>(lex_stream.NameString(enum_token));

    return clone;
}

Ast* AstEnumConstant::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstEnumConstant* clone = ast_pool -> GenEnumConstant(identifier_token);
    
    clone -> modifiers_opt = (modifiers_opt)
		?  (AstModifiers*) modifiers_opt -> Clone(ast_pool, lex_stream)
		: NULL;
    
    clone -> arguments_opt = (arguments_opt)
		? (AstArguments*) arguments_opt -> Clone(ast_pool, lex_stream)
		: NULL;    

    clone -> class_body_opt = (class_body_opt)
            	? (AstClassBody*) class_body_opt -> Clone(ast_pool, lex_stream)
            	: NULL;


    clone -> identifier_token_string = new wchar_t[wcslen(lex_stream.NameString(identifier_token)) + 1];
    wcscpy(clone -> identifier_token_string, lex_stream.NameString(identifier_token));


    clone -> identifier_token_string = const_cast<wchar_t*>(lex_stream.NameString(identifier_token));

    return clone;
}

Ast* AstInterfaceDeclaration::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstInterfaceDeclaration* clone = ast_pool -> GenInterfaceDeclaration();
    
    clone -> modifiers_opt = (modifiers_opt)
		?  (AstModifiers*) modifiers_opt -> Clone(ast_pool, lex_stream)
		 : NULL;
    clone -> interface_token = interface_token;
    
    clone -> type_parameters_opt = (type_parameters_opt)
		? (AstTypeParameters*) type_parameters_opt -> Clone(ast_pool, lex_stream)
		: NULL;
    clone -> AllocateInterfaces(NumInterfaces());
    for (unsigned i = 0; i < NumInterfaces(); i++)
        clone -> AddInterface((AstTypeName*) Interface(i) -> Clone(ast_pool, lex_stream));
    clone -> class_body = (AstClassBody*) class_body -> Clone(ast_pool, lex_stream);
    clone -> class_body -> owner = clone;


    clone -> interface_token_string = new wchar_t[wcslen(lex_stream.NameString(interface_token)) + 1];
    wcscpy(clone -> interface_token_string, lex_stream.NameString(interface_token));


    clone -> interface_token_string = const_cast<wchar_t*>(lex_stream.NameString(interface_token));

    return clone;
}

Ast* AstAnnotationDeclaration::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstAnnotationDeclaration* clone =
        ast_pool -> GenAnnotationDeclaration(interface_token);
    
    clone -> modifiers_opt = (modifiers_opt)
		? (AstModifiers*) modifiers_opt -> Clone(ast_pool, lex_stream)
		: NULL;
    clone -> class_body = (AstClassBody*) class_body -> Clone(ast_pool, lex_stream);
    clone -> class_body -> owner = clone;


    clone -> interface_token_string = new wchar_t[wcslen(lex_stream.NameString(interface_token)) + 1];
    wcscpy(clone -> interface_token_string, lex_stream.NameString(interface_token));


    clone -> interface_token_string = const_cast<wchar_t*>(lex_stream.NameString(interface_token));

    return clone;
}

Ast* AstLocalVariableStatement::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstLocalVariableStatement* clone = ast_pool -> GenLocalVariableStatement();
    
    clone -> modifiers_opt = (modifiers_opt)
					    ? (AstModifiers*) modifiers_opt -> Clone(ast_pool, lex_stream)
					    : NULL;
    clone -> type = (AstType*) type -> Clone(ast_pool, lex_stream);
    clone -> AllocateVariableDeclarators(NumVariableDeclarators());
    for (unsigned i = 0; i < NumVariableDeclarators(); i++)
        clone -> AddVariableDeclarator((AstVariableDeclarator*)
                                       VariableDeclarator(i) ->
                                       Clone(ast_pool, lex_stream));
    clone -> semicolon_token_opt = semicolon_token_opt;
    return clone;
}

Ast* AstLocalClassStatement::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    Ast* p = declaration -> Clone(ast_pool, lex_stream);
    if (p -> ClassDeclarationCast())
        return ast_pool -> GenLocalClassStatement((AstClassDeclaration*) p);
    else return ast_pool -> GenLocalClassStatement((AstEnumDeclaration*) p);
}

Ast* AstIfStatement::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstIfStatement* clone = ast_pool -> GenIfStatement();
    clone -> if_token = if_token;
    clone -> expression = (AstExpression*) expression -> Clone(ast_pool, lex_stream);
    clone -> true_statement = (AstBlock*) true_statement -> Clone(ast_pool, lex_stream);
    
    clone -> false_statement_opt = (false_statement_opt)
						      ? (AstBlock*) false_statement_opt -> Clone(ast_pool, lex_stream)
						      : NULL;

    clone -> if_token_string = new wchar_t[wcslen(lex_stream.NameString(if_token)) + 1];
    wcscpy(clone -> if_token_string, lex_stream.NameString(if_token));


    clone -> if_token_string = const_cast<wchar_t*>(lex_stream.NameString(if_token));

    return clone;
}

Ast* AstEmptyStatement::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstEmptyStatement* clone = ast_pool -> GenEmptyStatement(semicolon_token);


    clone -> semicolon_token_string = new wchar_t[wcslen(lex_stream.NameString(semicolon_token))  + 1];
    wcscpy(clone -> semicolon_token_string, lex_stream.NameString(semicolon_token));


    clone -> semicolon_token_string = const_cast<wchar_t*>(lex_stream.NameString(semicolon_token));

    return clone;
}

Ast* AstExpressionStatement::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstExpressionStatement* clone = ast_pool -> GenExpressionStatement();
    clone -> expression = (AstExpression*) expression -> Clone(ast_pool, lex_stream);
    clone -> semicolon_token_opt = semicolon_token_opt;
    return clone;
}

Ast* AstSwitchLabel::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstSwitchLabel* clone = ast_pool -> GenSwitchLabel();
    clone -> case_token = case_token;
    
    clone -> expression_opt = (expression_opt)
		? (AstExpression*) expression_opt -> Clone(ast_pool, lex_stream)
		: NULL;
    clone -> colon_token = colon_token;
    clone -> map_index = map_index;


    clone -> case_token_string = new wchar_t[wcslen(lex_stream.NameString(case_token)) + 1];
    wcscpy(clone -> case_token_string, lex_stream.NameString(case_token));


    clone -> case_token_string = const_cast<wchar_t*>(lex_stream.NameString(case_token));

    return clone;
}

Ast* AstSwitchBlockStatement::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstSwitchBlockStatement* clone = ast_pool -> GenSwitchBlockStatement();
    clone -> CloneBlock(ast_pool, this, lex_stream);
    clone -> AllocateSwitchLabels(NumSwitchLabels());
    for (unsigned i = 0; i < NumSwitchLabels(); i++)
        clone -> AddSwitchLabel((AstSwitchLabel*) SwitchLabel(i) ->
                                Clone(ast_pool, lex_stream));
    return clone;
}

Ast* AstSwitchStatement::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstSwitchStatement* clone = ast_pool -> GenSwitchStatement();
    clone -> switch_token = switch_token;
    clone -> expression = (AstExpression*) expression -> Clone(ast_pool, lex_stream);
    clone -> switch_block = (AstBlock*) switch_block -> Clone(ast_pool, lex_stream);
    clone -> AllocateCases(NumCases());
    if (DefaultCase())
    {
        clone -> DefaultCase() = ast_pool -> GenCaseElement(0, 0);
        *clone -> DefaultCase() = *DefaultCase();
    }
    for (unsigned i = 0; i < NumCases(); i++)
    {
        CaseElement* elt = ast_pool -> GenCaseElement(0, 0);
        *elt = *Case(i);
        clone -> AddCase(elt);
    }


    clone -> switch_token_string = new wchar_t[wcslen(lex_stream.NameString(switch_token)) + 1];
    wcscpy(clone -> switch_token_string, lex_stream.NameString(switch_token));


    clone -> switch_token_string = const_cast<wchar_t*>(lex_stream.NameString(switch_token));

    return clone;
}

Ast* AstWhileStatement::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstWhileStatement* clone = ast_pool -> GenWhileStatement();
    clone -> while_token = while_token;
    clone -> expression = (AstExpression*) expression -> Clone(ast_pool, lex_stream);
    clone -> statement = (AstBlock*) statement -> Clone(ast_pool, lex_stream);


    clone -> while_token_string = new wchar_t[wcslen(lex_stream.NameString(while_token)) +1];
    wcscpy(clone -> while_token_string, lex_stream.NameString(while_token));


    clone -> while_token_string = const_cast<wchar_t*>(lex_stream.NameString(while_token));

    return clone;
}

Ast* AstDoStatement::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstDoStatement* clone = ast_pool -> GenDoStatement();
    clone -> do_token = do_token;
    clone -> statement = (AstBlock*) statement -> Clone(ast_pool, lex_stream);
    clone -> while_token = while_token;
    clone -> expression = (AstExpression*) expression -> Clone(ast_pool, lex_stream);
    clone -> semicolon_token = semicolon_token;


    clone -> do_token_string = new wchar_t[wcslen(lex_stream.NameString(do_token)) + 1];
    clone -> while_token_string = new wchar_t[wcslen(lex_stream.NameString(while_token)) + 1];
    wcscpy(clone -> do_token_string, lex_stream.NameString(do_token));
    wcscpy(clone -> while_token_string, lex_stream.NameString(while_token));


    clone -> do_token_string = const_cast<wchar_t*>(lex_stream.NameString(do_token));
    clone -> while_token_string = const_cast<wchar_t*>(lex_stream.NameString(while_token));

    return clone;
}

Ast* AstForStatement::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    unsigned i;
    AstForStatement* clone = ast_pool -> GenForStatement();
    clone -> for_token = for_token;
    clone -> AllocateForInitStatements(NumForInitStatements());
    for (i = 0; i < NumForInitStatements(); i++)
        clone -> AddForInitStatement((AstStatement*)
                                     ForInitStatement(i) -> Clone(ast_pool, lex_stream));
    
    clone -> end_expression_opt =(end_expression_opt)
						     ? (AstExpression*) end_expression_opt -> Clone(ast_pool, lex_stream)
						     : NULL;
    clone -> AllocateForUpdateStatements(NumForUpdateStatements());
    for (i = 0; i < NumForUpdateStatements(); i++)
        clone -> AddForUpdateStatement((AstExpressionStatement*)
                                       ForUpdateStatement(i) ->
                                       Clone(ast_pool, lex_stream));
    clone -> statement = (AstBlock*) statement -> Clone(ast_pool, lex_stream);


    clone -> for_token_string = new wchar_t[wcslen(lex_stream.NameString(for_token)) + 1];
    wcscpy(clone -> for_token_string, lex_stream.NameString(for_token));


    clone -> for_token_string = const_cast<wchar_t*>(lex_stream.NameString(for_token));

    return clone;
}

Ast* AstForeachStatement::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstForeachStatement* clone = ast_pool -> GenForeachStatement();
    clone -> for_token = for_token;
    clone -> formal_parameter =
        (AstFormalParameter*) formal_parameter -> Clone(ast_pool, lex_stream);
    clone -> expression = (AstExpression*) expression -> Clone(ast_pool, lex_stream);
    clone -> statement = (AstBlock*) statement -> Clone(ast_pool, lex_stream);


    clone -> for_token_string = new wchar_t[wcslen(lex_stream.NameString(for_token)) + 1];
    wcscpy(clone -> for_token_string, lex_stream.NameString(for_token));


    clone -> for_token_string = const_cast<wchar_t*>(lex_stream.NameString(for_token));

    return clone;
}

Ast* AstBreakStatement::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstBreakStatement* clone = ast_pool -> GenBreakStatement();
    clone -> break_token = break_token;
    clone -> identifier_token_opt = identifier_token_opt;
    clone -> semicolon_token = semicolon_token;
    clone -> nesting_level = nesting_level;

    clone -> break_token_string = new wchar_t[wcslen(lex_stream.NameString(break_token)) + 1];
    clone -> identifier_token_opt_string = new wchar_t[wcslen(lex_stream.NameString(identifier_token_opt)) + 1];
    wcscpy(clone -> break_token_string, lex_stream.NameString(break_token));
    wcscpy(clone -> identifier_token_opt_string, lex_stream.NameString(identifier_token_opt));

    clone -> break_token_string = const_cast<wchar_t*>(lex_stream.NameString(break_token));
    clone -> identifier_token_opt_string = const_cast<wchar_t*>(lex_stream.NameString(identifier_token_opt));

    return clone;
}

Ast* AstContinueStatement::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstContinueStatement* clone = ast_pool -> GenContinueStatement();
    clone -> continue_token = continue_token;
    clone -> identifier_token_opt = identifier_token_opt;
    clone -> semicolon_token = semicolon_token;
    clone -> nesting_level = nesting_level;


    clone -> continue_token_string = new wchar_t[wcslen(lex_stream.NameString(continue_token)) + 1];
    clone -> identifier_token_opt_string = new wchar_t[wcslen(lex_stream.NameString(identifier_token_opt)) + 1];
    wcscpy(clone -> continue_token_string, lex_stream.NameString(continue_token));
    wcscpy(clone -> identifier_token_opt_string, lex_stream.NameString(identifier_token_opt));

    clone -> continue_token_string = const_cast<wchar_t*>(lex_stream.NameString(continue_token));
    clone -> identifier_token_opt_string = const_cast<wchar_t*>(lex_stream.NameString(identifier_token_opt));

    return clone;
}

Ast* AstReturnStatement::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstReturnStatement* clone = ast_pool -> GenReturnStatement();
    clone -> return_token = return_token;
    
    clone -> expression_opt =(expression_opt)
		? (AstExpression*) expression_opt -> Clone(ast_pool, lex_stream)
		: NULL;
    clone -> semicolon_token = semicolon_token;


    clone -> return_token_string = new wchar_t[wcslen(lex_stream.NameString(return_token)) + 1];
    wcscpy(clone -> return_token_string, lex_stream.NameString(return_token));

    clone -> return_token_string = const_cast<wchar_t*>(lex_stream.NameString(return_token));

    return clone;
}

Ast* AstThrowStatement::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstThrowStatement* clone = ast_pool -> GenThrowStatement();
    clone -> throw_token = throw_token;
    clone -> expression = (AstExpression*) expression -> Clone(ast_pool, lex_stream);
    clone -> semicolon_token = semicolon_token;

    clone -> throw_token_string = new wchar_t[wcslen(lex_stream.NameString(throw_token)) + 1];
    wcscpy(clone -> throw_token_string, lex_stream.NameString(throw_token));

    clone -> throw_token_string = const_cast<wchar_t*>(lex_stream.NameString(throw_token));

    return clone;
}

Ast* AstSynchronizedStatement::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstSynchronizedStatement* clone = ast_pool -> GenSynchronizedStatement();
    clone -> synchronized_token = synchronized_token;
    clone -> expression = (AstExpression*) expression -> Clone(ast_pool, lex_stream);
    clone -> block = (AstBlock*) block -> Clone(ast_pool, lex_stream);

    clone -> synchronized_token_string = new wchar_t[wcslen(lex_stream.NameString(synchronized_token)) + 1];
    wcscpy(clone -> synchronized_token_string, lex_stream.NameString(synchronized_token));

    clone -> synchronized_token_string = const_cast<wchar_t*>(lex_stream.NameString(synchronized_token));

    return clone;
}

Ast* AstAssertStatement::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstAssertStatement* clone = ast_pool -> GenAssertStatement();
    clone -> assert_token = assert_token;
    clone -> condition = (AstExpression*) condition -> Clone(ast_pool, lex_stream);
    
    clone -> message_opt = (message_opt)
		? (AstExpression*) message_opt -> Clone(ast_pool, lex_stream)
		: NULL;
    clone -> semicolon_token = semicolon_token;

    clone -> assert_token_string = new wchar_t[wcslen(lex_stream.NameString(assert_token)) + 1];
    wcscpy(clone -> assert_token_string, lex_stream.NameString(assert_token));

    clone -> assert_token_string = const_cast<wchar_t*>(lex_stream.NameString(assert_token));

    return clone;
}

Ast* AstCatchClause::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstCatchClause* clone = ast_pool -> GenCatchClause();
    clone -> catch_token = catch_token;
    clone -> formal_parameter =
        (AstFormalParameter*) formal_parameter -> Clone(ast_pool, lex_stream);
    clone -> block = (AstBlock*) block -> Clone(ast_pool, lex_stream);

    clone -> catch_token_string = new wchar_t[wcslen(lex_stream.NameString(catch_token)) + 1];
    wcscpy(clone -> catch_token_string, lex_stream.NameString(catch_token));

    clone -> catch_token_string = const_cast<wchar_t*>(lex_stream.NameString(catch_token));

    return clone;
}

Ast* AstFinallyClause::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstFinallyClause* clone = ast_pool -> GenFinallyClause();
    clone -> finally_token = finally_token;
    clone -> block = (AstBlock*) block -> Clone(ast_pool, lex_stream);

    clone -> finally_token_string = new wchar_t[wcslen(lex_stream.NameString(finally_token)) + 1];
    wcscpy(clone -> finally_token_string, lex_stream.NameString(finally_token));

    clone -> finally_token_string = const_cast<wchar_t*>(lex_stream.NameString(finally_token));

    return clone;
}

Ast* AstTryStatement::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstTryStatement* clone = ast_pool -> GenTryStatement();
    clone -> try_token = try_token;
    clone -> block = (AstBlock*) block -> Clone(ast_pool, lex_stream);
    clone -> AllocateCatchClauses(NumCatchClauses());
    for (unsigned i = 0; i < NumCatchClauses(); i++)
        clone -> AddCatchClause((AstCatchClause*) CatchClause(i) ->
                                Clone(ast_pool, lex_stream));
    
    clone -> finally_clause_opt = (finally_clause_opt)
		? (AstFinallyClause*) finally_clause_opt -> Clone(ast_pool, lex_stream)
		: NULL;

    clone -> try_token_string = new wchar_t[wcslen(lex_stream.NameString(try_token)) + 1];
    wcscpy(clone -> try_token_string, lex_stream.NameString(try_token));

    clone -> try_token_string = const_cast<wchar_t*>(lex_stream.NameString(try_token));

    return clone;
}

Ast* AstIntegerLiteral::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstIntegerLiteral* clone = ast_pool -> GenIntegerLiteral(integer_literal_token);


    clone -> integer_literal_token_string = new wchar_t[wcslen(lex_stream.NameString(integer_literal_token)) + 1];
    wcscpy(clone -> integer_literal_token_string, lex_stream.NameString(integer_literal_token));

    clone -> integer_literal_token_string = const_cast<wchar_t*>(lex_stream.NameString(integer_literal_token));

    return clone;
}

Ast* AstLongLiteral::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstLongLiteral* clone = ast_pool -> GenLongLiteral(long_literal_token);


    clone -> long_literal_token_string = new wchar_t[wcslen(lex_stream.NameString(long_literal_token)) + 1];
    wcscpy(clone -> long_literal_token_string, lex_stream.NameString(long_literal_token));

    clone -> long_literal_token_string = const_cast<wchar_t*>(lex_stream.NameString(long_literal_token));

    return clone;
}

Ast* AstFloatLiteral::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstFloatLiteral* clone = ast_pool -> GenFloatLiteral(float_literal_token);

    clone -> float_literal_token_string = new wchar_t[wcslen(lex_stream.NameString(float_literal_token)) + 1];
    wcscpy(clone -> float_literal_token_string, lex_stream.NameString(float_literal_token));

    clone -> float_literal_token_string = const_cast<wchar_t*>(lex_stream.NameString(float_literal_token));

    return clone;
}

Ast* AstDoubleLiteral::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstDoubleLiteral* clone = ast_pool -> GenDoubleLiteral(double_literal_token);

    clone -> double_literal_token_string = new wchar_t[wcslen(lex_stream.NameString(double_literal_token)) + 1];
    wcscpy(clone -> double_literal_token_string, lex_stream.NameString(double_literal_token));

    clone -> double_literal_token_string = const_cast<wchar_t*>(lex_stream.NameString(double_literal_token));

    return clone;
}

Ast* AstTrueLiteral::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstTrueLiteral* clone = ast_pool -> GenTrueLiteral(true_literal_token);

    clone -> true_literal_token_string = new wchar_t[wcslen(lex_stream.NameString(true_literal_token)) + 1];
    wcscpy(clone -> true_literal_token_string, lex_stream.NameString(true_literal_token));

    clone -> true_literal_token_string = const_cast<wchar_t*>(lex_stream.NameString(true_literal_token));

    return clone;
}

Ast* AstFalseLiteral::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstFalseLiteral* clone = ast_pool -> GenFalseLiteral(false_literal_token);

    clone -> false_literal_token_string = new wchar_t[wcslen(lex_stream.NameString(false_literal_token)) + 1];
    wcscpy(clone -> false_literal_token_string, lex_stream.NameString(false_literal_token));

    clone -> false_literal_token_string = const_cast<wchar_t*>(lex_stream.NameString(false_literal_token));

    return clone;
}

Ast* AstStringLiteral::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstStringLiteral* clone = ast_pool -> GenStringLiteral(string_literal_token);


    clone -> string_literal_token_string = new wchar_t[wcslen(lex_stream.NameString(string_literal_token)) + 1];
    wcscpy(clone -> string_literal_token_string, lex_stream.NameString(string_literal_token));


    clone -> string_literal_token_string = const_cast<wchar_t*>(lex_stream.NameString(string_literal_token));

    return clone;
}

Ast* AstCharacterLiteral::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstCharacterLiteral* clone = ast_pool -> GenCharacterLiteral(character_literal_token);


    clone -> character_literal_token_string = new wchar_t[wcslen(lex_stream.NameString(character_literal_token)) + 1];
    wcscpy(clone -> character_literal_token_string, lex_stream.NameString(character_literal_token));

    clone -> character_literal_token_string = const_cast<wchar_t*>(lex_stream.NameString(character_literal_token));

    return clone;
}

Ast* AstNullLiteral::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstNullLiteral* clone = ast_pool -> GenNullLiteral(null_token);
	
    clone -> null_token_string = new wchar_t[wcslen(lex_stream.NameString(null_token)) + 1];
    wcscpy(clone -> null_token_string, lex_stream.NameString(null_token));

    clone -> null_token_string = const_cast<wchar_t*>(lex_stream.NameString(null_token));

    return clone;
}

Ast* AstClassLiteral::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstClassLiteral* clone = ast_pool -> GenClassLiteral(class_token);
    clone -> type = (AstTypeName*) type -> Clone(ast_pool, lex_stream);
    
    clone -> resolution_opt = (resolution_opt)
		? (AstExpression*) resolution_opt -> Clone(ast_pool, lex_stream)
		: NULL;

    clone -> class_token_string = new wchar_t[wcslen(lex_stream.NameString(class_token)) + 1];
    wcscpy(clone -> class_token_string, lex_stream.NameString(class_token));

    clone -> class_token_string = const_cast<wchar_t*>(lex_stream.NameString(class_token));

    return clone;
}

Ast* AstThisExpression::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstThisExpression* clone = ast_pool -> GenThisExpression(this_token);
    
    clone -> base_opt = (base_opt)
		? (AstTypeName*) base_opt -> Clone(ast_pool, lex_stream)
		: NULL;
    
    clone -> resolution_opt =(resolution_opt)
		? (AstExpression*) resolution_opt -> Clone(ast_pool, lex_stream)
		: NULL;
	

    clone -> this_token_string = new wchar_t[wcslen(lex_stream.NameString(this_token)) + 1];
    wcscpy(clone -> this_token_string, lex_stream.NameString(this_token));

    clone -> this_token_string = const_cast<wchar_t*>(lex_stream.NameString(this_token));

    return clone;
}

Ast* AstSuperExpression::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstSuperExpression* clone = ast_pool -> GenSuperExpression(super_token);
    
    clone -> base_opt = (base_opt)
		? (AstTypeName*) base_opt -> Clone(ast_pool, lex_stream)
		: NULL;
    
    clone -> resolution_opt = (resolution_opt)
		? (AstExpression*) resolution_opt -> Clone(ast_pool, lex_stream)
		: NULL;

    clone -> super_token_string = new wchar_t[wcslen(lex_stream.NameString(super_token)) + 1];
    wcscpy(clone -> super_token_string, lex_stream.NameString(super_token));

    clone -> super_token_string = const_cast<wchar_t*>(lex_stream.NameString(super_token));

    return clone;
}

Ast* AstParenthesizedExpression::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstParenthesizedExpression* clone =
        ast_pool -> GenParenthesizedExpression();
    clone -> left_parenthesis_token = left_parenthesis_token;
    clone -> expression = (AstExpression*) expression -> Clone(ast_pool, lex_stream);
    clone -> right_parenthesis_token = right_parenthesis_token;

    clone -> left_parenthesis_token_string = new wchar_t[wcslen(lex_stream.NameString(left_parenthesis_token)) + 1];
    clone -> right_parenthesis_token_string = new wchar_t[wcslen(lex_stream.NameString(right_parenthesis_token)) + 1];
    wcscpy(clone -> left_parenthesis_token_string, lex_stream.NameString(left_parenthesis_token));
    wcscpy(clone -> right_parenthesis_token_string, lex_stream.NameString(right_parenthesis_token));

    clone -> left_parenthesis_token_string = const_cast<wchar_t*>(lex_stream.NameString(left_parenthesis_token));
    clone -> right_parenthesis_token_string = const_cast<wchar_t*>(lex_stream.NameString(right_parenthesis_token));

    return clone;
}

Ast* AstClassCreationExpression::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstClassCreationExpression* clone =
        ast_pool -> GenClassCreationExpression();
    
    clone -> base_opt = (base_opt)
		? (AstExpression*) base_opt -> Clone(ast_pool, lex_stream)
		: NULL;
    clone -> new_token = new_token;
    
    clone -> type_arguments_opt = (type_arguments_opt)
		? (AstTypeArguments*) type_arguments_opt -> Clone(ast_pool, lex_stream)
		: NULL;
    clone -> class_type = (AstTypeName*) class_type -> Clone(ast_pool, lex_stream);
    clone -> arguments = (AstArguments*) arguments -> Clone(ast_pool, lex_stream);
    
    clone -> class_body_opt = (class_body_opt)
		? (AstClassBody*) class_body_opt -> Clone(ast_pool, lex_stream)
		: NULL;
    
    clone -> resolution_opt = (resolution_opt)
		? (AstClassCreationExpression*) resolution_opt -> Clone(ast_pool, lex_stream)
		: NULL;

    clone -> new_token_string = new wchar_t[wcslen(lex_stream.NameString(new_token)) + 1];
    wcscpy(clone -> new_token_string, lex_stream.NameString(new_token));

    clone -> new_token_string = const_cast<wchar_t*>(lex_stream.NameString(new_token));

    return clone;
}

Ast* AstDimExpr::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstDimExpr* clone = ast_pool -> GenDimExpr();
    clone -> left_bracket_token = left_bracket_token;
    clone -> expression = (AstExpression*) expression -> Clone(ast_pool, lex_stream);
    clone -> right_bracket_token = right_bracket_token;
    return clone;
}

Ast* AstArrayCreationExpression::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstArrayCreationExpression* clone =
        ast_pool -> GenArrayCreationExpression();
    clone -> new_token = new_token;
    clone -> array_type = (AstType*) array_type -> Clone(ast_pool, lex_stream);
    clone -> AllocateDimExprs(NumDimExprs());
    for (unsigned i = 0; i < NumDimExprs(); i++)
        clone -> AddDimExpr((AstDimExpr*) DimExpr(i) -> Clone(ast_pool, lex_stream));
    
    clone -> brackets_opt = (brackets_opt)
		? (AstBrackets*) brackets_opt -> Clone(ast_pool, lex_stream)
		: NULL;
    
    clone -> array_initializer_opt = (array_initializer_opt)
		? (AstArrayInitializer*) array_initializer_opt -> Clone(ast_pool, lex_stream)
		: NULL;

    clone -> new_token_string = new wchar_t[wcslen(lex_stream.NameString(new_token)) + 1];
    wcscpy(clone -> new_token_string, lex_stream.NameString(new_token));

    clone -> new_token_string = const_cast<wchar_t*>(lex_stream.NameString(new_token));

    return clone;
}

Ast* AstFieldAccess::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstFieldAccess* clone = ast_pool -> GenFieldAccess();
    clone -> base = (AstExpression*) base -> Clone(ast_pool, lex_stream);
    clone -> identifier_token = identifier_token;
    
    clone -> resolution_opt = (resolution_opt)
		? (AstExpression*) resolution_opt -> Clone(ast_pool, lex_stream)
		: NULL;

/*
    clone -> identifier_token_string = new wchar_t[wcslen(lex_stream.NameString(identifier_token)) + 1];
    wcscpy(clone -> identifier_token_string, lex_stream.NameString(identifier_token));
*/
    clone -> identifier_token_string = const_cast<wchar_t*>(lex_stream.NameString(identifier_token));

    return clone;
}

Ast* AstMethodInvocation::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstMethodInvocation* clone =
        ast_pool -> GenMethodInvocation(identifier_token);
    
    clone -> base_opt = (base_opt)
		? (AstExpression*) base_opt -> Clone(ast_pool, lex_stream)
		: NULL;
    
    clone -> type_arguments_opt = (type_arguments_opt)
		? (AstTypeArguments*) type_arguments_opt -> Clone(ast_pool, lex_stream)
		: NULL;
    clone -> identifier_token = identifier_token;
    clone -> arguments = (AstArguments*) arguments -> Clone(ast_pool, lex_stream);
    
    clone -> resolution_opt = (resolution_opt)
		? (AstExpression*) resolution_opt -> Clone(ast_pool, lex_stream)
		: NULL;

    clone -> identifier_token_string = new wchar_t[wcslen(lex_stream.NameString(identifier_token)) + 1];
    wcscpy(clone -> identifier_token_string, lex_stream.NameString(identifier_token));

    clone -> identifier_token_string = const_cast<wchar_t*>(lex_stream.NameString(identifier_token));

    return clone;
}

Ast* AstArrayAccess::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstArrayAccess* clone = ast_pool -> GenArrayAccess();
    clone -> base = (AstExpression*) base -> Clone(ast_pool, lex_stream);
    clone -> left_bracket_token = left_bracket_token;
    clone -> expression = (AstExpression*) expression -> Clone(ast_pool, lex_stream);
    clone -> right_bracket_token = right_bracket_token;
    return clone;
}

Ast* AstPostUnaryExpression::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstPostUnaryExpression* clone =
        ast_pool -> GenPostUnaryExpression((PostUnaryExpressionTag) other_tag);
    clone -> expression = (AstExpression*) expression -> Clone(ast_pool, lex_stream);
    clone -> post_operator_token = post_operator_token;

    clone -> post_operator_token_string = new wchar_t[wcslen(lex_stream.NameString(post_operator_token)) + 1];
    wcscpy(clone -> post_operator_token_string, lex_stream.NameString(post_operator_token));

    clone -> post_operator_token_string = const_cast<wchar_t*>(lex_stream.NameString(post_operator_token));

    return clone;
}

Ast* AstPreUnaryExpression::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstPreUnaryExpression* clone =
        ast_pool -> GenPreUnaryExpression((PreUnaryExpressionTag) other_tag);
    clone -> pre_operator_token = pre_operator_token;
    clone -> expression = (AstExpression*) expression -> Clone(ast_pool, lex_stream);

    clone -> pre_operator_token_string = new wchar_t[wcslen(lex_stream.NameString(pre_operator_token)) + 1];
    wcscpy(clone -> pre_operator_token_string, lex_stream.NameString(pre_operator_token));

    clone -> pre_operator_token_string = const_cast<wchar_t*>(lex_stream.NameString(pre_operator_token));

    return clone;
}

Ast* AstCastExpression::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstCastExpression* clone = ast_pool -> GenCastExpression();
    clone -> left_parenthesis_token = left_parenthesis_token;
    if (type)
        clone -> type = (AstType*) type -> Clone(ast_pool, lex_stream);
    clone -> right_parenthesis_token = right_parenthesis_token;
    clone -> expression = (AstExpression*) expression -> Clone(ast_pool, lex_stream);

    clone -> type_name = new wchar_t[wcslen(Type() -> Name()) + 1];	
    wcscpy(clone -> type_name, Type() -> Name());	

    return clone;
}

Ast* AstBinaryExpression::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstBinaryExpression* clone =
        ast_pool -> GenBinaryExpression((BinaryExpressionTag) other_tag);
    clone -> left_expression =
        (AstExpression*) left_expression -> Clone(ast_pool, lex_stream);
    clone -> binary_operator_token = binary_operator_token;
    clone -> right_expression =
        (AstExpression*) right_expression -> Clone(ast_pool, lex_stream);

    clone -> binary_operator_token_string = new wchar_t[wcslen(lex_stream.NameString(binary_operator_token)) + 1];
    wcscpy(clone -> binary_operator_token_string, lex_stream.NameString(binary_operator_token));

    clone -> binary_operator_token_string = const_cast<wchar_t*>(lex_stream.NameString(binary_operator_token));

    return clone;
}

Ast* AstInstanceofExpression::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstInstanceofExpression* clone = ast_pool -> GenInstanceofExpression();
    clone -> expression = (AstExpression*) expression -> Clone(ast_pool, lex_stream);
    clone -> instanceof_token = instanceof_token;
    clone -> type = (AstType*) type -> Clone(ast_pool, lex_stream);
/*
    clone -> instanceof_token_string = new wchar_t[wcslen(lex_stream.NameString(instanceof_token)) + 1];
    wcscpy(clone -> instanceof_token_string, lex_stream.NameString(instanceof_token));
*/
    clone -> instanceof_token_string = const_cast<wchar_t*>(lex_stream.NameString(instanceof_token));

    return clone;
}

Ast* AstConditionalExpression::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstConditionalExpression* clone = ast_pool -> GenConditionalExpression();
    clone -> test_expression =
        (AstExpression*) test_expression -> Clone(ast_pool, lex_stream);
    clone -> question_token = question_token;
    clone -> true_expression =
        (AstExpression*) true_expression -> Clone(ast_pool, lex_stream);
    clone -> colon_token = colon_token;
    clone -> false_expression =
        (AstExpression*) false_expression -> Clone(ast_pool, lex_stream);
    return clone;
}

Ast* AstAssignmentExpression::Clone(StoragePool* ast_pool, LexStream& lex_stream)
{
    AstAssignmentExpression* clone = ast_pool ->
        GenAssignmentExpression((AssignmentExpressionTag) other_tag,
                                assignment_operator_token);
    clone -> left_hand_side =
        (AstExpression*) left_hand_side -> Clone(ast_pool, lex_stream);
    clone -> expression = (AstExpression*) expression -> Clone(ast_pool, lex_stream);
/*
    clone -> assignment_operator_token_string = new wchar_t[wcslen(lex_stream.NameString(assignment_operator_token)) + 1];
    wcscpy(clone -> assignment_operator_token_string, lex_stream.NameString(assignment_operator_token));
*/
    clone -> assignment_operator_token_string = const_cast<wchar_t*>(lex_stream.NameString(assignment_operator_token));

    return clone;
}

/***************************************************************************/
void AstBlock::Lexify(LexStream& lex_stream)
{
	for (unsigned i = 0; i < NumStatements(); i++)
            Statement(i) -> Lexify(lex_stream);

    	label_opt_string = const_cast<wchar_t*>(lex_stream.NameString(label_opt));
}

void AstName::Lexify(LexStream& lex_stream)
{
	if (base_opt)
		base_opt -> Lexify(lex_stream);

	identifier_token_string = const_cast<wchar_t*>(lex_stream.NameString(identifier_token));
}

void AstPrimitiveType::Lexify(LexStream& lex_stream)
{
	primitive_kind_token_string = const_cast<wchar_t*>(lex_stream.NameString(primitive_kind_token));
}

void AstBrackets::Lexify(LexStream& lex_stream)
{
}

void AstArrayType::Lexify(LexStream& lex_stream)
{
	type -> Lexify(lex_stream);
	brackets -> Lexify(lex_stream);
}

void AstWildcard::Lexify(LexStream& lex_stream)
{
    	if (bounds_opt)
		bounds_opt -> Lexify(lex_stream);		

	question_token_string = const_cast<wchar_t*>(lex_stream.NameString(question_token));
	extends_token_opt_string = const_cast<wchar_t*>(lex_stream.NameString(extends_token_opt));
	super_token_opt_string = const_cast<wchar_t*>(lex_stream.NameString(super_token_opt));
}

void AstTypeArguments::Lexify(LexStream& lex_stream)
{
    	for (unsigned i = 0; i < NumTypeArguments(); i++)
		TypeArgument(i) -> Lexify(lex_stream);
}

void AstTypeName::Lexify(LexStream& lex_stream)
{
	if (base_opt)
		base_opt -> Lexify(lex_stream);

	name -> Lexify(lex_stream);	
	
    	if (type_arguments_opt)
		type_arguments_opt -> Lexify(lex_stream);
}

void AstMemberValuePair::Lexify(LexStream& lex_stream)
{
    	member_value -> Lexify(lex_stream);

	identifier_token_opt_string = const_cast<wchar_t*>(lex_stream.NameString(identifier_token_opt));
}

void AstAnnotation::Lexify(LexStream& lex_stream)
{
	name -> Lexify(lex_stream);
	for (unsigned i = 0; i < NumMemberValuePairs(); i++)
		MemberValuePair(i) -> Lexify(lex_stream);
}

void AstModifierKeyword::Lexify(LexStream& lex_stream)
{
    	modifier_token_string = const_cast<wchar_t*>(lex_stream.NameString(modifier_token));
}

void AstModifiers::Lexify(LexStream& lex_stream)
{
	for (unsigned i = 0; i < NumModifiers(); i++)
      		Modifier(i) -> Lexify(lex_stream);
}

void AstPackageDeclaration::Lexify(LexStream& lex_stream)
{
	if (modifiers_opt)
		modifiers_opt -> Lexify(lex_stream);

	name -> Lexify(lex_stream);

    	package_token_string = const_cast<wchar_t*>(lex_stream.NameString(package_token));
}

void AstImportDeclaration::Lexify(LexStream& lex_stream)
{
	name -> Lexify(lex_stream);

	import_token_string = const_cast<wchar_t*>(lex_stream.NameString(import_token));
	static_token_opt_string = const_cast<wchar_t*>(lex_stream.NameString(static_token_opt));
	star_token_opt_string = const_cast<wchar_t*>(lex_stream.NameString(star_token_opt));
}

void AstCompilationUnit::Lexify(LexStream& lex_stream)
{
	unsigned i;

	if (package_declaration_opt)
		package_declaration_opt -> Lexify(lex_stream);
	for (i = 0; i < NumImportDeclarations(); i++)
        	ImportDeclaration(i) -> Lexify(lex_stream);
	for (i = 0; i < NumTypeDeclarations(); i++)
        	TypeDeclaration(i) -> Lexify(lex_stream);

	file_name = lex_stream.FileName();
}

void AstEmptyDeclaration::Lexify(LexStream& lex_stream)
{
	semicolon_token_string = const_cast<wchar_t*>(lex_stream.NameString(semicolon_token));
}

void AstClassBody::Lexify(LexStream& lex_stream)
{
	for (unsigned i = 0; i < NumClassBodyDeclarations(); i++)
       	ClassBodyDeclaration(i) -> Lexify(lex_stream);

	identifier_token_string = const_cast<wchar_t*>(lex_stream.NameString(identifier_token));
}

void AstTypeParameter::Lexify(LexStream& lex_stream)
{
	for (unsigned i = 0; i < NumBounds(); i++)
       	Bound(i) -> Lexify(lex_stream);

	identifier_token_string = const_cast<wchar_t*>(lex_stream.NameString(identifier_token));
}

void AstTypeParameters::Lexify(LexStream& lex_stream)
{
	for (unsigned i = 0; i < NumTypeParameters(); i++)
       	TypeParameter(i) -> Lexify(lex_stream);
}

void AstClassDeclaration::Lexify(LexStream& lex_stream)
{
	if (modifiers_opt)
		modifiers_opt -> Lexify(lex_stream);

	if (type_parameters_opt)
		type_parameters_opt -> Lexify(lex_stream);
    
	if (super_opt)
		super_opt -> Lexify(lex_stream);

	for (unsigned i = 0; i < NumInterfaces(); i++)
       	Interface(i) -> Lexify(lex_stream);

	class_body -> Lexify(lex_stream);

	class_token_string = const_cast<wchar_t*>(lex_stream.NameString(class_token));
}

void AstArrayInitializer::Lexify(LexStream& lex_stream)
{
	for (unsigned i = 0; i < NumVariableInitializers(); i++)
		VariableInitializer(i) -> Lexify(lex_stream);
}

void AstVariableDeclaratorId::Lexify(LexStream& lex_stream)
{
	if (brackets_opt)
		brackets_opt -> Lexify(lex_stream);

	identifier_token_string = const_cast<wchar_t*>(lex_stream.NameString(identifier_token));
}

void AstVariableDeclarator::Lexify(LexStream& lex_stream)
{
	variable_declarator_name -> Lexify(lex_stream);
    
	if (variable_initializer_opt)
		variable_initializer_opt -> Lexify(lex_stream);
}

void AstFieldDeclaration::Lexify(LexStream& lex_stream)
{
	if (modifiers_opt)
		modifiers_opt -> Lexify(lex_stream);

    	type -> Lexify(lex_stream);
	for (unsigned i = 0; i < NumVariableDeclarators(); i++)
       	VariableDeclarator(i) -> Lexify(lex_stream);
}

void AstFormalParameter::Lexify(LexStream& lex_stream)
{
	if (modifiers_opt)
		modifiers_opt -> Lexify(lex_stream);

    	type -> Lexify(lex_stream);
	formal_declarator -> Lexify(lex_stream);

	ellipsis_token_opt_string = const_cast<wchar_t*>(lex_stream.NameString(ellipsis_token_opt));
}

void AstMethodDeclarator::Lexify(LexStream& lex_stream)
{
	for (unsigned i = 0; i < NumFormalParameters(); i++)
       	FormalParameter(i) -> Lexify(lex_stream);

	if (brackets_opt)
		brackets_opt -> Lexify(lex_stream);

	identifier_token_string = const_cast<wchar_t*>(lex_stream.NameString(identifier_token));
}

void AstMethodBody::Lexify(LexStream& lex_stream)
{
	AstBlock::Lexify(lex_stream);

	if (explicit_constructor_opt)
		explicit_constructor_opt -> Lexify(lex_stream);    
}

void AstMethodDeclaration::Lexify(LexStream& lex_stream)
{
	if (modifiers_opt)
		modifiers_opt -> Lexify(lex_stream);
   
	if (type_parameters_opt)
		type_parameters_opt -> Lexify(lex_stream);
	
    	type -> Lexify(lex_stream);
    	method_declarator -> Lexify(lex_stream);

	for (unsigned i = 0; i < NumThrows(); i++)
       	Throw(i) -> Lexify(lex_stream);

	if (default_value_opt)
		default_value_opt -> Lexify(lex_stream);
    
	if (method_body_opt)
		method_body_opt -> Lexify(lex_stream);
}

void AstInitializerDeclaration::Lexify(LexStream& lex_stream)
{
    	if (modifiers_opt)
		modifiers_opt -> Lexify(lex_stream);

	block -> Lexify(lex_stream);    
}

void AstArguments::Lexify(LexStream& lex_stream)
{
    	unsigned i;
	for (i = 0; i < NumArguments(); i++)
       	Argument(i) -> Lexify(lex_stream);
}

void AstThisCall::Lexify(LexStream& lex_stream)
{
	if (type_arguments_opt)
		type_arguments_opt -> Lexify(lex_stream);

	arguments -> Lexify(lex_stream);

	this_token_string = const_cast<wchar_t*>(lex_stream.NameString(this_token));
}

void AstSuperCall::Lexify(LexStream& lex_stream)
{
	if (base_opt)
		base_opt -> Lexify(lex_stream);
    
	if (type_arguments_opt)
		type_arguments_opt -> Lexify(lex_stream);

	arguments -> Lexify(lex_stream);

	super_token_string = const_cast<wchar_t*>(lex_stream.NameString(super_token));
}

void AstConstructorDeclaration::Lexify(LexStream& lex_stream)
{
	if (modifiers_opt)
		modifiers_opt -> Lexify(lex_stream);
    
	if (type_parameters_opt)
		type_parameters_opt -> Lexify(lex_stream);

	constructor_declarator -> Lexify(lex_stream);

	for (unsigned i = 0; i < NumThrows(); i++)
       	Throw(i) -> Lexify(lex_stream);

	constructor_body -> Lexify(lex_stream);
}

void AstEnumDeclaration::Lexify(LexStream& lex_stream)
{
	unsigned i;
    
	if (modifiers_opt)
		modifiers_opt -> Lexify(lex_stream);
	for (i = 0; i < NumInterfaces(); i++)
       	Interface(i) -> Lexify(lex_stream);

	for (i = 0; i < NumEnumConstants(); i++)
       	EnumConstant(i) -> Lexify(lex_stream);

	class_body -> Lexify(lex_stream);

	enum_token_string = const_cast<wchar_t*>(lex_stream.NameString(enum_token));
}

void AstEnumConstant::Lexify(LexStream& lex_stream)
{
	if (modifiers_opt)
		modifiers_opt -> Lexify(lex_stream);
    
    	if (arguments_opt)
		arguments_opt -> Lexify(lex_stream);    

	if (class_body_opt)
		class_body_opt -> Lexify(lex_stream);

	identifier_token_string = const_cast<wchar_t*>(lex_stream.NameString(identifier_token));
}

void AstInterfaceDeclaration::Lexify(LexStream& lex_stream)
{
	if (modifiers_opt)
		modifiers_opt -> Lexify(lex_stream);
    
	if (type_parameters_opt)
		type_parameters_opt -> Lexify(lex_stream);

	for (unsigned i = 0; i < NumInterfaces(); i++)
       	Interface(i) -> Lexify(lex_stream);

	class_body -> Lexify( lex_stream);

	interface_token_string = const_cast<wchar_t*>(lex_stream.NameString(interface_token));
}

void AstAnnotationDeclaration::Lexify(LexStream& lex_stream)
{
	if (modifiers_opt)
		modifiers_opt -> Lexify(lex_stream);
	class_body -> Lexify(lex_stream);

	interface_token_string = const_cast<wchar_t*>(lex_stream.NameString(interface_token));
}

void AstLocalVariableStatement::Lexify(LexStream& lex_stream)
{
	if (modifiers_opt)
		modifiers_opt -> Lexify(lex_stream);

	type -> Lexify(lex_stream);
	for (unsigned i = 0; i < NumVariableDeclarators(); i++)
       	VariableDeclarator(i) -> Lexify(lex_stream);
}

void AstLocalClassStatement::Lexify(LexStream& lex_stream)
{
	declaration -> Lexify(lex_stream);
}

void AstIfStatement::Lexify(LexStream& lex_stream)
{
	expression -> Lexify(lex_stream);
    	true_statement -> Lexify(lex_stream);
    
	if (false_statement_opt)
		false_statement_opt -> Lexify(lex_stream);

	if_token_string = const_cast<wchar_t*>(lex_stream.NameString(if_token));
}

void AstEmptyStatement::Lexify(LexStream& lex_stream)
{
	semicolon_token_string = const_cast<wchar_t*>(lex_stream.NameString(semicolon_token));
}

void AstExpressionStatement::Lexify(LexStream& lex_stream)
{
	expression -> Lexify(lex_stream);
}

void AstSwitchLabel::Lexify(LexStream& lex_stream)
{
	if (expression_opt)
		expression_opt -> Lexify(lex_stream);

	case_token_string = const_cast<wchar_t*>(lex_stream.NameString(case_token));
}

void AstSwitchBlockStatement::Lexify(LexStream& lex_stream)
{
	for (unsigned i = 0; i < NumSwitchLabels(); i++)
       	SwitchLabel(i) -> Lexify(lex_stream);
	AstBlock::Lexify(lex_stream);
}

void AstSwitchStatement::Lexify(LexStream& lex_stream)
{
	expression -> Lexify(lex_stream);
	switch_block -> Lexify(lex_stream);

	switch_token_string = const_cast<wchar_t*>(lex_stream.NameString(switch_token));
}

void AstWhileStatement::Lexify(LexStream& lex_stream)
{
	expression -> Lexify(lex_stream);
	statement -> Lexify(lex_stream);

	while_token_string = const_cast<wchar_t*>(lex_stream.NameString(while_token));
}

void AstDoStatement::Lexify(LexStream& lex_stream)
{
	statement -> Lexify(lex_stream);
	expression -> Lexify(lex_stream);

	do_token_string = const_cast<wchar_t*>(lex_stream.NameString(do_token));
	while_token_string = const_cast<wchar_t*>(lex_stream.NameString(while_token));
}

void AstForStatement::Lexify(LexStream& lex_stream)
{
	unsigned i;
	for (i = 0; i < NumForInitStatements(); i++)
		ForInitStatement(i) -> Lexify(lex_stream);
    
	if (end_expression_opt)
		end_expression_opt -> Lexify(lex_stream);

	for (i = 0; i < NumForUpdateStatements(); i++)
       	ForUpdateStatement(i) -> Lexify(lex_stream);

	statement -> Lexify(lex_stream);

	for_token_string = const_cast<wchar_t*>(lex_stream.NameString(for_token));
}

void AstForeachStatement::Lexify(LexStream& lex_stream)
{
	formal_parameter -> Lexify(lex_stream);
	expression -> Lexify(lex_stream);
	statement -> Lexify(lex_stream);

	for_token_string = const_cast<wchar_t*>(lex_stream.NameString(for_token));
}

void AstBreakStatement::Lexify(LexStream& lex_stream)
{
	break_token_string = const_cast<wchar_t*>(lex_stream.NameString(break_token));
    	identifier_token_opt_string = const_cast<wchar_t*>(lex_stream.NameString(identifier_token_opt));
}

void AstContinueStatement::Lexify(LexStream& lex_stream)
{
	continue_token_string = const_cast<wchar_t*>(lex_stream.NameString(continue_token));
    	identifier_token_opt_string = const_cast<wchar_t*>(lex_stream.NameString(identifier_token_opt));
}

void AstReturnStatement::Lexify(LexStream& lex_stream)
{
	if (expression_opt)
		expression_opt -> Lexify(lex_stream);

	return_token_string = const_cast<wchar_t*>(lex_stream.NameString(return_token));
}

void AstThrowStatement::Lexify(LexStream& lex_stream)
{
	expression -> Lexify(lex_stream);

	throw_token_string = const_cast<wchar_t*>(lex_stream.NameString(throw_token));
}

void AstSynchronizedStatement::Lexify(LexStream& lex_stream)
{
	expression -> Lexify(lex_stream);
    	block -> Lexify(lex_stream);

	synchronized_token_string = const_cast<wchar_t*>(lex_stream.NameString(synchronized_token));
}

void AstAssertStatement::Lexify(LexStream& lex_stream)
{
	condition -> Lexify(lex_stream);
    
	if (message_opt)
		message_opt -> Lexify(lex_stream);

	assert_token_string = const_cast<wchar_t*>(lex_stream.NameString(assert_token));
}

void AstCatchClause::Lexify(LexStream& lex_stream)
{
	formal_parameter -> Lexify(lex_stream);
	block -> Lexify(lex_stream);

	catch_token_string = const_cast<wchar_t*>(lex_stream.NameString(catch_token));
}

void AstFinallyClause::Lexify(LexStream& lex_stream)
{
	block -> Lexify(lex_stream);

	finally_token_string = const_cast<wchar_t*>(lex_stream.NameString(finally_token));
}

void AstTryStatement::Lexify(LexStream& lex_stream)
{
	block -> Lexify(lex_stream);
	for (unsigned i = 0; i < NumCatchClauses(); i++)
       	CatchClause(i) -> Lexify(lex_stream);
    
	if (finally_clause_opt)
		finally_clause_opt -> Lexify(lex_stream);

	try_token_string = const_cast<wchar_t*>(lex_stream.NameString(try_token));
}

void AstIntegerLiteral::Lexify(LexStream& lex_stream)
{
	integer_literal_token_string = const_cast<wchar_t*>(lex_stream.NameString(integer_literal_token));
}

void AstLongLiteral::Lexify(LexStream& lex_stream)
{
	long_literal_token_string = const_cast<wchar_t*>(lex_stream.NameString(long_literal_token));
}

void AstFloatLiteral::Lexify(LexStream& lex_stream)
{
	float_literal_token_string = const_cast<wchar_t*>(lex_stream.NameString(float_literal_token));
}

void AstDoubleLiteral::Lexify(LexStream& lex_stream)
{
	double_literal_token_string = const_cast<wchar_t*>(lex_stream.NameString(double_literal_token));
}

void AstTrueLiteral::Lexify(LexStream& lex_stream)
{
	true_literal_token_string = const_cast<wchar_t*>(lex_stream.NameString(true_literal_token));
}

void AstFalseLiteral::Lexify(LexStream& lex_stream)
{
	false_literal_token_string = const_cast<wchar_t*>(lex_stream.NameString(false_literal_token));
}

void AstStringLiteral::Lexify(LexStream& lex_stream)
{
	string_literal_token_string = const_cast<wchar_t*>(lex_stream.NameString(string_literal_token));
}

void AstCharacterLiteral::Lexify(LexStream& lex_stream)
{
	character_literal_token_string = const_cast<wchar_t*>(lex_stream.NameString(character_literal_token));
}

void AstNullLiteral::Lexify(LexStream& lex_stream)
{
	null_token_string = const_cast<wchar_t*>(lex_stream.NameString(null_token));
}

void AstClassLiteral::Lexify(LexStream& lex_stream)
{
	type -> Lexify(lex_stream);
    
	if (resolution_opt)
		resolution_opt -> Lexify(lex_stream);

	class_token_string = const_cast<wchar_t*>(lex_stream.NameString(class_token));
}

void AstThisExpression::Lexify(LexStream& lex_stream)
{
	if (base_opt)
		base_opt -> Lexify(lex_stream);
    
	if (resolution_opt)
		resolution_opt -> Lexify(lex_stream);
	
	this_token_string = const_cast<wchar_t*>(lex_stream.NameString(this_token));
}

void AstSuperExpression::Lexify(LexStream& lex_stream)
{
	if (base_opt)
		base_opt -> Lexify(lex_stream);
    
	if (resolution_opt)
		resolution_opt -> Lexify(lex_stream);

	super_token_string = const_cast<wchar_t*>(lex_stream.NameString(super_token));
}

void AstParenthesizedExpression::Lexify(LexStream& lex_stream)
{
	expression -> Lexify(lex_stream);
	left_parenthesis_token_string = const_cast<wchar_t*>(lex_stream.NameString(left_parenthesis_token));
	right_parenthesis_token_string = const_cast<wchar_t*>(lex_stream.NameString(right_parenthesis_token));
}

void AstClassCreationExpression::Lexify(LexStream& lex_stream)
{
	if (base_opt)
		base_opt -> Lexify(lex_stream);

	if (type_arguments_opt)
		type_arguments_opt -> Lexify(lex_stream);

	class_type -> Lexify(lex_stream);
	arguments -> Lexify(lex_stream);
    
	if (class_body_opt)
		class_body_opt -> Lexify(lex_stream);
    
	if (resolution_opt)
		resolution_opt -> Lexify(lex_stream);
	new_token_string = const_cast<wchar_t*>(lex_stream.NameString(new_token));
}

void AstDimExpr::Lexify(LexStream& lex_stream)
{
	expression -> Lexify(lex_stream);
}

void AstArrayCreationExpression::Lexify(LexStream& lex_stream)
{
	array_type -> Lexify(lex_stream);
	for (unsigned i = 0; i < NumDimExprs(); i++)
       	DimExpr(i) -> Lexify(lex_stream);
    
	if (brackets_opt)
		brackets_opt -> Lexify(lex_stream);
    
	if (array_initializer_opt)
		array_initializer_opt -> Lexify(lex_stream);
	new_token_string = const_cast<wchar_t*>(lex_stream.NameString(new_token));
}

void AstFieldAccess::Lexify(LexStream& lex_stream)
{
	base -> Lexify(lex_stream);

	if (resolution_opt)
		resolution_opt -> Lexify(lex_stream);

	identifier_token_string = const_cast<wchar_t*>(lex_stream.NameString(identifier_token));
}

void AstMethodInvocation::Lexify(LexStream& lex_stream)
{
	if (base_opt)
		base_opt -> Lexify(lex_stream);
    
	if (type_arguments_opt)
		type_arguments_opt -> Lexify(lex_stream);

	arguments -> Lexify(lex_stream);
    
    	if (resolution_opt)
		resolution_opt -> Lexify(lex_stream);
	identifier_token_string = const_cast<wchar_t*>(lex_stream.NameString(identifier_token));
}

void AstArrayAccess::Lexify(LexStream& lex_stream)
{
	base -> Lexify(lex_stream);
	expression -> Lexify(lex_stream);
}

void AstPostUnaryExpression::Lexify(LexStream& lex_stream)
{
	expression -> Lexify(lex_stream);
	post_operator_token_string = const_cast<wchar_t*>(lex_stream.NameString(post_operator_token));
}

void AstPreUnaryExpression::Lexify(LexStream& lex_stream)
{
	expression -> Lexify(lex_stream);
	pre_operator_token_string = const_cast<wchar_t*>(lex_stream.NameString(pre_operator_token));
}

void AstCastExpression::Lexify(LexStream& lex_stream)
{
	if (type)
    		type -> Lexify(lex_stream);
	expression -> Lexify(lex_stream);
	type_name = const_cast<wchar_t*>(Type() -> Name());
}

void AstBinaryExpression::Lexify(LexStream& lex_stream)
{
	left_expression -> Lexify(lex_stream);
	right_expression -> Lexify(lex_stream);

	binary_operator_token_string = const_cast<wchar_t*>(lex_stream.NameString(binary_operator_token));
}

void AstInstanceofExpression::Lexify(LexStream& lex_stream)
{
	expression -> Lexify(lex_stream);
	type -> Lexify(lex_stream);

	instanceof_token_string = const_cast<wchar_t*>(lex_stream.NameString(instanceof_token));
}

void AstConditionalExpression::Lexify(LexStream& lex_stream)
{
	test_expression -> Lexify(lex_stream);
	true_expression -> Lexify(lex_stream);
	false_expression -> Lexify(lex_stream);
}

void AstAssignmentExpression::Lexify(LexStream& lex_stream)
{
	left_hand_side -> Lexify(lex_stream);
	expression -> Lexify(lex_stream);

	assignment_operator_token_string = const_cast<wchar_t*>(lex_stream.NameString(assignment_operator_token));
}

/***************************************************************************/

#ifdef JIKES_DEBUG

void AstBlock::Print()
{
    unsigned i;
    Coutput << '#' << id << " (";
    if (label_opt)
        Coutput << label_opt_string << ": ";
    Coutput << "Block at level " << nesting_level;
    if (block_symbol)
        Coutput << ", max_variable_index "
                << block_symbol -> max_variable_index
                << ", helper_variable_index "
                << block_symbol -> helper_variable_index;
    else Coutput << ", BLOCK_SYMBOL NOT SET";
    Coutput << ')';

    if (NumStatements() > 0)
    {
        Coutput << "    {";
        for (i = 0; i < NumStatements(); i++)
        {
            if (i % 10 == 0)
                Coutput << endl << "        ";
            Coutput << " #" << Statement(i) -> id;
        }
        Coutput << "    }" << endl;
        for (i = 0; i < NumStatements(); i++)
            Statement(i) -> Print();
    }
    else Coutput << endl;
}

void AstName::Print()
{
    Coutput << '#' << id << " (Name):  #"
            << (base_opt ? base_opt -> id : 0) << '.'
            << identifier_token_string << endl;
    if (base_opt)
        base_opt -> Print();
}

void AstPrimitiveType::Print()
{
    Coutput << '#' << id << " (PrimitiveType):  "
            << primitive_kind_token_string << endl;
}

void AstBrackets::Print()
{
    Coutput << '#' << id << " (Brackets, dims=" << dims << "):  ";
	/*
    for (TokenIndex i = left_bracket_token; i <= right_bracket_token; i++)
        Coutput << lex_stream.NameString(i);
        */
    Coutput << endl;
}

void AstArrayType::Print()
{
    Coutput << '#' << id << " (ArrayType):  "
            << '#' << type -> id << ' ' << brackets -> id << endl;
    type -> Print();
    brackets -> Print();
}

void AstWildcard::Print()
{
    Coutput << '#' << id << " (Wildcard):  "
            << question_token_string;
    if (extends_token_opt)
        Coutput << ' ' << extends_token_opt_string << " #"
                << bounds_opt -> id;
    else if (super_token_opt)
        Coutput << ' ' << super_token_opt_string << " #"
                << bounds_opt -> id;
    Coutput << endl;
    if (bounds_opt)
        bounds_opt -> Print();
}

void AstTypeArguments::Print()
{
    unsigned i;
    Coutput << '#' << id << " (TypeArguments):  <";
    for (i = 0; i < NumTypeArguments(); i++)
        Coutput << " #" << TypeArgument(i) -> id;
    Coutput << '>' << endl;
    for (i = 0; i < NumTypeArguments(); i++)
        TypeArgument(i) -> Print();
}

void AstTypeName::Print()
{
    Coutput << '#' << id << " (TypeName):  #"
            << (base_opt ? base_opt -> id : 0) << ".#" << name -> id << "<#"
            << (type_arguments_opt ? type_arguments_opt -> id : 0) << '>'
            << endl;
    if (base_opt)
        base_opt -> Print();
    name -> Print();
    if (type_arguments_opt)
        type_arguments_opt -> Print();
}

void AstMemberValuePair::Print()
{
    Coutput << '#' << id << " (MemberValuePair):  "
            << (identifier_token_opt
                ? identifier_token_opt_string : L"(value)")
            << "=#" << member_value -> id << endl;
    member_value -> Print();
}

void AstAnnotation::Print()
{
    unsigned i;
    Coutput << '#' << id << " (Annotation):  #" << name -> id << '(';
    for (i = 0; i < NumMemberValuePairs(); i++)
    {
        if (i % 10 == 0)
            Coutput << endl << "       ";
        Coutput << " #" << MemberValuePair(i) -> id;
    }
    Coutput << ')' << endl;
    name -> Print();
    for (i = 0; i < NumMemberValuePairs(); i++)
        MemberValuePair(i) -> Print();
}

void AstModifierKeyword::Print()
{
    Coutput << '#' << id << " (ModifierKeyword):  "
            << modifier_token_string << endl;
}

void AstModifiers::Print()
{
    unsigned i;
    Coutput << '#' << id << " (Modifiers): ";
    for (i = 0; i < NumModifiers(); i++)
        Coutput << " #" << Modifier(i) -> id;
    Coutput << endl;
    for (i = 0; i < NumModifiers(); i++)
        Modifier(i) -> Print();
}

void AstPackageDeclaration::Print()
{
    Coutput << '#' << id << " (PackageDeclaration):  #"
            << (modifiers_opt ? modifiers_opt -> id : 0) << ' '
            << package_token_string
            << " #" << name -> id << endl;
    if (modifiers_opt)
        modifiers_opt -> Print();
    name -> Print();
}

void AstImportDeclaration::Print()
{
    Coutput << '#' << id << " (ImportDeclaration):  ";
    if (static_token_opt)
        Coutput << static_token_opt_string << ' ';
    Coutput << import_token_string
            << " #" << name -> id;
    if (star_token_opt)
        Coutput << '.' << star_token_opt_string;
    Coutput << endl;
    name -> Print();
}

void AstCompilationUnit::Print()
{
    unsigned i;
    Coutput << endl << "AST structure for "
            << file_name
            << ':' << endl << endl
            << '#' << id << " (CompilationUnit):  #"
            << (package_declaration_opt ? package_declaration_opt -> id : 0)
            << " (";
    for (i = 0; i < NumImportDeclarations(); i++)
        Coutput << " #" << ImportDeclaration(i) -> id;
    Coutput << " ) (";
    for (i = 0; i < NumTypeDeclarations(); i++)
        Coutput << " #" << TypeDeclaration(i) -> id;
    Coutput << ')' << endl;

    if (package_declaration_opt)
        package_declaration_opt -> Print();
    for (i = 0; i < NumImportDeclarations(); i++)
        ImportDeclaration(i) -> Print();
    for (i = 0; i < NumTypeDeclarations(); i++)
        TypeDeclaration(i) -> Print();
}

void AstEmptyDeclaration::Print()
{
    Coutput << '#' << id << " (EmptyDeclaration):  "
            << semicolon_token_string << endl;
}

void AstClassBody::Print()
{
    unsigned i;
    Coutput << '#' << id << " (ClassBody):  "
            << endl << "    {";
    for (i = 0; i < NumClassBodyDeclarations(); i++)
    {
        if (i % 10 == 0)
            Coutput << endl << "       ";
        Coutput << " #" << ClassBodyDeclaration(i) -> id;
    }
    Coutput << endl << "    }" << endl;

    for (i = 0; i < NumClassBodyDeclarations(); i++)
        ClassBodyDeclaration(i) -> Print();
}

void AstTypeParameter::Print()
{
    unsigned i;
    Coutput << '#' << id << " (TypeParameter):  "
            << identifier_token_string << " (";
    for (i = 0; i < NumBounds(); i++)
        Coutput << " #" << Bound(i) -> id;
    Coutput << ')' << endl;
    for (i = 0; i < NumBounds(); i++)
        Bound(i) -> Print();
}

void AstTypeParameters::Print()
{
    unsigned i;
    Coutput << '#' << id << " (TypeParameters): <";
    for (i = 0; i < NumTypeParameters(); i++)
        Coutput << " #" << TypeParameter(i) -> id;
    Coutput << '>' << endl;
    for (i = 0; i < NumTypeParameters(); i++)
        TypeParameter(i) -> Print();
}

void AstClassDeclaration::Print()
{
    unsigned i;
    Coutput << '#' << id << " (ClassDeclaration):  #"
            << (modifiers_opt ? modifiers_opt -> id : 0) << ' '
            << class_token_string << ' '
            << class_body -> identifier_token_string << " #"
            << (type_parameters_opt ? type_parameters_opt -> id : 0)
            << " #" << (super_opt ? super_opt -> id : 0) << '(';
    for (i = 0; i < NumInterfaces(); i++)
        Coutput << " #" << Interface(i) -> id;
    Coutput << ") #" << class_body -> id << endl;
    if (modifiers_opt)
        modifiers_opt -> Print();
    if (type_parameters_opt)
        type_parameters_opt -> Print();
    if (super_opt)
        super_opt -> Print();
    for (i = 0; i < NumInterfaces(); i++)
        Interface(i) -> Print();
    class_body -> Print();
}

void AstArrayInitializer::Print()
{
    unsigned i;
    Coutput << '#' << id << " (ArrayInitializer):  "
            << endl << "    {";
    for (i = 0; i < NumVariableInitializers(); i++)
    {
        if (i % 10 == 0)
            Coutput << endl << "       ";
        Coutput << " #" << VariableInitializer(i) -> id;
    }
    Coutput << endl << "    }" << endl;

    for (i = 0; i < NumVariableInitializers(); i++)
        VariableInitializer(i) -> Print();
}

void AstVariableDeclaratorId::Print()
{
    Coutput << '#' << id << " (VariableDeclaratorId):  "
            << identifier_token_string << " #"
            << (brackets_opt ? brackets_opt -> id : 0) << endl;
    if (brackets_opt)
        brackets_opt -> Print();
}

void AstVariableDeclarator::Print()
{
    Coutput << '#' << id << " (VariableDeclarator):  " << '#'
            << variable_declarator_name -> id << " #"
            << (variable_initializer_opt ? variable_initializer_opt -> id : 0)
            << endl;
    variable_declarator_name -> Print();
    if (variable_initializer_opt)
        variable_initializer_opt -> Print();
}

void AstFieldDeclaration::Print()
{
    unsigned i;
    Coutput << '#' << id << " (FieldDeclaration):  #"
            << (modifiers_opt ? modifiers_opt -> id : 0)
            << " #" << type -> id << '(';
    for (i = 0; i < NumVariableDeclarators(); i++)
        Coutput << " #" << VariableDeclarator(i) -> id;
    Coutput << ')' << endl;
    if (modifiers_opt)
        modifiers_opt -> Print();
    type -> Print();
    for (i = 0; i < NumVariableDeclarators(); i++)
        VariableDeclarator(i) -> Print();
}

void AstFormalParameter::Print()
{
    Coutput << '#' << id << " (FormalParameter):  #"
            << (modifiers_opt ? modifiers_opt -> id : 0)
            << " #" << type -> id << " #" << formal_declarator -> id << endl;
    if (modifiers_opt)
        modifiers_opt -> Print();
    type -> Print();
    if (ellipsis_token_opt)
        Coutput << ellipsis_token_opt_string;
    formal_declarator -> Print();
}

void AstMethodDeclarator::Print()
{
    unsigned i;
    Coutput << '#' << id << " (MethodDeclarator):  "
            << identifier_token_string
            << " (";
    for (i = 0; i < NumFormalParameters(); i++)
        Coutput << " #" << FormalParameter(i) -> id;
    Coutput << " ) #" << (brackets_opt ? brackets_opt -> id : 0) << endl;
    for (i = 0; i < NumFormalParameters(); i++)
        FormalParameter(i) -> Print();
    if (brackets_opt)
        brackets_opt -> Print();
}

void AstMethodBody::Print()
{
    Coutput << '#' << id << " (MethodBody):  ";
    if (explicit_constructor_opt)
        Coutput << " #" << explicit_constructor_opt -> id << endl;
    else Coutput << " #0" << endl;
    AstBlock::Print();

    if (explicit_constructor_opt)
        explicit_constructor_opt -> Print();
}

void AstMethodDeclaration::Print()
{
    unsigned i;
    Coutput << '#' << id << " (MethodDeclaration):  #"
            << (modifiers_opt ? modifiers_opt -> id : 0) << " <#"
            << (type_parameters_opt ? type_parameters_opt -> id : 0)
            << "> #" << type -> id << " #" << method_declarator -> id
            << " throws: (";
    for (i = 0; i < NumThrows(); i++)
        Coutput << " #" << Throw(i) -> id;
    Coutput << ") default #"
            << (default_value_opt ? default_value_opt -> id : 0) << ' '
            << (method_body_opt ? method_body_opt -> id : 0) << endl;
    if (modifiers_opt)
        modifiers_opt -> Print();
    if (type_parameters_opt)
        type_parameters_opt -> Print();
    type -> Print();
    method_declarator -> Print();
    for (i = 0; i < NumThrows(); i++)
        Throw(i) -> Print();
    if (default_value_opt)
        default_value_opt -> Print();
    if (method_body_opt)
        method_body_opt -> Print();
}

void AstInitializerDeclaration::Print()
{
    Coutput << '#' << id << " (InitializerDeclaration):  #"
            << (modifiers_opt ? modifiers_opt -> id : 0)
            << " #" << block -> id << endl;
    if (modifiers_opt)
        modifiers_opt -> Print();
    block -> Print();
}

void AstArguments::Print()
{
    unsigned i;
    Coutput << '#' << id << " (Arguments):  (";
    for (i = 0; i < NumArguments(); i++)
        Coutput << " #" << Argument(i) -> id;
    Coutput << ')' << endl;
    for (i = 0; i < NumArguments(); i++)
        Argument(i) -> Print();
}

void AstThisCall::Print()
{
    Coutput << '#' << id << " (ThisCall):  #"
            << (type_arguments_opt ? type_arguments_opt -> id : 0)
            << this_token_string << " #" << arguments -> id
            << endl;
    if (type_arguments_opt)
        type_arguments_opt -> Print();
    arguments -> Print();
}

void AstSuperCall::Print()
{
    Coutput << '#' << id << " (SuperCall):  #"
            << (base_opt ? base_opt -> id : 0) << ".#"
            << (type_arguments_opt ? type_arguments_opt -> id : 0)
            << super_token_string << " #" << arguments -> id
            << endl;
    if (base_opt)
        base_opt -> Print();
    if (type_arguments_opt)
        type_arguments_opt -> Print();
    arguments -> Print();
}

void AstConstructorDeclaration::Print()
{
    unsigned i;
    Coutput << '#' << id << " (ConstructorDeclaration):  #"
            << (modifiers_opt ? modifiers_opt -> id : 0) << " <#"
            << (type_parameters_opt ? type_parameters_opt -> id : 0)
            << " #" << constructor_declarator -> id << " throws: (";
    for (i = 0; i < NumThrows(); i++)
        Coutput << " #" << Throw(i) -> id;
    Coutput << ") #" << constructor_body -> id << endl;
    if (modifiers_opt)
        modifiers_opt -> Print();
    if (type_parameters_opt)
        type_parameters_opt -> Print();
    constructor_declarator -> Print();
    for (i = 0; i < NumThrows(); i++)
        Throw(i) -> Print();
    constructor_body -> Print();
}

void AstEnumDeclaration::Print()
{
    unsigned i;
    Coutput << '#' << id << " (EnumDeclaration):  #"
            << (modifiers_opt ? modifiers_opt -> id : 0) << ' '
            << enum_token_string << ' '
            << class_body -> identifier_token_string << " (";
    for (i = 0; i < NumInterfaces(); i++)
        Coutput << " #" << Interface(i) -> id;
    Coutput << ") {";
    for (i = 0; i < NumEnumConstants(); i++)
        Coutput << " #" << EnumConstant(i) -> id;
    Coutput << "} #" << class_body -> id << endl;
    if (modifiers_opt)
        modifiers_opt -> Print();
    for (i = 0; i < NumInterfaces(); i++)
        Interface(i) -> Print();
    for (i = 0; i < NumEnumConstants(); i++)
        EnumConstant(i) -> Print();
    class_body -> Print();
}

void AstEnumConstant::Print()
{
    Coutput << '#' << id << " (EnumConstant):  #"
            << (modifiers_opt ? modifiers_opt -> id : 0) << ' '
            << identifier_token_string << " #"
            << (arguments_opt ? arguments_opt -> id : 0) << " #"
            << (class_body_opt ? class_body_opt -> id : 0) << endl;
    if (modifiers_opt)
        modifiers_opt -> Print();
    if (arguments_opt)
        arguments_opt -> Print();
    if (class_body_opt)
        class_body_opt -> Print();
}

void AstInterfaceDeclaration::Print()
{
    unsigned i;
    Coutput << '#' << id << " (InterfaceDeclaration):  #"
            << (modifiers_opt ? modifiers_opt -> id : 0) << ' '
            << interface_token_string << ' '
            << class_body -> identifier_token_string << " #"
            << (type_parameters_opt ? type_parameters_opt -> id : 0) << " (";
    for (i = 0; i < NumInterfaces(); i++)
        Coutput << " #" << Interface(i) -> id;
    Coutput << ") #" << class_body -> id << endl;
    if (modifiers_opt)
        modifiers_opt -> Print();
    if (type_parameters_opt)
        type_parameters_opt -> Print();
    for (i = 0; i < NumInterfaces(); i++)
        Interface(i) -> Print();
    class_body -> Print();
}

void AstAnnotationDeclaration::Print()
{
    Coutput << '#' << id << " (AnnotationDeclaration):  #"
            << (modifiers_opt ? modifiers_opt -> id : 0) << " @"
            << interface_token_string << ' '
            << class_body -> identifier_token_string << " #"
            << class_body -> id << endl;
    if (modifiers_opt)
        modifiers_opt -> Print();
    class_body -> Print();
}

void AstLocalVariableStatement::Print()
{
    unsigned i;
    Coutput << '#' << id << " (LocalVariableStatement):  #"
            << (modifiers_opt ? modifiers_opt -> id : 0)
            << " #" << type -> id << '(';
    for (i = 0; i < NumVariableDeclarators(); i++)
        Coutput << " #" << VariableDeclarator(i) -> id;
    Coutput << ')' << endl;
    if (modifiers_opt)
        modifiers_opt -> Print();
    type -> Print();
    for (i = 0; i < NumVariableDeclarators(); i++)
        VariableDeclarator(i) -> Print();
}

void AstLocalClassStatement::Print()
{
    Coutput << '#' << id << " (LocalClassStatement): #"
            << declaration -> id << endl;
    declaration -> Print();
}

void AstIfStatement::Print()
{
    Coutput << '#' << id << " (IfStatement):  "
            << if_token_string
            << " ( #" << expression -> id << " ) #" << true_statement -> id;
    if (false_statement_opt)
        Coutput << " else #" << false_statement_opt -> id;
    else Coutput << " #0";
    Coutput << endl;

    expression -> Print();
    true_statement -> Print();
    if (false_statement_opt)
        false_statement_opt -> Print();
}

void AstEmptyStatement::Print()
{
    Coutput << '#' << id << " (EmptyStatement):  "
            << semicolon_token_string
            << endl;
}

void AstExpressionStatement::Print()
{
    Coutput << '#' << id << " (ExpressionStatement):  #" << expression -> id
            << endl;
    expression -> Print();
}

void AstSwitchLabel::Print()
{
    Coutput << '#' << id << " (SwitchLabel, map_index " << map_index << "):  "
            << case_token_string << '#'
            << (expression_opt ? expression_opt -> id : 0) << ':' << endl;
    if (expression_opt)
        expression_opt -> Print();
}

void AstSwitchBlockStatement::Print()
{
    unsigned i;
    Coutput << '#' << id << " (SwitchBlockStatement): ";
    for (i = 0; i < NumSwitchLabels(); i++)
    {
        if (i % 10 == 0)
            Coutput << endl << "        ";
        Coutput << " #" << SwitchLabel(i) -> id << ':';
    }
    Coutput << endl;
    for (i = 0; i < NumSwitchLabels(); i++)
        SwitchLabel(i) -> Print();
    AstBlock::Print();
}

void AstSwitchStatement::Print()
{
    Coutput << '#' << id << " (SwitchStatement):  "
            << switch_token_string
            << " ( #" << expression -> id << " ) #" << switch_block -> id
            << endl;
    for (unsigned i = 0; i <= num_cases; i++)
    {
        Coutput << " case index: " << i;
        if (cases[i])
            Coutput << "  block: " << cases[i] -> block_index
                    << "  label: " << cases[i] -> case_index
                    << "  value: " << cases[i] -> value << endl;
        else Coutput << "(none)" << endl;
    }
    expression -> Print();
    switch_block -> Print();
}

void AstWhileStatement::Print()
{
    Coutput << '#' << id << " (WhileStatement):  "
            << while_token_string
            << " ( #" << expression -> id << " ) #" << statement -> id << endl;
    expression -> Print();
    statement -> Print();
}

void AstDoStatement::Print()
{
    Coutput << '#' << id << " (DoStatement):  "
            << do_token_string
            << " { #" << statement -> id << " } "
            << while_token_string
            << " ( #" << expression -> id << " ) #" << endl;

    statement -> Print();
    expression -> Print();
}

void AstForStatement::Print()
{
    unsigned i;
    Coutput << '#' << id << " (ForStatement):  ("
            << for_token_string;
    for (i = 0; i < NumForInitStatements(); i++)
        Coutput << " #" << ForInitStatement(i) -> id;
    Coutput << "; #" << (end_expression_opt ? end_expression_opt -> id : 0)
            << ';';
    for (i = 0; i < NumForUpdateStatements(); i++)
        Coutput << " #" << ForUpdateStatement(i) -> id;
    Coutput << ") #" << statement -> id << endl;

    for (i = 0; i < NumForInitStatements(); i++)
        ForInitStatement(i) -> Print();
    if (end_expression_opt)
        end_expression_opt -> Print();
    for (i = 0; i < NumForUpdateStatements(); i++)
        ForUpdateStatement(i) -> Print();
    statement -> Print();
}

void AstForeachStatement::Print()
{
    Coutput << '#' << id << " (ForeachStatement):  ("
            << for_token_string << "( #"
            << formal_parameter -> id << ": #" << expression -> id
            << ") #" << statement -> id << endl;
    formal_parameter -> Print();
    expression -> Print();
    statement -> Print();
}

void AstBreakStatement::Print()
{
    Coutput << '#' << id << " (BreakStatement):  "
            << break_token_string << ' '
            << (identifier_token_opt
                ? identifier_token_opt_string : L"")
            << " at nesting_level " << nesting_level << endl;
}

void AstContinueStatement::Print()
{
    Coutput << '#' << id << " (ContinueStatement):  "
            << continue_token_string << ' '
            << (identifier_token_opt
                ? identifier_token_opt_string : L"")
            << " at nesting_level " << nesting_level << endl;
}

void AstReturnStatement::Print()
{
    Coutput << '#' << id << " (ReturnStatement):  "
            << return_token_string
            << ' '
            << " #" << (expression_opt ? expression_opt -> id : 0) << endl;
    if (expression_opt)
        expression_opt -> Print();
}

void AstThrowStatement::Print()
{
    Coutput << '#' << id << " (ThrowStatement):  "
            << throw_token_string
            << ' '
            << " #" << expression -> id << endl;
    expression -> Print();
}

void AstSynchronizedStatement::Print()
{
    Coutput << '#' << id << " (SynchronizedStatement):  "
            << synchronized_token_string
            << " ( #" << expression -> id
            << " ) #" << block -> id << endl;
    expression -> Print();
    block -> Print();
}

void AstAssertStatement::Print()
{
    Coutput << '#' << id << " (AssertStatement):  "
            << assert_token_string
            << " ( #" << condition -> id;
    if (message_opt)
        Coutput << " : " << message_opt -> id;
    else Coutput << " #0";
    Coutput << " ;" << endl;
    condition -> Print();
    if (message_opt)
        message_opt -> Print();
}

void AstCatchClause::Print()
{
    Coutput << '#' << id << " (CatchClause):  "
            << catch_token_string
            << " #" << formal_parameter -> id
            << " #" << block -> id << endl;
    formal_parameter -> Print();
    block -> Print();
}

void AstFinallyClause::Print()
{
    Coutput << '#' << id << " (FinallyClause):  "
            << finally_token_string
            << " #" << block -> id << endl;
    block -> Print();
}

void AstTryStatement::Print()
{
    unsigned i;
    Coutput << '#' << id << " (TryStatement):  "
            << try_token_string
            << " #" << block -> id
            << " catch (";
    for (i = 0; i < NumCatchClauses(); i++)
        Coutput << " #" << CatchClause(i) -> id;
    Coutput << ") finally #"
            << (finally_clause_opt ? finally_clause_opt -> id : 0) << endl;

    block -> Print();
    for (i = 0; i < NumCatchClauses(); i++)
        CatchClause(i) -> Print();
    if (finally_clause_opt)
        finally_clause_opt -> Print();
}

void AstIntegerLiteral::Print()
{
    Coutput << '#' << id << " (IntegerLiteral):  "
            << integer_literal_token_string
            << endl;
}

void AstLongLiteral::Print()
{
    Coutput << '#' << id << " (LongLiteral):  "
            << long_literal_token_string
            << endl;
}

void AstFloatLiteral::Print()
{
    Coutput << '#' << id << " (FloatLiteral):  "
            << float_literal_token_string
            << endl;
}

void AstDoubleLiteral::Print()
{
    Coutput << '#' << id << " (DoubleLiteral):  "
            << double_literal_token_string
            << endl;
}

void AstTrueLiteral::Print()
{
    Coutput << '#' << id << " (TrueLiteral):  "
            << true_literal_token_string
            << endl;
}

void AstFalseLiteral::Print()
{
    Coutput << '#' << id << " (FalseLiteral):  "
            << false_literal_token_string
            << endl;
}

void AstStringLiteral::Print()
{
    Coutput << '#' << id << " (StringLiteral):  "
            << string_literal_token_string
            << endl;
}

void AstCharacterLiteral::Print()
{
    Coutput << '#' << id << " (CharacterLiteral):  "
            << character_literal_token_string
            << endl;
}

void AstNullLiteral::Print()
{
    Coutput << '#' << id << " (NullLiteral):  "
            << null_token_string
            << endl;
}

void AstClassLiteral::Print()
{
    Coutput << '#' << id << " (ClassLiteral):  #" << type -> id << ". "
            << class_token_string << endl;
    type -> Print();
}

void AstThisExpression::Print()
{
    Coutput << '#' << id << " (ThisExpression):  ";
    if (base_opt)
        Coutput << '#' << base_opt -> id << ". ";
    Coutput << this_token_string << endl;
    if (base_opt)
        base_opt -> Print();
}

void AstSuperExpression::Print()
{
    Coutput << '#' << id << " (SuperExpression):  ";
    if (base_opt)
        Coutput << '#' << base_opt -> id << ". ";
    Coutput << super_token_string << endl;
    if (base_opt)
        base_opt -> Print();
}

void AstParenthesizedExpression::Print()
{
    Coutput << '#' << id << " (ParenthesizedExpression):  "
            << left_parenthesis_token_string
            << '#' << expression -> id
            << right_parenthesis_token_string
            << endl;
    expression -> Print();
}

void AstClassCreationExpression::Print()
{
    Coutput << '#' << id << " (ClassCreationExpression):  #"
            << (base_opt ? base_opt -> id : 0) << ' '
            << new_token_string << " #"
            << (type_arguments_opt ? type_arguments_opt -> id : 0) << " #"
            << class_type -> id << " #" << arguments -> id << " #"
            << (class_body_opt ? class_body_opt -> id : 0) << endl;
    if (base_opt)
        base_opt -> Print();
    if (type_arguments_opt)
        type_arguments_opt -> Print();
    class_type -> Print();
    arguments -> Print();
    if (class_body_opt)
        class_body_opt -> Print();
}

void AstDimExpr::Print()
{
    Coutput << '#' << id << " (DimExpr):  [ #" << expression -> id << " ]"
            << endl;
    expression -> Print();
}

void AstArrayCreationExpression::Print()
{
    unsigned i;
    Coutput << '#' << id << " (ArrayCreationExpression):  "
            << new_token_string
            << " #" << array_type -> id << "dimexpr:( ";
    for (i = 0; i < NumDimExprs(); i++)
        Coutput << " #" << DimExpr(i) -> id;
    Coutput << ") brackets:#" << (brackets_opt ? brackets_opt -> id : 0)
            << " initializer:#"
            << (array_initializer_opt ? array_initializer_opt -> id : 0)
            << endl;
    array_type -> Print();
    for (i = 0; i < NumDimExprs(); i++)
        DimExpr(i) -> Print();
    if (brackets_opt)
        brackets_opt -> Print();
    if (array_initializer_opt)
        array_initializer_opt -> Print();
}

void AstFieldAccess::Print()
{
    Coutput << '#' << id << " (FieldAccess):  "
            << " #" << base -> id << ' '
            << identifier_token_string
            << endl;

    base -> Print();
}

void AstMethodInvocation::Print()
{
    Coutput << '#' << id << " (MethodInvocation):  #"
            << (base_opt ? base_opt -> id : 0) << ".#"
            << (type_arguments_opt ? type_arguments_opt -> id : 0) << ' '
            << identifier_token_string
            << " #" << arguments -> id << endl;
    if (base_opt)
        base_opt -> Print();
    if (type_arguments_opt)
        type_arguments_opt -> Print();
    arguments -> Print();
}

void AstArrayAccess::Print()
{
    Coutput << '#' << id << " (ArrayAccess):  "
            << '#' << base -> id
            << " [ #" << expression -> id << " ]" << endl;

    base -> Print();
    expression -> Print();
}

void AstPostUnaryExpression::Print()
{
    Coutput << '#' << id << " (PostUnaryExpression):  "
            << '#' << expression -> id
            << post_operator_token_string
            << endl;

    expression -> Print();
}

void AstPreUnaryExpression::Print()
{
    Coutput << '#' << id << " (PreUnaryExpression):  ";
    if (pre_operator_token_string)
		Coutput << pre_operator_token_string;
    else
    {
    	switch(Tag())
    	{
    		case NOT:
			Coutput << L"!";
			break;
		default:
			Coutput << L"unknown";
			break;
    	}
    }
    Coutput << " #" << expression -> id << endl;
    expression -> Print();
}

void AstCastExpression::Print()
{
    if (type)
    {
        Coutput << '#' << id << " #" << expression -> id << endl;
        type -> Print();
    }
    else
    {
        Coutput << '#' << id << " (Java Semantic Cast to " << Type()->Name() // instead of type_name
                << "):  #" << expression -> id << endl;
    }
    expression -> Print();
}

void AstBinaryExpression::Print()
{
    Coutput << '#' << id << " (BinaryExpression):  "
            << '#' << left_expression -> id << ' ';
    if (binary_operator_token_string)
		Coutput << binary_operator_token_string;
    else
    {
    	switch(Tag())
    	{
    		case AND_AND:
			Coutput << L"&&";
			break;
		default:
			Coutput << L"unknown";
			break;
    	}
    }
    Coutput << " #" << right_expression -> id << endl;

    left_expression -> Print();
    right_expression -> Print();
}

void AstInstanceofExpression::Print()
{
    Coutput << '#' << id << " (InstanceofExpression):  #"
            << expression -> id << ' '
            << instanceof_token_string
            << " #" << type -> id << endl;
    expression -> Print();
    type -> Print();
}

void AstConditionalExpression::Print()
{
    Coutput << '#' << id << " (ConditionalExpression):  "
            << '#' << test_expression -> id
            << " ? #" << true_expression -> id
            << " : #" << false_expression -> id << endl;

    test_expression -> Print();
    true_expression -> Print();
    false_expression -> Print();
}

void AstAssignmentExpression::Print()
{
    Coutput << '#' << id << " (AssignmentExpression):  "
            << '#' << left_hand_side -> id << ' '
            << assignment_operator_token_string
            << " #" << expression -> id << endl;

    left_hand_side -> Print();
    expression -> Print();
}



//
// These methods allow printing the Ast structure to Coutput (usually stdout).
//
void AstBlock::Print(LexStream& lex_stream)
{
    unsigned i;
    Coutput << '#' << id << " (";
    if (label_opt)
        Coutput << lex_stream.NameString(label_opt) << ": ";
    Coutput << "Block at level " << nesting_level;
    if (block_symbol)
        Coutput << ", max_variable_index "
                << block_symbol -> max_variable_index
                << ", helper_variable_index "
                << block_symbol -> helper_variable_index;
    else Coutput << ", BLOCK_SYMBOL NOT SET";
    Coutput << ')';

    if (NumStatements() > 0)
    {
        Coutput << "    {";
        for (i = 0; i < NumStatements(); i++)
        {
            if (i % 10 == 0)
                Coutput << endl << "        ";
            Coutput << " #" << Statement(i) -> id;
        }
        Coutput << "    }" << endl;
        for (i = 0; i < NumStatements(); i++)
            Statement(i) -> Print(lex_stream);
    }
    else Coutput << endl;
}

void AstName::Print(LexStream& lex_stream)
{
    Coutput << '#' << id << " (Name):  #"
            << (base_opt ? base_opt -> id : 0) << '.'
            << lex_stream.NameString(identifier_token) << endl;
    if (base_opt)
        base_opt -> Print(lex_stream);
}

void AstPrimitiveType::Print(LexStream& lex_stream)
{
    Coutput << '#' << id << " (PrimitiveType):  "
            << lex_stream.NameString(primitive_kind_token) << endl;
}

void AstBrackets::Print(LexStream& lex_stream)
{
    Coutput << '#' << id << " (Brackets, dims=" << dims << "):  ";
    for (TokenIndex i = left_bracket_token; i <= right_bracket_token; i++)
        Coutput << lex_stream.NameString(i);
    Coutput << endl;
}

void AstArrayType::Print(LexStream& lex_stream)
{
    Coutput << '#' << id << " (ArrayType):  "
            << '#' << type -> id << ' ' << brackets -> id << endl;
    type -> Print(lex_stream);
    brackets -> Print(lex_stream);
}

void AstWildcard::Print(LexStream& lex_stream)
{
    Coutput << '#' << id << " (Wildcard):  "
            << lex_stream.NameString(question_token);
    if (extends_token_opt)
        Coutput << ' ' << lex_stream.NameString(extends_token_opt) << " #"
                << bounds_opt -> id;
    else if (super_token_opt)
        Coutput << ' ' << lex_stream.NameString(super_token_opt) << " #"
                << bounds_opt -> id;
    Coutput << endl;
    if (bounds_opt)
        bounds_opt -> Print(lex_stream);
}

void AstTypeArguments::Print(LexStream& lex_stream)
{
    unsigned i;
    Coutput << '#' << id << " (TypeArguments):  <";
    for (i = 0; i < NumTypeArguments(); i++)
        Coutput << " #" << TypeArgument(i) -> id;
    Coutput << '>' << endl;
    for (i = 0; i < NumTypeArguments(); i++)
        TypeArgument(i) -> Print(lex_stream);
}

void AstTypeName::Print(LexStream& lex_stream)
{
    Coutput << '#' << id << " (TypeName):  #"
            << (base_opt ? base_opt -> id : 0) << ".#" << name -> id << "<#"
            << (type_arguments_opt ? type_arguments_opt -> id : 0) << '>'
            << endl;
    if (base_opt)
        base_opt -> Print(lex_stream);
    name -> Print(lex_stream);
    if (type_arguments_opt)
        type_arguments_opt -> Print(lex_stream);
}

void AstMemberValuePair::Print(LexStream& lex_stream)
{
    Coutput << '#' << id << " (MemberValuePair):  "
            << (identifier_token_opt
                ? lex_stream.NameString(identifier_token_opt) : L"(value)")
            << "=#" << member_value -> id << endl;
    member_value -> Print(lex_stream);
}

void AstAnnotation::Print(LexStream& lex_stream)
{
    unsigned i;
    Coutput << '#' << id << " (Annotation):  #" << name -> id << '(';
    for (i = 0; i < NumMemberValuePairs(); i++)
    {
        if (i % 10 == 0)
            Coutput << endl << "       ";
        Coutput << " #" << MemberValuePair(i) -> id;
    }
    Coutput << ')' << endl;
    name -> Print(lex_stream);
    for (i = 0; i < NumMemberValuePairs(); i++)
        MemberValuePair(i) -> Print(lex_stream);
}

void AstModifierKeyword::Print(LexStream& lex_stream)
{
    Coutput << '#' << id << " (ModifierKeyword):  "
            << lex_stream.NameString(modifier_token) << endl;
}

void AstModifiers::Print(LexStream& lex_stream)
{
    unsigned i;
    Coutput << '#' << id << " (Modifiers): ";
    for (i = 0; i < NumModifiers(); i++)
        Coutput << " #" << Modifier(i) -> id;
    Coutput << endl;
    for (i = 0; i < NumModifiers(); i++)
        Modifier(i) -> Print(lex_stream);
}

void AstPackageDeclaration::Print(LexStream& lex_stream)
{
    Coutput << '#' << id << " (PackageDeclaration):  #"
            << (modifiers_opt ? modifiers_opt -> id : 0) << ' '
            << lex_stream.NameString(package_token)
            << " #" << name -> id << endl;
    if (modifiers_opt)
        modifiers_opt -> Print(lex_stream);
    name -> Print(lex_stream);
}

void AstImportDeclaration::Print(LexStream& lex_stream)
{
    Coutput << '#' << id << " (ImportDeclaration):  ";
    if (static_token_opt)
        Coutput << lex_stream.NameString(static_token_opt) << ' ';
    Coutput << lex_stream.NameString(import_token)
            << " #" << name -> id;
    if (star_token_opt)
        Coutput << '.' << lex_stream.NameString(star_token_opt);
    Coutput << endl;
    name -> Print(lex_stream);
}

void AstCompilationUnit::Print(LexStream& lex_stream)
{
    unsigned i;
    Coutput << endl << "AST structure for "
            << lex_stream.FileName()
            << ':' << endl << endl
            << '#' << id << " (CompilationUnit):  #"
            << (package_declaration_opt ? package_declaration_opt -> id : 0)
            << " (";
    for (i = 0; i < NumImportDeclarations(); i++)
        Coutput << " #" << ImportDeclaration(i) -> id;
    Coutput << " ) (";
    for (i = 0; i < NumTypeDeclarations(); i++)
        Coutput << " #" << TypeDeclaration(i) -> id;
    Coutput << ')' << endl;

    if (package_declaration_opt)
        package_declaration_opt -> Print(lex_stream);
    for (i = 0; i < NumImportDeclarations(); i++)
        ImportDeclaration(i) -> Print(lex_stream);
    for (i = 0; i < NumTypeDeclarations(); i++)
        TypeDeclaration(i) -> Print(lex_stream);
}

void AstEmptyDeclaration::Print(LexStream& lex_stream)
{
    Coutput << '#' << id << " (EmptyDeclaration):  "
            << lex_stream.NameString(semicolon_token) << endl;
}

void AstClassBody::Print(LexStream& lex_stream)
{
    unsigned i;
    Coutput << '#' << id << " (ClassBody):  "
            << endl << "    {";
    for (i = 0; i < NumClassBodyDeclarations(); i++)
    {
        if (i % 10 == 0)
            Coutput << endl << "       ";
        Coutput << " #" << ClassBodyDeclaration(i) -> id;
    }
    Coutput << endl << "    }" << endl;

    for (i = 0; i < NumClassBodyDeclarations(); i++)
        ClassBodyDeclaration(i) -> Print(lex_stream);
}

void AstTypeParameter::Print(LexStream& lex_stream)
{
    unsigned i;
    Coutput << '#' << id << " (TypeParameter):  "
            << lex_stream.NameString(identifier_token) << " (";
    for (i = 0; i < NumBounds(); i++)
        Coutput << " #" << Bound(i) -> id;
    Coutput << ')' << endl;
    for (i = 0; i < NumBounds(); i++)
        Bound(i) -> Print(lex_stream);
}

void AstTypeParameters::Print(LexStream& lex_stream)
{
    unsigned i;
    Coutput << '#' << id << " (TypeParameters): <";
    for (i = 0; i < NumTypeParameters(); i++)
        Coutput << " #" << TypeParameter(i) -> id;
    Coutput << '>' << endl;
    for (i = 0; i < NumTypeParameters(); i++)
        TypeParameter(i) -> Print(lex_stream);
}

void AstClassDeclaration::Print(LexStream& lex_stream)
{
    // if (kind == CLASS)
    //    Coutput << "=== is class ===" << endl;
    unsigned i;
    Coutput << '#' << id << " (ClassDeclaration):  #"
            << (modifiers_opt ? modifiers_opt -> id : 0) << ' '
            << lex_stream.NameString(class_token) << ' '
            << lex_stream.NameString(class_body -> identifier_token) << " #"
            << (type_parameters_opt ? type_parameters_opt -> id : 0)
            << " #" << (super_opt ? super_opt -> id : 0) << '(';
    for (i = 0; i < NumInterfaces(); i++)
        Coutput << " #" << Interface(i) -> id;
    Coutput << ") #" << class_body -> id << endl;
    if (modifiers_opt)
        modifiers_opt -> Print(lex_stream);
    if (type_parameters_opt)
        type_parameters_opt -> Print(lex_stream);
    if (super_opt)
        super_opt -> Print(lex_stream);
    for (i = 0; i < NumInterfaces(); i++)
        Interface(i) -> Print(lex_stream);
    class_body -> Print(lex_stream);
}

void AstArrayInitializer::Print(LexStream& lex_stream)
{
    unsigned i;
    Coutput << '#' << id << " (ArrayInitializer):  "
            << endl << "    {";
    for (i = 0; i < NumVariableInitializers(); i++)
    {
        if (i % 10 == 0)
            Coutput << endl << "       ";
        Coutput << " #" << VariableInitializer(i) -> id;
    }
    Coutput << endl << "    }" << endl;

    for (i = 0; i < NumVariableInitializers(); i++)
        VariableInitializer(i) -> Print(lex_stream);
}

void AstVariableDeclaratorId::Print(LexStream& lex_stream)
{
 /*
    Coutput << lex_stream.NameString(identifier_token) << endl;
    if (brackets_opt)
        brackets_opt -> Print(lex_stream);
  */

    Coutput << '#' << id << " (VariableDeclaratorId):  "
            << lex_stream.NameString(identifier_token) << " #"
            << (brackets_opt ? brackets_opt -> id : 0) << endl;
    if (brackets_opt)
        brackets_opt -> Print(lex_stream);
}

void AstVariableDeclarator::Print(LexStream& lex_stream)
{
    Coutput << '#' << id << " (VariableDeclarator):  " << '#'
            << variable_declarator_name -> id << " #"
            << (variable_initializer_opt ? variable_initializer_opt -> id : 0)
            << endl;
    variable_declarator_name -> Print(lex_stream);
    if (variable_initializer_opt)
        variable_initializer_opt -> Print(lex_stream);
}

void AstFieldDeclaration::Print(LexStream& lex_stream)
{
    unsigned i;
    Coutput << '#' << id << " (FieldDeclaration):  #"
            << (modifiers_opt ? modifiers_opt -> id : 0)
            << " #" << type -> id << '(';
    for (i = 0; i < NumVariableDeclarators(); i++)
        Coutput << " #" << VariableDeclarator(i) -> id;
    Coutput << ')' << endl;
    if (modifiers_opt)
        modifiers_opt -> Print(lex_stream);
    type -> Print(lex_stream);
    for (i = 0; i < NumVariableDeclarators(); i++)
        VariableDeclarator(i) -> Print(lex_stream);
}

void AstFormalParameter::Print(LexStream& lex_stream)
{
    Coutput << '#' << id << " (FormalParameter):  #"
            << (modifiers_opt ? modifiers_opt -> id : 0)
            << " #" << type -> id << " #" << formal_declarator -> id << endl;
    if (modifiers_opt)
        modifiers_opt -> Print(lex_stream);
    type -> Print(lex_stream);
    if (ellipsis_token_opt)
        Coutput << lex_stream.NameString(ellipsis_token_opt);
    formal_declarator -> Print(lex_stream);
}

void AstMethodDeclarator::Print(LexStream& lex_stream)
{
    unsigned i;
    Coutput << '#' << id << " (MethodDeclarator):  "
            << lex_stream.NameString(identifier_token)
            << " (";
    for (i = 0; i < NumFormalParameters(); i++)
        Coutput << " #" << FormalParameter(i) -> id;
    Coutput << " ) #" << (brackets_opt ? brackets_opt -> id : 0) << endl;
    for (i = 0; i < NumFormalParameters(); i++)
        FormalParameter(i) -> Print(lex_stream);
    if (brackets_opt)
        brackets_opt -> Print(lex_stream);
}

void AstMethodBody::Print(LexStream& lex_stream)
{
    Coutput << '#' << id << " (MethodBody):  ";
    if (explicit_constructor_opt)
        Coutput << " #" << explicit_constructor_opt -> id << endl;
    else Coutput << " #0" << endl;
    AstBlock::Print(lex_stream);

    if (explicit_constructor_opt)
        explicit_constructor_opt -> Print(lex_stream);
}

void AstMethodDeclaration::Print(LexStream& lex_stream)
{
    unsigned i;
    Coutput << '#' << id << " (MethodDeclaration):  #"
            << (modifiers_opt ? modifiers_opt -> id : 0) << " <#"
            << (type_parameters_opt ? type_parameters_opt -> id : 0)
            << "> #" << type -> id << " #" << method_declarator -> id
            << " throws: (";
    for (i = 0; i < NumThrows(); i++)
        Coutput << " #" << Throw(i) -> id;
    Coutput << ") default #"
            << (default_value_opt ? default_value_opt -> id : 0) << ' '
            << (method_body_opt ? method_body_opt -> id : 0) << endl;
    if (modifiers_opt)
        modifiers_opt -> Print(lex_stream);
    if (type_parameters_opt)
        type_parameters_opt -> Print(lex_stream);
    type -> Print(lex_stream);
    method_declarator -> Print(lex_stream);
    for (i = 0; i < NumThrows(); i++)
        Throw(i) -> Print(lex_stream);
    if (default_value_opt)
        default_value_opt -> Print(lex_stream);
    if (method_body_opt)
        method_body_opt -> Print(lex_stream);
}

void AstInitializerDeclaration::Print(LexStream& lex_stream)
{
    Coutput << '#' << id << " (InitializerDeclaration):  #"
            << (modifiers_opt ? modifiers_opt -> id : 0)
            << " #" << block -> id << endl;
    if (modifiers_opt)
        modifiers_opt -> Print(lex_stream);
    block -> Print(lex_stream);
}

void AstArguments::Print(LexStream& lex_stream)
{
    unsigned i;
    Coutput << '#' << id << " (Arguments):  (";
    for (i = 0; i < NumArguments(); i++)
        Coutput << " #" << Argument(i) -> id;
    Coutput << ')' << endl;
    for (i = 0; i < NumArguments(); i++)
        Argument(i) -> Print(lex_stream);
}

void AstThisCall::Print(LexStream& lex_stream)
{
    Coutput << '#' << id << " (ThisCall):  #"
            << (type_arguments_opt ? type_arguments_opt -> id : 0)
            << lex_stream.NameString(this_token) << " #" << arguments -> id
            << endl;
    if (type_arguments_opt)
        type_arguments_opt -> Print(lex_stream);
    arguments -> Print(lex_stream);
}

void AstSuperCall::Print(LexStream& lex_stream)
{
    Coutput << '#' << id << " (SuperCall):  #"
            << (base_opt ? base_opt -> id : 0) << ".#"
            << (type_arguments_opt ? type_arguments_opt -> id : 0)
            << lex_stream.NameString(super_token) << " #" << arguments -> id
            << endl;
    if (base_opt)
        base_opt -> Print(lex_stream);
    if (type_arguments_opt)
        type_arguments_opt -> Print(lex_stream);
    arguments -> Print(lex_stream);
}

void AstConstructorDeclaration::Print(LexStream& lex_stream)
{
    unsigned i;
    Coutput << '#' << id << " (ConstructorDeclaration):  #"
            << (modifiers_opt ? modifiers_opt -> id : 0) << " <#"
            << (type_parameters_opt ? type_parameters_opt -> id : 0)
            << " #" << constructor_declarator -> id << " throws: (";
    for (i = 0; i < NumThrows(); i++)
        Coutput << " #" << Throw(i) -> id;
    Coutput << ") #" << constructor_body -> id << endl;
    if (modifiers_opt)
        modifiers_opt -> Print(lex_stream);
    if (type_parameters_opt)
        type_parameters_opt -> Print(lex_stream);
    constructor_declarator -> Print(lex_stream);
    for (i = 0; i < NumThrows(); i++)
        Throw(i) -> Print(lex_stream);
    constructor_body -> Print(lex_stream);
}

void AstEnumDeclaration::Print(LexStream& lex_stream)
{
    unsigned i;
    Coutput << '#' << id << " (EnumDeclaration):  #"
            << (modifiers_opt ? modifiers_opt -> id : 0) << ' '
            << lex_stream.NameString(enum_token) << ' '
            << lex_stream.NameString(class_body -> identifier_token) << " (";
    for (i = 0; i < NumInterfaces(); i++)
        Coutput << " #" << Interface(i) -> id;
    Coutput << ") {";
    for (i = 0; i < NumEnumConstants(); i++)
        Coutput << " #" << EnumConstant(i) -> id;
    Coutput << "} #" << class_body -> id << endl;
    if (modifiers_opt)
        modifiers_opt -> Print(lex_stream);
    for (i = 0; i < NumInterfaces(); i++)
        Interface(i) -> Print(lex_stream);
    for (i = 0; i < NumEnumConstants(); i++)
        EnumConstant(i) -> Print(lex_stream);
    class_body -> Print(lex_stream);
}

void AstEnumConstant::Print(LexStream& lex_stream)
{
    Coutput << '#' << id << " (EnumConstant):  #"
            << (modifiers_opt ? modifiers_opt -> id : 0) << ' '
            << lex_stream.NameString(identifier_token) << " #"
            << (arguments_opt ? arguments_opt -> id : 0) << " #"
            << (class_body_opt ? class_body_opt -> id : 0) << endl;
    if (modifiers_opt)
        modifiers_opt -> Print(lex_stream);
    if (arguments_opt)
        arguments_opt -> Print(lex_stream);
    if (class_body_opt)
        class_body_opt -> Print(lex_stream);
}

void AstInterfaceDeclaration::Print(LexStream& lex_stream)
{
    unsigned i;
    Coutput << '#' << id << " (InterfaceDeclaration):  #"
            << (modifiers_opt ? modifiers_opt -> id : 0) << ' '
            << lex_stream.NameString(interface_token) << ' '
            << lex_stream.NameString(class_body -> identifier_token) << " #"
            << (type_parameters_opt ? type_parameters_opt -> id : 0) << " (";
    for (i = 0; i < NumInterfaces(); i++)
        Coutput << " #" << Interface(i) -> id;
    Coutput << ") #" << class_body -> id << endl;
    if (modifiers_opt)
        modifiers_opt -> Print(lex_stream);
    if (type_parameters_opt)
        type_parameters_opt -> Print(lex_stream);
    for (i = 0; i < NumInterfaces(); i++)
        Interface(i) -> Print(lex_stream);
    class_body -> Print(lex_stream);
}

void AstAnnotationDeclaration::Print(LexStream& lex_stream)
{
    Coutput << '#' << id << " (AnnotationDeclaration):  #"
            << (modifiers_opt ? modifiers_opt -> id : 0) << " @"
            << lex_stream.NameString(interface_token) << ' '
            << lex_stream.NameString(class_body -> identifier_token) << " #"
            << class_body -> id << endl;
    if (modifiers_opt)
        modifiers_opt -> Print(lex_stream);
    class_body -> Print(lex_stream);
}

void AstLocalVariableStatement::Print(LexStream& lex_stream)
{
    unsigned i;
    Coutput << '#' << id << " (LocalVariableStatement):  #"
            << (modifiers_opt ? modifiers_opt -> id : 0)
            << " #" << type -> id << '(';
    for (i = 0; i < NumVariableDeclarators(); i++)
        Coutput << " #" << VariableDeclarator(i) -> id;
    Coutput << ')' << endl;
    if (modifiers_opt)
        modifiers_opt -> Print(lex_stream);
    type -> Print(lex_stream);
    for (i = 0; i < NumVariableDeclarators(); i++)
        VariableDeclarator(i) -> Print(lex_stream);
}

void AstLocalClassStatement::Print(LexStream& lex_stream)
{
    Coutput << '#' << id << " (LocalClassStatement): #"
            << declaration -> id << endl;
    declaration -> Print(lex_stream);
}

void AstIfStatement::Print(LexStream& lex_stream)
{
    Coutput << '#' << id << " (IfStatement):  "
            << lex_stream.NameString(if_token)
            << " ( #" << expression -> id << " ) #" << true_statement -> id;
    if (false_statement_opt)
        Coutput << " else #" << false_statement_opt -> id;
    else Coutput << " #0";
    Coutput << endl;

    expression -> Print(lex_stream);
    true_statement -> Print(lex_stream);
    if (false_statement_opt)
        false_statement_opt -> Print(lex_stream);
}

void AstEmptyStatement::Print(LexStream& lex_stream)
{
    Coutput << '#' << id << " (EmptyStatement):  "
            << lex_stream.NameString(semicolon_token)
            << endl;
}

void AstExpressionStatement::Print(LexStream& lex_stream)
{
    Coutput << '#' << id << " (ExpressionStatement):  #" << expression -> id
            << endl;
    expression -> Print(lex_stream);
}

void AstSwitchLabel::Print(LexStream& lex_stream)
{
    Coutput << '#' << id << " (SwitchLabel, map_index " << map_index << "):  "
            << lex_stream.NameString(case_token) << '#'
            << (expression_opt ? expression_opt -> id : 0) << ':' << endl;
    if (expression_opt)
        expression_opt -> Print(lex_stream);
}

void AstSwitchBlockStatement::Print(LexStream& lex_stream)
{
    unsigned i;
    Coutput << '#' << id << " (SwitchBlockStatement): ";
    for (i = 0; i < NumSwitchLabels(); i++)
    {
        if (i % 10 == 0)
            Coutput << endl << "        ";
        Coutput << " #" << SwitchLabel(i) -> id << ':';
    }
    Coutput << endl;
    for (i = 0; i < NumSwitchLabels(); i++)
        SwitchLabel(i) -> Print(lex_stream);
    AstBlock::Print(lex_stream);
}

void AstSwitchStatement::Print(LexStream& lex_stream)
{
    Coutput << '#' << id << " (SwitchStatement):  "
            << lex_stream.NameString(switch_token)
            << " ( #" << expression -> id << " ) #" << switch_block -> id
            << endl;
    for (unsigned i = 0; i <= num_cases; i++)
    {
        Coutput << " case index: " << i;
        if (cases[i])
            Coutput << "  block: " << cases[i] -> block_index
                    << "  label: " << cases[i] -> case_index
                    << "  value: " << cases[i] -> value << endl;
        else Coutput << "(none)" << endl;
    }
    expression -> Print(lex_stream);
    switch_block -> Print(lex_stream);
}

void AstWhileStatement::Print(LexStream& lex_stream)
{
    Coutput << '#' << id << " (WhileStatement):  "
            << lex_stream.NameString(while_token)
            << " ( #" << expression -> id << " ) #" << statement -> id << endl;
    expression -> Print(lex_stream);
    statement -> Print(lex_stream);
}

void AstDoStatement::Print(LexStream& lex_stream)
{
    Coutput << '#' << id << " (DoStatement):  "
            << lex_stream.NameString(do_token)
            << " { #" << statement -> id << " } "
            << lex_stream.NameString(while_token)
            << " ( #" << expression -> id << " ) #" << endl;

    statement -> Print(lex_stream);
    expression -> Print(lex_stream);
}

void AstForStatement::Print(LexStream& lex_stream)
{
    unsigned i;
    Coutput << '#' << id << " (ForStatement):  ("
            << lex_stream.NameString(for_token);
    for (i = 0; i < NumForInitStatements(); i++)
        Coutput << " #" << ForInitStatement(i) -> id;
    Coutput << "; #" << (end_expression_opt ? end_expression_opt -> id : 0)
            << ';';
    for (i = 0; i < NumForUpdateStatements(); i++)
        Coutput << " #" << ForUpdateStatement(i) -> id;
    Coutput << ") #" << statement -> id << endl;

    for (i = 0; i < NumForInitStatements(); i++)
        ForInitStatement(i) -> Print(lex_stream);
    if (end_expression_opt)
        end_expression_opt -> Print(lex_stream);
    for (i = 0; i < NumForUpdateStatements(); i++)
        ForUpdateStatement(i) -> Print(lex_stream);
    statement -> Print(lex_stream);
}

void AstForeachStatement::Print(LexStream& lex_stream)
{
    Coutput << '#' << id << " (ForeachStatement):  ("
            << lex_stream.NameString(for_token) << "( #"
            << formal_parameter -> id << ": #" << expression -> id
            << ") #" << statement -> id << endl;
    formal_parameter -> Print(lex_stream);
    expression -> Print(lex_stream);
    statement -> Print(lex_stream);
}

void AstBreakStatement::Print(LexStream& lex_stream)
{
    Coutput << '#' << id << " (BreakStatement):  "
            << lex_stream.NameString(break_token) << ' '
            << (identifier_token_opt
                ? lex_stream.NameString(identifier_token_opt) : L"")
            << " at nesting_level " << nesting_level << endl;
}

void AstContinueStatement::Print(LexStream& lex_stream)
{
    Coutput << '#' << id << " (ContinueStatement):  "
            << lex_stream.NameString(continue_token) << ' '
            << (identifier_token_opt
                ? lex_stream.NameString(identifier_token_opt) : L"")
            << " at nesting_level " << nesting_level << endl;
}

void AstReturnStatement::Print(LexStream& lex_stream)
{
    Coutput << '#' << id << " (ReturnStatement):  "
            << lex_stream.NameString(return_token)
            << ' '
            << " #" << (expression_opt ? expression_opt -> id : 0) << endl;
    if (expression_opt)
        expression_opt -> Print(lex_stream);
}

void AstThrowStatement::Print(LexStream& lex_stream)
{
    Coutput << '#' << id << " (ThrowStatement):  "
            << lex_stream.NameString(throw_token)
            << ' '
            << " #" << expression -> id << endl;
    expression -> Print(lex_stream);
}

void AstSynchronizedStatement::Print(LexStream& lex_stream)
{
    Coutput << '#' << id << " (SynchronizedStatement):  "
            << lex_stream.NameString(synchronized_token)
            << " ( #" << expression -> id
            << " ) #" << block -> id << endl;
    expression -> Print(lex_stream);
    block -> Print(lex_stream);
}

void AstAssertStatement::Print(LexStream& lex_stream)
{
    Coutput << '#' << id << " (AssertStatement):  "
            << lex_stream.NameString(assert_token)
            << " ( #" << condition -> id;
    if (message_opt)
        Coutput << " : " << message_opt -> id;
    else Coutput << " #0";
    Coutput << " ;" << endl;
    condition -> Print(lex_stream);
    if (message_opt)
        message_opt -> Print(lex_stream);
}

void AstCatchClause::Print(LexStream& lex_stream)
{
    Coutput << '#' << id << " (CatchClause):  "
            << lex_stream.NameString(catch_token)
            << " #" << formal_parameter -> id
            << " #" << block -> id << endl;
    formal_parameter -> Print(lex_stream);
    block -> Print(lex_stream);
}

void AstFinallyClause::Print(LexStream& lex_stream)
{
    Coutput << '#' << id << " (FinallyClause):  "
            << lex_stream.NameString(finally_token)
            << " #" << block -> id << endl;
    block -> Print(lex_stream);
}

void AstTryStatement::Print(LexStream& lex_stream)
{
    unsigned i;
    Coutput << '#' << id << " (TryStatement):  "
            << lex_stream.NameString(try_token)
            << " #" << block -> id
            << " catch (";
    for (i = 0; i < NumCatchClauses(); i++)
        Coutput << " #" << CatchClause(i) -> id;
    Coutput << ") finally #"
            << (finally_clause_opt ? finally_clause_opt -> id : 0) << endl;

    block -> Print(lex_stream);
    for (i = 0; i < NumCatchClauses(); i++)
        CatchClause(i) -> Print(lex_stream);
    if (finally_clause_opt)
        finally_clause_opt -> Print(lex_stream);
}

void AstIntegerLiteral::Print(LexStream& lex_stream)
{
    Coutput << '#' << id << " (IntegerLiteral):  "
            << lex_stream.NameString(integer_literal_token)
            << endl;
}

void AstLongLiteral::Print(LexStream& lex_stream)
{
    Coutput << '#' << id << " (LongLiteral):  "
            << lex_stream.NameString(long_literal_token)
            << endl;
}

void AstFloatLiteral::Print(LexStream& lex_stream)
{
    Coutput << '#' << id << " (FloatLiteral):  "
            << lex_stream.NameString(float_literal_token)
            << endl;
}

void AstDoubleLiteral::Print(LexStream& lex_stream)
{
    Coutput << '#' << id << " (DoubleLiteral):  "
            << lex_stream.NameString(double_literal_token)
            << endl;
}

void AstTrueLiteral::Print(LexStream& lex_stream)
{
    Coutput << '#' << id << " (TrueLiteral):  "
            << lex_stream.NameString(true_literal_token)
            << endl;
}

void AstFalseLiteral::Print(LexStream& lex_stream)
{
    Coutput << '#' << id << " (FalseLiteral):  "
            << lex_stream.NameString(false_literal_token)
            << endl;
}

void AstStringLiteral::Print(LexStream& lex_stream)
{
    Coutput << '#' << id << " (StringLiteral):  "
            << lex_stream.NameString(string_literal_token)
            << endl;
}

void AstCharacterLiteral::Print(LexStream& lex_stream)
{
    Coutput << '#' << id << " (CharacterLiteral):  "
            << lex_stream.NameString(character_literal_token)
            << endl;
}

void AstNullLiteral::Print(LexStream& lex_stream)
{
    Coutput << '#' << id << " (NullLiteral):  "
            << lex_stream.NameString(null_token)
            << endl;
}

void AstClassLiteral::Print(LexStream& lex_stream)
{
    Coutput << '#' << id << " (ClassLiteral):  #" << type -> id << ". "
            << lex_stream.NameString(class_token) << endl;
    type -> Print(lex_stream);
}

void AstThisExpression::Print(LexStream& lex_stream)
{
    Coutput << '#' << id << " (ThisExpression):  ";
    if (base_opt)
        Coutput << '#' << base_opt -> id << ". ";
    Coutput << lex_stream.NameString(this_token) << endl;
    if (base_opt)
        base_opt -> Print(lex_stream);
}

void AstSuperExpression::Print(LexStream& lex_stream)
{
    Coutput << '#' << id << " (SuperExpression):  ";
    if (base_opt)
        Coutput << '#' << base_opt -> id << ". ";
    Coutput << lex_stream.NameString(super_token) << endl;
    if (base_opt)
        base_opt -> Print(lex_stream);
}

void AstParenthesizedExpression::Print(LexStream& lex_stream)
{
    Coutput << '#' << id << " (ParenthesizedExpression):  "
            << lex_stream.NameString(left_parenthesis_token)
            << '#' << expression -> id
            << lex_stream.NameString(right_parenthesis_token)
            << endl;
    expression -> Print(lex_stream);
}

void AstClassCreationExpression::Print(LexStream& lex_stream)
{
    Coutput << '#' << id << " (ClassCreationExpression):  #"
            << (base_opt ? base_opt -> id : 0) << ' '
            << lex_stream.NameString(new_token) << " #"
            << (type_arguments_opt ? type_arguments_opt -> id : 0) << " #"
            << class_type -> id << " #" << arguments -> id << " #"
            << (class_body_opt ? class_body_opt -> id : 0) << endl;
    if (base_opt)
        base_opt -> Print(lex_stream);
    if (type_arguments_opt)
        type_arguments_opt -> Print(lex_stream);
    class_type -> Print(lex_stream);
    arguments -> Print(lex_stream);
    if (class_body_opt)
        class_body_opt -> Print(lex_stream);
}

void AstDimExpr::Print(LexStream& lex_stream)
{
    Coutput << '#' << id << " (DimExpr):  [ #" << expression -> id << " ]"
            << endl;
    expression -> Print(lex_stream);
}

void AstArrayCreationExpression::Print(LexStream& lex_stream)
{
    unsigned i;
    Coutput << '#' << id << " (ArrayCreationExpression):  "
            << lex_stream.NameString(new_token)
            << " #" << array_type -> id << "dimexpr:( ";
    for (i = 0; i < NumDimExprs(); i++)
        Coutput << " #" << DimExpr(i) -> id;
    Coutput << ") brackets:#" << (brackets_opt ? brackets_opt -> id : 0)
            << " initializer:#"
            << (array_initializer_opt ? array_initializer_opt -> id : 0)
            << endl;
    array_type -> Print(lex_stream);
    for (i = 0; i < NumDimExprs(); i++)
        DimExpr(i) -> Print(lex_stream);
    if (brackets_opt)
        brackets_opt -> Print(lex_stream);
    if (array_initializer_opt)
        array_initializer_opt -> Print(lex_stream);
}

void AstFieldAccess::Print(LexStream& lex_stream)
{
    Coutput << '#' << id << " (FieldAccess):  "
            << " #" << base -> id << ' '
            << lex_stream.NameString(identifier_token)
            << endl;

    base -> Print(lex_stream);
}

void AstMethodInvocation::Print(LexStream& lex_stream)
{
    Coutput << '#' << id << " (MethodInvocation):  #"
            << (base_opt ? base_opt -> id : 0) << ".#"
            << (type_arguments_opt ? type_arguments_opt -> id : 0) << ' '
            << lex_stream.NameString(identifier_token)
            << " #" << arguments -> id << endl;
    if (base_opt)
        base_opt -> Print(lex_stream);
    if (type_arguments_opt)
        type_arguments_opt -> Print(lex_stream);
    arguments -> Print(lex_stream);
}

void AstArrayAccess::Print(LexStream& lex_stream)
{
    Coutput << '#' << id << " (ArrayAccess):  "
            << '#' << base -> id
            << " [ #" << expression -> id << " ]" << endl;

    base -> Print(lex_stream);
    expression -> Print(lex_stream);
}

void AstPostUnaryExpression::Print(LexStream& lex_stream)
{
    Coutput << '#' << id << " (PostUnaryExpression):  "
            << '#' << expression -> id
            << lex_stream.NameString(post_operator_token)
            << endl;

    expression -> Print(lex_stream);
}

void AstPreUnaryExpression::Print(LexStream& lex_stream)
{
    Coutput << '#' << id << " (PreUnaryExpression):  "
            << lex_stream.NameString(pre_operator_token)
            << " #" << expression -> id << endl;

    expression -> Print(lex_stream);
}

void AstCastExpression::Print(LexStream& lex_stream)
{
    if (type)
    {
        Coutput << '#' << id << " #" << expression -> id << endl;
        type -> Print(lex_stream);
    }
    else
    {
        Coutput << '#' << id << " (Java Semantic Cast to " << Type() -> Name()
                << "):  #" << expression -> id << endl;
    }
    expression -> Print(lex_stream);
}

void AstBinaryExpression::Print(LexStream& lex_stream)
{
    Coutput << '#' << id << " (BinaryExpression):  "
            << '#' << left_expression -> id << ' '
            << lex_stream.NameString(binary_operator_token)
            << " #" << right_expression -> id << endl;

    left_expression -> Print(lex_stream);
    right_expression -> Print(lex_stream);
}

void AstInstanceofExpression::Print(LexStream& lex_stream)
{
    Coutput << '#' << id << " (InstanceofExpression):  #"
            << expression -> id << ' '
            << lex_stream.NameString(instanceof_token)
            << " #" << type -> id << endl;
    expression -> Print(lex_stream);
    type -> Print(lex_stream);
}

void AstConditionalExpression::Print(LexStream& lex_stream)
{
    Coutput << '#' << id << " (ConditionalExpression):  "
            << '#' << test_expression -> id
            << " ? #" << true_expression -> id
            << " : #" << false_expression -> id << endl;

    test_expression -> Print(lex_stream);
    true_expression -> Print(lex_stream);
    false_expression -> Print(lex_stream);
}

void AstAssignmentExpression::Print(LexStream& lex_stream)
{
    Coutput << '#' << id << " (AssignmentExpression):  "
            << '#' << left_hand_side -> id << ' '
            << lex_stream.NameString(assignment_operator_token)
            << " #" << expression -> id << endl;

    left_hand_side -> Print(lex_stream);
    expression -> Print(lex_stream);
}

#endif // JIKES_DEBUG


#ifdef HAVE_JIKES_NAMESPACE
} // Close namespace Jikes block
#endif
