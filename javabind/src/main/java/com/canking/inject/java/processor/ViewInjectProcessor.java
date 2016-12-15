package com.canking.inject.java.processor;


import com.example.annotation.ViewBinder;
import com.google.auto.service.AutoService;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class ViewInjectProcessor extends AbstractProcessor {
    private Map<String, ProxyInfo> mProxyMap = new HashMap<String, ProxyInfo>();

    private Elements elementUtils;
    private Filer filer;

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        for (Class<? extends Annotation> annotation : getSupportedAnnotations()) {
            types.add(annotation.getCanonicalName());
        }
        return types;
    }

    private Set<Class<? extends Annotation>> getSupportedAnnotations() {
        Set<Class<? extends Annotation>> annotations = new LinkedHashSet<>();

        annotations.add(ViewBinder.class);

        return annotations;
    }

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        elementUtils = env.getElementUtils();
        filer = env.getFiler();
    }


    @Override
    public boolean process(Set<? extends TypeElement> annotations,
                           RoundEnvironment roundEnv) {
        System.out.println("********Here is process build start...**********");

        String fqClassName, className, packageName;
        // Map<String, VariableElement> fields = new HashMap<String,
        // VariableElement>();

        for (Element ele : roundEnv.getElementsAnnotatedWith(ViewBinder.class)) {

            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "ele = " + ele);

            if (ele.getKind() == ElementKind.CLASS) {
                TypeElement classElement = (TypeElement) ele;
                PackageElement packageElement = (PackageElement) ele
                        .getEnclosingElement();
                fqClassName = classElement.getQualifiedName().toString();
                className = classElement.getSimpleName().toString();
                packageName = packageElement.getQualifiedName().toString();

                int layoutId = classElement.getAnnotation(ViewBinder.class)
                        .value();

                ProxyInfo proxyInfo = mProxyMap.get(fqClassName);
                if (proxyInfo != null) {
                    proxyInfo.setLayoutId(layoutId);
                } else {
                    proxyInfo = new ProxyInfo(packageName, className, filer);
                    proxyInfo.setTypeElement(classElement);
                    proxyInfo.setLayoutId(layoutId);
                    mProxyMap.put(fqClassName, proxyInfo);
                }
                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.NOTE,
                        "********annatated class : packageElement.getQualifiedName() = " + packageName
                                + " , getSimpleName = " + className
                                + "\n , classElement.getQualifiedName() = " + fqClassName + " getEnclosingElement:"
                                + ele.getEnclosingElement());

            } else if (ele.getKind() == ElementKind.FIELD) {
                VariableElement varElement = (VariableElement) ele;
                TypeElement classElement = (TypeElement) ele.getEnclosingElement();


                fqClassName = classElement.getQualifiedName().toString();
                PackageElement packageElement = elementUtils.getPackageOf(classElement);
                packageName = packageElement.getQualifiedName().toString();
                className = getClassName(classElement, packageName);

                int id = varElement.getAnnotation(ViewBinder.class).value();
                String fieldName = varElement.getSimpleName().toString();
                String fieldType = varElement.asType().toString();

                processingEnv.getMessager().printMessage(
                        Diagnostic.Kind.NOTE,
                        "annatated field : fieldName = "
                                + varElement.getSimpleName().toString()
                                + " , id = " + id + " , fileType = "
                                + fieldType + " getEnclosingElement:"
                                + varElement.getEnclosingElement() + " fqClassName:" + fqClassName);

                ProxyInfo proxyInfo = mProxyMap.get(fqClassName);
                if (proxyInfo == null) {
                    proxyInfo = new ProxyInfo(packageName, className, filer);
                    proxyInfo.setTypeElement(classElement);
                    mProxyMap.put(fqClassName, proxyInfo);
                }
                proxyInfo.putViewInfo(id,
                        new ViewInfo(id, fieldName, fieldType));

            }

        }

        for (String key : mProxyMap.keySet()) {
            ProxyInfo proxyInfo = mProxyMap.get(key);
            try {
                proxyInfo.generateJavaCode();
            } catch (Exception e) {
                System.out.println("generateJavaCode:" + e.getMessage());
            }
        }
        System.out.println("*********Here is process build end...**********");

        return false;
    }

    private static String getClassName(TypeElement type, String packageName) {
        int packageLen = packageName.length() + 1;
        return type.getQualifiedName().toString().substring(packageLen)
                .replace('.', '$');
    }

    private void print(Element element, String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message, element);
    }
}
