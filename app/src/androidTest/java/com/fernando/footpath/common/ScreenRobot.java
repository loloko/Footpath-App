package com.fernando.footpath.common;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import androidx.annotation.IdRes;
import androidx.annotation.StringRes;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.platform.app.InstrumentationRegistry;

import com.fernando.footpath.R;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.pressImeActionButton;
import static androidx.test.espresso.action.ViewActions.swipeDown;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.action.ViewActions.swipeRight;
import static androidx.test.espresso.action.ViewActions.swipeUp;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isClickable;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withTagValue;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.fernando.footpath.common.actions.CallOnClickAction.callOnClick;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;

@SuppressWarnings("unchecked")
public abstract class ScreenRobot<T extends ScreenRobot> {

    private Activity activityContext;

    public static <T extends ScreenRobot> T withRobot(Class<T> screenRobotClass) {
        if (screenRobotClass == null) {
            throw new IllegalArgumentException("instance class == null");
        }

        try {
            return screenRobotClass.newInstance();
        } catch (IllegalAccessException iae) {
            throw new RuntimeException("IllegalAccessException", iae);
        } catch (InstantiationException ie) {
            throw new RuntimeException("InstantiationException", ie);
        }
    }

    public Context getContext() {
        return InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    public T sleep() throws InterruptedException {
        return sleep(1);
    }

    public T sleep(int seconds) throws InterruptedException {
        Thread.sleep(seconds * 1000L);
        return (T) this;
    }

    public T checkIsDisplayed(@IdRes int... viewIds) {
        for (int viewId : viewIds) {
            onView(withId(viewId)).check(matches(isDisplayed()));
        }
        return (T) this;
    }

    public T checkIsClickable(@IdRes int... viewIds) {
        for (int viewId : viewIds) {
            onView(withId(viewId)).check(matches(isClickable()));
        }
        return (T) this;
    }

    public T checkIsHidden(@IdRes int... viewIds) {
        for (int viewId : viewIds) {
            onView(withId(viewId)).check(matches(not(isDisplayed())));
        }
        return (T) this;
    }

    public T checkDoesNotExist(@IdRes int... viewIds) {
        for (int viewId : viewIds) {
            onView(withId(viewId)).check(doesNotExist());
        }
        return (T) this;
    }

    public T checkViewHasText(@IdRes int viewId, @StringRes int messageResId) {
        onView(withId(viewId)).check(matches(withText(messageResId)));
        return (T) this;
    }

    public T checkViewHasDrawableAndTag(int imageResId) {
        onView(withTagValue(is((Object) imageResId))).check(matches(isDisplayed()));
        return (T) this;
    }

    public T scrollViewDown(@IdRes int viewIds) {
        onView(withId(viewIds)).perform(swipeUp(), click());
        return (T) this;
    }

    public T checkViewHasText(@IdRes int viewId, String expected) {
        onView(withId(viewId)).check(matches(withText(expected)));
        return (T) this;
    }

    public T scrollViewUp(@IdRes int viewIds) {
        onView(withId(viewIds)).perform(swipeDown(), click());
        return (T) this;
    }

    public T checkViewContainsText(@StringRes int text) {
        onView(withText(text)).check(matches(isDisplayed()));
        return (T) this;
    }

    public T checkViewHasHint(@IdRes int viewId, @StringRes int messageResId) {
        onView(withId(viewId)).check(matches(withHint(messageResId)));
        return (T) this;
    }

    public T callOnClickOnView(@IdRes int viewId) {
        // On small Views, click action isn't always detected.
        // To avoid this, use callOnClick().
        onView(withId(viewId)).perform(callOnClick());
        return (T) this;
    }

    public T clickOnView(@IdRes int viewId) {
        onView(withId(viewId)).perform(click());
        return (T) this;
    }

    public T pressBack() {
        Espresso.pressBack();
        return (T) this;
    }

    public T goBackFromToolbar() {
        onView(withContentDescription(R.string.abc_action_bar_up_description)).perform(click());
        return (T) this;
    }

    public T closeKeyboard() {
        Espresso.closeSoftKeyboard();
        return (T) this;
    }

    public T pressImeAction(@IdRes int viewId) {
        onView(withId(viewId)).perform(pressImeActionButton());
        return (T) this;
    }

    public T enterTextIntoView(@IdRes int viewId, String text) {
        onView(withId(viewId)).perform(typeText(text));
        Espresso.closeSoftKeyboard();
        return (T) this;
    }

    public T checkSnackBarDisplayed(@StringRes int message) {
        onView(withText(message)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        return (T) this;
    }

    public T checkDialogWithTextIsDisplayed(@StringRes int messageResId) {
        onView(withText(messageResId))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
        return (T) this;
    }

    public T clickButtonDialog(@StringRes int stringID) {
        onView(withText(stringID))
                .inRoot(isDialog())
                .check(matches(isDisplayed()))
                .perform(click());
        return (T) this;
    }

    public T swipeLeftOnView(@IdRes int viewId) {
        onView(withId(viewId)).perform(swipeLeft());
        return (T) this;
    }

    public T swipeRightOnView(@IdRes int viewId) {
        onView(withId(viewId)).perform(swipeRight());
        return (T) this;
    }

    public T clickOnCardForList(@IdRes int viewId, int position) {
        onView(withIndex(withId(viewId), position)).perform(click());
        return (T) this;
    }

    public static Matcher<View> withIndex(final Matcher<View> matcher, final int index) {
        return new TypeSafeMatcher<View>() {
            int currentIndex = 0;

            @Override
            public void describeTo(Description description) {
                description.appendText("with index: ");
                description.appendValue(index);
                matcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                return matcher.matches(view) && currentIndex++ == index;
            }
        };
    }
}




