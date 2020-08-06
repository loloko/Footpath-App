package com.fernando.footpath.screen.register;

import com.fernando.footpath.R;
import com.fernando.footpath.common.ScreenRobot;

public class RobotRegister extends ScreenRobot<RobotRegister> {


    private static final int FIELD_NAME = R.id.et_name;
    private static final int FIELD_EMAIL = R.id.et_email;
    private static final int FIELD_PASSWORD = R.id.et_password;
    private static final int FIELD_CONFIRM_PASSWORD = R.id.et_confirm_password;
    private static final int BT_REGISTER = R.id.bt_register;
    private static final int TOOLBAR = R.id.toolbar;


    private static final String REGISTER_NAME = "test24";
    private static final String REGISTER_EMAIL = "test24@gmail.com";
    private static final String REGISTER_PASSWORD = "123456d";

    private static final String REGISTER_EMAIL_COLLISION = "fernando2@gmail.com";

    public RobotRegister verifyScreenRegister() {
        checkViewContainsText(R.string.register_email);
        return this;
    }

    public RobotRegister verifyBtRegister() {
        checkIsDisplayed(BT_REGISTER);
        return this;
    }

    public RobotRegister clickBtRegister() {
        clickOnView(BT_REGISTER);
        return this;
    }

    public RobotRegister checkHintFieldName() {
        checkViewHasHint(FIELD_NAME, R.string.required_name);
        return this;
    }

    public RobotRegister checkHintFieldEmail() {
        checkViewHasHint(FIELD_EMAIL, R.string.required_email);
        return this;
    }

    public RobotRegister checkHintFieldPassword() {
        checkViewHasHint(FIELD_PASSWORD, R.string.required_password);
        return this;
    }

    public RobotRegister checkHintFieldConfirmPassword() {
        checkViewHasHint(FIELD_CONFIRM_PASSWORD, R.string.required_confirm_password);
        return this;
    }

    public RobotRegister typeName() {
        enterTextIntoView(FIELD_NAME, REGISTER_NAME);
        return this;
    }

    public RobotRegister typeEmail() {
        enterTextIntoView(FIELD_EMAIL, REGISTER_EMAIL);
        return this;
    }

    public RobotRegister typePassword() {
        enterTextIntoView(FIELD_PASSWORD, REGISTER_PASSWORD);
        return this;
    }

    public RobotRegister typeConfirmPassword() {
        enterTextIntoView(FIELD_CONFIRM_PASSWORD, REGISTER_PASSWORD);
        return this;
    }

    public RobotRegister typeDifferentPasswordBoth() {
        enterTextIntoView(FIELD_PASSWORD, REGISTER_PASSWORD);
        enterTextIntoView(FIELD_CONFIRM_PASSWORD, "123");
        return this;
    }

    public RobotRegister typeInvalidEmail() {
        enterTextIntoView(FIELD_EMAIL, "anything");
        return this;
    }

    public RobotRegister typeColissionEmail() {
        enterTextIntoView(FIELD_EMAIL, REGISTER_EMAIL_COLLISION);
        return this;
    }

    public RobotRegister typeWeakPasswordBothFields() {
        enterTextIntoView(FIELD_PASSWORD, "1234");
        enterTextIntoView(FIELD_CONFIRM_PASSWORD, "1234");
        return this;
    }

    public RobotRegister checkIfMainScreenIsDisplayed() {
        checkIsDisplayed(TOOLBAR);
        return this;
    }

    public RobotRegister checkSnackBarWeakPassword() {
        checkSnackBarDisplayed(R.string.weak_password);
        return this;
    }

    public RobotRegister checkSnackBarCollisionEmail() {
        checkSnackBarDisplayed(R.string.user_collision);
        return this;
    }

    public RobotRegister checkSnackBarInvalidEmail() {
        checkSnackBarDisplayed(R.string.invalid_email);
        return this;
    }

    public RobotRegister checkSnackBarPasswordsDontMatch() {
        checkSnackBarDisplayed(R.string.password_dont_match);
        return this;
    }

}
