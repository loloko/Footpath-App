package com.fernando.footpath.screen.login;

import androidx.test.espresso.Espresso;

import com.fernando.footpath.R;
import com.fernando.footpath.common.ScreenRobot;

public class RobotLogin extends ScreenRobot<RobotLogin> {


    private static final int FIELD_EMAIL = R.id.et_email;
    private static final int FIELD_PASSWORD = R.id.et_password;
    private static final int BT_LOGIN = R.id.bt_signin;
    private static final int BT_FORGOT_PASSWORD = R.id.tv_forgot_password;
    private static final int BT_REGISTER = R.id.tv_signup;
    private static final int TOOLBAR = R.id.toolbar;


    private static final String LOGIN_EMAIL = "fernando2@gmail.com";
    private static final String LOGIN_PASSWORD = "123456";


    public RobotLogin verifyScreenSignin() {
        checkViewContainsText(R.string.forgot_pasword);
        return this;
    }


    public RobotLogin verifyBtSignin() {
        checkIsDisplayed(BT_LOGIN);
        return this;
    }

    public RobotLogin clickBtSignIn() {
        clickOnView(BT_LOGIN);
        return this;
    }

    public RobotLogin clickBtForgotPassword() {
        clickOnView(BT_FORGOT_PASSWORD);
        return this;
    }

    public RobotLogin clickBtRegister() {
        clickOnView(BT_REGISTER);
        return this;
    }

    public RobotLogin checkHintFieldEmail() {
        checkViewHasHint(FIELD_EMAIL, R.string.required_email);
        return this;
    }

    public RobotLogin checkHintFieldPassword() {
        checkViewHasHint(FIELD_PASSWORD, R.string.required_password);
        return this;
    }

    public RobotLogin checkSnackBarCredentials() {
        checkSnackBarDisplayed(R.string.invalid_credentials);
        return this;
    }

    public RobotLogin typeEmail() {
        enterTextIntoView(FIELD_EMAIL, LOGIN_EMAIL);
        return this;
    }

    public RobotLogin typeWrongEmail() {
        enterTextIntoView(FIELD_EMAIL, "anything");
        return this;
    }

    public RobotLogin typePassword() {
        enterTextIntoView(FIELD_PASSWORD, LOGIN_PASSWORD);
        return this;
    }

    public RobotLogin verifyScreenRegister() {
        checkViewContainsText(R.string.register_email);
        return this;
    }

    public RobotLogin verifyScreenResetPassword() {
        checkViewContainsText(R.string.reset_password);
        return this;
    }

    //the view is full screen
    public RobotLogin pressBack() {
        Espresso.pressBack();
        return this;
    }

    public RobotLogin checkIfTrackScreenIsDisplayed() {
        checkIsDisplayed(TOOLBAR);
        return this;
    }


}
