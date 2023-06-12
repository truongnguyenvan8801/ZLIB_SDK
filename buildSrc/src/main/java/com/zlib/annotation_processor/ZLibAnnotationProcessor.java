package com.zlib.annotation_processor;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class ZLibAnnotationProcessor extends AbstractProcessor {

    private String annotationName;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        annotationName = processingEnv.getOptions().get("annotationName");
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        try {
            Class<?> annotationClass = Class.forName(annotationName);
            StringBuilder stringBuilder = new StringBuilder();
            for(Element element : roundEnvironment.getElementsAnnotatedWith((annotationClass.asSubclass(Annotation.class)))) {
                String className = ((TypeElement) element).getQualifiedName().toString();
                System.out.println(className);
                stringBuilder.append(className).append("\n");
            }


//            try(BufferedWriter writer = new BufferedWriter(new FileWriter()))
            try {
                FileObject file = processingEnv.getFiler().createResource(StandardLocation.SOURCE_OUTPUT, "resources", "scan_result.txt");
                Writer writer = file.openWriter();
                writer.write(stringBuilder.toString());
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        // Đăng ký sử dụng annotation AppModule
        return new HashSet<String>(){{
            add("com.zlib.annotation.AppModule");
        }};
    }
}
