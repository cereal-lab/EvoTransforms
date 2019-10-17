package edu.usf.csee.cereal.evotransform;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.exception.ExceptionUtils;

public class CmdRunner {

    public static class Output {
        public final List<String> out;
        public final List<String> err;
        public final int code;
        public final Exception e;

        public Output(List<String> out, List<String> err, int code, Exception e) {
            this.out = out;
            this.err = err;
            this.code = code;
            this.e = e;
        }

        public void preserve(String fileName) throws IOException {
            List<String> l = new ArrayList<String>();
            l.add("------code:" + code);
            if (out.size() > 0) {
                l.add("------output:");
                l.addAll(out);
            }
            if (err.size() > 0) {
                l.add("-----err:");
                l.addAll(err);
            }
            if (e != null) {
                l.add("-----exc:");
                l.add(e.toString());
                l.add(ExceptionUtils.getStackTrace(e));
            }
            Files.write(Paths.get(fileName), l);
        }
    }

    public static void main(String[] args) {
        javac("data/out/g0-i1", "data/out/g0-i4/POC.java");
        java("data/out/g0-i1", "POC", 30);
        System.out.println("Done compilation");
    }

    public static Output javac(String classPath, String file) {
        return runProgram(String.format("javac -cp %s %s", classPath, file), 4000, -1);
    }

    public static Output java(String classPath, String className, int maxLinesOnInfiniteLoop) {
        return runProgram(String.format("java -cp %s %s", classPath, className), 400, maxLinesOnInfiniteLoop);
    }

    public static List<String> collectLimitedOutput(InputStream stream, int maxLines) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            String line = null;
            Stream.Builder<String> builder = Stream.builder();
            while (((line = reader.readLine()) != null) && (maxLines-- > 0)) {
                builder.accept(line);
            }
            return builder.build().collect(Collectors.toList());
        }
    }

    public static List<String> collectOutput(InputStream stream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            String line = null;
            Stream.Builder<String> builder = Stream.builder();
            while ((line = reader.readLine()) != null) {
                builder.accept(line);
            }
            return builder.build().collect(Collectors.toList());
        }
    }

    public static Output runProgram(String cmd, long timeout, int maxLinesOnInfiniteLoop) {
        try {
            Process pr = Runtime.getRuntime().exec(cmd);
            Thread.yield(); //give time to created proc (assuming correct scheduling)
            boolean hasTerminated = pr.waitFor(timeout, TimeUnit.MILLISECONDS);
            List<String> out = Collections.emptyList();
            List<String> err = Collections.emptyList();
            int exitCode = -1;
            if (hasTerminated) {
                out = collectOutput(pr.getInputStream());
                err = collectOutput(pr.getErrorStream());
                exitCode = pr.exitValue();
            } else {
                Thread killingThr = new Thread(() -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    pr.destroyForcibly();
                });
                killingThr.start();
                out = collectLimitedOutput(pr.getInputStream(), maxLinesOnInfiniteLoop); //TODO: should be calculated from #expected * 2 - for poc it is ok
                err = collectLimitedOutput(pr.getErrorStream(), maxLinesOnInfiniteLoop); //this TODO is very important                
            }
            return new Output(out, err, exitCode, null);
        } catch (IOException | InterruptedException e) {            
            return new Output(Collections.emptyList(), Collections.emptyList(), -1, e);
        }        
    }
}