package org.smartregister.chw.activity.wcaro;

import android.Manifest;

import android.app.Activity;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.StringRes;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.core.internal.deps.guava.collect.Iterables;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;

import com.vijay.jsonwizard.activities.JsonFormActivity;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.smartregister.chw.R;
import org.smartregister.chw.activity.LoginActivity;
import org.smartregister.chw.activity.utils.Constants;
import org.smartregister.chw.activity.utils.Order;
import org.smartregister.chw.activity.utils.Utils;
import org.smartregister.family.activity.FamilyWizardFormActivity;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.startsWith;
import static org.smartregister.chw.activity.utils.Utils.getViewId;

public class AddFamilyMemberTest {

    @Rule
    public ActivityTestRule<LoginActivity> mActivityTestRule = new ActivityTestRule<>(LoginActivity.class);

    public ActivityTestRule<FamilyWizardFormActivity> mActivityTestRule2 = new ActivityTestRule<>(FamilyWizardFormActivity.class);

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(android.Manifest.permission.CALL_PHONE);

    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(Manifest.permission.CAMERA);

    @Rule
    public GrantPermissionRule mRuntimePermissionRule1 = GrantPermissionRule.grant(Manifest.permission.ACCESS_FINE_LOCATION);

    Utils utils = new Utils();

    public void setUp() throws InterruptedException {
        Thread.sleep(10000);
        utils.logIn(Constants.WcaroConfigs.wCaro_username, Constants.WcaroConfigs.wCaro_password);
    }


    @Test
    public void addFamilyMemberAboveFive() throws Throwable {
        onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring(Configs.TestConfigs.familyName + " Family"))
                .perform(click());
        onView(withId(R.id.fab))
                .perform(click());
        Thread.sleep(500);
        onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring("Add new family member"))
                .perform(click());
        Thread.sleep(100);
        onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring("Other family member"))
                .perform(click());

        Activity activity = getCurrentActivity();
        Thread.sleep(500);
        onView(withId(getViewId((JsonFormActivity) activity, "step1:national_id")))
                .perform(typeText(Configs.TestConfigs.nationalID));
        //onView(withId(getViewId((JsonFormActivity) activity, "step1:surname"))).perform(scrollTo(), typeText("JinaLaFamilia"));
        onView(withId(getViewId((JsonFormActivity) activity, "step1:same_as_fam_name")))
                .perform(scrollTo(), click());
        onView(withId(getViewId((JsonFormActivity) activity, "step1:first_name")))
                .perform(scrollTo(), typeText(Configs.TestConfigs.aboveFiveFirstNameTwo));
        onView(withId(getViewId((JsonFormActivity) activity, "step1:middle_name")))
                .perform(scrollTo(), typeText(Configs.TestConfigs.aboveFiveSecondNameTwo));
        //onView(withId(getViewId((JsonFormActivity) activity, "step1:dob")))
        // .perform(scrollTo(), click());
        //Thread.sleep(1000);
        //onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring("done")).perform(click());
        //Thread.sleep(500);
        onView(withId(getViewId((JsonFormActivity) activity, "step1:dob_unknown")))
                .perform(scrollTo(), click());
        onView(withId(getViewId((JsonFormActivity) activity, "step1:age")))
                .perform(scrollTo(), typeText(Configs.TestConfigs.aboveFiveage));
        onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring("Sex"))
                .perform(scrollTo(), click());
        Thread.sleep(500);
        onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring("Female"))
                .perform(click());
        Thread.sleep(500);
        onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring("Save"))
                .perform(click());
    }

    @Test
    public void addFamilyWithBlankFields() throws Throwable{
        onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring(Configs.TestConfigs.familyName + " Family"))
                .perform(click());
        onView(withId(R.id.fab))
                .perform(click());
        Thread.sleep(500);
        onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring("Add new family member"))
                .perform(click());
        Thread.sleep(100);
        onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring("Other family member"))
                .perform(click());
        Activity activity = getCurrentActivity();
        Thread.sleep(500);

        //onView(withId(getViewId((JsonFormActivity) activity, "step1:dob")))
        // .perform(scrollTo(), click());
        //Thread.sleep(1000);
        //onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring("done")).perform(click());
        //Thread.sleep(500);
        onView(withId(getViewId((JsonFormActivity) activity, "step1:dob_unknown")))
                .perform(scrollTo(), click());
        onView(withId(getViewId((JsonFormActivity) activity, "step1:age")))
                .perform(scrollTo(), typeText(Configs.TestConfigs.aboveFiveage));
        onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring("Save"))
                .perform(click());
        onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring("Found 3 error(s) in the form. Please correct them to submit."))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Thread.sleep(500);
    }

    @Test
    public void confirmUniqueIDPrepopulated() throws Throwable {
        onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring(Configs.TestConfigs.familyName + " Family"))
                .perform(click());
        onView(withId(R.id.fab))
                .perform(click());
        Thread.sleep(500);
        onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring("Add new family member"))
                .perform(click());
        Thread.sleep(100);
        onView(androidx.test.espresso.matcher.ViewMatchers.withSubstring("Other family member"))
                .perform(click());
        Activity activity = getCurrentActivity();
        Thread.sleep(500);
        onView(withId(getViewId((JsonFormActivity) activity, "step1:unique_id")))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Thread.sleep(500);

    }

    @After
    public void completeTests(){
        mActivityTestRule.finishActivity();
    }

    private String getString(@StringRes int resourceId) {
        return mActivityTestRule.getActivity().getString(resourceId);
    }

    private static Matcher<View> withError(final String expected) {
        return new TypeSafeMatcher<View>() {
            @Override
            protected boolean matchesSafely(View item) {
                if (item instanceof EditText) {
                    return ((EditText)item).getError().toString().equals(expected);
                }
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Not found error message" + expected + ", find it!");
            }
        };
    }

    Activity getCurrentActivity() throws Throwable {
        getInstrumentation().waitForIdleSync();
        final Activity[] activity = new Activity[1];
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                java.util.Collection<Activity> activities = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED);
                activity[0] = Iterables.getOnlyElement(activities);
            }
        });
        return activity[0];
    }


}