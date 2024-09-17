package CBuilder;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import org.junit.runner.RunWith;
import com.pholser.junit.quickcheck.From;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

@RunWith(JQF.class)
public class CompilerFuzzTest {
    @Fuzz
    public void testWithGenerator(@From(CBuilderGenerator.class) ProgramBuilder code)  {
        //
        //System.out.println(code.hashCode());
        //TODO: Check if Ordner erstellt wurde sonst fail
        File folder = new File("./build/compilerOutput/Fuzzing/"+code.hashCode());
        File[] listOfFiles = folder.listFiles();
        List<String> dirs = new ArrayList<>();
        List<String> files = new ArrayList<>();
        if(listOfFiles != null) {
            for (int i = 0; i < listOfFiles.length; i++) {
                if (listOfFiles[i].isFile()) {
                    files.add(listOfFiles[i].getName());
                } else if (listOfFiles[i].isDirectory()) {
                    dirs.add(listOfFiles[i].getName());
                }
            }
        } else {
            // Ordner wurde nicht erstellt
            System.exit(0);
        }
        if (
            files.contains("Makefile") & files.contains(".gitignore") & files.contains("Doxyfile") &
                dirs.contains("test") & dirs.contains("include") & dirs.contains("src")
        ) {
            try {
                ProcessBuilder processBuilder = new ProcessBuilder();
                processBuilder.directory(folder);
                processBuilder.command("make");//"-C", "./build/compilerOutput/Fuzzing" + code.hashCode()
                Process process = processBuilder.start();
                //Check if exit code ist richtig!
                int exitcode = process.waitFor();
                System.out.println(exitcode);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        } else {
            // Ordner wurde nicht richtig erstellt
            System.exit(0);
        }
        //TODO: Check if program erstellt wurde sonst fail
        //TODO: Check if ----FuzzerEnd letzte Zeile sonst fail

    }
}
