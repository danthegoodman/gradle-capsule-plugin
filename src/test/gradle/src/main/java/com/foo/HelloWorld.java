package com.foo;

class HelloWorld {
  @Override
  public String toString() {
    try {
      Class aClass = HelloWorld.class.getClassLoader().loadClass("org.junit.Test");
      return "Hello World";
    } catch (ClassNotFoundException e) {
      return "Unable to find Junit Test Annotation";
    }
  }
}
