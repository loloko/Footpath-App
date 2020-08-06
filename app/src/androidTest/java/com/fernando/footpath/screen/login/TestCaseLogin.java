package com.fernando.footpath.screen.login;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;

import com.fernando.footpath.activity.LoginActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class TestCaseLogin {


    @Rule
    public ActivityTestRule<LoginActivity> mActivityRule = new ActivityTestRule<>(LoginActivity.class, false, true);


    @Before
    public void Setup() {

    }

    @After
    public void Finish() {


    }

    @Test
    public void Test1() {

        new RobotLogin()
                .verifyScreenSignin()
                .verifyBtSignin();
    }

    @Test
    public void Test2() {

        new RobotLogin()
                .clickBtSignIn()
                .checkHintFieldEmail();
    }

    @Test
    public void Test3() {

        new RobotLogin()
                .typeEmail()
                .clickBtSignIn()
                .checkHintFieldPassword();
    }

    @Test
    public void Test4() {

        new RobotLogin()
                .typeWrongEmail()
                .typePassword()
                .clickBtSignIn()
                .checkSnackBarCredentials();

    }

    @Test
    public void Teste5() throws InterruptedException {

        new RobotLogin()
                .typeEmail()
                .typePassword()
                .clickBtSignIn()
                .sleep(10)
                .checkIfTrackScreenIsDisplayed();
    }

    @Test
    public void Test6() {

        new RobotLogin()
                .clickBtForgotPassword()
                .verifyScreenResetPassword()
                .pressBack()
                .verifyScreenSignin();
    }

    @Test
    public void Test7() {

        new RobotLogin()
                .clickBtRegister()
                .verifyScreenRegister()
                .pressBack()
                .verifyScreenSignin();
    }

}
