package edu.usf.csee.cereal.evotransform;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;
import org.metaborg.core.project.ISimpleProjectService;
import org.metaborg.core.syntax.ParseException;
import org.metaborg.spoofax.core.Spoofax;
import org.metaborg.spoofax.core.SpoofaxConstants;
import org.metaborg.spoofax.core.stratego.ResourceAgent;
import org.metaborg.spoofax.core.unit.ISpoofaxInputUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.meta.core.pluto.util.ResourceAgentTracker;
import org.metaborg.spoofax.meta.core.pluto.util.StrategoExecutor;
import org.metaborg.spoofax.meta.core.pluto.util.StrategoExecutor.ExecutionResult;
import org.metaborg.util.cmd.Arguments;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.StrategoString;

public class StrategoProxy {    

    private String langDir;
    private String template;
    private String smallerTemplate;
    private IStrategoTerm input;
    private IContext outContext;
    private String outContextFolder;
    private String eRuleTemplate;

    private StrategoProxy(String langDir, String template, String smallerTemplate, String eRuleTemplate,
            IStrategoTerm input, IContext outContext, String outContextFolder) {
        this.langDir = langDir;
        this.template = template;
        this.smallerTemplate = smallerTemplate;
        this.eRuleTemplate = eRuleTemplate;
        this.input = input;
        this.outContext = outContext;
        this.outContextFolder = outContextFolder;
    }
    public static StrategoProxy create() throws IOException, MetaborgException {
        String langDir = Paths.get("data", "lang").toAbsolutePath().toString();
        String template = 
            Files.lines(Paths.get(langDir, "trans", "_template.str"))
                .collect(Collectors.joining(System.lineSeparator()));        
        String smallerTemplate = 
            Files.lines(Paths.get(langDir, "trans", "_template2.str"))
                .collect(Collectors.joining(System.lineSeparator()));            
        String eRuleTemplate = 
            Files.lines(Paths.get(langDir, "trans", "_template3.str"))
                .collect(Collectors.joining(System.lineSeparator()));                
        IStrategoTerm input;
        IContext outContext;
        String outContextFolder; 
        try (Spoofax spoofax = new Spoofax(new CustomInjectModule())) {
            ILanguageImpl java = loadLanguage(spoofax, langDir);
            input = buildInitialTerm(spoofax, java);
            outContextFolder = Paths.get("data", "out").toString();
            FileObject projectLoc = spoofax.resourceService.resolve(outContextFolder);
            IProject outProj = ((ISimpleProjectService) spoofax.projectService).create(projectLoc);
            outContext = spoofax.contextService.get(outProj.location(), outProj, java); // output.source()
        }

        return new StrategoProxy(langDir, template, smallerTemplate, eRuleTemplate, input, outContext, outContextFolder);
    }

    public String getOutFolder() {
        return outContextFolder;
    }

    public static class InnerTest {
        public static void main(String[] args) {
            try (Spoofax spoofax = new Spoofax(new CustomInjectModule())) {
                //String customStr = template.replace("![]", modification);
                String langDir = Paths.get("data", "lang").toAbsolutePath().toString();
                String outContextFolder = Paths.get("data", "out").toString();
                StrategoOutput buildOut = buildStratego(spoofax, langDir);
                buildOut.preserve(Paths.get(outContextFolder, "innerTest.log").toString());
                //ILanguageImpl java = loadLanguage(spoofax, langDir);
                //StrategoOutput runOutput = runTransform(spoofax, java, "test-3");            
                //runOutput.preserve(Paths.get(outContextFolder, prefix, "e-gp-poc.log").toString());
                //return null;
            } catch (IOException | MetaborgException e) {
                e.printStackTrace();
                //return e;
            }                
        }
    }

    public static void main(String[] args) {
        try {
            StrategoProxy proxy = StrategoProxy.create();

            String[][] tests = 
                new String[][] {
                    new String[] { "![]", "test-1"},
                    new String[] { "![ |[ { ~stms* } ]| ]", "test-2"}
                };

            ILogger logger = LoggerUtils.logger("main");

            for (String[] test: tests)
            {                
                String modification = test[0];
                String prefix = test[1];
                logger.info("-------------------------------------");
                logger.info("Evaluating {} at {}", modification, prefix);
                Exception e = proxy.runModification(modification, prefix);
                if (e == null)
                {
                    String outFolder = Paths.get(proxy.getOutFolder(), prefix).toString();
                    String javaFile = Paths.get(outFolder, "POC.java").toString();
                    CmdRunner.Output output = CmdRunner.javac(outFolder, javaFile);
                    String outLog = Paths.get(outFolder, "javac.log").toString();
                    output.preserve(outLog);
                    output = CmdRunner.java(outFolder, "POC", 30);        
                    outLog = Paths.get(outFolder, "java.log").toString();
                    output.preserve(outLog);
                } else logger.error("Fail on runModification: {}", e);
            }

            System.out.println("Done compilation");
        } catch (IOException | MetaborgException e) {
            System.err.println("Uncontrolled error: " + e.toString());
            e.printStackTrace();
            System.exit(1);
        }

    }

    public Exception runModifications(List<String> modifications, String prefix) {
        try (Spoofax spoofax = new Spoofax(new CustomInjectModule())) {
            StringBuilder strategiesBuilder = new StringBuilder();
            for (int i = 0; i < modifications.size(); i++)
            {
                String strategy = String.format("gp-ind-%d =%n%s%n%s", i, smallerTemplate, modifications.get(i));
                strategiesBuilder.append(strategy).append(System.lineSeparator()).append(System.lineSeparator());
                String eRule = String.format("e-gp-ind-%d:%n%s%n", i, eRuleTemplate.replace("gp-poc", String.format("gp-ind-%d", i)));
                strategiesBuilder.append(eRule).append(System.lineSeparator()).append(System.lineSeparator());
            }
            String strategies = strategiesBuilder.toString();
            String customStr = String.format("%s%n%n%s", template, strategies);
            List<String> customStrLines = Arrays.asList(customStr);
            Files.write(Paths.get(langDir, "trans", "custom.str"), customStrLines);
            Path tranStrPath = Paths.get(outContextFolder, prefix, "tran.str");
            tranStrPath.getParent().toFile().mkdirs();
            Files.write(tranStrPath, customStrLines);
            StrategoOutput buildOut = buildStratego(spoofax, langDir);
            buildOut.preserve(Paths.get(outContextFolder, prefix, "strj.log").toString());
            ILanguageImpl java = loadLanguage(spoofax, langDir);
            for (int i = 0; i < modifications.size(); i++) {
                Path indPath = Paths.get(outContextFolder, prefix, String.valueOf(i));
                indPath.toFile().mkdirs();
                String indPrefix = indPath.toString();
                String transformName = String.format("e-gp-ind-%d", i);
                StrategoOutput runOutput = runTransform(spoofax, java, transformName, indPrefix);            
                runOutput.preserve(Paths.get(indPrefix, "e-gp-poc.log").toString());
            }
            return null;
        } catch (IOException | MetaborgException e) {
            
            return e;
        }
    }

    public Exception runModification(String modification, String prefix) {
        try (Spoofax spoofax = new Spoofax(new CustomInjectModule())) {
            String customStr = template.replace("![]", modification);
            List<String> customStrLines = Arrays.asList(customStr);
            Files.write(Paths.get(langDir, "trans", "custom.str"), customStrLines);
            Path tranStrPath = Paths.get(outContextFolder, prefix, "tran.str");
            tranStrPath.getParent().toFile().mkdirs();
            Files.write(tranStrPath, customStrLines);
            StrategoOutput buildOut = buildStratego(spoofax, langDir);
            buildOut.preserve(Paths.get(outContextFolder, prefix, "strj.log").toString());
            ILanguageImpl java = loadLanguage(spoofax, langDir);
            StrategoOutput runOutput = runTransform(spoofax, java, "e-gp-poc", prefix);            
            runOutput.preserve(Paths.get(outContextFolder, prefix, "e-gp-poc.log").toString());
            return null;
        } catch (IOException | MetaborgException e) {
            
            return e;
        }
    }

    // languageDir is data/lang
    /*
     * TODO: optimize - 15 sec per ind, 512 inds and 51 gens = [ strj | info ]
     * Front-end succeeded : [user/system] = [13.13s/0.00s] [ strj | info ]
     * Optimization succeeded -O 2 : [user/system] = [2.32s/0.00s] [ strj | info ]
     * Abstract syntax in
     * metaborg\stratego.rtree' [ strj | info ] Export of externals succeeded :
     * [user/system] = [0.49s/0.00s]
     */

     public static class StrategoOutput {
        public final String out;
        public final String err;
        public final Exception e;
        public final boolean success;

        public StrategoOutput(String out, String err, boolean success, Exception e) {
            this.out = out;
            this.err = err;
            this.success = success;
            this.e = e;
        }

        public void preserve(String fileName) throws IOException {
            List<String> l = new ArrayList<String>();
            l.add("-----status: " + (success ? "ok" : "FAIL"));
            if (out != null && out.length() > 0){
                l.add("------output:");
                l.add(out);
            }
            if (err != null && err.length() > 0){
                l.add("-----err:");
                l.add(err);
            }                        
            if (e != null) {
                l.add("-----exc:");
                l.add(e.toString());
                l.add(ExceptionUtils.getStackTrace(e));
            }
            Files.write(Paths.get(fileName), l);
        }

     }

    /*
        * Execute strj -i
        * ..\java-front\lang.java\trans\
        * metaborg_java.str -o
        * ..\java-front\lang.java\target\
        * metaborg\stratego.ctree -p lang.java.trans --library --clean -I \src-gen -I
        * \trans -I \lang.java --cache-dir target\stratego-cache -la stratego-lib -la
        * stratego-sglr -la stratego-gpp -la stratego-xtc -la stratego-aterm -la
        * stratego-sdf -la strc -F
    */
    // ctree but can be java - TODO
    public static StrategoOutput buildStratego(Spoofax spoofax, String languageDir) {
        final File strategoFile = Paths.get(languageDir, "trans", "metaborg_java.str").toFile();        
        final File output = Paths.get(languageDir, "target", "metaborg", "stratego.ctree").toFile();
        //output.delete(); // delete output ctree
        final File includeSrcGen = Paths.get(languageDir, "src-gen").toFile();
        final File includeTrans = Paths.get(languageDir, "trans").toFile();
        final File includeBase = Paths.get(languageDir).toFile();
        final File includeLib = Paths.get(languageDir, "lib").toFile();
        final File cacheDir = Paths.get(languageDir, "target", "stratego-cache").toFile();


        final Arguments arguments = new Arguments().addFile("-i", strategoFile) // input.inputFile)
                .addFile("-o", output) // input.outputPath)
                .addLine("-p lang.java.trans").add("--library").add("--clean").addFile("-I", includeSrcGen)
                .addFile("-I", includeTrans).addFile("-I", includeBase) // lib src-gen trans .
                                                                        // target\replicate\strj-includes)
                .addFile("-I", includeLib).addFile("--cache-dir", cacheDir).add("-la", "stratego-lib")
                .add("-la", "stratego-sglr").add("-la", "stratego-gpp").add("-la", "stratego-xtc")
                .add("-la", "stratego-aterm").add("-la", "stratego-sdf").add("-la", "strc").add("-F");
        // .add("-O", 1); //no effect on performance

        FileObject languageSpecificationDir = spoofax.resourceService.resolve(languageDir);
        final ResourceAgentTracker tracker = new ResourceAgentTracker(spoofax.resourceService, languageSpecificationDir,
                Pattern.quote("[ strj | info ]") + ".*", Pattern.quote("[ strj | error ] Compilation failed") + ".*",
                Pattern.quote("[ strj | warning ] Nullary constructor") + ".*",
                Pattern.quote("[ strj | warning ] No Stratego files found in directory") + ".*",
                Pattern.quote("[ strj | warning ] Found more than one matching subdirectory found for") + ".*",
                Pattern.quote(SpoofaxConstants.STRJ_INFO_WRITING_FILE) + ".*",
                Pattern.quote("          [\"") + ".*" + Pattern.quote("\"]"));
        final ResourceAgent agent = tracker.agent();
        agent.setAbsoluteWorkingDir(languageSpecificationDir);
        agent.setAbsoluteDefinitionDir(languageSpecificationDir);

        final ExecutionResult result = new StrategoExecutor().withStrjContext()
                .withStrategy(org.strategoxt.strj.main_0_0.instance).withTracker(tracker).withName("strj")
                .executeCLI(arguments);

        return new StrategoOutput(result.outLog, result.errLog, result.success, null);
    }


    /*Does not work*/
    // public static void buildLanguage(String languageDir) throws MetaborgException, IOException {
    //     try (Spoofax spoofax = new Spoofax(new MyModule()); SpoofaxMeta spoofaxMeta = new SpoofaxMeta(spoofax)) {
    //         FileObject languageSpecificationDir = spoofax.resourceService.resolve(languageDir);
    //         IProject project = ((ISimpleProjectService) spoofax.projectService).create(languageSpecificationDir);
    //         // IProject project = spoofax.projectService.get(languageSpecificationDir);
    //         // dependencies
    //         FileObject deps = spoofax.resourceService.resolve(languageDir + "/lib/");
    //         for (FileObject depArch : deps.findFiles(new FileExtensionSelector("spoofax-language")))
    //             spoofax.languageDiscoveryService.languageFromArchive(depArch);

    //         spoofax.languageService.getAllImpls().forEach(impl -> System.out.println("loaded " + impl.id().id));
    //         ISpoofaxLanguageSpec languageSpecification = spoofaxMeta.languageSpecService.get(project);

    //         LanguageSpecBuildInput input = new LanguageSpecBuildInput(languageSpecification);
    //         // spoofax.dependencyService.checkDependencies(project);

    //         // TODO: do not call this - it kills src-gen but generateSources cannot generate
    //         // all files
    //         // TODO: email to devs to ask question
    //         // spoofaxMeta.metaBuilder.clean(input);
    //         spoofaxMeta.metaBuilder.initialize(input);
    //         IFileAccess fileAccess = new IFileAccess() {
    //             @Override
    //             public void read(FileObject resource) {
    //                 System.out.println("file was read: " + resource.getName().toString());
    //             }

    //             @Override
    //             public void write(FileObject file) {
    //                 System.out.println("file was written: " + file.getName().toString());
    //             }

    //         };
    //         spoofaxMeta.metaBuilder.generateSources(input, fileAccess);
    //         spoofaxMeta.metaBuilder.compile(input);
    //         // do we need to archive? Do not think so
    //         spoofaxMeta.metaBuilder.pkg(input);
    //         // spoofaxMeta.metaBuilder.archive(input);
    //     }
    // }

    public static ILanguageImpl loadLanguage(Spoofax spoofax, String languageDir) throws MetaborgException {
        FileObject javaLocation = spoofax.resourceService.resolve(languageDir);
        ILanguageImpl java = spoofax.languageDiscoveryService.languageFromDirectory(javaLocation);// LanguageUtils.active(implementations);
        if (java == null) {
            System.err.println("No language implementation was found");
            System.exit(1);
        }
        return java;
    }

    public static IStrategoTerm buildInitialTerm(Spoofax spoofax, ILanguageImpl java) throws IOException, ParseException {
        FileObject javaFile = spoofax.resourceService.resolve("data/in/POC.java");
        String fileText = spoofax.sourceTextService.text(javaFile);
        ISpoofaxInputUnit input = spoofax.unitService.inputUnit(javaFile, fileText, java, null);  
        ISpoofaxParseUnit output = spoofax.syntaxService.parse(input);            
        if(!output.valid()) {
            System.err.println("Could not parse POC");
            System.exit(1);
        }
        IStrategoTerm inputTerm = spoofax.strategoCommon.builderInputTerm(output.ast(), null, null);
        return inputTerm;
    }
    //dir with language data/lang
    //or resources "res:lang.java-1.1.0-SNAPSHOT.spoofax-language";
    public StrategoOutput runTransform(Spoofax spoofax, ILanguageImpl java, String transformName, String prefix) {
        try {
            IStrategoTerm res = spoofax.strategoCommon.invoke(java, outContext, input, transformName);
            String strRes = ((StrategoString)res.getSubterm(1)).stringValue();
            //System.out.println(strRes);
            Path pocJava = Paths.get(prefix, "POC.java");
            pocJava.getParent().toFile().mkdirs();
            Files.write(pocJava, Arrays.asList(strRes));
            return new StrategoOutput("", "", true, null);
        } catch (MetaborgException | IOException e) {
            return new StrategoOutput("", "", false, e);
        }
    }

}