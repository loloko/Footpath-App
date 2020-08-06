package com.fernando.footpath.screen.record;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;

import com.fernando.footpath.activity.RecordActivity;
import com.fernando.footpath.activity.RegisterActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestCaseRecord {

    @Rule
    public ActivityTestRule<RecordActivity> mActivityRule = new ActivityTestRule<>(RecordActivity.class, false, true);


    @Before
    public void Setup() {

        //TODO must not have any track started
    }

    @After
    public void Finish() {

    }

    @Test
    public void A_TestScreen() {

        new RobotRecord()
                .verifyScreenRecord()
                .verifyBtStart();
    }

    //set android location to follow a track during test
    @Test
    public void B_TestRecordTrack() throws InterruptedException {

        new RobotRecord()
                .verifyScreenRecord()
                .clickBtStart()
                .sleep(5)
                .verifyBtStop()
                .sleep(20)
                .clickBtStop()
                .clickBtResume()
                .sleep(5)
                .verifyBtStop()
                .sleep(20)
                .clickBtStop()
                .verifyBtFinish()
                .clickBtFinish()
                .verifyScreenSaveRecord();
    }

    @Test
    public void C_TestSaveRecordTrackTitle() {

        new RobotRecord()
                .clickBtFinish()
                .verifyScreenSaveRecord()
                .clickRadio()
                .clickFabOption()
                .clickFabSave()
                .checkHintFieldTitle();
    }

    @Test
    public void D_TestSaveRecordTrackDescription() {

        new RobotRecord()
                .clickBtFinish()
                .verifyScreenSaveRecord()
                .clickRadio()
                .typeTitle()
                .clickFabOption()
                .clickFabSave()
                .checkHintFieldDescription();
    }

    @Test
    public void E_TestSaveRecordTrack() {

        new RobotRecord()
                .clickBtFinish()
                .verifyScreenSaveRecord()
                .clickRadio()
                .typeTitle()
                .typeDescription()
                .clickFabOption()
                .clickFabSave()
                .verifySavePopup();
    }

    @Test
    public void F_TestDeleteTrack() throws InterruptedException {

        B_TestRecordTrack();

        new RobotRecord()
                .clickFabOption()
                .clickFabDelete()
                .verifyDeletePopup()
                .clickDeleteDialog();
    }

}
