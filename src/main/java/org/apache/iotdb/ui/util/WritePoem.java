package org.apache.iotdb.ui.util;

import java.io.File;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

public class WritePoem {
	public static void main(String args[]) {
        try {

            /**
             * $T 传入一个类型,可以使class或者ClassName对象,这个会自动帮你导入包
             * $S 替换一个字符串,会用"引号"
             * $L 就是占位一个值,一个变量
             * $N 就是占位一个方法 可以传入一个MethodSpec对象
             */
            ClassName persionClassName = ClassName.get("org.apache.iotdb.ui.util", "Persion");//这种找到的形式都和class形式一样可以用

            MethodSpec main = MethodSpec.methodBuilder("main")
                    .addModifiers(Modifier.STATIC, Modifier.PUBLIC)
                    .addParameter(String[].class, "args") //添加参数
//                    .addParameter(persionClassName, "myPersion")//添加参数
                    .returns(Void.TYPE)
                    .addStatement("int value=$L", 10)
                    .addStatement("$T obj=new $T()", persionClassName, Persion.class)
                    .addStatement("$T.out.println($S)", System.class, "来吧创建一个类来玩玩吧!")
                    .beginControlFlow("while(value<$L)", 10)//循环开始
                    .addStatement("int value2=$L", 10)
                    .endControlFlow()
//                    .addStatement("return new $T()", Class.forName("cn.poet.bean.Persion"))
                    .build();
            //创建变量
            FieldSpec fieldSpec = FieldSpec.builder(String.class, "VALUE")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer("$S","AAA").build();

            TypeSpec classSpec = TypeSpec.classBuilder("Test2")
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addField(String.class, "name", Modifier.PUBLIC)
                    .addField(fieldSpec)
                    .addMethod(main)
//                    .addType(classSpec2)//追加一个内部类
                    .build();

            JavaFile build = JavaFile.builder("cn.poet", classSpec).build();

            build.writeTo(new File("src/main/java"));
        } catch (Exception e) {
            System.out.println("发生错误:" + e.getLocalizedMessage());
        }
    }
}
