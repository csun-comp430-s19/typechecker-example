# Typechecker Example #

Code written in class related to typechecking.

There is no lexing or parsing code in this respository; this is strictly for typechecking.
We intentionally don't build a typechecker on top of [syntax-example](https://github.com/csun-comp430-s19/syntax-example), as all syntactically-valid expressions in that language trivially are of type `int`.

We use a C-like language for this example.
The corresponding abstract grammar is below:

```
i is an integer
c is a character
var is a variable
fn is a function name
sn is a structure name
field is the name of a field in a structure
type ::= int | char | void | bool | // basic primitive types
         sn | // name of a user-defined struct
         type* // pointers
op ::= + | - | * | / | == | <
lhs ::= var | lhs.field | *lhs // used on left-hand side of assignment
exp ::= i | c | true | false | var |
        malloc(exp) | // allocates a number of bytes of memory. Runtime error if parameter <= 0
        sizeof(type) | // returns the number of bytes a value of type "type" consumes
        exp op exp |
        sn(exp*) | // creates a structure on the stack
        fn(exp*) | // calls a function
        (type)exp | // cast
        &lhs | // address-of (reference)
        *exp | // dereference
        exp.field // structure field access
varDec ::= type var
stmt ::= if (exp) { stmt } else { stmt } |
         while (exp) { stmt } |
         break |
         continue |
         varDec = exp | // combined variable declaration and initialization
         lhs = exp | // assignment
         return | // return void
         return exp | // return a value
         free(exp) | // frees memory
         stmt ; stmt | // one statement followed by another
         exp // expression statements (for calling functions with void return types)
structDec ::= sn { varDec* }
fDef ::= type fn(varDec*) { stmt }
program ::= structDec* fDef*
```

The typechecker and related semantic analysis needs to check:

- The types line up in expected places
- Function calls take expected parameters of expected types
- Stack structure creation takes expected parameters of expected types
- Structure field access accesses existant fields
- Functions return expected types
- Used variables have been declared
- `break` and `continue` are used within the body of `while`
