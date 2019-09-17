package com.japagram;

import com.japagram.ui.SplashScreenActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.ViewInteraction;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class UITest {

    @Rule
    public ActivityTestRule<SplashScreenActivity> mActivityTestRule = new ActivityTestRule<>(SplashScreenActivity.class);

    @Test
    public void JapaneseToolboxUserInterfaceTest() {
        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String dictButtontitle = "DICT";
        try {
            TestInputStringAndButton(dictButtontitle, "");
        } catch (NoMatchingViewException e) {
            dictButtontitle = "DICC";
        }

        TestInputStringAndButton(dictButtontitle, "");
        TestInputStringAndButton(dictButtontitle, "fdvsd");
        TestInputStringAndButton(dictButtontitle, "when");
        TestInputStringAndButton(dictButtontitle, "taberu");
        TestInputStringAndButton(dictButtontitle, "kyoukasho");
        TestInputStringAndButton(dictButtontitle, "suggee");
        TestInputStringAndButton(dictButtontitle, "言");

        TestInputStringAndButton("CONJ", "");
        TestInputStringAndButton("CONJ", "sdvsdvsd");
        TestInputStringAndButton("CONJ", "taberu");
        TestInputStringAndButton("CONJ", "言");
        TestInputStringAndButton("CONJ", "たべる");
        TestInputStringAndButton("CONJ", "食べる");
        TestInputStringAndButton("CONJ", "tabetai");
        TestInputStringAndButton("CONJ", "たべたい");
        TestInputStringAndButton("CONJ", "食べたい");
        TestInputStringAndButton("CONJ", "tabe nakereba naranai");
        TestInputStringAndButton("CONJ", "たべなければならない");
        TestInputStringAndButton("CONJ", "食べなければならない");
        //TestInputStringAndButton("CONJ", "tabete ikenai");
        //TestInputStringAndButton("CONJ", "てべていけない");
        //TestInputStringAndButton("CONJ", "食べていけない");
        //TestInputStringAndButton("CONJ", "ohanashi ni naru");
        //TestInputStringAndButton("CONJ", "おはなしになる");
        //TestInputStringAndButton("CONJ", "お話しになる");
        TestInputStringAndButton("CONJ", "yoyaku suru");
        TestInputStringAndButton("CONJ", "よやくする");
        TestInputStringAndButton("CONJ", "予約する");

        TestSpinnerLink(dictButtontitle, "taberu");
        TestTransliterator("tabertai");

        TestRadicalComposition();

        TestKanjiDecomposition("言", "亠");


    }

    private void TestInputStringAndButton(String button_string, String input) {

        ViewInteraction autoCompleteTextView = onView(
                allOf(withId(R.id.query), isDisplayed()));
        autoCompleteTextView.perform(click()); //Comment this out to speed the program

        ViewInteraction autoCompleteTextView2 = onView(
                allOf(withId(R.id.query), isDisplayed()));
        autoCompleteTextView2.perform(replaceText(input), closeSoftKeyboard());

        switch (button_string) {
            case "DICT": {
                ViewInteraction button = onView(
                        allOf(withId(R.id.button_dict), withText(button_string), isDisplayed()));
                button.perform(click());
                //button.perform(closeSoftKeyboard()).perform(scrollTo()).perform(click());
                break;
            }
            case "DICC": {
                ViewInteraction button = onView(
                        allOf(withId(R.id.button_dict), withText(button_string), isDisplayed()));
                button.perform(click());
                //button.perform(closeSoftKeyboard()).perform(scrollTo()).perform(click());
                break;
            }
            default: {
                ViewInteraction button = onView(
                        allOf(withId(R.id.button_conj), withText("CONJ"), isDisplayed()));
                button.perform(click());
                //button.perform(closeSoftKeyboard()).perform(scrollTo()).perform(click());
                break;
            }
        }
    }
    private void TestSpinnerLink(String button_string, String input) {

        ViewInteraction autoCompleteTextView = onView(
                allOf(withId(R.id.query), isDisplayed()));
        autoCompleteTextView.perform(click());

        ViewInteraction autoCompleteTextView2 = onView(
                allOf(withId(R.id.query), isDisplayed()));
        autoCompleteTextView2.perform(click());

        ViewInteraction autoCompleteTextView3 = onView(
                allOf(withId(R.id.query), isDisplayed()));
        autoCompleteTextView3.perform(replaceText(input), closeSoftKeyboard());

        ViewInteraction button = onView(
                allOf(withId(R.id.button_dict), withText(button_string), isDisplayed()));
        button.perform(click());

        ViewInteraction button2 = onView(
                allOf(withId(R.id.button_conj), withText("CONJ"), isDisplayed()));
        button2.perform(click());

//        ViewInteraction spinner = onView(
//                allOf(withId(R.id.VerbChooserSpinner),
//                        withParent(allOf(withId(R.id.VerbChooserSpinnerBox),
//                                withParent(withId(R.id.fragment_conjugator)))),
//                        isDisplayed()));
//        spinner.perform(click());
    }
    private void TestTransliterator(String input) {

//        ViewInteraction autoCompleteTextView = onView(
//                allOf(withId(R.id.inputQueryAutoCompleteTextView), isDisplayed()));
//        autoCompleteTextView.perform(click());

        ViewInteraction autoCompleteTextView2 = onView(
                allOf(withId(R.id.query), isDisplayed()));
        autoCompleteTextView2.perform(replaceText(input), closeSoftKeyboard());

        ViewInteraction button = onView(
                allOf(withId(R.id.button_convert), isDisplayed()));
        button.perform(click());

    }
    private void TestRadicalComposition() {

        ViewInteraction button2 = onView(
                allOf(withId(R.id.button_search_by_radical), isDisplayed()));
        //allOf(withId(R.id.button_searchByRadical), withText("女+子 &#8658; 好"), isDisplayed()));
        button2.perform(click());

//
//        ViewInteraction imageView = onView(
//                allOf(withId(SearchByRadicalsModuleFragment.selected_structures_forTest[0].getId()), isDisplayed()));
//        imageView.perform(click());
//
//        ViewInteraction imageView2 = onView(
//                withId(SearchByRadicalsModuleFragment.selected_substructures_forTest[0].getId()));
//        imageView2.perform(scrollTo(), click());
//
//        ViewInteraction button3 = onView(
//                withText("Search"));
//        button3.perform(scrollTo(), click());
//
//        ViewInteraction imageView3 = onView(
//                allOf(withId(SearchByRadicalsModuleFragment.selected_structures_forTest[3].getId()), isDisplayed()));
//        imageView3.perform(click());
//
//        ViewInteraction imageView4 = onView(
//                withId(SearchByRadicalsModuleFragment.selected_substructures_forTest[5].getId()));
//        imageView4.perform(scrollTo(), click());
//
//        ViewInteraction button4 = onView(
//                withText("Search"));
//        button4.perform(scrollTo(), click());
//
//        ViewInteraction imageView5 = onView(
//                allOf(withId(SearchByRadicalsModuleFragment.selected_structures_forTest[4].getId()), isDisplayed()));
//        imageView5.perform(click());
//
//        ViewInteraction imageView6 = onView(
//                withId(SearchByRadicalsModuleFragment.selected_structures_forTest[1].getId()));
//        imageView6.perform(scrollTo(), click());
//
//        ViewInteraction button5 = onView(
//                withText("Search"));
//        button5.perform(scrollTo(), click());
//
//        ViewInteraction imageView7 = onView(
//                allOf(withId(SearchByRadicalsModuleFragment.selected_structures_forTest[0].getId()), isDisplayed()));
//        imageView7.perform(click());
//
//        ViewInteraction button6 = onView(
//                withText("radical"));
//        button6.perform(scrollTo(), click());
//
//        ViewInteraction textView = onView(
//                allOf(withId(R.id.radical_search_result), withText("口"), isDisplayed()));
//        textView.perform(click());
//
//        ViewInteraction button7 = onView(
//                allOf(withText("enter"), isDisplayed()));
//        button7.perform(click());
//
//        ViewInteraction button8 = onView(
//                withText("radical"));
//        button8.perform(scrollTo(), click());
//
//        ViewInteraction textView2 = onView(
//                allOf(withId(R.id.radical_search_result), withText("厂"), isDisplayed()));
//        textView2.perform(click());
//
//        ViewInteraction button9 = onView(
//                allOf(withText("enter"), isDisplayed()));
//        button9.perform(click());
//
//        ViewInteraction button10 = onView(
//                withText("Search"));
//        button10.perform(scrollTo(), click());
//
//        ViewInteraction imageView8 = onView(
//                allOf(withId(SearchByRadicalsModuleFragment.selected_structures_forTest[1].getId()), isDisplayed()));
//        imageView8.perform(click());
//
//        ViewInteraction button11 = onView(
//                withText("Search"));
//        button11.perform(scrollTo(), click());
    }
    private void TestKanjiDecomposition(String kanji_level0, String kanji_level1) {

        ViewInteraction autoCompleteTextView2 = onView(
                allOf(withId(R.id.query), isDisplayed()));
        autoCompleteTextView2.perform(replaceText(kanji_level0), closeSoftKeyboard());

        ViewInteraction button2 = onView(
                allOf(withId(R.id.button_decompose), isDisplayed()));
        button2.perform(click());

        ViewInteraction textView = onView(
                allOf(withText(kanji_level1), isDisplayed()));
        textView.perform(click());
    }

}
