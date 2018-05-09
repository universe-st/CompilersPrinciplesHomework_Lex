import java.io.*;
import java.util.*;

public class LexicalAnalyser {

    public static boolean completed = true;

    public static String lexConfigSrcName = "lex.yy.c";
    public static String lexConfigExecName = "lexyyc";

    public static List<String> srcFilePathList;

    private static String seperator = "======================================================================";
    static {
        srcFilePathList = new ArrayList<>();
    }

    public static boolean compileLexConfig() {
        File lexSrcFile = new File(lexConfigSrcName);
        if (!lexSrcFile.exists()) {
            System.err.println("Cannot Find Lex Config Source File.");
            System.err.println("Lex config file should be name as " + lexConfigSrcName + ".");
            return false;
        }
        String command = "gcc -o " + lexConfigExecName + " " + lexConfigSrcName;
        System.out.println("Compiling Lex config...");
        // System.out.println(command);
        try {
            Process ps = Runtime.getRuntime().exec(command);
            ps.waitFor();
            if (ps.exitValue() == 0) {
                System.out.println("Compile Lex Config Succeed.");
                return true;
            } else {
                System.err.println("Lexical Configeration Compile ERROR.");
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean checkInputSrcFiles(String[] filePaths) {
        if (filePaths.length == 0) {
            System.err.println("No Source File Input.");
            return false;
        }
        for (String filePath : filePaths) {
            File src = new File(filePath);
            if (!src.exists()) {
                System.err.println(filePath + " Does Not Exists.");
                return false;
            } else {
                srcFilePathList.add(filePath);
            }
        }
        return true;
    }

    public static List<Token> lexAnalyzer(String src) throws Exception {
        List<Token> ret = new ArrayList<>();
        String line = null;
        File srcFile = new File(src);
        String fileName = srcFile.getName();
        fileName = fileName.substring(0,
                fileName.lastIndexOf(".") == -1 ? fileName.length() : fileName.lastIndexOf("."));
        String formatStr = "%-20s %-25s %-15s %-15s";
        long startTime = System.currentTimeMillis();
        try {
            List<String> command = new ArrayList<>();
            if (System.getProperty("os.name").equals("Linux")) {
                command.add("sh");
                command.add("-c");
                command.add("./" + lexConfigExecName + " < " + src);
            } else {
                command.add("cmd");
                command.add("/c");
                command.add(".\\" + lexConfigExecName + ".exe < " + src);
            }
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process ps = processBuilder.start();
            // System.out.println(command);
            BufferedInputStream inputStream = new BufferedInputStream(ps.getInputStream());
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));

            File tokenedFile = new File(fileName + ".tok");
            FileWriter fw = new FileWriter(tokenedFile);
            BufferedWriter writer = new BufferedWriter(fw);
            while ((line = br.readLine()) != null) {
                String[] tokenInfo = line.split("\t+");
                // for(int i = 0; i < tokenInfo.length;i++){
                // System.out.println(tokenInfo[i]);
                // }
                System.out.println(String.format(formatStr, tokenInfo[0], tokenInfo[1], tokenInfo[2], tokenInfo[3]));
                ret.add(new Token(tokenInfo[0], tokenInfo[1], tokenInfo[2], tokenInfo[3]));
                writer.write(String.format(formatStr, tokenInfo[0], tokenInfo[1], tokenInfo[2], tokenInfo[3]));
                writer.newLine();
            }

            boolean hasError = false;
            BufferedInputStream errorStream = new BufferedInputStream(ps.getErrorStream());
            BufferedReader ebr = new BufferedReader(new InputStreamReader(errorStream));
            while ((line = ebr.readLine()) != null) {
                System.out.println(line);
                hasError = true;
                writer.write(line);
                writer.newLine();
            }

            writer.flush();
            writer.close();
            fw.close();
            ps.waitFor();

            if (hasError) {
                throw new Exception("Lex Error In Source File: " + src);
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        long endTime = System.currentTimeMillis();
        System.out.printf("Lexical Analyzed Source File: " + src + " in %d ms.\n", endTime - startTime);
        System.out.println(seperator);
        return ret;
    }

    public static List<List<Token>> analyze(String[] filePaths) {
        if (!checkInputSrcFiles(filePaths)) {
            System.err.println("Lexical Analyer Quits. Due to invalid source files.");
            completed = false;
            return null;
        }

        if (!compileLexConfig()) {
            System.err.println("Lexical Analyser Quits. Due to lex config compilation error.");
            completed = false;
            return null;
        }
        System.out.println(seperator);
        List<List<Token>> ret = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        for (String filePath : filePaths) {
            try{
                ret.add(lexAnalyzer(filePath));
            } catch(Exception e){
                System.err.println(e.getMessage());
            }
            
        }
        long endTime = System.currentTimeMillis();
        System.out.printf("Total Analyzing Time: %d ms.\n", endTime - startTime);
        return ret;
    }

    public static void main(String[] args) {
        analyze(args);
        if (!completed)
            System.out.println("Lexcial Analyzing Aborts.");
        else {
            System.out.println("Lexical Analyzing Finishes.");
        }
    }
}

class Token {
    public String tokenType;
    public String attributeValue;
    public String lineNumber;
    public String linePosition;

    Token(String a, String b, String c, String d) {
        tokenType = a;
        attributeValue = b;
        lineNumber = c;
        linePosition = d;
    }

}
