package com.fernando.footpath.screen.record;

import com.fernando.footpath.R;
import com.fernando.footpath.common.ScreenRobot;

public class RobotRecord extends ScreenRobot<RobotRecord> {




    private static final int BT_START = R.id.bt_start;
    private static final int BT_STOP = R.id.bt_stop;
    private static final int BT_RESUME = R.id.bt_resume;
    private static final int BT_FINISH = R.id.bt_finish;

    private static final int FAB_OPTIONS = R.id.fab_plus;
    private static final int FAB_DELETE = R.id.fab_delete;
    private static final int FAB_SAVE = R.id.fab_save;

    private static final int RADIO_BUTTON = R.id.rb_moderate;
    private static final int FIELD_TITLE = R.id.et_title;
    private static final int FIELD_DESCRIPTION = R.id.et_description;


    public RobotRecord verifyScreenRecord() {
        checkViewContainsText(R.string.start);
        return this;
    }

    public RobotRecord verifyBtStart() {
        checkIsDisplayed(BT_START);
        return this;
    }

    public RobotRecord verifyBtStop() {
        checkIsDisplayed(BT_STOP);
        return this;
    }

    public RobotRecord verifyBtResume() {
        checkIsDisplayed(BT_RESUME);
        return this;
    }

    public RobotRecord verifyBtFinish() {
        checkIsDisplayed(BT_FINISH);
        return this;
    }

    public RobotRecord clickBtStart() {
        clickOnView(BT_START);
        return this;
    }

    public RobotRecord clickBtStop() {
        clickOnView(BT_STOP);
        return this;
    }

    public RobotRecord clickBtResume() {
        clickOnView(BT_RESUME);
        return this;
    }

    public RobotRecord clickBtFinish() {
        clickOnView(BT_FINISH);
        return this;
    }

    public RobotRecord clickFabOption() {
        clickOnView(FAB_OPTIONS);
        return this;
    }

    public RobotRecord clickFabDelete() {
        clickOnView(FAB_DELETE);
        return this;
    }

    public RobotRecord clickFabSave() {
        clickOnView(FAB_SAVE);
        return this;
    }
    public RobotRecord clickRadio() {
        clickOnView(RADIO_BUTTON);
        return this;
    }

    public RobotRecord verifyDeletePopup() {
        checkDialogWithTextIsDisplayed(R.string.attention);
        checkDialogWithTextIsDisplayed(R.string.delete_track);
        return this;
    }

    public RobotRecord verifySavePopup() {
        checkDialogWithTextIsDisplayed(R.string.hello);
        checkDialogWithTextIsDisplayed(R.string.save_track);
        return this;
    }

    public RobotRecord clickDeleteDialog() {
        clickButtonDialog(R.string.delete);
        return this;
    }

    public RobotRecord verifyScreenSaveRecord() {
        checkViewContainsText(R.string.how_difficult);
        return this;
    }

    public RobotRecord typeTitle() {
        enterTextIntoView(FIELD_TITLE, "DELETE TEST");
        return this;
    }

    public RobotRecord typeDescription() {
        enterTextIntoView(FIELD_DESCRIPTION, "DELETE TEST");
        return this;
    }

    public RobotRecord checkHintFieldTitle() {
        checkViewHasHint(FIELD_TITLE, R.string.required_title);
        return this;
    }

    public RobotRecord checkHintFieldDescription() {
        checkViewHasHint(FIELD_DESCRIPTION, R.string.required_description);
        return this;
    }
//
//    public RobotRecord checkHintFieldPassword() {
//        checkViewHasHint(FIELD_PASSWORD, R.string.required_password);
//        return this;
//    }
//
//    public RobotRecord checkHintFieldConfirmPassword() {
//        checkViewHasHint(FIELD_CONFIRM_PASSWORD, R.string.required_confirm_password);
//        return this;
//    }
//
//    public RobotRecord typeName() {
//        enterTextIntoView(FIELD_NAME, REGISTER_NAME);
//        return this;
//    }
//
//    public RobotRecord typeEmail() {
//        enterTextIntoView(FIELD_EMAIL, REGISTER_EMAIL);
//        return this;
//    }
//
//    public RobotRecord typePassword() {
//        enterTextIntoView(FIELD_PASSWORD, REGISTER_PASSWORD);
//        return this;
//    }
//
//    public RobotRecord typeConfirmPassword() {
//        enterTextIntoView(FIELD_CONFIRM_PASSWORD, REGISTER_PASSWORD);
//        return this;
//    }
//
//    public RobotRecord typeDifferentPasswordBoth() {
//        enterTextIntoView(FIELD_PASSWORD, REGISTER_PASSWORD);
//        enterTextIntoView(FIELD_CONFIRM_PASSWORD, "123");
//        return this;
//    }
//
//    public RobotRecord typeInvalidEmail() {
//        enterTextIntoView(FIELD_EMAIL, "anything");
//        return this;
//    }
//
//    public RobotRecord typeColissionEmail() {
//        enterTextIntoView(FIELD_EMAIL, REGISTER_EMAIL_COLLISION);
//        return this;
//    }
//
//    public RobotRecord typeWeakPasswordBothFields() {
//        enterTextIntoView(FIELD_PASSWORD, "1234");
//        enterTextIntoView(FIELD_CONFIRM_PASSWORD, "1234");
//        return this;
//    }
//
//    public RobotRecord checkIfMainScreenIsDisplayed() {
//        checkIsDisplayed(TOOLBAR);
//        return this;
//    }
//
//    public RobotRecord checkSnackBarWeakPassword() {
//        checkSnackBarDisplayed(R.string.weak_password);
//        return this;
//    }
//
//    public RobotRecord checkSnackBarCollisionEmail() {
//        checkSnackBarDisplayed(R.string.user_collision);
//        return this;
//    }
//
//    public RobotRecord checkSnackBarInvalidEmail() {
//        checkSnackBarDisplayed(R.string.invalid_email);
//        return this;
//    }
//
//    public RobotRecord checkSnackBarPasswordsDontMatch() {
//        checkSnackBarDisplayed(R.string.password_dont_match);
//        return this;
//    }

}
