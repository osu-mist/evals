package edu.osu.cws.evals.tests;

import edu.osu.cws.util.CWSUtil;
import org.testng.annotations.Test;

@Test
public class CWSUtilTest {

    public void shouldValidateNameSearch() {
        assert !CWSUtil.validateNameSearch("");
        assert !CWSUtil.validateNameSearch(null);

        assert !CWSUtil.validateNameSearch("12345");
        assert !CWSUtil.validateNameSearch("Jo2e");
        assert !CWSUtil.validateNameSearch("Jose2");

        assert CWSUtil.validateNameSearch("Clark");
        assert CWSUtil.validateNameSearch("James Bond");
        assert CWSUtil.validateNameSearch("James-Bond");
        assert CWSUtil.validateNameSearch("Bond, James");
        assert !CWSUtil.validateNameSearch("O'Brien");
    }

    public void shouldValidateOrgCode() {
        assert !CWSUtil.validateOrgCode("");
        assert !CWSUtil.validateOrgCode("abcde");
        assert !CWSUtil.validateOrgCode("123a");
        assert !CWSUtil.validateOrgCode("12345");
        assert CWSUtil.validateOrgCode("123456");
    }

    public void shouldParseCorrectly() {
        String unparsedText;
        String correctText;

        /** TEST 1 **/
        unparsedText = "A CatInA Hat";
        correctText = "A Cat.\nIn.\nA Hat";
        assert correctText.equals(CWSUtil.insertLineBreaks(unparsedText, 1));
        /** TEST 2 **/
        unparsedText = "aCatin a hat";
        correctText = "a.\nCatin a hat";
        assert correctText.equals(CWSUtil.insertLineBreaks(unparsedText, 1));
        /** TEST 3 **/
        unparsedText = "A CatInA Hat";
        correctText = "A Cat.\n\nIn.\n\nA Hat";
        assert correctText.equals(CWSUtil.insertLineBreaks(unparsedText, 2));
        /** TEST 4 **/
        unparsedText = "A Cat..\nSitsIn A WonderfulHat.";
        correctText = "A Cat..\nSits.\nIn A Wonderful.\nHat.";
        assert correctText.equals(CWSUtil.insertLineBreaks(unparsedText, 1));
    }
}
