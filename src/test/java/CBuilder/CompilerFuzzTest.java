package CBuilder;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import org.junit.Before;
import org.junit.runner.RunWith;
import com.pholser.junit.quickcheck.From;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import static org.junit.Assert.*;
@RunWith(JQF.class)
public class CompilerFuzzTest {

    private static String BASE_PATH = "./build/compilerOutput/Fuzzing/";

    @Before
    public void initCompiler() {

    }

    private void compile(ProgramBuilder code) {
        File folder = new File(BASE_PATH + code.hashCode());
        //Ordner wurde erstellt
        assertTrue(String.valueOf(code.hashCode()),folder.exists());
        File[] listOfFiles = folder.listFiles();
        List<String> dirs = new ArrayList<>();
        List<String> files = new ArrayList<>();
        //Ordner hat Inhalt
        assertTrue(listOfFiles.length > 0);
        for (File listOfFile : listOfFiles) {
            if (listOfFile.isFile()) {
                files.add(listOfFile.getName());
            } else if (listOfFile.isDirectory()) {
                dirs.add(listOfFile.getName());
            }
        }
        //Template kopieren hat funktioniert
        assertTrue(String.valueOf(code.hashCode()),files.contains("Makefile"));
        assertTrue(String.valueOf(code.hashCode()),files.contains(".gitignore"));
        assertTrue(String.valueOf(code.hashCode()),files.contains("Doxyfile"));
        assertTrue(String.valueOf(code.hashCode()),dirs.contains("test"));
        assertTrue(String.valueOf(code.hashCode()),dirs.contains("include"));
        assertTrue(String.valueOf(code.hashCode()),dirs.contains("src"));
        if (System.getProperty("fuzzer.compile")=="1") {
            int exitcode = -1;
            try {
                ProcessBuilder processBuilder = new ProcessBuilder();
                processBuilder.directory(folder);
                processBuilder.command("make");


                Process process = processBuilder.start();
                exitcode = process.waitFor();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //Compile hat funktioniert
            assertEquals(String.valueOf(code.hashCode()),0, exitcode);
            List<String> lines = new ArrayList<String>();
            try {
                ProcessBuilder processBuilder = new ProcessBuilder();
                processBuilder.directory(folder);
                processBuilder.command("./bin/program");
                Process process = processBuilder.start();
                exitcode = process.waitFor();
                BufferedReader reader =
                    new BufferedReader(new InputStreamReader(process.getInputStream()));

                String line;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            assertFalse(String.valueOf(code.hashCode()),lines.isEmpty());
            assertEquals(String.valueOf(code.hashCode()),0, exitcode);
            assertTrue(String.valueOf(code.hashCode()),lines.getFirst().equals("----------------FUZZING-Start"));
            assertTrue(String.valueOf(code.hashCode()),lines.getLast().equals("----------------FUZZING-End"));
        }
    }

    @Fuzz
    public void testWithGenerator(@From(CBuilderGenerator.class) ProgramBuilder code)  {
        compile(code);
    }
}
