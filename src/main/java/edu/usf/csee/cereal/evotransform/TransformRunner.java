package edu.usf.csee.cereal.evotransform;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.context.ContextException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;
import org.metaborg.core.project.ISimpleProjectService;
import org.metaborg.core.syntax.ParseException;
import org.metaborg.spoofax.core.Spoofax;
import org.metaborg.spoofax.core.unit.ISpoofaxInputUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.StrategoString;

public final class TransformRunner {

    // static final String[][] transforms = new String[][] {
    // // "e-test-occurrences",
    // // "e-package-decl-mod-v4",
    // //{ "A1", "e-stats-occurrences-caceffo-A1-scanf" }, { "A2",
    // "e-stats-occurrences-caceffo-A2" },
    // //{ "A3", "e-stats-occurrences-caceffo-A3" }, { "A4",
    // "e-stats-occurrences-caceffo-A4" },
    // { "A5", "e-stats-occurrences-caceffo-A5" }, { "A5c",
    // "e-stats-occurrences-caceffo-A5-complex" },
    // { "A6", "e-stats-occurrences-caceffo-A6" }, { "B1",
    // "e-stats-occurrences-caceffo-B1" },
    // { "B2B4", "e-stats-occurrences-caceffo-B2-B4" }, { "B2la",
    // "e-stats-occurrences-caceffo-B2-loop-acc" },
    // { "B3", "e-stats-occurrences-caceffo-B3" }, { "C2",
    // "e-stats-occurrences-caceffo-C2" },
    // { "C3", "e-stats-occurrences-caceffo-C3" }, { "D1",
    // "e-stats-occurrences-caceffo-D1" },
    // { "D2", "e-stats-occurrences-caceffo-D2" }, { "D3i",
    // "e-stats-occurrences-caceffo-D3-init" },
    // { "D3e", "e-stats-occurrences-caceffo-D3-exit" }, { "D5",
    // "e-stats-occurrences-caceffo-D5" },
    // { "E1v1", "e-stats-occurrences-caceffo-E1-v1" }, { "E1v2",
    // "e-stats-occurrences-caceffo-E1-v2" },
    // { "E2", "e-stats-occurrences-caceffo-E2" }, { "E3",
    // "e-stats-occurrences-caceffo-E3" },
    // { "E4", "e-stats-occurrences-caceffo-E4" }, { "E5",
    // "e-stats-occurrences-caceffo-E5" },
    // { "G1v1", "e-stats-occurrences-caceffo-G1-v1" }, { "G1v2",
    // "e-stats-occurrences-caceffo-G1-v2" },
    // { "G2", "e-stats-occurrences-caceffo-G2" }, { "G3",
    // "e-stats-occurrences-caceffo-G3" },
    // { "G4", "e-stats-occurrences-caceffo-G4" } };

    public static String applyTransform(Spoofax spoofax, ILanguageImpl java, IContext context, IStrategoTerm inputTerm,
            String transform) throws MetaborgException {
        IStrategoTerm res = spoofax.strategoCommon.invoke(java, context, inputTerm, transform);
        return res.getSubterm(1).toString();
    }

    /**
     * 
     * @param args args[0] should be the folder to which transforms should be
     *             applied args[1] should be pattern for file name args[2] path url
     *             to language pack args[3] mode - should be trans (for transform)
     *             or stats:<fileName> (for stats) args[3..] transform names in form
     *             simpleName:complexName or complexName complexName is the name
     *             from spoofax package simple name is shown in output
     * @return Side effect is creation of file with stats in user.dir, file name is
     *         transform-stats.csv
     * @throws InterpreterException
     */
    public static void main(String[] args) throws InterpreterException {
        String casesFolderString = args[0];
        String filePatternString = args[1];
        String languagePath = args[2];
        String mode = args[3]; // trans for actual transforms and stats:<fileName> for statistics

        List<String[]> transforms = Arrays.stream(args).skip(3).map(name -> {
            String[] splitted = name.split(":");
            return new String[] { splitted[0], splitted.length > 1 ? splitted[1] : splitted[0] };
        }).collect(Collectors.toList());

        // String path = Paths.get(System.getProperty("user.dir"), "code").toString();
        // System.setProperty("user.dir", path);
        try (final Spoofax spoofax = new Spoofax(new CustomInjectModule())) {
            // String javaUrl = "res:lang.java-1.1.0-SNAPSHOT.spoofax-language";
            FileObject javaLocation = spoofax.resourceService.resolve(languagePath);
            ILanguageImpl language = javaLocation.isFolder()
                    ? spoofax.languageDiscoveryService.languageFromDirectory(javaLocation)
                    : spoofax.languageDiscoveryService.languageFromArchive(javaLocation);// LanguageUtils.active(implementations);

            if (language == null) {
                System.err.format("[TransformRunner] No language at %s", languagePath);
                System.exit(1);
            }
            System.out.format("[TransformRunner] Loaded %s", language.toString());

            // final String folder = "liang10e\\exercises";
            final File casesFolder = new File(casesFolderString);
            // Pattern p =
            // Pattern.compile("Exercise0*(?<chapter>\\d*)_0*(?:\\d*)(?:Extra)?.java");
            Pattern filePattern = null;
            try {
                filePattern = Pattern.compile(filePatternString);
            } catch (PatternSyntaxException e) {
                System.err.format("[TransformRunner] Pattern is bad: %s", filePattern);
                System.exit(1);
            }
            FileObject projectLoc = spoofax.resourceService.resolve(casesFolderString);
            IProject proj = ((ISimpleProjectService) spoofax.projectService).create(projectLoc);
            
            String[] modeParams = mode.split(":");
            switch (modeParams[0])
            {
                case "stats":
                {
                    System.out.format("[TransformRunner] Occurence stats for %s%n", casesFolder.toString());
                    try (PrintWriter out = new PrintWriter(modeParams[1])) {
                        // printing header
                        for (String[] transform : transforms) {
                            out.format(",%s", transform[0]);
                        }
                        out.println();
                        actionForOneFile(spoofax, casesFolder, filePattern, proj, language, 
                            transforms, (res, e) -> {
                                if (e != null) {
                                    out.format(",%d", -1);
                                } else {
                                    try {                                
                                        Integer oc = Integer.valueOf(res.getSubterm(1).toString());
                                        out.format(",%d", oc);
                                    } catch (Exception ex) {
                                        out.format(",%d", -1);
                                    }
                                }
                            }, (file) -> out.println());
                        /*
                        for (final File file : casesFolder.listFiles()) {
                            Matcher fileMatch = filePattern.matcher(file.getName());
                            if (fileMatch.matches()) {
                                // int chapter = Integer.valueOf(fileMatch.group("chapter"));
                                // if (chapter < 4 || chapter > 8) continue;
                                String fileName = file.getName();
                                FileObject fileObj = spoofax.resourceService
                                        .resolve(Paths.get(casesFolderString, fileName).toString());
                                String fileContent = spoofax.sourceTextService.text(fileObj);
                                ISpoofaxInputUnit input = spoofax.unitService.inputUnit(fileObj, fileContent, language, null);
                                ISpoofaxParseUnit output = spoofax.syntaxService.parse(input);
                                if (!output.valid()) {
                                    System.err.println("Could not parse " + fileName);
                                    continue;
                                }
                                IContext context = spoofax.contextService.get(output.source(), proj, language);
                                IStrategoTerm inputTerm = spoofax.strategoCommon.builderInputTerm(output.ast(), null, null);
                                for (String[] transform : transforms) {
                                    // Optional<Integer> occurrenceOpt = getOccurrences(spoofax, language, context,
                                    // inputTerm, transform[1]);
                                    Optional<Integer> occurentcesOpt = Optional.empty();
                                    try {
                                        IStrategoTerm res = spoofax.strategoCommon.invoke(language, context, inputTerm,
                                                transform[1]);
                                        occurentcesOpt = Optional.of(Integer.valueOf(res.getSubterm(1).toString()));
                                    } catch (MetaborgException e) {
                                    }
                                    if (occurentcesOpt.isPresent())
                                        out.format(",%d", occurentcesOpt.get());
                                    else
                                        out.format(",%d", -1);
                                }
                                out.println();
                            }
                        }
                        */
                    } 
                    break;       
                }
                default: {
                    System.out.format("[TransformRunner] Performing transform of %s%n", casesFolder.toString());
                    actionForOneFile(spoofax, casesFolder, filePattern, proj, language, 
                    transforms, (res, e) -> {   
                        if (e != null) {
                            System.err.format("[TransformRunner] Transform failed during application: %s", e.getMessage());
                        }
                        try {
                            String fileName = ((StrategoString)res.getSubterm(0)).stringValue();
                            String result = ((StrategoString)res.getSubterm(1)).stringValue();
                            try {
                                Files.write(Paths.get(fileName), Arrays.asList(result));
                            } catch (IOException ex) {
                                System.err.format("[TransformRunner] Cannot write to %s.%nContent %s.%nError %s%n", fileName, result, ex.getMessage());
                            }
                        } catch (Exception ex) {
                            System.err.format("[TransformRunner] Transform return is not a tuple of (fileName, result)");
                        }
                    }, (file) -> {});
                    break;
                }
            }
        } catch (IOException | MetaborgException e) {
            System.err.println("[TransformRunner] Failed: %s" + e.getMessage());
            // e.printStackTrace();
        }
    }

    private static void actionForOneFile(Spoofax spoofax, File casesFolder, Pattern filePattern, IProject proj,
            ILanguageImpl language, List<String[]> transforms, BiConsumer<IStrategoTerm, MetaborgException> resultProcessor,
            Consumer<File> afterAllTransforms) throws IOException, ParseException, ContextException {
        for (final File file: casesFolder.listFiles())
        {
            Matcher fileMatch = filePattern.matcher(file.getName());                    
            if (fileMatch.matches())
            {
                //int chapter = Integer.valueOf(fileMatch.group("chapter"));
                //if (chapter < 4 || chapter > 8) continue;
                String fileName = file.getName();
                FileObject fileObj = spoofax.resourceService.resolve(Paths.get(casesFolder.toString(), fileName).toString());
                String fileContent = spoofax.sourceTextService.text(fileObj);
                ISpoofaxInputUnit input = spoofax.unitService.inputUnit(fileObj, fileContent, language, null);  
                ISpoofaxParseUnit output = spoofax.syntaxService.parse(input);            
                if(!output.valid()) {
                    System.err.println("Could not parse " + fileName);
                    continue;
                }
                IContext context = spoofax.contextService.get(output.source(), proj, language);
                IStrategoTerm inputTerm = spoofax.strategoCommon.builderInputTerm(output.ast(), null, null);

                for (String[] transform: transforms)
                {
                    IStrategoTerm res = null;
                    MetaborgException ex = null;
                    try {
                        res = spoofax.strategoCommon.invoke(language, context, inputTerm, transform[1]);                        
                    } catch (MetaborgException e) {
                        ex = e;
                        //System.err.format("[TransformRunner] Cannot run transform %s:%s - %s", transform[0], transform[1], e.getMessage());
                    }  
                    resultProcessor.accept(res, ex);                  
                }      
                afterAllTransforms.accept(file);
            }
        }        
    }
}
