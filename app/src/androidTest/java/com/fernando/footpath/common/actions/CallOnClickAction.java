package com.fernando.footpath.common.actions;


import android.view.View;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;

import org.hamcrest.Matcher;

import static androidx.test.espresso.matcher.ViewMatchers.isClickable;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static org.hamcrest.Matchers.allOf;

public class CallOnClickAction implements ViewAction {

    private CallOnClickAction(){}

    public static CallOnClickAction callOnClick(){
        return new CallOnClickAction();
    }

    @Override
    public Matcher<View> getConstraints() {
        return allOf(isClickable(), isDisplayed());
    }

    @Override
    public String getDescription() {
        return "CallOnClick";
    }

    @Override
    public void perform(UiController uiController, View view) {
        view.callOnClick();

    }
}
