package com.fernando.footpath.screen.register;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;

import com.fernando.footpath.activity.RegisterActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class TestCaseRegister {

    @Rule
    public ActivityTestRule<RegisterActivity> mActivityRule = new ActivityTestRule<>(RegisterActivity.class, false, true);


    @Before
    public void Setup() {

    }

    @After
    public void Finish() {


    }

    @Test
    public void TestScreen() {

        new RobotRegister()
                .verifyScreenRegister()
                .verifyBtRegister();
    }

    @Test
    public void TestEmptyName() {

        new RobotRegister()
                .clickBtRegister()
                .checkHintFieldName();
    }

    @Test
    public void TestEmptyEmail() {

        new RobotRegister()
                .typeName()
                .clickBtRegister()
                .checkHintFieldEmail();
    }

    @Test
    public void TestEmptyPassword() {

        new RobotRegister()
                .typeName()
                .typeEmail()
                .clickBtRegister()
                .checkHintFieldPassword();

    }

    @Test
    public void TestEmptyConfirmPassword() {

        new RobotRegister()
                .typeName()
                .typeEmail()
                .typePassword()
                .clickBtRegister()
                .checkHintFieldConfirmPassword();
    }

    @Test
    public void TestPasswordDontMatch() {

        new RobotRegister()
                .typeName()
                .typeEmail()
                .typeDifferentPasswordBoth()
                .clickBtRegister()
                .checkSnackBarPasswordsDontMatch();
    }

    @Test
    public void TestWeakPassword() throws InterruptedException {

        new RobotRegister()
                .typeName()
                .typeEmail()
                .typeWeakPasswordBothFields()
                .clickBtRegister()
                .sleep(2)
                .checkSnackBarWeakPassword();
    }

    @Test
    public void TestInvalidEmail() throws InterruptedException {

        new RobotRegister()
                .typeName()
                .typeInvalidEmail()
                .typePassword()
                .typeConfirmPassword()
                .clickBtRegister()
                .sleep(2)
                .checkSnackBarInvalidEmail();
    }

    @Test
    public void TestCollisionEmail() throws InterruptedException {

        new RobotRegister()
                .typeName()
                .typeColissionEmail()
                .typePassword()
                .typeConfirmPassword()
                .clickBtRegister()
                .sleep(2)
                .checkSnackBarCollisionEmail();
    }

}
