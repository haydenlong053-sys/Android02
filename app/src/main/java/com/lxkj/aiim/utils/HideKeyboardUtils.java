package com.lxkj.aiim.utils;

import android.widget.EditText;

import java.lang.reflect.Method;

public class HideKeyboardUtils {

    public void hideKeyboard(EditText numberLabel){
        Class cls= EditText.class;

        Method method;

        try{
            method=cls.getMethod("setShowSoftInputOnFocus",boolean.class);
            method.setAccessible(true);
            method.invoke(numberLabel,false);
        }catch(Exception e){

        }

    }
}
