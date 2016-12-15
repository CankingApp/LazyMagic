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
        TypeVariableName typeVariable = TypeVariableName.get("T", ClassName.bestGuess(targetClassName));
        ClassName parmClass = ClassName.get("com.canking.inject.bindview.processor", "AbstractInjector");

        TypeName targetType = TypeName.get(typeElement.asType());
        ParameterizedTypeName parameter = ParameterizedTypeName.get(parmClass, typeVariable);


        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("bind")
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class)
                .addAnnotation(Override.class)
                .addParameter(typeVariable, "target");
        ViewBinder a = typeElement.getAnnotation(ViewBinder.class);
        if (a != null && a.value() != -1) {
            methodBuilder.addStatement("target.setContentView($L)", a.value());
        }
        for (int key : idViewMap.keySet()) {
            ViewInfo viewInfo = idViewMap.get(key);
            methodBuilder.addStatement("target.$N = ($N) target.findViewById($L);", viewInfo.getName(), viewInfo.getType(), viewInfo.getId());
        }

        MethodSpec methodSpec = methodBuilder.build();

        //ClassName generic = ClassName.get(packageName, targetClassName);
        if (targetType instanceof ParameterizedTypeName) {
            targetType = ((ParameterizedTypeName) targetType).rawType;
        }
        //TypeName classT = ParameterizedTypeName.get(parmClass);
        TypeSpec classType = TypeSpec.classBuilder(proxyClassName)
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(typeVariable)
                .addMethod(methodSpec)
                .addField(typeVariable, "target", PRIVATE)
                .addSuperinterface(parameter)
                .build();


        JavaFile javaFile = JavaFile.builder(packageName, classType)
                .build();
        try {
            //javaFile.writeTo(System.out);
            javaFile.writeTo(filer);
        } catch (Exception e) {
            System.out.println("javaFile.writeTo(filer)" + e.getMessage());
            //e.printStackTrace();
        }
        return "";

    }

    private String getTargetClassName() {
        return targetClassName.replace("$", ".");
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
