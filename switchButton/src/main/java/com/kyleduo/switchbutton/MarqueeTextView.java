package com.kyleduo.switchbutton;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class MarqueeTextView extends TextView {

 public MarqueeTextView(Context context) {
  this(context,null);
 }

 public MarqueeTextView(Context context,  AttributeSet attrs) {
  this(context, attrs ,0);
 }

 public MarqueeTextView(Context context,  AttributeSet attrs, int defStyleAttr) {
  super(context, attrs, defStyleAttr);
 }

 //重写这个方法，强制返回true
 @Override
 public boolean isFocused() {
  return true;
 }
}