package com.canking.inject.java.processor;

import com.example.annotation.ViewBinder;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import static javax.lang.model.element.Modifier.PRIVATE;


public class ProxyInfo {
    private String packageName;
    private String targetClassName;
    private String proxyClassName;
    private TypeElement typeElement;
    private Filer filer;

    private int layoutId;

    private Map<Integer, ViewInfo> idViewMap = new HashMap<Integer, ViewInfo>();

    public static final String PROXY = "PROXY";

    public ProxyInfo(String packageName, String className, Filer filer) {
        this.packageName = packageName;
        this.targetClassName = className;
        this.filer = filer;
        this.proxyClassName = className + "_" + PROXY;

    }

    public void putViewInfo(int id, ViewInfo viewInfo) {
        idViewMap.put(id, viewInfo);
    }

    public Map<Integer, ViewInfo> getIdViewMap() {
        return idViewMap;
    }

    public String getProxyClassFullName() {
        return packageName + "." + proxyClassName;
    }

    public String generateJavaCode() {
        TypeVariableName typeVariable =  TypeVariableName.get("T", ClassName.bestGuess(targetClassName));
        ClassName parmClass = ClassName.get("com.canking.inject.bindview.processor", "AbstractInjector");

        TypeName targetType = TypeName.get(typeElement.asType());
        ClassName Obj = ClassName.get("java.lang", "Object");
        ClassName finder = ClassName.get("com.canking.inject.bindview", "Finder");
        ParameterizedTypeName parameter = ParameterizedTypeName.get(parmClass, typeVariable);
        int id = typeElement.getAnnotation(ViewBinder.class).value();


        MethodSpec inject = MethodSpec.methodBuilder("inject")
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addAnnotation(Override.class)
                .addParameter(finder, "finder")
                .addParameter(typeVariable, "target")
                .addParameter(Obj, "obj")
                .addStatement("target.setContentView($L)", id)
                .build();

        //ClassName generic = ClassName.get(packageName, targetClassName);
        if (targetType instanceof ParameterizedTypeName) {
            targetType = ((ParameterizedTypeName) targetType).rawType;
        }
        //TypeName classT = ParameterizedTypeName.get(parmClass);
        TypeSpec helloWorld = TypeSpec.classBuilder(proxyClassName)
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(typeVariable)
                .addMethod(inject)
                .addField(typeVariable, "target", PRIVATE)
                .addSuperinterface(parameter)
                .build();

        JavaFile javaFile = JavaFile.builder(packageName, helloWorld)
                .build();

        try {
            //javaFile.writeTo(System.out);
            javaFile.writeTo(filer);
        } catch (Exception e) {
            System.out.println("javaFile.writeTo(filer)"+e.getMessage());
            e.printStackTrace();
        }
//
//
//        StringBuilder builder = new StringBuilder();
//        builder.append("//Generated code auto. Do not modify!\n");
//        builder.append("package ").append(packageName).append(";\n\n");
//
//        builder.append("import android.view.View;\n");
//        builder.append("import com.canking.inject.bindview.Finder;\n");
//        builder.append("import com.canking.inject.bindview.processor.AbstractInjector;\n");
//        builder.append('\n');
//
//        builder.append("public class ").append(proxyClassName);
//        builder.append("<T extends ").append(getTargetClassName()).append(">");
//        builder.append(" implements AbstractInjector<T>");
//        builder.append(" {\n");
//
//        generateInjectMethod(builder);
//        builder.append('\n');
//
//        builder.append("}\n");
        return "";

    }

    private String getTargetClassName() {
        return targetClassName.replace("$", ".");
    }

    private void generateInjectMethod(StringBuilder builder) {
        builder.append("  @Override ")
                .append("public void inject(final Finder finder, final T target, Object source) {\n");

        if (layoutId > 0) {
            builder.append("    finder.setContentView( source , ");
            builder.append(layoutId + ");\n");
        }

        if (idViewMap.size() > 0) {
            builder.append("    View view;\n");
        } else {
            builder.append("  }\n");
            return;
        }

        for (Integer key : idViewMap.keySet()) {
            ViewInfo viewInfo = idViewMap.get(key);
            builder.append("    view = ");
            builder.append("finder.findViewById(source , ");
            builder.append(viewInfo.getId() + " );\n");
            builder.append("    target.").append(viewInfo.getName())
                    .append(" = ");
            builder.append("finder.castView( view ").append(", ")
                    .append(viewInfo.getId()).append(" , \"");
            builder.append(viewInfo.getName() + " \" );");
        }

        builder.append("  }\n");
    }

    public TypeElement getTypeElement() {
        return typeElement;
    }

    public void setTypeElement(TypeElement typeElement) {
        this.typeElement = typeElement;
    }

    public int getLayoutId() {
        return layoutId;
    }

    public void setLayoutId(int layoutId) {
        this.layoutId = layoutId;
    }

}
