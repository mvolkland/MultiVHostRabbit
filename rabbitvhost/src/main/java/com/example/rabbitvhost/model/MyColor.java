package com.example.rabbitvhost.model;

import java.io.Serializable;

public class MyColor implements Serializable {

  private static final long serialVersionUID = 1L;

  private String color;

  public MyColor(final String color) {
    this.color = color;
  }

  protected String getColor() {
    return color;
  }

  protected void setColor(final String color) {
    this.color = color;
  }

  @Override
  public String toString() {
    return "MyColor [color=" + color + "]";
  }


}
