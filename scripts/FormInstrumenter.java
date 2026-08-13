import com.intellij.compiler.instrumentation.InstrumentationClassFinder;
import com.intellij.compiler.instrumentation.InstrumenterClassWriter;
import com.intellij.uiDesigner.compiler.AsmCodeGenerator;
import com.intellij.uiDesigner.compiler.NestedFormLoader;
import com.intellij.uiDesigner.compiler.Utils;
import com.intellij.uiDesigner.lw.AsmClassPropertiesProvider;
import com.intellij.uiDesigner.lw.LwRootContainer;
import org.jetbrains.org.objectweb.asm.ClassReader;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Injects IntelliJ GUI Designer form initialization code into compiled classes.
 */
public class FormInstrumenter {
    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            throw new IllegalArgumentException("Usage: FormInstrumenter <classesDir> <classPath> <formFile>...");
        }

        File classesDir = new File(args[0]);
        String[] classPathEntries = args[1].split(File.pathSeparator);
        URL[] classPathUrls = new URL[classPathEntries.length + 1];
        classPathUrls[0] = classesDir.toURI().toURL();
        for (int index = 0; index < classPathEntries.length; index++) {
            classPathUrls[index + 1] = new File(classPathEntries[index]).toURI().toURL();
        }

        InstrumentationClassFinder classFinder = new InstrumentationClassFinder(classPathUrls,
                new URL[]{InstrumentationClassFinder.createJDKPlatformUrl(System.getProperty("java.home"))});
        AsmClassPropertiesProvider propertiesProvider = new AsmClassPropertiesProvider(classFinder);
        NestedFormLoader nestedFormLoader = new NestedFormLoader() {
            @Override
            public LwRootContainer loadForm(String formFileName) throws Exception {
                return Utils.getRootContainer(new File(formFileName).toURI().toURL(), propertiesProvider);
            }

            @Override
            public String getClassToBindName(LwRootContainer rootContainer) {
                return rootContainer.getClassToBind();
            }
        };

        try {
            for (int index = 2; index < args.length; index++) {
                File formFile = new File(args[index]);
                LwRootContainer rootContainer = Utils.getRootContainer(formFile.toURI().toURL(), propertiesProvider);
                File classFile = new File(classesDir,
                        rootContainer.getClassToBind().replace('.', File.separatorChar) + ".class");
                try (FileInputStream inputStream = new FileInputStream(classFile)) {
                    ClassReader classReader = new ClassReader(inputStream);
                    InstrumenterClassWriter classWriter = new InstrumenterClassWriter(classReader,
                            InstrumenterClassWriter.getAsmClassWriterFlags(
                                    InstrumenterClassWriter.getClassFileVersion(classReader)), classFinder);
                    AsmCodeGenerator generator = new AsmCodeGenerator(rootContainer, classFinder, nestedFormLoader,
                            false, false, classWriter);
                    generator.patchFile(classFile);
                    if (generator.getErrors().length > 0) {
                        throw new IllegalStateException("Failed to instrument " + formFile);
                    }
                }
            }
        } finally {
            classFinder.releaseResources();
        }
    }
}
