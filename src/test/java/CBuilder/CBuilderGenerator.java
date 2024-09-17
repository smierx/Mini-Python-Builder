package CBuilder;

import CBuilder.literals.BoolLiteral;
import CBuilder.literals.IntLiteral;
import CBuilder.literals.StringLiteral;
import CBuilder.objects.*;
import CBuilder.objects.functions.Argument;
import CBuilder.objects.functions.ReturnStatement;
import CBuilder.variables.Assignment;
import CBuilder.variables.VariableDeclaration;
import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;

import java.util.*;
import java.util.function.*;

public class CBuilderGenerator extends Generator<ProgramBuilder> {
    public CBuilderGenerator() {
        super(ProgramBuilder.class);
    }

    private ProgramBuilder program;
    private GenerationStatus status;
    private static Set<String> identifiers;
    private int statementDepth;
    private int expressionDepth;
    private static final int MAX_STATEMENT_DEPTH = 6;
    private static final int MAX_EXPRESSION_DEPTH = 10;
    private Reference printRef;
    public ProgramBuilder generate(SourceOfRandomness random, GenerationStatus status) {
        program = new ProgramBuilder();
        printRef = new Reference("print");
        List<Expression> parameterRefList = List.of(new Expression[] {new StringLiteral("----------------FUZZING-Start")});
        Call printCall = new Call(printRef, parameterRefList);
        program.addStatement(printCall);
        this.status = status;
        this.identifiers = new HashSet<>();
        this.statementDepth = 0;
        this.expressionDepth = 0;
        generateStatement(random);
        parameterRefList = List.of(new Expression[] {new StringLiteral("----------------FUZZING-End")});
        printCall = new Call(printRef, parameterRefList);
        program.addStatement(printCall);
        java.nio.file.Path fileOutput = java.nio.file.Paths.get("build/compilerOutput/Fuzzing/"+program.hashCode());
        try {
            this.program.writeProgram(fileOutput);
        } catch (Exception e) {
            System.out.println(e);
        }
        return program;
    }

    private void generateStatement(SourceOfRandomness random) {
        statementDepth++;
        //TODO: Sind die hier erwähnten Sachen richtig?
        if (statementDepth >= MAX_STATEMENT_DEPTH || random.nextBoolean()) {
            List<Consumer<SourceOfRandomness>> actions = Arrays.asList(
                //this::generateExpression
                this::generateAddition,
                //this::generateClass,//TODO: Vererbung
                this::generateFunction
                //this::generateBinaryTokens
            );
            int randomIndex = random.nextInt(actions.size());
            actions.get(randomIndex).accept(random);
        }
        else {}
        statementDepth--;
    }
    private void generateAddition(SourceOfRandomness random) {
        expressionDepth++;
        VariableDeclaration varA = new VariableDeclaration("a");
        Reference varAref = new Reference("a");
        Assignment varAassignment = random.choose(Arrays.<Function<SourceOfRandomness, Assignment>>asList(
            (r) -> this.generateInteger(r,varAref),
            (r) -> this.generateString(r,varAref),
            (r) -> this.generateBool(r,varAref)
        )).apply(random);
        VariableDeclaration varB = new VariableDeclaration("b");
        Reference varBref = new Reference("b");
        Assignment varBassignment = random.choose(Arrays.<Function<SourceOfRandomness, Assignment>>asList(
            (r) -> this.generateInteger(r,varAref),
            (r) -> this.generateString(r,varAref),
            (r) -> this.generateBool(r,varAref)
        )).apply(random);

        AttributeReference varAAdd = new AttributeReference("__add__",varAref);
        Call addInteger = new Call(varAAdd, List.of(new Expression[] {varBref}));

        this.program.addVariable(varA);
        this.program.addStatement(varAassignment);
        this.program.addVariable(varB);
        this.program.addStatement(varBassignment);
        this.program.addStatement(addInteger);
        expressionDepth--;
    }

    private void generateClass(SourceOfRandomness random) {
        // Name "A" usw. zufall basiert
        MPyClass Aclass =
            new MPyClass(
                "A",
                new Reference("__MPyType_Object"),
                List.of(
                    new CBuilder.objects.functions.Function[]{
                        new CBuilder.objects.functions.Function(
                            "print",
                            List.of(
                                new Statement[]{
                                    new Call(
                                        new Reference("print"),
                                        List.of(
                                            new Expression[]{
                                                new Reference("self")
                                            }))
                                }),
                            List.of(new Argument[]{new Argument("self", 0)}),
                            List.of()),
                        new CBuilder.objects.functions.Function(
                            "__init__",
                            List.of(
                                new Statement[]{
                                    new SuperCall(List.of()),
                                    new AttributeAssignment(
                                        new AttributeReference(
                                            "a", new Reference("self")),
                                        new IntLiteral(100))
                                }),
                            List.of(new Argument[]{new Argument("self", 0)}),
                            List.of())
                    }),
                new HashMap<>());
    }
    //TODO: add class statement
    //TODO: add class attribut print
    //TODO: Zufallbasiert was den klassen hinzugefügt wird.
    //TODO: Add Vererbung von hier

    private void generateFunction(SourceOfRandomness random) {
        //TODO verschiedene Arten von Funktionen implementieren
        //TODO: Addition usw. müssten auch gehen.
        statementDepth++;
        VariableDeclaration localVarYDec1 = new VariableDeclaration("y");
        Assignment assignYWithX = new Assignment(new Reference("y"), new Reference("x"));
        Call printY = new Call(this.printRef,List.of(new Expression[] {new Reference("y")}));
        Statement returnY = new ReturnStatement(new Reference("y"));

        List<Statement> body = List.of(new Statement[] {assignYWithX,printY,returnY});
        List<Argument> parameterArguments = List.of(new Argument[] {new Argument("x",0)});
        List<VariableDeclaration> localVariables = List.of(new VariableDeclaration[]{localVarYDec1});

        CBuilder.objects.functions.Function func1 = new CBuilder.objects.functions.Function("func1",body,parameterArguments,localVariables);//TODO: func-Counter
        this.program.addFunction(func1);
        VariableDeclaration varA = new VariableDeclaration("a");
        Reference varAref = new Reference("a");
        Assignment varAassignment = random.choose(Arrays.<Function<SourceOfRandomness, Assignment>>asList(
            (r) -> this.generateInteger(r,varAref),
            (r) -> this.generateString(r,varAref),
            (r) -> this.generateBool(r,varAref)
        )).apply(random);
        //TODO: Funktion aufruf oder nicht X-Mal
        Call callFunc = new Call(new Reference("func1"), List.of(new Expression[] {varAref}));
        Call callPrint = new Call(this.printRef,List.of(new Expression[] {callFunc}));
        this.program.addVariable(varA);
        this.program.addStatement(varAassignment);
        this.program.addStatement(callPrint);
        statementDepth--;
    }

    private Assignment generateInteger(SourceOfRandomness random,Reference var) {
        return new Assignment(var, new IntLiteral(random.nextInt(-33000,33000)));
    }

    private Assignment generateString(SourceOfRandomness random,Reference var) {
        //TODO: Überprüfen
        return new Assignment(var, new StringLiteral(gen().type(String.class).generate(random, status)));
    }

    private Assignment generateBool(SourceOfRandomness random,Reference var) {
        return new Assignment(var, new BoolLiteral(random.nextBoolean()));
    }
}
