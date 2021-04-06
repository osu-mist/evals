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
}
