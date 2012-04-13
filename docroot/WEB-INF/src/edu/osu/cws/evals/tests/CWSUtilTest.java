package edu.osu.cws.evals.tests;

import edu.osu.cws.util.CWSUtil;
import org.testng.annotations.Test;

@Test
public class CWSUtilTest {

    public void shouldValidateNameSearch() {
        assert CWSUtil.validateNameSearch("") == false;
        assert CWSUtil.validateNameSearch(null) == false;

        assert CWSUtil.validateNameSearch("12345") == false;
        assert CWSUtil.validateNameSearch("Jo2e") == false;
        assert CWSUtil.validateNameSearch("Jose2") == false;

        assert CWSUtil.validateNameSearch("Clark");
        assert CWSUtil.validateNameSearch("James Bond");
        assert CWSUtil.validateNameSearch("James-Bond");
        assert CWSUtil.validateNameSearch("Bond, James");
    }
}
